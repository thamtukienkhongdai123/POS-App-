package com.example.pos.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.pos.DatabaseHelper;
import com.example.pos.R;
import com.example.pos.adapters.CartAdapter;
import com.example.pos.databinding.LayoutCartBottomSheetBinding;
import com.example.pos.models.CartItem;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.List;
import java.util.Locale;

public class CartBottomSheetFragment extends BottomSheetDialogFragment {

    private LayoutCartBottomSheetBinding binding;
    private List<CartItem> cartItems;
    private CartAdapter.OnCartItemChangeListener listener;
    private OnCheckoutListener checkoutListener;
    private DatabaseHelper dbHelper;
    private double currentDiscount = 0;

    public interface OnCheckoutListener {
        void onCheckout(String name, String phone, boolean isTransfer, double discount);
    }

    public CartBottomSheetFragment(List<CartItem> cartItems, CartAdapter.OnCartItemChangeListener listener, OnCheckoutListener checkoutListener) {
        this.cartItems = cartItems;
        this.listener = listener;
        this.checkoutListener = checkoutListener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = LayoutCartBottomSheetBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        dbHelper = new DatabaseHelper(requireContext());

        updateTotal();

        CartAdapter adapter = new CartAdapter(cartItems, new CartAdapter.OnCartItemChangeListener() {
            @Override
            public void onQuantityChanged(int position, int newQuantity) {
                listener.onQuantityChanged(position, newQuantity);
                updateTotal();
                binding.rvCartBottomSheet.getAdapter().notifyItemChanged(position);
            }

            @Override
            public void onItemRemoved(int position) {
                listener.onItemRemoved(position);
                updateTotal();
                binding.rvCartBottomSheet.getAdapter().notifyItemRemoved(position);
                if (cartItems.isEmpty()) dismiss();
            }
        });

        binding.rvCartBottomSheet.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvCartBottomSheet.setAdapter(adapter);

        binding.btnApplyVoucherBS.setOnClickListener(v -> {
            String code = binding.etVoucherBS.getText().toString().trim();
            if (code.isEmpty()) {
                currentDiscount = 0;
                updateTotal();
                return;
            }

            double discount = dbHelper.validateVoucher(code);
            if (discount > 0) {
                currentDiscount = discount;
                Toast.makeText(getContext(), "Áp dụng thành công: -" + String.format(Locale.getDefault(), "%,.0fđ", discount), Toast.LENGTH_SHORT).show();
            } else {
                currentDiscount = 0;
                Toast.makeText(getContext(), "Mã giảm giá không hợp lệ", Toast.LENGTH_SHORT).show();
            }
            updateTotal();
        });

        binding.btnCheckoutBottomSheet.setOnClickListener(v -> {
            String name = binding.etCustomerNameBS.getText().toString();
            String phone = binding.etCustomerPhoneBS.getText().toString();
            boolean isTransfer = binding.togglePaymentMethodBS.getCheckedButtonId() == R.id.btnTransferBS;
            
            checkoutListener.onCheckout(name, phone, isTransfer, currentDiscount);
            dismiss();
        });
    }

    private void updateTotal() {
        double subtotal = 0;
        for (CartItem item : cartItems) {
            subtotal += item.getTotalPrice();
        }
        double total = Math.max(0, subtotal - currentDiscount);
        binding.tvTotalBS.setText(String.format(Locale.getDefault(), "%,.0fđ", total));
    }
}