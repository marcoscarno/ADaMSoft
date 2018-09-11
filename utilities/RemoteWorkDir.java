/**
* Copyright (c) MS
*
* This program is free software; you can redistribute it and/or
* modify it under the terms of the GNU General Public License
* as published by the Free Software Foundation; either version 2
* of the License, or (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program; if not, write to the Free Software
* Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/

package ADaMSoft.utilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

import java.util.Vector;

import ADaMSoft.keywords.Keywords;

/**
* This class stores the path of the remote working directory
* @author marco.scarno@gmail.com
* @date 07/09/2015
*/
public class RemoteWorkDir
{
	/**
	*Gets the path of the remote working directory
	*/
	public static String getWorkDir(String appServer, String appServerPort)
	{
		if (appServerPort==null)
			appServerPort=System.getProperty(Keywords.ServerPortListening);
		String dirWork="";
		String fileRemoteDir=System.getProperty(Keywords.WorkDir)+"remote.workdir";
		String encodingLocale=(System.getProperty( "file.encoding" )).toString();
		System.setProperty("file.encoding", encodingLocale);
		boolean existsRemoteDir = (new File(fileRemoteDir)).exists();
		if (!existsRemoteDir)
			return dirWork;
		if (existsRemoteDir)
		{
			try
			{
				ObjectInputStream in = new ObjectInputStream(new FileInputStream(fileRemoteDir));
				GenericContainerForRemoteDir temp=(GenericContainerForRemoteDir)in.readObject();
				in.close();
				Vector<String> tempv=new Vector<String>();
				tempv.add(0,appServer);
				tempv.add(1,appServerPort);
				dirWork=temp.get(tempv);
				if (dirWork==null)
					dirWork="";
			}
			catch (Exception e) {}
		}
		return dirWork;
	}
	/**
	*Sets the remote working directory
	*/
	public static void setWorkDir(String appServer, String appServerPort, String remoteworkdir)
	{
		String fileRemoteDir=System.getProperty(Keywords.WorkDir)+"remote.workdir";
		String encodingLocale=(System.getProperty( "file.encoding" )).toString();
		System.setProperty("file.encoding", encodingLocale);
		boolean existsRemoteDir = (new File(fileRemoteDir)).exists();
		GenericContainerForRemoteDir temp=null;
		if (!existsRemoteDir)
			temp=new GenericContainerForRemoteDir();
		if (existsRemoteDir)
		{
			try
			{
				ObjectInputStream in = new ObjectInputStream(new FileInputStream(fileRemoteDir));
				temp=(GenericContainerForRemoteDir)in.readObject();
				in.close();
			}
			catch (Exception e) {}
		}
		try
		{
			Vector<String> tempv=new Vector<String>();
			tempv.add(0,appServer);
			tempv.add(1,appServerPort);
			temp.put(tempv, remoteworkdir);
			ObjectOutput out = new ObjectOutputStream(new FileOutputStream(fileRemoteDir));
			out.writeObject(temp);
			out.close();
		}
		catch (Exception e) {}
	}
}
