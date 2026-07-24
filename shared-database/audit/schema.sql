CREATE TABLE IF NOT EXISTS audit_events (
    id SERIAL PRIMARY KEY,
    microservice VARCHAR(255) NOT NULL,
    action VARCHAR(255) NOT NULL,
    entity VARCHAR(255) NOT NULL,
    entity_id VARCHAR(255),
    correlation_id VARCHAR(255),
    payload JSONB,
    timestamp TIMESTAMP NOT NULL
    );


