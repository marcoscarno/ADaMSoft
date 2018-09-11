/**
* Copyright (c) 2018 ADaMSoft
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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import ADaMSoft.dataaccess.DictionaryReader;
import ADaMSoft.keywords.Keywords;
import ADaMSoft.procedures.LocalDictionaryWriter;
import ADaMSoft.utilities.ScriptParserUtilities;
import java.lang.UnsupportedClassVersionError;
import ADaMSoft.utilities.Compile_java_sdk;

/**
* Executes a data step and creates a data set view
* @author marco.scarno@gmail.com
* @date 11/06/2018
*/
public class ViewRunner extends ScriptParserUtilities
{
	String message;
	boolean steperror;
	private ClassLoader classtoexecute;
	public ViewRunner(Vector<String> KeywordValue)
	{
		int replacecondition=0;
		message="";
		steperror=false;
		Vector<String> action=new Vector<String>();
		Vector<String> dataset=new Vector<String>();
		Hashtable<String, Object> parameter=new Hashtable<String, Object>();
		Vector<String> toexecute=new Vector<String>();
		int existexecute=0;
		boolean errorexecute=false;
		boolean closeexecute=false;
		String pathnewdict="";
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
			return ;
		}
		if ((existexecute==1) && (!closeexecute))
		{
			message=Keywords.Language.getMessage(785)+"<br>\n";
			steperror=true;
			return ;
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
							return ;
						}
						if (infoProc.length<2)
						{
							message=Keywords.Language.getMessage(489)+"<br>\n";
							steperror=true;
							return ;
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
										return ;
									}
									if (infoVal.length!=2)
									{
										message=Keywords.Language.getMessage(490)+"<br>\n";
										steperror=true;
										return ;
									}
									dataset.add(infoProc[j]);
								}
								if ((infoProc[j].toLowerCase()).startsWith(Keywords.view))
								{
									String[] infoVal=new String[0];
									try
									{
										infoVal=infoProc[j].split("=");
									}
									catch (Exception e)
									{
										message=Keywords.Language.getMessage(1514)+"<br>\n";
										steperror=true;
										return ;
									}
									if (infoVal.length!=2)
									{
										message=Keywords.Language.getMessage(1514)+"<br>\n";
										steperror=true;
										return ;
									}
									String[] newdictinfo=new String[0];
									try
									{
										newdictinfo=infoVal[1].split("\\.");
									}
									catch (Exception e)
									{
										message=Keywords.Language.getMessage(1514)+"<br>\n";
										steperror=true;
										return ;
									}
									if (newdictinfo.length==1)
									{
										pathnewdict=Keywords.WorkDir+newdictinfo[0];
									}
									else if (newdictinfo.length==2)
									{
										String temppath=Keywords.project.getPath(newdictinfo[0]);
										if (temppath.equalsIgnoreCase(""))
										{
											message=Keywords.Language.getMessage(61)+" ("+newdictinfo[0]+")<br>\n";
											steperror=true;
											return ;
										}
										pathnewdict=temppath+newdictinfo[1];
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

		String keyword="Data View";
		String description="Data View";

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
			String tempsortvar=tableinfo.get(Keywords.SORTED.toLowerCase());
			if (tempsortvar==null)
				sortvar[i]="";
			else
				sortvar[i]=tempsortvar;
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
			if (actualvalue.indexOf("SHAREDOBJECTS.")>=0)
			{
				actualvalue=actualvalue.replaceAll("SHAREDOBJECTS.","Keywords.SHAREDOBJECTS.");
				KeywordValue.set(i, actualvalue);
			}
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
		vartokeep.clear();
		action.clear();
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

		if (orderedkeepvar.size()==0)
		{
			message=Keywords.Language.getMessage(1956)+"<br>\n";
			steperror=true;
			return;
		}

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

		Keywords.numreadview=Keywords.numreadview+1;
		String reffile=String.valueOf(Keywords.numreadview);

		String tempjava=System.getProperty(Keywords.WorkDir)+"Read_view"+reffile+".java";
		String tempclass=System.getProperty(Keywords.WorkDir)+"Read_view"+reffile+".class";
		String temperror=System.getProperty(Keywords.WorkDir)+"Read_view"+reffile+".pop";

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
			toexecute.set(i, actualvarname);
		}

		Vector<Hashtable<String, String>> newfixedvariableinfo=new Vector<Hashtable<String, String>>();
		Vector<Hashtable<String, String>> newcodelabel=new Vector<Hashtable<String, String>>();
		Vector<Hashtable<String, String>> newmissingdata=new Vector<Hashtable<String, String>>();

		if(orderedkeepvar.size()==0)
		{
			message=Keywords.Language.getMessage(1515)+"<br>\n";
			steperror=true;
			return;
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
					varinfotoadd.put(Keywords.VariableFormat.toLowerCase(), varformat);
					varinfotoadd.put(Keywords.LabelOfVariable.toLowerCase(), oldvar);
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
					Hashtable<String, String> varinfotoadd=new Hashtable<String, String>();
					Hashtable<String, String> cltoadd=new Hashtable<String, String>();
					Hashtable<String, String> mdtoadd=new Hashtable<String, String>();
					for (int i=0; i<dataset.size(); i++)
					{
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
								for (Enumeration<String> et=tempi.keys(); et.hasMoreElements();)
								{
									String temppar=et.nextElement();
									String tempval=tempi.get(temppar);
									varinfotoadd.put(temppar,tempval);
								}
								if (!noaddmissingrules)
								{
									for (Enumeration<String> et=tempc.keys(); et.hasMoreElements();)
									{
										String temppar=et.nextElement();
										String tempval=tempc.get(temppar);
										cltoadd.put(temppar,tempval);
									}
								}
								if (!noaddcodelabelrules)
								{
									for (Enumeration<String> et=tempm.keys(); et.hasMoreElements();)
									{
										String temppar=et.nextElement();
										String tempval=tempm.get(temppar);
										mdtoadd.put(temppar,tempval);
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

		LocalDictionaryWriter ld=new LocalDictionaryWriter(pathnewdict, keyword, description, null, "view", null,
		newfixedvariableinfo, null, newcodelabel, newmissingdata, null);

		BufferedWriter ds=null;

		try
		{
		    (new File(tempjava)).delete();
			(new File(tempclass)).delete();
			ds = new BufferedWriter(new FileWriter(tempjava, true));
			ds.write("import java.io.*;\n");
			ds.write("import java.util.*;\n");
			ds.write("import java.util.zip.*;\n");
			ds.write("import java.text.*;\n");
			ds.write("import java.lang.*;\n");
			ds.write("import java.net.*;\n");
			ds.write("import ADaMSoft.dataaccess.*;\n");
			ds.write("import ADaMSoft.keywords.Keywords;\n");
			ds.write("import ADaMSoft.procedures.*;\n");
			ds.write("import ADaMSoft.utilities.*;\n");
			ds.write("import ADaMSoft.algorithms.*;\n");
			ds.write("import ADaMSoft.algorithms.Algebra2DFile.*;\n");
			ds.write("import ADaMSoft.algorithms.clusters.*;\n");
			ds.write("import ADaMSoft.algorithms.frequencies.*;\n");
			ds.write("import cern.colt.matrix.*;\n");
			ds.write("import cern.colt.matrix.linalg.*;\n");
			ds.write("public class Read_view"+reffile+" extends DataTableViewer implements Runnable, Serializable {\n");
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

			ds.write("	int NWRITE=0;\n");
			ds.write("	Thread tR;\n");
			ds.write("	boolean cHecklast=false;\n");
			ds.write("	boolean iStowrite;\n");
			ds.write("	String[] aCtualrecord;\n");
			ds.write("	String[] wAitingrecord;\n");
			ds.write("	Vector<Hashtable<String, String>> fIxedvariableinfo;\n");
			ds.write("	Vector<String> wRiteformat;\n");
			ds.write("	int[] rIfvar;\n");
			ds.write("	boolean aVailable=false;\n");

			ds.write("	String mEssage=\"\";\n");
			ds.write("	DictionaryReader dIct=null;\n");
			ds.write("	DataReader dAta=null;\n");
			ds.write("	Object tEmpdsinfo;\n");
			ds.write("	Hashtable<String, Object> pArameters;\n");
			if (linalg)
				ds.write("	cern.colt.matrix.linalg.Algebra LINALG=new cern.colt.matrix.linalg.Algebra();\n");
			if (matprop)
				ds.write("	Property MATPROP=new Property(Property.DEFAULT);\n");
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
			ds.write("	int NWRITERECORDS=0;\n");

			ds.write("	public void setparameters(Hashtable<String, Object> oLdparameters){\n");
			ds.write("		pArameters=new Hashtable<String, Object>();\n");
			ds.write("		if (oLdparameters!=null)\n");
			ds.write("		{\n");
			ds.write("			for (Enumeration eT=oLdparameters.keys(); eT.hasMoreElements();)\n");
			ds.write("			{\n");
			ds.write("				String tEmppar=(String)eT.nextElement();\n");
			ds.write("				Object tEmpval=oLdparameters.get(tEmppar);\n");
			ds.write("				pArameters.put(tEmppar,tEmpval);\n");
			ds.write("			}\n");
			ds.write("		}\n");
			ds.write("	}\n");

			ds.write("	public boolean open (Hashtable<String, String> tAbleinfo, Vector<Hashtable<String, String>> fIxedvariableinfo,\n");
			ds.write("	Vector<Hashtable<String, String>> tAblevariableinfo){\n");
			ds.write("		this.iStowrite=iStowrite;\n");
			ds.write("		this.fIxedvariableinfo=fIxedvariableinfo;\n");
			ds.write("		wRiteformat=new Vector<String>();\n");
			ds.write("		try	{\n");
			ds.write("			rIfvar=new int[fIxedvariableinfo.size()];\n");
			ds.write("			wAitingrecord=new String[fIxedvariableinfo.size()];\n");
			ds.write("			for (int iT=0; iT<fIxedvariableinfo.size(); iT++){\n");
			ds.write("				Hashtable<String, String> tEmp=fIxedvariableinfo.get(iT);\n");
			ds.write("				String tEmprif=tEmp.get(Keywords.VariableNumber.toLowerCase());\n");
			ds.write("				rIfvar[iT]=Integer.parseInt(tEmprif);\n");
			ds.write("				wRiteformat.add(tEmp.get(Keywords.VariableFormat.toLowerCase()));\n");
			ds.write("			}\n");
			ds.write("		}\n");
			ds.write("		catch (Exception eN){\n");
			ds.write("			mEssage=mEssage+\"%354%\\n\";\n");
			ds.write("			return false;\n");
			ds.write("		}\n");
			ds.write("		try{\n");
			ds.write("			tR=new Thread(Read_view"+reffile+".this);\n");
			ds.write("			tR.start();\n");
			ds.write("		}\n");
			ds.write("		catch (Exception e){\n");
			ds.write("			mEssage=mEssage+\"%357%\\n\";\n");
			ds.write("			cHecklast=true;\n");
			ds.write("			return false;\n");
			ds.write("		}\n");
			ds.write("		return true;\n");
			ds.write("	}\n");

			ds.write("	public synchronized String[] getRecord(){\n");
			ds.write("		if(!cHecklast){\n");
			ds.write("			try	{\n");
			ds.write("				while(!aVailable){\n");
			ds.write("					wait();\n");
			ds.write("				}\n");
			ds.write("				aCtualrecord=new String[wAitingrecord.length];\n");
			ds.write("				for (int i=0; i<wAitingrecord.length; i++){\n");
			ds.write("					aCtualrecord[i]=wAitingrecord[i];\n");
			ds.write("				}\n");
			ds.write("				aVailable=false;\n");
			ds.write("				notify();\n");
			ds.write("			}\n");
			ds.write("			catch (Exception tT) {}\n");
			ds.write("		}\n");
			ds.write("		return aCtualrecord;\n");
			ds.write("	}\n");

			ds.write("	public String getMessage(){\n");
			ds.write("		return mEssage;\n");
			ds.write("	}\n");

			ds.write("	public boolean close(){\n");
			ds.write("		return true;\n");
			ds.write("	}\n");

			ds.write("	public boolean deletetable(){\n");
			ds.write("		return true;\n");
			ds.write("	}\n");

			ds.write("	public synchronized boolean isLast(){\n");
			ds.write("		while(!aVailable){\n");
			ds.write("			try {\n");
			ds.write("				wait(1000);\n");
			ds.write("			} catch (InterruptedException e) {}\n");
			ds.write("		}\n");
			ds.write("		return cHecklast;\n");
			ds.write("	}\n");

			ds.write("	public int getRecords(Hashtable<String, String> tAbleinfo){\n");
			ds.write("		String[] VALUES = null;\n");
			ds.write("		try{\n");

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
				ds.write("		if (!dAta.open(null, "+replacecondition+", false))\n");
				ds.write("		{\n");
				ds.write("			mEssage=mEssage+dAta.getmessage()+\"\\n\";\n");
				ds.write("			return 0;\n");
				ds.write("		}\n");
				DictionaryReader dr=new DictionaryReader(inputdictionary[i][1]);
				Vector<Hashtable<String, String>> infovar=dr.getfixedvariableinfo();
				ds.write("		while (!dAta.isLast()){\n");
				ds.write("		VALUES = dAta.getRecord();\n");
				ds.write("		NREAD_"+inputdictionary[i][0].toUpperCase()+"++;\n");
				ds.write("		NREAD++;\n");
				ds.write("		if (NREAD_"+inputdictionary[i][0].toUpperCase()+"==1) FIRST_"+inputdictionary[i][0].toUpperCase()+"=true;\n");
				ds.write("		else FIRST_"+inputdictionary[i][0].toUpperCase()+"=false;\n");
				if (!sortvar[i].equals(""))
				{
					ds.write("		if (NREAD_"+inputdictionary[i][0].toUpperCase()+"==1){\n");
					sortvar[i]=VarReplacer(sortvar[i].trim());
					String[] tempsortvar=sortvar[i].split(" ");
					for (int j=0; j<tempsortvar.length; j++)
					{
						ds.write("		FIRST_"+inputdictionary[i][0].toUpperCase()+"_"+tempsortvar[j].toUpperCase()+"=true;\n");
					}
					ds.write("		}\n");
					ds.write("		else {\n");
					for (int j=0; j<tempsortvar.length; j++)
					{
						ds.write("		FIRST_"+inputdictionary[i][0].toUpperCase()+"_"+tempsortvar[j].toUpperCase()+"=false;\n");
					}
					ds.write("		}\n");
				}
				for (int j=0; j<infovar.size(); j++)
				{
					String varname=(infovar.get(j)).get(Keywords.VariableName.toLowerCase());
					String vartype=(infovar.get(j)).get(Keywords.VariableFormat.toLowerCase());
					if (vartype.toLowerCase().startsWith(Keywords.NUMSuffix.toLowerCase()))
					{
						ds.write("			"+varname.toLowerCase()+"=TEXT2NUM(VALUES["+j+"].trim());\n");
					}
					else
						ds.write("			"+varname.toLowerCase()+"=VALUES["+j+"].trim();\n");
				}
				ds.write("		if (dAta.isLast())\n");
				ds.write("			LAST_"+inputdictionary[i][0].toUpperCase()+"=true;\n");
				ds.write("		else\n");
				ds.write("			LAST_"+inputdictionary[i][0].toUpperCase()+"=false;\n");

				if (!sortvar[i].equals(""))
				{
					ds.write("		if (NREAD_"+inputdictionary[i][0].toUpperCase()+"==1){\n");
					sortvar[i]=VarReplacer(sortvar[i].trim());
					String[] tempsortvar=sortvar[i].split(" ");
					for (int j=0; j<tempsortvar.length; j++)
					{
						ds.write("		VALUE_FIRST_"+inputdictionary[i][0].toUpperCase()+"_"+tempsortvar[j].toUpperCase()+"="+tempsortvar[j].toLowerCase()+";\n");
					}
					ds.write("		}\n");
					ds.write("		else {\n");
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
							ds.write("		if (VALUE_FIRST_"+inputdictionary[i][0].toUpperCase()+"_"+tempsortvar[j].toUpperCase()+"=="+tempsortvar[j].toLowerCase()+")\n");
							ds.write("			FIRST_"+inputdictionary[i][0].toUpperCase()+"_"+tempsortvar[j].toUpperCase()+"=false;\n");
							ds.write("		else {\n");
							ds.write("			FIRST_"+inputdictionary[i][0].toUpperCase()+"_"+tempsortvar[j].toUpperCase()+"=true;\n");
							ds.write("			VALUE_FIRST_"+inputdictionary[i][0].toUpperCase()+"_"+tempsortvar[j].toUpperCase()+"="+tempsortvar[j].toLowerCase()+";\n");
							ds.write("		}\n");
						}
						else
						{
							ds.write("		if (VALUE_FIRST_"+inputdictionary[i][0].toUpperCase()+"_"+tempsortvar[j].toUpperCase()+".equals("+tempsortvar[j].toLowerCase()+"))\n");
							ds.write("			FIRST_"+inputdictionary[i][0].toUpperCase()+"_"+tempsortvar[j].toUpperCase()+"=false;\n");
							ds.write("		else {\n");
							ds.write("			FIRST_"+inputdictionary[i][0].toUpperCase()+"_"+tempsortvar[j].toUpperCase()+"=true;\n");
							ds.write("			VALUE_FIRST_"+inputdictionary[i][0].toUpperCase()+"_"+tempsortvar[j].toUpperCase()+"="+tempsortvar[j].toLowerCase()+";\n");
							ds.write("		}\n");
						}
					}
					ds.write("		}\n");
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
							ds.write("		"+newvar+"=Double.NaN;\n");
						else
							ds.write("		"+newvar+"=\"\";\n");
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
								ds.write("		"+an+"["+String.valueOf(pnv)+"]=NUM2TEXT("+tempavals.toLowerCase()+");\n");
							else
								ds.write("		"+an+"["+String.valueOf(pnv)+"]="+tempavals.toLowerCase()+";\n");
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
								ds.write("		"+an+"["+String.valueOf(pnv)+"]="+tempavals.toLowerCase()+";\n");
							else
							{
								ds.write("		"+an+"["+String.valueOf(pnv)+"]=TEXT2NUM("+tempavals.toLowerCase()+");\n");
							}
						}
					}
				}

				for (int j=0; j<action.size(); j++)
				{
					String tempaction=action.get(j);
					try
					{
						tempaction=ReplaceUpperNameInString(tempaction,Keywords.OUTPUT);
						tempaction=tempaction.replaceAll(Keywords.OUTPUT, "OUTPUTRECORDS");
					}
					catch (Exception e) {}
					if (tempaction.indexOf(Keywords.SETARRAY+" ")>=0)
					{
						if (tempaction.indexOf(Keywords.SETARRAY+" ")>0)
						{
							ds.write("		"+tempaction.substring(0, tempaction.indexOf(Keywords.SETARRAY)));
						}
						String tsetarray=(tempaction).substring((tempaction).indexOf(Keywords.SETARRAY));
						tsetarray=tsetarray.trim();
						String[] tnamar=tsetarray.split(" ");
						if (defTextArray.get(tnamar[1])!=null)
						{
							Hashtable<String, Integer> avals=defTextArray.get(tnamar[1]);
							for (Enumeration<String> enva=avals.keys(); enva.hasMoreElements();)
							{
								String tempavals=enva.nextElement();
								int pnv=(avals.get(tempavals)).intValue();
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
								ds.write("		"+tempavals.toLowerCase()+"="+tnamar[1]+"["+String.valueOf(pnv)+"];\n");
							}
						}
					}
					else
						ds.write(tempaction+";\n");
				}
				if (!outputword)
					ds.write("			OUTPUTRECORDS();\n");
				if (!sortvar[i].equals(""))
				{
					sortvar[i]=VarReplacer(sortvar[i].trim());
					String[] tempsortvar=sortvar[i].split(" ");
					for (int j=0; j<tempsortvar.length; j++)
					{
						ds.write("		FIRST_"+inputdictionary[i][0].toUpperCase()+"_"+tempsortvar[j].toUpperCase()+"=false;\n");
					}
				}
				ds.write("			LAST_"+inputdictionary[i][0].toUpperCase()+"=false;\n");
				ds.write("		}dAta.close();\n");
				ds.write("		NREAD_"+inputdictionary[i][0].toUpperCase()+"=0;\n");
				ds.write("		FIRST_"+inputdictionary[i][0].toUpperCase()+"=false;\n");
			}
			if (dataset.size()==0)
			{
				for (int j=0; j<action.size(); j++)
				{
					String tempaction=action.get(j);
					try
					{
						tempaction=ReplaceUpperNameInString(tempaction,Keywords.OUTPUT);
						tempaction=tempaction.replaceAll(Keywords.OUTPUT, "OUTPUTRECORDS");
					}
					catch (Exception e) {}
					if (tempaction.indexOf(Keywords.SETARRAY+" ")>=0)
					{
						if (tempaction.indexOf(Keywords.SETARRAY+" ")>0)
						{
							ds.write("		"+tempaction.substring(0, tempaction.indexOf(Keywords.SETARRAY)));
						}
						String tsetarray=(tempaction).substring((tempaction).indexOf(Keywords.SETARRAY));
						tsetarray=tsetarray.trim();
						String[] tnamar=tsetarray.split(" ");
						if (defTextArray.get(tnamar[1])!=null)
						{
							Hashtable<String, Integer> avals=defTextArray.get(tnamar[1]);
							for (Enumeration<String> enva=avals.keys(); enva.hasMoreElements();)
							{
								String tempavals=enva.nextElement();
								int pnv=(avals.get(tempavals)).intValue();
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
								ds.write("		"+tempavals.toLowerCase()+"="+tnamar[1]+"["+String.valueOf(pnv)+"];\n");
							}
						}
					}
					else
						ds.write(tempaction+";\n");
				}
			}
			ds.write("			return NWRITERECORDS;\n");
			ds.write("		}\n");
			ds.write("	catch(Exception DSex){\n");
			ds.write("		try{\n");
			ds.write("			StringWriter SWex = new StringWriter();\n");
			ds.write("			PrintWriter PWex = new PrintWriter(SWex);\n");
			ds.write("			DSex.printStackTrace(PWex);\n");
			ds.write("			mEssage=mEssage+SWex.toString()+\"\\n\";\n");
			ds.write("			return 0;\n");
			ds.write("		}\n");
			ds.write("		catch(Exception DSSex){\n");
			ds.write("			mEssage=mEssage+DSSex.toString()+\"\\n\";\n");
			ds.write("			return 0;\n");
			ds.write("		}\n");
			ds.write("	}\n");
			ds.write("}\n");

			ds.write("	public synchronized void run(){\n");
			ds.write("		String[] VALUES = null;\n");
			ds.write("		try{\n");

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
				ds.write("		if (!dAta.open(null, "+replacecondition+", false)){\n");
				ds.write("			mEssage=mEssage+dAta.getmessage()+\"\\n\";\n");
				ds.write("			return;\n");
				ds.write("		}\n");
				DictionaryReader dr=new DictionaryReader(inputdictionary[i][1]);
				Vector<Hashtable<String, String>> infovar=dr.getfixedvariableinfo();
				ds.write("		while (!dAta.isLast()){\n");
				ds.write("		VALUES = dAta.getRecord();\n");
				ds.write("		if (VALUES==null){");
				ds.write("			cHecklast=true;\n");
				ds.write("			notify();\n");
				ds.write("			return;}\n");
				ds.write("		else{\n");



				ds.write("		NREAD_"+inputdictionary[i][0].toUpperCase()+"++;\n");
				ds.write("		NREAD++;\n");
				ds.write("		if (NREAD_"+inputdictionary[i][0].toUpperCase()+"==1) FIRST_"+inputdictionary[i][0].toUpperCase()+"=true;\n");
				ds.write("		else FIRST_"+inputdictionary[i][0].toUpperCase()+"=false;\n");
				if (!sortvar[i].equals(""))
				{
					ds.write("		if (NREAD_"+inputdictionary[i][0].toUpperCase()+"==1){\n");
					sortvar[i]=VarReplacer(sortvar[i].trim());
					String[] tempsortvar=sortvar[i].split(" ");
					for (int j=0; j<tempsortvar.length; j++)
					{
						ds.write("		FIRST_"+inputdictionary[i][0].toUpperCase()+"_"+tempsortvar[j].toUpperCase()+"=true;\n");
					}
					ds.write("		}\n");
					ds.write("		else {\n");
					for (int j=0; j<tempsortvar.length; j++)
					{
						ds.write("		FIRST_"+inputdictionary[i][0].toUpperCase()+"_"+tempsortvar[j].toUpperCase()+"=false;\n");
					}
					ds.write("		}\n");
				}
				for (int j=0; j<infovar.size(); j++)
				{
					String varname=(infovar.get(j)).get(Keywords.VariableName.toLowerCase());
					String vartype=(infovar.get(j)).get(Keywords.VariableFormat.toLowerCase());
					if (vartype.toLowerCase().startsWith(Keywords.NUMSuffix.toLowerCase()))
					{
						ds.write("			"+varname.toLowerCase()+"=TEXT2NUM(VALUES["+j+"].trim());\n");
					}
					else
						ds.write("			"+varname.toLowerCase()+"=VALUES["+j+"].trim();\n");
				}
				ds.write("		if (dAta.isLast())\n");
				ds.write("			LAST_"+inputdictionary[i][0].toUpperCase()+"=true;\n");
				ds.write("		else\n");
				ds.write("			LAST_"+inputdictionary[i][0].toUpperCase()+"=false;\n");

				if (!sortvar[i].equals(""))
				{
					ds.write("		if (NREAD_"+inputdictionary[i][0].toUpperCase()+"==1){\n");
					sortvar[i]=VarReplacer(sortvar[i].trim());
					String[] tempsortvar=sortvar[i].split(" ");
					for (int j=0; j<tempsortvar.length; j++)
					{
						ds.write("		VALUE_FIRST_"+inputdictionary[i][0].toUpperCase()+"_"+tempsortvar[j].toUpperCase()+"="+tempsortvar[j].toLowerCase()+";\n");
					}
					ds.write("		}\n");
					ds.write("		else {\n");
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
							ds.write("		if (VALUE_FIRST_"+inputdictionary[i][0].toUpperCase()+"_"+tempsortvar[j].toUpperCase()+"=="+tempsortvar[j].toLowerCase()+")\n");
							ds.write("			FIRST_"+inputdictionary[i][0].toUpperCase()+"_"+tempsortvar[j].toUpperCase()+"=false;\n");
							ds.write("		else {\n");
							ds.write("			FIRST_"+inputdictionary[i][0].toUpperCase()+"_"+tempsortvar[j].toUpperCase()+"=true;\n");
							ds.write("			VALUE_FIRST_"+inputdictionary[i][0].toUpperCase()+"_"+tempsortvar[j].toUpperCase()+"="+tempsortvar[j].toLowerCase()+";\n");
							ds.write("		}\n");
						}
						else
						{
							ds.write("		if (VALUE_FIRST_"+inputdictionary[i][0].toUpperCase()+"_"+tempsortvar[j].toUpperCase()+".equals("+tempsortvar[j].toLowerCase()+"))\n");
							ds.write("			FIRST_"+inputdictionary[i][0].toUpperCase()+"_"+tempsortvar[j].toUpperCase()+"=false;\n");
							ds.write("		else {\n");
							ds.write("			FIRST_"+inputdictionary[i][0].toUpperCase()+"_"+tempsortvar[j].toUpperCase()+"=true;\n");
							ds.write("			VALUE_FIRST_"+inputdictionary[i][0].toUpperCase()+"_"+tempsortvar[j].toUpperCase()+"="+tempsortvar[j].toLowerCase()+";\n");
							ds.write("		}\n");
						}
					}
					ds.write("		}\n");
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
							ds.write("		"+newvar+"=Double.NaN;\n");
						else
							ds.write("		"+newvar+"=\"\";\n");
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
								ds.write("		"+an+"["+String.valueOf(pnv)+"]=NUM2TEXT("+tempavals.toLowerCase()+");\n");
							else
								ds.write("		"+an+"["+String.valueOf(pnv)+"]="+tempavals.toLowerCase()+";\n");
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
								ds.write("		"+an+"["+String.valueOf(pnv)+"]="+tempavals.toLowerCase()+";\n");
							else
							{
								ds.write("		"+an+"["+String.valueOf(pnv)+"]=TEXT2NUM("+tempavals.toLowerCase()+");\n");
							}
						}
					}
				}

				for (int j=0; j<action.size(); j++)
				{
					ds.write(action.get(j)+";\n");
				}
				if (!outputword)
					ds.write("			OUTPUT();\n");
				if (!sortvar[i].equals(""))
				{
					sortvar[i]=VarReplacer(sortvar[i].trim());
					String[] tempsortvar=sortvar[i].split(" ");
					for (int j=0; j<tempsortvar.length; j++)
					{
						ds.write("		FIRST_"+inputdictionary[i][0].toUpperCase()+"_"+tempsortvar[j].toUpperCase()+"=false;\n");
					}
				}
				ds.write("			LAST_"+inputdictionary[i][0].toUpperCase()+"=false;\n");
				ds.write("		}}dAta.close();\n");
				ds.write("		NREAD_"+inputdictionary[i][0].toUpperCase()+"=0;\n");
				ds.write("		FIRST_"+inputdictionary[i][0].toUpperCase()+"=false;\n");
			}
			if (dataset.size()==0)
			{
				for (int j=0; j<action.size(); j++)
				{
					ds.write(action.get(j)+";\n");
				}
			}
			ds.write("			aVailable=true;\n");
			ds.write("			cHecklast=true;\n");
			ds.write("			notify();\n");
			ds.write("			return;\n");
			ds.write("		}\n");
			ds.write("	catch(Exception DSex){\n");
			ds.write("		try{\n");
			ds.write("			StringWriter SWex = new StringWriter();\n");
			ds.write("			PrintWriter PWex = new PrintWriter(SWex);\n");
			ds.write("			DSex.printStackTrace(PWex);\n");
			ds.write("			mEssage=mEssage+SWex.toString()+\"\\n\";\n");
			ds.write("		}\n");
			ds.write("		catch(Exception DSSex){\n");
			ds.write("			mEssage=mEssage+DSSex.toString()+\"\\n\";\n");
			ds.write("		}\n");
			ds.write("	}\n");
			ds.write("}\n");

			ds.write("	public synchronized void OUTPUT () {\n");
			ds.write("		String [] outValues=new String["+orderedkeepvar.size()+"];\n");
			ds.write("		NWRITE++;\n");
			for (int i=0; i<orderedkeepvar.size(); i++)
			{
				String vname=orderedkeepvar.get(i);
				String vtype=newvarnamelist.get(vname.toLowerCase());
				if (vtype==null)
					vtype=existentvarnamelist.get(vname.toLowerCase());
				if (vtype.toLowerCase().startsWith(Keywords.NUMSuffix.toLowerCase()))
				{
					ds.write("	outValues["+i+"]=NUM2TEXT("+vname.toLowerCase()+");\n");
				}
				else
					ds.write("	outValues["+i+"]="+vname.toLowerCase()+";\n");
			}
			ds.write("		try{\n");
			ds.write("				wAitingrecord=new String[fIxedvariableinfo.size()];\n");
			ds.write("				for (int iT=0; iT<rIfvar.length; iT++){\n");
			ds.write("					wAitingrecord[iT]=outValues[rIfvar[iT]];\n");
			ds.write("				}\n");
			ds.write("				aVailable=true;\n");
			ds.write("				notify();\n");
			ds.write("				wait();\n");
			ds.write("				outValues=null;\n");
			//ds.write("			}\n");
			ds.write("		}\n");
			ds.write("		catch (Exception eN){\n");
			ds.write("			aVailable=true;\n");
			ds.write("			cHecklast=true;\n");
			ds.write("			notify();\n");
			ds.write("		}\n");
			ds.write("	}\n");

			ds.write("	public void OUTPUTRECORDS () {\n");
			ds.write("		NWRITERECORDS++;\n");
			ds.write("	}\n");

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
		if (!is_java_sdk)
		{
			command=new String[3];
			command[0]="-classpath";
			command[1]=classpath;
			command[2]=tempjava;
			try
			{
				PrintWriter pw = new PrintWriter(new FileOutputStream(temperror));
				int errorCode=0;
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
					(new File(tempjava)).delete();
					try
					{
						message=Keywords.Language.getMessage(780)+"<br>\n";
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
				message=Keywords.Language.getMessage(777)+"<br>\n";
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
				steperror=true;
				(new File(tempjava)).delete();
				return;
			}
		}
		(new File(tempjava)).delete();
		try
		{
			File fileclass = new File(System.getProperty(Keywords.WorkDir));
			URL url = fileclass.toURI().toURL();
			URL[] urls = new URL[]{url};
			classtoexecute = new URLClassLoader(urls);
			classtoexecute.loadClass("Read_view"+reffile);
		}
		catch (Exception e)
		{
			message=message+Keywords.Language.getMessage(781)+"<br>\n"+e.toString()+"<br>\n";
			(new File(tempjava)).delete();
			(new File(tempclass)).delete();
			steperror=true;
			return;
		}

		ByteArrayOutputStream baos=null;
		try
		{
			byte[] buffer=new byte[1024];
			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(tempclass));
			baos = new ByteArrayOutputStream();
			int accessed=0;
			while((accessed=bis.read(buffer))!=-1)
			{
				baos.write(buffer,0 , accessed);
			}
			bis.close();
		}
		catch (Exception e)
		{
			try
			{
				baos.close();
			}
			catch (Exception ex) {}
			message=message+Keywords.Language.getMessage(1516)+"<br>\n";
			steperror=true;
			return;
		}

		parameter.put(Keywords.WorkDir,(System.getProperty(Keywords.WorkDir)));

		ld.setviewclass(baos.toByteArray());
		ld.setviewparameter(parameter);
		ld.setviewclassref(reffile);

		(new File(tempclass)).delete();
		String msg=ld.action();

		try
		{
			baos.close();
		}
		catch (Exception e) {}

		(new File(tempjava)).delete();

		if (msg.length()>=2)
			message=message+(msg.substring(2)).trim()+"<br>\n";
		if (msg.startsWith("0"))
			steperror=true;;
	}
	public boolean getError()
	{
		return steperror;
	}
	public String getMessage()
	{
		return message;
	}
}
