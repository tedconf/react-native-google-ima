#import "RNGoogleIMAManager.h"
#import "RNGoogleIMA.h"
#import <React/RCTBridge.h>
#import <React/RCTUIManager.h>
#import <AVFoundation/AVFoundation.h>

@implementation RNGoogleIMAManager

RCT_EXPORT_MODULE();

- (UIView *)view
{
    return [[RNGoogleIMA alloc] initWithEventDispatcher:self.bridge.eventDispatcher];
}

- (dispatch_queue_t)methodQueue
{
    return self.bridge.uiManager.methodQueue;
}

+ (BOOL)requiresMainQueueSetup
{
    return YES;
}
- (NSDictionary *)constantsToExport
{
    return @{
        @"googleIMA": @YES,
    };
}

RCT_EXPORT_VIEW_PROPERTY(enabled, BOOL);
RCT_EXPORT_VIEW_PROPERTY(contentSourceID, NSString);
RCT_EXPORT_VIEW_PROPERTY(videoID, NSString);
RCT_EXPORT_VIEW_PROPERTY(assetKey, NSString);
RCT_EXPORT_VIEW_PROPERTY(adTagParameters, NSDictionary);
RCT_EXPORT_VIEW_PROPERTY(imaSettings, NSDictionary);
RCT_EXPORT_VIEW_PROPERTY(playFallbackContent, BOOL);
RCT_EXPORT_VIEW_PROPERTY(componentWillUnmount, BOOL);

RCT_EXPORT_VIEW_PROPERTY(onAdsLoaderLoaded, RCTBubblingEventBlock);
RCT_EXPORT_VIEW_PROPERTY(onAdsLoaderFailed, RCTBubblingEventBlock);
RCT_EXPORT_VIEW_PROPERTY(onStreamManagerAdEvent, RCTBubblingEventBlock);
RCT_EXPORT_VIEW_PROPERTY(onStreamManagerAdProgress, RCTBubblingEventBlock);
RCT_EXPORT_VIEW_PROPERTY(onStreamManagerAdError, RCTBubblingEventBlock);
RCT_EXPORT_VIEW_PROPERTY(onAdsManagerAdEvent, RCTBubblingEventBlock);
RCT_EXPORT_VIEW_PROPERTY(onAdsManagerAdError, RCTBubblingEventBlock);


@end
