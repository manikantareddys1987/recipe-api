-- Database initialization script for Recipe API
-- This script runs automatically when PostgreSQL container starts

-- Create database user if not exists (already created by docker-compose)
-- CREATE USER recipeuser WITH PASSWORD 'recipepass123';

-- Create extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Create recipe table
CREATE TABLE IF NOT EXISTS recipes (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    instructions TEXT NOT NULL,
    type VARCHAR(50) NOT NULL CHECK (type IN ('VEGETARIAN', 'NON_VEGETARIAN')),
    number_of_servings INTEGER NOT NULL CHECK (number_of_servings > 0),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create ingredients table
CREATE TABLE IF NOT EXISTS ingredients (
    id SERIAL PRIMARY KEY,
    ingredient VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create recipe_ingredient join table
CREATE TABLE IF NOT EXISTS recipe_ingredient (
    recipe_id INTEGER NOT NULL REFERENCES recipes(id) ON DELETE CASCADE,
    ingredient_id INTEGER NOT NULL REFERENCES ingredients(id) ON DELETE CASCADE,
    PRIMARY KEY (recipe_id, ingredient_id)
);

-- Create indices for performance
CREATE INDEX IF NOT EXISTS idx_recipe_name ON recipes(name);
CREATE INDEX IF NOT EXISTS idx_recipe_type ON recipes(type);
CREATE INDEX IF NOT EXISTS idx_recipe_servings ON recipes(number_of_servings);
CREATE INDEX IF NOT EXISTS idx_ingredient_name ON ingredients(ingredient);
CREATE INDEX IF NOT EXISTS idx_recipe_ingredient_recipe ON recipe_ingredient(recipe_id);
CREATE INDEX IF NOT EXISTS idx_recipe_ingredient_ingredient ON recipe_ingredient(ingredient_id);

-- Grant permissions to recipeuser
GRANT ALL PRIVILEGES ON DATABASE recipedb TO recipeuser;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO recipeuser;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO recipeuser;

-- Insert sample data
INSERT INTO recipes (name, instructions, type, number_of_servings)
VALUES
    ('Pasta Carbonara', 'Boil pasta. Fry bacon. Mix eggs and cheese. Combine all.', 'NON_VEGETARIAN', 4),
    ('Vegetable Stir Fry', 'Chop vegetables. Heat oil. Stir fry until tender. Add soy sauce.', 'VEGETARIAN', 2),
    ('Salmon with Oven Roasted Potatoes', 'Prepare salmon. Slice potatoes. Bake at 400F for 20 minutes.', 'NON_VEGETARIAN', 3)
ON CONFLICT DO NOTHING;

INSERT INTO ingredients (ingredient)
VALUES
    ('pasta'),
    ('bacon'),
    ('eggs'),
    ('cheese'),
    ('potatoes'),
    ('salmon'),
    ('broccoli'),
    ('carrots'),
    ('soy sauce'),
    ('oil')
ON CONFLICT DO NOTHING;

-- Link recipes with ingredients
INSERT INTO recipe_ingredient (recipe_id, ingredient_id)
SELECT r.id, i.id FROM recipes r, ingredients i
WHERE (r.name = 'Pasta Carbonara' AND i.ingredient IN ('pasta', 'bacon', 'eggs', 'cheese'))
   OR (r.name = 'Vegetable Stir Fry' AND i.ingredient IN ('broccoli', 'carrots', 'oil', 'soy sauce'))
   OR (r.name = 'Salmon with Oven Roasted Potatoes' AND i.ingredient IN ('salmon', 'potatoes', 'oil'))
ON CONFLICT DO NOTHING;

