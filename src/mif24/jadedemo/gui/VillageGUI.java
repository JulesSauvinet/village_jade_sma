package mif24.jadedemo.gui;
import jade.core.AID;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Map;
import javax.swing.*;

import mif24.jadedemo.agent.ApplicationAgent;
import mif24.jadedemo.model.descriptor.VillageAgentDescriptor;
import mif24.jadedemo.model.descriptor.VillageDescriptor;


/**
 *
 * @author Yann
 */
public class VillageGUI extends JFrame implements ActionListener {
    
    ApplicationAgent applicationAgent = null;
    VillagePanel villagePanel = null;
    SetupPanel setupPanel = null;
    VillageDescriptor villageDescriptor = null;

    public static int villageWidthGui = 750;
    public static int villageHeightGui = 750;

    public void updateGraphics(){
        villagePanel.repaint();
    }
            
    public VillageGUI(ApplicationAgent agent, VillageDescriptor _villageDescriptor){
        
        super("MIF24 - JADE Demo");

        villageDescriptor = _villageDescriptor;
        applicationAgent = agent;
        
        setDefaultLookAndFeelDecorated(true);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);        
        Dimension winsize = new Dimension(1024, 800);
        setMinimumSize(winsize);
        setPreferredSize(winsize);        
        setSize(winsize);
        setResizable(false);
        setLocationRelativeTo(null);

        JMenuBar menuBar = new JMenuBar();  
        JMenu menu = new JMenu("Options");

        JMenuItem itemExit = new JMenuItem("Quitter");
        itemExit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.ALT_MASK));
        itemExit.setActionCommand("Quitter");
        itemExit.addActionListener(this);
        menu.add(itemExit);
        menuBar.add(menu);

        setJMenuBar(menuBar);

        villagePanel = new VillagePanel(this, villageWidthGui, villageHeightGui);
        getContentPane().setBackground(Color.WHITE);
        getContentPane().setLayout(new BorderLayout());

        setupPanel = new SetupPanel(this, 270, villageHeightGui);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.X_AXIS));

        mainPanel.add(villagePanel);
        mainPanel.add(setupPanel);

        this.getContentPane().add(mainPanel);

        pack();
        setVisible(true);
    }

    public Map<AID, VillageAgentDescriptor> getAgentMap(){
        return applicationAgent.getAgentMap();
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("Quitter")){
            applicationAgent.takeDown();
        }
    }

    public void reset() {
        villageDescriptor.reset();
        villagePanel.repaint();
        setupPanel.reset();
    }
}
