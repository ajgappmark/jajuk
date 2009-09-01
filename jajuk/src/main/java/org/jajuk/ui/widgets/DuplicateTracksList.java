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
 *  
 */

package org.jajuk.ui.widgets;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.miginfocom.swing.MigLayout;

import org.jajuk.base.File;
import org.jajuk.base.FileManager;
import org.jajuk.events.JajukEvent;
import org.jajuk.events.JajukEvents;
import org.jajuk.events.ObservationManager;
import org.jajuk.util.Messages;
import org.jajuk.util.UtilSystem;
import org.jajuk.util.log.Log;

public class DuplicateTracksList extends JPanel implements ListSelectionListener {

  private static final long serialVersionUID = 1L;

  private final JList list;
  private final JScrollPane listScrollPane;
  private final DefaultListModel listModel = new DefaultListModel();
  private final List<List<File>> allFiles;
  private List<File> flatFilesList;

  private final JButton deleteButton;
  private final JButton selectAllButton;
  private final JButton closeButton;

  public DuplicateTracksList(List<List<File>> files, JButton jbClose) {
    super(new BorderLayout());
    allFiles = files;
    closeButton = jbClose;
    populateList(files);

    list = new JList(listModel);
    list.addListSelectionListener(this);
    list.setVisibleRowCount(20);
    listScrollPane = new JScrollPane(list);

    deleteButton = new JButton(Messages.getString("Delete"));
    deleteButton.setActionCommand(Messages.getString("Delete"));
    deleteButton.addActionListener(new DeleteListener());

    selectAllButton = new JButton(Messages.getString("FindDuplicateTracksAction.4"));
    selectAllButton.setActionCommand(Messages.getString("FindDuplicateTracksAction.4"));
    selectAllButton.addActionListener(new SelectAllListener());

    JPanel buttonPane = new JPanel(new MigLayout("ins 5,right"));
    
    buttonPane.add(deleteButton,"sg buttons,center");
    buttonPane.add(selectAllButton,"sg buttons,center");
    buttonPane.add(closeButton,"sg buttons,center");

    buttonPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

    add(listScrollPane, BorderLayout.CENTER);
    add(buttonPane, BorderLayout.PAGE_END);
  }

  public final void populateList(List<List<File>> allFiles) {
    flatFilesList = new ArrayList<File>();
    for (List<File> lFiles : allFiles) {
      for (File f : lFiles) {
        flatFilesList.add(f);
      }
    }

    listModel.removeAllElements();
    for (List<File> lFiles : allFiles) {
      listModel.addElement(lFiles.get(0).getName() + " ( "
          + lFiles.get(0).getDirectory().getAbsolutePath() + " ) ");
      for (int i = 1; i < lFiles.size(); i++) {
        listModel.addElement("  + " + lFiles.get(i).getName() + " ( "
            + lFiles.get(i).getDirectory().getAbsolutePath() + " ) ");
      }
    }
  }

  class DeleteListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      int indices[] = list.getSelectedIndices();
      String sFiles = getSelectedFiles(indices);

      int iResu = Messages.getChoice(Messages.getString("Confirmation_delete_files") + " : \n\n"
          + sFiles + "\n" + indices.length + " " + Messages.getString("Confirmation_file_number"),
          JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
      if (iResu != JOptionPane.YES_OPTION) {
        return;
      }

      // Delete physically files from disk and from collection
      for (int i : indices) {
        try {
          UtilSystem.deleteFile(flatFilesList.get(i).getFIO());
          FileManager.getInstance().removeFile(flatFilesList.get(i));
        } catch (Exception ioe) {
          Log.error(131, ioe);
        }
      }

      // Remove table rows
      int deletedRows = 0;
      for (int i : indices) {
        listModel.removeElement(i - deletedRows);
        flatFilesList.remove(i - deletedRows);
        deleteFilefromList(i - deletedRows);
        deletedRows++;
      }

      populateList(allFiles);
      ObservationManager.notify(new JajukEvent(JajukEvents.DEVICE_REFRESH));
    }

    private void deleteFilefromList(int index) {
      int count = 0;
      for (int r = 0; r < allFiles.size(); r++) {
        for (int c = 0; c < allFiles.get(r).size(); c++) {
          if (count == index) {
            if (allFiles.get(r).size() <= 2) {
              allFiles.remove(r);
            } else {
              allFiles.get(r).remove(c);
            }
          }
          count++;
        }
      }
    }

    private String getSelectedFiles(int indices[]) {
      String sFiles = "";
      for (int k : indices) {
        sFiles += "* " + flatFilesList.get(k).getAbsolutePath() + "\n";
      }
      return sFiles;
    }
  }

  class SelectAllListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      List<Integer> iList = new ArrayList<Integer>();
      int i = 0;
      for (List<File> lFiles : allFiles) {
        i++;
        for (int k = 1; k < lFiles.size(); k++) {
          iList.add(i++);
        }
      }
      int[] indices = new int[iList.size()];
      for (int k = 0; k < iList.size(); k++) {
        indices[k] = iList.get(k);
      }

      list.setSelectedIndices(indices);
    }
  }

  public void valueChanged(ListSelectionEvent e) {
    if (!e.getValueIsAdjusting()) {

      if (list.getSelectedIndex() == -1) {
        // No selection, disable delete button.
        deleteButton.setEnabled(false);

      } else {
        // Selection, enable the delete button.
        deleteButton.setEnabled(true);
      }
    }
  }
}