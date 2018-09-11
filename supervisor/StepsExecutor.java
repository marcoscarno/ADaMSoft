/**
* Copyright © 2006-2013 CINECA
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
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLClassLoader;
import java.rmi.Naming;
import java.util.Hashtable;

import ADaMSoft.keywords.Keywords;
import ADaMSoft.procedures.Result;
import ADaMSoft.procedures.RunStep;
import ADaMSoft.procedures.Step;


/**
* This class permits to execute a step
* @author mscarno@cineca.it;
* @version 1.0.0, rev.: 19/09/13 by marco
*/

public class StepsExecutor
{
	String resmsg;
	private ClassLoader classtoexecute;
	/**
	*Starts the class and initializes the return messages
	*/
	public StepsExecutor()
	{
		resmsg="";
	}
	/**
	*Execute the current step
	*/
	public Result ExecuteStep(boolean extstep, Hashtable<String, Object> parameter, String stepname)
	{
		Keywords.percentage_total=0;
		Keywords.percentage_done=0;
		Keywords.currentExecutedStep=stepname;
		Result executionresult=null;
		if (!extstep)
		{
			parameter.put(Keywords.WorkDir,(System.getProperty(Keywords.WorkDir)));
			try
			{
				Class<?> classCommand = Class.forName(Keywords.SoftwareName+".procedures."+stepname);
        		RunStep comm = (RunStep) classCommand.newInstance();
				executionresult = comm.executionresult(parameter);
				comm = null;
				classCommand=null;
			}
			catch (InstantiationException In)
			{
				resmsg=Keywords.Language.getMessage(65)+"<br>\n";
				return null;
			}
			catch (IllegalAccessException In)
			{
				resmsg=Keywords.Language.getMessage(65)+"<br>\n";
				return null;
			}
			catch (ClassNotFoundException In)
			{
				resmsg=Keywords.Language.getMessage(66)+" ("+stepname+")<br>\n";
				return null;
			}
			catch (OutOfMemoryError ome)
			{
				resmsg=Keywords.Language.getMessage(1776)+" ("+stepname+")<br>\n";
				return null;
			}
			catch (Exception In)
			{
				resmsg=Keywords.Language.getMessage(67)+"<br>\n";
				String testDEBUG=System.getProperty("DEBUG");
				if (!testDEBUG.equalsIgnoreCase("false"))
				{
					StringWriter SWex = new StringWriter();
					PrintWriter PWex = new PrintWriter(SWex);
					In.printStackTrace(PWex);
					resmsg=resmsg+SWex.toString()+"<br>\n";
				}
				return null;
			}
		}
		else if (extstep)
		{
			parameter.put(Keywords.WorkDir,(System.getProperty(Keywords.WorkDir)));
			Class<?> classCommand=null;
			try
			{
				classCommand = Class.forName(stepname);
        		Step comm = (Step) classCommand.newInstance();
				executionresult = comm.executionresult(parameter);
				comm = null;
				classCommand=null;
			}
			catch (ClassNotFoundException In)
			{
				try
				{
					File fileclass = new File(System.getProperty(Keywords.WorkDir));
					URL url = fileclass.toURI().toURL();
					URL[] urls = new URL[]{url};
					classtoexecute = new URLClassLoader(urls);
					classCommand = classtoexecute.loadClass(stepname);
	        		Step comm = (Step)classCommand.newInstance();
					executionresult = comm.executionresult(parameter);
					comm = null;
					classCommand=null;
					return executionresult;
				}
				catch (ClassNotFoundException Inn)
				{
					resmsg=Keywords.Language.getMessage(66)+" ("+stepname+")<br>\n";
					return null;
				}
				catch (InstantiationException Inn)
				{
					resmsg=Keywords.Language.getMessage(65)+"<br>\n";
					return null;
				}
				catch (IllegalAccessException Inn)
				{
					resmsg=Keywords.Language.getMessage(65)+"<br>\n";
					return null;
				}
				catch (OutOfMemoryError omen)
				{
					resmsg=Keywords.Language.getMessage(1776)+" ("+stepname+")<br>\n";
					return null;
				}
				catch (Exception Inn)
				{
					resmsg=Keywords.Language.getMessage(67)+"<br>\n";
					String testDEBUG=System.getProperty("DEBUG");
					if (!testDEBUG.equalsIgnoreCase("false"))
					{
						StringWriter SWex = new StringWriter();
						PrintWriter PWex = new PrintWriter(SWex);
						In.printStackTrace(PWex);
						resmsg=resmsg+SWex.toString()+"<br>\n";
					}
					return null;
				}
			}
			catch (InstantiationException In)
			{
				resmsg=Keywords.Language.getMessage(65)+"<br>\n";
				return null;
			}
			catch (IllegalAccessException In)
			{
				resmsg=Keywords.Language.getMessage(65)+"<br>\n";
				return null;
			}
			catch (OutOfMemoryError ome)
			{
				resmsg=Keywords.Language.getMessage(1776)+" ("+stepname+")<br>\n";
				return null;
			}
			catch (Exception In)
			{
				resmsg=Keywords.Language.getMessage(67)+"<br>\n";
				String testDEBUG=System.getProperty("DEBUG");
				if (!testDEBUG.equalsIgnoreCase("false"))
				{
					StringWriter SWex = new StringWriter();
					PrintWriter PWex = new PrintWriter(SWex);
					In.printStackTrace(PWex);
					resmsg=resmsg+SWex.toString()+"<br>\n";
				}
				return null;
			}
		}
		return executionresult;
	}
	/**
	*Returns the execution results
	*/
	public String getresmsg()
	{
		return resmsg;
	}
}
