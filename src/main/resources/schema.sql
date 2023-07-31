CREATE table message (
    message_id VARCHAR PRIMARY KEY,
    client_id BIGINT,
    time TIMESTAMP,
    message VARCHAR,
    is_client BOOLEAN
);