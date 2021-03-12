import React, { Component } from "react";
import { requireNativeComponent, ViewPropTypes, View } from "react-native";
import PropTypes from "prop-types";

class RNGoogleIMA extends Component {
  _assignRoot = (component) => {
    this._root = component;
  };

  playFallbackContent = () => {
    this._root.setNativeProps({ playFallbackContent: true });
  };

  componentWillUnmount() {
    this._root.setNativeProps({ componentWillUnmount: true });
  }

  render() {
    const {
      children,
      enabled = true,
      contentSourceID = null,
      videoID = null,
      assetKey = null,
      imaSettings = null,
      adTagParameters = null,
      onAdsLoaderLoaded = null,
      onAdsLoaderFailed = null,
      onStreamManagerAdEvent = null,
      onStreamManagerAdProgress = null,
      onStreamManagerAdError = null,
      onAdsManagerAdEvent = null,
      onAdsManagerAdError = null,
      adContainerStyle = null,
      style,
    } = this.props;

    return (
      <RCTRNGoogleIMA
        ref={this._assignRoot}
        enabled={enabled}
        style={style}
        contentSourceID={contentSourceID}
        videoID={videoID}
        assetKey={assetKey}
        imaSettings={imaSettings}
        adTagParameters={adTagParameters}
        onAdsLoaderLoaded={onAdsLoaderLoaded}
        onAdsLoaderFailed={onAdsLoaderFailed}
        onStreamManagerAdEvent={onStreamManagerAdEvent}
        onStreamManagerAdProgress={onStreamManagerAdProgress}
        onStreamManagerAdError={onStreamManagerAdError}
        onAdsManagerAdEvent={onAdsManagerAdEvent}
        onAdsManagerAdError={onAdsManagerAdError}
        pointerEvents="box-none"
      >
        <View
          testID="adContainerView"
          nativeID="adContainerView"
          style={adContainerStyle}
          pointerEvents="box-none"
        />
        {children}
      </RCTRNGoogleIMA>
    );
  }
}

RNGoogleIMA.propTypes = {
  enabled: PropTypes.bool,
  contentSourceID: PropTypes.string,
  videoID: PropTypes.string,
  assetKey: PropTypes.string,
  adTagParameters: PropTypes.objectOf(PropTypes.string),
  imaSettings: PropTypes.objectOf(PropTypes.bool),
  style: ViewPropTypes.style,
  adContainerStyle: ViewPropTypes.style,

  onAdsLoaderLoaded: PropTypes.func,
  onAdsLoaderFailed: PropTypes.func,
  onStreamManagerAdEvent: PropTypes.func,
  onStreamManagerAdProgress: PropTypes.func,
  onStreamManagerAdError: PropTypes.func,
  onAdsManagerAdEvent: PropTypes.func,
  onAdsManagerAdError: PropTypes.func,
};

// const RCTRNGoogleIMA = requireNativeComponent('RNGoogleIMA', null);

const RCTRNGoogleIMA = requireNativeComponent("RNGoogleIMA", RNGoogleIMA, {
  nativeOnly: {
    playFallbackContent: true,
    componentWillUnmount: true,
  },
});

export default RNGoogleIMA;
