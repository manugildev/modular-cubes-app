package com.manugildev.modularcubes.data.models;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.PlaybackParams;


public class CubeAudio {

    private final Context ctx;
    private final int soundId;
    private MediaPlayer audioPlayer;
    private final PlaybackParams params;
    private ModularCube cube;

    public CubeAudio(ModularCube cube, Context ctx, int soundId) {
        this.cube = cube;
        this.ctx = ctx;
        this.soundId = soundId;
        audioPlayer = MediaPlayer.create(ctx, soundId);
        audioPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        params = new PlaybackParams();
        //audioPlayer.start();
        //audioPlayer.pause();
        audioPlayer.setLooping(true);

    }

    public void start() {
        if (audioPlayer != null && !audioPlayer.isPlaying()) {
            audioPlayer = MediaPlayer.create(ctx, soundId);
            audioPlayer.start();
            audioPlayer.setLooping(true);
        }
    }

    public void pause() {
        if (audioPlayer != null && audioPlayer.isPlaying()) audioPlayer.stop();
        //audioPlayer.release();
    }

    public void stop() {
        if (audioPlayer != null) audioPlayer.stop();
    }

    public void setPitch(int orientation) {

        params.setPitch((float) (1 - (orientation * 0.09)));
        audioPlayer.setPlaybackParams(params);
        if (!audioPlayer.isPlaying() && cube.isActivated()) start();
        //if (orientation == 1) audioPlayer.pause();
    }

    public boolean isPlaying() {
        return audioPlayer.isPlaying();
    }
}
