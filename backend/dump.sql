-- Chaotic Tower - Database Schema & Data Seed Dump
-- Target Database Engine: PostgreSQL
-- Database Name: chaotictower

-- -----------------------------------------------------
-- Table Structure: players
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS players (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NULL
);

-- -----------------------------------------------------
-- Table Structure: achievements
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS achievements (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description VARCHAR(255) NOT NULL
);

-- -----------------------------------------------------
-- Table Structure: leaderboard
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS leaderboard (
    id BIGSERIAL PRIMARY KEY,
    player_id BIGINT NOT NULL,
    game_mode VARCHAR(255) NOT NULL,
    score INTEGER NULL,
    time_record DOUBLE PRECISION NULL,
    max_height DOUBLE PRECISION NULL,
    CONSTRAINT fk_leaderboard_player FOREIGN KEY (player_id)
        REFERENCES players (id) ON DELETE CASCADE
);

-- -----------------------------------------------------
-- Table Structure: player_achievements
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS player_achievements (
    id BIGSERIAL PRIMARY KEY,
    player_id BIGINT NOT NULL,
    achievement_id BIGINT NOT NULL,
    unlocked_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_player_achievements_player FOREIGN KEY (player_id)
        REFERENCES players (id) ON DELETE CASCADE,
    CONSTRAINT fk_player_achievements_achievement FOREIGN KEY (achievement_id)
        REFERENCES achievements (id) ON DELETE CASCADE
);

-- -----------------------------------------------------
-- Indexes for Optimized Query Performance
-- -----------------------------------------------------
CREATE INDEX IF NOT EXISTS idx_leaderboard_game_mode ON leaderboard (game_mode);
CREATE INDEX IF NOT EXISTS idx_leaderboard_score ON leaderboard (score);
CREATE INDEX IF NOT EXISTS idx_leaderboard_time ON leaderboard (time_record);
CREATE INDEX IF NOT EXISTS idx_player_achievements_user ON player_achievements (player_id);

-- -----------------------------------------------------
-- Seed Mock Data
-- -----------------------------------------------------

-- Seed Player Accounts
-- (User 'alfa' is seeded with standard password 'alfa')
INSERT INTO players (id, username, password) VALUES
(1, 'alfa', 'alfa'),
(2, 'kirito', 'starburst'),
(3, 'asuna', 'lambent'),
(4, 'subaru', 'rembestgirl')
ON CONFLICT (id) DO NOTHING;

-- Reset PK Serial Sequence for players table
SELECT setval('players_id_seq', COALESCE((SELECT MAX(id)+1 FROM players), 1), false);

-- Seed Standard Achievements
INSERT INTO achievements (id, name, description) VALUES
(1, 'First Ascent', 'Unlock your first magic spell by reaching 4.0 meters.'),
(2, 'Solid Foundation', 'Successfully petrify a block using the Cement Spell.'),
(3, 'Tethered Together', 'Weld two adjacent blocks with the Ivy Spell.'),
(4, 'Speed Demon', 'Reach the 20m checkered finish line in Race Mode in under 30 seconds.'),
(5, 'Dense Stacking', 'Place 25 blocks below the laser line in Puzzle Mode.'),
(6, 'Towering Above', 'Build a tower of 12.0 meters height or higher in Survival Mode.')
ON CONFLICT (id) DO NOTHING;

-- Reset PK Serial Sequence for achievements table
SELECT setval('achievements_id_seq', COALESCE((SELECT MAX(id)+1 FROM achievements), 1), false);

-- Seed Competitive Leaderboard Records
INSERT INTO leaderboard (id, player_id, game_mode, score, time_record, max_height) VALUES
-- SURVIVAL MODE (Higher score/height is better)
(1, 1, 'SURVIVAL', 150, 48.5, 12.4),
(2, 2, 'SURVIVAL', 180, 52.1, 14.8),
(3, 3, 'SURVIVAL', 110, 31.4, 9.2),
(4, 4, 'SURVIVAL', 80, 24.8, 6.5),

-- PUZZLE MODE (Higher dense score/blocks count is better)
(5, 2, 'PUZZLE', 250, 68.2, 7.5),
(6, 1, 'PUZZLE', 220, 59.4, 6.8),
(7, 3, 'PUZZLE', 190, 50.1, 6.1),
(8, 4, 'PUZZLE', 120, 39.8, 4.2),

-- RACE MODE (Lower millisecond score/time is better)
(9, 2, 'RACE', 24120, 24.12, 20.0),
(10, 3, 'RACE', 27850, 27.85, 20.0),
(11, 1, 'RACE', 31440, 31.44, 20.0),
(12, 4, 'RACE', 42900, 42.90, 20.0)
ON CONFLICT (id) DO NOTHING;

-- Reset PK Serial Sequence for leaderboard table
SELECT setval('leaderboard_id_seq', COALESCE((SELECT MAX(id)+1 FROM leaderboard), 1), false);

-- Seed Some Initial Player Achievement Unlocks
INSERT INTO player_achievements (id, player_id, achievement_id, unlocked_at) VALUES
(1, 1, 1, CURRENT_TIMESTAMP - INTERVAL '3 days'),
(2, 1, 2, CURRENT_TIMESTAMP - INTERVAL '2 days'),
(3, 2, 4, CURRENT_TIMESTAMP - INTERVAL '1 day'),
(4, 2, 6, CURRENT_TIMESTAMP - INTERVAL '12 hours'),
(5, 3, 3, CURRENT_TIMESTAMP - INTERVAL '6 hours')
ON CONFLICT (id) DO NOTHING;

-- Reset PK Serial Sequence for player_achievements table
SELECT setval('player_achievements_id_seq', COALESCE((SELECT MAX(id)+1 FROM player_achievements), 1), false);
