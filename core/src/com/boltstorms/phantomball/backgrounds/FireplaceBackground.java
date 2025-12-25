package com.boltstorms.phantomball.backgrounds;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.boltstorms.phantomball.tools.GifDecoder;
import com.boltstorms.phantomball.tools.GifLoader;

public class FireplaceBackground {

    private final GifDecoder.GIFAnimation introGif;
    private final GifDecoder.GIFAnimation loopGif;

    private final Animation<TextureRegion> intro;
    private final Animation<TextureRegion> loop;

    private float time = 0f;
    private boolean introDone = false;

    public FireplaceBackground() {
        // Load once for intro, once for loop (simple and explicit)
        introGif = GifLoader.loadGif("OpenFireplace.gif", Animation.PlayMode.NORMAL);
        loopGif  = GifLoader.loadGif("AnimatedFireplace.gif", Animation.PlayMode.LOOP);

        intro = introGif.rebuildAnimation(introGif.frameDuration, Animation.PlayMode.NORMAL);
        loop  = loopGif.rebuildAnimation(loopGif.frameDuration, Animation.PlayMode.LOOP);
    }

    public void update(float dt) {
        time += dt;

        if (!introDone && intro.isAnimationFinished(time)) {
            introDone = true;
            time = 0f;
        }
    }

    public void render(SpriteBatch batch, float x, float y, float w, float h) {
        TextureRegion frame = introDone ? loop.getKeyFrame(time) : intro.getKeyFrame(time);
        batch.draw(frame, x, y, w, h);
    }

    public void dispose() {
        introGif.dispose();
        loopGif.dispose();
    }
}
