#include <AVFoundation/AVFoundation.h>
#import <React/RCTView.h>
#import <react-native-video/RCTVideo.h>
#import "UIView+React.h"

@import GoogleInteractiveMediaAds;

@interface RNGoogleIMA : RCTView <RCTVideoDelegate>

- (instancetype)initWithEventDispatcher:(RCTEventDispatcher *)eventDispatcher NS_DESIGNATED_INITIALIZER;

- (BOOL)willSetupPlayerItem:(AVPlayerItem *) playerItem forSource:(NSDictionary *) source;

@property (nonatomic, assign) BOOL enabled;

@property (nonatomic, copy) RCTBubblingEventBlock onAdsLoaderFailed;
@property (nonatomic, copy) RCTBubblingEventBlock onAdsLoaderLoaded;
@property (nonatomic, copy) RCTBubblingEventBlock onStreamManagerEvent;
@property (nonatomic, copy) RCTBubblingEventBlock onStreamManagerAdProgress;
@property (nonatomic, copy) RCTBubblingEventBlock onStreamManagerAdError;


@end
