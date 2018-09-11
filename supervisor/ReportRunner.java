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

import java.rmi.Naming;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import ADaMSoft.dataaccess.DictionaryReader;
import ADaMSoft.keywords.Keywords;
import ADaMSoft.procedures.Result;
import ADaMSoft.procedures.RunStep;
import ADaMSoft.procedures.StepResult;
import ADaMSoft.utilities.ScriptParserUtilities;

/**
* Creates a report from one or more data sets
* @author marco.scarno@gmail.com
* @date 02/09/2015
*/
public class ReportRunner extends ScriptParserUtilities
{
	String message;
	boolean steperror;
	public ReportRunner(Vector<?> KeywordValue)
	{
		message="";
		steperror=false;
		Hashtable<Integer, Hashtable<String, Object>> reportinfo=new Hashtable<Integer, Hashtable<String, Object>>();
		String reporttype="";
		boolean sortable=false;
		boolean nocheckhtml=false;
		int refds=1;
		String reportpath="";
		String reportname="";
		for (int j=0; j<KeywordValue.size(); j++)
		{
			String actualvalue=(String)KeywordValue.get(j);
			actualvalue=actualvalue.trim();
			actualvalue=MultipleSpacesReplacer(actualvalue);
			if (actualvalue.toLowerCase().startsWith(Keywords.REPORT.toLowerCase()))
			{
				try
				{
					actualvalue=SpacesBetweenEqualReplacer(actualvalue);
					String[] parts=actualvalue.split(" ");
					if (parts.length<2)
					{
						message=Keywords.Language.getMessage(412)+"<br>\n";
						steperror=true;
						return;
					}
					reporttype=(parts[1].substring(0,1)).toUpperCase()+((parts[1].substring(1)).toLowerCase());
					if (parts.length>2)
					{
						for (int i=2; i<parts.length; i++)
						{
							if (parts[i].toLowerCase().startsWith(reporttype.toLowerCase()+Keywords.LAYOUT.toLowerCase()+"="))
							{
								try
								{
									String[] temppart=parts[i].split("=");
									Hashtable<String, String> tempinfo=Keywords.project.getSetting(temppart[0], temppart[1]);
									if (tempinfo.size()==0)
									{
										message=Keywords.Language.getMessage(414)+" ("+temppart[1]+")<br>\n";
										steperror=true;
										return;
									}
									Hashtable<String, Object> newtempinfo=new Hashtable<String, Object>();
									for (Enumeration<String> e = tempinfo.keys() ; e.hasMoreElements() ;)
									{
										String par = e.nextElement();
										String val = tempinfo.get(par);
										newtempinfo.put(par,val);
									}
									reportinfo.put(new Integer(0), newtempinfo);
								}
								catch (Exception e)
								{
									message=Keywords.Language.getMessage(413)+"<br>\n";
									steperror=true;
									return;
								}
							}
							if (parts[i].equalsIgnoreCase(Keywords.sortable))
								sortable=true;
							if (parts[i].equalsIgnoreCase(Keywords.nocheckhtml))
								nocheckhtml=true;
							if (parts[i].toLowerCase().startsWith(Keywords.outreport.toLowerCase()+"="))
							{
								try
								{
									String[] temppart=parts[i].split("=");
									String[] repparts=temppart[1].split("\\.");
									if (repparts.length==1)
										reportname=repparts[0];
									if (repparts.length==2)
									{
										if (repparts[0].equalsIgnoreCase("work"))
											reportname=repparts[1];
										else
										{
											reportpath=repparts[0];
											reportname=repparts[1];
										}
									}
								}
								catch (Exception e)
								{
									message=Keywords.Language.getMessage(420)+"<br>\n";
									steperror=true;
									return;
								}
							}
						}
					}
				}
				catch (Exception e)
				{
					message=Keywords.Language.getMessage(412)+"<br>\n";
					steperror=true;
					return;
				}
			}
			else if (!actualvalue.toLowerCase().startsWith(Keywords.RUN.toLowerCase()))
			{
				Hashtable<String, Object> dsinfo=new Hashtable<String, Object>();
				try
				{
					String refdict="";
					String refvar="";
					String replace="";
					String layout="";
					actualvalue=SpacesBetweenEqualReplacer(actualvalue);
					String[] parts=actualvalue.split(" ");
					for (int i=0; i<parts.length; i++)
					{
						if (parts[i].toLowerCase().startsWith(Keywords.dict.toLowerCase()+"="))
						{
							String[] temp=parts[i].split("=");
							if (temp.length==2)
								refdict=temp[1];
						}
						if (parts[i].toLowerCase().startsWith(Keywords.replace.toLowerCase()+"="))
						{
							String[] temp=parts[i].split("=");
							if (temp.length==2)
								replace=temp[1];
						}
						if (parts[i].toLowerCase().startsWith(reporttype.toLowerCase()+Keywords.dslayout.toLowerCase()+"="))
						{
							String[] temp=parts[i].split("=");
							if (temp.length==2)
								layout=temp[1];
						}
						if (parts[i].toLowerCase().startsWith(Keywords.var.toLowerCase()+"="))
						{
							boolean isgood=true;
							for (int k=i; k<parts.length; k++)
							{
								if (parts[k].toLowerCase().startsWith(Keywords.dict.toLowerCase()+"="))
									isgood=false;
								if (parts[k].toLowerCase().startsWith(Keywords.replace.toLowerCase()+"="))
									isgood=false;
								if (parts[k].toLowerCase().startsWith(reporttype.toLowerCase()+Keywords.dslayout.toLowerCase()+"="))
									isgood=false;
								if (isgood)
								{
									String[] temp=parts[k].split("=");
									if (temp.length==2)
										refvar=refvar+temp[1]+" ";
									else
										refvar=refvar+parts[k]+" ";
								}
							}
						}
					}
					if (refdict.equals(""))
					{
						message=Keywords.Language.getMessage(418)+"<br>\n";
						steperror=true;
						return;
					}
					String dictname="";
					String[] partdict=refdict.split("\\.");
					if (partdict.length==1)
						dictname=System.getProperty(Keywords.WorkDir)+partdict[0];
					if (partdict.length==2)
					{
						if (partdict[0].equalsIgnoreCase("work"))
							dictname=System.getProperty(Keywords.WorkDir)+partdict[0];
						else
						{
							dictname=Keywords.project.getPath(partdict[0]);
							if (dictname.equals(""))
							{
								message=Keywords.Language.getMessage(61)+" ("+partdict[0]+")<br>\n";
								steperror=true;
								return;
							}
							dictname=dictname+partdict[1];
						}
					}
					DictionaryReader dr=new DictionaryReader(dictname);
					if (!dr.getmessageDictionaryReader().equals(""))
					{
						message=dr.getmessageDictionaryReader();
						steperror=true;
						return;
					}
					dsinfo.put(Keywords.dict, dr);
					if (!refvar.equals(""))
					{
						refvar=VarReplacer(refvar.trim());
						String[] selectedvar=refvar.split(" ");
						dsinfo.put(Keywords.var, selectedvar);
						String varnotexist="";
						for (int v=0; v<selectedvar.length; v++)
						{
							boolean tempvarexist=false;
							for (int r=0; r<dr.gettotalvar(); r++)
							{
								String realname=dr.getvarname(r);
								if (realname.equalsIgnoreCase(selectedvar[v]))
									tempvarexist=true;
							}
							if (!tempvarexist)
								varnotexist=varnotexist+selectedvar[v]+" ";
						}
						if (!varnotexist.equals(""))
						{
							message=Keywords.Language.getMessage(514)+" ("+varnotexist+")<br>\n";
							steperror=true;
							return;
						}
					}
					if (!replace.equals(""))
						dsinfo.put(Keywords.replace, replace);
					if (!layout.equals(""))
					{
						Hashtable<String, String> tempdslayout=Keywords.project.getSetting(reporttype+Keywords.dslayout, layout);
						if (tempdslayout.size()==0)
						{
							message=Keywords.Language.getMessage(419)+"<br>\n";
							steperror=true;
							return;
						}
						for (Enumeration<String> e = tempdslayout.keys() ; e.hasMoreElements() ;)
						{
							String par = e.nextElement();
							String val = tempdslayout.get(par);
							dsinfo.put(par.toLowerCase(), val);
						}
					}
					reportinfo.put(new Integer(refds), dsinfo);
					refds++;
				}
				catch (Exception e)
				{
					message=Keywords.Language.getMessage(417)+"<br>\n";
					steperror=true;
					return;
				}
			}
		}
		Hashtable<String, Object> parameter=new Hashtable<String, Object>();
		if (reportname.equals(""))
		{
			message=Keywords.Language.getMessage(421)+"<br>\n";
			steperror=true;
			return;
		}
		if (!reportpath.equals(""))
		{
			reportpath=Keywords.project.getPath(reportpath);
			if (reportpath.equals(""))
			{
				message=Keywords.Language.getMessage(422)+" ("+reportpath+")<br>\n";
				steperror=true;
				return;
			}
		}
		parameter.put("reportname", reportname);
		parameter.put("reportpath", reportpath);
		parameter.put("reportinfo", reportinfo);
		if (sortable)
			parameter.put(Keywords.sortable, "");
		if (nocheckhtml)
			parameter.put(Keywords.nocheckhtml, "");
		Result executionresult=null;
		String stepname="Report"+reporttype;
		parameter.put(Keywords.WorkDir,(System.getProperty(Keywords.WorkDir)));
		try
		{
			Class<?> classCommand = Class.forName(Keywords.SoftwareName+".procedures."+stepname);
       		RunStep comm = (RunStep) classCommand.newInstance();
			executionresult = comm.executionresult(parameter);
			comm = null;
		}
		catch (InstantiationException In)
		{
			message=message+Keywords.Language.getMessage(423)+"<br>\n";
			steperror=true;
			return;
		}
		catch (IllegalAccessException In)
		{
			message=message+Keywords.Language.getMessage(423)+"<br>\n";
			steperror=true;
			return;
		}
		catch (ClassNotFoundException In)
		{
			message=message+Keywords.Language.getMessage(424)+" ("+reporttype+")<br>\n";
			steperror=true;
			return;
		}
		catch (Exception In)
		{
			message=message+Keywords.Language.getMessage(425)+"<br>\n";
			steperror=true;
			return;
		}
		parameter.clear();
		parameter=null;
		System.gc();
		if (!executionresult.isCorrect())
		{
			message=message+executionresult.getMessage();
			steperror=true;
			return;
		}
		Vector<StepResult> results=executionresult.getResults();
		if (results==null)
			message=message+executionresult.getMessage();
		if (results!=null)
		{
			for (int i=0; i<results.size(); i++)
			{
				String msg=results.get(i).action();
				if (msg.length()>=2)
					message=message+(msg.substring(2)).trim()+"<br>\n";
				if (msg.startsWith("0"))
					steperror=true;
			}
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
