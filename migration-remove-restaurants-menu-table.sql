-- Migration script to remove the unnecessary restaurants_menu join table
-- This script should be run manually on the PostgreSQL database after deploying the code changes
-- 
-- Context: MenuItem now has a direct @JoinColumn(name = "restaurant_id") foreign key,
-- so the restaurants_menu join table is no longer needed.
--
-- Run this script with: psql -h localhost -U postgres -d smartDine -f migration-remove-restaurants-menu-table.sql

-- Step 1: Verify the table exists (informational query)
-- SELECT table_name FROM information_schema.tables WHERE table_name = 'restaurants_menu';

-- Step 2: Drop the foreign key constraints on the join table
ALTER TABLE IF EXISTS restaurants_menu DROP CONSTRAINT IF EXISTS fk8al08rpj9q2x2cqal1blmjc91;
ALTER TABLE IF EXISTS restaurants_menu DROP CONSTRAINT IF EXISTS fkfqxe0huu6p1w47e52dj0u35ov;

-- Step 3: Drop the restaurants_menu table
DROP TABLE IF EXISTS restaurants_menu;

-- Step 4: Verify the menu_items table now has restaurant_id column (informational query)
-- SELECT column_name, data_type FROM information_schema.columns 
-- WHERE table_name = 'menu_items' AND column_name = 'restaurant_id';

-- Migration completed successfully
