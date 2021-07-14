package com.karmaflux.minesweeper;

import android.content.SharedPreferences;

class Preferences {
    private int sizeX;
    private int sizeY;
    private float difficulty;
    private boolean freeDig = true;
    private final SharedPreferences sharedPreferences;

    Preferences(SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
    }

    int getSizeX() {
        return sizeX;
    }

    void setSizeX(int sizeX) {
        this.sizeX = sizeX;
    }

    int getSizeY() {
        return sizeY;
    }

    void setSizeY(int sizeY) {
        this.sizeY = sizeY;
    }

    float getDifficulty() {
        return difficulty;
    }

    void setDifficulty(float difficulty) {
        this.difficulty = difficulty;
    }

    boolean isFreeDig() {
        return freeDig;
    }

    void setFreeDig(boolean freeDig) {
        this.freeDig = freeDig;
    }

    void save() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("sizeX", sizeX);
        editor.putInt("sizeY", sizeY);
        editor.putFloat("difficulty", difficulty);
        editor.putBoolean("freeDig", freeDig);
        editor.apply();
    }

    void restore() {
        sizeX = sharedPreferences.getInt("sizeX", 9);
        sizeY = sharedPreferences.getInt("sizeY", 9);
        difficulty = sharedPreferences.getFloat("difficulty", 0.15f);
        freeDig = sharedPreferences.getBoolean("freeDig", true);
    }
}
