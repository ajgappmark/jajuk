/*
 *  Jajuk
 *  Copyright (C) 2007 The Jajuk Team
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *  $$Revision$$
 */

package org.jajuk.ui.wizard;

import org.jajuk.Main;
import org.jajuk.base.Device;
import org.jajuk.base.DeviceManager;
import org.jajuk.i18n.Messages;
import org.jajuk.ui.JajukFileChooser;
import org.jajuk.ui.JajukJDialog;
import org.jajuk.ui.PathSelector;
import org.jajuk.ui.perspectives.HelpPerspective;
import org.jajuk.ui.perspectives.SimplePerspective;
import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.JajukFileFilter;
import org.jajuk.util.Util;
import org.jajuk.util.log.Log;
import org.jdesktop.swingx.JXCollapsiblePane;
import org.jdesktop.swingx.JXHyperlink;
import org.jdesktop.swingx.VerticalLayout;

import info.clearthought.layout.TableLayout;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import javax.swing.Action;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;

/**
 * First time Wizard
 */
public class FirstTimeWizard extends JajukJDialog implements ITechnicalStrings, ActionListener {
	private static final long serialVersionUID = 1L;

	JLabel jlLeftIcon;

	JPanel jpRightPanel;

	JLabel jlWelcome;

	JLabel jlFileSelection;

	JTextField jtfFileSelected;

	JButton jbFileSelection;

	PathSelector path;

	JLabel jlRefreshTime;

	JTextField jtfRefreshTime;

	JLabel jlMins;

	JCheckBox jcbHelp;

	JXCollapsiblePane advanced;

	JPanel jpButtons;

	JButton jbOk;

	JButton jbCancel;

	JPanel jpMain;

	/** Selected directory */
	private File fDir;

	/**
	 * First time wizard
	 */
	public FirstTimeWizard() {
		setTitle(Messages.getString("FirstTimeWizard.0"));//$NON-NLS-1$
		int iX_SEPARATOR = 10;
		int iY_SEPARATOR = 10;
		jlLeftIcon = new JLabel(Util.getIcon(IMAGE_SEARCH));
		jpRightPanel = new JPanel();
		jlWelcome = new JLabel(Messages.getString("FirstTimeWizard.1")); //$NON-NLS-1$
		jlFileSelection = new JLabel(Messages.getString("FirstTimeWizard.2")); //$NON-NLS-1$
		jbFileSelection = new JButton(Util.getIcon(ICON_OPEN_DIR));
		jtfFileSelected = new JTextField(""); //$NON-NLS-1$
		jtfFileSelected.setForeground(Color.BLUE);
		jtfFileSelected.setEditable(false);
		jbFileSelection.addActionListener(this);
		JLabel jlWorkspace = new JLabel(Messages.getString("FirstTimeWizard.7"));
		jlWorkspace.setToolTipText(Messages.getString("FirstTimeWizard.7"));
		path = new PathSelector(new JajukFileFilter(JajukFileFilter.DirectoryFilter.getInstance()),
				System.getProperty("user.home"));
		path.setToolTipText(Messages.getString("FirstTimeWizard.7"));
		JPanel jpWorkspace = new JPanel(new VerticalLayout(iX_SEPARATOR));
		jpWorkspace.add(jlWorkspace);
		jpWorkspace.add(path);

		jcbHelp = new JCheckBox(Messages.getString("FirstTimeWizard.4")); //$NON-NLS-1$
		// Refresh time
		jlRefreshTime = new JLabel(Messages.getString("DeviceWizard.53"));//$NON-NLS-1$
		jtfRefreshTime = new JTextField("5");// 5 mins by default
		jlMins = new JLabel(Messages.getString("DeviceWizard.54"));//$NON-NLS-1$
		JPanel jpRefresh = new JPanel();
		double sizeRefresh[][] = {
				{ TableLayout.PREFERRED, iX_SEPARATOR, 50, iX_SEPARATOR, TableLayout.PREFERRED },
				{ 20 } };
		jpRefresh.setLayout(new TableLayout(sizeRefresh));
		jpRefresh.add(jlRefreshTime, "0,0"); //$NON-NLS-1$
		jpRefresh.add(jtfRefreshTime, "2,0"); //$NON-NLS-1$
		jpRefresh.add(jlMins, "4,0"); //$NON-NLS-1$
		// buttons
		jpButtons = new JPanel();
		jpButtons.setLayout(new FlowLayout(FlowLayout.CENTER));
		jbOk = new JButton(Messages.getString("OK")); //$NON-NLS-1$
		jbOk.setEnabled(false);
		jbOk.addActionListener(this);
		jbCancel = new JButton(Messages.getString("Cancel")); //$NON-NLS-1$
		jbCancel.addActionListener(this);
		jpButtons.add(jbOk);
		jpButtons.add(jbCancel);
		double sizeRight[][] = {
				{ TableLayout.PREFERRED, iX_SEPARATOR },
				{ iY_SEPARATOR, TableLayout.PREFERRED, iY_SEPARATOR, TableLayout.PREFERRED,
						iY_SEPARATOR, 20, 4 * iY_SEPARATOR, 40, iY_SEPARATOR,
						TableLayout.PREFERRED, iY_SEPARATOR, 40 } };

		FlowLayout flSelection = new FlowLayout(FlowLayout.LEFT);
		JPanel jpFileSelection = new JPanel();
		jpFileSelection.setLayout(flSelection);
		jpFileSelection.add(jbFileSelection);
		jpFileSelection.add(Box.createHorizontalStrut(10));
		jpFileSelection.add(jlFileSelection);
		advanced = new JXCollapsiblePane();
		advanced.setLayout(new VerticalLayout(iY_SEPARATOR));
		JXHyperlink toggle = new JXHyperlink(advanced.getActionMap().get(
				JXCollapsiblePane.TOGGLE_ACTION));
		toggle.setText(Messages.getString("FirstTimeWizard.6"));
		toggle.setFont(toggle.getFont().deriveFont(Font.BOLD));
		toggle.setOpaque(true);
		advanced.setCollapsed(true);
		toggle.setFocusPainted(false);
		// get the built-in toggle action
		Action toggleAction = advanced.getActionMap().get(JXCollapsiblePane.TOGGLE_ACTION);

		// use the collapse/expand icons from the JTree UI
		toggleAction.putValue(JXCollapsiblePane.COLLAPSE_ICON, UIManager
				.getIcon("Tree.expandedIcon"));
		toggleAction.putValue(JXCollapsiblePane.EXPAND_ICON, UIManager
				.getIcon("Tree.collapsedIcon"));
		advanced.add(jpWorkspace);
		advanced.add(jcbHelp);

		// jpRightPanel.setLayout(new TableLayout(sizeRight));
		jpRightPanel.setLayout(new VerticalLayout(iY_SEPARATOR));
		jpRightPanel.add(jlWelcome, "0,1"); //$NON-NLS-1$
		jpRightPanel.add(jpFileSelection, "0,3"); //$NON-NLS-1$
		jpRightPanel.add(jtfFileSelected, "0,5"); //$NON-NLS-1$
		jpRightPanel.add(jpRefresh, "0,7"); //$NON-NLS-1$
		jpRightPanel.add(toggle, "0,9"); //$NON-NLS-1$
		jpRightPanel.add(advanced);
		jpRightPanel.add(jpButtons, "0,11"); //$NON-NLS-1$
		double size[][] = { { 20, TableLayout.PREFERRED, 30, TableLayout.PREFERRED }, { 0.99 } };
		jpMain = (JPanel) getContentPane();
		jpMain.setLayout(new TableLayout(size));
		jpMain.add(jlLeftIcon, "1,0"); //$NON-NLS-1$
		jpMain.add(jpRightPanel, "3,0"); //$NON-NLS-1$
		getRootPane().setDefaultButton(jbOk);
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == jbCancel) {
			dispose(); // close window
		} else if (e.getSource() == jbFileSelection) {
			JajukFileChooser jfc = new JajukFileChooser(new JajukFileFilter(
					JajukFileFilter.DirectoryFilter.getInstance()));
			jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			jfc.setDialogTitle(Messages.getString("FirstTimeWizard.5"));//$NON-NLS-1$
			jfc.setMultiSelectionEnabled(false);
			int returnVal = jfc.showOpenDialog(this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				fDir = jfc.getSelectedFile();
				// check device availability
				String sCode = DeviceManager.getInstance().checkDeviceAvailablity(fDir.getName(),
						0, fDir.getAbsolutePath(), fDir.getAbsolutePath(), true);
				if (!sCode.equals("0")) { //$NON-NLS-1$
					Messages.showErrorMessage(sCode);
					jbOk.setEnabled(false);
					return;
				}
				jtfFileSelected.setText(fDir.getAbsolutePath());
				jbOk.setEnabled(true);
				jbOk.grabFocus();
			}
		} else if (e.getSource() == jbOk) {
			if (jcbHelp.isSelected()) {
				// set parameter perspective
				Main.setDefaultPerspective(HelpPerspective.class.getName());
			} else {
				// set Simple perspective
				Main.setDefaultPerspective(SimplePerspective.class.getName());
			}
			// Check workspace directory
			if (!path.getUrl().trim().equals("")) {
				if (!new File(path.getUrl()).canRead()) {
					Messages.showErrorMessage("165");
					return;
				}
			}
			// Set Workspace directory
			try {
				java.io.File bootstrap = new java.io.File(FILE_BOOTSTRAP);
				BufferedWriter bw = new BufferedWriter(new FileWriter(bootstrap));
				bw.write(path.getUrl());
				bw.flush();
				bw.close();
				// Store the workspace PATH
				Main.workspace = path.getUrl();
			} catch (Exception ex) {
				Messages.showErrorMessage("024");
				Log.debug("Cannot write bootstrap file");
			}
			// We have to create a device and to launch immediate refresh but
			// the environment
			// is still far from being operational at this startup state, so
			// Main will unlock it later
			new Thread() {
				public void run() {
					synchronized (Main.canLaunchRefresh) {
						try {
							Main.canLaunchRefresh.wait();
						} catch (InterruptedException e) {
							Log.error(e);
						}
					}
					// Create a directory device
					Device device = DeviceManager.getInstance().registerDevice(fDir.getName(), 0,
							fDir.getAbsolutePath());
					device.setProperty(XML_DEVICE_MOUNT_POINT, fDir.getAbsolutePath());
					device.setProperty(XML_DEVICE_AUTO_MOUNT, true);
					// Set refresh time
					double dRefreshTime = 5d;
					try {
						dRefreshTime = Double.parseDouble(jtfRefreshTime.getText());
						if (dRefreshTime < 0) {
							dRefreshTime = 0;
						}
					} catch (NumberFormatException e1) {
						dRefreshTime = 0;
					}
					device.setProperty(XML_DEVICE_AUTO_REFRESH, dRefreshTime);
					try {
						device.refresh(true, false);
					} catch (Exception e2) {
						Log.error("112", device.getName(), e2); //$NON-NLS-1$
						Messages.showErrorMessage("112", device.getName()); //$NON-NLS-1$
					}
				}
			}.start();

			// exit
			dispose();
		}
	}

}