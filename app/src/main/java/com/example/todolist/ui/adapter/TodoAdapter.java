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
        this.todos = new ArrayList<>(todos); // Create a copy to avoid reference issues
        notifyDataSetChanged();
    }

    class TodoViewHolder extends RecyclerView.ViewHolder {
        private TextView tvTitle, tvDescription, tvDate, tvPriority, tvCategory;
        private CheckBox cbCompleted;
        private ImageButton btnEdit, btnDelete;
        private View priorityIndicator;
        private boolean isBinding = false; // Flag to prevent recursive calls

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
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onTodoClick(todos.get(getAdapterPosition()));
                }
            });

            btnEdit.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onEditClick(todos.get(getAdapterPosition()));
                }
            });

            btnDelete.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onDeleteClick(todos.get(getAdapterPosition()));
                }
            });

            cbCompleted.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (!isBinding && listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    Todo todo = todos.get(getAdapterPosition());
                    listener.onCompleteToggle(todo, isChecked);
                }
            });
        }

        public void bind(Todo todo) {
            isBinding = true; // Prevent checkbox listener from firing during binding

            tvTitle.setText(todo.getTitle());
            tvDescription.setText(todo.getDescription());
            tvDate.setText(todo.getDate());
            tvPriority.setText(todo.getPriority());
            tvCategory.setText(todo.getCategory());

            // Set checkbox state without triggering listener
            cbCompleted.setChecked(todo.isCompleted());

            // Set priority color
            int priorityColor = getPriorityColor(todo.getPriority());
            priorityIndicator.setBackgroundColor(priorityColor);
            tvPriority.setTextColor(priorityColor);

            // Apply visual changes for completed todos
            if (todo.isCompleted()) {
                // Strike through text
                tvTitle.setPaintFlags(tvTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                tvDescription.setPaintFlags(tvDescription.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

                // Reduce opacity
                itemView.setAlpha(0.7f);

                // Disable edit button for completed todos
                btnEdit.setEnabled(false);
                btnEdit.setAlpha(0.5f);
            } else {
                // Remove strike through
                tvTitle.setPaintFlags(tvTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                tvDescription.setPaintFlags(tvDescription.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));

                // Full opacity
                itemView.setAlpha(1.0f);

                // Enable edit button
                btnEdit.setEnabled(true);
                btnEdit.setAlpha(1.0f);
            }

            isBinding = false; // Re-enable checkbox listener
        }

        private int getPriorityColor(String priority) {
            if (priority == null) {
                return ContextCompat.getColor(itemView.getContext(), R.color.priority_medium);
            }

            switch (priority.toUpperCase()) {
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