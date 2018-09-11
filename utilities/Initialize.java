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

import java.io.File;
import java.io.BufferedWriter;
import java.io.FileWriter;

import java.net.URI;
import java.net.URL;

import java.text.MessageFormat;
import java.lang.Runtime;
import java.util.Date;
import java.util.Random;

import ADaMSoft.keywords.Keywords;

/**
* The initializator of ADaMSoft
* @author marco.scarno@gmail.com
* @date 08/09/2015
*/
public class Initialize
{
	String adamsoft_path="";
	String msg="";
	public Initialize()
	{
		System.setProperty("gui_error", "");
		try
		{
			URL fp =getClass().getProtectionDomain().getCodeSource().getLocation();
			URI ffp=new URI(fp.toURI().getScheme(), null, fp.toURI().getPath(), fp.toURI().getQuery(), fp.toURI().getFragment());
			adamsoft_path=ffp.getPath();
			File temp=new File(adamsoft_path);
			adamsoft_path=temp.getParent();
			try
			{
				adamsoft_path=adamsoft_path.replaceAll("\\\\","/");
				if (!adamsoft_path.endsWith(System.getProperty("file.separator")))
					adamsoft_path=adamsoft_path+System.getProperty("file.separator");
			}
			catch (Exception e) {}
			Random generator = new Random();
			int randomIndex = generator.nextInt(100);
			java.util.Date dateProcedure=new java.util.Date();
			long timeProcedure=dateProcedure.getTime();
			String gui_error=adamsoft_path+"GUI_ERROR_"+String.valueOf(timeProcedure)+String.valueOf(randomIndex);
			File fgui = new File(gui_error);
			boolean efgui=fgui.exists();
			int cycles=0;
			while (efgui && cycles<10)
			{
				cycles++;
				randomIndex = generator.nextInt(100);
				dateProcedure=new java.util.Date();
				timeProcedure=dateProcedure.getTime();
				gui_error=adamsoft_path+"GUI_ERROR_"+String.valueOf(timeProcedure)+String.valueOf(randomIndex);
				fgui = new File(gui_error);
				efgui=fgui.exists();
			}
			fgui = new File(gui_error);
			efgui=fgui.exists();
			if (!efgui)
			{
				try
				{
					BufferedWriter g_error= new BufferedWriter(new FileWriter(new File(gui_error)));
					g_error.write("ERRORS IN GUI\n");
					g_error.close();
					System.setProperty("gui_error", gui_error);
				}
				catch (Exception e){}
			}
		}
		catch (Exception f){}
	}
	public String getADaMSoft_Path()
	{
		return adamsoft_path;
	}
	public boolean create_logfile(String out_logfile)
	{
		String logfile="<i>"+Keywords.Language.getMessage(90) + "<br>\n";
		logfile=logfile+Keywords.Language.getMessage(145) + ": "+ (System.getProperty("os.name")) + " - "+ Keywords.Language.getMessage(125) + ": "+ (System.getProperty("os.version"))+"<br>\n";
		logfile=logfile+ Keywords.Language.getMessage(126) + ": "+ (System.getProperty("java.version"))+"<br>\n";
		int infototmemory= (int)((Runtime.getRuntime().maxMemory())/1024);
		logfile=logfile+Keywords.Language.getMessage(3168) + ": "+ String.valueOf(infototmemory)+"<br>\n";
		Object[] paramsdate = new Object[] { new Date(), new Date(0) };
		logfile=logfile+MessageFormat.format(Keywords.Language.getMessage(127)+ " {0}", paramsdate)+"<br>\n";
		logfile=logfile+Keywords.Language.getMessage(128) + ": "+ System.getProperty(Keywords.WorkDir)+"<br>\n";
		logfile=logfile+Keywords.Language.getMessage(2630) + "\n"+ Keywords.Language.getMessage(3056)+"<br><br></i>\n";
		try
		{
			BufferedWriter outlogfile= new BufferedWriter(new FileWriter(new File(out_logfile)));
			outlogfile.write(logfile);
			outlogfile.close();
		}
		catch (Exception e)
		{
			msg=e.toString();
			return false;
		}
		return true;
	}
	public boolean create_outfile(String out_outfile)
	{
		try
		{
			BufferedWriter outfile= new BufferedWriter(new FileWriter(new File(out_outfile)));
			outfile.write("&nbsp;");
			outfile.close();
		}
		catch (Exception e)
		{
			msg=e.toString();
			return false;
		}
		return true;
	}
	public String getMessage()
	{
		return msg;
	}
}

