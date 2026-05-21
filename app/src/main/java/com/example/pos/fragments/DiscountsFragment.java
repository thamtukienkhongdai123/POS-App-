package com.example.pos.fragments;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pos.DatabaseHelper;
import com.example.pos.R;
import com.example.pos.databinding.FragmentDiscountsBinding;
import com.example.pos.databinding.ItemDiscountBinding;
import com.example.pos.models.Voucher;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DiscountsFragment extends Fragment {
    private FragmentDiscountsBinding binding;
    private DatabaseHelper dbHelper;
    private List<Voucher> allVouchers = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentDiscountsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        dbHelper = new DatabaseHelper(getContext());
        
        setupSearch();

        binding.btnShowAddVoucherDialog.setOnClickListener(v -> {
            showAddVoucherDialog();
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadVouchers();
    }

    private void setupSearch() {
        binding.etSearchDiscount.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterVouchers(s.toString());
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });
    }

    private void filterVouchers(String query) {
        List<Voucher> filtered = new ArrayList<>();
        for (Voucher v : allVouchers) {
            if (v.getCode().toLowerCase().contains(query.toLowerCase())) {
                filtered.add(v);
            }
        }
        updateRecyclerView(filtered);
    }

    private void showAddVoucherDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_voucher, null);
        EditText etCode = dialogView.findViewById(R.id.etNewVoucherCode);
        EditText etAmount = dialogView.findViewById(R.id.etNewVoucherAmount);

        new AlertDialog.Builder(requireContext())
                .setTitle("Thêm mã giảm giá mới")
                .setView(dialogView)
                .setPositiveButton("Tạo mã", (dialog, which) -> {
                    String code = etCode.getText().toString().trim();
                    String amountStr = etAmount.getText().toString().trim();
                    
                    if (code.isEmpty() || amountStr.isEmpty()) {
                        Toast.makeText(getContext(), "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    try {
                        double amount = Double.parseDouble(amountStr);
                        SQLiteDatabase db = dbHelper.getWritableDatabase();
                        ContentValues values = new ContentValues();
                        values.put(DatabaseHelper.COLUMN_VOUCH_CODE, code);
                        values.put(DatabaseHelper.COLUMN_VOUCH_AMOUNT, amount);
                        values.put(DatabaseHelper.COLUMN_VOUCH_STATUS, "ACTIVE");
                        
                        long result = db.insert(DatabaseHelper.TABLE_VOUCHERS, null, values);
                        if (result != -1) {
                            Toast.makeText(getContext(), "Đã thêm mã giảm giá thành công", Toast.LENGTH_SHORT).show();
                            dbHelper.addLog(requireContext(), "Created Voucher", code);
                            loadVouchers();
                        } else {
                            Toast.makeText(getContext(), "Lỗi: Mã này đã tồn tại", Toast.LENGTH_SHORT).show();
                        }
                    } catch (NumberFormatException e) {
                        Toast.makeText(getContext(), "Số tiền không hợp lệ", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void loadVouchers() {
        allVouchers.clear();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_VOUCHERS, null, null, null, null, null, null);

        int activeCount = 0;
        if (cursor != null && cursor.moveToFirst()) {
            do {
                int idIdx = cursor.getColumnIndex(DatabaseHelper.COLUMN_VOUCH_ID);
                int codeIdx = cursor.getColumnIndex(DatabaseHelper.COLUMN_VOUCH_CODE);
                int amountIdx = cursor.getColumnIndex(DatabaseHelper.COLUMN_VOUCH_AMOUNT);
                int statusIdx = cursor.getColumnIndex(DatabaseHelper.COLUMN_VOUCH_STATUS);

                if (idIdx != -1 && codeIdx != -1 && amountIdx != -1 && statusIdx != -1) {
                    long id = cursor.getLong(idIdx);
                    String code = cursor.getString(codeIdx);
                    double amount = cursor.getDouble(amountIdx);
                    String status = cursor.getString(statusIdx);
                    Voucher v = new Voucher(id, code, amount, status);
                    allVouchers.add(v);
                    if ("ACTIVE".equalsIgnoreCase(status)) activeCount++;
                }
            } while (cursor.moveToNext());
            cursor.close();
        }

        updateStats(activeCount);
        updateRecyclerView(allVouchers);
    }

    private void updateRecyclerView(List<Voucher> vouchers) {
        binding.tvVoucherCountLabel.setText(vouchers.size() + " Mã giảm giá");
        binding.rvDiscounts.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvDiscounts.setAdapter(new RecyclerView.Adapter<VoucherViewHolder>() {
            @NonNull
            @Override
            public VoucherViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                ItemDiscountBinding b = ItemDiscountBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
                return new VoucherViewHolder(b);
            }

            @Override
            public void onBindViewHolder(@NonNull VoucherViewHolder holder, int position) {
                Voucher v = vouchers.get(position);
                holder.binding.tvVoucherCode.setText(v.getCode());
                holder.binding.tvVoucherAmount.setText(String.format(Locale.getDefault(), "%,.0fđ", v.getAmount()));
                holder.binding.tvVoucherStatus.setText(v.getStatus());

                // Set color for status
                if ("ACTIVE".equalsIgnoreCase(v.getStatus())) {
                    holder.binding.tvVoucherStatus.setTextColor(android.graphics.Color.parseColor("#10B981"));
                } else {
                    holder.binding.tvVoucherStatus.setTextColor(android.graphics.Color.parseColor("#EF4444"));
                }

                holder.binding.btnEditDiscount.setOnClickListener(view -> {
                    showEditVoucherDialog(v);
                });

                holder.binding.btnDeleteDiscount.setOnClickListener(view -> {
                    showDeleteVoucherConfirmation(v);
                });
            }

            @Override
            public int getItemCount() {
                return vouchers.size();
            }
        });
    }

    private void updateStats(int activeCount) {
        binding.tvActiveVouchersCount.setText(String.valueOf(activeCount));
        
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*), SUM(" + DatabaseHelper.COLUMN_ORDER_DISCOUNT + ") FROM " + DatabaseHelper.TABLE_ORDERS + " WHERE " + DatabaseHelper.COLUMN_ORDER_DISCOUNT + " > 0", null);
        
        int usages = 0;
        double totalDiscount = 0;
        
        if (cursor.moveToFirst()) {
            usages = cursor.getInt(0);
            totalDiscount = cursor.getDouble(1);
        }
        cursor.close();
        
        binding.tvTotalUsages.setText(String.valueOf(usages));
        binding.tvTotalDiscountGiven.setText(String.format(Locale.getDefault(), "%,.0fđ", totalDiscount));
    }

    private void showEditVoucherDialog(Voucher voucher) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_voucher, null);
        EditText etEditVoucherCode = dialogView.findViewById(R.id.etEditVoucherCode);
        EditText etEditVoucherAmount = dialogView.findViewById(R.id.etEditVoucherAmount);
        Spinner spinnerEditVoucherStatus = dialogView.findViewById(R.id.spinnerEditVoucherStatus);

        etEditVoucherCode.setText(voucher.getCode());
        etEditVoucherAmount.setText(String.valueOf(voucher.getAmount()));

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.voucher_statuses, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEditVoucherStatus.setAdapter(adapter);
        if (voucher.getStatus() != null) {
            int spinnerPosition = adapter.getPosition(voucher.getStatus());
            spinnerEditVoucherStatus.setSelection(spinnerPosition);
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Chỉnh sửa mã giảm giá")
                .setView(dialogView)
                .setPositiveButton("Cập nhật", (dialog, which) -> {
                    String newCode = etEditVoucherCode.getText().toString().trim();
                    String newAmountStr = etEditVoucherAmount.getText().toString().trim();
                    String newStatus = spinnerEditVoucherStatus.getSelectedItem().toString();

                    if (newCode.isEmpty() || newAmountStr.isEmpty()) {
                        Toast.makeText(requireContext(), "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    try {
                        double newAmount = Double.parseDouble(newAmountStr);
                        dbHelper.updateVoucher(voucher.getId(), newCode, newAmount, newStatus);
                        dbHelper.addLog(requireContext(), "Updated Voucher", newCode);
                        loadVouchers();
                        Toast.makeText(requireContext(), "Đã cập nhật mã giảm giá thành công!", Toast.LENGTH_SHORT).show();
                    } catch (NumberFormatException e) {
                        Toast.makeText(requireContext(), "Số tiền không hợp lệ", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showDeleteVoucherConfirmation(Voucher voucher) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa mã giảm giá \"" + voucher.getCode() + "\" không?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    dbHelper.deleteVoucher(voucher.getId());
                    dbHelper.addLog(requireContext(), "Deleted Voucher", voucher.getCode());
                    loadVouchers();
                    Toast.makeText(requireContext(), "Đã xóa mã giảm giá thành công!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private static class VoucherViewHolder extends RecyclerView.ViewHolder {
        ItemDiscountBinding binding;
        public VoucherViewHolder(ItemDiscountBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
