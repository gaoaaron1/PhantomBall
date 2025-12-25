package com.boltstorms.phantomball.tools;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

import java.io.InputStream;

public class AndroidGifDecoder implements IGifDecoder {

    @Override
    public GifDecoder.GIFAnimation decode(Animation.PlayMode playMode, InputStream is) {

        LegacyGifDecoder.Result result = LegacyGifDecoder.decodeToFrames(is);

        Array<TextureRegion> frames = new Array<>(result.frames.size);
        Array<Texture> textures = new Array<>(result.frames.size);

        float total = 0f;

        for (int i = 0; i < result.frames.size; i++) {
            Pixmap pm = result.frames.get(i);

            // Delay is already in seconds from LegacyGifDecoder, but clamp anyway
            float delay = result.delays.get(i);
            if (delay <= 0f) delay = 0.1f;

            Texture tex = new Texture(pm);

            // Choose one:
            // - Nearest avoids halo artifacts when scaling (good for GIF transparency)
            tex.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

            TextureRegion region = new TextureRegion(tex);

            // If your Android GIF shows upside down, uncomment ONE of these approaches:
            // Approach A: flip region once
            // region.flip(false, true);

            frames.add(region);
            textures.add(tex);

            total += delay;

            // In our LegacyGifDecoder we created NEW Pixmaps for each frame, so this is safe:
            pm.dispose();
        }

        float frameDuration = frames.size > 0 ? (total / frames.size) : 0.1f;

        // You can still set PlayMode on the Animation later via rebuildAnimation()
        return new GifDecoder.GIFAnimation(frames, textures, frameDuration);
    }
}
