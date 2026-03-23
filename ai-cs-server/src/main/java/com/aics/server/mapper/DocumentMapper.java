package com.aics.server.mapper;

import com.aics.common.entity.Document;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 知识库文档 Mapper
 */
@Mapper
public interface DocumentMapper extends BaseMapper<Document> {
}
