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

package ADaMSoft;

import java.io.InputStreamReader;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Random;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.nio.channels.FileChannel;
import javax.swing.SwingUtilities;

import javax.swing.JOptionPane;
import javax.swing.JFileChooser;

import ADaMSoft.gui.MainGUI;
import ADaMSoft.keywords.Keywords;
import ADaMSoft.supervisor.ExecuteRunner;
import ADaMSoft.utilities.EndSession;
import ADaMSoft.utilities.LanguageReader;
import ADaMSoft.utilities.Project;
import ADaMSoft.utilities.MailCommand;
import ADaMSoft.utilities.Initialize;
import ADaMSoft.utilities.CheckJavac;

/**
* This is the main ADaMSoft class
* @author marco.scarno@gmail.com
* @date 11/06/2018
*/
public class ADaMSoft
{
	private static String tsmtpport="";
	private static String tmailserver="";
	private static String tmailto="";
	private static String tmailuser="";
	private static String tuserpassword="";
	/**
	*The main of ADaMSoft<p>
	*Usage: java -jar ADaMSoft.jar ADaMSoft.ini(optional) script_name_with_path.ADaMScmd(optional) or script_name_with_path.ADaMScex(optional) project_setting.ADaMSps(Optional) <p>
	*Where:<p>
	*The ADaMSoft.ini file contains the information on paths, settings, etc, of ADaMSoft;<p>
	*The ADaMScmd file contains the list of commands step to be executed;<p>
	*The ADaMSps file contains the values of the Project Settings that will be automatically loaded.<p>
	*Note: if the ADaMSoft.ini file is missing, than it is searched into the directory where the user starts the software
	*/
	public static void main(String[] argv)
	{
		String osversion=System.getProperty("os.name").toString();
		boolean iswindows=false;
		Vector<String> preliminar_statements=new Vector<String>();
		if ((System.getProperty("file.separator")).equals("\\"))
			System.setProperty("file.separator", "/");
		Keywords.general_percentage_total=0;
		Keywords.general_percentage_done=0;
		boolean batch=false;
		String iniFile="";
		String fileSetup="";
		String otherlogdir="";
		boolean deletecex=false;
		boolean deletecmd=false;
		boolean nobatch=false;
		boolean server=false;
		String javaversion=System.getProperty("java.version").toString();
		boolean is_java_sdk=false;
		boolean javac_ok=false;
		for (int i=0; i<Keywords.VersionJavaCompiler.length; i++)
		{
			if (javaversion.startsWith(Keywords.VersionJavaCompiler[i]))
			{
				is_java_sdk=true;
			}
		}
		if (is_java_sdk)
		{
			javac_ok=(new CheckJavac()).simple_check_executable("");
		}
		System.setProperty("dstoopen", "");
		System.setProperty("cmdtoopen", "");
		System.setProperty("server_release", "http://adamsoft.sourceforge.net/Release.html");
		System.setProperty("server_update", "http://adamsoft.sourceforge.net/PathUpdate.txt");
		boolean exegui=false;
		boolean minimal=false;
		System.setProperty("exeingui", "false");
		for (int i=0; i<argv.length; i++)
		{
			if (argv[i].equalsIgnoreCase("scriptextension="))
			{
				try
				{
					String[] temp_scripte=argv[i].split("=");
					if (temp_scripte.length==2)
					{
						Keywords.ScriptExtension=temp_scripte[1];
					}
				}
				catch (Exception ese){}
				System.out.println("Actual extension for a script file: "+Keywords.ScriptExtension);
			}
		}
		for (int i=0; i<argv.length; i++)
		{
			if (argv[i].equalsIgnoreCase("nobatch"))
				nobatch=true;
			if (argv[i].equalsIgnoreCase("minimal"))
				minimal=true;
			else if (argv[i].equalsIgnoreCase("exeingui"))
				System.setProperty("exeingui", "true");
			else if (argv[i].toLowerCase().endsWith(Keywords.IniExtension.toLowerCase()))
			{
				try
				{
					argv[i]=argv[i].replaceAll("\\\\","/");
				}
				catch (Exception fs){}
				iniFile=argv[i];
			}
			else if (argv[i].toLowerCase().endsWith(Keywords.ScriptExtension.toLowerCase()))
			{
				batch=true;
				try
				{
					argv[i]=argv[i].replaceAll("\\\\","/");
				}
				catch (Exception fs){}
				fileSetup=argv[i];
			}
			else if (argv[i].toLowerCase().endsWith(Keywords.DictionaryExtension.toLowerCase()))
			{
				try
				{
					argv[i]=argv[i].replaceAll("\\\\","/");
				}
				catch (Exception fs){}
				System.setProperty("dstoopen", argv[i]);
			}
			else if (argv[i].toLowerCase().startsWith(Keywords.logdir))
			{
				try
				{
					argv[i+1]=argv[i+1].replaceAll("\\\\","/");
					otherlogdir=argv[i+1];
					if (!otherlogdir.endsWith(System.getProperty("file.separator")))
						otherlogdir=otherlogdir+System.getProperty("file.separator");
				}
				catch (Exception e) {}
			}
		}
		System.setProperty("minimal", "false");
		if (minimal) System.setProperty("minimal", "true");
		String openedDirectory="";
		File localdir = new File (".");
		try
		{
			openedDirectory=localdir.getCanonicalPath();
			try
			{
				openedDirectory=openedDirectory.replaceAll("\\\\","/");
			}
			catch (Exception fs){}
			if (!openedDirectory.endsWith(System.getProperty("file.separator")))
				openedDirectory=openedDirectory+System.getProperty("file.separator");
		}
		catch (Exception e){}
		if (!fileSetup.equals(""))
		{
			if(!fileSetup.toLowerCase().startsWith("http://"))
			{
				File opened_cmd = new File(fileSetup);
				boolean existsscript = (opened_cmd.exists());
				if (!existsscript)
				{
					fileSetup=openedDirectory+fileSetup;
					opened_cmd = new File(fileSetup);
					existsscript = (opened_cmd.exists());
					if (!existsscript)
					{
						JOptionPane.showMessageDialog(null, "The script file does not exist ("+fileSetup+")");
						System.exit(1);
					}
				}
				String test_exeingui=System.getProperty("exeingui");
				if (test_exeingui!=null)
				{
					if (test_exeingui.equalsIgnoreCase("true"))
					{
						exegui=true;
						nobatch=true;
					}
				}
			}
			else
			{
				try
				{
					java.net.URL fileUrl =  new java.net.URL(fileSetup);
					BufferedReader in = new BufferedReader(new InputStreamReader(fileUrl.openStream()));
					in.readLine();
					in.close();
					String test_exeingui=System.getProperty("exeingui");
					if (test_exeingui!=null)
					{
						if (test_exeingui.equalsIgnoreCase("true"))
						{
							exegui=true;
							nobatch=true;
						}
					}
				}
				catch (Exception efh)
				{
					JOptionPane.showMessageDialog(null, "The script file does not exist ("+fileSetup+")");
					System.exit(1);
				}
			}
		}
		if ((nobatch) && (batch))
		{
			batch=false;
			System.setProperty("cmdtoopen", fileSetup);
			fileSetup="";
		}
		Initialize initADaMSoft=new Initialize();
		String main_directory=initADaMSoft.getADaMSoft_Path();
		System.setProperty("openedDirectory",openedDirectory);
		System.setProperty("main_directory",main_directory);
		preliminar_statements.add("define openeddirectory="+openedDirectory);
		preliminar_statements.add("define main_directory="+main_directory);
		(new File(main_directory+"UpdateADaMSoft.jar")).delete();
		String str="";
		if (iniFile.equals(""))
		{
			try
			{
				iniFile=openedDirectory+Keywords.SoftwareName+Keywords.IniExtension;
				boolean exists = (new File(iniFile)).exists();
				if (!exists && !batch)
				{
					iniFile="";
					iniFile=main_directory+Keywords.SoftwareName+Keywords.IniExtension;
					exists = (new File(iniFile)).exists();
					if (!exists)
					{
						iniFile="";
						if (!exegui) JOptionPane.showMessageDialog(null, "The ADaMSoft.ini file does not exist. The package will be started with the basic options");
					}
				}
			}
			catch (Exception ex)
			{
				if (!batch)
					JOptionPane.showMessageDialog(null, "Error Accessing the ADaMSoft.ini file","Error", JOptionPane.ERROR_MESSAGE);
				else
					System.out.println("Error Accessing the ADaMSoft.ini file");
				System.exit(1);
			}
		}
		System.setProperty("DEBUG","false");
		System.setProperty(Keywords.WorkDir,"");
		System.setProperty(Keywords.MaxDataBuffered,"1000");
		System.setProperty(Keywords.MaxDBRecords,"1000");
		System.setProperty(Keywords.FileBufferDim,"1024");
		System.setProperty(Keywords.ServerPortListening,"");
		System.setProperty("resources","");
		System.setProperty("IniFile", iniFile);
		System.setProperty("writelog", "yes");
		System.setProperty("halton", "yes");
		System.setProperty("LocalizeNumbers", "yes");
		System.setProperty("AutoConvertValues", "no");
		String autoexec_script="";
		if (!iniFile.equals(""))
		{
			try
			{
				boolean corrected=false;
				BufferedReader in = new BufferedReader(new FileReader(iniFile));
				try
				{
					str = in.readLine();
					if (str.equalsIgnoreCase("#ADaMSoft init file release 4"))  corrected=true;
				}
				catch (Exception ei){}
				if (corrected)
				{
					while ((str = in.readLine()) != null)
					{
						if(!(str.trim()).equals(""))
						{
							if (!str.startsWith("#"))
							{
								String [] tempClass=str.split("=");
								if (tempClass.length==2)
								{
									if (tempClass[0].equalsIgnoreCase("server_update"))
									{
										System.setProperty("server_update", tempClass[1].trim());
									}
									if (tempClass[0].equalsIgnoreCase("javac_path") && is_java_sdk)
									{
										javac_ok=(new CheckJavac()).simple_check_executable(tempClass[1].trim());
									}
									else if (tempClass[0].equalsIgnoreCase("autoexec_script"))
									{
										autoexec_script=tempClass[1].trim();
										autoexec_script=autoexec_script.replaceAll("\\\\","/");
									}
									else if (tempClass[0].equalsIgnoreCase(Keywords.proxyHost))
									{
										preliminar_statements.add("option proxyhost="+tempClass[1].trim());
										System.setProperty( "proxySet", "true" );
										System.setProperty( "http.proxyHost", tempClass[1].trim());
										System.setProperty( "https.proxyHost", tempClass[1].trim());
									}
									else if (tempClass[0].equalsIgnoreCase(Keywords.proxyPort))
									{
										preliminar_statements.add("option proxyport="+tempClass[1].trim());
										System.setProperty( "http.proxyPort", tempClass[1].trim());
										System.setProperty( "https.proxyPort", tempClass[1].trim());
									}
									else if (tempClass[0].equalsIgnoreCase(Keywords.proxyUser))
									{
										preliminar_statements.add("option proxyUser="+tempClass[1].trim());
										System.setProperty( "http.proxyUser", tempClass[1].trim());
										System.setProperty( "https.proxyUser", tempClass[1].trim());
									}
									else if (tempClass[0].equalsIgnoreCase(Keywords.proxyPassword))
									{
										preliminar_statements.add("option proxyPassword="+tempClass[1].trim());
										System.setProperty( "http.proxyPassword", tempClass[1].trim());
										System.setProperty( "https.proxyPassword", tempClass[1].trim());
									}
									else if (tempClass[0].equalsIgnoreCase(Keywords.WorkDir))
									{
										try
										{
											tempClass[1]=tempClass[1].replaceAll("\\\\","/");
										}
										catch (Exception fs){}
										File tempdir=new File(tempClass[1].trim());
										tempdir = tempdir.getAbsoluteFile();
										tempClass[1]=(tempdir.toString()).trim();
										try
										{
											tempClass[1]=tempClass[1].replaceAll("\\\\","/");
										}
										catch (Exception fs){}
										boolean exists = (new File(tempClass[1].trim())).exists();
										if (!exists)
										{
											if (!batch)
												JOptionPane.showMessageDialog(null, "Error: "+Keywords.WorkDir+" does not exist in ADaMSoft.ini", "Error",JOptionPane.ERROR_MESSAGE);
											else
												System.out.println("Error: "+Keywords.WorkDir+" does not exist in ADaMSoft.ini");
											System.exit(1);
										}
										if (!tempClass[1].endsWith(System.getProperty("file.separator")))
											tempClass[1]=tempClass[1]+System.getProperty("file.separator");
										System.setProperty(Keywords.WorkDir,tempClass[1].trim());
									}
									else if (tempClass[0].equalsIgnoreCase(Keywords.ServerPortListening))
										System.setProperty(Keywords.ServerPortListening,tempClass[1].trim());
									else if (tempClass[0].equalsIgnoreCase(Keywords.MaxDBRecords))
									{
										try
										{
											String tempvalue=tempClass[1].trim();
											Integer.parseInt(tempvalue);
										}
										catch (NumberFormatException nfe)
										{
											if (!batch)
												JOptionPane.showMessageDialog(null, "Error in "+Keywords.MaxDBRecords+" value into the ini file in ADaMSoft.ini", "Error",JOptionPane.ERROR_MESSAGE);
											else
												System.out.println("Error in "+Keywords.MaxDBRecords+" value into the ini file in ADaMSoft.ini");
											System.exit(1);
										}
										preliminar_statements.add("option maxdbrecords="+tempClass[1].trim());
										System.setProperty(Keywords.MaxDBRecords,tempClass[1].trim());
									}
									else if (tempClass[0].equalsIgnoreCase(Keywords.MaxDataBuffered))
									{
										try
										{
											String tempvalue=tempClass[1].trim();
											int MaxDataBuffered=Integer.parseInt(tempvalue);
											if(MaxDataBuffered<=0)
											{
												if (!batch)
													JOptionPane.showMessageDialog(null, "Error: "+Keywords.MaxDataBuffered+" does not exist in ADaMSoft.ini", "Error",JOptionPane.ERROR_MESSAGE);
												else
													System.out.println("Error: "+Keywords.MaxDataBuffered+" does not exist in ADaMSoft.ini");
												System.exit(1);
											}
										}
										catch (NumberFormatException nfe)
										{
											if (!batch)
												JOptionPane.showMessageDialog(null, "Error in "+Keywords.MaxDataBuffered+" value into the ini file in ADaMSoft.ini", "Error",JOptionPane.ERROR_MESSAGE);
											else
												System.out.println("Error in "+Keywords.MaxDataBuffered+" value into the ini file in ADaMSoft.ini");
											System.exit(1);
										}
										preliminar_statements.add("option maxdatabuffered="+tempClass[1].trim());
										System.setProperty(Keywords.MaxDataBuffered,tempClass[1].trim());
									}
									else if (tempClass[0].equalsIgnoreCase(Keywords.FileBufferDim))
									{
										try
										{
											String tempvalue=tempClass[1].trim();
											int FileBufferDim=Integer.parseInt(tempvalue);
											if(FileBufferDim<=0)
											{
												if (!batch)
													JOptionPane.showMessageDialog(null, "Error: "+Keywords.FileBufferDim+" does not exist in ADaMSoft.ini", "Error",JOptionPane.ERROR_MESSAGE);
												else
													System.out.println("Error: "+Keywords.FileBufferDim+" does not exist in ADaMSoft.ini");
												System.exit(1);
											}
										}
										catch (NumberFormatException nfe)
										{
											if (!batch)
												JOptionPane.showMessageDialog(null, "Error in "+Keywords.FileBufferDim+" value into the ini file in ADaMSoft.ini", "Error",JOptionPane.ERROR_MESSAGE);
											else
												System.out.println("Error in "+Keywords.FileBufferDim+" value into the ini file in ADaMSoft.ini");
											System.exit(1);
										}
										preliminar_statements.add("option filebufferdim="+tempClass[1].trim());
										System.setProperty(Keywords.FileBufferDim,tempClass[1].trim());
									}
								}
							}
						}
					}
				}
				in.close();
			}
			catch (Exception ex)
			{
				System.setProperty("IniFile", "");
			}
		}
		if (System.getProperty(Keywords.WorkDir).equals(""))
			System.setProperty(Keywords.WorkDir, System.getProperty("main_directory"));
		System.setProperty("lastOpenedDirectory", openedDirectory);
		Random generator = new Random();
		int randomIndex = generator.nextInt(100);
		java.util.Date dateProcedure=new java.util.Date();
		long timeProcedure=dateProcedure.getTime();
		String dirUserWork=System.getProperty(Keywords.WorkDir)+Keywords.SoftwareName+String.valueOf(timeProcedure)+String.valueOf(randomIndex);
		File wd = new File(dirUserWork);
		boolean dwe=wd.exists();
		int cycles=0;
		while (dwe && cycles<10)
		{
			cycles++;
			randomIndex = generator.nextInt(100);
			dateProcedure=new java.util.Date();
			timeProcedure=dateProcedure.getTime();
			dirUserWork=System.getProperty(Keywords.WorkDir)+Keywords.SoftwareName+String.valueOf(timeProcedure)+String.valueOf(randomIndex);
			wd = new File(dirUserWork);
			dwe=wd.exists();
		}
		boolean success = (new File(dirUserWork)).mkdir();
		if (!success)
		{
			if (!batch)
				JOptionPane.showMessageDialog(null, "Error creating the Working Directory: "+dirUserWork, "Error",JOptionPane.ERROR_MESSAGE);
			else
				System.out.println("Error creating the Working Directory: "+dirUserWork);
			System.exit(1);
		}
		try
		{
			dirUserWork=dirUserWork.replaceAll("\\\\","/");
			if (!dirUserWork.endsWith(System.getProperty("file.separator")))
				dirUserWork=dirUserWork+System.getProperty("file.separator");
		}
		catch (Exception e) {}
		Keywords.Language=new LanguageReader();
		Keywords.Language.readMessages();
		System.setProperty(Keywords.WorkDir, dirUserWork);
		String out_logfile=dirUserWork+"ADaMSoftLog.html";
		String out_outfile=dirUserWork+"ADaMSoftOut.html";
		System.setProperty("out_logfile", out_logfile);
		System.setProperty("out_outfile", out_outfile);
		if (!initADaMSoft.create_logfile(out_logfile))
		{
			if (!batch)
				JOptionPane.showMessageDialog(null, "Error creating the Log File: "+initADaMSoft.getMessage(), "Error",JOptionPane.ERROR_MESSAGE);
			else
				System.out.println("Error creating the Log File: "+initADaMSoft.getMessage());
			System.exit(1);
		}
		if (!initADaMSoft.create_outfile(out_outfile))
		{
			if (!batch)
				JOptionPane.showMessageDialog(null, "Error creating the Log File: "+initADaMSoft.getMessage(), "Error",JOptionPane.ERROR_MESSAGE);
			else
				System.out.println("Error creating the Log File: "+initADaMSoft.getMessage());
			System.exit(1);
		}
		preliminar_statements.add("define workdir="+System.getProperty(Keywords.WorkDir));
		preliminar_statements.add("path work="+System.getProperty(Keywords.WorkDir));
		System.setProperty(Keywords.WorkDir,dirUserWork);
		String classpath=System.getProperty("java.class.path").toString();
		if ((osversion.toUpperCase()).startsWith("WIN")) iswindows=true;
 		if (iswindows)
			classpath=classpath+";"+dirUserWork;
		else
			classpath=classpath+":"+dirUserWork;
		System.setProperty("java.class.path", classpath);
		Keywords.project = new Project();
		if (preliminar_statements.size()>0)
		{
			String initial_statements="";
			for (int i=0; i<preliminar_statements.size(); i++)
			{
				initial_statements=initial_statements+preliminar_statements.get(i)+";";
			}
			new ExecuteRunner(2, initial_statements);
		}
		if (!autoexec_script.equals(""))
		{
			if(!autoexec_script.toLowerCase().endsWith(Keywords.ScriptExtension.toLowerCase()))
				autoexec_script=autoexec_script+Keywords.ScriptExtension;
			new ExecuteRunner(0, autoexec_script);
		}
		iniFile=System.getProperty("IniFile").toString();
		if(!batch)
		{
			boolean added_javapath=false;
			if (is_java_sdk && Keywords.javac_path.equals(""))
			{
				JOptionPane.showMessageDialog(null, "<html>Note, on JAVA release 9+ it is required to have specified the path of the JAVA Compiler.<br>This requires to have installed the <i>Java JDK.</i></html>");
				if (iniFile.equals(""))
				{
					JOptionPane.showMessageDialog(null, "<html>It seems that the <i>ADaMSoft.ini</i> file is missing; it can contain the path of the JAVA compiler that will be automatically used in each session.<br>Follow the suggestion in the next panel to create it.</html>");
				}
				else
				{
					Object[] options = {"Yes",
                    					"No (the session will be closed)"};
					int n = JOptionPane.showOptionDialog(null,
							"Do you want to specify the path of the JAVA Compiler manually?",
							 "Missing JAVA Compiler",
					JOptionPane.YES_NO_OPTION,
    				JOptionPane.QUESTION_MESSAGE,
    				null,
    				options,
    				options[0]);
    				if (n!=JOptionPane.YES_OPTION)
    				{
						EndSession endsession=new EndSession();
						if(!endsession.getresult())
						{
							System.out.println(endsession.getMessage());
						}
						System.exit(1);
					}
					Object[] options_javac = {"Yes (browse directories)",
					                    	  "No (write the path in a text field)"};
					int nj = JOptionPane.showOptionDialog(null,
												"Do you want to browse the directories or manually specify the path?",
												 "Path of JAVA Compiler",
										JOptionPane.YES_NO_OPTION,
					    				JOptionPane.QUESTION_MESSAGE,
					    				null,
					    				options_javac,
					    				options_javac[0]);
					if (nj==JOptionPane.YES_OPTION)
    				{
						while (!javac_ok)
						{
							JOptionPane.showMessageDialog(null, "Select the main directory in which is the JAVA compiler executable (JAVAC)");
							JFileChooser chooser= new JFileChooser();
							chooser.setCurrentDirectory(new java.io.File("."));
							chooser.setDialogTitle("Directory in which is the Java compiler");
							chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
							chooser.setAcceptAllFileFilterUsed(false);
							if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
							{
								String dir_javac=(chooser.getSelectedFile()).toString();
								try
								{
									dir_javac=dir_javac.replaceAll("\\\\","/");
								}
								catch (Exception fs){}
								javac_ok=(new CheckJavac()).simple_check_executable(dir_javac);
								if (!javac_ok)
									JOptionPane.showMessageDialog(null, "Error, the directory doesn't contain the JAVA compiler executable");
								else added_javapath=true;
							}
							if (!javac_ok)
							{
								Object[] options_noj = {"Yes",
					                    	  			"No (exit)"};
								int njj = JOptionPane.showOptionDialog(null,
												"Do you want to browse again the directories?",
												 "Missing JAVA Compiler",
											JOptionPane.YES_NO_OPTION,
					    					JOptionPane.QUESTION_MESSAGE,
					    					null,
					    					options_noj,
					    					options_noj[0]);
								if (njj==JOptionPane.NO_OPTION)
								{
									EndSession endsession=new EndSession();
									if(!endsession.getresult())
									{
										System.out.println(endsession.getMessage());
									}
									System.exit(1);
								}
							}
						}
					}
					else if (nj==JOptionPane.NO_OPTION)
    				{
						while (!javac_ok)
						{
							String dir_javac = (String)JOptionPane.showInputDialog(
										null,
										"Write here the path",
										"Directory in which is the Java compiler",
										JOptionPane.PLAIN_MESSAGE,
										null,
										null,
										null);
							if (dir_javac!=null)
							{
								try
								{
									dir_javac=dir_javac.replaceAll("\\\\","/");
								}
								catch (Exception fs){}
								javac_ok=(new CheckJavac()).simple_check_executable(dir_javac);
								if (!javac_ok)
									JOptionPane.showMessageDialog(null, "Error, the directory doesn't contain the JAVA compiler executable");
								else added_javapath=true;
							}
							if (!javac_ok)
							{
								Object[] options_noj = {"Yes",
					                    	  			"No (exit)"};
								int njj = JOptionPane.showOptionDialog(null,
												"Do you want to insert again the directory?",
												 "Missing JAVA Compiler",
											JOptionPane.YES_NO_OPTION,
					    					JOptionPane.QUESTION_MESSAGE,
					    					null,
					    					options_noj,
					    					options_noj[0]);
								if (njj==JOptionPane.NO_OPTION)
								{
									EndSession endsession=new EndSession();
									if(!endsession.getresult())
									{
										System.out.println(endsession.getMessage());
									}
									System.exit(1);
								}
							}
						}
					}
					else
					{
						EndSession endsession=new EndSession();
						if(!endsession.getresult())
						{
							System.out.println(endsession.getMessage());
						}
						System.exit(1);
					}
				}
			}
			System.setProperty("isbatch", "false");
			System.setProperty("OutputFile", "");
			System.setProperty("listproc", "false");
			if (is_java_sdk && added_javapath && !iniFile.equals(""))
			{
				Object[] options_addjavac = {"Yes",
					                    	  "No"};
				int nji = JOptionPane.showOptionDialog(null,
												"Do you want to add the PATH of the Java compiler in the ADaMSoft.ini file?",
												"Modify the ADaMSoft.ini file",
										JOptionPane.YES_NO_OPTION,
					    				JOptionPane.QUESTION_MESSAGE,
					    				null,
					    				options_addjavac,
					    				options_addjavac[0]);
				if (nji==JOptionPane.YES_OPTION)
    			{
					String current_inirecord="";
					try
					{
						BufferedReader in = new BufferedReader(new FileReader(iniFile));
						while ((str = in.readLine()) != null)
						{
							current_inirecord=current_inirecord+str+"\n";
						}
						in.close();
						current_inirecord=current_inirecord+"javac_path="+Keywords.javac_path+"\n";
						BufferedWriter out_inifile = new BufferedWriter(new FileWriter(iniFile));
						out_inifile.write(current_inirecord);
						out_inifile.close();
					}
					catch (Exception e)
					{
						JOptionPane.showMessageDialog(null, "Error writing in the file: "+e.toString());
					}
				}
			}
			try
			{
				SwingUtilities.invokeLater(new Runnable()
				{
					  public void run()
					  {
						new MainGUI();
					  }
				});
			}
			catch (Exception e)
			{
				if (!System.getProperty("gui_error").equals(""))
				{
					StringWriter SWex = new StringWriter();
					PrintWriter PWex = new PrintWriter(SWex);
					e.printStackTrace(PWex);
					try
					{
						String gui_error=System.getProperty("gui_error");
						BufferedWriter g_error= new BufferedWriter(new FileWriter(new File(gui_error),true));
						g_error.write(SWex.toString()+"\n");
						g_error.close();
						System.setProperty("gui_error", gui_error);
					}
					catch (Exception ef){}
				}
				System.exit(1);
			}
		}
		else
		{
			if (is_java_sdk && Keywords.javac_path.equals(""))
			{
				System.out.println("Error: with JAVA release 9+ it is required to have specified the path of the JAVA compiler in the ADaMSoft.ini file. This require to have the Java JDK.");
				EndSession endsession=new EndSession();
				if(!endsession.getresult())
				{
					System.out.println(endsession.getMessage());
				}
				System.exit(1);
			}
			System.setProperty("isbatch", "true");
			String logfile ="";
			String outputfile ="";
			if(!fileSetup.toLowerCase().startsWith("http://"))
			{
				if(!(fileSetup.toLowerCase()).endsWith(Keywords.ScriptExtension.toLowerCase()))
				{
					logfile = fileSetup+Keywords.LogExtension;
					outputfile=fileSetup+".html";
				}
				else
				{
					logfile = fileSetup.substring(0,fileSetup.lastIndexOf("."))+Keywords.LogExtension;
					outputfile = fileSetup.substring(0,fileSetup.lastIndexOf("."))+".html";
				}
			}
			else
			{
				String suffix_file_setup=fileSetup.substring(fileSetup.lastIndexOf("/")+1);
				logfile = suffix_file_setup.substring(0,suffix_file_setup.lastIndexOf("."))+Keywords.LogExtension;
				outputfile = suffix_file_setup.substring(0,suffix_file_setup.lastIndexOf("."))+".html";
				logfile=openedDirectory+logfile;
				outputfile=openedDirectory+outputfile;
			}
			if (!otherlogdir.equals(""))
			{
				File testdir = new File(logfile);
				String logfilename=testdir.getName();
				logfile=otherlogdir+logfile;
			}
			if(!fileSetup.toLowerCase().endsWith(Keywords.ScriptExtension.toLowerCase()))
				fileSetup=fileSetup+Keywords.ScriptExtension;
			new ExecuteRunner(0, fileSetup);
			if (deletecmd)
				(new File(fileSetup)).delete();
			try
			{
				FileChannel inputChannel = null;
				FileChannel outputChannel = null;
				try
				{
					FileInputStream fileInputStream = new FileInputStream(new File(out_logfile));
					inputChannel = fileInputStream.getChannel();
					FileOutputStream fileOutputStream = new FileOutputStream(new File(logfile));
					outputChannel = fileOutputStream.getChannel();
					outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
					inputChannel.close();
					outputChannel.close();
					fileInputStream.close();
					fileOutputStream.close();
					fileInputStream = new FileInputStream(new File(out_outfile));
					inputChannel = fileInputStream.getChannel();
					fileOutputStream = new FileOutputStream(new File(outputfile));
					outputChannel = fileOutputStream.getChannel();
					outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
					inputChannel.close();
					outputChannel.close();
					fileInputStream.close();
					fileOutputStream.close();
				}
				catch (Exception e)
				{
					System.out.println("Error writing the results in the output files: "+e.toString());
				}
				EndSession endsession=new EndSession();
				if(!endsession.getresult())
				{
					System.out.println(endsession.getMessage());
					System.exit(1);
				}
				inputChannel.close();
				outputChannel.close();
			}
			catch(Exception ex)
			{
				System.out.println("Error writing the results in the output files: "+ex.toString());
				System.exit(1);
			}
			System.exit(0);
		}
	}
}
