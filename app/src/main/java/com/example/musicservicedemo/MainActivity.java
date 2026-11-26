package com.example.musicservicedemo;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private TextView tvSongTitle;
    private TextView tvArtist;
    private TextView tvStatus;
    private Button btnPlayPause;
    private Button btnPrev;
    private Button btnNext;
    private ListView lvSongs;

    private MusicService musicService;
    private boolean isBound = false;

    // ServiceConnection để bind và giao tiếp trực tiếp với Foreground Service
    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
            musicService = binder.getService();
            isBound = true;

            musicService.setOnPlaybackChangeListener((song, isPlaying, index) -> {
                runOnUiThread(() -> updateUI(song, isPlaying));
            });

            // Cập nhật trạng thái hiện tại khi vừa bind
            Song currentSong = musicService.getCurrentSong();
            updateUI(currentSong, musicService.isPlaying());
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
            musicService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvSongTitle = findViewById(R.id.tvSongTitle);
        tvArtist = findViewById(R.id.tvArtist);
        tvStatus = findViewById(R.id.tvStatus);
        btnPlayPause = findViewById(R.id.btnPlayPause);
        btnPrev = findViewById(R.id.btnPrev);
        btnNext = findViewById(R.id.btnNext);
        lvSongs = findViewById(R.id.lvSongs);

        // Xin quyền Notification (bắt buộc cho Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }

        // Khởi động Foreground Service để Service sống độc lập khi thoát ra Home
        Intent startIntent = new Intent(this, MusicService.class);
        startIntent.setAction(MusicService.ACTION_START_FOREGROUND);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(startIntent);
        } else {
            startService(startIntent);
        }

        // Setup ListView 10 bài hát
        Song[] playlist = MusicService.getPlaylist();
        ArrayList<String> displayList = new ArrayList<>();
        for (int i = 0; i < playlist.length; i++) {
            displayList.add((i + 1) + ". " + playlist[i].getTitle() + " - " + playlist[i].getArtist());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, displayList);
        lvSongs.setAdapter(adapter);

        lvSongs.setOnItemClickListener((parent, view, position, id) -> {
            if (isBound && musicService != null) {
                musicService.playSong(position);
            }
        });

        btnPlayPause.setOnClickListener(v -> {
            if (isBound && musicService != null) {
                musicService.togglePlayPause();
            }
        });

        btnPrev.setOnClickListener(v -> {
            if (isBound && musicService != null) {
                musicService.prevSong();
            }
        });

        btnNext.setOnClickListener(v -> {
            if (isBound && musicService != null) {
                musicService.nextSong();
            }
        });
    }

    private void updateUI(Song song, boolean isPlaying) {
        if (song != null) {
            tvSongTitle.setText(song.getTitle());
            tvArtist.setText("Nghệ sĩ: " + song.getArtist());
            tvStatus.setText("Trạng thái: " + (isPlaying ? "Đang phát (Foreground Service)" : "Tạm dừng"));
            btnPlayPause.setText(isPlaying ? "⏸ Pause" : "▶ Play");
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Bind với Service khi Activity hiển thị
        Intent intent = new Intent(this, MusicService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Unbind khi Activity thoát ra màn hình chính (Service vẫn chạy nền nhờ startForeground)
        if (isBound) {
            if (musicService != null) {
                musicService.setOnPlaybackChangeListener(null);
            }
            unbindService(serviceConnection);
            isBound = false;
        }
    }
}
