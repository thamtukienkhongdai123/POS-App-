package com.example.pos.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pos.DatabaseHelper;
import com.example.pos.R;
import com.example.pos.databinding.FragmentReturnsBinding;
import com.example.pos.databinding.ItemReturnBinding;
import com.example.pos.models.Order;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class ReturnsFragment extends Fragment {
    private FragmentReturnsBinding binding;
    private DatabaseHelper dbHelper;
    private List<Order> allOrders = new ArrayList<>();
    private ReturnAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentReturnsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        dbHelper = new DatabaseHelper(getContext());
        
        setupRecyclerView();
        setupSearch();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadOrders();
    }

    private void setupRecyclerView() {
        adapter = new ReturnAdapter(new ArrayList<>());
        binding.rvReturns.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvReturns.setAdapter(adapter);
    }

    private void loadOrders() {
        allOrders = dbHelper.getAllOrders();
        updateList(allOrders);
        updateStats();
    }

    private void updateStats() {
        int refundedCount = 0;
        double totalRefundedAmount = 0;
        int restockedItems = 0;

        for (Order order : allOrders) {
            if ("REFUNDED".equalsIgnoreCase(order.getStatus())) {
                refundedCount++;
                totalRefundedAmount += order.getTotal();
                
                // Estimate restocked items from order items
                List<DatabaseHelper.OrderItem> items = dbHelper.getOrderItems(order.getOrderId());
                for (DatabaseHelper.OrderItem item : items) {
                    restockedItems += item.quantity;
                }
            }
        }

        binding.tvTotalRefundedOrders.setText(String.valueOf(refundedCount));
        binding.tvTotalRestockedItems.setText(String.valueOf(restockedItems));
        binding.tvTotalRefundedAmount.setText(String.format(Locale.getDefault(), "%,.0fđ", totalRefundedAmount));
    }

    private void setupSearch() {
        binding.etSearchOrder.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterOrders(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filterOrders(String query) {
        List<Order> filtered = allOrders.stream()
                .filter(o -> o.getOrderId().toLowerCase().contains(query.toLowerCase()) || 
                            o.getCustomer().toLowerCase().contains(query.toLowerCase()))
                .collect(Collectors.toList());
        updateList(filtered);
    }

    private void updateList(List<Order> list) {
        adapter.updateData(list);
    }

    private class ReturnAdapter extends RecyclerView.Adapter<ReturnViewHolder> {
        private List<Order> orders;

        public ReturnAdapter(List<Order> orders) {
            this.orders = orders;
        }

        public void updateData(List<Order> newOrders) {
            this.orders = newOrders;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ReturnViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemReturnBinding b = ItemReturnBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new ReturnViewHolder(b);
        }

        @Override
        public void onBindViewHolder(@NonNull ReturnViewHolder holder, int position) {
            Order order = orders.get(position);
            holder.binding.tvOrderCode.setText(order.getOrderId());
            holder.binding.tvOrderDate.setText(order.getDate());
            holder.binding.tvCustomerName.setText(order.getCustomer());
            holder.binding.tvOrderTotal.setText(String.format(Locale.getDefault(), "%,.0fđ", order.getTotal()));
            holder.binding.tvOrderStatus.setText(order.getStatus());

            boolean isRefunded = "REFUNDED".equalsIgnoreCase(order.getStatus());
            
            // UI adjustments for status
            if (isRefunded) {
                holder.binding.tvOrderStatus.setTextColor(android.graphics.Color.parseColor("#EF4444"));
                holder.binding.btnRefund.setVisibility(View.GONE);
            } else {
                holder.binding.tvOrderStatus.setTextColor(android.graphics.Color.parseColor("#10B981"));
                holder.binding.btnRefund.setVisibility(View.VISIBLE);
            }

            holder.binding.btnRefund.setOnClickListener(v -> {
                showRefundConfirmation(order);
            });
        }

        private void showRefundConfirmation(Order order) {
            new AlertDialog.Builder(requireContext())
                .setTitle("Xác nhận hoàn hàng")
                .setMessage("Bạn có muốn hoàn tiền cho đơn hàng " + order.getOrderId() + " không?")
                .setPositiveButton("Xác nhận hoàn", (dialog, which) -> {
                    dbHelper.updateOrderStatus(order.getOrderId(), "REFUNDED");
                    
                    // Restore stock
                    List<DatabaseHelper.OrderItem> items = dbHelper.getOrderItems(order.getOrderId());
                    for (DatabaseHelper.OrderItem item : items) {
                        dbHelper.restoreStock(item.productName, item.quantity);
                    }

                    dbHelper.addLog(requireContext(), "Refunded Order", order.getOrderId());
                    Toast.makeText(requireContext(), "Đã hoàn tiền thành công", Toast.LENGTH_SHORT).show();
                    loadOrders();
                })
                .setNegativeButton("Hủy", null)
                .show();
        }

        @Override
        public int getItemCount() {
            return orders.size();
        }
    }

    private static class ReturnViewHolder extends RecyclerView.ViewHolder {
        ItemReturnBinding binding;
        ReturnViewHolder(ItemReturnBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
