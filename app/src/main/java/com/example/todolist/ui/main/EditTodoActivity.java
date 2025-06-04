package com.example.todolist.ui.main;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.todolist.databinding.ActivityEditTodoBinding;
import com.example.todolist.data.firebase.FirebaseAuthManager;
import com.example.todolist.data.firebase.FirestoreManager;
import com.example.todolist.data.model.Todo;
import com.google.firebase.auth.FirebaseUser;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class EditTodoActivity extends AppCompatActivity {
    private ActivityEditTodoBinding binding;
    private FirebaseAuthManager authManager;
    private FirestoreManager firestoreManager;
    private Calendar selectedDate;
    private String todoId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditTodoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authManager = new FirebaseAuthManager(this);
        firestoreManager = new FirestoreManager();
        selectedDate = Calendar.getInstance();

        setupToolbar();
        setupDatePicker();
        setupButtons();
        loadTodoData();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Edit Todo");
        }
    }

    private void setupDatePicker() {
        binding.etDate.setOnClickListener(v -> showDatePicker());
        binding.etDate.setFocusable(false);
        binding.etDate.setClickable(true);
    }

    private void setupButtons() {
        binding.btnSave.setOnClickListener(v -> updateTodo());
        binding.btnDelete.setOnClickListener(v -> showDeleteConfirmation());
        binding.btnCancel.setOnClickListener(v -> finish());
    }

    private void loadTodoData() {
        todoId = getIntent().getStringExtra("TODO_ID");
        String title = getIntent().getStringExtra("TITLE");
        String description = getIntent().getStringExtra("DESCRIPTION");
        String date = getIntent().getStringExtra("DATE");
        String priority = getIntent().getStringExtra("PRIORITY");
        String category = getIntent().getStringExtra("CATEGORY");

        if (todoId == null) {
            Toast.makeText(this, "Error: Todo ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        binding.etTitle.setText(title);
        binding.etDescription.setText(description);
        binding.etDate.setText(date);

        // Parse and set the date
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            selectedDate.setTime(sdf.parse(date));
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    selectedDate.set(Calendar.YEAR, year);
                    selectedDate.set(Calendar.MONTH, month);
                    selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    updateDateField();
                },
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH));

        datePickerDialog.show();
    }

    private void updateDateField() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        binding.etDate.setText(sdf.format(selectedDate.getTime()));
    }

    private void updateTodo() {
        String title = binding.etTitle.getText().toString().trim();
        String description = binding.etDescription.getText().toString().trim();
        String date = binding.etDate.getText().toString().trim();

        if (title.isEmpty()) {
            binding.etTitle.setError("Title is required");
            binding.etTitle.requestFocus();
            return;
        }

        if (description.isEmpty()) {
            binding.etDescription.setError("Description is required");
            binding.etDescription.requestFocus();
            return;
        }

        FirebaseUser currentUser = authManager.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading();

        Todo todo = new Todo(title, description, date, currentUser.getUid());
        todo.setId(todoId);
        todo.setPriority("MEDIUM");
        todo.setCategory("General");

        firestoreManager.updateTodo(todo, new FirestoreManager.FirestoreCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                hideLoading();
                Toast.makeText(EditTodoActivity.this, "Todo updated successfully", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onFailure(String error) {
                hideLoading();
                Toast.makeText(EditTodoActivity.this, "Failed to update todo: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showDeleteConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Todo")
                .setMessage("Are you sure you want to delete this todo?")
                .setPositiveButton("Delete", (dialog, which) -> deleteTodo())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteTodo() {
        showLoading();

        firestoreManager.deleteTodo(todoId, new FirestoreManager.FirestoreCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                hideLoading();
                Toast.makeText(EditTodoActivity.this, "Todo deleted successfully", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onFailure(String error) {
                hideLoading();
                Toast.makeText(EditTodoActivity.this, "Failed to delete todo: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showLoading() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnSave.setEnabled(false);
        binding.btnDelete.setEnabled(false);
    }

    private void hideLoading() {
        binding.progressBar.setVisibility(View.GONE);
        binding.btnSave.setEnabled(true);
        binding.btnDelete.setEnabled(true);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}