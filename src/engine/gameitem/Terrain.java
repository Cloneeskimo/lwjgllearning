package engine.gameitem;

import engine.graphics.HeightMap;

public class Terrain {

    //Data
    private final GameItem[] gameItems;

    //Constructor
    public Terrain(int blocksPerRow, float scale, float minY, float maxY, String heightMapFile, String textureFile, int textureInc) throws Exception {
        this.gameItems = new GameItem[blocksPerRow * blocksPerRow];
        HeightMap heightMap = new HeightMap(minY, maxY, heightMapFile, textureFile, textureInc);
        for (int row = 0; row < blocksPerRow; row++) {
            for (int col = 0; col < blocksPerRow; col++) {
                float xDisplacement = (col - ((float)blocksPerRow - 1) / 2.0f) * scale * heightMap.getXLength();
                float zDisplacement = (row - ((float)blocksPerRow - 1) / 2.0f) * scale * heightMap.getZLength();

                GameItem chunk = new GameItem(heightMap.getMesh());
                chunk.setScale(scale);
                chunk.setPosition(xDisplacement, 0, zDisplacement);
                gameItems[row * blocksPerRow + col] = chunk;
            }
        }
    }

    //Accessor
    public GameItem[] getGameItems() { return this.gameItems; }
}
