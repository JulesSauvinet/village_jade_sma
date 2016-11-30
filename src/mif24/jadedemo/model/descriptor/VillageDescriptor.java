package mif24.jadedemo.model.descriptor;

import jade.core.AID;
import mif24.jadedemo.model.entity.Entity;
import mif24.jadedemo.model.entity.VillagerEntity;
import mif24.jadedemo.model.status.AgeStatus;
import mif24.jadedemo.model.status.SexeStatus;
import mif24.jadedemo.gui.VillageGUI;
import mif24.jadedemo.util.AgentIcons;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Jules on 14/05/2016.
 */
public class VillageDescriptor implements Serializable{
    public static int nbVillageAgentsChildren =30;
    public static int nbVillageAgentsAdults =60;
    public static int nbVillageChamps =50;
    public static int nbVillageArbres = 30;
    public static int nbVillageQuantiteDeau =500;
    public static int nbVillageQuantiteNourriture = 500;
    public static int nbVillageBaiesSauvages =10;
    public static int nbVillageMaisons = nbVillageAgentsAdults/2;

    public static int nbVillageAgentsChildrenMax = 4*nbVillageAgentsChildren;
    public static int nbVillageAgentsAdultsMax = 4*nbVillageAgentsAdults;
    public static int nbVillageChampsMax = 2*nbVillageChamps;
    public static int nbVillageArbresMax = 2*nbVillageArbres;
    public static int nbVillageBaiesSauvagesMax = 2*nbVillageBaiesSauvages;
    public static int nbVillageQuantiteDeauMax = 2*nbVillageQuantiteDeau;


    public static int villageWidthModel = 37;
    public static int villageHeightModel =37;

    public static int entityWidth  =(VillageGUI.villageWidthGui-10)/ VillageDescriptor.villageWidthModel;
    public static int entityHeight =(VillageGUI.villageHeightGui-10)/ VillageDescriptor.villageHeightModel;

    public static int nbVillageois = nbVillageAgentsChildren+nbVillageAgentsAdults;

    public Entity[][] gridVillage = new Entity[villageWidthModel][villageHeightModel];

    public int hapinness = 0; //Le bonheur du village

    ///////////////////////////////////////////////////
    public Set<Integer> posAvailables = new HashSet<>();
    public Set<Integer> houseAvailables = new HashSet<>();
    public boolean isRain = false;
    public boolean isOnFire = false;

    public VillageDescriptor(){
        fillGrid();
    }

    public void reset() {
        isOnFire = false;
        nbVillageAgentsChildren=30;
        nbVillageAgentsAdults = 60;
        nbVillageMaisons = 20;
        nbVillageChamps =50;
        nbVillageArbres=30;
        nbVillageQuantiteDeau =500;
        nbVillageQuantiteNourriture = 500;
        nbVillageBaiesSauvages =10;
        villageWidthModel = 37;
        villageHeightModel =37;
        entityWidth  =(VillageGUI.villageWidthGui-10)/ VillageDescriptor.villageWidthModel;
        entityHeight =(VillageGUI.villageHeightGui-10)/ VillageDescriptor.villageHeightModel;
        fillGrid();

    }

    private void fillGrid() {
        for (int i=0; i<villageWidthModel; i++){
            for (int j=0; j<villageHeightModel; j++){
                gridVillage[i][j] = new Entity(i,j,EntityDescriptor.NONE);
            }
        }
        posAvailables.clear();

        for (int i =0; i<(villageHeightModel*villageWidthModel); i++){
            posAvailables.add(i);
        }

        int x =0;
        int y =0;
        /* On créé le puit au milieu de la grille en supposant que la grille est suffisamment grande! */
        if (VillageDescriptor.villageWidthModel % 2 == 0){
            x = VillageDescriptor.villageWidthModel/2;
        }
        else {
            x = (VillageDescriptor.villageWidthModel-1)/2;
        }
        if (VillageDescriptor.villageHeightModel % 2 == 0){
            y = VillageDescriptor.villageHeightModel/2;
        }
        else {
            y = (VillageDescriptor.villageHeightModel-1)/2;
        }

        gridVillage[x][y] = new Entity(x,y,EntityDescriptor.WATER);
        int posIdx = y*VillageDescriptor.villageHeightModel+x;
        posAvailables.remove(posIdx);
        gridVillage[x+1][y] = new Entity(x+1, y, EntityDescriptor.WATER);
        posIdx = y*VillageDescriptor.villageHeightModel+x+1;
        posAvailables.remove(posIdx);
        gridVillage[x][y+1] = new Entity(x, y+1, EntityDescriptor.WATER);
        posIdx = (y+1)*VillageDescriptor.villageHeightModel+x;
        posAvailables.remove(posIdx);
        gridVillage[x+1][y+1] = new Entity(x+1, y+1, EntityDescriptor.WATER);
        posIdx = (y+1)*VillageDescriptor.villageHeightModel+x+1;
        posAvailables.remove(posIdx);

        houseAvailables.clear();
        for (int i=0; i<nbVillageAgentsAdults/2; i++){
            setEntityToGrid(EntityDescriptor.HOUSE);
        }
        for (int i=0; i<nbVillageBaiesSauvages; i++){
            setEntityToGrid(EntityDescriptor.BERRY);
        }
        for (int i=0; i<nbVillageArbres; i++){
            setEntityToGrid(EntityDescriptor.TREE);
        }

        for (int i=0; i<nbVillageChamps; i++){
            setEntityToGrid(EntityDescriptor.CAMP);
        }
    }

    /// renvoie la position de la maison, ou -1 si pas de place dispo ou plus d'arbres à couper
    synchronized public int getAvailableHouse(){
        
        int pos = getRandomHousePos();// d'abord on regarde parmi les maisons disponibles...
        
        if (pos == -1){// toutes les maisons sont prises:
            if (!cutTree())
                return -1;//plus d'arbres!
            else {// sinon, on a coupé un arbre pour faire une nouvelle maison..
                System.out.println("Village : on a coupé un arbre pour faire une nouvelle maison.");
                if (setEntityToGrid(EntityDescriptor.HOUSE)){
                    nbVillageMaisons++;      
                    return getRandomHousePos();
                }
            }
        }         
        return pos;
    }

    synchronized public boolean spawnTree(){
        if (setEntityToGrid(EntityDescriptor.TREE)){
            nbVillageArbres++;
            return true;
        }
        return false;
    }

    public boolean cutTree(){  
        synchronized(gridVillage){
            for (int i=0; i<villageWidthModel; i++){// todo : faire du random ici plutôt
                for (int j=0; j<villageHeightModel; j++){
                    if (gridVillage[i][j].entityDescriptor == EntityDescriptor.TREE){
                        gridVillage[i][j] =  new Entity(i,j,EntityDescriptor.NONE);
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    synchronized public boolean spawnBerry(){
        if (setEntityToGrid(EntityDescriptor.BERRY)){
            nbVillageBaiesSauvages++;
            return true;
        }
        return false;
    }
    
    synchronized public boolean spawnField(){
        if (setEntityToGrid(EntityDescriptor.CAMP)){
            nbVillageChamps++;
            return true;
        }
        return false;
    }


    public void updateAttribute(String s, int source) {
        switch(s){
            case "Enfants":
                nbVillageAgentsChildren = source;
                break;
            case "Adultes":
                nbVillageAgentsAdults = source;
                break;
            case "Champs":
                nbVillageChamps = source;
                break;
            case "Nourriture":
                nbVillageQuantiteNourriture = source;
                break;
            case "Arbres":
                nbVillageArbres = source;
                break;
            case "Maisons":
                nbVillageMaisons = source;
                break;
            case "Eau":
                nbVillageQuantiteDeau = source;
                break;
            case "Baies":
                nbVillageBaiesSauvages = source;
                break;
            default:
                break;
        }
    }

    //ON CENTRALISE LES METHODES POUR L'INSTANT
    public void updatePosition(VillageAgentDescriptor descriptor) {
        synchronized (posAvailables) {
            int oldPos = descriptor.getPos();
            int newPos = getRandomPos(oldPos);

            if (oldPos != -1){
                posAvailables.add(oldPos);
            }
            posAvailables.remove(newPos);
            if (descriptor.getX() != -1 && descriptor.getY() != -1){
                gridVillage[descriptor.getX()][descriptor.getY()] = new Entity(descriptor.getX(), descriptor.getY(), EntityDescriptor.NONE);
            }
            descriptor.setPos(newPos);
            gridVillage[descriptor.getX()][descriptor.getY()] = descriptor.getEntity();

        }
    }

    public void update(){
        
        updateVegetation();
        updateWater();
        updateFood();
    }
    
    public void updateVegetation(){
        
        double r = Math.random() * 100.0;
        if (r > 85.0)
            spawnTree();
        r = Math.random() * 100.0;
        if (r > 70.0)
            spawnBerry();
    }
    
    public void updateWater() {

        nbVillageQuantiteDeau -= (nbVillageAgentsAdults/6)+(nbVillageAgentsChildren/12);

        isRain=false;
        double rain = Math.random();
        if (rain > 0.75){
            isRain = true;
            nbVillageQuantiteDeau+= 100 + (int)Math.floor(Math.random()*50.0);
        }

        if (nbVillageQuantiteDeau >1000){
            nbVillageQuantiteDeau=1000;
        }

        if (nbVillageQuantiteDeau <0){
            nbVillageQuantiteDeau = 0;
        }

        if (nbVillageQuantiteDeau == 0) {
            isOnFire = true;
        }
    }

    public void updateFood(){

        nbVillageQuantiteNourriture -= (nbVillageAgentsAdults/2)+(nbVillageAgentsChildren/4);

        int recolte = (int)(Math.random()*3.0*(double)nbVillageChamps) + 2*nbVillageBaiesSauvages;
        nbVillageQuantiteNourriture += recolte;
        if (nbVillageQuantiteNourriture > 1000)
            nbVillageQuantiteNourriture = 1000;
        if (nbVillageQuantiteNourriture < 0)
            nbVillageQuantiteNourriture = 0;
    }

    /// renvoie -1 si pas de place disponible
    public boolean setEntityToGrid(EntityDescriptor entityDescriptor) {
        synchronized (posAvailables) {
            int pos = getRandomPos();
            if (pos == -1)
                return false;
            int x = pos % VillageDescriptor.villageWidthModel;
            int y = pos / VillageDescriptor.villageHeightModel;

            gridVillage[x][y] = new Entity(x, y, entityDescriptor);
            posAvailables.remove(pos);
            
            if (entityDescriptor == EntityDescriptor.HOUSE)
                houseAvailables.add(pos);
            return true;
        }
    }

    private int getRandomHousePos(){
        if (houseAvailables.size() == 0)
            return -1;
        int posIdx = (((int) Math.floor(Math.random() * houseAvailables.size()))+1) % houseAvailables.size();
        int cpt = 0;
        int pos = -1;
        for (Integer i : houseAvailables){
            if (cpt == posIdx){
                pos = i;
            }
            cpt++;
        }
        return pos;
    }
    
    private int getRandomPos() {
        if (posAvailables.size() == 0)
            return -1;
        int posIdx = (((int) Math.floor(Math.random() * posAvailables.size()))+1) % posAvailables.size();
        int cpt = 0;
        int pos = -1;
        for (Integer i : posAvailables){
            if (cpt == posIdx){
                pos = i;
            }
            cpt++;
        }
        return pos;
    }

    
    private int getRandomPos(int oldPos) {
        int pos = -1;

        if (oldPos == -1){
            return getRandomPos();
        }
        else {
            List<Integer> posPossible = new ArrayList<>();
            posPossible.add(oldPos + 1);
            posPossible.add(oldPos - 1);
            posPossible.add(oldPos + VillageDescriptor.villageHeightModel);
            posPossible.add(oldPos - VillageDescriptor.villageHeightModel);
            posPossible.add(oldPos - VillageDescriptor.villageHeightModel - 1);
            posPossible.add(oldPos - VillageDescriptor.villageHeightModel + 1);
            posPossible.add(oldPos + VillageDescriptor.villageHeightModel - 1);
            posPossible.add(oldPos + VillageDescriptor.villageHeightModel + 1);
            java.util.Collections.shuffle(posPossible);

            for (Integer i : posPossible) {
                if (posAvailables.contains(i)) {
                    pos = i;
                }
            }
            if (pos == -1) {
//                System.out.println("on garde la meme pos...");
                pos = oldPos;
            }
        }

        return pos;
    }

    public void updateGrid() {
        fillGrid();
    }



    public void updateSizeEntity(int size) {
        entityWidth=size;
        entityHeight=size;

        villageWidthModel = (VillageGUI.villageWidthGui-10)/ entityWidth;
        villageHeightModel =(VillageGUI.villageHeightGui-10)/ entityHeight;

        gridVillage = new Entity[villageWidthModel][villageHeightModel];
        fillGrid();
    }


    public void updateNeighbours(VillageAgentDescriptor descriptor) {
        descriptor.neighbours.clear();
        for (int i = 0; i<gridVillage.length; i++){
            for (int j = 0; j<gridVillage[i].length; j++){
                if (gridVillage[i][j].entityDescriptor == EntityDescriptor.PEOPLE){
                    if (Math.abs(gridVillage[i][j].pos-descriptor.getPos()) <=2 && Math.abs(gridVillage[i][j].pos-descriptor.getPos()) != 0){
                        descriptor.neighbours.add((VillagerEntity) gridVillage[i][j]);
                    }
                }
            }
        }
    }

    public void updateIdAgent(VillageAgentDescriptor descriptor, AID senderAID) {
        if (descriptor.getEntity()._idAgent == null) {
            descriptor.getEntity()._idAgent = senderAID;
        }
    }
}
