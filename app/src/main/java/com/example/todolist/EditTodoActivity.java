package com.example.todolist;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class EditTodoActivity extends AppCompatActivity {
    private EditText etTitle, etDescription, etDate;
    private Button btnSave, btnDelete;
    private int todoId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_todo);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.editTodo), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        etTitle = findViewById(R.id.etTitle);
        etDescription = findViewById(R.id.etDescription);
        etDate = findViewById(R.id.etDate);
        btnSave = findViewById(R.id.btnSave);
        btnDelete = findViewById(R.id.btnDelete);

        Intent intent = getIntent();
        String title = intent.getStringExtra("TITLE");
        String description = intent.getStringExtra("DESCRIPTION");
        String date = intent.getStringExtra("DATE");
        todoId = intent.getIntExtra("ID", -1);

        if (todoId == -1) {
            Toast.makeText(this, "Error: No valid todo ID found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        etTitle.setText(title);
        etDescription.setText(description);
        etDate.setText(date);

        btnSave.setOnClickListener(v -> {
            String updatedTitle = etTitle.getText().toString().trim();
            String updatedDescription = etDescription.getText().toString().trim();
            String updatedDate = etDate.getText().toString().trim();

            if (updatedTitle.isEmpty() || updatedDescription.isEmpty() || updatedDate.isEmpty()) {
                Toast.makeText(EditTodoActivity.this, "Harap lengkapi semua kolom!", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent resultIntent = new Intent();
            resultIntent.putExtra("TITLE", updatedTitle);
            resultIntent.putExtra("DESCRIPTION", updatedDescription);
            resultIntent.putExtra("DATE", updatedDate);
            resultIntent.putExtra("ID", todoId); // Pass the ID back for database operations
            setResult(RESULT_OK, resultIntent);
            finish();
        });

        // Delete button now simply returns to MainActivity with result
        // The actual deletion will be handled by MainActivity using the ViewModel
        btnDelete.setOnClickListener(v -> {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("ID", todoId);
            resultIntent.putExtra("DELETE", true); // Flag to indicate deletion
            setResult(RESULT_OK, resultIntent);
            finish();
        });
    }
}