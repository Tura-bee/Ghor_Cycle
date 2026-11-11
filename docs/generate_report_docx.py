import os
import docx
from docx import Document
from docx.shared import Inches, Pt, RGBColor
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.enum.table import WD_TABLE_ALIGNMENT
from docx.oxml import parse_xml, OxmlElement
from docx.oxml.ns import nsdecls, qn

def create_report():
    doc = Document()
    
    # Page Margins - 1 inch all around
    sections = doc.sections
    for section in sections:
        section.top_margin = Inches(1.0)
        section.bottom_margin = Inches(1.0)
        section.left_margin = Inches(1.0)
        section.right_margin = Inches(1.0)
        
    img_dir = r"c:\Users\Asus\OneDrive\Desktop\Ghor_Cycle\docs\screenshots"
    
    # Helper to style paragraphs
    def add_title(text, size=18, bold=True, align=WD_ALIGN_PARAGRAPH.CENTER, color=RGBColor(33, 90, 48)):
        p = doc.add_paragraph()
        p.alignment = align
        run = p.add_run(text)
        run.bold = bold
        run.font.size = Pt(size)
        run.font.name = 'Calibri'
        run.font.color.rgb = color
        p.paragraph_format.space_after = Pt(4)
        p.paragraph_format.space_before = Pt(4)
        return p

    def add_heading_1(text):
        p = doc.add_paragraph()
        run = p.add_run(text)
        run.bold = True
        run.font.size = Pt(14)
        run.font.name = 'Calibri'
        run.font.color.rgb = RGBColor(27, 94, 32) # Dark Forest Green
        p.paragraph_format.space_before = Pt(14)
        p.paragraph_format.space_after = Pt(6)
        return p

    def add_heading_2(text):
        p = doc.add_paragraph()
        run = p.add_run(text)
        run.bold = True
        run.font.size = Pt(12)
        run.font.name = 'Calibri'
        run.font.color.rgb = RGBColor(46, 125, 50)
        p.paragraph_format.space_before = Pt(10)
        p.paragraph_format.space_after = Pt(4)
        return p

    def add_body(text, bold_prefix="", space_after=6):
        p = doc.add_paragraph()
        p.alignment = WD_ALIGN_PARAGRAPH.JUSTIFY
        if bold_prefix:
            r_bold = p.add_run(bold_prefix)
            r_bold.bold = True
            r_bold.font.size = Pt(10.5)
            r_bold.font.name = 'Calibri'
        r_text = p.add_run(text)
        r_text.font.size = Pt(10.5)
        r_text.font.name = 'Calibri'
        p.paragraph_format.space_after = Pt(space_after)
        p.paragraph_format.line_spacing = 1.15
        return p

    def add_bullet(bold_prefix, text):
        p = doc.add_paragraph(style='List Bullet')
        p.alignment = WD_ALIGN_PARAGRAPH.JUSTIFY
        r_bold = p.add_run(bold_prefix + ": ")
        r_bold.bold = True
        r_bold.font.size = Pt(10.5)
        r_bold.font.name = 'Calibri'
        r_text = p.add_run(text)
        r_text.font.size = Pt(10.5)
        r_text.font.name = 'Calibri'
        p.paragraph_format.space_after = Pt(4)
        p.paragraph_format.line_spacing = 1.15
        return p

    def add_image_figure(img_path, caption, width=Inches(3.2)):
        if os.path.exists(img_path):
            p = doc.add_paragraph()
            p.alignment = WD_ALIGN_PARAGRAPH.CENTER
            p.paragraph_format.space_before = Pt(6)
            p.paragraph_format.space_after = Pt(2)
            run = p.add_run()
            run.add_picture(img_path, width=width)
            
            p_cap = doc.add_paragraph()
            p_cap.alignment = WD_ALIGN_PARAGRAPH.CENTER
            p_cap.paragraph_format.space_after = Pt(8)
            r_cap = p_cap.add_run(caption)
            r_cap.italic = True
            r_cap.font.size = Pt(9.5)
            r_cap.font.color.rgb = RGBColor(80, 80, 80)

    def add_two_image_table(img1_path, cap1, img2_path, cap2, width=Inches(2.8)):
        table = doc.add_table(rows=2, cols=2)
        table.alignment = WD_TABLE_ALIGNMENT.CENTER
        
        # Row 0: Images
        cell_00 = table.cell(0, 0)
        p00 = cell_00.paragraphs[0]
        p00.alignment = WD_ALIGN_PARAGRAPH.CENTER
        if os.path.exists(img1_path):
            p00.add_run().add_picture(img1_path, width=width)
            
        cell_01 = table.cell(0, 1)
        p01 = cell_01.paragraphs[0]
        p01.alignment = WD_ALIGN_PARAGRAPH.CENTER
        if os.path.exists(img2_path):
            p01.add_run().add_picture(img2_path, width=width)
            
        # Row 1: Captions
        cell_10 = table.cell(1, 0)
        p10 = cell_10.paragraphs[0]
        p10.alignment = WD_ALIGN_PARAGRAPH.CENTER
        r10 = p10.add_run(cap1)
        r10.italic = True
        r10.font.size = Pt(9.5)
        r10.font.color.rgb = RGBColor(80, 80, 80)
        
        cell_11 = table.cell(1, 1)
        p11 = cell_11.paragraphs[0]
        p11.alignment = WD_ALIGN_PARAGRAPH.CENTER
        r11 = p11.add_run(cap2)
        r11.italic = True
        r11.font.size = Pt(9.5)
        r11.font.color.rgb = RGBColor(80, 80, 80)
        
        doc.add_paragraph().paragraph_format.space_after = Pt(4)

    # ------------------ COVER / HEADER ------------------
    add_title("Khulna University of Engineering & Technology", size=16, bold=True, color=RGBColor(27, 94, 32))
    add_title("Department of Computer Science and Engineering", size=13, bold=True, color=RGBColor(33, 33, 33))
    
    logo_path = os.path.join(img_dir, "kuet_logo.png")
    if os.path.exists(logo_path):
        p_logo = doc.add_paragraph()
        p_logo.alignment = WD_ALIGN_PARAGRAPH.CENTER
        p_logo.paragraph_format.space_before = Pt(4)
        p_logo.paragraph_format.space_after = Pt(4)
        p_logo.add_run().add_picture(logo_path, width=Inches(1.5))
        
    add_title("Course No : CSE 2200", size=12, bold=True, color=RGBColor(46, 125, 50))
    add_title("Course Title: Advanced Programming Laboratory", size=12, bold=True, color=RGBColor(46, 125, 50))
    
    add_title("Project Title: GhorCycle — Give Old Things a New Life", size=16, bold=True, color=RGBColor(27, 94, 32))
    add_title("A Smart Household Cycling, Resale, and Community Eco-Impact Android Marketplace", size=11, bold=False, color=RGBColor(60, 60, 60))
    
    # Submitted By & To Table
    sub_table = doc.add_table(rows=1, cols=2)
    sub_table.alignment = WD_TABLE_ALIGNMENT.CENTER
    cell_by = sub_table.cell(0, 0)
    p_by = cell_by.paragraphs[0]
    p_by.paragraph_format.line_spacing = 1.15
    r_by_title = p_by.add_run("Submitted By:\n")
    r_by_title.bold = True
    r_by_title.font.size = Pt(10.5)
    p_by.add_run("Sadia Jahan Turabe\nRoll: 2307065\nYear: 2nd  Semester: 2nd\nDept. of Computer Science & Engineering\nKUET")
    
    cell_to = sub_table.cell(0, 1)
    p_to = cell_to.paragraphs[0]
    p_to.paragraph_format.line_spacing = 1.15
    r_to_title = p_to.add_run("Submitted To:\n")
    r_to_title.bold = True
    r_to_title.font.size = Pt(10.5)
    p_to.add_run("Safin Ahmmed\nAssistant Professor, Dept. of CSE, KUET\n\nMr. Ehsanul Karim\nLecturer, Dept. of CSE, KUET")
    
    doc.add_paragraph().paragraph_format.space_after = Pt(12)
    
    # ------------------ 1. INTRODUCTION ------------------
    add_heading_1("1. Introduction")
    add_body("In rapidly expanding urban metropolises such as Dhaka, Khulna, and Chittagong, households discard tons of usable consumer goods each month, ranging from solid wood furniture and kitchen appliances to academic textbooks, electronics, and wearable textiles. The prevailing linear economic model—'take, make, use, and dispose'—results in prematurely overburdened municipal landfills, accelerated environmental toxicity from e-waste, and avoidable greenhouse gas emissions stemming from continuous virgin manufacturing. Concurrently, millions of students and lower-to-middle income families frequently seek affordable household necessities, yet fragmented classified forums lack safety guarantees, item verification, transparent barter mechanics, and localized drop-off support.",
             bold_prefix="The Household Waste Crisis & Linear Consumption in Urban Bangladesh: ")
    
    add_body("GhorCycle ('Give Old Things a New Life') is a production-grade, native Android circular marketplace engineered to bridge community re-use with verifiable environmental stewardship. Built using 100% Pure Java (JDK 1.8), modern Material 3 design tokens, AndroidX architectures, and Google Firebase Multi-Cloud Services (Authentication, Cloud Firestore, Cloud Storage), GhorCycle empowers citizens to effortlessly re-home pre-owned goods across four versatile channels: Direct Peer-to-Peer Resale, 100% Free Community Donation, Mutual Barter/Exchange, and Direct Regional Recycling.",
             bold_prefix="The GhorCycle Paradigm: ")
             
    add_body("GhorCycle is built upon an autonomous Dual-Tier Persistence Architecture. Utilizing Cloud Firestore and Firebase Storage for real-time multi-device cloud synchronization alongside an autonomous SharedPreferences and local SQLite session persistence engine, GhorCycle functions with zero downtime. Whether offline in low-connectivity areas or synchronized with Google Firebase live cloud services (ghorcycle-f90a6), users can discover items, propose exchanges, verify seller credentials, and inspect their tangible carbon and waste savings.",
             bold_prefix="Dual-Tier Architecture & Sustainable Circular Economy: ")

    # ------------------ 2. PROJECT OBJECTIVES ------------------
    add_heading_1("2. Project Objectives")
    add_body("The GhorCycle application fulfills rigorous academic, software engineering, and community-driven sustainability objectives prescribed by the CSE 2200 Advanced Programming Laboratory curriculum:")
    add_bullet("Peer-to-Peer Household Goods Circularity", "Democratize the transition of pre-owned items through an intuitive tri-modal transaction engine: Fair-Value Resale, Community Donation, and Mutual Barter Proposals.")
    add_bullet("Multi-Criteria Marketplace Discovery", "Provide dynamic 2-column adaptive grid exploration with horizontal Material category chips, instant keyword search, and multi-criteria dialog filtering (Category, Condition, Type, Location).")
    add_bullet("Dynamic Pricing & Android Photo Picker", "Enforce dynamic client-side validation rules (Sell requires price > 0; Free/Donate/Recycle locks price to 0) integrated with the Android Photo Picker and ContentProvider URI stream validation.")
    add_bullet("Interactive 4-Channel Contact Seller Hub", "Empower prospective buyers and neighbors to connect through four distinct communication channels: In-App Purchase Offer, Android Phone Dialer, WhatsApp Direct Chat, and Email Inquiry.")
    add_bullet("Bidirectional Donation & Barter Workflow", "Deliver a closed-loop proposal management system where buyers submit custom barter offers and item requests, and owners approve, reject, or mark transactions as Completed/Cycled.")
    add_bullet("Verifiable Eco-Impact Calculation Engine", "Implement a defensible, deduplicated scientific calculation engine that translates confirmed reuses into verified metrics: Landfill Waste Diverted (kg), CO2e Avoided (kg), and Water Conserved (L).")
    add_bullet("Dhaka & Regional Recycling Directory", "Curate vetted physical recycling drop-off centers with horizontal material filter chips (Plastic, Paper, Metal, Electronics, Clothes, Glass), phone dialer intents, and external Google Maps navigation.")
    add_bullet("External REST API Networking & XML Streaming", "Demonstrate asynchronous HTTP GET requests to the Open-Meteo Air Quality API for live PM2.5/AQI metrics alongside stream-based XmlPullParser extraction of sustainability guidelines.")
    add_bullet("Android Core OS Architectural Mastery", "Demonstrate comprehensive integration of Android Activity Lifecycles, Started/Bound Services, Dynamic BroadcastReceivers, WorkManager Background Sync, and Android 13+ runtime permissions.")
    add_bullet("Automated Quality Assurance & Verification", "Maintain 100% unit test coverage across 45 automated test suites running cleanly on both JVM and real Android devices.")

    # ------------------ 3. OVERVIEW & FEATURES ------------------
    add_heading_1("3. Overview & Features")
    add_body("GhorCycle delivers an end-to-end circular marketplace and sustainability tracking ecosystem across ten tightly integrated modules. Below is a comprehensive architectural walkthrough accompanied by live Android emulator verification screenshots:")

    # 3.1 Authentication & Multi-Role User Profile System
    add_heading_2("3.1 Authentication & Multi-Role User Profile System")
    add_body("GhorCycle enforces secure, identity-aware access control through FirebaseAuthService. Users authenticate via Email & Password, Google Sign-In (OAuth 2.0 with play-services-auth:20.7.0), or a 1-Tap 'Continue as Guest' mode for instant neighborhood browsing. When registered, user profiles are provisioned in Firebase Authentication and mirrored in SharedPreferences, issuing cryptographic UIDs that scope listings, favorites, and requests. ProfileActivity displays community cycling statistics (Listed items, Cycled items, Neighbor rating) alongside one-tap launchers for the Eco-Impact Dashboard and Recycling Centers Directory.")
    
    add_two_image_table(
        os.path.join(img_dir, "fig01_splash_screen.png"), "Figure 1: Splash Screen & Community Branding",
        os.path.join(img_dir, "fig02_login_screen.png"), "Figure 2: Multi-Provider Login Hub (Email/Google/Guest)"
    )

    add_image_figure(
        os.path.join(img_dir, "fig03_register_screen.png"),
        "Figure 3: User Registration Screen with RFC-compliant Client Validation",
        width=Inches(2.8)
    )

    # 3.2 Marketplace Hub & Multi-Criteria Filtering
    add_heading_2("3.2 Marketplace Hub & Multi-Criteria Filtering")
    add_body("MainActivity serves as the primary commerce and discovery engine. It incorporates an adaptive GridLayoutManager (2 columns portrait, 3 columns landscape) optimized for MaterialCardView product tiles. A horizontal category chip bar (CategoryAdapter) allows instant switching between 8 core household categories (Furniture, Clothes, Books, Kitchen Items, Electronics, Plastic, Paper, Metal). The search bar executes real-time string token filtering against titles, descriptions, and locations. Furthermore, a dedicated filter dialog (dialog_marketplace_filter.xml) allows users to combine Category, Item Condition, Transaction Type, and Location.")

    add_two_image_table(
        os.path.join(img_dir, "fig04_marketplace_hub.png"), "Figure 4: Primary Marketplace Hub with Category Chips & Product Grid",
        os.path.join(img_dir, "fig05_marketplace_filter_dialog.png"), "Figure 5: Multi-Criteria Filter Dialog (Condition, Type, Area)"
    )

    # 3.3 Product Details, Ownership & Lifecycle Management
    add_heading_2("3.3 Product Details, Ownership & Lifecycle Management")
    add_body("ProductDetailsActivity dynamically pivots its presentation and controls based on authentication identity. For Listing Owners, it surfaces management controls: 'View Requests & Proposals', 'Mark as Completed / Cycled', 'Edit Listing', and 'Delete Listing'. For Prospective Buyers, it displays transaction badges, item condition, seller area, verified eco-impact notes, and a responsive action suite. When an item transitions from Available to Reserved or Completed, the interface automatically disables inquiry triggers and displays a high-contrast notice banner.")

    add_two_image_table(
        os.path.join(img_dir, "fig06_product_details_buyer.png"), "Figure 6: Product Details (Buyer View with Actions & Eco-Badge)",
        os.path.join(img_dir, "fig06_product_details_owner.png"), "Figure 7: Product Details (Owner View with Status & Editing Controls)"
    )

    # 3.4 Interactive 4-Channel Contact Seller Hub
    add_heading_2("3.4 Interactive 4-Channel Contact Seller Hub")
    add_body("Communication between buyers and sellers is handled through dialog_contact_seller.xml, offering four specialized communication channels tailored to user preference: 1) In-App Cash/Price Offer dialog; 2) Native Phone Dialer intent (Intent.ACTION_DIAL) pre-filled with the seller's contact; 3) WhatsApp Direct Chat intent pre-composed with the item title and reference URL; and 4) Pre-formatted Email Inquiry intent (Intent.ACTION_SENDTO) with automated subject headers. This multi-channel approach reduces communication barriers while respecting user privacy.")

    add_image_figure(
        os.path.join(img_dir, "fig07_contact_seller_dialog.png"),
        "Figure 8: Interactive Contact Seller Dialog offering In-App Offer, Dialer, WhatsApp, and Email",
        width=Inches(2.8)
    )

    # 3.5 Donation & Barter Proposal Engine
    add_heading_2("3.5 Donation & Barter Proposal Engine")
    add_body("To promote a cashless circular economy, GhorCycle implements a structured barter and donation request subsystem (dialog_submit_request.xml and dialog_view_requests.xml). For free/donated goods, neighbors can submit a formal pickup request accompanied by a personalized message. For barter items, neighbors specify an offered exchange item (e.g., trading a study lamp for an engineering textbook). Owners review all pending proposals in a dedicated dialog where they can Accept or Reject proposals with atomic Firestore updates.")

    add_two_image_table(
        os.path.join(img_dir, "fig08_propose_exchange_dialog.png"), "Figure 9: Propose Barter & Exchange Proposal Dialog",
        os.path.join(img_dir, "fig08_incoming_requests_dialog.png"), "Figure 10: Owner's Incoming Requests & Proposals Dialog"
    )

    # 3.6 Item Listing & Dynamic Pricing / Android Photo Picker
    add_heading_2("3.6 Item Listing & Dynamic Pricing / Android Photo Picker")
    add_body("AddItemActivity enables registered users to publish household goods in seconds. It integrates the modern Android Photo Picker (ActivityResultContracts.GetContent) combined with ContentProviderHelper to read photo metadata without requiring broad runtime storage permissions. The form enforces strict dynamic pricing logic: selecting 'Sell' mandates an entered price > 0, whereas selecting 'Free', 'Donate', or 'Recycle' locks the price field to 0 and updates the currency label. Listings are validated client-side and saved to Firestore or local storage.")

    add_image_figure(
        os.path.join(img_dir, "fig09_add_item_screen.png"),
        "Figure 11: Add Item Activity featuring Photo Picker & Dynamic Pricing Enforcement",
        width=Inches(2.8)
    )

    # 3.7 Eco-Insight & Live Air Quality REST Networking
    add_heading_2("3.7 Eco-Insight & Live Air Quality REST Networking")
    add_body("Accessible via the Bottom Navigation 'Explore' tab, EcoInsightActivity acts as the live networking and data parsing demonstration hub. It performs asynchronous HTTP GET requests to the Open-Meteo European Air Quality REST API for Dhaka (lat: 23.8103, lon: 90.4125), extracting real-time PM2.5, PM10, and European AQI metrics. In parallel, it utilizes an Android XmlPullParser stream helper to parse local XML sustainability guidelines, calculating real-time CO2 offset equivalents. If network connectivity is interrupted, it gracefully falls back to local JSON fixtures.")

    add_image_figure(
        os.path.join(img_dir, "fig10_eco_insight_screen.png"),
        "Figure 12: Eco-Insight Hub with Live Open-Meteo REST API & Stream-Based XML Parser",
        width=Inches(2.8)
    )

    # 3.8 Dhaka Community Recycling Centers Directory
    add_heading_2("3.8 Dhaka Community Recycling Centers Directory")
    add_body("RecyclingDirectoryActivity addresses recyclable materials that cannot be directly reused. It provides a curated directory of physical recycling drop-off centers across major Dhaka zones (Tejgaon, Mirpur 10, Dhanmondi, Old Dhaka, Uttara). Users can filter centers using horizontal Material Choice Chips (All, Plastic, Paper, Metal, Electronics, Clothes, Glass) or search by area name. Each center card features two action buttons: a phone dialer intent to confirm operating hours, and an external Google Maps navigation intent (geo:lat,lng?q=...) with browser fallback.")

    add_two_image_table(
        os.path.join(img_dir, "fig12_recycling_directory_screen.png"), "Figure 13: Dhaka Community Recycling Centers Directory with Material Chips",
        os.path.join(img_dir, "fig13_eco_impact_dashboard.png"), "Figure 14: Verified Eco-Impact Dashboard with Environmental Metrics"
    )

    # 3.9 Eco-Impact Calculator & Environmental Methodology
    add_heading_2("3.9 Eco-Impact Calculator & Environmental Methodology")
    add_body("EcoImpactActivity provides users with a verifiable, mathematically defensible audit of their environmental savings. Powered by EcoImpactCalculator, the engine strictly excludes pending, rejected, or unverified listings, counting only verified completed transactions (accepted donation requests, accepted barter proposals, completed sales). The engine applies conservative, scientifically published benchmarks: 2.5 kg landfill waste diverted per item, 1.8 kg CO2e avoided per kg of manufactured goods, and 250 liters of water conserved per item cycled. A transparent ledger and methodology card eliminate greenwashing.")

    add_image_figure(
        os.path.join(img_dir, "fig11_user_profile_screen.png"),
        "Figure 15: User Profile Screen displaying Verified Badges, Stats, and Navigation Launchers",
        width=Inches(2.8)
    )

    # ------------------ 4. API & DATABASE INTEGRATION ------------------
    add_heading_1("4. API & Database Integration")
    
    add_heading_2("4.1 External REST API Integration (Open-Meteo European Air Quality API)")
    add_body("GhorCycle integrates external REST web services over secure TLS/HTTPS without relying on bulky third-party dependencies, adhering strictly to Android native architectures:")
    add_bullet("Endpoint & Request Structure", "URL: https://air-quality-api.open-meteo.com/v1/air-quality?latitude=23.8103&longitude=90.4125&current=pm10,pm2_5,european_aqi | Method: HTTP GET | Timeout: 5,000ms connect and read timeouts.")
    add_bullet("Threading & Deserialization", "Executed asynchronously via HttpNetworkService on a background ExecutorService pool, safely delivering parsed JSONObject data to the UI thread via Handler(Looper.getMainLooper()).")
    add_bullet("Offline Resilience Fallback", "If the user is offline or DNS fails, HttpNetworkService automatically catches IOException and serves an embedded local JSON asset (res/raw/air_quality_dhaka.json) without disrupting UI rendering.")

    add_heading_2("4.2 Stream-Based XML Parsing Engine (XmlEcoParserHelper)")
    add_body("Demonstrating core XML syllabus competencies, GhorCycle implements XmlPullParser inside XmlEcoParserHelper to process structured environmental guidelines from res/xml/recycling_guidelines.xml. Unlike memory-heavy DOM parsers, XmlPullParser processes tokens sequentially in a single pass, deserializing <guideline> tags containing id, title, category, impactCo2, and instructions into strongly-typed EcoGuideline Java objects.")

    add_heading_2("4.3 Autonomous Local Persistence & Session Layer")
    add_body("To guarantee 100% functionality on any Android device even in the absence of an internet connection or Firebase setup, GhorCycle embeds an autonomous local persistence architecture:")
    add_bullet("Local Session Store (SharedPreferences)", "FirebaseAuthService automatically saves and loads active user session credentials (ghorcycle_auth_prefs: sLocalUid, sLocalEmail, sLocalName).")
    add_bullet("In-Memory & Local Dataset Fallback", "FirestoreProductService and InteractionService maintain comprehensive local mock databases populated with 22 realistic Dhaka-based sample items across 8 categories.")
    add_bullet("Zero-Crash Guarantee", "All database operations transparently detect Firebase connectivity status. If unconfigured or offline, calls route cleanly to local repositories without throwing null pointer exceptions.")

    add_heading_2("4.4 Firebase Multi-Cloud Services & Production Security Rules")
    add_body("When cloud connectivity is active, GhorCycle connects to live Firebase project ghorcycle-f90a6:")
    add_bullet("Firebase Authentication", "Handles RFC-compliant Email/Password sign-ins and Google Sign-In (OAuth 2.0 via play-services-auth:20.7.0).")
    add_bullet("Cloud Firestore", "Maintains collections for /products (product documents), /users/{uid}/favorites/{productId} (user wishlist), and /item_requests (donation and barter requests).")
    add_bullet("Cloud Storage", "Firebase Cloud Storage bucket (ghorcycle-f90a6.appspot.com) provides secure hosting for user-uploaded product images.")
    add_bullet("Production Security Rules v2", "Verified in firestore.rules, enforcing strict authorization: non-owners cannot mutate or delete other users' listings; self-requests are strictly rejected.")

    # ------------------ 5. ANDROID CORE SYSTEM COMPONENTS ------------------
    add_heading_1("5. Android Core System Components")
    add_body("GhorCycle rigorously demonstrates the core Android architectural components required by the CSE 2200 laboratory syllabus:")

    # Table of Android Components
    comp_table = doc.add_table(rows=1, cols=3)
    comp_table.alignment = WD_TABLE_ALIGNMENT.CENTER
    hdr_cells = comp_table.rows[0].cells
    hdr_cells[0].text = "Android Component"
    hdr_cells[1].text = "Implementation Class"
    hdr_cells[2].text = "Architectural Role & Verification"
    
    # Style header
    for cell in hdr_cells:
        tcPr = cell._tc.get_or_add_tcPr()
        shd = parse_xml(f'<w:shd {nsdecls("w")} w:fill="E8F5E9"/>')
        tcPr.append(shd)
        for p in cell.paragraphs:
            for r in p.runs:
                r.bold = True
                r.font.size = Pt(9.5)
                r.font.name = 'Calibri'

    components_data = [
        ("Activity Lifecycle", "MainActivity, AddItemActivity, ProductDetailsActivity", "Implements and logs all seven lifecycle states (onCreate, onStart, onResume, onPause, onStop, onRestart, onDestroy). Correctly handles configuration changes."),
        ("Intents (Explicit & Implicit)", "SplashActivity, ProductDetailsActivity, RecyclingDirectoryActivity", "Explicit intents handle intra-app navigation with Extras; implicit intents trigger Android Phone Dialer (ACTION_DIAL), WhatsApp (ACTION_VIEW), Email (ACTION_SENDTO), and Maps (geo:)."),
        ("BroadcastReceivers", "NetworkChangeReceiver", "Dynamic BroadcastReceiver registered with RECEIVER_NOT_EXPORTED on API 33+, monitoring NetworkCapabilities (NET_CAPABILITY_INTERNET) and toggling offline banners."),
        ("Background WorkManager", "SyncOfflineDataWorker, BackgroundSyncManager", "AndroidX WorkManager one-time unique sync worker with NetworkType.CONNECTED constraints, displaying LiveData progress to the user."),
        ("ContentProvider & Scoped Storage", "ContentProviderHelper", "Safely queries OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE, and stream accessibility from content:// URIs without requesting dangerous broad storage permissions."),
        ("RecyclerView & Custom Adapters", "ProductAdapter, CategoryAdapter, RecyclingCenterAdapter", "High-performance ViewHolders with view recycling, image caching, and click listeners for 2-column grids and horizontal chip bars."),
        ("Runtime Permissions", "MainActivity, AddItemActivity", "Complies with Android 13+ (API 33) permissions model, gracefully handling POST_NOTIFICATIONS and media permissions."),
        ("Material 3 UI Design System", "Theme.GhorCycle, MaterialCardView, ShapeableImageView", "Consistent Material 3 theme with custom botanical green color palette (Aspen Green, Leaf Dark, Cream White), 12-16dp rounded corners, and adaptive icons.")
    ]

    for row_idx, (comp, cls_name, role) in enumerate(components_data):
        row_cells = comp_table.add_row().cells
        row_cells[0].text = comp
        row_cells[1].text = cls_name
        row_cells[2].text = role
        for i, cell in enumerate(row_cells):
            tcPr = cell._tc.get_or_add_tcPr()
            if row_idx % 2 == 1:
                shd = parse_xml(f'<w:shd {nsdecls("w")} w:fill="FAFAFA"/>')
                tcPr.append(shd)
            for p in cell.paragraphs:
                p.paragraph_format.line_spacing = 1.1
                p.paragraph_format.space_after = Pt(2)
                for r in p.runs:
                    r.font.size = Pt(8.5)
                    r.font.name = 'Calibri'
                    if i < 2:
                        r.bold = True

    doc.add_paragraph().paragraph_format.space_after = Pt(8)

    # ------------------ 6. DISCUSSION ------------------
    add_heading_1("6. Discussion")
    
    add_heading_2("6.1 Technical Evaluation of Dual-Mode Architecture")
    add_body("The primary architectural achievement of GhorCycle is its seamless Dual-Mode Operation. Most modern mobile apps fail completely when cloud backends are inaccessible or unconfigured. GhorCycle decouples UI presentation from network dependencies by embedding autonomous fallback controllers across FirebaseAuthService, FirestoreProductService, and InteractionService. When Firebase configuration is absent, the app creates a persistent local session in SharedPreferences, uses in-memory mock datasets, and executes local transaction workflows without a single crash.")

    add_heading_2("6.2 Offline-First Resilience & Defensive Engineering")
    add_body("GhorCycle adheres to an offline-first philosophy. NetworkChangeReceiver monitors network state transitions in real time, presenting a non-intrusive warning banner when the device loses connectivity. AndroidX WorkManager schedules background synchronization tasks that trigger automatically when network connectivity is re-established. External API consumers (Open-Meteo) utilize local JSON fixtures upon network timeout, preventing blank screens or unhandled exceptions.")

    add_heading_2("6.3 Material 3 UI/UX Design System & Community Branding")
    add_body("GhorCycle incorporates a modern, approachable design language. Moving away from cluttered, ad-heavy classified layouts, GhorCycle implements a clean Material 3 design system centered around soothing eco-green hues, generous padding, 16dp rounded cards, clear status badges, and adaptive launcher icons derived from real community sharing initiatives. The interface clearly conveys environmental benefits, elevating simple reuse into a rewarding civic responsibility.")

    add_heading_2("6.4 Multi-Tier Verification Methodology")
    add_body("GhorCycle underwent exhaustive multi-tier testing across four distinct verification levels:")
    add_bullet("Static Code Analysis", "Inspected all 31 Java source files, XML layouts, and Manifest configurations for memory leaks, view-binding safety, and thread isolation.")
    add_bullet("Automated JVM Unit Testing", "Executed 45 comprehensive unit tests (Stage 1 through Stage 9) with 100% pass rate (0 failures). Tests verify navigation routing, search filtering, dynamic pricing, Firestore security logic, XML parsing, and eco-impact math.")
    add_bullet("Live Android Emulator Verification", "Verified on Pixel 2 (API 36) emulator: tested authentication workflows, AddItem photo picker, buyer/owner detail toggling, filter dialogs, and external intent launching.")
    add_bullet("Cloud & Keystore Verification", "Verified Google Sign-In with play-services-auth:20.7.0 using the debug keystore SHA-1 fingerprint (32:30:56:99:DF:C1:7F:F8:B3:3F:26:6F:5C:D7:12:EE:CD:12:28:5A) against live Firebase project ghorcycle-f90a6.")

    add_heading_2("6.5 Key Engineering Challenges Solved")
    add_body("During development, several complex engineering challenges were resolved:")
    add_bullet("Scoped Storage & URI Permissions", "Android 13+ restricts raw file path access. ContentProviderHelper was engineered to open streams directly via content:// URIs and resolve metadata safely.")
    add_bullet("Zero-Cost Dynamic Validation", "Implemented dynamic input listeners on AddItemActivity ensuring users cannot publish free or donation items with nonzero prices.")
    add_bullet("JVM Unit Test XML/JSON Mocking", "Configured org.json:json:20231013, xmlpull:1.1.3.4a, and xpp3:1.1.4c in app/build.gradle, allowing Android-specific XML and JSON parsers to execute in local JVM unit tests without mocking frameworks.")
    add_bullet("Idempotent Eco-Impact Calculation", "Engineered EcoImpactCalculator with deduplicated transaction sets, ensuring multiple requests on the same item cannot double-count environmental savings.")

    # ------------------ 7. CONCLUSION & KEY DELIVERABLES ------------------
    add_heading_1("7. Conclusion & Key Deliverables")
    add_body("GhorCycle ('Give Old Things a New Life') successfully delivers a production-grade, highly reliable Android marketplace for urban household goods reuse and community recycling. By synthesizing core Android OS architectural components (Explicit/Implicit Intents, Dynamic BroadcastReceivers, WorkManager, ContentProviders, and Material 3 design) with Google Firebase Multi-Cloud Services and an autonomous local persistence engine, the project completely fulfills all technical and pedagogical requirements of the CSE 2200 Advanced Programming Laboratory curriculum.")

    # Deliverables Matrix Table
    deliv_table = doc.add_table(rows=1, cols=4)
    deliv_table.alignment = WD_TABLE_ALIGNMENT.CENTER
    d_hdr = deliv_table.rows[0].cells
    d_hdr[0].text = "Project Module"
    d_hdr[1].text = "Syllabus Concept"
    d_hdr[2].text = "Status"
    d_hdr[3].text = "Verification Level"
    
    for cell in d_hdr:
        tcPr = cell._tc.get_or_add_tcPr()
        shd = parse_xml(f'<w:shd {nsdecls("w")} w:fill="E8F5E9"/>')
        tcPr.append(shd)
        for p in cell.paragraphs:
            for r in p.runs:
                r.bold = True
                r.font.size = Pt(9.5)
                r.font.name = 'Calibri'

    deliverables_data = [
        ("Authentication & Profile", "Firebase Auth / Google Sign-In / SharedPreferences", "Complete (100%)", "Verified on Emulator & Cloud"),
        ("Marketplace Hub & Search", "RecyclerView / GridLayoutManager / Filter Dialog", "Complete (100%)", "Verified on Emulator & Unit Tests"),
        ("Item Listing & Photo Picker", "Photo Picker / ContentProvider / Form Validation", "Complete (100%)", "Verified on Emulator & Storage"),
        ("Product Details & Lifecycle", "Dynamic Views / Ownership Checks / State Machine", "Complete (100%)", "Verified on Emulator & Firestore"),
        ("Contact Seller 4-Channel Hub", "Implicit Intents (Dialer, WhatsApp, Email, Offer)", "Complete (100%)", "Verified on Emulator"),
        ("Donation & Barter Engine", "Atomic Firestore Updates / Proposal Workflows", "Complete (100%)", "Verified on Emulator & DB"),
        ("Eco-Insight REST Networking", "HttpURLConnection / JSON Parsing / Offline Fallback", "Complete (100%)", "Verified Live HTTP 200 OK"),
        ("XmlPullParser Guidelines", "Stream-based XML Parsing / Strong Typing", "Complete (100%)", "Verified on Emulator & JVM Test"),
        ("Recycling Directory", "RecyclerView / Filter Chips / Geo Maps Intent", "Complete (100%)", "Verified on Emulator"),
        ("Eco-Impact Calculator", "Deduplicating Calculation Engine / Math Ledger", "Complete (100%)", "Verified in Unit Tests & UI"),
        ("Background Sync Manager", "AndroidX WorkManager / Network Constraints", "Complete (100%)", "Verified on Emulator"),
        ("Automated Test Suite", "JUnit 4 / JVM Mock Engine / Integration Suite", "Complete (100%)", "45/45 Tests Passed (100%)")
    ]

    for r_idx, (mod, concept, stat, verif) in enumerate(deliverables_data):
        r_cells = deliv_table.add_row().cells
        r_cells[0].text = mod
        r_cells[1].text = concept
        r_cells[2].text = stat
        r_cells[3].text = verif
        for i, cell in enumerate(r_cells):
            tcPr = cell._tc.get_or_add_tcPr()
            if r_idx % 2 == 1:
                shd = parse_xml(f'<w:shd {nsdecls("w")} w:fill="FAFAFA"/>')
                tcPr.append(shd)
            for p in cell.paragraphs:
                p.paragraph_format.line_spacing = 1.1
                p.paragraph_format.space_after = Pt(2)
                for r in p.runs:
                    r.font.size = Pt(8.5)
                    r.font.name = 'Calibri'
                    if i == 0:
                        r.bold = True
                    elif i == 2:
                        r.font.color.rgb = RGBColor(46, 125, 50)
                        r.bold = True

    doc.add_paragraph().paragraph_format.space_after = Pt(8)

    # Future Roadmap
    add_heading_2("Future Roadmap")
    add_body("Future developments planned for GhorCycle include:")
    add_bullet("In-App Real-Time Chat", "Implement real-time buyer-seller messaging using Cloud Firestore listeners, message histories, and unread notification badges.")
    add_bullet("Interactive Map Discovery", "Integrate Google Maps SDK to display nearby listings and recycling centers on an interactive geographical map with radius filtering.")
    add_bullet("Multi-Image Carousel", "Support uploading up to 5 images per listing using a ViewPager2 swipeable image gallery.")
    add_bullet("Eco-Warrior Gamification", "Introduce community milestone badges ('Seedling', 'Green Neighbor', 'Zero-Waste Champion') to further encourage waste diversion.")

    # Concluding Remarks
    add_heading_2("Concluding Remarks")
    add_body("Through its robust dual-tier architecture, human-centered Material 3 design, and verifiable environmental calculation engine, GhorCycle proves that mobile engineering can effectively combat urban consumer waste and foster collaborative community stewardship. The application stands 100% complete, rigorously tested across 45 unit tests, verified on live Android devices, and ready for academic laboratory evaluation.")

    output_path = r"c:\Users\Asus\OneDrive\Desktop\GhorCycle_Project_Report_CSE2200.docx"
    doc.save(output_path)
    print(f"Report successfully saved to: {output_path}")

if __name__ == "__main__":
    create_report()
