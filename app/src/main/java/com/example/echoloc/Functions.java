package com.example.echoloc;

import android.content.Context;
import android.graphics.drawable.Animatable2;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

/**
 * Fonctions uselles
 */
public abstract class Functions {
    /**
     * Application en boucle d'une animation sur le logo pour un temps de chargement
     * @param i logo
     * @param d animation
     */
    public static void applyLoopAnimation(ImageView i, Drawable d){
        i.setImageDrawable(d);
        d = i.getDrawable();
        if (d instanceof AnimatedVectorDrawable) {
            AnimatedVectorDrawable animation = (AnimatedVectorDrawable) d;
            animation.registerAnimationCallback(new Animatable2.AnimationCallback() {
                @Override
                public void onAnimationEnd(Drawable drawable) {
                    animation.start();
                }
            });
            animation.start();
        }
    }

    /**
     * Obtention de distance (en mètre) entre 2 points
     * @param lat1 lat pt1
     * @param lon1 lon pt1
     * @param lat2 lat pt2
     * @param lon2 lon pt2
     * @return distance en mètre
     */
    public static double getDistance(double lat1, double lon1, double lat2, double lon2){
        double R = 6371e3; // metres
        double phi1 = lat1 * Math.PI / 180; // φ, λ in radians
        double phi2 = lat2 * Math.PI / 180;
        double deltaPhi = (lat2-lat1) * Math.PI/180;
        double deltaLam = (lon2-lon1) * Math.PI/180;

        double a = Math.sin(deltaPhi/2) * Math.sin(deltaPhi/2) +
                Math.cos(phi1) * Math.cos(phi2) *
                        Math.sin(deltaLam/2) * Math.sin(deltaLam/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

        double d = R * c;
        return (int)d;
    }

    /**
     * String formatée de la distance (...km, ...m, ...cm)
     * @param context Activité de provenance
     * @param dis distance
     * @return String formatée
     */
    public static String distanceToText(Context context, double dis){
        double cm = dis*100;
        int m = (int)dis;
        int km = (int) (dis/1000);
        if (km >= 1) {
            return km + context.getString(R.string.game_final_kilometer_unit);
        } else if (m >= 1) {
            return m + context.getString(R.string.game_final_meter_unit);
        } else {
            return cm + context.getString(R.string.game_final_centimeter_unit);
        }
    }

}