/* eslint-disable import/no-extraneous-dependencies */
import React from 'react';
import { requireNativeComponent, ViewPropTypes } from 'react-native';
import PropTypes from 'prop-types';

const RCTRNGoogleIMA = requireNativeComponent('RNGoogleIMA', null);

const RNGoogleIMA = React.forwardRef((props, ref) => {
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
    style,
  } = props;

  React.useEffect(() => {
    if (RCTRNGoogleIMA) {
      ref({ ima: true });
    }
  }, [ref]);

  return (
    <RCTRNGoogleIMA
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
    >
      {children}
    </RCTRNGoogleIMA>
  );
});

RNGoogleIMA.propTypes = {
  enabled: PropTypes.bool,
  contentSourceID: PropTypes.string,
  videoID: PropTypes.string,
  assetKey: PropTypes.string,
  adTagParameters: PropTypes.objectOf(PropTypes.string),
  imaSettings: PropTypes.objectOf(PropTypes.bool),
  style: ViewPropTypes.style,

  onAdsLoaderLoaded: PropTypes.func,
  onAdsLoaderFailed: PropTypes.func,
  onStreamManagerAdEvent: PropTypes.func,
  onStreamManagerAdProgress: PropTypes.func,
  onStreamManagerAdError: PropTypes.func,
  onAdsManagerAdEvent: PropTypes.func,
  onAdsManagerAdError: PropTypes.func,
};

export default RNGoogleIMA;
