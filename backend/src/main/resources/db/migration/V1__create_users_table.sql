-- Create users table
CREATE TABLE users (
  id_user      BIGSERIAL PRIMARY KEY,
  auth_id      UUID NOT NULL UNIQUE,
  nome_user    VARCHAR(100) NOT NULL,
  role         TEXT NOT NULL CHECK (role IN ('admin','customer')),
  created_at   TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
  updated_at   TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

-- Create index on auth_id for better query performance
CREATE INDEX idx_users_auth_id ON users(auth_id);

-- Create index on role for filtering queries
CREATE INDEX idx_users_role ON users(role);
