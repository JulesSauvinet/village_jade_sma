package mif24.jadedemo.gui;

import mif24.jadedemo.model.descriptor.VillageDescriptor;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;

/**
 * Created by Jules on 14/05/2016.
 */
public class SetupPanel extends JPanel implements ActionListener{
    VillageGUI villageGUI = null;
    VillageDescriptor villageDescriptor = null;
    int width, height;
    ArrayList<JLabel> labels = new ArrayList<>();
    JSlider childrenSlider = null;
    JSlider adultsSlider = null;
    JSlider waterSlider = null;
    JSlider campSlider = null;
    JSlider treeSlider = null;
    JSlider baiesSlider = null;
    private boolean start =false;

    public SetupPanel(VillageGUI gui, int _width, int _height){
        super();
        this.villageGUI = gui;
        this.villageDescriptor = gui.villageDescriptor;
        this.width = _width;
        this.height = _height;
        this.setPreferredSize(new Dimension(width, height));
        this.setBackground(new Color(223, 242, 255));
        this.repaint();

        JPanel settingsPanel = new JPanel();
        settingsPanel.setBackground(new Color(223, 242, 255));
        settingsPanel.setPreferredSize(new Dimension(220, 740));
        settingsPanel.setLayout(new BoxLayout(settingsPanel, BoxLayout.Y_AXIS));

        JButton setupButton = new JButton("start");
        setupButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                start = true; villageGUI.applicationAgent.start();
            }
        });

        JButton resetButton = new JButton("reset");
        resetButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                start = false; villageGUI.applicationAgent.reset();
            }
        });

        JButton updateButton = new JButton("update");
        updateButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                update();
            }
        });

        JLabel label1 = new JLabel("Enfants");
        JLabel label2 = new JLabel("Adultes");
        JLabel label3 = new JLabel("Chefs");
        JLabel label4 = new JLabel("Eau");
        JLabel label5 = new JLabel("Champs");
        JLabel label6 = new JLabel("Arbres");
        JLabel label7 = new JLabel("Baies");

        labels.add(label1);labels.add(label2);labels.add(label3);labels.add(label4);labels.add(label5);labels.add(label6);labels.add(label7);

        final JLabel label0 = new JLabel("Taille icônes:");
        JSlider sizeIconSlider = new JSlider(JSlider.HORIZONTAL,
                15, 30, 20);
        sizeIconSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if (!start){
                    label0.setText(label0.getText().split(":")[0] + ": " + ((JSlider)e.getSource()).getValue());
                    villageGUI.villageDescriptor.updateSizeEntity(((JSlider)e.getSource()).getValue());
                    villageGUI.updateGraphics();
                }
            }
        });
        sizeIconSlider.setMajorTickSpacing(5);
        sizeIconSlider.setMinorTickSpacing(1);
        sizeIconSlider.setPaintTicks(true);
        sizeIconSlider.setPaintLabels(true);
        sizeIconSlider.setPreferredSize(new Dimension(200,50));

        childrenSlider = new JSlider(JSlider.HORIZONTAL,
                1, villageDescriptor.nbVillageAgentsChildrenMax, villageDescriptor.nbVillageAgentsChildren);
        configureSlider(childrenSlider, label1, (int)Math.floor(villageDescriptor.nbVillageAgentsChildrenMax/20), (int)Math.floor(villageDescriptor.nbVillageAgentsChildrenMax/5));

        adultsSlider = new JSlider(JSlider.HORIZONTAL,
                1, villageDescriptor.nbVillageAgentsAdultsMax, villageDescriptor.nbVillageAgentsAdults);
        configureSlider(adultsSlider, label2,  (int)Math.floor(villageDescriptor.nbVillageAgentsAdultsMax/20), (int)Math.floor(villageDescriptor.nbVillageAgentsAdultsMax/5));

        waterSlider = new JSlider(JSlider.HORIZONTAL,
                1, villageDescriptor.nbVillageQuantiteDeauMax, villageDescriptor.nbVillageQuantiteDeau);
        configureSlider(waterSlider,label4,(int)Math.floor(villageDescriptor.nbVillageQuantiteDeauMax/20), (int)Math.floor(villageDescriptor.nbVillageQuantiteDeauMax/5));

        campSlider = new JSlider(JSlider.HORIZONTAL,
                1,  villageDescriptor.nbVillageChampsMax, villageDescriptor.nbVillageChamps);
        configureSlider(campSlider, label5,(int)Math.floor(villageDescriptor.nbVillageChampsMax/20), (int)Math.floor(villageDescriptor.nbVillageChampsMax/5));

        treeSlider = new JSlider(JSlider.HORIZONTAL,
                1, villageDescriptor.nbVillageArbresMax, villageDescriptor.nbVillageArbres);
        configureSlider(treeSlider,label6, (int)Math.floor(villageDescriptor.nbVillageArbresMax/20), (int)Math.floor(villageDescriptor.nbVillageArbresMax/5));

        baiesSlider = new JSlider(JSlider.HORIZONTAL,
                1, villageDescriptor.nbVillageBaiesSauvagesMax, villageDescriptor.nbVillageBaiesSauvages);
        configureSlider(baiesSlider, label7, (int)Math.floor(villageDescriptor.nbVillageBaiesSauvagesMax/20), (int)Math.floor(villageDescriptor.nbVillageBaiesSauvagesMax/5));

        settingsPanel.add(Box.createRigidArea(new Dimension(0,10)));
        settingsPanel.add(Box.createRigidArea(new Dimension(80,0)));
        settingsPanel.add(setupButton);
        settingsPanel.add(Box.createRigidArea(new Dimension(0,20)));

        settingsPanel.add(label0);
        settingsPanel.add(sizeIconSlider);
        settingsPanel.add(Box.createRigidArea(new Dimension(0,20)));

        settingsPanel.add(label1);
        settingsPanel.add(childrenSlider);
        settingsPanel.add(Box.createRigidArea(new Dimension(0,20)));

        settingsPanel.add(label2);
        settingsPanel.add(adultsSlider);
        settingsPanel.add(Box.createRigidArea(new Dimension(0,20)));

        settingsPanel.add(label4);
        settingsPanel.add(waterSlider);
        settingsPanel.add(Box.createRigidArea(new Dimension(0,20)));

        settingsPanel.add(label5);
        settingsPanel.add(campSlider);
        settingsPanel.add(Box.createRigidArea(new Dimension(0,20)));

        settingsPanel.add(label6);
        settingsPanel.add(treeSlider);
        settingsPanel.add(Box.createRigidArea(new Dimension(0,20)));

        settingsPanel.add(label7);
        settingsPanel.add(baiesSlider);

        settingsPanel.add(Box.createRigidArea(new Dimension(0,25)));
        settingsPanel.add(Box.createRigidArea(new Dimension(80,0)));
        settingsPanel.add(updateButton);

        settingsPanel.add(Box.createRigidArea(new Dimension(0,15)));
        settingsPanel.add(Box.createRigidArea(new Dimension(80,0)));
        settingsPanel.add(resetButton);

        this.add(settingsPanel);
        //+maison
    }

    private void configureSlider(JSlider slider, final JLabel label, int minTickSpacing, int maxTickSpacing) {
        label.setText(label.getText().split(":")[0] + ": " + slider.getValue());
        slider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                label.setText(label.getText().split(":")[0] + ": " + ((JSlider)e.getSource()).getValue());
                villageGUI.villageDescriptor.updateAttribute(label.getText().split(":")[0], ((JSlider)e.getSource()).getValue());
            }
        });
        slider.setMajorTickSpacing(maxTickSpacing);
        slider.setMinorTickSpacing(minTickSpacing);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        slider.setPreferredSize(new Dimension(200,50));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
        g2.setColor(new Color(0, 0, 0));
        g2.setStroke(new java.awt.BasicStroke(2)); // thickness of 3.0f
        g2.drawRect(1, 1, width - 4,height - 4);
    }

    @Override
    public void actionPerformed(ActionEvent e) {

    }

    public void reset() {
        updateSliders();
    }

    public void update() {
        if (!start){
            updateDescriptor();
        }
    }

    private void updateDescriptor() {
        villageDescriptor.nbVillageAgentsChildren = childrenSlider.getValue();
        villageDescriptor.nbVillageAgentsAdults= adultsSlider.getValue();
        villageDescriptor.nbVillageQuantiteDeau= waterSlider.getValue();
        villageDescriptor.nbVillageArbres= treeSlider.getValue();
        villageDescriptor.nbVillageChamps = campSlider.getValue();
        villageDescriptor.nbVillageBaiesSauvages=baiesSlider.getValue();

        villageDescriptor.updateGrid();
        villageGUI.updateGraphics();
    }

    private void updateSliders() {
        childrenSlider.setValue(villageDescriptor.nbVillageAgentsChildren);
        adultsSlider.setValue(villageDescriptor.nbVillageAgentsAdults);
        waterSlider.setValue(villageDescriptor.nbVillageQuantiteDeau);
        treeSlider.setValue(villageDescriptor.nbVillageArbres);
        campSlider.setValue(villageDescriptor.nbVillageChamps);
        baiesSlider.setValue(villageDescriptor.nbVillageBaiesSauvages);
    }
}
