package mif24.jadedemo.container;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.core.Runtime;
import jade.wrapper.ContainerController;
import mif24.jadedemo.agent.ApplicationAgent;

/**
 * Created by Jules on 13/05/2016.
 */
public class SecondBoot {

    public static String PROPERTIES_FILE = "resources/propertiesSecond.properties";
    public static Runtime secondRT = null;
    /**
     * @param args
     */
    public static void main(String[] args) {
        Runtime rt = Runtime.instance();
        secondRT = rt;
        Profile p = null;
        try {
            p = new ProfileImpl(PROPERTIES_FILE);
            ContainerController container = rt.createAgentContainer(p);
            AgentController app = container.createNewAgent("appAgent",
                    "mif24.jadedemo.agent.ApplicationAgent", null);
            app.start();

        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }

    }

}