/**
* Copyright (c) 2015 ADaMSoft
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

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.util.Hashtable;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

/**
* This method reads the messagges and returns the association between numbers and text
* @author marco.scarno@gmail.com
* @date 02/09/2015
*/
public class LanguageReader
{
	Hashtable<Integer, String> messages;
	/**
	*Initialize the method and reads the message file
	*/
	public LanguageReader()
	{
		messages=new Hashtable<Integer, String>();
	}
	public void readMessages()
	{
		messages.clear();
		try
		{
			URL fp = getClass().getProtectionDomain().getCodeSource().getLocation();
			URI ffp=new URI(fp.toURI().getScheme(), null, fp.toURI().getPath(), fp.toURI().getQuery(), fp.toURI().getFragment());
			String fpath=ffp.getPath();
			if (System.getProperty("execute_debug")!=null)
			{
				if (System.getProperty("execute_debug").equalsIgnoreCase("yes"))
					fpath="c:/ADaMSoft/ADaMSoft.jar";
			}
			JarFile jar = new JarFile(new File(fpath));
			ZipEntry entry = jar.getEntry("Languages/ADaMSoftMessagges_en_US.properties");
			BufferedReader in = new BufferedReader(new InputStreamReader(jar.getInputStream(entry)));
			String str="";
			while ((str = in.readLine()) != null)
			{
				if (str!=null)
				{
					if(!(str.trim()).equals(""))
					{
						String [] tempClass=str.split("=");
						try
						{
							int nmess=Integer.parseInt(tempClass[0].trim());
							String mess="";
							for (int i=1; i<tempClass.length; i++)
							{
								if (i>1)
									mess=mess+"=";
								mess = mess+tempClass[i].trim();
							}
							messages.put(new Integer(nmess), mess);
						}
						catch (Exception exn) {}
					}
				}
			}
			in.close();
			jar.close();
		}
		catch (Exception ex) {}
	}
	public String getMessage(int message)
	{
		String mess=messages.get(new Integer(message));
		if (mess==null)
			mess="Message not found or error reading messagges\n";
		return mess;
	}
	public String getMessage(String message)
	{
		try
		{
			int code=Integer.parseInt(message);
			String mess=messages.get(new Integer(code));
			if (mess==null)
				mess="Message not found or error reading messagges\n";
			return mess;
		}
		catch (Exception e)
		{
			return "Message not found or error reading messagges\n";
		}
	}
		
}
