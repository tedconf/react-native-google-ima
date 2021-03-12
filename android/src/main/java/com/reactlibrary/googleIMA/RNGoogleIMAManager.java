package com.reactlibrary.googleIMA;

import androidx.annotation.NonNull;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.ViewGroupManager;
import com.facebook.react.uimanager.annotations.ReactProp;

import javax.annotation.Nullable;

public class RNGoogleIMAManager extends ViewGroupManager<com.ted.android.dai.RNGoogleIMAView> {

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
    protected com.ted.android.dai.RNGoogleIMAView createViewInstance(@NonNull ThemedReactContext reactContext) {
        return new com.ted.android.dai.RNGoogleIMAView(reactContext);
    }

    @ReactProp(name = "contentSourceID")
    public void setContentSourceID(com.ted.android.dai.RNGoogleIMAView view, @Nullable String contentSourceID) {
        view.setContentSourceID(contentSourceID != null ? contentSourceID : "");
    }

    @ReactProp(name = "videoID")
    public void setVideoID(com.ted.android.dai.RNGoogleIMAView view, @Nullable String videoID) {
        view.setVideoID(videoID != null ? videoID : "");
    }

    @ReactProp(name = "assetKey")
    public void setAssetKey(com.ted.android.dai.RNGoogleIMAView view, @Nullable String assetKey) {
        view.setAssetKey(assetKey != null ? assetKey : "");
    }

    @ReactProp(name = "adTagParameters")
    public void setAdTagParameters(com.ted.android.dai.RNGoogleIMAView view, @Nullable ReadableMap adTagParameters) {
        view.setAdTagParameters(adTagParameters);
    }
}
