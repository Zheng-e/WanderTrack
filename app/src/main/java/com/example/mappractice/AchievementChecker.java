package com.example.mappractice;

import android.database.Cursor;
import net.sqlcipher.database.SQLiteDatabase;
import android.util.Log;


// 实现等级、称号操作
public class AchievementChecker {
    private final AchievementTracker tracker;
    private static final String CURRENT_USER_ID = "test_user_001";

    public AchievementChecker(AchievementTracker tracker) {
        this.tracker = tracker;
    }

    public void checkAllAchievements() {
        checkLevelAchievements();
        checkNightAchievements();
        checkPolarAchievement();
        checkContinentAchievements();
    }

    // ==========等级成就检测==========
    private void checkLevelAchievements() {

        int cityCount = tracker.getCityCount();
        int currentLevel = tracker.getUserCurrentLevel();
        int provinceCount = tracker.getProvinceCount();
        int countryCount = tracker.getCountryCount();
        int continentCount = tracker.getContinentCount();
        int internationalCity = tracker.getInternationalCityCount("China"); // 此处默认本土国家为china


        if (cityCount >= 1 && currentLevel == 0) {
            tracker.updateUserLevel(1, "新手旅行者");
        }
        if (cityCount >= 10 && currentLevel == 1) {
            tracker.updateUserLevel(2, "城市探索者");
        }
        if (provinceCount >= 5 && currentLevel == 2) {
            tracker.updateUserLevel(3, "省级达人");
        }
        if (cityCount >= 30 && currentLevel == 3) {
            tracker.updateUserLevel(4, "国家旅行者");
        }
        if (countryCount >= 5 && currentLevel == 4) {
            tracker.updateUserLevel(5, "国际开拓者");
        }
        if (continentCount >= 2 && currentLevel == 5) {
            tracker.updateUserLevel(6, "大洲开拓者");
        }
        if (countryCount >= 50 && continentCount >= 5 && currentLevel == 6) {
            tracker.updateUserLevel(7, "环球大师");
        }
    }

    // ==========夜间访问成就==========
    private void checkNightAchievements() {
        int nightVisitCount = tracker.getNightVisitCount();
        if (nightVisitCount >= 10) {
            tracker.unlockTitle("night_visitor", "夜访者");
        }
    }

    // ==========极地成就==========
    private void checkPolarAchievement() {
        if (tracker.checkPolarRegionVisit()) {
            tracker.unlockTitle("polar_explorer", "极地探险家");
        }
    }

    // ==========大洲成就检测==========
    private void checkContinentAchievements() {
        checkContinentThreshold("Asia", 15, "亚洲行者");
        checkContinentThreshold("Europe", 10, "欧洲收藏家");
        checkContinentThreshold("Africa", 5, "非洲探险家");
        checkContinentThreshold("South America", 8, "南美秘境客");
        checkContinentThreshold("Oceania", 3, "大洋洲航海家");
        //不清楚这里参数为什么这么配，为编译通过暂且注释
//        checkContinentThreshold(7, "七洲行者");
    }

    private void checkContinentThreshold(String continent, int reqCount, String titleName) {
        int cityCount = tracker.getCityCountByContinent(continent);
        if (cityCount >= reqCount) {
            tracker.unlockTitle(continent.toLowerCase() + "_explorer", titleName);
        }
    }

    private void checkContinentNum(int reqCount, String titleName) {
        int continentCount = tracker.getContinentCount();
        if (continentCount == reqCount) {
            tracker.unlockTitle("all_continent_explorer", titleName);
        }
    }

}