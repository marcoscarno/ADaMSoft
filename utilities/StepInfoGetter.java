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
import java.net.URI;
import java.net.URL;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeMap;
import java.util.Vector;
import java.util.Hashtable;
import java.util.jar.JarFile;
import java.util.jar.JarEntry;

import ADaMSoft.keywords.Keywords;
import ADaMSoft.procedures.RunStep;

/**
* Returns all the names of the installed executable steps
* @author marco.scarno@gmail.com
* @date 07/09/2015
*/
public class StepInfoGetter
{
	public StepInfoGetter() {}
	Hashtable<String, Vector<String>> listmessages=new Hashtable<String,  Vector<String>>();
	public TreeMap<String, TreeMap<String, String>> getStepInfo(String type)
	{
		listmessages.clear();
		TreeMap<String, TreeMap<String, String>> step=new TreeMap<String, TreeMap<String, String>>();
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
			File file = new File(fpath);
			JarFile jf = new JarFile(file);
			for(Enumeration<JarEntry> en=jf.entries(); en.hasMoreElements(); )
			{
				String name = (en.nextElement()).toString();
				name=name.substring(name.lastIndexOf("/")+1);
				if((name.endsWith(".class")) && (name.indexOf("$")<=0) && (name.toUpperCase().startsWith(type.toUpperCase())) )
				{
					try
					{
						String proc_parameters="";
						name=name.substring(0,name.indexOf(".class"));
						Class<?> classCommand = Class.forName(Keywords.SoftwareName+".procedures."+name);
						RunStep comm = (RunStep) classCommand.newInstance();
						LinkedList<?> parameters = comm.getparameters();
						Iterator<?> i = parameters.iterator();
						while(i.hasNext())
						{
							GetRequiredParameters par = (GetRequiredParameters)i.next();
							String par_type=par.getType();
							int par_label=par.getLabel();
							proc_parameters=proc_parameters+Keywords.Language.getMessage(par_label)+" ";
							if (!par_type.equals("listsingle=530_NULL,531_all,532_codelabel,533_missing"))
							{
								if (par_type.indexOf("=")>0)
								{
									try
									{
										String[] partp=par_type.split("=");
										if (partp[1].indexOf(",")>0)
										{
											String[] partpp=partp[1].split(",");
											for (int h=0; h<partpp.length; h++)
											{
												String[] ms=partpp[h].split("_");
												proc_parameters=proc_parameters+Keywords.Language.getMessage(Integer.parseInt(ms[0]))+" ";
											}
										}
									}
									catch (Exception ep){}
								}
							}
						}
						String[] info = comm.getstepinfo();
						String stepgroup=Keywords.Language.getMessage(info[0]);
						String stepname =info[1];
						TreeMap<String, String> tempstep=step.get(stepgroup);
						if (tempstep==null)
						{
							tempstep=new TreeMap<String, String>();
							tempstep.put(stepname, name);
						}
						else
						{
							tempstep.put(stepname, name);
						}
						step.put(stepgroup, tempstep);
						proc_parameters=Keywords.Language.getMessage(Integer.parseInt(stepname))+" "+proc_parameters;
						proc_parameters=stepgroup+" "+proc_parameters;
						Vector<String> ip=new Vector<String>();
						ip.add(stepgroup);
						ip.add(Keywords.Language.getMessage(Integer.parseInt(stepname)));
						ip.add(name);
						listmessages.put(proc_parameters, ip);
					}
					catch (Exception e) 
					{
					}
				}
			}
			jf.close();
		}
		catch (Exception exc) {}
		return step;
	}
	public Hashtable<String,  Vector<String>> getTexts()
	{
		return listmessages;
	}
}
