package com.example.pos.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
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

import com.bumptech.glide.Glide;
import com.example.pos.DatabaseHelper;
import com.example.pos.R;
import com.example.pos.databinding.FragmentStockBinding;
import com.example.pos.databinding.ItemStockBinding;
import com.example.pos.models.Product;

import java.util.ArrayList;
import java.util.List;

public class StockFragment extends Fragment {
    private FragmentStockBinding binding;
    private DatabaseHelper dbHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentStockBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        dbHelper = new DatabaseHelper(getContext());
        loadStock();

        binding.fabAddStock.setOnClickListener(v -> {
            AddProductDialogFragment dialog = new AddProductDialogFragment();
            dialog.show(getParentFragmentManager(), "AddProductDialogFragment");
        });

        binding.btnRefreshStock.setOnClickListener(v -> loadStock());

        binding.etSearchStock.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // TODO: Implement search filter if needed
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        getParentFragmentManager().setFragmentResultListener("product_changed", getViewLifecycleOwner(), (requestKey, result) -> {
            loadStock();
        });
    }

    private void loadStock() {
        List<Product> products = dbHelper.getAllProducts();
        List<StockItem> stockList = new ArrayList<>();

        if (products != null) {
            for (Product p : products) {
                android.database.sqlite.SQLiteDatabase db = dbHelper.getReadableDatabase();
                android.database.Cursor cursor = db.query(DatabaseHelper.TABLE_VARIANTS, null,
                    DatabaseHelper.COLUMN_VAR_PROD_ID + "=?", new String[]{String.valueOf(p.getId())}, null, null, null);

                if (cursor.moveToFirst()) {
                    do {
                        int idIdx = cursor.getColumnIndex(DatabaseHelper.COLUMN_VAR_ID);
                        int sizeIdx = cursor.getColumnIndex(DatabaseHelper.COLUMN_VAR_SIZE);
                        int stockIdx = cursor.getColumnIndex(DatabaseHelper.COLUMN_VAR_STOCK);

                        if (idIdx != -1 && sizeIdx != -1 && stockIdx != -1) {
                            long varId = cursor.getLong(idIdx);
                            String size = cursor.getString(sizeIdx);
                            int stock = cursor.getInt(stockIdx);
                            stockList.add(new StockItem(varId, p.getId(), p.getName(), size, stock, p.getImageUrl()));
                        }
                    } while (cursor.moveToNext());
                }
                cursor.close();
            }
        }

        if (stockList.isEmpty()) {
            binding.llEmpty.setVisibility(View.VISIBLE);
            binding.rvStock.setVisibility(View.GONE);
        } else {
            binding.llEmpty.setVisibility(View.GONE);
            binding.rvStock.setVisibility(View.VISIBLE);
            binding.rvStock.setLayoutManager(new LinearLayoutManager(getContext()));
            binding.rvStock.setAdapter(new RecyclerView.Adapter<StockViewHolder>() {
                @NonNull
                @Override
                public StockViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                    ItemStockBinding b = ItemStockBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
                    return new StockViewHolder(b);
                }

                @Override
                public void onBindViewHolder(@NonNull StockViewHolder holder, int position) {
                    StockItem s = stockList.get(position);
                    holder.binding.tvStockProductName.setText(s.name);
                    holder.binding.tvStockVariant.setText(s.size);
                    holder.binding.tvStockCount.setText(String.valueOf(s.count));

                    Glide.with(holder.itemView.getContext())
                        .load(s.imageUrl)
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .error(android.R.drawable.ic_menu_report_image)
                        .into(holder.binding.ivStockProductImage);

                    holder.binding.btnEditStock.setOnClickListener(v -> {
                        Product p = dbHelper.getProductById(s.productId);
                        if (p != null) {
                            AddProductDialogFragment.newInstance(p).show(getParentFragmentManager(), "AddProductDialogFragment");
                        }
                    });

                    holder.binding.btnDeleteStock.setOnClickListener(v -> {
                        new AlertDialog.Builder(getContext())
                            .setTitle("Xác nhận xóa")
                            .setMessage("Xóa biến thể " + s.size + " của " + s.name + "?")
                            .setPositiveButton("Xóa", (dialog, which) -> {
                                dbHelper.deleteVariant(s.id);
                                dbHelper.addLog(requireContext(), "Deleted Variant", s.name + " (" + s.size + ")");
                                loadStock();
                            })
                            .setNegativeButton("Hủy", null)
                            .show();
                    });
                }

                @Override
                public int getItemCount() {
                    return stockList.size();
                }
            });
        }
    }

    private static class StockItem {
        long id, productId;
        String name, size, imageUrl;
        int count;
        StockItem(long id, long productId, String name, String size, int count, String imageUrl) {
            this.id = id;
            this.productId = productId;
            this.name = name;
            this.size = size;
            this.count = count;
            this.imageUrl = imageUrl;
        }
    }

    private static class StockViewHolder extends RecyclerView.ViewHolder {
        ItemStockBinding binding;
        StockViewHolder(ItemStockBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}