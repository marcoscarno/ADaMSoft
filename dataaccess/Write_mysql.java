/**
* Copyright (c) 2015 MS
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

package ADaMSoft.dataaccess;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Hashtable;
import java.util.Vector;
import java.util.LinkedList;
import java.io.Serializable;

import ADaMSoft.keywords.Keywords;
import ADaMSoft.utilities.GetSettingParameters;
/**
* This class writes the values into a table in a mysql server
* @author marco.scarno@gmail.com
* @date 03/09/2015
*/
public class Write_mysql implements Serializable, DataTableWriter
{
	private static final long serialVersionUID = 1L;
	Connection conn;
	Statement stmt;
	int totalrecord;
	String message;
	Hashtable<String, String> tabinfo;
	Vector<Hashtable<String, String>> varinfo;
	String newtable;
	String oldtable;
	String queryvar;
	boolean writingerror;
	boolean[] ftmtext;
	double tv;
	/**
	*Returns the parameter required to create a data table into a mysql server.<p>
	*These are SERVER, DB, PORT, USER, PASSWORD, and DICT.
	*/
	public LinkedList<GetSettingParameters> initialize()
	{
		LinkedList<GetSettingParameters> tabpar=new LinkedList<GetSettingParameters>();
		tabpar.add(new GetSettingParameters(Keywords.server, true, 429));
		tabpar.add(new GetSettingParameters(Keywords.db, true, 695));
		tabpar.add(new GetSettingParameters(Keywords.port, false, 432));
		tabpar.add(new GetSettingParameters(Keywords.user, true, 430));
		tabpar.add(new GetSettingParameters(Keywords.password, false, 431));
		tabpar.add(new GetSettingParameters(Keywords.dict, false, 405));
		return tabpar;
	}
	/**
	*Opens the data file and returns false for the error (in this case with getmessage() it is possibile to see the error
	*/
	public boolean open(Hashtable<String, String> tableinfo, Vector<Hashtable<String, String>> tempvarinfo)
	{
		ftmtext=new boolean[tempvarinfo.size()];
		writingerror=false;
		queryvar="";
		try
		{
			Class.forName("com.mysql.jdbc.Driver").newInstance();
		}
		catch (Exception e)
		{
			message="%693%<br>\n";
			return false;
		}
		totalrecord=0;
		tabinfo=new Hashtable<String, String>();
		String table=tableinfo.get(Keywords.table.toLowerCase());
		if (table==null)
			table=tableinfo.get(Keywords.tablename);
		newtable=table;
		String port=tableinfo.get(Keywords.port.toLowerCase());
		if (port==null)
			port="3306";
		String user=tableinfo.get(Keywords.user.toLowerCase());
		String server=tableinfo.get(Keywords.server.toLowerCase());
		String password=tableinfo.get(Keywords.password.toLowerCase());
		String db=tableinfo.get(Keywords.db.toLowerCase());
		if (password==null)
			password="";
		else
			tabinfo.put(Keywords.password.toLowerCase(), password);
		tabinfo.put(Keywords.table.toLowerCase(), newtable);
		tabinfo.put(Keywords.port.toLowerCase(), port);
		tabinfo.put(Keywords.user.toLowerCase(), user);
		tabinfo.put(Keywords.db.toLowerCase(), db);
		tabinfo.put(Keywords.server.toLowerCase(), server);
		oldtable=newtable+"tmp";
		varinfo=new Vector<Hashtable<String, String>>();
		try
		{
			String url = "jdbc:mysql://"+server+":"+port+"/";
			Class.forName ("com.mysql.jdbc.Driver").newInstance ();
			conn = DriverManager.getConnection (url, user, password);
		}
		catch (Exception e)
		{
			message="%694%<br>\n";
			return false;
		}
		String query="";
		boolean ExistDB=false;
		ResultSet rs;
		try
		{
			stmt = conn.createStatement();
			query="show databases";
			rs = stmt.executeQuery(query);
			rs.first();
			do
			{
				if(rs.getString(1).equalsIgnoreCase(db))
					ExistDB=true;
			}while(rs.next());
		}
		catch (Exception e)
		{
			message="%696%<br>\n";
			return false;
		}
		if (!ExistDB)
		{
			try
			{
				query="CREATE DATABASE " + db.toLowerCase();
				stmt.executeUpdate(query);
			}
			catch (Exception e)
			{
				message="%697%<br>\n";
				return false;
			}
		}
		try
		{
			stmt.executeUpdate("USE "+ db);
		}
		catch (Exception sqle)
		{
			message="%698%<br>\n";
			return false;
		}
		boolean ExistTable = false;
		try
		{
			query="show tables";
			rs=stmt.executeQuery(query);
			rs.last();
			int NumberOfTables=rs.getRow();
			if (NumberOfTables>0)
			{
				rs.first();
				do
				{
					if(rs.getString(1).equalsIgnoreCase(oldtable))
						ExistTable=true;
				} while(rs.next());
			}
		}
		catch (Exception sqle)
		{
			message="%699%<br>\n";
			return false;
		}
		if (ExistTable)
		{
			try
			{
				stmt.executeUpdate("USE "+ db);
				rs = stmt.executeQuery(query);
				stmt.executeUpdate("DROP TABLE " + oldtable);
			}
			catch (Exception e)
			{
				message="%700%<br>\n";
				return false;
			}
		}
		query="CREATE TABLE "+oldtable;
		queryvar=" (";
		query=query+" (";
		for(int i=0;i<tempvarinfo.size();i++)
		{
			Hashtable<String, String> currentvar=tempvarinfo.get(i);
			String wfmt=currentvar.get(Keywords.VariableFormat.toLowerCase());
			if ((wfmt.length()==Keywords.NUMSuffix.length()) && (wfmt.toUpperCase().startsWith(Keywords.NUMSuffix.toUpperCase())))
				ftmtext[i]=false;
			else if ((wfmt.length()>Keywords.NUMSuffix.length()) && (wfmt.toUpperCase().startsWith(Keywords.NUMSuffix.toUpperCase())))
			{
				String num=wfmt.substring(Keywords.NUMSuffix.length());
				if ((num.toUpperCase()).startsWith(Keywords.DTSuffix))
					ftmtext[i]=true;
				else if ((num.toUpperCase()).startsWith(Keywords.DATESuffix))
					ftmtext[i]=true;
				else if ((num.toUpperCase()).startsWith(Keywords.TIMESuffix))
					ftmtext[i]=true;
			}
			else
				ftmtext[i]=true;
			String tempname=currentvar.get(Keywords.VariableName.toLowerCase());
			if (ftmtext[i])
				query+=tempname.trim()+" TEXT";
			else
				query+=tempname.trim()+" DOUBLE";
			queryvar=queryvar+tempname.trim()+" ";
			if (i<(tempvarinfo.size())-1)
			{
				queryvar=queryvar+", " ;
				query+=", " ;
			}
			Hashtable<String, String> tempv=new Hashtable<String, String>();
			tempv.put(Keywords.MysqlVariableName.toLowerCase(), tempname);
			varinfo.add(tempv);
		}
		queryvar=queryvar+")";
		query=query+")";
		try
		{
			stmt.executeUpdate(query);
		}
		catch (Exception e)
		{
			message="%701%<br>\n";
			return false;
		}
		return true;
	}
	/**
	*Write the values and returns false in case of error
	*/
	public boolean writevalues(String[] values)
	{
		if (writingerror)
			return false;
		try
		{
			totalrecord++;
			String query="INSERT INTO "+ oldtable+queryvar+" VALUES (";
			for(int i=0;i<values.length;i++)
			{
				try
				{
					values[i]=values[i].replaceAll("\t"," ");
					values[i]=values[i].replaceAll("'","");
					values[i]=values[i].replaceAll("\\\\","");
				}
				catch (Exception e) {}
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
		catch(Exception ex)
		{
			writingerror=true;
			message="%702%<br>\n";
			return false;
		}
		return true;
	}
	/**
	*Delete the temporary file
	*/
	public boolean deletetmp()
	{
		try
		{
			stmt.executeUpdate("DROP TABLE " + oldtable);
			stmt.close();
			stmt=null;
			conn.close();
			conn=null;
			return true;
		}
		catch (Exception e) {}
		return false;
	}
	/**
	*Close the file and returns false in case of error
	*/
	public boolean close()
	{
		ResultSet rs;
		String query="";
		if (writingerror)
		{
			message="%702%<br>\n";
			try
			{
				stmt.executeUpdate("DROP TABLE " + oldtable);
			}
			catch (Exception e) {}
			try
			{
				stmt.close();
				stmt=null;
				conn.close();
				conn=null;
			}
			catch (Exception e) {}
			return false;
		}
		boolean ExistTable = false;
		try
		{
			if (totalrecord==0)
			{
				stmt.executeUpdate("DROP TABLE " + oldtable);
				message="%786%<br>\n";
				stmt.close();
				stmt=null;
				conn.close();
				conn=null;
				return false;
			}
			query="show tables";
			rs=stmt.executeQuery(query);
			rs.last();
			int NumberOfTables=rs.getRow();
			if (NumberOfTables>0)
			{
				rs.first();
				do
				{
					if(rs.getString(1).equalsIgnoreCase(newtable))
						ExistTable=true;
				} while(rs.next());
			}
			if (ExistTable)
			{
				stmt.executeUpdate("DROP TABLE " + newtable);
			}
			stmt.executeUpdate("RENAME TABLE "+ oldtable +" TO "+newtable);
			stmt.close();
			stmt=null;
			conn.close();
			conn=null;
			return true;
		}
		catch (Exception e)
		{
			message="%703%<br>\n";
		}
		return false;
	}
	/**
	*Returns the error message
	*/
	public String getmessage()
	{
		return message;
	}
	/**
	*Returns the information on the data table that will be inserted in the new dictionary
	*/
	public Hashtable<String, String> getTableInfo ()
	{
		return tabinfo;
	}
	/**
	*Returns the information on the variables, specific for this kind of data file
	*/
	public Vector<Hashtable<String, String>> getVariablesInfo()
	{
		return varinfo;
	}
}
