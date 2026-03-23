-- =============================================
-- AI 客服中间件 · PGVector 初始化脚本
-- 数据库: PostgreSQL + PGVector 扩展
-- =============================================

-- 启用 PGVector 扩展
CREATE EXTENSION IF NOT EXISTS vector;

-- 文档分块向量表
CREATE TABLE IF NOT EXISTS acs_document_chunk (
    id BIGSERIAL PRIMARY KEY,
    document_id BIGINT NOT NULL,
    app_id BIGINT NOT NULL,
    chunk_index INT NOT NULL DEFAULT 0,
    content TEXT NOT NULL,
    embedding vector(1536),  -- OpenAI/DeepSeek 默认 1536 维
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 索引
CREATE INDEX IF NOT EXISTS idx_chunk_app_id ON acs_document_chunk(app_id);
CREATE INDEX IF NOT EXISTS idx_chunk_document_id ON acs_document_chunk(document_id);

-- 向量索引（IVFFlat，加速相似度检索）
-- 注意：需要至少有一定数据量后才能创建 IVFFlat 索引
-- 数据量较少时可先使用精确检索
-- CREATE INDEX IF NOT EXISTS idx_chunk_embedding ON acs_document_chunk
--     USING ivfflat (embedding vector_cosine_ops) WITH (lists = 100);
