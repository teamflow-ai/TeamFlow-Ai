-- Each service owns its own schema. They share a MySQL instance for local
-- convenience only; nothing reads across schema boundaries, so they can be
-- split onto separate servers in production without code changes.
CREATE DATABASE IF NOT EXISTS teamflow_identity CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS teamflow_project  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
