package com.reactlibrary.googleDAI;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableMapKeySetIterator;
import com.facebook.react.bridge.ReadableType;
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
import com.google.ads.interactivemedia.v3.api.StreamRequest.StreamFormat;
import com.google.ads.interactivemedia.v3.api.player.VideoProgressUpdate;
import com.google.ads.interactivemedia.v3.api.player.VideoStreamPlayer;

import com.brentvatne.exoplayer.ReactExoplayerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

public class SampleAdsWrapper implements AdEvent.AdEventListener,
        AdErrorEvent.AdErrorListener, AdsLoader.AdsLoadedListener {

    // Live stream asset key.
    private static final String TEST_ASSET_KEY = "sN_IYUG8STe1ZzhIIE_ksA";

    // VOD HLS content source and video IDs.
    private static final String TEST_HLS_CONTENT_SOURCE_ID = "19463";
    private static final String TEST_HLS_VIDEO_ID = "googleio-highlights";

    private static final String PLAYER_TYPE = "DAISamplePlayer";

    /**
     * Log interface, so we can output the log commands to the UI or similar.
     */
    public interface Logger {
        void log(String logMessage);
    }

    private ImaSdkFactory mSdkFactory;
    private AdsLoader mAdsLoader;
    private StreamDisplayContainer mDisplayContainer;
    private StreamManager mStreamManager;
    private List<VideoStreamPlayer.VideoStreamPlayerCallback> mPlayerCallbacks;

    private ReactExoplayerView mVideoPlayer;
    private Context mContext;
    private ViewGroup mAdUiContainer;

    private String mFallbackUrl;
    private Logger mLogger;

    public SampleAdsWrapper(Context context, ReactExoplayerView videoPlayer,
                            ViewGroup adUiContainer) {
        mVideoPlayer = videoPlayer;
        mContext = context;
        mAdUiContainer = adUiContainer;
        mSdkFactory = ImaSdkFactory.getInstance();
        mPlayerCallbacks = new ArrayList<>();
        createAdsLoader();
    }

    private void createAdsLoader() {
        ImaSdkSettings settings = mSdkFactory.createImaSdkSettings();
        VideoStreamPlayer videoStreamPlayer = createVideoStreamPlayer();
        mDisplayContainer =
                ImaSdkFactory.createStreamDisplayContainer(mAdUiContainer, videoStreamPlayer);
//        mVideoPlayer.setSampleVideoPlayerCallback(
//                new SampleVideoPlayerCallback() {
//                    @Override
//                    public void onUserTextReceived(String userText) {
//                        for (VideoStreamPlayer.VideoStreamPlayerCallback callback : mPlayerCallbacks) {
//                            callback.onUserTextReceived(userText);
//                        }
//                    }
//
//                    @Override
//                    public void onSeek(int windowIndex, long positionMs) {
//                        // See if we would seek past an ad, and if so, jump back to it.
//                        long newSeekPositionMs = positionMs;
//                        if (mStreamManager != null) {
//                            CuePoint prevCuePoint =
//                                    mStreamManager.getPreviousCuePointForStreamTime(positionMs / 1000);
//                            if (prevCuePoint != null && !prevCuePoint.isPlayed()) {
//                                newSeekPositionMs = (long) (prevCuePoint.getStartTime() * 1000);
//                            }
//                        }
//                        mVideoPlayer.seekTo(windowIndex, newSeekPositionMs);
//                    }
//                });
        mAdsLoader = mSdkFactory.createAdsLoader(mContext, settings, mDisplayContainer);
    }

    public void requestAndPlayVodStream(String contentSourceID, String videoID, ReadableMap adTagParametersMap) {
        requestAndPlayAds();
        StreamRequest request = mSdkFactory.createVodStreamRequest(
                contentSourceID, videoID, null); // apiKey
        request.setFormat(StreamFormat.HLS);
        HashMap<String,String> adTagParameters = new HashMap<>();
        if (adTagParametersMap != null) {
            for (
                    ReadableMapKeySetIterator it = adTagParametersMap.keySetIterator();
                    it.hasNextKey();
            ) {
                String key = it.nextKey();
                ReadableType type = adTagParametersMap.getType(key);
                switch(type) {
                    case String:
                        adTagParameters.put(key, adTagParametersMap.getString(key));
                        break;
                }
            }
        }
        request.setAdTagParameters(adTagParameters);
        mAdsLoader.requestStream(request);
    }

    public void requestAndPlayAds() {
        mAdsLoader.addAdErrorListener(this);
        mAdsLoader.addAdsLoadedListener(this);
//        mAdsLoader.requestStream(buildStreamRequest());
    }



    private StreamRequest buildStreamRequest() {
        StreamRequest request;
//        switch (CONTENT_TYPE) {
//            case LIVE_HLS:
//                // Live HLS stream request.
//                return mSdkFactory.createLiveStreamRequest(TEST_ASSET_KEY, null);
//            case VOD_HLS:
                // VOD HLS request.
                request =
                        mSdkFactory.createVodStreamRequest(
                                TEST_HLS_CONTENT_SOURCE_ID, TEST_HLS_VIDEO_ID, null); // apiKey
                request.setFormat(StreamFormat.HLS);
                return request;
//            case VOD_DASH:
//                // VOD DASH request.
//                request =
//                        mSdkFactory.createVodStreamRequest(
//                                TEST_DASH_CONTENT_SOURCE_ID, TEST_DASH_VIDEO_ID, null); // apiKey
//                request.setFormat(StreamFormat.DASH);
//                return request;
//            default:
//                // Content type not selected.
//                return null;
//        }
    }

    private VideoStreamPlayer createVideoStreamPlayer() {
        return new VideoStreamPlayer() {
            @Override
            public void loadUrl(String url, List<HashMap<String, String>> subtitles) {
                log("loadUrl ".concat(url));
//                mVideoPlayer.setStreamUrl(url);
//                mVideoPlayer.play();
            }

            @Override
            public int getVolume() {
                // Make the video player play at the current device volume.
                return 100;
            }

            @Override
            public void addCallback(VideoStreamPlayerCallback videoStreamPlayerCallback) {
                mPlayerCallbacks.add(videoStreamPlayerCallback);
            }

            @Override
            public void removeCallback(VideoStreamPlayerCallback videoStreamPlayerCallback) {
                mPlayerCallbacks.remove(videoStreamPlayerCallback);
            }

            @Override
            public void onAdBreakStarted() {
                // Disable player controls.
//                mVideoPlayer.enableControls(false);
                log("Ad Break Started\n");
            }

            @Override
            public void onAdBreakEnded() {
                // Re-enable player controls.
//                mVideoPlayer.enableControls(true);
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
//                mVideoPlayer.seekTo(timeMs);
                log("seek");
            }

            @Override
            public VideoProgressUpdate getContentProgress() {
                return new VideoProgressUpdate(0,0);
//                        mVideoPlayer.getCurrentPositionPeriod(), mVideoPlayer.getDuration());
            }
        };
    }

    /** AdErrorListener implementation **/
    @Override
    public void onAdError(AdErrorEvent event) {
        // play fallback URL.
        // mVideoPlayer.setStreamUrl(mFallbackUrl);
        // mVideoPlayer.enableControls(true);
        // mVideoPlayer.play();
    }

    /** AdEventListener implementation **/
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

    /** AdsLoadedListener implementation **/
    @Override
    public void onAdsManagerLoaded(AdsManagerLoadedEvent event) {
        mStreamManager = event.getStreamManager();
        mStreamManager.addAdErrorListener(this);
        mStreamManager.addAdEventListener(this);
        mStreamManager.init();
    }

    /** Sets fallback URL in case ads stream fails. **/
    void setFallbackUrl(String url) {
        mFallbackUrl = url;
    }

    public void destroy() {

    }


    private void log(String message) {
        Log.v(">>> SampleAdsWrapper", message);
    }
}
