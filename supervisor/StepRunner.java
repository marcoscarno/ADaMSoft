/**
* Copyright (c) ADaMSoft
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

import java.net.InetAddress;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import ADaMSoft.dataaccess.DictionaryReader;
import ADaMSoft.keywords.Keywords;
import ADaMSoft.procedures.Result;
import ADaMSoft.procedures.StepResult;
import ADaMSoft.utilities.ADaMSdoc;
import ADaMSoft.utilities.ScriptParserUtilities;

/**
* Executes a step (PROC, ADAMSODC or TOC)
* @author marco.scarno@gmail.com
* @date 12/05/2017
*/
public class StepRunner extends ScriptParserUtilities
{
	String message;
	boolean steperror;
	public StepRunner(Vector<?> KeywordValue, int steptype)
	{
		message="";
		steperror=false;
		boolean extstep=false;
		String typestep="";
		if (steptype==1)
			typestep=(Keywords.PROC.substring(0,1)).toUpperCase()+((Keywords.PROC.substring(1)).toLowerCase());
		if (steptype==2)
			typestep=(Keywords.DOCUMENT.substring(0,1)).toUpperCase()+((Keywords.DOCUMENT.substring(1)).toLowerCase());
		if (steptype==3)
			typestep=(Keywords.TOC.substring(0,1)).toUpperCase()+((Keywords.TOC.substring(1)).toLowerCase());
		if (steptype==4)
		{
			extstep=true;
			typestep=(Keywords.EXT.substring(0,1)).toUpperCase()+((Keywords.EXT.substring(1)).toLowerCase());
		}
		boolean exportoutput=false;
		String stepname="";
		Hashtable<String, String> tempparameter=new Hashtable<String, String>();
		Vector<String> checksameoutnames=new Vector<String>();
		for (int i=0; i<KeywordValue.size(); i++)
		{
			String actualvalue=(String)KeywordValue.get(i);
			actualvalue=actualvalue.trim();
			actualvalue=MultipleSpacesReplacer(actualvalue);
			if ((actualvalue.toLowerCase()).startsWith(typestep.toLowerCase()))
			{
				String tempstringforout=actualvalue.toLowerCase();
				int posviewout=tempstringforout.indexOf(Keywords.viewout);
				if (posviewout>0)
				{
					exportoutput=true;
					tempstringforout=actualvalue.substring(0, posviewout);
					try
					{
						actualvalue=actualvalue.substring(posviewout+Keywords.viewout.length());
					}
					catch (Exception e) {}
					actualvalue=tempstringforout.trim()+" "+actualvalue.trim();
				}
				String[] infoproc=(SpacesBetweenEqualReplacer(actualvalue)).split(" ");
				if (infoproc.length<2)
				{
					message=Keywords.Language.getMessage(59)+"<br>\n";
					steperror=true;
					return;
				}
				stepname=typestep+(infoproc[1].substring(0,1)).toUpperCase()+((infoproc[1].substring(1)).toLowerCase());
				for (int j=2; j<infoproc.length; j++)
				{
					if (infoproc[j].indexOf("=")<=0)
						tempparameter.put(infoproc[j].toLowerCase(),"");
					else
					{
						String[] partinfoproc=infoproc[j].split("=");
						if (partinfoproc.length!=2)
						{
							message=Keywords.Language.getMessage(59)+"<br>\n";
							steperror=true;
							return;
						}
						boolean ispathandname=false;
						for (int k=0; k<(Keywords.KeywordsForPathAndName).length; k++)
						{
							String testwordpath=Keywords.KeywordsForPathAndName[k].toLowerCase();
							if ((partinfoproc[0].toLowerCase().startsWith(testwordpath)) || (partinfoproc[0].toLowerCase().endsWith(testwordpath)))
							{
								ispathandname=true;
								tempparameter.put(partinfoproc[0].toLowerCase(), partinfoproc[1]);
							}
						}
						for (int k=0; k<(Keywords.KeywordsForPath).length; k++)
						{
							String testwordpath=Keywords.KeywordsForPath[k].toLowerCase();
							if ((partinfoproc[0].toLowerCase().startsWith(testwordpath)) || (partinfoproc[0].toLowerCase().endsWith(testwordpath)))
							{
								ispathandname=true;
								tempparameter.put(partinfoproc[0].toLowerCase(), partinfoproc[1]);
							}
						}
						if (!ispathandname)
						{
							String testisoutset="";
							String testsettingname=partinfoproc[1];
							String testname="";
							if (partinfoproc[1].indexOf(".")>0)
							{
								String[] partsetting=partinfoproc[1].split("\\.");
								if (partsetting.length==2)
								{
									testsettingname=partsetting[0];
									testname=partsetting[1];
								}
								testisoutset=testsettingname;
							}
							String setttingtosearch=partinfoproc[0];
							if ((setttingtosearch.toLowerCase()).startsWith(Keywords.OUT.toLowerCase()))
							{
								checksameoutnames.add(partinfoproc[1]);
								setttingtosearch=Keywords.OUT.toLowerCase();
							}
							else
								testisoutset="";
							Hashtable<String, String> testsetting=Keywords.project.getSetting(setttingtosearch, testsettingname);
							if (testsetting.size()>0)
							{
								for (Enumeration<String> e = testsetting.keys() ; e.hasMoreElements() ;)
								{
									String par = e.nextElement();
									String val = testsetting.get(par);
									for(int h=0; h<(Keywords.KeywordsForSetting).length; h++)
									{
										if (partinfoproc[0].equalsIgnoreCase(Keywords.KeywordsForSetting[h]))
											par=(partinfoproc[0]+"_"+par);
									}
									if (partinfoproc[0].toLowerCase().startsWith(Keywords.OUT.toLowerCase()))
										par=(partinfoproc[0]+"_"+par);
									tempparameter.put(par.toLowerCase(), val);
								}
							}
							else if (testisoutset.equals(""))
								tempparameter.put(partinfoproc[0].toLowerCase(), partinfoproc[1]);
							else if (!testisoutset.equals(""))
							{
								message=Keywords.Language.getMessage(1158)+" ("+testisoutset+")<br>\n";
								steperror=true;
								return;
							}
							if (!testname.equalsIgnoreCase(""))
								tempparameter.put(partinfoproc[0].toLowerCase(), testname);
						}
					}
				}
			}
			if ((!(actualvalue.toLowerCase()).startsWith(typestep.toLowerCase())) && (!actualvalue.equalsIgnoreCase(Keywords.RUN)))
			{
				String[] infoproc=actualvalue.split(" ");
				if (infoproc.length==1)
					tempparameter.put(actualvalue.toLowerCase(), "");
				if (infoproc.length>=2)
				{
					String par=infoproc[0];
					String val="";
					for (int j=1; j<infoproc.length; j++)
					{
						val=val+infoproc[j];
						if (j<infoproc.length-1)
							val=val+" ";
					}
					if (!val.startsWith("&"))
					{
						if (tempparameter.get(par.toLowerCase())!=null)
						{
							String tempval=tempparameter.get(par.toLowerCase());
							val=tempval+";"+val;
						}
						tempparameter.put(par.toLowerCase(), val);
					}
				}
			}
		}
		if (checksameoutnames.size()>1)
		{
			for (int i=0; i<checksameoutnames.size()-1; i++)
			{
				for (int j=i+1; j<checksameoutnames.size(); j++)
				{
					if (checksameoutnames.get(i).equalsIgnoreCase(checksameoutnames.get(j)))
					{
						message=Keywords.Language.getMessage(1884)+" ("+checksameoutnames.get(i)+")<br>\n";
						steperror=true;
						return;
					}

				}
			}
		}
		tempparameter.put(Keywords.client_host.toLowerCase(),"LOCALHOST");
		try
		{
	        InetAddress addr = InetAddress.getLocalHost();
	        String ipaddress=addr.toString();
			tempparameter.put(Keywords.client_host.toLowerCase(),ipaddress);
	    }
	    catch (Exception ex) {}
		for (Enumeration<String> e = tempparameter.keys() ; e.hasMoreElements() ;)
		{
			String par = e.nextElement();
			String val = tempparameter.get(par);
			for (int k=0; k<(Keywords.KeywordsForPathAndName).length; k++)
			{
				par=par.toLowerCase();
				String testwordpath=Keywords.KeywordsForPathAndName[k].toLowerCase();
				if (((par.startsWith(testwordpath)) || (par.endsWith(testwordpath))) && (par.indexOf("_")<0))
				{
					if (val.indexOf(".")<=0)
						tempparameter.put(par, Keywords.WorkDir+val);
					else
					{
						String temppath=Keywords.project.getPath(val.substring(0, val.indexOf(".")));
						if (temppath==null)
						{
							message=Keywords.Language.getMessage(1062)+" ("+par+")<br>\n";
							steperror=true;
							return;
						}
						else if (temppath.equalsIgnoreCase(""))
						{
							message=Keywords.Language.getMessage(61)+" ("+val.substring(0, val.indexOf("."))+")<br>\n";
							steperror=true;
							return;
						}
						tempparameter.put(par, temppath+(val.substring(val.indexOf(".")+1)));
					}
				}
				if (((par.startsWith(testwordpath)) || (par.endsWith(testwordpath))) && (par.indexOf("_")>0))
				{
					String temppath=Keywords.project.getPath(val);
					if (temppath.equalsIgnoreCase(""))
					{
						message=Keywords.Language.getMessage(61)+" ("+val+")<br>\n";
						steperror=true;
						return;
					}
					tempparameter.put(par, temppath);
				}
			}
			for (int k=0; k<(Keywords.KeywordsForPath).length; k++)
			{
				par=par.toLowerCase();
				String testwordpath=Keywords.KeywordsForPath[k].toLowerCase();
				if ( (par.startsWith(testwordpath) || par.endsWith(testwordpath)) && (!par.startsWith("dict")))
				{
					String temppath=Keywords.project.getPath(val);
					if (temppath.equalsIgnoreCase(""))
					{
						message=Keywords.Language.getMessage(61)+" ("+val+")<br>\n";
						steperror=true;
						return;
					}
					tempparameter.put(par, temppath);
				}
			}
		}
		Hashtable<String, Object> parameter=new Hashtable<String, Object>();
		System.setProperty(Keywords.docpwd, "");
		for (Enumeration<String> e = tempparameter.keys() ; e.hasMoreElements() ;)
		{
			String par = e.nextElement();
			String val = tempparameter.get(par);
			if ((par.toLowerCase()).startsWith(Keywords.var.toLowerCase()))
			{
				val=VarReplacer(val.trim());
				tempparameter.remove(par);
				parameter.put(par.toLowerCase(), val);
			}
			else if (par.equalsIgnoreCase(Keywords.decryptwith))
				System.setProperty(Keywords.docpwd,val);
			else if (par.equalsIgnoreCase(Keywords.docfile))
			{
				String passPhrase=tempparameter.get(Keywords.encryptwith);
				try
				{
					if (passPhrase==null)
					{
						ADaMSdoc adamsdoc=new ADaMSdoc(val);
						parameter.put(Keywords.document, adamsdoc);
					}
					else
					{
						parameter.put(Keywords.encryptwith, "");
						ADaMSdoc adamsdoc=new ADaMSdoc(val, passPhrase);
						parameter.put(Keywords.document, adamsdoc);
					}
					parameter.put(par, val);
				}
				catch (Exception ex)
				{
					int errormsg=64;
					try
					{
						errormsg=Integer.parseInt(ex.getMessage());
					}
					catch (Exception exe) {}
					message=Keywords.Language.getMessage(errormsg)+" ("+val+")<br>\n";
					steperror=true;
					return;
				}
			}
			else if (par.startsWith(Keywords.dict))
			{
				DictionaryReader dr=new DictionaryReader(val);
				if (!dr.getmessageDictionaryReader().equals(""))
				{
					if ((par.equalsIgnoreCase("dicto")) && (stepname.equalsIgnoreCase("procappend")))
						dr=null;
					else
					{
						message=dr.getmessageDictionaryReader()+"<br>\n";
						steperror=true;
						return;
					}
				}
				if (dr!=null)
					parameter.put(par, dr);
			}
			else
				parameter.put(par, val);
		}
		parameter.put("possiblegui", "false");
		String verifyguiprocs=System.getProperty("isbatch");
		if (verifyguiprocs.equalsIgnoreCase("false")) parameter.put("possiblegui", "true");
		StepsExecutor se=new StepsExecutor();
		Result executionresult=se.ExecuteStep(extstep, parameter, stepname);
		if (executionresult==null)
		{
			message=message+se.getresmsg();
			steperror=true;
			return;
		}
		Vector<StepResult> results=executionresult.getResults();
		if (results!=null)
		{
			for (int i=0; i<results.size(); i++)
			{
				if ((steptype!=2) && (exportoutput))
					results.get(i).exportOutput();
				String msg=results.get(i).action();
				if (msg.length()>=2)
					message=message+(msg.substring(2)).trim()+"\n";
				if (msg.startsWith("0"))
				{
					parameter.clear();
					parameter=null;
					System.gc();
					steperror=true;
					return;
				}
			}
		}
		if (executionresult.getMessage()!=null)
		{
			if (!executionresult.getMessage().equals("")) message=message+executionresult.getMessage();
		}
		parameter.clear();
		parameter=null;
		System.gc();
		if (!executionresult.isCorrect())
		{
			steperror=true;
		}
	}
	public String getMessage()
	{
		return message;
	}
	public boolean getError()
	{
		return steperror;
	}
}
