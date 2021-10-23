package com.example.note.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.note.models.Note;

import java.util.List;

@Dao
public interface NoteDao {
    @Query("SELECT * FROM note")
    List<Note> getAll();

    @Query("SELECT * FROM note WHERE id IN (:ids)")
    List<Note> loadAllByIds(int[] ids);

    @Insert
    void insertAll(Note... notes);

    @Delete
    void delete(Note note);
}
