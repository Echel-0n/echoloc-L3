package com.example.echoloc;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;

public abstract class GameMode {
    public static final int MAP_FINDING = 0;
    public static final int BUTTON_FINDING = 1;
    public static final int TEXT_FINDING = 2;

    public static class MapFinding extends GameMode{
        public GoogleMap map;
        public Marker marker = null;
        public LatLng startPosition;
        public float startZoom;
        public double mapFindingMaxDistanceForWinning;
        public MapFinding(LatLng startPosition, float startZoom,
                          long mapFindingMaxDistanceForWinning){
            this.startPosition = startPosition;
            this.startZoom = startZoom;
            this.mapFindingMaxDistanceForWinning = mapFindingMaxDistanceForWinning;
        }
    }

    public static class ButtonFinding extends GameMode{
        public int finalZoom;
        public int numberOfCities;
        public ArrayList<ButtonCity> cities;
        public ArrayList<EchoButton> buttons;

        public ButtonFinding(int numberOfCities, int finalZoom, ArrayList<ButtonCity> cities){
            this.numberOfCities = numberOfCities;
            this.cities = cities;
            this.buttons = new ArrayList<>();
            this.finalZoom = finalZoom;
        }

        public static class ButtonCity{
            public String name;
            public LatLng coord;
            public boolean goodAnswer;
            public ButtonCity(String name, LatLng coord, boolean goodAnswer){
                this.name = name;
                this.coord = coord;
                this.goodAnswer = goodAnswer;
            }
        }
    }

    public static class TextFinding extends GameMode{
        public int finalZoom;
        public TextFinding(int finalZoom){
            this.finalZoom = finalZoom;
        }
    }
}
