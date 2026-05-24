# Chaotic Tower — Project Knowledge Base

## Quick Facts
- **Repo:** `d:\programming\Java\chaoticTower` (monorepo: `frontend/` + `backend/`)
- **Language:** Java 17
- **Frontend:** LibGDX (LWJGL3 Desktop), Box2D physics, FreeType fonts
- **Backend:** Spring Boot 3.x, PostgreSQL, Spring Data JPA
- **Build:** Gradle (Groovy) — separate builds for frontend & backend
- **Resolution:** 1920×1080
- **Progress:** ~95%

---

## Architecture Overview

```
chaoticTower/
├── frontend/                    # LibGDX game client (Gradle project)
│   ├── core/                    # Platform-independent game code
│   │   └── src/main/java/com/alfa/chaotictower/
│   │       ├── Main.java                    # extends Game, SpriteBatch lifecycle
│   │       ├── GameAssetManager.java        # Singleton, loads 4 font sizes from pixel.ttf
│   │       ├── command/                     # Command Pattern (#8)
│   │       │   ├── InputCommand.java        # Interface: execute(Block)
│   │       │   ├── InputHandler.java        # Maps keys→commands, DAS/ARR auto-repeat
│   │       │   ├── CollisionUtils.java      # Static AABB vertex overlap checking
│   │       │   ├── MoveLeftCommand.java     # Shifts block -0.5 units (overlap-guarded)
│   │       │   ├── MoveRightCommand.java    # Shifts block +0.5 units (overlap-guarded)
│   │       │   ├── RotateCommand.java       # Rotates 90° + wall kick (±0.5, ±1.0, overlap-guarded)
│   │       │   └── SoftDropCommand.java     # Fast drop on hold + spell multiplier
│   │       ├── entity/
│   │       │   ├── Block.java               # Poolable, composite Box2D body, scale & spell flags
│   │       │   └── Player.java              # Lives, score, maxHeight, spell effect flags
│   │       ├── factory/
│   │       │   └── BlockFactory.java        # Singleton + Pool<Block>, 7-bag randomizer
│   │       ├── magic/                       # Magic Spell System (2P restricted, 15s cooldown, mystery grant)
│   │       │   ├── Spell.java               # Abstract base (name, light/dark, duration)
│   │       │   ├── SpellContext.java         # Context (caster, target, world, blocks)
│   │       │   ├── SpellManager.java         # Grant, cast, track active effects
│   │       │   ├── light/                    # Light magic (self-buff)
│   │       │   │   ├── CementSpell.java      # Last block → immovable static body
│   │       │   │   ├── IvySpell.java         # WeldJoint top 2 blocks together
│   │       │   │   └── LightningSpell.java   # Destroy last placed block
│   │       │   └── dark/                     # Dark magic (opponent-debuff)
│   │       │       ├── FrostSpell.java       # Near-zero friction (0.02f) & ice-blue tint for 15s
│   │       │       ├── WeightSpell.java      # Giant blocks (2.0 scale, 4x mass & size) for 10s
│   │       │       └── SpeedUpSpell.java     # 3× fall speed for 10s
│   │       ├── network/
│   │       │   └── ApiClient.java           # HTTP client (login, register, submitScore, getTop10)
│   │       ├── screen/
│   │       │   ├── MainMenuScreen.java      # Login & Register (Double-field keyboard focus & mouse UI)
│   │       │   ├── ModeSelectScreen.java    # 1P/2P + mode selection (widened/centered card UI, mouse hover/click support)
│   │       │   ├── PlayingScreen.java       # Main game loop + rendering
│   │       │   └── GameOverScreen.java      # Results + score submission
│   │       └── strategy/                    # Strategy Pattern (#7)
│   │           ├── GameModeStrategy.java     # Interface
│   │           ├── SurvivalStrategy.java     # SP: survive, MP: last standing
│   │           ├── RaceStrategy.java         # MP: first to 20m wins (infinite lives, specialized race progress HUD)
│   │           ├── TimeAttackStrategy.java   # SP: reach 20m in 2 min
│   │           └── PuzzleStrategy.java       # Stack below laser, floor penalty
│   │   └── lwjgl3/                  # Desktop launcher
│   │       └── Lwjgl3Launcher.java  # Window config (1920×1080)
│   └── assets/
│       └── pixel.ttf            # Game font
│
│── backend/chaoticTower-server/ # Spring Boot REST API
│   └── src/main/java/com/alfa/backend/
│       ├── config/WebConfig.java
│       ├── controller/          # PlayerController (login, register), LeaderboardController, AchievementController
│       ├── entity/              # Player (with password field), Leaderboard, Achievement, PlayerAchievement (JPA)
│       ├── repository/          # JPA repositories
│       └── service/             # PlayerService (with seeded 'alfa' account), LeaderboardService, AchievementService
```

---

## 9 Design Patterns Implemented

| # | Pattern | Where |
|---|---------|-------|
| 1 | Game Loop | `render()` in LibGDX |
| 2 | Singleton | `GameAssetManager`, `BlockFactory` |
| 3 | State | Screen transitions (MainMenu→ModeSelect→Playing→GameOver) |
| 4 | Factory | `BlockFactory.spawnBlock()` — 7 tetromino types, 7-bag randomizer |
| 5 | Object Pool | `Pool<Block>` in `BlockFactory` |
| 6 | Observer | Box2D `ContactListener` (landing, friction) |
| 7 | Strategy | `GameModeStrategy` interface → Survival/Race/TimeAttack/Puzzle |
| 8 | Command | `InputCommand` interface → MoveLeft/MoveRight/Rotate/SoftDrop |
| 9 | Repository | Spring Data JPA repositories |

---

## Critical Box2D Physics & Environment Notes

### Block Lifecycle & Movement Security
1. **Controlled:** `gravityScale=0`, `fixedRotation=true`, falls at normal constant velocity (`-0.7f`) or soft drop fast velocity (`-6.0f`).
   - *Grid Drift Prevention:* Active controlled blocks have their horizontal positions tracked and restored across the `world.step(...)` boundaries, with their horizontal linear velocity cleared to `0`. This keeps blocks perfectly locked to vertical grid columns during descent.
   - *Overlap-Guarded Movement:* Horizontal movements (`MoveLeftCommand`/`MoveRightCommand`) and rotations (`RotateCommand`) perform a predictive transform translation and check vertex overlaps via `CollisionUtils.hasOverlap()`. If a physical overlap occurs, the transform is instantly rolled back, preventing block penetration into walls or other blocks.
2. **Settling:** Triggered by `beginContact` when bottom normal > 0.4. Added to `blocksToSettle` queue (NEVER modify physics inside callbacks!)
3. **Settled:** Snapped to nearest grid increment dynamically based on local transformed tile positions at 90-degree rotations. Odd-width blocks snap to integer grid lines `N * grid`, while even-width blocks snap to grid half-lines `N * grid + grid / 2f`. Once settled: `gravityScale=1.2`, `fixedRotation=false`, `angularDamping=8.0` (high damping for high solidity), `linearDamping=0.8` (realistic crisp fall inertia), `friction=0.95` (extreme surface grip), `restitution=0.0` (no elastic bounciness).
4. **Out-of-bounds:** If `y < -2f`, destroy body → free to pool → player loses life (or floor penalty in Puzzle mode).

### Known Box2D Gotchas
- **NEVER** call `world.destroyBody()` or change body type inside `beginContact`/`preSolve` → use deferred queue.
- **NEVER** return a Block to pool before `world.destroyBody(block.body)` → stale pointer crash.
- Hitbox is `TILE_SIZE/2 - 0.01f` to prevent ghost collisions between adjacent tiles.
- `preSolve` overrides contact friction to 0 for side-wall contacts on active controlled blocks to allow sliding into gaps, while preserving the custom low friction overrides (`0.02f`) on frosted blocks.

### Environment Layout
- **1P:** Viewport 20×30, pedestal at (10,2), walls at x=2.5 and x=17.5.
- **2P:** Viewport 40×30, pedestals at (10,2) and (30,2), divider at x=20, walls at 2.5/17.5/22.5/37.5.

---

## Rendering System (No Textures)

All visuals are procedural using `ShapeRenderer`:
- Blocks: 7 colors (O=yellow, I=cyan, T=purple, L=orange, J=blue, S=green, Z=red).
- Each tile: filled rect + outline + semi-transparent highlight strip.
- Scale-aware sizes: Weighted blocks dynamically scale visual tile sizes and highlight offsets by a scale of `2.0f`.
- Spell visual overrides: cemented=grey tint, ivied=green tint, frosted=semi-transparent ice-blue `(0.5f, 0.8f, 1f, 0.85f)` tint.
- Controlled blocks have pulsing brightness effect.
- Background: vertex-colored gradient rect.
- Grid: semi-transparent lines at 1-unit intervals.
- Target line (Race/TimeAttack): glowing yellow line at pedestal_top + 20m.
- Puzzle laser line: pulsing red/orange at adjustable height.
- Next block preview: mini tiles centered mathematically using centroids drawn in HUD panels.
- 3-2-1-GO countdown overlay before gameplay.

Camera: Smooth-lerp upward to follow the tallest tower (CAMERA_LERP_SPEED=2.5) with a viewport floor clamp at `MIN_CAMERA_Y=15`.

Font rendering: `pixel.ttf` loaded at 4 sizes (72/40/32/22px) via FreeType.

---

## Magic Spell System (Multiplayer Only)

Spells are restricted to 2-Player mode, with a 15-second cooldown (real-time HUD timer display) and mystery grants (1/6 equal probability). Active spells are presented as a hidden cyan `MAGIC READY` indicator until they are cast.

### Light Spells (Self-buff)
| Spell | Effect | Duration |
|-------|--------|----------|
| Cement | Last block → immovable static body | Instant |
| Ivy | WeldJoint binding top 2 blocks | Instant |
| Lightning | Destroys last placed block | Instant |

### Dark Spells (Opponent-debuff)
| Spell | Effect | Duration |
|-------|--------|----------|
| Frost | Near-zero friction (0.02f) & ice-blue tint | 15s |
| Weight | Scales blocks by 2.0 (4x mass & size) | 10s |
| Speed Up | 3× fall speed multiplier | 10s |

---

## Input Controls

| Action | P1 | P2 |
|--------|----|----|
| Move Left | A | ← |
| Move Right | D | → |
| Soft Drop (Fast) | S | ↓ |
| Rotate | W | ↑ |
| Cast Spell | F | Num0 |
| Pause | Esc | Esc |

DAS/ARR: Hold left/right → initial delay 200ms → auto-repeat at 50ms intervals.

---

## Backend API

| Endpoint | Method | Purpose |
|----------|--------|---------|
| `/api/players/login?username=X&password=Y` | POST | Login verification with password matching |
| `/api/players/register?username=X&password=Y` | POST | Register a new user account (fails if taken) |
| `/api/players/{id}` | GET | Get player by ID |
| `/api/leaderboard/submit` | POST | Submit score + time + maxHeight |
| `/api/leaderboard/top10?gameMode=X` | GET | Top 10 scores per mode |
| `/api/achievements/unlock` | POST | Unlock achievement for player |

DB: PostgreSQL `chaotictower`, tables: players, leaderboard, achievements, player_achievements.

---

## Screen Transition Gotcha

**CRITICAL:** Never call `game.setScreen()` inside `render()` before `batch.end()`. The `setScreen()` call triggers `hide()` → `dispose()`, which destroys the SpriteBatch while it's still in use. Always:
1. Process rendering first (batch.begin → draw → batch.end)
2. Handle input AFTER batch.end()
3. Use a thread-safe `transitioning` guard flag at the beginning and inside the rendering pass to abort immediately after `game.setScreen()` is called.

---

## How to Run

```bash
# Frontend (game client)
cd frontend
.\gradlew.bat lwjgl3:run

# Backend (Spring Boot)
cd backend\chaoticTower-server
.\gradlew.bat bootRun
# Requires: PostgreSQL running on localhost:5432, DB "chaotictower", env var DB_PASSWORD set
```

---

## Remaining Work (~4%)
- Sound effects
- Particle effects (block settle, block destroy, spell cast)
- Texture/sprite-based block rendering (replace ShapeRenderer)
- Leaderboard display screen in frontend
- Backend: Puzzle mode scoring support
- Achievement tracking and display
