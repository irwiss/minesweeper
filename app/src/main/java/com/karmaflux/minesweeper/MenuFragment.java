package com.karmaflux.minesweeper;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

public class MenuFragment extends Fragment {
    private TextView tvDifficulty;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_menu, container, false);
    }

    private void updateTvDifficulty() {
        Preferences prefs = ((MainActivity) requireActivity()).getPreferences();
        int bombsAmount = MineBoard.estimateBombs(prefs.getSizeX(), prefs.getSizeY(), prefs.getDifficulty());
        tvDifficulty.setText(getString(R.string.difficulty, bombsAmount));
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Preferences prefs = ((MainActivity) requireActivity()).getPreferences();

        EditText etSizeX = view.findViewById(R.id.fieldX);
        EditText etSizeY = view.findViewById(R.id.fieldY);
        SeekBar skDifficulty = view.findViewById(R.id.seekBar);
        CheckBox cbFreeDig = view.findViewById(R.id.checkBoxFreeDig);
        TextView tvLargeWarning = view.findViewById(R.id.tvLargeBoardWarning);
        tvDifficulty = view.findViewById(R.id.textViewDifficulty);
        updateTvDifficulty();

        cbFreeDig.setChecked(prefs.isFreeDig());

        tvLargeWarning.setVisibility(Math.max(prefs.getSizeX(), prefs.getSizeY()) > 16 ? View.VISIBLE : View.INVISIBLE);

        etSizeX.setText(String.valueOf(prefs.getSizeX()));
        etSizeY.setText(String.valueOf(prefs.getSizeY()));

        skDifficulty.setProgress((int) (100f * prefs.getDifficulty()));
        skDifficulty.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    prefs.setDifficulty((float) progress / seekBar.getMax());
                    updateTvDifficulty();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        View.OnFocusChangeListener onFocusChangeListener = (v, hasFocus) -> {
            if (hasFocus)
                return;
            etSizeX.setText(String.valueOf(prefs.getSizeX()));
            etSizeY.setText(String.valueOf(prefs.getSizeY()));
            final boolean isLargeBoard = Math.max(prefs.getSizeX(), prefs.getSizeY()) > 16;
            tvLargeWarning.setVisibility(isLargeBoard ? View.VISIBLE : View.INVISIBLE);
        };
        etSizeX.setOnFocusChangeListener(onFocusChangeListener);
        etSizeY.setOnFocusChangeListener(onFocusChangeListener);

        etSizeX.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                try {
                    prefs.setSizeX(Math.max(4, Math.min(48, Math.max(Integer.parseInt(etSizeX.getText().toString()), 2))));
                    updateTvDifficulty();
                } catch (Exception ignored) {
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });

        etSizeY.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                try {
                    prefs.setSizeY(Math.max(4, Math.min(48, Math.max(Integer.parseInt(etSizeY.getText().toString()), 2))));
                    updateTvDifficulty();
                } catch (Exception ignored) {
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });

        etSizeY.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE || event != null &&
                    event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                if (event == null || !event.isShiftPressed()) {
                    etSizeY.clearFocus();

                    return false; // consume event
                }
            }
            return false; // pass on
        });

        cbFreeDig.setOnCheckedChangeListener((buttonView, isChecked) -> prefs.setFreeDig(isChecked));

        view.findViewById(R.id.button_play).setOnClickListener(v -> {
            prefs.save();
            NavHostFragment.findNavController(MenuFragment.this)
                    .navigate(R.id.action_MenuFragment_to_GameFragment);
        });
    }
}