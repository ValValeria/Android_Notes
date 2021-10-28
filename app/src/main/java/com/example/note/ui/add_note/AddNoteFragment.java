package com.example.note.ui.add_note;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.room.Room;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import com.example.note.database.AppDatabase;
import com.example.note.databinding.FragmentAddNoteBinding;
import com.example.note.models.Note;
import com.google.android.material.snackbar.Snackbar;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Date;
import java.util.List;


public class AddNoteFragment extends Fragment {
    private FragmentAddNoteBinding fragmentAddNoteBinding;
    private AppDatabase appDatabase;
    private boolean showInHomeScreen = true;
    private ActivityResultLauncher<String> mGetPhotoContent;
    private ActivityResultLauncher<String> mGetVideoContent;

    public AddNoteFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        appDatabase = Room.databaseBuilder(requireActivity().getApplicationContext(),
                AppDatabase.class, AppDatabase.DB_NAME).build();

        mGetPhotoContent = registerForActivityResult(new ActivityResultContracts.GetContent(),
                uri -> {
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), uri);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        fragmentAddNoteBinding = FragmentAddNoteBinding.inflate(inflater);

        fragmentAddNoteBinding.switchMaterial.setOnCheckedChangeListener((compoundButton, b) -> AddNoteFragment.this.showInHomeScreen = b);
        fragmentAddNoteBinding.uploadBtn.setOnClickListener(view -> mGetPhotoContent.launch("image/**"));

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
                note.setPinned(AddNoteFragment.this.showInHomeScreen);

                AsyncTask.execute(() -> {
                    List<Note> noteList = appDatabase.noteDao().getAll();

                    int id = 0;

                    if(noteList.size() == 0){
                        id = 1;
                    } else{
                        Note lastNote = noteList.get(noteList.size() - 1);
                        id = lastNote.getId() + 1;
                    }

                    note.setId(id);

                    appDatabase.noteDao().insertAll(note);

                    Snackbar.make(view, "The note is added", Snackbar.LENGTH_LONG)
                            .setAction("Close", null).show();
                });
            }
        });
    }
}