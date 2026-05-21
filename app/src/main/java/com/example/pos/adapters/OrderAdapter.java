package com.example.pos.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.pos.databinding.ItemOrderBinding;
import com.example.pos.models.Order;
import java.util.List;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.ViewHolder> {
    private List<Order> orders;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Order order);
        void onExportPdfClick(Order order);
    }

    public OrderAdapter(List<Order> orders) {
        this.orders = orders;
    }

    public OrderAdapter(List<Order> orders, OnItemClickListener listener) {
        this.orders = orders;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemOrderBinding binding = ItemOrderBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Order order = orders.get(position);
        holder.binding.tvOrderId.setText(order.getOrderId());
        holder.binding.tvDate.setText(order.getDate());
        holder.binding.tvCustomer.setText(order.getCustomer());
        holder.binding.tvTotal.setText(String.format(java.util.Locale.getDefault(), "%,.0fđ", order.getTotal()));
        holder.binding.tvStatus.setText(order.getStatus());

        // Load customer phone
        com.example.pos.DatabaseHelper dbHelper = new com.example.pos.DatabaseHelper(holder.itemView.getContext());
        String phone = dbHelper.getCustomerPhone(order.getCustomer());
        holder.binding.tvCustomerPhone.setText(phone != null ? phone : "Không có SĐT");
        
        // Handle status color
        if ("REFUNDED".equalsIgnoreCase(order.getStatus())) {
            holder.binding.tvStatus.setTextColor(android.graphics.Color.parseColor("#EF4444"));
        } else {
            holder.binding.tvStatus.setTextColor(android.graphics.Color.parseColor("#10B981"));
        }
        
        holder.binding.btnExportPdf.setOnClickListener(v -> {
            if (listener != null) {
                listener.onExportPdfClick(order);
            }
        });

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(order);
            }
        });
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ItemOrderBinding binding;
        public ViewHolder(ItemOrderBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}