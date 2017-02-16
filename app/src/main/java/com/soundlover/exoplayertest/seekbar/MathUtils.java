package com.soundlover.exoplayertest.seekbar;

import android.view.MotionEvent;

/**
 * Created by Sergiy on 12.12.2016.
 */

public class MathUtils {

    private static final String TAG = MathUtils.class.getSimpleName();

    public static double calcDistance(float currentX, float currentY, float initX, float initY) {
        return Math.sqrt(Math.pow(currentX - initX, 2) + Math.pow(currentY - initY, 2));
    }

    public static boolean hasTouchInCircle(MotionEvent event, float x, float y, float radius) {
        double dx = Math.pow(event.getX() - x, 2);
        double dy = Math.pow(event.getY() - y, 2);

        if ((dx + dy) < Math.pow(radius, 2)) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean hasTouchInRectangle(MotionEvent event, float x, float y, float width, float height){
        return event.getX() >= x && event.getX() <= x + width
                && event.getY() <= height ;
    }

}
