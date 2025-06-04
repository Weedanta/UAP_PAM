package com.example.todolist.ui.main;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.todolist.data.firebase.FirestoreManager;
import com.example.todolist.data.model.Todo;
import com.example.todolist.data.repository.TodoRepository;

import java.util.List;

public class TodoViewModel extends ViewModel {
    private TodoRepository repository;
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private MutableLiveData<String> successMessage = new MutableLiveData<>();
    private LiveData<List<Todo>> userTodos;
    private String currentUserId;

    public TodoViewModel() {
        repository = new TodoRepository();
    }

    public void setUserId(String userId) {
        this.currentUserId = userId;
        userTodos = repository.getUserTodos(userId);
    }

    public LiveData<List<Todo>> getUserTodos() {
        return userTodos;
    }

    public void addTodo(Todo todo) {
        repository.addTodo(todo, new FirestoreManager.FirestoreCallback<String>() {
            @Override
            public void onSuccess(String result) {
                successMessage.setValue("Todo added successfully");
            }

            @Override
            public void onFailure(String error) {
                errorMessage.setValue("Failed to add todo: " + error);
            }
        });
    }

    public void updateTodo(Todo todo) {
        repository.updateTodo(todo, new FirestoreManager.FirestoreCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                successMessage.setValue("Todo updated successfully");
            }

            @Override
            public void onFailure(String error) {
                errorMessage.setValue("Failed to update todo: " + error);
            }
        });
    }

    public void deleteTodo(String todoId) {
        repository.deleteTodo(todoId, new FirestoreManager.FirestoreCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                successMessage.setValue("Todo deleted successfully");
            }

            @Override
            public void onFailure(String error) {
                errorMessage.setValue("Failed to delete todo: " + error);
            }
        });
    }

    public void toggleTodoComplete(String todoId, boolean isCompleted) {
        repository.toggleTodoComplete(todoId, isCompleted, new FirestoreManager.FirestoreCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                // No message needed for toggle
            }

            @Override
            public void onFailure(String error) {
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

    @Override
    protected void onCleared() {
        super.onCleared();
        repository.removeListener();
    }
}