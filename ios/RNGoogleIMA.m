#import "RNGoogleIMA.h"
#import "RNGoogleIMAConverters.m"
#import "UIView+React.h"

static NSString *const statusKeyPath = @"status";
static NSString *const rctVideoNativeID = @"RNGoogleIMAPlayer";

@interface RNGoogleIMA () <IMAAdsLoaderDelegate, IMAStreamManagerDelegate, IMAAVPlayerVideoDisplayDelegate>


@end

@implementation RNGoogleIMA

IMAAdDisplayContainer* _adDisplayContainer;
AVPlayer* _contentPlayer;
AVPlayerItem* _fallbackPlayerItem;
NSDictionary* _source;
RCTVideo* _rctVideo;
IMAAdsLoader* _adsLoader;
IMAStreamManager* _streamManager;
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

- (BOOL)isRCTVideo:(UIView *)view {
    return [view.nativeID isEqualToString: rctVideoNativeID] && [view respondsToSelector:@selector(setRctVideoDelegate:)];
}

- (void)insertReactSubview:(UIView *)subview atIndex:(NSInteger)atIndex
{
    [super insertReactSubview:subview atIndex:atIndex];
    if ([self isRCTVideo:subview]) {
        [self setupRCTVideo:(RCTVideo *)subview];
    } else if (subview.reactSubviews.count > 0) {
        for (int i = 0; i < subview.reactSubviews.count; i++) {
            UIView* subsubview = [subview.reactSubviews objectAtIndex:i];
            if ([self isRCTVideo:subsubview]) {
                [self setupRCTVideo:(RCTVideo *)subsubview];
            }
        }
    }
}

- (void)removeReactSubview:(UIView *)subview
{
    [super removeReactSubview:subview];
    if ([subview.nativeID isEqualToString: rctVideoNativeID]) {
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
        [self requestStreamForSource: source];
        return YES;
    }
    _contentPlayer = nil;
    _fallbackPlayerItem = nil;
    _source = nil;
    return NO;
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
    // adsLoadedData.streamManager is set because we made an IMAStreamRequest.
    _streamManager = adsLoadedData.streamManager;
    _streamManager.delegate = self;
    [_streamManager initializeWithAdsRenderingSettings:nil];
    if (self.onAdsLoaderLoaded) {
        self.onAdsLoaderLoaded(
                               @{
                                 @"adLoadedData": convertAdsLoadedData(adsLoadedData),
                                 @"target": self.reactTag
                                 });
    }
}

- (void)adsLoader:(IMAAdsLoader *)loader failedWithErrorData:(IMAAdLoadingErrorData *)adErrorData {
    if (self.onAdsLoaderFailed) {
        [NSNumber numberWithLongLong:adErrorData.adError.type];
        [NSNumber numberWithLongLong:adErrorData.adError.code];
        self.onAdsLoaderFailed(
                               @{
                                 @"adErrorData": convertAdLoadingErrorData(adErrorData),
                                 @"target": self.reactTag
                                 });
    }
}

#pragma mark StreamManager Delegates

- (void)streamManager:(IMAStreamManager *)streamManager didReceiveAdEvent:(IMAAdEvent *)event {
    switch (event.type) {
        case kIMAAdEvent_STREAM_STARTED: {
            AVPlayerItem* playerItem = _contentPlayer.currentItem;
            [_rctVideo setupPlayerItem:playerItem forSource:_source withPlayer:_contentPlayer];
            [_rctVideo observeValueForKeyPath:statusKeyPath ofObject:playerItem change:nil context:nil];
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
