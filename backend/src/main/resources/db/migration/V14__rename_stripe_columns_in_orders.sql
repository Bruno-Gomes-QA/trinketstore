-- Remove stripe_ prefix from orders table columns
ALTER TABLE orders RENAME COLUMN stripe_checkout_id TO checkout_id;
ALTER TABLE orders RENAME COLUMN stripe_payment_intent TO payment_intent;

