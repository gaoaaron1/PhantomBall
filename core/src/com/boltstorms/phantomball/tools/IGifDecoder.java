package com.boltstorms.phantomball.tools;

import com.badlogic.gdx.graphics.g2d.Animation;

import java.io.InputStream;

public interface IGifDecoder {
    GifDecoder.GIFAnimation decode(Animation.PlayMode playMode, InputStream is);
}
