package com.reactlibrary.googleIMA;

import androidx.annotation.NonNull;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.ViewGroupManager;
import com.facebook.react.uimanager.annotations.ReactProp;

import java.util.Map;

import javax.annotation.Nullable;

public class RNGoogleIMAManager extends ViewGroupManager<RNGoogleIMAView> {

  private static final String REACT_CLASS = "RNGoogleIMA";

  private static final String PROP_SRC = "src";

  ReactApplicationContext reactContext;

  public RNGoogleIMAManager(ReactApplicationContext reactContext) {
    this.reactContext = reactContext;
  }

  @NonNull
  @Override
  public String getName() {
    return REACT_CLASS;
  }

  @NonNull
  @Override
  protected RNGoogleIMAView createViewInstance(@NonNull ThemedReactContext reactContext) {
    return new RNGoogleIMAView(reactContext);
  }

  @ReactProp(name = "enabled", defaultBoolean = true)
  public void enabled(RNGoogleIMAView view, boolean enabled) {
    view.setEnabled(enabled);
  }

  @ReactProp(name = "contentSourceID")
  public void setContentSourceID(RNGoogleIMAView view, @Nullable String contentSourceID) {
    view.setContentSourceID(contentSourceID != null ? contentSourceID : "");
  }

  @ReactProp(name = "videoID")
  public void setVideoID(RNGoogleIMAView view, @Nullable String videoID) {
    view.setVideoID(videoID != null ? videoID : "");
  }

  @ReactProp(name = "assetKey")
  public void setAssetKey(RNGoogleIMAView view, @Nullable String assetKey) {
    view.setAssetKey(assetKey != null ? assetKey : "");
  }

  @ReactProp(name = "adTagParameters")
  public void setAdTagParameters(RNGoogleIMAView view, @Nullable ReadableMap adTagParameters) {
    view.setAdTagParameters(adTagParameters);
  }

  @ReactProp(name = "imaSettings")
  public void setImaSettings(RNGoogleIMAView view, @Nullable ReadableMap imaSettings) {
    view.setImaSettings(imaSettings);
  }

  @ReactProp(name = "playFallbackContent")
  public void setPlayFallbackContent(RNGoogleIMAView view, boolean playFallbackContent) {
    view.setPlayFallbackContent();
  }

  @ReactProp(name = "componentWillUnmount")
  public void setComponentWillUnmount(RNGoogleIMAView view, boolean componentWillUnmount) {
    view.setComponentWillUnmount(componentWillUnmount);
  }

  @Nullable
  @Override
  public Map<String, Object> getExportedCustomBubblingEventTypeConstants() {
    return MapBuilder.<String, Object>builder()
      .put(
        "onAdsLoaderLoaded",
        MapBuilder.of(
          "phasedRegistrationNames",
          MapBuilder.of("bubbled", "onAdsLoaderLoaded")))
      .put(
        "onAdsLoaderFailed",
        MapBuilder.of(
          "phasedRegistrationNames",
          MapBuilder.of("bubbled", "onAdsLoaderFailed")))
      .put(
        "onStreamManagerAdEvent",
        MapBuilder.of(
          "phasedRegistrationNames",
          MapBuilder.of("bubbled", "onStreamManagerAdEvent")))
      .put(
        "onStreamManagerAdProgress",
        MapBuilder.of(
          "phasedRegistrationNames",
          MapBuilder.of("bubbled", "onStreamManagerAdProgress")))
      .put(
        "onStreamManagerAdError",
        MapBuilder.of(
          "phasedRegistrationNames",
          MapBuilder.of("bubbled", "onStreamManagerAdError")))
      .put(
        "onAdsManagerAdEvent",
        MapBuilder.of(
          "phasedRegistrationNames",
          MapBuilder.of("bubbled", "onAdsManagerAdEvent")))
      .put(
        "onAdsManagerAdError",
        MapBuilder.of(
          "phasedRegistrationNames",
          MapBuilder.of("bubbled", "onAdsManagerAdError")))
      .build();
  }
}
