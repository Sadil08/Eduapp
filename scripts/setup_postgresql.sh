#!/bin/bash

# EduApp PostgreSQL Setup Script
# This script sets up PostgreSQL database and inserts sample data

echo "Setting up EduApp PostgreSQL Database..."

# Database configuration
DB_NAME="eduapp_db"
DB_USER="eduapp_user"
DB_PASSWORD="$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi"

# Check if PostgreSQL is installed
if ! command -v psql &> /dev/null; then
    echo "PostgreSQL is not installed. Please install PostgreSQL first."
    echo "Ubuntu/Debian: sudo apt install postgresql postgresql-contrib"
    echo "macOS: brew install postgresql"
    exit 1
fi

# Check if PostgreSQL is running
if ! pg_isready -q; then
    echo "PostgreSQL is not running. Starting PostgreSQL..."
    # Try to start PostgreSQL (works on Ubuntu/Debian)
    sudo systemctl start postgresql 2>/dev/null || brew services start postgresql 2>/dev/null || {
        echo "Could not start PostgreSQL automatically. Please start it manually."
        exit 1
    }
fi

echo "Creating database and user..."

# Create database and user
sudo -u postgres psql << EOF
-- Create database if it doesn't exist
SELECT 'CREATE DATABASE $DB_NAME'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = '$DB_NAME')\gexec

-- Create user and grant privileges
DO \$\$
BEGIN
   IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = '$DB_USER') THEN
      CREATE USER $DB_USER WITH PASSWORD '$DB_PASSWORD';
   END IF;
END
\$\$;

GRANT ALL PRIVILEGES ON DATABASE $DB_NAME TO $DB_USER;
EOF

echo "Database created successfully!"

echo "Inserting sample data..."
# Insert sample data
psql -U $DB_USER -d $DB_NAME -f 02_sample_data.sql

if [ $? -eq 0 ]; then
    echo "Sample data inserted successfully!"
    echo ""
    echo "Setup complete! You can now run the Spring Boot application."
    echo ""
    echo "Login credentials:"
    echo "Admin: admin@eduapp.com / password"
    echo "Student: student@eduapp.com / password"
else
    echo "Error inserting sample data. Please check the script and try again."
    exit 1
fi