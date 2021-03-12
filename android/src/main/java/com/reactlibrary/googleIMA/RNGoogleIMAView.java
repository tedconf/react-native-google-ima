package com.reactlibrary.googleIMA;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.net.Uri;
import android.view.View;
import android.os.Build;
import android.view.ViewGroup;
import android.webkit.WebView;

import androidx.annotation.NonNull;

import com.brentvatne.exoplayer.ReactExoplayerView;
import com.brentvatne.exoplayer.ReactExoplayerViewDelegateInterface;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableMapKeySetIterator;
import com.facebook.react.bridge.ReadableNativeMap;
import com.facebook.react.uimanager.util.ReactFindViewUtil;
import com.facebook.react.views.view.ReactViewGroup;
import com.google.ads.interactivemedia.v3.api.AdErrorEvent;
import com.google.ads.interactivemedia.v3.api.AdEvent;
import com.google.ads.interactivemedia.v3.api.AdsLoader;
import com.google.ads.interactivemedia.v3.api.AdsManagerLoadedEvent;
import com.google.ads.interactivemedia.v3.api.CuePoint;
import com.google.ads.interactivemedia.v3.api.ImaSdkFactory;
import com.google.ads.interactivemedia.v3.api.ImaSdkSettings;
import com.google.ads.interactivemedia.v3.api.StreamDisplayContainer;
import com.google.ads.interactivemedia.v3.api.StreamManager;
import com.google.ads.interactivemedia.v3.api.StreamRequest;
import com.google.ads.interactivemedia.v3.api.player.VideoProgressUpdate;
import com.google.ads.interactivemedia.v3.api.player.VideoStreamPlayer;
import com.google.android.exoplayer2.source.MediaSource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressLint("ViewConstructor")
public class RNGoogleIMAView extends ReactViewGroup implements ReactExoplayerViewDelegateInterface,
        AdEvent.AdEventListener, AdErrorEvent.AdErrorListener, AdsLoader.AdsLoadedListener {

    private static final String TAG = "ReactExoplayerView";

    private boolean willSetSource = false;

    private ImaSdkFactory sdkFactory;
    private List<VideoStreamPlayer.VideoStreamPlayerCallback> playerCallbacks;

    private ViewGroup adContainerView;
    private StreamDisplayContainer adDisplayContainer;
    private RNGoogleIMAVideoWrapper videoPlayer;

    private AdsLoader adsLoader;
    private StreamManager streamManager;

    static public ReactExoplayerViewDelegateInterface subDelegate;

    String contentSourceID = "";
    String videoID = "";
    String assetKey = "";
    HashMap<String, String> adTagParameters;
    ReadableNativeMap _imaSettings;


    public RNGoogleIMAView(@NonNull Context context) {
        super(context);
        initialize();
    }

    private void initialize() {
        sdkFactory = ImaSdkFactory.getInstance();
        playerCallbacks = new ArrayList<>();
    };

    public void setContentSourceID(String contentSourceID)
    {
        this.contentSourceID = contentSourceID;
    }

    public void setVideoID(String videoID)
    {
        this.videoID = videoID;
    }

    public void setAssetKey(String assetKey)
    {
        this.assetKey = assetKey;
    }

    public void setAdTagParameters(ReadableMap adTagParameters) {
        this.adTagParameters = new HashMap<>();
        if (adTagParameters != null) {
            ReadableMapKeySetIterator iterator = adTagParameters.keySetIterator();
            while(iterator.hasNextKey()) {
                String key = iterator.nextKey();
                this.adTagParameters.put(key, adTagParameters.getString(key));
            }
        }
    }

    @Override
    public void addView(View child, int index, LayoutParams params) {
        super.addView(child, index, params);
        ReactExoplayerView reactExoplayerView = ReactExoplayerView.findRCTVideo(child);
        if (reactExoplayerView != null) {
            reactExoplayerView.setDelegate(this);
            videoPlayer = new RNGoogleIMAVideoWrapper(reactExoplayerView);
            initializeVideo(videoPlayer);
        }
    }

    @Override
    public MediaSource buildMediaSource(ReactExoplayerView reactExoplayerView, Uri uri, String overrideExtension) {
        if (subDelegate != null && !willSetSource) {
            return subDelegate.buildMediaSource(reactExoplayerView, uri, overrideExtension);
        }
        return null;
    }

    @Override
    public boolean setSrc(ReactExoplayerView reactExoplayerView, Uri uri, String extension, Map<String, String> headers) {
        if (videoPlayer != null) {
            videoPlayer.setFallbackUri(uri);
            videoPlayer.setFallbackExtension(extension);
            videoPlayer.setFallbackHeaders(headers);
        }
        willSetSource = requestAndPlayAds();
        return willSetSource;
    }

    private void invalidateExistingAdDisplay() {
        this.invalidatePlayer();

        adDisplayContainer = null;
    }


    void invalidatePlayer() {
        streamManager = null;
        adsLoader = null;
    }

    private void initializeVideo(RNGoogleIMAVideoWrapper videoPlayer) {
        this.invalidateExistingAdDisplay();

        if(this.videoPlayer != null) {
            this.videoPlayer.setDelegate(null);
            this.videoPlayer = null;
        }

        if (videoPlayer != null) {
            this.videoPlayer = videoPlayer;
            this.videoPlayer.setDelegate(this);
            // Create an ad display container for ad rendering.
            createAdsLoader();
        }
    }

    private void createAdsLoader() {
        enableWebViewDebugging();
        VideoStreamPlayer videoStreamPlayer = createVideoStreamPlayer();
        if (adContainerView == null) {
            adContainerView = (ViewGroup) ReactFindViewUtil.findView(this, "adContainerView");
        }

        adDisplayContainer =
                ImaSdkFactory.createStreamDisplayContainer(adContainerView, videoStreamPlayer);

        ImaSdkSettings settings = sdkFactory.createImaSdkSettings();

        videoPlayer.setSampleVideoPlayerCallback(
                new RNGoogleIMAVideoWrapper.SampleVideoPlayerCallback() {
                    @Override
                    public void onUserTextReceived(String userText) {
                        for (VideoStreamPlayer.VideoStreamPlayerCallback callback : playerCallbacks) {
                            callback.onUserTextReceived(userText);
                        }
                    }

                    @Override
                    public void onSeek(int windowIndex, long positionMs) {
                        // See if we would seek past an ad, and if so, jump back to it.
                        long newSeekPositionMs = positionMs;
                        if (streamManager != null) {
                            CuePoint prevCuePoint =
                                    streamManager.getPreviousCuePointForStreamTime(positionMs / 1000);
                            if (prevCuePoint != null && !prevCuePoint.isPlayed()) {
                                newSeekPositionMs = (long) (prevCuePoint.getStartTime() * 1000);
                            }
                        }
                        videoPlayer.seekTo(windowIndex, newSeekPositionMs);
                    }
                });
        adsLoader = sdkFactory.createAdsLoader(getContext(), settings, adDisplayContainer);
    }

    /** Log interface, so we can output the log commands to the UI or similar. */
    public interface Logger {
        void log(String logMessage);
    }

    private String fallbackUrl;
    private RNGoogleIMAAdsWrapper.Logger logger;

    @TargetApi(19)
    private void enableWebViewDebugging() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }
    }

    public boolean requestAndPlayAds() {
        if (videoPlayer == null || adsLoader == null) {
            return false;
        }
        StreamRequest request = buildStreamRequest();
        if (request == null) {
            return false;
        }
        request.setAdTagParameters(adTagParameters);
        adsLoader.addAdErrorListener(this);
        adsLoader.addAdsLoadedListener(this);
        adsLoader.requestStream(request);
        return true;
    }

    private StreamRequest buildStreamRequest() {
        StreamRequest request = null;
        if (!assetKey.isEmpty()) {
            // Live HLS stream request.
            return sdkFactory.createLiveStreamRequest(assetKey, null);
        } else if (!videoID.isEmpty() && !contentSourceID.isEmpty()) {
            // VOD HLS request.
            request =
                    sdkFactory.createVodStreamRequest(
                            contentSourceID, videoID, null); // apiKey
            request.setFormat(StreamRequest.StreamFormat.HLS);
            // request.setFormat(StreamRequest.StreamFormat.HLS);
        }

        return request;
    }

    private VideoStreamPlayer createVideoStreamPlayer() {
        return new VideoStreamPlayer() {
            @Override
            public void loadUrl(String url, List<HashMap<String, String>> subtitles) {
                videoPlayer.setStreamUrl(url);
                videoPlayer.play();
            }

            @Override
            public void pause() {
                // Pause player.
                videoPlayer.pause();
            }

            @Override
            public void resume() {
                // Resume player.
                videoPlayer.resume();
            }

            @Override
            public int getVolume() {
                // Make the video player play at the current device volume.
                return 100;
            }

            @Override
            public void addCallback(VideoStreamPlayerCallback videoStreamPlayerCallback) {
                playerCallbacks.add(videoStreamPlayerCallback);
            }

            @Override
            public void removeCallback(VideoStreamPlayerCallback videoStreamPlayerCallback) {
                playerCallbacks.remove(videoStreamPlayerCallback);
            }

            @Override
            public void onAdBreakStarted() {
                // Disable player controls.
                videoPlayer.enableControls(false);
                log("Ad Break Started\n");
            }

            @Override
            public void onAdBreakEnded() {
                // Re-enable player controls.
                if (videoPlayer != null) {
                    videoPlayer.enableControls(true);
                }
                log("Ad Break Ended\n");
            }

            @Override
            public void onAdPeriodStarted() {
                log("Ad Period Started\n");
            }

            @Override
            public void onAdPeriodEnded() {
                log("Ad Period Ended\n");
            }

            @Override
            public void seek(long timeMs) {
                // An ad was skipped. Skip to the content time.
                videoPlayer.seekTo(timeMs);
                log("seek");
            }

            @Override
            public VideoProgressUpdate getContentProgress() {
                return new VideoProgressUpdate(
                        videoPlayer.getCurrentPositionMs(), videoPlayer.getDuration());
            }
        };
    }

    /** AdErrorListener implementation */
    @Override
    public void onAdError(AdErrorEvent event) {
        log(String.format("Error: %s\n", event.getError().getMessage()));
        // play fallback URL.
        log("Playing fallback Url\n");

        videoPlayer.playFallback();
    }

    /** AdEventListener implementation */
    @Override
    public void onAdEvent(AdEvent event) {
        switch (event.getType()) {
            case AD_PROGRESS:
                // Do nothing or else log will be filled by these messages.
                break;
            default:
                log(String.format("Event: %s\n", event.getType()));
                break;
        }
    }

    /** AdsLoadedListener implementation */
    @Override
    public void onAdsManagerLoaded(AdsManagerLoadedEvent event) {
        streamManager = event.getStreamManager();
        streamManager.addAdErrorListener(this);
        streamManager.addAdEventListener(this);
        streamManager.init();
    }

    /** Sets fallback URL in case ads stream fails. */
    void setFallbackUrl(String url) {
        fallbackUrl = url;
    }

    /** Sets logger for displaying events to screen. Optional. */
    void setLogger(RNGoogleIMAAdsWrapper.Logger logger) {
        this.logger = logger;
    }

    private void log(String message) {
        if (logger != null) {
            logger.log(message);
        }
    }
}
