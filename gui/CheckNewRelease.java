/**
* Copyright (c) 2015 MS
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

package ADaMSoft.gui;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import javax.swing.JDialog;
import javax.swing.JOptionPane;

import ADaMSoft.keywords.Keywords;

/**
* This class checks if a new ADaMSoft release is on the web site
* @author marco.scarno@gmail.com
* @date 03/09/2015
*/
public class CheckNewRelease extends Thread
{
	String[] actualrelease;
	public CheckNewRelease()
	{
		actualrelease=null;
		String temp=Keywords.Language.getMessage(90);
		try
		{
			temp=temp.substring(temp.indexOf("(")+1,temp.indexOf(")"));
			actualrelease=temp.split("\\.");
		}
		catch (Exception e){}
	}
	public void run()
	{
		boolean thereisnew=false;
		if (actualrelease==null) return;
		String server=System.getProperty("server_release");
		if (server.startsWith("http"))
		{
			try
			{
				if (server.endsWith(".html"))
				{
					URL urladamsoft = new URL(server);
					BufferedReader buffReader = new BufferedReader(new InputStreamReader(urladamsoft.openStream()));
					String str;
					String content="";
					while ((str = buffReader.readLine()) != null)
					{
						content +=str;
					}
					buffReader.close();
					String release=content.substring(content.indexOf("RELEASE="));
					release=release.substring("RELEASE=".length(), release.indexOf("<"));
					String[] newrelease=release.split("\\.");
					int dimension=actualrelease.length;
					if (newrelease.length<dimension) dimension=newrelease.length;
					for (int i=0; i<dimension; i++)
					{
						try
						{
							double ti=Double.parseDouble(actualrelease[i]);
							double ts=Double.parseDouble(newrelease[i]);
							if (ts>ti)
							{
								thereisnew=true;
								break;
							}
							if (ti>ts) break;
						}
						catch (Exception e){}
					}
					if (thereisnew)
					{
						JOptionPane pane = new JOptionPane(Keywords.Language.getMessage(2092)+" ("+release+")");
						JDialog dialog = pane.createDialog(MainGUI.desktop, Keywords.Language.getMessage(2094)+" "+server);
						dialog.validate();
						dialog.setVisible(true);
						JOptionPane.showMessageDialog(MainGUI.desktop, "<html>To install the new release download from<br><b>https://adamsoft.sourceforge.net</b><br>the new <i>ADAMSOFT_JRE#</i> jar file and overwrite the old one.<br>Note that <i>#</i> identifies your current release of JAVA RE</html>",
						"Install the new release", JOptionPane.INFORMATION_MESSAGE);
					}
				}
			}
			catch (Exception e){}
		}
	}
}
