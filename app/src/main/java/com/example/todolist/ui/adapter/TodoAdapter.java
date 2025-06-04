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
import androidx.recyclerview.widget.DiffUtil;
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
        Log.d(TAG, "📱 TodoAdapter created");
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
        if (position < todos.size()) {
            Todo todo = todos.get(position);
            holder.bind(todo);
        }
    }

    @Override
    public int getItemCount() {
        return todos.size();
    }

    public void setTodos(List<Todo> newTodos) {
        Log.d(TAG, "📊 setTodos called with " + (newTodos != null ? newTodos.size() : "null") + " items");

        if (newTodos == null) {
            newTodos = new ArrayList<>();
        }

        // Use DiffUtil for better performance and animations
        TodoDiffCallback diffCallback = new TodoDiffCallback(this.todos, newTodos);
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);

        this.todos.clear();
        this.todos.addAll(newTodos);

        diffResult.dispatchUpdatesTo(this);

        Log.d(TAG, "📱 Adapter updated with " + this.todos.size() + " todos");

        // Log todos for debugging
        for (int i = 0; i < this.todos.size(); i++) {
            Todo todo = this.todos.get(i);
            Log.d(TAG, "   " + (i+1) + ". " + todo.getTitle() + " (completed: " + todo.isCompleted() + ")");
        }
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
                    Log.d(TAG, "✏️ Edit button clicked for position: " + position);
                    listener.onEditClick(todos.get(position));
                }
            });

            btnDelete.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    Log.d(TAG, "🗑️ Delete button clicked for position: " + position);
                    listener.onDeleteClick(todos.get(position));
                }
            });

            cbCompleted.setOnCheckedChangeListener((buttonView, isChecked) -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    Log.d(TAG, "✅ Checkbox toggled for position: " + position + " -> " + isChecked);
                    listener.onCompleteToggle(todos.get(position), isChecked);
                }
            });
        }

        public void bind(Todo todo) {
            if (todo == null) {
                Log.w(TAG, "⚠️ Attempting to bind null todo");
                return;
            }

            tvTitle.setText(todo.getTitle());
            tvDescription.setText(todo.getDescription());
            tvDate.setText(todo.getDate());
            tvPriority.setText(todo.getPriority());
            tvCategory.setText(todo.getCategory());

            // Set checkbox without triggering listener
            cbCompleted.setOnCheckedChangeListener(null);
            cbCompleted.setChecked(todo.isCompleted());
            setupClickListeners(); // Re-setup listeners

            // Set priority color
            int priorityColor = getPriorityColor(todo.getPriority());
            priorityIndicator.setBackgroundColor(priorityColor);
            tvPriority.setTextColor(priorityColor);

            // Apply completed styling
            applyCompletedStyling(todo.isCompleted());
        }

        private void applyCompletedStyling(boolean isCompleted) {
            if (isCompleted) {
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
            if (priority == null) return ContextCompat.getColor(itemView.getContext(), R.color.priority_medium);

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

    // DiffUtil Callback for efficient updates
    private static class TodoDiffCallback extends DiffUtil.Callback {
        private final List<Todo> oldList;
        private final List<Todo> newList;

        public TodoDiffCallback(List<Todo> oldList, List<Todo> newList) {
            this.oldList = oldList;
            this.newList = newList;
        }

        @Override
        public int getOldListSize() {
            return oldList.size();
        }

        @Override
        public int getNewListSize() {
            return newList.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            Todo oldTodo = oldList.get(oldItemPosition);
            Todo newTodo = newList.get(newItemPosition);

            if (oldTodo == null || newTodo == null) return false;

            return oldTodo.getId() != null && oldTodo.getId().equals(newTodo.getId());
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            Todo oldTodo = oldList.get(oldItemPosition);
            Todo newTodo = newList.get(newItemPosition);

            if (oldTodo == null || newTodo == null) return false;

            return oldTodo.getTitle().equals(newTodo.getTitle()) &&
                    oldTodo.getDescription().equals(newTodo.getDescription()) &&
                    oldTodo.getDate().equals(newTodo.getDate()) &&
                    oldTodo.getPriority().equals(newTodo.getPriority()) &&
                    oldTodo.getCategory().equals(newTodo.getCategory()) &&
                    oldTodo.isCompleted() == newTodo.isCompleted();
        }
    }
}