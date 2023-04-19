package com.example.echoloc;

import java.util.Calendar;

/**
 * Classe permettant de créer un chronomètre
 */
public class Compteur {
    private final long currentTime;
    private long chrono;
    private long diff;
    private long stopTime;

    /**
     * Instancation
     */
    private Compteur(){
        currentTime = Calendar.getInstance().getTimeInMillis();
        chrono = 0;
        diff = 0;
        stopTime = -1;
    }

    /**
     * Demarrage d'un nouveau compteur
     * @return Compteur démarré
     */
    public static Compteur start(){
        return new Compteur();
    }

    /**
     * Obtention de la progression du chrono
     * @return progression du chrono
     */
    public long getValue(){
        if (stopTime == -1) {
            update();
        }
        return chrono;
    }

    /**
     * String formater (hh:mm:ss.millis) pour affichage du chrono
     * @param val chrono
     * @return String formater
     */
    public static String getString(long val){
        String res = "";
        long hour = val/(60*60*1000);
        long hourRest = val%(60*60*1000);
        if (hour > 9) {
            res += hour + ":";
        } else if (hour >= 0) {
            res += "0"+hour+":";
        }
        long min = hourRest/(60*1000);
        long minRest = hourRest%(60*1000);
        if (min > 9) {
            res += min + ":";
        } else if (min >= 0) {
            res += "0"+min+":";
        }
        long sec = minRest/(1000);
        long millis = minRest%(1000);
        if (sec > 9) {
            res += sec + ".";
        } else if (sec >= 0) {
            res += "0"+sec+".";
        }
        if (millis > 99) {
            res += millis;
        } else if (millis > 9) {
            res += "0"+millis;
        } else if (millis >= 0) {
            res += "00"+millis;
        }
        return res;
    }
    /**
     * String formater (hh:mm:ss) pour affichage du chrono
     * @param val chrono
     * @return String formater
     */
    public static String getStringSec(long val){
        String res = "";
        long hour = val/(60*60*1000);
        long hourRest = val%(60*60*1000);
        if (hour > 9) {
            res += hour + ":";
        } else if (hour >= 0) {
            res += "0"+hour+":";
        }
        long min = hourRest/(60*1000);
        long minRest = hourRest%(60*1000);
        if (min > 9) {
            res += min + ":";
        } else if (min >= 0) {
            res += "0"+min+":";
        }
        long sec = minRest/(1000);
        if (sec > 9) {
            res += sec;
        } else if (sec >= 0) {
            res += "0"+sec;
        }
        return res;
    }
    /**
     * String formater (mm:ss) pour affichage du chrono
     * @param val chrono
     * @return String formater
     */
    public static String getStringMinSec(long val){
        String res = "";
        long min = val/(60*1000);
        long minRest = val%(60*1000);
        if (min > 9) {
            res += min + ":";
        } else if (min >= 0) {
            res += "0"+min+":";
        }
        long sec = minRest/(1000);
        if (sec > 9) {
            res += sec;
        } else if (sec >= 0) {
            res += "0"+sec;
        }
        return res;
    }

    /***
     * Met en pause le chrono
     */
    public void stop(){
        update();
        stopTime = Calendar.getInstance().getTimeInMillis();
    }

    /**
     * Reprend le chrono
     */
    public void resume(){
        diff += (Calendar.getInstance().getTimeInMillis() - stopTime);
        stopTime = -1;
    }

    /**
     * Permet de garder le chrono à jour via des appels interne
     */
    private void update(){
        chrono = (Calendar.getInstance().getTimeInMillis() - currentTime) - diff;
    }
}
