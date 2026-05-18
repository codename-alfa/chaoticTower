# Laporan Konteks Proyek Game: Chaotic Tower

## 1. Informasi Umum Proyek

| | |
|---|---|
| **Nama Game** | Chaotic Tower |
| **Repositori** | `codename-alfa/chaoticTower` |
| **Pengembang** | Danish Al Fayyadh Sunarta (codename-alfa) |
| **Genre** | Physics-based puzzle (terinspirasi dari Tricky Towers) |
| **Bahasa Pemrograman** | Java (100%) |
| **Arsitektur** | Client-Server terpisah (Monorepo: `frontend/` dan `backend/`) |
| **Resolusi** | 1920 × 1080 (Fullscreen-ready) |

### Tentang Tricky Towers (Referensi)

**Tricky Towers** adalah game *physics-based puzzle* oleh WeirdBeard Games (2016), sering dijuluki **"Tetris dengan hukum fisika"**. Pemain menyusun balok tetromino setinggi dan sestabil mungkin — balok yang disusun sembarangan akan miring, goyah, dan runtuh karena hukum gravitasi. Game Chaotic Tower terinspirasi langsung dari mekanik ini.

---

## 2. Stack Teknologi

| Layer | Teknologi |
|-------|-----------|
| **Game Client (Frontend)** | LibGDX (LWJGL3/Desktop) |
| **Physics Engine** | Box2D (via ekstensi LibGDX) |
| **Rendering** | `ShapeRenderer` (blok berwarna) + `SpriteBatch` (HUD/teks) |
| **Font Rendering** | FreeType (`gdx-freetype`) — `pixel.ttf` dalam 4 ukuran |
| **Backend (REST API)** | Spring Boot 3.x (Java 17) |
| **Database** | PostgreSQL (via Spring Data JPA / Hibernate) |
| **Build System** | Gradle (Groovy) untuk klien maupun server |

---

## 3. Mode Permainan & Aturan

Game ini difokuskan pada penyusunan balok tanpa menghapusnya (Tetris dengan fisika). Pemain memilih jumlah pemain dan mode permainan melalui **Mode Select Screen**.

### Single Player
| Mode | Deskripsi |
|------|-----------|
| **Survival** | Bertahan dengan 3 nyawa, bangun menara setinggi mungkin. Game berakhir saat nyawa habis. |
| **Time Attack** | Capai ketinggian target (20 meter) dalam batas waktu 2 menit. Nyawa tetap berlaku. |

### Local Multiplayer (2 Players — Split Screen)
| Mode | Deskripsi |
|------|-----------|
| **Survival** | Kedua pemain mulai dengan 3 nyawa. Pemain yang nyawanya habis lebih dulu kalah. |
| **Race** | Pemain pertama yang menaranya menyentuh garis target ketinggian (20 meter) menang. |

### Mekanik Inti
* **Nyawa:** Berkurang 1 jika balok jatuh melewati batas bawah (`y < -2f`). Terdapat sistem *cooldown* 2 detik agar efek domino keruntuhan tidak langsung menghabiskan seluruh nyawa.
* **Skor:** +10 poin setiap balok berhasil mendarat (*per-player scoring*).
* **Max Height:** Tinggi menara tertinggi dilacak secara real-time dan dikirim ke backend.
* **Kontrol:** Player 1 menggunakan WASD, Player 2 menggunakan Arrow Keys.

---

## 4. Tuning Fisika Box2D & Mekanik Balok

Bagian paling kompleks dari proyek ini adalah mengatur fisika Box2D agar terasa solid seperti Tricky Towers.

* **Bentuk Balok:** 7 bentuk Tetromino asli (O, I, T, L, J, S, Z) yang dibentuk menggunakan *Composite Bodies* (kumpulan fixture dalam 1 body). Setiap tipe memiliki warna unik (kuning, cyan, ungu, oranye, biru, hijau, merah).
* **Skala:** Ukuran dasar balok `0.5f` unit agar layar memiliki ruang manuver yang lebih luas.
* **Pergerakan Terkontrol (Controlled State):** Balok yang sedang turun merupakan `DynamicBody` dengan `gravityScale=0`. Friksi di-nol-kan saat menyentuh dinding samping bangunan (via `preSolve`) agar balok dapat diselipkan ke dalam celah sempit tanpa menjatuhkan menara. Tombol A/D menggeser balok dalam interval kaku `0.5f` unit.
* **Pendaratan (Settled State):** Saat bagian bawah balok menabrak pijakan atau balok lain, fisika penuh mengambil alih — `gravityScale=1.2`, friksi `0.55`, restitusi `0`, dan *Angular Damping* `2.5` untuk meredam *jitter*. Ukuran *hitbox* balok dikurangi `-0.01f` untuk mencegah *ghost collisions*.
* **Sistem Pedestal:** Lantai berbentuk pulau kecil `StaticBody`. Balok yang meleset langsung jatuh dan dihancurkan — berfungsi sebagai *garbage collection* alami untuk mencegah Box2D *crash* akibat penumpukan objek.
* **Environment Adaptif:** Single player menggunakan 1 pedestal terpusat dengan viewport `20×30`; multiplayer menggunakan 2 pedestal terpisah dengan viewport `40×30` dan dinding pembatas di tengah.

---

## 5. Implementasi Design Patterns (9 Pola)

| # | Pattern | Implementasi |
|---|---------|-------------|
| 1 | **Game Loop** | Metode `render()` LibGDX sebagai loop utama. |
| 2 | **Singleton** | `GameAssetManager` dan `BlockFactory` — hanya satu instans. |
| 3 | **State** | Transisi antar `Screen`: `MainMenuScreen` → `ModeSelectScreen` → `PlayingScreen` → `GameOverScreen`. |
| 4 | **Factory** | `BlockFactory` memproduksi 7 bentuk Tetromino secara acak (RNG) dari `shapeDefinitions`. |
| 5 | **Object Pool** | `Pool<Block>` di `BlockFactory` — me-*recycle* objek Java tanpa GC spike. |
| 6 | **Observer** | Box2D `ContactListener` memicu logika pendaratan (`beginContact`) dan pengaturan friksi (`preSolve`). |
| 7 | **Strategy** | Interface `GameModeStrategy` dengan 3 implementasi konkret: `SurvivalStrategy`, `RaceStrategy`, `TimeAttackStrategy`. Masing-masing mendefinisikan `checkWinCondition()`, `checkLoseCondition()`, dan `getResultText()`. |
| 8 | **Command** | Interface `InputCommand` dengan 4 implementasi: `MoveLeftCommand`, `MoveRightCommand`, `RotateCommand`, `SoftDropCommand`. Dikoordinasikan oleh `InputHandler` yang memetakan key-binding per pemain ke command objects. |
| 9 | **Repository / DAO** | Spring Data JPA: `PlayerRepository`, `LeaderboardRepository`, `AchievementRepository`, `PlayerAchievementRepository`. |

---

## 6. Arsitektur Backend & Database

### Database (PostgreSQL: `chaotictower`)

Skema menggunakan 4 tabel relasional:

| Tabel | Kolom |
|-------|-------|
| `players` | id, username |
| `leaderboard` | id, player_id (FK), game_mode, score, time_record, max_height |
| `achievements` | id, name, description |
| `player_achievements` | id, player_id (FK), achievement_id (FK), unlocked_at |

### REST API Endpoints

| Method | Endpoint | Fungsi |
|--------|----------|--------|
| `POST` | `/api/players/login?username=X` | Register atau login (get-or-create). |
| `GET` | `/api/players/{id}` | Ambil data player berdasarkan ID. |
| `POST` | `/api/leaderboard/submit` | Submit skor + waktu + max height ke leaderboard. |
| `GET` | `/api/leaderboard/top10?gameMode=X` | Ambil 10 skor tertinggi per mode. |
| `POST` | `/api/achievements/unlock` | Buka achievement untuk player tertentu. |

Password database disembunyikan menggunakan Environment Variables (`${DB_PASSWORD}`). Konfigurasi CORS telah disetel via `WebConfig`.

---

## 7. Struktur File Frontend

```
core/src/main/java/com/alfa/chaotictower/
├── Main.java                         # Entry point, extends Game
├── GameAssetManager.java             # Singleton — loads 4 font sizes dari pixel.ttf
├── command/                          # Command Pattern (Design Pattern #8)
│   ├── InputCommand.java             # Interface
│   ├── InputHandler.java             # Maps key-bindings → commands per player
│   ├── MoveLeftCommand.java
│   ├── MoveRightCommand.java
│   ├── RotateCommand.java
│   └── SoftDropCommand.java
├── entity/
│   ├── Block.java                    # Poolable tetromino — stores physics body + render data
│   └── Player.java                   # Lives, score, max height, spawn logic
├── factory/
│   └── BlockFactory.java             # Singleton factory + Object Pool
├── network/
│   └── ApiClient.java                # HTTP client untuk komunikasi dengan backend
├── screen/
│   ├── MainMenuScreen.java           # Login screen — gradient bg, styled input
│   ├── ModeSelectScreen.java         # Player count + game mode selection (card UI)
│   ├── PlayingScreen.java            # Game utama — rendering, physics, HUD
│   └── GameOverScreen.java           # Hasil akhir + submit score ke backend
└── strategy/                         # Strategy Pattern (Design Pattern #7)
    ├── GameModeStrategy.java          # Interface
    ├── SurvivalStrategy.java
    ├── RaceStrategy.java
    └── TimeAttackStrategy.java
```

---

## 8. Rendering & Visual

Game tidak menggunakan texture/sprite — seluruh visual dirender secara prosedural menggunakan `ShapeRenderer` dan `SpriteBatch`:

* **Background:** Gradient gelap (navy → hitam) menggunakan vertex-colored `rect()`.
* **Grid:** Garis tipis semi-transparan untuk memberikan skala visual.
* **Blok Tetromino:** 7 warna unik per tipe. Setiap tile dirender sebagai filled rectangle + outline hitam + highlight strip semi-transparan di atas. Blok yang sedang dikontrol memiliki efek *pulsing* (berkedip terang).
* **Pedestal:** Gradient abu-abu dengan highlight putih di tepi atas.
* **Dinding & Pembatas:** Rectangle semi-transparan.
* **Target Line:** Garis kuning *glowing* yang berkedip (untuk mode Race dan Time Attack).
* **HUD:** Font `pixel.ttf` dalam 4 ukuran (72px judul, 40px menu, 32px HUD, 22px small). Lives ditampilkan sebagai simbol hati (♥). Skor dan max height per-player.
* **Menu Screens:** Gradient background, card-based selection UI, floating decorative blocks pada Main Menu, blinking cursor, dan mode description text.

---

## 9. Status Progres & Bug yang Telah Diatasi

### Estimasi Progres: **~80%**

| Komponen | Status |
|----------|--------|
| Fisika Box2D (7 Tetromino) | ✅ Selesai |
| Object Pool + Factory | ✅ Selesai |
| Contact Listener (landing + friction) | ✅ Selesai |
| Deferred Execution (settle queue) | ✅ Selesai |
| Single Player + Multiplayer | ✅ Selesai |
| 3 Mode Permainan (Survival, Race, Time Attack) | ✅ Selesai |
| Strategy Pattern (win/loss conditions) | ✅ Selesai |
| Command Pattern (input handling) | ✅ Selesai |
| Mode Select Screen | ✅ Selesai |
| Colored block rendering (ShapeRenderer) | ✅ Selesai |
| Visual overhaul (semua screen) | ✅ Selesai |
| Per-player scoring + max height tracking | ✅ Selesai |
| Backend REST API + Database schema | ✅ Selesai |
| Frontend ↔ Backend komunikasi (ApiClient) | ✅ Selesai |
| Resolusi 1920×1080 | ✅ Selesai |
| Texture/sprite-based rendering | ❌ Belum |
| Sound effects & musik | ❌ Belum |
| Particle effects | ❌ Belum |
| Sistem Magic (Light & Dark) | ❌ Belum |
| Polish & animasi transisi | ❌ Belum |

### Bug Kritis yang Telah Diperbaiki

1. **Exit Value 1 (Box2D Mem Crash):** Terjadi karena mengubah properti fisik objek di pertengahan kalkulasi collision (di dalam `preSolve`/`beginContact`). Diselesaikan menggunakan sistem antrean *Deferred Execution* (`blocksToSettle`).
2. **Double Game Over:** Diselesaikan menggunakan *guard flag* `isGameOver` agar memori dunia tidak dihapus berkali-kali dalam 1 frame.
3. **Stale Pointers pada Pool:** Memastikan `body` lama dihapus (`world.destroyBody()`) sebelum objek `Block` dikembalikan ke pool `BlockFactory`.
4. **getTextInput Bug:** `Gdx.input.getTextInput()` pada Windows menyebabkan *freeze/deadlock*. Dilewati dengan mengimplementasikan text input native langsung di `MainMenuScreen` canvas menggunakan `InputAdapter.keyTyped()`.
5. **No Buffer Allocated (SpriteBatch Crash):** `ModeSelectScreen.render()` memanggil `handleInput()` sebelum `batch.begin()`. Ketika `handleInput()` memicu `game.setScreen()`, metode `hide()` → `dispose()` terpanggil sehingga `SpriteBatch` di-dispose sementara `render()` masih berjalan. Diperbaiki dengan memindahkan `handleInput()` ke setelah `batch.end()` dan menambahkan *guard flag* `transitioning`.