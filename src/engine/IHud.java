package engine;

import engine.gameitem.GameItem;

public interface IHud {

    //GameItem Array
    GameItem[] getGameItems();

    //Cleanup Method
    default void cleanup() {
        GameItem[] gameItems = this.getGameItems();
        for (GameItem gameItem : gameItems) {
            gameItem.cleanup();
        }
    }
}
