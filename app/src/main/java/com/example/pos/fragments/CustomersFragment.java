package com.example.pos.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pos.DatabaseHelper;
import com.example.pos.databinding.FragmentCustomersBinding;
import com.example.pos.databinding.ItemCustomerBinding;
import com.example.pos.models.Customer;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class CustomersFragment extends Fragment {
    private FragmentCustomersBinding binding;
    private DatabaseHelper dbHelper;
    private List<Customer> allCustomers = new ArrayList<>();
    private CustomerAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCustomersBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        dbHelper = new DatabaseHelper(getContext());
        
        setupRecyclerView();
        loadCustomers();
        setupSearch();
        setupButtons();
    }

    private void setupButtons() {
        binding.btnAddCustomer.setOnClickListener(v -> {
            // TODO: Open add customer dialog
        });
        
        binding.btnPrevPage.setOnClickListener(v -> {
            // Handle pagination
        });
        
        binding.btnNextPage.setOnClickListener(v -> {
            // Handle pagination
        });
    }

    private void setupRecyclerView() {
        adapter = new CustomerAdapter(new ArrayList<>());
        binding.rvCustomers.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvCustomers.setAdapter(adapter);
    }

    private void loadCustomers() {
        allCustomers.clear();
        android.database.sqlite.SQLiteDatabase db = dbHelper.getReadableDatabase();
        android.database.Cursor cursor = db.query(DatabaseHelper.TABLE_CUSTOMERS, null, null, null, null, null, DatabaseHelper.COLUMN_CUST_TOTAL_SPENT + " DESC");

        double totalSpending = 0;

        if (cursor.moveToFirst()) {
            do {
                String name = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_CUST_NAME));
                String phone = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_CUST_PHONE));
                String email = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_CUST_EMAIL));
                String address = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_CUST_ADDRESS));
                double spent = cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.COLUMN_CUST_TOTAL_SPENT));
                
                // Get order count for this customer
                int orderCount = getOrderCountForCustomer(name);
                
                Customer customer = new Customer(name, phone, spent);
                customer.setOrderCount(orderCount);
                customer.setEmail(email != null ? email : (name.toLowerCase().replace(" ", "") + "@gmail.com"));
                customer.setAddress(address != null ? address : "Chưa cập nhật địa chỉ");
                
                allCustomers.add(customer);
                totalSpending += spent;
            } while (cursor.moveToNext());
        }
        cursor.close();

        updateStats(totalSpending);
        updateList(allCustomers);
    }

    private int getOrderCountForCustomer(String customerName) {
        android.database.sqlite.SQLiteDatabase db = dbHelper.getReadableDatabase();
        android.database.Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_ORDERS + 
                " WHERE " + DatabaseHelper.COLUMN_ORDER_CUSTOMER + " = ?", new String[]{customerName});
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    private void updateStats(double totalSpending) {
        int count = allCustomers.size();
        binding.tvTotalCustomersCount.setText(String.valueOf(count));
        binding.tvTotalSpending.setText(String.format(Locale.getDefault(), "%,.0fđ", totalSpending));
        
        double avg = count > 0 ? totalSpending / count : 0;
        binding.tvAverageSpending.setText(String.format(Locale.getDefault(), "%,.0fđ", avg));
    }

    private void setupSearch() {
        binding.etSearchCustomer.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterCustomers(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filterCustomers(String query) {
        List<Customer> filteredList = allCustomers.stream()
                .filter(c -> c.getName().toLowerCase().contains(query.toLowerCase()) || 
                            c.getPhone().contains(query))
                .collect(Collectors.toList());
        updateList(filteredList);
    }

    private void updateList(List<Customer> list) {
        adapter.updateData(list);
        binding.tvCustomerCountLabel.setText("Hiển thị " + list.size() + " / " + allCustomers.size() + " Khách hàng");
        binding.tvPageNumber.setText("1"); // Simplified for now
    }

    private class CustomerAdapter extends RecyclerView.Adapter<CustomerViewHolder> {
        private List<Customer> customers;

        public CustomerAdapter(List<Customer> customers) {
            this.customers = customers;
        }

        public void updateData(List<Customer> newCustomers) {
            this.customers = newCustomers;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public CustomerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemCustomerBinding b = ItemCustomerBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new CustomerViewHolder(b);
        }

        @Override
        public void onBindViewHolder(@NonNull CustomerViewHolder holder, int position) {
            Customer c = customers.get(position);
            holder.binding.tvCustomerName.setText(c.getName());
            holder.binding.tvCustomerEmail.setText(c.getEmail());
            holder.binding.tvCustomerPhone.setText(c.getPhone());
            holder.binding.tvCustomerAddress.setText(c.getAddress());
            holder.binding.tvOrderCount.setText(c.getOrderCount() + " đơn");
            holder.binding.tvTotalSpent.setText(String.format(Locale.getDefault(), "%,.0fđ", c.getTotalSpent()));
            
            holder.binding.btnEditCustomer.setOnClickListener(v -> {
                // Handle edit
            });
            
            holder.binding.btnDeleteCustomer.setOnClickListener(v -> {
                // Handle delete
            });
        }

        @Override
        public int getItemCount() {
            return customers.size();
        }
    }

    private static class CustomerViewHolder extends RecyclerView.ViewHolder {
        ItemCustomerBinding binding;
        public CustomerViewHolder(ItemCustomerBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}