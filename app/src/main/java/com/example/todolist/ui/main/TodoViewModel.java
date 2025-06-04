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
        Log.d(TAG, "🧠 TodoViewModel created");
        repository = new TodoRepository();
    }

    public void setUserId(String userId) {
        setUserId(userId, false);
    }

    public void setUserId(String userId, boolean forceRefresh) {
        Log.d(TAG, "👤 Setting userId: " + userId + " (forceRefresh: " + forceRefresh + ")");

        if (userId == null || userId.isEmpty()) {
            Log.e(TAG, "❌ Invalid userId provided");
            errorMessage.setValue("Invalid user ID");
            return;
        }

        // Update if userId changed OR force refresh requested
        if (!userId.equals(this.currentUserId) || forceRefresh) {
            Log.d(TAG, "🔄 Updating LiveData for user: " + userId);
            this.currentUserId = userId;

            // Remove previous listener if exists
            if (repository != null) {
                repository.removeListener();
            }

            // Get fresh LiveData for new user
            userTodos = repository.getUserTodos(userId);
            Log.d(TAG, "📊 LiveData setup/refreshed for user: " + userId);
        } else {
            Log.d(TAG, "👤 Same userId, skipping update");
        }
    }

    public LiveData<List<Todo>> getUserTodos() {
        if (userTodos == null) {
            Log.w(TAG, "⚠️ getUserTodos() called but userTodos is null");
            // Return empty LiveData to prevent crashes
            return new MutableLiveData<>();
        }
        return userTodos;
    }

    public void addTodo(Todo todo) {
        Log.d(TAG, "➕ Adding todo: " + todo.getTitle());

        if (currentUserId == null) {
            Log.e(TAG, "❌ Cannot add todo: no user set");
            errorMessage.setValue("No user authenticated");
            return;
        }

        repository.addTodo(todo, new FirestoreManager.FirestoreCallback<String>() {
            @Override
            public void onSuccess(String result) {
                Log.d(TAG, "✅ Todo added successfully: " + result);
                successMessage.setValue("Todo added successfully");

                // Clear success message after showing
                clearMessages();
            }

            @Override
            public void onFailure(String error) {
                Log.e(TAG, "❌ Failed to add todo: " + error);
                errorMessage.setValue("Failed to add todo: " + error);
            }
        });
    }

    public void updateTodo(Todo todo) {
        Log.d(TAG, "✏️ Updating todo: " + todo.getTitle());

        repository.updateTodo(todo, new FirestoreManager.FirestoreCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                Log.d(TAG, "✅ Todo updated successfully");
                successMessage.setValue("Todo updated successfully");
                clearMessages();
            }

            @Override
            public void onFailure(String error) {
                Log.e(TAG, "❌ Failed to update todo: " + error);
                errorMessage.setValue("Failed to update todo: " + error);
            }
        });
    }

    public void deleteTodo(String todoId) {
        Log.d(TAG, "🗑️ Deleting todo: " + todoId);

        repository.deleteTodo(todoId, new FirestoreManager.FirestoreCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                Log.d(TAG, "✅ Todo deleted successfully");
                successMessage.setValue("Todo deleted successfully");
                clearMessages();
            }

            @Override
            public void onFailure(String error) {
                Log.e(TAG, "❌ Failed to delete todo: " + error);
                errorMessage.setValue("Failed to delete todo: " + error);
            }
        });
    }

    public void toggleTodoComplete(String todoId, boolean isCompleted) {
        Log.d(TAG, "✅ Toggling todo complete: " + todoId + " -> " + isCompleted);

        repository.toggleTodoComplete(todoId, isCompleted, new FirestoreManager.FirestoreCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                Log.d(TAG, "✅ Todo completion toggled successfully");
                // Don't show success message for toggle operations
            }

            @Override
            public void onFailure(String error) {
                Log.e(TAG, "❌ Failed to toggle todo completion: " + error);
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

    // Method untuk refresh data secara manual
    public void refreshData() {
        Log.d(TAG, "🔄 FORCE refreshing data for user: " + currentUserId);
        if (currentUserId != null) {
            setUserId(currentUserId, true); // Force refresh
        }
    }

    // Clear messages after some time
    private void clearMessages() {
        // Clear messages after 1 second to prevent them from showing again
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            if (errorMessage.getValue() != null && !errorMessage.getValue().isEmpty()) {
                errorMessage.setValue("");
            }
            if (successMessage.getValue() != null && !successMessage.getValue().isEmpty()) {
                successMessage.setValue("");
            }
        }, 1000);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        Log.d(TAG, "💀 TodoViewModel cleared - removing listeners");
        if (repository != null) {
            repository.removeListener();
        }
    }
}