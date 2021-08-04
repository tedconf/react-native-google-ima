package com.reactlibrary.googleIMA;

import android.util.Log;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeArray;
import com.facebook.react.bridge.WritableNativeMap;
import com.google.ads.interactivemedia.v3.api.Ad;
import com.google.ads.interactivemedia.v3.api.AdError;
import com.google.ads.interactivemedia.v3.api.AdErrorEvent;
import com.google.ads.interactivemedia.v3.api.AdEvent;
import com.google.ads.interactivemedia.v3.api.AdPodInfo;
import com.google.ads.interactivemedia.v3.api.AdsManager;
import com.google.ads.interactivemedia.v3.api.AdsManagerLoadedEvent;
import com.google.ads.interactivemedia.v3.api.CuePoint;
import com.google.ads.interactivemedia.v3.api.StreamManager;
import com.google.ads.interactivemedia.v3.api.UiElement;

import java.util.List;
import java.util.Map;

public class RNGoogleIMAConverters {
  static WritableMap convertAdsLoadedData(AdsManagerLoadedEvent adsLoadedData) {
    WritableMap data = Arguments.createMap();
    data.putMap("adsManager", convertAdsManager(adsLoadedData.getAdsManager()));
    data.putMap("streamManager", convertStreamManager(adsLoadedData.getStreamManager()));
    WritableNativeMap adsLoadedDataDictionary = new WritableNativeMap();
    adsLoadedDataDictionary.putMap("adsLoadedData", data);
    return adsLoadedDataDictionary;
  }

  static WritableMap convertAdsManager(AdsManager adsManager) {
    if (adsManager == null) {
      return null;
    }
    WritableMap data = Arguments.createMap();

    data.putArray("adCuePoints", convertArray(adsManager.getAdCuePoints().toArray(new Float[0])));

    WritableNativeMap adPlaybackInfo = new WritableNativeMap();
    adPlaybackInfo.putInt("currentMediaTime", (int) adsManager.getAdProgress().getCurrentTimeMs());
    adPlaybackInfo.putInt("totalMediaTime", (int) adsManager.getAdProgress().getDurationMs());
    adPlaybackInfo.putInt("bufferedMediaTime", 0);
    adPlaybackInfo.putBoolean("playing", true);
    data.putMap("adPlaybackInfo", adPlaybackInfo);

    return data;
  }

  static WritableMap convertStreamManager(StreamManager streamManager) {
    WritableMap data = Arguments.createMap();

    data.putString("streamId", streamManager.getStreamId());

    return data;
  }

  static WritableArray convertArray(Object[] items) {
    WritableArray writableArray = Arguments.createArray();
    for (Object item : items) {
      if (item.getClass().equals(String.class)) {
        writableArray.pushString((String) item);
      } else if (item.getClass().equals(String.class)) {
        writableArray.pushString((String) item);
      } else if (item.getClass().equals(UiElement.class)) {
        writableArray.pushString(((UiElement) item).getName());
      }

    }
    return writableArray;
  }

  public static WritableMap convertAdEvent(AdEvent adEvent, List<CuePoint> cuepoints) {
    if (adEvent != null) {
      String adEventTypeString = adEvent.getType().toString();

      if (adEvent.getType() == AdEvent.AdEventType.COMPLETED) {
        adEventTypeString = "COMPLETE";
      }

      WritableNativeMap adEventContentsDictionary = new WritableNativeMap();
      adEventContentsDictionary.putString("type", adEventTypeString);
      adEventContentsDictionary.putString("typeString", adEvent.getType().name());
      adEventContentsDictionary.putMap("ad", convertAd(adEvent.getAd()));
      adEventContentsDictionary.putMap("adData", convertAdData(adEvent.getAdData(), cuepoints));

      WritableNativeMap adEventDictionary = new WritableNativeMap();
      adEventDictionary.putMap("adEvent", adEventContentsDictionary);

      return adEventDictionary;
    }
    return null;
  }

  public static WritableMap convertAdData(Map<String, String> adData, List<CuePoint> cuepoints) {
    WritableMap adDataDictionary = Arguments.createMap();
    if (cuepoints != null) {
      WritableArray cuepointsList = Arguments.createArray();
      for (CuePoint cuePoint : cuepoints) {
        cuepointsList.pushMap(convertAdCuepoint(cuePoint));
      }
      adDataDictionary.putArray("cuepoints", cuepointsList);
    }
    if (adData != null) {
      String[] keys = (String[]) adData.keySet().toArray();
//      for (String key : keys) {
//        if (key.equals("cuepoints")) {
//          adDataDictionary.putString(key, adData.get(key));
////                    adDataDictionary.putArray(key, convertArray());
//        }
//      }
//            for (int i = 0; i < keys.length; i++)
//            {
//                String key = keys[i];
//                if (key.equals("cuepoints")) {
//
//                }
////                if ([key isEqualToString:@"cuepoints"]) {
////                NSArray<IMACuepoint*>* cuepointsArray = [adData objectForKey:key];
////                NSMutableArray<NSDictionary*>* cuepointsMutable = [[NSMutableArray alloc] init];
////                for (int j = 0; j < cuepointsArray.count; j++)
////                {
////                    [cuepointsMutable addObject:orNull(convertAdCuepoint([cuepointsArray objectAtIndex:j]))];
////                }
////                [adDataDictionary setValue:cuepointsMutable forKey:key];
//
//            }
    }
    return adDataDictionary;
  }


  public static WritableMap convertAdCuepoint(CuePoint cuepoint) {
    WritableMap adDataDictionary = Arguments.createMap();

    if (cuepoint != null) {
      adDataDictionary.putDouble("startTime", cuepoint.getStartTime());
      adDataDictionary.putDouble("endTime", cuepoint.getEndTime());
      adDataDictionary.putBoolean("played", cuepoint.isPlayed());
    }

    return adDataDictionary;
  }

  public static WritableMap convertAd(Ad ad) {
    WritableMap adDictionary = Arguments.createMap();
    try {
      if (ad != null) {
        adDictionary.putString("adId", ad.getAdId());
        adDictionary.putString("adTitle", ad.getTitle());
        adDictionary.putString("adDescription", ad.getDescription());
        adDictionary.putString("adSystem", ad.getAdSystem());
        adDictionary.putString("contentType", ad.getContentType());
        adDictionary.putDouble("duration", ad.getDuration());
        adDictionary.putArray("uiElements", convertArray(ad.getUiElements().toArray(new UiElement[0])));
        adDictionary.putInt("width", ad.getWidth());
        adDictionary.putInt("height", ad.getHeight());
        adDictionary.putInt("vastMediaWidth", ad.getVastMediaWidth());
        adDictionary.putInt("vastMediaHeight", ad.getVastMediaHeight());
        adDictionary.putInt("vastMediaBitrate", ad.getVastMediaWidth());
        adDictionary.putBoolean("linear", ad.isLinear());
        adDictionary.putBoolean("skippable", ad.isSkippable());
        adDictionary.putDouble("skipTimeOffset", ad.getSkipTimeOffset());
        adDictionary.putMap("adPodInfo", convertAdPodInfo(ad.getAdPodInfo()));
        adDictionary.putString("traffickingParameters", ad.getTraffickingParameters());
        adDictionary.putString("creativeID", ad.getCreativeId());
        adDictionary.putArray("universalAdIds", convertArray(ad.getUniversalAdIds()));
//                adDictionary.putString("universalAdIdRegistry", ad.getUniversalAdIdRegistry());
        adDictionary.putString("advertiserName", ad.getAdvertiserName());
        adDictionary.putString("surveyURL", ad.getSurveyUrl());
        adDictionary.putString("dealID", ad.getDealId());
        adDictionary.putArray("wrapperAdIDs", convertArray(ad.getAdWrapperIds()));
        adDictionary.putArray("wrapperSystems", convertArray(ad.getAdWrapperSystems()));
      }
    } catch (Exception e) {
      System.out.print(e.getMessage());
    }
    return adDictionary;
  }

  public static WritableMap convertAdPodInfo(AdPodInfo adPodInfo) {
    WritableNativeMap adPodInfoDictionary = new WritableNativeMap();
    if (adPodInfo != null) {
      adPodInfoDictionary.putInt("totalAds", adPodInfo.getTotalAds());
      adPodInfoDictionary.putInt("adPosition", adPodInfo.getAdPosition());
      adPodInfoDictionary.putBoolean("isBumper", adPodInfo.isBumper());
      adPodInfoDictionary.putInt("podIndex", adPodInfo.getPodIndex());
      adPodInfoDictionary.putDouble("timeOffset", adPodInfo.getTimeOffset());
      adPodInfoDictionary.putDouble("maxDuration", adPodInfo.getMaxDuration());
    }
    return adPodInfoDictionary;
  }

  public static WritableMap convertAdErrorEvent(AdErrorEvent adErrorEvent) {
    WritableMap adPodInfoDictionary = Arguments.createMap();
    if (adErrorEvent != null) {
      AdError adError = adErrorEvent.getError();
      if (adError != null) {
        adPodInfoDictionary.putString("type", adError.getErrorType().name());
        adPodInfoDictionary.putString("code", adError.getErrorCode().name());
        adPodInfoDictionary.putString("message", adError.getMessage());
      }
    }
    return adPodInfoDictionary;
  }


}
