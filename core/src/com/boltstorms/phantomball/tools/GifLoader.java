package com.boltstorms.phantomball.tools;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.Animation;

import java.io.InputStream;

public final class GifLoader {

    private GifLoader() {}

    /** Set this in DesktopLauncher / AndroidLauncher */
    public static IGifDecoder DECODER;

    public static GifDecoder.GIFAnimation loadGif(String path, Animation.PlayMode playMode) {
        if (DECODER == null) {
            throw new IllegalStateException("GifLoader.DECODER not set! Set it in your launcher.");
        }

        FileHandle file = Gdx.files.internal(path);
        try (InputStream is = file.read()) {
            return DECODER.decode(playMode, is);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load GIF: " + path, e);
        }
    }
}
