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

import ADaMSoft.dataaccess.DataReader;
import ADaMSoft.dataaccess.DictionaryReader;
import ADaMSoft.keywords.Keywords;
import ADaMSoft.procedures.Result;
import ADaMSoft.procedures.Step;
import ADaMSoft.procedures.StepResult;
import ADaMSoft.utilities.ScriptParserUtilities;

import java.lang.UnsupportedClassVersionError;
import ADaMSoft.utilities.Compile_java_sdk;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

/**
* Executes SQL queries
* @author marco.scarno@gmail.com
* @date 13/06/2018
*/
public class SqlRunner extends ScriptParserUtilities
{
	String message;
	boolean steperror;
	private ClassLoader classtoexecute;
	public SqlRunner(Vector<String> KeywordValue)
	{
		Keywords.currentExecutedStep="SQL";
		message="";
		steperror=false;
		String outdb="";
		String outdb_path=System.getProperty(Keywords.WorkDir);
		Vector<String> action=new Vector<String>();
		Vector<String> dataset=new Vector<String>();
		Hashtable<String, Object> parameter=new Hashtable<String, Object>();
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
					if (actualtype.equalsIgnoreCase(Keywords.SQL))
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
							message=Keywords.Language.getMessage(4286)+"<br>\n";
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
										message=Keywords.Language.getMessage(4287)+"<br>\n";
										steperror=true;
										return;
									}
									if (infoVal.length<2 || infoVal.length>3)
									{
										message=Keywords.Language.getMessage(4287)+"<br>\n";
										steperror=true;
										return;
									}
									dataset.add(infoProc[j]);
								}
								if ((infoProc[j].toUpperCase()).startsWith(Keywords.outdb))
								{
									String[] infoVal=new String[0];
									try
									{
										infoVal=infoProc[j].split("=");
									}
									catch (Exception e)
									{
										message=Keywords.Language.getMessage(4288)+"<br>\n";
										steperror=true;
										return;
									}
									if (infoVal.length!=2)
									{
										message=Keywords.Language.getMessage(4288)+"<br>\n";
										steperror=true;
										return;
									}
									String[] dbparts=new String[0];
									try
									{
										dbparts=infoVal[1].split("\\.");
									}
									catch (Exception e)
									{
										message=Keywords.Language.getMessage(4288)+"<br>\n";
										steperror=true;
										return;
									}
									if (dbparts.length>2)
									{
										message=Keywords.Language.getMessage(4288)+"<br>\n";
										steperror=true;
										return;
									}
									if (dbparts.length==1)
									{
										outdb=dbparts[0];
									}
									if (dbparts.length==2)
									{
										outdb=dbparts[1];
										outdb_path=Keywords.project.getPath(dbparts[0]);
										if (outdb_path==null)
										{
											message=Keywords.Language.getMessage(4289)+" ("+dbparts[1]+")<br>\n";
											steperror=true;
											return;
										}
										if (outdb_path.equals(""))
										{
											message=Keywords.Language.getMessage(4289)+" ("+dbparts[1]+")<br>\n";
											steperror=true;
											return;
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
		if (outdb.equals(""))
		{
			message=Keywords.Language.getMessage(4295)+"<br>\n";
			steperror=true;
			return;
		}
		String [][] inputdictionary=new String[dataset.size()][3];
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
				inputdictionary[i][0]=namefile;
				inputdictionary[i][1]=inputdictionary[i][1]+namefile;
			}
			else
			{
				inputdictionary[i][1]=System.getProperty(Keywords.WorkDir)+infoVal[1];
				inputdictionary[i][0]=infoVal[1];
			}
			inputdictionary[i][2]="";
			if (infoVal.length>1)
				if (infoVal[1].equalsIgnoreCase(Keywords.MEMORY)) inputdictionary[i][2]="memory";
		}
		if (!outdb_path.endsWith(System.getProperty("file.separator")))
			outdb_path=outdb_path+System.getProperty("file.separator");
		Connection conn=null;
		Statement stmt=null;
		ResultSet rs=null;
		ResultSetMetaData meta=null;
		try
		{
			Class.forName("org.hsqldb.jdbcDriver");
			conn = DriverManager.getConnection("jdbc:hsqldb:file:"+outdb_path+outdb);
			Keywords.SQL_DB.add(outdb_path+outdb);
			stmt= conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
		}
		catch (Exception e)
		{
			message=Keywords.Language.getMessage(4290)+"<br>\n"+e.toString()+"<br>\n";
			steperror=true;
			return;

		}
		String query="";
		String tableschema="";
		String queryvar="";
		if (inputdictionary.length>0)
		{
			Keywords.currentExecutedStep="SQL (exporting data sets)";
			ExportDsInDB edidb=new ExportDsInDB(inputdictionary, conn, stmt, rs);
			edidb.start();
			try
			{
				Thread.sleep(1000);
			}
			catch (Exception e){}
			while (edidb.check_is_running())
			{
				try
				{
					Thread.sleep(1000);
				}
				catch (Exception e){}
			}
			if (!edidb.get_result_message().equals(""))
			{
				message=edidb.get_result_message();
				steperror=true;
				return;
			}
		}
		if (action.size()>0)
		{
			Keywords.currentExecutedStep="SQL (executing queries)";
			ExecuteQueriesInDB eqid=new ExecuteQueriesInDB(action, conn, stmt, rs);
			eqid.start();
			try
			{
				Thread.sleep(1000);
			}
			catch (Exception e){}
			while (eqid.check_is_quering())
			{
				try
				{
					Thread.sleep(1000);
				}
				catch (Exception e){}
			}
			if (!eqid.get_result_queries().equals(""))
			{
				message=eqid.get_result_queries();
				steperror=true;
				return;
			}
		}
		action.clear();
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
	public class ExportDsInDB extends Thread
	{
		String query="";
		String tableschema="";
		String queryvar="";
		String [][] inputdictionary;
		Connection conn=null;
		Statement stmt=null;
		ResultSet rs=null;
		boolean is_running;
		String result_message="";
		public ExportDsInDB(String [][] inputdictionary, Connection conn, Statement stmt, ResultSet rs)
		{
			this.inputdictionary=inputdictionary;
			this.conn=conn;
			this.stmt=stmt;
			this.rs=rs;
			is_running=false;
			result_message="";
		}
		public boolean check_is_running()
		{
			return is_running;
		}
		public String get_result_message()
		{
			return result_message;
		}
		public void run()
		{
			String filelog=(String)System.getProperty("out_logfile");
			is_running=true;
			for (int j=0; j<inputdictionary.length; j++)
			{
				try
				{
					Keywords.semwritelog.acquire();
					BufferedWriter logwriter = new BufferedWriter(new FileWriter(filelog,true));
					logwriter.write(Keywords.Language.getMessage(4296)+": "+inputdictionary[j][0]+"<br>\n");
					logwriter.close();
					Keywords.semwritelog.release();
				}
				catch (Exception e){}
				if (is_running)
				{
					try
					{
						DictionaryReader dr=new DictionaryReader(inputdictionary[j][1]);
						if (!dr.getmessageDictionaryReader().equals(""))
						{
							result_message=dr.getmessageDictionaryReader();
							is_running=false;
							stmt.close();
							conn.close();
						}
						if (is_running)
						{
							if (inputdictionary[j][2].equals("memory"))
								query="CREATE TABLE "+inputdictionary[j][0];
							else
								query="CREATE CACHED TABLE "+inputdictionary[j][0];
							queryvar=" (";
							tableschema=" (";
							Vector<Hashtable<String, String>> tempvarinfo=dr.getfixedvariableinfo();
							boolean[] ftmtext=new boolean[tempvarinfo.size()];
							for (int i=0; i<tempvarinfo.size(); i++)
							{
								String varname=(tempvarinfo.get(i)).get(Keywords.VariableName.toLowerCase());
								String vartype=(tempvarinfo.get(i)).get(Keywords.VariableFormat.toLowerCase());
								if (vartype.toLowerCase().startsWith(Keywords.TEXTSuffix.toLowerCase()))
								{
									ftmtext[i]=true;
									tableschema+=varname.trim()+" LONGVARCHAR";
								}
								else
								{
									ftmtext[i]=false;
									tableschema+=varname.trim()+" DOUBLE";
								}
								queryvar=queryvar+varname.trim()+" ";
								if (i<(tempvarinfo.size())-1)
								{
									queryvar=queryvar+", " ;
									tableschema+=", " ;
								}
							}
							queryvar=queryvar+")";
							tableschema+=")";
							try
							{
								stmt.executeUpdate(query+tableschema);
							}
							catch (Exception e)
							{
								result_message=Keywords.Language.getMessage(4291)+"<br>\n"+e.toString()+"<br>\n";
								is_running=false;
								stmt.close();
								conn.close();
							}
							if (is_running)
							{
								DataReader data = new DataReader(dr);
								data.open(null, 0, false);
								String[] values=null;
								double tv;
								while (!data.isLast())
								{
									query="INSERT INTO "+ inputdictionary[j][0]+queryvar+" VALUES (";
									values = data.getRecord();
									for(int i=0;i<values.length;i++)
									{
										if(values[i].equals(""))
											values[i]="NULL";
										else
										{
											if (!ftmtext[i])
											{
												try
												{
													tv=Double.parseDouble(values[i]);
													if (Double.isNaN(tv))
														values[i]="NULL";
												}
												catch (Exception e)
												{
													values[i]="NULL";
												}
											}
										}
										if (values[i].equals("NULL"))
											query=query+"NULL";
										else
										{
											if (!ftmtext[i])
												query=query+values[i].trim()+" ";
											else
												query=query+"'"+values[i].trim()+"' ";
										}
										if (i<values.length-1)
											query=query+", " ;
									}
									query=query+")";
									stmt.executeUpdate(query);
								}
								data.close();
								try
								{
									Keywords.semwritelog.acquire();
									BufferedWriter logwriter = new BufferedWriter(new FileWriter(filelog,true));
									logwriter.write(Keywords.Language.getMessage(4297)+": "+inputdictionary[j][0]+"<br>\n");
									logwriter.close();
									Keywords.semwritelog.release();
								}
								catch (Exception e){}
							}
						}
					}
					catch (Exception ee)
					{
						result_message=Keywords.Language.getMessage(4291)+"<br>\n"+ee.toString()+"<br>\n";
						is_running=false;
						try
						{
							stmt.close();
							conn.close();
						}
						catch (Exception eee){}
					}
				}
			}
			is_running=false;
		}
	}
	public class ExecuteQueriesInDB extends Thread
	{
		Vector<String> action;
		String query="";
		String tableschema="";
		String queryvar="";
		Connection conn=null;
		Statement stmt=null;
		ResultSet rs=null;
		boolean is_running;
		String result_message="";
		int colCount=0;
		int nrow=0;
		String temp_message="";
		ResultSetMetaData meta;
		public ExecuteQueriesInDB(Vector<String> action, Connection conn, Statement stmt, ResultSet rs)
		{
			this.action=action;
			this.conn=conn;
			this.stmt=stmt;
			this.rs=rs;
			is_running=false;
			result_message="";
		}
		public boolean check_is_quering()
		{
			return is_running;
		}
		public String get_result_queries()
		{
			return result_message;
		}
		public void run()
		{
			String filelog=(String)System.getProperty("out_logfile");
			is_running=true;
			for (int i=0; i<action.size(); i++)
			{
				if (is_running)
				{
					try
					{
						Keywords.semwritelog.acquire();
						BufferedWriter logwriter = new BufferedWriter(new FileWriter(filelog,true));
						logwriter.write(Keywords.Language.getMessage(4298)+": "+action.get(i)+"<br>\n");
						logwriter.close();
						Keywords.semwritelog.release();
					}
					catch (Exception e){}
					try
					{
						rs=stmt.executeQuery(action.get(i));
						meta = rs.getMetaData();
						colCount = meta.getColumnCount();
						nrow=0;
						temp_message="<table border=1><tr>";
						for (int j=1; j<=colCount; j++)
						{
							temp_message=temp_message+"<th><b>"+meta.getColumnName(j)+"</b></th>";
						}
						temp_message=temp_message+"</tr>";
						while (rs.next())
						{
							nrow++;
							temp_message=temp_message+"<tr>";
							for (int col=1; col <= colCount; col++)
							{
								Object value = rs.getObject(col);
								if (value != null)
								{
									temp_message=temp_message+"<td>"+value.toString()+"</td>";
								}
								else
								{
									temp_message=temp_message+"<td>&nbsp;</td>";
								}
							}
							temp_message=temp_message+"</tr>";
						}
						if (nrow>0)
						{
							try
							{
								Keywords.semwritelog.acquire();
								BufferedWriter logwriter = new BufferedWriter(new FileWriter(filelog,true));
								logwriter.write(Keywords.Language.getMessage(4299)+"</b><br><br>\n");
								logwriter.close();
								Keywords.semwritelog.release();
							}
							catch (Exception e){}
							try
							{
								temp_message=temp_message+"</table><br><br>";
								Keywords.semwriteOut.acquire();
								BufferedWriter outwriter = new BufferedWriter(new FileWriter(System.getProperty("out_outfile"),true));
								outwriter.write(temp_message);
								outwriter.close();
								Keywords.semwriteOut.release();
							}
							catch (Exception e){}
						}
						if (nrow==0)
						{
							try
							{
								Keywords.semwritelog.acquire();
								BufferedWriter logwriter = new BufferedWriter(new FileWriter(filelog,true));
								logwriter.write(Keywords.Language.getMessage(4293)+"</b><br><br>\n");
								logwriter.close();
								Keywords.semwritelog.release();
							}
							catch (Exception e){}
						}
					}
					catch (Exception e)
					{
						result_message=Keywords.Language.getMessage(4294)+"<br>\n"+e.toString()+"<br>\n";
						try
						{
							stmt.close();
							conn.close();
						}
						catch (Exception eee){}
						is_running=false;
					}
				}
			}
			is_running=false;
		}
	}
}
