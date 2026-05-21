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
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.pos.DatabaseHelper;
import com.example.pos.R;
import com.example.pos.adapters.CartAdapter;
import com.example.pos.adapters.ProductGridAdapter;
import com.example.pos.databinding.FragmentSaleBinding;
import com.example.pos.models.CartItem;
import com.example.pos.models.Order;
import com.example.pos.models.Product;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SaleFragment extends Fragment implements CartAdapter.OnCartItemChangeListener {

    private FragmentSaleBinding binding;
    private DatabaseHelper dbHelper;
    private List<Product> allProducts;
    private List<Product> filteredProducts;
    private List<CartItem> cartItems;
    private CartAdapter cartAdapter;
    private ProductGridAdapter productAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSaleBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        dbHelper = new DatabaseHelper(getContext());
        
        allProducts = dbHelper.getAllProducts();
        filteredProducts = new ArrayList<>(allProducts);
        cartItems = new ArrayList<>();

        // Product Grid Setup
        productAdapter = new ProductGridAdapter(filteredProducts, product -> addToCart(product));
        int spanCount = getResources().getConfiguration().orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE ? 3 : 2;
        binding.rvProductSelection.setLayoutManager(new GridLayoutManager(getContext(), spanCount));
        binding.rvProductSelection.setAdapter(productAdapter);

        // Cart Setup
        cartAdapter = new CartAdapter(cartItems, this);
        if (binding.rvCart != null && binding.rvCart.getVisibility() != View.GONE) {
            binding.rvCart.setLayoutManager(new LinearLayoutManager(getContext()));
            binding.rvCart.setAdapter(cartAdapter);
        }

        // Search Logic
        binding.etSearchSale.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { filterProducts(); }
            @Override public void afterTextChanged(Editable s) {}
        });

        // Filter Chips
        binding.chipGroupFilters.setOnCheckedChangeListener((group, checkedId) -> filterProducts());

        // Full Sync Listener
        getParentFragmentManager().setFragmentResultListener("product_changed", getViewLifecycleOwner(), (requestKey, result) -> {
            allProducts = dbHelper.getAllProducts();
            filterProducts();
        });

        // Checkout Logic
        binding.btnCheckout.setOnClickListener(v -> {
            String name = binding.etCustomerName.getText().toString();
            String phone = binding.etCustomerPhone.getText().toString();
            boolean isTransfer = false;
            if (binding.togglePaymentMethod != null) {
                isTransfer = binding.togglePaymentMethod.getCheckedButtonId() == R.id.btnTransfer;
            }
            
            double discount = 0;
            if (binding.etVoucher != null && !binding.etVoucher.getText().toString().isEmpty()) {
                discount = dbHelper.validateVoucher(binding.etVoucher.getText().toString());
            }
            
            performCheckout(name, phone, isTransfer, discount);
        });

        if (binding.btnViewCart != null) {
            binding.btnViewCart.setOnClickListener(v -> {
                CartBottomSheetFragment bottomSheet = new CartBottomSheetFragment(cartItems, this, 
                    (name, phone, isTransfer, discount) -> performCheckout(name, phone, isTransfer, discount));
                bottomSheet.show(getChildFragmentManager(), "cart_bottom_sheet");
            });
        }

        if (binding.btnClearCart != null) {
            binding.btnClearCart.setOnClickListener(v -> clearCart());
        }

        // Voucher Logic
        if (binding.btnApplyVoucher != null) {
            binding.btnApplyVoucher.setOnClickListener(v -> {
                String code = binding.etVoucher.getText().toString().trim();
                double discount = dbHelper.validateVoucher(code);
                if (discount > 0) {
                    Toast.makeText(getContext(), "Áp dụng mã giảm giá thành công: " + String.format(Locale.getDefault(), "%,.0fđ", discount), Toast.LENGTH_SHORT).show();
                    updateTotalsWithDiscount(discount);
                } else {
                    Toast.makeText(getContext(), "Mã giảm giá không hợp lệ", Toast.LENGTH_SHORT).show();
                }
            });
        }

        updateTotals();
    }

    private void updateTotalsWithDiscount(double discount) {
        double subtotal = calculateTotal();
        double total = Math.max(0, subtotal - discount);
        String totalStr = String.format(Locale.getDefault(), "%,.0fđ", total);
        if (binding.tvSubtotal != null) binding.tvSubtotal.setText(String.format(Locale.getDefault(), "%,.0fđ", subtotal));
        binding.tvTotal.setText(totalStr);
    }

    private void performCheckout(String name, String phone, boolean isTransfer, double discount) {
        if (cartItems.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng chọn sản phẩm", Toast.LENGTH_SHORT).show();
            return;
        }

        String customerName = name.isEmpty() ? "Khách lẻ" : name;
        
        double subtotal = calculateTotal();
        double total = Math.max(0, subtotal - discount);

        String date = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());
        String orderCode = "ORD-" + (System.currentTimeMillis() % 10000);

        Order newOrder = new Order(orderCode, date, customerName, new com.example.pos.UserManager(requireContext()).getLoggedInUser(), total, "HOÀN TẤT", discount);

        // Save Order and update linked data
        dbHelper.addOrder(newOrder);
        dbHelper.addLog(requireContext(), "Checkout", orderCode + " - " + total);
        dbHelper.updateCustomer(customerName, phone.isEmpty() ? "0" : phone, total);
        for (CartItem item : cartItems) {
            dbHelper.updateStock(item.getProduct().getName(), item.getQuantity());
            dbHelper.addOrderItem(orderCode, item.getProduct().getName(), item.getQuantity(), item.getTotalPrice() / item.getQuantity());
        }

        if (isTransfer) {
            PaymentDialogFragment dialog = PaymentDialogFragment.newInstance(String.format(Locale.getDefault(), "%,.0fđ", total));
            dialog.show(getChildFragmentManager(), "payment_dialog");
        } else {
            Toast.makeText(getContext(), "Thanh toán thành công!", Toast.LENGTH_SHORT).show();
        }
        clearCart();
    }

    private void filterProducts() {
        String query = binding.etSearchSale.getText().toString().toLowerCase();
        int checkedId = binding.chipGroupFilters.getCheckedChipId();
        String genderFilter = "";
        if (checkedId == R.id.chipNam) genderFilter = "Nam";
        else if (checkedId == R.id.chipNu) genderFilter = "Nữ";
        else if (checkedId == R.id.chipUnisex) genderFilter = "Unisex";

        final String finalGender = genderFilter;
        filteredProducts.clear();
        for (Product p : allProducts) {
            boolean matchesQuery = p.getName().toLowerCase().contains(query) || p.getBrand().toLowerCase().contains(query);
            boolean matchesGender = finalGender.isEmpty() || p.getGender().equalsIgnoreCase(finalGender);
            
            // Check if product has any stock left in any variant
            boolean hasStock = false;
            for (Product.Variant v : p.getVariants()) {
                if (v.getStock() > 0) {
                    hasStock = true;
                    break;
                }
            }

            if (matchesQuery && matchesGender && hasStock) {
                filteredProducts.add(p);
            }
        }
        productAdapter.notifyDataSetChanged();
    }

    private double calculateTotal() {
        double total = 0;
        for (CartItem item : cartItems) {
            total += item.getTotalPrice();
        }
        return total;
    }

    private void addToCart(Product product) {
        // Find the first variant that has stock > 0
        Product.Variant availableVariant = null;
        for (Product.Variant v : product.getVariants()) {
            if (v.getStock() > 0) {
                availableVariant = v;
                break;
            }
        }

        if (availableVariant == null) {
            Toast.makeText(getContext(), "Sản phẩm này đã hết hàng!", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean found = false;
        for (CartItem item : cartItems) {
            if (item.getProduct().getName().equals(product.getName())) {
                if (item.getQuantity() >= availableVariant.getStock()) {
                    Toast.makeText(getContext(), "Không thể thêm quá số lượng trong kho!", Toast.LENGTH_SHORT).show();
                    return;
                }
                item.setQuantity(item.getQuantity() + 1);
                found = true;
                break;
            }
        }
        if (!found) {
            cartItems.add(new CartItem(product, 1));
        }
        cartAdapter.notifyDataSetChanged();
        updateTotals();
    }

    private void updateTotals() {
        double total = calculateTotal();
        String totalStr = String.format(Locale.getDefault(), "%,.0fđ", total);
        if (binding.tvSubtotal != null) binding.tvSubtotal.setText(totalStr);
        binding.tvTotal.setText(totalStr);
        
        if (binding.layoutEmptyCart != null && binding.rvCart != null) {
            binding.layoutEmptyCart.setVisibility(cartItems.isEmpty() ? View.VISIBLE : View.GONE);
            binding.rvCart.setVisibility(cartItems.isEmpty() ? View.GONE : View.VISIBLE);
        }
    }

    private void clearCart() {
        cartItems.clear();
        binding.etCustomerName.setText("");
        binding.etCustomerPhone.setText("");
        if (binding.etVoucher != null) binding.etVoucher.setText("");
        cartAdapter.notifyDataSetChanged();
        updateTotals();
    }

    @Override
    public void onQuantityChanged(int position, int newQuantity) {
        cartItems.get(position).setQuantity(newQuantity);
        cartAdapter.notifyItemChanged(position);
        updateTotals();
    }

    @Override
    public void onItemRemoved(int position) {
        cartItems.remove(position);
        cartAdapter.notifyItemRemoved(position);
        cartAdapter.notifyItemRangeChanged(position, cartItems.size());
        updateTotals();
    }
}