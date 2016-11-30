package mif24.jadedemo.model.entity;

import jade.core.AID;
import mif24.jadedemo.model.descriptor.EntityDescriptor;
import mif24.jadedemo.model.descriptor.VillageAgentDescriptor;
import mif24.jadedemo.model.status.AgeStatus;
import mif24.jadedemo.model.status.SexeStatus;

import java.awt.*;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Jules on 18/05/2016.
 */
public class VillagerEntity extends Entity implements Serializable {

    /* Caracteristiques personnelles -> pour l'accouplement et l'election d'un chef notamment, et la duree de vie*/
    public int beauty;
    public int intelligence;
    public int fun;
    public int money;
    public int ambition;
    public int egoism;
    public int confidence;

    public double age;
    public double happiness; // bonheur, entre 0.0 et 100.0

    public AgeStatus ageStatus;
    public SexeStatus sexeStatus;

    private boolean inCouple = false;

    public AID _idAgent = null;
    private String name;
    public VillagerEntity conjoint = null;
    public Color colorCouple = Color.pink;

    public int housePos = -1; // position de sa maison, -1 si aucune
    
    public VillagerEntity(){
        super();
    }

    public  VillagerEntity(int height, int width, AgeStatus ageStatus, String name){
        super(height, width, EntityDescriptor.PEOPLE);
        beauty = (int)(Math.random()*100);
        intelligence = (int)(Math.random()*100);
        fun = (int)(Math.random()*100);
        money = (int)(Math.random()*100);
        ambition = (int)(Math.random()*100);
        egoism = (int)(Math.random()*100);
        confidence = (int)(Math.random()*100);
        age = (int)(Math.random()*100);

        this.ageStatus = ageStatus;
        this.name = name;

        double sexeRdm = Math.random();
        if (sexeRdm >= 0.5){
            sexeStatus = SexeStatus.FEMININ;
        }
        else{
            sexeStatus = SexeStatus.MASCULIN;
        }

        switch (ageStatus){
            case CHILD:
                age = (int)(Math.random()*15);
                break;
            case ADULT:
                age = 15 + (int)(Math.random()*45);
                break;
            case OLD:
                age = 60 + (int)(Math.random()*40);
                break;
            default:
                break;
        }

    }

    public boolean matchWith(VillagerEntity ve) {
        if (this.ageStatus != ve.ageStatus) {
            return false;
        }
        if (this.sexeStatus == ve.sexeStatus) {
            return false;
        }
        double caractCorrespCoeff =   (Math.abs((double)(money-ve.money))+Math.abs((double)(intelligence-ve.intelligence))
                                    + Math.abs((double)(fun-ve.fun))    +Math.abs((double)(confidence-ve.confidence))
                                    + Math.abs((double)(egoism-ve.egoism)))/(500.);

        //System.out.println("CARACTCOEFF : " + caractCorrespCoeff);
        double matchCoeff = Math.random()*(1-caractCorrespCoeff);//*(money/100.0)*(intelligency/100.0)*(fun/100.0)*(confidence/100.0)*(1.0-(egoism/100.0));
        //System.out.println("MATCHCOEFF : " + matchCoeff);

        //TODO revoir le calcul
        if (matchCoeff > 0.5){
            System.out.println("Je suis tombé amoureux de " + ve.name);
            return true;
        }

        return false;
    }


    public boolean inCouple() {
        return inCouple;
    }

    public void toCouple() {
        inCouple = true;
    }

    public void toUnCouple() {
        inCouple = false;
    }

    public AgeStatus getAgeStatus() {
        return ageStatus;
    }

    public SexeStatus getSexeStatus() {
        return sexeStatus;
    }

    public void setIdAgent(AID _idAgent){
        this._idAgent = _idAgent;
    }
    
    public void decreaseHappiness(double val){
        happiness -= val;
        if (happiness < 0.0)
            happiness = 0.0;
    }
    
    public void increaseHappiness(double val){
        happiness += val;
        if (happiness > 100.0)
            happiness = 100.0;
    }
}
