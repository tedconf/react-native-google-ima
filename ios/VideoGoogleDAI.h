#include <AVFoundation/AVFoundation.h>
#import <React/RCTView.h>
#import <react-native-video/RCTVideo.h>
#import "UIView+React.h"

@import GoogleInteractiveMediaAds;

@interface RCTVideoGoogleDAI : RCTView <RCTVideoDelegate>

- (instancetype)initWithEventDispatcher:(RCTEventDispatcher *)eventDispatcher NS_DESIGNATED_INITIALIZER;

- (AVPlayer *)didSetupPlayerWithPlayerItem:(AVPlayerItem *) playerItem withSource:(NSDictionary *) source;

@end
