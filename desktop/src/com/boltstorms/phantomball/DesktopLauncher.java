package com.boltstorms.phantomball;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

public class DesktopLauncher {
	public static void main(String[] arg) {
		Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
		config.setForegroundFPS(60);
		config.setTitle("PhantomBall");

		// Phone-like portrait window
		config.setWindowedMode(540, 960);
		config.setResizable(false);

		new Lwjgl3Application(new PhantomBallGame(), config);
	}
}
