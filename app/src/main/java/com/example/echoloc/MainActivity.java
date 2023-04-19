package com.example.echoloc;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;

/**
 * Activitité principale, choix du jeu
 */
public class MainActivity extends AppCompatActivity {


    /**
     * Creation de l'intent
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     *
     */
    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Objects.requireNonNull(getSupportActionBar()).hide();
    }

    /**
     * Si l'utilisateur demande à parametrer son mode de jeu
     * @param view vue
     */
    public void setSettings(View view) {
        Intent i = new Intent(MainActivity.this, GameSettingsActivity.class);
        startActivity(i);
    }

    /**
     * Si l'uitilisateur demande avoir voir ses stats via le bouton
     * @param view vue
     */
    public void showStats(View view) {
        Intent i = new Intent(this, StatsActivity.class);
        startActivity(i);
    }

    /**
     * Si l'uitilisateur demande à jouer au mode par défaut via le bouton
     * @param view vue
     */
    public void launchEcholocDefaultGame(View view) {
        new GameTask(this,
                new GameSettings(
                        GameMode.MAP_FINDING,
                        getString(R.string.langCode),
                        12,
                        "FR",
                        150,
                        0,
                        500
                )).execute();
    }
}