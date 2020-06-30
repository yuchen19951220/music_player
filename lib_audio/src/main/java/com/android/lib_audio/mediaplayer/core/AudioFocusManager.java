package com.android.lib_audio.mediaplayer.core;

import android.content.Context;
import android.media.AudioManager;

/**
 * 音频焦点播放器
 */
public class AudioFocusManager implements AudioManager.OnAudioFocusChangeListener {

    //响应回调接口 到 audioPlayer执行
    private AudioFocusListener mAudioFocusListener;
    private AudioManager audioManager;

    public AudioFocusManager(Context context, AudioFocusListener listener) {
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        mAudioFocusListener = listener;
    }

    /**
     *     查看是否有焦点
     */
    public boolean requestAudioFocus() {
        return audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
    }

    /**
     * 释放音频焦点
     */
    public void abandonAudioFocus() {
        audioManager.abandonAudioFocus(this);
    }
    @Override
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange){
            // 重新获得焦点
            case AudioManager.AUDIOFOCUS_GAIN:
                if (mAudioFocusListener != null) mAudioFocusListener.audioFocusGrant();
                break;
            // 永久丢失焦点，如被其他播放器抢占
            case AudioManager.AUDIOFOCUS_LOSS:
                if (mAudioFocusListener != null) mAudioFocusListener.audioFocusLoss();
                break;
            // 短暂丢失焦点，如来电
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                if (mAudioFocusListener != null) mAudioFocusListener.audioFocusLossTransient();
                break;
            // 瞬间丢失焦点，如通知
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                if (mAudioFocusListener != null) mAudioFocusListener.audioFocusLossDuck();
                break;

        }
    }

    /**
     * 音频焦点改变,接口回调，
     */
    public interface AudioFocusListener {
        //获得焦点回调处理
        void audioFocusGrant();

        //永久失去焦点回调处理
        void audioFocusLoss();

        //短暂失去焦点回调处理
        void audioFocusLossTransient();

        //瞬间失去焦点回调
        void audioFocusLossDuck();
    }
}
