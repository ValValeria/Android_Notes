package com.example.note.ui.update_note;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.example.note.R;
import com.example.note.ui.view_note.ViewNoteFragment;
import com.google.android.material.textfield.TextInputEditText;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class UpdateNoteDialogFragment extends DialogFragment {
    @NonNull
    @NotNull
    @Override
    public Dialog onCreateDialog(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        LayoutInflater layoutInflater = getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.dialog_change_note, null, false);
        TextInputEditText titleInputEditText = view.findViewById(R.id.titleInput);
        TextInputEditText descriptionInputEditText = view.findViewById(R.id.descrInput);

        AlertDialog.Builder dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Change the note")
                .setView(view)
                .setPositiveButton("Save", (dialogInterface, i) -> {
                    String title = titleInputEditText.getText().toString();
                    String description = descriptionInputEditText.getText().toString();

                    final HashMap<String, String> hashMap = new HashMap<>();
                    hashMap.put("title", title);
                    hashMap.put("description", description);

                    ViewNoteFragment.changes.onNext(hashMap);
                })
                .setNegativeButton("Undo", (dialogInterface, i) -> {
                    this.dismiss();
                });

        return dialog.create();
    }
}
