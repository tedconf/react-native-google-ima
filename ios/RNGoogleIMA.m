#import "RNGoogleIMA.h"
#import "RNGoogleIMAConverters.m"
#import "UIView+React.h"

static NSString *const statusKeyPath = @"status";
static NSString *const rctVideoNativeID = @"RNGoogleIMAPlayer";

@interface RNGoogleIMA () <IMAAdsLoaderDelegate, IMAStreamManagerDelegate, IMAAVPlayerVideoDisplayDelegate, IMAAdsManagerDelegate>


@end

@implementation RNGoogleIMA

UIView* _adContainerView;
IMAAdDisplayContainer* _adDisplayContainer;
AVPlayer* _contentPlayer;
AVPlayerItem* _fallbackPlayerItem;
NSDictionary* _source;
RCTVideo* _rctVideo;
IMAAdsLoader* _adsLoader;
IMAStreamManager* _streamManager;
IMAAdsManager* _adsManager;
IMAAVPlayerVideoDisplay* _avPlayerVideoDisplay;
NSString* _contentSourceID;
NSString* _videoID;
NSString* _assetKey;
NSDictionary* _adTagParameters;
NSDictionary* _imaSettings;


- (instancetype)initWithEventDispatcher:(RCTEventDispatcher *)eventDispatcher
{
    self = [super init];
    return self;
}

- (void)setContentSourceID:(NSString *)contentSourceID
{
    _contentSourceID = ![contentSourceID isEqualToString:@""] ? contentSourceID : nil;
}

- (void)setVideoID:(NSString *)videoID
{
    _videoID = ![videoID isEqualToString:@""] ? videoID : nil;
}

- (void)setAssetKey:(NSString *)assetKey
{
    _assetKey = ![assetKey isEqualToString:@""] ? assetKey : nil;
}

- (void)setAdTagParameters:(NSDictionary *)adTagParameters
{
    _adTagParameters = adTagParameters != nil && adTagParameters.count > 0 ? adTagParameters : nil;
}

- (void)setImaSettings:(NSDictionary *)imaSettings
{
    _imaSettings = imaSettings != nil && imaSettings.count > 0 ? imaSettings : nil;
}

- (void)setEnabled:(BOOL)enabled
{
    _enabled = enabled;
}

- (void)setPlayFallbackContent:(BOOL)value
{
    [self playFallbackContent];
}

- (void)setComponentWillUnmount:(BOOL)value
{
    [self invalidatePlayer];
}



- (UIView *)findRCTVideo:(UIView *)view {
    UIView* rctVideo = nil;
    if ([view respondsToSelector:@selector(setRctVideoDelegate:)]) {
        rctVideo = view;
    } else {
        if (view.reactSubviews.count > 0) {
            for (int i = 0; i < view.reactSubviews.count; i++) {
                UIView* foundRCTVideo = [self findRCTVideo:[view.reactSubviews objectAtIndex:i]];
                if (foundRCTVideo != nil) {
                    rctVideo = foundRCTVideo;
                    break;
                }
            }
        }
    }
    return rctVideo;
}

- (void)insertReactSubview:(UIView *)subview atIndex:(NSInteger)atIndex
{
    [super insertReactSubview:subview atIndex:atIndex];
    if (subview.accessibilityIdentifier && [subview.accessibilityIdentifier isEqualToString:@"adContainerView"]) {
        _adContainerView = subview;
    } else {
        UIView* rctVideo = [self findRCTVideo:subview];
        if (rctVideo != nil) {
            // NSLog(@"IMA >>> Found rctVideo");
            [self configure:(RCTVideo *)rctVideo];
        }
    }

}

- (void)removeReactSubview:(UIView *)subview
{
    [super removeReactSubview:subview];

    if (subview.accessibilityIdentifier && [subview.accessibilityIdentifier isEqualToString:@"adContainerView"]) {
        _adContainerView = nil;
    } else {
        UIView* rctVideo = [self findRCTVideo:subview];
        if (rctVideo != nil) {
            [self configure:nil];
        }
    }
}

- (void)removeFromSuperview
{
    _adsLoader = nil;
    _adsLoader = nil;
    [self configure:nil];

    _fallbackPlayerItem = nil;
    _source = nil;

    [super removeFromSuperview];
}

-(void) configure:(RCTVideo *) rctVideo
{
    [self invalidateExistingAdDisplay];

    if(_rctVideo != nil && _rctVideo.rctVideoDelegate == self) {
        _rctVideo.rctVideoDelegate = nil;
        _rctVideo = nil;
    }

    if (rctVideo) {
        _rctVideo = rctVideo;
        _rctVideo.rctVideoDelegate = self;
        // Create an ad display container for ad rendering.
        _adDisplayContainer = [[IMAAdDisplayContainer alloc] initWithAdContainer:_adContainerView companionSlots:nil];

    }
}

- (void)invalidateExistingAdDisplay
{
    [self invalidatePlayer];

    _adDisplayContainer = nil;
}

- (void)invalidatePlayer
{
    if (_streamManager != nil) {
        [_streamManager destroy];
        _streamManager.delegate = nil;
        _streamManager = nil;
    }
    if (_avPlayerVideoDisplay != nil) {
        if (_avPlayerVideoDisplay.player != nil) {
            [_avPlayerVideoDisplay.player pause];
        }
        _avPlayerVideoDisplay = nil;
    }
    if (_adsManager != nil) {
        [_adsManager pause];
        [_adsManager destroy];
        _adsManager = nil;
    }
    if (_contentPlayer != nil) {
        [_contentPlayer pause];
        _contentPlayer = nil;
    }
    if (_adsLoader != nil) {
        _adsLoader.delegate = nil;
        _adsLoader = nil;
    }
}

-(BOOL) shouldSetupPlayerItem:(AVPlayerItem *) playerItem forSource:(NSDictionary *) source {
    if (_enabled) {
        if (_rctVideo && _contentSourceID != nil && (_assetKey != nil || _videoID != nil)) {
            _fallbackPlayerItem = playerItem;
            _source = source;

            // if (!_contentPlayer) {
            [self invalidatePlayer];

            _contentPlayer = [AVPlayer playerWithPlayerItem:nil];
            [_contentPlayer pause];
            [_contentPlayer setRate:0];

            _avPlayerVideoDisplay = [[IMAAVPlayerVideoDisplay alloc] initWithAVPlayer:_contentPlayer];
            // }

            [self requestStreamForSource: source];
            // NSLog(@"IMA >>> willSetupPlayerItem YES! uri: %@", [source objectForKey:@"uri"]);
            return YES;
        }
        _contentPlayer = nil;
        _fallbackPlayerItem = nil;
        _source = nil;
    }
    // NSLog(@"IMA >>> willSetupPlayerItem NO!");
    return NO;
}

-(void) playFallbackContent {
    if (_fallbackPlayerItem != nil) {
        [_contentPlayer replaceCurrentItemWithPlayerItem:_fallbackPlayerItem];
        [_rctVideo setupPlayerItem:_fallbackPlayerItem forSource:_source withPlayer:_contentPlayer];
        [_rctVideo observeValueForKeyPath:statusKeyPath ofObject:_fallbackPlayerItem change:nil context:nil];
    }
}

#pragma mark SDK Setup

- (void)requestStreamForSource:(NSDictionary *)source {
    if (_streamManager != nil) {
        _streamManager.delegate = nil;
        [_streamManager destroy];
        _streamManager = nil;
    }

    // Create a stream request. Use one of "Live stream request" or "VOD request".
    IMAStreamRequest *request;
    if (_assetKey != nil) {
        // Live stream request.
        request = [[IMALiveStreamRequest alloc] initWithAssetKey:_assetKey
                                              adDisplayContainer:_adDisplayContainer
                                                    videoDisplay:_avPlayerVideoDisplay];
    } else {
        request = [[IMAVODStreamRequest alloc] initWithContentSourceID:_contentSourceID
                                                               videoID:_videoID
                                                    adDisplayContainer:_adDisplayContainer
                                                          videoDisplay:_avPlayerVideoDisplay];
    }

    [request setAdTagParameters:_adTagParameters];

    [self setupAdsLoader];

    [_adsLoader requestStreamWithRequest:request];
}

- (void)setupAdsLoader {
    IMASettings* settings = [[IMASettings alloc] init];

    if (_imaSettings != nil) {
        if ([_imaSettings objectForKey:@"autoPlayAdBreaks"] != nil) {
            settings.autoPlayAdBreaks = [[_imaSettings objectForKey:@"autoPlayAdBreaks"] boolValue];
        }
        if ([_imaSettings objectForKey:@"disableNowPlayingInfo"] != nil) {
            settings.disableNowPlayingInfo = [[_imaSettings objectForKey:@"disableNowPlayingInfo"] boolValue];
        }
        if ([_imaSettings objectForKey:@"enableBackgroundPlayback"] != nil) {
            settings.enableBackgroundPlayback = [[_imaSettings objectForKey:@"enableBackgroundPlayback"] boolValue];
        }
        if ([_imaSettings objectForKey:@"enableDebugMode"] != nil) {
            settings.enableDebugMode = [[_imaSettings objectForKey:@"enableDebugMode"] boolValue];
        }
    }

    _adsLoader = [[IMAAdsLoader alloc] initWithSettings:settings];
    _adsLoader.delegate = self;
}

#pragma mark AdsLoader Delegates

- (void)adsLoader:(IMAAdsLoader *)loader adsLoadedWithData:(IMAAdsLoadedData *)adsLoadedData {
    // NSLog(@"IMA >>> adsLoader:adsLoadedWithData");
    // adsLoadedData.streamManager is set because we made an IMAStreamRequest.
    if (adsLoadedData.streamManager != nil) {
        _streamManager = adsLoadedData.streamManager;
        _streamManager.delegate = self;
        [_streamManager initializeWithAdsRenderingSettings:nil];
    } else {
        _streamManager = nil;
    }

    if (adsLoadedData.adsManager != nil) {
        _adsManager = adsLoadedData.adsManager;
        _adsManager.delegate = self;
        [_adsManager initializeWithAdsRenderingSettings:nil];
    } else {
        _adsManager = nil;
    }

    if (self.onAdsLoaderLoaded) {
        self.onAdsLoaderLoaded(
                               @{
                                 @"adsLoadedData": convertAdsLoadedData(adsLoadedData),
                                 @"target": self.reactTag
                                 });
    }
}

- (void)adsLoader:(IMAAdsLoader *)loader failedWithErrorData:(IMAAdLoadingErrorData *)adErrorData {
    // NSLog(@"IMA >>> adsLoader:failedWithErrorData");
    if (self.onAdsLoaderFailed) {
        self.onAdsLoaderFailed(
                               @{
                                 @"adErrorData": convertAdLoadingErrorData(adErrorData),
                                 @"target": self.reactTag
                                 });
    }
}

#pragma mark AdsManager Delegates

- (void)adsManager:(IMAAdsManager *)adsManager didReceiveAdEvent:(IMAAdEvent *)event {
    // NSLog(@"IMA >>> adsManager:didReceiveAdEvent");
    // When the SDK notified us that ads have been loaded, play them.
    // if (event.type == kIMAAdEvent_LOADED) {
    //     [adsManager start];
    // }

    // NSLog(@"IMA >>> onAdsManagerAdEvent");
    if (self.onAdsManagerAdEvent) {
        self.onAdsManagerAdEvent(
                                  @{
                                    @"adEvent": convertAdEvent(event),
                                    @"target": self.reactTag
                                    });
    }
}

- (void)adsManager:(IMAAdsManager *)adsManager didReceiveAdError:(IMAAdError *)error {
    // NSLog(@"IMA >>> adsManager:didReceiveAdError");
    // Something went wrong with the ads manager after ads were loaded. Log the error and play the
    // content.
    // NSLog(@"AdsManager error: %@", error.message);
    // [self.contentPlayer play];
    // [_contentPlayer play];
    // NSLog(@"IMA >>> onAdsManagerAdError");
    if (self.onAdsManagerAdError) {
        self.onAdsManagerAdError(
                                    @{
                                      @"error": convertAdError(error),
                                      @"target": self.reactTag
                                      });
    }
}

- (void)adsManagerDidRequestContentPause:(IMAAdsManager *)adsManager {
    // NSLog(@"IMA >>> adsManagerDidRequestContentPause");
    // NSLog(@"IMA >>> adsManagerDidRequestContentPause");
    // The SDK is going to play ads, so pause the content.
    // [self.contentPlayer pause];
}

- (void)adsManagerDidRequestContentResume:(IMAAdsManager *)adsManager {
    // NSLog(@"IMA >>> adsManagerDidRequestContentResume");
    // NSLog(@"IMA >>> adsManagerDidRequestContentResume");
    // The SDK is done playing ads (at least for now), so resume the content.
    // [self.contentPlayer play];
}

#pragma mark StreamManager Delegates

- (void)streamManager:(IMAStreamManager *)streamManager didReceiveAdEvent:(IMAAdEvent *)event {
    // NSLog(@"IMA >>> onStreamManagerAdEvent");
    // NSLog(@"IMA >>> streamManager:didReceiveAdEvent %@", event.typeString);
    switch (event.type) {
        case kIMAAdEvent_STREAM_STARTED: {

            [_contentPlayer pause];
            [_avPlayerVideoDisplay pause];
            AVPlayer* player = _avPlayerVideoDisplay.player;
            AVPlayerItem* playerItem = _avPlayerVideoDisplay.playerItem;
            [_rctVideo setupPlayerItem:playerItem forSource:_source withPlayer:player];
            [_rctVideo observeValueForKeyPath:statusKeyPath ofObject:playerItem change:nil context:nil];
            break;
        }
        default:
            break;
    }

    if (self.onStreamManagerAdEvent) {
        self.onStreamManagerAdEvent(
                                  @{
                                    @"adEvent": convertAdEvent(event),
                                    @"target": self.reactTag
                                    });
    }
}

- (void)streamManager:(IMAStreamManager *)streamManager didReceiveAdError:(IMAAdError *)error {
    NSLog(@"IMA >>> onStreamManagerAdError");
    if (self.onStreamManagerAdError) {
        self.onStreamManagerAdError(
                                    @{
                                      @"error": convertAdError(error),
                                      @"target": self.reactTag
                                      });
    }
}

- (void)streamManager:(IMAStreamManager *)streamManager adDidProgressToTime:(NSTimeInterval)progress adDuration:(NSTimeInterval)adDuration adPosition:(NSInteger)adPosition totalAds:(NSInteger)totalAds adBreakDuration:(NSTimeInterval)adBreakDuration {
    if (self.onStreamManagerAdProgress) {
        self.onStreamManagerAdProgress(
                                       @{
                                         @"streamManager": convertStreamManager(streamManager),
                                         @"progress": [NSNumber numberWithDouble:progress],
                                         @"adDuration": [NSNumber numberWithDouble:adDuration],
                                         @"adPosition": [NSNumber numberWithLong:adPosition],
                                         @"totalAds": [NSNumber numberWithLong:totalAds],
                                         @"adBreakDuration": [NSNumber numberWithDouble:adBreakDuration],
                                         @"target": self.reactTag
                                         });
    }
}

// #pragma mark AVPlayerVideoDisplay Delegates

 - (void)avPlayerVideoDisplay:(IMAAVPlayerVideoDisplay *)avPlayerVideoDisplay
          willLoadStreamAsset:(AVURLAsset *)avUrlAsset {
      NSLog(@"IMA >>> avPlayerVideoDisplay:willLoadStreamAsset:");
//     [avPlayerVideoDisplay.player pause];
 }

@end
