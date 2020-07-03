package com.android.lib_base.audio;

import android.content.Context;

import com.alibaba.android.arouter.facade.template.IProvider;

public interface AudioService extends IProvider {
    void pauseAudio();

    void resumeAudio();

}
