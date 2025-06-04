package com.example.todolist.data.repository;

import androidx.lifecycle.LiveData;

import com.example.todolist.data.firebase.FirestoreManager;
import com.example.todolist.data.model.Todo;

import java.util.List;

public class TodoRepository {
    private FirestoreManager firestoreManager;

    public TodoRepository() {
        firestoreManager = new FirestoreManager();
    }

    public void addTodo(Todo todo, FirestoreManager.FirestoreCallback<String> callback) {
        firestoreManager.addTodo(todo, callback);
    }

    public void updateTodo(Todo todo, FirestoreManager.FirestoreCallback<Void> callback) {
        firestoreManager.updateTodo(todo, callback);
    }

    public void deleteTodo(String todoId, FirestoreManager.FirestoreCallback<Void> callback) {
        firestoreManager.deleteTodo(todoId, callback);
    }

    public LiveData<List<Todo>> getUserTodos(String userId) {
        return firestoreManager.getUserTodos(userId);
    }

    public void toggleTodoComplete(String todoId, boolean isCompleted, FirestoreManager.FirestoreCallback<Void> callback) {
        firestoreManager.toggleTodoComplete(todoId, isCompleted, callback);
    }

    public void removeListener() {
        firestoreManager.removeListener();
    }
}