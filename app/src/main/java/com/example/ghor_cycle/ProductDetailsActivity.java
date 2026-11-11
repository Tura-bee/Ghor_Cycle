package com.example.ghor_cycle;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseUser;

import java.util.List;

/**
 * ProductDetailsActivity displays comprehensive details for an item in GhorCycle.
 *
 * Implements:
 * 1. Extraction and presentation of Firestore document fields.
 * 2. Visual handling of priced vs FREE community items.
 * 3. Dynamic category icon or custom photo display.
 * 4. Strict owner protection: Only listing owner can see and use Edit & Delete actions.
 * 5. Public privacy: Never exposes private user credentials or phone numbers.
 * 6. Interactive click listeners for buyer-seller workflows.
 */
public class ProductDetailsActivity extends AppCompatActivity {

    private static final String TAG = "GhorCycle_Lifecycle";

    public static final String EXTRA_PRODUCT_ID = "com.ghorcycle.extra.PRODUCT_ID";
    public static final String EXTRA_PRODUCT_NAME = "com.ghorcycle.extra.PRODUCT_NAME";
    public static final String EXTRA_PRODUCT_PRICE = "com.ghorcycle.extra.PRODUCT_PRICE";
    public static final String EXTRA_PRODUCT_CATEGORY = "com.ghorcycle.extra.PRODUCT_CATEGORY";
    public static final String EXTRA_PRODUCT_SELLER = "com.ghorcycle.extra.PRODUCT_SELLER";
    public static final String EXTRA_PRODUCT_CONDITION = "com.ghorcycle.extra.PRODUCT_CONDITION";
    public static final String EXTRA_PRODUCT_DESCRIPTION = "com.ghorcycle.extra.PRODUCT_DESCRIPTION";
    public static final String EXTRA_PRODUCT_IMAGE_RES = "com.ghorcycle.extra.PRODUCT_IMAGE_RES";
    public static final String EXTRA_PRODUCT_IS_FREE = "com.ghorcycle.extra.PRODUCT_IS_FREE";
    public static final String EXTRA_PRODUCT_OWNER_ID = "com.ghorcycle.extra.PRODUCT_OWNER_ID";
    public static final String EXTRA_PRODUCT_IMAGE_URL = "com.ghorcycle.extra.PRODUCT_IMAGE_URL";
    public static final String EXTRA_PRODUCT_STATUS = "com.ghorcycle.extra.PRODUCT_STATUS";
    public static final String EXTRA_PRODUCT_TRANSACTION_TYPE = "com.ghorcycle.extra.PRODUCT_TRANSACTION_TYPE";

    private String productId;
    private String productName;
    private String productPrice;
    private String productCategory;
    private String productSeller;
    private String productCondition;
    private String productDescription;
    private int productImageRes = 0;
    private boolean isProductFree = false;
    private String ownerId = "";
    private String imageUrl = "";
    private String productStatus = Product.STATUS_AVAILABLE;
    private String productTransactionType = Product.TYPE_SELL;

    private TextView tvStatusBadge;
    private TextView tvUnavailableBanner;
    private ActivityResultLauncher<Intent> editLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "ProductDetailsActivity: onCreate() - Extracting Intent extras and inflating UI");
        setContentView(R.layout.activity_product_details);

        // Register edit launcher to refresh on edit/delete
        editLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        if (result.getData() != null && result.getData().hasExtra(AddItemActivity.EXTRA_EDIT_PRODUCT)) {
                            Product updated = (Product) result.getData().getSerializableExtra(AddItemActivity.EXTRA_EDIT_PRODUCT);
                            if (updated != null) {
                                refreshUIWithProduct(updated);
                            }
                        }
                    } else if (result.getResultCode() == RESULT_FIRST_USER) {
                        // Deleted
                        finish();
                    }
                }
        );

        // Extract extras passed from caller Intent
        if (getIntent() != null) {
            productId = getIntent().getStringExtra(EXTRA_PRODUCT_ID);
            productName = getIntent().getStringExtra(EXTRA_PRODUCT_NAME);
            productPrice = getIntent().getStringExtra(EXTRA_PRODUCT_PRICE);
            productCategory = getIntent().getStringExtra(EXTRA_PRODUCT_CATEGORY);
            productSeller = getIntent().getStringExtra(EXTRA_PRODUCT_SELLER);
            productCondition = getIntent().getStringExtra(EXTRA_PRODUCT_CONDITION);
            productDescription = getIntent().getStringExtra(EXTRA_PRODUCT_DESCRIPTION);
            productImageRes = getIntent().getIntExtra(EXTRA_PRODUCT_IMAGE_RES, 0);
            isProductFree = getIntent().getBooleanExtra(EXTRA_PRODUCT_IS_FREE, false);
            ownerId = getIntent().getStringExtra(EXTRA_PRODUCT_OWNER_ID);
            imageUrl = getIntent().getStringExtra(EXTRA_PRODUCT_IMAGE_URL);
            String statusExtra = getIntent().getStringExtra(EXTRA_PRODUCT_STATUS);
            if (!TextUtils.isEmpty(statusExtra)) {
                productStatus = statusExtra;
            }
            String typeExtra = getIntent().getStringExtra(EXTRA_PRODUCT_TRANSACTION_TYPE);
            if (!TextUtils.isEmpty(typeExtra)) {
                productTransactionType = typeExtra;
            }
        }

        // Apply fallbacks if any extra was not supplied
        if (TextUtils.isEmpty(productId)) productId = "GC-DEMO-00";
        if (TextUtils.isEmpty(productName)) productName = "GhorCycle Community Item";
        if (TextUtils.isEmpty(productPrice)) productPrice = "৳ 0";
        if (TextUtils.isEmpty(productCategory)) productCategory = "General";
        if (TextUtils.isEmpty(productSeller)) productSeller = "Dhanmondi, Dhaka";
        if (TextUtils.isEmpty(productCondition)) productCondition = "Pre-loved";
        if (TextUtils.isEmpty(productDescription)) {
            productDescription = "Quality verified household good looking for a second life.";
        }

        if (productPrice.equalsIgnoreCase("FREE") || productPrice.equalsIgnoreCase("৳ 0") || isProductFree) {
            isProductFree = true;
            productPrice = "FREE";
        }

        Log.d(TAG, "ProductDetailsActivity: Displaying item [ID=" + productId + ", Name=" + productName + "]");

        bindViewsAndPopulate();
        setupOwnerAndBuyerActions();

        // Register custom Back-press dispatcher
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                handleNavigateBack();
            }
        });
    }

    private void bindViewsAndPopulate() {
        TextView tvIdBadge = findViewById(R.id.tvDetailIdBadge);
        TextView tvName = findViewById(R.id.tvDetailName);
        TextView tvPrice = findViewById(R.id.tvDetailPrice);
        TextView tvFreeBadge = findViewById(R.id.tvDetailFreeBadge);
        tvStatusBadge = findViewById(R.id.tvDetailStatusBadge);
        TextView tvCategory = findViewById(R.id.tvDetailCategory);
        TextView tvCondition = findViewById(R.id.tvDetailCondition);
        TextView tvSeller = findViewById(R.id.tvDetailSeller);
        TextView tvDescription = findViewById(R.id.tvDetailDescription);
        ImageView ivImage = findViewById(R.id.ivDetailProductImage);

        Button btnBack = findViewById(R.id.btnDetailBack);
        Button btnBackHome = findViewById(R.id.btnDetailBackHome);

        tvIdBadge.setText("ID: " + productId);
        tvName.setText(productName);
        tvCategory.setText("Category: " + productCategory);
        tvCondition.setText(productCondition);
        tvSeller.setText("Location: " + productSeller);
        tvDescription.setText(productDescription);

        // Price & Free formatting
        if (isProductFree) {
            tvPrice.setText("FREE");
            tvPrice.setTextColor(ContextCompat.getColor(this, R.color.eco_free_badge));
            tvFreeBadge.setVisibility(View.VISIBLE);
        } else {
            tvPrice.setText(productPrice);
            tvPrice.setTextColor(ContextCompat.getColor(this, R.color.eco_green_primary));
            tvFreeBadge.setVisibility(View.GONE);
        }

        updateStatusUI();

        // Image handling: custom photo URI or category placeholder
        if (!TextUtils.isEmpty(imageUrl)) {
            try {
                ivImage.setImageURI(Uri.parse(imageUrl));
                ivImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
            } catch (Exception e) {
                ivImage.setImageResource(FirestoreProductService.getCategoryIconRes(productCategory));
            }
        } else if (productImageRes != 0) {
            ivImage.setImageResource(productImageRes);
        } else {
            ivImage.setImageResource(FirestoreProductService.getCategoryIconRes(productCategory));
        }
        ivImage.setContentDescription("Photo and illustration for " + productName);

        btnBack.setOnClickListener(v -> handleNavigateBack());
        btnBackHome.setOnClickListener(v -> handleNavigateBack());
    }

    private void updateStatusUI() {
        if (tvStatusBadge != null) {
            tvStatusBadge.setText("Status: " + productStatus);
            if (Product.STATUS_COMPLETED.equalsIgnoreCase(productStatus)
                    || Product.STATUS_SOLD.equalsIgnoreCase(productStatus)
                    || Product.STATUS_CYCLED.equalsIgnoreCase(productStatus)) {
                tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_condition);
                tvStatusBadge.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
            } else if (Product.STATUS_RESERVED.equalsIgnoreCase(productStatus)) {
                tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_free);
                tvStatusBadge.setTextColor(ContextCompat.getColor(this, R.color.white));
            } else {
                tvStatusBadge.setBackgroundResource(R.drawable.bg_role_tag);
                tvStatusBadge.setTextColor(ContextCompat.getColor(this, R.color.eco_green_dark));
            }
        }
    }

    private void setupOwnerAndBuyerActions() {
        LinearLayout layoutOwnerActions = findViewById(R.id.layoutOwnerActions);
        LinearLayout layoutBuyerActions = findViewById(R.id.layoutBuyerActions);

        Button btnEditListing = findViewById(R.id.btnDetailEditListing);
        Button btnDeleteListing = findViewById(R.id.btnDetailDeleteListing);
        Button btnViewRequests = findViewById(R.id.btnDetailViewRequests);
        Button btnMarkCompleted = findViewById(R.id.btnDetailMarkCompleted);

        tvUnavailableBanner = findViewById(R.id.tvDetailUnavailableBanner);
        Button btnRequestDonation = findViewById(R.id.btnDetailRequestDonation);
        Button btnProposeExchange = findViewById(R.id.btnDetailProposeExchange);
        Button btnContactSeller = findViewById(R.id.btnDetailContactSeller);
        Button btnMakeOffer = findViewById(R.id.btnDetailMakeOffer);

        String currentUid = FirebaseAuthService.getCurrentUserId();
        boolean isOwner = currentUid != null && !TextUtils.isEmpty(ownerId) && currentUid.equals(ownerId);

        if (isOwner) {
            layoutOwnerActions.setVisibility(View.VISIBLE);
            layoutBuyerActions.setVisibility(View.GONE);

            boolean isAlreadyCompleted = Product.STATUS_COMPLETED.equalsIgnoreCase(productStatus)
                    || Product.STATUS_SOLD.equalsIgnoreCase(productStatus)
                    || Product.STATUS_CYCLED.equalsIgnoreCase(productStatus);

            if (isAlreadyCompleted) {
                btnMarkCompleted.setEnabled(false);
                btnMarkCompleted.setText("✔ Transaction Completed");
            } else {
                btnMarkCompleted.setEnabled(true);
                btnMarkCompleted.setText("✔ Mark as Completed / Cycled");
                btnMarkCompleted.setOnClickListener(v -> markProductAsCompleted());
            }

            btnViewRequests.setOnClickListener(v -> showRequestsDialog());

            btnEditListing.setOnClickListener(v -> {
                Log.d(TAG, "ProductDetailsActivity: Owner opening Edit mode for ID=" + productId);
                Product editProduct = new Product(
                        productId,
                        ownerId,
                        productName,
                        productDescription,
                        productCategory,
                        productCondition,
                        isProductFree ? Product.TYPE_FREE : Product.TYPE_SELL,
                        isProductFree ? 0.0 : parsePrice(productPrice),
                        productSeller,
                        imageUrl,
                        productStatus
                );

                Intent editIntent = new Intent(ProductDetailsActivity.this, AddItemActivity.class);
                editIntent.putExtra(AddItemActivity.EXTRA_EDIT_PRODUCT, editProduct);
                editLauncher.launch(editIntent);
            });

            btnDeleteListing.setOnClickListener(v -> {
                new AlertDialog.Builder(this)
                        .setTitle("Delete Listing")
                        .setMessage("Are you sure you want to permanently delete this listing?")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            FirestoreProductService.deleteProduct(productId, ownerId, new FirestoreProductService.ProductDeleteCallback() {
                                @Override
                                public void onSuccess() {
                                    Toast.makeText(ProductDetailsActivity.this, "Listing deleted.", Toast.LENGTH_SHORT).show();
                                    finish();
                                }

                                @Override
                                public void onFailure(String errorMessage) {
                                    Toast.makeText(ProductDetailsActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                                }
                            });
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            });
        } else {
            layoutOwnerActions.setVisibility(View.GONE);
            layoutBuyerActions.setVisibility(View.VISIBLE);

            boolean isCompleted = Product.STATUS_COMPLETED.equalsIgnoreCase(productStatus)
                    || Product.STATUS_SOLD.equalsIgnoreCase(productStatus)
                    || Product.STATUS_CYCLED.equalsIgnoreCase(productStatus);
            boolean isReserved = Product.STATUS_RESERVED.equalsIgnoreCase(productStatus);

            if (isCompleted || isReserved) {
                tvUnavailableBanner.setVisibility(View.VISIBLE);
                if (isCompleted) {
                    tvUnavailableBanner.setText("This item has been completed and is no longer available.");
                } else {
                    tvUnavailableBanner.setText("This item is currently reserved for a neighbor.");
                }
                btnRequestDonation.setEnabled(false);
                btnRequestDonation.setAlpha(0.4f);
                btnProposeExchange.setEnabled(false);
                btnProposeExchange.setAlpha(0.4f);
                btnContactSeller.setEnabled(false);
                btnContactSeller.setAlpha(0.4f);
                btnMakeOffer.setEnabled(false);
                btnMakeOffer.setAlpha(0.4f);
            } else {
                tvUnavailableBanner.setVisibility(View.GONE);
                btnRequestDonation.setEnabled(true);
                btnRequestDonation.setAlpha(1.0f);
                btnProposeExchange.setEnabled(true);
                btnProposeExchange.setAlpha(1.0f);
                btnContactSeller.setEnabled(true);
                btnContactSeller.setAlpha(1.0f);
                btnMakeOffer.setEnabled(true);
                btnMakeOffer.setAlpha(1.0f);

                btnRequestDonation.setOnClickListener(v -> showSubmitRequestDialog(ItemRequest.TYPE_DONATION));
                btnProposeExchange.setOnClickListener(v -> showSubmitRequestDialog(ItemRequest.TYPE_EXCHANGE));

                btnContactSeller.setOnClickListener(v -> showContactSellerDialog());

                btnMakeOffer.setOnClickListener(v -> {
                    showSubmitRequestDialog(ItemRequest.TYPE_DONATION);
                });
            }
        }
    }

    private void showContactSellerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_contact_seller, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        ImageButton btnClose = dialogView.findViewById(R.id.btnContactDialogClose);
        TextView tvProdName = dialogView.findViewById(R.id.tvContactProductName);
        TextView tvProdLoc = dialogView.findViewById(R.id.tvContactProductLocation);
        TextView tvProdPrice = dialogView.findViewById(R.id.tvContactProductPrice);

        Button btnInAppOffer = dialogView.findViewById(R.id.btnContactInAppOffer);
        Button btnPhone = dialogView.findViewById(R.id.btnContactPhone);
        Button btnWhatsApp = dialogView.findViewById(R.id.btnContactWhatsApp);
        Button btnEmail = dialogView.findViewById(R.id.btnContactEmail);

        tvProdName.setText(productName);
        tvProdLoc.setText("📍 " + productSeller);
        tvProdPrice.setText(isProductFree ? "FREE" : productPrice);

        btnClose.setOnClickListener(v -> dialog.dismiss());

        // 1. In-App Offer
        btnInAppOffer.setOnClickListener(v -> {
            dialog.dismiss();
            showSubmitRequestDialog(ItemRequest.TYPE_DONATION);
        });

        // 2. Call / Phone Dialer Intent
        btnPhone.setOnClickListener(v -> {
            String defaultPhone = "01711000000";
            Intent dialIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + defaultPhone));
            try {
                startActivity(dialIntent);
            } catch (Exception e) {
                Toast.makeText(this, "No phone dialer application found.", Toast.LENGTH_SHORT).show();
            }
        });

        // 3. WhatsApp Direct Chat Intent
        btnWhatsApp.setOnClickListener(v -> {
            String whatsappNumber = "+8801711000000";
            String prefilledText = "Hello! I am interested in your GhorCycle listing: \"" + productName + "\" (" + (isProductFree ? "FREE" : productPrice) + "). Is it still available?";
            try {
                String url = "https://api.whatsapp.com/send?phone=" + whatsappNumber + "&text=" + Uri.encode(prefilledText);
                Intent whatsappIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(whatsappIntent);
            } catch (Exception e) {
                Toast.makeText(this, "WhatsApp is not installed on this device.", Toast.LENGTH_SHORT).show();
            }
        });

        // 4. Send Email Inquiry Intent
        btnEmail.setOnClickListener(v -> {
            String subject = "Inquiry: " + productName + " on GhorCycle";
            String body = "Hi,\n\nI saw your listing for \"" + productName + "\" listed in " + productSeller + " on GhorCycle.\nI would like to inquire about purchasing / picking this up.\n\nThank you!";
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
            emailIntent.setData(Uri.parse("mailto:"));
            emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"seller@ghorcycle.com"});
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
            emailIntent.putExtra(Intent.EXTRA_TEXT, body);
            try {
                startActivity(Intent.createChooser(emailIntent, "Send email inquiry to seller"));
            } catch (Exception e) {
                Toast.makeText(this, "No email app found.", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private void showSubmitRequestDialog(final String requestType) {
        String currentUid = FirebaseAuthService.getCurrentUserId();
        if (currentUid == null) {
            Toast.makeText(this, "Please log in to send a request.", Toast.LENGTH_LONG).show();
            return;
        }

        if (currentUid.equals(ownerId)) {
            Toast.makeText(this, "Invalid action: You cannot request your own product listing.", Toast.LENGTH_LONG).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_submit_request, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        TextView tvTitle = dialogView.findViewById(R.id.tvDialogRequestTitle);
        TextView tvSubtitle = dialogView.findViewById(R.id.tvDialogRequestSubtitle);
        LinearLayout layoutOffered = dialogView.findViewById(R.id.layoutOfferedItemField);
        EditText etOffered = dialogView.findViewById(R.id.etDialogOfferedItem);
        EditText etMessage = dialogView.findViewById(R.id.etDialogRequestMessage);
        Button btnCancel = dialogView.findViewById(R.id.btnDialogCancelRequest);
        Button btnSubmit = dialogView.findViewById(R.id.btnDialogSubmitRequest);

        if (ItemRequest.TYPE_EXCHANGE.equalsIgnoreCase(requestType)) {
            tvTitle.setText("Propose Barter / Exchange");
            tvSubtitle.setText("Offer an item in exchange for \"" + productName + "\".");
            layoutOffered.setVisibility(View.VISIBLE);
        } else {
            tvTitle.setText("Request Donated / Free Item");
            tvSubtitle.setText("Send a claim request for \"" + productName + "\".");
            layoutOffered.setVisibility(View.GONE);
        }

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSubmit.setOnClickListener(v -> {
            String message = etMessage.getText().toString().trim();
            String offeredItem = etOffered.getText().toString().trim();

            if (ItemRequest.TYPE_EXCHANGE.equalsIgnoreCase(requestType) && TextUtils.isEmpty(offeredItem)) {
                etOffered.setError("Please describe the item you are offering.");
                etOffered.requestFocus();
                return;
            }

            if (TextUtils.isEmpty(message)) {
                etMessage.setError("Please enter a note for the owner.");
                etMessage.requestFocus();
                return;
            }

            ItemRequest request = new ItemRequest(
                    null,
                    productId,
                    productName,
                    ownerId,
                    FirebaseAuthService.getCurrentUserId(),
                    FirebaseAuthService.getCurrentUserEmail(),
                    requestType,
                    offeredItem,
                    message,
                    ItemRequest.STATUS_PENDING
            );

            Product currentProduct = new Product(
                    productId, ownerId, productName, productDescription,
                    productCategory, productCondition, productTransactionType,
                    parsePrice(productPrice), productSeller, imageUrl, productStatus
            );

            btnSubmit.setEnabled(false);
            InteractionService.submitRequest(request, currentProduct, new InteractionService.RequestCallback() {
                @Override
                public void onSuccess(ItemRequest createdRequest) {
                    dialog.dismiss();
                    Toast.makeText(ProductDetailsActivity.this, "Request submitted successfully to the owner!", Toast.LENGTH_LONG).show();
                }

                @Override
                public void onFailure(String errorMessage) {
                    btnSubmit.setEnabled(true);
                    Toast.makeText(ProductDetailsActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                }
            });
        });

        dialog.show();
    }

    private void showRequestsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_view_requests, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        ImageButton btnClose = dialogView.findViewById(R.id.btnCloseRequestsDialog);
        TextView tvEmpty = dialogView.findViewById(R.id.tvNoRequestsEmpty);
        LinearLayout container = dialogView.findViewById(R.id.containerRequestsList);

        btnClose.setOnClickListener(v -> dialog.dismiss());

        InteractionService.listenToProductRequests(productId, new InteractionService.RequestListCallback() {
            @Override
            public void onLoaded(List<ItemRequest> requests) {
                container.removeAllViews();
                if (requests == null || requests.isEmpty()) {
                    tvEmpty.setVisibility(View.VISIBLE);
                } else {
                    tvEmpty.setVisibility(View.GONE);
                    for (ItemRequest r : requests) {
                        View row = getLayoutInflater().inflate(R.layout.item_request_row, container, false);
                        TextView tvReq = row.findViewById(R.id.tvRequestRequester);
                        TextView tvType = row.findViewById(R.id.tvRequestTypeBadge);
                        TextView tvStatus = row.findViewById(R.id.tvRequestStatusBadge);
                        TextView tvOffered = row.findViewById(R.id.tvRequestOfferedItem);
                        TextView tvMsg = row.findViewById(R.id.tvRequestMessage);
                        LinearLayout layoutActions = row.findViewById(R.id.layoutRequestActionButtons);
                        Button btnAccept = row.findViewById(R.id.btnAcceptRequest);
                        Button btnReject = row.findViewById(R.id.btnRejectRequest);

                        tvReq.setText(r.getRequesterEmail() != null ? r.getRequesterEmail() : "Neighbor Requester");
                        tvType.setText(r.getRequestType());
                        tvStatus.setText(r.getStatus());
                        tvMsg.setText(r.getMessage());

                        if (!TextUtils.isEmpty(r.getOfferedItem())) {
                            tvOffered.setVisibility(View.VISIBLE);
                            tvOffered.setText("Offered: " + r.getOfferedItem());
                        } else {
                            tvOffered.setVisibility(View.GONE);
                        }

                        if (ItemRequest.STATUS_PENDING.equalsIgnoreCase(r.getStatus())) {
                            layoutActions.setVisibility(View.VISIBLE);
                            btnAccept.setOnClickListener(v -> {
                                InteractionService.respondToRequest(r, ItemRequest.STATUS_ACCEPTED, new InteractionService.ActionCallback() {
                                    @Override
                                    public void onSuccess() {
                                        productStatus = Product.STATUS_RESERVED;
                                        updateStatusUI();
                                        setupOwnerAndBuyerActions();
                                        dialog.dismiss();
                                        Toast.makeText(ProductDetailsActivity.this, "Request accepted! Item is now Reserved.", Toast.LENGTH_SHORT).show();
                                    }

                                    @Override
                                    public void onFailure(String errorMessage) {
                                        Toast.makeText(ProductDetailsActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                                    }
                                });
                            });

                            btnReject.setOnClickListener(v -> {
                                InteractionService.respondToRequest(r, ItemRequest.STATUS_REJECTED, new InteractionService.ActionCallback() {
                                    @Override
                                    public void onSuccess() {
                                        dialog.dismiss();
                                        Toast.makeText(ProductDetailsActivity.this, "Request rejected.", Toast.LENGTH_SHORT).show();
                                    }

                                    @Override
                                    public void onFailure(String errorMessage) {
                                        Toast.makeText(ProductDetailsActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                                    }
                                });
                            });
                        } else {
                            layoutActions.setVisibility(View.GONE);
                        }

                        container.addView(row);
                    }
                }
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(ProductDetailsActivity.this, "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private void markProductAsCompleted() {
        new AlertDialog.Builder(this)
                .setTitle("Mark as Completed")
                .setMessage("Have you completed this donation or handover? This will finalize the transaction and mark the listing as Completed.")
                .setPositiveButton("Complete", (dialog, which) -> {
                    InteractionService.completeProductTransaction(productId, ownerId, new InteractionService.ActionCallback() {
                        @Override
                        public void onSuccess() {
                            productStatus = Product.STATUS_COMPLETED;
                            updateStatusUI();
                            setupOwnerAndBuyerActions();
                            Toast.makeText(ProductDetailsActivity.this, "Transaction completed! Listing archived.", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(String errorMessage) {
                            Toast.makeText(ProductDetailsActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private double parsePrice(String priceString) {
        if (TextUtils.isEmpty(priceString)) return 0.0;
        try {
            String clean = priceString.replaceAll("[^0-9.]", "");
            return Double.parseDouble(clean);
        } catch (Exception e) {
            return 0.0;
        }
    }

    private void refreshUIWithProduct(Product p) {
        this.productName = p.getTitle();
        this.productCategory = p.getCategory();
        this.productCondition = p.getCondition();
        this.productPrice = p.getFormattedPrice();
        this.productSeller = p.getLocation();
        this.productDescription = p.getDescription();
        this.isProductFree = p.isFree();
        this.imageUrl = p.getImageUrl();

        bindViewsAndPopulate();
        setupOwnerAndBuyerActions();
    }

    private void handleNavigateBack() {
        Log.d(TAG, "ProductDetailsActivity: Navigating back to parent catalog");
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "ProductDetailsActivity: onStart() - Activity visible");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "ProductDetailsActivity: onResume() - Activity focused");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "ProductDetailsActivity: onPause() - Activity pausing");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "ProductDetailsActivity: onStop() - Activity stopped");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "ProductDetailsActivity: onRestart() - Activity restarting");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "ProductDetailsActivity: onDestroy() - Activity destroyed");
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG, "ProductDetailsActivity: onSaveInstanceState() - Preserving product state");
        outState.putString("product_id", productId);
        outState.putString("product_name", productName);
        outState.putString("product_price", productPrice);
        outState.putBoolean("product_is_free", isProductFree);
        outState.putString("product_owner_id", ownerId);
        outState.putString("product_image_url", imageUrl);
    }
}
