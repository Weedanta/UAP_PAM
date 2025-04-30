package com.example.todolist;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class ToDoViewModel extends AndroidViewModel {
    private ToDoRepository repository;
    private LiveData<List<ToDoModel>> allTodos;

    public ToDoViewModel(@NonNull Application application) {
        super(application);
        repository = new ToDoRepository(application);
        allTodos = repository.getAllTodos();
    }

    public void insert(ToDoModel todo) {
        repository.insert(todo);
    }

    public void update(ToDoModel todo) {
        repository.update(todo);
    }

    public void delete(ToDoModel todo) {
        repository.delete(todo);
    }

    public void deleteAllTodos() {
        repository.deleteAllTodos();
    }

    public LiveData<List<ToDoModel>> getAllTodos() {
        return allTodos;
    }
}