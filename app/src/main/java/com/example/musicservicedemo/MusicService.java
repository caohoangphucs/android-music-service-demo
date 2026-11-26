package com.example.musicservicedemo;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.IBinder;
import androidx.core.app.NotificationCompat;
import java.io.IOException;

public class MusicService extends Service {
    public static final String ACTION_START_BACKGROUND = "ACTION_START_BG";
    public static final String ACTION_START_FOREGROUND = "ACTION_START_FG";
    public static final String ACTION_STOP = "ACTION_STOP";
    public static final String ACTION_NEXT = "ACTION_NEXT";
    public static final String ACTION_PREV = "ACTION_PREV";
    public static final String ACTION_PLAY_INDEX = "ACTION_PLAY_INDEX";

    public static final String EXTRA_INDEX = "EXTRA_INDEX";
    public static final String EXTRA_MODE = "EXTRA_MODE"; // "fg" or "bg"

    private static final String CHANNEL_ID = "MusicDemoChannel";

    private MediaPlayer mediaPlayer;
    private int currentIndex = 0;
    private boolean isForegroundMode = true;

    private static final Song[] PLAYLIST = new Song[]{
        new Song("Acoustic Breeze", "Sunrise Melody", R.raw.song_01, "https://raw.githubusercontent.com/caohoangphucs/android-music-service-demo/main/app/src/main/res/raw/song_01.mp3"),
        new Song("Sunny Days", "Morning Walk", R.raw.song_02, "https://raw.githubusercontent.com/caohoangphucs/android-music-service-demo/main/app/src/main/res/raw/song_02.mp3"),
        new Song("Chill Lofi Beat", "Coffee Time", R.raw.song_03, "https://raw.githubusercontent.com/caohoangphucs/android-music-service-demo/main/app/src/main/res/raw/song_03.mp3"),
        new Song("City Lights", "Night Drive", R.raw.song_04, "https://raw.githubusercontent.com/caohoangphucs/android-music-service-demo/main/app/src/main/res/raw/song_04.mp3"),
        new Song("Dreamy Sky", "Cloud Watching", R.raw.song_05, "https://raw.githubusercontent.com/caohoangphucs/android-music-service-demo/main/app/src/main/res/raw/song_05.mp3"),
        new Song("Ocean Waves", "Relaxing Vibe", R.raw.song_06, "https://raw.githubusercontent.com/caohoangphucs/android-music-service-demo/main/app/src/main/res/raw/song_06.mp3"),
        new Song("Forest Rain", "Peaceful Moments", R.raw.song_07, "https://raw.githubusercontent.com/caohoangphucs/android-music-service-demo/main/app/src/main/res/raw/song_07.mp3"),
        new Song("Midnight Jazz", "Cozy Lounge", R.raw.song_08, "https://raw.githubusercontent.com/caohoangphucs/android-music-service-demo/main/app/src/main/res/raw/song_08.mp3"),
        new Song("Golden Hour", "Sunset Horizon", R.raw.demo_music, "https://raw.githubusercontent.com/caohoangphucs/android-music-service-demo/main/app/src/main/res/raw/song_09.mp3"),
        new Song("Sweet Memories", "Nostalgia Sound", R.raw.song_10, "https://raw.githubusercontent.com/caohoangphucs/android-music-service-demo/main/app/src/main/res/raw/song_10.mp3")
    };

    public static Song[] getPlaylist() {
        return PLAYLIST;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getAction() != null) {
            String action = intent.getAction();

            if (ACTION_START_BACKGROUND.equals(action)) {
                isForegroundMode = false;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    stopForeground(STOP_FOREGROUND_REMOVE);
                } else {
                    stopForeground(true);
                }
                playSong(currentIndex);
            } else if (ACTION_START_FOREGROUND.equals(action)) {
                isForegroundMode = true;
                playSong(currentIndex);
            } else if (ACTION_PLAY_INDEX.equals(action)) {
                currentIndex = intent.getIntExtra(EXTRA_INDEX, 0);
                if (intent.hasExtra(EXTRA_MODE)) {
                    isForegroundMode = "fg".equals(intent.getStringExtra(EXTRA_MODE));
                }
                playSong(currentIndex);
            } else if (ACTION_NEXT.equals(action)) {
                currentIndex = (currentIndex + 1) % PLAYLIST.length;
                playSong(currentIndex);
            } else if (ACTION_PREV.equals(action)) {
                currentIndex = (currentIndex - 1 + PLAYLIST.length) % PLAYLIST.length;
                playSong(currentIndex);
            } else if (ACTION_STOP.equals(action)) {
                stopMusic();
                stopSelf();
            }
        }
        return START_NOT_STICKY;
    }

    private void playSong(int index) {
        if (index < 0 || index >= PLAYLIST.length) index = 0;
        currentIndex = index;
        Song song = PLAYLIST[index];

        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }

        // Ưu tiên phát từ local raw, fallback sang online URL nếu cần
        mediaPlayer = MediaPlayer.create(this, song.getRawResId());
        if (mediaPlayer == null) {
            try {
                mediaPlayer = new MediaPlayer();
                mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build());
                mediaPlayer.setDataSource(song.getOnlineUrl());
                mediaPlayer.prepare();
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }

        mediaPlayer.setOnCompletionListener(mp -> {
            currentIndex = (currentIndex + 1) % PLAYLIST.length;
            playSong(currentIndex);
        });

        mediaPlayer.start();

        if (isForegroundMode) {
            updateForegroundNotification(song);
        }
    }

    private void updateForegroundNotification(Song song) {
        createNotificationChannel();
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Playing: " + song.getTitle())
                .setContentText(song.getArtist() + " (" + (currentIndex + 1) + "/" + PLAYLIST.length + ")")
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .build();

        startForeground(1, notification);
    }

    private void stopMusic() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Music Service Notification",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    @Override
    public void onDestroy() {
        stopMusic();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
