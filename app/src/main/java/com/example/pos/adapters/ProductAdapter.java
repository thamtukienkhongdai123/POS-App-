package com.example.pos.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.pos.databinding.ItemProductBinding;
import com.example.pos.models.Product;
import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {
    private List<Product> products;
    private OnProductActionListener listener;

    public interface OnProductActionListener {
        void onEdit(Product product);
        void onDelete(Product product);
    }

    public ProductAdapter(List<Product> products, OnProductActionListener listener) {
        this.products = products;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemProductBinding binding = ItemProductBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = products.get(position);
        holder.binding.tvProductName.setText(product.getName());
        holder.binding.tvBrand.setText(product.getBrand());
        
        int totalStock = 0;
        double minPrice = Double.MAX_VALUE;
        double maxPrice = 0;
        StringBuilder variantsStr = new StringBuilder("Size: ");
        
        if (product.getVariants() != null && !product.getVariants().isEmpty()) {
            for (Product.Variant variant : product.getVariants()) {
                variantsStr.append(variant.getSize()).append(", ");
                totalStock += variant.getStock();
                if (variant.getPrice() < minPrice) minPrice = variant.getPrice();
                if (variant.getPrice() > maxPrice) maxPrice = variant.getPrice();
            }
            
            if (variantsStr.length() > 6) {
                variantsStr.setLength(variantsStr.length() - 2);
            }
            holder.binding.tvVariants.setText(variantsStr.toString());
            
            String priceRange;
            if (minPrice == maxPrice) {
                priceRange = String.format(java.util.Locale.getDefault(), "%,.0fđ", minPrice);
            } else {
                priceRange = String.format(java.util.Locale.getDefault(), "%,.0fđ - %,.0fđ", minPrice, maxPrice);
            }
            holder.binding.tvProductPrice.setText("Giá: " + priceRange);
        } else {
            holder.binding.tvVariants.setText("Size: Chưa xác định");
            holder.binding.tvProductPrice.setText("Giá: Liên hệ");
        }
        
        holder.binding.tvProductStock.setText("Tổng tồn: " + totalStock);

        Glide.with(holder.itemView.getContext())
                .load(product.getImageUrl())
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_report_image)
                .into(holder.binding.ivProduct);

        holder.binding.btnEdit.setOnClickListener(v -> listener.onEdit(product));
        holder.binding.btnDelete.setOnClickListener(v -> listener.onDelete(product));
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ItemProductBinding binding;
        public ViewHolder(ItemProductBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}