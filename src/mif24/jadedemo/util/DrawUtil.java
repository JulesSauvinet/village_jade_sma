package mif24.jadedemo.util;

import java.awt.*;
import java.util.Random;

/**
 * Created by Jules on 18/05/2016.
 */
public class DrawUtil {

    public static Color getRandomColor(){
        Random rand = new Random();

        float r = rand.nextFloat();
        float g = rand.nextFloat();
        float b = rand.nextFloat();

        Color randomColor = new Color(r, g, b);

        return randomColor;
    }
}
