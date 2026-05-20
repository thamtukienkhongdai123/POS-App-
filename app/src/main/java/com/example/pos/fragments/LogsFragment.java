package com.example.pos.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pos.DatabaseHelper;
import com.example.pos.R;
import com.example.pos.databinding.FragmentLogsBinding;
import com.example.pos.databinding.ItemLogBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class LogsFragment extends Fragment {
    private FragmentLogsBinding binding;
    private DatabaseHelper dbHelper;
    private List<LogEntry> allLogs = new ArrayList<>();
    private LogAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentLogsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        dbHelper = new DatabaseHelper(getContext());
        
        setupRecyclerView();
        loadLogs();
        setupSearch();
    }

    private void setupRecyclerView() {
        adapter = new LogAdapter(new ArrayList<>());
        binding.rvLogs.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvLogs.setAdapter(adapter);
    }

    private void loadLogs() {
        allLogs.clear();
        android.database.sqlite.SQLiteDatabase db = dbHelper.getReadableDatabase();
        android.database.Cursor cursor = db.query(DatabaseHelper.TABLE_LOGS, null, null, null, null, null, DatabaseHelper.COLUMN_LOG_ID + " DESC");

        if (cursor.moveToFirst()) {
            do {
                int timeIdx = cursor.getColumnIndex(DatabaseHelper.COLUMN_LOG_TIME);
                int userIdx = cursor.getColumnIndex(DatabaseHelper.COLUMN_LOG_USER);
                int actionIdx = cursor.getColumnIndex(DatabaseHelper.COLUMN_LOG_ACTION);
                int targetIdx = cursor.getColumnIndex(DatabaseHelper.COLUMN_LOG_TARGET);

                if (timeIdx != -1 && userIdx != -1 && actionIdx != -1 && targetIdx != -1) {
                    String time = cursor.getString(timeIdx);
                    String user = cursor.getString(userIdx);
                    String action = cursor.getString(actionIdx);
                    String target = cursor.getString(targetIdx);
                    allLogs.add(new LogEntry(time, user, action, target));
                }
            } while (cursor.moveToNext());
        }
        cursor.close();

        updateList(allLogs);
    }

    private void setupSearch() {
        binding.etSearchLog.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterLogs(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filterLogs(String query) {
        List<LogEntry> filtered = allLogs.stream()
                .filter(l -> l.action.toLowerCase().contains(query.toLowerCase()) || 
                            l.target.toLowerCase().contains(query.toLowerCase()) ||
                            l.user.toLowerCase().contains(query.toLowerCase()))
                .collect(Collectors.toList());
        updateList(filtered);
    }

    private void updateList(List<LogEntry> logs) {
        adapter.updateData(logs);
        binding.tvTotalLogs.setText(logs.size() + " bản ghi");
        
        if (logs.isEmpty()) {
            binding.llEmptyLogs.setVisibility(View.VISIBLE);
            binding.rvLogs.setVisibility(View.GONE);
        } else {
            binding.llEmptyLogs.setVisibility(View.GONE);
            binding.rvLogs.setVisibility(View.VISIBLE);
        }
    }

    private static class LogEntry {
        String time, user, action, target;
        LogEntry(String time, String user, String action, String target) {
            this.time = time;
            this.user = user;
            this.action = action;
            this.target = target;
        }
    }

    private class LogAdapter extends RecyclerView.Adapter<LogViewHolder> {
        private List<LogEntry> logs;

        public LogAdapter(List<LogEntry> logs) {
            this.logs = logs;
        }

        public void updateData(List<LogEntry> newLogs) {
            this.logs = newLogs;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public LogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemLogBinding b = ItemLogBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new LogViewHolder(b);
        }

        @Override
        public void onBindViewHolder(@NonNull LogViewHolder holder, int position) {
            LogEntry log = logs.get(position);
            holder.binding.tvLogTime.setText(log.time);
            holder.binding.tvLogUser.setText(log.user);
            holder.binding.tvLogAction.setText(formatAction(log.action));
            holder.binding.tvLogTarget.setText(log.target);

            // Color code the action badge
            String action = log.action.toLowerCase();
            if (action.contains("created") || action.contains("thêm") || action.contains("tạo")) {
                holder.binding.tvLogAction.setTextColor(android.graphics.Color.parseColor("#10B981"));
            } else if (action.contains("deleted") || action.contains("xóa")) {
                holder.binding.tvLogAction.setTextColor(android.graphics.Color.parseColor("#EF4444"));
            } else if (action.contains("updated") || action.contains("sửa") || action.contains("cập nhật")) {
                holder.binding.tvLogAction.setTextColor(android.graphics.Color.parseColor("#F59E0B"));
            } else {
                holder.binding.tvLogAction.setTextColor(android.graphics.Color.parseColor("#6366F1"));
            }
        }

        private String formatAction(String action) {
            // Simple mapping for display as chips
            if (action.toLowerCase().contains("created")) return "Tạo mới";
            if (action.toLowerCase().contains("deleted")) return "Đã xóa";
            if (action.toLowerCase().contains("updated")) return "Cập nhật";
            if (action.toLowerCase().contains("checkout")) return "Bán hàng";
            if (action.toLowerCase().contains("refund")) return "Hoàn tiền";
            return action;
        }

        @Override
        public int getItemCount() {
            return logs.size();
        }
    }

    private static class LogViewHolder extends RecyclerView.ViewHolder {
        ItemLogBinding binding;
        LogViewHolder(ItemLogBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
