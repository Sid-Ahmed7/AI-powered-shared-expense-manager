CREATE TABLE password_reset_tokens (
    id           UUID         PRIMARY KEY,
    account_id   UUID         NOT NULL,
    token_hash   CHAR(64)     NOT NULL UNIQUE,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    expires_at   TIMESTAMPTZ  NOT NULL,
    used_at      TIMESTAMPTZ
);

CREATE INDEX idx_prt_account_id ON password_reset_tokens (account_id);
CREATE INDEX idx_prt_expires_at ON password_reset_tokens (expires_at);