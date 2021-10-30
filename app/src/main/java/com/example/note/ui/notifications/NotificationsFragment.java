package com.example.note.ui.notifications;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.ObservableBoolean;
import androidx.fragment.app.Fragment;
import androidx.room.Room;

import com.example.note.R;
import com.example.note.database.AppDatabase;
import com.example.note.databinding.FragmentNotificationsBinding;
import com.example.note.models.Note;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NotificationsFragment extends Fragment {
    private FragmentNotificationsBinding binding;
    public final ObservableBoolean noResults = new ObservableBoolean(true);
    private final ArrayList<Bitmap> bitmapArrayList = new ArrayList<>();
    private AppDatabase appDatabase;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        appDatabase = Room.databaseBuilder(requireActivity().getApplicationContext(),
                AppDatabase.class, AppDatabase.DB_NAME).build();

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        AsyncTask.execute(() -> {
            List<Note> noteList = appDatabase.noteDao().getAll();

            for(Note note: noteList){
                try {
                    JSONArray urls = new JSONArray(note.getRelated());

                    for (int i = 0; i < urls.length(); i++) {
                        Uri uri = Uri.parse(urls.get(i).toString());
                        ContentResolver contentResolver = requireContext().getContentResolver();
                        String type = contentResolver.getType(uri);
                        LayoutInflater layoutInflater = (LayoutInflater) requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                        if(type.startsWith("image/")){
                           Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), uri);
                           View image = layoutInflater.inflate(R.layout.image, binding.images, true);

                           ImageView imageView = image.findViewById(R.id.image);
                           imageView.setImageBitmap(bitmap);
                        }

                        if(type.startsWith("video/")){
                            View view1 = layoutInflater.inflate(R.layout.video, binding.images, true);
                            VideoView videoView = view1.findViewById(R.id.video);
                            videoView.setVideoURI(uri);
                        }

                        noResults.set(false);
                    }
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}