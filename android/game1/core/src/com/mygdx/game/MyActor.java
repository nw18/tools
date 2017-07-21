package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.Box2D;
import com.badlogic.gdx.physics.box2d.utils.Box2DBuild;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

/**
 * Created by Administrator on 2017/7/16.
 */

public class MyActor extends Actor  {
    private final TextureRegion textture;

    public MyActor(TextureRegion texture) {
        //super();
        this.textture = texture;
        setSize(texture.getRegionWidth(),texture.getRegionHeight());
        addListener(clickListener);
    }

    private ClickListener clickListener = new ClickListener() {
        @Override
        public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
            setX((float) Math.random() * (Gdx.graphics.getWidth() - getWidth()));
            setY((float) Math.random() * (Gdx.graphics.getHeight() - getHeight()));
            return true;
        }
    };

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
        if (this.textture != null) {
            batch.draw(textture,getX(), getY(),
                    getOriginX(), getOriginY(),
                    getWidth(), getHeight(),
                    getScaleX(), getScaleY(),
                    getRotation());
        }
    }

    @Override
    public void act(float delta) {
        super.act(delta);
    }
}
