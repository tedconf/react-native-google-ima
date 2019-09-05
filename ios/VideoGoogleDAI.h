#include <AVFoundation/AVFoundation.h>
#import <React/RCTView.h>
#import <react-native-video/RCTVideo.h>
#import "UIView+React.h"

@import GoogleInteractiveMediaAds;
//@class RCTVideo;

@interface RCTVideoGoogleDAI : RCTView <RCTVideoDelegate>

- (instancetype)initWithEventDispatcher:(RCTEventDispatcher *)eventDispatcher NS_DESIGNATED_INITIALIZER;

- (void)requestStreamForSource:(NSDictionary *)source;
- (AVPlayer *)didSetupPlayerWithPlayerItem:(AVPlayerItem *) playerItem withSource:(NSDictionary *) source;

//- (RCTVideoGoogleDAI *)initWithRCTVideo:(RCTVideo *)rctVideo;

@end
