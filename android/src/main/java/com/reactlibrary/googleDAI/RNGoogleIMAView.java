package com.reactlibrary.googleDAI;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.brentvatne.exoplayer.ReactExoplayerView;

import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.views.view.ReactViewGroup;


public class RNGoogleIMAView extends ReactViewGroup {
    private ThemedReactContext mThemedReactContext;
    private ReactViewGroup mAdContainerView;
    private String mContentSourceID;
    private String mVideoID;
    private SampleAdsWrapper mAdsWrapper;
    private ReadableMap mAdTagParameters;
    private ReactExoplayerView mVideoView;

    public RNGoogleIMAView(final ThemedReactContext themedReactContext) {
        super(themedReactContext);

        mThemedReactContext = themedReactContext;

        setOnHierarchyChangeListener(new OnHierarchyChangeListener() {
            @Override
            public void onChildViewAdded(View parent, View child) {
                log("onChildViewAdded");
                Object tag = child.getTag();
                if (tag != null && tag.toString().equals("adContainerView") && child instanceof ReactViewGroup) {
                    log("onChildViewAdded - adContainerView");
                    mAdContainerView = (ReactViewGroup) child;
                    adsWrapperSetup();
                } else if (child instanceof ViewGroup) {
                    ViewGroup childAsViewGroup = (ViewGroup) child;
                    if (childAsViewGroup.getChildAt(0) instanceof ReactExoplayerView) {
                        log("onChildViewAdded - ExoPlayerView");
                        mVideoView = (ReactExoplayerView) childAsViewGroup.getChildAt(0);
                        adsWrapperSetup();
                    } else {
                        log("onChildViewAdded - NOT ExoPlayerView");
                    }
                }
            }

            @Override
            public void onChildViewRemoved(View parent, View child) {
                if (child == mAdContainerView) {
                    mAdContainerView = null;
                    adsWrapperRemove();
                } else if (child == mVideoView) {
                    mVideoView = null;
                    adsWrapperRemove();
                }
            }
        });
    }

    void adsWrapperSetup() {
        if (mThemedReactContext != null && mVideoView != null && mAdContainerView != null) {
            mAdsWrapper = new SampleAdsWrapper(mThemedReactContext, mVideoView, mAdContainerView);
        }
    }

    void adsWrapperRemove() {
        if (mAdsWrapper != null) {
            mAdsWrapper.destroy();
            mAdsWrapper = null;
        }
//        mAdsWrapper = new SampleAdsWrapper(mThemedReactContext, null, mAdContainerView);
    }

    public void setContentSourceID(String contentSourceID) {
        mContentSourceID = contentSourceID;
    }

    public void setVideoID(String videoID) {
        mVideoID = videoID;
    }

    public void loadAds(String videoID, String contentSourceID, ReadableMap adTagParameters) {
        mVideoID = videoID;
        mContentSourceID = contentSourceID;
        mAdTagParameters = adTagParameters;
        log("loadAds");
        if (mAdsWrapper != null) {
            log("requestAndPlayVodStream");
            mAdsWrapper.requestAndPlayVodStream(mContentSourceID, mVideoID, mAdTagParameters);
        }
    }

//    searchAdContainer(ReactView view) {
//
//    }

    void log(String message) {
        Log.v(">>> RNGoogleIMAView", message);
    }

}