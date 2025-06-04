package com.example.todolist.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
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
    }

    private void setupRecyclerView() {
        adapter = new TodoAdapter(this);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerView.setAdapter(adapter);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(TodoViewModel.class);

        FirebaseUser currentUser = authManager.getCurrentUser();
        if (currentUser != null) {
            viewModel.setUserId(currentUser.getUid());
        }
    }

    private void setupFab() {
        binding.fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), AddTodoActivity.class);
            startActivity(intent);
        });
    }

    private void observeViewModel() {
        viewModel.getUserTodos().observe(getViewLifecycleOwner(), todos -> {
            if (todos != null) {
                adapter.setTodos(todos);
                binding.emptyView.setVisibility(todos.isEmpty() ? View.VISIBLE : View.GONE);
                binding.recyclerView.setVisibility(todos.isEmpty() ? View.GONE : View.VISIBLE);
            }
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
            }
        });

        viewModel.getSuccessMessage().observe(getViewLifecycleOwner(), success -> {
            if (success != null) {
                Toast.makeText(getContext(), success, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onTodoClick(Todo todo) {
        // Handle todo click (e.g., show details)
    }

    @Override
    public void onEditClick(Todo todo) {
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
        viewModel.deleteTodo(todo.getId());
    }

    @Override
    public void onCompleteToggle(Todo todo, boolean isCompleted) {
        viewModel.toggleTodoComplete(todo.getId(), isCompleted);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}