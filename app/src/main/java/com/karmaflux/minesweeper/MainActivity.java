package com.karmaflux.minesweeper;

import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private SoundPool soundPool;
    private int shovelSound;
    private int explosionSound;
    private int winSound;
    private Preferences preferences;

    Preferences getPreferences() {
        return preferences;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = new Preferences(getSharedPreferences("main", MODE_PRIVATE));
        preferences.restore();
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onPause() {
        super.onPause();
        soundPool.release();
        shovelSound = 0;
        explosionSound = 0;
        winSound = 0;
        soundPool = null;

        preferences.save();
    }

    @Override
    protected void onResume() {
        super.onResume();

        soundPool = new SoundPool.Builder()
                .setMaxStreams(4)
                .setAudioAttributes(new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME).build())
                .build();

        shovelSound = soundPool.load(this, R.raw.shovel, 1);
        explosionSound = soundPool.load(this, R.raw.explosion, 1);
        winSound = soundPool.load(this, R.raw.win, 1);

        preferences.restore();
    }

    private void playSound(int soundId) {
        if (soundPool != null) {
            soundPool.play(soundId, 1.0f, 1.0f, 0, 0, 1.0f);
        }
    }

    void playShovelSound() {
        playSound(shovelSound);
    }

    void playExplosionSound() {
        playSound(explosionSound);
    }

    void playWinSound() {
        playSound(winSound);
    }
}
