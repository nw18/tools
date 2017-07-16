package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;

public class MyGdxGame extends ApplicationAdapter {
	Texture img;
	int width,height;
	Stage mainStage;

	@Override
	public void resize(int width, int height) {
		this.width = width;
		this.height = height;
	}

	@Override
	public void create () {
		img = new Texture("badlogic.jpg");
		mainStage = new Stage();
		Gdx.input.setInputProcessor(mainStage);
		mainStage.addActor(new MyActor(new TextureRegion(img)));
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(1, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		//batch.begin();
		mainStage.act();
		mainStage.draw();
		//batch.end();
	}
	
	@Override
	public void dispose () {
		mainStage.dispose();
		img.dispose();
	}
}
