package com.example.echoloc;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import com.example.echoloc.db.Stats;
import com.example.echoloc.db.StatsDAO;
import com.example.echoloc.db.StatsDB;

import java.util.List;
import java.util.Objects;

public class StatsActivity extends AppCompatActivity {
    ViewGroup vg;
    GetDatabaseTask task;
    int gameMode = GameMode.MAP_FINDING;

    @Override
    @SuppressLint("StaticFieldLeak")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);
        Objects.requireNonNull(getSupportActionBar()).hide();

        vg = findViewById(R.id.stats_layout);
        task = new GetDatabaseTask();
        task.execute();

        // Switch de focus entre les différents boutons du haut

        EchoButton butMap = findViewById(R.id.chooseMapButton);
        EchoButton butBut = findViewById(R.id.chooseButtonsButton);
        EchoButton butTex = findViewById(R.id.chooseTextButton);
        butMap.setOnSelectedChanged(()->{
            vg.removeAllViewsInLayout();
            if (butMap.isButtonSelected()){
                gameMode = GameMode.MAP_FINDING;
                butBut.setButtonSelected(false);
                butTex.setButtonSelected(false);
                task.cancel(true);
                task = new GetDatabaseTask();
                task.execute();
            }
        });
        butBut.setOnSelectedChanged(()->{
            vg.removeAllViewsInLayout();
            if (butBut.isButtonSelected()){
                gameMode = GameMode.BUTTON_FINDING;
                butMap.setButtonSelected(false);
                butTex.setButtonSelected(false);
                task.cancel(true);
                task = new GetDatabaseTask();
                task.execute();
            }
        });
        butTex.setOnSelectedChanged(()->{
            vg.removeAllViewsInLayout();
            if (butTex.isButtonSelected()){
                gameMode = GameMode.TEXT_FINDING;
                butBut.setButtonSelected(false);
                butMap.setButtonSelected(false);
                task.cancel(true);
                task = new GetDatabaseTask();
                task.execute();
            }
        });
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    protected void setStats(List<Stats> stats){
        for (Stats stat : stats){
            ViewGroup v = (ViewGroup) LayoutInflater.from(this)
                    .inflate(R.layout.activity_stats_stat, vg, false);
            vg.addView(v);
            if (stat.success){
                ((ImageView)v.getChildAt(0)).setImageDrawable(getDrawable(R.drawable.valid));
            } else {
                ((ImageView)v.getChildAt(0)).setImageDrawable(getDrawable(R.drawable.invalid));
            }
            ((EchoButton)v.getChildAt(1))
                    .setText(stat.cityName);
            ((EchoButton)v.getChildAt(2))
                    .setText(Compteur.getStringMinSec(stat.duration));
            if (gameMode == GameMode.MAP_FINDING){
                ((EchoButton)v.getChildAt(3))
                        .setText(stat.distance);
            } else if (gameMode == GameMode.BUTTON_FINDING){
                ((EchoButton)v.getChildAt(3))
                        .setText(String.valueOf(stat.buttonsCount));
            } else {
                v.getChildAt(3)
                        .setVisibility(View.GONE);
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    public class GetDatabaseTask extends AsyncTask<String, Void, String> {
        public GetDatabaseTask() {
            super();
        }

        @Override
        protected String doInBackground(String... strings) {
            StatsDB db = Room.databaseBuilder(getApplicationContext(),
                    StatsDB.class, "database-name").build();
            StatsDAO userDao = db.statDao();
            List<Stats> stats = userDao.getByGameModeSorted(gameMode);
            runOnUiThread(() -> setStats(stats));
            return null;
        }
    }
}