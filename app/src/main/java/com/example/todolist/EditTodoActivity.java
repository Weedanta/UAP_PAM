package com.example.todolist;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class EditTodoActivity extends AppCompatActivity {
    private EditText etTitle, etDescription, etDate;
    private Button btnSave, btnDelete;

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
        int position = intent.getIntExtra("POSITION", -1);

        etTitle.setText(title);
        etDescription.setText(description);
        etDate.setText(date);

        btnSave.setOnClickListener(v -> {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("TITLE", etTitle.getText().toString());
            resultIntent.putExtra("DESCRIPTION", etDescription.getText().toString());
            resultIntent.putExtra("DATE", etDate.getText().toString());
            resultIntent.putExtra("POSITION", position);
            setResult(RESULT_OK, resultIntent);
            finish();
        });

        btnDelete.setOnClickListener(v -> {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("POSITION", position);
            setResult(RESULT_OK, resultIntent);
            finish();
        });
    }
}
