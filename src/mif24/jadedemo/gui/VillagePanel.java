/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mif24.jadedemo.gui;

import jade.core.AID;

import java.awt.*;
import java.util.Map;
import javax.swing.*;

import mif24.jadedemo.model.descriptor.EntityDescriptor;
import mif24.jadedemo.model.descriptor.VillageAgentDescriptor;
import mif24.jadedemo.model.descriptor.VillageDescriptor;
import mif24.jadedemo.util.EnvironmentIcons;

/**
 *
 * @author Yann
 */
public class VillagePanel extends JPanel {
    
    VillageGUI villageGUI = null;
    VillageDescriptor villageDescriptor = null;
    int villageWidth, villageHeight;
    final static String IMAGE_PATH = "resources/images/";
    
    public VillagePanel(VillageGUI gui, int width, int height){
        super();
        this.villageGUI = gui;
        this.villageDescriptor=villageGUI.villageDescriptor;
        this.villageWidth = width;
        this.villageHeight = height;
        this.setPreferredSize(new Dimension(width, height));
//        this.setSize(new Dimension(width, height));
        this.setBackground(Color.WHITE);
    }

    @Override
    protected synchronized void paintComponent(Graphics g){
        super.paintComponent(g);
        java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();

        g2.setColor(new Color(255, 255, 255));
        g2.fillRect(0, 0, villageHeight-2, villageWidth-2);
        g2.setColor(new Color(0, 0, 0));
        g2.setStroke(new java.awt.BasicStroke(2)); // thickness of 3.0f
        g2.drawRect(1, 1, villageHeight-4, villageWidth-4);


        Map<AID, VillageAgentDescriptor> map = villageGUI.getAgentMap();
        for (AID aid : map.keySet()) {
            VillageAgentDescriptor agent = map.get(aid);
            Image img = Toolkit.getDefaultToolkit().getImage(agent.imgIcon);

            if (agent.getPos() != -1) {
                g2.drawImage(img, agent.getX() * agent.getWidth() + 2, agent.getY() * agent.getHeight() + 2, agent.getWidth(), agent.getHeight(), this);

                if (agent.getEntity().inCouple()){
                    drawHeart(g2, agent.getX() * agent.getWidth() + 2, agent.getY() * agent.getHeight() + 2, (int) (agent.getWidth()*(4./5.)), (int) (agent.getHeight()*(4./5.)), agent.getEntity().colorCouple);
                }
            }

        }

        boolean drawWell = false;
        for (int i=0; i<villageDescriptor.gridVillage.length; i++){
            for (int j=0; j<villageDescriptor.gridVillage.length; j++) {
                String icon_path = EnvironmentIcons.getIcon(villageDescriptor.gridVillage[i][j].entityDescriptor);
                if (icon_path != null) {
                    Image img = Toolkit.getDefaultToolkit().getImage(icon_path);
                    g2.drawImage(img, i * villageDescriptor.entityWidth + 2, j *villageDescriptor.entityHeight  + 2, villageDescriptor.entityWidth, villageDescriptor.entityHeight, this);
                }
                else if (villageDescriptor.gridVillage[i][j].entityDescriptor == EntityDescriptor.WATER & !drawWell){
                    drawWell = true;
                    /* On nuance le bleu pour la quantité d'eau */
                    int waterAmount = villageDescriptor.nbVillageQuantiteDeau;
                    g2.setColor(new Color(0,255-(waterAmount+20)/4,255));
                    if (waterAmount == 0){
                        g2.setColor(new Color(255,255,255));
                    }
                    g2.fillOval(i * villageDescriptor.entityWidth + 2, j *villageDescriptor.entityHeight  + 2, villageDescriptor.entityWidth*2, villageDescriptor.entityHeight*2);
                    g2.setStroke(new java.awt.BasicStroke(3)); // thickness of 3.0f
                    g2.setColor(new Color(130,130,130));
                    g2.drawOval(i * villageDescriptor.entityWidth + 2, j *villageDescriptor.entityHeight  + 2, villageDescriptor.entityWidth*2, villageDescriptor.entityHeight*2);
                }
                else if (villageDescriptor.gridVillage[i][j].entityDescriptor == EntityDescriptor.NONE & villageDescriptor.isRain){
                    g2.setColor(new Color(200,200,200));
                    g2.setStroke(new java.awt.BasicStroke(1)); // thickness of 3.0f

                    int rainLength = villageDescriptor.entityHeight/5;
                    int rainGap = villageDescriptor.entityWidth/5;
                    drawLineRain(g2, i, j, 2, 2, 2, 2+rainLength);
                    drawLineRain(g2, i, j, 2+rainGap, 2+rainGap, 2, 2+rainLength);
                    drawLineRain(g2, i, j, 2+2*rainGap, 2+2*rainGap, 2, 2+rainLength);
                    drawLineRain(g2, i, j, 2+3*rainGap, 2+3*rainGap, 2, 2+rainLength);

                    drawLineRain(g2, i, j, 3, 3, 4+rainGap, 4+rainGap+rainLength);
                    drawLineRain(g2, i, j, 3+rainGap, 3+rainGap, 4+rainGap, 4+rainGap+rainLength);
                    drawLineRain(g2, i, j, 3+2*rainGap, 3+2*rainGap, 4+ rainGap, 4+rainGap+rainLength);
                    drawLineRain(g2, i, j, 3+3*rainGap, 3+3*rainGap, 4+ rainGap, 4+rainGap+rainLength);

                    drawLineRain(g2, i, j, 2, 2, 4+2*rainGap,4+2*rainGap+rainLength);
                    drawLineRain(g2, i, j, 2+rainGap, 2+rainGap, 4+2*rainGap, 4+2*rainGap+rainLength);
                    drawLineRain(g2, i, j, 2+2*rainGap, 2+2*rainGap, 4+2*rainGap, 4+2*rainGap+rainLength);
                    drawLineRain(g2, i, j, 2+3*rainGap, 2+3*rainGap, 4+2*rainGap, 4+2*rainGap+rainLength);
                }

                if (villageDescriptor.isOnFire){
                    icon_path = EnvironmentIcons.getIcon(EntityDescriptor.FIRE);
                    Image img = Toolkit.getDefaultToolkit().getImage(icon_path);
                    g2.drawImage(img, i * villageDescriptor.entityWidth + 2, j *villageDescriptor.entityHeight  + 2, villageDescriptor.entityWidth, villageDescriptor.entityHeight, this);
                }
            }
        }
        g2.finalize();
    }

    public void drawLineRain(Graphics2D g2, int i, int j, int x1, int x2, int y1, int y2){
        g2.drawLine(i * villageDescriptor.entityWidth + x1, j *villageDescriptor.entityHeight  + y1, i * villageDescriptor.entityWidth + x2, j *villageDescriptor.entityHeight  + y2);
    }

    public void drawHeart(Graphics2D g2, int x, int y, int width, int height, Color color){

        int[] xp = { x, x + width, x + width/2 };
        int[] yp = { y + width/3, y + width/3, y + (int)(width*(3./4.))};

        g2.setColor(color);

        g2.fillOval(x, y, width/2, width/2); //left circle
        g2.fillOval(x + width/2, y, width/2, width/2); //to cover middle spot
        g2.fillOval(x + width/3, y + width/4, width/4, width/4); //right circle
        g2.fillPolygon(xp, yp, xp.length);
    }


}
