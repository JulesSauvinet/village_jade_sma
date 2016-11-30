package mif24.jadedemo.util;

import mif24.jadedemo.model.status.AgeStatus;
import mif24.jadedemo.model.status.SexeStatus;

/**
 * Created by Jules on 15/05/2016.
 */
public class AgentIcons {

    public final static String ICON_PATH = "resources/images/";

    /* Woman */
    final static String woman_black = "woman_black.png";
    final static String woman_latino = "woman_latino.png";
    final static String woman_redhead = "woman_redhead.png";
    final static String woman_blond = "woman_blond.png";
    final static String woman_brownhair = "woman_brownhair.png";
    final static String woman_blackhair = "woman_blackhair.png";
    final static String woman_asian = "woman_asian.png";
    final static String[] womenIcon= {woman_black, woman_latino,woman_redhead,woman_blond,woman_brownhair,woman_blackhair,woman_asian};


    /* Man */
    final static String man_black = "man_black.png";
    final static String man_latino = "man_latino.png";
    final static String man_redhead = "man_redhead.png";
    final static String man_blond = "man_blond.png";
    final static String man_brownhair = "man_brownhair.png";
    final static String man_blackhair = "man_blackhair.png";
    final static String man_asian = "man_asian.png";
    final static String[] menIcon= {man_black, man_latino,man_redhead,man_blond,man_brownhair,man_blackhair,man_asian};


    /* girl */
    final static String girl_black = "girl_black.png";
    final static String girl_latino = "girl_latino.png";
    final static String girl_redhead = "girl_redhead.png";
    final static String girl_blond = "girl_blond.png";
    final static String girl_brownhair = "girl_brownhair.png";
    final static String girl_blackhair = "girl_blackhair.png";
    final static String girl_asian = "girl_asian.png";
    final static String[] girlsIcon= {girl_black, girl_latino,girl_redhead,girl_blond,girl_brownhair,girl_blackhair,girl_asian};


    /* boy */
    final static String boy_black = "boy_black.png";
    final static String boy_latino = "boy_latino.png";
    final static String boy_redhead = "boy_redhead.png";
    final static String boy_blond = "boy_blond.png";
    final static String boy_brownhair = "boy_brownhair.png";
    final static String boy_blackhair = "boy_blackhair.png";
    final static String boy_asian = "boy_asian.png";
    final static String[] boysIcon= {boy_black, boy_latino,boy_redhead,boy_blond,boy_brownhair,boy_blackhair,boy_asian};


    public static String getRandomIcon(AgeStatus ageStatus, SexeStatus sexeStatus) {
        String icon_path = ICON_PATH;
        switch (ageStatus){
            case CHILD:
                switch (sexeStatus){
                    case MASCULIN:
                        String boyIcon = boysIcon[(int)(Math.random()*100)%boysIcon.length];
                        icon_path+="boy/"+boyIcon;
                        break;
                    case FEMININ:
                        String girlIcon = girlsIcon[(int)(Math.random()*100)%girlsIcon.length];
                        icon_path+="girl/"+girlIcon;
                        break;
                }
                break;
            case ADULT:
                switch (sexeStatus){
                    case MASCULIN:
                        String manIcon = menIcon[(int)(Math.random()*100)%menIcon.length];
                        icon_path+="man/"+manIcon;
                        break;
                    case FEMININ:
                        String womanIcon = womenIcon[(int)(Math.random()*100)%womenIcon.length];
                        icon_path+="woman/"+womanIcon;
                        break;
                }
                break;
            case OLD:
                switch (sexeStatus){
                    case MASCULIN:
                        icon_path+="old/"+"old_man_generic.png";
                        break;
                    case FEMININ:
                        icon_path+="old/"+"old_woman_generic.png";
                        break;
                }
                break;
            default:
                break;
        }
        return icon_path;
    }
}
