package com.federicoterzi.bipcutremote;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    // Beep configuration
    public static final int BEEP_DURATION = 500;     // Beep duration in milliseconds

    // Frequency configuration
    public static final int START_CLIP_FREQ = 1000;  // Frequency to mark the start or confirm a clip
    public static final int ERROR_FREQ = 1300;       // Frequency to mark an error in the clip
    // The frequencies to generate
    private int[] freqs = new int[] {START_CLIP_FREQ, ERROR_FREQ};

    // This will hold the freqs for the specified frequencies
    private Map<Integer, short[]> sampleMap= new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button startButton = findViewById(R.id.start_clip);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playFrequency(START_CLIP_FREQ);
            }
        });

        Button errorButton = findViewById(R.id.mark_error);
        errorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playFrequency(ERROR_FREQ);
            }
        });

        // Generate the frequencies audio tracks
        generateAudioTracks();
    }

    /**
     * Generate the audio tracks in another thread.
     */
    private void generateAudioTracks() {
        new Thread() {
            @Override
            public void run() {
                for (int freq : freqs) {
                    generateTone(freq, BEEP_DURATION);
                }
            }
        }.start();
    }

    /**
     * Generate the specified audio frequency.
     * Credits: https://gist.github.com/slightfoot/6330866
     * @param freqHz
     * @param durationMs
     */
    private void generateTone(int freqHz, int durationMs)
    {
        int count = (int)(44100.0 * 2.0 * (durationMs / 1000.0)) & ~1;
        short[] samples = new short[count];
        for(int i = 0; i < count; i += 2){
            short sample = (short)(Math.sin(2 * Math.PI * i / (44100.0 / freqHz)) * 0x7FFF);
            samples[i + 0] = sample;
            samples[i + 1] = sample;
        }
        sampleMap.put(freqHz, samples);
    }

    /**
     * Play the given frequency
     * @param freq
     */
    private void playFrequency(int freq) {
        short[] trackSamples = sampleMap.get(freq);
        if (trackSamples != null) {
            AudioTrack track = new AudioTrack(AudioManager.STREAM_MUSIC, 44100,
                    AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT,
                    trackSamples.length * (Short.SIZE / 8), AudioTrack.MODE_STATIC);
            track.write(trackSamples, 0, trackSamples.length);
            track.play();
        }
    }
}
