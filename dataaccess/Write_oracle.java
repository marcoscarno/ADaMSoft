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
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.Hashtable;
import java.util.Vector;
import java.util.LinkedList;
import java.io.Serializable;

import ADaMSoft.keywords.Keywords;
import ADaMSoft.utilities.GetSettingParameters;
/**
* This class writes the values into a table in a oracle server
* @author marco.scarno@gmail.com
* @date 03/09/2015
*/
public class Write_oracle implements Serializable, DataTableWriter
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
	*Returns the parameter required to create a data table into a oracle server.<p>
	*/
	public LinkedList<GetSettingParameters> initialize()
	{
		LinkedList<GetSettingParameters> tabpar=new LinkedList<GetSettingParameters>();
		tabpar.add(new GetSettingParameters(Keywords.server, true, 1698));
		tabpar.add(new GetSettingParameters(Keywords.service, false, 1702));
		tabpar.add(new GetSettingParameters(Keywords.sid, false, 1724));
		tabpar.add(new GetSettingParameters(Keywords.port, false, 1699));
		tabpar.add(new GetSettingParameters(Keywords.user, true, 1700));
		tabpar.add(new GetSettingParameters(Keywords.password, false, 1701));
		tabpar.add(new GetSettingParameters(Keywords.dict, false, 405));
		return tabpar;
	}
	/**
	*Opens the data file and returns false for the error (in this case with getmessage() it is possibile to see the error)
	*/
	public boolean open(Hashtable<String, String> tableinfo, Vector<Hashtable<String, String>> tempvarinfo)
	{
		ftmtext=new boolean[tempvarinfo.size()];
		message="";
		writingerror=false;
		queryvar="";
		try
		{
			Class.forName("oracle.jdbc.driver.OracleDriver").newInstance();
		}
		catch (Exception e)
		{
			message="%1692%<br>\n";
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
			port="1521";
		if (port.equals(""))
			port="1521";
		String user=tableinfo.get(Keywords.user.toLowerCase());
		if (user==null)
			user="";
		String server=tableinfo.get(Keywords.server.toLowerCase());
		String password=tableinfo.get(Keywords.password.toLowerCase());
		String service=tableinfo.get(Keywords.service.toLowerCase());
		String sid=tableinfo.get(Keywords.sid.toLowerCase());
		if (service==null)
			service="";
		if (sid==null)
			sid="";
		if (password==null)
			password="";
		else
			tabinfo.put(Keywords.password.toLowerCase(), password);
		tabinfo.put(Keywords.table.toLowerCase(), table);
		tabinfo.put(Keywords.port.toLowerCase(), port);
		tabinfo.put(Keywords.user.toLowerCase(), user);
		tabinfo.put(Keywords.server.toLowerCase(), server);
		tabinfo.put(Keywords.service.toLowerCase(), service);
		tabinfo.put(Keywords.sid.toLowerCase(), sid);
		oldtable=newtable+"tmp";
		varinfo=new Vector<Hashtable<String, String>>();
		try
		{
			String credential="";
			if(!user.equals(""))
				credential=user+"/"+password;
			String url="";
			if(!sid.equals(""))
			{
				url = "jdbc:oracle:thin:"+"@//"+server+":"+port+"/"+sid;
			}
			else{
				url = "jdbc:oracle:thin:"+credential+"@//"+server+":"+port+(service.equals("")?"":"/")+service;
			}
			Class.forName ("oracle.jdbc.driver.OracleDriver").newInstance ();
			conn = DriverManager.getConnection (url, user, password);
			stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
		}
		catch (Exception e)
		{
			message="%1693%<br>\n";
			return false;
		}
		String query="";
		ResultSet rs;
		boolean ExistOldTable = false;
		try
		{
			query="select * from (select a.*, ROWNUM r_n_u_m_ from (select * from "+table+")a where ROWNUM<10) where r_n_u_m_ >=0";
			rs = stmt.executeQuery(query);
			ResultSetMetaData metadata = rs.getMetaData();
			if(metadata.getColumnCount()>0)
			{
				ExistOldTable=true;
			}
		}
		catch (Exception sqle){}
		if (ExistOldTable)
		{
			try
			{
				rs = stmt.executeQuery(query);
				stmt.executeUpdate("DROP TABLE " + table);
			}
			catch (Exception e)
			{
				message="%1704%<br>\n";
				return false;
			}
		}
		ExistOldTable = false;
		try
		{
			query="select * from (select a.*, ROWNUM r_n_u_m_ from (select * from "+oldtable+")a where ROWNUM<10) where r_n_u_m_ >=0";
			rs = stmt.executeQuery(query);
			ResultSetMetaData metadata = rs.getMetaData();
			if(metadata.getColumnCount()>0)
			{
				ExistOldTable=true;
			}
		}
		catch (Exception sqle){}
		if (ExistOldTable)
		{
			try
			{
				rs = stmt.executeQuery(query);
				stmt.executeUpdate("DROP TABLE " + oldtable);
			}
			catch (Exception e)
			{
				message="%1704%<br>\n";
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
				query+=tempname.trim()+" VARCHAR(4000)";
			else
				query+=tempname.trim()+" NUMBER";
			queryvar=queryvar+tempname.trim()+" ";
			if (i<(tempvarinfo.size())-1)
			{
				queryvar=queryvar+", " ;
				query+=", " ;
			}
			Hashtable<String, String> tempv=new Hashtable<String, String>();
			tempv.put(Keywords.oraclevariablename.toLowerCase(), tempname);
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
			message="%1705%<br>\n";
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
			stmt.executeQuery(query);
		}
		catch(Exception ex)
		{
			writingerror=true;
			message="%1706%<br>\n";
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
			if (!message.equals(""))
				message="%1707%<br>\n"+message;
			else
				message="%1707%<br>\n";
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
			query="select table_name from user_tables";
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
			stmt.executeUpdate("ALTER TABLE "+ oldtable +" RENAME TO "+newtable);
			stmt.close();
			stmt=null;
			conn.close();
			conn=null;
			return true;
		}
		catch (Exception e)
		{
			message="%1708%<br>\n";
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
