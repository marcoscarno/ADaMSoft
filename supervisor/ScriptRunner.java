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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.Vector;
import org.apache.commons.lang.StringEscapeUtils;

import ADaMSoft.keywords.Keywords;
import ADaMSoft.utilities.ScriptParserUtilities;

/**
* Executes the command script
* @author marco.scarno@gmail.com
* @date 13/06/2019
*/
public class ScriptRunner extends ScriptParserUtilities
{
	Vector<String> newstep;
	String message;
	boolean steperror, currentsteperror;
	String[] scriptparts;
	public ScriptRunner (Vector<String> step)
	{
		Keywords.percentage_total=0;
		Keywords.percentage_done=0;
		newstep=new Vector<String>();
		message="";
		if (Keywords.stop_script)
		{
			Keywords.percentage_total=0;
			Keywords.percentage_done=0;
			Keywords.currentExecutedStep="";
			currentsteperror=true;
			return;
		}
		steperror=false;
		currentsteperror=false;
		boolean currentwritelog=false;
		if ((System.getProperty("writelog")).equals("yes"))
			currentwritelog=true;
		try
		{
			boolean ismacrostep=false;
			try
			{
				String [] actualKeyword=step.get(0).split(" ");
				if (actualKeyword[0].equalsIgnoreCase(Keywords.MACROSTEP))
					ismacrostep=true;
			}
			catch (Exception em) {}
			if (!ismacrostep)
			{
				for (int s=0; s<step.size(); s++)
				{
					String actualstatement=step.get(s);
					if (actualstatement.indexOf("&")>=0)
					{
						TreeMap<String, String> checkkey=Keywords.project.getNamesAndDefinitions();
						for (Iterator<String> it = checkkey.keySet().iterator(); it.hasNext();)
						{
							String keyname = it.next();
							String keyvalue=checkkey.get(keyname);
							actualstatement=ReplaceDefine(actualstatement, "&"+keyname, keyvalue);
						}
						step.set(s, actualstatement);
					}
				}
			}
			for (int i=0; i<step.size(); i++)
			{
				if (step.get(i).indexOf(";")>=0)
				{
					scriptparts=(step.get(i)).split(";");
					for (int j=0; j<scriptparts.length; j++)
					{
						scriptparts[j]=scriptparts[j].trim();
						if (!scriptparts[j].equals(""))
							newstep.add(scriptparts[j].trim());
					}
				}
				else newstep.add(step.get(i));
			}
			if (newstep.size()==1)
			{
				String actualstatement=newstep.get(0).trim();
				String [] actualKeyword=actualstatement.split(" ");
				for (int i=0; i<Keywords.SimpleKeywords.length; i++)
				{
					if (actualKeyword[0].equalsIgnoreCase(Keywords.SimpleKeywords[i]))
					{
						if (currentwritelog)
						{
							if (actualKeyword[0].equalsIgnoreCase(Keywords.MSG))
							{
								message=message+"<b>"+StringEscapeUtils.escapeHtml(actualstatement)+"</b><br><br>\n";
							}
							else
								message=message+actualstatement+";<br>\n";
						}
						if (actualKeyword[0].equalsIgnoreCase(Keywords.PATH))
						{
							Keywords.currentExecutedStep="PATH";
							PathRunner pathrunner=new PathRunner(actualstatement);
							currentsteperror=pathrunner.getError();
							if (currentsteperror)
								message=message+"<font color=red>"+pathrunner.getMessage()+"</font><br>\n";
							if (!currentsteperror && currentwritelog)
								message=message+"<font color=green>"+pathrunner.getMessage()+"</font><br>\n";
							Keywords.currentExecutedStep="";
						}
						else if (actualKeyword[0].equalsIgnoreCase(Keywords.EXPORTDS2OUT))
						{
							Keywords.currentExecutedStep="EXPORTDSOUT";
							DSExporter dsexporter=new DSExporter(actualstatement);
							currentsteperror=dsexporter.getError();
							if (currentsteperror)
								message=message+"<font color=red>"+dsexporter.getMessage()+"</font><br>\n";
							if (!currentsteperror && currentwritelog && !dsexporter.getMessage().equals(""))
								message=message+"<font color=blue>"+dsexporter.getMessage()+"</font><br>\n";
							Keywords.currentExecutedStep="";
						}
						else if (actualKeyword[0].equalsIgnoreCase(Keywords.EXEMACROSTEP))
						{
							Keywords.currentExecutedStep="EXEMACROSTEP";
							MacroStepExecutor mstepexecutor=new MacroStepExecutor(actualstatement);
							currentsteperror=mstepexecutor.getError();
							if (currentsteperror && !mstepexecutor.getMessage().equals(""))
								message=message+"<font color=red>"+mstepexecutor.getMessage()+"</font><br>\n";
							Keywords.currentExecutedStep="";
						}
						else if (actualKeyword[0].equalsIgnoreCase(Keywords.DELMACROSTEP))
						{
							Keywords.currentExecutedStep="DELMACROSTEP";
							MacroStepDeleter mstepdeleter=new MacroStepDeleter(actualstatement);
							currentsteperror=mstepdeleter.getError();
							if (currentsteperror)
								message=message+"<font color=red>"+mstepdeleter.getMessage()+"</font><br>\n";
							if (!currentsteperror && currentwritelog)
								message=message+mstepdeleter.getMessage();
							Keywords.currentExecutedStep="";
						}
						else if (actualKeyword[0].equalsIgnoreCase(Keywords.EXECUTE))
						{
							Keywords.currentExecutedStep="EXECUTE";
							ExecuteRunner erunner=new ExecuteRunner(1, actualstatement);
							currentsteperror=erunner.getError();
							Keywords.currentExecutedStep="";
						}
						else if (actualKeyword[0].equalsIgnoreCase(Keywords.OPTION))
						{
							Keywords.currentExecutedStep="OPTION";
							actualstatement=actualstatement.replaceAll("\\s+", " ");
							String [] optionvalue=actualstatement.split(" ");
							if (optionvalue.length>=2)
							{
								if (optionvalue[1].equalsIgnoreCase(Keywords.numdecimals))
								{
									String tempoptionv="";
									if (optionvalue.length>2)
									{
										for (int y=1; y<optionvalue.length; y++)
										{
											tempoptionv=tempoptionv+optionvalue[y].trim();
										}
										optionvalue=new String[2];
										optionvalue[1]=tempoptionv;
									}
								}
								else if (optionvalue[1].equalsIgnoreCase(Keywords.dateformat))
								{
									String tempoptionv="";
									if (optionvalue.length>2)
									{
										for (int y=1; y<optionvalue.length; y++)
										{
											tempoptionv=tempoptionv+optionvalue[y].trim();
										}
										optionvalue=new String[2];
										optionvalue[1]=tempoptionv;
									}
								}
								else if (optionvalue[1].toLowerCase().startsWith(Keywords.MaxDBRecords.toLowerCase()))
								{
									String tempoptionv="";
									if (optionvalue.length>2)
									{
										for (int y=1; y<optionvalue.length; y++)
										{
											tempoptionv=tempoptionv+optionvalue[y].trim();
										}
										optionvalue=new String[2];
										optionvalue[1]=tempoptionv;
									}
								}
								else if (optionvalue[1].toLowerCase().startsWith(Keywords.script_extension.toLowerCase()))
								{
									String tempoptionv="";
									if (optionvalue.length>2)
									{
										for (int y=1; y<optionvalue.length; y++)
										{
											tempoptionv=tempoptionv+optionvalue[y].trim();
										}
										optionvalue=new String[2];
										optionvalue[1]=tempoptionv;
									}
								}
								else if (optionvalue[1].toLowerCase().startsWith(Keywords.proxyHost.toLowerCase()))
								{
									String tempoptionv="";
									if (optionvalue.length>2)
									{
										for (int y=1; y<optionvalue.length; y++)
										{
											tempoptionv=tempoptionv+optionvalue[y].trim();
										}
										optionvalue=new String[2];
										optionvalue[1]=tempoptionv;
									}
								}
								else if (optionvalue[1].toLowerCase().startsWith(Keywords.proxyPort.toLowerCase()))
								{
									String tempoptionv="";
									if (optionvalue.length>2)
									{
										for (int y=1; y<optionvalue.length; y++)
										{
											tempoptionv=tempoptionv+optionvalue[y].trim();
										}
										optionvalue=new String[2];
										optionvalue[1]=tempoptionv;
									}
								}
								else if (optionvalue[1].toLowerCase().startsWith(Keywords.proxyUser.toLowerCase()))
								{
									String tempoptionv="";
									if (optionvalue.length>2)
									{
										for (int y=1; y<optionvalue.length; y++)
										{
											tempoptionv=tempoptionv+optionvalue[y].trim();
										}
										optionvalue=new String[2];
										optionvalue[1]=tempoptionv;
									}
								}
								else if (optionvalue[1].toLowerCase().startsWith(Keywords.proxyPassword.toLowerCase()))
								{
									String tempoptionv="";
									if (optionvalue.length>2)
									{
										for (int y=1; y<optionvalue.length; y++)
										{
											tempoptionv=tempoptionv+optionvalue[y].trim();
										}
										optionvalue=new String[2];
										optionvalue[1]=tempoptionv;
									}
								}
								else if (optionvalue[1].toLowerCase().startsWith(Keywords.MaxDataBuffered.toLowerCase()))
								{
									String tempoptionv="";
									if (optionvalue.length>2)
									{
										for (int y=1; y<optionvalue.length; y++)
										{
											tempoptionv=tempoptionv+optionvalue[y].trim();
										}
										optionvalue=new String[2];
										optionvalue[1]=tempoptionv;
									}
								}
								else if (optionvalue[1].toLowerCase().startsWith(Keywords.FileBufferDim.toLowerCase()))
								{
									String tempoptionv="";
									if (optionvalue.length>2)
									{
										for (int y=1; y<optionvalue.length; y++)
										{
											tempoptionv=tempoptionv+optionvalue[y].trim();
										}
										optionvalue=new String[2];
										optionvalue[1]=tempoptionv;
									}
								}
							}
							if (optionvalue.length==2)
							{
								if (optionvalue[1].equalsIgnoreCase(Keywords.nohaltonerror))
								{
									message=message+"<font color=green>"+Keywords.Language.getMessage(15)+"</font><br>\n";
									System.setProperty("halton", "no");
								}
								else if (optionvalue[1].equalsIgnoreCase(Keywords.haltonerror))
								{
									message=message+"<font color=green>"+Keywords.Language.getMessage(16)+"</font><br>\n";
									System.setProperty("halton", "yes");
								}
								else if (optionvalue[1].equalsIgnoreCase(Keywords.nolog))
								{
									message=message+"<font color=green>"+Keywords.Language.getMessage(17)+"</font><br>\n";
									System.setProperty("writelog", "no");
								}
								else if (optionvalue[1].equalsIgnoreCase(Keywords.log))
								{
									message="<font color=green>"+Keywords.Language.getMessage(18)+"</font><br>\n";
									System.setProperty("writelog", "yes");
								}
								else if (optionvalue[1].equalsIgnoreCase(Keywords.DEBUG))
								{
									message=message+"<font color=green>"+Keywords.Language.getMessage(2022)+"</font><br>\n";
									System.setProperty("DEBUG","true");
								}
								else if (optionvalue[1].equalsIgnoreCase(Keywords.NODEBUG))
								{
									message=message+"<font color=green>"+Keywords.Language.getMessage(2023)+"</font><br>\n";
									System.setProperty("DEBUG","false");
								}
								else if (optionvalue[1].equalsIgnoreCase(Keywords.uselocalefornumbers))
								{
									message=message+"<font color=green>"+Keywords.Language.getMessage(2222)+"</font><br>\n";
									System.setProperty("uselocalefornumbers","true");
								}
								else if (optionvalue[1].equalsIgnoreCase(Keywords.nouselocalefornumbers))
								{
									message=message+"<font color=green>"+Keywords.Language.getMessage(2223)+"</font><br>\n";
									System.setProperty("uselocalefornumbers","false");
								}
								else if (optionvalue[1].toLowerCase().startsWith(Keywords.numdecimals.toLowerCase()))
								{
									try
									{
										String tempvalue=optionvalue[1].trim();
										String[] ttempvalue=tempvalue.split("=");
										int maxdec=Integer.parseInt(ttempvalue[1]);
										message=message+"<font color=green>"+Keywords.Language.getMessage(2224)+" ("+optionvalue[1]+")</font><br>\n";
										System.setProperty("numdecimals",String.valueOf(maxdec));
									}
									catch (NumberFormatException nfe)
									{
										message=message+"<font color=red>"+Keywords.Language.getMessage(2225)+" ("+optionvalue[1]+")</font><br>\n";
									}
								}
								else if (optionvalue[1].toLowerCase().startsWith(Keywords.script_extension.toLowerCase()))
								{
									try
									{
										String tempvalue=optionvalue[1].trim();
										String[] ttempvalue=tempvalue.split("=");
										if (ttempvalue.length==2)
										{
											if (ttempvalue[1].startsWith("."))
											{
												Keywords.ScriptExtension=ttempvalue[1];
												message=message+"<font color=green>"+Keywords.Language.getMessage(4013)+": "+ttempvalue[1]+"</font><br>\n";
											}
											else
												message=message+"<font color=red>"+Keywords.Language.getMessage(4102)+"</font><br>\n";
										}
										else
										{
											message=message+"<font color=red>"+Keywords.Language.getMessage(4102)+"</font><br>\n";
										}
									}
									catch (Exception nfe)
									{
										message=message+"<font color=red>"+Keywords.Language.getMessage(4102)+"</font><br>\n";
									}
								}
								else if (optionvalue[1].toLowerCase().startsWith(Keywords.dateformat.toLowerCase()))
								{
									try
									{
										String tempvalue=optionvalue[1].trim();
										String[] ttempvalue=tempvalue.split("=");
										message=message+"<font color=green>"+Keywords.Language.getMessage(2700)+" ("+ttempvalue[1]+")</font><br>\n";
										System.setProperty("writedatefmt", ttempvalue[1]);
									}
									catch (Exception nfe)
									{
										message=message+"<font color=red>"+Keywords.Language.getMessage(2701)+"</font><br>\n";
									}
								}
								else if (optionvalue[1].toLowerCase().startsWith(Keywords.MaxDBRecords.toLowerCase()))
								{
									try
									{
										String[] tempMaxDBRecords=optionvalue[1].split("=");
										String tempvalue=tempMaxDBRecords[1].trim();
										int MaxDBRecords=Integer.parseInt(tempvalue);
										System.setProperty(Keywords.MaxDBRecords, String.valueOf(MaxDBRecords));
										message=message+"<font color=green>"+Keywords.Language.getMessage(3951)+" ("+String.valueOf(MaxDBRecords)+")</font><br>\n";
									}
									catch (Exception nfe)
									{
										message=message+"<font color=red>"+Keywords.Language.getMessage(1774)+"</font><br>\n";
									}
								}
								else if (optionvalue[1].toLowerCase().startsWith(Keywords.proxyHost.toLowerCase()))
								{
									try
									{
										String[] tempinfo=optionvalue[1].split("=");
										System.setProperty( "proxySet", "true" );
										System.setProperty( "http.proxyHost", tempinfo[1].trim());
										System.setProperty( "https.proxyHost", tempinfo[1].trim());
										message=message+"<font color=green>"+Keywords.Language.getMessage(3938)+" ("+String.valueOf(tempinfo[1].trim())+")</font><br>\n";
									}
									catch (Exception e)
									{
										message=message+"<font color=red>"+Keywords.Language.getMessage(3939)+"</font><br>\n";
									}
								}
								else if (optionvalue[1].toLowerCase().startsWith(Keywords.proxyPort.toLowerCase()))
								{
									try
									{
										String[] tempinfo=optionvalue[1].split("=");
										System.setProperty( "http.proxyPort", tempinfo[1].trim());
										System.setProperty( "https.proxyPort", tempinfo[1].trim());
										message=message+"<font color=green>"+Keywords.Language.getMessage(3940)+" ("+String.valueOf(tempinfo[1].trim())+")</font><br>\n";
									}
									catch (Exception e)
									{
										message=message+"<font color=red>"+Keywords.Language.getMessage(3941)+"</font><br>\n";
									}
								}
								else if (optionvalue[1].toLowerCase().startsWith(Keywords.proxyUser.toLowerCase()))
								{
									try
									{
										String[] tempinfo=optionvalue[1].split("=");
										System.setProperty( "http.proxyUser", tempinfo[1].trim());
										System.setProperty( "https.proxyUser", tempinfo[1].trim());
										message=message+"<font color=green>"+Keywords.Language.getMessage(3942)+" ("+String.valueOf(tempinfo[1].trim())+")</font><br>\n";
									}
									catch (Exception e)
									{
										message=message+"<font color=red>"+Keywords.Language.getMessage(3943)+"</font><br>\n";
									}
								}
								else if (optionvalue[1].toLowerCase().startsWith(Keywords.proxyPassword.toLowerCase()))
								{
									try
									{
										String[] tempinfo=optionvalue[1].split("=");
										System.setProperty( "http.proxyPassword", tempinfo[1].trim());
										System.setProperty( "https.proxyPassword", tempinfo[1].trim());
										message=message+"<font color=green>"+Keywords.Language.getMessage(3944)+" ("+String.valueOf(tempinfo[1].trim())+")</font><br>\n";
									}
									catch (Exception e)
									{
										message=message+"<font color=red>"+Keywords.Language.getMessage(3945)+"</font><br>\n";
									}
								}
								else if (optionvalue[1].toLowerCase().startsWith(Keywords.MaxDataBuffered.toLowerCase()))
								{
									try
									{
										String[] tempinfo=optionvalue[1].split("=");
										String tempvalue=tempinfo[1].trim();
										int MaxDataBuffered=Integer.parseInt(tempvalue);
										System.setProperty(Keywords.MaxDataBuffered, String.valueOf(MaxDataBuffered));
										message=message+"<font color=green>"+Keywords.Language.getMessage(3946)+" ("+String.valueOf(MaxDataBuffered)+")</font><br>\n";
									}
									catch (Exception nfe)
									{
										message=message+"<font color=red>"+Keywords.Language.getMessage(3947)+"</font><br>\n";
									}
								}
								else if (optionvalue[1].toLowerCase().startsWith(Keywords.FileBufferDim.toLowerCase()))
								{
									try
									{
										String[] tempinfo=optionvalue[1].split("=");
										String tempvalue=tempinfo[1].trim();
										int FileBufferDim=Integer.parseInt(tempvalue);
										System.setProperty(Keywords.FileBufferDim, String.valueOf(Keywords.MaxDataBuffered));
										message=message+"<font color=green>"+Keywords.Language.getMessage(3948)+" ("+String.valueOf(FileBufferDim)+")</font><br>\n";
									}
									catch (Exception nfe)
									{
										message=message+"<font color=red>"+Keywords.Language.getMessage(3949)+"</font><br>\n";
									}
								}
							}
							else
							{
								message=message+Keywords.Language.getMessage(19)+"<br>\n";
							}
							Keywords.currentExecutedStep="";
						}
						else if (actualKeyword[0].equalsIgnoreCase(Keywords.DEFINE))
						{
							Keywords.currentExecutedStep="DEFINE";
							DefineRunner definerunner=new DefineRunner(actualstatement);
							currentsteperror=definerunner.getError();
							if (currentsteperror)
								message=message+"<font color=red>"+definerunner.getMessage()+"</font><br>\n";
							if (!currentsteperror && currentwritelog)
								message=message+"<font color=green>"+definerunner.getMessage()+"</font>\n";
							Keywords.currentExecutedStep="";
						}
					}
				}
				Keywords.currentExecutedStep="";
			}
			else
			{
				String [] actualKeyword=newstep.get(0).split(" ");
				if (actualKeyword[0].equalsIgnoreCase(Keywords.SETTING))
				{
					Keywords.currentExecutedStep="SETTING";
					if (currentwritelog)
					{
						for (int h=0; h<newstep.size(); h++)
						{
							message=message+StringEscapeUtils.escapeHtml(newstep.get(h))+";<br>\n";
						}
					}
					SettingRunner settingrunner=new SettingRunner(newstep);
					currentsteperror=settingrunner.getError();
					if (currentsteperror)
						message=message+"<font color=red>"+settingrunner.getMessage()+"</font>\n";
					if (!currentsteperror && currentwritelog)
						message=message+"<font color=green>"+settingrunner.getMessage()+"</font>\n";
					Keywords.currentExecutedStep="";
				}
				else if (actualKeyword[0].equalsIgnoreCase(Keywords.MACROSTEP))
				{
					Keywords.currentExecutedStep="MACROSTEP";
					if (currentwritelog)
					{
						for (int h=0; h<newstep.size(); h++)
						{
							message=message+StringEscapeUtils.escapeHtml(newstep.get(h))+";<br>\n";
						}
						message=message+"MEND;<br>\n";
					}
					MacroStepRunner msteprunner=new MacroStepRunner(newstep);
					currentsteperror=msteprunner.getError();
					if (currentsteperror)
						message=message+"<font color=red>"+msteprunner.getMessage()+"</font>\n";
					if (!currentsteperror && currentwritelog)
						message=message+"<font color=blue>"+msteprunner.getMessage()+"</font>\n";
					Keywords.currentExecutedStep="";
				}
				else if (actualKeyword[0].equalsIgnoreCase(Keywords.EXT))
				{
					Keywords.currentExecutedStep="EXT";
					if (currentwritelog)
					{
						for (int h=0; h<newstep.size(); h++)
						{
							message=message+StringEscapeUtils.escapeHtml(newstep.get(h))+";<br>\n";
						}
					}
					StepRunner steprunner=new StepRunner(newstep, 4);
					currentsteperror=steprunner.getError();
					if (currentsteperror)
						message=message+"<font color=red>"+steprunner.getMessage()+"</font>\n";
					if (!currentsteperror && currentwritelog)
						message=message+"<font color=blue>"+steprunner.getMessage()+"</font>\n";
					Keywords.currentExecutedStep="";
				}
				else if (actualKeyword[0].equalsIgnoreCase(Keywords.PROC))
				{
					Keywords.currentExecutedStep="PROC";
					if (currentwritelog)
					{
						for (int h=0; h<newstep.size(); h++)
						{
							message=message+StringEscapeUtils.escapeHtml(newstep.get(h))+";<br>\n";
						}
					}
					StepRunner steprunner=new StepRunner(newstep, 1);
					currentsteperror=steprunner.getError();
					if (currentsteperror)
						message=message+"<font color=red>"+steprunner.getMessage()+"</font>\n";
					if (!currentsteperror && currentwritelog)
						message=message+"<font color=blue>"+steprunner.getMessage()+"</font>\n";
					Keywords.currentExecutedStep="";
				}
				else if (actualKeyword[0].equalsIgnoreCase(Keywords.SQL))
				{
					Keywords.currentExecutedStep="SQL";
					if (currentwritelog)
					{
						for (int h=0; h<newstep.size(); h++)
						{
							message=message+StringEscapeUtils.escapeHtml(newstep.get(h))+";<br>\n";
						}
					}
					SqlRunner sqlrunner=new SqlRunner(newstep);
					currentsteperror=sqlrunner.getError();
					if (currentsteperror)
						message=message+"<font color=red>"+sqlrunner.getMessage()+"</font>\n";
					if (!currentsteperror && currentwritelog)
						message=message+"<font color=blue>"+sqlrunner.getMessage()+"</font>\n";
					Keywords.currentExecutedStep="";
				}
				else if (actualKeyword[0].equalsIgnoreCase(Keywords.REPORT))
				{
					Keywords.currentExecutedStep="REPORT";
					if (currentwritelog)
					{
						for (int h=0; h<newstep.size(); h++)
						{
							message=message+StringEscapeUtils.escapeHtml(newstep.get(h))+";<br>\n";
						}
					}
					ReportRunner reportrunner=new ReportRunner(newstep);
					currentsteperror=reportrunner.getError();
					if (currentsteperror)
						message=message+"<font color=red>"+reportrunner.getMessage()+"</font>\n";
					if (!currentsteperror && currentwritelog)
						message=message+"<font color=blue>"+reportrunner.getMessage()+"</font>\n";
					Keywords.currentExecutedStep="";
				}
				else if (actualKeyword[0].equalsIgnoreCase(Keywords.DATASET))
				{
					Keywords.currentExecutedStep="DATASET";
					if (currentwritelog)
					{
						for (int h=0; h<newstep.size(); h++)
						{
							String actualCommand=newstep.get(h);
							actualCommand=actualCommand.replaceAll(Keywords.SeMiCoLoN,"%;");
							message=message+StringEscapeUtils.escapeHtml(actualCommand)+";<br>\n";
						}
					}
					DatasetRunner datasetrunner=new DatasetRunner(newstep);
					currentsteperror=datasetrunner.getError();
					if (currentsteperror)
						message=message+"<font color=red>"+datasetrunner.getMessage()+"</font>\n";
					if (!currentsteperror && currentwritelog)
						message=message+"<font color=blue>"+datasetrunner.getMessage()+"</font>\n";
					Keywords.currentExecutedStep="";
				}
				else if (actualKeyword[0].equalsIgnoreCase(Keywords.JAVACODE))
				{
					Keywords.currentExecutedStep="JAVACODE";
					if (currentwritelog)
					{
						for (int h=0; h<newstep.size(); h++)
						{
							String actualCommand=newstep.get(h);
							actualCommand=actualCommand.replaceAll(Keywords.SeMiCoLoN,"%;");
							message=message+StringEscapeUtils.escapeHtml(actualCommand)+";<br>\n";
						}
					}
					JAVACODERunner javacoderunner=new JAVACODERunner(newstep);
					currentsteperror=javacoderunner.getError();
					if (currentsteperror)
						message=message+"<font color=red>"+javacoderunner.getMessage()+"</font>\n";
					if (!currentsteperror && currentwritelog)
						message=message+"<font color=blue>"+javacoderunner.getMessage()+"</font>\n";
					Keywords.currentExecutedStep="";
				}
				else if (actualKeyword[0].equalsIgnoreCase(Keywords.SCL))
				{
					Keywords.currentExecutedStep="SCL";
					if (currentwritelog)
					{
						for (int h=0; h<newstep.size(); h++)
						{
							String actualCommand=newstep.get(h);
							actualCommand=actualCommand.replaceAll(Keywords.SeMiCoLoN,"%;");
							message=message+StringEscapeUtils.escapeHtml(actualCommand)+";<br>\n";
						}
					}
					SCLRunner sclrunner=new SCLRunner(newstep);
					currentsteperror=sclrunner.getError();
					if (currentsteperror)
						message=message+"<font color=red>"+sclrunner.getMessage()+"</font>\n";
					if (!currentsteperror && currentwritelog)
						message=message+"<font color=blue>"+sclrunner.getMessage()+"</font>\n";
					Keywords.currentExecutedStep="";
				}
				else if (actualKeyword[0].equalsIgnoreCase(Keywords.DICTIONARY))
				{
					Keywords.currentExecutedStep="DICTIONARY";
					if (currentwritelog)
					{
						for (int h=0; h<newstep.size(); h++)
						{
							message=message+StringEscapeUtils.escapeHtml(newstep.get(h))+";<br>\n";
						}
					}
					DictionaryRunner dictionaryrunner=new DictionaryRunner(newstep);
					currentsteperror=dictionaryrunner.getError();
					if (currentsteperror)
						message=message+"<font color=red>"+dictionaryrunner.getMessage()+"</font>\n";
					if (!currentsteperror && currentwritelog)
						message=message+"<font color=blue>"+dictionaryrunner.getMessage()+"</font>\n";
					Keywords.currentExecutedStep="";
				}
				else if (actualKeyword[0].equalsIgnoreCase(Keywords.DOCUMENT))
				{
					Keywords.currentExecutedStep="ADAMSDOC";
					if (currentwritelog)
					{
						for (int h=0; h<newstep.size(); h++)
						{
							message=message+newstep.get(h)+";<br>\n";
						}
					}
					StepRunner steprunner=new StepRunner(newstep, 2);
					currentsteperror=steprunner.getError();
					if (currentsteperror)
						message=message+"<font color=red>"+steprunner.getMessage()+"</font>\n";
					if (!currentsteperror && currentwritelog)
						message=message+"<font color=blue>"+steprunner.getMessage()+"</font>\n";
					Keywords.currentExecutedStep="";
				}
				else if (actualKeyword[0].equalsIgnoreCase(Keywords.TOC))
				{
					Keywords.currentExecutedStep="TOC";
					if (currentwritelog)
					{
						for (int h=0; h<newstep.size(); h++)
						{
							message=message+newstep.get(h)+";<br>\n";
						}
					}
					StepRunner steprunner=new StepRunner(newstep, 3);
					currentsteperror=steprunner.getError();
					if (currentsteperror)
						message=message+"<font color=red>"+steprunner.getMessage()+"</font>\n";
					if (!currentsteperror && currentwritelog)
						message=message+"<font color=blue>"+steprunner.getMessage()+"</font>\n";
					Keywords.currentExecutedStep="";
				}
				Keywords.currentExecutedStep="";
			}
			Keywords.percentage_total=0;
			Keywords.percentage_done=0;
		}
		catch (Exception e)
		{
			Keywords.percentage_total=0;
			Keywords.percentage_done=0;
			Keywords.currentExecutedStep="";
			currentsteperror=true;
			String testDEBUG=System.getProperty("DEBUG");
			if (!testDEBUG.equalsIgnoreCase("false"))
			{
				StringWriter SWex = new StringWriter();
				PrintWriter PWex = new PrintWriter(SWex);
				e.printStackTrace(PWex);
				String details=SWex.toString();
				message=message+Keywords.Language.getMessage(6)+"<br>\n"+Keywords.Language.getMessage(2915)+"<br>\n"+details+"<br>\n<br>\n";
			}
			else
				message=message+Keywords.Language.getMessage(6)+"<br><br>\n";
		}
	}
	/**
	*Return true in case of an error during the execution of the current step
	*/
	public boolean getSteperror()
	{
		Keywords.laststepstate=currentsteperror;
		return currentsteperror;
	}
	/**
	*Return the message related to the execution of the actual step
	*/
	public String getMessageexecution()
	{
		Keywords.laststepmessage=message;
		return message;
	}
	private String ReplaceDefine(String inputstring, String name, String value)
	{
		String TempName=inputstring.toLowerCase();
		name=name.toLowerCase();
		int checkvarname=TempName.indexOf(name);
		while (checkvarname>=0)
		{
			if (checkvarname==0)
			{
				inputstring=value+inputstring.substring(checkvarname+name.length());
				TempName=inputstring.toLowerCase();
			}
			if (checkvarname>0)
			{
				inputstring=inputstring.substring(0,checkvarname)+value+inputstring.substring(checkvarname+name.length());
				TempName=inputstring.toLowerCase();
			}
			checkvarname=TempName.indexOf(name);
		}
		return inputstring;
	}
}
