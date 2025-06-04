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
        if (userId == null) {
            Log.w(TAG, "setUserId called with null userId");
            return;
        }

        // Always refresh data when setUserId is called, even if it's the same user
        // This ensures we get fresh data from Firestore
        Log.d(TAG, "Setting user ID: " + userId + " (previous: " + currentUserId + ")");

        // Remove previous listener if exists
        if (repository != null) {
            repository.removeListener();
        }

        this.currentUserId = userId;
        userTodos = repository.getUserTodos(userId);

        Log.d(TAG, "LiveData setup completed for user: " + userId);
    }

    public LiveData<List<Todo>> getUserTodos() {
        if (userTodos == null) {
            Log.w(TAG, "getUserTodos called but userTodos is null");
        }
        return userTodos;
    }

    public void addTodo(Todo todo) {
        Log.d(TAG, "Adding todo: " + todo.getTitle());
        repository.addTodo(todo, new FirestoreManager.FirestoreCallback<String>() {
            @Override
            public void onSuccess(String result) {
                Log.d(TAG, "Todo added successfully with ID: " + result);
                successMessage.postValue("Todo added successfully");
                // Data will automatically refresh via LiveData listener
            }

            @Override
            public void onFailure(String error) {
                Log.e(TAG, "Failed to add todo: " + error);
                errorMessage.postValue("Failed to add todo: " + error);
            }
        });
    }

    public void updateTodo(Todo todo) {
        Log.d(TAG, "Updating todo: " + todo.getTitle());
        repository.updateTodo(todo, new FirestoreManager.FirestoreCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                Log.d(TAG, "Todo updated successfully");
                successMessage.postValue("Todo updated successfully");
                // Data will automatically refresh via LiveData listener
            }

            @Override
            public void onFailure(String error) {
                Log.e(TAG, "Failed to update todo: " + error);
                errorMessage.postValue("Failed to update todo: " + error);
            }
        });
    }

    public void deleteTodo(String todoId) {
        Log.d(TAG, "Deleting todo with ID: " + todoId);
        repository.deleteTodo(todoId, new FirestoreManager.FirestoreCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                Log.d(TAG, "Todo deleted successfully");
                successMessage.postValue("Todo deleted successfully");
                // Data will automatically refresh via LiveData listener
            }

            @Override
            public void onFailure(String error) {
                Log.e(TAG, "Failed to delete todo: " + error);
                errorMessage.postValue("Failed to delete todo: " + error);
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
                // Data will automatically refresh via LiveData listener
            }

            @Override
            public void onFailure(String error) {
                Log.e(TAG, "Failed to toggle todo completion: " + error);
                errorMessage.postValue("Failed to update todo: " + error);
            }
        });
    }

    public void refreshData() {
        if (currentUserId != null) {
            Log.d(TAG, "Manually refreshing data for user: " + currentUserId);
            // Force refresh by re-setting the user ID
            String userId = currentUserId;
            currentUserId = null; // Reset to force refresh
            setUserId(userId);
        } else {
            Log.w(TAG, "Cannot refresh data - no current user ID");
        }
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<String> getSuccessMessage() {
        return successMessage;
    }

    public void clearMessages() {
        Log.d(TAG, "Clearing messages");
        errorMessage.postValue(null);
        successMessage.postValue(null);
    }

    public String getCurrentUserId() {
        return currentUserId;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        Log.d(TAG, "TodoViewModel cleared, removing listeners");
        if (repository != null) {
            repository.removeListener();
        }
    }
}