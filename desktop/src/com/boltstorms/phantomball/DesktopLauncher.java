package com.boltstorms.phantomball;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.boltstorms.phantomball.tools.DesktopGifDecoder;
import com.boltstorms.phantomball.tools.GifLoader;

public class DesktopLauncher {
	public static void main(String[] arg) {

		// ✅ Desktop uses the ImageIO/AWT decoder (desktop module only)
		GifLoader.DECODER = new DesktopGifDecoder();

		Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
		config.setForegroundFPS(60);
		config.setTitle("PhantomBall");

		// Phone-like portrait window
		config.setWindowedMode(540, 960);
		config.setResizable(false);

		new Lwjgl3Application(new PhantomBallGame(), config);
	}
}
