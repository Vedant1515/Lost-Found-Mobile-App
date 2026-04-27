package com.example.lostandfound;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ItemListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ItemAdapter adapter;
    private TextView tvEmpty;
    private DatabaseHelper db;
    private List<LostFoundItem> allItems = new ArrayList<>();
    private String currentSearch = "";
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

        db = new DatabaseHelper(this);
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
                currentSearch = query;
                applyFilters();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                currentSearch = newText;
                applyFilters();
                return true;
            }
        });

        Spinner spinnerFilter = findViewById(R.id.spinnerFilter);
        ArrayAdapter<String> filterAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, FILTER_OPTIONS);
        filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFilter.setAdapter(filterAdapter);
        spinnerFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                currentCategory = FILTER_OPTIONS[pos];
                applyFilters();
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
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
