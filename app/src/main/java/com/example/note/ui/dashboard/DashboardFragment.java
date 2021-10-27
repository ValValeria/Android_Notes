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
import java.util.concurrent.ConcurrentLinkedDeque;


@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class DashboardFragment extends Fragment {
    private FragmentDashboardBinding binding;
    private static final ConcurrentLinkedDeque<Note> notes = new ConcurrentLinkedDeque<>();
    private AppDatabase appDatabase;
    private LinearLayout linearLayout;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        appDatabase = Room.databaseBuilder(requireActivity().getApplicationContext(),
                AppDatabase.class, AppDatabase.DB_NAME).build();
        linearLayout = binding.list;

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                notes.addAll(appDatabase.noteDao().getAll());

                view.post(DashboardFragment.this::addViews);
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