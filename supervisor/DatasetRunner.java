/**
* Copyright (c) 2018 MS
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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLClassLoader;
import java.rmi.Naming;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import java.io.StringWriter;


import ADaMSoft.dataaccess.DictionaryReader;
import ADaMSoft.keywords.Keywords;
import ADaMSoft.procedures.Result;
import ADaMSoft.procedures.Step;
import ADaMSoft.procedures.StepResult;
import ADaMSoft.utilities.ScriptParserUtilities;

import java.lang.UnsupportedClassVersionError;
import ADaMSoft.utilities.Compile_java_sdk;


/**
* Executes a data step
* @author marco.scarno@gmail.com
* @date 11/06/2018
*/
public class DatasetRunner extends ScriptParserUtilities
{
	String message;
	boolean steperror;
	private ClassLoader classtoexecute;
	public DatasetRunner(Vector<String> KeywordValue)
	{
		message="";
		steperror=false;
		Vector<String> Implements=new Vector<String>();
		Vector<String> Import=new Vector<String>();
		Vector<Vector<String>> newmethods=new Vector<Vector<String>>();
		int replacecondition=0;
		String testview=KeywordValue.get(0).toLowerCase();
		if((testview.contains(Keywords.out+"=")) && (testview.contains(Keywords.view+"=")))
		{
			message=Keywords.Language.getMessage(1513)+"<br>\n";
			steperror=true;
			return;
		}
		if(testview.contains(Keywords.view+"="))
		{
			ViewRunner viewrunner=new ViewRunner(KeywordValue);
			steperror=viewrunner.getError();
			message=viewrunner.getMessage();
			return;
		}
		Vector<String> action=new Vector<String>();
		Vector<String> dataset=new Vector<String>();
		Hashtable<String, Object> parameter=new Hashtable<String, Object>();
		boolean outspecified=false;
		boolean nullds=false;
		Vector<String> toexecute=new Vector<String>();
		int existexecute=0;
		boolean errorexecute=false;
		boolean closeexecute=false;

		int newtotalmethods=0;
		int existnewmethod=0;
		boolean errornewmethod=false;
		boolean closenewmethod=false;
		for (int i=0; i<KeywordValue.size(); i++)
		{
			String actualvalue=KeywordValue.get(i);
			actualvalue=actualvalue.trim();
			actualvalue=MultipleSpacesReplacer(actualvalue);
			actualvalue=actualvalue.trim();
			if ((actualvalue.equalsIgnoreCase(Keywords.newmethod)) && (existnewmethod!=0))
				errornewmethod=true;
			if ((actualvalue.equalsIgnoreCase(Keywords.newmethod)) && (existnewmethod==0))
			{
				existnewmethod++;
				newtotalmethods++;
			}
			if ((actualvalue.equalsIgnoreCase(Keywords.endnewmethod)) && (existnewmethod!=0))
			{
				existnewmethod=existnewmethod-1;
				closenewmethod=true;
			}
		}
		for (int i=0; i<KeywordValue.size(); i++)
		{
			String actualvalue=KeywordValue.get(i);
			actualvalue=actualvalue.trim();
			actualvalue=MultipleSpacesReplacer(actualvalue);
			actualvalue=actualvalue.trim();
			if (actualvalue.indexOf("SHAREDOBJECTS.")>=0)
			{
				actualvalue=actualvalue.replaceAll("SHAREDOBJECTS.","Keywords.SHAREDOBJECTS.");
				KeywordValue.set(i, actualvalue);
			}
			if (actualvalue.toUpperCase().startsWith(Keywords.IMPLEMENTS.toUpperCase()+" "))
			{
				try
				{
					String[] ti=actualvalue.split(" ");
					if (ti.length!=2)
					{
						message=Keywords.Language.getMessage(2704)+"<br>\n";
						steperror=true;
						return;
					}
					Implements.add(ti[1]);
					KeywordValue.set(i, "");
				}
				catch (Exception ei)
				{
					message=Keywords.Language.getMessage(2704)+"<br>\n";
					steperror=true;
					return;
				}
			}
			if (actualvalue.toUpperCase().startsWith(Keywords.IMPORT.toUpperCase()+" "))
			{
				try
				{
					String[] ti=actualvalue.split(" ");
					if (ti.length!=2)
					{
						message=Keywords.Language.getMessage(2705)+"<br>\n";
						steperror=true;
						return;
					}
					Import.add(ti[1]);
					KeywordValue.set(i, "");
				}
				catch (Exception ei)
				{
					message=Keywords.Language.getMessage(2705)+"<br>\n";
					steperror=true;
					return;
				}
			}
		}
		for (int i=0; i<KeywordValue.size(); i++)
		{
			String actualvalue=KeywordValue.get(i);
			actualvalue=actualvalue.trim();
			actualvalue=MultipleSpacesReplacer(actualvalue);
			actualvalue=actualvalue.trim();
			if ((actualvalue.equalsIgnoreCase(Keywords.newmethod)) && (existnewmethod!=0))
				errornewmethod=true;
			if ((actualvalue.equalsIgnoreCase(Keywords.newmethod)) && (existnewmethod==0))
			{
				existnewmethod++;
				newtotalmethods++;
			}
			if ((actualvalue.equalsIgnoreCase(Keywords.endnewmethod)) && (existnewmethod!=0))
			{
				existnewmethod=existnewmethod-1;
				closenewmethod=true;
			}
		}
		if (errornewmethod)
		{
			message=Keywords.Language.getMessage(2702)+"<br>\n";
			steperror=true;
			return;
		}
		if ((existnewmethod!=0) && (!closenewmethod))
		{
			message=Keywords.Language.getMessage(2703)+"<br>\n";
			steperror=true;
			return;
		}
		if (newtotalmethods>0)
		{
			for (int i=0; i<newtotalmethods; i++)
			{
				Vector<String> tempnewmethod=new Vector<String>();
				newmethods.add(tempnewmethod);
			}
			newtotalmethods=0;
			existnewmethod=0;
			closenewmethod=false;
			for (int i=0; i<KeywordValue.size(); i++)
			{
				String actualvalue=KeywordValue.get(i);
				actualvalue=actualvalue.trim();
				actualvalue=MultipleSpacesReplacer(actualvalue);
				actualvalue=actualvalue.trim();
				if (existnewmethod>0)
				{
					if (!actualvalue.equalsIgnoreCase(Keywords.endnewmethod))
					{
						Vector<String> tempnewmethod=newmethods.get(newtotalmethods-1);
						tempnewmethod.add(actualvalue);
						newmethods.set(newtotalmethods-1,tempnewmethod);
					}
					KeywordValue.set(i, "");
				}
				if ((actualvalue.equalsIgnoreCase(Keywords.newmethod)) && (existnewmethod==0))
				{
					existnewmethod++;
					newtotalmethods++;
					KeywordValue.set(i, "");
				}
				if ((actualvalue.equalsIgnoreCase(Keywords.endnewmethod)) && (existnewmethod!=0))
				{
					KeywordValue.set(i, "");
					existnewmethod=existnewmethod-1;
					closenewmethod=true;
				}
			}
		}

		for (int i=1; i<KeywordValue.size(); i++)
		{
			String actualvalue=KeywordValue.get(i);
			actualvalue=actualvalue.trim();
			actualvalue=MultipleSpacesReplacer(actualvalue);
			actualvalue.trim();
			actualvalue=ReplaceUpperNameInString(actualvalue,Keywords.OUTPUT);
			int posoutoc=actualvalue.indexOf(Keywords.OUTPUT+"()");
			if (posoutoc>=0)
			{
				try
				{
					actualvalue=actualvalue.substring(0, posoutoc)+Keywords.OUTPUT+actualvalue.substring(posoutoc+(Keywords.OUTPUT+"()").length());
					KeywordValue.set(i, actualvalue);
				}
				catch (Exception e) {}
			}

			if ((actualvalue.equalsIgnoreCase(Keywords.exebefore)) && (existexecute!=0))
				errorexecute=true;
			if ((actualvalue.equalsIgnoreCase(Keywords.exebefore)) && (existexecute==0))
				existexecute++;
			if ((actualvalue.equalsIgnoreCase(Keywords.endexebefore)) && (existexecute==1))
				closeexecute=true;
		}
		if (errorexecute)
		{
			message=Keywords.Language.getMessage(784)+"<br>\n";
			steperror=true;
			return;
		}
		if ((existexecute==1) && (!closeexecute))
		{
			message=Keywords.Language.getMessage(785)+"<br>\n";
			steperror=true;
			return;
		}
		if (existexecute==1)
		{
			closeexecute=false;
			for (int i=0; i<KeywordValue.size(); i++)
			{
				String actualvalue=KeywordValue.get(i);
				actualvalue=actualvalue.trim();
				actualvalue=MultipleSpacesReplacer(actualvalue);
				actualvalue.trim();
				if (actualvalue.equalsIgnoreCase(Keywords.endexebefore))
				{
					KeywordValue.set(i,"");
					closeexecute=false;
				}
				if (closeexecute)
				{
					KeywordValue.set(i,"");
					toexecute.add(actualvalue);
				}
				if (actualvalue.equalsIgnoreCase(Keywords.exebefore))
				{
					KeywordValue.set(i, "");
					closeexecute=true;
				}
			}
		}

		for (int i=0; i<KeywordValue.size(); i++)
		{
			String actualvalue=KeywordValue.get(i);
			actualvalue=actualvalue.trim();
			if (!actualvalue.equals(""))
			{
				actualvalue=MultipleSpacesReplacer(actualvalue);
				String actualtype=actualvalue;
				String actualname="";
				if (!actualvalue.equalsIgnoreCase(Keywords.RUN))
				{
					try
					{
						actualtype=actualvalue.substring(0,actualvalue.indexOf(" "));
					}
					catch (Exception e){}
					try
					{
						actualname=actualvalue.substring(actualvalue.indexOf(" "));
					}
					catch (Exception e){}
					if (actualtype.equalsIgnoreCase(Keywords.DATASET))
					{
						String [] infoProc=new String[0];
						try
						{
							actualvalue=actualvalue.trim();
							actualvalue=SpacesBetweenEqualReplacer(actualvalue);
							actualvalue=actualvalue.trim();
							infoProc=actualvalue.split(" ");
						}
						catch (Exception e)
						{
							message=Keywords.Language.getMessage(489)+"<br>\n";
							steperror=true;
							return;
						}
						if (infoProc.length<2)
						{
							message=Keywords.Language.getMessage(489)+"<br>\n";
							steperror=true;
							return;
						}
						for (int j=1; j<infoProc.length; j++)
						{
							if (infoProc[j].indexOf("=")>0)
							{
								if ((infoProc[j].toLowerCase()).startsWith(Keywords.dict))
								{
									String[] infoVal=new String[0];
									try
									{
										infoVal=infoProc[j].split("=");
									}
									catch (Exception e)
									{
										message=Keywords.Language.getMessage(490)+"<br>\n";
										steperror=true;
										return;
									}
									if (infoVal.length!=2)
									{
										message=Keywords.Language.getMessage(490)+"<br>\n";
										steperror=true;
										return;
									}
									dataset.add(infoProc[j]);
								}
								if ((infoProc[j].toUpperCase()).startsWith(Keywords.OUT))
								{
									outspecified=true;
									String[] infoVal=new String[0];
									try
									{
										infoVal=infoProc[j].split("=");
									}
									catch (Exception e)
									{
										message=Keywords.Language.getMessage(491)+"<br>\n";
										steperror=true;
										return;
									}
									if (infoVal.length!=2)
									{
										message=Keywords.Language.getMessage(491)+"<br>\n";
										steperror=true;
										return;
									}
									String settingname=infoVal[1];
									String settingvalue="";
									Hashtable<String, String> parset=new Hashtable<String, String>();
									if (settingname.equals(Keywords._NULL_))
										nullds=true;
									else
									{
										if (settingname.indexOf(".")>0)
										{
											String [] settingnamevalue=settingname.split("\\.");
											settingname=settingnamevalue[0];
											settingvalue=settingnamevalue[1];
											parset=Keywords.project.getSetting(infoVal[0], settingname);
											if (parset.isEmpty())
											{
												message=Keywords.Language.getMessage(492)+" ("+settingname+")<br>\n";
												steperror=true;
												return;
											}
											for (Enumeration<String> en=parset.keys(); en.hasMoreElements();)
											{
												String NodeNames=en.nextElement();
												String NodeValues=parset.get(NodeNames);
												boolean iswork=false;
												if ((NodeNames.equalsIgnoreCase(Keywords.dict)) && (!NodeNames.equalsIgnoreCase(Keywords.work)))
												{
													String temppath=Keywords.project.getPath(NodeValues);
													if (temppath.equalsIgnoreCase(""))
													{
														message=Keywords.Language.getMessage(61)+" ("+NodeValues+")<br>\n";
														steperror=true;
														return;
													}
													NodeValues=temppath;
												}
												if ((NodeNames.equalsIgnoreCase(Keywords.dict)) && (NodeNames.equalsIgnoreCase(Keywords.work)))
													iswork=true;
												if ((NodeNames.equalsIgnoreCase(Keywords.DATA)) && (!NodeNames.equalsIgnoreCase(Keywords.work)))
												{
													String temppath=Keywords.project.getPath(NodeValues);
													if (temppath.equalsIgnoreCase(""))
													{
														message=Keywords.Language.getMessage(61)+" ("+NodeValues+")<br>\n";
														steperror=true;
														return;
													}
													NodeValues=temppath;
												}
												if ((NodeNames.equalsIgnoreCase(Keywords.DATA)) && (NodeNames.equalsIgnoreCase(Keywords.work)))
													iswork=true;
												if (!iswork)
													parameter.put(Keywords.OUT.toLowerCase()+"_"+NodeNames.toLowerCase(), NodeValues);
											}
											parameter.put(Keywords.OUT.toLowerCase(), settingvalue);
										}
										else
										{
											parameter.put(Keywords.OUT.toLowerCase(), settingname);
											settingvalue=settingname;
										}
									}
								}
							}
						}
					}
					else
					{
						action.add(actualtype+" "+actualname.trim());
					}
				}
			}
		}
		if (!outspecified)
		{
			message=Keywords.Language.getMessage(495)+"<br>\n";
			steperror=true;
			return;
		}
		parameter.put(Keywords.client_host.toLowerCase(),"LOCALHOST");
		try
		{
	        InetAddress addr = InetAddress.getLocalHost();
	        String ipaddress=addr.toString();
			parameter.put(Keywords.client_host.toLowerCase(),ipaddress);
	    }
	    catch (Exception ex) {}
		String[] sortvar=new String[dataset.size()];
		String [][] inputdictionary=new String[dataset.size()][2];
		for (int i=0; i<dataset.size(); i++)
		{
			String actualvalue=dataset.get(i);
			String [] infoVal=actualvalue.split("=");
			String namefile="";
			if (infoVal[1].indexOf(".")>0)
			{
				String[] pathparts=new String[0];
				try
				{
					pathparts=infoVal[1].split("\\.");
				}
				catch (Exception e)
				{
					message=Keywords.Language.getMessage(490)+"<br>\n";
					steperror=true;
					return;
				}
				if (pathparts.length!=2)
				{
					message=Keywords.Language.getMessage(490)+"<br>\n";
					steperror=true;
					return;
				}
				namefile=pathparts[1];
				inputdictionary[i][1]=Keywords.project.getPath(pathparts[0]);
				if (inputdictionary[i][1]==null)
				{
					message=Keywords.Language.getMessage(61)+" ("+pathparts[0]+")<br>\n";
					steperror=true;
					return;
				}
				if (inputdictionary[i][1].equals(""))
				{
					message=Keywords.Language.getMessage(61)+" ("+pathparts[0]+")<br>\n";
					steperror=true;
					return;
				}
				inputdictionary[i][0]=infoVal[0];
				inputdictionary[i][1]=inputdictionary[i][1]+namefile;
			}
			else
			{
				inputdictionary[i][1]=System.getProperty(Keywords.WorkDir)+infoVal[1];
				inputdictionary[i][0]=infoVal[0];
			}
		}
		boolean checkdsname=false;
		for (int i=0; i<dataset.size(); i++)
		{
			for (int j=i+1; j<dataset.size(); j++)
			{
				if (inputdictionary[i][0].equalsIgnoreCase(inputdictionary[j][0]))
					checkdsname=true;
			}
		}
		if (checkdsname)
		{
			message=Keywords.Language.getMessage(496)+"<br>\n";
			steperror=true;
			return;
		}

		String keyword="";
		String description="";

		Hashtable<String, String> existentvarnamelist=new Hashtable<String, String>();
		Vector<String[]>orderedexistentvar=new Vector<String[]>();
		boolean checkvar=true;
		for (int i=0; i<dataset.size(); i++)
		{
			DictionaryReader dr=new DictionaryReader(inputdictionary[i][1]);
			if (!dr.getmessageDictionaryReader().equals(""))
			{
				message=dr.getmessageDictionaryReader();
				steperror=true;
				return;
			}
			parameter.put(inputdictionary[i][0], dr);
			keyword=keyword+dr.getkeyword()+" ";
			description=description+dr.getdescription()+" ";
			Hashtable<String, String> tableinfo=dr.getdatatableinfo();
			String tempsortvar="";
			if (tableinfo!=null)
			{
				tempsortvar=tableinfo.get(Keywords.SORTED.toLowerCase());
				if (tempsortvar==null)
					sortvar[i]="";
				else
					sortvar[i]=tempsortvar;
			}
			else
				sortvar[i]="";
			Vector<Hashtable<String, String>> tempvarinfo=dr.getfixedvariableinfo();
			for (int j=0; j<tempvarinfo.size(); j++)
			{
				String varname=(tempvarinfo.get(j)).get(Keywords.VariableName.toLowerCase());
				String vartype=(tempvarinfo.get(j)).get(Keywords.VariableFormat.toLowerCase());
				if (vartype.toLowerCase().startsWith(Keywords.TEXTSuffix.toLowerCase()))
					vartype="TEXT";
				else
					vartype="NUM";
				String testvar=existentvarnamelist.get(varname.toLowerCase());
				if (testvar!=null)
				{
					if (!testvar.equalsIgnoreCase(vartype))
						checkvar=false;
				}
				if (existentvarnamelist.get(varname.toLowerCase())==null)
				{
					existentvarnamelist.put(varname.toLowerCase(), vartype.toLowerCase());
					String[] tempevinfo=new String[2];
					tempevinfo[0]=varname.toLowerCase();
					tempevinfo[1]=vartype.toLowerCase();
					orderedexistentvar.add(tempevinfo);
				}
			}
		}
		if (!checkvar)
		{
			message=Keywords.Language.getMessage(768)+"<br>\n";
			steperror=true;
			return;
		}
		description=description.trim();
		keyword=keyword.trim();

		Hashtable<String, String> newvarnamelist=new Hashtable<String, String>();
		Vector<String[]>orderednewvar=new Vector<String[]>();
		Vector<String> newaction=new Vector<String>();
		for (int i=0; i<action.size(); i++)
		{
			String actualvalue=action.get(i);
			if ((actualvalue.toLowerCase()).startsWith(Keywords.newvar))
			{
				String varname=actualvalue.substring(actualvalue.indexOf(" "));
				varname=varname.trim();
				String [] infonewvar=new String[0];
				try
				{
					infonewvar=varname.split("=");
				}
				catch (Exception e)
				{
					message=Keywords.Language.getMessage(769)+"<br>\n";
					steperror=true;
					return;
				}
				if (infonewvar.length!=2)
				{
					message=Keywords.Language.getMessage(769)+"<br>\n";
					steperror=true;
					return;
				}
				for (int j=0; j<infonewvar.length; j++)
				{
					infonewvar[j]=infonewvar[j].trim();
				}
				if ((!infonewvar[1].equalsIgnoreCase(Keywords.NUMSuffix)) && (!infonewvar[1].equalsIgnoreCase(Keywords.TEXTSuffix)))
				{
					message=Keywords.Language.getMessage(769)+"<br>\n";
					steperror=true;
					return;
				}
				String newvarname=VarReplacer(infonewvar[0]);
				if (newvarname==null)
				{
					message=Keywords.Language.getMessage(769)+"<br>\n";
					steperror=true;
					return;
				}
				String [] newvararray=new String[0];
				try
				{
					newvararray=newvarname.split(" ");
				}
				catch (Exception e)
				{
					message=Keywords.Language.getMessage(769)+"<br>\n";
					steperror=true;
					return;
				}
				if (newvararray.length==0)
				{
					message=Keywords.Language.getMessage(769)+"<br>\n";
					steperror=true;
					return;
				}
				for (int j=0; j<newvararray.length; j++)
				{
					for (Enumeration<String> en=existentvarnamelist.keys(); en.hasMoreElements();)
					{
						String oldvar=en.nextElement();
						if (newvararray[j].equalsIgnoreCase(oldvar))
							checkvar=false;
					}
					if (newvarnamelist.get(newvararray[j].toLowerCase())==null)
					{
						newvarnamelist.put(newvararray[j].toLowerCase(), infonewvar[1].toLowerCase());
						String[] tempevinfo=new String[2];
						tempevinfo[0]=newvararray[j].toLowerCase();
						tempevinfo[1]=infonewvar[1].toLowerCase();
						orderednewvar.add(tempevinfo);
					}
				}
			}
			else if ((actualvalue.toLowerCase()).startsWith(Keywords.replace.toLowerCase()))
			{
				String[] inforetain=actualvalue.split(" ");
				if (inforetain.length!=2)
				{
					message=Keywords.Language.getMessage(2089)+"<br>\n";
					steperror=true;
					return;
				}
				if (inforetain[1].equalsIgnoreCase(Keywords.replaceall))
					replacecondition=1;
				else if (inforetain[1].equalsIgnoreCase(Keywords.replaceformat))
					replacecondition=2;
				else if (inforetain[1].equalsIgnoreCase(Keywords.replacemissing))
					replacecondition=3;
				else
				{
					message=Keywords.Language.getMessage(2089)+"<br>\n";
					steperror=true;
					return;
				}
			}
			else
				newaction.add(actualvalue);
		}
		if (!checkvar)
		{
			message=Keywords.Language.getMessage(770)+"<br>\n";
			steperror=true;
			return;
		}
		action.clear();
		Hashtable<String, String> vartoretain=new Hashtable<String, String>();
		boolean existinoldvar=false;
		for (int i=0; i<newaction.size(); i++)
		{
			String actualvalue=newaction.get(i);
			if ((actualvalue.toLowerCase()).startsWith(Keywords.retain))
			{
				String newvarname=VarReplacer(actualvalue.substring(actualvalue.indexOf(" ")));
				newvarname=newvarname.trim();
				if (newvarname==null)
				{
					message=Keywords.Language.getMessage(771)+"<br>\n";
					steperror=true;
					return;
				}
				String [] newvararray=new String[0];
				try
				{
					newvararray=newvarname.split(" ");
				}
				catch (Exception e)
				{
					message=Keywords.Language.getMessage(771)+"<br>\n";
					steperror=true;
					return;
				}
				if (newvararray.length==0)
				{
					message=Keywords.Language.getMessage(771)+"<br>\n";
					steperror=true;
					return;
				}
				for (int j=0; j<newvararray.length; j++)
				{
					newvararray[j]=newvararray[j].trim();
					String oldvartype="";
					boolean existretain=false;
					for (Enumeration<String> en=existentvarnamelist.keys(); en.hasMoreElements();)
					{
						String oldvar=en.nextElement();
						if (newvararray[j].equalsIgnoreCase(oldvar))
							existinoldvar=true;
					}
					for (Enumeration<String> en=newvarnamelist.keys(); en.hasMoreElements();)
					{
						String oldvar=en.nextElement();
						if (newvararray[j].equalsIgnoreCase(oldvar))
						{
							oldvartype=newvarnamelist.get(oldvar);
							existretain=true;
						}
					}
					if (!existretain)
						checkvar=false;
					vartoretain.put(newvararray[j].toLowerCase(), oldvartype.toLowerCase());
				}
			}
			else
				action.add(actualvalue);
		}
		if (existinoldvar)
		{
			message=Keywords.Language.getMessage(789)+"<br>\n";
			steperror=true;
			return;
		}
		if (!checkvar)
		{
			message=Keywords.Language.getMessage(772)+"<br>\n";
			steperror=true;
			return;
		}
		newaction.clear();
		Hashtable<String, String> vartokeep=new Hashtable<String, String>();
		Vector<String>temporderedkeepvar=new Vector<String>();
		String varexistretain="";
		for (int i=0; i<action.size(); i++)
		{
			String actualvalue=action.get(i);
			if ((actualvalue.toLowerCase()).startsWith(Keywords.keep))
			{
				String newvarname=VarReplacer(actualvalue.substring(actualvalue.indexOf(" ")));
				newvarname=newvarname.trim();
				if (newvarname==null)
				{
					message=Keywords.Language.getMessage(773)+"<br>\n";
					steperror=true;
					return;
				}
				String [] newvararray=new String[0];
				try
				{
					newvararray=newvarname.split(" ");
				}
				catch (Exception e)
				{
					message=Keywords.Language.getMessage(773)+"<br>\n";
					steperror=true;
					return;
				}
				if (newvararray.length==0)
				{
					message=Keywords.Language.getMessage(773)+"<br>\n";
					steperror=true;
					return;
				}
				for (int j=0; j<newvararray.length; j++)
				{
					newvararray[j]=newvararray[j].trim();
					boolean existretain=false;
					for (Enumeration<String> en=existentvarnamelist.keys(); en.hasMoreElements();)
					{
						String oldvar=en.nextElement();
						if (newvararray[j].equalsIgnoreCase(oldvar))
							existretain=true;
					}
					for (Enumeration<String> en=newvarnamelist.keys(); en.hasMoreElements();)
					{
						String oldvar=en.nextElement();
						if (newvararray[j].equalsIgnoreCase(oldvar))
							existretain=true;
					}
					if (!existretain)
					{
						checkvar=false;
						varexistretain=varexistretain+newvararray[j]+" ";
					}
					if (vartokeep.get(newvararray[j].toLowerCase())==null)
					{
						vartokeep.put(newvararray[j].toLowerCase(),"");
						temporderedkeepvar.add(newvararray[j].toLowerCase());
					}
				}
			}
			else
				newaction.add(actualvalue);
		}
		if (!checkvar)
		{
			varexistretain=varexistretain.trim();
			message=Keywords.Language.getMessage(774)+" ("+varexistretain+")<br>\n";
			steperror=true;
			return;
		}

		vartokeep.clear();
		action.clear();

		if (temporderedkeepvar.size()==0)
		{
			for (int i=0; i<orderedexistentvar.size(); i++)
			{
				String[] tempevinfo=orderedexistentvar.get(i);
				temporderedkeepvar.add(tempevinfo[0]);
			}
			for (int i=0; i<orderednewvar.size(); i++)
			{
				String[] tempevinfo=orderednewvar.get(i);
				temporderedkeepvar.add(tempevinfo[0]);
			}
		}
		varexistretain="";
		for (int i=0; i<newaction.size(); i++)
		{
			String actualvalue=newaction.get(i);
			if ((actualvalue.toLowerCase()).startsWith(Keywords.drop))
			{
				String newvarname=VarReplacer(actualvalue.substring(actualvalue.indexOf(" ")));
				newvarname=newvarname.trim();
				if (newvarname==null)
				{
					message=Keywords.Language.getMessage(1954)+"<br>\n";
					steperror=true;
					return;
				}
				String [] newvararray=new String[0];
				try
				{
					newvararray=newvarname.split(" ");
				}
				catch (Exception e)
				{
					message=Keywords.Language.getMessage(1954)+"<br>\n";
					steperror=true;
					return;
				}
				if (newvararray.length==0)
				{
					message=Keywords.Language.getMessage(1954)+"<br>\n";
					steperror=true;
					return;
				}
				for (int j=0; j<newvararray.length; j++)
				{
					newvararray[j]=newvararray[j].trim();
					boolean existretain=false;
					for (Enumeration<String> en=existentvarnamelist.keys(); en.hasMoreElements();)
					{
						String oldvar=en.nextElement();
						if (newvararray[j].equalsIgnoreCase(oldvar))
							existretain=true;
					}
					for (Enumeration<String> en=newvarnamelist.keys(); en.hasMoreElements();)
					{
						String oldvar=en.nextElement();
						if (newvararray[j].equalsIgnoreCase(oldvar))
							existretain=true;
					}
					if (!existretain)
					{
						varexistretain=varexistretain+newvararray[j]+" ";
						checkvar=false;
					}
					if (vartokeep.get(newvararray[j].toLowerCase())==null)
					{
						vartokeep.put(newvararray[j].toLowerCase(),"");
					}
				}
			}
			else
				action.add(actualvalue);
		}
		if (!checkvar)
		{
			varexistretain=varexistretain.trim();
			message=Keywords.Language.getMessage(1955)+" ("+varexistretain+")<br>\n";
			steperror=true;
			return;
		}
		Vector<String>orderedkeepvar=new Vector<String>();
		for (int i=0; i<temporderedkeepvar.size(); i++)
		{
			if (vartokeep.get((temporderedkeepvar.get(i)).toLowerCase())==null)
				orderedkeepvar.add((temporderedkeepvar.get(i)).toLowerCase());
		}

		if ((!nullds) && (orderedkeepvar.size()==0))
		{
			message=Keywords.Language.getMessage(1956)+"<br>\n";
			steperror=true;
			return;
		}

		newaction.clear();
		Vector<String> newclassesfords=new Vector<String>();
		for (int i=0; i<action.size(); i++)
		{
			String actualvalue=action.get(i);
			if ((actualvalue.toLowerCase()).startsWith(Keywords.freqcounter))
			{
				String newvarname=VarReplacer(actualvalue.substring(actualvalue.indexOf(" ")));
				newvarname=newvarname.trim();
				if (newvarname==null)
				{
					message=Keywords.Language.getMessage(1976)+"<br>\n";
					steperror=true;
					return;
				}
				String[] newfreqcounter=newvarname.split(" ");
				for (int j=0; j<newfreqcounter.length; j++)
				{
					newclassesfords.add("FreqCounter "+newfreqcounter[j].toUpperCase()+"=new FreqCounter();");
				}
			}
			else if ((actualvalue.toLowerCase()).startsWith(Keywords.evalstat))
			{
				String newvarname=VarReplacer(actualvalue.substring(actualvalue.indexOf(" ")));
				newvarname=newvarname.trim();
				if (newvarname==null)
				{
					message=Keywords.Language.getMessage(1977)+"<br>\n";
					steperror=true;
					return;
				}
				String[] newevalstat=newvarname.split(" ");
				for (int j=0; j<newevalstat.length; j++)
				{
					newclassesfords.add("EvalStat "+newevalstat[j].toUpperCase()+"=new EvalStat();");
				}
			}
			else
				newaction.add(action.get(i));
		}
		action.clear();

		Vector<String>orderedallvar=new Vector<String>();
		for (int i=0; i<orderedexistentvar.size(); i++)
		{
			String[] tempevinfo=orderedexistentvar.get(i);
			orderedallvar.add(tempevinfo[0]);
		}
		for (int i=0; i<orderednewvar.size(); i++)
		{
			String[] tempevinfo=orderednewvar.get(i);
			orderedallvar.add(tempevinfo[0]);
		}
		Hashtable<String, Hashtable<String, Integer>> defTextArray=new Hashtable<String,Hashtable<String, Integer>>();
		Hashtable<String, Hashtable<String, Integer>> defNumArray=new Hashtable<String,Hashtable<String, Integer>>();
		for (int i=0; i<newaction.size(); i++)
		{
			String actualvarname=(newaction.get(i)).trim();
			if (actualvarname.startsWith(Keywords.ARRAY+" "))
			{
				actualvarname=MultipleSpacesReplacer(actualvarname);
				if (actualvarname.indexOf(" ")<0)
				{
					message=Keywords.Language.getMessage(2393)+"<br>\n";
					steperror=true;
					return;
				}
				String[] textarrayparts=actualvarname.split(" ");
				if (textarrayparts.length<3)
				{
					message=Keywords.Language.getMessage(2393)+"<br>\n";
					steperror=true;
					return;
				}
				String textarrayname=textarrayparts[1].toUpperCase();
				String textarrayvars="";
				for (int j=2; j<textarrayparts.length; j++)
				{
					textarrayvars=textarrayvars+" "+textarrayparts[j].trim();
				}
				textarrayvars=textarrayvars.trim();
				textarrayvars=VarReplacer(textarrayvars);
				String[] textarraylistvars=textarrayvars.split(" ");
				for (int ii=0;ii<textarraylistvars.length; ii++)
				{
					boolean etextarraylistvar=false;
					for (int jj=0; jj<orderedallvar.size(); jj++)
					{
						if (textarraylistvars[ii].equalsIgnoreCase(orderedallvar.get(jj)))
							etextarraylistvar=true;
					}
					if (!etextarraylistvar)
					{
						message=Keywords.Language.getMessage(2395)+" ("+textarraylistvars[ii]+")<br>\n";
						steperror=true;
						return;
					}
				}
				if (defTextArray.get(textarrayname)!=null)
				{
					Hashtable<String, Integer> varsdefTextArray=defTextArray.get(textarrayname);
					int refpvar=varsdefTextArray.size();
					for (int j=0; j<textarraylistvars.length; j++)
					{
						if (varsdefTextArray.get(textarraylistvars[j].toLowerCase())==null)
						{
							varsdefTextArray.put(textarraylistvars[j].toLowerCase(), new Integer(refpvar));
							refpvar++;
						}
					}
					defTextArray.put(textarrayname, varsdefTextArray);
				}
				else
				{
					Hashtable<String, Integer> varsdefTextArray=new Hashtable<String, Integer>();
					int refpvar=0;
					for (int j=0; j<textarraylistvars.length; j++)
					{
						if (varsdefTextArray.get(textarraylistvars[j].toLowerCase())==null)
						{
							varsdefTextArray.put(textarraylistvars[j].toLowerCase(), new Integer(refpvar));
							refpvar++;
						}
					}
					defTextArray.put(textarrayname, varsdefTextArray);
				}
				newaction.set(i,"");
			}
			if (actualvarname.startsWith(Keywords.ARRAYNUM))
			{
				actualvarname=MultipleSpacesReplacer(actualvarname);
				if (actualvarname.indexOf(" ")<0)
				{
					message=Keywords.Language.getMessage(2394)+"<br>\n";
					steperror=true;
					return;
				}
				String[] numarrayparts=actualvarname.split(" ");
				if (numarrayparts.length<3)
				{
					message=Keywords.Language.getMessage(2394)+"<br>\n";
					steperror=true;
					return;
				}
				String numarrayname=numarrayparts[1].toUpperCase();
				String numarrayvars="";
				for (int j=2; j<numarrayparts.length; j++)
				{
					numarrayvars=numarrayvars+" "+numarrayparts[j].trim();
				}
				numarrayvars=numarrayvars.trim();
				numarrayvars=VarReplacer(numarrayvars);
				String[] numarraylistvars=numarrayvars.split(" ");
				for (int ii=0;ii<numarraylistvars.length; ii++)
				{
					boolean enumarraylistvar=false;
					for (int jj=0; jj<orderedallvar.size(); jj++)
					{
						if (numarraylistvars[ii].equalsIgnoreCase(orderedallvar.get(jj)))
							enumarraylistvar=true;
					}
					if (!enumarraylistvar)
					{
						message=Keywords.Language.getMessage(2396)+" ("+numarraylistvars[ii]+")<br>\n";
						steperror=true;
						return;
					}
				}
				if (defNumArray.get(numarrayname)!=null)
				{
					Hashtable<String, Integer> varsdefNumArray=defNumArray.get(numarrayname);
					int refpvar=varsdefNumArray.size();
					for (int j=0; j<numarraylistvars.length; j++)
					{
						if (varsdefNumArray.get(numarraylistvars[j].toLowerCase())==null)
						{
							varsdefNumArray.put(numarraylistvars[j].toLowerCase(), new Integer(refpvar));
							refpvar++;
						}
					}
					defNumArray.put(numarrayname, varsdefNumArray);
				}
				else
				{
					Hashtable<String, Integer> varsdefNumArray=new Hashtable<String, Integer>();
					int refpvar=0;
					for (int j=0; j<numarraylistvars.length; j++)
					{
						if (varsdefNumArray.get(numarraylistvars[j].toLowerCase())==null)
						{
							varsdefNumArray.put(numarraylistvars[j].toLowerCase(), new Integer(refpvar));
							refpvar++;
						}
					}
					defNumArray.put(numarrayname, varsdefNumArray);
				}
				newaction.set(i,"");
			}
		}

		Keywords.numdataset=Keywords.numdataset+1;
		String reffile=String.valueOf(Keywords.numdataset);

		String tempjava=System.getProperty(Keywords.WorkDir)+"Datastep"+String.valueOf(reffile)+".java";
		String tempclass=System.getProperty(Keywords.WorkDir)+"Datastep"+String.valueOf(reffile)+".class";
		String temperror=System.getProperty(Keywords.WorkDir)+"Datastep"+String.valueOf(reffile)+".pop";

		parameter.put("keyword", keyword);
		parameter.put("description", description);

		boolean outputword=false;
		boolean linalg=false;
		boolean matprop=false;

		for (int i=0; i<newaction.size(); i++)
		{
			String actualvarname=newaction.get(i);
			if (actualvarname.indexOf("LINALG.")>=0)
				linalg=true;
			if (actualvarname.indexOf("MATPROP.")>=0)
				matprop=true;
			if (!actualvarname.equals(""))
			{
				if (actualvarname.indexOf(Keywords.SETARRAY+" ")>0)
				{
					String tsetarray=actualvarname.substring(actualvarname.indexOf(Keywords.SETARRAY));
					tsetarray=tsetarray.trim();
					try
					{
						String[] tnamar=tsetarray.split(" ");
						if (tnamar.length!=2)
						{
							message=Keywords.Language.getMessage(2411)+"<br>\n";
							steperror=true;
							return;
						}
						if ((defTextArray.get(tnamar[1])==null) &&  (defNumArray.get(tnamar[1])==null))
						{
							message=Keywords.Language.getMessage(2412)+" ("+tnamar[1]+")<br>\n";
							steperror=true;
							return;
						}
					}
					catch (Exception earn)
					{
						message=Keywords.Language.getMessage(2411)+"<br>\n";
						steperror=true;
						return;
					}
				}
				actualvarname=actualvarname.replaceAll(Keywords.SeMiCoLoN,";");
				if ((actualvarname.toUpperCase()).indexOf(Keywords.OUTPUT)>=0)
					outputword=true;
				actualvarname=ReplaceUpperNameInString(actualvarname,"_N_");
				for (int j=0; j<dataset.size(); j++)
				{
					String tempname=("LAST_"+inputdictionary[j][0]).toLowerCase();
					actualvarname=ReplaceUpperNameInString(actualvarname,tempname);
				}
				for (int j=0; j<dataset.size(); j++)
				{
					String tempname=("_N_"+inputdictionary[j][0]).toLowerCase();
					actualvarname=ReplaceUpperNameInString(actualvarname,tempname);
				}
				for (int j=0; j<dataset.size(); j++)
				{
					String tempname=inputdictionary[j][0];
					if (!sortvar[j].equals(""))
					{
						String [] tempnamesort=sortvar[j].split(" ");
						for (int h=0; h<tempnamesort.length; h++)
						{
							tempname="FIRST_"+tempnamesort[h]+"_"+tempname;
							actualvarname=ReplaceUpperNameInString(actualvarname,tempname);
						}
					}
				}
				actualvarname=ReplaceUpperNameInString(actualvarname,Keywords.OUTPUT);
				try
				{
					actualvarname=actualvarname.replaceAll(Keywords.OUTPUT, Keywords.OUTPUTREP);
				}
				catch (Exception e) {}
				actualvarname=ReplaceUpperNameInString(actualvarname,Keywords.THENDO);
				try
				{
					actualvarname=actualvarname.replaceAll(Keywords.THENDO, "{");
				}
				catch (Exception e) {}
				actualvarname=ReplaceUpperNameInString(actualvarname,Keywords.OPENB);
				try
				{
					actualvarname=actualvarname.replaceAll(Keywords.OPENB, "{");
				}
				catch (Exception e) {}
				actualvarname=ReplaceUpperNameInString(actualvarname,Keywords.CLOSEB);
				try
				{
					actualvarname=actualvarname.replaceAll(Keywords.CLOSEB, "}");
				}
				catch (Exception e) {}
				actualvarname=ReplaceUpperNameInString(actualvarname,Keywords.ENDDO);
				try
				{
					actualvarname=actualvarname.replaceAll(Keywords.ENDDO, "}");
				}
				catch (Exception e) {}
				try
				{
					actualvarname=actualvarname.replaceAll(Keywords.ENDSCRIPT, " new Result(\"\",false,null)");
				}
				catch (Exception e) {}
				action.add(actualvarname);
			}
		}
		newaction.clear();

		for (int i=0; i<toexecute.size(); i++)
		{
			String actualvarname=toexecute.get(i);
			actualvarname=actualvarname.replaceAll(Keywords.SeMiCoLoN,";");
			actualvarname=ReplaceUpperNameInString(actualvarname,Keywords.THENDO);
			try
			{
				actualvarname=actualvarname.replaceAll(Keywords.THENDO, "{");
			}
			catch (Exception e) {}
			actualvarname=ReplaceUpperNameInString(actualvarname,Keywords.OPENB);
			try
			{
				actualvarname=actualvarname.replaceAll(Keywords.OPENB, "{");
			}
			catch (Exception e) {}
			actualvarname=ReplaceUpperNameInString(actualvarname,Keywords.CLOSEB);
			try
			{
				actualvarname=actualvarname.replaceAll(Keywords.CLOSEB, "}");
			}
			catch (Exception e) {}
			actualvarname=ReplaceUpperNameInString(actualvarname,Keywords.ENDDO);
			try
			{
				actualvarname=actualvarname.replaceAll(Keywords.ENDDO, "}");
			}
			catch (Exception e) {}
			try
			{
				actualvarname=actualvarname.replaceAll(Keywords.ENDSCRIPT, " new Result(\"\",false,null)");
			}
			catch (Exception e) {}
			toexecute.set(i, actualvarname);
		}

		boolean noaddmissingrules=false;
		boolean noaddcodelabelrules=false;
		if (replacecondition==1)
		{
			noaddmissingrules=true;
			noaddcodelabelrules=true;
		}
		if (replacecondition==2)
			noaddcodelabelrules=true;
		if (replacecondition==3)
			noaddmissingrules=true;

		BufferedWriter ds=null;

		Vector<Hashtable<String, String>> newfixedvariableinfo=new Vector<Hashtable<String, String>>();
		Vector<Hashtable<String, String>> newcodelabel=new Vector<Hashtable<String, String>>();
		Vector<Hashtable<String, String>> newmissingdata=new Vector<Hashtable<String, String>>();

		try
		{
		    (new File(tempjava)).delete();
			(new File(tempclass)).delete();
			ds = new BufferedWriter(new FileWriter(tempjava, true));
			for (int i=0; i<Import.size(); i++)
			{
				ds.write("import "+Import.get(i)+";\n");
			}
			ds.write("import javax.naming.*;\n");
			ds.write("import javax.naming.directory.*;\n");
			ds.write("import java.io.*;\n");
			ds.write("import java.util.zip.*;\n");
			ds.write("import java.sql.*;\n");
			ds.write("import java.util.*;\n");
			ds.write("import java.util.regex.*;\n");
			ds.write("import java.text.*;\n");
			ds.write("import java.lang.*;\n");
			ds.write("import java.net.*;\n");
			ds.write("import java.awt.*;\n");
			ds.write("import java.awt.event.*;\n");
			ds.write("import javax.swing.*;\n");
			ds.write("import javax.swing.event.*;\n");
			ds.write("import javax.swing.tree.*;\n");
			ds.write("import javax.swing.text.*;\n");
			ds.write("import cern.colt.matrix.*;\n");
			ds.write("import cern.colt.matrix.linalg.*;\n");
			ds.write("import ADaMSoft.gui.*;\n");
			ds.write("import ADaMSoft.dataaccess.*;\n");
			ds.write("import ADaMSoft.keywords.Keywords;\n");
			ds.write("import ADaMSoft.procedures.*;\n");
			ds.write("import ADaMSoft.utilities.*;\n");
			ds.write("import ADaMSoft.algorithms.*;\n");
			ds.write("import ADaMSoft.algorithms.Algebra2DFile.*;\n");
			ds.write("import ADaMSoft.algorithms.clusters.*;\n");
			ds.write("import ADaMSoft.algorithms.frequencies.*;\n");
			String newimplements="";
			for (int i=0; i<Implements.size(); i++)
			{
				newimplements=newimplements+", "+Implements.get(i);
			}
			ds.write("public class Datastep"+String.valueOf(reffile)+" extends ADaMSoftFunctions implements Step, Serializable "+newimplements+"{\n");
			if (newclassesfords.size()>0)
			{
				for (int i=0; i<newclassesfords.size(); i++)
				{
					ds.write(newclassesfords.get(i)+";\n");
				}
			}
			ds.write("	private static final long serialVersionUID = 1L;\n");
			for (int i=0; i<orderedallvar.size(); i++)
			{
				String vname=orderedallvar.get(i);
				String vtype=newvarnamelist.get(vname.toLowerCase());
				if (vtype==null)
					vtype=existentvarnamelist.get(vname.toLowerCase());
				if (vtype.toLowerCase().startsWith(Keywords.NUMSuffix.toLowerCase()))
					ds.write("	double "+vname.toLowerCase()+"=Double.NaN;\n");
				else
					ds.write("	String "+vname.toLowerCase()+"=\"\";\n");
			}
			ds.write("	String kEyword=\"\";\n");
			ds.write("	String[] outValues=null;\n");
			ds.write("	String dEscription=\"\";\n");
			ds.write("	DictionaryReader dIct=null;\n");
			ds.write("	DataReader dAta=null;\n");
			ds.write("	Object tEmpdsinfo;\n");
			if (defTextArray.size()>0)
			{
				for (Enumeration<String> en=defTextArray.keys(); en.hasMoreElements();)
				{
					String an=en.nextElement();
					Hashtable<String, Integer> avals=defTextArray.get(an);
					ds.write("	String[] "+an+"=new String["+String.valueOf(avals.size())+"];\n");
				}
			}
			if (defNumArray.size()>0)
			{
				for (Enumeration<String> en=defNumArray.keys(); en.hasMoreElements();)
				{
					String an=en.nextElement();
					Hashtable<String, Integer> avals=defNumArray.get(an);
					ds.write("	double[] "+an+"=new double["+String.valueOf(avals.size())+"];\n");
				}
			}
			if (!nullds)
			{
				ds.write("	DataWriter dW=null;\n");
				ds.write("	int NWRITE=0;\n");
				ds.write("	String[] OUTVALUES=new String["+orderedkeepvar.size()+"];\n");
			}
			if (linalg)
				ds.write("	cern.colt.matrix.linalg.Algebra LINALG=new cern.colt.matrix.linalg.Algebra();\n");
			if (matprop)
				ds.write("	Property MATPROP=new Property(Property.DEFAULT);\n");

			for (int i=0; i<dataset.size(); i++)
			{
				ds.write("	int NREAD_"+inputdictionary[i][0].toUpperCase()+"=0;\n");
				ds.write("	boolean FIRST_"+inputdictionary[i][0].toUpperCase()+"=false;\n");
				ds.write("	boolean LAST_"+inputdictionary[i][0].toUpperCase()+"=false;\n");
				if (!sortvar[i].equals(""))
				{
					DictionaryReader dr=new DictionaryReader(inputdictionary[i][1]);
					Vector<Hashtable<String, String>> infovar=dr.getfixedvariableinfo();
					sortvar[i]=VarReplacer(sortvar[i].trim());
					String[] tempsortvar=sortvar[i].split(" ");
					for (int j=0; j<tempsortvar.length; j++)
					{
						boolean isnum=false;
						for (int h=0; h<infovar.size(); h++)
						{
							String varname=(infovar.get(h)).get(Keywords.VariableName.toLowerCase());
							String vartype=(infovar.get(h)).get(Keywords.VariableFormat.toLowerCase());
							if ((tempsortvar[j].trim()).equalsIgnoreCase(varname.trim()))
							{
								if (vartype.toLowerCase().startsWith(Keywords.NUMSuffix.toLowerCase()))
									isnum=true;
							}
						}
						if (isnum)
							ds.write("	double VALUE_FIRST_"+inputdictionary[i][0].toUpperCase()+"_"+tempsortvar[j].toUpperCase()+"=Double.NaN;\n");
						else
							ds.write("	String VALUE_FIRST_"+inputdictionary[i][0].toUpperCase()+"_"+tempsortvar[j].toUpperCase()+"=\"\";\n");
						ds.write("	boolean FIRST_"+inputdictionary[i][0].toUpperCase()+"_"+tempsortvar[j].toUpperCase()+"=false;\n");
					}
				}
			}
			if (dataset.size()>0)
				ds.write("	int NREAD=0;\n");
			ds.write("	public Result executionresult(Hashtable<String, Object> pArameters){\n");
			ds.write("		String[] VALUES = null;\n");
			if (!nullds)
			{
				ds.write("		Object Objectnewfixedvariableinfo=pArameters.get(\"NewFixedVariableInfo\");\n");
				ds.write("		Object Objectnewcodelabel=pArameters.get(\"NewCodeLabel\");\n");
				ds.write("		Object Objectnewmissingdata=pArameters.get(\"NewMissingData\");\n");
				ds.write("		Vector<Hashtable<String, String>> nEwfixedvariableinfo=(Vector<Hashtable<String, String>>)Objectnewfixedvariableinfo;\n");
				ds.write("		Vector<Hashtable<String, String>> nEwcodelabel=(Vector<Hashtable<String, String>>)Objectnewcodelabel;\n");
				ds.write("		Vector<Hashtable<String, String>> nEwmissingdata=(Vector<Hashtable<String, String>>)Objectnewmissingdata;\n");
				ds.write("		String [] rEquiredparameters=new String[] {Keywords.OUT.toLowerCase()};\n");
				ds.write("		String [] oPtionalparameters=new String[0];\n");
				for (int o=0; o<orderedkeepvar.size(); o++)
				{
					String actualvar=orderedkeepvar.get(o);
					for (Enumeration<String> ev=newvarnamelist.keys(); ev.hasMoreElements();)
					{
						String oldvar=ev.nextElement();
						if (oldvar.equalsIgnoreCase(actualvar))
						{
							Hashtable<String, String> cltoadd=new Hashtable<String, String>();
							Hashtable<String, String> mdtoadd=new Hashtable<String, String>();
							Hashtable<String, String> varinfotoadd=new Hashtable<String, String>();
							String varformat=newvarnamelist.get(oldvar);
							varinfotoadd.put(Keywords.VariableName.toLowerCase(), oldvar);
							varinfotoadd.put(Keywords.VariableFormat.toLowerCase(),varformat);
							varinfotoadd.put(Keywords.LabelOfVariable.toLowerCase(),oldvar);
							newfixedvariableinfo.add(varinfotoadd);
							newcodelabel.add(cltoadd);
							newmissingdata.add(mdtoadd);
						}
					}
					for (Enumeration<String> ev=existentvarnamelist.keys(); ev.hasMoreElements();)
					{
						String oldvar=ev.nextElement();
						if (oldvar.equalsIgnoreCase(actualvar))
						{
							Hashtable<String, String> cltoadd=new Hashtable<String, String>();
							Hashtable<String, String> mdtoadd=new Hashtable<String, String>();
							Hashtable<String, String> varinfotoadd=new Hashtable<String, String>();
							for (int i=0; i<dataset.size(); i++)
							{
								Hashtable<String, String> treatedvars=new Hashtable<String, String>();
								DictionaryReader dr=new DictionaryReader(inputdictionary[i][1]);
								Vector<Hashtable<String, String>> tempvi=dr.getfixedvariableinfo();
								Vector<Hashtable<String, String>> tempcl=dr.getcodelabel();
								Vector<Hashtable<String, String>> tempmd=dr.getmissingdata();
								for (int j=0; j<tempvi.size(); j++)
								{
									Hashtable<String, String> tempi=tempvi.get(j);
									Hashtable<String, String> tempc=tempcl.get(j);
									Hashtable<String, String> tempm=tempmd.get(j);
									String vn=tempi.get(Keywords.VariableName.toLowerCase());
									if (vn.equalsIgnoreCase(oldvar))
									{
										Vector<String[]> infotowrite=new Vector<String[]>();
										boolean alreadytreated=false;
										for (Enumeration<String> et=tempi.keys(); et.hasMoreElements();)
										{
											String[] tempinfotowrite=new String[2];
											String temppar=et.nextElement();
											String tempval=tempi.get(temppar);
											if (treatedvars.get(tempval)!=null)
												alreadytreated=true;
											else if (temppar.equalsIgnoreCase("variablenumber"))
												treatedvars.put(tempval, "");
											else
											{
												tempinfotowrite[0]=temppar;
												tempinfotowrite[1]=tempval;
												infotowrite.add(tempinfotowrite);
											}
										}
										if (!alreadytreated)
										{
											for (int vtotrea=0; vtotrea<infotowrite.size(); vtotrea++)
											{
												String[] tempinfotowrite=infotowrite.get(vtotrea);
												varinfotoadd.put(tempinfotowrite[0],tempinfotowrite[1]);
											}
										}
										if (!noaddcodelabelrules)
										{
											for (Enumeration<String> et=tempc.keys(); et.hasMoreElements();)
											{
												String temppar=et.nextElement();
												String tempval=tempc.get(temppar);
												cltoadd.put(temppar, tempval);
											}
										}
										if (!noaddmissingrules)
										{
											for (Enumeration<String> et=tempm.keys(); et.hasMoreElements();)
											{
												String temppar=et.nextElement();
												String tempval=tempm.get(temppar);
												mdtoadd.put(temppar, tempval);
											}
										}
									}
								}
							}
							newfixedvariableinfo.add(varinfotoadd);
							newcodelabel.add(cltoadd);
							newmissingdata.add(mdtoadd);
						}
					}
				}
				ds.write("		Keywords.percentage_total=0;\n");
				ds.write("		Keywords.percentage_done=0;\n");
				ds.write("		StepUtilities steputilities=new StepUtilities();\n");
				ds.write("		if (!steputilities.checkParameters(rEquiredparameters, oPtionalparameters, pArameters)){\n");
				ds.write("			return new Result(steputilities.getMessage(), false, null);}\n");
				ds.write("		dW=new DataWriter(pArameters, Keywords.OUT.toLowerCase());\n");
				ds.write("		if (!dW.getmessage().equals(\"\"))\n");
				ds.write("			return new Result(dW.getmessage(), false, null);\n");
				ds.write("		kEyword=(String)pArameters.get(\"keyword\");\n");
				ds.write("		dEscription=(String)pArameters.get(\"description\");\n");
				ds.write("		String aUthor=(String)pArameters.get(Keywords.client_host.toLowerCase());\n");
				ds.write("		if (!dW.opendatatable(nEwfixedvariableinfo))\n");
				ds.write("			return new Result(dW.getmessage(), false, null);\n");
				ds.write("		for (int iI=0; iI<OUTVALUES.length; iI++){\n");
				ds.write("			OUTVALUES[iI]=\"\";}\n");
			}
			for (int i=0; i<toexecute.size(); i++)
			{
				String tempe=toexecute.get(i);
				ds.write("	"+tempe+";\n");
			}
			for (int i=0; i<dataset.size(); i++)
			{
				ds.write("		tEmpdsinfo=pArameters.get(\""+inputdictionary[i][0]+"\");\n");
				ds.write("		dIct=(DictionaryReader)tEmpdsinfo;\n");
				ds.write("		dAta = new DataReader(dIct);\n");
				ds.write("			if (!dAta.open(null, "+replacecondition+", false))\n");
				ds.write("		return new Result(dAta.getmessage(), false, null);\n");
				DictionaryReader dr=new DictionaryReader(inputdictionary[i][1]);
				Vector<Hashtable<String, String>> infovar=dr.getfixedvariableinfo();
				ds.write("		while (!dAta.isLast()){\n");

				for (int kk=0; kk<orderedallvar.size(); kk++)
				{
					String vname=orderedallvar.get(kk);
					String vtype=newvarnamelist.get(vname.toLowerCase());
					if (vtype==null)
					{
						vtype=existentvarnamelist.get(vname.toLowerCase());
						if (vtype.toLowerCase().startsWith(Keywords.NUMSuffix.toLowerCase()))
							ds.write("		"+vname.toLowerCase()+"=Double.NaN;\n");
						else
							ds.write("		"+vname.toLowerCase()+"=\"\";\n");
					}
				}

				ds.write("		VALUES = dAta.getRecord();\n");
				ds.write("		if (VALUES != null){\n");
				ds.write("			NREAD_"+inputdictionary[i][0].toUpperCase()+"++;\n");
				ds.write("			NREAD++;\n");
				ds.write("			if (NREAD_"+inputdictionary[i][0].toUpperCase()+"==1) FIRST_"+inputdictionary[i][0].toUpperCase()+"=true;\n");
				ds.write("			else FIRST_"+inputdictionary[i][0].toUpperCase()+"=false;\n");
				if (!sortvar[i].equals(""))
				{
					ds.write("			if (NREAD_"+inputdictionary[i][0].toUpperCase()+"==1){\n");
					sortvar[i]=VarReplacer(sortvar[i].trim());
					String[] tempsortvar=sortvar[i].split(" ");
					for (int j=0; j<tempsortvar.length; j++)
					{
						ds.write("			FIRST_"+inputdictionary[i][0].toUpperCase()+"_"+tempsortvar[j].toUpperCase()+"=true;\n");
					}
					ds.write("			}\n");
					ds.write("			else {\n");
					for (int j=0; j<tempsortvar.length; j++)
					{
						ds.write("			FIRST_"+inputdictionary[i][0].toUpperCase()+"_"+tempsortvar[j].toUpperCase()+"=false;\n");
					}
					ds.write("			}\n");
				}
				for (int j=0; j<infovar.size(); j++)
				{
					String varname=(infovar.get(j)).get(Keywords.VariableName.toLowerCase());
					String vartype=(infovar.get(j)).get(Keywords.VariableFormat.toLowerCase());
					if (vartype.toLowerCase().startsWith(Keywords.NUMSuffix.toLowerCase()))
						ds.write("			"+varname.toLowerCase()+"=TEXT2NUM(VALUES["+j+"].trim());\n");
					else
						ds.write("			"+varname.toLowerCase()+"=VALUES["+j+"].trim();\n");
				}
				ds.write("			if (dAta.isLast())\n");
				ds.write("				LAST_"+inputdictionary[i][0].toUpperCase()+"=true;\n");
				ds.write("			else\n");
				ds.write("				LAST_"+inputdictionary[i][0].toUpperCase()+"=false;\n");

				if (!sortvar[i].equals(""))
				{
					ds.write("			if (NREAD_"+inputdictionary[i][0].toUpperCase()+"==1){\n");
					sortvar[i]=VarReplacer(sortvar[i].trim());
					String[] tempsortvar=sortvar[i].split(" ");
					for (int j=0; j<tempsortvar.length; j++)
					{
						ds.write("			VALUE_FIRST_"+inputdictionary[i][0].toUpperCase()+"_"+tempsortvar[j].toUpperCase()+"="+tempsortvar[j].toLowerCase()+";\n");
					}
					ds.write("			}\n");
					ds.write("			else {\n");
					for (int j=0; j<tempsortvar.length; j++)
					{
						boolean isnum=false;
						for (int h=0; h<infovar.size(); h++)
						{
							String varname=(infovar.get(h)).get(Keywords.VariableName.toLowerCase());
							String vartype=(infovar.get(h)).get(Keywords.VariableFormat.toLowerCase());
							if ((tempsortvar[j].trim()).equalsIgnoreCase(varname.trim()))
							{
								if (vartype.toLowerCase().startsWith(Keywords.NUMSuffix.toLowerCase()))
									isnum=true;
							}
						}
						if (isnum)
						{
							ds.write("			if (VALUE_FIRST_"+inputdictionary[i][0].toUpperCase()+"_"+tempsortvar[j].toUpperCase()+"=="+tempsortvar[j].toLowerCase()+")\n");
							ds.write("				FIRST_"+inputdictionary[i][0].toUpperCase()+"_"+tempsortvar[j].toUpperCase()+"=false;\n");
							ds.write("			else {\n");
							ds.write("				FIRST_"+inputdictionary[i][0].toUpperCase()+"_"+tempsortvar[j].toUpperCase()+"=true;\n");
							ds.write("				VALUE_FIRST_"+inputdictionary[i][0].toUpperCase()+"_"+tempsortvar[j].toUpperCase()+"="+tempsortvar[j].toLowerCase()+";\n");
							ds.write("			}\n");
						}
						else
						{
							ds.write("			if (VALUE_FIRST_"+inputdictionary[i][0].toUpperCase()+"_"+tempsortvar[j].toUpperCase()+".equals("+tempsortvar[j].toLowerCase()+"))\n");
							ds.write("				FIRST_"+inputdictionary[i][0].toUpperCase()+"_"+tempsortvar[j].toUpperCase()+"=false;\n");
							ds.write("			else {\n");
							ds.write("				FIRST_"+inputdictionary[i][0].toUpperCase()+"_"+tempsortvar[j].toUpperCase()+"=true;\n");
							ds.write("				VALUE_FIRST_"+inputdictionary[i][0].toUpperCase()+"_"+tempsortvar[j].toUpperCase()+"="+tempsortvar[j].toLowerCase()+";\n");
							ds.write("			}\n");
						}
					}
					ds.write("			}\n");
				}
				for (Enumeration<String> ev=newvarnamelist.keys(); ev.hasMoreElements();)
				{
					String newvar=ev.nextElement();
					String newvartye=newvarnamelist.get(newvar);
					boolean istoretain=false;
					for (Enumeration<String> erv=vartoretain.keys(); erv.hasMoreElements();)
					{
						String retvar=erv.nextElement();
						if (newvar.equalsIgnoreCase(retvar))
							istoretain=true;
					}
					if (!istoretain)
					{
						if (newvartye.equalsIgnoreCase(Keywords.NUMSuffix))
							ds.write("			"+newvar+"=Double.NaN;\n");
						else
							ds.write("			"+newvar+"=\"\";\n");
					}
				}
				if (defTextArray.size()>0)
				{
					for (Enumeration<String> en=defTextArray.keys(); en.hasMoreElements();)
					{
						String an=en.nextElement();
						Hashtable<String, Integer> avals=defTextArray.get(an);
						for (Enumeration<String> enva=avals.keys(); enva.hasMoreElements();)
						{
							String tempavals=enva.nextElement();
							int pnv=(avals.get(tempavals)).intValue();
							String vtype=newvarnamelist.get(tempavals.toLowerCase());
							if (vtype==null)
								vtype=existentvarnamelist.get(tempavals.toLowerCase());
							if (vtype.toLowerCase().startsWith(Keywords.NUMSuffix.toLowerCase()))
								ds.write("			"+an+"["+String.valueOf(pnv)+"]=NUM2TEXT("+tempavals.toLowerCase()+");\n");
							else
								ds.write("			"+an+"["+String.valueOf(pnv)+"]="+tempavals.toLowerCase()+";\n");
						}
					}
				}
				if (defNumArray.size()>0)
				{
					for (Enumeration<String> en=defNumArray.keys(); en.hasMoreElements();)
					{
						String an=en.nextElement();
						Hashtable<String, Integer> avals=defNumArray.get(an);
						for (Enumeration<String> enva=avals.keys(); enva.hasMoreElements();)
						{
							String tempavals=enva.nextElement();
							int pnv=(avals.get(tempavals)).intValue();
							String vtype=newvarnamelist.get(tempavals.toLowerCase());
							if (vtype==null)
								vtype=existentvarnamelist.get(tempavals.toLowerCase());
							if (vtype.toLowerCase().startsWith(Keywords.NUMSuffix.toLowerCase()))
								ds.write("			"+an+"["+String.valueOf(pnv)+"]="+tempavals.toLowerCase()+";\n");
							else
							{
								ds.write("			"+an+"["+String.valueOf(pnv)+"]=TEXT2NUM("+tempavals.toLowerCase()+");\n");
							}
						}
					}
				}
				if (action.size()>0)
				{
					ds.write("			try{\n");
					for (int j=0; j<action.size(); j++)
					{
						if (action.get(j).indexOf(Keywords.SETARRAY+" ")>=0)
						{
							if (action.get(j).indexOf(Keywords.SETARRAY+" ")>0)
							{
								ds.write("		"+action.get(j).substring(0, action.get(j).indexOf(Keywords.SETARRAY)));
							}
							String tsetarray=(action.get(j)).substring((action.get(j)).indexOf(Keywords.SETARRAY));
							tsetarray=tsetarray.trim();
							String[] tnamar=tsetarray.split(" ");
							if (defTextArray.get(tnamar[1])!=null)
							{
								Hashtable<String, Integer> avals=defTextArray.get(tnamar[1]);
								for (Enumeration<String> enva=avals.keys(); enva.hasMoreElements();)
								{
									String tempavals=enva.nextElement();
									int pnv=(avals.get(tempavals)).intValue();
									String vtype=newvarnamelist.get(tempavals.toLowerCase());
									if (vtype==null)
										vtype=existentvarnamelist.get(tempavals.toLowerCase());
									if (vtype.toLowerCase().startsWith(Keywords.NUMSuffix.toLowerCase()))
									{
										ds.write("			try{\n");
										ds.write("			"+tempavals.toLowerCase()+"=Double.parseDouble("+tnamar[1]+"["+String.valueOf(pnv)+"]);\n");
										ds.write("			}catch (Exception eNAr){\n");
										ds.write("		"+tempavals.toLowerCase()+"=Double.NaN;}\n");
									}
									else
										ds.write("			"+tempavals.toLowerCase()+"="+tnamar[1]+"["+String.valueOf(pnv)+"];\n");
								}
							}
							if (defNumArray.get(tnamar[1])!=null)
							{
								Hashtable<String, Integer> avals=defNumArray.get(tnamar[1]);
								for (Enumeration<String> enva=avals.keys(); enva.hasMoreElements();)
								{
									String tempavals=enva.nextElement();
									int pnv=(avals.get(tempavals)).intValue();
									String vtype=newvarnamelist.get(tempavals.toLowerCase());
									if (vtype==null)
										vtype=existentvarnamelist.get(tempavals.toLowerCase());
									if (vtype.toLowerCase().startsWith(Keywords.NUMSuffix.toLowerCase()))
										ds.write("		"+tempavals.toLowerCase()+"="+tnamar[1]+"["+String.valueOf(pnv)+"];\n");
									else
									{
										ds.write("			try{\n");
										ds.write("			"+tempavals.toLowerCase()+"=String.valueOf("+tnamar[1]+"["+String.valueOf(pnv)+"]);\n");
										ds.write("			}catch (Exception eNAr){\n");
										ds.write("		"+tempavals.toLowerCase()+"=\"\";}\n");
									}
								}
							}
						}
						else
							ds.write(action.get(j)+";\n");
					}
					ds.write("			}\n");
					ds.write("			catch(Exception DSex){\n");
					ds.write("				try{\n");
					if (!nullds)
						ds.write("				dAta.close();\n dW.deletetmp();\n");
					ds.write("					StringWriter SWex = new StringWriter();\n");
					ds.write("					PrintWriter PWex = new PrintWriter(SWex);\n");
					ds.write("					DSex.printStackTrace(PWex);\n");
					ds.write("					return new Result(SWex.toString()+\"<br>\", false, null);\n");
					ds.write("				}\n");
					ds.write("				catch(Exception DSSex){\n");
					if (!nullds)
						ds.write("				dW.deletetmp();\n");
					ds.write("					return new Result(DSSex.toString()+\"<br>\\n\", false, null);\n");
					ds.write("				}\n");
					ds.write("			}\n");
				}

				if ((!outputword) && (!nullds))
					ds.write("				OUTPUT();\n");
				if (!sortvar[i].equals(""))
				{
					sortvar[i]=VarReplacer(sortvar[i].trim());
					String[] tempsortvar=sortvar[i].split(" ");
					for (int j=0; j<tempsortvar.length; j++)
					{
						ds.write("			FIRST_"+inputdictionary[i][0].toUpperCase()+"_"+tempsortvar[j].toUpperCase()+"=false;\n");
					}
				}
				ds.write("				LAST_"+inputdictionary[i][0].toUpperCase()+"=false;\n");
				ds.write("			}\n");
				ds.write("		}dAta.close();\n");
				ds.write("		NREAD_"+inputdictionary[i][0].toUpperCase()+"=0;\n");
				ds.write("		FIRST_"+inputdictionary[i][0].toUpperCase()+"=false;\n");
			}
			if (dataset.size()==0)
			{
				if (action.size()>0)
				{
					ds.write("		try{\n");
					for (int j=0; j<action.size(); j++)
					{
						if (action.get(j).indexOf(Keywords.SETARRAY+" ")>=0)
						{
							if (action.get(j).indexOf(Keywords.SETARRAY+" ")>0)
							{
								ds.write("		"+action.get(j).substring(0, action.get(j).indexOf(Keywords.SETARRAY)));
							}
							String tsetarray=(action.get(j)).substring((action.get(j)).indexOf(Keywords.SETARRAY));
							tsetarray=tsetarray.trim();
							String[] tnamar=tsetarray.split(" ");
							if (defTextArray.get(tnamar[1])!=null)
							{
								Hashtable<String, Integer> avals=defTextArray.get(tnamar[1]);
								for (Enumeration<String> enva=avals.keys(); enva.hasMoreElements();)
								{
									String tempavals=enva.nextElement();
									int pnv=(avals.get(tempavals)).intValue();
									String vtype=newvarnamelist.get(tempavals.toLowerCase());
									if (vtype==null)
										vtype=existentvarnamelist.get(tempavals.toLowerCase());
									if (vtype.toLowerCase().startsWith(Keywords.NUMSuffix.toLowerCase()))
									{
										ds.write("		try{\n");
										ds.write("		"+tempavals.toLowerCase()+"=Double.parseDouble("+tnamar[1]+"["+String.valueOf(pnv)+"]);\n");
										ds.write("		}catch (Exception eNAr){\n");
										ds.write("		"+tempavals.toLowerCase()+"=Double.NaN;}\n");
									}
									else
										ds.write("		"+tempavals.toLowerCase()+"="+tnamar[1]+"["+String.valueOf(pnv)+"];\n");
								}
							}
							if (defNumArray.get(tnamar[1])!=null)
							{
								Hashtable<String, Integer> avals=defNumArray.get(tnamar[1]);
								for (Enumeration<String> enva=avals.keys(); enva.hasMoreElements();)
								{
									String tempavals=enva.nextElement();
									int pnv=(avals.get(tempavals)).intValue();
									String vtype=newvarnamelist.get(tempavals.toLowerCase());
									if (vtype==null)
										vtype=existentvarnamelist.get(tempavals.toLowerCase());
									if (vtype.toLowerCase().startsWith(Keywords.NUMSuffix.toLowerCase()))
										ds.write("		"+tempavals.toLowerCase()+"="+tnamar[1]+"["+String.valueOf(pnv)+"];\n");
									else
									{
										ds.write("		try{\n");
										ds.write("		"+tempavals.toLowerCase()+"=String.valueOf("+tnamar[1]+"["+String.valueOf(pnv)+"]);\n");
										ds.write("		}catch (Exception eNAr){\n");
										ds.write("		"+tempavals.toLowerCase()+"=\"\";}\n");
									}
								}
							}
						}
						else
							ds.write(action.get(j)+";\n");
					}
					ds.write("		;}\n");
					ds.write("		catch(Exception DSex){\n");
					ds.write("			try{\n");
					if (!nullds)
						ds.write("			dW.deletetmp();\n");
					ds.write("				StringWriter SWex = new StringWriter();\n");
					ds.write("				PrintWriter PWex = new PrintWriter(SWex);\n");
					ds.write("				DSex.printStackTrace(PWex);\n");
					ds.write("				return new Result(SWex.toString()+\"<br>\", false, null);\n");
					ds.write("			}\n");
					ds.write("			catch(Exception DSSex){\n");
					if (!nullds)
						ds.write("			dW.deletetmp();\n");
					ds.write("				return new Result(DSSex.toString()+\"<br>\\n\", false, null);\n");
					ds.write("			}\n");
					ds.write("		}\n");
				}
			}
			ds.write("		Vector<StepResult> rEsult = new Vector<StepResult>();\n");
			if (!nullds)
			{
				ds.write("			int rEcordWritten=dW.getRecordsWritten();\n");
				ds.write("			if (rEcordWritten==0)\n{");
				ds.write("				dW.deletetmp();\n");
				String percent="%";
				ds.write("				return new Result(\""+percent+"2911"+percent+"<br>\\n\", false, null);}\n");
				ds.write("			boolean rEsclose=dW.close();\n");
				ds.write("			if (!rEsclose)\n{");
				ds.write("				dW.deletetmp();\n");
				ds.write("				return new Result(dW.getmessage(), false, null);}\n");
				ds.write("			Vector<Hashtable<String, String>> tAblevariableinfo=dW.getVarInfo();\n");
				ds.write("			Hashtable<String, String> dAtatableinfo=dW.getTableInfo();\n");

				ds.write("			LocalDictionaryWriter LdiW=new LocalDictionaryWriter(dW.getdictpath(), kEyword, dEscription, aUthor, dW.gettabletype(),");
				ds.write("				dAtatableinfo, nEwfixedvariableinfo, tAblevariableinfo, nEwcodelabel, nEwmissingdata, null);\n");
				ds.write("			LdiW.setRecordWritten(rEcordWritten);\n");
				ds.write("			rEsult.add(LdiW);\n");
				ds.write("			return new Result(\"\", true, rEsult);\n");
			}
			else
				ds.write("		return new Result(\"%782%<br>\\n\", true, null);\n");
			ds.write("}\n");
			if (!nullds)
			{
				ds.write("	public void OUTPUT () {\n");
				ds.write("	NWRITE++;\n");
				ds.write("		outValues=new String["+orderedkeepvar.size()+"];\n");
				for (int i=0; i<orderedkeepvar.size(); i++)
				{
					String vname=orderedkeepvar.get(i);
					String vtype=newvarnamelist.get(vname.toLowerCase());
					if (vtype==null)
						vtype=existentvarnamelist.get(vname.toLowerCase());
					if (vtype.toLowerCase().startsWith(Keywords.NUMSuffix.toLowerCase()))
						ds.write("	outValues["+i+"]=NUM2TEXT("+vname.toLowerCase()+");\n");
					else
						ds.write("	outValues["+i+"]="+vname.toLowerCase()+";\n");
				}
				ds.write("	for (int iTeRa=0; iTeRa<"+orderedkeepvar.size()+"; iTeRa++){\n");
				ds.write("		if (!OUTVALUES[iTeRa].equals(\"\")) outValues[iTeRa]=OUTVALUES[iTeRa];\n");
				ds.write("			OUTVALUES[iTeRa]=\"\";}\n");
				ds.write("		dW.write(outValues);\n");
				ds.write("	}\n");
			}
			if (newmethods.size()>0)
			{
				for (int i=0; i<newmethods.size(); i++)
				{
					Vector<String> tempnm=newmethods.get(i);
					for (int j=0; j<tempnm.size(); j++)
					{
						String actualvarname=tempnm.get(j);
						actualvarname=actualvarname.replaceAll(Keywords.SeMiCoLoN,";");
						actualvarname=ReplaceUpperNameInString(actualvarname,Keywords.THENDO);
						try
						{
							actualvarname=actualvarname.replaceAll(Keywords.THENDO, "{");
						}
						catch (Exception e) {}
						actualvarname=ReplaceUpperNameInString(actualvarname,Keywords.OPENB);
						try
						{
							actualvarname=actualvarname.replaceAll(Keywords.OPENB, "{");
						}
						catch (Exception e) {}
						actualvarname=ReplaceUpperNameInString(actualvarname,Keywords.CLOSEB);
						try
						{
							actualvarname=actualvarname.replaceAll(Keywords.CLOSEB, "}");
						}
						catch (Exception e) {}
						actualvarname=ReplaceUpperNameInString(actualvarname,Keywords.ENDDO);
						try
						{
							actualvarname=actualvarname.replaceAll(Keywords.ENDDO, "}");
						}
						catch (Exception e) {}
						try
						{
							actualvarname=actualvarname.replaceAll(Keywords.ENDSCRIPT, " new Result(\"\",false,null)");
						}
						catch (Exception e) {}
						ds.write(actualvarname+";\n");
					}
				}
			}
			ds.write("}\n");
			ds.close();
		}
		catch (Exception e)
		{
			try
			{
				ds.close();
			}
			catch (Exception ec) {}
			message=Keywords.Language.getMessage(775)+"<br>\n";
			steperror=true;
			return;
		}

		parameter.put("NewFixedVariableInfo", newfixedvariableinfo);
		parameter.put("NewCodeLabel", newcodelabel);
		parameter.put("NewMissingData", newmissingdata);

		String osversion=System.getProperty("os.name").toString();
		osversion=osversion.trim();

		String classpath=System.getProperty ("java.class.path").toString();
		String javaversion=System.getProperty("java.version").toString();
		String[] command=new String[4];
		boolean compok=false;
		boolean is_java_sdk=false;
		for (int i=0; i<Keywords.VersionJavaCompiler.length; i++)
		{
			if (javaversion.startsWith(Keywords.VersionJavaCompiler[i]))
			{
				is_java_sdk=true;
				compok=true;
			}
		}
		if (!compok)
		{
			if (javaversion.startsWith("1.8")) compok=true;
			if (javaversion.startsWith("1.7")) compok=true;
			if (javaversion.startsWith("1.6")) compok=true;
			if (javaversion.startsWith("8")) compok=true;
			if (javaversion.startsWith("7")) compok=true;
			if (javaversion.startsWith("6")) compok=true;
		}
		if (!compok)
		{
			message=Keywords.Language.getMessage(776)+"<br>\n";
			steperror=true;
			return;
		}
		boolean appendcode=false;
		String testDEBUG=System.getProperty("DEBUG");
		if (!testDEBUG.equalsIgnoreCase("false"))
			appendcode=true;
		String codetoappend="";
		if (!is_java_sdk)
		{
			command=new String[3];
			command[0]="-classpath";
			command[1]=classpath;
			command[2]=tempjava;
			try
			{
				PrintWriter pw = new PrintWriter(new FileWriter(temperror));
				int errorCode = 0;
				try
				{
					errorCode = com.sun.tools.javac.Main.compile(command, pw);
				}
				catch (UnsupportedClassVersionError ue)
				{
					message=Keywords.Language.getMessage(3052)+"<br>\n";
					try
					{
						pw.flush();
						pw.close();
					}
					catch (Exception epw) {}
					steperror=true;
					return;
				}
				catch (Exception eee)
				{
					message=Keywords.Language.getMessage(3052)+"<br>\n";
					try
					{
						pw.flush();
						pw.close();
					}
					catch (Exception epw) {}
					steperror=true;
					return;
				}
				pw.flush();
				pw.close();
				if (errorCode==0)
					(new File(temperror)).delete();
				else
				{
					if (appendcode)
					{
						try
						{
							BufferedReader in = new BufferedReader(new FileReader(tempjava));
							String strcode="";
							int refrows=1;
							codetoappend="<i>";
							while ((strcode = in.readLine()) != null)
							{
								codetoappend=codetoappend+"("+String.valueOf(refrows)+") "+strcode+"<br>\n";
								refrows++;
							}
							in.close();
							codetoappend=codetoappend+"</i>";
						}
						catch (IOException e) {}
						codetoappend=Keywords.Language.getMessage(2187)+"<br>\n"+codetoappend+"<br>\n";
					}
					(new File(tempjava)).delete();
					try
					{
						message=codetoappend+Keywords.Language.getMessage(780)+"<br>\n";
						BufferedReader in = new BufferedReader(new FileReader(temperror));
						String str;
						while ((str = in.readLine()) != null)
						{
							message=message+str+"<br>\n";
						}
						in.close();
						(new File(temperror)).delete();
					}
					catch (IOException e)
					{
						(new File(temperror)).delete();
					}
					steperror=true;
					return;
				}
			}
			catch (Exception ee)
			{
				StringWriter errors_compiler = new StringWriter();
				ee.printStackTrace(new PrintWriter(errors_compiler));
				String ecompiler=errors_compiler.toString();
				message=Keywords.Language.getMessage(777)+"<br>"+ecompiler.toString()+"<br>\n";
				steperror=true;
				return;
			}
		}
		else
		{
			String[] res_compiler=(new Compile_java_sdk()).compile_java_sdk(tempjava);
			if (!res_compiler[0].equals("0"))
			{
				message=Keywords.Language.getMessage(4235)+"<br>\n";
				message=message+res_compiler[1]+"<br>";
				if (appendcode)
				{
					String code9toappend="<i>";
					try
					{
						BufferedReader in = new BufferedReader(new FileReader(tempjava));
						String strcode="";
						int refrows=1;
						while ((strcode = in.readLine()) != null)
						{
							code9toappend=code9toappend+"("+String.valueOf(refrows)+") "+strcode+"<br>\n";
							refrows++;
						}
						in.close();
						code9toappend=code9toappend+"</i><br>";
					}
					catch (IOException e) {}
					code9toappend=Keywords.Language.getMessage(2187)+"\n"+code9toappend+"<br>\n";
					message=message+"<br>"+code9toappend;
				}
				steperror=true;
				(new File(tempjava)).delete();
				return;
			}
		}
		(new File(tempjava)).delete();
		Result executionresult=null;
		parameter.put(Keywords.WorkDir,(System.getProperty(Keywords.WorkDir)));
		try
		{
			File fileclass = new File(System.getProperty(Keywords.WorkDir));
			URL url = fileclass.toURI().toURL();
			URL[] urls = new URL[]{url};
			classtoexecute = new URLClassLoader(urls);
			Class<?> cls = classtoexecute.loadClass("Datastep"+String.valueOf(reffile));
       		Step comm = (Step)cls.newInstance();
			executionresult = comm.executionresult(parameter);
			comm = null;
			cls=null;
			classtoexecute=null;
		}
		catch (Exception e)
		{
			message=message+Keywords.Language.getMessage(781)+"<br>\n"+e.toString()+"<br>\n";
			if (appendcode)
			{
				try
				{
					BufferedReader in = new BufferedReader(new FileReader(tempjava));
					String strcode="";
					int refrows=1;
					codetoappend="<i>";
					while ((strcode = in.readLine()) != null)
					{
						codetoappend=codetoappend+"("+String.valueOf(refrows)+") "+strcode+"<br>\n";
						refrows++;
					}
					in.close();
					codetoappend=codetoappend+"</i>";
				}
				catch (IOException eee) {}
				codetoappend=Keywords.Language.getMessage(2274)+"<br>\n"+codetoappend+"<br>\n";
				message=message+codetoappend;
			}
			(new File(tempjava)).delete();
			(new File(tempclass)).delete();
			steperror=true;
			return;
		}
		if (executionresult==null)
		{
			parameter.clear();
			parameter=null;
			System.gc();
			message=Keywords.Language.getMessage(782)+"<br>\n";
			steperror=true;
			return;
		}
		if (!executionresult.isCorrect())
		{
			String resultdatastep=executionresult.getMessage();
			int positionerror=resultdatastep.indexOf("at Datastep"+String.valueOf(reffile)+".executionresult(Datastep"+String.valueOf(reffile)+".java:");
			if (positionerror<=0)
				message=message+executionresult.getMessage();
			else
			{
				resultdatastep=resultdatastep.substring(positionerror+42);
				resultdatastep=resultdatastep.substring(0,resultdatastep.indexOf(")"));
				int lineerror=0;
				try
				{
					lineerror=Integer.parseInt(resultdatastep);
				}
				catch (Exception ee) {}
				if (lineerror==0)
				{
					message=message+executionresult.getMessage();
				}
				else
				{
					String firstlineerror=executionresult.getMessage();
					firstlineerror=firstlineerror.substring(0,firstlineerror.indexOf("\n"));
					try
					{
						BufferedReader br=new BufferedReader(new FileReader(tempjava));
						String strerror="";
						for (int i=0; i<lineerror; i++)
							strerror = br.readLine();
						br.close();
						firstlineerror=firstlineerror+"<br>\n"+"%1335% "+strerror+"<br>\n";
						message=message+firstlineerror;
					}
					catch (Exception ee)
					{
						message=message+executionresult.getMessage();
					}
				}
				if (appendcode)
				{
					try
					{
						BufferedReader in = new BufferedReader(new FileReader(tempjava));
						String strcode="";
						int refrows=1;
						codetoappend="<i>";
						while ((strcode = in.readLine()) != null)
						{
							codetoappend=codetoappend+"("+String.valueOf(refrows)+") "+strcode+"<br>\n";
							refrows++;
						}
						in.close();
						codetoappend=codetoappend+"</i>";
					}
					catch (IOException eee) {}
					codetoappend=Keywords.Language.getMessage(2274)+"<br>\n"+codetoappend+"<br>\n";
					message=message+codetoappend;
				}
				(new File(tempjava)).delete();
				(new File(tempclass)).delete();
			}
			parameter.clear();
			parameter=null;
			System.gc();
			steperror=true;
			return;
		}
		(new File(tempclass)).delete();
		(new File(tempjava)).delete();
		Vector<StepResult> results=executionresult.getResults();
		if (results==null)
			message=message+executionresult.getMessage();
		if (results!=null)
		{
			for (int i=0; i<results.size(); i++)
			{
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
		parameter.clear();
		parameter=null;
		System.gc();
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
