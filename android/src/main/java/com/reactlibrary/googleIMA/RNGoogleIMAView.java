package com.reactlibrary.googleIMA;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import com.brentvatne.exoplayer.ReactExoplayerView;
import com.brentvatne.exoplayer.ReactExoplayerViewDelegateInterface;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableMapKeySetIterator;
import com.facebook.react.bridge.ReadableNativeMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.events.RCTEventEmitter;
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
  StreamManagerEventBridgeDelegate, AdsLoaderEventBridgeDelegate {

  private static final String TAG = "ReactExoplayerView";

  private boolean shouldSetupPlayer = false;
  private boolean playingFeedbackContent = false;

  // Ads Wrapper
  private ImaSdkFactory sdkFactory;
  private AdsLoader adsLoader;
  private StreamDisplayContainer adDisplayContainer;
  private StreamManager streamManager;
  private List<VideoStreamPlayer.VideoStreamPlayerCallback> playerCallbacks;

  private RNGoogleIMAVideoWrapper videoPlayer;
  private Context context;
  private ViewGroup adUiContainer;
  private FrameLayout innerLayout;

  private String fallbackUrl;
  private Logger logger;

  //

  String contentSourceID = "";
  String videoID = "";
  String assetKey = "";
  HashMap<String, String> adTagParameters;
  ReadableNativeMap _imaSettings;

  StreamManagerEventBridge streamManagerEventBridge;
  AdsLoaderEventBridge adsLoaderEventBridge;


  public RNGoogleIMAView(@NonNull Context context) {
    super(context);
    streamManagerEventBridge = new StreamManagerEventBridge(this);
    adsLoaderEventBridge = new AdsLoaderEventBridge(this);
    initialize();

    addOnLayoutChangeListener(this::onLayoutChange);
  }

  private void initialize() {
    sdkFactory = ImaSdkFactory.getInstance();
    playerCallbacks = new ArrayList<>();
  }

  public void setContentSourceID(String contentSourceID) {
    this.contentSourceID = contentSourceID;
  }

  public void setVideoID(String videoID) {
    this.videoID = videoID;
    playingFeedbackContent = false;
  }

  public void setAssetKey(String assetKey) {
    this.assetKey = assetKey;
  }

  public void setAdTagParameters(ReadableMap adTagParameters) {
    this.adTagParameters = new HashMap<>();
    if (adTagParameters != null) {
      ReadableMapKeySetIterator iterator = adTagParameters.keySetIterator();
      while (iterator.hasNextKey()) {
        String key = iterator.nextKey();
        this.adTagParameters.put(key, adTagParameters.getString(key));
      }
    }
  }

  public void setImaSettings(ReadableMap imaSettings) {

  }

  public void setPlayFallbackContent() {
    if (videoPlayer != null) {
      this.playingFeedbackContent = true;
      videoPlayer.playFallback();
    }

  }

  public void setComponentWillUnmount(boolean componentWillUnmount) {

  }

  @Override
  public void addView(View child, int index, LayoutParams params) {
    super.addView(child, index, params);
    ReactExoplayerView reactExoplayerView = ReactExoplayerView.findRCTVideo(child);
    if (reactExoplayerView != null) {
      initializeVideo(new RNGoogleIMAVideoWrapper(reactExoplayerView));
    }
  }

  @Override
  public MediaSource buildMediaSource(ReactExoplayerView reactExoplayerView, Uri uri, String overrideExtension) {
    return null;
  }

  @Override
  public boolean setSrc(ReactExoplayerView reactExoplayerView, Uri uri, String extension, Map<String, String> headers) {
    shouldSetupPlayer = false;
    if (isEnabled()) {
      if (videoPlayer != null) {
        videoPlayer.setFallbackUri(uri);
        videoPlayer.setFallbackExtension(extension);
        videoPlayer.setFallbackHeaders(headers);
      }
    }
    shouldSetupPlayer = requestStream();
    return shouldSetupPlayer;
  }

  private void invalidateExistingAdDisplay() {
    this.invalidatePlayer();

    adDisplayContainer = null;
  }

  void invalidatePlayer() {
    invalidateStreamManager();
    invalidateAdsLoader();
  }

  void invalidateStreamManager() {
    if (streamManager != null) {
      StreamManager sm = streamManager;
      streamManager = null;
      sm.removeAdErrorListener(streamManagerEventBridge);
      sm.removeAdEventListener(streamManagerEventBridge);
      sm.destroy();
    }
  }

  void invalidateAdsLoader() {
    if (adsLoader != null) {
      AdsLoader al = adsLoader;
      adsLoader = null;
      al.removeAdErrorListener(adsLoaderEventBridge);
      al.removeAdsLoadedListener(adsLoaderEventBridge);
    }
  }

  private void initializeVideo(RNGoogleIMAVideoWrapper videoPlayer) {
    this.invalidateExistingAdDisplay();

    if (this.videoPlayer != null && this.videoPlayer != videoPlayer) {
      this.videoPlayer.setDelegate(null);
      this.videoPlayer = null;
    }

    if (videoPlayer != null && this.videoPlayer != videoPlayer) {
      this.videoPlayer = videoPlayer;
      this.videoPlayer.setDelegate(this);
    }
  }

  private void setupAdsLoader() {
    ImaSdkSettings settings = sdkFactory.createImaSdkSettings();
    settings.setPlayerType("RNGoogleIMA");

    VideoStreamPlayer videoStreamPlayer = createVideoStreamPlayer();
    adUiContainer = (ViewGroup) ReactFindViewUtil.findView(this, "adContainerView");
    if (adUiContainer == null) {
      return;
    }

    adUiContainer.removeOnLayoutChangeListener(this::onLayoutChangeAdUiContainer);
    adUiContainer.addOnLayoutChangeListener(this::onLayoutChangeAdUiContainer);
    adUiContainer.removeAllViews();
    reLayout();

    adDisplayContainer =
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
    adsLoader = sdkFactory.createAdsLoader(getContext(), settings, adDisplayContainer);
  }

  @Override
  protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
    super.onLayout(changed, left, top, right, bottom);

    reLayout();
  }

  public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
    reLayout();
  }

  public void onLayoutChangeAdUiContainer(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
    reLayout();
  }

  private void reLayout() {
    if (adUiContainer == null) return;
    int l = adUiContainer.getChildCount();

    int measuredWidth = adUiContainer.getMeasuredWidth();
    int measureSpecWidth = MeasureSpec.makeMeasureSpec(measuredWidth, MeasureSpec.EXACTLY);
    int measuredHeight = adUiContainer.getMeasuredHeight();
    int measureSpecHeight = MeasureSpec.makeMeasureSpec(measuredHeight, MeasureSpec.EXACTLY);

    for (int i = 0; i < l; i++) {
      View view = adUiContainer.getChildAt(i);
      view.measure(measureSpecWidth, measureSpecHeight);
      view.layout(view.getLeft(), view.getTop(), view.getMeasuredWidth(), view.getMeasuredHeight());
    }

  }

  @TargetApi(19)
  private void enableWebViewDebugging() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
      WebView.setWebContentsDebuggingEnabled(true);
    }
  }

  public boolean requestStream() {
    if (videoPlayer == null) {
      return false;
    }
    StreamRequest streamRequest = buildStreamRequest();
    if (streamRequest == null) {
      return false;
    }
//    invalidatePlayer();
    streamRequest.setAdTagParameters(adTagParameters);

    setupAdsLoader();

    adsLoader.addAdErrorListener(adsLoaderEventBridge);
    adsLoader.addAdsLoadedListener(adsLoaderEventBridge);
    adsLoader.requestStream(streamRequest);
    return true;
  }

  private StreamRequest buildStreamRequest() {
    StreamRequest request = null;
    if (!assetKey.isEmpty()) {
      // Live HLS stream request.
      return sdkFactory.createLiveStreamRequest(assetKey, null);
    } else if (!videoID.isEmpty() && !contentSourceID.isEmpty()) {
      request =
        sdkFactory.createVodStreamRequest(
          contentSourceID, videoID, null);
      request.setFormat(StreamRequest.StreamFormat.HLS);
    }

    return request;
  }

  private VideoStreamPlayer createVideoStreamPlayer() {
    return new VideoStreamPlayer() {
      @Override
      public void loadUrl(String url, List<HashMap<String, String>> subtitles) {
        if (!playingFeedbackContent) {
          videoPlayer.setStreamUrl(url);
          videoPlayer.play();
        }
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
        long positionMs = 0;
        long duration = 0;
        try {
          positionMs = videoPlayer.getCurrentPositionMs();
          duration = videoPlayer.getDuration();
        } catch(Exception e) {
          //
        }
        return new VideoProgressUpdate(positionMs, duration);
      }
    };
  }

  /**
   * StreamManager Events
   */
  public void onStreamManagerAdError(AdErrorEvent adErrorEvent) {
    sendEvent("onStreamManagerAdError", RNGoogleIMAConverters.convertAdErrorEvent(adErrorEvent));
  }

  public void onStreamManagerAdEvent(AdEvent adEvent) {
//    log(adEvent.getType().toString());
    List<CuePoint> cuepoints = null;
    if (this.streamManager != null) {
      cuepoints = this.streamManager.getCuePoints();
    }
    WritableMap map = RNGoogleIMAConverters.convertAdEvent(adEvent, cuepoints);
    sendEvent("onStreamManagerAdEvent", map);
  }

  /**
   * AdsLoader Events
   */
  public void onAdsLoaderAdsLoadedWithData(AdsManagerLoadedEvent adsLoadedDataEvent) {
    invalidateStreamManager();
    streamManager = adsLoadedDataEvent.getStreamManager();
    streamManager.addAdErrorListener(streamManagerEventBridge);
    streamManager.addAdEventListener(streamManagerEventBridge);
    streamManager.init();

    sendEvent("onAdsLoaderLoaded", RNGoogleIMAConverters.convertAdsLoadedData(adsLoadedDataEvent));
  }

  public void onAdsLoaderAdError(AdErrorEvent adErrorEvent) {
    // TODO: bubble
    sendEvent("onAdsLoaderFailed", RNGoogleIMAConverters.convertAdErrorEvent(adErrorEvent));
  }

  /**
   * Sets logger for displaying events to screen. Optional.
   */
  void setLogger(Logger logger) {
    this.logger = logger;
  }

  private void log(String message) {
//    if (logger != null) {
//      logger.log(message);
//    }
    Log.d("DAI", message);
  }

  public boolean sendEvent(String eventName, WritableMap eventData) {
    ReactContext reactContext = (ReactContext) getContext();

    if (reactContext != null) {
      reactContext.getJSModule(RCTEventEmitter.class).receiveEvent(
        getId(),
        eventName,
        eventData);
    }

    return true;
  }
}

class AdsLoaderEventBridge implements AdErrorEvent.AdErrorListener, AdsLoader.AdsLoadedListener {

  final AdsLoaderEventBridgeDelegate delegate;

  AdsLoaderEventBridge(AdsLoaderEventBridgeDelegate delegate) {
    this.delegate = delegate;
  }

  @Override
  public void onAdError(AdErrorEvent adErrorEvent) {
    delegate.onAdsLoaderAdError(adErrorEvent);
  }

  @Override
  public void onAdsManagerLoaded(AdsManagerLoadedEvent adsManagerLoadedEvent) {
    delegate.onAdsLoaderAdsLoadedWithData(adsManagerLoadedEvent);
  }
}

interface AdsLoaderEventBridgeDelegate {
  void onAdsLoaderAdError(AdErrorEvent adErrorEvent);

  void onAdsLoaderAdsLoadedWithData(AdsManagerLoadedEvent adsManagerLoadedEvent);
}

interface Logger {
  void log(String logMessage);
}

interface StreamManagerEventBridgeDelegate {
  void onStreamManagerAdError(AdErrorEvent adErrorEvent);

  void onStreamManagerAdEvent(AdEvent adEvent);
}

class StreamManagerEventBridge implements AdErrorEvent.AdErrorListener, AdEvent.AdEventListener {

  final StreamManagerEventBridgeDelegate delegate;

  StreamManagerEventBridge(StreamManagerEventBridgeDelegate delegate) {
    this.delegate = delegate;
  }

  @Override
  public void onAdError(AdErrorEvent adErrorEvent) {
    delegate.onStreamManagerAdError(adErrorEvent);
  }

  @Override
  public void onAdEvent(AdEvent adEvent) {
    delegate.onStreamManagerAdEvent(adEvent);
  }
}
