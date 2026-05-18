# Chaotic Tower — Project Knowledge Base

## Quick Facts
- **Repo:** `d:\programming\Java\chaoticTower` (monorepo: `frontend/` + `backend/`)
- **Language:** Java 17
- **Frontend:** LibGDX (LWJGL3 Desktop), Box2D physics, FreeType fonts
- **Backend:** Spring Boot 3.x, PostgreSQL, Spring Data JPA
- **Build:** Gradle (Groovy) — separate builds for frontend & backend
- **Resolution:** 1920×1080
- **Progress:** ~80%

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
│   │       │   ├── InputHandler.java        # Maps keys→commands per player
│   │       │   ├── MoveLeftCommand.java     # Shifts block -0.5 units
│   │       │   ├── MoveRightCommand.java    # Shifts block +0.5 units
│   │       │   ├── RotateCommand.java       # Rotates block 90°
│   │       │   └── SoftDropCommand.java     # Normal/fast fall speed
│   │       ├── entity/
│   │       │   ├── Block.java               # Poolable, composite Box2D body, render data
│   │       │   └── Player.java              # Lives, score, maxHeight, spawn timer
│   │       ├── factory/
│   │       │   └── BlockFactory.java        # Singleton + Pool<Block>, 7 tetromino shapes
│   │       ├── network/
│   │       │   └── ApiClient.java           # HTTP client (login, submitScore, getTop10)
│   │       ├── screen/
│   │       │   ├── MainMenuScreen.java      # Login (username typed + Enter)
│   │       │   ├── ModeSelectScreen.java    # 1P/2P + mode selection (card UI)
│   │       │   ├── PlayingScreen.java       # Main game loop + rendering
│   │       │   └── GameOverScreen.java      # Results + score submission
│   │       └── strategy/                    # Strategy Pattern (#7)
│   │           ├── GameModeStrategy.java     # Interface
│   │           ├── SurvivalStrategy.java     # SP: survive, MP: last standing
│   │           ├── RaceStrategy.java         # MP: first to 20m wins
│   │           └── TimeAttackStrategy.java   # SP: reach 20m in 2 min
│   ├── lwjgl3/                  # Desktop launcher
│   │   └── Lwjgl3Launcher.java  # Window config (1920×1080)
│   └── assets/
│       └── pixel.ttf            # Game font
│
└── backend/chaoticTower-server/ # Spring Boot REST API
    └── src/main/java/com/alfa/backend/
        ├── config/WebConfig.java
        ├── controller/          # PlayerController, LeaderboardController, AchievementController
        ├── entity/              # Player, Leaderboard, Achievement, PlayerAchievement (JPA)
        ├── repository/          # JPA repositories
        └── service/             # PlayerService, LeaderboardService, AchievementService
```

---

## 9 Design Patterns Implemented

| # | Pattern | Where |
|---|---------|-------|
| 1 | Game Loop | `render()` in LibGDX |
| 2 | Singleton | `GameAssetManager`, `BlockFactory` |
| 3 | State | Screen transitions (MainMenu→ModeSelect→Playing→GameOver) |
| 4 | Factory | `BlockFactory.spawnBlock()` — 7 tetromino types |
| 5 | Object Pool | `Pool<Block>` in `BlockFactory` |
| 6 | Observer | Box2D `ContactListener` (landing, friction) |
| 7 | Strategy | `GameModeStrategy` interface → Survival/Race/TimeAttack |
| 8 | Command | `InputCommand` interface → MoveLeft/MoveRight/Rotate/SoftDrop |
| 9 | Repository | Spring Data JPA repositories |

---

## Critical Box2D Physics Notes

### Block Lifecycle
1. **Controlled:** `gravityScale=0`, `fixedRotation=true`, falls at constant velocity (-2.5 or -12.0 for fast drop)
2. **Settling:** Triggered by `beginContact` when bottom normal > 0.4. Added to `blocksToSettle` queue (NEVER modify physics inside callbacks!)
3. **Settled:** `gravityScale=1.2`, `fixedRotation=false`, `angularDamping=2.5`, `linearDamping=0.05`
4. **Out-of-bounds:** If `y < -2f`, destroy body → free to pool → player loses life

### Known Box2D Gotchas
- **NEVER** call `world.destroyBody()` or change body type inside `beginContact`/`preSolve` → use deferred queue
- **NEVER** return a Block to pool before `world.destroyBody(block.body)` → stale pointer crash
- Hitbox is `TILE_SIZE/2 - 0.01f` to prevent ghost collisions between adjacent tiles
- `preSolve` overrides friction to 0 for side-wall contacts on controlled blocks (enables sliding into gaps)

### Environment Layout
- **1P:** Viewport 20×30, pedestal at (10,2), walls at x=2.5 and x=17.5
- **2P:** Viewport 40×30, pedestals at (10,2) and (30,2), divider at x=20, walls at 2.5/17.5/22.5/37.5

---

## Rendering System (No Textures)

All visuals are procedural using `ShapeRenderer`:
- Blocks: 7 colors (O=yellow, I=cyan, T=purple, L=orange, J=blue, S=green, Z=red)
- Each tile: filled rect + outline + semi-transparent highlight strip
- Controlled blocks have pulsing brightness effect
- Background: vertex-colored gradient rect
- Grid: semi-transparent lines at 1-unit intervals
- Target line (Race/TimeAttack): glowing yellow line at pedestal_top + 20m

Font rendering: `pixel.ttf` loaded at 4 sizes (72/40/32/22px) via FreeType

---

## Backend API

| Endpoint | Method | Purpose |
|----------|--------|---------|
| `/api/players/login?username=X` | POST | Register or login (get-or-create) |
| `/api/players/{id}` | GET | Get player by ID |
| `/api/leaderboard/submit` | POST | Submit score + time + maxHeight |
| `/api/leaderboard/top10?gameMode=X` | GET | Top 10 scores per mode |
| `/api/achievements/unlock` | POST | Unlock achievement for player |

DB: PostgreSQL `chaotictower`, tables: players, leaderboard, achievements, player_achievements

---

## Screen Transition Gotcha

**CRITICAL:** Never call `game.setScreen()` inside `render()` before `batch.end()`. The `setScreen()` call triggers `hide()` → `dispose()`, which destroys the SpriteBatch while it's still in use. Always:
1. Process rendering first (batch.begin → draw → batch.end)
2. Handle input AFTER batch.end()
3. Use a `transitioning` guard flag to prevent further rendering after `setScreen()`

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

## Remaining Work (~20%)
- Texture/sprite-based block rendering (replace ShapeRenderer)
- Sound effects & background music
- Particle effects (block settle, block destroy)
- Magic system (Light magic = defense, Dark magic = attack)
- UI polish & screen transition animations
