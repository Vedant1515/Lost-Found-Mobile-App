package com.example.lostandfound;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class ItemDetailActivity extends AppCompatActivity {

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

        if (itemId == -1) {
            finish();
            return;
        }

        item = db.getItemById(itemId);
        if (item == null) {
            finish();
            return;
        }

        displayItem();

        Button btnRemove = findViewById(R.id.btnRemove);
        btnRemove.setOnClickListener(v -> confirmRemove());
    }

    private void displayItem() {
        TextView tvPostType = findViewById(R.id.tvPostType);
        TextView tvName = findViewById(R.id.tvName);
        TextView tvPhone = findViewById(R.id.tvPhone);
        TextView tvDescription = findViewById(R.id.tvDescription);
        TextView tvDate = findViewById(R.id.tvDate);
        TextView tvLocation = findViewById(R.id.tvLocation);
        TextView tvCategory = findViewById(R.id.tvCategory);
        TextView tvPostedAgo = findViewById(R.id.tvPostedAgo);
        ImageView ivImage = findViewById(R.id.ivImage);

        tvPostType.setText(item.getPostType());
        tvName.setText(item.getName());
        tvPhone.setText(item.getPhone());
        tvDescription.setText(item.getDescription());
        tvDate.setText(item.getDate());
        tvLocation.setText(item.getLocation());
        tvCategory.setText(item.getCategory());
        tvPostedAgo.setText("Posted " + item.getTimeAgo());

        GradientDrawable badge = new GradientDrawable();
        badge.setCornerRadius(16f);
        badge.setColor("Lost".equals(item.getPostType())
                ? Color.parseColor("#E53935")
                : Color.parseColor("#43A047"));
        tvPostType.setBackground(badge);
        tvPostType.setTextColor(Color.WHITE);

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
        if (menuItem.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(menuItem);
    }
}
