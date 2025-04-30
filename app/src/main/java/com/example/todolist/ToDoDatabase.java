package com.example.todolist;

import android.content.Context;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(entities = {ToDoModel.class}, version = 1, exportSchema = false)
public abstract class ToDoDatabase extends RoomDatabase {
    private static ToDoDatabase instance;
    public abstract ToDoDao todoDao();

    public static synchronized ToDoDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            ToDoDatabase.class, "todo_database")
                    .fallbackToDestructiveMigration()
                    .addCallback(roomCallback)
                    .build();
        }
        return instance;
    }

    private static RoomDatabase.Callback roomCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            new PopulateDbAsyncTask(instance).execute();
        }
    };

    private static class PopulateDbAsyncTask extends AsyncTask<Void, Void, Void> {
        private ToDoDao todoDao;

        private PopulateDbAsyncTask(ToDoDatabase db) {
            todoDao = db.todoDao();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            todoDao.insert(new ToDoModel("Capek", "Lama juga bikin tugas kayak gini", "01/05/2025"));
            todoDao.insert(new ToDoModel("Dahlah", "Kak Nilainya 100 yaa kak, udah niat banget buat ni", "01/05/2025"));
            return null;
        }
    }
}