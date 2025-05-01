package com.example.todolist;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ToDoAdapter adapter;
    private ToDoViewModel todoViewModel;
    private List<ToDoModel> todoList = new ArrayList<>();

    public static final int ADD_TODO_REQUEST = 100;
    public static final int EDIT_TODO_REQUEST = 200;

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

        // Initialize the adapter with empty list (will be populated from database)
        adapter = new ToDoAdapter(new ArrayList<>(), new ToDoAdapter.OnItemClickListener() {
            @Override
            public void onEditClick(int position) {
                ToDoModel todo = todoList.get(position);

                Intent intent = new Intent(MainActivity.this, EditTodoActivity.class);
                intent.putExtra("TITLE", todo.getTitle());
                intent.putExtra("DESCRIPTION", todo.getDescription());
                intent.putExtra("DATE", todo.getDate());
                intent.putExtra("ID", todo.getId());
                startActivityForResult(intent, EDIT_TODO_REQUEST);
            }

            @Override
            public void onDeleteClick(int position) {
                ToDoModel todoToDelete = todoList.get(position);
                todoViewModel.delete(todoToDelete);
                Toast.makeText(MainActivity.this, "Todo deleted!", Toast.LENGTH_SHORT).show();
            }
        });

        recyclerView.setAdapter(adapter);

        // Initialize ViewModel
        todoViewModel = new ViewModelProvider(this).get(ToDoViewModel.class);

        // Observe the LiveData from ViewModel
        todoViewModel.getAllTodos().observe(this, new Observer<List<ToDoModel>>() {
            @Override
            public void onChanged(List<ToDoModel> todos) {
                // Update the cached copy of the todos in the adapter
                todoList = todos;
                adapter.setTodos(todos);
            }
        });

        btnAdd.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddTodoActivity.class);
            startActivityForResult(intent, ADD_TODO_REQUEST);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == ADD_TODO_REQUEST) {
                // Handle add todo
                String title = data.getStringExtra("TITLE");
                String description = data.getStringExtra("DESCRIPTION");
                String date = data.getStringExtra("DATE");

                if (title != null && description != null && date != null) {
                    ToDoModel newTodo = new ToDoModel(title, description, date);
                    todoViewModel.insert(newTodo);
                    Toast.makeText(this, "Todo added!", Toast.LENGTH_SHORT).show();
                }

            } else if (requestCode == EDIT_TODO_REQUEST) {
                // Check if this is a delete operation
                if (data.getBooleanExtra("DELETE", false)) {
                    int id = data.getIntExtra("ID", -1);
                    if (id != -1) {
                        // Find the todo with this ID and delete it
                        for (ToDoModel todo : todoList) {
                            if (todo.getId() == id) {
                                todoViewModel.delete(todo);
                                Toast.makeText(this, "Todo deleted!", Toast.LENGTH_SHORT).show();
                                break;
                            }
                        }
                    }
                    return;
                }

                // Handle edit todo
                String title = data.getStringExtra("TITLE");
                String description = data.getStringExtra("DESCRIPTION");
                String date = data.getStringExtra("DATE");
                int id = data.getIntExtra("ID", -1);

                if (id == -1) {
                    Toast.makeText(this, "Todo can't be updated!", Toast.LENGTH_SHORT).show();
                    return;
                }

                ToDoModel updatedTodo = new ToDoModel(title, description, date);
                updatedTodo.setId(id);
                todoViewModel.update(updatedTodo);
                Toast.makeText(this, "Todo updated!", Toast.LENGTH_SHORT).show();
            }
        }
    }
}