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

package ADaMSoft.utilities;

import java.io.*;
import java.net.*;
import java.util.*;

/**
* This class verifies if the current URL can be accessed according to what in the Robots.txt file
* @author marco.scarno@gmail.com
* @date 07/09/2015
*/
public class RoboSafe
{
	/**
	* This class returns true if the received URL can be accessed
	*/
	public static boolean verify(String url)
	{
		try
		{
			URL urltest = new URL(url);
			File fileurl = new File(urltest.getPath());
			String parentPath = fileurl.getParent( );
			URL parentUrl = new URL( urltest.getProtocol( ), urltest.getHost( ), urltest.getPort( ), parentPath );
			String strRobot = (parentUrl.toString()).replaceAll("\\\\","/");
			if (!strRobot.endsWith("/")) strRobot = strRobot + "/";
			strRobot = strRobot + "robots.txt";
			URL urlRobot = new URL(strRobot);
			HttpURLConnection huc =  (HttpURLConnection)  urlRobot.openConnection();
			huc.setRequestMethod("GET");
			HttpURLConnection.setFollowRedirects(false);
			huc.setConnectTimeout(1000);
			huc.setReadTimeout(1000);
			huc.connect();
			if (huc.getResponseCode()!=200) return true;
			String strCommands="";
			InputStream urlRobotStream = huc.getInputStream();
			byte[] b = new byte[1000];
			int numRead = urlRobotStream.read(b);
			if (numRead>0)
			{
				strCommands = new String(b, 0, numRead);
				while (numRead != -1)
				{
					numRead = urlRobotStream.read(b);
					if (numRead != -1)
					{
						String newCommands = new String(b, 0, numRead);
						strCommands += newCommands;
					}
				}
			}
			urlRobotStream.close();
			int index = 0;
			while ((index = strCommands.indexOf("Disallow:", index)) != -1)
			{
				index += "Disallow:".length();
				String strPath = strCommands.substring(index);
				StringTokenizer st = new StringTokenizer(strPath);
				if (!st.hasMoreTokens())
					break;
				String strBadPath = st.nextToken();
				if (url.indexOf(strBadPath) == 0)
				return false;
			}
			return true;
		}
		catch (Exception e)
		{
			return true;
		}
	}
}
