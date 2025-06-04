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
            callback.onFailure("Todo ID is null");
            return;
        }

        db.collection(TODOS_COLLECTION)
                .document(todo.getId())
                .set(todo)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Todo updated successfully");
                    callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error updating todo", e);
                    callback.onFailure(e.getMessage());
                });
    }

    public void deleteTodo(String todoId, FirestoreCallback<Void> callback) {
        db.collection(TODOS_COLLECTION)
                .document(todoId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Todo deleted successfully");
                    callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error deleting todo", e);
                    callback.onFailure(e.getMessage());
                });
    }

    public LiveData<List<Todo>> getUserTodos(String userId) {
        // Only setup listener if user changed or not set up yet
        if (currentUserId == null || !currentUserId.equals(userId)) {
            setupTodosListener(userId);
            currentUserId = userId;
        }
        return todosLiveData;
    }

    private void setupTodosListener(String userId) {
        // Remove existing listener
        if (todosListener != null) {
            todosListener.remove();
        }

        Log.d(TAG, "Setting up todos listener for user: " + userId);

        todosListener = db.collection(TODOS_COLLECTION)
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.w(TAG, "Listen failed.", error);
                        return;
                    }

                    List<Todo> todos = new ArrayList<>();
                    if (value != null && !value.isEmpty()) {
                        Log.d(TAG, "Received " + value.size() + " todos from Firestore");
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            Todo todo = doc.toObject(Todo.class);
                            if (todo != null) {
                                todo.setId(doc.getId());
                                todos.add(todo);
                                Log.d(TAG, "Added todo: " + todo.getTitle());
                            }
                        }
                    } else {
                        Log.d(TAG, "No todos found for user");
                    }

                    Log.d(TAG, "Updating LiveData with " + todos.size() + " todos");
                    todosLiveData.setValue(todos);
                });
    }

    public void toggleTodoComplete(String todoId, boolean isCompleted, FirestoreCallback<Void> callback) {
        db.collection(TODOS_COLLECTION)
                .document(todoId)
                .update("completed", isCompleted)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Todo completion status updated");
                    callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error updating todo completion", e);
                    callback.onFailure(e.getMessage());
                });
    }

    // User Operations
    public void getUser(String userId, FirestoreCallback<User> callback) {
        db.collection(USERS_COLLECTION)
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        callback.onSuccess(user);
                    } else {
                        callback.onFailure("User not found");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error getting user", e);
                    callback.onFailure(e.getMessage());
                });
    }

    public void updateUser(User user, FirestoreCallback<Void> callback) {
        db.collection(USERS_COLLECTION)
                .document(user.getUid())
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "User updated successfully");
                    callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error updating user", e);
                    callback.onFailure(e.getMessage());
                });
    }

    public void removeListener() {
        if (todosListener != null) {
            todosListener.remove();
            todosListener = null;
            Log.d(TAG, "Todos listener removed");
        }
        currentUserId = null;
    }
}