import React from 'react';
import {
  requireNativeComponent,
  StyleSheet,
  ViewPropTypes,
} from 'react-native';
import PropTypes from 'prop-types';
import RNVideo from 'react-native-video';

const RCTVideoGoogleDAI = requireNativeComponent('RCTVideoGoogleDAI', null);

const VideoGoogleDAI = ({
  playerRef,
  daiRef,
  contentSourceID,
  videoID,
  assetKey,
  style,
  ...playerProps
}) => {
  const videoRef = React.useRef();

  React.useEffect(() => {
    if (videoRef.current) {
      playerRef(videoRef.current);
    }
  }, [playerRef]);

  return (
    <>
      <RCTVideoGoogleDAI
        ref={daiRef}
        style={style}
        contentSourceID={contentSourceID}
        videoID={videoID}
        assetKey={assetKey}
      >
        <RNVideo
          nativeID="RCTVideoGoogleDAI"
          {...playerProps}
          ref={videoRef}
          style={styles.videoPlayer}
        />
      </RCTVideoGoogleDAI>
    </>
  );
};

VideoGoogleDAI.propTypes = {
  playerRef: PropTypes.func,
  daiRef: PropTypes.func,
  contentSourceID: PropTypes.string,
  videoID: PropTypes.string,
  assetKey: PropTypes.string,
  style: ViewPropTypes,
};

export default VideoGoogleDAI;

const styles = StyleSheet.create({
  videoPlayer: {
    flex: 1,
  },
});
