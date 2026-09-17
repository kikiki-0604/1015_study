CREATE TABLE IF NOT EXISTS cooking_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    dish_name VARCHAR(255) NOT NULL,
    recipe_url VARCHAR(1024) NOT NULL,
    cooked_on DATE NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_cooked_on (cooked_on)
);
