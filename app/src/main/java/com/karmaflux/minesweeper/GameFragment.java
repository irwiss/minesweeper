package com.karmaflux.minesweeper;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

public class GameFragment extends Fragment implements SweeperGridView.IGameOverHandler {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_game, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SweeperGridView gv = requireView().findViewById(R.id.sweeperGridView);
        gv.setup((MainActivity) requireActivity(), this);

        view.findViewById(R.id.btnGameToMenu).setOnClickListener(v ->
                NavHostFragment.findNavController(GameFragment.this)
                        .popBackStack());
    }

    @Override
    public void Win() {
        NavHostFragment.findNavController(GameFragment.this)
                .navigate(R.id.action_GameFragment_to_WinFragment);
    }

    @Override
    public void Lose() {
        NavHostFragment.findNavController(GameFragment.this)
                .navigate(R.id.action_GameFragment_to_LoseFragment);
    }
}