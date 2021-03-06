/*
 *  Jajuk
 *  Copyright (C) The Jajuk Team
 *  http://jajuk.info
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
package org.jajuk.services.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.digest.DigestUtils;
import org.jajuk.base.Collection;
import org.jajuk.base.DeviceManager;
import org.jajuk.services.bookmark.History;
import org.jajuk.services.players.QueueModel;
import org.jajuk.services.players.StackItem;
import org.jajuk.services.webradio.CustomRadiosPersistenceHelper;
import org.jajuk.services.webradio.PresetRadiosPersistenceHelper;
import org.jajuk.ui.perspectives.IPerspective;
import org.jajuk.util.log.Log;

/**
 * This thread is responsible for commiting configuration or collection files on events. 
 * This allows to save files during Jajuk running and not only when exiting the app as before. 
 * <p>
 * It is sometimes difficult to get clear events to check to so we also start a differential check 
 * on a regular basis through a thread
 * </p>
 * <p>
 * Singleton
 * <p>
 */
public final class PersistenceService extends Thread {
  public enum Urgency {
    HIGH, MEDIUM, LOW
  }

  private static PersistenceService self = new PersistenceService();
  private String lastCommitQueueCheckSum;
  private static final int HEART_BEAT_MS = 1000;
  private static final int MIN_DELAY_AFTER_PERSPECTIVE_CHANGE_MS = 5000;
  private static final int DELAY_HIGH_URGENCY_BEATS = 5;
  private static final int DELAY_MEDIUM_URGENCY_BEATS = 15;
  private static final int DELAY_LOW_URGENCY_BEATS = 600 * HEART_BEAT_MS;
  /** Collection change flag **/
  private volatile Map<Urgency, Boolean> collectionChanged = new HashMap<Urgency, Boolean>(3);
  private volatile boolean radiosChanged = false;
  private volatile boolean historyChanged = false;
  private volatile Map<IPerspective, Long> dateMinNextPerspectiveCommit = new HashMap<IPerspective, Long>(
      10);

  /**
   * Inform the persister service that the perspective should be commited
   * @param perspective the perspective that changed
   */
  public void setPerspectiveChanged(IPerspective perspective) {
    synchronized (dateMinNextPerspectiveCommit) {
      dateMinNextPerspectiveCommit.put(perspective,
          (System.currentTimeMillis() + MIN_DELAY_AFTER_PERSPECTIVE_CHANGE_MS));
    }
  }

  /**
   * Inform the persister service that the history should be commited
   */
  public void setHistoryChanged() {
    historyChanged = true;
  }

  /**
   * Inform the persister service that the collection should be commited with the given urgency
   * @param urgency the urgency for the collection to be commited
   */
  public void setCollectionChanged(Urgency urgency) {
    collectionChanged.put(urgency, true);
  }

  /**
   * Inform the persister service that the radios should be commited
   */
  public void setRadiosChanged() {
    radiosChanged = true;
  }

  /**
   * Instantiates a new rating manager.
   */
  private PersistenceService() {
    // set thread name
    super("Persistence Manager Thread");
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Thread#run()
   */
  @Override
  public void run() {
    init();
    int comp = 1;
    while (!ExitService.isExiting()) {
      try {
        Thread.sleep(HEART_BEAT_MS);
        if (comp % DELAY_HIGH_URGENCY_BEATS == 0) {
          performHighUrgencyActions();
        }
        if (comp % DELAY_MEDIUM_URGENCY_BEATS == 0) {
          performMediumUrgencyActions();
        }
        if (comp % DELAY_LOW_URGENCY_BEATS == 0) {
          performLowUrgencyActions();
        }
        comp++;
      } catch (Exception e) {
        Log.error(e);
      }
    }
  }

  private void init() {
    this.lastCommitQueueCheckSum = getQueueModelChecksum();
    collectionChanged.put(Urgency.LOW, false);
    collectionChanged.put(Urgency.MEDIUM, false);
    collectionChanged.put(Urgency.HIGH, false);
    setPriority(Thread.MAX_PRIORITY);
  }

  private void performHighUrgencyActions() throws Exception {
    commitWebradiosIfRequired();
    if (collectionChanged.get(Urgency.HIGH) && !DeviceManager.getInstance().isAnyDeviceRefreshing()) {
      try {
        Collection.commit();
      } finally {
        collectionChanged.put(Urgency.HIGH, false);
      }
    }
  }

  private void performMediumUrgencyActions() throws Exception {
    // Queue
    commitQueueModelIfRequired();
    // Collection
    if (collectionChanged.get(Urgency.MEDIUM)
        && !DeviceManager.getInstance().isAnyDeviceRefreshing()) {
      try {
        Collection.commit();
      } finally {
        collectionChanged.put(Urgency.MEDIUM, false);
      }
    }
    // Perspectives
    handcommitPerspectivesIfRequired();
  }

  private void handcommitPerspectivesIfRequired() throws Exception {
    List<IPerspective> datesCopy = new ArrayList<IPerspective>(
        dateMinNextPerspectiveCommit.keySet());
    for (IPerspective perspective : datesCopy) {
      if (System.currentTimeMillis() - dateMinNextPerspectiveCommit.get(perspective) >= 0) {
        try {
          perspective.commit();
        } finally {
          synchronized (dateMinNextPerspectiveCommit) {
            dateMinNextPerspectiveCommit.remove(perspective);
          }
        }
      }
    }
  }

  private void performLowUrgencyActions() throws Exception {
    //History
    commitHistoryIfRequired();
    // Collection
    if (collectionChanged.get(Urgency.LOW) && !DeviceManager.getInstance().isAnyDeviceRefreshing()) {
      try {
        Collection.commit();
      } finally {
        collectionChanged.put(Urgency.LOW, false);
      }
    }
  }

  private void commitWebradiosIfRequired() throws IOException {
    try {
      if (radiosChanged) {
        // Commit webradios
        CustomRadiosPersistenceHelper.commit();
        PresetRadiosPersistenceHelper.commit();
      }
    } finally {
      radiosChanged = false;
    }
  }

  private void commitQueueModelIfRequired() throws IOException {
    String checksum = getQueueModelChecksum();
    if (!checksum.equals(this.lastCommitQueueCheckSum)) {
      try {
        QueueModel.commit();
      } finally {
        this.lastCommitQueueCheckSum = checksum;
      }
    }
  }

  private void commitHistoryIfRequired() throws IOException {
    if (historyChanged) {
      try {
        History.commit();
      } finally {
        historyChanged = false;
      }
    }
  }

  private String getQueueModelChecksum() {
    StringBuilder sb = new StringBuilder();
    for (StackItem item : QueueModel.getQueue()) {
      sb.append(item.getFile().getID());
    }
    // Do not use MD5Processor class here to avoid the intern() method that 
    // could create a permgen memory leak
    byte[] checksum = DigestUtils.md5(sb.toString());
    return new String(checksum);
  }

  public static PersistenceService getInstance() {
    return self;
  }
}
