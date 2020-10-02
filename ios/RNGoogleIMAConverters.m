@import GoogleInteractiveMediaAds;

id orNull(id value) {
    return value != nil ? value : [NSNull null];
}

NSMutableArray* convertArray(NSArray* source) {
    NSMutableArray *target = [[NSMutableArray alloc] init];
    if (source != nil) {
        for (int i = 0; i < source.count; ++i) {
            [target addObject:[source objectAtIndex:i]];
        }
    }
    return target;
}

NSDictionary* convertAdsManager(IMAAdsManager* adsManager)
{
    return @{
             @"adCuePoints": convertArray(adsManager.adCuePoints),
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

NSDictionary* convertAdPodInfo(IMAAdPodInfo* adPodInfo)
{
    NSDictionary* adPodInfoDictionary = nil;
    if (adPodInfo != nil) {
        adPodInfoDictionary = @{
                                @"totalAds": [NSNumber numberWithLong:adPodInfo.totalAds],
                                @"adPosition": [NSNumber numberWithLong:adPodInfo.adPosition],
                                @"isBumper": [NSNumber numberWithLong:adPodInfo.isBumper],
                                @"podIndex": [NSNumber numberWithLong:adPodInfo.podIndex],
                                @"timeOffset": [NSNumber numberWithDouble:adPodInfo.timeOffset],
                                @"maxDuration": [NSNumber numberWithDouble:adPodInfo.maxDuration],
                                };
    }
    return adPodInfoDictionary;
}

NSDictionary* convertAd(IMAAd* ad) {
    NSDictionary* adDictionary = nil;
    @try {
        if (ad != nil) {
            adDictionary =@{
                @"adId": orNull(ad.adId),
                @"adTitle": orNull(ad.adTitle),
                @"adDescription": orNull(ad.adDescription),
                @"adSystem": orNull(ad.adSystem),
                @"contentType": orNull(ad.contentType),
                @"duration": [NSNumber numberWithDouble:ad.duration],
                @"uiElements": convertArray(ad.uiElements),
                @"width": [NSNumber numberWithInteger:ad.width],
                @"height": [NSNumber numberWithInteger:ad.height],
                @"vastMediaWidth": [NSNumber numberWithInteger:ad.VASTMediaWidth],
                @"vastMediaHeight": [NSNumber numberWithInteger:ad.VASTMediaHeight],
                @"vastMediaBitrate": [NSNumber numberWithInteger:ad.VASTMediaBitrate],
                @"linear": [NSNumber numberWithBool:ad.linear],
                @"skippable": [NSNumber numberWithBool:ad.skippable],
                @"skipTimeOffset": [NSNumber numberWithDouble:ad.skipTimeOffset],
                @"adPodInfo": orNull(convertAdPodInfo(ad.adPodInfo)),
                @"traffickingParameters": orNull(ad.traffickingParameters),
                @"creativeID": orNull(ad.creativeID),
                @"universalAdIdValue": orNull(ad.universalAdIdValue),
                @"universalAdIdRegistry": orNull(ad.universalAdIdRegistry),
                @"advertiserName": orNull(ad.advertiserName),
                @"surveyURL": orNull(ad.surveyURL),
                @"dealID": orNull(ad.dealID),
                @"wrapperAdIDs": convertArray(ad.wrapperAdIDs),
                @"wrapperSystems": convertArray(ad.wrapperSystems),
            };
        }
    } @catch (NSException *exception) {
    } @finally {
        return adDictionary;
    }
}

NSDictionary* convertAdCuepoint(IMACuepoint* cuepoint) {
    NSDictionary* cuepointDictionary;
    
    if (cuepoint != nil) {
        cuepointDictionary = @{
                               @"startTime": [NSNumber numberWithDouble:cuepoint.startTime],
                               @"endTime": [NSNumber numberWithDouble:cuepoint.endTime],
                               @"played": [NSNumber numberWithBool:cuepoint.played],
                               };
    }
    
    return cuepointDictionary;
}

NSDictionary* converAdData(NSDictionary* adData) {
    if (adData) {
        NSMutableDictionary* adDataDictionary = [[NSMutableDictionary alloc] init];
        NSArray<NSString*>* keys = [adData allKeys];
        for (int i = 0; i < keys.count; i++)
        {
            NSString* key = [keys objectAtIndex:i];
            if ([key isEqualToString:@"cuepoints"]) {
                NSArray<IMACuepoint*>* cuepointsArray = [adData objectForKey:key];
                NSMutableArray<NSDictionary*>* cuepointsMutable = [[NSMutableArray alloc] init];
                for (int j = 0; j < cuepointsArray.count; j++)
                {
                    [cuepointsMutable addObject:orNull(convertAdCuepoint([cuepointsArray objectAtIndex:j]))];
                }
                [adDataDictionary setValue:cuepointsMutable forKey:key];
            }
        }
        return adDataDictionary;
    }
    return nil;
}

NSDictionary* convertAdEvent(IMAAdEvent* event) {
    NSDictionary* adEventDictionary = nil;
    if (event != nil) {
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
        adEventDictionary = @{
                              @"type": adEventTypeString,
                              @"typeString": event.typeString,
                              @"ad": orNull(convertAd(event.ad)),
                              @"adData": orNull(converAdData(event.adData)),
                              };
    }
    
    return adEventDictionary;
}

