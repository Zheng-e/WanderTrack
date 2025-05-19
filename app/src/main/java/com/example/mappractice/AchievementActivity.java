package com.example.mappractice;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class AchievementActivity extends AppCompatActivity {

    private LinearLayout achievementContainer;
    private AchievementTracker tracker;
    private AchievementChecker checker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_achievement);

        achievementContainer = findViewById(R.id.achievement_container);
        tracker = new AchievementTracker(this);
        checker = new AchievementChecker(tracker);

        checker.checkAllAchievements();

        displayAchievements();
    }

    private void displayAchievements() {
        List<AchievementItem> achievements = generateAchievementList();

        for (AchievementItem item : achievements) {
            LinearLayout itemLayout = new LinearLayout(this);
            itemLayout.setOrientation(LinearLayout.VERTICAL);
            itemLayout.setPadding(16, 16, 16, 16);

            TextView titleView = new TextView(this);
            titleView.setText(item.getName() + (item.isUnlocked() ? " ✅" : ""));
            titleView.setTextSize(18f);

            ProgressBar progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
            progressBar.setMax(item.getGoal());
            progressBar.setProgress(item.getCurrent());

            TextView progressText = new TextView(this);
            progressText.setText(item.getCurrent() + " / " + item.getGoal());

            itemLayout.addView(titleView);
            itemLayout.addView(progressBar);
            itemLayout.addView(progressText);

            achievementContainer.addView(itemLayout);
        }
    }

    private List<AchievementItem> generateAchievementList() {
        List<AchievementItem> list = new ArrayList<>();

        // 等级成就
        int level = tracker.getUserCurrentLevel();
        int cityCount = tracker.getCityCount();
        int provinceCount = tracker.getProvinceCount();
        int countryCount = tracker.getCountryCount();
        int continentCount = tracker.getContinentCount();

        list.add(new AchievementItem("新手旅行者", cityCount, 1, level >= 1));
        list.add(new AchievementItem("城市探索者", cityCount, 10, level >= 2));
        list.add(new AchievementItem("省级达人", provinceCount, 5, level >= 3));
        list.add(new AchievementItem("国家旅行者", cityCount, 30, level >= 4));
        list.add(new AchievementItem("国际开拓者", countryCount, 5, level >= 5));
        list.add(new AchievementItem("大洲开拓者", continentCount, 2, level >= 6));
        list.add(new AchievementItem("环球大师", countryCount, 50, level >= 7)); //  continentCount >= 5 也算条件

        // 夜间访问成就
        int nightVisit = tracker.getNightVisitCount();
        boolean nightUnlocked = tracker.isTitleUnlocked("night_visitor");
        list.add(new AchievementItem("夜访者", nightVisit, 10, nightUnlocked));

        // 极地探险家
        boolean polarVisited = tracker.checkPolarRegionVisit();
        list.add(new AchievementItem("极地探险家", polarVisited ? 1 : 0, 1, polarVisited));

        // 大洲成就
        list.add(makeContinentAchievement("Asia", 15, "亚洲行者"));
        list.add(makeContinentAchievement("Europe", 10, "欧洲收藏家"));
        list.add(makeContinentAchievement("Africa", 5, "非洲探险家"));
        list.add(makeContinentAchievement("South America", 8, "南美秘境客"));
        list.add(makeContinentAchievement("Oceania", 3, "大洋洲航海家"));

        return list;
    }

    private AchievementItem makeContinentAchievement(String continent, int reqCount, String title) {
        int visited = tracker.getCityCountByContinent(continent);
        boolean unlocked = tracker.isTitleUnlocked(continent.toLowerCase() + "_explorer");
        return new AchievementItem(title, visited, reqCount, unlocked);
    }
}
