package com.example.echoloc;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class GameSettings implements Parcelable {
    private int gameMode;
    private String language;
    private int minZoom;
    private String country;
    private int intoMostPopulate;
    private int numberOfButtons;
    private long maxDistanceForWinning;

    public GameSettings(int gameMode, String language, int minZoom,
                    String country, int intoMostPopulate, int numberOfButtons,
                        long maxDistanceForWinning) {
        this.gameMode = gameMode;
        this.language = language;
        this.minZoom = minZoom;
        this.country = country;
        this.intoMostPopulate = intoMostPopulate;
        this.numberOfButtons = numberOfButtons;
        this.maxDistanceForWinning = maxDistanceForWinning;
    }


    protected GameSettings(Parcel in) {
        gameMode = in.readInt();
        language = in.readString();
        minZoom = in.readInt();
        country = in.readString();
        intoMostPopulate = in.readInt();
        numberOfButtons = in.readInt();
        maxDistanceForWinning = in.readLong();
    }

    public static final Creator<GameSettings> CREATOR = new Creator<GameSettings>() {
        @Override
        public GameSettings createFromParcel(Parcel in) {
            return new GameSettings(in);
        }

        @Override
        public GameSettings[] newArray(int size) {
            return new GameSettings[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeInt(gameMode);
        dest.writeString(language);
        dest.writeInt(minZoom);
        dest.writeString(country);
        dest.writeInt(intoMostPopulate);
        dest.writeInt(numberOfButtons);
        dest.writeDouble(maxDistanceForWinning);
    }

    public int getGameMode() {
        return gameMode;
    }

    public void setGameMode(int gameMode) {
        this.gameMode = gameMode;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public int getIntoMostPopulate() {
        return intoMostPopulate;
    }

    public void setIntoMostPopulate(int intoMostPopulate) {
        this.intoMostPopulate = intoMostPopulate;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public int getNumberOfButtons() {
        return numberOfButtons;
    }

    public void setNumberOfButtons(int numberOfButtons) {
        this.numberOfButtons = numberOfButtons;
    }

    public int getMinZoom() {
        return minZoom;
    }

    public void setMinZoom(int minZoom) {
        this.minZoom = minZoom;
    }

    public long getMaxDistanceForWinning() {
        return maxDistanceForWinning;
    }

    public void setMaxDistanceForWinning(long maxDistanceForWinning) {
        this.maxDistanceForWinning = maxDistanceForWinning;
    }

    @Override
    public String toString() {
        return "GameSettings{" +
                "gameMode=" + gameMode +
                ", language='" + language + '\'' +
                ", minZoom=" + minZoom +
                ", country='" + country + '\'' +
                ", intoMostPopulate=" + intoMostPopulate +
                ", numberOfButtons=" + numberOfButtons +
                ", maxDistanceForWinning=" + maxDistanceForWinning +
                '}';
    }
}
