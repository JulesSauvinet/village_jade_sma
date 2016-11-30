package mif24.jadedemo.container;


import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;


/**
 * Created by Jules on 13/05/2016.
 */
public class MainBoot {

    public static String PROPERTIES_FILE = "resources/prop.properties";
    public static Runtime  mainRT = null;
    /**
     * @param args
     */
    public static void main(String[] args) {
        Runtime rt = Runtime.instance();
        mainRT = rt;
        Profile p = null;
        try {
            p = new ProfileImpl(PROPERTIES_FILE);
            AgentContainer container = rt.createMainContainer(p);
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }

    }

}
