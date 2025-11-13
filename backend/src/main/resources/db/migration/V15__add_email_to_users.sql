-- Add email column to users table
ALTER TABLE users
ADD COLUMN email VARCHAR(255) UNIQUE;

-- Create index on email for better query performance
CREATE INDEX idx_users_email ON users(email);

