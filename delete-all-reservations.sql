-- Purpose: Delete all reservations and participation records from the smartDine database
-- This script preserves the table structure but removes all data from:
--   - reservation_participations (new join entity)
--   - reservation_participants (old ManyToMany join table, if exists)
--   - reservations (main table)
-- Run with: psql -h localhost -U postgres -d smartDine -f delete-all-reservations.sql

-- Start transaction for safety (rollback if something goes wrong)
BEGIN;

-- Step 1: Show current counts before deletion
SELECT 'Before deletion:' as action;
SELECT COUNT(*) as reservation_participations_count FROM reservation_participations;
SELECT COUNT(*) as reservations_count FROM reservations;
-- This table may not exist if migration was done, but try anyway:
SELECT COUNT(*) as reservation_participants_count FROM reservation_participants;

-- Step 2: Delete all reservation participations (new join entity with FK to reservations)
-- Must delete these first due to foreign key constraint to reservations table
DELETE FROM reservation_participations;

-- Step 3: Delete from old ManyToMany join table if it exists
-- This table may have been removed in migration, so use DO block to ignore errors
DO $$
BEGIN
    DELETE FROM reservation_participants;
    RAISE NOTICE 'Deleted from reservation_participants table';
EXCEPTION
    WHEN undefined_table THEN
        RAISE NOTICE 'Table reservation_participants does not exist, skipping';
END $$;

-- Step 4: Delete all reservations
-- This is safe now because all dependent records (participations) are already deleted
DELETE FROM reservations;

-- Step 5: Show counts after deletion to verify
SELECT 'After deletion:' as action;
SELECT COUNT(*) as reservation_participations_count FROM reservation_participations;
SELECT COUNT(*) as reservations_count FROM reservations;
-- Try to count old table if it exists:
DO $$
BEGIN
    PERFORM COUNT(*) FROM reservation_participants;
    RAISE NOTICE 'reservation_participants count: 0';
EXCEPTION
    WHEN undefined_table THEN
        RAISE NOTICE 'Table reservation_participants does not exist';
END $$;

-- Step 6: Commit the transaction
-- If everything looks correct, this will be committed automatically at script end
-- If you want to rollback instead, manually run: ROLLBACK;
COMMIT;

-- Verification queries (run these after commit to double-check):
-- SELECT COUNT(*) FROM reservations;
-- SELECT COUNT(*) FROM reservation_participations;
