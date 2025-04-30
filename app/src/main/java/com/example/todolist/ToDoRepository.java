package com.example.todolist;

import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import java.util.List;

public class ToDoRepository {
    private ToDoDao todoDao;
    private LiveData<List<ToDoModel>> allTodos;

    public ToDoRepository(Application application) {
        ToDoDatabase database = ToDoDatabase.getInstance(application);
        todoDao = database.todoDao();
        allTodos = todoDao.getAllTodos();
    }

    public void insert(ToDoModel todo) {
        new InsertToDoAsyncTask(todoDao).execute(todo);
    }

    public void update(ToDoModel todo) {
        new UpdateToDoAsyncTask(todoDao).execute(todo);
    }

    public void delete(ToDoModel todo) {
        new DeleteToDoAsyncTask(todoDao).execute(todo);
    }

    public void deleteAllTodos() {
        new DeleteAllToDosAsyncTask(todoDao).execute();
    }

    public LiveData<List<ToDoModel>> getAllTodos() {
        return allTodos;
    }

    private static class InsertToDoAsyncTask extends AsyncTask<ToDoModel, Void, Void> {
        private ToDoDao todoDao;

        private InsertToDoAsyncTask(ToDoDao todoDao) {
            this.todoDao = todoDao;
        }

        @Override
        protected Void doInBackground(ToDoModel... todos) {
            todoDao.insert(todos[0]);
            return null;
        }
    }

    private static class UpdateToDoAsyncTask extends AsyncTask<ToDoModel, Void, Void> {
        private ToDoDao todoDao;

        private UpdateToDoAsyncTask(ToDoDao todoDao) {
            this.todoDao = todoDao;
        }

        @Override
        protected Void doInBackground(ToDoModel... todos) {
            todoDao.update(todos[0]);
            return null;
        }
    }

    private static class DeleteToDoAsyncTask extends AsyncTask<ToDoModel, Void, Void> {
        private ToDoDao todoDao;

        private DeleteToDoAsyncTask(ToDoDao todoDao) {
            this.todoDao = todoDao;
        }

        @Override
        protected Void doInBackground(ToDoModel... todos) {
            todoDao.delete(todos[0]);
            return null;
        }
    }

    private static class DeleteAllToDosAsyncTask extends AsyncTask<Void, Void, Void> {
        private ToDoDao todoDao;

        private DeleteAllToDosAsyncTask(ToDoDao todoDao) {
            this.todoDao = todoDao;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            todoDao.deleteAllTodos();
            return null;
        }
    }
}