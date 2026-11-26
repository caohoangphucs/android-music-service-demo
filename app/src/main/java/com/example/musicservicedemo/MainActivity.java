package com.example.musicservicedemo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private TextView tvStatus;
    private boolean isForegroundMode = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvStatus = findViewById(R.id.tvStatus);
        Button btnForeground = findViewById(R.id.btnForeground);
        Button btnBackground = findViewById(R.id.btnBackground);
        Button btnStop = findViewById(R.id.btnStop);
        Button btnPrev = findViewById(R.id.btnPrev);
        Button btnNext = findViewById(R.id.btnNext);
        ListView lvSongs = findViewById(R.id.lvSongs);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }

        // Setup 10 songs list
        Song[] playlist = MusicService.getPlaylist();
        ArrayList<String> songDisplayList = new ArrayList<>();
        for (int i = 0; i < playlist.length; i++) {
            songDisplayList.add((i + 1) + ". " + playlist[i].getTitle() + " - " + playlist[i].getArtist());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, songDisplayList);
        lvSongs.setAdapter(adapter);

        lvSongs.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(MainActivity.this, MusicService.class);
            intent.setAction(MusicService.ACTION_PLAY_INDEX);
            intent.putExtra(MusicService.EXTRA_INDEX, position);
            intent.putExtra(MusicService.EXTRA_MODE, isForegroundMode ? "fg" : "bg");
            if (isForegroundMode && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent);
            } else {
                startService(intent);
            }
            tvStatus.setText("Đang phát: " + playlist[position].getTitle() + " (" + (isForegroundMode ? "Foreground" : "Background") + ")");
        });

        btnForeground.setOnClickListener(v -> {
            isForegroundMode = true;
            Intent intent = new Intent(MainActivity.this, MusicService.class);
            intent.setAction(MusicService.ACTION_START_FOREGROUND);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent);
            } else {
                startService(intent);
            }
            tvStatus.setText("Chế độ: Foreground Service (Có Notification)");
            Toast.makeText(this, "Started Foreground Service", Toast.LENGTH_SHORT).show();
        });

        btnBackground.setOnClickListener(v -> {
            isForegroundMode = false;
            Intent intent = new Intent(MainActivity.this, MusicService.class);
            intent.setAction(MusicService.ACTION_START_BACKGROUND);
            startService(intent);
            tvStatus.setText("Chế độ: Background Service");
            Toast.makeText(this, "Started Background Service", Toast.LENGTH_SHORT).show();
        });

        btnStop.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MusicService.class);
            intent.setAction(MusicService.ACTION_STOP);
            startService(intent);
            tvStatus.setText("Trạng thái: Đã dừng");
            Toast.makeText(this, "Stopped Service", Toast.LENGTH_SHORT).show();
        });

        btnPrev.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MusicService.class);
            intent.setAction(MusicService.ACTION_PREV);
            startService(intent);
        });

        btnNext.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MusicService.class);
            intent.setAction(MusicService.ACTION_NEXT);
            startService(intent);
        });
    }
}
