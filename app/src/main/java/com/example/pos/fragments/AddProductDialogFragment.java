package com.example.pos.fragments;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.example.pos.DatabaseHelper;
import com.example.pos.R;
import com.example.pos.databinding.DialogAddProductBinding;
import com.example.pos.databinding.ItemAddVariantBinding;
import com.example.pos.models.Product;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class AddProductDialogFragment extends DialogFragment {

    private DialogAddProductBinding binding;
    private DatabaseHelper dbHelper;
    private Product editProduct;
    private String selectedImageUri = "";
    private List<ItemAddVariantBinding> variantBindings = new ArrayList<>();

    private final ActivityResultLauncher<String> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedImageUri = uri.toString();
                    binding.layoutUploadImage.findViewById(R.id.ivPreview).setVisibility(View.VISIBLE);
                    Glide.with(this).load(uri).into((android.widget.ImageView) binding.layoutUploadImage.findViewById(R.id.ivPreview));
                }
            }
    );

    public static AddProductDialogFragment newInstance(Product product) {
        AddProductDialogFragment fragment = new AddProductDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable("product", product);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            editProduct = (Product) getArguments().getSerializable("product");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogAddProductBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        dbHelper = new DatabaseHelper(getContext());

        binding.btnClose.setOnClickListener(v -> dismiss());
        binding.btnCancel.setOnClickListener(v -> dismiss());

        // Setup Gender Dropdown
        String[] genders = new String[]{"Nam", "Nữ", "Unisex (Cả hai)"};
        android.widget.ArrayAdapter<String> genderAdapter = new android.widget.ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, genders);
        binding.spinnerGender.setAdapter(genderAdapter);
        binding.spinnerGender.setOnClickListener(v -> binding.spinnerGender.showDropDown());
        binding.spinnerGender.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) binding.spinnerGender.showDropDown();
        });

        // Setup TabLayout for Image
        binding.tabLayoutImage.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    binding.etImageUrl.setVisibility(View.VISIBLE);
                    binding.layoutUploadImage.setVisibility(View.GONE);
                } else {
                    binding.etImageUrl.setVisibility(View.GONE);
                    binding.layoutUploadImage.setVisibility(View.VISIBLE);
                }
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        // Real-time Image Preview for Link
        binding.etImageUrl.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(android.text.Editable s) {
                String url = s.toString().trim();
                if (!url.isEmpty()) {
                    android.widget.ImageView ivPreview = binding.layoutUploadImage.findViewById(R.id.ivPreview);
                    ivPreview.setVisibility(View.VISIBLE);
                    binding.layoutUploadImage.setVisibility(View.VISIBLE); // Show layout to see preview
                    Glide.with(AddProductDialogFragment.this).load(url).into(ivPreview);
                }
            }
        });

        binding.btnSelectImage.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));

        if (editProduct != null) {
            binding.etProductName.setText(editProduct.getName());
            binding.etBrand.setText(editProduct.getBrand());
            
            String img = editProduct.getImageUrl();
            if (img != null && img.startsWith("content://")) {
                TabLayout.Tab tab = binding.tabLayoutImage.getTabAt(1);
                if (tab != null) tab.select();
                selectedImageUri = img;
                binding.layoutUploadImage.findViewById(R.id.ivPreview).setVisibility(View.VISIBLE);
                Glide.with(this).load(img).into((android.widget.ImageView) binding.layoutUploadImage.findViewById(R.id.ivPreview));
            } else {
                binding.etImageUrl.setText(img);
            }
            
            binding.spinnerGender.setText(editProduct.getGender(), false);
            
            for (Product.Variant variant : editProduct.getVariants()) {
                addVariantRowWithData(variant);
            }
        } else {
            addNewVariantRow();
        }

        binding.btnAddVariant.setOnClickListener(v -> addNewVariantRow());

        binding.btnSave.setOnClickListener(v -> saveProduct());
    }

    private void addNewVariantRow() {
        ItemAddVariantBinding vBinding = ItemAddVariantBinding.inflate(getLayoutInflater(), binding.containerVariants, true);
        variantBindings.add(vBinding);
        vBinding.btnDeleteVariant.setOnClickListener(v -> {
            binding.containerVariants.removeView(vBinding.getRoot());
            variantBindings.remove(vBinding);
        });
    }

    private void addVariantRowWithData(Product.Variant variant) {
        ItemAddVariantBinding vBinding = ItemAddVariantBinding.inflate(getLayoutInflater(), binding.containerVariants, true);
        vBinding.etSize.setText(variant.getSize());
        vBinding.etPrice.setText(String.valueOf((int) variant.getPrice()));
        vBinding.etStock.setText(String.valueOf(variant.getStock()));
        variantBindings.add(vBinding);
        vBinding.btnDeleteVariant.setOnClickListener(v -> {
            binding.containerVariants.removeView(vBinding.getRoot());
            variantBindings.remove(vBinding);
        });
    }

    private void saveProduct() {
        String name = binding.etProductName.getText().toString().trim();
        String brand = binding.etBrand.getText().toString().trim();
        String imageUrl = (binding.tabLayoutImage.getSelectedTabPosition() == 0) 
                ? binding.etImageUrl.getText().toString().trim() 
                : selectedImageUri;
        String gender = binding.spinnerGender.getText().toString();

        if (name.isEmpty() || brand.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng nhập tên và thương hiệu", Toast.LENGTH_SHORT).show();
            return;
        }

        android.database.sqlite.SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            if (editProduct != null) {
                dbHelper.deleteProduct(db, editProduct.getId());
            }

            long productId = dbHelper.insertProduct(db, name, brand, gender, imageUrl);
            if (productId != -1) {
                int savedCount = 0;
                // Duyệt qua tất cả các View trong container để lấy dữ liệu thực tế nhất
                for (int i = 0; i < binding.containerVariants.getChildCount(); i++) {
                    View row = binding.containerVariants.getChildAt(i);
                    EditText etSize = row.findViewById(R.id.etSize);
                    EditText etPrice = row.findViewById(R.id.etPrice);
                    EditText etStock = row.findViewById(R.id.etStock);

                    if (etSize != null && etPrice != null && etStock != null) {
                        String size = etSize.getText().toString().trim();
                        String priceStr = etPrice.getText().toString().trim();
                        String stockStr = etStock.getText().toString().trim();

                        // Lưu nếu có bất kỳ thông tin nào được nhập vào dòng này
                        if (!size.isEmpty() || !priceStr.isEmpty() || !stockStr.isEmpty()) {
                            double price = Double.parseDouble(priceStr.isEmpty() ? "0" : priceStr);
                            int stock = Integer.parseInt(stockStr.isEmpty() ? "0" : stockStr);
                            dbHelper.insertVariant(db, productId, size, price, stock, "");
                            savedCount++;
                        }
                    }
                }
                
                if (savedCount > 0) {
                    db.setTransactionSuccessful();
                    db.endTransaction(); // Kết thúc giao dịch ngay lập tức
                    
                    Toast.makeText(getContext(), "Đã lưu " + name + " với " + savedCount + " loại!", Toast.LENGTH_SHORT).show();
                    getParentFragmentManager().setFragmentResult("product_changed", new Bundle());
                    dismiss();
                    return; // Thoát hàm để tránh chạy vào phần catch/finally bên dưới sai lệch
                } else {
                    Toast.makeText(getContext(), "Vui lòng nhập ít nhất một dòng Giá và Số lượng!", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            Toast.makeText(getContext(), "Lỗi hệ thống: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            if (db.inTransaction()) {
                db.endTransaction();
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
    }
}