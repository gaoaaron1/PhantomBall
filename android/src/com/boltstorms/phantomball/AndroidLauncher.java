package com.boltstorms.phantomball;

import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.boltstorms.phantomball.tools.AndroidGifDecoder;
import com.boltstorms.phantomball.tools.GifLoader;

public class AndroidLauncher extends AndroidApplication {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// ✅ IMPORTANT: tell core to use Android decoder
		GifLoader.DECODER = new AndroidGifDecoder();

		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		config.useImmersiveMode = true;
		config.useWakelock = true;

		initialize(new PhantomBallGame(), config);
	}
}
