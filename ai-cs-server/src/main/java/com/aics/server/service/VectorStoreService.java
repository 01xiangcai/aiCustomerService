package com.aics.server.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 向量存储服务
 * 使用 PGVector 存储和检索文档分块的向量嵌入
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VectorStoreService {

    private final JdbcTemplate pgJdbcTemplate;
    private final EmbeddingModel embeddingModel;

    /**
     * 存储文档分块（向量化后存入 PGVector）
     *
     * @param documentId 文档 ID
     * @param appId      应用 ID
     * @param chunks     文本分块列表
     */
    public void storeChunks(Long documentId, Long appId, List<String> chunks) {
        for (int i = 0; i < chunks.size(); i++) {
            String chunk = chunks.get(i);
            try {
                // 文本转向量
                float[] embedding = embeddingModel.embed(chunk);
                String vectorStr = toVectorString(embedding);

                pgJdbcTemplate.update(
                        "INSERT INTO acs_document_chunk (document_id, app_id, chunk_index, content, embedding) VALUES (?, ?, ?, ?, ?::vector)",
                        documentId, appId, i, chunk, vectorStr);
            } catch (Exception e) {
                log.warn("分块 {} 向量化失败，跳过: {}", i, e.getMessage());
            }
        }
        log.info("已存储 {} 个分块到向量数据库, documentId={}", chunks.size(), documentId);
    }

    /**
     * 相似度检索（RAG 核心）
     *
     * @param appId     应用 ID
     * @param query     用户查询文本
     * @param topK      返回最相似的 K 条
     * @param threshold 最低相似度阈值（0-1）
     * @return 匹配的文档分块内容列表
     */
    public List<String> searchSimilar(Long appId, String query, int topK, double threshold) {
        try {
            // 查询文本转向量
            float[] queryEmbedding = embeddingModel.embed(query);
            String vectorStr = toVectorString(queryEmbedding);

            // PGVector 余弦相似度检索
            String sql = """
                    SELECT content, 1 - (embedding <=> ?::vector) AS similarity
                    FROM acs_document_chunk
                    WHERE app_id = ?
                      AND 1 - (embedding <=> ?::vector) > ?
                    ORDER BY embedding <=> ?::vector
                    LIMIT ?
                    """;

            return pgJdbcTemplate.query(sql,
                    (rs, rowNum) -> {
                        String content = rs.getString("content");
                        double similarity = rs.getDouble("similarity");
                        log.info("RAG 命中片段 (similarity: {}): {}", similarity, content.length() > 50 ? content.substring(0, 50) + "..." : content);
                        return content;
                    },
                    vectorStr, appId, vectorStr, threshold, vectorStr, topK);
        } catch (Exception e) {
            log.error("向量检索失败", e);
            return List.of();
        }
    }

    /**
     * 删除文档的所有向量数据
     */
    public void deleteByDocumentId(Long documentId) {
        pgJdbcTemplate.update("DELETE FROM acs_document_chunk WHERE document_id = ?", documentId);
    }

    /**
     * 将 float 数组转为 PGVector 格式字符串
     */
    private String toVectorString(float[] embedding) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < embedding.length; i++) {
            if (i > 0)
                sb.append(",");
            sb.append(embedding[i]);
        }
        sb.append("]");
        return sb.toString();
    }
}
