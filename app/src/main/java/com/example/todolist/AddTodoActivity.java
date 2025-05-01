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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AddTodoActivity extends AppCompatActivity {
    private EditText etTitle, etDesc, etDate;
    private Button btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_todo);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.addTodo), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        etTitle = findViewById(R.id.etTitle);
        etDesc = findViewById(R.id.etDesc);
        etDate = findViewById(R.id.etDate);
        btnSave = findViewById(R.id.btnSave);

        // Set default date to today
        String currentDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
        etDate.setText(currentDate);

        btnSave.setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            String desc = etDesc.getText().toString().trim();
            String date = etDate.getText().toString().trim();

            if (title.isEmpty() || desc.isEmpty() || date.isEmpty()) {
                Toast.makeText(AddTodoActivity.this, "Harap lengkapi semua kolom!", Toast.LENGTH_SHORT).show();
            } else {
                // Create intent with proper keys for MainActivity
                Intent resultIntent = new Intent();
                resultIntent.putExtra("TITLE", title);
                resultIntent.putExtra("DESCRIPTION", desc); // Changed from DESC to DESCRIPTION to match MainActivity
                resultIntent.putExtra("DATE", date);
                setResult(RESULT_OK, resultIntent);
                finish();
            }
        });
    }
}