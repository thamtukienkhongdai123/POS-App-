package com.example.pos.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.pos.DatabaseHelper;
import com.example.pos.UserManager;
import com.example.pos.databinding.FragmentSettingsBinding;

public class SettingsFragment extends Fragment {
    private FragmentSettingsBinding binding;
    private DatabaseHelper dbHelper;
    private UserManager userManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        dbHelper = new DatabaseHelper(getContext());
        userManager = new UserManager(requireContext());

        String currentUsername = userManager.getLoggedInUser();
        binding.etSettingsUsername.setText(currentUsername);
        binding.etSettingsRole.setText("Quản Trị Viên");

        binding.btnUpdatePassword.setOnClickListener(v -> {
            String newPass = binding.etNewPassword.getText().toString();
            String confirmPass = binding.etConfirmNewPassword.getText().toString();

            if (newPass.isEmpty() || newPass.length() < 6) {
                Toast.makeText(getContext(), "Mật khẩu phải từ 6 ký tự", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!newPass.equals(confirmPass)) {
                Toast.makeText(getContext(), "Mật khẩu xác nhận không khớp", Toast.LENGTH_SHORT).show();
                return;
            }

            // Update in DB for the actual current user
            android.database.sqlite.SQLiteDatabase db = dbHelper.getWritableDatabase();
            android.content.ContentValues values = new android.content.ContentValues();
            values.put(DatabaseHelper.COLUMN_PASSWORD, newPass);
            db.update(DatabaseHelper.TABLE_USERS, values, DatabaseHelper.COLUMN_USERNAME + "=?", new String[]{currentUsername});
            
            Toast.makeText(getContext(), "Đã cập nhật mật khẩu cho tài khoản " + currentUsername + " thành công!", Toast.LENGTH_SHORT).show();
            binding.etNewPassword.setText("");
            binding.etConfirmNewPassword.setText("");
        });
    }
}