package com.soundlover.exoplayertest;

import com.google.android.exoplayer2.extractor.Extractor;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.extractor.mp3.Mp3Extractor;
import com.google.android.exoplayer2.extractor.mp4.Mp4Extractor;
import com.google.android.exoplayer2.extractor.ogg.OggExtractor;
import com.google.android.exoplayer2.extractor.wav.WavExtractor;

/**
 * Created by Sergiy on 06.02.2017.
 */

public class AudioExtractorFactory implements ExtractorsFactory {

    @Override
    public Extractor[] createExtractors() {
        return new Extractor[]{
                new OggExtractor(),
                new WavExtractor(),
                new Mp3Extractor(),
                new Mp4Extractor()
        };
    }
}
