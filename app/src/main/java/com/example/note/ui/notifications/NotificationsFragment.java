package com.example.note.ui.notifications;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.databinding.ObservableBoolean;
import androidx.fragment.app.Fragment;
import com.example.note.databinding.FragmentNotificationsBinding;

public class NotificationsFragment extends Fragment {
    private FragmentNotificationsBinding binding;
    public final ObservableBoolean noResults = new ObservableBoolean(true);

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentNotificationsBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}