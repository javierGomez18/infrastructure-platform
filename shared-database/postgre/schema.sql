CREATE TABLE IF NOT EXISTS audit_events (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    microservice TEXT NOT NULL,
    action TEXT NOT NULL,
    entity TEXT NOT NULL,
    entityId TEXT,
    correlationId TEXT,
    payload TEXT,
    timestamp TEXT NOT NULL
);

