#import "VideoGoogleDAI.h"

static NSString *const statusKeyPath = @"status";
static NSString *const rctVideoNativeID = @"RCTVideoGoogleDAI";

@interface RCTVideoGoogleDAI () <IMAAdsLoaderDelegate, IMAStreamManagerDelegate, IMAAVPlayerVideoDisplayDelegate>


@end

@implementation RCTVideoGoogleDAI

IMAAdDisplayContainer* _adDisplayContainer;
AVPlayer* _contentPlayer;
RCTVideo* _rctVideo;
IMAAdsLoader* _adsLoader;
IMAStreamManager* _streamManager;
IMAAVPlayerVideoDisplay* _avPlayerVideoDisplay;
NSString* _contentSourceID;
NSString* _videoID;
NSString* _assetKey;

- (instancetype)initWithEventDispatcher:(RCTEventDispatcher *)eventDispatcher
{
    if ((self = [super init])) {
        [self setupAdsLoader];
    }
    return self;
}

- (void)setContentSourceID:(NSString *)contentSourceID
{
    _contentSourceID = contentSourceID;
}

- (void)setVideoID:(NSString *)videoID
{
    _videoID = videoID;
}

- (void)setAssetKey:(NSString *)assetKey
{
    _assetKey = assetKey;
}

- (void)insertReactSubview:(UIView *)subview atIndex:(NSInteger)atIndex
{
    NSLog(@"IMA >>> insertReactSubview");
    [super insertReactSubview:subview atIndex:atIndex];
    if ([subview.nativeID isEqualToString: rctVideoNativeID]) {
        NSLog(@"IMA >>> insertReactSubview is RCTVideoGoogleDAI");
        [self setupRCTVideo:(RCTVideo *)subview];
    }
}

- (void)removeReactSubview:(UIView *)subview
{
    [super removeReactSubview:subview];
    NSLog(@"IMA >>> removeReactSubview");
    if ([subview.nativeID isEqualToString: rctVideoNativeID]) {
        NSLog(@"IMA >>> removeReactSubview was RCTVideoGoogleDAI");
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

-(AVPlayer *) didSetupPlayerWithPlayerItem:(AVPlayerItem *) playerItem withSource:(NSDictionary *) source {
    NSLog(@"IMA >>> didSetupPlayerWithPlayerItem:withSource");
    if (_contentSourceID != nil && (_assetKey != nil || _videoID != nil)) {
        _contentPlayer = [AVPlayer playerWithPlayerItem:nil];
        [self requestStreamForSource: source];
        return _contentPlayer;
    }
    return nil;
}

#pragma mark SDK Setup

- (void)setupAdsLoader {
    NSLog(@"IMA >>> setupAdsLoader");
    IMASettings* settings = [[IMASettings alloc] init];
    settings.autoPlayAdBreaks = NO;
    settings.enableDebugMode = YES;
    _adsLoader = [[IMAAdsLoader alloc] initWithSettings:settings];
    _adsLoader.delegate = self;
}

- (void)requestStreamForSource:(NSDictionary *)source {
    NSLog(@"IMA >>> requestStreamForSource");
    // Create an ad display container for ad rendering.
    _adDisplayContainer = [[IMAAdDisplayContainer alloc] initWithAdContainer:_rctVideo companionSlots:nil];

    // Create an IMAAVPlayerVideoDisplay to give the SDK access to your video player.
    _avPlayerVideoDisplay = [[IMAAVPlayerVideoDisplay alloc] initWithAVPlayer:_contentPlayer];
    // avPlayerVideoDisplay.delegate = self;

    // Create a stream request. Use one of "Live stream request" or "VOD request".
    IMAStreamRequest *request;
    if (_assetKey) {
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
    [request setAdTagParameters:@{@"cust_params":@"dfptest=REACT_NATIVE_DEV", @"iu":@"/5641/ted3/mobile"}];
    [_adsLoader requestStreamWithRequest:request];
}

#pragma mark AdsLoader Delegates

- (void)adsLoader:(IMAAdsLoader *)loader adsLoadedWithData:(IMAAdsLoadedData *)adsLoadedData {
//    NSLog(@"IMA >>> Stream created with: %@.", adsLoadedData.streamManager.streamId);
    // adsLoadedData.streamManager is set because we made an IMAStreamRequest.
    _streamManager = adsLoadedData.streamManager;
    _streamManager.delegate = self;
    [_streamManager initializeWithAdsRenderingSettings:nil];
}

- (void)adsLoader:(IMAAdsLoader *)loader failedWithErrorData:(IMAAdLoadingErrorData *)adErrorData {
    // Something went wrong loading ads. Log the error and play the content.
    NSLog(@"IMA >>> AdsLoader error, code:%ld, message: %@", adErrorData.adError.code,
          adErrorData.adError.message);
//    [self.contentPlayer play];
}

#pragma mark StreamManager Delegates

- (void)streamManager:(IMAStreamManager *)streamManager didReceiveAdEvent:(IMAAdEvent *)event {
//    NSLog(@"IMA >>> StreamManager event (%@).", event.typeString);
    switch (event.type) {

        /**
        *  Stream has loaded (only used for dynamic ad insertion).
        */
        case kIMAAdEvent_STREAM_LOADED: {
//            if (_rctVideo.paused) {
//                [_avPlayerVideoDisplay pause];
//            }
            NSLog(@"IMA >>> StreamManager event (%@/%@).", event.typeString, @"kIMAAdEvent_STREAM_LOADED");
            break;
        }

        /**
        *  Stream has started playing (only used for dynamic ad insertion). Start
        *  Picture-in-Picture here if applicable.
        */
        case kIMAAdEvent_STREAM_STARTED: {
            NSLog(@"IMA >>> StreamManager event (%@/%@).", event.typeString, @"kIMAAdEvent_STREAM_STARTED");
            NSLog(@"IMA >>> self->_player.currentItem.duration: %f", CMTimeGetSeconds(_contentPlayer.currentItem.duration));
            AVPlayerItem* playerItem = _contentPlayer.currentItem;
            NSDictionary* source = [[NSDictionary alloc] init];
            [_rctVideo setupWithPlayer:_contentPlayer playerItem:playerItem source:source];
            [_rctVideo observeValueForKeyPath:statusKeyPath ofObject:playerItem change:nil context:nil];
            break;
        }

        #pragma mark ADEvent - Advertising Events
        /**
        *  Ad break ready.
        */
        case kIMAAdEvent_AD_BREAK_READY: {
            NSLog(@"IMA >>> StreamManager event (%@/%@).", event.typeString, @"kIMAAdEvent_AD_BREAK_READY");
            break;
        }
        /**
        *  Ad break started (only used for dynamic ad insertion).
        */
        case kIMAAdEvent_AD_BREAK_STARTED: {
            // [_avPlayerVideoDisplay pause];
            NSLog(@"IMA >>> StreamManager event (%@/%@).", event.typeString, @"kIMAAdEvent_AD_BREAK_STARTED");
            NSLog(@"Ad break started");
            break;
        }
        /**
        *  Ad break ended (only used for dynamic ad insertion).
        */
        case kIMAAdEvent_AD_BREAK_ENDED: {
            NSLog(@"IMA >>> StreamManager event (%@/%@).", event.typeString, @"kIMAAdEvent_AD_BREAK_ENDED");
            NSLog(@"Ad break ended");
            break;
        }
        /**
        *  Ad period started is fired when an ad period starts. This includes the
        *  entire ad break including slate as well. This event will be fired even for
        *  ads that are being replayed or when seeking to the middle of an ad break.
        *  (only used for dynamic ad insertion).
        */
        case kIMAAdEvent_AD_PERIOD_STARTED: {
            NSLog(@"IMA >>> StreamManager event (%@/%@).", event.typeString, @"kIMAAdEvent_AD_PERIOD_STARTED");
            NSLog(@"Ad period started");
            // [self->_player pause];
            break;
        }
        /**
        *  Ad period ended (only used for dynamic ad insertion).
        */
        case kIMAAdEvent_AD_PERIOD_ENDED: {
            NSLog(@"IMA >>> StreamManager event (%@/%@).", event.typeString, @"kIMAAdEvent_AD_PERIOD_ENDED");
            NSLog(@"Ad period ended");
            break;
        }
        /**
        *  All ads managed by the ads manager have completed.
        */
        case kIMAAdEvent_ALL_ADS_COMPLETED: {
            NSLog(@"IMA >>> StreamManager event (%@/%@).", event.typeString, @"kIMAAdEvent_ALL_ADS_COMPLETED");
            break;
        }

        #pragma mark ADEvent - Single AD Events
        /**
        *  An ad was loaded.
        */
        case kIMAAdEvent_LOADED: {
            NSLog(@"IMA >>> StreamManager event (%@/%@).", event.typeString, @"kIMAAdEvent_LOADED");
            // [self->_player pause];
            break;
        }
        /**
        *  Ad has started.
        */
        case kIMAAdEvent_STARTED: {
            NSLog(@"IMA >>> StreamManager event (%@/%@).", event.typeString, @"kIMAAdEvent_STARTED");
            NSString *extendedAdPodInfo = [[NSString alloc]
                                            initWithFormat:@"Showing ad %ld/%ld, bumper: %@, title: %@, description: %@, contentType:"
                                            @"%@, pod index: %ld, time offset: %lf, max duration: %lf.",
                                            (long)event.ad.adPodInfo.adPosition, (long)event.ad.adPodInfo.totalAds,
                                            event.ad.adPodInfo.isBumper ? @"YES" : @"NO", event.ad.adTitle,
                                            event.ad.adDescription, event.ad.contentType, (long)event.ad.adPodInfo.podIndex,
                                            event.ad.adPodInfo.timeOffset, event.ad.adPodInfo.maxDuration];

            NSLog(@"IMA >>> extendedAdPodInfo %@", extendedAdPodInfo);
            // [self->_player pause];
            break;
        }

        /**
        *  Ad clicked.
        */
        case kIMAAdEvent_CLICKED: {
            NSLog(@"IMA >>> StreamManager event (%@/%@).", event.typeString, @"kIMAAdEvent_CLICKED");
            break;
        }
        /**
        *  Ad tapped.
        */
        case kIMAAdEvent_TAPPED: {
            NSLog(@"IMA >>> StreamManager event (%@/%@).", event.typeString, @"kIMAAdEvent_TAPPED");
            break;
        }
        /**
        *  Cuepoints changed for VOD stream (only used for dynamic ad insertion).
        *  For this event, the <code>IMAAdEvent.adData</code> property contains a list of
        *  <code>IMACuepoint</code>s at <code>IMAAdEvent.adData[@"cuepoints"]</code>.
        */
        case kIMAAdEvent_CUEPOINTS_CHANGED: {
            // Avoid Ad Skipping
//            NSLog(@"IMA >>> StreamManager event (%@/%@).", event.typeString, @"kIMAAdEvent_CUEPOINTS_CHANGED");
            break;
        }
        /**
        *  A log event for the ads being played. These are typically non fatal errors.
        */
        case kIMAAdEvent_LOG: {
            NSLog(@"IMA >>> StreamManager event (%@/%@).", event.typeString, @"kIMAAdEvent_LOG");
            break;
        }

        /**
        *  Ad paused.
        */
        case kIMAAdEvent_PAUSE: {
            NSLog(@"IMA >>> StreamManager event (%@/%@).", event.typeString, @"kIMAAdEvent_PAUSE");
            break;
        }
        /**
        *  Ad resumed.
        */
        case kIMAAdEvent_RESUME: {
            NSLog(@"IMA >>> StreamManager event (%@/%@).", event.typeString, @"kIMAAdEvent_RESUME");
            // [self->_player pause];
            break;
        }
        /**
        *  Ad has skipped.
        */
        case kIMAAdEvent_SKIPPED: {
            NSLog(@"IMA >>> StreamManager event (%@/%@).", event.typeString, @"kIMAAdEvent_SKIPPED");
            break;
        }
        #pragma mark ADEvent - Single AD Events - Progress
        /**
        *  First quartile of a linear ad was reached.
        */
        case kIMAAdEvent_FIRST_QUARTILE: {
            NSLog(@"IMA >>> StreamManager Single Ad %@ (%@).", event.typeString, @"kIMAAdEvent_FIRST_QUARTILE");
            break;
        }
        /**
        *  Midpoint of a linear ad was reached.
        */
        case kIMAAdEvent_MIDPOINT: {
            NSLog(@"IMA >>> StreamManager Single Ad %@ (%@).", event.typeString, @"kIMAAdEvent_MIDPOINT");
            break;
        }
        /**
        *  Third quartile of a linear ad was reached.
        */
        case kIMAAdEvent_THIRD_QUARTILE: {
            NSLog(@"IMA >>> StreamManager Single Ad %@ (%@).", event.typeString, @"kIMAAdEvent_THIRD_QUARTILE");
            break;
        }
        /**
        *  Single ad has finished.
        */
        case kIMAAdEvent_COMPLETE: {
            NSLog(@"IMA >>> StreamManager Single Ad %@ (%@).", event.typeString, @"kIMAAdEvent_COMPLETE");
            break;
        }

        default:
            break;
    }
}

- (void)streamManager:(IMAStreamManager *)streamManager didReceiveAdError:(IMAAdError *)error {
    NSLog(@"IMA >>> StreamManager error with type: %ld\ncode: %ld\nmessage: %@", error.type, error.code,
          error.message);
//    [self.contentPlayer play];
}

- (void)streamManager:(IMAStreamManager *)streamManager adDidProgressToTime:(NSTimeInterval)time adDuration:(NSTimeInterval)adDuration adPosition:(NSInteger)adPosition totalAds:(NSInteger)totalAds adBreakDuration:(NSTimeInterval)adBreakDuration {
    // onAdProgress
//    NSLog(@"IMA >>> (void)streamManager:(IMAStreamManager *)streamManager adDidProgressToTime:(NSTimeInterval)time adDuration:(NSTimeInterval)adDuration adPosition:(NSInteger)adPosition totalAds:(NSInteger)totalAds adBreakDuration:(NSTimeInterval)adBreakDuration");
}

#pragma mark AVPlayerVideoDisplay Delegates

- (void)avPlayerVideoDisplay:(IMAAVPlayerVideoDisplay *)avPlayerVideoDisplay
         willLoadStreamAsset:(AVURLAsset *)avUrlAsset {
    NSLog(@"- (void)avPlayerVideoDisplay:(IMAAVPlayerVideoDisplay *)avPlayerVideoDisplay willLoadStreamAsset:(AVURLAsset *)avUrlAsset;");
    [avPlayerVideoDisplay.player pause];
}

@end
