package com.example.pos.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.pos.DatabaseHelper;
import com.example.pos.databinding.FragmentDashboardBinding;
import com.example.pos.models.Order;
import com.example.pos.models.Product;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DashboardFragment extends Fragment {
    private FragmentDashboardBinding binding;
    private DatabaseHelper dbHelper;
    private SharedPreferences prefs;
    private float taxPercent = 10f;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        dbHelper = new DatabaseHelper(getContext());
        prefs = requireContext().getSharedPreferences("settings", Context.MODE_PRIVATE);
        taxPercent = prefs.getFloat("tax_percent", 10f);

        binding.btnEditTax.setOnClickListener(v -> showTaxEditDialog());
        binding.cvTax.setOnClickListener(v -> showTaxEditDialog());
    }

    @Override
    public void onResume() {
        super.onResume();
        loadStats();
        setupChart();
    }

    private void showTaxEditDialog() {
        EditText et = new EditText(getContext());
        et.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        et.setText(String.valueOf(taxPercent));
        et.setHint("Nhập % thuế (ví dụ: 10)");

        new AlertDialog.Builder(getContext())
                .setTitle("Điều chỉnh thuế (%)")
                .setView(et)
                .setPositiveButton("Cập nhật", (dialog, which) -> {
                    try {
                        float newTax = Float.parseFloat(et.getText().toString());
                        taxPercent = newTax;
                        prefs.edit().putFloat("tax_percent", taxPercent).apply();
                        loadStats();
                    } catch (Exception ignored) {}
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void loadStats() {
        List<Order> orders = dbHelper.getAllOrders();
        List<Product> products = dbHelper.getAllProducts();

        double totalRevenue = 0;
        int validOrderCount = 0;
        if (orders != null) {
            for (Order order : orders) {
                if (!"REFUNDED".equalsIgnoreCase(order.getStatus())) {
                    totalRevenue += order.getTotal();
                    validOrderCount++;
                }
            }
        }

        int lowStockCount = 0;
        if (products != null) {
            for (Product product : products) {
                for (Product.Variant variant : product.getVariants()) {
                    if (variant.getStock() < 5) {
                        lowStockCount++;
                        break;
                    }
                }
            }
        }

        double taxAmount = totalRevenue * (taxPercent / 100);
        double netRevenue = totalRevenue - taxAmount;

        binding.tvRevenue.setText(String.format(Locale.getDefault(), "%,.0fđ", totalRevenue));
        binding.tvOrderCount.setText(String.valueOf(validOrderCount));
        binding.tvProductCount.setText(String.valueOf(products != null ? products.size() : 0));
        binding.tvLowStock.setText(String.valueOf(lowStockCount));

        binding.tvTaxLabel.setText(String.format(Locale.getDefault(), "Thuế (%.1f%%)", taxPercent));
        binding.tvTaxAmount.setText(String.format(Locale.getDefault(), "%,.0fđ", taxAmount));
        binding.tvNetRevenue.setText(String.format(Locale.getDefault(), "%,.0fđ", netRevenue));
    }

    private void setupChart() {
        Map<String, Double> revenueData = dbHelper.getMonthlyRevenue();
        if (revenueData.isEmpty()) {
            binding.barChartRevenue.setNoDataText("Chưa có dữ liệu doanh thu");
            return;
        }

        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        int index = 0;
        for (Map.Entry<String, Double> entry : revenueData.entrySet()) {
            entries.add(new BarEntry(index, entry.getValue().floatValue()));
            labels.add(entry.getKey());
            index++;
        }

        BarDataSet dataSet = new BarDataSet(entries, "Doanh thu (VNĐ)");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueTextSize(10f);

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.6f);
        binding.barChartRevenue.setData(barData);

        // Customize Chart
        binding.barChartRevenue.getDescription().setEnabled(false);
        binding.barChartRevenue.setFitBars(true);
        binding.barChartRevenue.animateY(1000);
        binding.barChartRevenue.getLegend().setEnabled(true);

        XAxis xAxis = binding.barChartRevenue.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(labels.size());

        binding.barChartRevenue.getAxisRight().setEnabled(false);
        binding.barChartRevenue.getAxisLeft().setAxisMinimum(0f);
        binding.barChartRevenue.invalidate();
    }
}
