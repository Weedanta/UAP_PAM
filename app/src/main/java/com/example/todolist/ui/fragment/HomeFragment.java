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

import java.util.List;

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
        Log.d(TAG, "HomeFragment onViewCreated");

        authManager = new FirebaseAuthManager(requireActivity());
        setupRecyclerView();
        setupViewModel();
        setupFab();
        observeViewModel();
    }

    private void setupRecyclerView() {
        Log.d(TAG, "Setting up RecyclerView");
        adapter = new TodoAdapter(this);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerView.setAdapter(adapter);
    }

    private void setupViewModel() {
        Log.d(TAG, "Setting up ViewModel");
        viewModel = new ViewModelProvider(this).get(TodoViewModel.class);

        FirebaseUser currentUser = authManager.getCurrentUser();
        if (currentUser != null) {
            Log.d(TAG, "Current user: " + currentUser.getUid());
            viewModel.setUserId(currentUser.getUid());
        } else {
            Log.w(TAG, "No current user found");
        }
    }

    private void setupFab() {
        binding.fabAdd.setOnClickListener(v -> {
            Log.d(TAG, "FAB clicked, starting AddTodoActivity");
            Intent intent = new Intent(getContext(), AddTodoActivity.class);
            startActivity(intent);
        });
    }

    private void observeViewModel() {
        Log.d(TAG, "Setting up observers");

        // Observe todos
        viewModel.getUserTodos().observe(getViewLifecycleOwner(), todos -> {
            Log.d(TAG, "Todos updated: " + (todos != null ? todos.size() : 0) + " items");
            if (todos != null) {
                updateUI(todos);
                updateStats(todos);
            }
        });

        // Observe error messages
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Log.e(TAG, "Error: " + error);
                Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
                viewModel.clearMessages(); // Clear the message after showing
            }
        });

        // Observe success messages
        viewModel.getSuccessMessage().observe(getViewLifecycleOwner(), success -> {
            if (success != null && !success.isEmpty()) {
                Log.d(TAG, "Success: " + success);
                Toast.makeText(getContext(), success, Toast.LENGTH_SHORT).show();
                viewModel.clearMessages(); // Clear the message after showing
            }
        });
    }

    private void updateUI(List<Todo> todos) {
        adapter.setTodos(todos);

        if (todos.isEmpty()) {
            binding.emptyView.setVisibility(View.VISIBLE);
            binding.recyclerView.setVisibility(View.GONE);
            Log.d(TAG, "No todos - showing empty view");
        } else {
            binding.emptyView.setVisibility(View.GONE);
            binding.recyclerView.setVisibility(View.VISIBLE);
            Log.d(TAG, "Showing " + todos.size() + " todos");
        }
    }

    private void updateStats(List<Todo> todos) {
        int total = todos.size();
        int completed = 0;
        int pending = 0;

        for (Todo todo : todos) {
            if (todo.isCompleted()) {
                completed++;
            } else {
                pending++;
            }
        }

        binding.tvTotalTodos.setText(String.valueOf(total));
        binding.tvCompletedTodos.setText(String.valueOf(completed));
        binding.tvPendingTodos.setText(String.valueOf(pending));

        Log.d(TAG, "Stats updated - Total: " + total + ", Completed: " + completed + ", Pending: " + pending);
    }

    @Override
    public void onTodoClick(Todo todo) {
        Log.d(TAG, "Todo clicked: " + todo.getTitle());
        // Handle todo click (e.g., show details or toggle completion)
        onCompleteToggle(todo, !todo.isCompleted());
    }

    @Override
    public void onEditClick(Todo todo) {
        Log.d(TAG, "Edit clicked for todo: " + todo.getTitle());
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
        Log.d(TAG, "Delete clicked for todo: " + todo.getTitle());
        viewModel.deleteTodo(todo.getId());
    }

    @Override
    public void onCompleteToggle(Todo todo, boolean isCompleted) {
        Log.d(TAG, "Toggle completion for todo: " + todo.getTitle() + " to " + isCompleted);
        viewModel.toggleTodoComplete(todo.getId(), isCompleted);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "HomeFragment onResume");

        // Refresh user ID in case user changed
        FirebaseUser currentUser = authManager.getCurrentUser();
        if (currentUser != null && viewModel != null) {
            viewModel.setUserId(currentUser.getUid());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "HomeFragment onDestroyView");
        binding = null;
    }
}