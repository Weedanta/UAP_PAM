package com.example.todolist;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

@Entity(tableName = "todo_table")
public class ToDoModel {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @NonNull
    private String title;

    @NonNull
    private String description;

    @NonNull
    private String date;

    public ToDoModel(@NonNull String title, @NonNull String description, @NonNull String date) {
        this.title = title;
        this.description = description;
        this.date = date;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @NonNull
    public String getTitle() {
        return title;
    }

    @NonNull
    public String getDescription() {
        return description;
    }

    @NonNull
    public String getDate() {
        return date;
    }
}