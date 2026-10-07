
CREATE TABLE refresh_tokens (
    id                BIGSERIAL    PRIMARY KEY,
    user_id           UUID       NOT NULL,
    family_id         UUID         NOT NULL,
    token_hash        CHAR(64)     NOT NULL UNIQUE,
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT now(),
    family_started_at TIMESTAMPTZ  NOT NULL,
    expires_at        TIMESTAMPTZ  NOT NULL,
    revoked_at        TIMESTAMPTZ,
    revoke_reason     VARCHAR(20),
    replaced_at       TIMESTAMPTZ,
    user_agent        VARCHAR(200),
    ip                VARCHAR(50)
);

CREATE INDEX idx_refresh_tokens_family_id  ON refresh_tokens (family_id);
CREATE INDEX idx_refresh_tokens_user_id    ON refresh_tokens (user_id);
CREATE INDEX idx_refresh_tokens_expires_at ON refresh_tokens (expires_at);
