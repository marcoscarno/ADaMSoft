/**
* Copyright © 2015 MS
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

import java.net.InetAddress;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import ADaMSoft.dataaccess.DictionaryReader;
import ADaMSoft.keywords.Keywords;
import ADaMSoft.procedures.Result;
import ADaMSoft.procedures.StepResult;
import ADaMSoft.supervisor.DictionaryRunner;
import ADaMSoft.supervisor.ExecuteRunner;
import ADaMSoft.supervisor.MacroStepExecutor;
import ADaMSoft.supervisor.MacroStepRunner;
import ADaMSoft.supervisor.MacroStepVoidExecutor;
import ADaMSoft.supervisor.StepsExecutor;


/**
* This class executes a step as called by the SCL language
* @author marco.scarno@gmail.com
* @date 02/09/2015
*/

public class ADaMSoftSteps extends ScriptParserUtilities
{
	Hashtable<String, Object> pr;
	String resmsg;
	Vector<String> prdict;
	/**
	*Starts the class and creates the main objects
	*/
	public ADaMSoftSteps()
	{
		resmsg="";
		pr=new Hashtable<String, Object>();
		prdict=new Vector<String>();
	}
	/**
	*Delete the actual PARAMETER values
	*/
	public void CLEARPARAMETER()
	{
		pr.clear();
		return;
	}
	/**
	*Delete the single PARAMETER value
	*/
	public void CLEARPARAMETER(String value)
	{
		value=value.toLowerCase();
		pr.remove(value);
		return;
	}
	/**
	*
	*/
	public void ADDDICTACTION(String action)
	{
		prdict.add(action);
	}
	/**
	*Add a name and a value to the actual PARAMETER
	*/
	public boolean ADDPARAMETER(String namevalue)
	{
		try
		{
			namevalue=namevalue.trim();
			if (namevalue.indexOf(" ")>0)
			{
				String name=namevalue.substring(0, namevalue.indexOf(" "));
				String value="";
				try
				{
					value=namevalue.substring(namevalue.indexOf(" ")+1);
					value=value.trim();
				}
				catch (Exception ex) {}
				name=name.trim();
				return ADDPARAMETER(name, value);
			}
			else if (namevalue.indexOf("=")>0)
			{
				String name=namevalue.substring(0, namevalue.indexOf("="));
				String value="";
				try
				{
					value=namevalue.substring(namevalue.indexOf("=")+1);
					value=value.trim();
				}
				catch (Exception ex) {}
				name=name.trim()+"=";
				return ADDPARAMETER(name, value);
			}
			else
				return ADDPARAMETER(namevalue, "ON");
		}
		catch (Exception ex)
		{
			pr.clear();
			resmsg=resmsg+"%2106% ("+namevalue+")<br>\n";
			return false;
		}
	}
	/**
	*Add a name and a value to the actual PARAMETER
	*/
	public boolean ADDPARAMETER(String name, String value)
	{
		try
		{
			name=name.trim();
			if (value!=null)
				value=value.trim();
			else
				value="";
			if (name.indexOf("=")>0)
			{
				name=name.substring(0, name.indexOf("="));
				boolean ispathandname=false;
				for (int k=0; k<(Keywords.KeywordsForPathAndName).length; k++)
				{
					String testwordpath=Keywords.KeywordsForPathAndName[k].toLowerCase();
					if ((name.toLowerCase().startsWith(testwordpath)) || (name.toLowerCase().endsWith(testwordpath)))
					{
						ispathandname=true;
						pr.put(name.toLowerCase(), value);
					}
				}
				for (int k=0; k<(Keywords.KeywordsForPath).length; k++)
				{
					String testwordpath=Keywords.KeywordsForPath[k].toLowerCase();
					if ((name.toLowerCase().startsWith(testwordpath)) || (name.toLowerCase().endsWith(testwordpath)))
					{
						ispathandname=true;
						pr.put(name.toLowerCase(), value);
					}
				}
				if (!ispathandname)
				{
					String testsettingname=value;
					String testsettingvalu=value;
					boolean dotpresent=false;
					if (value.indexOf(".")>0)
					{
						dotpresent=true;
						String[] partsetting=value.split("\\.");
						if (partsetting.length==2)
						{
							testsettingname=partsetting[0];
							testsettingvalu=partsetting[1];
						}
					}
					String settingtosearch=name;
					if ((settingtosearch.toLowerCase()).startsWith(Keywords.OUT.toLowerCase()))
						settingtosearch=Keywords.OUT.toLowerCase();
					Hashtable<String, String> testsetting=Keywords.project.getSetting(settingtosearch, testsettingname);
					if (testsetting.size()>0)
					{
						for (Enumeration<String> e = testsetting.keys() ; e.hasMoreElements() ;)
						{
							String par = e.nextElement();
							String val = testsetting.get(par);
							for(int h=0; h<(Keywords.KeywordsForSetting).length; h++)
							{
								if (name.equalsIgnoreCase(Keywords.KeywordsForSetting[h]))
									par=(name+"_"+par);
							}
							if (name.toLowerCase().startsWith(Keywords.OUT.toLowerCase()))
								par=(name+"_"+par);
							pr.put(par.toLowerCase(), val);
						}
					}
					else if (!settingtosearch.equals(Keywords.OUT.toLowerCase()))
						pr.put(settingtosearch.toLowerCase(), testsettingvalu);
					else if ((settingtosearch.equals(Keywords.OUT.toLowerCase())) && (dotpresent))
					{
						resmsg=resmsg+"%1158% ("+testsettingname+")<br>\n";
						pr.clear();
						return false;
					}
					if (dotpresent)
					{
						if (!value.equalsIgnoreCase(""))
							pr.put(name.toLowerCase(), testsettingvalu);
					}
					else
						pr.put(name.toLowerCase(), value);
				}
			}
			else
				pr.put(name.toLowerCase(), value);
			return true;
		}
		catch (Exception ex)
		{
			pr.clear();
			resmsg=resmsg+"%2106% ("+name+" "+value+")<br>\n";
			return false;
		}
	}
	/**
	*Execute the current step
	*/
	public boolean EXECUTESTEP(String name)
	{
		try
		{
			boolean extstep=false;
			boolean exportoutput=false;
			name=name.trim();
			name=name.toUpperCase();
			String typestep="";
			int steptype=0;
			if (name.indexOf(Keywords.PROC)>=0)
			{
				steptype=1;
				name=name.substring(Keywords.PROC.length());
				typestep=(Keywords.PROC.substring(0,1)).toUpperCase()+((Keywords.PROC.substring(1)).toLowerCase());
				name=typestep+(name.substring(0,1)).toUpperCase()+((name.substring(1)).toLowerCase());
			}
			else if (name.indexOf(Keywords.DOCUMENT)>=0)
			{
				steptype=2;
				name=name.substring(Keywords.DOCUMENT.length());
				typestep=(Keywords.DOCUMENT.substring(0,1)).toUpperCase()+((Keywords.DOCUMENT.substring(1)).toLowerCase());
				name=typestep+(name.substring(0,1)).toUpperCase()+((name.substring(1)).toLowerCase());
			}
			else if (name.indexOf(Keywords.TOC)>=0)
			{
				steptype=3;
				name=name.substring(Keywords.TOC.length());
				typestep=(Keywords.TOC.substring(0,1)).toUpperCase()+((Keywords.TOC.substring(1)).toLowerCase());
				name=typestep+(name.substring(0,1)).toUpperCase()+((name.substring(1)).toLowerCase());
			}
			else
			{
				steptype=4;
				name=name.substring(Keywords.EXT.length());
				typestep=(Keywords.EXT.substring(0,1)).toUpperCase()+((Keywords.EXT.substring(1)).toLowerCase());
				name=typestep+(name.substring(0,1)).toUpperCase()+((name.substring(1)).toLowerCase());
				extstep=true;
			}
			pr.put(Keywords.client_host.toLowerCase(),"LOCALHOST");
			try
			{
	    	    InetAddress addr = InetAddress.getLocalHost();
	    	    String ipaddress=addr.toString();
				pr.put(Keywords.client_host.toLowerCase(),ipaddress);
	    	}
	    	catch (Exception ex) {}
			Hashtable<String, Object> oldpr=new Hashtable<String, Object>();
			for (Enumeration<String> e = pr.keys() ; e.hasMoreElements() ;)
			{
				String par = e.nextElement();
				Object valobj=pr.get(par);
				oldpr.put(par, valobj);
				String val = (valobj).toString();
				if (par.indexOf(Keywords.viewout)>=0)
					exportoutput=true;
				for (int k=0; k<(Keywords.KeywordsForPathAndName).length; k++)
				{
					par=par.toLowerCase();
					String testwordpath=Keywords.KeywordsForPathAndName[k].toLowerCase();
					if (((par.startsWith(testwordpath)) || (par.endsWith(testwordpath))) && (par.indexOf("_")<0))
					{
						if (val.indexOf(".")<=0)
							pr.put(par.toLowerCase(), Keywords.WorkDir+val);
						else
						{
							String temppath=Keywords.project.getPath(val.substring(0, val.indexOf(".")));
							if (temppath==null)
							{
								resmsg=resmsg+"%1062% ("+par+")<br>\n";
								pr.clear();
								return false;
							}
							if (temppath.equalsIgnoreCase(""))
							{
								resmsg=resmsg+"%61% ("+val.substring(0, val.indexOf("."))+")<br>\n";
								pr.clear();
								return false;
							}
							pr.put(par.toLowerCase(), temppath+(val.substring(val.indexOf(".")+1)));
						}
					}
					if (((par.startsWith(testwordpath)) || (par.endsWith(testwordpath))) && (par.indexOf("_")>0))
					{
						String temppath=Keywords.project.getPath(val);
						if (temppath.equalsIgnoreCase(""))
						{
							resmsg=resmsg+"%61% ("+val+")<br>\n";
							pr.clear();
							return false;
						}
						pr.put(par.toLowerCase(), temppath);
					}
				}
				for (int k=0; k<(Keywords.KeywordsForPath).length; k++)
				{
					par=par.toLowerCase();
					String testwordpath=Keywords.KeywordsForPath[k].toLowerCase();
					if ((par.startsWith(testwordpath)) || (par.endsWith(testwordpath)))
					{
						String temppath=Keywords.project.getPath(val);
						if (temppath.equalsIgnoreCase(""))
						{
							resmsg=resmsg+"%61% ("+val+")<br>\n";
							pr.clear();
							return false;
						}
						pr.put(par.toLowerCase(), temppath);
					}
				}
			}
			for (Enumeration<String> e = pr.keys() ; e.hasMoreElements() ;)
			{
				String par = e.nextElement();
				String val = (pr.get(par)).toString();
				if ((par.toLowerCase()).startsWith(Keywords.var.toLowerCase()))
				{
					val=VarReplacer(val.trim());
					pr.put(par.toLowerCase(), val);
				}
				if (par.startsWith(Keywords.dict))
				{
					DictionaryReader dr=new DictionaryReader(val);
					if (!dr.getmessageDictionaryReader().equals(""))
					{
						resmsg=resmsg+dr.getmessageDictionaryReader();
						pr.clear();
						return false;
					}
					pr.put(par.toLowerCase(), dr);
				}
				if (par.equalsIgnoreCase(Keywords.decryptwith))
					System.setProperty(Keywords.docpwd,val);
				if (par.equalsIgnoreCase(Keywords.docfile))
				{
					try
					{
						String passPhrase=(pr.get(Keywords.encryptwith)).toString();
						if (passPhrase==null)
						{
							ADaMSdoc adamsdoc=new ADaMSdoc(val);
							pr.put(Keywords.document, adamsdoc);
						}
						else
						{
							pr.put(Keywords.encryptwith, "");
							ADaMSdoc adamsdoc=new ADaMSdoc(val, passPhrase);
							pr.put(Keywords.document, adamsdoc);
						}
						pr.put(par.toLowerCase(), val);
					}
					catch (Exception ex)
					{
						resmsg=resmsg+"%64% ("+val+")<br>\n";
						pr.clear();
						return false;
					}
				}
			}
			StepsExecutor se=new StepsExecutor();
			Result executionresult=se.ExecuteStep(extstep, pr, name);
			resmsg=resmsg+se.getresmsg();
			if (executionresult==null)
				return false;
			if (!executionresult.isCorrect())
			{
				Keywords.laststepstate=false;
				Keywords.laststepmessage=executionresult.getMessage()+"<br>\n";
				resmsg=resmsg+executionresult.getMessage()+"<br>\n";
				pr.clear();
				System.gc();
				return false;
			}

			Vector<StepResult> results=executionresult.getResults();
			if (results==null)
				resmsg=resmsg+executionresult.getMessage();
			if (results!=null)
			{
				for (int i=0; i<results.size(); i++)
				{
					if ((steptype!=2) && (exportoutput))
						results.get(i).exportOutput();
					String msg=results.get(i).action();
					if (msg.length()>=2)
						resmsg=resmsg+(msg.substring(2)).trim()+"<br>\n";
					if (msg.startsWith("0"))
					{
						pr.clear();
						System.gc();
						return false;
					}
				}
			}
			System.gc();
			pr.clear();
			for (Enumeration<String> e = oldpr.keys() ; e.hasMoreElements() ;)
			{
				String par = e.nextElement();
				Object valobj=oldpr.get(par);
				pr.put(par, valobj);
			}
			oldpr.clear();
			return true;
		}
		catch (Exception ex)
		{
			pr.clear();
			resmsg=resmsg+"%2107%<br>\n";
			return false;
		}
	}
	/**
	*Return the result message
	*/
	public String getresmsg()
	{
		String retresmsg=resmsg;
		resmsg="";
		return retresmsg;
	}
	/**
	*Execute the current dictinary step
	*/
	public boolean EXECUTEDICTIONARY(String indict, String outdict)
	{
		try
		{
			String tempprdict=Keywords.DICTIONARY+" "+Keywords.dict+"="+indict;
			if (!outdict.equals(""))
				tempprdict=tempprdict+" "+Keywords.outdict+"="+outdict;
			prdict.add(tempprdict);
			DictionaryRunner drun=new DictionaryRunner(prdict);
			boolean resdr=drun.getError();
			String msgdict=drun.getMessage();
			resmsg=resmsg+msgdict+"<br>\n";
			prdict.clear();
			if (!resdr)
				return true;
			else
				return false;
		}
		catch (Exception ex)
		{
			prdict.clear();
			resmsg=resmsg+"%2107%<br>\n";
			return false;
		}
	}
	/**
	*Execute the current dictinary step
	*/
	public boolean EXECUTEDICTIONARY(String indict)
	{
		return EXECUTEDICTIONARY(indict,"");
	}
	/**
	*Execute a MACROSTEP
	*/
	public boolean EXECUTEMACROSTEP(String msname)
	{
		MacroStepExecutor mse=new MacroStepExecutor(Keywords.EXEMACROSTEP+" "+msname);
		try
		{
			boolean resmse=mse.getError();
			resmsg=resmsg+mse.getMessage();
			if (resmse)
				return false;
			else
				return true;
		}
		catch (Exception ex)
		{
			resmsg=resmsg+"%2214%<br>\n";
			return false;
		}
	}
	/**
	*Execute an External ADaMSoft Script file
	*/
	public boolean EXECUTECMDFILE(String fname)
	{
		try
		{
			if (!fname.endsWith(Keywords.ScriptExtension))
				fname=fname+Keywords.ScriptExtension;
		}
		catch (Exception e)
		{
			resmsg=resmsg+Keywords.Language.getMessage(2683)+"<br>\n"+e.toString()+"<br>\n";
			return false;
		}
		ExecuteRunner er=new ExecuteRunner(0, fname);
		boolean resultcmdfile=er.getError();
		if (resultcmdfile)
			return false;
		return true;
	}
	/**
	*Execute an External ADaMSoft Script file with the void option
	*/
	public void EXECUTEVOIDCMDFILE(String fname)
	{
		try
		{
			if (!fname.endsWith(Keywords.ScriptExtension))
				fname=fname+Keywords.ScriptExtension;
		}
		catch (Exception e)
		{
			resmsg=resmsg+Keywords.Language.getMessage(2683)+"<br>\n"+e.toString()+"<br>\n";
			return;
		}
		ExecuteRunner er=new ExecuteRunner(0, fname);
		er.getError();
		return;
	}
	/**
	*Execute in batch an External ADaMSoft Script file with the void option
	*/
	public void EXECUTEVOIDBATCHCMDFILE(String fname)
	{
		try
		{
			if (!fname.endsWith(Keywords.ScriptExtension))
				fname=fname+Keywords.ScriptExtension;
		}
		catch (Exception e)
		{
			resmsg=resmsg+Keywords.Language.getMessage(2683)+"<br>\n"+e.toString()+"<br>\n";
			return;
		}
		ExecuteVoidCMD evcmd=new ExecuteVoidCMD(fname);
		evcmd.start();
		return;
	}
	/**
	*Execute a MACROSTEP with the batch and void option
	*/
	public void EXECUTEVOIDBATCHMACROSTEP(String msname)
	{
		ExecuteVoidMS evs=new ExecuteVoidMS(msname);
		evs.start();
	}
	/**
	*Execute a MACROSTEP with the void option
	*/
	public void EXECUTEVOIDMACROSTEP(String msname)
	{
		MacroStepVoidExecutor mse=new MacroStepVoidExecutor();
		try
		{
			mse.getresult(Keywords.EXEMACROSTEP+" "+msname);
		}
		catch (Exception ex){}
	}
	/**
	*Adds a MACROSTEP
	*/
	public boolean ADDMACROSTEP(String msname, String steps)
	{
		try
		{
			String tempcode=Keywords.MACROSTEP+" "+msname+";"+steps;
			int prescom=tempcode.indexOf("/*");
			try
			{
				while (prescom>=0)
				{
					int posend=tempcode.indexOf("*/");
					if (posend<0)
					{
						resmsg=resmsg+Keywords.Language.getMessage(2110)+"\n";
						return false;
					}
					tempcode=tempcode.substring(0, prescom)+tempcode.substring(posend+2);
					prescom=tempcode.indexOf("/*");
				}
			}
			catch (Exception e)
			{
				resmsg=resmsg+Keywords.Language.getMessage(2110)+"\n";
				return false;
			}
			String[] scriptparts=tempcode.split(";");
			Vector<String> step=new Vector<String>();
			for (int s=0; s<scriptparts.length; s++)
			{
				scriptparts[s]=scriptparts[s].trim();
				if (!scriptparts[s].equals(""))
					step.add(scriptparts[s]);
			}
			MacroStepRunner msr=new MacroStepRunner(step);
			boolean resmse=msr.getError();
			resmsg=resmsg+msr.getMessage();
			if (resmse)
				return false;
			else
				return true;
		}
		catch (Exception ex)
		{
			resmsg=resmsg+"%2322%\n";
			return false;
		}
	}
	class ExecuteVoidMS extends Thread
	{
		String msname;
		public ExecuteVoidMS(String msname)
		{
			this.msname = msname;
		}
		public void run() throws NullPointerException
		{
			MacroStepVoidExecutor mse=new MacroStepVoidExecutor();
			try
			{
				mse.getresult(Keywords.EXEMACROSTEP+" "+msname);
			}
			catch (Exception ex){}
		}
	}
	class ExecuteVoidCMD extends Thread
	{
		String fname;
		public ExecuteVoidCMD(String fname)
		{
			this.fname = fname;
		}
		public void run() throws NullPointerException
		{
			new ExecuteRunner(0, fname);
		}
	}
}
