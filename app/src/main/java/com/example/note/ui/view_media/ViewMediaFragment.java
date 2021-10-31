package com.example.note.ui.view_media;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.databinding.ObservableBoolean;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.room.Room;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import com.example.note.R;
import com.example.note.database.AppDatabase;
import com.example.note.databinding.FragmentViewMediaBinding;
import com.example.note.models.Note;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;

public class ViewMediaFragment extends Fragment {
    private FragmentViewMediaBinding fragmentViewMediaBinding;
    public final static String URI = "uri";
    public final static String ID = "id";
    public final ObservableBoolean isVideo = new ObservableBoolean(false);
    private NavController navController;
    private AppDatabase appDatabase;
    private Note note = new Note();
    private Uri uri;
    private int id;

    public ViewMediaFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        fragmentViewMediaBinding = FragmentViewMediaBinding.inflate(inflater);
        fragmentViewMediaBinding.setIsVideo(isVideo);
        appDatabase = Room.databaseBuilder(requireActivity().getApplicationContext(),
                AppDatabase.class, AppDatabase.DB_NAME).build();
        navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);

        uri = Uri.parse(requireArguments().getString(ViewMediaFragment.URI));
        id = requireArguments().getInt(ViewMediaFragment.ID);

        return fragmentViewMediaBinding.getRoot();
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        AsyncTask.execute(() -> {
            note = appDatabase.noteDao().loadAllByIds(new int[]{id}).get(0);

            if(note == null){
               navController.navigate(R.id.navigation_notifications);
            } else {
               fragmentViewMediaBinding.deleteBtn.setOnClickListener(view1 -> {
                   JSONArray jsonArray = null;
                   int index = -1;

                   try {
                       jsonArray = new JSONArray(note.getContent());

                       for (int i = 0; i < jsonArray.length(); i++) {
                           String path = jsonArray.getString(i);

                           if(path.equals(uri.toString())){
                              index = i;

                              break;
                           }
                       }

                       if(index >= 0){
                           jsonArray.remove(index);
                       }

                       String newContent = jsonArray.toString();
                       note.setContent(newContent);

                       appDatabase.noteDao().delete(note);
                       appDatabase.noteDao().insertAll(note);

                       Toast.makeText(requireContext(), "The media is deleted", Toast.LENGTH_LONG)
                               .show();
                       navController.navigate(R.id.navigation_notifications);
                   } catch (JSONException e) {
                       e.printStackTrace();
                   }
               });
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();

        ContentResolver contentResolver = requireContext().getContentResolver();
        String type = contentResolver.getType(uri);

        if(type.startsWith("image/")) {
            Bitmap bitmap = null;

            try {
                bitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), uri);
                fragmentViewMediaBinding.image.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if(type.startsWith("video/")){
            isVideo.set(true);

            VideoView videoView = fragmentViewMediaBinding.video;
            videoView.setVideoURI(uri);

            MediaController mediaController = new MediaController(requireActivity());
            videoView.setMediaController(mediaController);
        }
    }
}