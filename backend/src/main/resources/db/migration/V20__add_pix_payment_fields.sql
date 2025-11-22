-- Expand PIX storage on orders
ALTER TABLE orders
    ALTER COLUMN pickup_qr_token TYPE TEXT;

ALTER TABLE orders
    ADD COLUMN IF NOT EXISTS pix_qr_code_base64 TEXT,
    ADD COLUMN IF NOT EXISTS pix_expires_at TIMESTAMPTZ;
