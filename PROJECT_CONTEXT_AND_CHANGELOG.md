# GhorCycle — Project Context & Changelog

> **Purpose**: This file serves as the single source of truth for the **GhorCycle** Android application architecture, implemented stages (Stages 1 through 9 — 100% Complete), file inventory, data schemas, and chronological change history. Share or reference this file in any new chat session to immediately load the complete project context.

---

## 1. Project Overview
- **App Name**: GhorCycle ("Smart Household Cycling & Resale Marketplace")
- **Root Project Name**: `Ghor_Cycle` (configured in `settings.gradle`)
- **Active Project Directories**:
  - Primary Workspace: `c:\Users\Asus\OneDrive\Desktop\Ghor_Cycle`
  - Android Studio Synced Mirror: `c:\Users\Asus\AndroidStudioProjects\Ghor_Cycle_F`
- **Package / Namespace / Application ID**: `com.example.ghor_cycle` (renamed from `com.example.myapplication`)
- **Theme**: `Theme.GhorCycle` (`Base.Theme.GhorCycle` in `res/values/themes.xml`)
- **Language & Stack**: 100% Pure Java (JDK 1.8 target) & Android XML (Zero Kotlin, Zero Compose). Android SDK (`minSdk 24`, `compileSdk 33`, `targetSdk 33`), Material 3 (`1.5.0`), AndroidX AppCompat (`1.4.1`) & ConstraintLayout (`2.1.3`), Firebase BoM (`32.8.0` — Auth, Firestore, Storage), AndroidX WorkManager (`2.8.1`).
- **Build Output Path**: Configured in root `build.gradle` to `C:/Users/Asus/.android_build/Ghor_Cycle/${project.name}`.
- **Ready-to-Install Phone APK**: `C:\Users\Asus\OneDrive\Desktop\GhorCycle.apk` (also at `C:\Users\Asus\.android_build\Ghor_Cycle\app\outputs\apk\debug\app-debug.apk`).
- **Firebase & Autonomous Local Fallback Status**:
  - `google-services` plugin (`4.4.2`) is configured in root `build.gradle` and prepared in `app/build.gradle` (uncomment line 3 once `google-services.json` for package `com.example.ghor_cycle` is placed in `app/`).
  - **Self-Contained Local Persistence**: `FirebaseAuthService`, `FirestoreProductService`, and `InteractionService` feature automatic runtime detection. When `google-services.json` is not present, `FirebaseAuthService` automatically provisions and persists a local user session in `SharedPreferences` (`ghorcycle_auth_prefs`), allowing users on physical phones to register, sign in, post items with photos, favorite items, send donation/exchange requests, and track eco-impact with zero errors.

---

## 2. Architecture & Codebase Inventory (All 9 Stages Implemented)

### Activities (`app/src/main/java/com/example/ghor_cycle/`)
1. **`SplashActivity.java`** (`activity_splash.xml`) — *Stage 1 & 3*
   - Launcher entry point with 2.2s auto-routing timer & manual "Get Started" button.
   - Initializes `FirebaseAuthService.init(this)` and checks `FirebaseAuthService.isUserLoggedIn()` (both Firebase and persistent local session): routes authenticated users to `MainActivity` and unauthenticated users to `LoginActivity`.
2. **`LoginActivity.java`** (`activity_login.xml`) — *Stage 1 & 3*
   - Email & password sign-in via `FirebaseAuthService` (with automatic local fallback), client-side validation, loading state, readable error banner, "Continue as Guest" mode, and link to `RegisterActivity`.
3. **`RegisterActivity.java`** (`activity_register.xml`) — *Stage 1 & 3*
   - New user registration with Full Name, Email, Phone, Password, Confirm Password. Updates Firebase user profile display name (or creates local persistent neighbor session) and routes directly to `MainActivity`.
4. **`MainActivity.java`** (`activity_main.xml`) — *Stage 1, 2, 4, 5, 6*
   - Primary marketplace hub with adaptive `GridLayoutManager` (2 columns portrait, 3 columns landscape), horizontal category chip bar (`CategoryAdapter`), real-time search bar, multi-criteria filter dialog (`dialog_marketplace_filter.xml`), favorites filter mode, offline connectivity banner (`NetworkChangeReceiver`), background sync trigger (`BackgroundSyncManager`), and `BottomNavigationView`.
5. **`AddItemActivity.java`** (`activity_add_item.xml`) — *Stage 1, 4, 6*
   - Authenticated item creation & owner editing/deletion screen.
   - Integrates Android Photo Picker (`ActivityResultContracts.GetContent`) + `ContentProviderHelper` URI metadata validation.
   - Enforces dynamic pricing (`Sell` requires price > 0; `Free`/`Donate`/`Recycle` lock price to 0).
6. **`ProductDetailsActivity.java`** (`activity_product_details.xml`) — *Stage 1, 2, 4, 5*
   - Displays full product details, status badge (`Available`, `Reserved`, `Completed`), and eco-impact note.
   - **Owner View**: View incoming requests (`dialog_view_requests.xml`), Accept/Reject requests, Mark as Completed/Cycled, Edit listing, or Delete listing.
   - **Buyer View**: Request Free/Donated item or Propose Barter/Exchange (`dialog_submit_request.xml`), Contact Seller, Make Offer. Disables actions when item is `Reserved` or `Completed`.
7. **`ProfileActivity.java`** (`activity_profile.xml`) — *Stage 1, 3, 8*
   - Displays authenticated user's name, email, truncated UID (or Guest state), community cycling stats, **🌱 Eco-Impact Dashboard** launcher (`btnProfileEcoImpact`), **♻ Recycling Centers Directory** launcher (`btnProfileRecyclingDirectory`), My Active Listings button, Edit Profile button, and Sign Out / Switch Account action.
8. **`EcoInsightActivity.java`** (`activity_eco_insight.xml`) — *Stage 7*
   - Networking & Parsing hub accessible from the Bottom Navigation `Explore` tab.
   - Demonstrates live HTTP GET requests to Open-Meteo Air Quality API for Dhaka, offline JSON fixture loading, malformed JSON error handling, and `XmlPullParser` recycling guidelines parsing.
9. **`RecyclingDirectoryActivity.java`** (`activity_recycling_directory.xml`) — *Stage 8*
   - Displays curated Dhaka community recycling centers (`RecyclingDataProvider`) in a `RecyclerView` (`RecyclingCenterAdapter` + `item_recycling_center.xml`).
   - Supports real-time search by area/name, horizontal material filter chips (`All Materials`, `Plastic`, `Paper`, `Metal`, `Electronics`, `Clothes`, `Glass`), phone dialer intent (`Intent.ACTION_DIAL`), and external Google Maps navigation intent (`geo:lat,lng?q=...`) with browser fallback.
10. **`EcoImpactActivity.java`** (`activity_eco_impact.xml`) — *Stage 8*
    - Displays the user's verified environmental impact dashboard computed by `EcoImpactCalculator`.
    - Shows total reused items, breakdown of Donations / Exchanges / Direct Reuses, defensible environmental savings (Landfill Waste Diverted in kg, CO2e Avoided in kg, Water Conserved in L), deduplicated transaction ledger, and transparent scientific methodology card.

### Models & Adapters (`app/src/main/java/com/example/ghor_cycle/`)
- **`Product.java`**: Core marketplace model (`Serializable`) with 13 Firestore fields (`productId`, `ownerId`, `title`, `description`, `category`, `condition`, `transactionType`, `price`, `location`, `imageUrl`, `status`, `createdAt`, `updatedAt`) + UI helpers.
- **`ItemRequest.java`**: Donation/Exchange request model (`requestId`, `productId`, `productTitle`, `ownerId`, `requesterId`, `requesterEmail`, `requestType` [`DONATION`/`EXCHANGE`], `offeredItem`, `message`, `status` [`PENDING`/`ACCEPTED`/`REJECTED`/`CANCELLED`], `createdAt`, `updatedAt`).
- **`Category.java`**: Filter chip model (`name`, `iconResId`, `isSelected`).
- **`EcoAirQuality.java`**: Domain model for Dhaka air quality metrics (`pm25`, `pm10`, `europeanAqi`, `isFromLiveApi`, `httpStatusCode`) and contextual eco recommendations.
- **`EcoGuideline.java`**: Domain model for parsed XML recycling guidelines (`id`, `title`, `category`, `impactCo2`, `instructions`).
- **`RecyclingCenter.java`**: Model for physical recycling centers (`centerId`, `name`, `addressOrArea`, `acceptedMaterials`, `contactPhone`, `contactEmail`, `openingHours`, `latitude`, `longitude`, `isSampleData`, `notes`) with `hasValidCoordinates()` and `acceptsMaterial(String)`.
- **`EcoImpactSummary.java`**: Model holding verified reuse counts (`successfulDonationsCount`, `completedExchangesCount`, `completedSalesReuseCount`, `totalItemsReused`), environmental indicators (`estimatedWasteDivertedKg`, `estimatedCo2AvoidedKg`, `estimatedWaterSavedLiters`), and deduplicated `Set<String> verifiedTransactionIds`.
- **`ProductAdapter.java`** (`item_product_card.xml`): RecyclerView adapter for product grid cards with favorite heart toggle.
- **`CategoryAdapter.java`** (`item_category_chip.xml`): Horizontal RecyclerView adapter for the 9 category chips (`All` + 8 core categories).
- **`RecyclingCenterAdapter.java`** (`item_recycling_center.xml`): RecyclerView adapter for recycling centers with Directions and Call buttons.

### Services, Engines & Background Components (`app/src/main/java/com/example/ghor_cycle/`)
- **`FirebaseAuthService.java`**: Wraps `FirebaseAuth` login, registration, sign-out, RFC email regex validation, password length validation (>= 6 chars), exception translation, and automatic `SharedPreferences` local session fallback (`init`, `isUserLoggedIn`, `getCurrentUserId`, `getCurrentUserEmail`, `getCurrentUserName`).
- **`FirestoreProductService.java`**: Manages Firestore `products` collection CRUD, owner authorization checks (`getCurrentUserId()`), `FirebaseStorage` image upload with local URI fallback, and in-memory fallback dataset when Firebase is unconfigured.
- **`InteractionService.java`**: Manages `users/{userId}/favorites/{productId}` and `item_requests` collection, `fetchUserRequests(userId, callback)`, duplicate request prevention, owner accept/reject transitions (`Available` -> `Reserved`), and transaction completion (`Completed`).
- **`SampleDataProvider.java`**: Provides the 8 core categories (`Furniture`, `Clothes`, `Books`, `Kitchen Items`, `Electronics`, `Plastic`, `Paper`, `Metal`), 22 realistic Dhaka-based sample products, and multi-criteria filtering logic.
- **`RecyclingDataProvider.java`**: Provides 5 curated Dhaka recycling centers (Tejgaon, Mirpur 10, Dhanmondi, Old Dhaka, Uttara) transparently tagged as sample community records (`isSampleData = true`) with real coordinates and `filterCenters(...)` overloads.
- **`EcoImpactCalculator.java`**: Deduplicating calculation engine enforcing verified-only counting (`ItemRequest.STATUS_ACCEPTED` and `Product.isCompleted()`), excluding pending/rejected records, and applying documented conservative benchmarks (`2.5 kg` waste/item, `1.8 kg CO2e/kg` material, `250 L` water/item).
- **`NetworkChangeReceiver.java`**: Dynamic `BroadcastReceiver` (`RECEIVER_NOT_EXPORTED` on API 33+) with `NetworkCapabilities` validation (`NET_CAPABILITY_INTERNET` + `NET_CAPABILITY_VALIDATED`).
- **`BackgroundSyncManager.java` & `SyncOfflineDataWorker.java`**: AndroidX `WorkManager` unique one-time sync worker with `NetworkType.CONNECTED` constraint and `LiveData` status observation.
- **`ContentProviderHelper.java`**: Resolves `OpenableColumns.DISPLAY_NAME`, `OpenableColumns.SIZE`, and stream accessibility from `content://` URIs without requesting broad storage permissions.
- **`HttpNetworkService.java`**: Asynchronous `HttpURLConnection` REST client on a background `ExecutorService` with `Handler(Looper.getMainLooper())` callbacks and `JSONObject` deserialization.
- **`XmlEcoParserHelper.java`**: Stream-based `XmlPullParser` implementation parsing `<guidelines>` XML into `EcoGuideline` objects.

### Security Rules & Automated Tests
- **`firestore.rules`**: Production-grade Firestore Security Rules v2 enforcing authentication, ownership checks, immutable identity fields, self-request prevention, and schema validation for `/products/{productId}`, `/users/{userId}/favorites/{productId}`, and `/item_requests/{requestId}`.
- **Unit Tests (`app/src/test/java/com/example/ghor_cycle/`) — 45 Tests, 0 Failures (100% Pass Rate)**:
  1. `Stage1NavigationUnitTest.java` (3 tests)
  2. `Stage2MarketplaceUnitTest.java` (6 tests)
  3. `Stage3AuthUnitTest.java` (7 tests)
  4. `Stage4FirestoreProductUnitTest.java` (8 tests)
  5. `Stage5InteractionsUnitTest.java` (7 tests)
  6. `Stage6AndroidComponentsUnitTest.java` (4 tests)
  7. `Stage7NetworkingUnitTest.java` (4 tests — uses `org.json:json:20231013`, `xmlpull:1.1.3.4a`, `xpp3:1.1.4c` for JVM execution)
  8. `Stage8EcoImpactUnitTest.java` (4 tests — recycling center filtering, coordinates, strict verification & deduplication math)
  9. `Stage9IntegrationUnitTest.java` (1 comprehensive end-to-end 2-account marketplace workflow test)
  10. `ExampleUnitTest.java` (1 test)

---

## 3. How to Connect Live Firebase Cloud Sync (Optional Next Step)
The app works 100% out-of-the-box on any phone using its autonomous local persistence engine. To switch to live cloud synchronization across multiple devices:
1. Open [Firebase Console](https://console.firebase.google.com/) -> **Add project** (`GhorCycle`).
2. Add an **Android app** with package name: **`com.example.ghor_cycle`**.
3. Download **`google-services.json`** and place it inside `app/google-services.json`.
4. In Firebase Console:
   - Enable **Authentication** -> **Sign-in method** -> **Email/Password**.
   - Enable **Firestore Database** and publish the rules from `firestore.rules`.
5. Uncomment line 3 (`id 'com.google.gms.google-services'`) in `app/build.gradle` and run `.\gradlew.bat assembleDebug`.

---

## 4. Chronological Changelog

### [2026-11-11] — Initial Context Load & Folder Switch
- Indexed the **GhorCycle** codebase across Stages 1–7 and created `PROJECT_CONTEXT_AND_CHANGELOG.md`.

### [2026-11-11] — Stage 7 JVM Unit Test Fix & Stage 8 Implementation (Recycling Directory & Eco-Impact)
- Added `testImplementation` dependencies (`org.json:json:20231013`, `xmlpull:xmlpull:1.1.3.4a`, `xpp3:xpp3:1.1.4c`) in `app/build.gradle` so Android JSON/XML parsers execute cleanly in local JVM unit tests.
- Created `RecyclingCenter.java`, `RecyclingDataProvider.java`, `RecyclingCenterAdapter.java`, `item_recycling_center.xml`, `activity_recycling_directory.xml`, and `RecyclingDirectoryActivity.java`.
- Created `EcoImpactSummary.java`, `EcoImpactCalculator.java`, `activity_eco_impact.xml`, and `EcoImpactActivity.java`.
- Added `fetchUserRequests` to `InteractionService.java` and wired Eco-Impact and Recycling Directory buttons in `activity_profile.xml` and `ProfileActivity.java`.
- Registered `RecyclingDirectoryActivity` and `EcoImpactActivity` in `AndroidManifest.xml`.
- Created and passed `Stage8EcoImpactUnitTest.java`.

### [2026-11-11] — Stage 9 Final Audit, Autonomous Phone Mode & Project Rename to `Ghor_Cycle`
- Created `Stage9IntegrationUnitTest.java` verifying the complete two-account workflow (registration -> listing creation -> discovery -> favorites -> donation request -> owner acceptance -> completion -> eco-impact calculation -> security/deduplication checks).
- Implemented autonomous local session persistence in `FirebaseAuthService.java` (`SharedPreferences` fallback) and integrated `getCurrentUserId()` / `isUserLoggedIn()` across `SplashActivity`, `LoginActivity`, `RegisterActivity`, `AddItemActivity`, `ProductDetailsActivity`, `ProfileActivity`, `EcoImpactActivity`, `FirestoreProductService`, and `InteractionService` so the APK works immediately on physical phones without requiring `google-services.json`.
- Renamed root project from `"My Application"` to `"Ghor_Cycle"` in `settings.gradle`.
- Renamed application package, namespace, and `applicationId` from `com.example.myapplication` to **`com.example.ghor_cycle`** across `app/build.gradle`, `AndroidManifest.xml`, `themes.xml` (`Theme.GhorCycle`), all 31 main Java classes, all 10 unit test classes, and `ExampleInstrumentedTest.java`.
- Verified all **45 unit tests pass (100%)** and built fresh debug APK to `C:\Users\Asus\OneDrive\Desktop\GhorCycle.apk`.
