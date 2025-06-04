package com.example.todolist.data.firebase;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.todolist.data.model.Todo;
import com.example.todolist.data.model.User;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class FirestoreManager {
    private static final String TAG = "FirestoreManager";
    private static final String TODOS_COLLECTION = "todos";
    private static final String USERS_COLLECTION = "users";

    private FirebaseFirestore db;
    private ListenerRegistration todosListener;
    private MutableLiveData<List<Todo>> todosLiveData;
    private String currentUserId;

    public interface FirestoreCallback<T> {
        void onSuccess(T result);
        void onFailure(String error);
    }

    public FirestoreManager() {
        db = FirebaseFirestore.getInstance();
        todosLiveData = new MutableLiveData<>();
    }

    // Todo Operations
    public void addTodo(Todo todo, FirestoreCallback<String> callback) {
        Log.d(TAG, "Adding todo: " + todo.getTitle());
        db.collection(TODOS_COLLECTION)
                .add(todo)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Todo added with ID: " + documentReference.getId());
                    callback.onSuccess(documentReference.getId());
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error adding todo", e);
                    callback.onFailure(e.getMessage());
                });
    }

    public void updateTodo(Todo todo, FirestoreCallback<Void> callback) {
        if (todo.getId() == null) {
            Log.w(TAG, "updateTodo: Todo ID is null");
            callback.onFailure("Todo ID is null");
            return;
        }

        Log.d(TAG, "Updating todo: " + todo.getId());
        db.collection(TODOS_COLLECTION)
                .document(todo.getId())
                .set(todo)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Todo updated successfully: " + todo.getId());
                    callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error updating todo", e);
                    callback.onFailure(e.getMessage());
                });
    }

    public void deleteTodo(String todoId, FirestoreCallback<Void> callback) {
        Log.d(TAG, "Deleting todo: " + todoId);
        db.collection(TODOS_COLLECTION)
                .document(todoId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Todo deleted successfully: " + todoId);
                    callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error deleting todo", e);
                    callback.onFailure(e.getMessage());
                });
    }

    public LiveData<List<Todo>> getUserTodos(String userId) {
        Log.d(TAG, "getUserTodos called for userId: " + userId);

        // Always setup a fresh listener for the user
        setupTodosListener(userId);
        currentUserId = userId;

        return todosLiveData;
    }

    private void setupTodosListener(String userId) {
        // Remove existing listener first
        removeListener();

        Log.d(TAG, "Setting up fresh todos listener for user: " + userId);

        todosListener = db.collection(TODOS_COLLECTION)
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.w(TAG, "Listen failed for user: " + userId, error);
                        return;
                    }

                    List<Todo> todos = new ArrayList<>();
                    if (value != null && !value.isEmpty()) {
                        Log.d(TAG, "Received " + value.size() + " todos from Firestore for user: " + userId);
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            try {
                                Todo todo = doc.toObject(Todo.class);
                                if (todo != null) {
                                    todo.setId(doc.getId());
                                    todos.add(todo);
                                    Log.d(TAG, "Added todo: " + todo.getTitle() + " (ID: " + todo.getId() + ")");
                                } else {
                                    Log.w(TAG, "Failed to convert document to Todo: " + doc.getId());
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error converting document to Todo: " + doc.getId(), e);
                            }
                        }
                    } else {
                        Log.d(TAG, "No todos found for user: " + userId);
                    }

                    Log.d(TAG, "Updating LiveData with " + todos.size() + " todos for user: " + userId);
                    todosLiveData.postValue(todos);
                });
    }

    public void toggleTodoComplete(String todoId, boolean isCompleted, FirestoreCallback<Void> callback) {
        Log.d(TAG, "Toggling todo completion: " + todoId + " to " + isCompleted);
        db.collection(TODOS_COLLECTION)
                .document(todoId)
                .update("completed", isCompleted)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Todo completion status updated: " + todoId);
                    callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error updating todo completion", e);
                    callback.onFailure(e.getMessage());
                });
    }

    // User Operations
    public void getUser(String userId, FirestoreCallback<User> callback) {
        Log.d(TAG, "Getting user: " + userId);
        db.collection(USERS_COLLECTION)
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        Log.d(TAG, "User retrieved successfully: " + userId);
                        callback.onSuccess(user);
                    } else {
                        Log.w(TAG, "User not found: " + userId);
                        callback.onFailure("User not found");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error getting user", e);
                    callback.onFailure(e.getMessage());
                });
    }

    public void updateUser(User user, FirestoreCallback<Void> callback) {
        Log.d(TAG, "Updating user: " + user.getUid());
        db.collection(USERS_COLLECTION)
                .document(user.getUid())
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "User updated successfully: " + user.getUid());
                    callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error updating user", e);
                    callback.onFailure(e.getMessage());
                });
    }

    public void removeListener() {
        if (todosListener != null) {
            Log.d(TAG, "Removing todos listener for user: " + currentUserId);
            todosListener.remove();
            todosListener = null;
        }
    }

    public void forceRefresh() {
        if (currentUserId != null) {
            Log.d(TAG, "Force refreshing data for user: " + currentUserId);
            setupTodosListener(currentUserId);
        } else {
            Log.w(TAG, "Cannot force refresh - no current user ID");
        }
    }
}