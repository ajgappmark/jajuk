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
 *  $Revision$
 */

package org.jajuk.ui.views;

import org.jajuk.base.Event;
import org.jajuk.base.FIFO;
import org.jajuk.base.File;
import org.jajuk.base.ObservationManager;
import org.jajuk.base.Observer;
import org.jajuk.base.Track;
import org.jajuk.base.WebRadio;
import org.jajuk.i18n.Messages;
import org.jajuk.ui.action.ActionManager;
import org.jajuk.ui.action.JajukAction;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.EventSubject;
import org.jajuk.util.Util;
import org.jajuk.util.log.Log;

import java.awt.Font;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import ext.services.lyrics.LyricsService;

/**
 * Lyrics view
 * <p>
 * Data comes currently from http://www.lyrc.com.ar
 * </p>
 */
public class LyricsView extends ViewAdapter implements Observer {

	private static final long serialVersionUID = 2229941034734574056L;

	private JTextArea textarea;

	private JLabel jlTitle;

	private JLabel jlAuthor;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jajuk.ui.IView#getDesc()
	 */
	public String getDesc() {
		return Messages.getString("LyricsView.0");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jajuk.ui.IView#initUI()
	 */
	public void initUI() {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		textarea = new JTextArea();
		textarea.setLineWrap(true);
		textarea.setWrapStyleWord(true);
		textarea.setEditable(false);
		textarea.setVisible(false);
		textarea.setMargin(new Insets(10, 10, 10, 10));
		textarea
				.setFont(new Font("Dialog", Font.BOLD, ConfigurationManager.getInt(CONF_FONTS_SIZE)));
		textarea.addMouseListener(new MouseAdapter() {

			@Override
			public void mousePressed(MouseEvent e) {
				if (e.isPopupTrigger()) {
					JPopupMenu menu = new JPopupMenu();
					menu.add(new JMenuItem(ActionManager.getAction(JajukAction.COPY_TO_CLIPBOARD)));
					menu.add(new JMenuItem(ActionManager.getAction(JajukAction.LAUNCH_IN_BROWSER)));
					menu.show(textarea, e.getX(), e.getY());
				}

			}

		});
		jlAuthor = new JLabel();
		jlAuthor.setFont(new Font("Dialog", Font.PLAIN, ConfigurationManager
				.getInt(CONF_FONTS_SIZE) + 2));
		jlTitle = new JLabel();
		jlTitle.setFont(new Font("Dialog", Font.PLAIN, ConfigurationManager
				.getInt(CONF_FONTS_SIZE) + 4));
		JScrollPane jsp = new JScrollPane(textarea);
		textarea.setFont(new Font("Dialog", Font.PLAIN, ConfigurationManager
				.getInt(CONF_FONTS_SIZE)));
		int height = getHeight() - 200;
		FormLayout layout = new FormLayout(
		// --columns
				"3dlu,p:grow, 3dlu",
				// --rows
				"5dlu, p, 3dlu, p, 3dlu,"+height+":grow ,3dlu"); // rows
		PanelBuilder builder = new PanelBuilder(layout);
		CellConstraints cc = new CellConstraints();
		// Add items
		builder.add(jlTitle, cc.xy(2, 2));
		builder.add(jlAuthor, cc.xy(2, 4));
		builder.add(jsp, cc.xy(2, 6));
		JPanel p = builder.getPanel();
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		add(p);

		ObservationManager.register(this);
		// check if a track has already been launched
		update(new Event(EventSubject.EVENT_FILE_LAUNCHED, ObservationManager
				.getDetailsLastOccurence(EventSubject.EVENT_FILE_LAUNCHED)));

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jajuk.base.Observer#getRegistrationKeys()
	 */
	public Set<EventSubject> getRegistrationKeys() {
		HashSet<EventSubject> eventSubjectSet = new HashSet<EventSubject>();
		eventSubjectSet.add(EventSubject.EVENT_FILE_LAUNCHED);
		eventSubjectSet.add(EventSubject.EVENT_ZERO);
		eventSubjectSet.add(EventSubject.EVENT_WEBRADIO_LAUNCHED);
		return eventSubjectSet;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jajuk.base.Observer#update(org.jajuk.base.Event)
	 */
	public void update(final Event event) {
		SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				EventSubject subject = event.getSubject();
				if (subject.equals(EventSubject.EVENT_FILE_LAUNCHED)) {
					File file = FIFO.getInstance().getCurrentFile();
					if (file != null) {
						Track track = FIFO.getInstance().getCurrentFile().getTrack();
						String sURL = "http://www.lyrc.com.ar/en/tema1en.php?artist="
								+ track.getAuthor().getName2() + "&songname=" + track.getName();
						textarea.setVisible(true);
						textarea.setToolTipText(sURL);
						setText(LyricsService.getLyrics(track.getAuthor().getName2(), track
								.getName()));
						jlAuthor.setText(track.getAuthor().getName2());
						jlTitle.setText(track.getName());
						Util.copyData = sURL;
						try {
							Util.url = new URL(sURL);
						} catch (MalformedURLException e) {
							Log.error(e);
						}
					}
				} else if (subject.equals(EventSubject.EVENT_ZERO)) {
					textarea.setVisible(false);
					jlAuthor.setText("");
					jlTitle.setText(Messages.getString("JajukWindow.18"));
				} else if (subject.equals(EventSubject.EVENT_WEBRADIO_LAUNCHED)) {
					WebRadio radio = (WebRadio) event.getDetails().get(DETAIL_CONTENT);
					if (radio != null) {
						jlTitle.setText(radio.getName());
						jlAuthor.setText("");
						textarea.setVisible(false);
					}
				}
			}

		});

	}

	// update text area text
	private void setText(String lyrics) {
		textarea.setText(lyrics);
	}

}
