package com.example.note.ui.dashboard;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NavUtils;
import androidx.databinding.ObservableBoolean;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.room.Room;
import com.example.note.R;
import com.example.note.database.AppDatabase;
import com.example.note.databinding.FragmentDashboardBinding;
import com.example.note.models.Note;
import com.example.note.ui.view_note.ViewNoteFragment;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;


@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class DashboardFragment extends Fragment {
    private FragmentDashboardBinding binding;
    private static final ConcurrentLinkedDeque<Note> notes = new ConcurrentLinkedDeque<>();
    private static final ConcurrentLinkedDeque<Integer> noteIds = new ConcurrentLinkedDeque<>();
    public final ObservableBoolean noResults = new ObservableBoolean(true);
    private AppDatabase appDatabase;
    private LinearLayout linearLayout;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        binding.setNoResults(noResults);

        appDatabase = Room.databaseBuilder(requireActivity().getApplicationContext(),
                AppDatabase.class, AppDatabase.DB_NAME).build();
        linearLayout = binding.list;

        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();

        loadData();
    }

    @Override
    public void onPause() {
        super.onPause();

        noResults.set(true);
        notes.clear();
        noteIds.clear();

        linearLayout.removeAllViews();
        linearLayout.requestLayout();
        linearLayout.invalidate();
    }

    private void loadData(){
        AsyncTask.execute(() -> {
            List<Note> noteList = appDatabase.noteDao().getAll();

            for(Note note: noteList){
                if(!noteIds.contains(note.getId())){
                    DashboardFragment.notes.add(note);
                    DashboardFragment.noteIds.add(note.getId());
                }
            }

            if(notes.size() > 0){
                requireView().post(DashboardFragment.this::addViews);
                noResults.set(false);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    /**
     * Adds views to fragment layout
     */
    private void addViews(){
        for(Note note: notes){
            LayoutInflater layoutInflater = (LayoutInflater) requireActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = layoutInflater.inflate(R.layout.list_item, linearLayout, false);

            TextView textView = view.findViewById(R.id.text);
            textView.setText(note.getTitle());

            Button button = view.findViewById(R.id.view_note);
            button.setOnClickListener(view1 -> {
                NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);

                Bundle bundle = new Bundle();
                bundle.putInt(ViewNoteFragment.KEY, note.getId());

                navController.navigate(R.id.navigation_view_note, bundle);
            });

            linearLayout.addView(view);
            linearLayout.invalidate();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}