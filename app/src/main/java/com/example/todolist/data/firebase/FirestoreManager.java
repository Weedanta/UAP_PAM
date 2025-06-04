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

    public interface FirestoreCallback<T> {
        void onSuccess(T result);
        void onFailure(String error);
    }

    public FirestoreManager() {
        db = FirebaseFirestore.getInstance();
        Log.d(TAG, "📊 FirestoreManager initialized");
    }

    // Todo Operations
    public void addTodo(Todo todo, FirestoreCallback<String> callback) {
        Log.d(TAG, "➕ Adding todo: " + todo.getTitle());

        db.collection(TODOS_COLLECTION)
                .add(todo)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "✅ Todo added with ID: " + documentReference.getId());
                    callback.onSuccess(documentReference.getId());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Error adding todo: " + e.getMessage());
                    callback.onFailure(e.getMessage());
                });
    }

    public void updateTodo(Todo todo, FirestoreCallback<Void> callback) {
        if (todo.getId() == null) {
            Log.e(TAG, "❌ Cannot update todo: ID is null");
            callback.onFailure("Todo ID is null");
            return;
        }

        Log.d(TAG, "✏️ Updating todo: " + todo.getId());

        db.collection(TODOS_COLLECTION)
                .document(todo.getId())
                .set(todo)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "✅ Todo updated successfully: " + todo.getId());
                    callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Error updating todo: " + e.getMessage());
                    callback.onFailure(e.getMessage());
                });
    }

    public void deleteTodo(String todoId, FirestoreCallback<Void> callback) {
        Log.d(TAG, "🗑️ Deleting todo: " + todoId);

        db.collection(TODOS_COLLECTION)
                .document(todoId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "✅ Todo deleted successfully: " + todoId);
                    callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Error deleting todo: " + e.getMessage());
                    callback.onFailure(e.getMessage());
                });
    }

    public LiveData<List<Todo>> getUserTodos(String userId) {
        Log.d(TAG, "📊 Setting up real-time listener for user: " + userId);

        MutableLiveData<List<Todo>> todosLiveData = new MutableLiveData<>();

        // Remove any existing listener first
        if (todosListener != null) {
            Log.d(TAG, "🔌 Removing previous listener");
            todosListener.remove();
            todosListener = null;
        }

        // Setup new listener
        todosListener = db.collection(TODOS_COLLECTION)
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e(TAG, "❌ Firestore listener error: " + error.getMessage());
                        todosLiveData.setValue(new ArrayList<>()); // Set empty list on error
                        return;
                    }

                    List<Todo> todos = new ArrayList<>();
                    if (value != null && !value.isEmpty()) {
                        Log.d(TAG, "📊 Firestore snapshot received: " + value.size() + " documents");

                        for (DocumentSnapshot doc : value.getDocuments()) {
                            try {
                                Todo todo = doc.toObject(Todo.class);
                                if (todo != null) {
                                    todo.setId(doc.getId());
                                    todos.add(todo);
                                    Log.d(TAG, "   ✅ Loaded: " + todo.getTitle() + " (ID: " + todo.getId() + ")");
                                } else {
                                    Log.w(TAG, "   ⚠️ Failed to parse document: " + doc.getId());
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "   ❌ Error parsing document " + doc.getId() + ": " + e.getMessage());
                            }
                        }
                    } else {
                        Log.d(TAG, "📭 No todos found for user: " + userId);
                    }

                    Log.d(TAG, "🔄 Updating LiveData with " + todos.size() + " todos");
                    todosLiveData.setValue(todos);
                });

        Log.d(TAG, "👂 Real-time listener setup complete for user: " + userId);
        return todosLiveData;
    }

    public void toggleTodoComplete(String todoId, boolean isCompleted, FirestoreCallback<Void> callback) {
        Log.d(TAG, "✅ Toggling completion for todo: " + todoId + " -> " + isCompleted);

        db.collection(TODOS_COLLECTION)
                .document(todoId)
                .update("isCompleted", isCompleted) // Use same field name as in model
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "✅ Todo completion status updated: " + todoId);
                    callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Error updating todo completion: " + e.getMessage());
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
                    Log.e(TAG, "❌ Error getting user: " + e.getMessage());
                    callback.onFailure(e.getMessage());
                });
    }

    public void updateUser(User user, FirestoreCallback<Void> callback) {
        db.collection(USERS_COLLECTION)
                .document(user.getUid())
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "✅ User updated successfully");
                    callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Error updating user: " + e.getMessage());
                    callback.onFailure(e.getMessage());
                });
    }

    public void removeListener() {
        if (todosListener != null) {
            Log.d(TAG, "🔌 Removing Firestore listener");
            todosListener.remove();
            todosListener = null;
        }
    }
}