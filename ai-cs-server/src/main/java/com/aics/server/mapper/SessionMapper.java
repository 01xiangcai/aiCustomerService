package com.aics.server.mapper;

import com.aics.common.entity.Session;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会话 Mapper
 */
@Mapper
public interface SessionMapper extends BaseMapper<Session> {
}
