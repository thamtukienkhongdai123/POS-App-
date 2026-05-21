package com.example.pos.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.pos.DatabaseHelper;
import com.example.pos.adapters.ProductAdapter;
import com.example.pos.databinding.FragmentProductsBinding;
import com.example.pos.models.Product;

import java.util.List;

public class ProductsFragment extends Fragment implements ProductAdapter.OnProductActionListener {
    private FragmentProductsBinding binding;
    private DatabaseHelper dbHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProductsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        dbHelper = new DatabaseHelper(getContext());

        binding.btnAddProduct.setOnClickListener(v -> {
            AddProductDialogFragment dialog = new AddProductDialogFragment();
            dialog.show(getParentFragmentManager(), "add_product_dialog");
        });
        
        // Listen for dialog dismiss to reload
        getParentFragmentManager().setFragmentResultListener("product_changed", getViewLifecycleOwner(), (requestKey, result) -> {
            loadProducts();
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadProducts();
    }

    private void loadProducts() {
        List<Product> productList = dbHelper.getAllProducts();
        if (productList == null || productList.isEmpty()) {
            binding.tvEmptyProducts.setVisibility(View.VISIBLE);
            binding.rvProducts.setVisibility(View.GONE);
        } else {
            binding.tvEmptyProducts.setVisibility(View.GONE);
            binding.rvProducts.setVisibility(View.VISIBLE);
            ProductAdapter adapter = new ProductAdapter(productList, this);
            binding.rvProducts.setLayoutManager(new LinearLayoutManager(getContext()));
            binding.rvProducts.setAdapter(adapter);
        }
    }

    @Override
    public void onEdit(Product product) {
        AddProductDialogFragment dialog = AddProductDialogFragment.newInstance(product);
        dialog.show(getParentFragmentManager(), "edit_product_dialog");
    }

    @Override
    public void onDelete(Product product) {
        new AlertDialog.Builder(getContext())
            .setTitle("Xác nhận xóa")
            .setMessage("Bạn có chắc muốn xóa sản phẩm " + product.getName() + " không?")
            .setPositiveButton("Xóa", (dialog, which) -> {
                dbHelper.deleteProduct(product.getId());
                dbHelper.addLog(requireContext(), "Deleted Product", product.getName());
                loadProducts();
                Toast.makeText(getContext(), "Đã xóa sản phẩm", Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("Hủy", null)
            .show();
    }
}