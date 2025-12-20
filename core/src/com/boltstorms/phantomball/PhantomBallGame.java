package com.boltstorms.phantomball;

import com.badlogic.gdx.Game;
import com.boltstorms.phantomball.screens.GameScreen;
import com.boltstorms.phantomball.screens.MenuScreen;

public class PhantomBallGame extends Game {

	@Override
	public void create() {
		// Start the game on the GameScreen
		setScreen(new MenuScreen(this));

	}
}
