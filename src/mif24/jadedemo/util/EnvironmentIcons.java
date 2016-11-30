package mif24.jadedemo.util;

import mif24.jadedemo.model.descriptor.EntityDescriptor;

/**
 * Created by Jules on 15/05/2016.
 */
public class EnvironmentIcons {

    public final static String ICON_PATH = "resources/images/environment/";
    public static  int alea = 0;

    public static String getIcon(EntityDescriptor entityDescriptor) {
        String icon = ICON_PATH;
        switch (entityDescriptor) {
            case BERRY:
                icon+="berry_bush.png";
                break;
            case CAMP:
                icon+="camp.png";
                break;
            case WATER:
                icon=null;
                break;
            case TREE:
                icon+="tree.png";
                break;
            case HOUSE:
                icon+="house.png";
                break;
            case FIRE:
                icon += "fire_" + alea + ".png";
                alea = (alea+1)%2;
                break;
            case PEOPLE:
                icon=null;
                break;
            case NONE:
                icon = null;
                break;
            default:
                break;
        }
        return icon;
    }
}
