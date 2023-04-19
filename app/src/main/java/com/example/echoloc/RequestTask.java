package com.example.echoloc;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;

public class RequestTask extends AsyncTask<String, Void, String> {
    @SuppressLint("StaticFieldLeak")
    private Context context;
    private int gameMode;
    private String country;
    private String language;
    private int intoMostPopulate;
    private int numberOfButtons;
    private long maxDistanceForWinning;
    private int minZoom;
    private GameSettings gameSettings;
    private boolean isRequeteFailed;
    private GameException requeteFailException;

    public RequestTask(){ super(); }

    public void create(Context context, GameSettings gameSettings){
        this.gameSettings = gameSettings;
        this.context = context;
        this.gameMode = gameSettings.getGameMode();
        this.language = gameSettings.getLanguage();
        this.minZoom = gameSettings.getMinZoom();
        this.country = gameSettings.getCountry();
        this.intoMostPopulate = gameSettings.getIntoMostPopulate();
        this.numberOfButtons = gameSettings.getNumberOfButtons();
        this.maxDistanceForWinning = gameSettings.getMaxDistanceForWinning();
        this.isRequeteFailed = false;
    }

    // Le corps de la tâche asynchrone (exécuté en tâche de fond)
    //  lance la requète
    protected String doInBackground(String... args) {
        return requete();
    }
    private String requete() {
        String response = null;
        try {
            HttpURLConnection connection;
            String strURL = "https://wft-geo-db.p.rapidapi.com/v1/geo/cities" +
                    "?types=CITY" +
                    "&sort=-population";

            strURL += "&languageCode="+language;

            if (gameMode == GameMode.BUTTON_FINDING){
                strURL += "&limit="+GeoDBAPI.MAX_CITIES_PER_REQUEST;
            }

            if (country != null && !country.equals("")) {
                strURL += "&countryIds=" + country;
            }

            if (intoMostPopulate > 0){
                int i = -1;
                Random r = new Random();
                while (i == -1) {
                    i = r.nextInt(intoMostPopulate);
                }
                strURL += "&offset="+i;
            }

            URL url = new URL(strURL);

            try {
                connection = (HttpURLConnection) url.openConnection();
            } catch (IOException e) {
                isRequeteFailed = true;
                requeteFailException= new GameException(GameException.CONNECTION_ERROR,
                        context.getString(R.string.error_connection));
                return null;
            }

            connection.setRequestMethod("GET");
            connection.setRequestProperty("X-RapidAPI-Key", GeoDBAPI.KEY);
            connection.setRequestProperty("X-RapidAPI-Host", GeoDBAPI.HOST);

            InputStream inputStream;
            try {
                inputStream = connection.getInputStream();
            } catch (IOException e) {
                inputStream = connection.getErrorStream();                if (inputStream == null){
                    isRequeteFailed = true;
                    requeteFailException= new GameException(GameException.CONNECTION_ERROR,
                            context.getString(R.string.error_connection));
                    return null;
                }
            }
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String ligne;
            try {
                ligne = bufferedReader.readLine();
                StringBuilder responseBuilder = new StringBuilder();
                while (ligne!= null){
                    responseBuilder.append(ligne);
                    ligne = bufferedReader.readLine();
                }
                response = responseBuilder.toString();
            } catch (IOException e) {
                isRequeteFailed = true;
                requeteFailException= new GameException(GameException.DATA_READ_ERROR,
                        context.getString(R.string.error_data_read));
            }
        } catch (MalformedURLException e) {
            isRequeteFailed = true;
            requeteFailException= new GameException(GameException.URL_ERROR,
                    context.getString(R.string.error_url));
        } catch (ProtocolException e) {
            isRequeteFailed = true;
            requeteFailException= new GameException(GameException.PROTOCOL_ERROR,
                    context.getString(R.string.error_protocol));
        }
        return response;
    }
    private static class InfoGameMode{
        public String name;
        public double pointLat, pointLon;
        public ArrayList<GameMode.ButtonFinding.ButtonCity> bc = new ArrayList<>();
    }
    private InfoGameMode decodeJSON(String response) throws GameException {
        InfoGameMode igm = new InfoGameMode();

        JSONArray a;
        try {
            JSONObject jso = new JSONObject(response);
            a = jso.getJSONArray("data");
            if (a.length() == 0) { // Pas de données - OffSet donner supérieur au nombre de villes
                throw new GameException(GameException.OUT_OF_CITY_RANGE,
                        context.getString(R.string.error_city_range),
                        jso.getJSONObject("metadata")
                                .getInt("totalCount"));
            } else {
                switch (gameMode) {
                    case GameMode.MAP_FINDING:
                    case GameMode.TEXT_FINDING:
                        JSONObject city = a.getJSONObject(0);
                        igm.pointLat = city.getDouble("latitude");
                        igm.pointLon = city.getDouble("longitude");
                        igm.name = city.getString("name");
                        break;
                    case GameMode.BUTTON_FINDING:
                        int[] selected = new int[numberOfButtons];
                        for (int i=0; i<numberOfButtons; i++){
                            selected[i] = -1;
                        }

                        Random r = new Random();
                        int goodOne = r.nextInt(a.length());
                        selected[0] = goodOne;

                        for (int i=1; i<selected.length; i++){
                            int newOne = -1;
                            boolean selectedContainNewOne = true;
                            while (selectedContainNewOne){
                                selectedContainNewOne = false;
                                newOne = r.nextInt(a.length());
                                for (int j : selected){
                                    if (j == newOne) {
                                        selectedContainNewOne = true;
                                        break;
                                    }
                                }
                            }
                            selected[i] = newOne;
                        }

                        for (int i : selected) {
                            JSONObject o = a.getJSONObject(i);
                            if (i == goodOne) {
                                igm.pointLat = o.getDouble("latitude");
                                igm.pointLon = o.getDouble("longitude");
                                igm.name = o.getString("name");
                            }
                            igm.bc.add(new GameMode.ButtonFinding.ButtonCity(
                                    o.getString("name"),
                                    new LatLng(
                                            o.getDouble("latitude"),
                                            o.getDouble("longitude")
                                    ),
                                    i == goodOne
                            ));
                        }
                        break;
                }
            }
        } catch (JSONException e) {
            throw new GameException(GameException.PARSING_ERROR);
        }
        return igm;
    }

    // Méthode appelée lorsque la tâche de fond sera terminée
    //  Affiche le résultat
    protected void onPostExecute(String response) {
        if (!isRequeteFailed) {
            try {
                InfoGameMode igm = decodeJSON(response);

                Intent i = new Intent(context, GameActivity.class);

                GameMode provGM = null;
                switch (gameMode) {
                    case GameMode.MAP_FINDING:
                        provGM = new GameMode.MapFinding(
                                new LatLng(0, 0),
                                2,
                                maxDistanceForWinning
                        );
                        break;
                    case GameMode.BUTTON_FINDING:
                        provGM = new GameMode.ButtonFinding(
                                numberOfButtons,
                                5,
                                igm.bc
                        );
                        break;
                    case GameMode.TEXT_FINDING:
                        provGM = new GameMode.TextFinding(
                                5
                        );
                        break;
                }
                completeIntent(
                        i,
                        gameMode,
                        igm.name,
                        igm.pointLat,
                        igm.pointLon,
                        minZoom,
                        provGM
                );

                context.startActivity(i);
                onSuccessed();

            } catch (GameException e) {
                onFail(e);
            }
        } else {
            onFail(requeteFailException);
        }
    }
    public void completeIntent(Intent i, int gameMode, String cityName, double pointLat, double pointLon,
                               float minZoom, GameMode gm){

        i.putExtra("gameSettings", gameSettings);
        i.putExtra("gameMode",gameMode);
        i.putExtra("cityName", cityName);
        i.putExtra("pointLat", pointLat);
        i.putExtra("pointLon", pointLon);
        i.putExtra("minZoom", minZoom);

        switch (gameMode) {
            case GameMode.MAP_FINDING:
                GameMode.MapFinding agm1 = (GameMode.MapFinding)gm;
                i.putExtra("mapFindingMaxDistanceForWinning", maxDistanceForWinning);
                i.putExtra("mapFindingStartLat", agm1.startPosition.latitude);
                i.putExtra("mapFindingStartLon", agm1.startPosition.longitude);
                i.putExtra("mapFindingStartZoom", agm1.startZoom);
                break;

            case GameMode.BUTTON_FINDING:
                GameMode.ButtonFinding agm2 = (GameMode.ButtonFinding)gm;

                i.putExtra("buttonFindingCountCities", agm2.numberOfCities);

                String[] name = new String[agm2.numberOfCities];
                double[] clat = new double[agm2.numberOfCities];
                double[] clon = new double[agm2.numberOfCities];
                boolean[] cans = new boolean[agm2.numberOfCities];

                for (int j=0; j<agm2.numberOfCities; j++){
                    GameMode.ButtonFinding.ButtonCity bc = agm2.cities.get(j);
                    name[j] = bc.name;
                    clat[j] = bc.coord.latitude;
                    clon[j] = bc.coord.longitude;
                    cans[j] = bc.goodAnswer;
                }

                i.putExtra("buttonFindingCityName", name);
                i.putExtra("buttonFindingCityLat", clat);
                i.putExtra("buttonFindingCityLon", clon);
                i.putExtra("buttonFindingCityIsAnswer", cans);

                i.putExtra("buttonFindingCityFinalZoom", agm2.finalZoom);
                break;
            case GameMode.TEXT_FINDING:
                GameMode.TextFinding agm3 = (GameMode.TextFinding)gm;
                i.putExtra("textFindingCityFinalZoom", agm3.finalZoom);
                break;
        }
    }

    private Runnable onSuccessedRunnable;
    public void setOnSuccessed(Runnable r){
        onSuccessedRunnable = r;
    }
    private void onSuccessed(){
        if (onSuccessedRunnable != null){
            onSuccessedRunnable.run();
        }
    }
    private GameExceptionRunnable onFailRunnable;
    public void setOnFail(GameExceptionRunnable r){
        onFailRunnable = r;
    }
    private void onFail(GameException e){
        if (onFailRunnable != null){
            onFailRunnable.fail(e);
        }
    }
}
