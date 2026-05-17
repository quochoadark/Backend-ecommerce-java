-- Add index on categories.name for faster lookup
CREATE INDEX idx_categories_name ON categories(name);
