package com.example.todolist;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ToDoDao {
    @Insert
    void insert(ToDoModel todo);

    @Update
    void update(ToDoModel todo);

    @Delete
    void delete(ToDoModel todo);

    @Query("DELETE FROM todo_table")
    void deleteAllTodos();

    @Query("SELECT * FROM todo_table")
    LiveData<List<ToDoModel>> getAllTodos();
}