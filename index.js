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
    adTagParameters = null,
    onAdsLoaderLoaded = null,
    onAdsLoaderFailed = null,
    onStreamManagerEvent = null,
    onStreamManagerAdProgress = null,
    onStreamManagerAdError = null,
    style,
  } = props;

  React.useEffect(() => {
    if (RCTRNGoogleIMA) {
      ref({});
    }
  }, [ref]);

  return (
    <RCTRNGoogleIMA
      enabled={enabled}
      style={style}
      contentSourceID={contentSourceID}
      videoID={videoID}
      assetKey={assetKey}
      adTagParameters={adTagParameters}
      onAdsLoaderLoaded={onAdsLoaderLoaded}
      onAdsLoaderFailed={onAdsLoaderFailed}
      onStreamManagerEvent={onStreamManagerEvent}
      onStreamManagerAdProgress={onStreamManagerAdProgress}
      onStreamManagerAdError={onStreamManagerAdError}
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
  style: ViewPropTypes.style,

  onAdsLoaderLoaded: PropTypes.func,
  onAdsLoaderFailed: PropTypes.func,
  onStreamManagerEvent: PropTypes.func,
  onStreamManagerAdProgress: PropTypes.func,
  onStreamManagerAdError: PropTypes.func,
};

export default RNGoogleIMA;
