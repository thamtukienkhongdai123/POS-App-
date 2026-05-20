package com.example.pos.fragments;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.pos.DatabaseHelper;
import com.example.pos.R;
import com.example.pos.adapters.OrderAdapter;
import com.example.pos.databinding.FragmentOrdersBinding;
import com.example.pos.models.Order;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Locale;

public class OrdersFragment extends Fragment implements OrderAdapter.OnItemClickListener {
    private FragmentOrdersBinding binding;
    private DatabaseHelper dbHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentOrdersBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        dbHelper = new DatabaseHelper(requireContext());
        loadOrders();
        checkPermissions();
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }
    }

    private void loadOrders() {
        List<Order> orderList = dbHelper.getAllOrders();
        if (orderList == null || orderList.isEmpty()) {
            binding.tvEmptyOrders.setVisibility(View.VISIBLE);
            binding.rvOrders.setVisibility(View.GONE);
            binding.tvOrderSummary.setText(getString(R.string.order_summary_empty));
        } else {
            binding.tvEmptyOrders.setVisibility(View.GONE);
            binding.rvOrders.setVisibility(View.VISIBLE);
            binding.tvOrderSummary.setText(getString(R.string.order_summary, orderList.size()));
            OrderAdapter adapter = new OrderAdapter(orderList, this);
            binding.rvOrders.setLayoutManager(new LinearLayoutManager(requireContext()));
            binding.rvOrders.setAdapter(adapter);
        }
    }

    @Override
    public void onItemClick(Order order) { }

    @Override
    public void onExportPdfClick(Order order) {
        exportOrderToPdf(order);
    }

    private void exportOrderToPdf(Order order) {
        List<DatabaseHelper.OrderItem> items = dbHelper.getOrderItems(order.getOrderId());
        PdfDocument document = new PdfDocument();

        // Kích thước trang chuẩn cho hóa đơn nhiệt (80mm ~ 300px)
        int pageWidth = 300;
        int headerHeight = 350; 
        int itemHeight = 35; 
        int footerHeight = 350;
        int pageHeight = headerHeight + (items.size() * itemHeight) + footerHeight;

        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();

        int x = 15;
        int y = 45;

        // Vẽ khung viền tinh tế
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1f);
        paint.setColor(Color.DKGRAY);
        canvas.drawRect(5, 5, pageWidth - 5, pageHeight - 5, paint);
        
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.BLACK);

        // Header - Tên cửa hàng
        paint.setTextSize(24f);
        paint.setFakeBoldText(true);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("LUXESCENTS POS", pageWidth / 2f, y, paint);

        // Thông tin cửa hàng chi tiết
        paint.setTextSize(9f);
        paint.setFakeBoldText(false);
        y += 25;
        canvas.drawText("THẾ GIỚI NƯỚC HOA CAO CẤP", pageWidth / 2f, y, paint);
        y += 15;
        canvas.drawText("Đ/C: 123 Đường Perfume, Quận 1, TP. HCM", pageWidth / 2f, y, paint);
        y += 15;
        canvas.drawText("Hotline: 0123.456.789 - 0987.654.321", pageWidth / 2f, y, paint);
        y += 15;
        canvas.drawText("Email: sales@luxescents.vn", pageWidth / 2f, y, paint);
        y += 15;
        canvas.drawText("Website: www.luxescents.vn", pageWidth / 2f, y, paint);

        y += 20;
        paint.setStrokeWidth(1.2f);
        canvas.drawLine(x, y, pageWidth - x, y, paint);

        // Tiêu đề hóa đơn
        y += 40;
        paint.setFakeBoldText(true);
        paint.setTextSize(18f);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("HÓA ĐƠN BÁN LẺ", pageWidth / 2f, y, paint);

        // Thông tin khách hàng & Đơn hàng
        paint.setTextSize(10f);
        paint.setFakeBoldText(false);
        paint.setTextAlign(Paint.Align.LEFT);
        y += 35;
        canvas.drawText("Số HD: " + order.getOrderId(), x, y, paint);
        paint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText("Ngày: " + order.getDate(), pageWidth - x, y, paint);
        
        paint.setTextAlign(Paint.Align.LEFT);
        y += 20;
        String customerName = order.getCustomer();
        String phone = dbHelper.getCustomerPhone(customerName);
        canvas.drawText("Khách hàng: " + (customerName != null ? customerName : "Khách vãng lai"), x, y, paint);
        
        if (phone != null && !phone.isEmpty()) {
            y += 20;
            canvas.drawText("Điện thoại: " + phone, x, y, paint);
        }
        
        y += 20;
        canvas.drawText("Nhân viên: " + order.getStaff(), x, y, paint);

        // Bảng sản phẩm - Tiêu đề cột
        y += 30;
        paint.setStrokeWidth(1.5f);
        canvas.drawLine(x, y, pageWidth - x, y, paint);
        y += 20;
        paint.setFakeBoldText(true);
        canvas.drawText("Sản phẩm", x, y, paint);
        canvas.drawText("SL", 140, y, paint);
        canvas.drawText("Đơn giá", 175, y, paint);
        paint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText("T.Tiền", pageWidth - x, y, paint);

        y += 10;
        paint.setStrokeWidth(0.8f);
        canvas.drawLine(x, y, pageWidth - x, y, paint);

        // Danh sách sản phẩm
        paint.setFakeBoldText(false);
        paint.setTextAlign(Paint.Align.LEFT);
        y += 25;

        double subtotal = 0;
        for (DatabaseHelper.OrderItem item : items) {
            String name = item.productName;
            if (name.length() > 18) name = name.substring(0, 16) + "..";
            
            canvas.drawText(name, x, y, paint);
            canvas.drawText(String.valueOf(item.quantity), 145, y, paint);
            canvas.drawText(String.format(Locale.getDefault(), "%,.0f", item.price), 175, y, paint);
            
            double itemTotal = item.price * item.quantity;
            paint.setTextAlign(Paint.Align.RIGHT);
            canvas.drawText(String.format(Locale.getDefault(), "%,.0f", itemTotal), pageWidth - x, y, paint);
            paint.setTextAlign(Paint.Align.LEFT);
            
            subtotal += itemTotal;
            y += 25;
        }

        // Phần tổng kết chi phí
        y += 10;
        paint.setStrokeWidth(1f);
        canvas.drawLine(x, y, pageWidth - x, y, paint);

        double discount = order.getDiscount();
        double tax = (subtotal - discount) * 0.1;
        double finalTotal = (subtotal - discount) + tax;

        y += 30;
        paint.setTextSize(11f);
        paint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText("Cộng tiền hàng:", x, y, paint);
        paint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText(String.format(Locale.getDefault(), "%,.0f", subtotal), pageWidth - x, y, paint);

        if (discount > 0) {
            y += 20;
            paint.setTextAlign(Paint.Align.LEFT);
            canvas.drawText("Chiết khấu:", x, y, paint);
            paint.setTextAlign(Paint.Align.RIGHT);
            canvas.drawText(String.format(Locale.getDefault(), "-%,.0f", discount), pageWidth - x, y, paint);
        }

        y += 20;
        paint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText("Thuế GTGT (10%):", x, y, paint);
        paint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText(String.format(Locale.getDefault(), "%,.0f", tax), pageWidth - x, y, paint);

        y += 35;
        paint.setTextSize(16f);
        paint.setFakeBoldText(true);
        paint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText("TỔNG THANH TOÁN:", x, y, paint);
        paint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText(String.format(Locale.getDefault(), "%,.0f VNĐ", finalTotal), pageWidth - x, y, paint);

        // Footer - Lời cảm ơn và lưu ý
        paint.setFakeBoldText(false);
        paint.setTextSize(10f);
        paint.setTextAlign(Paint.Align.CENTER);
        y += 70;
        canvas.drawText("Cảm ơn Quý khách. Hẹn gặp lại!", pageWidth / 2f, y, paint);
        
        y += 20;
        paint.setTextSize(8f);
        paint.setColor(Color.GRAY);
        canvas.drawText("Quý khách vui lòng kiểm tra hàng trước khi rời quầy.", pageWidth / 2f, y, paint);
        y += 12;
        canvas.drawText("Mọi thắc mắc vui lòng liên hệ Hotline trong 24h.", pageWidth / 2f, y, paint);
        
        y += 30;
        paint.setFakeBoldText(true);
        paint.setTextSize(9f);
        paint.setColor(Color.BLACK);
        canvas.drawText("--- HỆ THỐNG QUẢN LÝ LUXESCENTS ---", pageWidth / 2f, y, paint);

        document.finishPage(page);

        String fileName = "Bill_" + order.getOrderId().replace("-", "_") + ".pdf";
        saveAndOpenPdf(document, fileName);
    }

    private void saveAndOpenPdf(PdfDocument document, String fileName) {
        try {
            // Sử dụng thư mục Download chuẩn để tránh lỗi FileProvider
            File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            if (!dir.exists() && !dir.mkdirs()) {
                dir = requireContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
            }
            File file = new File(dir, fileName);

            FileOutputStream fos = new FileOutputStream(file);
            document.writeTo(fos);
            document.close();
            fos.close();

            // Cập nhật Media Scanner để file xuất hiện trên PC ngay lập tức
            android.media.MediaScannerConnection.scanFile(requireContext(), 
                new String[]{file.getAbsolutePath()}, new String[]{"application/pdf"}, null);

            Toast.makeText(requireContext(), "Đã xuất hóa đơn: " + fileName, Toast.LENGTH_LONG).show();
            openPdfFile(file);
        } catch (Exception e) {
            if (document != null) try { document.close(); } catch (Exception ignored) {}
            Toast.makeText(requireContext(), "Lỗi: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void openPdfFile(File file) {
        try {
            // Kiểm tra file tồn tại
            if (!file.exists()) {
                Toast.makeText(requireContext(), "File không tồn tại trên bộ nhớ!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Tạo URI an toàn qua FileProvider
            Uri uri = FileProvider.getUriForFile(requireContext(), 
                requireContext().getPackageName() + ".provider", file);
            
            // Intent để mở file
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "application/pdf");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            
            // Luôn dùng Chooser để người dùng có nhiều lựa chọn
            Intent chooser = Intent.createChooser(intent, "Mở hóa đơn qua:");
            
            // Kiểm tra và thực thi
            if (intent.resolveActivity(requireContext().getPackageManager()) != null) {
                startActivity(chooser);
            } else {
                // Nếu không có app đọc PDF, dùng chế độ Chia sẻ (Zalo, Bluetooth, Drive...)
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("application/pdf");
                shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(Intent.createChooser(shareIntent, "Máy chưa có app đọc PDF, hãy chia sẻ qua:"));
            }
        } catch (Exception e) {
            // Hiện lỗi chi tiết để debug
            Toast.makeText(requireContext(), "Lỗi: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }
}
