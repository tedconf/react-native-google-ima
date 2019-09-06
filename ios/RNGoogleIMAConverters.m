@import GoogleInteractiveMediaAds;

NSDictionary* convertAdsManager(IMAAdsManager* adsManager)
{
    NSMutableArray *adCuePoints = [[NSMutableArray alloc] init];
    for (int i = 0; i < adsManager.adCuePoints.count; ++i) {
        NSNumber* cuePoint = [adsManager.adCuePoints objectAtIndex:i];
        [adCuePoints addObject:cuePoint];
    }

    return @{
        @"adCuePoints": adCuePoints,
        @"adPlaybackInfo": @{
            @"currentMediaTime": [NSNumber numberWithDouble:adsManager.adPlaybackInfo.currentMediaTime],
            @"totalMediaTime": [NSNumber numberWithDouble:adsManager.adPlaybackInfo.totalMediaTime],
            @"bufferedMediaTime": [NSNumber numberWithDouble:adsManager.adPlaybackInfo.bufferedMediaTime],
            @"playing": [NSNumber numberWithBool:adsManager.adPlaybackInfo.playing],
        }
    };
}

NSDictionary* convertStreamManager(IMAStreamManager* streamManager)
{
    return @{@"streamId": streamManager.streamId};
}

NSDictionary* convertAdError(IMAAdError* adError)
{
    return @{
        @"type": [NSNumber numberWithLongLong:adError.type],
        @"code": [NSNumber numberWithLongLong:adError.code],
        @"message": adError.message,
    };
}

NSDictionary* convertAdLoadingErrorData(IMAAdLoadingErrorData* adErrorData)
{
    return @{
        @"adError": convertAdError(adErrorData.adError),
    };
}

NSDictionary* convertAdsLoadedData(IMAAdsLoadedData* adsLoadedData)
{
    return @{
         @"adsManager": convertAdsManager(adsLoadedData.adsManager),
         @"streamManager": convertStreamManager(adsLoadedData.streamManager),
    };
}

NSDictionary* convertAdEvent(IMAAdEvent* event) {
    NSString* adEventTypeString = nil;
    switch (event.type) {
        case kIMAAdEvent_STREAM_LOADED: {
            adEventTypeString = @"STREAM_LOADED";
            break;
        }
        case kIMAAdEvent_STREAM_STARTED: {
            adEventTypeString = @"STREAM_STARTED";
            break;
        }
        case kIMAAdEvent_AD_BREAK_READY: {
            adEventTypeString = @"AD_BREAK_READY";
            break;
        }
        case kIMAAdEvent_AD_BREAK_STARTED: {
            adEventTypeString = @"AD_BREAK_STARTED";
            break;
        }
        case kIMAAdEvent_AD_BREAK_ENDED: {
            adEventTypeString = @"AD_BREAK_ENDED";
            break;
        }
        case kIMAAdEvent_AD_PERIOD_STARTED: {
            adEventTypeString = @"AD_PERIOD_STARTED";
            break;
        }
        case kIMAAdEvent_AD_PERIOD_ENDED: {
            adEventTypeString = @"AD_PERIOD_ENDED";
            break;
        }
        case kIMAAdEvent_ALL_ADS_COMPLETED: {
            adEventTypeString = @"ALL_ADS_COMPLETED";
            break;
        }
        case kIMAAdEvent_LOADED: {
            adEventTypeString = @"LOADED";
            break;
        }
        case kIMAAdEvent_STARTED: {
            adEventTypeString = @"STARTED";
            break;
        }
        case kIMAAdEvent_CLICKED: {
            adEventTypeString = @"CLICKED";
            break;
        }
        case kIMAAdEvent_TAPPED: {
            adEventTypeString = @"TAPPED";
            break;
        }
        case kIMAAdEvent_CUEPOINTS_CHANGED: {
            adEventTypeString = @"CUEPOINTS_CHANGED";
            break;
        }
        case kIMAAdEvent_LOG: {
            adEventTypeString = @"LOG";
            break;
        }
        case kIMAAdEvent_PAUSE: {
            adEventTypeString = @"PAUSE";
            break;
        }
        case kIMAAdEvent_RESUME: {
            adEventTypeString = @"RESUME";
            break;
        }
        case kIMAAdEvent_SKIPPED: {
            adEventTypeString = @"SKIPPED";
            break;
        }
        case kIMAAdEvent_FIRST_QUARTILE: {
            adEventTypeString = @"FIRST_QUARTILE";
            break;
        }
        case kIMAAdEvent_MIDPOINT: {
            adEventTypeString = @"MIDPOINT";
            break;
        }
        /**
        *  Third quartile of a linear ad was reached.
        */
        case kIMAAdEvent_THIRD_QUARTILE: {
            adEventTypeString = @"THIRD_QUARTILE";
            break;
        }
        /**
        *  Single ad has finished.
        */
        case kIMAAdEvent_COMPLETE: {
            adEventTypeString = @"COMPLETE";
            break;
        }

        default:
            break;
    }

    return  @{
        @"type": adEventTypeString,
        @"typeString": event.typeString,
        // @"code": [NSNumber numberWithLongLong:adErrorData.adError.code],
        // @"message": adErrorData.adError.message,
        };
}
