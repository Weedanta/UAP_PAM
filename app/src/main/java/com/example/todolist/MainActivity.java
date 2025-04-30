package com.example.todolist;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import java.util.ArrayList;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ToDoAdapter adapter;
    private ArrayList<ToDoModel> todoList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        Button btnAdd = findViewById(R.id.btnAdd);

        adapter = new ToDoAdapter(todoList, new ToDoAdapter.OnItemClickListener() {
            @Override
            public void onEditClick(int position) {
                ToDoModel todo = todoList.get(position);

                Intent intent = new Intent(MainActivity.this, EditTodoActivity.class);
                intent.putExtra("TITLE", todo.getTitle());
                intent.putExtra("DESCRIPTION", todo.getDescription());
                intent.putExtra("DATE", todo.getDate());
                intent.putExtra("POSITION", position);
                startActivityForResult(intent, 200);
            }

            @Override
            public void onDeleteClick(int position) {
                todoList.remove(position);
                adapter.notifyItemRemoved(position);
            }
        });

        recyclerView.setAdapter(adapter);

        String date = new SimpleDateFormat("dd/MM/yyyy").format(new Date());
        todoList.add(new ToDoModel("Capek", "Lama juga bikin tugas kayak gini", date));
        todoList.add(new ToDoModel("Dahlah", "Kak Nilainya 100 yaa kak, udah niat banget buat ni", date));
        todoList.add(new ToDoModel("Ini Tambahan", "Ini adalah tambahan", date));
        adapter.notifyItemInserted(todoList.size() - 1);

        btnAdd.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddTodoActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == 200) {
                String title = data.getStringExtra("TITLE");
                String description = data.getStringExtra("DESCRIPTION");
                String date = data.getStringExtra("DATE");
                int position = data.getIntExtra("POSITION", -1);

                if (position != -1) {
                    ToDoModel updatedTodo = new ToDoModel(title, description, date);
                    todoList.set(position, updatedTodo);
                    adapter.notifyItemChanged(position);
                }
            }
        }
    }
}
