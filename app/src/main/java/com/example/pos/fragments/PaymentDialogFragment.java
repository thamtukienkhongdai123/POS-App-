package com.example.pos.fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.example.pos.databinding.DialogPaymentQrBinding;

import java.util.Locale;

public class PaymentDialogFragment extends DialogFragment {

    private DialogPaymentQrBinding binding;
    private String amount;
    private SharedPreferences prefs;

    private static final String PREFS_PAYMENT = "PaymentPrefs";
    private static final String KEY_BANK_NAME = "bank_name";

    private static final String KEY_ACCOUNT_NAME = "account_name";
    private static final String KEY_ACCOUNT_NUMBER = "account_number";

    public static PaymentDialogFragment newInstance(String amount) {
        PaymentDialogFragment fragment = new PaymentDialogFragment();
        Bundle args = new Bundle();
        args.putString("amount", amount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            amount = getArguments().getString("amount");
        }
        prefs = requireContext().getSharedPreferences(PREFS_PAYMENT, Context.MODE_PRIVATE);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogPaymentQrBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        binding.tvPaymentAmount.setText(amount);
        
        loadAccountInfo();
        generateQRCode();
        
        binding.btnEditAccount.setOnClickListener(v -> {
            if (binding.layoutEditAccount.getVisibility() == View.VISIBLE) {
                binding.layoutEditAccount.setVisibility(View.GONE);
            } else {
                binding.layoutEditAccount.setVisibility(View.VISIBLE);
                binding.etEditBankName.setText(binding.tvBankName.getText());
                binding.etEditAccountName.setText(binding.tvAccountName.getText());
                binding.etEditAccountNumber.setText(binding.tvAccountNumber.getText());
            }
        });

        binding.btnSaveAccount.setOnClickListener(v -> {
            String bank = binding.etEditBankName.getText().toString().trim();
            String name = binding.etEditAccountName.getText().toString().trim();
            String number = binding.etEditAccountNumber.getText().toString().trim();

            if (bank.isEmpty() || name.isEmpty() || number.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            prefs.edit()
                    .putString(KEY_BANK_NAME, bank)
                    .putString(KEY_ACCOUNT_NAME, name)
                    .putString(KEY_ACCOUNT_NUMBER, number)
                    .apply();

            loadAccountInfo();
            generateQRCode();
            binding.layoutEditAccount.setVisibility(View.GONE);
            Toast.makeText(getContext(), "Đã cập nhật thông tin tài khoản", Toast.LENGTH_SHORT).show();
        });

        binding.btnClose.setOnClickListener(v -> dismiss());
        binding.btnCancel.setOnClickListener(v -> dismiss());
        binding.btnConfirmPaid.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Xác nhận đã nhận tiền thành công!", Toast.LENGTH_SHORT).show();
            dismiss();
        });
    }

    private void loadAccountInfo() {
        String bank = prefs.getString(KEY_BANK_NAME, "VietinBank");
        String name = prefs.getString(KEY_ACCOUNT_NAME, "NGUYEN TRUNG KIEN");
        String number = prefs.getString(KEY_ACCOUNT_NUMBER, "0338352423");

        binding.tvBankName.setText(bank);
        binding.tvAccountName.setText(name);
        binding.tvAccountNumber.setText(number);
    }

    private void generateQRCode() {
        String bank = binding.tvBankName.getText().toString().toLowerCase().replace(" ", "");
        String name = binding.tvAccountName.getText().toString().replace(" ", "%20");
        String number = binding.tvAccountNumber.getText().toString();
        
        String rawAmount = amount.replaceAll("[^0-9]", "");
        
        String qrUrl = "https://img.vietqr.io/image/" + bank + "-" + number + "-compact.png" +
                "?amount=" + rawAmount +
                "&addInfo=LuxePOS%20thanh%20toan" +
                "&accountName=" + name;

        Glide.with(this)
                .load(qrUrl)
                .placeholder(android.R.drawable.ic_menu_rotate)
                .error(android.R.drawable.ic_dialog_alert)
                .into(binding.ivQR);
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.9);
            int height = ViewGroup.LayoutParams.WRAP_CONTENT;
            dialog.getWindow().setLayout(width, height);
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
    }
}