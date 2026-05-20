package com.example.pos.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.pos.databinding.ItemCartBinding;
import com.example.pos.models.CartItem;
import java.util.List;
import java.util.Locale;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {
    private List<CartItem> cartItems;
    private OnCartItemChangeListener listener;

    public interface OnCartItemChangeListener {
        void onQuantityChanged(int position, int newQuantity);
        void onItemRemoved(int position);
    }

    public CartAdapter(List<CartItem> cartItems, OnCartItemChangeListener listener) {
        this.cartItems = cartItems;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCartBinding binding = ItemCartBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CartItem item = cartItems.get(position);
        holder.binding.tvProductName.setText(item.getProduct().getName());
        holder.binding.tvProductVariant.setText(!item.getProduct().getVariants().isEmpty() ? item.getProduct().getVariants().get(0).getSize() : "");
        holder.binding.tvQuantity.setText(String.valueOf(item.getQuantity()));
        holder.binding.tvTotal.setText(String.format(Locale.getDefault(), "%,.0fđ", item.getTotalPrice()));

        holder.binding.btnPlus.setOnClickListener(v -> {
            listener.onQuantityChanged(position, item.getQuantity() + 1);
        });

        holder.binding.btnMinus.setOnClickListener(v -> {
            if (item.getQuantity() > 1) {
                listener.onQuantityChanged(position, item.getQuantity() - 1);
            }
        });

        holder.binding.btnRemove.setOnClickListener(v -> {
            listener.onItemRemoved(position);
        });
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ItemCartBinding binding;
        public ViewHolder(ItemCartBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}