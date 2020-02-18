import React, { Component } from 'react';
import { requireNativeComponent, ViewPropTypes, View } from 'react-native';
import Video from 'react-native-video';
import PropTypes from 'prop-types';

class GoogleIMA extends Component {
  _assignRoot = component => {
    this._root = component;
  };

  playFallbackContent = () => {
    this._root.setNativeProps({ playFallbackContent: true });
  };

  componentDidMount() {
    this.loadAds();
  }

  componentWillUnmount() {
    this._root.setNativeProps({ componentWillUnmount: true });
  }

  componentDidUpdate(prevProps) {
    if (this.props.source !== prevProps.source) {
      this.loadAds();
    }
  }

  loadAds = () => {
    if (this.props.source) {
      const { contentSourceID, videoID, adTagParameters } = this.props;
      this._root.setNativeProps({
        loadAds: {
          contentSourceID,
          videoID,
          adTagParameters: {
            cust_params: adTagParameters?.cust_params || '',
            iu: adTagParameters?.iu || '',
          },
        },
      });
    }
  };

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
      ...playerProps
    } = this.props;

    return (
      <RNGoogleIMA
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
          style={adContainerStyle}
          pointerEvents="box-none"
        />
        <Video {...playerProps} source={undefined} />
      </RNGoogleIMA>
    );
  }
}

GoogleIMA.propTypes = {
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

const RNGoogleIMA = requireNativeComponent('RNGoogleIMA', GoogleIMA, {
  nativeOnly: {
    playFallbackContent: true,
    componentWillUnmount: true,
    loadAds: true,
  },
});

export default GoogleIMA;
