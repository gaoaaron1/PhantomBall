package com.boltstorms.phantomball;

import com.badlogic.gdx.Game;
import com.boltstorms.phantomball.screens.MenuScreen;

public class PhantomBallGame extends Game {

	@Override
	public void create() {
		setScreen(new MenuScreen(this));
	}
}
