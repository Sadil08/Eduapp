---
module: core
affects: []
files: ['TestDB.java', 'list_s3.py']
---

# Core Module

## Purpose
This module contains diagnostic and infrastructure-verification utilities for the `eduapp` project. It exists to validate connectivity to the PostgreSQL database and the Supabase S3-compatible object store, and is not part of the application's production runtime.

## Public interfaces
- `TestDB.main(String[] args)` — Opens a JDBC connection to the `eduapp` PostgreSQL database, executes a raw `SELECT` query against the `questions` table, and prints the last 5 rows (`id`, `image_url`, `requires_image_display`) to stdout.
- `list_s3.py` (top-level script) — Instantiates a boto3 S3 client pointed at a Supabase S3 endpoint and lists all object keys in the `eduapp-images` bucket, printing each `Key` to stdout.

## Data flow
- **TestDB.java**: Credentials and JDBC URL are hardcoded (`jdbc:postgresql://localhost:5432/eduapp`, user `postgres`); query results flow from PostgreSQL → `ResultSet` → stdout.
- **list_s3.py**: Credentials (`SUPABASE_S3_ENDPOINT`, `SUPABASE_S3_ACCESS_KEY`, `SUPABASE_S3_SECRET_KEY`) are read from a `.env` file via `dotenv.load_dotenv`; object metadata flows from Supabase S3 → `response['Contents']` → stdout.

## Architecture principles
- Both scripts are standalone entry points with no shared abstractions or reusable classes/functions.
- All error handling is limited to bare `try/except` or `try/catch` blocks that print the exception and exit — no retries, no structured logging.
- `TestDB.java` uses a hardcoded password and does not use a connection pool or prepared statements.
- `list_s3.py` uses `region_name='auto'`, indicating reliance on Supabase's S3-compatible API rather than native AWS S3.

## Dependencies
- Internal: none
- External: `java.sql` (JDBC, standard library); `postgresql` JDBC driver (implicit, must be on classpath); `boto3` (Python AWS SDK); `python-dotenv` (`dotenv.load_dotenv`)

## Known gaps / TODOs
- `TestDB.java` contains hardcoded credentials (`"postgres"` / `"password"`), which must be externalised before any non-local use.
- `conn.close()` in `TestDB.java` is not in a `finally` block or try-with-resources, so the connection leaks on exception.
- `list_s3.py` does not handle S3 pagination — `list_objects_v2` returns at most 1 000 keys; large buckets will be silently truncated.
- Neither utility has tests, parameterisation, or integration with the broader application build system.