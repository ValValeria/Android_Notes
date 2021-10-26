package com.example.note.ui.add_note;

import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.room.Room;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.note.R;
import com.example.note.database.AppDatabase;
import com.example.note.databinding.FragmentAddNoteBinding;
import com.example.note.models.Note;
import com.google.android.material.snackbar.Snackbar;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


public class AddNoteFragment extends Fragment {
    private FragmentAddNoteBinding fragmentAddNoteBinding;
    private AppDatabase appDatabase;

    public AddNoteFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        appDatabase = Room.databaseBuilder(requireActivity().getApplicationContext(),
                AppDatabase.class, AppDatabase.DB_NAME).build();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        fragmentAddNoteBinding = FragmentAddNoteBinding.inflate(inflater);

        return fragmentAddNoteBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        fragmentAddNoteBinding.addNoteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String title = fragmentAddNoteBinding.titleInput.getText().toString();
                String description = fragmentAddNoteBinding.descrInput.getText().toString();

                Note note = new Note();
                note.setTitle(title);
                note.setContent(description);
                note.setRelated("[]");
                note.setTime(new Date().getTime());

                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        List<Note> noteList = appDatabase.noteDao().getAll();

                        int id = noteList.get(noteList.size() - 1).getId() + 1;
                        note.setId(id);

                        appDatabase.noteDao().insertAll(note);

                        Snackbar.make(view, "The note is added", Snackbar.LENGTH_LONG)
                                .setAction("Close", null).show();
                    }
                });
            }
        });
    }
}