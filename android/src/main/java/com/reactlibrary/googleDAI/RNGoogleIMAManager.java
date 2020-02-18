package com.reactlibrary.googleDAI;

import android.util.Log;
import android.view.View;

// AppCompatCheckBox import for React Native pre-0.60:
// import androidx.appcompat.widget.AppCompatCheckBox;
// AppCompatCheckBox import for React Native 0.60(+):
// import androidx.appcompat.widget.AppCompatCheckBox;

import androidx.annotation.Nullable;

import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.ViewGroupManager;
import com.facebook.react.uimanager.annotations.ReactProp;

public class RNGoogleIMAManager extends ViewGroupManager<RNGoogleIMAView> {

    public static final String REACT_CLASS = "RNGoogleIMA";

    private ReactContext mReactContext;

    public RNGoogleIMAManager(ReactContext reactContext) {
        mReactContext = reactContext;
    }

    @Override
    public String getName() {
        return REACT_CLASS;
    }

    @Override
    public RNGoogleIMAView createViewInstance(ThemedReactContext c) {
        // TODO: Implement some actually useful functionality
        Log.v(">>> IMA", "createViewInstance");
        return new RNGoogleIMAView(c);
    }

    @ReactProp(name = "loadAds")
    public void loadAds(final RNGoogleIMAView videoView, @Nullable ReadableMap loadAdsParams) {
        @Nullable ReadableMap adTagParameters = loadAdsParams.getMap("adTagParameters");
        Log.v(">>> IMA", "setSrc");
        videoView.loadAds(
            loadAdsParams.getString("videoID"),
            loadAdsParams.getString("contentSourceID"),
            adTagParameters
        );
//        int mainVer = src.getInt(PROP_SRC_MAINVER);
//        int patchVer = src.getInt(PROP_SRC_PATCHVER);
//        if(mainVer<0) { mainVer = 0; }
//        if(patchVer<0) { patchVer = 0; }
//        if(mainVer>0) {
//            videoView.setSrc(
//                    src.getString(PROP_SRC_URI),
//                    src.getString(PROP_SRC_TYPE),
//                    src.getBoolean(PROP_SRC_IS_NETWORK),
//                    src.getBoolean(PROP_SRC_IS_ASSET),
//                    src.getMap(PROP_SRC_HEADERS),
//                    mainVer,
//                    patchVer
//            );
//        }
//        else {
//            videoView.setSrc(
//                    src.getString(PROP_SRC_URI),
//                    src.getString(PROP_SRC_TYPE),
//                    src.getBoolean(PROP_SRC_IS_NETWORK),
//                    src.getBoolean(PROP_SRC_IS_ASSET),
//                    src.getMap(PROP_SRC_HEADERS)
//            );
//        }
    }

    @ReactProp(name = "src")
    public void setSrc(final RNGoogleIMAView videoView, @Nullable ReadableMap src) {
        Log.v(">>> IMA", "setSrc");
//        int mainVer = src.getInt(PROP_SRC_MAINVER);
//        int patchVer = src.getInt(PROP_SRC_PATCHVER);
//        if(mainVer<0) { mainVer = 0; }
//        if(patchVer<0) { patchVer = 0; }
//        if(mainVer>0) {
//            videoView.setSrc(
//                    src.getString(PROP_SRC_URI),
//                    src.getString(PROP_SRC_TYPE),
//                    src.getBoolean(PROP_SRC_IS_NETWORK),
//                    src.getBoolean(PROP_SRC_IS_ASSET),
//                    src.getMap(PROP_SRC_HEADERS),
//                    mainVer,
//                    patchVer
//            );
//        }
//        else {
//            videoView.setSrc(
//                    src.getString(PROP_SRC_URI),
//                    src.getString(PROP_SRC_TYPE),
//                    src.getBoolean(PROP_SRC_IS_NETWORK),
//                    src.getBoolean(PROP_SRC_IS_ASSET),
//                    src.getMap(PROP_SRC_HEADERS)
//            );
//        }
    }

    @ReactProp(name = "contentSourceID")
    public void setContentSourceID(final RNGoogleIMAView imaView, final String contentSourceID) {
        Log.v(">>> IMA", "setContentSourceID ".concat(contentSourceID));
        imaView.setContentSourceID(contentSourceID);
    }

    @ReactProp(name = "videoID")
    public void setVideoID(final RNGoogleIMAView imaView, final String videoID) {
        Log.v(">>> IMA", "setVideoID ".concat(videoID));
        imaView.setVideoID(videoID);
    }
}



//        contentSourceID={contentSourceID}
//                videoID={currentMedia?.mediaSlug}
