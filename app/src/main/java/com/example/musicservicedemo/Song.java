package com.example.musicservicedemo;

public class Song {
    private final String title;
    private final String artist;
    private final int rawResId;
    private final String onlineUrl;

    public Song(String title, String artist, int rawResId, String onlineUrl) {
        this.title = title;
        this.artist = artist;
        this.rawResId = rawResId;
        this.onlineUrl = onlineUrl;
    }

    public String getTitle() { return title; }
    public String getArtist() { return artist; }
    public int getRawResId() { return rawResId; }
    public String getOnlineUrl() { return onlineUrl; }
}
