package com.example.lostandfound;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LostFoundItem {
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
    private double latitude;
    private double longitude;

    public LostFoundItem() {}

    public LostFoundItem(int id, String postType, String name, String phone,
                         String description, String date, String location,
                         String category, String imageUri, String createdAt,
                         double latitude, double longitude) {
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
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getPostType() { return postType; }
    public void setPostType(String postType) { this.postType = postType; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getImageUri() { return imageUri; }
    public void setImageUri(String imageUri) { this.imageUri = imageUri; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

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
