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

import ADaMSoft.dataaccess.DictionaryReader;
import ADaMSoft.keywords.Keywords;
import ADaMSoft.procedures.Result;
import ADaMSoft.procedures.Step;
import ADaMSoft.procedures.StepResult;
import ADaMSoft.utilities.ScriptParserUtilities;
import java.lang.UnsupportedClassVersionError;
import ADaMSoft.utilities.Compile_java_sdk;

/**
* Executes the step command language
* @author marco.scarno@gmail.com
* @date 11/06/2018
*/
public class SCLRunner extends ScriptParserUtilities
{
	String message;
	boolean steperror;
	private ClassLoader classtoexecute;
	public SCLRunner(Vector<String> KeywordValue) throws java.lang.UnsupportedClassVersionError
	{
		message="";
		steperror=false;
		int replacecondition=0;
		Vector<String> action=new Vector<String>();
		Vector<String> dataset=new Vector<String>();
		Vector<String> Implements=new Vector<String>();
		Vector<String> Import=new Vector<String>();
		Vector<Vector<String>> newmethods=new Vector<Vector<String>>();
		Hashtable<String, Object> parameter=new Hashtable<String, Object>();
		Vector<String> toexecute=new Vector<String>();
		int existexecute=0;
		boolean errorexecute=false;
		boolean closeexecute=false;
		boolean extspecified=false;
		String pathextname="";
		String extname="";
		for (int i=0; i<KeywordValue.size(); i++)
		{
			String actualvalue=KeywordValue.get(i);
			actualvalue=actualvalue.trim();
			actualvalue=MultipleSpacesReplacer(actualvalue);
			actualvalue=actualvalue.trim();
			if ((actualvalue.equalsIgnoreCase(Keywords.exebefore)) && (existexecute!=0))
				errorexecute=true;
			if ((actualvalue.equalsIgnoreCase(Keywords.exebefore)) && (existexecute==0))
				existexecute++;
			if ((actualvalue.equalsIgnoreCase(Keywords.endexebefore)) && (existexecute==1))
				closeexecute=true;
		}
		if (errorexecute)
		{
			message=Keywords.Language.getMessage(2097)+"<br>\n";
			steperror=true;
			return;
		}
		if ((existexecute==1) && (!closeexecute))
		{
			message=Keywords.Language.getMessage(2098)+"<br>\n";
			steperror=true;
			return;
		}
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
			if (actualvalue.indexOf("SHAREDOBJECTS.")>=0)
			{
				actualvalue=actualvalue.replaceAll("SHAREDOBJECTS.","Keywords.SHAREDOBJECTS.");
				KeywordValue.set(i, actualvalue);
			}
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
		if (existexecute==1)
		{
			closeexecute=false;
			for (int i=0; i<KeywordValue.size(); i++)
			{
				String actualvalue=KeywordValue.get(i);
				actualvalue=actualvalue.trim();
				actualvalue=MultipleSpacesReplacer(actualvalue);
				actualvalue=actualvalue.trim();
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
					if (actualtype.equalsIgnoreCase(Keywords.SCL))
					{
						try
						{
							actualvalue=actualvalue.trim();
						}
						catch (Exception e){}
						if (actualvalue.indexOf(" ")>0)
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
								message=Keywords.Language.getMessage(2099)+"<br>\n";
								steperror=true;
								return;
							}
							if (infoProc.length<2)
							{
								message=Keywords.Language.getMessage(2099)+"<br>\n";
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
											message=Keywords.Language.getMessage(2100)+"<br>\n";
											steperror=true;
											return;
										}
										if (infoVal.length!=2)
										{
											message=Keywords.Language.getMessage(2100)+"<br>\n";
											steperror=true;
											return;
										}
										dataset.add(infoProc[j]);
									}
									if ((infoProc[j].toLowerCase()).startsWith(Keywords.saveasext))
									{
										extspecified=true;
										String[] infoVal=new String[0];
										try
										{
											infoVal=infoProc[j].split("=");
										}
										catch (Exception e)
										{
											message=Keywords.Language.getMessage(2658)+"<br>\n";
											steperror=true;
											return;
										}
										if (infoVal.length!=2)
										{
											message=Keywords.Language.getMessage(2658)+"<br>\n";
											steperror=true;
											return;
										}
										pathextname=infoVal[1];
										if (pathextname.indexOf(".")>0)
										{
											String[] tempextinfo=pathextname.split("\\.");
											pathextname=tempextinfo[0];
											extname=tempextinfo[1];
											String temppathext=Keywords.project.getPath(pathextname);
											if (temppathext.equalsIgnoreCase(""))
											{
												message=Keywords.Language.getMessage(61)+" ("+pathextname+")<br>\n";
												steperror=true;
												return;
											}
											pathextname=temppathext;
										}
										else
										{
											pathextname=System.getProperty(Keywords.WorkDir).toString();
											extname=infoVal[1];
										}
										extname = extname.toLowerCase();
										String firstChar=extname.substring(0,1);
										extname = extname.replaceFirst(firstChar, firstChar.toUpperCase());
									}
									if ((infoProc[j].toUpperCase()).startsWith(Keywords.OUT))
									{
										message=Keywords.Language.getMessage(2101)+"<br>\n";
										steperror=true;
										return;
									}
									if ((infoProc[j].toUpperCase()).startsWith(Keywords.VIEW))
									{
										message=Keywords.Language.getMessage(2101)+"<br>\n";
										steperror=true;
										return;
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
					message=Keywords.Language.getMessage(2100)+"<br>\n";
					steperror=true;
					return;
				}
				if (pathparts.length!=2)
				{
					message=Keywords.Language.getMessage(2100)+"<br>\n";
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
		Vector<String> newaction=new Vector<String>();
		Vector<String> newclassesfords=new Vector<String>();
		Vector<String> extnumpar=new Vector<String>();
		Vector<String> exttextpar=new Vector<String>();
		for (int i=0; i<action.size(); i++)
		{
			String actualvalue=action.get(i);
			if ((actualvalue.toLowerCase()).startsWith(Keywords.replace.toLowerCase()))
			{
				String[] inforetain=actualvalue.split(" ");
				if (inforetain.length!=2)
				{
					message=Keywords.Language.getMessage(2105)+"<br>\n";
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
			else if ((actualvalue.toLowerCase()).startsWith(Keywords.freqcounter))
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
			else if ((actualvalue.toLowerCase()).startsWith(Keywords.parameternum))
			{
				if (!extspecified)
				{
					message=Keywords.Language.getMessage(2662)+"<br>\n";
					steperror=true;
					return;
				}
				String newvarname=VarReplacer(actualvalue.substring(actualvalue.indexOf(" ")));
				newvarname=newvarname.trim();
				if (newvarname==null)
				{
					message=Keywords.Language.getMessage(2660)+"<br>\n";
					steperror=true;
					return;
				}
				String[] newevalstat=newvarname.split(" ");
				for (int j=0; j<newevalstat.length; j++)
				{
					extnumpar.add(newevalstat[j].toUpperCase());
				}
			}
			else if ((actualvalue.toLowerCase()).startsWith(Keywords.parametertext))
			{
				if (!extspecified)
				{
					message=Keywords.Language.getMessage(2662)+"<br>\n";
					steperror=true;
					return;
				}
				String newvarname=VarReplacer(actualvalue.substring(actualvalue.indexOf(" ")));
				newvarname=newvarname.trim();
				if (newvarname==null)
				{
					message=Keywords.Language.getMessage(2661)+"<br>\n";
					steperror=true;
					return;
				}
				String[] newevalstat=newvarname.split(" ");
				for (int j=0; j<newevalstat.length; j++)
				{
					exttextpar.add(newevalstat[j].toUpperCase());
				}
			}
			else
				newaction.add(actualvalue);
		}
		action.clear();
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
					for (int jj=0; jj<orderedexistentvar.size(); jj++)
					{
						String[] iorderedexistentvar=orderedexistentvar.get(jj);
						if (textarraylistvars[ii].equalsIgnoreCase(iorderedexistentvar[0]))
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
					for (int jj=0; jj<orderedexistentvar.size(); jj++)
					{
						String[] iorderedexistentvar=orderedexistentvar.get(jj);
						if (numarraylistvars[ii].equalsIgnoreCase(iorderedexistentvar[0]))
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

		if (extspecified)
		{
			tempjava=pathextname+"Ext"+extname+".java";
			tempclass=pathextname+"Ext"+extname+".class";
			temperror=pathextname+"Ext"+extname+".pop";
		}

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
				boolean addp=false;
				if (actualvarname.indexOf("ADDPARAMETER(")>=0)
					addp=true;
				if (actualvarname.indexOf("ADDPARAMETER ")>=0)
					addp=true;
				if (addp)
				{
					try
					{
						actualvarname=actualvarname.replaceAll("ADDPARAMETER", "AdamsOftS.ADDPARAMETER");
					}
					catch (Exception e) {}
				}
				boolean addpc=false;
				if (actualvarname.indexOf("CLEARPARAMETER(")>=0)
					addpc=true;
				if (actualvarname.indexOf("CLEARPARAMETER ")>=0)
					addpc=true;
				if (addpc)
				{
					try
					{
						actualvarname=actualvarname.replaceAll("CLEARPARAMETER", "AdamsOftS.CLEARPARAMETER");
					}
					catch (Exception e) {}
				}
				boolean adde=false;
				if (actualvarname.indexOf("EXECUTESTEP(")>=0)
					adde=true;
				if (actualvarname.indexOf("EXECUTESTEP ")>=0)
					adde=true;
				if (adde)
				{
					try
					{
						actualvarname=actualvarname.replaceAll("EXECUTESTEP", "resExeStep=AdamsOftS.EXECUTESTEP");
					}
					catch (Exception e) {}
				}
				boolean addms=false;
				if (actualvarname.indexOf("EXECUTEMACROSTEP(")>=0)
					addms=true;
				if (actualvarname.indexOf("EXECUTEMACROSTEP ")>=0)
					addms=true;
				if (addms)
				{
					try
					{
						actualvarname=actualvarname.replaceAll("EXECUTEMACROSTEP", "resExeStep=AdamsOftS.EXECUTEMACROSTEP");
					}
					catch (Exception e) {}
				}
				boolean execf=false;
				if (actualvarname.indexOf("EXECUTECMDFILE(")>=0)
					execf=true;
				if (actualvarname.indexOf("EXECUTECMDFILE ")>=0)
					execf=true;
				if (execf)
				{
					try
					{
						actualvarname=actualvarname.replaceAll("EXECUTECMDFILE", "resExeStep=AdamsOftS.EXECUTECMDFILE");
					}
					catch (Exception e) {}
				}
				boolean exevcf=false;
				if (actualvarname.indexOf("EXECUTEVOIDCMDFILE(")>=0)
					exevcf=true;
				if (actualvarname.indexOf("EXECUTEVOIDCMDFILE ")>=0)
					exevcf=true;
				if (exevcf)
				{
					try
					{
						actualvarname=actualvarname.replaceAll("EXECUTEVOIDCMDFILE", "AdamsOftS.EXECUTEVOIDCMDFILE");
					}
					catch (Exception e) {}
				}
				boolean exevbcf=false;
				if (actualvarname.indexOf("EXECUTEVOIDBATCHCMDFILE(")>=0)
					exevbcf=true;
				if (actualvarname.indexOf("EXECUTEVOIDBATCHCMDFILE ")>=0)
					exevbcf=true;
				if (exevbcf)
				{
					try
					{
						actualvarname=actualvarname.replaceAll("EXECUTEVOIDBATCHCMDFILE", "AdamsOftS.EXECUTEVOIDBATCHCMDFILE");
					}
					catch (Exception e) {}
				}
				boolean addvms=false;
				if (actualvarname.indexOf("EXECUTEVOIDMACROSTEP(")>=0)
					addvms=true;
				if (actualvarname.indexOf("EXECUTEVOIDMACROSTEP ")>=0)
					addvms=true;
				if (addvms)
				{
					try
					{
						actualvarname=actualvarname.replaceAll("EXECUTEVOIDMACROSTEP", "AdamsOftS.EXECUTEVOIDMACROSTEP");
					}
					catch (Exception e) {}
				}
				boolean addvbms=false;
				if (actualvarname.indexOf("EXECUTEVOIDBATCHMACROSTEP(")>=0)
					addvbms=true;
				if (actualvarname.indexOf("EXECUTEVOIDBATCHMACROSTEP ")>=0)
					addvbms=true;
				if (addvbms)
				{
					try
					{
						actualvarname=actualvarname.replaceAll("EXECUTEVOIDBATCHMACROSTEP", "AdamsOftS.EXECUTEVOIDBATCHMACROSTEP");
					}
					catch (Exception e) {}
				}
				boolean addmst=false;
				if (actualvarname.indexOf("ADDMACROSTEP(")>=0)
					addmst=true;
				if (actualvarname.indexOf("ADDMACROSTEP ")>=0)
					addmst=true;
				if (addmst)
				{
					try
					{
						actualvarname=actualvarname.replaceAll("ADDMACROSTEP", "resExeStep=AdamsOftS.ADDMACROSTEP");
					}
					catch (Exception e) {}
				}
				boolean addd=false;
				if (actualvarname.indexOf("ADDDICTACTION(")>=0)
					addd=true;
				if (actualvarname.indexOf("ADDDICTACTION ")>=0)
					addd=true;
				if (addd)
				{
					try
					{
						actualvarname=actualvarname.replaceAll("ADDDICTACTION", "AdamsOftS.ADDDICTACTION");
					}
					catch (Exception e) {}
				}
				boolean added=false;
				if (actualvarname.indexOf("EXECUTEDICTIONARY(")>=0)
					added=true;
				if (actualvarname.indexOf("EXECUTEDICTIONARY ")>=0)
					added=true;
				if (added)
				{
					try
					{
						actualvarname=actualvarname.replaceAll("EXECUTEDICTIONARY", "resExeStep=AdamsOftS.EXECUTEDICTIONARY");
					}
					catch (Exception e) {}
				}
				action.add(actualvarname);
				if (actualvarname.indexOf("AdamsOftS.EXECUTECMDFILE")>=0)
				{
					action.add("if (!resExeStep) return new Result(reSExe+AdamsOftS.getresmsg(), false, null);\n");
					action.add("reSExe=reSExe+AdamsOftS.getresmsg();\n");
				}
				if (actualvarname.indexOf("AdamsOftS.EXECUTEMACROSTEP")>=0)
				{
					action.add("if (!resExeStep) return new Result(reSExe+AdamsOftS.getresmsg(), false, null);\n");
					action.add("reSExe=reSExe+AdamsOftS.getresmsg();\n");
				}
				if (actualvarname.indexOf("AdamsOftS.ADDMACROSTEP")>=0)
				{
					action.add("if (!resExeStep) return new Result(reSExe+AdamsOftS.getresmsg(), false, null);\n");
					action.add("reSExe=reSExe+AdamsOftS.getresmsg();\n");
				}
				if (actualvarname.indexOf("AdamsOftS.EXECUTESTEP")>=0)
				{
					action.add("if (!resExeStep) return new Result(reSExe+AdamsOftS.getresmsg(), false, null);\n");
					action.add("reSExe=reSExe+AdamsOftS.getresmsg();\n");
				}
				if (actualvarname.indexOf("AdamsOftS.EXECUTEDICTIONARY")>=0)
				{
					action.add("if (!resExeStep) return new Result(reSExe+AdamsOftS.getresmsg(), false, null);\n");
					action.add("reSExe=reSExe+AdamsOftS.getresmsg();\n");
				}
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
			boolean addp=false;
			if (actualvarname.indexOf("ADDPARAMETER(")>=0)
				addp=true;
			if (actualvarname.indexOf("ADDPARAMETER ")>=0)
				addp=true;
			if (addp)
			{
				try
				{
					actualvarname=actualvarname.replaceAll("ADDPARAMETER", "AdamsOftS.ADDPARAMETER");
				}
				catch (Exception e) {}
			}
			boolean addpc=false;
			if (actualvarname.indexOf("CLEARPARAMETER(")>=0)
				addpc=true;
			if (actualvarname.indexOf("CLEARPARAMETER ")>=0)
				addpc=true;
			if (addpc)
			{
				try
				{
					actualvarname=actualvarname.replaceAll("CLEARPARAMETER", "AdamsOftS.CLEARPARAMETER");
				}
				catch (Exception e) {}
			}
			boolean addms=false;
			if (actualvarname.indexOf("EXECUTEMACROSTEP(")>=0)
				addms=true;
			if (actualvarname.indexOf("EXECUTEMACROSTEP ")>=0)
				addms=true;
			if (addms)
			{
				try
				{
					actualvarname=actualvarname.replaceAll("EXECUTEMACROSTEP", "resExeStep=AdamsOftS.EXECUTEMACROSTEP");
				}
				catch (Exception e) {}
			}
			boolean execf=false;
			if (actualvarname.indexOf("EXECUTECMDFILE(")>=0)
				execf=true;
			if (actualvarname.indexOf("EXECUTECMDFILE ")>=0)
				execf=true;
			if (execf)
			{
				try
				{
					actualvarname=actualvarname.replaceAll("EXECUTECMDFILE", "resExeStep=AdamsOftS.EXECUTECMDFILE");
				}
				catch (Exception e) {}
			}
			boolean exevcf=false;
			if (actualvarname.indexOf("EXECUTEVOIDCMDFILE(")>=0)
				exevcf=true;
			if (actualvarname.indexOf("EXECUTEVOIDCMDFILE ")>=0)
				exevcf=true;
			if (exevcf)
			{
				try
				{
					actualvarname=actualvarname.replaceAll("EXECUTEVOIDCMDFILE", "AdamsOftS.EXECUTEVOIDCMDFILE");
				}
				catch (Exception e) {}
			}
			boolean exevbcf=false;
			if (actualvarname.indexOf("EXECUTEVOIDBATCHCMDFILE(")>=0)
				exevbcf=true;
			if (actualvarname.indexOf("EXECUTEVOIDBATCHCMDFILE ")>=0)
				exevbcf=true;
			if (exevbcf)
			{
				try
				{
					actualvarname=actualvarname.replaceAll("EXECUTEVOIDBATCHCMDFILE", "AdamsOftS.EXECUTEVOIDBATCHCMDFILE");
				}
				catch (Exception e) {}
			}
			boolean addvms=false;
			if (actualvarname.indexOf("EXECUTEVOIDMACROSTEP(")>=0)
				addvms=true;
			if (actualvarname.indexOf("EXECUTEVOIDMACROSTEP ")>=0)
				addvms=true;
			if (addvms)
			{
				try
				{
					actualvarname=actualvarname.replaceAll("EXECUTEVOIDMACROSTEP", "AdamsOftS.EXECUTEVOIDMACROSTEP");
				}
				catch (Exception e) {}
			}
			boolean addvbms=false;
			if (actualvarname.indexOf("EXECUTEVOIDBATCHMACROSTEP(")>=0)
				addvbms=true;
			if (actualvarname.indexOf("EXECUTEVOIDBATCHMACROSTEP ")>=0)
				addvbms=true;
			if (addvbms)
			{
				try
				{
					actualvarname=actualvarname.replaceAll("EXECUTEVOIDBATCHMACROSTEP", "AdamsOftS.EXECUTEVOIDBATCHMACROSTEP");
				}
				catch (Exception e) {}
			}
			boolean addmst=false;
			if (actualvarname.indexOf("ADDMACROSTEP(")>=0)
				addmst=true;
			if (actualvarname.indexOf("ADDMACROSTEP ")>=0)
				addmst=true;
			if (addmst)
			{
				try
				{
					actualvarname=actualvarname.replaceAll("ADDMACROSTEP", "resExeStep=AdamsOftS.ADDMACROSTEP");
				}
				catch (Exception e) {}
			}
			boolean adde=false;
			if (actualvarname.indexOf("EXECUTESTEP(")>=0)
				adde=true;
			if (actualvarname.indexOf("EXECUTESTEP ")>=0)
				adde=true;
			if (adde)
			{
				try
				{
					actualvarname=actualvarname.replaceAll("EXECUTESTEP", "resExeStep=AdamsOftS.EXECUTESTEP");
				}
				catch (Exception e) {}
			}
			boolean addd=false;
			if (actualvarname.indexOf("ADDDICTACTION(")>=0)
				addd=true;
			if (actualvarname.indexOf("ADDDICTACTION ")>=0)
				addd=true;
			if (addd)
			{
				try
				{
					actualvarname=actualvarname.replaceAll("ADDDICTACTION", "AdamsOftS.ADDDICTACTION");
				}
				catch (Exception e) {}
			}
			boolean added=false;
			if (actualvarname.indexOf("EXECUTEDICTIONARY(")>=0)
				added=true;
			if (actualvarname.indexOf("EXECUTEDICTIONARY ")>=0)
				added=true;
			if (added)
			{
				try
				{
					actualvarname=actualvarname.replaceAll("EXECUTEDICTIONARY", "resExeStep=AdamsOftS.EXECUTEDICTIONARY");
				}
				catch (Exception e) {}
			}
			newaction.add(actualvarname);
			if (actualvarname.indexOf("AdamsOftS.EXECUTECMDFILE")>=0)
			{
				action.add("if (!resExeStep) return new Result(reSExe+AdamsOftS.getresmsg(), false, null);\n");
				action.add("reSExe=reSExe+AdamsOftS.getresmsg();\n");
			}
			if (actualvarname.indexOf("AdamsOftS.ADDMACROSTEP")>=0)
			{
				newaction.add("if (!resExeStep) return new Result(reSExe+AdamsOftS.getresmsg(), false, null);\n");
				newaction.add("reSExe=reSExe+AdamsOftS.getresmsg();\n");
			}
			if (actualvarname.indexOf("AdamsOftS.EXECUTESTEP")>=0)
			{
				newaction.add("if (!resExeStep) return new Result(reSExe+AdamsOftS.getresmsg(), false, null);\n");
				newaction.add("reSExe=reSExe+AdamsOftS.getresmsg();\n");
			}
			if (actualvarname.indexOf("AdamsOftS.EXECUTEDICTIONARY")>=0)
			{
				newaction.add("if (!resExeStep) return new Result(reSExe+AdamsOftS.getresmsg(), false, null);\n");
				newaction.add("reSExe=reSExe+AdamsOftS.getresmsg();\n");
			}
			if (actualvarname.indexOf("AdamsOftS.EXECUTEMACROSTEP")>=0)
			{
				newaction.add("if (!resExeStep) return new Result(reSExe+AdamsOftS.getresmsg(), false, null);\n");
				newaction.add("reSExe=reSExe+AdamsOftS.getresmsg();\n");
			}
		}

		BufferedWriter ds=null;

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
			ds.write("import javax.swing.table.*;\n");
			ds.write("import javax.swing.border.*;\n");
			ds.write("import ADaMSoft.dataaccess.*;\n");
			ds.write("import ADaMSoft.gui.*;\n");
			ds.write("import ADaMSoft.keywords.Keywords;\n");
			ds.write("import ADaMSoft.procedures.*;\n");
			ds.write("import ADaMSoft.utilities.*;\n");
			ds.write("import ADaMSoft.algorithms.*;\n");
			ds.write("import ADaMSoft.algorithms.Algebra2DFile.*;\n");
			ds.write("import ADaMSoft.algorithms.clusters.*;\n");
			ds.write("import ADaMSoft.algorithms.frequencies.*;\n");
			ds.write("import cern.colt.matrix.*;\n");
			ds.write("import cern.colt.matrix.linalg.*;\n");
			String newimplements="";
			for (int i=0; i<Implements.size(); i++)
			{
				newimplements=newimplements+", "+Implements.get(i);
			}
			if (extspecified)
				ds.write("public class Ext"+extname+" extends ADaMSoftFunctions implements Step, Serializable "+newimplements+"{\n");
			else
				ds.write("public class Datastep"+String.valueOf(reffile)+" extends ADaMSoftFunctions implements Step, Serializable "+newimplements+"{\n");
			if (newclassesfords.size()>0)
			{
				for (int i=0; i<newclassesfords.size(); i++)
				{
					ds.write(newclassesfords.get(i)+";\n");
				}
			}
			ds.write("	ADaMSoftSteps AdamsOftS = new ADaMSoftSteps();\n");
			ds.write("	private static final long serialVersionUID = 1L;\n");
			for (int i=0; i<orderedexistentvar.size(); i++)
			{
				String[] vname=orderedexistentvar.get(i);
				if (vname[1].toLowerCase().startsWith(Keywords.NUMSuffix.toLowerCase()))
					ds.write("	double "+vname[0].toLowerCase()+"=Double.NaN;\n");
				else
					ds.write("	String "+vname[0].toLowerCase()+"=\"\";\n");
			}
			ds.write("	DictionaryReader dIct=null;\n");
			ds.write("	DataReader dAta=null;\n");
			ds.write("	Object tEmpdsinfo;\n");
			if (!extspecified)
				ds.write("	String reSExe=\"%2108%<br>\\n\";\n");
			else
				ds.write("	String reSExe=\"%2672% Ext "+extname+"<br>\\n\";\n");
			ds.write("	boolean reSExeB=true;\n");
			ds.write("	boolean resExeStep=true;\n");
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
			for (int i=0; i<extnumpar.size(); i++)
			{
				ds.write("	double "+extnumpar.get(i)+"=Double.NaN;\n");
			}
			for (int i=0; i<exttextpar.size(); i++)
			{
				ds.write("	String "+exttextpar.get(i)+"=\"\";\n");
			}
			ds.write("	public Result executionresult(Hashtable<String, Object> pArameters){\n");
			for (int i=0; i<extnumpar.size(); i++)
			{
				String tempnnumextp=extnumpar.get(i);
				ds.write("		String t_"+tempnnumextp+"_t=(String)pArameters.get(\""+tempnnumextp.toLowerCase()+"\");\n");
				ds.write("		if(t_"+tempnnumextp+"_t!=null) { try { "+tempnnumextp+"=Double.parseDouble(t_"+tempnnumextp+"_t); } catch (Exception ePext) {} }\n");
			}
			for (int i=0; i<exttextpar.size(); i++)
			{
				ds.write("		"+exttextpar.get(i)+"=(String)pArameters.get(\""+exttextpar.get(i).toLowerCase()+"\");\n");
			}
			ds.write("		String[] VALUES = null;\n");
			for (int i=0; i<newaction.size(); i++)
			{
				String tempe=newaction.get(i);
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

				ds.write("		VALUES = dAta.getRecord();\n");
				ds.write("		if (VALUES != null) {");
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
						ds.write("			try{\n");
						ds.write("				"+varname.toLowerCase()+"=Double.parseDouble(VALUES["+j+"].trim());\n");
						ds.write("			}catch (Exception eN) {"+varname.toLowerCase()+"=Double.NaN;}\n");
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
							String vtype=existentvarnamelist.get(tempavals.toLowerCase());
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
							String vtype=existentvarnamelist.get(tempavals.toLowerCase());
							if (vtype.toLowerCase().startsWith(Keywords.NUMSuffix.toLowerCase()))
								ds.write("		"+an+"["+String.valueOf(pnv)+"]="+tempavals.toLowerCase()+";\n");
							else
								ds.write("		"+an+"["+String.valueOf(pnv)+"]=TEXT2NUM("+tempavals.toLowerCase()+");\n");
						}
					}
				}
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
									String vtype=existentvarnamelist.get(tempavals.toLowerCase());
									int pnv=(avals.get(tempavals)).intValue();
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
									String vtype=existentvarnamelist.get(tempavals.toLowerCase());
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
					ds.write("		}\n");
					ds.write("		catch(Exception DSex){\n");
					ds.write("			try{\n");
					ds.write("				StringWriter SWex = new StringWriter();\n");
					ds.write("				PrintWriter PWex = new PrintWriter(SWex);\n");
					ds.write("				DSex.printStackTrace(PWex);\n");
					ds.write("				return new Result(reSExe+SWex.toString()+\"<br>\\n\", false, null);\n");
					ds.write("			}\n");
					ds.write("			catch(Exception DSSex){\n");
					ds.write("				return new Result(reSExe+DSSex.toString()+\"<br>\\n\", false, null);\n");
					ds.write("			}\n");
					ds.write("		}\n");
				}
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
									String vtype=existentvarnamelist.get(tempavals.toLowerCase());
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
									String vtype=existentvarnamelist.get(tempavals.toLowerCase());
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
					ds.write("		}\n");
					ds.write("		catch(Exception DSex){\n");
					ds.write("			try{\n");
					ds.write("				StringWriter SWex = new StringWriter();\n");
					ds.write("				PrintWriter PWex = new PrintWriter(SWex);\n");
					ds.write("				DSex.printStackTrace(PWex);\n");
					ds.write("				return new Result(reSExe+SWex.toString()+\"<br>\\n\", false, null);\n");
					ds.write("			}\n");
					ds.write("			catch(Exception DSSex){\n");
					ds.write("				return new Result(reSExe+DSSex.toString()+\"<br>\\n\", false, null);\n");
					ds.write("			}\n");
					ds.write("		}\n");
				}
			}
			ds.write("		Vector<StepResult> rEsult = new Vector<StepResult>();\n");
			if (!extspecified)
				ds.write("		return new Result(reSExe+\"%2109%<br>\\n\", reSExeB, null);\n");
			else
				ds.write("		return new Result(reSExe+\"%2671%<br>\\n\", reSExeB, null);\n");
			ds.write("	}\n");
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
						boolean addp=false;
						if (actualvarname.indexOf("ADDPARAMETER(")>=0)
							addp=true;
						if (actualvarname.indexOf("ADDPARAMETER ")>=0)
							addp=true;
						if (addp)
						{
							try
							{
								actualvarname=actualvarname.replaceAll("ADDPARAMETER", "AdamsOftS.ADDPARAMETER");
							}
							catch (Exception e) {}
						}
						boolean addpc=false;
						if (actualvarname.indexOf("CLEARPARAMETER(")>=0)
							addpc=true;
						if (actualvarname.indexOf("CLEARPARAMETER ")>=0)
							addpc=true;
						if (addpc)
						{
							try
							{
								actualvarname=actualvarname.replaceAll("CLEARPARAMETER", "AdamsOftS.CLEARPARAMETER");
							}
							catch (Exception e) {}
						}
						boolean addms=false;
						if (actualvarname.indexOf("EXECUTEMACROSTEP(")>=0)
							addms=true;
						if (actualvarname.indexOf("EXECUTEMACROSTEP ")>=0)
							addms=true;
						if (addms)
						{
							try
							{
								actualvarname=actualvarname.replaceAll("EXECUTEMACROSTEP", "resExeStep=AdamsOftS.EXECUTEMACROSTEP");
							}
							catch (Exception e) {}
						}
						boolean execf=false;
						if (actualvarname.indexOf("EXECUTECMDFILE(")>=0)
							execf=true;
						if (actualvarname.indexOf("EXECUTECMDFILE ")>=0)
							execf=true;
						if (execf)
						{
							try
							{
								actualvarname=actualvarname.replaceAll("EXECUTECMDFILE", "resExeStep=AdamsOftS.EXECUTECMDFILE");
							}
							catch (Exception e) {}
						}
						boolean exevcf=false;
						if (actualvarname.indexOf("EXECUTEVOIDCMDFILE(")>=0)
							exevcf=true;
						if (actualvarname.indexOf("EXECUTEVOIDCMDFILE ")>=0)
							exevcf=true;
						if (exevcf)
						{
							try
							{
								actualvarname=actualvarname.replaceAll("EXECUTEVOIDCMDFILE", "AdamsOftS.EXECUTEVOIDCMDFILE");
							}
							catch (Exception e) {}
						}
						boolean exevbcf=false;
						if (actualvarname.indexOf("EXECUTEVOIDBATCHCMDFILE(")>=0)
							exevbcf=true;
						if (actualvarname.indexOf("EXECUTEVOIDBATCHCMDFILE ")>=0)
							exevbcf=true;
						if (exevbcf)
						{
							try
							{
								actualvarname=actualvarname.replaceAll("EXECUTEVOIDBATCHCMDFILE", "AdamsOftS.EXECUTEVOIDBATCHCMDFILE");
							}
							catch (Exception e) {}
						}
						boolean addvms=false;
						if (actualvarname.indexOf("EXECUTEVOIDMACROSTEP(")>=0)
							addvms=true;
						if (actualvarname.indexOf("EXECUTEVOIDMACROSTEP ")>=0)
							addvms=true;
						if (addvms)
						{
							try
							{
								actualvarname=actualvarname.replaceAll("EXECUTEVOIDMACROSTEP", "AdamsOftS.EXECUTEVOIDMACROSTEP");
							}
							catch (Exception e) {}
						}
						boolean addvbms=false;
						if (actualvarname.indexOf("EXECUTEVOIDBATCHMACROSTEP(")>=0)
							addvbms=true;
						if (actualvarname.indexOf("EXECUTEVOIDBATCHMACROSTEP ")>=0)
							addvbms=true;
						if (addvbms)
						{
							try
							{
								actualvarname=actualvarname.replaceAll("EXECUTEVOIDBATCHMACROSTEP", "AdamsOftS.EXECUTEVOIDBATCHMACROSTEP");
							}
							catch (Exception e) {}
						}
						boolean addmst=false;
						if (actualvarname.indexOf("ADDMACROSTEP(")>=0)
							addmst=true;
						if (actualvarname.indexOf("ADDMACROSTEP ")>=0)
							addmst=true;
						if (addmst)
						{
							try
							{
								actualvarname=actualvarname.replaceAll("ADDMACROSTEP", "resExeStep=AdamsOftS.ADDMACROSTEP");
							}
							catch (Exception e) {}
						}
						boolean adde=false;
						if (actualvarname.indexOf("EXECUTESTEP(")>=0)
							adde=true;
						if (actualvarname.indexOf("EXECUTESTEP ")>=0)
							adde=true;
						if (adde)
						{
							try
							{
								actualvarname=actualvarname.replaceAll("EXECUTESTEP", "resExeStep=AdamsOftS.EXECUTESTEP");
							}
							catch (Exception e) {}
						}
						boolean addd=false;
						if (actualvarname.indexOf("ADDDICTACTION(")>=0)
							addd=true;
						if (actualvarname.indexOf("ADDDICTACTION ")>=0)
							addd=true;
						if (addd)
						{
							try
							{
								actualvarname=actualvarname.replaceAll("ADDDICTACTION", "AdamsOftS.ADDDICTACTION");
							}
							catch (Exception e) {}
						}
						boolean added=false;
						if (actualvarname.indexOf("EXECUTEDICTIONARY(")>=0)
							added=true;
						if (actualvarname.indexOf("EXECUTEDICTIONARY ")>=0)
							added=true;
						if (added)
						{
							try
							{
								actualvarname=actualvarname.replaceAll("EXECUTEDICTIONARY", "resExeStep=AdamsOftS.EXECUTEDICTIONARY");
							}
							catch (Exception e) {}
						}
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
			message=Keywords.Language.getMessage(2663)+"<br>\n"+e.toString()+"<br>\n";
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
					if (appendcode)
					{
						try
						{
							BufferedReader in = new BufferedReader(new FileReader(tempjava));
							String strcode="";
							int refrows=1;
							while ((strcode = in.readLine()) != null)
							{
								codetoappend=codetoappend+"("+String.valueOf(refrows)+") "+strcode+"<br>\n";
								refrows++;
							}
							in.close();
						}
						catch (IOException e) {}
						codetoappend=Keywords.Language.getMessage(2187)+"<br>\n"+codetoappend+"<br>\n";
					}
					if (!extspecified)
						(new File(tempjava)).delete();
					try
					{
						message=codetoappend+Keywords.Language.getMessage(2312)+"<br>\n";
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
				message=Keywords.Language.getMessage(2313)+"<br>\n";
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
		if (extspecified)
		{
			message=Keywords.Language.getMessage(2664)+"<br>\n";
			message=message+Keywords.Language.getMessage(2665)+" Ext "+extname+"<br>\n";
			message=message+Keywords.Language.getMessage(2666)+" "+tempclass+"<br>\n";
			message=message+Keywords.Language.getMessage(2669)+" "+tempjava+" "+Keywords.Language.getMessage(2670)+"<br>\n";
			message=message+Keywords.Language.getMessage(2667)+"\n"+Keywords.Language.getMessage(2668)+"<br>\n";
			parameter.clear();
			parameter=null;
			System.gc();
			steperror=false;
			return;
		}
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
			classtoexecute=null;
		}
		catch (Exception e)
		{
			message=message+Keywords.Language.getMessage(2314)+"<br>\n"+e.toString()+"<br>\n";
			if (appendcode)
			{
				try
				{
					BufferedReader in = new BufferedReader(new FileReader(tempjava));
					String strcode="";
					int refrows=1;
					while ((strcode = in.readLine()) != null)
					{
						codetoappend=codetoappend+"("+String.valueOf(refrows)+") "+strcode+"<br>\n";
						refrows++;
					}
					in.close();
				}
				catch (IOException eee) {}
				codetoappend=Keywords.Language.getMessage(2334)+"<br>\n"+codetoappend+"<br>\n";
			}
			(new File(tempjava)).delete();
			message=message+codetoappend+"<br>\n";
			(new File(tempclass)).delete();
			steperror=true;
			return;
		}
		if (executionresult==null)
		{
			message=Keywords.Language.getMessage(2316)+"<br>\n";
			parameter.clear();
			parameter=null;
			System.gc();
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
						while ((strcode = in.readLine()) != null)
						{
							codetoappend=codetoappend+"("+String.valueOf(refrows)+") "+strcode+"<br>\n";
							refrows++;
						}
						in.close();
					}
					catch (IOException e) {}
					codetoappend=Keywords.Language.getMessage(2334)+"\n"+codetoappend+"\n";
				}
				message=message+codetoappend+"\n";
				(new File(tempjava)).delete();
				(new File(tempclass)).delete();
			}
			parameter.clear();
			parameter=null;
			executionresult=null;
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
					steperror=true;
			}
			executionresult=null;
		}
		parameter.clear();
		parameter=null;
		executionresult=null;
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
