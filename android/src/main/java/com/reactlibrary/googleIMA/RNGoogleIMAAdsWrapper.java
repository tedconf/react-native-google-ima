package com.reactlibrary.googleIMA;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.ViewGroup;
import android.webkit.WebView;

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RNGoogleIMAAdsWrapper
  implements AdEvent.AdEventListener, AdErrorEvent.AdErrorListener, AdsLoader.AdsLoadedListener {

  // Live stream asset key.
  private static final String TEST_ASSET_KEY = "sN_IYUG8STe1ZzhIIE_ksA";

  // VOD HLS content source and video IDs.
  private static final String TEST_HLS_CONTENT_SOURCE_ID = "2528370";
  private static final String TEST_HLS_VIDEO_ID = "tears-of-steel";

  // VOD DASH content source and video IDs.
  private static final String TEST_DASH_CONTENT_SOURCE_ID = "2474148";
  private static final String TEST_DASH_VIDEO_ID = "bbb-clear";

  private static final String PLAYER_TYPE = "DAISamplePlayer";

  private enum ContentType {
    LIVE_HLS,
    VOD_HLS,
    VOD_DASH,
  }

  // Select a LIVE HLS stream. To play a VOD HLS stream or a VOD DASH stream, set CONTENT_TYPE to
  // the associated enum.
  private static final ContentType CONTENT_TYPE = ContentType.VOD_HLS;

  /**
   * Log interface, so we can output the log commands to the UI or similar.
   */
  public interface Logger {
    void log(String logMessage);
  }

  private final ImaSdkFactory sdkFactory;
  private AdsLoader adsLoader;
  private StreamDisplayContainer displayContainer;
  private StreamManager streamManager;
  private final List<VideoStreamPlayer.VideoStreamPlayerCallback> playerCallbacks;

  private final RNGoogleIMAVideoWrapper videoPlayer;
  private final Context context;
  private final ViewGroup adUiContainer;

  private String fallbackUrl;
  private Logger logger;

  /**
   * Creates a new SampleAdsWrapper that implements IMA direct-ad-insertion.
   *
   * @param context       the app's context.
   * @param videoPlayer   underlying HLS video player.
   * @param adUiContainer ViewGroup in which to display the ad's UI.
   */
  public RNGoogleIMAAdsWrapper(
    Context context,
    RNGoogleIMAVideoWrapper videoPlayer,
    ViewGroup adUiContainer) {
    this.videoPlayer = videoPlayer;
    this.context = context;
    this.adUiContainer = adUiContainer;
    sdkFactory = ImaSdkFactory.getInstance();
    playerCallbacks = new ArrayList<>();
    createAdsLoader();
  }

  @TargetApi(19)
  private void enableWebViewDebugging() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
      WebView.setWebContentsDebuggingEnabled(true);
    }
  }

  private void createAdsLoader() {
    ImaSdkSettings settings = sdkFactory.createImaSdkSettings();
    // Change any settings as necessary here.
    settings.setPlayerType(PLAYER_TYPE);
    enableWebViewDebugging();
    VideoStreamPlayer videoStreamPlayer = createVideoStreamPlayer();
    displayContainer =
      ImaSdkFactory.createStreamDisplayContainer(adUiContainer, videoStreamPlayer);
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
    adsLoader = sdkFactory.createAdsLoader(context, settings, displayContainer);
  }

  public void requestAndPlayAds() {
    adsLoader.addAdErrorListener(this);
    adsLoader.addAdsLoadedListener(this);
    adsLoader.requestStream(buildStreamRequest());
  }

  private StreamRequest buildStreamRequest() {
    StreamRequest request;
    switch (CONTENT_TYPE) {
      case LIVE_HLS:
        // Live HLS stream request.
        return sdkFactory.createLiveStreamRequest(TEST_ASSET_KEY, null);
      case VOD_HLS:
        // VOD HLS request.
        request =
          sdkFactory.createVodStreamRequest(
            TEST_HLS_CONTENT_SOURCE_ID, TEST_HLS_VIDEO_ID, null); // apiKey
        request.setFormat(StreamRequest.StreamFormat.HLS);
        return request;
      case VOD_DASH:
        // VOD DASH request.
        request =
          sdkFactory.createVodStreamRequest(
            TEST_DASH_CONTENT_SOURCE_ID, TEST_DASH_VIDEO_ID, null); // apiKey
        request.setFormat(StreamRequest.StreamFormat.DASH);
        return request;
      default:
        // Content type not selected.
        return null;
    }
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

  /**
   * AdErrorListener implementation
   */
  @Override
  public void onAdError(AdErrorEvent event) {
    log(String.format("Error: %s\n", event.getError().getMessage()));
    // play fallback URL.
    log("Playing fallback Url\n");
//        videoPlayer.setStreamUrl(fallbackUrl);
//        videoPlayer.enableControls(true);
//        videoPlayer.play();
    videoPlayer.playFallback();
  }

  /**
   * AdEventListener implementation
   */
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

  /**
   * AdsLoadedListener implementation
   */
  @Override
  public void onAdsManagerLoaded(AdsManagerLoadedEvent event) {
    streamManager = event.getStreamManager();
    streamManager.addAdErrorListener(this);
    streamManager.addAdEventListener(this);
    streamManager.init();
  }

  /**
   * Sets fallback URL in case ads stream fails.
   */
  void setFallbackUrl(String url) {
    fallbackUrl = url;
  }

  /**
   * Sets logger for displaying events to screen. Optional.
   */
  void setLogger(Logger logger) {
    this.logger = logger;
  }

  private void log(String message) {
    if (logger != null) {
      logger.log(message);
//    } else {
//      Log.d("ReactNative", message);
    }
  }
}
