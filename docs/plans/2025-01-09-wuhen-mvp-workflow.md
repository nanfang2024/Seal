***

intent: Build WuHen (无痕) Android app with pure client-side video parsing for 5 Chinese platforms
success\_criteria:

- MVP version compiles and runs without errors

- Core parsing architecture implemented for all 5 platforms

- Download manager supports background queue with resume capability

- Material Design 3 UI with 3 Tab navigation completed

- All unit tests pass with >80% coverage
  risk\_level: medium
  auto\_approve: false

***

## Phase 1: Project Initialization & Architecture Foundation

### Step 1.1: Create Android Project Structure

action: Initialize new Android project with correct package name and basic configuration
loop: false
max\_iterations: 1
verify:
type: artifact
path: app/src/main/java/com/wu/hen
assert:
kind: exists

### Step 1.2: Configure Gradle Build Files

action: Set up build.gradle.kts files with required dependencies (Compose, Room, DataStore, OkHttp)
loop: false
max\_iterations: 1
verify:
type: shell
command: ./gradlew dependencies --configuration debugImplementation | grep -E "compose|room|datastore|okhttp"

### Step 1.3: Define Data Models

action: Create core data classes (VideoInfo, DownloadTask, VideoFormatOption) in data/models package
loop: false
max\_iterations: 1
verify:
type: artifact
path: app/src/main/java/com/wu/hen/data/models
assert:
kind: matches-glob
value: "\*.kt"

### Step 1.4: Setup Room Database

action: Implement AppDatabase class, DAO interfaces, and migration strategy
loop: false
max\_iterations: 1
verify:
type: shell
command: ./gradlew app:schemaDebug

### Step 1.5: Implement DataStore Preferences

action: Create UserPreferences data store with default values structure
loop: false
max\_iterations: 1
verify:
type: artifact
path: app/src/main/java/com/wu/hen/data/preferences
assert:
kind: exists

### Step 1.6: Define Parser Interface Contract

action: Create VideoPlatformParser interface and base PlatformParser abstract class
loop: false
max\_iterations: 1
verify:
type: shell
command: grep -r "interface VideoPlatformParser" app/src/main/java/

### Step 1.7: Create DownloadManager Skeleton

action: Implement DownloadManager class with coroutine scope and task queue structure
loop: false
max\_iterations: 1
verify:
type: shell
command: grep -r "class DownloadManager" app/src/main/java/

## Phase 2: UI Framework & Navigation

### Step 2.1: Theme Configuration

action: Set up Material Design 3 theme with custom color schemes (light/dark)
loop: false
max\_iterations: 1
verify:
type: browser
url: <http://localhost:3000> (once app running)
check: Material 3 theme applied with WuHen branding colors

### Step 2.2: Navigation Graph Setup

action: Create NavGraph with three destination routes: Home, Downloads, Settings
loop: false
max\_iterations: 1
verify:
type: shell
command: grep -r "enum class Screen" app/src/main/java/

### Step 2.3: Bottom Navigation Bar Implementation

action: Implement M3 BottomNavigation with 3 items (Home, Queue, Settings)
loop: false
max\_iterations: 1
verify:
type: shell
command: ./gradlew app:lintDebug

### Step 2.4: Home Screen Skeleton

action: Create HomeScreen composable with URL input field and clipboard button
loop: false
max\_iterations: 1
verify:
type: shell
command: grep -r "@Composable.\*fun HomeScreen" app/src/main/java/

### Step 2.5: Download List Screen Skeleton

action: Create DownloadListScreen composable with RecyclerView-like lazyColumn
loop: false
max\_iterations: 1
verify:
type: shell
command: grep -r "@Composable.\*fun DownloadListScreen" app/src/main/java/

### Step 2.6: Settings Screen Skeleton

action: Create SettingsScreen composable with preference categories (display, storage, platform)
loop: false
max\_iterations: 1
verify:
type: shell
command: grep -r "@Composable.\*fun SettingsScreen" app/src/main/java/

### Step 2.7: ViewModel Architecture Setup

action: Implement ViewModel classes with StateFlow for each screen (HomeViewModel, DownloadViewModel, SettingsViewModel)
loop: false
max\_iterations: 1
verify:
type: shell
command: ls app/src/main/java/com/wu/hen/ui/viewmodel/ | wc -l

## Phase 3: Core Parser Implementations

### Step 3.1: Common Parser Utilities

action: Create URL pattern matching utilities, HTTP client wrapper with retry logic
loop: false
max\_iterations: 1
verify:
type: shell
command: grep -r "OkHttpClient" app/src/main/java/com/wu/hen/platform/common/

### Step 3.2: Douyin Parser Implementation

action: Implement DouyinParser with signature extraction and video URL resolution
loop: until parse success
max\_iterations: 3
verify:
type: shell
command: cd test && python3 test\_douyin.py && cd ..

### Step 3.3: Kuaishou Parser Implementation

action: Implement KuaishouParser with mms token generation and stream extraction
loop: until parse success
max\_iterations: 3
verify:
type: shell
command: cd test && python3 test\_kuaishou.py && cd ..

### Step 3.4: Bilibili Parser Implementation

action: Implement BilibiliParser with aid/cid parameter parsing and quality mapping
loop: until parse success
max\_iterations: 3
verify:
type: shell
command: cd test && python3 test\_bilibili.py && cd ..

### Step 3.5: Xiaohongshu Parser Implementation

action: Implement XiaohongshuParser with note ID extraction and encrypted parameter handling
loop: until parse success
max\_iterations: 3
verify:
type: shell
command: cd test && python3 test\_xiaohongshu.py && cd ..

### Step 3.6: Pipixia Parser Implementation

action: Implement PipixiaParser with short video ID resolution and format detection
loop: until parse success
max\_iterations: 3
verify:
type: shell
command: cd test && python3 test\_pipixia.py && cd ..

### Step 3.7: Platform Registry System

action: Create PlatformRegistry singleton to manage parser discovery and selection
loop: false
max\_iterations: 1
verify:
type: shell
command: grep -r "class PlatformRegistry" app/src/main/java/

## Phase 4: Download Engine Development

### Step 4.1: Resume Downloader Core

action: Implement ResumeDownloader with chunk-based HTTP range requests
loop: false
max\_iterations: 1
verify:
type: shell
command: ./gradlew test --tests "*ResumeDownloaderTest*"

### Step 4.2: Download Task Worker

action: Create DownloadTaskWorker with ProgressState emission via Channel
loop: false
max\_iterations: 1
verify:
type: shell
command: grep -r "class DownloadTaskWorker" app/src/main/java/

### Step 4.3: Foreground Service Integration

action: Implement DownloadQueueService with notification channel and status display
loop: false
max\_iterations: 1
verify:
type: shell
command: grep -r "class DownloadQueueService" app/src/main/java/

### Step 4.4: Storage Management System

action: Create StorageManager for quota checking, cleanup, and space estimation
loop: false
max\_iterations: 1
verify:
type: shell
command: ./gradlew test --tests "*StorageManagerTest*"

### Step 4.5: Download Repository Layer

action: Implement DownloadRepository with room database integration and query methods
loop: false
max\_iterations: 1
verify:
type: shell
command: ./gradlew test --tests "*DownloadRepositoryTest*"

## Phase 5: Metadata & File Management

### Step 5.1: Filename Generator

action: Create FileNameGenerator with template-based naming and collision avoidance
loop: false
max\_iterations: 1
verify:
type: shell
command: ./gradlew test --tests "*FileNameGeneratorTest*"

### Step 5.2: Metadata Extractor

action: Implement VideoMetadataExtractor for thumbnail, title, author, duration info
loop: false
max\_iterations: 1
verify:
type: shell
command: grep -r "class VideoMetadataExtractor" app/src/main/java/

### Step 5.3: Tag Writer Integration

action: Add exiftool or mu4 library for metadata embedding (MP4/MKV tagging)
loop: false
max\_iterations: 1
verify:
type: shell
command: grep -r "metadata.\*embed" app/build.gradle.kts

### Step 5.4: File Copy & Move Utilities

action: Implement safe file operations with atomic moves and rollback on failure
loop: false
max\_iterations: 1
verify:
type: shell
command: grep -r "suspend fun.\*copyFile" app/src/main/java/

### Step 5.5: Format Detection Helper

action: Create FormatDetector to identify uploaded video container types and map to output format
loop: false
max\_iterations: 1
verify:
type: shell
command: grep -r "class FormatDetector" app/src/main/java/

## Phase 6: UI Feature Implementation

### Step 6.1: Parse Result Dialog

action: Create dialog with video info card and quality selection chips
loop: false
max\_iterations: 1
verify:
type: browser
url: <http://localhost:3000>
check: Parse result shows 3+ quality options

### Step 6.2: Download Item Card

action: Implement DownloadItem composable with progress bar and pause/resume buttons
loop: false
max\_iterations: 1
verify:
type: shell
command: grep -r "@Composable.\*fun DownloadItem" app/src/main/java/

### Step 6.3: Clipboard Listener

action: Create ClipboardMonitor that detects link pastes and triggers parse flow
loop: false
max\_iterations: 1
verify:
type: shell
command: grep -r "ClipboardManager" app/src/main/java/

### Step 6.4: Sharing Intent Handler

action: Implement IntentFilter for SHARE\_ACTION and deep link processing
loop: false
max\_iterations: 1
verify:
type: shell
command: grep -A 5 "android.intent.action.SEND" app/src/main/AndroidManifest.xml

### Step 6.5: Empty States & Loading Indicators

action: Add appropriate empty state illustrations and loading skeletons for all screens
loop: false
max\_iterations: 1
verify:
type: shell
command: grep -r "EmptyState|LoadingSkeleton" app/src/main/java/

### Step 6.6: Error Handling UI

action: Implement Snackbar/ErrorBanner for common error scenarios (parse fail, download error)
loop: false
max\_iterations: 1
verify:
type: shell
command: grep -r "ErrorUI|SnackBar" app/src/main/java/

### Step 6.7: Search & Filter in Download List

action: Add search bar and filter chips (all/completed/paused) for download queue
loop: false
max\_iterations: 1
verify:
type: shell
command: grep -r "filterByStatus|searchQuery" app/src/main/java/

## Phase 7: Settings & Preferences

### Step 7.1: Quality Default Preference

action: Create dropdown in settings for default quality preference (auto/1080p/720p/480p)
loop: false
max\_iterations: 1
verify:
type: shell
command: grep -r "defaultQuality" app/src/main/java/

### Step 7.2: Storage Path Configuration

action: Implement storage location selector with available space display
loop: false
max\_iterations: 1
verify:
type: shell
command: grep -r "storagePath|preferredDirectory" app/src/main/java/

### Step 7.3: Platform Toggle Switches

action: Add checkboxes for each platform to enable/disable specific parsers
loop: false
max\_iterations: 1
verify:
type: shell
command: grep -r "enabledPlatforms" app/src/main/java/

### Step 7.4: Clear Cache Functionality

action: Implement cache cleaning with confirmation dialog and space freed estimate
loop: false
max\_iterations: 1
verify:
type: shell
command: ./gradlew test --tests "*CacheCleanerTest*"

### Step 7.5: About & Disclaimer Screen

action: Create about screen with version info, license, legal disclaimer, and privacy policy link
loop: false
max\_iterations: 1
verify:
type: shell
command: grep -r "@Composable.\*fun AboutScreen" app/src/main/java/

## Phase 8: Testing & Validation

### Step 8.1: Unit Tests for Parsers

action: Write comprehensive unit tests for all 5 platform parsers with mock URLs
loop: false
max\_iterations: 1
verify:
type: shell
command: ./gradlew test --tests "*ParserTest*"

### Step 8.2: Integration Tests for Download Flow

action: Create integration tests simulating complete parse → download lifecycle
loop: false
max\_iterations: 1
verify:
type: shell
command: ./gradlew connectedDebugAndroidTest

### Step 8.3: UI Tests for Key Flows

action: Implement Compose UI tests for critical user journeys (paste link → download)
loop: false
max\_iterations:1
verify:
type: shell
command: ./gradlew composeLint

### Step 8.4: Performance Benchmarking

action: Add benchmarks for parse time, download speed, and memory usage
loop: false
max\_iterations: 1
verify:
type: shell
command: ./gradlew assembleBenchmark

### Step 8.5: Edge Case Testing

action: Test boundary conditions (large files, interrupted downloads, invalid URLs)
loop: false
max\_iterations: 1
verify:
type: shell
command: ./gradlew test

## Phase 9: Polish & Release Preparation

### Step 9.1: ProGuard/R8 Rules

action: Configure obfuscation rules for kept classes and network reflection
loop: false
max\_iterations: 1
verify:
type: artifact
path: app/proguard-rules.pro
assert:
kind: exists

### Step 9.2: Icon & Branding Assets

action: Generate adaptive icons, splash screen assets for all densities
loop: false
max\_iterations: 1
verify:
type: artifact
path: app/src/main/res/mipmap-\*
assert:
kind: matches-glob
value: "*ic\_launcher*.png"

### Step 9.3: Multi-language Support Prep

action: Externalize all string resources and create resource folders for future localization
loop: false
max\_iterations: 1
verify:
type: shell
command: grep -r "@" app/src/main/res/values/strings.xml | wc -l

### Step 9.4: Crash Reporting Integration

action: Integrate Firebase Crashlytics or similar for production monitoring
loop: false
max\_iterations: 1
verify:
type: shell
command: grep -r "firebase-crashlytics" app/build.gradle.kts

### Step 9.5: Documentation Creation

action: Write README.md with build instructions, contributing guide, and usage documentation
loop: false
max\_iterations: 1
verify:
type: artifact
path: README.md
assert:
kind: exists

### Step 9.6: Beta Sign & Release Bundle

action: Generate signed AAB release bundle for Google Play distribution
loop: false
max\_iterations: 1
verify:
type: shell
command: ./gradlew bundleRelease

## Gate Checklist

Before any major phase completion:

- [ ] All unit tests passing (>80% coverage)

- [ ] No lint warnings/errors

- [ ] Memory leak analysis passed

- [ ] UI rendering <16ms per frame

- [ ] APK size <50MB

- [ ] Security review completed

