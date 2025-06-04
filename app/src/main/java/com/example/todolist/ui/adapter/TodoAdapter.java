package com.example.todolist.ui.adapter;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.todolist.R;
import com.example.todolist.data.model.Todo;

import java.util.ArrayList;
import java.util.List;

public class TodoAdapter extends RecyclerView.Adapter<TodoAdapter.TodoViewHolder> {
    private List<Todo> todos = new ArrayList<>();
    private OnTodoClickListener listener;

    public interface OnTodoClickListener {
        void onTodoClick(Todo todo);
        void onEditClick(Todo todo);
        void onDeleteClick(Todo todo);
        void onCompleteToggle(Todo todo, boolean isCompleted);
    }

    public TodoAdapter(OnTodoClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public TodoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_todo_modern, parent, false);
        return new TodoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TodoViewHolder holder, int position) {
        Todo todo = todos.get(position);
        holder.bind(todo);
    }

    @Override
    public int getItemCount() {
        return todos.size();
    }

    public void setTodos(List<Todo> todos) {
        this.todos = todos;
        notifyDataSetChanged();
    }

    class TodoViewHolder extends RecyclerView.ViewHolder {
        private TextView tvTitle, tvDescription, tvDate, tvPriority, tvCategory;
        private CheckBox cbCompleted;
        private ImageButton btnEdit, btnDelete;
        private View priorityIndicator;

        public TodoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvPriority = itemView.findViewById(R.id.tvPriority);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            cbCompleted = itemView.findViewById(R.id.cbCompleted);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            priorityIndicator = itemView.findViewById(R.id.priorityIndicator);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onTodoClick(todos.get(getAdapterPosition()));
                }
            });

            btnEdit.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditClick(todos.get(getAdapterPosition()));
                }
            });

            btnDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteClick(todos.get(getAdapterPosition()));
                }
            });

            cbCompleted.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (listener != null) {
                    listener.onCompleteToggle(todos.get(getAdapterPosition()), isChecked);
                }
            });
        }

        public void bind(Todo todo) {
            tvTitle.setText(todo.getTitle());
            tvDescription.setText(todo.getDescription());
            tvDate.setText(todo.getDate());
            tvPriority.setText(todo.getPriority());
            tvCategory.setText(todo.getCategory());
            cbCompleted.setChecked(todo.isCompleted());

            // Set priority color
            int priorityColor = getPriorityColor(todo.getPriority());
            priorityIndicator.setBackgroundColor(priorityColor);
            tvPriority.setTextColor(priorityColor);

            // Strike through completed todos
            if (todo.isCompleted()) {
                tvTitle.setPaintFlags(tvTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                tvDescription.setPaintFlags(tvDescription.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                itemView.setAlpha(0.7f);
            } else {
                tvTitle.setPaintFlags(tvTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                tvDescription.setPaintFlags(tvDescription.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                itemView.setAlpha(1.0f);
            }
        }

        private int getPriorityColor(String priority) {
            switch (priority) {
                case "HIGH":
                    return ContextCompat.getColor(itemView.getContext(), R.color.priority_high);
                case "MEDIUM":
                    return ContextCompat.getColor(itemView.getContext(), R.color.priority_medium);
                case "LOW":
                    return ContextCompat.getColor(itemView.getContext(), R.color.priority_low);
                default:
                    return ContextCompat.getColor(itemView.getContext(), R.color.priority_medium);
            }
        }
    }
}