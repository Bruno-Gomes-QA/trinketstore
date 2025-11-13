-- Remove a coluna stripe_price_id da tabela prices
ALTER TABLE prices DROP COLUMN IF EXISTS stripe_price_id;

