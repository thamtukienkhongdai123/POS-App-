package com.example.pos.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pos.DatabaseHelper;
import com.example.pos.R;
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
        setupSearch();
        setupButtons();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadCustomers();
    }

    private void setupRecyclerView() {
        adapter = new CustomerAdapter(new ArrayList<>());
        binding.rvCustomers.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvCustomers.setAdapter(adapter);
    }

    private void setupButtons() {
        binding.btnAddCustomer.setOnClickListener(v -> showCustomerDialog(null));
        
        binding.btnPrevPage.setOnClickListener(v -> {
            // Placeholder for pagination
        });
        
        binding.btnNextPage.setOnClickListener(v -> {
            // Placeholder for pagination
        });
    }

    private void loadCustomers() {
        allCustomers.clear();
        android.database.sqlite.SQLiteDatabase db = dbHelper.getReadableDatabase();
        android.database.Cursor cursor = db.query(DatabaseHelper.TABLE_CUSTOMERS, null, null, null, null, null, DatabaseHelper.COLUMN_CUST_ID + " DESC");

        double totalSpending = 0;

        if (cursor.moveToFirst()) {
            do {
                int nameIdx = cursor.getColumnIndex(DatabaseHelper.COLUMN_CUST_NAME);
                int phoneIdx = cursor.getColumnIndex(DatabaseHelper.COLUMN_CUST_PHONE);
                int emailIdx = cursor.getColumnIndex(DatabaseHelper.COLUMN_CUST_EMAIL);
                int addressIdx = cursor.getColumnIndex(DatabaseHelper.COLUMN_CUST_ADDRESS);
                int spentIdx = cursor.getColumnIndex(DatabaseHelper.COLUMN_CUST_TOTAL_SPENT);

                if (nameIdx != -1 && phoneIdx != -1 && emailIdx != -1 && addressIdx != -1 && spentIdx != -1) {
                    String name = cursor.getString(nameIdx);
                    String phone = cursor.getString(phoneIdx);
                    String email = cursor.getString(emailIdx);
                    String address = cursor.getString(addressIdx);
                    double spent = cursor.getDouble(spentIdx);
                    
                    int orderCount = getOrderCountForCustomer(name);
                    
                    Customer customer = new Customer(name, phone, spent);
                    customer.setOrderCount(orderCount);
                    customer.setEmail(email != null ? email : (name.toLowerCase().replace(" ", "") + "@gmail.com"));
                    customer.setAddress(address != null ? address : "Chưa cập nhật địa chỉ");
                    
                    allCustomers.add(customer);
                    totalSpending += spent;
                }
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
        binding.tvPageNumber.setText("1");
    }

    private void showCustomerDialog(@Nullable Customer customer) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_customer, null);
        EditText etName = dialogView.findViewById(R.id.etCustName);
        EditText etPhone = dialogView.findViewById(R.id.etCustPhone);
        EditText etEmail = dialogView.findViewById(R.id.etCustEmail);
        EditText etAddress = dialogView.findViewById(R.id.etCustAddress);

        if (customer != null) {
            etName.setText(customer.getName());
            etPhone.setText(customer.getPhone());
            etEmail.setText(customer.getEmail());
            etAddress.setText(customer.getAddress());
        }

        new AlertDialog.Builder(getContext())
                .setTitle(customer == null ? "Thêm khách hàng" : "Sửa khách hàng")
                .setView(dialogView)
                .setPositiveButton(customer == null ? "Thêm" : "Cập nhật", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    String phone = etPhone.getText().toString().trim();
                    String email = etEmail.getText().toString().trim();
                    String address = etAddress.getText().toString().trim();

                    if (name.isEmpty() || phone.isEmpty()) {
                        Toast.makeText(getContext(), "Vui lòng nhập tên và SĐT", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (customer == null) {
                        dbHelper.updateCustomer(name, phone, 0); // Reusing updateCustomer for basic insert
                        // Since updateCustomer doesn't take email/address, we update it immediately after
                        dbHelper.updateCustomerInfo(phone, name, phone, email, address);
                        dbHelper.addLog(requireContext(), "Created Customer", name);
                    } else {
                        dbHelper.updateCustomerInfo(customer.getPhone(), name, phone, email, address);
                        dbHelper.addLog(requireContext(), "Updated Customer", name);
                    }
                    loadCustomers();
                })
                .setNegativeButton("Hủy", null)
                .show();
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
            
            holder.binding.btnEditCustomer.setOnClickListener(v -> showCustomerDialog(c));
            
            holder.binding.btnDeleteCustomer.setOnClickListener(v -> {
                new AlertDialog.Builder(getContext())
                        .setTitle("Xác nhận xóa")
                        .setMessage("Xóa khách hàng " + c.getName() + "?")
                        .setPositiveButton("Xóa", (dialog, which) -> {
                            dbHelper.deleteCustomer(c.getPhone());
                            dbHelper.addLog(requireContext(), "Deleted Customer", c.getName());
                            loadCustomers();
                        })
                        .setNegativeButton("Hủy", null)
                        .show();
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
