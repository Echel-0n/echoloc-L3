package com.example.echoloc;

import static com.example.echoloc.Functions.distanceToText;
import static com.example.echoloc.Functions.getDistance;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.icu.text.Collator;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import com.example.echoloc.db.Stats;
import com.example.echoloc.db.StatsDAO;
import com.example.echoloc.db.StatsDB;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.Objects;

/**
 * Activité du jeu
 */
public class GameActivity extends AppCompatActivity {
    // Géneral
    private GoogleMap m;
    private boolean nightMode;
    private Marker mark;
    private float minZoom;

    // Usage en fonction
    private int answerLayoutHeightYDelta;
    private int answerLayoutHeight;
    private LatLng answerLayoutHeightOldCoord;
    private boolean finished;
    private String distanceMapFinding;

    // Paramètres de partie
    private int gameMode;
    private GameMode mode;
    private String cityName;
    private double pointLat;
    private double pointLon;
    private LatLng coord;
    private Compteur compteur;

    private boolean winned;

    /**
     * Création de l'activité
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     *
     */
    @SuppressLint({"ClickableViewAccessibility", "UseCompatLoadingForDrawables", "MissingInflatedId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Objects.requireNonNull(getSupportActionBar()).hide();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        winned = false;
        finished = false;

        // Animation de chargement
        Functions.applyLoopAnimation(
                findViewById(R.id.loading_screen_logo),
                getDrawable(R.drawable.echo_logo_animated_morphing)
        );

        // Obtention des extras
        gameMode = getIntent().getIntExtra("gameMode", -1);
        cityName = getIntent().getStringExtra("cityName");
        pointLat = getIntent().getDoubleExtra("pointLat", 0);
        pointLon = getIntent().getDoubleExtra("pointLon", 0);
        coord = new LatLng(pointLat,pointLon);
        minZoom = getIntent().getFloatExtra("minZoom", 12);

        // Récupération des extras en fonction du mode de jeu
        switch (gameMode) {
            case GameMode.MAP_FINDING:
                mode = new GameMode.MapFinding(
                        new LatLng(
                                getIntent().getDoubleExtra("mapFindingStartLat", 0),
                                getIntent().getDoubleExtra("mapFindingStartLon", 0)
                        ),
                        getIntent().getFloatExtra("mapFindingStartZoom", 2),
                        getIntent().getLongExtra("mapFindingMaxDistanceForWinning", 750)
                );
                break;

            case GameMode.BUTTON_FINDING:
                int countCities = getIntent().getIntExtra("buttonFindingCountCities", 0);
                ArrayList<GameMode.ButtonFinding.ButtonCity> bcList = new ArrayList<>();
                int buttCityFinalZoom = getIntent().getIntExtra("buttonFindingCityFinalZoom",5);

                String[] cName = getIntent()
                        .getStringArrayExtra("buttonFindingCityName");
                double[] cLat = getIntent()
                        .getDoubleArrayExtra("buttonFindingCityLat");
                double[] cLon = getIntent()
                        .getDoubleArrayExtra("buttonFindingCityLon");
                boolean[] cIsAnswer = getIntent()
                        .getBooleanArrayExtra("buttonFindingCityIsAnswer");

                for (int i = 0; i<countCities; i++){
                    bcList.add(new GameMode.ButtonFinding.ButtonCity(
                            cName[i],
                            new LatLng( cLat[i], cLon[i] ),
                            cIsAnswer[i]
                    ));
                }
                mode = new GameMode.ButtonFinding(countCities, buttCityFinalZoom, bcList);
                break;

            case GameMode.TEXT_FINDING:
                mode = new GameMode.TextFinding(
                        getIntent().getIntExtra("textFindingCityFinalZoom", 5)
                );
                break;

            default:
                finish();
                break;
        }


        // Style
        int nightModeFlags =
                getResources().getConfiguration().uiMode &
                        Configuration.UI_MODE_NIGHT_MASK;
        switch (nightModeFlags) {
            case Configuration.UI_MODE_NIGHT_UNDEFINED:
            case Configuration.UI_MODE_NIGHT_YES:
                nightMode = true;
                break;
            case Configuration.UI_MODE_NIGHT_NO:
                nightMode = false;
                break;
        }

        // Map principale
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null){
            mapFragment.getMapAsync(this::onMainMapReady);
        }

        // Interface
            // Buttons Listeners
            View adjustAnswer = findViewById(R.id.adjustAnswerLayoutButton);

            adjustAnswer.setOnTouchListener((view, event) -> {
                View rl = findViewById(R.id.userAnswerLayout);
                ViewGroup.LayoutParams lp = rl.getLayoutParams();

                final int Ymain = findViewById(R.id.mainLayout).getMeasuredHeight();
                final int Y = (int) event.getRawY();

                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:
                        answerLayoutHeightYDelta = Y;
                        answerLayoutHeight = lp.height;
                        answerLayoutHeightOldCoord = m.getCameraPosition().target;
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_MOVE:
                        int newHeight = answerLayoutHeight - (Y - answerLayoutHeightYDelta);
                        // Answer Layout max/min
                        if (newHeight <= Ymain - dpToPx(100) // max
                                && newHeight >= adjustAnswer.getHeight() + // min
                                findViewById(R.id.validUserAnswerButton).getHeight() +
                                rl.getPaddingBottom() + rl.getPaddingTop()) {
                            lp.height = newHeight;
                        }
                        modifMapPadding();
                        if (answerLayoutHeightOldCoord != null){
                            m.moveCamera(
                                    CameraUpdateFactory.newLatLng(answerLayoutHeightOldCoord));
                        }
                        break;
                }
                return true;
            });

            // Inflate with game mode elements
            RelativeLayout ual = findViewById(R.id.userAnswerLayout);
            switch (gameMode) {
                case GameMode.MAP_FINDING:
                    // Map de réponse
                    // Insertion map
                    LayoutInflater.from(this)
                            .inflate(R.layout.activity_game_answer_map_fragment,
                                    ual, true);

                    // Chargement map
                    SupportMapFragment answerMapFragment = (SupportMapFragment) getSupportFragmentManager()
                            .findFragmentById(R.id.answerMap);
                    if (answerMapFragment != null) {
                        answerMapFragment.getMapAsync(this::onAnswerMapReady);
                    }
                    break;
                case GameMode.BUTTON_FINDING:
                    // Interface
                    findViewById(R.id.validUserAnswerButton)
                            .setVisibility(View.INVISIBLE);
                    ual.setPadding(0, ual.getPaddingTop(), 0, 0);
                    ual.getLayoutParams().height = findViewById(R.id.mainLayout).getHeight()/2;
                    // Bouton réponse
                    GameMode.ButtonFinding data = (GameMode.ButtonFinding)mode;

                    ArrayList<GameMode.ButtonFinding.ButtonCity> alBC = data.cities;
                    int cityCounter = 0;

                    int numberOfCities = data.numberOfCities;

                    // Inflate
                    LayoutInflater
                            .from(this)
                            .inflate(R.layout.activity_game_answer_buttons_column,
                                    ual, true);

                    // Définition
                    ScrollView scrollLayout = findViewById(R.id.userButtonAnswerScrollLayout);
                    LinearLayout columnLayout = findViewById(R.id.userButtonAnswerLayout);

                    for (int i=0; i<numberOfCities; i++){

                        GameMode.ButtonFinding.ButtonCity bc = alBC.get(cityCounter);
                        cityCounter++;

                        EchoButton ecb = new EchoButton(getApplicationContext());
                        ecb.setPadding(dpToPx(5),dpToPx(5),dpToPx(5),dpToPx(5));
                        ecb.setInnerPadding(dpToPx(5),dpToPx(25),dpToPx(5),dpToPx(25));
                        ecb.setBackgroundColorSelected(getColor(R.color.main_color));
                        ecb.setStokeColorSelected(getColor(R.color.main_color));
                        ecb.setStokeColorUnselected(getColor(R.color.main_color));
                        ecb.setTextColorSelected(getColor(R.color.white));
                        ecb.setTextColorUnselected(getColor(R.color.main_color));
                        ecb.setRadius(dpToPx(5));
                        ecb.setStrokeWidth(dpToPx(1));
                        ecb.setButtonSelected(false);
                        ecb.setText(bc.name);
                        ecb.setTextSize(dpToPx(12));
                        ecb.setAnimationSpeed(250);
                        ecb.setTag(bc);

                        columnLayout.addView(ecb);

                        data.buttons.add(ecb);

                        ecb.setOnSelectedChanged(() -> {
                            if (ecb.isButtonSelected()) { // Si le boutton vient d'être sélectionner
                                for (EchoButton b : data.buttons) {
                                    if (b.isButtonSelected()) {
                                        b.setButtonSelected(false);
                                    }
                                }
                                ecb.setButtonSelected(true);
                                findViewById(R.id.validUserAnswerButton)
                                        .setVisibility(View.VISIBLE);

                                scrollLayout.setPadding(
                                        scrollLayout.getPaddingLeft(),
                                        scrollLayout.getPaddingTop(),
                                        scrollLayout.getPaddingRight(),
                                        findViewById(R.id.validUserAnswerButton).getHeight()
                                );

                            } else { // S'il vient d'être désélectionner
                                findViewById(R.id.validUserAnswerButton)
                                        .setVisibility(View.INVISIBLE);

                                scrollLayout.setPadding(
                                        scrollLayout.getPaddingLeft(),
                                        scrollLayout.getPaddingTop(),
                                        scrollLayout.getPaddingRight(),
                                        0
                                );
                            }
                        });
                    }

                    scrollLayout.post(() -> {
                        int ull = columnLayout.getHeight();
                        int usl = scrollLayout.getHeight() -
                                (scrollLayout.getPaddingTop() + scrollLayout.getPaddingBottom());
                        if (ull<usl){
                            ual.getLayoutParams().height = ual.getHeight() - (usl-ull);
                        }
                        modifMapPadding();
                    });
                    break;

                case GameMode.TEXT_FINDING:
                    // Interface
                    findViewById(R.id.adjustAnswerLayoutButton).setVisibility(View.INVISIBLE);
                    findViewById(R.id.validUserAnswerButton)
                            .setVisibility(View.INVISIBLE);
                    ual.setPadding(0, ual.getPaddingTop(), 0, 0);

                    LayoutInflater
                            .from(this)
                            .inflate(R.layout.activity_game_answer_text,
                                    ual, true);

                    // Bouton réponse
                    EditText et = findViewById(R.id.userTextAnswerEditText);


                    et.addTextChangedListener(
                            new TextWatcher() {
                                @Override
                                public void onTextChanged(CharSequence s,
                                                          int i, int i1, int i2) {
                                    if (s.length() != 0) {
                                        findViewById(R.id.validUserAnswerButton)
                                                .setVisibility(View.VISIBLE);
                                    } else {
                                        findViewById(R.id.validUserAnswerButton)
                                                .setVisibility(View.INVISIBLE);
                                    }
                                }
                                public void beforeTextChanged(CharSequence s,
                                                              int i, int i1, int i2) {}
                                public void afterTextChanged(Editable editable) {}
                            }
                    );
                    findViewById(R.id.userTextAnswerLayout).post(() -> {
                        int ull = et.getHeight();
                        View uslView = findViewById(R.id.userTextAnswerLayout);
                        int usl = uslView.getHeight() -
                                (uslView.getPaddingTop() + uslView.getPaddingBottom());
                        if (ull<usl){
                            ual.getLayoutParams().height = ual.getHeight() - (usl-ull);
                            System.out.println(ual.getLayoutParams().height);
                            modifMapPaddingKeepPos();
                        }
                    });
                    break;
            }
            findViewById(R.id.mainLayout).post(this::userAnswerLayoutSizeAdaptation);
    }

    /**
     * Quand la est
     * @param googleMap fragment map
     */
    public void onMainMapReady(GoogleMap googleMap) {
        // Définition
        m = googleMap;
        UiSettings mUi = m.getUiSettings();

        // Positionnement et limitation de la caméra
        m.moveCamera(CameraUpdateFactory.newLatLngZoom(coord, minZoom));
        m.setMinZoomPreference(minZoom);

        // Camera options
        m.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        m.setIndoorEnabled(false);
        mUi.setCompassEnabled(false);
        mUi.setMyLocationButtonEnabled(false);
        mUi.setMapToolbarEnabled(false);
        mUi.setIndoorLevelPickerEnabled(false);
        mUi.setTiltGesturesEnabled(false);

        //Style
        modifMapPaddingKeepPos();

        // Move Listener
        cameraMoveGestion();

        // Maps Listeners
        m.setOnCameraMoveListener(this::cameraMoveGestion);
        m.setOnMarkerClickListener(marker -> {
            if (marker.equals(mark) && !finished){ recenterMap(); }
            return true;
        });

        // Mise en place du compteur en haut à droite de la map
        compteur = Compteur.start();
        Handler handler=new Handler();
        handler.post(new Runnable(){
            @Override
            public void run() {
                ((TextView)findViewById(R.id.compteur))
                        .setText(Compteur.getStringSec(compteur.getValue()));
                handler.postDelayed(this,10); // refresh
            }
        });

        // Préremplissage de l'écran de fin
        ((TextView) findViewById(R.id.final_screen_city_name)).setText(cityName);
        if (gameMode == GameMode.MAP_FINDING){
            findViewById(R.id.final_screen_distance_for_map_finding).setVisibility(View.VISIBLE);
        }
    }

    public void onAnswerMapReady(GoogleMap am) {
        GameMode.MapFinding data = (GameMode.MapFinding)mode;
        // Définition
        UiSettings mUi = am.getUiSettings();
        data.map = am;

        // Positionnement et limitation de la caméra
        am.moveCamera(CameraUpdateFactory.newLatLngZoom(
                data.startPosition, data.startZoom));

        // Camera options
        am.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mUi.setCompassEnabled(false);
        mUi.setMyLocationButtonEnabled(false);
        mUi.setMapToolbarEnabled(false);
        mUi.setIndoorLevelPickerEnabled(false);
        mUi.setTiltGesturesEnabled(false);
        mUi.setRotateGesturesEnabled(false);

        // Interface
        EchoButton validUserAnswerButton = findViewById(R.id.validUserAnswerButton);
        validUserAnswerButton.setVisibility(View.INVISIBLE);

        // Maps Listeners
        am.setOnMapLongClickListener(l -> {
            if (data.marker != null) {
                data.marker.remove();
            }
            @SuppressLint("UseCompatLoadingForDrawables")
            BitmapDescriptor markerIcon = getMarkerIconFromDrawable(
                    getDrawable(R.drawable.point_logo_full));
            data.marker = am.addMarker(new MarkerOptions()
                    .position(new LatLng(l.latitude, l.longitude))
                    .icon(markerIcon)
                    .draggable(true)
            );

            if (validUserAnswerButton.getVisibility() != View.VISIBLE
                    && data.marker != null) {
                validUserAnswerButton.setVisibility(View.VISIBLE);
            }
        });

        //Style
        if (nightMode) {
            am.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(this, R.raw.night_map)
            );
        }
    }

    private void userAnswerLayoutSizeAdaptation(){
        LatLng provLL = m.getCameraPosition().target;
        findViewById(R.id.userAnswerLayout).getLayoutParams().height =
                findViewById(R.id.mainLayout).getMeasuredHeight()/2;
        m.setPadding(dpToPx(10),dpToPx(10),dpToPx(10),
                findViewById(R.id.mainLayout).getMeasuredHeight()/2);
        m.moveCamera(CameraUpdateFactory.newLatLng(provLL));
    }

    @SuppressLint("StaticFieldLeak")
    public void validUserAnswer(View v){
        boolean isGoodAnswerFind = false;
        compteur.stop();

        if (gameMode == GameMode.MAP_FINDING) {
            GameMode.MapFinding data = (GameMode.MapFinding) mode;
            if (data.marker != null) {
                LatLng ansPos = data.marker.getPosition();

                findViewById(R.id.showAnswerLayoutButton).setVisibility(View.INVISIBLE);
                m.getUiSettings().setAllGesturesEnabled(false);
                m.setMinZoomPreference(-1);

                double maxLat;
                double minLat;
                double maxLon;
                double minLon;

                double ansLat = ansPos.latitude;
                double ansLon = ansPos.longitude;
                double iniLat = pointLat;
                double iniLon = pointLon;


                if (ansLat < iniLat) {
                    minLat = ansLat;
                    maxLat = iniLat;
                } else {
                    minLat = iniLat;
                    maxLat = ansLat;
                }
                if (ansLon < iniLon) {
                    minLon = ansLon;
                    maxLon = iniLon;
                } else {
                    minLon = iniLon;
                    maxLon = ansLon;
                }

                double distance = getDistance(iniLat,iniLon,ansLat,ansLon);

                distanceMapFinding = distanceToText(this, distance);

                ((TextView)findViewById(R.id.final_screen_distance_text_for_map_finding))
                        .setText(distanceMapFinding);

                isGoodAnswerFind = (distance <= ((GameMode.MapFinding)mode)
                        .mapFindingMaxDistanceForWinning);

                LinearLayout ll = findViewById(R.id.buttonsLayout);
                ll.animate()
                        .translationX(-ll.getWidth());
                RelativeLayout rl = findViewById(R.id.userAnswerLayout);
                LatLng latlon = m.getCameraPosition().target;
                rl.animate()
                        .translationY(rl.getHeight())
                        .setUpdateListener(listener -> {
                            modifMapPadding();
                            m.moveCamera(CameraUpdateFactory.newLatLng(latlon));
                        })
                        .withEndAction(() -> {
                            @SuppressLint("UseCompatLoadingForDrawables")
                            BitmapDescriptor markerIcon = getMarkerIconFromDrawable(
                                    getDrawable(R.drawable.point_logo_full));
                            m.addMarker(new MarkerOptions()
                                    .position(ansPos)
                                    .icon(markerIcon));

                            Polyline line = m.addPolyline(new PolylineOptions()
                                    .clickable(true)
                                    .add(
                                            ansPos,
                                            coord)
                            );
                            line.setColor(getColor(R.color.main_color));

                            m.animateCamera(
                                    CameraUpdateFactory.newLatLngBounds(new LatLngBounds(
                                            new LatLng(minLat, minLon),
                                            new LatLng(maxLat, maxLon)
                                    ), dpToPx(45)),
                                    new GoogleMap.CancelableCallback() {
                                        @Override
                                        public void onCancel() {
                                        }

                                        @Override
                                        public void onFinish() {
                                            m.getUiSettings().setScrollGesturesEnabled(true);
                                            m.getUiSettings().setZoomGesturesEnabled(true);
                                        }
                                    }
                            );
                        });
            }
        }

        if (gameMode == GameMode.BUTTON_FINDING){
            GameMode.ButtonFinding data = (GameMode.ButtonFinding)mode;
            GameMode.ButtonFinding.ButtonCity bc = null;
            for (EchoButton b : data.buttons){
                if (b.isButtonSelected()){
                    bc = (GameMode.ButtonFinding.ButtonCity) b.getTag();
                }
            }
            if (bc != null) {
                LatLng ansPos = bc.coord;

                findViewById(R.id.showAnswerLayoutButton).setVisibility(View.INVISIBLE);
                m.getUiSettings().setAllGesturesEnabled(false);
                m.setMinZoomPreference(-1);

                double maxLat;
                double minLat;
                double maxLon;
                double minLon;

                double ansLat = ansPos.latitude;
                double ansLon = ansPos.longitude;
                double iniLat = pointLat;
                double iniLon = pointLon;


                if (ansLat < iniLat) {
                    minLat = ansLat;
                    maxLat = iniLat;
                } else {
                    minLat = iniLat;
                    maxLat = ansLat;
                }
                if (ansLon < iniLon) {
                    minLon = ansLon;
                    maxLon = iniLon;
                } else {
                    minLon = iniLon;
                    maxLon = ansLon;
                }

                LinearLayout ll = findViewById(R.id.buttonsLayout);
                ll.animate()
                        .translationX(-ll.getWidth());
                RelativeLayout rl = findViewById(R.id.userAnswerLayout);
                LatLng latlon = m.getCameraPosition().target;
                GameMode.ButtonFinding.ButtonCity finalBc = bc;

                isGoodAnswerFind = bc.goodAnswer;

                rl.animate()
                        .translationY(rl.getHeight())
                        .setUpdateListener(listener -> {
                            modifMapPadding();
                            m.moveCamera(CameraUpdateFactory.newLatLng(latlon));
                        })
                        .withEndAction(() -> {
                            @SuppressLint("UseCompatLoadingForDrawables")
                            BitmapDescriptor markerIcon = getMarkerIconFromDrawable(
                                    getDrawable(R.drawable.point_logo_full));
                            m.addMarker(new MarkerOptions()
                                    .position(ansPos)
                                    .icon(markerIcon));
                            if (!finalBc.goodAnswer) { // Mauvaise réponse
                                Polyline line = m.addPolyline(new PolylineOptions()
                                        .clickable(true)
                                        .add(ansPos, coord)
                                );
                                line.setColor(getColor(R.color.main_color));
                                m.animateCamera(
                                        CameraUpdateFactory.newLatLngBounds(new LatLngBounds(
                                                new LatLng(minLat, minLon),
                                                new LatLng(maxLat, maxLon)
                                        ), dpToPx(45)),
                                        new GoogleMap.CancelableCallback() {
                                            @Override
                                            public void onCancel() { onFinish(); }
                                            @Override
                                            public void onFinish() {
                                                m.getUiSettings().setScrollGesturesEnabled(true);
                                                m.getUiSettings().setZoomGesturesEnabled(true);
                                            }
                                        }
                                );
                            } else { // Bonne réponse
                                m.animateCamera(
                                        CameraUpdateFactory.newLatLngZoom(
                                                coord,((GameMode.ButtonFinding) mode).finalZoom
                                        ),
                                        new GoogleMap.CancelableCallback() {
                                            @Override
                                            public void onCancel() { onFinish(); }
                                            @Override
                                            public void onFinish() {
                                                m.getUiSettings().setScrollGesturesEnabled(true);
                                                m.getUiSettings().setZoomGesturesEnabled(true);
                                            }
                                        }
                                );
                            }
                        });
            }
        }

        if (gameMode == GameMode.TEXT_FINDING){
            View temp = this.getCurrentFocus();
            if (temp != null) { // Hide keyboard
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(temp.getWindowToken(), 0);
            }

            GameMode.TextFinding data = (GameMode.TextFinding)mode;

            findViewById(R.id.showAnswerLayoutButton).setVisibility(View.INVISIBLE);
            m.getUiSettings().setAllGesturesEnabled(false);
            m.setMinZoomPreference(-1);

            LinearLayout ll = findViewById(R.id.buttonsLayout);
            ll.animate()
                    .translationX(-ll.getWidth());
            RelativeLayout rl = findViewById(R.id.userAnswerLayout);
            LatLng latlon = m.getCameraPosition().target;

            final Collator collator = Collator.getInstance();
            collator.setStrength(Collator.PRIMARY);

            isGoodAnswerFind = (collator.compare( // Comparaison, sans accent, majuscule, ... de la réponse donnée à la bonne réponse
                    ((EditText)findViewById(R.id.userTextAnswerEditText)).getText(),
                    cityName
                )) == 0;


            rl.animate()
                    .translationY(rl.getHeight())
                    .setUpdateListener(listener -> {
                        modifMapPadding();
                        m.moveCamera(CameraUpdateFactory.newLatLng(latlon));
                    })
                    .withEndAction(() -> {
                        @SuppressLint("UseCompatLoadingForDrawables")
                        BitmapDescriptor markerIcon = getMarkerIconFromDrawable(
                                getDrawable(R.drawable.point_logo_full));
                        m.addMarker(new MarkerOptions()
                                .position(coord)
                                .icon(markerIcon));
                        m.animateCamera(
                            CameraUpdateFactory.newLatLngZoom(coord,data.finalZoom),
                            new GoogleMap.CancelableCallback() {
                                @Override public void onCancel() { onFinish(); }
                                @Override public void onFinish() {
                                    m.getUiSettings().setScrollGesturesEnabled(true);
                                    m.getUiSettings().setZoomGesturesEnabled(true);
                                }
                            }
                        );
                    });
        }

        // Remplissage et affichage de lécran de fin
        winned = isGoodAnswerFind;
        finished = true;

        ((TextView)findViewById(R.id.final_screen_time))
                .setText(Compteur.getString(compteur.getValue()));

        if (isGoodAnswerFind){
            findViewById(R.id.final_screen_correct_answer)
                    .setBackgroundColor(getColor(R.color.main_color));
            ((TextView)findViewById(R.id.final_screen_correct_answer))
                    .setText(getString(R.string.word_wellPlay));
        }

        findViewById(R.id.final_screen).setVisibility(View.VISIBLE);
        findViewById(R.id.final_screen).animate()
                .alpha(1);

        // Add new score to Database
        new AddDatabaseTask().execute();
    }

    private void cameraMoveGestion(){
        pingGestion();
        mapButtonsGestion();
    }
    private void pingGestion(){
        // Gestion apparition en fonction du zoom
        if (mark == null) {
            @SuppressLint("UseCompatLoadingForDrawables")
            BitmapDescriptor markerIcon = getMarkerIconFromDrawable(
                    getDrawable(R.drawable.point_logo_empty));
            mark = m.addMarker(new MarkerOptions()
                    .position(coord)
                    .icon(markerIcon)
            );
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void mapButtonsGestion(){
        // Boutton pour recentrer si marker hors de fenetre
            ImageButton recentreButton = findViewById(R.id.recentreButton);

            LatLngBounds currentScreen = m.getProjection().getVisibleRegion().latLngBounds;
            if(!currentScreen.contains(coord) || mark == null) {
                // marker outside visible region
                recentreButton.setBackground(
                        getDrawable(R.drawable.point_logo_empty));
            } else {
                // marker inside visible region
                recentreButton.setBackground(
                        getDrawable(R.drawable.point_logo_full));
            }

        // Boutton pour recentrer sur le nord
            ImageButton recenterMapOnNorthButton = findViewById(R.id.recenterMapOnNorthButton);

            float currentOrientation = m.getCameraPosition().bearing;
            if(currentOrientation == 0) {
                // Orientation North
                recenterMapOnNorthButton.setBackground(
                        getDrawable(R.drawable.button_center_on_north_full));
            } else {
                // Other orientation
                recenterMapOnNorthButton.setBackground(
                        getDrawable(R.drawable.button_center_on_north_empty));
            }
    }

    private BitmapDescriptor getMarkerIconFromDrawable(Drawable drawable) {
        Canvas canvas = new Canvas();
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    public void hideAnswerLayoutButton(View view) { moveAnswerLayout(findViewById(R.id.userAnswerLayout).getHeight()); }
    public void showAnswerLayoutButton(View view) { moveAnswerLayout(0); }
    private void moveAnswerLayout(int translat){
        RelativeLayout v = findViewById(R.id.userAnswerLayout);
        LatLng ll = m.getCameraPosition().target;
        v.animate()
                .translationY(translat)
                .setUpdateListener(listener -> {
                    modifMapPadding();
                    m.moveCamera( CameraUpdateFactory.newLatLng(ll) );
                });
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration c) {
        super.onConfigurationChanged(c);

        // For screen rotate or screen sized changed event :
        View adjustAnswer = findViewById(R.id.adjustAnswerLayoutButton);
        final int Ymain = dpToPx(c.screenHeightDp);

        View rl = findViewById(R.id.userAnswerLayout);
        ViewGroup.LayoutParams lp = rl.getLayoutParams();

        int newHeight = rl.getHeight();

        if (newHeight > Ymain - dpToPx(100)) {
            newHeight = Ymain - dpToPx(100);
        }
        if (newHeight < adjustAnswer.getHeight()) {
            newHeight = adjustAnswer.getHeight();
        }

        lp.height = newHeight;

        rl.post(() -> rl.setLayoutParams(lp));
        modifMapPaddingKeepPos();
    }

    public void recenterMap(View view) { recenterMap(); }
    private void recenterMap() {
        m.animateCamera(
                CameraUpdateFactory.newLatLng(coord)
        );
    }

    public void recenterMapOnNorth(View view) { recenterMapOnNorth(); }
    private void recenterMapOnNorth(){
        // Recentre la map sur le nord
        CameraPosition cp = m.getCameraPosition();
        CameraPosition newCP = new CameraPosition(cp.target, cp.zoom, cp.tilt, 0);
        m.animateCamera(
                CameraUpdateFactory.newCameraPosition(newCP)
        );
    }

    private void modifMapPaddingKeepPos(){
        LatLng ll = m.getCameraPosition().target;
        modifMapPadding();
        m.moveCamera(CameraUpdateFactory.newLatLngZoom(ll, minZoom));
    }
    private void modifMapPadding(){
        int px = dpToPx(10);
        RelativeLayout v = findViewById(R.id.userAnswerLayout);
        m.setPadding(px,px,px,
                v.getLayoutParams().height - ((int)v.getTranslationY()));
    }

    private int dpToPx(int dp){
        return (int)TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp /*dp*/,
                getResources().getDisplayMetrics()
        );
    }


    // Post Game Events
    private GameTask replayTask;
    public void goHome(View view) {
        finish();
    }

    public void replayGame(View view) {
        switchFinalScreen(true);

        GameSettings gs = getIntent().getParcelableExtra("gameSettings");

        replayTask = new GameTask(this, gs);
        replayTask.setOnTaskCancelled(()-> switchFinalScreen(false));
        replayTask.setOnTaskFailed(e -> ((TextView)findViewById(R.id.loading_screen_message)).setText(e.getMessage()));
        replayTask.setOnTaskRetried(() -> findViewById(R.id.loading_screen_logo).animate()
                .translationX(
                        findViewById(R.id.loading_screen_logo).getWidth()/12F
                )
                .setInterpolator(time -> {
                    float t = (time*8);
                    if (t <= 1) {
                        if (t <= 0.25) {
                            return -(4 * t);
                        } else if (t <= 0.75) {
                            return (4 * t) - 2;
                        } else {
                            return -(4 * t) + 4;
                        }
                    } else if (t <= 2) {
                        t--;
                        if (t <= 0.25) {
                            return -(4 * t);
                        } else if (t <= 0.75) {
                            return (4 * t) - 2;
                        } else {
                            return -(4 * t) + 4;
                        }
                    } else {
                        return 0;
                    }
                })
                .setDuration(GeoDBAPI.TIME_IN_MILLIS_BETWEEN_2_REQUEST)
                .withEndAction(() -> findViewById(R.id.loading_screen_logo).setTranslationX(0)));
        replayTask.setOnTaskFinished(this::finish);
        replayTask.execute();
    }

    public void cancelReplay(View view) {
        replayTask.cancel();
    }

    private void switchFinalScreen(boolean toLoadingScreen) {
        if (toLoadingScreen){
            findViewById(R.id.loading_screen).setVisibility(View.VISIBLE);
            findViewById(R.id.loading_screen_logo).setTranslationX(0);
            ((TextView) findViewById(R.id.loading_screen_message)).setText("");
            findViewById(R.id.loading_screen).animate()
                    .alpha(1);
            findViewById(R.id.final_screen).animate()
                    .alpha(0)
                    .withEndAction(() -> findViewById(R.id.final_screen).setVisibility(View.INVISIBLE));
        } else {
            findViewById(R.id.final_screen).setVisibility(View.VISIBLE);
            findViewById(R.id.final_screen).animate()
                    .alpha(1);
            findViewById(R.id.loading_screen).animate()
                    .alpha(0)
                    .withEndAction(() -> {
                        findViewById(R.id.loading_screen).setVisibility(View.INVISIBLE);
                        findViewById(R.id.loading_screen_logo).setTranslationX(0);
                        ((TextView) findViewById(R.id.loading_screen_message)).setText("");
                    });
        }
    }


    @SuppressLint("StaticFieldLeak")
    private class AddDatabaseTask extends AsyncTask<String, Void, String> {
        public AddDatabaseTask() { super(); }

        @Override
        protected String doInBackground(String... strings) {
            StatsDB db = Room.databaseBuilder(getApplicationContext(),
                    StatsDB.class, "database-name").build();

            StatsDAO userDao = db.statDao();
            int nbButs = 0;
            if (gameMode == GameMode.BUTTON_FINDING){
                nbButs = ((GameMode.ButtonFinding)mode).numberOfCities;
            }
            userDao.insert(new Stats(
                    gameMode,
                    winned,
                    cityName,
                    compteur.getValue(),
                    0,
                    nbButs,
                    distanceMapFinding
            ));


            return null;
        }
    }
}