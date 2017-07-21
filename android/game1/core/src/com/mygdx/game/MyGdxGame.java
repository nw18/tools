package com.mygdx.game;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;

public class MyGdxGame extends ApplicationAdapter {
	FrameInit frameInit;

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
}
