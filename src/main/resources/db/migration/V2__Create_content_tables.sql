CREATE TABLE contents (
    id UUID PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    author_id UUID NOT NULL REFERENCES users(id),
    thumbnail_path VARCHAR(255),
    file_type VARCHAR(50) NOT NULL,
    file_path VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- Индексы для ускорения поиска
CREATE INDEX idx_contents_author_id ON contents(author_id);
CREATE INDEX idx_contents_file_type ON contents(file_type);
CREATE INDEX idx_contents_title ON contents(title);

