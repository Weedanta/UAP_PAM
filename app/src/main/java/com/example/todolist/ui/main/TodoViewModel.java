package com.example.todolist.ui.main;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.todolist.data.firebase.FirestoreManager;
import com.example.todolist.data.model.Todo;
import com.example.todolist.data.repository.TodoRepository;

import java.util.List;

public class TodoViewModel extends ViewModel {
    private static final String TAG = "TodoViewModel";

    private TodoRepository repository;
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private MutableLiveData<String> successMessage = new MutableLiveData<>();
    private LiveData<List<Todo>> userTodos;
    private String currentUserId;

    public TodoViewModel() {
        repository = new TodoRepository();
        Log.d(TAG, "TodoViewModel created");
    }

    public void setUserId(String userId) {
        if (currentUserId == null || !currentUserId.equals(userId)) {
            Log.d(TAG, "Setting user ID: " + userId);
            this.currentUserId = userId;
            userTodos = repository.getUserTodos(userId);
        }
    }

    public LiveData<List<Todo>> getUserTodos() {
        return userTodos;
    }

    public void addTodo(Todo todo) {
        Log.d(TAG, "Adding todo: " + todo.getTitle());
        repository.addTodo(todo, new FirestoreManager.FirestoreCallback<String>() {
            @Override
            public void onSuccess(String result) {
                Log.d(TAG, "Todo added successfully with ID: " + result);
                successMessage.setValue("Todo added successfully");
            }

            @Override
            public void onFailure(String error) {
                Log.e(TAG, "Failed to add todo: " + error);
                errorMessage.setValue("Failed to add todo: " + error);
            }
        });
    }

    public void updateTodo(Todo todo) {
        Log.d(TAG, "Updating todo: " + todo.getTitle());
        repository.updateTodo(todo, new FirestoreManager.FirestoreCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                Log.d(TAG, "Todo updated successfully");
                successMessage.setValue("Todo updated successfully");
            }

            @Override
            public void onFailure(String error) {
                Log.e(TAG, "Failed to update todo: " + error);
                errorMessage.setValue("Failed to update todo: " + error);
            }
        });
    }

    public void deleteTodo(String todoId) {
        Log.d(TAG, "Deleting todo with ID: " + todoId);
        repository.deleteTodo(todoId, new FirestoreManager.FirestoreCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                Log.d(TAG, "Todo deleted successfully");
                successMessage.setValue("Todo deleted successfully");
            }

            @Override
            public void onFailure(String error) {
                Log.e(TAG, "Failed to delete todo: " + error);
                errorMessage.setValue("Failed to delete todo: " + error);
            }
        });
    }

    public void toggleTodoComplete(String todoId, boolean isCompleted) {
        Log.d(TAG, "Toggling todo completion: " + todoId + " to " + isCompleted);
        repository.toggleTodoComplete(todoId, isCompleted, new FirestoreManager.FirestoreCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                Log.d(TAG, "Todo completion toggled successfully");
                // No success message for toggle to avoid spam
            }

            @Override
            public void onFailure(String error) {
                Log.e(TAG, "Failed to toggle todo completion: " + error);
                errorMessage.setValue("Failed to update todo: " + error);
            }
        });
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<String> getSuccessMessage() {
        return successMessage;
    }

    public void clearMessages() {
        errorMessage.setValue(null);
        successMessage.setValue(null);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        Log.d(TAG, "TodoViewModel cleared, removing listeners");
        repository.removeListener();
    }
}