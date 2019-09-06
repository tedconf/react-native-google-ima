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
      <RCTRNGoogleIMA
        style={style}
        contentSourceID={contentSourceID}
        videoID={videoID}
        assetKey={assetKey}
        onAdsLoaderLoaded={({ nativeEvent: event }) => {
          console.log(`DAI >>> onAdsLoaderLoaded`, event.adLoadedData);
        }}
        onAdsLoaderFailed={({ nativeEvent: event }) => {
          console.log(`DAI >>> onAdsLoaderFailed`, event);
        }}
        onStreamManagerEvent={({ nativeEvent: event }) => {
          if (
            ['RESUME', 'STREAM_STARTED', 'STREAM_LOADED', 'STARTED'].indexOf(
              event.adEvent.type,
            ) !== -1
          ) {
            console.log(`DAI >>> onStreamManagerEvent`, event.adEvent);
          }
        }}
      >
        <RNVideo
          {...playerProps}
          nativeID="RNGoogleIMAPlayer"
          ref={this.onPlayerRef}
          style={styles.videoPlayer}
        />
      </RCTRNGoogleIMA>
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
