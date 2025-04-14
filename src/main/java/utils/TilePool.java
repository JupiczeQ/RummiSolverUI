package utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import model.Tile;

public class TilePool {
    private List<Tile> allTiles;

    public TilePool(){
        allTiles = new ArrayList<>();
        String[] colors = {"red", "blue", "black", "orange"};

        for(int set = 0; set < 2; set++)
        {
            for(String color : colors){
                for(int i = 1; i <= 13; i++){
                    allTiles.add(new Tile(i, color, false));
                }
            }
        }

        allTiles.add(new Tile(0,"joker",true));
        allTiles.add(new Tile(0,"joker",true));

        Collections.shuffle(allTiles);
    }

    public Tile drawTile(){
        if (allTiles.isEmpty()) {
            return null;
        }
        return allTiles.remove(0);
    }

    public boolean isEmpty(){
        return allTiles.isEmpty();
    }

    public int size(){
        return allTiles.size();
    }
}