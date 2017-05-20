package com.arshan.cokestudio;

import java.io.Serializable;

/**
 * Created by Arshan on 19-Mar-2017.
 */

public class SongsPojo implements Serializable {
    private String song, url, artists, cover_image;

    public SongsPojo() {
    }

    public SongsPojo(String song, String url, String cover_image, String artists) {
        this.song = song;
        this.url = url;
        this.cover_image = cover_image;
        this.artists = artists;

    }

    public String getSong() {
        return song;
    }

    public void setSong(String song) {
        this.song = song;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getArtists() {
        return artists;
    }

    public void setArtists(String artists) {
        this.artists = artists;
    }

    public String getCover_image() {
        return cover_image;
    }

    public void setCover_image(String cover_image) {
        this.cover_image = cover_image;
    }
}
