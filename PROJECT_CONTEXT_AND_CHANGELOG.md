# GhorCycle — Project Context & Changelog

> **Purpose**: This file serves as the single source of truth for the **GhorCycle** Android application architecture, implemented stages, file inventory, data schemas, and chronological change history. Share or reference this file in any new chat session to immediately load the complete project context.

---

## 1. Project Overview
- **App Name**: GhorCycle ("Smart Household Cycling & Resale Marketplace")
- **Active Project Directory**: `c:\Users\Asus\AndroidStudioProjects\Ghor_Cycle_F` (migrated from `c:\Users\Asus\OneDrive\Desktop\Ghor_Cycle` outside OneDrive)
- **Package / Namespace**: `com.example.myapplication`
- **Language & Stack**: Java (JDK 1.8 target), Android SDK (`minSdk 24`, `compileSdk 33`, `targetSdk 33`), Material 3 (`1.5.0`), AndroidX AppCompat & ConstraintLayout, Firebase BoM (`32.8.0` — Auth, Firestore, Storage), AndroidX WorkManager (`2.8.1`).
- **Build Output Path**: Configured in root `build.gradle` to `C:/Users/Asus/.android_build/Ghor_Cycle/${project.name}`.
- **Firebase Status**: `google-services` plugin is prepared in `build.gradle` (currently commented in `app/build.gradle` until `google-services.json` is dropped into `app/`). All services (`FirebaseAuthService`, `FirestoreProductService`, `InteractionService`) include automatic runtime detection and seamless in-memory offline fallback caches when `google-services.json` is absent.

---

## 2. Architecture & Codebase Inventory

### Activities (`app/src/main/java/com/example/myapplication/`)
1. **`SplashActivity.java`** (`activity_splash.xml`)
   - Launcher entry point with 2.2s auto-routing timer & manual "Get Started" button.
   - Checks persistent `FirebaseAuthService.getCurrentUser()` state: routes authenticated users to `MainActivity` and unauthenticated users to `LoginActivity`.
2. **`LoginActivity.java`** (`activity_login.xml`)
   - Email & password sign-in via `FirebaseAuthService`, client-side validation, loading state, readable error banner, "Continue as Guest" mode, and link to `RegisterActivity`.
3. **`RegisterActivity.java`** (`activity_register.xml`)
   - New user registration with Full Name, Email, Phone, Password, Confirm Password. Updates Firebase user profile display name and routes to `MainActivity`.
4. **`MainActivity.java`** (`activity_main.xml`)
   - Primary marketplace hub with adaptive `GridLayoutManager` (2 columns portrait, 3 columns landscape), horizontal category chip bar (`CategoryAdapter`), real-time search bar, multi-criteria filter dialog (`dialog_marketplace_filter.xml`), favorites filter mode, offline connectivity banner (`NetworkChangeReceiver`), background sync trigger (`BackgroundSyncManager`), and `BottomNavigationView`.
5. **`AddItemActivity.java`** (`activity_add_item.xml`)
   - Authenticated item creation & owner editing/deletion screen.
   - Integrates Android Photo Picker (`ActivityResultContracts.GetContent`) + `ContentProviderHelper` URI metadata validation.
   - Enforces dynamic pricing (`Sell` requires price > 0; `Free`/`Donate`/`Recycle` lock price to 0).
6. **`ProductDetailsActivity.java`** (`activity_product_details.xml`)
   - Displays full product details, status badge (`Available`, `Reserved`, `Completed`), and eco-impact note.
   - **Owner View**: View incoming requests (`dialog_view_requests.xml`), Accept/Reject requests, Mark as Completed/Cycled, Edit listing, or Delete listing.
   - **Buyer View**: Request Free/Donated item or Propose Barter/Exchange (`dialog_submit_request.xml`), Contact Seller, Make Offer. Disables actions when item is `Reserved` or `Completed`.
7. **`ProfileActivity.java`** (`activity_profile.xml`)
   - Displays authenticated user's name, email, truncated UID (or Guest state), community cycling stats, My Active Listings button, Edit Profile button, and Sign Out / Switch Account action.
8. **`EcoInsightActivity.java`** (`activity_eco_insight.xml`)
   - Stage 7 Networking & Parsing hub accessible from the Bottom Navigation `Explore` tab.
   - Demonstrates live HTTP GET requests to Open-Meteo Air Quality API for Dhaka, offline JSON fixture loading, malformed JSON error handling, and `XmlPullParser` recycling guidelines parsing.

### Models & Adapters
- **`Product.java`**: Core marketplace model (`Serializable`) with 13 Firestore fields (`productId`, `ownerId`, `title`, `description`, `category`, `condition`, `transactionType`, `price`, `location`, `imageUrl`, `status`, `createdAt`, `updatedAt`) + UI helpers.
- **`ItemRequest.java`**: Donation/Exchange request model (`requestId`, `productId`, `productTitle`, `ownerId`, `requesterId`, `requesterEmail`, `requestType` [`DONATION`/`EXCHANGE`], `offeredItem`, `message`, `status` [`PENDING`/`ACCEPTED`/`REJECTED`/`CANCELLED`], `createdAt`, `updatedAt`).
- **`Category.java`**: Filter chip model (`name`, `iconResId`, `isSelected`).
- **`EcoAirQuality.java`**: Domain model for Dhaka air quality metrics (`pm25`, `pm10`, `europeanAqi`, `isFromLiveApi`, `httpStatusCode`) and contextual eco recommendations.
- **`EcoGuideline.java`**: Domain model for parsed XML recycling guidelines (`id`, `title`, `category`, `impactCo2`, `instructions`).
- **`ProductAdapter.java`** (`item_product_card.xml`): RecyclerView adapter for product grid cards with favorite heart toggle.
- **`CategoryAdapter.java`** (`item_category_chip.xml`): Horizontal RecyclerView adapter for the 9 category chips (`All` + 8 core categories).

### Services, Helpers & Background Components
- **`FirebaseAuthService.java`**: Wraps `FirebaseAuth` login, registration, sign-out, RFC email regex validation, password length validation (>= 6 chars), and exception translation.
- **`FirestoreProductService.java`**: Manages Firestore `products` collection CRUD, owner authorization checks, `FirebaseStorage` image upload with local URI fallback, and in-memory fallback dataset when Firebase is unconfigured.
- **`InteractionService.java`**: Manages `users/{userId}/favorites/{productId}` and `item_requests` collection, duplicate request prevention, owner accept/reject transitions (`Available` -> `Reserved`), and transaction completion (`Completed`).
- **`SampleDataProvider.java`**: Provides the 8 core categories (`Furniture`, `Clothes`, `Books`, `Kitchen Items`, `Electronics`, `Plastic`, `Paper`, `Metal`), 22 realistic Dhaka-based sample products, and multi-criteria filtering logic.
- **`NetworkChangeReceiver.java`**: Dynamic `BroadcastReceiver` (`RECEIVER_NOT_EXPORTED` on API 33+) with `NetworkCapabilities` validation (`NET_CAPABILITY_INTERNET` + `NET_CAPABILITY_VALIDATED`).
- **`BackgroundSyncManager.java` & `SyncOfflineDataWorker.java`**: AndroidX `WorkManager` unique one-time sync worker with `NetworkType.CONNECTED` constraint and `LiveData` status observation.
- **`ContentProviderHelper.java`**: Resolves `OpenableColumns.DISPLAY_NAME`, `OpenableColumns.SIZE`, and stream accessibility from `content://` URIs without requesting broad storage permissions.
- **`HttpNetworkService.java`**: Asynchronous `HttpURLConnection` REST client on a background `ExecutorService` with `Handler(Looper.getMainLooper())` callbacks and `JSONObject` deserialization.
- **`XmlEcoParserHelper.java`**: Stream-based `XmlPullParser` implementation parsing `<guidelines>` XML into `EcoGuideline` objects.

### Security Rules & Tests
- **`firestore.rules`**: Production-grade Firestore Security Rules v2 enforcing authentication, ownership checks, immutable identity fields, and schema validation for `/products/{productId}`, `/users/{userId}/favorites/{productId}`, and `/item_requests/{requestId}`.
- **Unit Tests (`app/src/test/java/com/example/myapplication/`)**:
  - `Stage1NavigationUnitTest.java`
  - `Stage2MarketplaceUnitTest.java`
  - `Stage3AuthUnitTest.java`
  - `Stage4FirestoreProductUnitTest.java`
  - `Stage5InteractionsUnitTest.java`
  - `Stage6AndroidComponentsUnitTest.java`
  - `Stage7NetworkingUnitTest.java`

---

## 3. Potential Areas for Debugging & Enhancement Identified During Codebase Review
1. **Saved Instance State Restoration**:
   - `LoginActivity`, `RegisterActivity`, `ProductDetailsActivity`, and `ProfileActivity` save values in `onSaveInstanceState()`, but some fields are not restored in `onCreate()` / `onRestoreInstanceState()`.
   - `MainActivity` saves `currentQuery`, `selectedCategory`, and `currentUserName`, but could also preserve `selectedCondition`, `selectedTransactionType`, `selectedLocation`, and `isFavoritesOnly`.
2. **Profile Screen Actions**:
   - `btnProfileEdit` ("Edit Profile") and `btnProfileListings` ("My Active Listings") in `ProfileActivity` currently show placeholder `Toast` messages rather than interactive dialogs or filtered views.
   - Profile statistics (`Listed: 6`, `Cycled: 14`, `Rating: 4.9 ★`) in `activity_profile.xml` are currently static placeholders and can be dynamically computed from the user's listings.
3. **ProductDetailsActivity Seller & Favorite State**:
   - When opening `ProductDetailsActivity` from `MainActivity`, `EXTRA_PRODUCT_SELLER` is populated with `product.getLocation()` instead of seller name + location, and `ProductDetailsActivity` does not preserve `productTransactionType` when editing if it was `Donate` or `Recycle` (it checks `isProductFree ? Product.TYPE_FREE : Product.TYPE_SELL`).
4. **Listener Registration Cleanup in Dialog**:
   - In `ProductDetailsActivity.showRequestsDialog()`, `InteractionService.listenToProductRequests(...)` returns a `ListenerRegistration` that should be removed when the dialog is dismissed to avoid a listener leak.

---

## 4. Chronological Changelog

### [2026-11-11] — Initial Context Load & Folder Switch to `Ghor_Cycle_F`
- Read and indexed the entire **GhorCycle** codebase across root Gradle configuration, `firestore.rules`, `AndroidManifest.xml`, all 21 Java classes, all 14 XML layouts/menus/values, and all 7 unit test suites.
- Verified that `c:\Users\Asus\AndroidStudioProjects\Ghor_Cycle_F` matches the full codebase and set it as the primary target directory for all upcoming edits and builds.
- Created `PROJECT_CONTEXT_AND_CHANGELOG.md` in `c:\Users\Asus\AndroidStudioProjects\Ghor_Cycle_F\PROJECT_CONTEXT_AND_CHANGELOG.md` (and synced with `c:\Users\Asus\OneDrive\Desktop\Ghor_Cycle\PROJECT_CONTEXT_AND_CHANGELOG.md`).
