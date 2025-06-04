package com.example.todolist.ui.main;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
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

    // Data for dropdowns
    private String[] priorityOptions = {"HIGH", "MEDIUM", "LOW"};
    private String[] categoryOptions = {"General", "Work", "Personal", "Shopping", "Health", "Study"};
    private String selectedPriority = "MEDIUM";
    private String selectedCategory = "General";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            binding = ActivityEditTodoBinding.inflate(getLayoutInflater());
            setContentView(binding.getRoot());

            initializeComponents();
            setupUI();
            loadTodoData();
        } catch (Exception e) {
            Toast.makeText(this, "Error starting edit activity: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void initializeComponents() {
        authManager = new FirebaseAuthManager(this);
        firestoreManager = new FirestoreManager();
        selectedDate = Calendar.getInstance();
    }

    private void setupUI() {
        setupToolbar();
        setupDatePicker();
        setupDropdowns();
        setupButtons();
    }

    private void setupToolbar() {
        try {
            if (binding.toolbar != null) {
                setSupportActionBar(binding.toolbar);
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                    getSupportActionBar().setTitle("Edit Todo");
                }
            } else {
                setTitle("Edit Todo");
            }
        } catch (Exception e) {
            setTitle("Edit Todo");
        }
    }

    private void setupDatePicker() {
        try {
            if (binding.etDate != null) {
                binding.etDate.setOnClickListener(v -> showDatePicker());
                binding.etDate.setFocusable(false);
                binding.etDate.setClickable(true);
            }
        } catch (Exception e) {
            // Ignore date picker setup errors
        }
    }

    private void setupDropdowns() {
        try {
            // Setup Priority Dropdown
            if (binding.spinnerPriority != null) {
                ArrayAdapter<String> priorityAdapter = new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_dropdown_item_1line,
                        priorityOptions
                );
                binding.spinnerPriority.setAdapter(priorityAdapter);
                binding.spinnerPriority.setOnItemClickListener((parent, view, position, id) -> {
                    selectedPriority = priorityOptions[position];
                });
            }

            // Setup Category Dropdown
            if (binding.spinnerCategory != null) {
                ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_dropdown_item_1line,
                        categoryOptions
                );
                binding.spinnerCategory.setAdapter(categoryAdapter);
                binding.spinnerCategory.setOnItemClickListener((parent, view, position, id) -> {
                    selectedCategory = categoryOptions[position];
                });
            }
        } catch (Exception e) {
            // Fallback values
            selectedPriority = "MEDIUM";
            selectedCategory = "General";
        }
    }

    private void setupButtons() {
        try {
            if (binding.btnSave != null) {
                binding.btnSave.setOnClickListener(v -> updateTodo());
            }

            if (binding.btnDelete != null) {
                binding.btnDelete.setOnClickListener(v -> showDeleteConfirmation());
            }

            if (binding.btnCancel != null) {
                binding.btnCancel.setOnClickListener(v -> finish());
            }
        } catch (Exception e) {
            // Ignore button setup errors
        }
    }

    private void loadTodoData() {
        try {
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

            // Set text fields safely
            setTextSafely(binding.etTitle, title);
            setTextSafely(binding.etDescription, description);
            setTextSafely(binding.etDate, date);

            // Set priority and category with fallback
            selectedPriority = (priority != null && !priority.isEmpty()) ? priority : "MEDIUM";
            selectedCategory = (category != null && !category.isEmpty()) ? category : "General";

            // Update dropdown displays
            if (binding.spinnerPriority != null) {
                binding.spinnerPriority.setText(selectedPriority, false);
            }
            if (binding.spinnerCategory != null) {
                binding.spinnerCategory.setText(selectedCategory, false);
            }

            // Parse and set the date
            if (date != null && !date.isEmpty()) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    selectedDate.setTime(sdf.parse(date));
                } catch (ParseException e) {
                    // Keep current date if parsing fails
                }
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error loading todo data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void setTextSafely(Object editText, String text) {
        try {
            if (editText != null && editText instanceof com.google.android.material.textfield.TextInputEditText && text != null) {
                ((com.google.android.material.textfield.TextInputEditText) editText).setText(text);
            }
        } catch (Exception e) {
            // Ignore
        }
    }

    private void showDatePicker() {
        try {
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
        } catch (Exception e) {
            Toast.makeText(this, "Unable to open date picker", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateDateField() {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            if (binding.etDate != null) {
                binding.etDate.setText(sdf.format(selectedDate.getTime()));
            }
        } catch (Exception e) {
            // Ignore date formatting errors
        }
    }

    private void updateTodo() {
        try {
            // Get input values safely
            String title = getTextSafely(binding.etTitle);
            String description = getTextSafely(binding.etDescription);
            String date = getTextSafely(binding.etDate);

            // Validation
            if (title.isEmpty()) {
                showError(binding.etTitle, "Title is required");
                return;
            }

            if (description.isEmpty()) {
                showError(binding.etDescription, "Description is required");
                return;
            }

            if (date.isEmpty()) {
                Toast.makeText(this, "Date is required", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check authentication
            FirebaseUser currentUser = authManager.getCurrentUser();
            if (currentUser == null) {
                Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
                return;
            }

            // Show loading
            showLoading();

            // Create and update todo
            Todo todo = new Todo(title, description, date, currentUser.getUid());
            todo.setId(todoId);
            todo.setPriority(selectedPriority);
            todo.setCategory(selectedCategory);

            firestoreManager.updateTodo(todo, new FirestoreManager.FirestoreCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    runOnUiThread(() -> {
                        hideLoading();
                        Toast.makeText(EditTodoActivity.this, "Todo updated successfully", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    });
                }

                @Override
                public void onFailure(String error) {
                    runOnUiThread(() -> {
                        hideLoading();
                        Toast.makeText(EditTodoActivity.this, "Failed to update todo: " + error, Toast.LENGTH_LONG).show();
                    });
                }
            });

        } catch (Exception e) {
            hideLoading();
            Toast.makeText(this, "Error updating todo: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private String getTextSafely(Object editText) {
        try {
            if (editText != null && editText instanceof com.google.android.material.textfield.TextInputEditText) {
                return ((com.google.android.material.textfield.TextInputEditText) editText).getText().toString().trim();
            }
        } catch (Exception e) {
            // Ignore
        }
        return "";
    }

    private void showError(Object editText, String message) {
        try {
            if (editText != null && editText instanceof com.google.android.material.textfield.TextInputEditText) {
                com.google.android.material.textfield.TextInputEditText et =
                        (com.google.android.material.textfield.TextInputEditText) editText;
                et.setError(message);
                et.requestFocus();
            } else {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
    }

    private void showDeleteConfirmation() {
        try {
            new AlertDialog.Builder(this)
                    .setTitle("Delete Todo")
                    .setMessage("Are you sure you want to delete this todo?")
                    .setPositiveButton("Delete", (dialog, which) -> deleteTodo())
                    .setNegativeButton("Cancel", null)
                    .show();
        } catch (Exception e) {
            // If dialog fails, ask via toast
            Toast.makeText(this, "Delete this todo? Press delete button again to confirm.", Toast.LENGTH_LONG).show();
            // Change delete button to confirmation mode
            if (binding.btnDelete != null) {
                binding.btnDelete.setText("Confirm Delete");
                binding.btnDelete.setOnClickListener(v -> deleteTodo());
            }
        }
    }

    private void deleteTodo() {
        try {
            showLoading();

            firestoreManager.deleteTodo(todoId, new FirestoreManager.FirestoreCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    runOnUiThread(() -> {
                        hideLoading();
                        Toast.makeText(EditTodoActivity.this, "Todo deleted successfully", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    });
                }

                @Override
                public void onFailure(String error) {
                    runOnUiThread(() -> {
                        hideLoading();
                        Toast.makeText(EditTodoActivity.this, "Failed to delete todo: " + error, Toast.LENGTH_LONG).show();
                    });
                }
            });
        } catch (Exception e) {
            hideLoading();
            Toast.makeText(this, "Error deleting todo: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void showLoading() {
        try {
            if (binding.progressBar != null) {
                binding.progressBar.setVisibility(View.VISIBLE);
            }
            if (binding.btnSave != null) {
                binding.btnSave.setEnabled(false);
            }
            if (binding.btnDelete != null) {
                binding.btnDelete.setEnabled(false);
            }
        } catch (Exception e) {
            // Ignore loading UI errors
        }
    }

    private void hideLoading() {
        try {
            if (binding.progressBar != null) {
                binding.progressBar.setVisibility(View.GONE);
            }
            if (binding.btnSave != null) {
                binding.btnSave.setEnabled(true);
            }
            if (binding.btnDelete != null) {
                binding.btnDelete.setEnabled(true);
            }
        } catch (Exception e) {
            // Ignore loading UI errors
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            binding = null;
        } catch (Exception e) {
            // Ignore cleanup errors
        }
    }
}