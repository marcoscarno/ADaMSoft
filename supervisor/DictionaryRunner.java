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

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import ADaMSoft.dataaccess.DictionaryReader;
import ADaMSoft.keywords.Keywords;
import ADaMSoft.procedures.LocalDictionaryWriter;
import ADaMSoft.utilities.AddCodeLabelVerifier;
import ADaMSoft.utilities.AddMissingDataVerifier;
import ADaMSoft.utilities.ScriptParserUtilities;
import ADaMSoft.utilities.CheckVarNames;

/**
* Change the values inside a dictionary
* @author marco.scarno@gmail.com
* @date 02/09/2015
*/
public class DictionaryRunner extends ScriptParserUtilities
{
	String message;
	boolean steperror;
	public DictionaryRunner(Vector<?> KeywordValue)
	{
		message="";
		steperror=false;
		String pathdictin="";
		String pathdictout="";
		String namedictin="";
		String namedictout="";
		Vector<String> action=new Vector<String>();
		for (int i=0; i<KeywordValue.size(); i++)
		{
			String actualvalue=(String)KeywordValue.get(i);
			actualvalue=actualvalue.trim();
			actualvalue=MultipleSpacesReplacer(actualvalue);
			if (actualvalue.toLowerCase().startsWith(Keywords.DICTIONARY.toLowerCase()+" "))
			{
				String statement="";
				try
				{
					statement=actualvalue.substring(actualvalue.indexOf(" "));
					statement=statement.trim();
					statement=SpacesBetweenEqualReplacer(statement);
					statement=statement.trim();
				}
				catch (Exception e)
				{
					message=Keywords.Language.getMessage(257)+"<br>\n";
					steperror=true;
					return;
				}
				String[] statementparts=statement.split(" ");
				for (int j=0; j<statementparts.length; j++)
				{
					if (statementparts[j].toLowerCase().startsWith(Keywords.dict))
					{
						String[] dictin=statementparts[j].split("=");
						if (dictin.length!=2)
						{
							message=Keywords.Language.getMessage(257)+"<br>\n";
							steperror=true;
							return;
						}
						String[] newdictin=dictin[1].split("\\.");
						if (newdictin.length==1)
							namedictin=newdictin[0].trim();
						if (newdictin.length==2)
						{
							pathdictin=newdictin[0].trim();
							namedictin=newdictin[1].trim();
						}
					}
					if (statementparts[j].toLowerCase().startsWith(Keywords.outdict))
					{
						String[] dictout=statementparts[j].split("=");
						if (dictout.length!=2)
						{
							message=Keywords.Language.getMessage(257)+"<br>\n";
							steperror=true;
							return;
						}
						dictout=dictout[1].split("\\.");
						if (dictout.length==1)
							namedictout=dictout[0];
						if (dictout.length==2)
						{
							pathdictout=dictout[0];
							namedictout=dictout[1];
						}
					}
				}
			}
			else if (!actualvalue.toLowerCase().startsWith(Keywords.RUN.toLowerCase()))
				action.add(actualvalue);
		}
		if (namedictin.equals(""))
		{
			message=Keywords.Language.getMessage(258)+"<br>\n";
			steperror=true;
			return;
		}
		if (pathdictin.equalsIgnoreCase("WORK"))
			pathdictin="";
		if (!pathdictin.equals(""))
		{
			String oldpathin=pathdictin;
			pathdictin=Keywords.project.getPath(pathdictin);
			if (pathdictin.equals(""))
			{
				message=Keywords.Language.getMessage(61)+" ("+oldpathin+")<br>\n";
				steperror=true;
				return;
			}
		}
		else
			pathdictin=System.getProperty(Keywords.WorkDir);
		if (pathdictout.equalsIgnoreCase("WORK"))
			pathdictout="";
		if (!pathdictout.equals(""))
		{
			String oldpathin=pathdictout;
			pathdictout=Keywords.project.getPath(pathdictout);
			if (pathdictout.equals(""))
			{
				message=Keywords.Language.getMessage(61)+" ("+oldpathin+")<br>\n";
				steperror=true;
				return;
			}
		}
		else
			pathdictout=System.getProperty(Keywords.WorkDir);
		DictionaryReader dr=new DictionaryReader(pathdictin+namedictin);
		if (!dr.getmessageDictionaryReader().equals(""))
		{
			message=dr.getmessageDictionaryReader();
			steperror=true;
			return;
		}
		String keyword=dr.getkeyword();
		String description=dr.getdescription();
		String author=dr.getauthor();
		String datatabletype=dr.getdatatabletype();
		String changedatapath="";
		String newpath="";
		Hashtable<String, String> tableinfo=dr.getdatatableinfo();
		Vector<Hashtable<String, String>> fixedvariableinfo=dr.getfixedvariableinfo();
		Vector<Hashtable<String, String>> tablevariableinfo=dr.gettablevariableinfo();
		Vector<Hashtable<String, String>> codelabel=dr.getcodelabel();
		Vector<Hashtable<String, String>> missingdata=dr.getmissingdata();

		Object vclass=dr.getviewclass();
		String vclassr="";
		Hashtable<String, Object> vclassp=null;
		if (vclass!=null)
		{
			vclassr=dr.getviewclassref();
			vclassr=dr.getviewclassref();
			vclassp=dr.getparameters();
		}

		for (int i=0; i<action.size(); i++)
		{
			String currentaction=action.get(i).trim();
			if (currentaction.toLowerCase().startsWith(Keywords.changedatapath.toLowerCase()))
			{
				try
				{
					changedatapath=currentaction.substring((Keywords.changedatapath).length());
					changedatapath=changedatapath.trim();
				}
				catch (Exception e)
				{
					message=message+Keywords.Language.getMessage(2714)+" ("+currentaction+")<br>\n";
					steperror=true;
					return;
				}
				if (changedatapath.equals(""))
				{
					message=message+Keywords.Language.getMessage(2714)+" ("+currentaction+")<br>\n";
					steperror=true;
					return;
				}
				try
				{
					newpath=Keywords.project.getPath(changedatapath);
					if (newpath==null)
					{
						message=Keywords.Language.getMessage(61)+" ("+changedatapath+")<br>\n";
						steperror=true;
						return;
					}
					if (newpath.equals(""))
					{
						message=Keywords.Language.getMessage(61)+" ("+changedatapath+")<br>\n";
						steperror=true;
						return;
					}
				}
				catch (Exception e)
				{
					message=Keywords.Language.getMessage(61)+" ("+changedatapath+")<br>\n";
					steperror=true;
					return;
				}
				boolean ispossibile=false;
				for (int j=0; j<Keywords.datatypewithpath.length; j++)
				{
					if ((Keywords.datatypewithpath[j].trim()).equalsIgnoreCase(datatabletype.trim()))
						ispossibile=true;
				}
				if (!ispossibile)
				{
					message=Keywords.Language.getMessage(2715)+" ("+datatabletype.toUpperCase()+")<br>\n";
					steperror=true;
					return;
				}
			}
			if (currentaction.toLowerCase().startsWith(Keywords.descriptiondictionary.toLowerCase()))
			{
				String value="";
				try
				{
					value=currentaction.substring((Keywords.descriptiondictionary).length());
					value=value.trim();
				}
				catch (Exception e) {}
				if (!value.equals(""))
				{
					description=value;
					message=message+Keywords.Language.getMessage(259)+" "+description+"<br>\n";
				}
			}
			if (currentaction.toLowerCase().startsWith(Keywords.keyworddictionary.toLowerCase()))
			{
				String value="";
				try
				{
					value=currentaction.substring((Keywords.keyworddictionary).length());
					value=value.trim();
				}
				catch (Exception e) {}
				if (!value.equals(""))
				{
					keyword=value;
					message=message+Keywords.Language.getMessage(260)+" "+keyword+"<br>\n";
				}
			}
			if (currentaction.toLowerCase().startsWith(Keywords.labeldictionary.toLowerCase()))
			{
				String value="";
				try
				{
					value=currentaction.substring((Keywords.labeldictionary).length());
					value=value.trim();
				}
				catch (Exception e) {}
				if (!value.equals(""))
				{
					String [] partlabel = value.split("=");
					if (partlabel.length>=2)
					{
						String templabel="";
						for (int j=1; j<partlabel.length; j++)
						{
							templabel=templabel+partlabel[j];
							if (j<(partlabel.length-1))
								templabel=templabel+"=";
						}
						for (int j=0; j<fixedvariableinfo.size(); j++)
						{
							Hashtable<String, String> temp=fixedvariableinfo.get(j);
							String check=temp.get(Keywords.VariableName.toLowerCase());
							if (check.equalsIgnoreCase(partlabel[0]))
							{
								temp.put(Keywords.LabelOfVariable.toLowerCase(), templabel);
								fixedvariableinfo.set(j, temp);
								message=message+Keywords.Language.getMessage(261)+" "+check+"-->"+templabel+"<br>\n";
								break;
							}
						}
					}
				}
			}
			if (currentaction.toLowerCase().startsWith(Keywords.writefmtdictionary.toLowerCase()))
			{
				String value="";
				try
				{
					value=currentaction.substring((Keywords.writefmtdictionary).length());
					value=value.trim();
				}
				catch (Exception e) {}
				if (!value.equals(""))
				{
					String [] partformat = value.split("=");
					if (partformat.length==2)
					{
						if (!WriteFormatVerifier(partformat[1]))
							message=message+Keywords.Language.getMessage(263)+"<br>\n";
						else
						{
							for (int j=0; j<fixedvariableinfo.size(); j++)
							{
								Hashtable<String, String> temp=fixedvariableinfo.get(j);
								String check=temp.get(Keywords.VariableName.toLowerCase());
								if (check.equalsIgnoreCase(partformat[0]))
								{
									temp.put(Keywords.VariableFormat.toLowerCase(), partformat[1]);
									fixedvariableinfo.set(j, temp);
									message=message+Keywords.Language.getMessage(262)+" "+check+"-->"+partformat[1]+"<br>\n";
									break;
								}
							}
						}
					}
				}
			}
			if (currentaction.toLowerCase().startsWith(Keywords.rename.toLowerCase()))
			{
				String value="";
				try
				{
					value=currentaction.substring((Keywords.rename).length());
					value=value.trim();
				}
				catch (Exception e) {}
				if (!value.equals(""))
				{
					String [] partrename = value.split("=");
					if (partrename.length==2)
					{
						boolean exist=false;
						for (int j=0; j<fixedvariableinfo.size(); j++)
						{
							Hashtable<String, String> temp=fixedvariableinfo.get(j);
							String check=temp.get(Keywords.VariableName.toLowerCase());
							if (check.equalsIgnoreCase(partrename[1]))
							{
								exist=true;
								break;
							}
						}
						if (exist)
							message=message+Keywords.Language.getMessage(264)+" ("+partrename[0]+")<br>\n";
						else
						{
							for (int j=0; j<fixedvariableinfo.size(); j++)
							{
								Hashtable<String, String> temp=fixedvariableinfo.get(j);
								String check=temp.get(Keywords.VariableName.toLowerCase());
								if (check.equalsIgnoreCase(partrename[0]))
								{
									temp.put(Keywords.VariableName.toLowerCase(), partrename[1]);
									fixedvariableinfo.set(j, temp);
									message=message+Keywords.Language.getMessage(265)+" "+check+"-->"+partrename[1]+"<br>\n";
									break;
								}
							}
						}
					}
				}
			}
			if (currentaction.toLowerCase().startsWith(Keywords.addcodelabeldictionary.toLowerCase()))
			{
				String value="";
				try
				{
					value=currentaction.substring((Keywords.addcodelabeldictionary).length());
					value=value.trim();
				}
				catch (Exception e) {}
				if (!value.equals(""))
				{
					int typecode=0;
					String [] codelabelresult=value.split("=");
					if (codelabelresult.length==2)
					{
						typecode=1;
						if (((codelabelresult[1].toLowerCase()).indexOf(Keywords.delete))>=0)
							typecode=2;
						if (codelabelresult[1].indexOf("@")>=0)
							typecode=3;
					}
					if (codelabelresult.length==3)
					{
						typecode=4;
						if (((codelabelresult[2].toLowerCase()).indexOf(Keywords.delete))>=0)
							typecode=5;
					}
					if (typecode==1)
					{
						String namevar=codelabelresult[0].trim();
						String nameset=codelabelresult[1];
						Hashtable<String,String> settingcodelabel=Keywords.project.getSetting(Keywords.CODELABEL,nameset);
						if (!settingcodelabel.isEmpty())
						{
							for (int j=0; j<fixedvariableinfo.size(); j++)
							{
								Hashtable<String, String> temp=fixedvariableinfo.get(j);
								String check=temp.get(Keywords.VariableName.toLowerCase());
								if (check.equalsIgnoreCase(namevar))
								{
									Hashtable<String, String> tempcodelabel=codelabel.get(j);
									for (Enumeration<String> en=settingcodelabel.keys(); en.hasMoreElements();)
									{
										String newcode=en.nextElement();
										String newvalue=settingcodelabel.get(newcode);
										newcode=newcode.substring(newcode.indexOf("_")+1);
										tempcodelabel.put(newcode, newvalue);
									}
									codelabel.set(j, tempcodelabel);
									message=message+Keywords.Language.getMessage(266)+" "+namevar+"-->"+nameset+"<br>\n";
									break;
								}
							}
						}
					}
					if (typecode==2)
					{
						String namevar=codelabelresult[0].trim();
						for (int j=0; j<fixedvariableinfo.size(); j++)
						{
							Hashtable<String, String> temp=fixedvariableinfo.get(j);
							String check=temp.get(Keywords.VariableName.toLowerCase());
							if (check.equalsIgnoreCase(namevar))
							{
								Hashtable<String, String> tempcl=new Hashtable<String, String>();
								codelabel.set(j, tempcl);
								message=message+Keywords.Language.getMessage(267)+" "+namevar+"<br>\n";
								break;
							}
						}
					}
					if (typecode==3)
					{
						String namevar=codelabelresult[0].trim();
						String namedict=codelabelresult[1];
						String othername=namedict.substring(0,namedict.indexOf("@"));
						namedict=namedict.substring(namedict.indexOf("@")+1);
						String dir=System.getProperty(Keywords.WorkDir);
						if (namedict.indexOf(".")>0)
						{
							String [] pathparts=namedict.split("\\.");
							if (pathparts.length!=2)
							{
								message=Keywords.Language.getMessage(268)+"<br>\n";
								steperror=true;
								return;
							}
							namedict=pathparts[1];
							dir=Keywords.project.getPath(pathparts[0]);
							if (dir.equals(""))
							{
								message=message+Keywords.Language.getMessage(32)+" ("+pathparts[0]+")<br>\n";
								steperror=true;
								return;
							}
						}
						namedict=dir+namedict;
						DictionaryReader newdr=new DictionaryReader(namedict);
						if (!newdr.getmessageDictionaryReader().equals(""))
						{
							message=message+newdr.getmessageDictionaryReader();
							steperror=true;
							return;
						}
						Vector<Hashtable<String, String>> newfixedvariableinfo=newdr.getfixedvariableinfo();
						Vector<Hashtable<String, String>> newcodelabel=newdr.getcodelabel();
						for (int j=0; j<newfixedvariableinfo.size(); j++)
						{
							Hashtable<String, String> newtemp=newfixedvariableinfo.get(j);
							String newcheck=newtemp.get(Keywords.VariableName.toLowerCase());
							if (newcheck.equalsIgnoreCase(othername))
							{
								Hashtable<String, String> tempnewcodelabel= newcodelabel.get(j);
								for (int h=0; h<fixedvariableinfo.size(); h++)
								{
									Hashtable<String, String> temp=fixedvariableinfo.get(h);
									String check=temp.get(Keywords.VariableName.toLowerCase());
									if (check.equalsIgnoreCase(namevar))
									{
										if (tempnewcodelabel.size()>0)
										{
											Hashtable<String, String> tempcodelabel= codelabel.get(j);
											for (Enumeration<String> en=tempnewcodelabel.keys(); en.hasMoreElements();)
											{
												String newcode=en.nextElement();
												String newvalue=tempnewcodelabel.get(newcode);
												tempcodelabel.put(newcode, newvalue);
											}
											codelabel.set(h, tempcodelabel);
											message=message+Keywords.Language.getMessage(269)+" ("+othername+"@"+namedict+")<br>\n";
										}
										break;
									}
								}
							}
						}
					}
					if (typecode==4)
					{
						String namevar=codelabelresult[0].trim();
						String valcode=codelabelresult[1].trim();
						AddCodeLabelVerifier aclv=new AddCodeLabelVerifier(valcode);
						if (!aclv.getMessage().equals(""))
							message=message+Keywords.Language.getMessage(270)+")<br>\n";
						else
						{
							for (int j=0; j<fixedvariableinfo.size(); j++)
							{
								Hashtable<String, String> temp=fixedvariableinfo.get(j);
								String check=temp.get(Keywords.VariableName.toLowerCase());
								if (check.equalsIgnoreCase(namevar))
								{
									Hashtable<String, String> tempcodelabel=codelabel.get(j);
									tempcodelabel.put(valcode, codelabelresult[2].trim());
									codelabel.set(j, tempcodelabel);
									message=message+Keywords.Language.getMessage(271)+" "+namevar+" ("+valcode+"->"+codelabelresult[2]+")<br>\n";
									break;
								}
							}
						}
					}
					if (typecode==5)
					{
						String namevar=codelabelresult[0].trim();
						String valcode=codelabelresult[1].trim();
						for (int j=0; j<fixedvariableinfo.size(); j++)
						{
							Hashtable<String, String> temp=fixedvariableinfo.get(j);
							String check=temp.get(Keywords.VariableName.toLowerCase());
							if (check.equalsIgnoreCase(namevar))
							{
								Hashtable<String, String> tempcodelabel=codelabel.get(j);
								tempcodelabel.remove(valcode);
								codelabel.set(j, tempcodelabel);
								message=message+Keywords.Language.getMessage(272)+" "+namevar+" ("+valcode+")<br>\n";
								break;
							}
						}
					}
				}
			}
			if (currentaction.toLowerCase().startsWith(Keywords.addmd.toLowerCase()))
			{
				String value="";
				try
				{
					value=currentaction.substring((Keywords.addmd).length());
					value.trim();
				}
				catch (Exception e) {}
				if (!value.equals(""))
				{
					String [] mdresult=value.split("=");
					if (mdresult.length==2)
					{
						String namevar=mdresult[0].trim();
						if (((mdresult[1].toLowerCase()).indexOf(Keywords.delete))>=0)
						{
							for (int j=0; j<fixedvariableinfo.size(); j++)
							{
								Hashtable<String, String> temp=fixedvariableinfo.get(j);
								String check=temp.get(Keywords.VariableName.toLowerCase());
								if (check.equalsIgnoreCase(namevar))
								{
									Hashtable<String, String> tempmd=new Hashtable<String, String>();
									missingdata.set(j, tempmd);
									message=message+Keywords.Language.getMessage(277)+" "+namevar+"<br>\n";
									break;
								}
							}
						}
						else
						{
							for (int j=0; j<fixedvariableinfo.size(); j++)
							{
								Hashtable<String, String> temp=fixedvariableinfo.get(j);
								String check=temp.get(Keywords.VariableName.toLowerCase());
								if (check.equalsIgnoreCase(namevar))
								{
									Hashtable<String, String> tempmissingdata=missingdata.get(j);
									if (!AddMissingDataVerifier.getresult(mdresult[1]))
										message=message+AddMissingDataVerifier.messageAddMissingDataVerifier;
									else
									{
										tempmissingdata.put(mdresult[1],"");
										missingdata.set(j, tempmissingdata);
										message=message+Keywords.Language.getMessage(278)+" "+namevar+"<br>\n";
									}
									break;
								}
							}
						}
					}
					if (mdresult.length==3)
					{
						String namevar=mdresult[0].trim();
						if (mdresult[2].equalsIgnoreCase(Keywords.delete))
						{
							for (int j=0; j<fixedvariableinfo.size(); j++)
							{
								Hashtable<String, String> temp=fixedvariableinfo.get(j);
								String check=temp.get(Keywords.VariableName.toLowerCase());
								if (check.equalsIgnoreCase(namevar))
								{
									missingdata.remove(mdresult[1]);
									message=message+Keywords.Language.getMessage(279)+" "+namevar+"<br>\n";
									break;
								}
							}
						}
					}
				}
			}
		}
		namedictin=pathdictin+namedictin;
		if (namedictout.equals(""))
			namedictout=namedictin;
		else
			namedictout=pathdictout+namedictout;
		String workdir=System.getProperty(Keywords.WorkDir);
		if (!CheckVarNames.getResultCheck(fixedvariableinfo, workdir).equals(""))
		{
			message=message+CheckVarNames.getResultCheck(fixedvariableinfo, workdir);
			steperror=true;
			return;
		}

		LocalDictionaryWriter ldw=new LocalDictionaryWriter(namedictout, keyword, description, author, datatabletype,
		tableinfo, fixedvariableinfo, tablevariableinfo, codelabel, missingdata, null);

		if (vclass!=null)
		{
			ldw.setviewclass(vclass);
			ldw.setviewparameter(vclassp);
			ldw.setviewclassref(vclassr);
		}
		if (!changedatapath.equals(""))
		{
			ldw.setnewpath(newpath);
		}

		String resultldw=ldw.action();
		if (resultldw.length()>=2)
			message=message+(resultldw.substring(2)).trim()+"<br>\n";
		if (resultldw.startsWith("0"))
			steperror=true;
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
