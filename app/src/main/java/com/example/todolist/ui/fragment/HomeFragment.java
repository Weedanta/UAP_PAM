package com.example.todolist.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.todolist.databinding.FragmentHomeBinding;
import com.example.todolist.data.firebase.FirebaseAuthManager;
import com.example.todolist.data.model.Todo;
import com.example.todolist.ui.adapter.TodoAdapter;
import com.example.todolist.ui.main.AddTodoActivity;
import com.example.todolist.ui.main.EditTodoActivity;
import com.example.todolist.ui.main.TodoViewModel;
import com.google.firebase.auth.FirebaseUser;

public class HomeFragment extends Fragment implements TodoAdapter.OnTodoClickListener {
    private static final String TAG = "HomeFragment";

    private FragmentHomeBinding binding;
    private TodoViewModel viewModel;
    private TodoAdapter adapter;
    private FirebaseAuthManager authManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Log.d(TAG, "🏠 HomeFragment onViewCreated");

        authManager = new FirebaseAuthManager(requireActivity());
        setupRecyclerView();
        setupViewModel();
        setupFab();
        observeViewModel();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "🔄 HomeFragment onResume - Refreshing data...");

        // Refresh data setiap kali fragment muncul
        refreshTodos();
    }

    private void setupRecyclerView() {
        Log.d(TAG, "📱 Setting up RecyclerView");
        adapter = new TodoAdapter(this);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerView.setAdapter(adapter);
    }

    private void setupViewModel() {
        Log.d(TAG, "🧠 Setting up ViewModel");
        viewModel = new ViewModelProvider(this).get(TodoViewModel.class);

        FirebaseUser currentUser = authManager.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            Log.d(TAG, "✅ User authenticated: " + currentUser.getEmail());
            Log.d(TAG, "👤 Setting userId in ViewModel: " + userId);

            viewModel.setUserId(userId);
        } else {
            Log.e(TAG, "❌ No authenticated user found!");
            Toast.makeText(getContext(), "Please login again", Toast.LENGTH_LONG).show();
        }
    }

    private void setupFab() {
        binding.fabAdd.setOnClickListener(v -> {
            Log.d(TAG, "➕ FAB clicked - Opening AddTodoActivity");
            Intent intent = new Intent(getContext(), AddTodoActivity.class);
            startActivity(intent);
        });
    }

    private void observeViewModel() {
        Log.d(TAG, "👀 Setting up observers");

        // Observer untuk todos LiveData
        viewModel.getUserTodos().observe(getViewLifecycleOwner(), todos -> {
            Log.d(TAG, "📊 LiveData changed - Todos received: " + (todos != null ? todos.size() : "null"));

            if (todos != null) {
                Log.d(TAG, "📝 Todo list details:");
                for (int i = 0; i < todos.size(); i++) {
                    Todo todo = todos.get(i);
                    Log.d(TAG, "   " + (i+1) + ". " + todo.getTitle() +
                            " (ID: " + todo.getId() +
                            ", completed: " + todo.isCompleted() +
                            ", userId: " + todo.getUserId() + ")");
                }

                Log.d(TAG, "🔄 Updating adapter...");
                adapter.setTodos(todos);
                updateUI(todos);
                Log.d(TAG, "✅ UI update complete");
            } else {
                Log.w(TAG, "⚠️ Todos list is null");
                updateUI(null);
            }
        });

        // Observer untuk error messages
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Log.e(TAG, "❌ Error: " + error);
                Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
            }
        });

        // Observer untuk success messages
        viewModel.getSuccessMessage().observe(getViewLifecycleOwner(), success -> {
            if (success != null && !success.isEmpty()) {
                Log.d(TAG, "✅ Success: " + success);
                Toast.makeText(getContext(), success, Toast.LENGTH_SHORT).show();

                // Force refresh data setelah operasi berhasil
                Log.d(TAG, "🔄 Force refreshing after success...");
                viewModel.refreshData();
            }
        });
    }

    private void updateUI(java.util.List<Todo> todos) {
        if (todos == null || todos.isEmpty()) {
            Log.d(TAG, "📭 No todos - showing empty view");
            binding.emptyView.setVisibility(View.VISIBLE);
            binding.recyclerView.setVisibility(View.GONE);

            // Update stats
            binding.tvTotalTodos.setText("0");
            binding.tvPendingTodos.setText("0");
            binding.tvCompletedTodos.setText("0");
        } else {
            Log.d(TAG, "📋 Showing todos list (" + todos.size() + " items)");
            binding.emptyView.setVisibility(View.GONE);
            binding.recyclerView.setVisibility(View.VISIBLE);

            // Calculate stats
            int total = todos.size();
            int completed = 0;
            for (Todo todo : todos) {
                if (todo.isCompleted()) {
                    completed++;
                }
            }
            int pending = total - completed;

            // Update stats
            binding.tvTotalTodos.setText(String.valueOf(total));
            binding.tvPendingTodos.setText(String.valueOf(pending));
            binding.tvCompletedTodos.setText(String.valueOf(completed));

            Log.d(TAG, "📊 Stats - Total: " + total + ", Pending: " + pending + ", Completed: " + completed);
        }
    }

    private void refreshTodos() {
        Log.d(TAG, "🔄 Manually refreshing todos via ViewModel...");
        if (viewModel != null) {
            viewModel.refreshData();
        }
    }

    @Override
    public void onTodoClick(Todo todo) {
        Log.d(TAG, "👆 Todo clicked: " + todo.getTitle());
        // Handle todo click (e.g., show details)
    }

    @Override
    public void onEditClick(Todo todo) {
        Log.d(TAG, "✏️ Edit clicked for: " + todo.getTitle());
        Intent intent = new Intent(getContext(), EditTodoActivity.class);
        intent.putExtra("TODO_ID", todo.getId());
        intent.putExtra("TITLE", todo.getTitle());
        intent.putExtra("DESCRIPTION", todo.getDescription());
        intent.putExtra("DATE", todo.getDate());
        intent.putExtra("PRIORITY", todo.getPriority());
        intent.putExtra("CATEGORY", todo.getCategory());
        startActivity(intent);
    }

    @Override
    public void onDeleteClick(Todo todo) {
        Log.d(TAG, "🗑️ Delete clicked for: " + todo.getTitle());
        viewModel.deleteTodo(todo.getId());
    }

    @Override
    public void onCompleteToggle(Todo todo, boolean isCompleted) {
        Log.d(TAG, "✅ Toggle complete for: " + todo.getTitle() + " -> " + isCompleted);
        viewModel.toggleTodoComplete(todo.getId(), isCompleted);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "💀 HomeFragment onDestroyView");
        binding = null;
    }
}