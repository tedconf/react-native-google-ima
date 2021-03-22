package com.reactlibrary.googleIMA;

import android.net.Uri;

import com.brentvatne.exoplayer.ReactExoplayerView;
import com.google.android.exoplayer2.source.MediaSource;

import java.util.Map;

public interface RNGoogleIMADelegateInterface {
  public MediaSource buildMediaSource(ReactExoplayerView reactExoplayerView, Uri uri, String overrideExtension);

  public boolean setSrc(ReactExoplayerView reactExoplayerView, Uri uri, String extension, Map<String, String> headers);
}
