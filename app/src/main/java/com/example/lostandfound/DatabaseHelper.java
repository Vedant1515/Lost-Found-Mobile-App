package com.example.lostandfound;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "lostandfound.db";
    private static final int DB_VERSION = 1;
    private static final String TABLE = "lost_found_items";

    private static final String COL_ID = "id";
    private static final String COL_POST_TYPE = "post_type";
    private static final String COL_NAME = "name";
    private static final String COL_PHONE = "phone";
    private static final String COL_DESCRIPTION = "description";
    private static final String COL_DATE = "date";
    private static final String COL_LOCATION = "location";
    private static final String COL_CATEGORY = "category";
    private static final String COL_IMAGE_URI = "image_uri";
    private static final String COL_CREATED_AT = "created_at";

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_POST_TYPE + " TEXT, " +
                COL_NAME + " TEXT, " +
                COL_PHONE + " TEXT, " +
                COL_DESCRIPTION + " TEXT, " +
                COL_DATE + " TEXT, " +
                COL_LOCATION + " TEXT, " +
                COL_CATEGORY + " TEXT, " +
                COL_IMAGE_URI + " TEXT, " +
                COL_CREATED_AT + " TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE);
        onCreate(db);
    }

    public long insertItem(LostFoundItem item) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put(COL_POST_TYPE, item.getPostType());
        v.put(COL_NAME, item.getName());
        v.put(COL_PHONE, item.getPhone());
        v.put(COL_DESCRIPTION, item.getDescription());
        v.put(COL_DATE, item.getDate());
        v.put(COL_LOCATION, item.getLocation());
        v.put(COL_CATEGORY, item.getCategory());
        v.put(COL_IMAGE_URI, item.getImageUri());
        v.put(COL_CREATED_AT, new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss",
                Locale.getDefault()).format(new Date()));
        long id = db.insert(TABLE, null, v);
        db.close();
        return id;
    }

    public List<LostFoundItem> getAllItems() {
        List<LostFoundItem> items = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + TABLE + " ORDER BY " + COL_CREATED_AT + " DESC", null);
        if (c.moveToFirst()) {
            do { items.add(fromCursor(c)); } while (c.moveToNext());
        }
        c.close();
        db.close();
        return items;
    }

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

    public List<LostFoundItem> getItemsByCategory(String category) {
        List<LostFoundItem> items = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.query(TABLE, null, COL_CATEGORY + "=?",
                new String[]{category}, null, null, COL_CREATED_AT + " DESC");
        if (c.moveToFirst()) {
            do { items.add(fromCursor(c)); } while (c.moveToNext());
        }
        c.close();
        db.close();
        return items;
    }

    public List<LostFoundItem> searchItems(String query) {
        List<LostFoundItem> items = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String like = "%" + query + "%";
        Cursor c = db.query(TABLE, null,
                COL_NAME + " LIKE ? OR " + COL_DESCRIPTION + " LIKE ?",
                new String[]{like, like}, null, null, COL_CREATED_AT + " DESC");
        if (c.moveToFirst()) {
            do { items.add(fromCursor(c)); } while (c.moveToNext());
        }
        c.close();
        db.close();
        return items;
    }

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
