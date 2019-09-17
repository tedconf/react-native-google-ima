#import "RNGoogleIMA.h"
#import "RNGoogleIMAConverters.m"
#import "UIView+React.h"

static NSString *const statusKeyPath = @"status";
static NSString *const rctVideoNativeID = @"RNGoogleIMAPlayer";

@interface RNGoogleIMA () <IMAAdsLoaderDelegate, IMAStreamManagerDelegate, IMAAVPlayerVideoDisplayDelegate, IMAAdsManagerDelegate>


@end

@implementation RNGoogleIMA

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

- (instancetype)initWithEventDispatcher:(RCTEventDispatcher *)eventDispatcher
{
    if ((self = [super init])) {
        [self setupAdsLoader];
    }
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
    UIView* rctVideo = [self findRCTVideo:subview];
    if (rctVideo != nil) {
        [self setupRCTVideo:(RCTVideo *)rctVideo];
    }
}

- (void)removeReactSubview:(UIView *)subview
{
    [super removeReactSubview:subview];
    UIView* rctVideo = [self findRCTVideo:subview];
    if (rctVideo != nil) {
        [self setupRCTVideo:nil];
    }
}

-(void) setupRCTVideo: (RCTVideo *) rctVideo
{
    if (rctVideo != nil && rctVideo != _rctVideo) {
        if (_rctVideo != nil) {
            _rctVideo.rctVideoDelegate = nil;
        }
        _rctVideo = rctVideo;
        _rctVideo.rctVideoDelegate = self;
    } else if(rctVideo == nil) {
        // CLEANUP!
    }
}

-(BOOL) willSetupPlayerItem:(AVPlayerItem *) playerItem forSource:(NSDictionary *) source {
    if (_contentSourceID != nil && (_assetKey != nil || _videoID != nil)) {
        _fallbackPlayerItem = playerItem;
        _source = source;
        _contentPlayer = [AVPlayer playerWithPlayerItem:nil];
        [_contentPlayer setRate:0];
        [_contentPlayer pause];
        [self requestStreamForSource: source];
        return YES;
    }
    _contentPlayer = nil;
    _fallbackPlayerItem = nil;
    _source = nil;
    return NO;
}

-(void) playFallbackContent {
    if (_fallbackPlayerItem != nil) {
        [_contentPlayer replaceCurrentItemWithPlayerItem:_fallbackPlayerItem];
        [_rctVideo setupPlayerItem:_fallbackPlayerItem forSource:_source withPlayer:_contentPlayer];
    }
}

#pragma mark SDK Setup

- (void)setupAdsLoader {
    IMASettings* settings = [[IMASettings alloc] init];
    settings.autoPlayAdBreaks = NO;
    // settings.enableDebugMode = YES;
    _adsLoader = [[IMAAdsLoader alloc] initWithSettings:settings];
    _adsLoader.delegate = self;
}

- (void)requestStreamForSource:(NSDictionary *)source {
    // Create an ad display container for ad rendering.
    _adDisplayContainer = [[IMAAdDisplayContainer alloc] initWithAdContainer:_rctVideo companionSlots:nil];

    // Create an IMAAVPlayerVideoDisplay to give the SDK access to your video player.
    _avPlayerVideoDisplay = [[IMAAVPlayerVideoDisplay alloc] initWithAVPlayer:_contentPlayer];
    // avPlayerVideoDisplay.delegate = self;

    // Create a stream request. Use one of "Live stream request" or "VOD request".
    IMAStreamRequest *request;
    if (_assetKey != nil) {
        // Live stream request.
        request = [[IMALiveStreamRequest alloc] initWithAssetKey:_assetKey
                                              adDisplayContainer:_adDisplayContainer
                                                    videoDisplay:_avPlayerVideoDisplay];
    } else {
        // VOD request. Comment out the IMALiveStreamRequest above and uncomment this IMAVODStreamRequest
        // to switch from a livestream to a VOD stream.
        request = [[IMAVODStreamRequest alloc] initWithContentSourceID:_contentSourceID
                                                               videoID:_videoID
                                                    adDisplayContainer:_adDisplayContainer
                                                          videoDisplay:_avPlayerVideoDisplay];
    }
    if (_adTagParameters != nil) {
        [request setAdTagParameters:_adTagParameters];
    }
    [_adsLoader requestStreamWithRequest:request];
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
                                 @"adLoadedData": convertAdsLoadedData(adsLoadedData),
                                 @"target": self.reactTag
                                 });
    }
}

- (void)adsLoader:(IMAAdsLoader *)loader failedWithErrorData:(IMAAdLoadingErrorData *)adErrorData {
    // NSLog(@"IMA >>> adsLoader:failedWithErrorData");
    // [self playFallbackContent];
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
}

- (void)adsManager:(IMAAdsManager *)adsManager didReceiveAdError:(IMAAdError *)error {
    // NSLog(@"IMA >>> adsManager:didReceiveAdError");
    // Something went wrong with the ads manager after ads were loaded. Log the error and play the
    // content.
    // NSLog(@"AdsManager error: %@", error.message);
    // [self.contentPlayer play];
}

- (void)adsManagerDidRequestContentPause:(IMAAdsManager *)adsManager {
    // NSLog(@"IMA >>> adsManagerDidRequestContentPause");
    // The SDK is going to play ads, so pause the content.
    // [self.contentPlayer pause];
}

- (void)adsManagerDidRequestContentResume:(IMAAdsManager *)adsManager {
    // NSLog(@"IMA >>> adsManagerDidRequestContentResume");
    // The SDK is done playing ads (at least for now), so resume the content.
    // [self.contentPlayer play];
}

#pragma mark StreamManager Delegates

- (void)streamManager:(IMAStreamManager *)streamManager didReceiveAdEvent:(IMAAdEvent *)event {
    // NSLog(@"IMA >>> streamManager:didReceiveAdEvent %@", event.typeString);
    switch (event.type) {
        case kIMAAdEvent_STREAM_STARTED: {
            AVPlayerItem* playerItem = _contentPlayer.currentItem;
            [_rctVideo setupPlayerItem:playerItem forSource:_source withPlayer:_contentPlayer];
            [_rctVideo observeValueForKeyPath:statusKeyPath ofObject:playerItem change:nil context:nil];
            [_avPlayerVideoDisplay pause];
            break;
        }
        default:
            break;
    }

    if (self.onStreamManagerEvent) {
        self.onStreamManagerEvent(
                                  @{
                                    @"adEvent": convertAdEvent(event),
                                    @"target": self.reactTag
                                    });
    }
}

- (void)streamManager:(IMAStreamManager *)streamManager didReceiveAdError:(IMAAdError *)error {
    // NSLog(@"IMA >>> streamManager:didReceiveAdError");
    [self playFallbackContent];
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

// - (void)avPlayerVideoDisplay:(IMAAVPlayerVideoDisplay *)avPlayerVideoDisplay
//          willLoadStreamAsset:(AVURLAsset *)avUrlAsset {
//     NSLog(@"- (void)avPlayerVideoDisplay:(IMAAVPlayerVideoDisplay *)avPlayerVideoDisplay willLoadStreamAsset:(AVURLAsset *)avUrlAsset;");
//     [avPlayerVideoDisplay.player pause];
// }

@end
