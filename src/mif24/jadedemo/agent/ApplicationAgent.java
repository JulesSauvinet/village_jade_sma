package mif24.jadedemo.agent;

import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.TickerBehaviour;
import mif24.jadedemo.container.SecondBoot;
import mif24.jadedemo.model.descriptor.VillageAgentDescriptor;
import mif24.jadedemo.model.descriptor.VillageDescriptor;
import mif24.jadedemo.model.descriptor.VillagePolicyDescriptor;
import mif24.jadedemo.model.status.AgeStatus;
import mif24.jadedemo.gui.VillageGUI;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * L'agent (unique) responsable de l'application : vue de tous les agents, en charge du GUI.
 * @author Yann
 */
public class ApplicationAgent extends Agent {
    
    VillageGUI villageGUI = null;
    boolean doQuit = false;
    ConcurrentHashMap<AID, VillageAgentDescriptor> agentMap = new ConcurrentHashMap<>();
    private ArrayList<AgentController> agentsControllers = new ArrayList<>();

    private CyclicBehaviour cyclicBehaviour = null;
    VillageDescriptor villageDescriptor = null;
    VillagePolicyDescriptor policyDescriptor = null; // politique du village: instance unique répliquée à travers tous les agents.

    private ArrayList<AID> deadAgents = new ArrayList<>();
    private int agentNameCounter = 0;

    private boolean registered = false; // enregistré auprès des pages jaunes ou pas

    //synchronized
    public ConcurrentHashMap<AID, VillageAgentDescriptor> getAgentMap(){
        //return new ConcurrentHashMap<>(agentMap);
        return agentMap;
    }
    
    protected void setup() {
        
  	System.out.println("Hello World! Je suis l'ApplicationAgent en charge du GUI, mon nom est "+getLocalName()+".");   	

        // S'enregistrer auprès des pages jaunes de JADE:
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());

        DFAgentDescription dfdEmpty = new DFAgentDescription();
        dfdEmpty.setName(getAID());

        ServiceDescription sd = new ServiceDescription();
        sd.setType("ApplicationAgent-Services");
        sd.setName("SendTo-AppAgent-AgentDescriptor");
        dfd.addServices(sd);
        try {
            if (DFService.search(this, dfdEmpty).length != 0){
                DFService.modify(this, dfd);
            }
            else{
                DFService.register(this,dfd);
            }
            registered = true;
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }

        // Création du GUI:
        final ApplicationAgent app = this;
        javax.swing.SwingUtilities.invokeLater(new Runnable(){
            public void run(){
                villageDescriptor = new VillageDescriptor();
                villageGUI = new VillageGUI(app, villageDescriptor);
            }
        });

    }

    public void start(){
        startVillageAgents();

        addCyclicBehaviour();// polling des descriptors des agents du village
        addUpdateVillageBehaviour();// m.a.j du village (eau, etc)
        addUpdateGraphicBehaviour();// m.a.j du GUI

    }



    private void addUpdateVillageBehaviour(){
        addBehaviour(new TickerBehaviour(this,1000) {
            @Override
            protected void onTick() {
                villageDescriptor.update();                
            }
        });
    }

    private void addUpdateGraphicBehaviour() {
        addBehaviour(new TickerBehaviour(this,1000) {
            @Override
            protected void onTick() {
                if (villageGUI != null){
                    villageGUI.updateGraphics();
                }
            }
        });
    }

    private void startVillageAgents() {
        policyDescriptor = new VillagePolicyDescriptor();
        int chiefIndex = (int)Math.floor(Math.random()*(double)(villageDescriptor.nbVillageAgentsAdults-1));
        // Création des Agents adultes:
        for (int i=0; i < villageDescriptor.nbVillageAgentsAdults; ++i){
            try {
                String nameAgent = "Villageois" + agentNameCounter;
                VillageAgentDescriptor agentDescriptor = new VillageAgentDescriptor(VillageAgent.agentWidth,VillageAgent.agentHeight, AgeStatus.ADULT, nameAgent);
                if (i == chiefIndex)
                    agentDescriptor.setChief(true);

                agentNameCounter++;
                villageDescriptor.updatePosition(agentDescriptor);
                Object[] arguments = new Object[3];
                arguments[0] = agentDescriptor;
                arguments[1] = villageDescriptor;
                arguments[2] = policyDescriptor;
                AgentController agentController = getContainerController().createNewAgent(nameAgent, "mif24.jadedemo.agent.VillageAgent", arguments);
                agentsControllers.add(agentController);
                agentController.start(); // Lancement de l'agent JADE

            } catch (StaleProxyException ex) {
                System.out.println("Exception : "+ex);
            }
        }
        // Création des Agents enfants:
        for (int i=0; i < villageDescriptor.nbVillageAgentsChildren; ++i){
            spawnKid();
        }
    }

    private void addCyclicBehaviour() {
        cyclicBehaviour = new CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage msg = myAgent.receive();

                if (msg != null){
                    try {
                         if (msg.getProtocol().equals("updateDescriptor")) {
                             /* On récupère le descriptor */
                             VillageAgentDescriptor descriptor = (VillageAgentDescriptor) msg.getContentObject();
                             AID senderAID = msg.getSender();

                             if (!deadAgents.contains(senderAID) && descriptor.dead){
                                 if (descriptor.chief){
                                     electNewChief();
                                 }
                                 killAgent(senderAID);
                             }
                             /*else if (!deadAgents.contains(senderAID)){
                                 //On met a jour l'age de l'agent
                                 updateAgeVilAgent(descriptor, senderAID);
                             }*/

                             else if (!deadAgents.contains(senderAID)) {
                                 //On met à jour l'id de l'agent
                                 updateIdAgent(descriptor, senderAID);
                                 //On met à jour la position de l'agent
                                 updatePosVillageAgent(descriptor, senderAID);
                                 //On met a jour les voisins de l'agent
                                 updateNeighboursAgent(descriptor);

                                /* On envoie le descriptor */
                                 ACLMessage answer = new ACLMessage(ACLMessage.INFORM);
                                 answer.addReceiver(senderAID);
                                 try {
                                     answer.setProtocol("updateDescriptor");
                                     answer.setContentObject(descriptor);
                                 } catch (IOException ex) {
                                     ex.printStackTrace();
                                 }
                                 myAgent.send(answer);

                                 if (!descriptor.dead) {
                                     agentMap.put(senderAID, descriptor);
                                 }
                             }
                         }
                         else if (msg.getProtocol().equals("spawnKid")){
                             spawnKid();
                             VillageDescriptor.nbVillageAgentsChildren++;
                             VillageDescriptor.nbVillageois++;
                             System.out.println("[nouvelle naissance]");
                         }
                         else if (msg.getProtocol().equals("NeedNewHouse")){                             
                             try {                                 
                                int housePos = villageDescriptor.getAvailableHouse();
                                if (housePos != -1)
                                    villageDescriptor.houseAvailables.remove(housePos);
                                ACLMessage resp = new ACLMessage(ACLMessage.INFORM);
                                resp.setProtocol("NewHouseResponse");
                                resp.setContentObject(new Integer(housePos));
                                resp.addReceiver(msg.getSender());
                                myAgent.send(resp);                                                                 
                             } catch (IOException ex) {
                                 //
                             }                             
                         }
                         else if (msg.getProtocol().equals("LeaveHouse")){ // appellé quand une villageoise quitte sa maison (marriage) et la donne au premier venu
                             Integer housePos = (Integer)msg.getContentObject();
                             if (housePos != -1)
                                 villageDescriptor.houseAvailables.add(housePos);
                         }
                    }
                    catch (UnreadableException e){
                        e.printStackTrace();
                    }
                } else {
                    block();//économise le CPU (le behaviour est débloqué dès qu'un nouveau message arrive...)
                }
            }
        };
        addBehaviour(cyclicBehaviour);
    }

    private void updateIdAgent(VillageAgentDescriptor descriptor, AID senderAID) {
        villageDescriptor.updateIdAgent(descriptor, senderAID);
    }

    private void updateNeighboursAgent(VillageAgentDescriptor descriptor) {
        villageDescriptor.updateNeighbours(descriptor);
    }

    private void spawnKid(){
        try {
            String nameAgent = "Villageois-" + agentNameCounter;
            VillageAgentDescriptor agentDescriptor = new VillageAgentDescriptor(VillageAgent.agentWidth,VillageAgent.agentHeight, AgeStatus.CHILD, nameAgent);
            agentDescriptor.setAge(0.0);
            villageDescriptor.updatePosition(agentDescriptor);
            agentNameCounter++;

            Object[] arguments = new Object[3];
            arguments[0] = agentDescriptor;
            arguments[1] = villageDescriptor;
            arguments[2] = policyDescriptor;
            AgentController agentController = getContainerController().createNewAgent(nameAgent, "mif24.jadedemo.agent.VillageAgent", arguments);
            agentsControllers.add(agentController);
            agentController.start();
        } catch (StaleProxyException ex) {
            System.out.println("Exception : "+ex);
        }
    }

    private void electNewChief()
    {
        // à développer: pour l'instant méchanisme basique
        AID nextChiefAID = null;
        while (nextChiefAID == null){
            
            int rand = (int)(Math.random() * (agentMap.size()-1));            
            int i =0;
            for (AID aid : agentMap.keySet()){
                if (i == rand && agentMap.get(aid).getEntity().ageStatus == AgeStatus.ADULT){
                    nextChiefAID = aid;
                    break;
                }
                i++;
            }
        }
        ACLMessage order = new ACLMessage(ACLMessage.INFORM);
        order.addReceiver(nextChiefAID);
        order.setProtocol("YouAreNextChief");
        send(order);
    }
    
    private void killAgent(final AID senderAID) {
        
        synchronized (agentsControllers) { 
          synchronized (agentMap) {
                
                AgentController agentToRemove = null;
                System.out.println("Kill agent :" + senderAID.getLocalName());
                for (AgentController ac : agentsControllers) {
                    try {
                        if (ac != null) {
                            if (ac.getName().equals(senderAID.getName())) {
                                //System.out.println("agent " + ac.getName() + " mort");
                                agentToRemove = ac;
                                break;
                            }
                        }
                    } catch (StaleProxyException e) {
                        e.printStackTrace();
                    }
                }
                addBehaviour(new OneShotBehaviour() {
                    @Override
                    public void action() {
                        ACLMessage killOrder = new ACLMessage(ACLMessage.INFORM);
                        killOrder.addReceiver(senderAID);
                        killOrder.setProtocol("kill");
                        myAgent.send(killOrder);
                    }
                });
                /*try {
                    agentToRemove.kill();
                } catch (StaleProxyException e) {
                    e.printStackTrace();
                }*//* soit ça soit un suicide*/
                //Pour l'instant suicide car plus drole?*/
                agentMap.remove(senderAID);
                agentsControllers.remove(agentToRemove);
                deadAgents.add(senderAID);
            }
        }

    }

    private void updatePosVillageAgent(VillageAgentDescriptor descriptor, AID senderAID) {
//        System.out.println("ApplicationAgent : j'ai reçu les coordonnées de " + senderAID.getLocalName() + " (" + descriptor.getX() + ", " + descriptor.getY() + ")");
//        System.out.println("ApplicationAgent : updatePos de " + senderAID.getLocalName());
        villageDescriptor.updatePosition(descriptor);
    }

    public void takeDown() {
        // Deregister from the yellow pages
        if (registered)
            try {
                DFService.deregister(this);
                registered = false;
            } catch (FIPAException fe) {
                fe.printStackTrace();
            }

        try{
            SecondBoot.secondRT.shutDown();
        } catch(Exception ex){
            System.out.println(ex);
        }

        // Close the GUI
        villageGUI.dispose();
        // Printout a dismissal message
        System.out.println("L'ApplicationAgent se termine.");
        System.exit(0);

    }
    
    public void quitApplication(){
        doQuit = true;
    }

    public void reset() {
        System.out.println("reset");
        if (cyclicBehaviour != null) {
            for (AgentController ac : agentsControllers){
                try {
                    System.out.println("kill agent : " + ac.getName());
                    ac.kill();
                } catch (StaleProxyException e) {
                    e.printStackTrace();
                }
            }
            villageGUI.reset();
            this.removeBehaviour(cyclicBehaviour);
            cyclicBehaviour = null;
            agentsControllers.clear();
            agentMap.clear();
        }
    }
}
