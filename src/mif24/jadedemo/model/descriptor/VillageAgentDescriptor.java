package mif24.jadedemo.model.descriptor;

import jade.core.AID;
import mif24.jadedemo.agent.VillageAgent;
import mif24.jadedemo.model.entity.Entity;
import mif24.jadedemo.model.entity.VillagerEntity;
import mif24.jadedemo.model.status.AgeStatus;
import mif24.jadedemo.model.status.SexeStatus;
import mif24.jadedemo.util.AgentIcons;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * POJO - permet de passer l'état d'un agent au GUI.
 * @author Yann
 */
public class VillageAgentDescriptor implements Serializable {

    private final VillagerEntity villagerEntity;

    public String imgIcon = null;
    public boolean dead = false;

    public boolean chief = false; //chef ou pas
    
    public ArrayList<VillagerEntity> neighbours = new ArrayList<>();

    public VillageAgentDescriptor(int height, int width, AgeStatus ageStatus, String name){
        villagerEntity = new VillagerEntity(height, width, ageStatus, name);
        imgIcon = AgentIcons.getRandomIcon(ageStatus, villagerEntity.sexeStatus);
    }

    public void setPosition(int x, int y){
        villagerEntity.X = x;
        villagerEntity.Y = y;
    }
       
    public int getX(){ return villagerEntity.X; }
    public int getY(){ return villagerEntity.Y; }

    public int getHeight() {
        return villagerEntity.height;
    }

    public int getWidth() {
        return villagerEntity.width;
    }

    public void setPos(int pos) {
        villagerEntity.pos = pos;
        villagerEntity.X=pos%VillageDescriptor.villageWidthModel;
        villagerEntity.Y=pos/VillageDescriptor.villageHeightModel;
    }

    public int getPos() {
        return villagerEntity.pos;
    }


    public void updateAge() {
        setAge(getAge()+0.5);
        switch ((int) getAge()){
            case 16 :
                setAgeStatus(AgeStatus.ADULT);
                VillageDescriptor.nbVillageAgentsAdults++;
                VillageDescriptor.nbVillageAgentsChildren--;
                if (getSexeStatus() == SexeStatus.FEMININ){
                    imgIcon = AgentIcons.ICON_PATH + "woman/woman_" + imgIcon.split("_")[1];
                }
                else {
                    imgIcon = AgentIcons.ICON_PATH + "man/man_" + imgIcon.split("_")[1];
                }
            case 60 :
                setAgeStatus(AgeStatus.OLD);
                if (getSexeStatus() == SexeStatus.FEMININ){
                    imgIcon = AgentIcons.ICON_PATH + "old/old_woman.png";
                }
                else {
                    imgIcon = AgentIcons.ICON_PATH + "old/old_man.png";
                }
                break;
            default:
                break;
        }
    }

    public double getAge() {
        return villagerEntity.age;
    }

    public AgeStatus getAgeStatus() {
        return villagerEntity.ageStatus;
    }

    public void setAge(double v) {
        villagerEntity.age=v;
    }

    public SexeStatus getSexeStatus() {
        return  villagerEntity.sexeStatus;
    }

    public void setAgeStatus(AgeStatus ageStatus) {
        villagerEntity.ageStatus=ageStatus;
    }

    public VillagerEntity getEntity() {
        return villagerEntity;
    }
    public ArrayList<VillagerEntity> getNeighbours() {
        return neighbours;
    }

    public void setNeighbours(ArrayList<VillagerEntity> neighbours) {
        this.neighbours = neighbours;
    }

    public boolean isChief() {
        return chief;
    }

    public void setChief(boolean chief) {
        this.chief = chief;
    }    
}
