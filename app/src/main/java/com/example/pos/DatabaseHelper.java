package com.example.pos;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.pos.models.Product;
import com.example.pos.models.Order;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "LuxeScents.db";
    private static final int DATABASE_VERSION = 6;

    // Table Users
    public static final String TABLE_USERS = "users";
    public static final String COLUMN_USER_ID = "id";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_PASSWORD = "password";

    // Table Products
    public static final String TABLE_PRODUCTS = "products";
    public static final String COLUMN_PROD_ID = "id";
    public static final String COLUMN_PROD_NAME = "name";
    public static final String COLUMN_PROD_BRAND = "brand";
    public static final String COLUMN_PROD_GENDER = "gender";
    public static final String COLUMN_PROD_IMAGE = "image_url";

    // Table Variants
    public static final String TABLE_VARIANTS = "variants";
    public static final String COLUMN_VAR_ID = "id";
    public static final String COLUMN_VAR_PROD_ID = "product_id";
    public static final String COLUMN_VAR_SIZE = "size";
    public static final String COLUMN_VAR_PRICE = "price";
    public static final String COLUMN_VAR_STOCK = "stock";
    public static final String COLUMN_VAR_BARCODE = "barcode";

    // Table Orders
    public static final String TABLE_ORDERS = "orders";
    public static final String COLUMN_ORDER_ID = "id";
    public static final String COLUMN_ORDER_CODE = "order_code";
    public static final String COLUMN_ORDER_DATE = "date";
    public static final String COLUMN_ORDER_CUSTOMER = "customer";
    public static final String COLUMN_ORDER_TOTAL = "total";
    public static final String COLUMN_ORDER_STATUS = "status";
    public static final String COLUMN_ORDER_DISCOUNT = "discount";

    // Table Customers
    public static final String TABLE_CUSTOMERS = "customers";
    public static final String COLUMN_CUST_ID = "id";
    public static final String COLUMN_CUST_NAME = "name";
    public static final String COLUMN_CUST_PHONE = "phone";
    public static final String COLUMN_CUST_EMAIL = "email";
    public static final String COLUMN_CUST_ADDRESS = "address";
    public static final String COLUMN_CUST_TOTAL_SPENT = "total_spent";

    // Table Vouchers
    public static final String TABLE_VOUCHERS = "vouchers";
    public static final String COLUMN_VOUCH_ID = "id";
    public static final String COLUMN_VOUCH_CODE = "code";
    public static final String COLUMN_VOUCH_AMOUNT = "amount";
    public static final String COLUMN_VOUCH_STATUS = "status";

    // Table Order Items
    public static final String TABLE_ORDER_ITEMS = "order_items";
    public static final String COLUMN_ITEM_ID = "id";
    public static final String COLUMN_ITEM_ORDER_CODE = "order_code";
    public static final String COLUMN_ITEM_PRODUCT_NAME = "product_name";
    public static final String COLUMN_ITEM_QUANTITY = "quantity";
    public static final String COLUMN_ITEM_PRICE = "price";

    // Table Logs
    public static final String TABLE_LOGS = "logs";
    public static final String COLUMN_LOG_ID = "id";
    public static final String COLUMN_LOG_TIME = "time";
    public static final String COLUMN_LOG_USER = "user";
    public static final String COLUMN_LOG_ACTION = "log_action";
    public static final String COLUMN_LOG_TARGET = "target";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + "("
                + COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_USERNAME + " TEXT UNIQUE,"
                + COLUMN_PASSWORD + " TEXT" + ")";
        db.execSQL(CREATE_USERS_TABLE);

        String CREATE_PRODUCTS_TABLE = "CREATE TABLE " + TABLE_PRODUCTS + "("
                + COLUMN_PROD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_PROD_NAME + " TEXT,"
                + COLUMN_PROD_BRAND + " TEXT,"
                + COLUMN_PROD_GENDER + " TEXT,"
                + COLUMN_PROD_IMAGE + " TEXT" + ")";
        db.execSQL(CREATE_PRODUCTS_TABLE);

        String CREATE_VARIANTS_TABLE = "CREATE TABLE " + TABLE_VARIANTS + "("
                + COLUMN_VAR_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_VAR_PROD_ID + " INTEGER,"
                + COLUMN_VAR_SIZE + " TEXT,"
                + COLUMN_VAR_PRICE + " REAL,"
                + COLUMN_VAR_STOCK + " INTEGER,"
                + COLUMN_VAR_BARCODE + " TEXT,"
                + "FOREIGN KEY(" + COLUMN_VAR_PROD_ID + ") REFERENCES " + TABLE_PRODUCTS + "(" + COLUMN_PROD_ID + ") ON DELETE CASCADE)";
        db.execSQL(CREATE_VARIANTS_TABLE);

        String CREATE_ORDERS_TABLE = "CREATE TABLE " + TABLE_ORDERS + "("
                + COLUMN_ORDER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_ORDER_CODE + " TEXT,"
                + COLUMN_ORDER_DATE + " TEXT,"
                + COLUMN_ORDER_CUSTOMER + " TEXT,"
                + COLUMN_ORDER_TOTAL + " REAL,"
                + COLUMN_ORDER_STATUS + " TEXT,"
                + COLUMN_ORDER_DISCOUNT + " REAL DEFAULT 0" + ")";
        db.execSQL(CREATE_ORDERS_TABLE);

        String CREATE_CUSTOMERS_TABLE = "CREATE TABLE " + TABLE_CUSTOMERS + "("
                + COLUMN_CUST_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_CUST_NAME + " TEXT,"
                + COLUMN_CUST_PHONE + " TEXT UNIQUE,"
                + COLUMN_CUST_EMAIL + " TEXT,"
                + COLUMN_CUST_ADDRESS + " TEXT,"
                + COLUMN_CUST_TOTAL_SPENT + " REAL DEFAULT 0)";
        db.execSQL(CREATE_CUSTOMERS_TABLE);

        String CREATE_VOUCHERS_TABLE = "CREATE TABLE " + TABLE_VOUCHERS + "("
                + COLUMN_VOUCH_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_VOUCH_CODE + " TEXT UNIQUE,"
                + COLUMN_VOUCH_AMOUNT + " REAL,"
                + COLUMN_VOUCH_STATUS + " TEXT)";
        db.execSQL(CREATE_VOUCHERS_TABLE);

        String CREATE_ORDER_ITEMS_TABLE = "CREATE TABLE " + TABLE_ORDER_ITEMS + "("
                + COLUMN_ITEM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_ITEM_ORDER_CODE + " TEXT,"
                + COLUMN_ITEM_PRODUCT_NAME + " TEXT,"
                + COLUMN_ITEM_QUANTITY + " INTEGER,"
                + COLUMN_ITEM_PRICE + " REAL)";
        db.execSQL(CREATE_ORDER_ITEMS_TABLE);

        String CREATE_LOGS_TABLE = "CREATE TABLE " + TABLE_LOGS + "("
                + COLUMN_LOG_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_LOG_TIME + " TEXT,"
                + COLUMN_LOG_USER + " TEXT,"
                + COLUMN_LOG_ACTION + " TEXT,"
                + COLUMN_LOG_TARGET + " TEXT)";
        db.execSQL(CREATE_LOGS_TABLE);
        
        insertInitialData(db);
    }

    public void addLog(Context context, String action, String target) {
        UserManager userManager = new UserManager(context);
        String user = userManager.getLoggedInUser();
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_LOG_TIME, new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss", java.util.Locale.getDefault()).format(new java.util.Date()));
        values.put(COLUMN_LOG_USER, user);
        values.put(COLUMN_LOG_ACTION, action);
        values.put(COLUMN_LOG_TARGET, target);
        db.insert(TABLE_LOGS, null, values);
    }

    public void updateVariantStock(long variantId, int newStock) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_VAR_STOCK, newStock);
        db.update(TABLE_VARIANTS, values, COLUMN_VAR_ID + "=?", new String[]{String.valueOf(variantId)});
    }

    public void deleteVariant(long variantId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_VARIANTS, COLUMN_VAR_ID + "=?", new String[]{String.valueOf(variantId)});
    }

    private void insertInitialData(SQLiteDatabase db) {
        // Tài khoản mặc định để tránh bị văng sau khi reset DB
        ContentValues userValues = new ContentValues();
        userValues.put(COLUMN_USERNAME, "admin2026");
        userValues.put(COLUMN_PASSWORD, "123456");
        db.insert(TABLE_USERS, null, userValues);

        long prodId1 = insertProduct(db, "Dior Sauvage", "Dior", "Nam", "");
        insertVariant(db, prodId1, "60ml", 2000000, 10, "3348901250141");
        insertVariant(db, prodId1, "100ml", 3500000, 5, "3348901250158");

        long prodId2 = insertProduct(db, "Chanel No.5", "Chanel", "Nữ", "");
        insertVariant(db, prodId2, "50ml", 4500000, 8, "3145891054504");

        long prodId3 = insertProduct(db, "Gucci Bloom", "Gucci", "Nữ", "");
        insertVariant(db, prodId3, "50ml", 3200000, 15, "8011003833252");

        long prodId4 = insertProduct(db, "Versace Eros", "Versace", "Nam", "");
        insertVariant(db, prodId4, "100ml", 1800000, 20, "8011003809219");

        long prodId5 = insertProduct(db, "Bleu de Chanel", "Chanel", "Nam", "");
        insertVariant(db, prodId5, "50ml", 120000, 10, "3145891074502");

        long prodId6 = insertProduct(db, "Lancôme La Vie Est Belle", "Lancôme", "Nữ", "");
        insertVariant(db, prodId6, "75ml", 95000, 5, "3605532612690");

        // Initial Vouchers
        ContentValues v1 = new ContentValues();
        v1.put(COLUMN_VOUCH_CODE, "LUXE2026");
        v1.put(COLUMN_VOUCH_AMOUNT, 500000);
        v1.put(COLUMN_VOUCH_STATUS, "ACTIVE");
        db.insert(TABLE_VOUCHERS, null, v1);
    }

    public long insertProduct(SQLiteDatabase db, String name, String brand, String gender, String image) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_PROD_NAME, name);
        values.put(COLUMN_PROD_BRAND, brand);
        values.put(COLUMN_PROD_GENDER, gender);
        values.put(COLUMN_PROD_IMAGE, image);
        return db.insert(TABLE_PRODUCTS, null, values);
    }

    public void insertVariant(SQLiteDatabase db, long prodId, String size, double price, int stock, String barcode) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_VAR_PROD_ID, prodId);
        values.put(COLUMN_VAR_SIZE, size);
        values.put(COLUMN_VAR_PRICE, price);
        values.put(COLUMN_VAR_STOCK, stock);
        values.put(COLUMN_VAR_BARCODE, barcode);
        db.insert(TABLE_VARIANTS, null, values);
    }

    public void updateStock(String productName, int quantity) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("UPDATE " + TABLE_VARIANTS + " SET " + COLUMN_VAR_STOCK + " = " + COLUMN_VAR_STOCK + " - " + quantity 
            + " WHERE " + COLUMN_VAR_PROD_ID + " IN (SELECT " + COLUMN_PROD_ID + " FROM " + TABLE_PRODUCTS + " WHERE " + COLUMN_PROD_NAME + " = ?)", 
            new Object[]{productName});
    }

    public void updateCustomer(String name, String phone, double spent) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_CUST_NAME, name);
        values.put(COLUMN_CUST_PHONE, phone);
        values.put(COLUMN_CUST_TOTAL_SPENT, spent);
        
        long id = db.insertWithOnConflict(TABLE_CUSTOMERS, null, values, SQLiteDatabase.CONFLICT_IGNORE);
        if (id == -1) {
            db.execSQL("UPDATE " + TABLE_CUSTOMERS + " SET " + COLUMN_CUST_TOTAL_SPENT + " = " + COLUMN_CUST_TOTAL_SPENT + " + " + spent + " WHERE " + COLUMN_CUST_PHONE + " = ?", new Object[]{phone});
        }
    }

    public double validateVoucher(String code) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_VOUCHERS, new String[]{COLUMN_VOUCH_AMOUNT}, COLUMN_VOUCH_CODE + "=? AND " + COLUMN_VOUCH_STATUS + "='ACTIVE'", new String[]{code}, null, null, null);
        if (cursor.moveToFirst()) {
            double amount = cursor.getDouble(0);
            cursor.close();
            return amount;
        }
        cursor.close();
        return 0;
    }

    public void updateVoucher(long id, String code, double amount, String status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_VOUCH_CODE, code);
        values.put(COLUMN_VOUCH_AMOUNT, amount);
        values.put(COLUMN_VOUCH_STATUS, status);
        db.update(TABLE_VOUCHERS, values, COLUMN_VOUCH_ID + "=?", new String[]{String.valueOf(id)});
    }

    public void deleteVoucher(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_VOUCHERS, COLUMN_VOUCH_ID + "=?", new String[]{String.valueOf(id)});
    }

    public void deleteProduct(long productId) {
        SQLiteDatabase db = this.getWritableDatabase();
        deleteProduct(db, productId);
    }

    public void deleteProduct(SQLiteDatabase db, long productId) {
        db.delete(TABLE_PRODUCTS, COLUMN_PROD_ID + "=?", new String[]{String.valueOf(productId)});
    }

    public void updateOrderStatus(String orderCode, String status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ORDER_STATUS, status);
        db.update(TABLE_ORDERS, values, COLUMN_ORDER_CODE + "=?", new String[]{orderCode});
    }

    public java.util.Map<String, Integer> getTopProducts() {
        java.util.Map<String, Integer> topProducts = new java.util.LinkedHashMap<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + COLUMN_ITEM_PRODUCT_NAME + ", SUM(" + COLUMN_ITEM_QUANTITY + ") as total_qty " +
                "FROM " + TABLE_ORDER_ITEMS + " " +
                "GROUP BY " + COLUMN_ITEM_PRODUCT_NAME + " " +
                "ORDER BY total_qty DESC LIMIT 5";
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            do {
                topProducts.put(cursor.getString(0), cursor.getInt(1));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return topProducts;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 6) {
            try {
                // Thêm các cột mới cho bảng khách hàng nếu chưa có
                db.execSQL("ALTER TABLE " + TABLE_CUSTOMERS + " ADD COLUMN " + COLUMN_CUST_EMAIL + " TEXT");
                db.execSQL("ALTER TABLE " + TABLE_CUSTOMERS + " ADD COLUMN " + COLUMN_CUST_ADDRESS + " TEXT");
            } catch (Exception ignored) {}
        }
        if (oldVersion < 5) {
            try {
                // Kiểm tra xem cột discount đã tồn tại chưa trước khi thêm
                Cursor cursor = db.rawQuery("PRAGMA table_info(" + TABLE_ORDERS + ")", null);
                boolean hasDiscount = false;
                int nameIndex = cursor.getColumnIndex("name");
                if (nameIndex != -1) {
                    while (cursor.moveToNext()) {
                        if (COLUMN_ORDER_DISCOUNT.equals(cursor.getString(nameIndex))) {
                            hasDiscount = true;
                            break;
                        }
                    }
                }
                cursor.close();

                if (!hasDiscount) {
                    db.execSQL("ALTER TABLE " + TABLE_ORDERS + " ADD COLUMN " + COLUMN_ORDER_DISCOUNT + " REAL DEFAULT 0");
                }
            } catch (Exception e) {
                // Nếu lỗi nặng (mất bảng), thực hiện reset lại toàn bộ để cứu app
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRODUCTS);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_VARIANTS);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_ORDERS);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_ORDER_ITEMS);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_CUSTOMERS);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_VOUCHERS);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOGS);
                onCreate(db);
            }
        }
    }

    public boolean addUser(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_PASSWORD, password);
        long result = db.insert(TABLE_USERS, null, values);
        return result != -1;
    }

    public boolean checkUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = COLUMN_USERNAME + " = ? AND " + COLUMN_PASSWORD + " = ?";
        String[] selectionArgs = {username, password};
        Cursor cursor = db.query(TABLE_USERS, null, selection, selectionArgs, null, null, null);
        int count = cursor.getCount();
        cursor.close();
        return count > 0;
    }

    public boolean userExists(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = COLUMN_USERNAME + " = ?";
        String[] selectionArgs = {username};
        Cursor cursor = db.query(TABLE_USERS, null, selection, selectionArgs, null, null, null);
        int count = cursor.getCount();
        cursor.close();
        return count > 0;
    }

    public List<Product> getAllProducts() {
        List<Product> productList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_PRODUCTS, null, null, null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            int idIndex = cursor.getColumnIndex(COLUMN_PROD_ID);
            int nameIndex = cursor.getColumnIndex(COLUMN_PROD_NAME);
            int brandIndex = cursor.getColumnIndex(COLUMN_PROD_BRAND);
            int genderIndex = cursor.getColumnIndex(COLUMN_PROD_GENDER);
            int imageIndex = cursor.getColumnIndex(COLUMN_PROD_IMAGE);

            do {
                if (idIndex != -1 && nameIndex != -1 && brandIndex != -1 && genderIndex != -1 && imageIndex != -1) {
                    long id = cursor.getLong(idIndex);
                    String name = cursor.getString(nameIndex);
                    String brand = cursor.getString(brandIndex);
                    String gender = cursor.getString(genderIndex);
                    String image = cursor.getString(imageIndex);

                    List<Product.Variant> variants = getVariantsForProduct(id);
                    Product product = new Product(name, brand, gender, image, variants);
                    product.setId(id);
                    productList.add(product);
                }
            } while (cursor.moveToNext());
            cursor.close();
        }
        return productList;
    }

    public Product getProductById(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_PRODUCTS, null, COLUMN_PROD_ID + "=?", new String[]{String.valueOf(id)}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int nameIndex = cursor.getColumnIndex(COLUMN_PROD_NAME);
            int brandIndex = cursor.getColumnIndex(COLUMN_PROD_BRAND);
            int genderIndex = cursor.getColumnIndex(COLUMN_PROD_GENDER);
            int imageIndex = cursor.getColumnIndex(COLUMN_PROD_IMAGE);

            if (nameIndex != -1 && brandIndex != -1 && genderIndex != -1 && imageIndex != -1) {
                String name = cursor.getString(nameIndex);
                String brand = cursor.getString(brandIndex);
                String gender = cursor.getString(genderIndex);
                String image = cursor.getString(imageIndex);

                List<Product.Variant> variants = getVariantsForProduct(id);
                Product product = new Product(name, brand, gender, image, variants);
                product.setId(id);
                cursor.close();
                return product;
            }
        }
        if (cursor != null) cursor.close();
        return null;
    }

    public List<Product.Variant> getVariantsForProduct(long productId) {
        List<Product.Variant> variants = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = COLUMN_VAR_PROD_ID + " = ?";
        String[] selectionArgs = {String.valueOf(productId)};
        Cursor cursor = db.query(TABLE_VARIANTS, null, selection, selectionArgs, null, null, null);

        if (cursor != null) {
            int sizeIndex = cursor.getColumnIndex(COLUMN_VAR_SIZE);
            int priceIndex = cursor.getColumnIndex(COLUMN_VAR_PRICE);
            int stockIndex = cursor.getColumnIndex(COLUMN_VAR_STOCK);
            int barcodeIndex = cursor.getColumnIndex(COLUMN_VAR_BARCODE);
            
            while (cursor.moveToNext()) {
                String size = sizeIndex != -1 ? cursor.getString(sizeIndex) : "";
                double price = priceIndex != -1 ? cursor.getDouble(priceIndex) : 0;
                int stock = stockIndex != -1 ? cursor.getInt(stockIndex) : 0;
                String barcode = barcodeIndex != -1 ? cursor.getString(barcodeIndex) : "";
                variants.add(new Product.Variant(size, barcode, price, stock));
            }
            cursor.close();
        }
        return variants;
    }

    public void addOrder(Order order) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ORDER_CODE, order.getOrderId());
        values.put(COLUMN_ORDER_DATE, order.getDate());
        values.put(COLUMN_ORDER_CUSTOMER, order.getCustomer());
        values.put(COLUMN_ORDER_TOTAL, order.getTotal());
        values.put(COLUMN_ORDER_STATUS, order.getStatus());
        values.put(COLUMN_ORDER_DISCOUNT, order.getDiscount());
        db.insert(TABLE_ORDERS, null, values);
    }

    public void addOrderItem(String orderCode, String productName, int quantity, double price) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ITEM_ORDER_CODE, orderCode);
        values.put(COLUMN_ITEM_PRODUCT_NAME, productName);
        values.put(COLUMN_ITEM_QUANTITY, quantity);
        values.put(COLUMN_ITEM_PRICE, price);
        db.insert(TABLE_ORDER_ITEMS, null, values);
    }

    public List<OrderItem> getOrderItems(String orderCode) {
        List<OrderItem> items = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_ORDER_ITEMS, null, COLUMN_ITEM_ORDER_CODE + "=?", new String[]{orderCode}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                int nameIdx = cursor.getColumnIndex(COLUMN_ITEM_PRODUCT_NAME);
                int qtyIdx = cursor.getColumnIndex(COLUMN_ITEM_QUANTITY);
                int priceIdx = cursor.getColumnIndex(COLUMN_ITEM_PRICE);
                
                if (nameIdx != -1 && qtyIdx != -1 && priceIdx != -1) {
                    String name = cursor.getString(nameIdx);
                    int qty = cursor.getInt(qtyIdx);
                    double price = cursor.getDouble(priceIdx);
                    items.add(new OrderItem(name, qty, price));
                }
            } while (cursor.moveToNext());
            cursor.close();
        }
        return items;
    }

    public static class OrderItem {
        public String productName;
        public int quantity;
        public double price;
        public OrderItem(String productName, int quantity, double price) {
            this.productName = productName;
            this.quantity = quantity;
            this.price = price;
        }
    }

    public void restoreStock(String productName, int quantity) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("UPDATE " + TABLE_VARIANTS + " SET " + COLUMN_VAR_STOCK + " = " + COLUMN_VAR_STOCK + " + " + quantity 
            + " WHERE " + COLUMN_VAR_PROD_ID + " IN (SELECT " + COLUMN_PROD_ID + " FROM " + TABLE_PRODUCTS + " WHERE " + COLUMN_PROD_NAME + " = ?)", 
            new Object[]{productName});
    }

    public List<Order> getAllOrders() {
        List<Order> orders = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_ORDERS, null, null, null, null, null, COLUMN_ORDER_ID + " DESC");

        if (cursor.moveToFirst()) {
            do {
                int codeIndex = cursor.getColumnIndex(COLUMN_ORDER_CODE);
                int dateIndex = cursor.getColumnIndex(COLUMN_ORDER_DATE);
                int customerIndex = cursor.getColumnIndex(COLUMN_ORDER_CUSTOMER);
                int totalIndex = cursor.getColumnIndex(COLUMN_ORDER_TOTAL);
                int statusIndex = cursor.getColumnIndex(COLUMN_ORDER_STATUS);
                int discountIndex = cursor.getColumnIndex(COLUMN_ORDER_DISCOUNT);

                if (codeIndex != -1 && dateIndex != -1 && customerIndex != -1 && totalIndex != -1 && statusIndex != -1 && discountIndex != -1) {
                    String code = cursor.getString(codeIndex);
                    String date = cursor.getString(dateIndex);
                    String customer = cursor.getString(customerIndex);
                    double total = cursor.getDouble(totalIndex);
                    String status = cursor.getString(statusIndex);
                    double discount = cursor.getDouble(discountIndex);
                    orders.add(new Order(code, date, customer, "staff", total, status, discount));
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        return orders;
    }

    public String getCustomerPhone(String name) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_CUSTOMERS, new String[]{COLUMN_CUST_PHONE}, COLUMN_CUST_NAME + "=?", new String[]{name}, null, null, null);
        String phone = "N/A";
        if (cursor.moveToFirst()) {
            phone = cursor.getString(0);
        }
        cursor.close();
        return phone;
    }

    public void deleteCustomer(String phone) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_CUSTOMERS, COLUMN_CUST_PHONE + "=?", new String[]{phone});
    }

    public void updateCustomerInfo(String oldPhone, String name, String newPhone, String email, String address) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_CUST_NAME, name);
        values.put(COLUMN_CUST_PHONE, newPhone);
        values.put(COLUMN_CUST_EMAIL, email);
        values.put(COLUMN_CUST_ADDRESS, address);
        db.update(TABLE_CUSTOMERS, values, COLUMN_CUST_PHONE + "=?", new String[]{oldPhone});
    }

    public java.util.Map<String, Double> getMonthlyRevenue() {
        java.util.Map<String, Double> revenueData = new java.util.LinkedHashMap<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT substr(" + COLUMN_ORDER_DATE + ", 4, 7) as month, SUM(" + COLUMN_ORDER_TOTAL + ") " +
                "FROM " + TABLE_ORDERS + " " +
                "WHERE " + COLUMN_ORDER_STATUS + " != 'REFUNDED' " +
                "GROUP BY month " +
                "ORDER BY month ASC";
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            do {
                revenueData.put(cursor.getString(0), cursor.getDouble(1));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return revenueData;
    }
}