package com.mygdx.game;

import android.content.Intent;
import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.mygdx.game.MyGdxGame;
import com.mygdx.game.activitys.ActivityConfig;
import com.mygdx.game.activitys.ActivityCreateRoom;
import com.mygdx.game.activitys.ActivityJoinRoom;
import com.mygdx.game.events.CommandBus;
import com.mygdx.game.events.ICommander;

public class AndroidLauncher extends AndroidApplication implements ICommander {
	static final int REQ_CONFIG = 100;
	static final int REQ_JOIN_ROOM = 200;
	static final int REQ_CREATE_ROOM = 300;
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
		Intent it = null;
		switch (cmd){
			case "self_config":
				it = new Intent(this, ActivityConfig.class);
				startActivityForResult(it,REQ_CONFIG);
			case "join_room":
				it = new Intent(this, ActivityCreateRoom.class);
				startActivityForResult(it,REQ_CREATE_ROOM);
			case "make_room":
				it = new Intent(this, ActivityJoinRoom.class);
				startActivityForResult(it,REQ_JOIN_ROOM);
		}
		return it != null;
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
