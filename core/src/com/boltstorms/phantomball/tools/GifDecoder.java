package com.boltstorms.phantomball.tools;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

/**
 * Shared types only (safe for Android).
 * DO NOT import javax.imageio or java.awt here.
 */
public final class GifDecoder {

    private GifDecoder() {}

    public static class GIFAnimation {
        public final Array<TextureRegion> frames;
        public final Array<Texture> textures; // so you can dispose safely
        public final float frameDuration;

        public GIFAnimation(Array<TextureRegion> frames, Array<Texture> textures, float frameDuration) {
            this.frames = frames;
            this.textures = textures;
            this.frameDuration = frameDuration;
        }

        public Animation<TextureRegion> rebuildAnimation(float duration, Animation.PlayMode playMode) {
            Animation<TextureRegion> anim = new Animation<>(duration, frames);
            anim.setPlayMode(playMode);
            return anim;
        }

        public void dispose() {
            for (Texture t : textures) {
                if (t != null) t.dispose();
            }
        }
    }
}
