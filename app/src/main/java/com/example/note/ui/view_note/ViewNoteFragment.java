package com.example.note.ui.view_note;

import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.databinding.ObservableBoolean;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.room.Room;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.VideoView;

import com.example.note.R;
import com.example.note.database.AppDatabase;
import com.example.note.databinding.FragmentViewNoteBinding;
import com.example.note.models.Note;
import com.example.note.ui.update_note.UpdateNoteDialogFragment;
import com.example.note.ui.view_media.ViewMediaFragment;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import io.reactivex.rxjava3.subjects.PublishSubject;


public class ViewNoteFragment extends Fragment {
    private final String TAG = ViewNoteFragment.class.getName();
    public static PublishSubject<Map<String, String>> changes = PublishSubject.create();
    public final ObservableBoolean hasImages = new ObservableBoolean(false);
    private LayoutInflater layoutInflater;
    public final static String KEY = "id";
    private int id;
    private AppDatabase appDatabase;
    private FragmentViewNoteBinding fragmentViewNoteBinding;
    private NavController navController;
    private Note note;

    public ViewNoteFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        id = requireArguments().getInt(KEY);
        appDatabase = Room.databaseBuilder(requireActivity().getApplicationContext(),
                AppDatabase.class, AppDatabase.DB_NAME).build();
        navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);
        layoutInflater = requireActivity().getLayoutInflater();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        fragmentViewNoteBinding = FragmentViewNoteBinding.inflate(inflater);

        return fragmentViewNoteBinding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();

        AsyncTask.execute(() -> {
            int id = ViewNoteFragment.this.id;
            List<Note> list = ViewNoteFragment.this.appDatabase.noteDao().loadAllByIds(new int[]{id});
            note = list.get(list.size() - 1);

            if(note != null){
                requireActivity().runOnUiThread(() -> {
                    setupData(note);
                });
            }
        });
    }

    private void setupData(Note note){
        fragmentViewNoteBinding.title.setText(note.getTitle());
        fragmentViewNoteBinding.description.setText(note.getContent());
        fragmentViewNoteBinding.deleteBtn.setOnClickListener(view -> {
            AsyncTask.execute(() -> {
                ViewNoteFragment.this.appDatabase.noteDao().delete(note);
            });

            Snackbar snackbar = Snackbar.make(view, "Note is deleted!", Snackbar.LENGTH_SHORT);
            snackbar.show();

            navController.navigate(R.id.navigation_dashboard);
        });
        fragmentViewNoteBinding.updateBtn.setOnClickListener(view -> {
            UpdateNoteDialogFragment updateNoteDialogFragment = new UpdateNoteDialogFragment();
            updateNoteDialogFragment.show(getParentFragmentManager(), "");

            changes.subscribe(v -> {
                String title = v.get("title");
                String description = v.get("description");

                AsyncTask.execute(() -> {
                    appDatabase.noteDao().delete(note);

                    note.setTitle(title);
                    note.setContent(description);

                    appDatabase.noteDao().insertAll(note);

                    Toast.makeText(requireContext(), "The changes is saved", Toast.LENGTH_LONG)
                            .show();
                });
            });
        });

        requireActivity().runOnUiThread(this::loadMedia);
    }

    private void loadMedia(){
        try {
            JSONArray urls = new JSONArray(note.getRelated());

            for (int i = 0; i < urls.length(); i++) {
                final ContentResolver contentResolver = requireActivity().getContentResolver();
                final Uri uri = Uri.parse(urls.get(i).toString());
                String[] proj = new String[]{MediaStore.Images.ImageColumns.TITLE};
                Cursor cursor = contentResolver.query(uri, proj, null, null, null);
                View view = null;

                if(cursor.moveToNext()){
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), uri);
                    view = layoutInflater.inflate(R.layout.image, fragmentViewNoteBinding.imagesList, false);

                    ImageView imageView = view.findViewById(R.id.image);
                    imageView.setImageBitmap(bitmap);

                    Log.i(TAG, "The image is found");
                } else {
                    proj = new String[]{MediaStore.Video.VideoColumns.TITLE};
                    cursor = contentResolver.query(uri, proj, null, null, null);

                    if(cursor.moveToNext()){
                        view = layoutInflater.inflate(R.layout.video, fragmentViewNoteBinding.imagesList, true);

                        VideoView videoView = view.findViewById(R.id.video);
                        videoView.setVideoURI(uri);

                        Log.i(TAG, "The video is found");
                    }
                }


                final View finalView = view;
                final String uriStr = uri.toString();

                if(finalView != null){
                    Button viewNote = finalView.findViewById(R.id.delete_btn);
                    viewNote.setVisibility(View.GONE);

                    finalView.setOnClickListener((v) -> {
                        Bundle bundle = new Bundle();
                        bundle.putString(ViewMediaFragment.URI, uriStr);
                        bundle.putInt(ViewMediaFragment.ID, note.getId());

                        navController.navigate(R.id.navigation_view_media, bundle);
                    });

                    hasImages.set(true);

                    fragmentViewNoteBinding.imagesList.addView(finalView);
                    fragmentViewNoteBinding.imagesList.requestLayout();
                    fragmentViewNoteBinding.imagesList.invalidate();
                    fragmentViewNoteBinding.card.setVisibility(View.VISIBLE);

                    Log.i(TAG, "The view is added");
                } else {
                    Toast.makeText(requireContext(), "The media is not founded", Toast.LENGTH_LONG)
                            .show();
                    fragmentViewNoteBinding.card.setVisibility(View.GONE);
                }

                cursor.close();
            }
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
    }
}