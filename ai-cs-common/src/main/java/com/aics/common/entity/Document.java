package com.aics.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 知识库文档表
 */
@Data
@TableName("acs_document")
public class Document implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 所属应用 */
    private Long appId;

    /** 原始文件名 */
    private String fileName;

    /** 文件类型（pdf/docx/txt） */
    private String fileType;

    /** 文件大小（字节） */
    private Long fileSize;

    /** 存储路径 */
    private String filePath;

    /** 解析状态：0-待解析，1-解析中，2-已完成，3-失败 */
    private Integer parseStatus;

    /** 分块数量 */
    private Integer chunkCount;

    /** 解析失败原因 */
    private String errorMsg;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
