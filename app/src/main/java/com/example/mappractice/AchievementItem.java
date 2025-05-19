package com.example.mappractice;

import android.content.Context;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;

public class AchievementItem{
    private final String name;
    private final int current;
    private final int goal;
    private final boolean unlocked;

    public AchievementItem(String name, int current, int goal, boolean unlocked) {
        this.name = name;
        this.current = current;
        this.goal = goal;
        this.unlocked = unlocked;
    }

    public String getName() { return name; }
    public int getCurrent() { return current; }
    public int getGoal() { return goal; }
    public boolean isUnlocked() { return unlocked; }
}
