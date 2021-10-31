package com.example.note.ui.home;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.room.Room;
import com.example.note.R;
import com.example.note.database.AppDatabase;
import com.example.note.databinding.FragmentHomeBinding;
import com.example.note.models.Note;
import com.example.note.ui.view_note.ViewNoteFragment;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;

public class HomeFragment extends Fragment {
    private FragmentHomeBinding binding;
    private AppDatabase appDatabase;
    private final ArrayList<Note> noteArrayList = new ArrayList<>();
    private LinearLayout linearLayout;
    public final ObservableBoolean noResults = new ObservableBoolean();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        binding.setNoResults(noResults);

        appDatabase = Room.databaseBuilder(requireActivity().getApplicationContext(),
                AppDatabase.class, AppDatabase.DB_NAME).build();
        linearLayout = binding.list;

        noResults.set(true);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        AsyncTask.execute(() -> {
            for(Note note: appDatabase.noteDao().getAll()){
                if(note.isPinned()){
                    noteArrayList.add(note);
                }
            }

            if(noteArrayList.size() > 0){
               addViews();
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
        for(Note note: noteArrayList){
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
}