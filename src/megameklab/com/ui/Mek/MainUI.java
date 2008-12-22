/*
 * MegaMekLab - Copyright (C) 2008 
 * 
 * Original author - jtighe (torren@users.sourceforge.net)
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 */

package megameklab.com.ui.Mek;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;

import megamek.MegaMek;
import megamek.client.ui.swing.UnitLoadingDialog;
import megamek.common.BipedMech;
import megamek.common.Engine;
import megamek.common.Entity;
import megamek.common.EntityListFile;
import megamek.common.EquipmentType;
import megamek.common.Mech;
import megamek.common.QuadMech;
import megamek.common.TechConstants;
import megamek.common.UnitType;
import megamek.common.verifier.EntityVerifier;
import megamek.common.verifier.TestEntity;
import megamek.common.verifier.TestMech;
import megameklab.com.ui.Mek.Printing.PrintAdvancedMech;
import megameklab.com.ui.Mek.Printing.PrintAdvancedQuad;
import megameklab.com.ui.Mek.Printing.PrintMech;
import megameklab.com.ui.Mek.Printing.PrintQuad;
import megameklab.com.ui.Mek.tabs.ArmorTab;
import megameklab.com.ui.Mek.tabs.BuildTab;
import megameklab.com.ui.Mek.tabs.EquipmentTab;
import megameklab.com.ui.Mek.tabs.StructureTab;
import megameklab.com.ui.Mek.tabs.WeaponTab;
import megameklab.com.ui.dialog.UnitViewerDialog;
import megameklab.com.util.CConfig;
import megameklab.com.util.RefreshListener;
import megameklab.com.util.SaveMechToMTF;
import megameklab.com.util.UnitUtil;

public class MainUI extends JFrame implements RefreshListener {

	/**
     * 
     */
	private static final long serialVersionUID = -5836932822468918198L;
	private static final String VERSION = "0.0.0.9-107";

	Mech entity = null;
	JMenuBar menuBar = new JMenuBar();
	JMenu file = new JMenu("File");
	JMenu help = new JMenu("Help");
	JMenu validate = new JMenu("Validate");
	JTabbedPane ConfigPane = new JTabbedPane(SwingConstants.TOP);
	JPanel contentPane;
	private StructureTab structureTab;
	private ArmorTab armorTab;
	private EquipmentTab equipmentTab;
	private WeaponTab weaponTab;
	private BuildTab buildTab;
	private Header header;
	private StatusBar statusbar;
	JPanel masterPanel = new JPanel();
	JScrollPane scroll = new JScrollPane();
	public CConfig config;

	public MainUI() {

		UnitUtil.loadFonts();
		config = new CConfig();
		System.out.println("Staring MegaMekLab version: " + VERSION);
		file.setMnemonic('F');
		JMenuItem item = new JMenuItem();

		item.setText("Load");
		item.setMnemonic('L');
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jMenuLoadEntity_actionPerformed(e);
			}
		});
		file.add(item);

		item = new JMenuItem();
		item.setText("Save");
		item.setMnemonic('S');
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				UnitUtil.compactCriticals(entity);
				UnitUtil.reIndexCrits(entity);
				SaveMechToMTF.getInstance(entity, entity.getChassis() + " " + entity.getModel() + ".mtf").save();
			}
		});
		file.add(item);

		item = new JMenuItem("Reset");
		item.setMnemonic('R');
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jMenuResetEntity_actionPerformed(e);
			}
		});
		file.add(item);

		JMenu printMenu = new JMenu("Print");
		printMenu.setMnemonic('P');

		JMenu standardRecordSheet = new JMenu("Standard Record Sheet");
		standardRecordSheet.setMnemonic('S');

		item = new JMenuItem("Current Unit");
		item.setMnemonic('C');
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jMenuPrint_actionPerformed(e);
			}
		});
		standardRecordSheet.add(item);

		item = new JMenuItem("From MUL");
		item.setMnemonic('F');
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jMenuPrintMulMechs_actionPerformed(e);
			}
		});
		standardRecordSheet.add(item);
		printMenu.add(standardRecordSheet);

		JMenu advancedRecordSheet = new JMenu("Advanced Record Sheet");
		advancedRecordSheet.setMnemonic('A');

		item = new JMenuItem("Current Unit");
		item.setMnemonic('C');
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jMenuAdvancedPrint_actionPerformed(e);
			}
		});
		advancedRecordSheet.add(item);

		item = new JMenuItem("From MUL");
		item.setMnemonic('F');
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jMenuPrintAdvancedMulMechs_actionPerformed(e);
			}
		});
		advancedRecordSheet.add(item);
		printMenu.add(advancedRecordSheet);

		file.add(printMenu);

		file.addSeparator();

		item = new JMenuItem();
		item.setText("Exit");
		item.setMnemonic('x');
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jMenuExit_actionPerformed(e);
			}
		});
		file.add(item);

		item = new JMenuItem();
		item.setText("About");
		item.setMnemonic('A');
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jMenuHelpAbout_actionPerformed();
			}
		});
		help.add(item);

		item = new JMenuItem();
		item.setText("Validate Current Unit");
		item.setMnemonic('V');
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jMenuValidateUnit_actionPerformed();
			}
		});
		validate.add(item);

		item = new JMenuItem();
		item.setText("BV Calculations");
		item.setMnemonic('B');
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jMenuBVCalculations_actionPerformed();
			}
		});
		validate.add(item);

		menuBar.add(file);
		menuBar.add(validate);
		menuBar.add(help);

		setLocation(getLocation().x + 10, getLocation().y);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent evt) {
				config.setParam("WINDOWSTATE", Integer.toString(getExtendedState()));
				// Only save position and size if not maximized or minimized.
				if (getExtendedState() == Frame.NORMAL) {
					config.setParam("WINDOWHEIGHT", Integer.toString(getHeight()));
					config.setParam("WINDOWWIDTH", Integer.toString(getWidth()));
					config.setParam("WINDOWLEFT", Integer.toString(getX()));
					config.setParam("WINDOWTOP", Integer.toString(getY()));
				}
				config.saveConfig();

				System.exit(0);
			}
		});

		// ConfigPane.setMinimumSize(new Dimension(300, 300));
		createNewMech(false);
		setTitle(entity.getChassis() + " " + entity.getModel() + ".mtf");
		setJMenuBar(menuBar);
		scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		scroll.setViewportView(masterPanel);
		scroll.setBorder(BorderFactory.createEmptyBorder());
		this.add(scroll);
		Dimension maxSize = new Dimension(config.getIntParam("WINDOWWIDTH"), config.getIntParam("WINDOWHEIGHT"));
		// masterPanel.setPreferredSize(new Dimension(600,400));
		// scroll.setPreferredSize(maxSize);
		setResizable(true);
		setSize(maxSize);
		setMaximumSize(maxSize);
		setPreferredSize(maxSize);
		setExtendedState(config.getIntParam("WINDOWSTATE"));
		setLocation(config.getIntParam("WINDOWLEFT"), config.getIntParam("WINDOWTOP"));

		reloadTabs();
		setVisible(true);
		repaint();
		refreshAll();
	}

	private void loadUnit() {
		UnitLoadingDialog unitLoadingDialog = new UnitLoadingDialog(this);
		UnitViewerDialog viewer = new UnitViewerDialog(this, unitLoadingDialog, UnitType.MEK, config);

		viewer.run();
		// viewer.setVisible(true);

		if (viewer != null) {

			if (!(viewer.getSelectedEntity() instanceof Mech)) {
				return;
			}
			entity = (Mech) viewer.getSelectedEntity();
			UnitUtil.removeOneShotAmmo(entity);
			UnitUtil.expandUnitMounts(entity);

			viewer.setVisible(false);
			viewer.dispose();

			reloadTabs();
			setVisible(true);
			this.repaint();
			refreshAll();
		}
	}

	public void jMenuLoadEntity_actionPerformed(ActionEvent event) {
		loadUnit();
	}

	public void jMenuResetEntity_actionPerformed(ActionEvent event) {

		createNewMech(false);
		structureTab.updateMech(entity);
		armorTab.updateMech(entity);
		equipmentTab.updateMech(entity);
		weaponTab.updateMech(entity);
		buildTab.updateMech(entity);
		statusbar.updateMech(entity);
		header.updateMech(entity);

		weaponTab.getView().removeAllWeapons();
		equipmentTab.getView().removeAllEquipment();

		statusbar.refresh();
		structureTab.refresh();
		armorTab.refresh();
		equipmentTab.refresh();
		weaponTab.refresh();
		buildTab.refresh();
	}

	public void jMenuPrint_actionPerformed(ActionEvent event) {

		if (entity instanceof QuadMech) {
			ArrayList<Mech> mechList = new ArrayList<Mech>();
			mechList.add(entity);

			PrintQuad sp = new PrintQuad(mechList);

			sp.print();
		} else {
			ArrayList<Mech> mechList = new ArrayList<Mech>();
			mechList.add(entity);

			PrintMech sp = new PrintMech(mechList);

			sp.print();
		}
	}

	public void jMenuPrintMulMechs_actionPerformed(ActionEvent event) {
		ArrayList<Mech> quadList = new ArrayList<Mech>();
		ArrayList<Mech> bipedList = new ArrayList<Mech>();

		FileDialog f = new FileDialog(new JDialog(), "Load Mul");
		f.setDirectory(System.getProperty("user.dir"));
		f.setFilenameFilter(new FilenameFilter() {
			public boolean accept(final File dir, final String name) {
				return (null != name && name.endsWith(".mul"));
			}
		});
		f.setVisible(true);

		if (f.getFile() != null) {
			Vector<Entity> loadedUnits = new Vector<Entity>();
			try {
				loadedUnits = EntityListFile.loadFrom(new File(f.getDirectory() + f.getFile()));
				loadedUnits.trimToSize();
			} catch (Exception ex) {
				ex.printStackTrace();
				return;
			}

			for (Entity unit : loadedUnits) {
				if (unit instanceof QuadMech) {
					quadList.add((Mech) unit);
				} else if (unit instanceof BipedMech) {
					bipedList.add((Mech) unit);
				}
			}

			if (bipedList.size() > 0) {
				PrintMech printMech = new PrintMech(bipedList);

				printMech.print();
			}

			if (quadList.size() > 0) {
				PrintQuad printQuad = new PrintQuad(quadList);

				printQuad.print();
			}

		}

	}

	public void jMenuAdvancedPrint_actionPerformed(ActionEvent event) {

		if (entity instanceof QuadMech) {
			String fImageName = "./data/images/recordsheets/toquad.png";

			ArrayList<Mech> mechList = new ArrayList<Mech>();
			mechList.add(entity);

			PrintAdvancedQuad sp = new PrintAdvancedQuad(getToolkit().getImage(fImageName), mechList);

			sp.print();

		} else {
			String fImageName = "./data/images/recordsheets/tobiped.png";

			ArrayList<Mech> mechList = new ArrayList<Mech>();
			mechList.add(entity);

			PrintAdvancedMech sp = new PrintAdvancedMech(getToolkit().getImage(fImageName), mechList);

			sp.print();
		}
	}

	public void jMenuPrintAdvancedMulMechs_actionPerformed(ActionEvent event) {
		ArrayList<Mech> quadList = new ArrayList<Mech>();
		ArrayList<Mech> bipedList = new ArrayList<Mech>();

		FileDialog f = new FileDialog(new JDialog(), "Load Mul");
		f.setDirectory(System.getProperty("user.dir"));
		f.setFilenameFilter(new FilenameFilter() {
			public boolean accept(final File dir, final String name) {
				return (null != name && name.endsWith(".mul"));
			}
		});
		f.setVisible(true);

		if (f.getFile() != null) {
			Vector<Entity> loadedUnits = new Vector<Entity>();
			try {
				loadedUnits = EntityListFile.loadFrom(new File(f.getDirectory() + f.getFile()));
				loadedUnits.trimToSize();
			} catch (Exception ex) {
				ex.printStackTrace();
				return;
			}

			for (Entity unit : loadedUnits) {
				if (unit instanceof QuadMech) {
					quadList.add((Mech) unit);
				} else if (unit instanceof BipedMech) {
					bipedList.add((Mech) unit);
				}
			}

			String fImageName = "./data/images/recordsheets/tobiped.png";

			Image image = getToolkit().getImage(fImageName);

			if (bipedList.size() > 0) {
				PrintAdvancedMech printMech = new PrintAdvancedMech(image, bipedList);

				printMech.print();
			}

			fImageName = "./data/images/recordsheets/toquad.png";

			image = getToolkit().getImage(fImageName);

			if (quadList.size() > 0) {
				PrintAdvancedQuad printQuad = new PrintAdvancedQuad(image, quadList);

				printQuad.print();
			}

		}

	}

	// Show BV Calculations

	public void jMenuBVCalculations_actionPerformed() {
		String bvText = entity.getBVText();

		JScrollPane scroll = new JScrollPane();
		JPanel panel = new JPanel();
		JTextArea text = new JTextArea();

		scroll.setViewportView(panel);
		scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		panel.add(text);

		text.setText(bvText);

		scroll.setVisible(true);

		JDialog jdialog = new JDialog();

		jdialog.add(scroll);
		Dimension size = new Dimension(config.getIntParam("WINDOWWIDTH") / 2, config.getIntParam("WINDOWHEIGHT"));

		jdialog.setPreferredSize(size);
		jdialog.setMinimumSize(size);

		jdialog.setLocationRelativeTo(this);
		jdialog.setVisible(true);
		// JOptionPane.showMessageDialog(this, bvText, "BV Calculations",
		// JOptionPane.INFORMATION_MESSAGE);
	}

	// Show Validation data.
	public void jMenuValidateUnit_actionPerformed() {

		EntityVerifier entityVerifier = new EntityVerifier(new File("data/mechfiles/UnitVerifierOptions.xml"));
		StringBuffer sb = new StringBuffer();
		TestEntity testEntity = null;
		testEntity = new TestMech(entity, entityVerifier.mechOption, null);
		if (testEntity != null) {
			testEntity.correctEntity(sb, true);
		}

		if (sb.length() > 0) {
			JOptionPane.showMessageDialog(this, sb.toString(), "Unit Validation", JOptionPane.ERROR_MESSAGE);
		} else {
			JOptionPane.showMessageDialog(this, "Validation Passed", "Unit Validation", JOptionPane.INFORMATION_MESSAGE);
		}

	}

	// Show data about MegaMekLab
	public void jMenuHelpAbout_actionPerformed() {

		// make the dialog
		JDialog dlg = new JDialog(this, "MegaMekLab Info");

		// set up the contents
		JPanel child = new JPanel();
		child.setLayout(new BoxLayout(child, BoxLayout.Y_AXIS));

		// set the text up.
		JLabel mekwars = new JLabel("MegaMekLab Version: " + VERSION);
		JLabel version = new JLabel("MegaMek Version: " + MegaMek.VERSION);
		JLabel license1 = new JLabel("MegaMekLab software is under GPL. See");
		JLabel license2 = new JLabel("license.txt in ./Docs/licenses for details.");
		JLabel license3 = new JLabel("Project Info:");
		JLabel license4 = new JLabel("       http://www.sourceforge.net/projects/megameklab       ");

		// center everything
		mekwars.setAlignmentX(Component.CENTER_ALIGNMENT);
		version.setAlignmentX(Component.CENTER_ALIGNMENT);
		license1.setAlignmentX(Component.CENTER_ALIGNMENT);
		license2.setAlignmentX(Component.CENTER_ALIGNMENT);
		license3.setAlignmentX(Component.CENTER_ALIGNMENT);
		license4.setAlignmentX(Component.CENTER_ALIGNMENT);

		// add to child panel
		child.add(new JLabel("\n"));
		child.add(mekwars);
		child.add(version);
		child.add(new JLabel("\n"));
		child.add(license1);
		child.add(license2);
		child.add(new JLabel("\n"));
		child.add(license3);
		child.add(license4);
		child.add(new JLabel("\n"));

		// then add child panel to the content pane.
		dlg.getContentPane().add(child);

		// set the location of the dialog
		Dimension dlgSize = dlg.getPreferredSize();
		Dimension frmSize = getSize();
		Point loc = getLocation();
		dlg.setLocation((frmSize.width - dlgSize.width) / 2 + loc.x, (frmSize.height - dlgSize.height) / 2 + loc.y);
		dlg.setModal(true);
		dlg.setResizable(false);
		dlg.pack();
		dlg.setVisible(true);
	}

	public void reloadTabs() {
		masterPanel.removeAll();
		ConfigPane.removeAll();

		masterPanel.setLayout(new BoxLayout(masterPanel, BoxLayout.Y_AXIS));
		armorTab = new ArmorTab(entity);
		armorTab.setArmorType(entity.getArmorType());
		armorTab.setStructureType(entity.getStructureType());
		armorTab.refresh();

		structureTab = new StructureTab(entity);

		header = new Header(entity);
		statusbar = new StatusBar(entity);
		equipmentTab = new EquipmentTab(entity);
		weaponTab = new WeaponTab(entity);
		buildTab = new BuildTab(entity, equipmentTab, weaponTab);
		header.addRefreshedListener(this);
		structureTab.addRefreshedListener(this);
		armorTab.addRefreshedListener(this);
		equipmentTab.addRefreshedListener(this);
		weaponTab.addRefreshedListener(this);
		buildTab.addRefreshedListener(this);

		ConfigPane.addTab("Structure", structureTab);
		ConfigPane.addTab("Armor", armorTab);
		ConfigPane.addTab("Equipment", equipmentTab);
		ConfigPane.addTab("Weapons", weaponTab);
		ConfigPane.addTab("Build", buildTab);

		masterPanel.add(header);
		masterPanel.add(ConfigPane);
		masterPanel.add(statusbar);

		refreshHeader();
		this.repaint();
	}

	public void jMenuExit_actionPerformed(ActionEvent event) {
		System.exit(0);
	}

	public void createNewMech(boolean isQuad) {

		if (isQuad) {
			entity = new QuadMech(Mech.GYRO_STANDARD, Mech.COCKPIT_STANDARD);
		} else {
			entity = new BipedMech(Mech.GYRO_STANDARD, Mech.COCKPIT_STANDARD);
		}

		entity.setYear(2750);
		entity.setTechLevel(TechConstants.T_INTRO_BOXSET);
		entity.setWeight(25);
		entity.setEngine(new Engine(25, Engine.NORMAL_ENGINE, 0));
		entity.setArmorType(EquipmentType.T_ARMOR_STANDARD);
		entity.setStructureType(EquipmentType.T_STRUCTURE_STANDARD);

		entity.addGyro();
		entity.addEngineCrits();
		entity.addCockpit();
		UnitUtil.updateHeatSinks(entity, 10, 0);

		entity.autoSetInternal();
		for (int loc = 0; loc <= Mech.LOC_LLEG; loc++) {
			entity.setArmor(0, loc);
			entity.setArmor(0, loc, true);
		}

		entity.setChassis("New");
		entity.setModel("Mek");

	}

	public void refreshAll() {

		if ((structureTab.isQuad() && !(entity instanceof QuadMech)) || (!structureTab.isQuad() && entity instanceof QuadMech)) {
			createNewMech(structureTab.isQuad());
			structureTab.updateMech(entity);
			armorTab.updateMech(entity);
			equipmentTab.updateMech(entity);
			weaponTab.updateMech(entity);
			buildTab.updateMech(entity);
			statusbar.updateMech(entity);
			header.updateMech(entity);
		}
		statusbar.refresh();
		structureTab.refresh();
		armorTab.refresh();
		equipmentTab.refresh();
		weaponTab.refresh();
		buildTab.refresh();
	}

	public void refreshArmor() {
		armorTab.refresh();
	}

	public void refreshBuild() {
		buildTab.refresh();
	}

	public void refreshEquipment() {
		equipmentTab.refresh();

	}

	public void refreshHeader() {
		setTitle(entity.getChassis() + " " + entity.getModel() + ".mtf");
	}

	public void refreshStatus() {
		statusbar.refresh();
	}

	public void refreshStructure() {
		structureTab.refresh();
	}

	public void refreshWeapons() {
		weaponTab.refresh();
	}

}