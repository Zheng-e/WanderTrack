package com.example.mappractice;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.res.Resources;
import android.os.Build;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

public class UIAnimationTool {

    public static void dialogFadeIn(View rootView, long duration){
        int screenHeight = Resources.getSystem().getDisplayMetrics().heightPixels;
        float position = (float) (screenHeight*0.05);
        rootView.setTranslationY(position);
        rootView.setAlpha(0f);

        rootView.animate()
                .translationY(0f)
                .alpha(1f)
                .setDuration(duration)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(@NonNull Animator animation, boolean isReverse) {
//                        super.onAnimationStart(animation, isReverse);
                        rootView.setVisibility(View.VISIBLE);
                    }
                })
                .start();
    }

    public static void dialogFadeOut(View rootView, long duration, AlertDialog dialog){
        System.out.println("running dialogFadeOut");
        rootView.animate().alpha(0f).setDuration(duration)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation){
                        dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                    }

                    @Override
                    public void onAnimationEnd(@NonNull Animator animation, boolean isReverse) {
                        System.out.println("trying to dismiss");
                        rootView.setVisibility(View.GONE);
                        dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
//                        dialog.dismiss();
//                        dialog.runOnUiThread

                        System.out.println("dismiss succeed");
                    }
                }).start();
    }
}
