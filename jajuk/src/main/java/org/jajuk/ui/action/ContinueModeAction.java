/*
 *  Jajuk
 *  Copyright (C) 2005 The Jajuk Team
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
package org.jajuk.ui.action;

import org.jajuk.base.FIFO;
import org.jajuk.base.FileManager;
import org.jajuk.base.StackItem;
import org.jajuk.i18n.Messages;
import org.jajuk.ui.CommandJPanel;
import org.jajuk.ui.JajukJMenuBar;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.Util;
import org.jajuk.util.error.JajukException;

import java.awt.event.ActionEvent;

import javax.swing.BorderFactory;

public class ContinueModeAction extends ActionBase {

	private static final long serialVersionUID = 1L;

	ContinueModeAction() {
		super(
				Messages.getString("JajukJMenuBar.12"), Util.getIcon(ICON_CONTINUE), //$NON-NLS-1$
				true); //$NON-NLS-1$
		setShortDescription(Messages.getString("CommandJPanel.3")); //$NON-NLS-1$
	}

	public void perform(ActionEvent evt) throws JajukException {
		boolean b = ConfigurationManager.getBoolean(CONF_STATE_CONTINUE);
		ConfigurationManager.setProperty(CONF_STATE_CONTINUE, Boolean
				.toString(!b));

		JajukJMenuBar.getInstance().jcbmiContinue.setSelected(!b);
		CommandJPanel.getInstance().jbContinue.setSelected(!b);

		if (!b) { // enabled button
			CommandJPanel.getInstance().jbContinue.setBorder(BorderFactory
					.createLoweredBevelBorder());
			if (FIFO.isStopped()) {
				// if nothing playing, play next track if possible
				StackItem item = FIFO.getInstance().getLastPlayed();
				if (item != null) {
					FIFO.getInstance().push(
							new StackItem(FileManager.getInstance()
									.getNextFile(item.getFile())), false);
				}
			}
		}
		// computes planned tracks
		FIFO.getInstance().computesPlanned(false);
	}
}
