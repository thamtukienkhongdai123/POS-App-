package com.example.pos.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

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

    public interface OnCheckoutListener {
        void onCheckout(String name, String phone, boolean isTransfer);
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

        binding.btnCheckoutBottomSheet.setOnClickListener(v -> {
            String name = binding.etCustomerNameBS.getText().toString();
            String phone = binding.etCustomerPhoneBS.getText().toString();
            boolean isTransfer = binding.togglePaymentMethodBS.getCheckedButtonId() == R.id.btnTransferBS;
            
            checkoutListener.onCheckout(name, phone, isTransfer);
            dismiss();
        });
    }

    private void updateTotal() {
        double total = 0;
        for (CartItem item : cartItems) {
            total += item.getTotalPrice();
        }
        binding.tvTotalBS.setText(String.format(Locale.getDefault(), "%,.0fđ", total));
    }
}