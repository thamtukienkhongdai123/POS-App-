package com.example.pos.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.pos.R;
import com.example.pos.databinding.ItemProductGridBinding;
import com.example.pos.models.Product;
import java.util.List;

public class ProductGridAdapter extends RecyclerView.Adapter<ProductGridAdapter.ViewHolder> {
    private List<Product> products;
    private OnProductClickListener listener;

    public interface OnProductClickListener {
        void onProductClick(Product product);
    }

    public ProductGridAdapter(List<Product> products, OnProductClickListener listener) {
        this.products = products;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemProductGridBinding binding = ItemProductGridBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = products.get(position);
        holder.binding.tvProductBrand.setText(product.getBrand());
        holder.binding.tvProductName.setText(product.getName());
        holder.binding.tvVariantCount.setText(product.getVariants().size() + " loại");
        
        int totalStock = 0;
        if (!product.getVariants().isEmpty()) {
            holder.binding.tvPrice.setText(String.format("%,.0fđ", product.getVariants().get(0).getPrice()));
            for (com.example.pos.models.Product.Variant v : product.getVariants()) {
                totalStock += v.getStock();
            }
        } else {
            holder.binding.tvPrice.setText("Hết hàng");
        }
        
        holder.binding.tvStockGrid.setText("Kho: " + totalStock);

        Glide.with(holder.itemView.getContext())
                .load(product.getImageUrl())
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_report_image)
                .into(holder.binding.ivProduct);
        
        // Cả cái card hoặc nút + đều thêm vào giỏ
        View.OnClickListener clickListener = v -> listener.onProductClick(product);
        holder.itemView.setOnClickListener(clickListener);
        holder.binding.btnAddToCart.setOnClickListener(clickListener);
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ItemProductGridBinding binding;
        public ViewHolder(ItemProductGridBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}