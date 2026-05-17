-- Add indexes for products table
CREATE INDEX idx_products_category ON products(category_id);
CREATE INDEX idx_products_price ON products(price);

-- Add stock_quantity column
ALTER TABLE products ADD COLUMN stock_quantity INT NOT NULL DEFAULT 0;
