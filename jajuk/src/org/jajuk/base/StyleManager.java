/*
 *  Jajuk
 *  Copyright (C) 2003 Bertrand Florat
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

package org.jajuk.base;

import java.util.ArrayList;
import java.util.Iterator;

import org.jajuk.util.MD5Processor;

/**
 * Convenient class to manage styles
 * @author Bertrand Florat 
 * @created 17 oct. 2003
 */
public class StyleManager extends ItemManager {
    /**Self instance*/
    private static StyleManager singleton;

	/**
	 * No constructor available, only static access
	 */
	private StyleManager() {
		super();
        //register properties
        //ID
        registerProperty(new PropertyMetaInformation(XML_ID,false,true,false,false,true,String.class,null,null));
        //Name
        registerProperty(new PropertyMetaInformation(XML_NAME,false,true,true,true,true,String.class,null,null));
        //Expand
        registerProperty(new PropertyMetaInformation(XML_EXPANDED,false,false,false,false,false,Boolean.class,null,"false"));
	}

    /**
     * @return singleton
     */
    public static StyleManager getInstance(){
      if (singleton == null){
          singleton = new StyleManager();
      }
        return singleton;
    }

	/**
	 * Register a style
	 * 
	 * @param sName
	 */
	public  synchronized Style registerStyle(String sName) {
		String sId = MD5Processor.hash(sName.trim().toLowerCase());
		return registerStyle(sId, sName);
	}

	/**
	 * Register a style with a known id
	 * 
	 * @param sName
	 */
	public  synchronized Style registerStyle(String sId, String sName) {
		if (hmItems.containsKey(sId)) {
			return (Style) hmItems.get(sId);
		}
		Style style = new Style(sId, sName);
		hmItems.put(sId, style);
        restorePropertiesAfterRefresh(style);
		return style;
	}
    
      /**
     * Change the item name
     * @param old
     * @param sNewName
     * @return new item
     */
    public synchronized Style changeStyleName(Style old,String sNewName){
        Style newItem = registerStyle(sNewName);
        ArrayList alTracks = new ArrayList(TrackManager.getInstance().getItems()); //we need to create a new list to avoid concurrent exceptions
        Iterator it = alTracks.iterator();
        while (it.hasNext()){
            Track track = (Track)it.next();
            if (track.getStyle().equals(old)){
                TrackManager.getInstance().changeTrackStyle(track,sNewName);
            }
        }
        cleanup();//remove useless items if no more tracks use it
        return newItem;
    }
		
	/**
	 * Format the Style name to be normalized :
	 * <p>
	 * -no underscores or other non-ascii characters
	 * <p>
	 * -no spaces at the begin and the end
	 * <p>
	 * -All in upper case
	 * <p>
	 * exemple: "ROCK"
	 * 
	 * @param sName
	 * @return
	 */
	private static synchronized String format(String sName) {
		String sOut;
		sOut = sName.trim(); //supress spaces at the begin and the end
		sOut.replace('-', ' '); //move - to space
		sOut.replace('_', ' '); //move _ to space
		sOut = sOut.toUpperCase();
		return sOut;
	}
	
 /* (non-Javadoc)
     * @see org.jajuk.base.ItemManager#getIdentifier()
     */
    public String getIdentifier() {
        return XML_STYLES;
    }

}