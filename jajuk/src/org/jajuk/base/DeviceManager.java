/*
 *  Jajuk
 *  Copyright (C) 2003 bflorat
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

import org.jajuk.i18n.Messages;
import org.jajuk.ui.ObservationManager;
import org.jajuk.util.MD5Processor;

/**
 *  Convenient class to manage devices
 * @Author    bflorat
 * @created    17 oct. 2003
 */
public class DeviceManager implements ITechnicalStrings{
	/**Device collection**/
	static ArrayList alDevices = new ArrayList(100);
	/**Device ids*/
	static ArrayList alDeviceIds = new ArrayList(100);
	/**Supported device types names*/
	static private ArrayList alDevicesTypes = new ArrayList(10);
	
	/**
	 * No constructor available, only static access
	 */
	private DeviceManager() {
		super();
	}

	/**
	 * Register a device
	 *@param sName
	 *@return device 
	 */
	public static synchronized Device  registerDevice(String sName,int iDeviceType,String sUrl,String sMountPoint) {
		String sId = MD5Processor.hash(sUrl+sName+iDeviceType);
		return registerDevice(sId,sName,iDeviceType,sUrl,sMountPoint);
	}
	
	/**
	 * Register a device with a known id
	 *@param sName
	 *@return device 
	 */
	public static synchronized Device  registerDevice(String sId,String sName,int iDeviceType,String sUrl,String sMountPoint) {
		//check none device already has this name
		Iterator it = alDevices.iterator();
		while (it.hasNext()){
			Device device = (Device)it.next();
			if ( sName.equals(device.getName())){
				return null;
			}
		}
		Device device = new Device(sId,sName,iDeviceType,sUrl,sMountPoint);
		alDeviceIds.add(sId);
		alDevices.add(device);
		return device;
	}
	
	/**
	 * Register a device type
	 * @param sDeviceType
	 */
	public static void registerDeviceType(String sDeviceType){
	    alDevicesTypes.add(sDeviceType);
	}
	
	/**
	 * @return number of registered devices
	 */
	public static int getDeviceTypesNumber(){
	    return alDevicesTypes.size();
	}
	
	/**
	 * @return Device types iteration
	 */
	public static Iterator getDeviceTypes(){
	    return alDevicesTypes.iterator();
	}
	
	/**
	 * Get a device type name for a given index
	 * @param index
	 * @return device name for a given index
	 */
	public static String getDeviceType(int index){
	    return (String)alDevicesTypes.get(index);
	}
	
	
	/**Return all registred devices*/
	public static synchronized ArrayList getDevices() {
		return alDevices;
	}
	
	/**
	 * Return device by id
	 * @param sName
	 * @return
	 */
	public static synchronized Device getDevice(String sId) {
		Device device = null;
		int index = alDeviceIds.indexOf(sId);
		if (index != -1){
			device = (Device) alDevices.get(index);
		}
		return device;
	}
	
	
	
	/**
	 * Remove a device
	 * @param device
	 */
	public static synchronized void removeDevice(Device device){
		//if device is refreshing or synchronizing, just leave
		if (device.isSynchronizing() || device.isRefreshing()){
			Messages.showErrorMessage("013"); //$NON-NLS-1$
			return;
		}
		//check if device can be unmounted
		if (!FIFO.canUnmount(device)){
			Messages.showErrorMessage("121"); //$NON-NLS-1$
			return;
		}
		//if it is mounted, try to unmount it
		if (device.isMounted()){ 
			try{
				device.unmount();
			}
			catch(Exception e){
				Messages.showErrorMessage("013"); //$NON-NLS-1$
				return;
			}
		}
		alDevices.remove(device);
		alDeviceIds.remove(device.getId());
		DirectoryManager.cleanDevice(device.getId());
		FileManager.cleanDevice(device.getId());
		PlaylistFileManager.cleanDevice(device.getId());
		//	Clean the collection up
		org.jajuk.base.Collection.cleanup();
		//refresh views
		ObservationManager.notify(EVENT_DEVICE_REFRESH);
	}
	
	/**
	 * @return whether any device is currently refreshing
	 */
	public static boolean isAnyDeviceRefreshing(){
		boolean bOut = false;
		Iterator it = DeviceManager.getDevices().iterator();
		while ( it.hasNext()){
			Device device = (Device)it.next();
			if ( device.isRefreshing()){
				bOut = true;
				break;
			}
		}
		return bOut;
	}
	
	

}
