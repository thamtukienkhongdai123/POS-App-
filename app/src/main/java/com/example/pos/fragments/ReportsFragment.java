package com.example.pos.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.pos.DatabaseHelper;
import com.example.pos.databinding.FragmentReportsBinding;
import com.example.pos.models.Order;
import com.example.pos.models.Product;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Map;
import java.util.List;
import java.util.Locale;

public class ReportsFragment extends Fragment {
    private FragmentReportsBinding binding;
    private DatabaseHelper dbHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentReportsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        dbHelper = new DatabaseHelper(getContext());
        loadReports();
    }

    private void loadReports() {
        List<Order> orders = dbHelper.getAllOrders();
        double totalRevenue = 0;
        if (orders != null) {
            for (Order order : orders) {
                totalRevenue += order.getTotal();
            }
        }

        binding.tvReportTotalRevenue.setText(String.format(Locale.getDefault(), "%,.0fđ", totalRevenue));
        binding.tvReportOrderCount.setText(String.valueOf(orders != null ? orders.size() : 0));
        
        java.util.HashSet<String> uniqueCustomers = new java.util.HashSet<>();
        if (orders != null) {
            for (Order o : orders) uniqueCustomers.add(o.getCustomer());
        }
        binding.tvReportCustomerCount.setText(String.valueOf(uniqueCustomers.size()));

        // Top Products
        Map<String, Integer> topProductsMap = dbHelper.getTopProducts();
        List<Map.Entry<String, Integer>> topProductsList = new ArrayList<>();
        if (topProductsMap != null) {
            topProductsList.addAll(topProductsMap.entrySet());
        }

        if (topProductsList.isEmpty()) {
            binding.tvEmptyTopProducts.setVisibility(View.VISIBLE);
            binding.rvTopProducts.setVisibility(View.GONE);
        } else {
            binding.tvEmptyTopProducts.setVisibility(View.GONE);
            binding.rvTopProducts.setVisibility(View.VISIBLE);
            binding.rvTopProducts.setLayoutManager(new LinearLayoutManager(getContext()));
            binding.rvTopProducts.setAdapter(new RecyclerView.Adapter<TopProductViewHolder>() {
                @NonNull
                @Override
                public TopProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                    View v = LayoutInflater.from(parent.getContext()).inflate(com.example.pos.R.layout.item_top_product, parent, false);
                    return new TopProductViewHolder(v);
                }

                @Override
                public void onBindViewHolder(@NonNull TopProductViewHolder holder, int position) {
                    Map.Entry<String, Integer> entry = topProductsList.get(position);
                    holder.name.setText(entry.getKey());
                    holder.qty.setText(String.valueOf(entry.getValue()));
                }

                @Override
                public int getItemCount() {
                    return topProductsList.size();
                }
            });
        }
    }

    private static class TopProductViewHolder extends RecyclerView.ViewHolder {
        TextView name, qty;
        TopProductViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(com.example.pos.R.id.tvTopProductName);
            qty = itemView.findViewById(com.example.pos.R.id.tvTopProductQty);
        }
    }
}