package com.example.pos.fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.example.pos.R;
import com.example.pos.databinding.DialogPaymentQrBinding;

import java.util.ArrayList;
import java.util.List;
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
        setupBankSuggestions();
        
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

    private void setupBankSuggestions() {
        List<BankInfo> banks = new ArrayList<>();
        banks.add(new BankInfo("MB Bank", "MB"));
        banks.add(new BankInfo("VietinBank", "ICB"));
        banks.add(new BankInfo("Vietcombank", "VCB"));
        banks.add(new BankInfo("BIDV", "BIDV"));
        banks.add(new BankInfo("Agribank", "VBA"));
        banks.add(new BankInfo("Techcombank", "TCB"));
        banks.add(new BankInfo("ACB", "ACB"));
        banks.add(new BankInfo("TPBank", "TPB"));
        banks.add(new BankInfo("VPBank", "VPB"));
        banks.add(new BankInfo("Sacombank", "STB"));
        banks.add(new BankInfo("HDBank", "HDB"));
        banks.add(new BankInfo("VIB", "VIB"));
        banks.add(new BankInfo("SHB", "SHB"));
        banks.add(new BankInfo("MSB", "MSB"));
        banks.add(new BankInfo("OCB", "OCB"));
        banks.add(new BankInfo("Nam A Bank", "NAB"));
        banks.add(new BankInfo("LPBank", "LPB"));
        banks.add(new BankInfo("SeABank", "SEA"));
        banks.add(new BankInfo("Eximbank", "EIB"));
        banks.add(new BankInfo("BaoViet Bank", "BVB"));
        banks.add(new BankInfo("Public Bank", "PBVN"));
        banks.add(new BankInfo("PVcomBank", "PVCB"));
        banks.add(new BankInfo("Bac A Bank", "BAB"));
        banks.add(new BankInfo("Dong A Bank", "DAB"));
        banks.add(new BankInfo("Viet A Bank", "VAB"));
        banks.add(new BankInfo("NCB", "NCB"));
        banks.add(new BankInfo("GPBank", "GPB"));
        banks.add(new BankInfo("OceanBank", "Oceanbank"));
        banks.add(new BankInfo("Kienlongbank", "KLB"));
        banks.add(new BankInfo("Vietbank", "VIB"));
        banks.add(new BankInfo("PG Bank", "PGB"));
        banks.add(new BankInfo("Saigonbank", "SGB"));

        BankAdapter adapter = new BankAdapter(requireContext(), banks);
        binding.etEditBankName.setAdapter(adapter);
        
        // Cấu hình để luôn hiện danh sách khi nhấn vào
        binding.etEditBankName.setThreshold(0); 
        binding.etEditBankName.setOnClickListener(v -> {
            if (!binding.etEditBankName.isPopupShowing()) {
                binding.etEditBankName.showDropDown();
            }
        });
        
        binding.etEditBankName.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus && binding.etEditBankName.getWindowToken() != null) {
                binding.etEditBankName.showDropDown();
            }
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
        String bankInput = binding.tvBankName.getText().toString();
        // Try to match with common bank short names for VietQR
        String bankCode = getBankCode(bankInput);
        
        String name = binding.tvAccountName.getText().toString().replace(" ", "%20");
        String number = binding.tvAccountNumber.getText().toString();
        
        String rawAmount = amount.replaceAll("[^0-9]", "");
        
        String qrUrl = "https://img.vietqr.io/image/" + bankCode + "-" + number + "-compact.png" +
                "?amount=" + rawAmount +
                "&addInfo=LuxePOS%20thanh%20toan" +
                "&accountName=" + name;

        Glide.with(this)
                .load(qrUrl)
                .placeholder(android.R.drawable.ic_menu_rotate)
                .error(android.R.drawable.ic_dialog_alert)
                .into(binding.ivQR);
    }

    private String getBankCode(String bankName) {
        String name = bankName.toLowerCase();
        if (name.contains("mb")) return "MB";
        if (name.contains("vietin")) return "ICB";
        if (name.contains("vietcom")) return "VCB";
        if (name.contains("bidv")) return "BIDV";
        if (name.contains("agri")) return "VBA";
        if (name.contains("techcom")) return "TCB";
        if (name.contains("acb")) return "ACB";
        if (name.contains("tp")) return "TPB";
        if (name.contains("vp")) return "VPB";
        if (name.contains("saco")) return "STB";
        if (name.contains("hd")) return "HDB";
        if (name.contains("vib")) return "VIB";
        if (name.contains("shb")) return "SHB";
        if (name.contains("msb")) return "MSB";
        if (name.contains("ocb")) return "OCB";
        return bankName.replace(" ", ""); // Fallback
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

    private static class BankInfo {
        String name;
        String code;
        BankInfo(String name, String code) {
            this.name = name;
            this.code = code;
        }
        @Override
        public String toString() { return name; }
    }

    private class BankAdapter extends ArrayAdapter<BankInfo> {
        private List<BankInfo> fullList;
        private List<BankInfo> filteredList;

        public BankAdapter(@NonNull Context context, @NonNull List<BankInfo> banks) {
            super(context, 0, banks);
            this.fullList = new ArrayList<>(banks);
            this.filteredList = new ArrayList<>(banks);
        }

        @Override
        public int getCount() { return filteredList.size(); }

        @Nullable
        @Override
        public BankInfo getItem(int position) { return filteredList.get(position); }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_bank_dropdown, parent, false);
            }
            
            BankInfo bank = getItem(position);
            ImageView ivLogo = convertView.findViewById(R.id.ivBankLogo);
            TextView tvName = convertView.findViewById(R.id.tvBankName);
            
            if (bank != null) {
                tvName.setText(bank.name);
                String logoUrl = "https://api.vietqr.io/img/" + bank.code + ".png";
                Glide.with(getContext()).load(logoUrl).into(ivLogo);
            }
            
            return convertView;
        }

        @NonNull
        @Override
        public Filter getFilter() {
            return new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults results = new FilterResults();
                    if (constraint == null || constraint.length() == 0) {
                        results.values = fullList;
                        results.count = fullList.size();
                    } else {
                        List<BankInfo> suggestions = new ArrayList<>();
                        String filterPattern = constraint.toString().toLowerCase().trim();
                        for (BankInfo item : fullList) {
                            if (item.name.toLowerCase().contains(filterPattern)) {
                                suggestions.add(item);
                            }
                        }
                        results.values = suggestions;
                        results.count = suggestions.size();
                    }
                    return results;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    filteredList = (List<BankInfo>) results.values;
                    notifyDataSetChanged();
                }

                @Override
                public CharSequence convertResultToString(Object resultValue) {
                    return ((BankInfo) resultValue).name;
                }
            };
        }
    }
}
