import React from 'react';
import {
  requireNativeComponent,
  StyleSheet,
  ViewPropTypes,
} from 'react-native';
import PropTypes from 'prop-types';
import RNVideo from 'react-native-video';

const RCTRNGoogleIMA = requireNativeComponent('RNGoogleIMA', null);

class RNGoogleIMA extends React.PureComponent {
  plyerRef = null;

  onPlayerRef = ref => {
    const { playerRef } = this.props;
    this.playerRef = ref;
    if (playerRef) {
      playerRef(ref);
    }
  };

  seek = (time, tolerance) =>
    this.playerRef && this.playerRef.seek(time, tolerance);

  render() {
    const {
      playerRef,
      daiRef,
      contentSourceID,
      videoID,
      assetKey,
      style,
      ...playerProps
    } = this.props;

    return (
      <>
        <RCTRNGoogleIMA
          style={style}
          contentSourceID={contentSourceID}
          videoID={videoID}
          assetKey={assetKey}
          onAdsLoaderLoaded={({ nativeEvent: { adLoadedData } }) => {
            console.log(`DAI >>> onAdsLoaderLoaded`, adLoadedData);
          }}
          onAdsLoaderFailed={({ nativeEvent: { adErrorData } }) => {
            console.log(`DAI >>> onAdsLoaderFailed`, adErrorData);
          }}
          onStreamManagerEvent={({ nativeEvent: { adEvent } }) => {
            console.log(`DAI >>> onStreamManagerEvent`, adEvent);
          }}
        >
          <RNVideo
            {...playerProps}
            nativeID="RNGoogleIMAPlayer"
            ref={this.onPlayerRef}
            style={styles.videoPlayer}
          />
        </RCTRNGoogleIMA>
      </>
    );
  }
}

RNGoogleIMA.propTypes = {
  playerRef: PropTypes.func,
  daiRef: PropTypes.func,
  contentSourceID: PropTypes.string,
  videoID: PropTypes.string,
  assetKey: PropTypes.string,
  style: ViewPropTypes.style,
};

export default RNGoogleIMA;

const styles = StyleSheet.create({
  videoPlayer: {
    flex: 1,
  },
});
