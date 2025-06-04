package com.example.todolist.ui.adapter;

import android.graphics.Paint;
import android.util.Log;
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
    private static final String TAG = "TodoAdapter";

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
        Log.d(TAG, "Setting todos: " + todos.size() + " items");
        this.todos = new ArrayList<>(todos); // Create a copy to avoid reference issues
        notifyDataSetChanged();
    }

    class TodoViewHolder extends RecyclerView.ViewHolder {
        private TextView tvTitle, tvDescription, tvDate, tvPriority, tvCategory;
        private CheckBox cbCompleted;
        private ImageButton btnEdit, btnDelete;
        private View priorityIndicator;
        private boolean isBinding = false; // Flag to prevent infinite loops

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

            setupClickListeners();
        }

        private void setupClickListeners() {
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onTodoClick(todos.get(position));
                }
            });

            btnEdit.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onEditClick(todos.get(position));
                }
            });

            btnDelete.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onDeleteClick(todos.get(position));
                }
            });

            cbCompleted.setOnCheckedChangeListener((buttonView, isChecked) -> {
                int position = getAdapterPosition();
                if (!isBinding && position != RecyclerView.NO_POSITION && listener != null) {
                    Log.d(TAG, "Checkbox changed: " + isChecked + " for position " + position);
                    listener.onCompleteToggle(todos.get(position), isChecked);
                }
            });
        }

        public void bind(Todo todo) {
            isBinding = true; // Prevent checkbox listener from firing during bind

            Log.d(TAG, "Binding todo: " + todo.getTitle() + ", completed: " + todo.isCompleted());

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

            // Apply completed style
            applyCompletedStyle(todo.isCompleted());

            isBinding = false; // Re-enable checkbox listener
        }

        private void applyCompletedStyle(boolean isCompleted) {
            if (isCompleted) {
                // Strike through text
                tvTitle.setPaintFlags(tvTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                tvDescription.setPaintFlags(tvDescription.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

                // Reduce opacity
                itemView.setAlpha(0.7f);

                // Change text color to secondary
                tvTitle.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.text_secondary));
                tvDescription.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.text_secondary));
            } else {
                // Remove strike through
                tvTitle.setPaintFlags(tvTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                tvDescription.setPaintFlags(tvDescription.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));

                // Full opacity
                itemView.setAlpha(1.0f);

                // Normal text color
                tvTitle.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.text_primary));
                tvDescription.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.text_secondary));
            }
        }

        private int getPriorityColor(String priority) {
            if (priority == null) priority = "MEDIUM";

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