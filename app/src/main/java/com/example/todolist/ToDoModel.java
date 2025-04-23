package com.example.todolist;

public class ToDoModel {
    private String title;
    private String description;
    private String date;

    public ToDoModel(String title, String description, String date) {
        this.title = title;
        this.description = description;
        this.date = date;
    }


    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
}
