package mif24.jadedemo.agent;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import mif24.jadedemo.model.descriptor.VillageAgentDescriptor;
import mif24.jadedemo.model.descriptor.VillageDescriptor;
import mif24.jadedemo.model.descriptor.VillagePolicyDescriptor;
import mif24.jadedemo.model.entity.VillagerEntity;
import mif24.jadedemo.model.status.AgeStatus;
import mif24.jadedemo.model.status.SexeStatus;
import mif24.jadedemo.util.DrawUtil;

import java.awt.*;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Yann
 */
public class VillageAgent extends Agent {

    static int agentWidth = VillageDescriptor.entityWidth;
    static int agentHeight = VillageDescriptor.entityHeight;

    VillageDescriptor villageDescriptor = null;// read only à cause des threads
    VillagePolicyDescriptor policy = null; // politique courante
    VillageAgentDescriptor descriptor = null;

    AID applicationAgentAID;
    boolean registered = false; // enregistré auprès des pages jaunes ou pas
    int childrenCount = 0;

    protected void setup(){
        
        descriptor = (VillageAgentDescriptor) getArguments()[0];
        villageDescriptor = (VillageDescriptor) getArguments()[1];
        policy = (VillagePolicyDescriptor) getArguments()[2];

        if (descriptor.chief)
            System.out.println("Bonjour, je suis le Chef du village "+getLocalName()+", j'ai "+(int)descriptor.getAge()+" ans.");
        else System.out.println("Bonjour, je suis l'agent "+getLocalName()+", j'ai "+(int)descriptor.getAge()+" ans.");

        // Lire les pages jaunes pour découvrir le nom (AID) de l'ApplicationAgent:
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("ApplicationAgent-Services");
        template.addServices(sd);
        try
        {
            DFAgentDescription[] result = DFService.search(this, template);
            assert(result.length == 1);
            applicationAgentAID = result[0].getName();            
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
        System.out.println(getLocalName()+" : je connais l'ApplicationAgent, son AID = "+applicationAgentAID);

        // Pages Jaunes:
        register();
        // Comportements:
        tickerUpdateDescriptor();
        if (descriptor.isChief())
            tickerUpdatePolicy();
        if (descriptor.getEntity().ageStatus == AgeStatus.ADULT){
            tickerFindPartner();
            tickerMakeKids();
            tickerOther();
        }
        cyclicListenToMessages();
    }

    // M.A.J des pages jaunes pour cet agent:
    private void updateRegister(){
        // on se dé-enregistre avant de se ré-enregistrer avec éventuellement de nouveaux services FIPA.
        deregister();
        register();
    }

    private void register(){

        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());

        boolean reg = false;
        if (descriptor.getEntity().conjoint == null && descriptor.getEntity().ageStatus != AgeStatus.CHILD)
        {
            reg = true;
            ServiceDescription sd = new ServiceDescription();
            if (descriptor.getEntity().sexeStatus == SexeStatus.MASCULIN)
                sd.setType("VillageAgent-Adult-Man");
            else sd.setType("VillageAgent-Adult-Woman");
            sd.setName(getLocalName());
            dfd.addServices(sd);
        }
        if (reg)
            try {
                DFService.register(this, dfd);
                registered = true;
            } catch (FIPAException fe) {
                try {
                    DFService.modify(this, dfd);
                    System.out.println("On modifie la dfd de l'agent qui etait deja enregistre");
                } catch (FIPAException e) {
                    e.printStackTrace();
                }
            }
    }

    private void deregister() {

        if (!registered)
            return;
        try {
            DFService.deregister(this);
            registered = false;
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }

    private void cyclicListenToMessages(){
        /* Behaviour dédié à la réception de messages : plus robuste */
        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {

                ACLMessage msg = myAgent.receive();
                if (msg != null){
                    try {
                        if (msg.getProtocol().equals("kill")) {
                            System.out.println("Je suis mort : j'etais " + getLocalName() + ", j'avais " + (int) descriptor.getAge() + " ans...");
                            if (descriptor.getEntity().conjoint == null && descriptor.getEntity().housePos != -1){
                                try{
                                    ACLMessage info = new ACLMessage(ACLMessage.INFORM);
                                    info.addReceiver(applicationAgentAID);
                                    info.setProtocol("LeaveHouse");
                                    info.setContentObject(descriptor.getEntity().housePos);                                    
                                    myAgent.send(info);
                                    System.out.println(getLocalName()+": après ma mort, je lègue ma maison au village...");
                                } catch (Exception ex){
                                    System.err.println("Erreur - impossible de mettre à jour la maison après décès.");
                                }
                            } 
                            else if (descriptor.getEntity().conjoint != null){
                                ACLMessage deadinfo = new ACLMessage(ACLMessage.INFORM);
                                deadinfo.addReceiver(descriptor.getEntity().conjoint._idAgent);
                                deadinfo.setProtocol("ConjointMort");
                                myAgent.send(deadinfo);
                            }
                            doDelete();
                        } 
                        else if (msg.getProtocol().equals("updateDescriptor")) {
                            try {
                                descriptor = (VillageAgentDescriptor) msg.getContentObject();
                            } catch (UnreadableException e) {
                                e.printStackTrace();
                            }
                        } 
                        else if (msg.getProtocol().equals("askPartner")) {
                            try {
                                VillagerEntity potentialPartner = (VillagerEntity) msg.getContentObject();

                                //Si l'agent n'est pas en couple et qu'il a été sensible au charme de l'agent qui lui a fait une proposition
                                if (!descriptor.getEntity().inCouple() && descriptor.getEntity().matchWith(potentialPartner)) {
                                    descriptor.getEntity().toCouple();
                                    descriptor.getEntity().conjoint = potentialPartner;
                                    descriptor.getEntity().colorCouple = DrawUtil.getRandomColor();

                                    AID partnerAID = msg.getSender();

                                    ACLMessage response = new ACLMessage(ACLMessage.INFORM);
                                    response.addReceiver(partnerAID);

                                    System.out.println("Je contacte: " + partnerAID.getLocalName() + "pour lui répondre que je suis aussi in love, " + "je suis : " + this.getAgent().getAID().getLocalName());

                                    try {
                                        response.setProtocol("okPartner");
                                        response.setContentObject(descriptor.getEntity());
                                    } catch (IOException ex) {
                                        ex.printStackTrace();
                                    }
                                    myAgent.send(response);
                                }

                            } catch (UnreadableException e) {
                                //e.printStackTrace();// 1 fois sur 100 on passe là (pourquoi?)
                            }
                        } 
                        else if (msg.getProtocol().equals("okPartner")) {
                            if (descriptor.getEntity().conjoint == null) {

                                VillagerEntity ve = (VillagerEntity) msg.getContentObject();
                                descriptor.getEntity().toCouple();
                                descriptor.getEntity().conjoint = ve;
                                descriptor.getEntity().colorCouple = ve.colorCouple;
                                if (descriptor.getEntity().sexeStatus == SexeStatus.FEMININ){                                    
                                    try{
                                        ACLMessage info = new ACLMessage(ACLMessage.INFORM);
                                        info.addReceiver(applicationAgentAID);
                                        info.setProtocol("LeaveHouse");
                                        info.setContentObject(descriptor.getEntity().housePos);
                                        descriptor.getEntity().housePos = descriptor.getEntity().conjoint.housePos; // je quitte ma maison pour celle du conjoint
                                        myAgent.send(info);
                                        System.out.println(getLocalName()+": je quitte ma maison pour celle de mon conjoint.");
                                    } catch (Exception ex){
                                        System.err.println("Erreur - impossible de mettre à jour la maison après marriage.");
                                    }
                                }
                                AID conjointAID = msg.getSender();// Union célébrée et consomée ...
                                //System.out.println("MARRIAGE : je suis "+getLocalName()+", je me marrie avec "+conjointAID.getLocalName());
                                updateRegister();
                                ACLMessage resp = new ACLMessage(ACLMessage.INFORM);
                                resp.addReceiver(conjointAID);

                                resp.setProtocol("okPartner_ACK");
                                myAgent.send(resp);
                            }
                        }
                        else if (msg.getProtocol().equals("okPartner_ACK")) {
                            AID conjointAID = msg.getSender();// Union célébrée et consomée ...
                            System.out.println("MARRIAGE : je suis " + getLocalName() + ", je me marrie avec " + conjointAID.getLocalName());
                            updateRegister();     
                            if (descriptor.getEntity().sexeStatus == SexeStatus.FEMININ){                                    
                                try{
                                    ACLMessage info = new ACLMessage(ACLMessage.INFORM);
                                    info.addReceiver(applicationAgentAID);
                                    info.setProtocol("LeaveHouse");
                                    info.setContentObject(descriptor.getEntity().housePos);
                                    descriptor.getEntity().housePos = descriptor.getEntity().conjoint.housePos; // je quitte ma maison pour celle du conjoint
                                    myAgent.send(info);
                                    System.out.println(getLocalName()+": je quitte ma maison pour celle de mon conjoint.");
                                } catch (Exception ex){
                                    System.err.println("Erreur - impossible de mettre à jour la maison après marriage.");
                                }
                            }
                        } 
                        else if (msg.getProtocol().equals("ConjointMort")){
                            descriptor.getEntity().conjoint = null;
                            // pour l'instant on ne se remarrie pas..
                        }
                        else if (msg.getProtocol().equals("spawnKid") && descriptor.getEntity().conjoint != null && descriptor.getEntity().conjoint._idAgent == msg.getSender()) {
                            childrenCount++;// "mon conjoint viens de m'informer qu'on a fait un enfant"
                        }
                        else if (msg.getProtocol().equals("YouAreNextChief")){
                            descriptor.chief = true;
                            tickerUpdatePolicy();
                            System.out.println(getLocalName() + " : JE SUIS LE NOUVEAU CHEF DU VILLAGE");
                        }
                        else if (msg.getProtocol().equals("NewHouseResponse") && descriptor.getEntity().housePos == -1){
                            Integer newHousePos = (Integer)msg.getContentObject();
                            if (newHousePos == -1){
                                descriptor.getEntity().decreaseHappiness(12.0);
                                System.out.println(getLocalName()+" [MALHEUREUX] : Je n'ai toujours pas de maison...");
                            } else {
                                descriptor.getEntity().increaseHappiness(25.0);
                                descriptor.getEntity().housePos = newHousePos;
                                System.out.println(getLocalName()+": j'ai une nouvelle maison !");
                            }
                        }
                    } catch (UnreadableException e) {
                        e.printStackTrace();
                    }
                }
                else block();
            }
        });
    }

    
    private void updateAgent(){
        updateAge();
        if (!descriptor.dead){
            if (villageDescriptor.isOnFire){
                double r = Math.random()*100.0;
                if (r > 30.0)
                  die();  
            }
            if (villageDescriptor.nbVillageQuantiteDeau < 100.0){
                double r = Math.random()*100.0;
                if (r > 50.0){
                  System.out.println("Je meurs de déshydratation...");
                  die();
                }
            }
            if (villageDescriptor.nbVillageQuantiteNourriture < 100.0){
                double r = Math.random()*100.0;
                if (r > 70.0){
                  System.out.println("Je meurs de faim...");
                  die();
                }
            }                
        }
    }
    
    private void die(){
        descriptor.dead = true;
        if (descriptor.getAgeStatus() == AgeStatus.CHILD){
            VillageDescriptor.nbVillageAgentsChildren--;
        }
        else {
            VillageDescriptor.nbVillageAgentsAdults--;
        }
        VillageDescriptor.nbVillageois--;
    }
    
    private void updateAge(){

        boolean trans = (descriptor.getEntity().ageStatus == AgeStatus.CHILD);
        descriptor.updateAge();
        if (descriptor.getEntity().age >= 100){
            die();
        }
        else {
            int deadRandom = (int)((Math.random()*descriptor.getEntity().age*descriptor.getEntity().age)/100);
            if (deadRandom > 70){
                die();
            }
        }
        
        if (trans && !descriptor.dead && descriptor.getEntity().ageStatus==AgeStatus.ADULT){// passage à l'âge adulte:
            updateRegister();
            tickerFindPartner();
            tickerMakeKids();
            tickerOther();
        }
    }

    private void tickerUpdateDescriptor() {
        /*
         * ajoute un TickerBehaviour qui:
         *   - contacte l'ApplicationAgent
         *   - lui communique le descriptor (sous forme de message ACL)
         */
        addBehaviour(new TickerBehaviour(this, 2000){
            protected void onTick(){

                updateAgent();

                ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                msg.addReceiver(applicationAgentAID);
//                System.out.println("Je notifie l'application agent de mon nouvel état - " + this.getAgent().getAID());
                try {
                    msg.setProtocol("updateDescriptor");
                    msg.setContentObject(descriptor);
                } catch (IOException ex){
                    ex.printStackTrace();
                }
                myAgent.send(msg);
            }
        });
    }

    private void tickerUpdatePolicy(){
        /*
         * ajoute un TickerBehaviour qui:
         *   - met à jour la politique du village (cet agent est le chef du village)
         */
        addBehaviour(new TickerBehaviour(this, 8000){
            protected void onTick(){
                if (!descriptor.chief)
                    return;

                if (villageDescriptor.nbVillageQuantiteNourriture < 300){
                    policy.setMakeFields(true);
                    System.out.println("CHIEF SAYS : more fields !");
                } else {
                    policy.setMakeFields(false);
                    System.out.println("CHIEF SAYS : enough fields.");
                }

                if (villageDescriptor.nbVillageAgentsAdults/2 > villageDescriptor.nbVillageMaisons){
                    policy.setMakeHouses(true);
                    System.out.println("CHIEF SAYS : more houses !");
                } else {
                    policy.setMakeHouses(false);
                    System.out.println("CHIEF SAYS : enough houses.");
                }

                double ratio = (double)villageDescriptor.nbVillageAgentsChildren / (double)villageDescriptor.nbVillageAgentsAdults;
                if (ratio < 1.0){
                    policy.setMaxChildrenPerCouple(policy.getMaxChildrenPerCouple() + 1);
                    System.out.println("CHIEF SAYS : more children !");
                } else if (ratio > 2.0){
                    policy.setMaxChildrenPerCouple(policy.getMaxChildrenPerCouple() - 1);
                    System.out.println("CHIEF SAYS : less children.");
                }
            }
        });
    }

    private void tickerFindPartner(){

        // Enfin ajouter le comportement cyclique de recherce de partenaire:
        final VillageAgent thisAgent = this;
        addBehaviour(new TickerBehaviour(this, 2000) {
            @Override
            protected void onTick() {
                
                //Recherche via les pages jaunes de partenaire (DEPRECATED)
                /*if (descriptor.getEntity().inCouple())
                    return;
                // Demander aux pages jaunes la liste des partenaires potentiels:
                DFAgentDescription template = new DFAgentDescription();
                ServiceDescription sd = new ServiceDescription();
                if (descriptor.getEntity().sexeStatus == SexeStatus.FEMININ)
                    sd.setType("VillageAgent-Adult-Man");
                else sd.setType("VillageAgent-Adult-Woman");
                template.addServices(sd);
                try
                {
                    DFAgentDescription[] result = DFService.search(thisAgent, template);
                    for (DFAgentDescription dad: result){
                        AID partnerAID = dad.getName();
                        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                        msg.addReceiver(partnerAID);
                        msg.setProtocol("askPartner");
                        myAgent.send(msg);
                    }
                } catch (FIPAException fe) {
                    fe.printStackTrace();
                }*/

                //Recherche parmi les voisins géographiques un partenaire compatible
                try {
                    ArrayList<VillagerEntity> neighbours = descriptor.getNeighbours();
                    for (VillagerEntity ve : neighbours){
                        if (!descriptor.getEntity().inCouple() && descriptor.getEntity().matchWith(ve) && !ve.inCouple()){
                            if (ve._idAgent != null && descriptor.getEntity()._idAgent != null){
                                AID partnerAID =  ve._idAgent;
                                ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                                msg.addReceiver(partnerAID);
                                System.out.println("Je contacte romantiquement: " + partnerAID.getLocalName() + " (pour voir si ça le fait), " + "je suis : " + this.getAgent().getAID().getLocalName());
                                msg.setProtocol("askPartner");
                                msg.setContentObject(descriptor.getEntity());
                                myAgent.send(msg);
                            }
                        }
                    }
                } catch (IOException ex){
                    ex.printStackTrace();
                }
            }
        });
    }

    private void tickerMakeKids(){        
        addBehaviour(new TickerBehaviour(this, 10000) {
            @Override
            protected void onTick() {
                if (!descriptor.getEntity().inCouple() || descriptor.getEntity().conjoint==null || descriptor.getAge() > 45.0)
                    return;
                if (policy.getMaxChildrenPerCouple() <= childrenCount)
                    return;
                ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                msg.addReceiver(applicationAgentAID);
                msg.addReceiver(descriptor.getEntity().conjoint._idAgent);
                msg.setProtocol("spawnKid");
                myAgent.send(msg);
                childrenCount++;
            }
        });
    }

    private void tickerOther(){
        // Le comportement cyclique pour faire d'autres trucs (maisons, etc)
        addBehaviour(new TickerBehaviour(this, 5000) {
            @Override
            protected void onTick() {
                double r = Math.random();

                if (r < 0.2 && (policy.isMakeHouses() || (descriptor.getEntity().egoism > 42.0 && descriptor.getEntity().housePos == -1))){
                    /*if (villageDescriptor.spawnHouse())
                        System.out.println("[Nouvelle Maison]");
                    else 
                        System.out.println("[plus de place! impossible de construire une Maison]");*/
                    ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                    msg.addReceiver(applicationAgentAID);
                    msg.setProtocol("NeedNewHouse");
                    myAgent.send(msg);
                }
                if (r < 0.2 && (policy.isMakeFields() || descriptor.getEntity().ambition > 80.0)){// only a test
                    if (villageDescriptor.spawnField())
                        System.out.println("[Nouveau Champ]");
                    else 
                        System.out.println("[plus de place! impossible de construire un Champ]");
                }
            }
        });
    }

}
