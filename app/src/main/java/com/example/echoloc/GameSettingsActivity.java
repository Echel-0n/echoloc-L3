package com.example.echoloc;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class GameSettingsActivity extends AppCompatActivity {

    private int gameMode = GameMode.MAP_FINDING;
    private String country = null;

    private final int intoMostPopulateDefault = 500,
            numberOfButtonsMax = GeoDBAPI.MAX_CITIES_PER_REQUEST,
            numberOfButtonsDefault = 3,
            minZoomMax = 20,
            minZoomDefault = 12,
            intoMostPopulateMax = 600000;
    private Integer intoMostPopulate = intoMostPopulateDefault,
            numberOfButtons = numberOfButtonsDefault,
            minZoom = minZoomDefault;

    private GameTask playTask;

    Map<String, String> countries;
    ArrayList<String> countriesName;
    private static boolean isCountryNameModifiedByListener = false,
            isIntoMostPopulateModifiedByListener = false,
            isNbOfButtonsModifiedByListener = false,
            isMinZoomModifiedByListener = false;

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_settings);
        Objects.requireNonNull(getSupportActionBar()).hide();

        // Obtention des différents pays (dans la langue locale)
        // Et de leurs codes ISO-2 pour l'API
        countries = new HashMap<>();
        countriesName = new ArrayList<>();
        for (String iso : Locale.getISOCountries()) {
            // language = "" car il prend par default le langage du téléphone
            Locale l = new Locale("", iso);
            countries.put(l.getDisplayCountry(), iso);
            countriesName.add(l.getDisplayCountry());
        }

        // Ajout des continents
        String cont1 = getString(R.string.word_asia);
        countries.put(cont1, getString(R.string.asia_countries_iso2));
        countriesName.add(cont1);
        String cont2 = getString(R.string.word_africa);
        countries.put(cont2, getString(R.string.africa_countries_iso2));
        countriesName.add(cont2);
        String cont3 = getString(R.string.word_north_america);
        countries.put(cont3, getString(R.string.north_america_countries_iso2));
        countriesName.add(cont3);
        String cont4 = getString(R.string.word_south_america);
        countries.put(cont4, getString(R.string.south_america_countries_iso2));
        countriesName.add(cont4);
        String cont5 = getString(R.string.word_europe);
        countries.put(cont5, getString(R.string.europe_countries_iso2));
        countriesName.add(cont5);
        String cont6 = getString(R.string.word_oceania);
        countries.put(cont6, getString(R.string.oceania_countries_iso2));
        countriesName.add(cont6);

        // Triage par ordre alphabétique
        countriesName.sort(String::compareTo);

        // Listeners
        EditText etCountries = findViewById(R.id.etCountries);
        etCountries.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            /**
             * Changement du nom de pays donné par les pays existants de la liste countriesName
             */
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!isCountryNameModifiedByListener){
                    int ignoreAfter = etCountries.getSelectionEnd();
                    if (s.length() > 0 && ignoreAfter > 0) {
                        int bestCommonLetters = 0;
                        String countryForReplace = null;
                        for (String name : countriesName) {
                            int commonLetters = 0;
                            for (int i = 0; i < s.length() && i < name.length()
                                    && i < ignoreAfter; i++) {
                                if (Character.toLowerCase(s.charAt(i)) ==
                                        Character.toLowerCase(name.charAt(i))) {
                                    commonLetters++;
                                } else {
                                    break;
                                }
                            }
                            if (commonLetters < bestCommonLetters) {
                                break;
                            } else if (commonLetters > bestCommonLetters) {
                                countryForReplace = name;
                                bestCommonLetters = commonLetters;
                            }
                        }
                        if (countryForReplace != null) {
                            isCountryNameModifiedByListener = true;
                            etCountries.setText(countryForReplace);
                            country = countries.get(countryForReplace);
                            etCountries.setSelection(bestCommonLetters);
                            isCountryNameModifiedByListener = false;
                        }
                    } else {
                        if (ignoreAfter <= 0) {
                            isCountryNameModifiedByListener = true;
                            etCountries.setText("");
                            isCountryNameModifiedByListener = false;
                        }
                        country = null;
                    }
                }
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        EditText etIntoMostPopulate = findViewById(R.id.intoMostPopulate);
        etIntoMostPopulate.setHint(String.valueOf(intoMostPopulateDefault));
        etIntoMostPopulate.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            /**
             * Limitation du range de ville indiqué
             */
            @Override @SuppressLint("SetTextI18n")
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0 && !isIntoMostPopulateModifiedByListener) {
                    int i = Integer.parseInt(s.toString());
                    String iS;
                    if (i > intoMostPopulateMax) {
                        iS = String.valueOf(intoMostPopulateMax);
                        intoMostPopulate = intoMostPopulateMax;
                    } else {
                        iS = String.valueOf(i);
                        intoMostPopulate = i;
                    }
                    isIntoMostPopulateModifiedByListener = true;
                    etIntoMostPopulate.setText(iS);
                    etIntoMostPopulate.setSelection(iS.length());
                    isIntoMostPopulateModifiedByListener = false;
                } else if (s.length() <= 0) {
                    intoMostPopulate = intoMostPopulateDefault;
                }
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        EditText etMinZoom = findViewById(R.id.minZoom);
        etMinZoom.setHint(String.valueOf(minZoomDefault));
        etMinZoom.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            /**
             * Limitation du zoom minimum
             */
            @Override @SuppressLint("SetTextI18n")
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0 && !isMinZoomModifiedByListener) {
                    int i = Integer.parseInt(s.toString());
                    String iS;
                    if (i > minZoomMax) {
                        iS = String.valueOf(minZoomMax);
                        minZoom = minZoomMax;
                    } else {
                        iS = String.valueOf(i);
                        minZoom = i;
                    }
                    isMinZoomModifiedByListener = true;
                    etMinZoom.setText(iS);
                    etMinZoom.setSelection(iS.length());
                    isMinZoomModifiedByListener = false;
                }
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        EditText etCountOfButtons = findViewById(R.id.countOfButtons);
        etCountOfButtons.setHint(String.valueOf(numberOfButtonsDefault));
        etCountOfButtons.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            /**
             * Limitation du nombre de boutons
             */
            @Override @SuppressLint("SetTextI18n")
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0 && !isNbOfButtonsModifiedByListener) {
                    int i = Integer.parseInt(s.toString());
                    String iS;
                    if (i > numberOfButtonsMax) {
                        iS = String.valueOf(numberOfButtonsMax);
                        numberOfButtons = numberOfButtonsMax;
                    } else {
                        iS = String.valueOf(i);
                        numberOfButtons = i;
                    }
                    isNbOfButtonsModifiedByListener = true;
                    etCountOfButtons.setText(iS);
                    etCountOfButtons.setSelection(iS.length());
                    isNbOfButtonsModifiedByListener = false;
                }
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Choix entre Carte, Boutons ou Nom (mode de jeu)
        // + Ajout de la ligne du nombre de boutons ou non

        View layNbBut = findViewById(R.id.countOfButtonsLayout);
        layNbBut.setVisibility(View.GONE);

        EchoButton butMap = findViewById(R.id.chooseMapButton);
        EchoButton butBut = findViewById(R.id.chooseButtonsButton);
        EchoButton butTex = findViewById(R.id.chooseTextButton);
        butMap.setOnSelectedChanged(()->{
            if (butMap.isButtonSelected()){
                gameMode = GameMode.MAP_FINDING;
                butBut.setButtonSelected(false);
                butTex.setButtonSelected(false);
                layNbBut.setVisibility(View.GONE);
            }
        });
        butBut.setOnSelectedChanged(()->{
            if (butBut.isButtonSelected()){
                gameMode = GameMode.BUTTON_FINDING;
                butMap.setButtonSelected(false);
                butTex.setButtonSelected(false);
                layNbBut.setVisibility(View.VISIBLE);
            } else {
                layNbBut.setVisibility(View.GONE);
            }
        });
        butTex.setOnSelectedChanged(()->{
            if (butTex.isButtonSelected()){
                gameMode = GameMode.TEXT_FINDING;
                butBut.setButtonSelected(false);
                butMap.setButtonSelected(false);
                layNbBut.setVisibility(View.GONE);
            }
        });

        // Loading screen
        findViewById(R.id.loading_screen).setVisibility(View.INVISIBLE);
        findViewById(R.id.loading_screen).setAlpha(0);
        Functions.applyLoopAnimation(
                findViewById(R.id.loading_screen_logo),
                getDrawable(R.drawable.echo_logo_animated_morphing)
        );
    }

    public void showStats(View view) {
        Intent i = new Intent(this, StatsActivity.class);
        startActivity(i);
        finish();
    }

    @SuppressLint("StaticFieldLeak")
    public void play(View view) {
        findViewById(R.id.loading_screen_logo).setRotation(0);
        findViewById(R.id.loading_screen).setVisibility(View.VISIBLE);
        setSettingsClickable(false);
        findViewById(R.id.loading_screen).animate()
                .alpha(1);

        long defaultMaxDistanceForWinning = 10000;
        GameSettings gs = new GameSettings(
                gameMode, getString(R.string.langCode), minZoom,
                country, intoMostPopulate, numberOfButtons,
                defaultMaxDistanceForWinning);

        playTask = new GameTask(this, gs);
        playTask.setOnTaskCancelled(()-> findViewById(R.id.loading_screen).animate()
                .alpha(0)
                .withEndAction(()->{
                    findViewById(R.id.loading_screen).setVisibility(View.INVISIBLE);
                    setSettingsClickable(true);
                    findViewById(R.id.loading_screen_logo).setTranslationX(0);
                    ((TextView)findViewById(R.id.loading_screen_message)).setText("");
                })
        );
        playTask.setOnTaskFailed(e -> {
            if (e.getExceptionType() == GameException.OUT_OF_CITY_RANGE) {
                ((EditText) findViewById(R.id.intoMostPopulate))
                        .setText(String.valueOf((int) e.getData()));
            }
            ((TextView)findViewById(R.id.loading_screen_message)).setText(e.getMessage());
        });
        playTask.setOnTaskRetried(() -> findViewById(R.id.loading_screen_logo).animate()
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
        playTask.setOnTaskFinished(()->{
            findViewById(R.id.loading_screen).setAlpha(0);
            findViewById(R.id.loading_screen).setVisibility(View.INVISIBLE);
            setSettingsClickable(true);
            ((TextView)findViewById(R.id.loading_screen_message)).setText("");
            finish();
        });
        playTask.execute();
    }

    public void cancel(View view) {
        if(playTask != null){
            playTask.cancel();
        }
    }

    private void setSettingsClickable(boolean clickable) {
        findViewById(R.id.countOfButtons).setClickable(clickable);
        findViewById(R.id.countOfButtons).setEnabled(clickable);
        findViewById(R.id.minZoom).setClickable(clickable);
        findViewById(R.id.minZoom).setEnabled(clickable);
        findViewById(R.id.intoMostPopulate).setClickable(clickable);
        findViewById(R.id.intoMostPopulate).setEnabled(clickable);
        findViewById(R.id.etCountries).setClickable(clickable);
        findViewById(R.id.etCountries).setEnabled(clickable);
        findViewById(R.id.chooseMapButton).setClickable(clickable);
        findViewById(R.id.chooseMapButton).setEnabled(clickable);
        findViewById(R.id.chooseButtonsButton).setClickable(clickable);
        findViewById(R.id.chooseButtonsButton).setEnabled(clickable);
        findViewById(R.id.chooseTextButton).setClickable(clickable);
        findViewById(R.id.chooseTextButton).setEnabled(clickable);
        findViewById(R.id.button_play).setClickable(clickable);
        findViewById(R.id.button_play).setEnabled(clickable);
    }
}