/**
* Copyright © 2015 ADaMSoft
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

package ADaMSoft.supervisor;

import java.io.File;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.Vector;

import ADaMSoft.keywords.Keywords;

/**
* Executes the path steps
* @author marco.scarno@gmail.com
* @date 02/09/2015
*/
public class PathRunner
{
	String message;
	boolean steperror;
	public PathRunner (String actualstatement)
	{
		message="";
		steperror=false;
		actualstatement=actualstatement.trim();
		String valpath="";
		try
		{
			valpath=actualstatement.substring(actualstatement.indexOf(" "));
			valpath=valpath.trim();
		}
		catch (Exception ex)
		{
			message=Keywords.Language.getMessage(20);
			steperror=true;
			return;
		}
		int type=0;
		String [] infopath;
		String pathname="";
		String pathpath="";
		if (valpath.equalsIgnoreCase(Keywords.clear))
			type=1;
		else if (valpath.indexOf("=")>0)
		{
			infopath=valpath.split("=");
			if (infopath.length!=2)
			{
				message=Keywords.Language.getMessage(20);
				steperror=true;
				return;
			}
			pathname=infopath[0].toLowerCase();
			pathname=pathname.trim();
			if (pathname.equalsIgnoreCase(Keywords.work))
			{
				Vector<String> test=Keywords.project.getPaths();
				if (test.size()==0 && !pathpath.equalsIgnoreCase(Keywords.clear)) Keywords.project.addPath(pathname, pathpath);
				else
				{
					message=Keywords.Language.getMessage(25);
					steperror=true;
					return;
				}
			}
			if (infopath[1].equalsIgnoreCase(Keywords.clear))
				type=2;
			else
				pathpath=infopath[1].trim();
		}
		else
		{
			message=Keywords.Language.getMessage(20);
			steperror=true;
			return;
		}
		if (type>0)
		{
			TreeMap<String, String> definedpath=Keywords.project.getNamesAndPaths();
			if (definedpath.size()==0)
			{
				message=Keywords.Language.getMessage(21);
				steperror=true;
				return;
			}
			if (type==2)
			{
				boolean nameexist=false;
				for (Iterator<String> it = definedpath.keySet().iterator(); it.hasNext();) 
				{
					String actualpath = it.next();
					if (actualpath.equalsIgnoreCase(pathname))
						nameexist=true;
				}
				if (!nameexist)
				{
					message=Keywords.Language.getMessage(22);
					steperror=true;
					return;
				}
			}
		}
		if (type==0)
		{
			if (!(pathpath.toUpperCase()).startsWith("HTTP"))
			{
				File tempdir=new File(pathpath);
				pathpath=tempdir.getPath();
				boolean exists = (new File(pathpath)).exists();
				if (!exists)
					message="<i>"+Keywords.Language.getMessage(23)+"</i><br>";
			}
			try
			{
				pathpath=pathpath.replaceAll("\\\\","/");
			}
			catch (Exception fs){}
			if ((!pathpath.endsWith(System.getProperty("file.separator"))) && (!(pathpath.toUpperCase()).startsWith("HTTP")))
				pathpath=pathpath+System.getProperty("file.separator");
			if ((!pathpath.endsWith(System.getProperty("file.separator"))) && ((pathpath.toUpperCase()).startsWith("HTTP")))
				pathpath=pathpath+"/";
		}

		if (type==0)
			Keywords.project.addPath(pathname, pathpath);
		else if (type==1)
			Keywords.project.clearPath();
		else if (type==2)
			Keywords.project.delPath(pathname);

		if (type==0)
			message=message+Keywords.Language.getMessage(28);
		if (type==1)
			message=message+Keywords.Language.getMessage(30);
		if (type==2)
			message=message+Keywords.Language.getMessage(29);
	}
	/**
	*Returns the result
	*/
	public String getMessage()
	{
		return message;
	}
	public boolean getError()
	{
		return steperror;
	}
}
