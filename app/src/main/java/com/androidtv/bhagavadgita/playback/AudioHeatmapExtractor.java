package com.androidtv.bhagavadgita.playback;

import android.content.Context;
import android.media.AudioFormat;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.net.Uri;

import com.androidtv.bhagavadgita.comman.LogTag;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AudioHeatmapExtractor {

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();

    public interface HeatmapCallback {
        void onHeatmapReady(float[] heatData);
        void onError(Exception e);
    }

    public static void extractHeatmapFromUrl(
            Context context,
            Uri mediaUri,
            int targetPoints,
            HeatmapCallback callback) {

        EXECUTOR.execute(() -> {
            MediaExtractor extractor = new MediaExtractor();
            MediaCodec codec = null;

            try {
                extractor.setDataSource(context, mediaUri, null);

                int audioTrackIndex = -1;
                MediaFormat format = null;
                for (int i = 0; i < extractor.getTrackCount(); i++) {
                    MediaFormat mf = extractor.getTrackFormat(i);
                    String mime = mf.getString(MediaFormat.KEY_MIME);
                    if (mime != null && mime.startsWith("audio/")) {
                        audioTrackIndex = i;
                        format = mf;
                        break;
                    }
                }

                if (audioTrackIndex == -1) {
                    throw new IOException("No audio track found in media.");
                }

                extractor.selectTrack(audioTrackIndex);
                long durationUs = format.getLong(MediaFormat.KEY_DURATION);
                long intervalUs = durationUs / targetPoints;

                String mime = format.getString(MediaFormat.KEY_MIME);
                codec = MediaCodec.createDecoderByType(mime);
                codec.configure(format, null, null, 0);
                codec.start();

                float[] heatData = new float[targetPoints];
                MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
                long timeoutUs = 5000;

                // SAMPLE ONLY 80 SLICES ACROSS THE TIMELINE (Skip the rest)
                for (int i = 0; i < targetPoints; i++) {
                    long targetTimeUs = i * intervalUs;

                    // Seek directly to this bucket
                    extractor.seekTo(targetTimeUs, MediaExtractor.SEEK_TO_CLOSEST_SYNC);
                    codec.flush();

                    float bucketMax = 0f;
                    int decodedFrames = 0;

                    // Decode only 2 small frames at this position, then jump to the next bucket
                    while (decodedFrames < 2) {
                        int inIndex = codec.dequeueInputBuffer(timeoutUs);
                        if (inIndex >= 0) {
                            ByteBuffer buffer = codec.getInputBuffer(inIndex);
                            int sampleSize = extractor.readSampleData(buffer, 0);
                            if (sampleSize > 0) {
                                codec.queueInputBuffer(inIndex, 0, sampleSize, extractor.getSampleTime(), 0);
                                extractor.advance();
                            } else {
                                break;
                            }
                        }

                        int outIndex = codec.dequeueOutputBuffer(bufferInfo, timeoutUs);
                        if (outIndex >= 0) {
                            ByteBuffer outBuffer = codec.getOutputBuffer(outIndex);
                            if (outBuffer != null && bufferInfo.size > 0) {
                                float amp = calculatePeakAmplitude(outBuffer, bufferInfo);
                                if (amp > bucketMax) {
                                    bucketMax = amp;
                                }
                                decodedFrames++;
                            }
                            codec.releaseOutputBuffer(outIndex, false);
                        } else if (inIndex < 0) {
                            // Timeout / no data, move on
                            break;
                        }
                    }

                    heatData[i] = bucketMax;
                }

                // Normalize amplitudes (0.0 to 1.0)
                float globalMax = 1f;
                for (float v : heatData) {
                    if (v > globalMax) globalMax = v;
                }

                for (int i = 0; i < targetPoints; i++) {
                    // Apply a baseline offset (0.15f) so silent segments don't clip flat to 0
                    heatData[i] = 0.15f + (0.85f * (heatData[i] / globalMax));
                }

                callback.onHeatmapReady(heatData);

            } catch (Exception e) {
                callback.onError(e);
            } finally {
                if (codec != null) {
                    try {
                        codec.stop();
                        codec.release();
                    } catch (Exception ignored) {}
                }
                extractor.release();
            }
        });
    }

    private static float calculatePeakAmplitude(ByteBuffer buffer, MediaCodec.BufferInfo info) {
        if (buffer == null || info.size <= 0) return 0f;

        buffer.position(info.offset);
        buffer.limit(info.offset + info.size);

        int max = 0;
        while (buffer.remaining() >= 2) {
            short sample = buffer.getShort();
            int abs = Math.abs(sample);
            if (abs > max) max = abs;
        }
        return (float) max;
    }
}