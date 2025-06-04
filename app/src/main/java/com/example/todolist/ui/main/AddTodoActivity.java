package com.example.todolist.ui.main;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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

        try {
            binding = ActivityAddTodoBinding.inflate(getLayoutInflater());
            setContentView(binding.getRoot());

            initializeComponents();
            setupUI();
        } catch (Exception e) {
            Toast.makeText(this, "Error starting activity: " + e.getMessage(), Toast.LENGTH_LONG).show();
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
        setupButtons();
        setDefaultDate();
    }

    private void setupToolbar() {
        try {
            // Set up toolbar if it exists
            if (binding.toolbar != null) {
                setSupportActionBar(binding.toolbar);
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                    getSupportActionBar().setTitle("Add New Todo");
                }
            } else {
                // Fallback: just set title
                setTitle("Add New Todo");
            }
        } catch (Exception e) {
            // If toolbar setup fails, just set title
            setTitle("Add New Todo");
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

    private void setupButtons() {
        try {
            if (binding.btnSave != null) {
                binding.btnSave.setOnClickListener(v -> saveTodo());
            }

            if (binding.btnCancel != null) {
                binding.btnCancel.setOnClickListener(v -> finish());
            }
        } catch (Exception e) {
            // Ignore button setup errors
        }
    }

    private void setDefaultDate() {
        updateDateField();
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
            // If date formatting fails, set current date manually
            if (binding.etDate != null) {
                binding.etDate.setText("10/06/2025");
            }
        }
    }

    private void saveTodo() {
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

            // Create and save todo
            Todo todo = new Todo(title, description, date, currentUser.getUid());
            todo.setPriority("MEDIUM");
            todo.setCategory("General");

            firestoreManager.addTodo(todo, new FirestoreManager.FirestoreCallback<String>() {
                @Override
                public void onSuccess(String result) {
                    runOnUiThread(() -> {
                        hideLoading();
                        Toast.makeText(AddTodoActivity.this, "Todo added successfully", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    });
                }

                @Override
                public void onFailure(String error) {
                    runOnUiThread(() -> {
                        hideLoading();
                        Toast.makeText(AddTodoActivity.this, "Failed to add todo: " + error, Toast.LENGTH_LONG).show();
                    });
                }
            });

        } catch (Exception e) {
            hideLoading();
            Toast.makeText(this, "Error saving todo: " + e.getMessage(), Toast.LENGTH_LONG).show();
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

    private void showLoading() {
        try {
            if (binding.progressBar != null) {
                binding.progressBar.setVisibility(View.VISIBLE);
            }
            if (binding.btnSave != null) {
                binding.btnSave.setEnabled(false);
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