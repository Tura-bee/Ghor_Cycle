package com.example.ghor_cycle;

import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

/**
 * ProductAdapter binds community goods data into reusable MaterialCardView product items
 * with accessible image content descriptions and visual Free/Priced labels.
 */
public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    public interface OnProductClickListener {
        void onProductClick(Product product);
    }

    public interface OnFavoriteClickListener {
        void onFavoriteClick(Product product, int position);
    }

    private final List<Product> productList = new ArrayList<>();
    private final OnProductClickListener clickListener;
    private OnFavoriteClickListener favoriteClickListener;

    public ProductAdapter(List<Product> products, OnProductClickListener clickListener) {
        if (products != null) {
            this.productList.addAll(products);
        }
        this.clickListener = clickListener;
    }

    public void setOnFavoriteClickListener(OnFavoriteClickListener favoriteClickListener) {
        this.favoriteClickListener = favoriteClickListener;
    }

    public void updateProducts(List<Product> newProducts) {
        this.productList.clear();
        if (newProducts != null) {
            this.productList.addAll(newProducts);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product_card, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);
        holder.bind(product, clickListener, favoriteClickListener);
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {

        private final MaterialCardView cardProduct;
        private final ImageView ivProductImage;
        private final TextView tvProductCategoryBadge;
        private final ImageButton btnProductFavorite;
        private final TextView tvProductCondition;
        private final TextView tvProductName;
        private final TextView tvProductLocation;
        private final TextView tvProductPrice;
        private final TextView tvFreeBadge;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            cardProduct = itemView.findViewById(R.id.cardProduct);
            ivProductImage = itemView.findViewById(R.id.ivProductImage);
            tvProductCategoryBadge = itemView.findViewById(R.id.tvProductCategoryBadge);
            btnProductFavorite = itemView.findViewById(R.id.btnProductFavorite);
            tvProductCondition = itemView.findViewById(R.id.tvProductCondition);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvProductLocation = itemView.findViewById(R.id.tvProductLocation);
            tvProductPrice = itemView.findViewById(R.id.tvProductPrice);
            tvFreeBadge = itemView.findViewById(R.id.tvFreeBadge);
        }

        public void bind(final Product product,
                         final OnProductClickListener clickListener,
                         final OnFavoriteClickListener favoriteClickListener) {

            tvProductName.setText(product.getName());
            tvProductCategoryBadge.setText(product.getCategory());
            tvProductCondition.setText(product.getCondition());
            tvProductLocation.setText(product.getLocation());

            // Image handling: custom photo URI or vector drawable
            if (!TextUtils.isEmpty(product.getImageUrl())) {
                try {
                    ivProductImage.setImageURI(Uri.parse(product.getImageUrl()));
                    ivProductImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    ivProductImage.setPadding(0, 0, 0, 0);
                } catch (Exception e) {
                    ivProductImage.setImageResource(product.getImageResId() != 0 ? product.getImageResId() : R.drawable.ic_category_all);
                    ivProductImage.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                    ivProductImage.setPadding(26, 26, 26, 26);
                }
            } else if (product.getImageResId() != 0) {
                ivProductImage.setImageResource(product.getImageResId());
                ivProductImage.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                ivProductImage.setPadding(26, 26, 26, 26);
            } else {
                ivProductImage.setImageResource(R.drawable.ic_category_all);
                ivProductImage.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                ivProductImage.setPadding(26, 26, 26, 26);
            }
            ivProductImage.setContentDescription("Illustration for " + product.getName() + " in " + product.getCategory());

            // Handle Free vs Priced formatting
            if (product.isFree()) {
                tvProductPrice.setText("FREE");
                tvProductPrice.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.eco_free_badge));
                tvFreeBadge.setVisibility(View.VISIBLE);
            } else {
                tvProductPrice.setText(product.getFormattedPrice());
                tvProductPrice.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.eco_green_primary));
                tvFreeBadge.setVisibility(View.GONE);
            }

            // Favorite button state
            updateFavoriteIcon(product.isFavorite());
            btnProductFavorite.setOnClickListener(v -> {
                boolean newState = !product.isFavorite();
                product.setFavorite(newState);
                updateFavoriteIcon(newState);
                if (favoriteClickListener != null) {
                    favoriteClickListener.onFavoriteClick(product, getAdapterPosition());
                }
            });

            // Card click listener
            cardProduct.setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onProductClick(product);
                }
            });
        }

        private void updateFavoriteIcon(boolean isFavorite) {
            if (isFavorite) {
                btnProductFavorite.setImageResource(R.drawable.ic_heart_filled);
                btnProductFavorite.setColorFilter(ContextCompat.getColor(itemView.getContext(), R.color.status_danger));
                btnProductFavorite.setContentDescription("Remove from favorites");
            } else {
                btnProductFavorite.setImageResource(R.drawable.ic_heart_outline);
                btnProductFavorite.setColorFilter(ContextCompat.getColor(itemView.getContext(), R.color.text_secondary));
                btnProductFavorite.setContentDescription("Save to favorites");
            }
        }
    }
}
