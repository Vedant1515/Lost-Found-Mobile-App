package com.example.lostandfound;

// ─────────────────────────────────────────────────────────────────────────────
//  MERGED SUBMISSION FILE — Lost & Found App (SIT708 Task 7.1)
//  Author  : Vedant Pandya
//  Language: Java (Android)
//  Contains: LostFoundItem · DatabaseHelper · ItemAdapter
//            MainActivity · CreateAdvertActivity
//            ItemListActivity · ItemDetailActivity
// ─────────────────────────────────────────────────────────────────────────────

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.textfield.TextInputEditText;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

// ═════════════════════════════════════════════════════════════════════════════
// FILE 1 — LostFoundItem.java
// Model / POJO class representing one database row.
// ═════════════════════════════════════════════════════════════════════════════

class LostFoundItem {

    private int id;
    private String postType;
    private String name;
    private String phone;
    private String description;
    private String date;
    private String location;
    private String category;
    private String imageUri;
    private String createdAt;

    public LostFoundItem() {}

    public LostFoundItem(int id, String postType, String name, String phone,
                         String description, String date, String location,
                         String category, String imageUri, String createdAt) {
        this.id = id;
        this.postType = postType;
        this.name = name;
        this.phone = phone;
        this.description = description;
        this.date = date;
        this.location = location;
        this.category = category;
        this.imageUri = imageUri;
        this.createdAt = createdAt;
    }

    // Getters
    public int getId() { return id; }
    public String getPostType() { return postType; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public String getDescription() { return description; }
    public String getDate() { return date; }
    public String getLocation() { return location; }
    public String getCategory() { return category; }
    public String getImageUri() { return imageUri; }
    public String getCreatedAt() { return createdAt; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setPostType(String postType) { this.postType = postType; }
    public void setName(String name) { this.name = name; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setDescription(String description) { this.description = description; }
    public void setDate(String date) { this.date = date; }
    public void setLocation(String location) { this.location = location; }
    public void setCategory(String category) { this.category = category; }
    public void setImageUri(String imageUri) { this.imageUri = imageUri; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    // Returns a human-readable relative time string from the stored ISO timestamp.
    public String getTimeAgo() {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            Date past = sdf.parse(createdAt);
            if (past == null) return "";
            long diff = new Date().getTime() - past.getTime();
            long seconds = diff / 1000;
            long minutes = seconds / 60;
            long hours   = minutes / 60;
            long days    = hours   / 24;
            if (days    > 0) return days    + (days    == 1 ? " day ago"    : " days ago");
            if (hours   > 0) return hours   + (hours   == 1 ? " hour ago"   : " hours ago");
            if (minutes > 0) return minutes + (minutes == 1 ? " minute ago" : " minutes ago");
            return "Just now";
        } catch (Exception e) {
            return "";
        }
    }
}

// ═════════════════════════════════════════════════════════════════════════════
// FILE 2 — DatabaseHelper.java
// SQLiteOpenHelper with full CRUD: insert, getAll, getById,
// getByCategory, search, and delete.
// ═════════════════════════════════════════════════════════════════════════════

class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME   = "lostandfound.db";
    private static final int    DB_VERSION = 1;
    private static final String TABLE     = "lost_found_items";

    private static final String COL_ID          = "id";
    private static final String COL_POST_TYPE   = "post_type";
    private static final String COL_NAME        = "name";
    private static final String COL_PHONE       = "phone";
    private static final String COL_DESCRIPTION = "description";
    private static final String COL_DATE        = "date";
    private static final String COL_LOCATION    = "location";
    private static final String COL_CATEGORY    = "category";
    private static final String COL_IMAGE_URI   = "image_uri";
    private static final String COL_CREATED_AT  = "created_at";

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE + " (" +
                COL_ID          + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_POST_TYPE   + " TEXT, " +
                COL_NAME        + " TEXT, " +
                COL_PHONE       + " TEXT, " +
                COL_DESCRIPTION + " TEXT, " +
                COL_DATE        + " TEXT, " +
                COL_LOCATION    + " TEXT, " +
                COL_CATEGORY    + " TEXT, " +
                COL_IMAGE_URI   + " TEXT, " +
                COL_CREATED_AT  + " TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE);
        onCreate(db);
    }

    // Insert a new item; created_at is auto-set to the current ISO timestamp.
    public long insertItem(LostFoundItem item) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put(COL_POST_TYPE,   item.getPostType());
        v.put(COL_NAME,        item.getName());
        v.put(COL_PHONE,       item.getPhone());
        v.put(COL_DESCRIPTION, item.getDescription());
        v.put(COL_DATE,        item.getDate());
        v.put(COL_LOCATION,    item.getLocation());
        v.put(COL_CATEGORY,    item.getCategory());
        v.put(COL_IMAGE_URI,   item.getImageUri());
        v.put(COL_CREATED_AT,
                new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                        .format(new Date()));
        long id = db.insert(TABLE, null, v);
        db.close();
        return id;
    }

    // Returns all items ordered by newest first.
    public List<LostFoundItem> getAllItems() {
        List<LostFoundItem> items = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT * FROM " + TABLE + " ORDER BY " + COL_CREATED_AT + " DESC", null);
        if (c.moveToFirst()) { do { items.add(fromCursor(c)); } while (c.moveToNext()); }
        c.close();
        db.close();
        return items;
    }

    // Fetches a single item by its primary key.
    public LostFoundItem getItemById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.query(TABLE, null, COL_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null);
        LostFoundItem item = null;
        if (c.moveToFirst()) item = fromCursor(c);
        c.close();
        db.close();
        return item;
    }

    // Returns all items matching a specific category.
    public List<LostFoundItem> getItemsByCategory(String category) {
        List<LostFoundItem> items = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.query(TABLE, null, COL_CATEGORY + "=?",
                new String[]{category}, null, null, COL_CREATED_AT + " DESC");
        if (c.moveToFirst()) { do { items.add(fromCursor(c)); } while (c.moveToNext()); }
        c.close();
        db.close();
        return items;
    }

    // Searches name and description columns using SQL LIKE.
    public List<LostFoundItem> searchItems(String query) {
        List<LostFoundItem> items = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String like = "%" + query + "%";
        Cursor c = db.query(TABLE, null,
                COL_NAME + " LIKE ? OR " + COL_DESCRIPTION + " LIKE ?",
                new String[]{like, like}, null, null, COL_CREATED_AT + " DESC");
        if (c.moveToFirst()) { do { items.add(fromCursor(c)); } while (c.moveToNext()); }
        c.close();
        db.close();
        return items;
    }

    // Deletes a row by ID; returns the number of rows affected.
    public int deleteItem(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete(TABLE, COL_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
        return rows;
    }

    private LostFoundItem fromCursor(Cursor c) {
        return new LostFoundItem(
                c.getInt(c.getColumnIndexOrThrow(COL_ID)),
                c.getString(c.getColumnIndexOrThrow(COL_POST_TYPE)),
                c.getString(c.getColumnIndexOrThrow(COL_NAME)),
                c.getString(c.getColumnIndexOrThrow(COL_PHONE)),
                c.getString(c.getColumnIndexOrThrow(COL_DESCRIPTION)),
                c.getString(c.getColumnIndexOrThrow(COL_DATE)),
                c.getString(c.getColumnIndexOrThrow(COL_LOCATION)),
                c.getString(c.getColumnIndexOrThrow(COL_CATEGORY)),
                c.getString(c.getColumnIndexOrThrow(COL_IMAGE_URI)),
                c.getString(c.getColumnIndexOrThrow(COL_CREATED_AT))
        );
    }
}

// ═════════════════════════════════════════════════════════════════════════════
// FILE 3 — ItemAdapter.java
// RecyclerView adapter. Colours the Lost/Found badge dynamically and
// exposes an OnItemClickListener interface.
// ═════════════════════════════════════════════════════════════════════════════

class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ViewHolder> {

    private List<LostFoundItem> items = new ArrayList<>();
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(LostFoundItem item);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    // Replaces the current list and refreshes the RecyclerView.
    public void updateList(List<LostFoundItem> newItems) {
        this.items = new ArrayList<>(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LostFoundItem item = items.get(position);

        holder.tvName.setText(item.getName());
        holder.tvCategory.setText(item.getCategory());
        holder.tvLocation.setText(item.getLocation());
        holder.tvTimeAgo.setText(item.getTimeAgo());
        holder.tvType.setText(item.getPostType());

        // Colour the badge red for Lost, green for Found.
        GradientDrawable badge = new GradientDrawable();
        badge.setCornerRadius(16f);
        badge.setColor("Lost".equals(item.getPostType())
                ? Color.parseColor("#E53935")
                : Color.parseColor("#43A047"));
        holder.tvType.setBackground(badge);
        holder.tvType.setTextColor(Color.WHITE);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(item);
        });
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvType, tvName, tvCategory, tvLocation, tvTimeAgo;

        ViewHolder(View itemView) {
            super(itemView);
            tvType     = itemView.findViewById(R.id.tvType);
            tvName     = itemView.findViewById(R.id.tvName);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvTimeAgo  = itemView.findViewById(R.id.tvTimeAgo);
        }
    }
}

// ═════════════════════════════════════════════════════════════════════════════
// FILE 4 — MainActivity.java
// Home screen. Two buttons navigate to CreateAdvertActivity
// and ItemListActivity respectively.
// ═════════════════════════════════════════════════════════════════════════════

public class LostandFoundApp extends AppCompatActivity {

    // NOTE FOR SUBMISSION:
    // In the actual project this class is named MainActivity and lives in
    // MainActivity.java. It is declared public here because Java requires
    // exactly one public class per file, and that class must match the filename.
    // All other classes in this file are package-private (no public modifier),
    // which is valid Java. The real project compiles from the individual files.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Lost & Found");
        }

        findViewById(R.id.btnCreateAdvert).setOnClickListener(v ->
                startActivity(new Intent(this, CreateAdvertActivity.class)));

        findViewById(R.id.btnShowItems).setOnClickListener(v ->
                startActivity(new Intent(this, ItemListActivity.class)));
    }
}

// ═════════════════════════════════════════════════════════════════════════════
// FILE 5 — CreateAdvertActivity.java
// Form screen: RadioGroup (Lost/Found), five TextInputEditText fields,
// category Spinner, image picker via ActivityResultLauncher, DatePickerDialog,
// and validation before inserting into SQLite.
// ═════════════════════════════════════════════════════════════════════════════

class CreateAdvertActivity extends AppCompatActivity {

    private TextInputEditText etName, etPhone, etDescription, etDate, etLocation;
    private RadioGroup rgPostType;
    private Spinner spinnerCategory;
    private ImageView ivImagePreview;
    private String selectedImageUri = "";
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    private static final String[] CATEGORIES =
            {"Electronics", "Pets", "Wallets", "Keys", "Clothing", "Other"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_advert);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Create Advert");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        etName        = findViewById(R.id.etName);
        etPhone       = findViewById(R.id.etPhone);
        etDescription = findViewById(R.id.etDescription);
        etDate        = findViewById(R.id.etDate);
        etLocation    = findViewById(R.id.etLocation);
        rgPostType    = findViewById(R.id.rgPostType);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        ivImagePreview  = findViewById(R.id.ivImagePreview);

        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, CATEGORIES);
        catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(catAdapter);

        // Modern image picker — replaces deprecated startActivityForResult.
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK
                            && result.getData() != null) {
                        Uri uri = result.getData().getData();
                        if (uri != null) {
                            try {
                                // Persist permission so URI survives app restarts.
                                getContentResolver().takePersistableUriPermission(
                                        uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            } catch (SecurityException ignored) {}
                            selectedImageUri = uri.toString();
                            ivImagePreview.setImageURI(uri);
                            ivImagePreview.setVisibility(View.VISIBLE);
                        }
                    }
                });

        // Date field opens a DatePickerDialog instead of the keyboard.
        etDate.setOnClickListener(v -> showDatePicker());

        findViewById(R.id.btnUploadImage).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                    | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            imagePickerLauncher.launch(intent);
        });

        findViewById(R.id.btnSave).setOnClickListener(v -> saveAdvert());
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, day) ->
                etDate.setText(String.format("%04d-%02d-%02d", year, month + 1, day)),
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void saveAdvert() {
        int selectedId = rgPostType.getCheckedRadioButtonId();
        if (selectedId == -1) {
            Toast.makeText(this, "Please select Lost or Found", Toast.LENGTH_SHORT).show();
            return;
        }
        String postType     = (selectedId == R.id.rbLost) ? "Lost" : "Found";
        String name         = etName.getText()        != null ? etName.getText().toString().trim()        : "";
        String phone        = etPhone.getText()       != null ? etPhone.getText().toString().trim()       : "";
        String description  = etDescription.getText() != null ? etDescription.getText().toString().trim() : "";
        String date         = etDate.getText()        != null ? etDate.getText().toString().trim()        : "";
        String location     = etLocation.getText()    != null ? etLocation.getText().toString().trim()    : "";
        String category     = spinnerCategory.getSelectedItem().toString();

        if (name.isEmpty() || phone.isEmpty() || description.isEmpty()
                || date.isEmpty() || location.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        LostFoundItem item = new LostFoundItem();
        item.setPostType(postType);
        item.setName(name);
        item.setPhone(phone);
        item.setDescription(description);
        item.setDate(date);
        item.setLocation(location);
        item.setCategory(category);
        item.setImageUri(selectedImageUri);

        DatabaseHelper db = new DatabaseHelper(this);
        long id = db.insertItem(item);

        if (id != -1) {
            Toast.makeText(this, "Advert saved!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Failed to save advert", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { finish(); return true; }
        return super.onOptionsItemSelected(item);
    }
}

// ═════════════════════════════════════════════════════════════════════════════
// FILE 6 — ItemListActivity.java
// Displays all listings in a RecyclerView. Loads from DB in onResume so
// the list refreshes after returning from CreateAdvertActivity.
// Search and category filter are combined in-memory via applyFilters().
// ═════════════════════════════════════════════════════════════════════════════

class ItemListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ItemAdapter adapter;
    private TextView tvEmpty;
    private DatabaseHelper db;
    private List<LostFoundItem> allItems = new ArrayList<>();
    private String currentSearch   = "";
    private String currentCategory = "All";

    private static final String[] FILTER_OPTIONS =
            {"All", "Electronics", "Pets", "Wallets", "Keys", "Clothing", "Other"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_list);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Lost & Found Items");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        db      = new DatabaseHelper(this);
        tvEmpty = findViewById(R.id.tvEmpty);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(
                new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        adapter = new ItemAdapter();
        adapter.setOnItemClickListener(item -> {
            Intent intent = new Intent(this, ItemDetailActivity.class);
            intent.putExtra("ITEM_ID", item.getId());
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        SearchView searchView = findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                currentSearch = query; applyFilters(); return true;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                currentSearch = newText; applyFilters(); return true;
            }
        });

        Spinner spinnerFilter = findViewById(R.id.spinnerFilter);
        ArrayAdapter<String> filterAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, FILTER_OPTIONS);
        filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFilter.setAdapter(filterAdapter);
        spinnerFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> p, View v, int pos, long id) {
                currentCategory = FILTER_OPTIONS[pos]; applyFilters();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        allItems = db.getAllItems();
        applyFilters();
    }

    // Filters allItems in memory by both search query and category.
    private void applyFilters() {
        String query = currentSearch.toLowerCase().trim();
        List<LostFoundItem> result = new ArrayList<>();

        for (LostFoundItem item : allItems) {
            boolean matchesSearch = query.isEmpty()
                    || item.getName().toLowerCase().contains(query)
                    || item.getDescription().toLowerCase().contains(query);
            boolean matchesCategory = currentCategory.equals("All")
                    || item.getCategory().equals(currentCategory);
            if (matchesSearch && matchesCategory) result.add(item);
        }

        adapter.updateList(result);
        boolean empty = result.isEmpty();
        tvEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { finish(); return true; }
        return super.onOptionsItemSelected(item);
    }
}

// ═════════════════════════════════════════════════════════════════════════════
// FILE 7 — ItemDetailActivity.java
// Fetches one item by ID from the DB and displays all its fields.
// Image loading is wrapped in try/catch (URI permission may be revoked).
// REMOVE button shows an AlertDialog before deleting.
// ═════════════════════════════════════════════════════════════════════════════

class ItemDetailActivity extends AppCompatActivity {

    private DatabaseHelper db;
    private LostFoundItem item;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_detail);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Item Details");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        db = new DatabaseHelper(this);
        int itemId = getIntent().getIntExtra("ITEM_ID", -1);

        if (itemId == -1) { finish(); return; }
        item = db.getItemById(itemId);
        if (item == null) { finish(); return; }

        displayItem();
        findViewById(R.id.btnRemove).setOnClickListener(v -> confirmRemove());
    }

    private void displayItem() {
        TextView tvPostType  = findViewById(R.id.tvPostType);
        TextView tvName      = findViewById(R.id.tvName);
        TextView tvPhone     = findViewById(R.id.tvPhone);
        TextView tvDesc      = findViewById(R.id.tvDescription);
        TextView tvDate      = findViewById(R.id.tvDate);
        TextView tvLocation  = findViewById(R.id.tvLocation);
        TextView tvCategory  = findViewById(R.id.tvCategory);
        TextView tvPostedAgo = findViewById(R.id.tvPostedAgo);
        ImageView ivImage    = findViewById(R.id.ivImage);

        tvPostType.setText(item.getPostType());
        tvName.setText(item.getName());
        tvPhone.setText(item.getPhone());
        tvDesc.setText(item.getDescription());
        tvDate.setText(item.getDate());
        tvLocation.setText(item.getLocation());
        tvCategory.setText(item.getCategory());
        tvPostedAgo.setText("Posted " + item.getTimeAgo());

        // Colour the post-type badge.
        GradientDrawable badge = new GradientDrawable();
        badge.setCornerRadius(16f);
        badge.setColor("Lost".equals(item.getPostType())
                ? Color.parseColor("#E53935")
                : Color.parseColor("#43A047"));
        tvPostType.setBackground(badge);
        tvPostType.setTextColor(Color.WHITE);

        // Load image if a URI was stored; hide the ImageView if not.
        if (item.getImageUri() != null && !item.getImageUri().isEmpty()) {
            try {
                ivImage.setImageURI(Uri.parse(item.getImageUri()));
                ivImage.setVisibility(View.VISIBLE);
            } catch (Exception e) {
                ivImage.setVisibility(View.GONE);
            }
        } else {
            ivImage.setVisibility(View.GONE);
        }
    }

    private void confirmRemove() {
        new AlertDialog.Builder(this)
                .setTitle("Remove Item")
                .setMessage("Are you sure you want to remove this item?")
                .setPositiveButton("Remove", (dialog, which) -> {
                    db.deleteItem(item.getId());
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) { finish(); return true; }
        return super.onOptionsItemSelected(menuItem);
    }
}
