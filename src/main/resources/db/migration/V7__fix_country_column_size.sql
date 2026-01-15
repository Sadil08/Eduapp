-- Fix country column size to accommodate full country names from geo-location service
-- The ip-api.com service returns full country names, not ISO codes

ALTER TABLE users
ALTER COLUMN country TYPE VARCHAR(100);
