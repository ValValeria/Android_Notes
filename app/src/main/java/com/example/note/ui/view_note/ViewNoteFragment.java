package com.example.note.ui.view_note;

import android.os.AsyncTask;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.room.Room;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.example.note.R;
import com.example.note.database.AppDatabase;
import com.example.note.databinding.FragmentViewNoteBinding;
import com.example.note.models.Note;
import com.example.note.ui.update_note.UpdateNoteDialogFragment;
import com.google.android.material.snackbar.Snackbar;
import java.util.List;
import java.util.Map;
import io.reactivex.rxjava3.subjects.PublishSubject;


public class ViewNoteFragment extends Fragment {
    public static PublishSubject<Map<String, String>> changes = PublishSubject.create();
    public final static String KEY = "id";
    private int id;
    private AppDatabase appDatabase;
    private FragmentViewNoteBinding fragmentViewNoteBinding;
    private NavController navController;

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

        LoadingTask loadingTask = new LoadingTask();
        loadingTask.execute();
    }

    private class LoadingTask extends AsyncTask<Void, Void, Note>{

        @Override
        protected Note doInBackground(Void... voids) {
            int id = ViewNoteFragment.this.id;
            List<Note> list = ViewNoteFragment.this.appDatabase.noteDao().loadAllByIds(new int[]{id});

            if(list.size() > 0){
               return list.get(list.size() - 1);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Note note) {
            super.onPostExecute(note);

            FragmentViewNoteBinding fragmentViewNoteBinding = ViewNoteFragment.this.fragmentViewNoteBinding;

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
        }
    }
}