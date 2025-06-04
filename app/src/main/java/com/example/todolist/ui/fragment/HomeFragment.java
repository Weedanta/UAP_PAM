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

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment implements TodoAdapter.OnTodoClickListener {
    private static final String TAG = "HomeFragment";
    private static final int ADD_TODO_REQUEST = 1001;
    private static final int EDIT_TODO_REQUEST = 1002;

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

        authManager = new FirebaseAuthManager(requireActivity());
        setupRecyclerView();
        setupViewModel();
        setupFab();
        observeViewModel();

        Log.d(TAG, "HomeFragment onViewCreated - setting up initial data");
    }

    private void setupRecyclerView() {
        adapter = new TodoAdapter(this);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerView.setAdapter(adapter);
        Log.d(TAG, "RecyclerView setup completed");
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(TodoViewModel.class);

        FirebaseUser currentUser = authManager.getCurrentUser();
        if (currentUser != null) {
            Log.d(TAG, "Setting up ViewModel for user: " + currentUser.getUid());
            viewModel.setUserId(currentUser.getUid());
        } else {
            Log.w(TAG, "Current user is null");
        }
    }

    private void setupFab() {
        binding.fabAdd.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(getContext(), AddTodoActivity.class);
                startActivityForResult(intent, ADD_TODO_REQUEST);
            } catch (Exception e) {
                Toast.makeText(getContext(), "Error opening add todo: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void observeViewModel() {
        viewModel.getUserTodos().observe(getViewLifecycleOwner(), todos -> {
            Log.d(TAG, "Received todos update: " + (todos != null ? todos.size() : 0) + " items");

            if (todos != null) {
                adapter.setTodos(todos);
                updateStatistics(todos);

                // Show/hide empty view
                if (todos.isEmpty()) {
                    binding.emptyView.setVisibility(View.VISIBLE);
                    binding.recyclerView.setVisibility(View.GONE);
                    Log.d(TAG, "Showing empty view");
                } else {
                    binding.emptyView.setVisibility(View.GONE);
                    binding.recyclerView.setVisibility(View.VISIBLE);
                    Log.d(TAG, "Showing recycler view with " + todos.size() + " todos");
                }
            }
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Log.e(TAG, "Error message received: " + error);
                Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
            }
        });

        viewModel.getSuccessMessage().observe(getViewLifecycleOwner(), success -> {
            if (success != null) {
                Log.d(TAG, "Success message received: " + success);
                Toast.makeText(getContext(), success, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateStatistics(List<Todo> todos) {
        int total = todos.size();
        int completed = 0;

        for (Todo todo : todos) {
            if (todo.isCompleted()) {
                completed++;
            }
        }

        int pending = total - completed;

        binding.tvTotalTodos.setText(String.valueOf(total));
        binding.tvPendingTodos.setText(String.valueOf(pending));
        binding.tvCompletedTodos.setText(String.valueOf(completed));

        Log.d(TAG, "Statistics updated - Total: " + total + ", Pending: " + pending + ", Completed: " + completed);
    }

    @Override
    public void onTodoClick(Todo todo) {
        // Handle todo click (e.g., show details)
        Log.d(TAG, "Todo clicked: " + todo.getTitle());
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
        startActivityForResult(intent, EDIT_TODO_REQUEST);
    }

    @Override
    public void onDeleteClick(Todo todo) {
        Log.d(TAG, "Delete clicked for todo: " + todo.getTitle());
        viewModel.deleteTodo(todo.getId());
    }

    @Override
    public void onCompleteToggle(Todo todo, boolean isCompleted) {
        Log.d(TAG, "Toggle completion for todo: " + todo.getTitle() + " to " + isCompleted);

        // Update the local todo object immediately for UI responsiveness
        todo.setCompleted(isCompleted);
        adapter.notifyDataSetChanged();

        // Update statistics immediately
        List<Todo> currentTodos = new ArrayList<>();
        if (viewModel.getUserTodos().getValue() != null) {
            currentTodos.addAll(viewModel.getUserTodos().getValue());
        }
        updateStatistics(currentTodos);

        // Then update in Firebase
        viewModel.toggleTodoComplete(todo.getId(), isCompleted);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d(TAG, "onActivityResult - requestCode: " + requestCode + ", resultCode: " + resultCode);

        if (resultCode == getActivity().RESULT_OK) {
            switch (requestCode) {
                case ADD_TODO_REQUEST:
                    Log.d(TAG, "Add todo completed successfully - refreshing data");
                    refreshData();
                    break;
                case EDIT_TODO_REQUEST:
                    Log.d(TAG, "Edit todo completed successfully - refreshing data");
                    refreshData();
                    break;
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "HomeFragment onResume - refreshing data");
        refreshData();
    }

    private void refreshData() {
        Log.d(TAG, "Refreshing data...");

        // Clear any previous messages
        viewModel.clearMessages();

        // Re-setup ViewModel with current user
        FirebaseUser currentUser = authManager.getCurrentUser();
        if (currentUser != null) {
            Log.d(TAG, "Refreshing data for user: " + currentUser.getUid());
            viewModel.setUserId(currentUser.getUid());
        } else {
            Log.w(TAG, "Cannot refresh data - current user is null");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "HomeFragment onDestroyView");
        binding = null;
    }
}