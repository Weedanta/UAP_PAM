package com.example.todolist.data.model;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Todo {
    @DocumentId
    private String id;
    private String title;
    private String description;
    private String date;
    private String userId;
    private boolean isCompleted;
    private String priority; // HIGH, MEDIUM, LOW
    private String category;
    @ServerTimestamp
    private Date createdAt;
    @ServerTimestamp
    private Date updatedAt;

    public Todo() {
        // Required empty constructor for Firestore
        this.isCompleted = false;
        this.priority = "MEDIUM";
        this.category = "General";
    }

    public Todo(String title, String description, String date, String userId) {
        this.title = title != null ? title : "";
        this.description = description != null ? description : "";
        this.date = date != null ? date : "";
        this.userId = userId != null ? userId : "";
        this.isCompleted = false;
        this.priority = "MEDIUM";
        this.category = "General";
    }

    // Method untuk debugging - convert ke Map
    public Map<String, Object> toMap() {
        Map<String, Object> result = new HashMap<>();
        result.put("title", title);
        result.put("description", description);
        result.put("date", date);
        result.put("userId", userId);
        result.put("isCompleted", isCompleted);
        result.put("priority", priority);
        result.put("category", category);
        return result;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title != null ? title : "";
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description != null ? description : "";
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDate() {
        return date != null ? date : "";
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getUserId() {
        return userId != null ? userId : "";
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public String getPriority() {
        return priority != null ? priority : "MEDIUM";
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getCategory() {
        return category != null ? category : "General";
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
}