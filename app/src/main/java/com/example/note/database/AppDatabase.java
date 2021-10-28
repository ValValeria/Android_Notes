package com.example.note.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.example.note.dao.NoteDao;
import com.example.note.models.Note;

@Database(entities = {Note.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public final static String DB_NAME = "NOTES_DB";
    public abstract NoteDao noteDao();
}
