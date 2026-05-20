package com.example.pos;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;

import com.example.pos.databinding.ActivityMainBinding;
import com.example.pos.fragments.DashboardFragment;
import com.example.pos.fragments.SaleFragment;
import com.example.pos.fragments.ProductsFragment;
import com.example.pos.fragments.OrdersFragment;
import com.example.pos.fragments.CustomersFragment;
import com.example.pos.fragments.StockFragment;
import com.example.pos.fragments.DiscountsFragment;

import com.example.pos.fragments.LogsFragment;
import com.example.pos.fragments.ReturnsFragment;
import com.example.pos.fragments.SettingsFragment;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, binding.drawerLayout, binding.toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        binding.drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        binding.navView.setNavigationItemSelectedListener(this);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.nav_host_fragment, new DashboardFragment())
                    .commit();
            binding.navView.setCheckedItem(R.id.nav_dashboard);
            setTitle("Bảng Điều Khiển");
        }
    }

    @Override
    public void onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment selectedFragment = null;
        String title = "";

        int id = item.getItemId();
        if (id == R.id.nav_sale) {
            selectedFragment = new SaleFragment();
            title = "Bán Hàng";
        } else if (id == R.id.nav_dashboard) {
            selectedFragment = new DashboardFragment();
            title = "Bảng Điều Khiển";
        } else if (id == R.id.nav_products) {
            selectedFragment = new ProductsFragment();
            title = "Sản Phẩm";
        } else if (id == R.id.nav_orders) {
            selectedFragment = new OrdersFragment();
            title = "Lịch Sử Đơn Hàng";
        } else if (id == R.id.nav_customers) {
            selectedFragment = new CustomersFragment();
            title = "Khách Hàng";
        } else if (id == R.id.nav_stock) {
            selectedFragment = new StockFragment();
            title = "Quản Lý Tồn Kho";
        } else if (id == R.id.nav_discounts) {
            selectedFragment = new DiscountsFragment();
            title = "Mã Giảm Giá";

        } else if (id == R.id.nav_logs) {
            selectedFragment = new LogsFragment();
            title = "Nhật Ký Hoạt Động";
        } else if (id == R.id.nav_returns) {
            selectedFragment = new ReturnsFragment();
            title = "Hoàn Hàng";
        } else if (id == R.id.nav_settings) {
            selectedFragment = new SettingsFragment();
            title = "Cài Đặt";
        }

        if (selectedFragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.nav_host_fragment, selectedFragment)
                    .commit();
            setTitle(title);
        }

        binding.drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}