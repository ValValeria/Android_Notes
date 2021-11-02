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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.ObservableBoolean;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.room.Room;
import com.example.note.R;
import com.example.note.database.AppDatabase;
import com.example.note.databinding.FragmentNotificationsBinding;
import com.example.note.models.Note;
import com.example.note.ui.view_media.ViewMediaFragment;
import com.example.note.ui.view_note.ViewNoteFragment;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class NotificationsFragment extends Fragment {
    private FragmentNotificationsBinding binding;
    public final ObservableBoolean noResults = new ObservableBoolean(true);
    private final String TAG = NotificationsFragment.class.getName();
    private AppDatabase appDatabase;
    private NavController navController;
    private LayoutInflater layoutInflater;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        binding.setNoResults(noResults);

        appDatabase = Room.databaseBuilder(requireActivity().getApplicationContext(),
                AppDatabase.class, AppDatabase.DB_NAME).build();
        navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);
        layoutInflater = (LayoutInflater) requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();

        Button button = requireActivity().findViewById(R.id.add_new_note_btn);
        button.setOnClickListener(view -> {
            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);

            navController.navigate(R.id.navigation_add_note);
        });

        Disposable disposable = appDatabase.noteDao().getAllNotes()
                .subscribeOn(Schedulers.io())
                .subscribe(this::loadData);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void handleClick(Uri uri, Note note){
        Bundle bundle = new Bundle();
        bundle.putString(ViewMediaFragment.URI, uri.toString());
        bundle.putInt(ViewMediaFragment.ID, note.getId());

        navController.navigate(R.id.navigation_view_media, bundle);
    }

    private void loadData(List<Note> noteList){
        for(Note note: noteList){
            try {
                JSONArray urls = new JSONArray(note.getRelated());

                for (int i = 0; i < urls.length(); i++) {
                    final ContentResolver contentResolver = requireActivity().getContentResolver();
                    final Uri uri = Uri.parse(urls.get(i).toString());
                    String[] proj = new String[]{MediaStore.Images.ImageColumns.TITLE};
                    Cursor cursor = contentResolver.query(uri, proj, null, null, null);
                    View view = null;

                    if(cursor.moveToNext()){
                        int titleIdx =
                                cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.TITLE);
                        String title = cursor.getString(titleIdx);

                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), uri);
                        view = layoutInflater.inflate(R.layout.notification_image, binding.images, false);

                        ImageView imageView = view.findViewById(R.id.image);
                        imageView.setImageBitmap(bitmap);

                        Log.i(TAG, "The image is found");
                    } else {
                        proj = new String[]{MediaStore.Video.VideoColumns.TITLE};
                        cursor = contentResolver.query(uri, proj, null, null, null);

                        if(cursor.moveToNext()){
                            view = layoutInflater.inflate(R.layout.notification_video, binding.images, true);

                            VideoView videoView = view.findViewById(R.id.video);
                            videoView.setVideoURI(uri);

                            Log.i(TAG, "The video is found");
                        }
                    }


                    final View finalView = view;

                    if(finalView != null){
                        requireView().post(() -> {
                            Button viewMedia = finalView.findViewById(R.id.view_media);
                            Button viewNote = finalView.findViewById(R.id.view_note);

                            viewMedia.setOnClickListener((v) -> handleClick(uri, note));
                            viewNote.setOnClickListener((v) -> {
                                Bundle bundle = new Bundle();
                                bundle.putInt(ViewNoteFragment.KEY, note.getId());

                                navController.navigate(R.id.navigation_view_note, bundle);
                            });

                            binding.images.addView(finalView);
                            binding.images.requestLayout();
                            binding.images.invalidate();

                            noResults.set(false);
                            Log.i(TAG, "The view is added");
                        });
                    } else {
                        Toast.makeText(requireContext(), "The media is not founded", Toast.LENGTH_LONG)
                                .show();
                    }

                    cursor.close();
                }
            } catch (JSONException | IOException e) {
                e.printStackTrace();
            }
        }
    }
}