/*
 * MegaMekLab
 * Copyright (c) 2019-2022 - The MegaMek Team. All Rights Reserved.
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 */
package megameklab.ui;

import megamek.client.ui.swing.UnitLoadingDialog;
import megamek.client.ui.swing.util.UIUtil;
import megamek.client.ui.swing.widget.MegamekButton;
import megamek.client.ui.swing.widget.SkinSpecification;
import megamek.client.ui.swing.widget.SkinSpecification.UIComponents;
import megamek.client.ui.swing.widget.SkinXMLHandler;
import megamek.common.*;
import megamek.common.util.EncodeControl;
import megamek.common.util.ImageUtil;
import megamek.common.util.fileUtils.MegaMekFile;
import megameklab.MMLConstants;
import megameklab.ui.dialog.LoadingDialog;
import megameklab.ui.dialog.MegaMekLabUnitSelectorDialog;
import megameklab.util.UnitUtil;
import org.apache.logging.log4j.LogManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ResourceBundle;
import java.util.TreeMap;

/**
 * A startup splash screen for MegaMekLab
 * @author Taharqa
 */
public class StartupGUI extends JPanel {
    JFrame frame;
    Image imgSplash;
    BufferedImage backgroundIcon;
    
    /** A map of resolution widths to file names for the startup screen */
    private final TreeMap<Integer, String> startupScreenImages = new TreeMap<>();
    {
        startupScreenImages.put(0, "data/images/misc/mml_start_spooky_hd.jpg"); // TODO : Remove inline filename
        startupScreenImages.put(1441, "data/images/misc/mml_start_spooky_fhd.jpg"); // TODO : Remove inline filename
        startupScreenImages.put(1921, "data/images/misc/mml_start_spooky_uhd.jpg"); // TODO : Remove inline filename
    }
    
    private final ResourceBundle resourceMap = ResourceBundle.getBundle("megameklab.resources.Splash", new EncodeControl());
    
    public StartupGUI() {       
        initComponents();
    }

    private void initComponents() {
        SkinSpecification skinSpec = SkinXMLHandler.getSkin(UIComponents.MainMenuBorder.getComp(), true);
        
        frame = new JFrame("MegaMekLab");
        setBackground(UIManager.getColor("controlHighlight"));

        Dimension scaledMonitorSize = UIUtil.getScaledScreenSize(frame);
        JLabel splash = UIUtil.createSplashComponent(startupScreenImages, frame);
        add(splash, BorderLayout.CENTER);
        
        if (skinSpec.hasBackgrounds()) {
            if (skinSpec.backgrounds.size() > 1) {
                File file = new MegaMekFile(Configuration.widgetsDir(),
                        skinSpec.backgrounds.get(1)).getFile();
                if (!file.exists()) {
                    LogManager.getLogger().error("Background icon doesn't exist: " + file.getAbsolutePath());
                } else {
                    backgroundIcon = (BufferedImage) ImageUtil.loadImageFromFile(file.toString());
                }
            }
        } else {
            backgroundIcon = null;
        }
        
        JLabel labVersion = new JLabel(resourceMap.getString("version.text") + MMLConstants.VERSION, JLabel.CENTER);
        labVersion.setPreferredSize(new Dimension(250,15));
        if (!skinSpec.fontColors.isEmpty()) {
            labVersion.setForeground(skinSpec.fontColors.get(0));
        }

        MegamekButton btnLoadUnit = new MegamekButton(resourceMap.getString("btnLoadUnit.text"),
                UIComponents.MainMenuButton.getComp(), true);
        btnLoadUnit.addActionListener(evt -> loadUnit());
        
        MegamekButton btnNewMek = new MegamekButton(resourceMap.getString("btnNewMek.text"),
                UIComponents.MainMenuButton.getComp(), true);
        btnNewMek.addActionListener(evt -> newUnit(Entity.ETYPE_MECH));
        
        MegamekButton btnNewVee = new MegamekButton(resourceMap.getString("btnNewVee.text"),
                UIComponents.MainMenuButton.getComp(), true);
        btnNewVee.addActionListener(evt -> newUnit(Entity.ETYPE_TANK));
        
        MegamekButton btnNewSupportVee = new MegamekButton(resourceMap.getString("btnNewSupportVee.text"),
                UIComponents.MainMenuButton.getComp(), true);
        btnNewSupportVee.addActionListener(evt -> newUnit(Entity.ETYPE_SUPPORT_TANK));
        
        MegamekButton btnNewBA = new MegamekButton(resourceMap.getString("btnNewBA.text"),
                UIComponents.MainMenuButton.getComp(), true);
        btnNewBA.addActionListener(evt -> newUnit(Entity.ETYPE_BATTLEARMOR));
        
        MegamekButton btnNewAero = new MegamekButton(resourceMap.getString("btnNewAero.text"),
                UIComponents.MainMenuButton.getComp(), true);
        btnNewAero.addActionListener(evt -> newUnit(Entity.ETYPE_AERO));

        MegamekButton btnNewDropper = new MegamekButton(resourceMap.getString("btnNewDropper.text"),
                UIComponents.MainMenuButton.getComp(), true);
        btnNewDropper.addActionListener(evt -> newUnit(Entity.ETYPE_DROPSHIP));
        
        MegamekButton btnNewLargeCraft = new MegamekButton(resourceMap.getString("btnNewLargeCraft.text"),
                UIComponents.MainMenuButton.getComp(), true);
        btnNewLargeCraft.addActionListener(evt -> newUnit(Entity.ETYPE_JUMPSHIP));
        
        MegamekButton btnNewProto = new MegamekButton(resourceMap.getString("btnNewProto.text"),
                UIComponents.MainMenuButton.getComp(), true);
        btnNewProto.addActionListener(evt -> newUnit(Entity.ETYPE_PROTOMECH));
        
        MegamekButton btnNewPbi = new MegamekButton(resourceMap.getString("btnNewPbi.text"),
                UIComponents.MainMenuButton.getComp(), true);
        btnNewPbi.addActionListener(evt -> newUnit(Entity.ETYPE_INFANTRY));
        
        MegamekButton btnQuit = new MegamekButton(resourceMap.getString("btnQuit.text"),
                UIComponents.MainMenuButton.getComp(), true);
        btnQuit.addActionListener(evt -> System.exit(0));

        FontMetrics metrics = btnNewDropper.getFontMetrics(btnNewDropper.getFont());
        int width = metrics.stringWidth(btnNewDropper.getText());
        int height = metrics.getHeight();
        Dimension textDim = new Dimension(width + 50, height + 10);

        // Strive for no more than ~90% of the screen and use golden ratio to make
        // the button width "look" reasonable.
        int maximumWidth = (int) (0.9 * scaledMonitorSize.width) - splash.getPreferredSize().width;

        //no more than 50% of image width
        if (maximumWidth > (int) (0.5 * splash.getPreferredSize().width)) {
            maximumWidth = (int) (0.5 * splash.getPreferredSize().width);
        }

        Dimension minButtonDim = new Dimension((int)(maximumWidth / 1.618), 25);
        if (textDim.getWidth() > minButtonDim.getWidth()) {
            minButtonDim = textDim;
        }
        
        btnLoadUnit.setMinimumSize(minButtonDim);
        btnLoadUnit.setPreferredSize(minButtonDim);
        btnNewMek.setMinimumSize(minButtonDim);
        btnNewMek.setPreferredSize(minButtonDim);
        btnNewVee.setMinimumSize(minButtonDim);
        btnNewVee.setPreferredSize(minButtonDim);
        btnNewSupportVee.setMinimumSize(minButtonDim);
        btnNewSupportVee.setPreferredSize(minButtonDim);
        btnNewBA.setMinimumSize(minButtonDim);
        btnNewBA.setPreferredSize(minButtonDim);
        btnNewAero.setMinimumSize(minButtonDim);
        btnNewAero.setPreferredSize(minButtonDim);
        btnNewDropper.setMinimumSize(minButtonDim);
        btnNewDropper.setPreferredSize(minButtonDim);
        btnNewLargeCraft.setMinimumSize(minButtonDim);
        btnNewLargeCraft.setPreferredSize(minButtonDim);
        btnNewPbi.setMinimumSize(minButtonDim);
        btnNewPbi.setPreferredSize(minButtonDim);
        btnNewProto.setMinimumSize(minButtonDim);
        btnNewProto.setPreferredSize(minButtonDim);
        btnQuit.setMinimumSize(minButtonDim);
        btnQuit.setPreferredSize(minButtonDim);
        
        // layout
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        // Left Column
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(10, 5, 10, 10);
        c.ipadx = 10; c.ipady = 5;
        c.gridx = 0;  c.gridy = 0;
        c.fill = GridBagConstraints.NONE;
        c.weightx = 0.0; c.weighty = 0.0;
        c.gridwidth = 1;
        c.gridheight = 12;
        add(splash, c);
        // Right Column
        c.insets = new Insets(2, 2, 2, 10);
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1.0; c.weighty = 1.0;
        c.ipadx = 0; c.ipady = 0;
        c.gridheight = 1;
        c.gridx = 1; c.gridy = 0;
        add(labVersion, c);
        c.gridy++;
        add(btnLoadUnit, c);
        c.gridy++;
        add(btnNewMek, c);
        c.gridy++;
        add(btnNewVee, c);
        c.gridy++;
        add(btnNewSupportVee, c);
        c.gridy++;
        add(btnNewProto, c);
        c.gridy++;
        add(btnNewBA, c);
        c.gridy++;
        add(btnNewPbi, c);
        c.gridy++;
        add(btnNewAero, c);
        c.gridy++;
        add(btnNewDropper, c);
        c.gridy++;
        add(btnNewLargeCraft, c);
        c.gridy++;
        add(btnQuit, c);
        
        frame.setResizable(false);
        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(this, BorderLayout.CENTER);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                frame.setVisible(false);
                System.exit(0);
            }
        });
        frame.validate();
        frame.pack();
        // center the screen
        frame.setLocationRelativeTo(null);

        frame.setVisible(true);
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundIcon == null) {
            return;
        }
        int w = getWidth();
        int h = getHeight();
        int iW = backgroundIcon.getWidth();
        int iH = backgroundIcon.getHeight();
        // If the image isn't loaded, prevent an infinite loop
        if ((iW < 1) || (iH < 1)) {
            return;
        }
        for (int x = 0; x < w; x += iW) {
            for (int y = 0; y < h; y += iH) {
                g.drawImage(backgroundIcon, x, y, null);
            }
        }
     }

    /**
     * This function will create a new mainUI frame (via the loading dialog) for the 
     * given unit type and get rid of the splash screen
     * @param type an <code>int</code> corresponding to the unit type to construct
     */
    private void newUnit(long type) {
        newUnit(type, false, false, null);
    }
    
    private void newUnit(long type, boolean primitive, boolean industrial, Entity en) {
        frame.setVisible(false);
        LoadingDialog ld = new LoadingDialog(frame, type, primitive, industrial, en);
        ld.setVisible(true);
    }
    
    private void loadUnit() {
        EquipmentType.initializeTypes();
        UnitLoadingDialog unitLoadingDialog = new UnitLoadingDialog(frame);
        unitLoadingDialog.setVisible(true);
        MegaMekLabUnitSelectorDialog viewer = new MegaMekLabUnitSelectorDialog(frame, unitLoadingDialog);

        Entity newUnit = viewer.getChosenEntity();
        viewer.setVisible(false);
        viewer.dispose();

        if (null == newUnit) {
            return;
        }

        if (!UnitUtil.validateUnit(newUnit).isBlank()) {
            JOptionPane.showMessageDialog(frame, String.format(
                    resourceMap.getString("message.invalidUnit.format"),
                            UnitUtil.validateUnit(newUnit)));
        }

        if (newUnit.isSupportVehicle()) {
            newUnit(Entity.ETYPE_SUPPORT_TANK, false, false, newUnit);
        } else if (newUnit.hasETypeFlag(Entity.ETYPE_SMALL_CRAFT)) {
            newUnit(Entity.ETYPE_DROPSHIP, newUnit.isPrimitive(), false, newUnit);
        } else if (newUnit.hasETypeFlag(Entity.ETYPE_JUMPSHIP)) {
            newUnit(Entity.ETYPE_JUMPSHIP, newUnit.isPrimitive(), false, newUnit);
        } else if ((newUnit instanceof Aero) && !(newUnit instanceof FixedWingSupport)) {
            newUnit(Entity.ETYPE_AERO, newUnit.isPrimitive(), false, newUnit);
        } else if (newUnit instanceof BattleArmor) {
            newUnit(Entity.ETYPE_BATTLEARMOR, false, false, newUnit);
        } else if (newUnit instanceof Infantry) {
            newUnit(Entity.ETYPE_INFANTRY, false, false, newUnit);
        } else if (newUnit instanceof Mech) {
            newUnit(Entity.ETYPE_MECH, false, false, newUnit);
        } else if (newUnit instanceof Protomech) {
            newUnit(Entity.ETYPE_PROTOMECH, false, false, newUnit);
        } else if ((newUnit instanceof Tank) && !(newUnit instanceof GunEmplacement)) {
            newUnit(Entity.ETYPE_TANK, false, false, newUnit);
        }
    }
}
