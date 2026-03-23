package com.aics.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 租户表
 */
@Data
@TableName("acs_tenant")
public class Tenant implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 租户名称 */
    private String tenantName;

    /** 联系邮箱（登录账号） */
    private String email;

    /** 登录密码（加密存储） */
    private String password;

    /** 手机号 */
    private String phone;

    /** 状态：0-禁用，1-正常 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
