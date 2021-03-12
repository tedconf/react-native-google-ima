package com.reactlibrary.googleIMA;

import android.net.Uri;

import com.brentvatne.exoplayer.ReactExoplayerView;

import java.util.HashMap;
import java.util.Map;

public class RNGoogleIMAVideoWrapper {

    private ReactExoplayerView reactExoplayerView;
    private Uri fallbackUri;
    private String fallbackExtension;
    private Map<String, String> fallbackHeaders;
    private String streamUrl;

    public RNGoogleIMAVideoWrapper(ReactExoplayerView reactExoplayerView) {
        this.reactExoplayerView = reactExoplayerView;
    }

    public static RNGoogleIMAVideoWrapper fromReactExoplayerView(ReactExoplayerView reactExoplayerView) {
        return new RNGoogleIMAVideoWrapper(reactExoplayerView);
    }

    public void setFallbackUri(Uri uri) {
        this.fallbackUri = uri;
    }

    public void setFallbackExtension(String extension) {
        this.fallbackExtension = extension;
    }

    public void setFallbackHeaders(Map<String, String> headers) {
        this.fallbackHeaders = headers;
    }

    public void playFallback() {
        reactExoplayerView.setSrc(fallbackUri, fallbackExtension, fallbackHeaders, false);
    }

    public void setStreamUrl(String url) {
        this.streamUrl = url;
    }

    public void play() {
        if (streamUrl != null) {
            reactExoplayerView.setSrc(Uri.parse(streamUrl), "", new HashMap<String, String>(), false);
            reactExoplayerView.setPausedModifier(false);
        } else {
            playFallback();
        }
    }

    public void resume() {
//        reactExoplayerView.setPausedModifier();
    }

    public void setDelegate(RNGoogleIMAView rnGoogleIMAView) {
        reactExoplayerView.delegate = rnGoogleIMAView;
    }

    /** Video player callback to be called when TXXX ID3 tag is received or seeking occurs. */
    public interface SampleVideoPlayerCallback {
        void onUserTextReceived(String userText);

        void onSeek(int windowIndex, long positionMs);
    }

    public void pause() {
    }

    public void enableControls(boolean b) {
    }

    public void seekTo(long positionMs) {

    }

    public void seekTo(int windowIndex, long timeMs) {

    }

    public long getCurrentPositionMs() {
        return 0;
    }

    public long getDuration() {
        return 0;
    }

    // Methods for exposing player information.
    public void setSampleVideoPlayerCallback(SampleVideoPlayerCallback callback) {
//        playerCallback = callback;
    }
}
