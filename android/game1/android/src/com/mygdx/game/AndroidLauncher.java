package com.mygdx.game;

import android.content.Intent;
import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.mygdx.game.MyGdxGame;
import com.mygdx.game.activitys.ActivityConfig;
import com.mygdx.game.events.CommandBus;
import com.mygdx.game.events.ICommander;

public class AndroidLauncher extends AndroidApplication implements ICommander {
	static final int REQ_CONFIG = 100;
	private MyGdxGame myGdxGame;
	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		CommandBus.getInstance().register(this);
		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		myGdxGame = new MyGdxGame();
		initialize(myGdxGame, config);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public boolean onCommand(String cmd, Object... args) {
		switch (cmd){
			case "self_config":
				Intent it = new Intent(this, ActivityConfig.class);
				startActivityForResult(it,REQ_CONFIG);
				return true;
			case "join_room":

				return true;
			case "make_room":

				return true;
		}
		return false;
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}

	@Override
	protected void onDestroy() {
		CommandBus.getInstance().unregister(this);
		super.onDestroy();
	}
}
