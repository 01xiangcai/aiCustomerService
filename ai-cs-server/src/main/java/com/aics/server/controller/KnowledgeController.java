package com.aics.server.controller;

import com.aics.common.entity.Document;
import com.aics.common.result.PageResult;
import com.aics.common.result.R;
import com.aics.server.service.KnowledgeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 知识库管理接口（需要登录）
 */
@RestController
@RequestMapping("/api/knowledge/{appId}")
@RequiredArgsConstructor
public class KnowledgeController {

    private final KnowledgeService knowledgeService;

    /**
     * 上传文档
     */
    @PostMapping("/upload")
    public R<Document> upload(@PathVariable("appId") Long appId,
            @RequestParam("file") MultipartFile file,
            Authentication auth) {
        Long tenantId = (Long) auth.getPrincipal();
        return R.ok(knowledgeService.uploadDocument(tenantId, appId, file));
    }

    /**
     * 文档列表
     */
    @GetMapping("/documents")
    public R<PageResult<Document>> listDocuments(
            @PathVariable("appId") Long appId,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            Authentication auth) {
        Long tenantId = (Long) auth.getPrincipal();
        return R.ok(knowledgeService.listDocuments(tenantId, appId, page, size));
    }

    /**
     * 删除文档
     */
    @DeleteMapping("/documents/{docId}")
    public R<Void> deleteDocument(@PathVariable("appId") Long appId,
            @PathVariable("docId") Long docId,
            Authentication auth) {
        Long tenantId = (Long) auth.getPrincipal();
        knowledgeService.deleteDocument(tenantId, appId, docId);
        return R.ok();
    }
}
