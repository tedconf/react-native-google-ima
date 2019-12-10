package com.reactlibrary.googleDAI;

import android.view.View;

// AppCompatCheckBox import for React Native pre-0.60:
// import android.support.v7.widget.AppCompatCheckBox;
// AppCompatCheckBox import for React Native 0.60(+):
// import androidx.appcompat.widget.AppCompatCheckBox;

import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;

public class VideoGoogleDAIManager extends SimpleViewManager<View> {

    public static final String REACT_CLASS = "VideoGoogleDAI";

    @Override
    public String getName() {
        return REACT_CLASS;
    }

    @Override
    public View createViewInstance(ThemedReactContext c) {
        // TODO: Implement some actually useful functionality
        return new View(c);
    }
}
