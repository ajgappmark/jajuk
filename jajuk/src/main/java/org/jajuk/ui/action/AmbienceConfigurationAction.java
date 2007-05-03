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

import org.jajuk.i18n.Messages;
import org.jajuk.ui.wizard.AmbienceWizard;
import org.jajuk.util.Util;

import java.awt.event.ActionEvent;

/**
 * Action for configure ambiences
 */
public class AmbienceConfigurationAction extends ActionBase {

	private static final long serialVersionUID = 1L;

	AmbienceConfigurationAction() {
		super(
				Messages.getString("CommandJPanel.19"), Util.getIcon(ICON_STYLE), true); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public void perform(ActionEvent evt) {
		AmbienceWizard ambience = new AmbienceWizard(); // display the ambience
		// wizard
		ambience.show();
	}
}
