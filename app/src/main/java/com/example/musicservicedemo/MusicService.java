package com.example.musicservicedemo;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import androidx.core.app.NotificationCompat;
import java.io.IOException;

public class MusicService extends Service {
    public static final String ACTION_START_FOREGROUND = "ACTION_START_FOREGROUND";
    public static final String ACTION_PLAY_PAUSE = "ACTION_PLAY_PAUSE";
    public static final String ACTION_NEXT = "ACTION_NEXT";
    public static final String ACTION_PREV = "ACTION_PREV";
    public static final String ACTION_STOP = "ACTION_STOP";

    private static final String CHANNEL_ID = "MusicDemoChannel";
    private static final int NOTIFICATION_ID = 1;

    private MediaPlayer mediaPlayer;
    private int currentIndex = 0;
    private final IBinder binder = new MusicBinder();

    public interface OnPlaybackChangeListener {
        void onPlaybackChanged(Song song, boolean isPlaying, int index);
    }
    private OnPlaybackChangeListener playbackChangeListener;

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

    public class MusicBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }

    public static Song[] getPlaylist() {
        return PLAYLIST;
    }

    public void setOnPlaybackChangeListener(OnPlaybackChangeListener listener) {
        this.playbackChangeListener = listener;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getAction() != null) {
            String action = intent.getAction();
            if (ACTION_START_FOREGROUND.equals(action)) {
                if (mediaPlayer == null) {
                    playSong(currentIndex);
                } else {
                    updateNotification();
                }
            } else if (ACTION_PLAY_PAUSE.equals(action)) {
                togglePlayPause();
            } else if (ACTION_NEXT.equals(action)) {
                nextSong();
            } else if (ACTION_PREV.equals(action)) {
                prevSong();
            } else if (ACTION_STOP.equals(action)) {
                stopMusic();
                stopForeground(true);
                stopSelf();
            }
        }
        return START_NOT_STICKY;
    }

    public void playSong(int index) {
        if (index < 0 || index >= PLAYLIST.length) index = 0;
        currentIndex = index;
        Song song = PLAYLIST[index];

        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }

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

        mediaPlayer.setOnCompletionListener(mp -> nextSong());
        mediaPlayer.start();

        updateNotification();
        notifyStateChanged();
    }

    public void togglePlayPause() {
        if (mediaPlayer == null) {
            playSong(currentIndex);
            return;
        }
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        } else {
            mediaPlayer.start();
        }
        updateNotification();
        notifyStateChanged();
    }

    public void nextSong() {
        currentIndex = (currentIndex + 1) % PLAYLIST.length;
        playSong(currentIndex);
    }

    public void prevSong() {
        currentIndex = (currentIndex - 1 + PLAYLIST.length) % PLAYLIST.length;
        playSong(currentIndex);
    }

    public void stopMusic() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
        notifyStateChanged();
    }

    public boolean isPlaying() {
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }

    public Song getCurrentSong() {
        return PLAYLIST[currentIndex];
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    private void notifyStateChanged() {
        if (playbackChangeListener != null) {
            playbackChangeListener.onPlaybackChanged(getCurrentSong(), isPlaying(), currentIndex);
        }
    }

    private void updateNotification() {
        createNotificationChannel();
        Song song = getCurrentSong();
        boolean playing = isPlaying();

        Intent openAppIntent = new Intent(this, MainActivity.class);
        PendingIntent pOpenApp = PendingIntent.getActivity(this, 0, openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Actions in Notification
        Intent prevIntent = new Intent(this, MusicService.class).setAction(ACTION_PREV);
        PendingIntent pPrev = PendingIntent.getService(this, 1, prevIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Intent playPauseIntent = new Intent(this, MusicService.class).setAction(ACTION_PLAY_PAUSE);
        PendingIntent pPlayPause = PendingIntent.getService(this, 2, playPauseIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Intent nextIntent = new Intent(this, MusicService.class).setAction(ACTION_NEXT);
        PendingIntent pNext = PendingIntent.getService(this, 3, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(song.getTitle())
                .setContentText(song.getArtist() + (playing ? " (Đang phát)" : " (Tạm dừng)"))
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setContentIntent(pOpenApp)
                .addAction(android.R.drawable.ic_media_previous, "Prev", pPrev)
                .addAction(playing ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play,
                        playing ? "Pause" : "Play", pPlayPause)
                .addAction(android.R.drawable.ic_media_next, "Next", pNext)
                .setOngoing(playing)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();

        startForeground(NOTIFICATION_ID, notification);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Music Playback Service",
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
}
