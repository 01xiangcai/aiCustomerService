package com.aics.server.service;

import com.aics.common.entity.App;
import com.aics.common.entity.Document;
import com.aics.common.exception.BizException;
import com.aics.common.result.PageResult;
import com.aics.server.mapper.DocumentMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

/**
 * 知识库服务
 * 负责文档上传、解析、分块存储
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeService {

    private final DocumentMapper documentMapper;
    private final VectorStoreService vectorStoreService;
    private final AppService appService;

    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

    /** 每个分块的最大字符数 */
    private static final int CHUNK_SIZE = 500;
    /** 分块之间的重叠字符数 */
    private static final int CHUNK_OVERLAP = 50;

    /**
     * 上传文档
     *
     * @param tenantId 租户 ID
     * @param appId    应用 ID
     * @param file     上传文件
     * @return 文档记录
     */
    public Document uploadDocument(Long tenantId, Long appId, MultipartFile file) {
        // 验证应用归属
        App app = appService.getApp(tenantId, appId);

        // 验证文件
        String originalName = file.getOriginalFilename();
        if (originalName == null || originalName.isBlank()) {
            throw BizException.badRequest("文件名不能为空");
        }

        String fileType = getFileExtension(originalName);
        List<String> allowedTypes = List.of("pdf", "txt", "doc", "docx", "md");
        if (!allowedTypes.contains(fileType.toLowerCase())) {
            throw BizException.badRequest("不支持的文件格式，仅支持: " + String.join(", ", allowedTypes));
        }

        // 保存文件到本地
        String savedPath = saveFile(file, appId);

        // 创建文档记录
        Document doc = new Document();
        doc.setAppId(appId);
        doc.setFileName(originalName);
        doc.setFileType(fileType);
        doc.setFileSize(file.getSize());
        doc.setFilePath(savedPath);
        doc.setParseStatus(0); // 待解析
        doc.setChunkCount(0);
        documentMapper.insert(doc);

        // 异步解析文档
        parseDocumentAsync(doc);

        return doc;
    }

    /**
     * 查询文档列表
     */
    public PageResult<Document> listDocuments(Long tenantId, Long appId, int page, int size) {
        appService.getApp(tenantId, appId); // 验证权限

        Page<Document> pageParam = new Page<>(page, size);
        Page<Document> result = documentMapper.selectPage(pageParam,
                new LambdaQueryWrapper<Document>()
                        .eq(Document::getAppId, appId)
                        .orderByDesc(Document::getCreatedAt));
        return new PageResult<>(result.getTotal(), result.getRecords(),
                result.getCurrent(), result.getSize());
    }

    /**
     * 删除文档
     */
    public void deleteDocument(Long tenantId, Long appId, Long docId) {
        appService.getApp(tenantId, appId); // 验证权限

        Document doc = documentMapper.selectById(docId);
        if (doc == null || !doc.getAppId().equals(appId)) {
            throw BizException.notFound("文档不存在");
        }

        // 删除向量数据
        vectorStoreService.deleteByDocumentId(docId);

        // 删除文件
        try {
            Files.deleteIfExists(Paths.get(doc.getFilePath()));
        } catch (IOException e) {
            log.warn("删除文件失败: {}", doc.getFilePath(), e);
        }

        // 删除记录
        documentMapper.deleteById(docId);
    }

    /**
     * 异步解析文档
     */
    @Async
    public void parseDocumentAsync(Document doc) {
        try {
            // 更新状态为解析中
            doc.setParseStatus(1);
            documentMapper.updateById(doc);

            // 使用 Tika 解析文档内容
            Tika tika = new Tika();
            String content;
            try (InputStream is = Files.newInputStream(Paths.get(doc.getFilePath()))) {
                content = tika.parseToString(is);
            }

            if (content == null || content.isBlank()) {
                doc.setParseStatus(3);
                doc.setErrorMsg("文档内容为空");
                documentMapper.updateById(doc);
                return;
            }

            // 文本分块
            List<String> chunks = splitIntoChunks(content);

            // 存入向量数据库
            vectorStoreService.storeChunks(doc.getId(), doc.getAppId(), chunks);

            // 更新文档状态
            doc.setParseStatus(2);
            doc.setChunkCount(chunks.size());
            documentMapper.updateById(doc);

            log.info("文档解析完成: {} → {} 个分块", doc.getFileName(), chunks.size());

        } catch (Exception e) {
            log.error("文档解析失败: {}", doc.getFileName(), e);
            doc.setParseStatus(3);
            doc.setErrorMsg(e.getMessage());
            documentMapper.updateById(doc);
        }
    }

    /**
     * 文本分块（固定大小 + 重叠）
     */
    private List<String> splitIntoChunks(String text) {
        List<String> chunks = new java.util.ArrayList<>();
        // 先按段落分割
        String[] paragraphs = text.split("\\n\\s*\\n");
        StringBuilder currentChunk = new StringBuilder();

        for (String paragraph : paragraphs) {
            String trimmed = paragraph.trim();
            if (trimmed.isEmpty())
                continue;

            if (currentChunk.length() + trimmed.length() > CHUNK_SIZE && !currentChunk.isEmpty()) {
                chunks.add(currentChunk.toString().trim());
                // 保留重叠部分
                String overlap = currentChunk.substring(
                        Math.max(0, currentChunk.length() - CHUNK_OVERLAP));
                currentChunk = new StringBuilder(overlap);
            }
            currentChunk.append(trimmed).append("\n");
        }

        if (!currentChunk.isEmpty()) {
            chunks.add(currentChunk.toString().trim());
        }

        // 如果整个文档没有段落分隔，按固定大小切
        if (chunks.isEmpty() && !text.trim().isEmpty()) {
            for (int i = 0; i < text.length(); i += CHUNK_SIZE - CHUNK_OVERLAP) {
                int end = Math.min(i + CHUNK_SIZE, text.length());
                chunks.add(text.substring(i, end).trim());
            }
        }

        return chunks;
    }

    /**
     * 保存文件到本地
     */
    private String saveFile(MultipartFile file, Long appId) {
        try {
            Path dirPath = Paths.get(uploadDir, String.valueOf(appId));
            Files.createDirectories(dirPath);

            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path filePath = dirPath.resolve(fileName);
            file.transferTo(filePath.toFile());

            return filePath.toString();
        } catch (IOException e) {
            throw new RuntimeException("文件保存失败", e);
        }
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String fileName) {
        int idx = fileName.lastIndexOf('.');
        return idx > 0 ? fileName.substring(idx + 1) : "";
    }
}
