-- Make email nullable
ALTER TABLE users ALTER COLUMN email DROP NOT NULL;
  
-- Add phone_number column
ALTER TABLE users ADD COLUMN phone_number VARCHAR(15) UNIQUE;
  
-- Remove unnecessary columns
ALTER TABLE users 
  DROP COLUMN username,
  DROP COLUMN password_hash,
  DROP COLUMN is_active;
    
-- Rename first_name to name and drop last_name
ALTER TABLE users RENAME COLUMN first_name TO name;
ALTER TABLE users DROP COLUMN last_name;
  
-- Add index for phone number
CREATE INDEX idx_users_phone_number ON users(phone_number);
