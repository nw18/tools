package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.UUID;

/**
 * Created by newind on 17-7-21.
 */

public class Frame implements Screen {
    private static class NameStage {
        private String name;
        private Stage stage;
        NameStage(Stage stage) {
            this.name = UUID.randomUUID().toString();
            this.stage = stage;
        }
        NameStage(String name,Stage stage) {
            this.name = name;
            this.stage = stage;
        }
    }

    LinkedList<NameStage> stageLinkedList = new LinkedList<NameStage>();

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        if(stageLinkedList.isEmpty()) {
            return;
        }
        Stage stage = stageLinkedList.getLast().stage;
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        for (NameStage nameStage: stageLinkedList) {
            nameStage.stage.dispose();
        }
        stageLinkedList.clear();
    }

    public void pushStage(String name,Stage stage) {
        stageLinkedList.addLast(new NameStage(name, stage));
        Gdx.input.setInputProcessor(stage);
    }

    public void pushStage(Stage stage) {
        stageLinkedList.addLast(new NameStage(stage));
        Gdx.input.setInputProcessor(stage);
    }

    public boolean showStage(String name) {
        for (Iterator<NameStage> it = stageLinkedList.iterator(); it.hasNext();){
            NameStage nameStage = it.next();
            if (nameStage.name.equals(name)) {
                it.remove();
                stageLinkedList.push(nameStage);
                Gdx.input.setInputProcessor(nameStage.stage);
                return true;
            }
        }
        return false;
    }

    public boolean showStage(Stage stage) {
        for (Iterator<NameStage> it = stageLinkedList.iterator(); it.hasNext();){
            NameStage nameStage = it.next();
            if (stage == nameStage.stage) {
                it.remove();
                stageLinkedList.push(nameStage);
                Gdx.input.setInputProcessor(nameStage.stage);
                return true;
            }
        }
        return false;
    }



    ///// 创建一个按钮
    public static Button makeButton(String fileName) {
        Button.ButtonStyle style = new Button.ButtonStyle();
        Texture texMakeRoom = new Texture(Gdx.files.internal(fileName));
        int buttonWidth = texMakeRoom.getWidth();
        int buttonHeight = texMakeRoom.getHeight() / 2;
        style.up = new TextureRegionDrawable(new TextureRegion(texMakeRoom,0,0,buttonWidth,buttonHeight));
        style.down = new TextureRegionDrawable(new TextureRegion(texMakeRoom,0,buttonHeight,buttonWidth,buttonHeight));
        return new Button(style);
    }

    public static TextField makeTextField(int width,int height) {

    }

    public static Label makeLabel(String text){

    }

    public static Texture makeRectBorder(int width, int height, Color color) {
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        pixmap.setColor(color.r,color.g,color.b,color.a);
        pixmap.drawRectangle(0, 0, pixmap.getWidth(), pixmap.getHeight());
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    public static class Layout {
        public static void InVertical(Stage stage,int xStart, int yFrom,int yTo,int width,int height) {
            Array<Actor> array = stage.getActors();
            int minY = Math.min(yFrom,yTo);
            int maxY = Math.max(yFrom,yTo);
            float space = ((maxY - minY) - height * array.size) / (array.size + 1.0f);
            for (int i = 0; i < array.size; i++) {
                array.get(i).setSize(width,height);
                array.get(i).setPosition(xStart, minY + (i+1) * space + i * height);
            }
        }

        public static void InDialog(Stage stage,int a){

        }
    }
}
