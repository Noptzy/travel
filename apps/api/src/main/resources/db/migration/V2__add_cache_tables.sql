CREATE TABLE api_cache (
    cache_key VARCHAR(512) PRIMARY KEY,
    provider VARCHAR(64) NOT NULL,
    payload JSON NOT NULL,
    expires_at TIMESTAMP(6) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_api_cache_expires_at ON api_cache(expires_at);
