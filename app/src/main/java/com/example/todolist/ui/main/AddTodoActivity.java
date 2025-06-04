package com.example.todolist.ui.main;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.todolist.R;
import com.example.todolist.databinding.ActivityAddTodoBinding;
import com.example.todolist.data.firebase.FirebaseAuthManager;
import com.example.todolist.data.firebase.FirestoreManager;
import com.example.todolist.data.model.Todo;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddTodoActivity extends AppCompatActivity {
    private ActivityAddTodoBinding binding;
    private FirebaseAuthManager authManager;
    private FirestoreManager firestoreManager;
    private Calendar selectedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddTodoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authManager = new FirebaseAuthManager(this);
        firestoreManager = new FirestoreManager();
        selectedDate = Calendar.getInstance();

        setupToolbar();
        setupSpinners();
        setupDatePicker();
        setupButtons();
        setDefaultDate();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Add New Todo");
        }
    }

    private void setupSpinners() {
        // Priority spinner
        String[] priorities = {"LOW", "MEDIUM", "HIGH"};
        ArrayAdapter<String> priorityAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, priorities);
        binding.spinnerPriority.setAdapter(priorityAdapter);
        binding.spinnerPriority.setSelection(1); // Default to MEDIUM

        // Category spinner
        String[] categories = {"General", "Work", "Personal", "Shopping", "Health", "Study"};
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, categories);
        binding.spinnerCategory.setAdapter(categoryAdapter);
    }

    private void setupDatePicker() {
        binding.etDate.setOnClickListener(v -> showDatePicker());
        binding.etDate.setFocusable(false);
        binding.etDate.setClickable(true);
    }

    private void setupButtons() {
        binding.btnSave.setOnClickListener(v -> saveTodo());
        binding.btnCancel.setOnClickListener(v -> finish());
    }

    private void setDefaultDate() {
        updateDateField();
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

    private void saveTodo() {
        String title = binding.etTitle.getText().toString().trim();
        String description = binding.etDescription.getText().toString().trim();
        String date = binding.etDate.getText().toString().trim();
        String priority = binding.spinnerPriority.getSelectedItem().toString();
        String category = binding.spinnerCategory.getSelectedItem().toString();

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
        todo.setPriority(priority);
        todo.setCategory(category);

        firestoreManager.addTodo(todo, new FirestoreManager.FirestoreCallback<String>() {
            @Override
            public void onSuccess(String result) {
                hideLoading();
                Toast.makeText(AddTodoActivity.this, "Todo added successfully", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onFailure(String error) {
                hideLoading();
                Toast.makeText(AddTodoActivity.this, "Failed to add todo: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showLoading() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnSave.setEnabled(false);
    }

    private void hideLoading() {
        binding.progressBar.setVisibility(View.GONE);
        binding.btnSave.setEnabled(true);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}