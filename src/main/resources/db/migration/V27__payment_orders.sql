-- Server-side payment orders. The price is computed by the server when the order is created and
-- access is granted only when the gateway's signed notification marks the order PAID.
CREATE TABLE IF NOT EXISTS payment_orders (
    id                BIGSERIAL PRIMARY KEY,
    order_id          VARCHAR(64)    NOT NULL,
    user_id           BIGINT         NOT NULL REFERENCES users(id),
    kind              VARCHAR(20)    NOT NULL,
    status            VARCHAR(20)    NOT NULL,
    amount            NUMERIC(10, 2) NOT NULL,
    currency          VARCHAR(3)     NOT NULL,
    bundle_ids        VARCHAR(1000),
    custom_bundle_id  BIGINT,
    paper_id          BIGINT,
    origin_bundle_id  BIGINT,
    attempts_count    INTEGER,
    gateway_payment_id VARCHAR(100),
    created_at        TIMESTAMP      NOT NULL DEFAULT now(),
    paid_at           TIMESTAMP,
    fulfilled_at      TIMESTAMP,
    CONSTRAINT uq_payment_orders_order_id UNIQUE (order_id),
    CONSTRAINT uq_payment_orders_gateway_payment UNIQUE (gateway_payment_id)
);

CREATE INDEX IF NOT EXISTS idx_payment_orders_user ON payment_orders (user_id, created_at DESC);
