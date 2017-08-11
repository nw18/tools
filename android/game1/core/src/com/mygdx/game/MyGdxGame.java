package com.mygdx.game;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.mygdx.game.events.ICommander;

public class MyGdxGame extends ApplicationAdapter {
	private static MyGdxGame theGame = null;
	private ICommander commander;
	private FrameInit frameInit;

	public MyGdxGame() {
		theGame = this;
	}

	@Override
	public void resize(int width, int height) {

	}

	@Override
	public void create () {
		Gdx.app.setLogLevel(Application.LOG_DEBUG);
		frameInit = new FrameInit();
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(0, 0, 0, 0);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		frameInit.render(Gdx.graphics.getDeltaTime());
	}
	
	@Override
	public void dispose () {
		frameInit.dispose();
	}

	public static MyGdxGame getInstance() {
		return theGame;
	}
}
