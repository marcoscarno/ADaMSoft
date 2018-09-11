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
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Hashtable;
import java.util.Vector;

import ADaMSoft.keywords.Keywords;

/**
* This class reads the values contained into a table that is in a MySQL data base
* @author marco.scarno@gmail.com
* @date 03/09/2015
*/
public class Read_sqlserver extends DataTableReader
{
	Connection conn;
	ResultSet rs;
	Statement stmt;
	String table;
	int maxdatatoread=1000;
	/**
	*If true, means that the last observation was reached
	*/
	boolean checklast=false;
	/**
	*This is the string array of the actual record
	*/
	String[] actualrecord;
	/**
	*This is the string array of the next record
	*/
	String[] waitingrecord;
	/**
	*This is the message that will be returned in case of error
	*/
	String message="";
	/**
	*These are the actual number of observation read and the total number
	*/
	int actualobs, totalobs;
	/**
	*Contains the number of the columns of the requested variables
	*/
	String rifvar;
	/**
	*Opens the delimited text file and return false in case of errors
	*/
	public boolean open (Hashtable<String, String> tableinfo, Vector<Hashtable<String, String>> fixedvariableinfo,
	Vector<Hashtable<String, String>> tablevariableinfo)
	{
		checklast=false;
		try
		{
			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver").newInstance();
		}
		catch (Exception e)
		{
			message="%2748%<br>\n";
			return false;
		}
		rifvar="";
		try
		{
			for (int i=0; i<tablevariableinfo.size(); i++)
			{
				Hashtable<String, String> tempt=tablevariableinfo.get(i);
				String temprif=tempt.get(Keywords.SqlServerVariableName.toLowerCase());
				rifvar=rifvar+temprif;
				if (i<(fixedvariableinfo.size()-1))
					rifvar=rifvar+",";
			}
		}
		catch (Exception e)
		{
			message="%354%<br>\n";
			return false;
		}

		table=tableinfo.get(Keywords.table.toLowerCase());
		String port=tableinfo.get(Keywords.port.toLowerCase());
		if (port==null)
			port="1433";
		String user=tableinfo.get(Keywords.user.toLowerCase());
		String server=tableinfo.get(Keywords.server.toLowerCase());
		String password=tableinfo.get(Keywords.password.toLowerCase());
		String db=tableinfo.get(Keywords.db.toLowerCase());

		try
		{
			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
			String connectionUrl = "jdbc:sqlserver://"+server;
			if (port!=null)
				connectionUrl=connectionUrl+";port="+port;
			connectionUrl=connectionUrl+";database="+db+";user="+user;
			if (password!=null)
			{
				if (!password.equals(""))
					connectionUrl=connectionUrl+";password="+password;
			}
			connectionUrl=connectionUrl+";";
			conn = DriverManager.getConnection(connectionUrl);
			stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE, ResultSet.HOLD_CURSORS_OVER_COMMIT);
		}
		catch (Exception e)
		{
			message="%2749%<br>\n";
			return false;
		}
		String query="select "+rifvar+" from "+table;
		try
		{
			rs = stmt.executeQuery(query);
			rs.first();
			waitingrecord=new String[fixedvariableinfo.size()];
			for (int i=0; i<fixedvariableinfo.size(); i++)
			{
				String tempv="";
				try
				{
					Object ob = rs.getObject(i+1);
					if(ob!=null)
					{
						tempv=rs.getObject(i+1).toString();
					}
					else
					{
						tempv="";
					}
				}
				catch (SQLException eval) {}
				if (tempv==null)
					tempv="";
				if ((tempv.trim().equals("")) || (tempv.equalsIgnoreCase("NULL")))
					tempv="";
				waitingrecord[i]=tempv.trim();
			}
		}
		catch (Exception sqle)
		{
			try
			{
				stmt.close();
				stmt=null;
				conn.close();
				conn=null;
			}
			catch (Exception pq) {}
			message="%2750%<br>\n";
			return false;
		}
		return true;
	}
	/**
	*Delete the data table
	*/
	public boolean deletetable()
	{
		try
		{
			stmt.executeUpdate("DROP TABLE " + table);
			stmt.close();
			stmt=null;
			conn.close();
			conn=null;
			return true;
		}
		catch (Exception e)
		{
			return false;
		}
	}
	/**
	*Returns the current record
	*/
	public synchronized String[] getRecord()
	{
		actualrecord=new String[waitingrecord.length];
		for (int i=0; i<waitingrecord.length; i++)
		{
			actualrecord[i]=waitingrecord[i];
			try
			{
				actualrecord[i]=actualrecord[i].replaceAll("\n"," ");
			}
			catch (Exception ero) {}
		}
		try
		{
			if (rs.next())
			{
				for (int i=0; i<waitingrecord.length; i++)
				{
					String tempv="";
					try
					{
						Object ob = rs.getObject(i+1);
						if(ob!=null)
						{
							tempv=rs.getObject(i+1).toString();
						}
						else
						{
							tempv="";
						}
					}
					catch (SQLException eval) {}
					if (tempv==null)
						tempv="";
					if ((tempv.trim().equals("")) || (tempv.equalsIgnoreCase("NULL")))
						tempv="";
					waitingrecord[i]=tempv.trim();
				}
			}
			else
				checklast=true;
		}
		catch (Exception esq)
		{
			checklast=true;
		}
		return actualrecord;
	}
	/**
	*Returns true if the last observations was reached
	*/
	public synchronized boolean isLast()
	{
		return checklast;
	}
	/**
	*Close the file and returns false in case of error
	*/
	public boolean close()
	{
		try
		{
			stmt.close();
			stmt=null;
			conn.close();
			conn=null;
			return true;
		}
		catch (Exception e)
		{
			message="%2751%<br>\n";
			return false;
		}
	}
	/**
	*Returns an int with the number of records contained in the delimited file
	*/
	public int getRecords(Hashtable<String, String> tableinfo)
	{
		int totrecords=0;
		try
		{
			String table=tableinfo.get(Keywords.table.toLowerCase());
			String port=tableinfo.get(Keywords.port.toLowerCase());
			String user=tableinfo.get(Keywords.user.toLowerCase());
			String server=tableinfo.get(Keywords.server.toLowerCase());
			String password=tableinfo.get(Keywords.password.toLowerCase());
			String db=tableinfo.get(Keywords.db.toLowerCase());

			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
			String connectionUrl = "jdbc:sqlserver://"+server;
			if (port!=null)
				connectionUrl=connectionUrl+";port="+port;
			connectionUrl=connectionUrl+";database="+db+";user="+user;
			if (password!=null)
				connectionUrl=connectionUrl+";password="+password;
			connectionUrl=connectionUrl+";";
			Connection rconn = DriverManager.getConnection(connectionUrl);
			Statement rstmt = rconn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE, ResultSet.HOLD_CURSORS_OVER_COMMIT);

			String query="select count(*) from "+table;
			ResultSet rrs = rstmt.executeQuery(query);
			rrs.first();
			totrecords = rrs.getInt(1);
		}
		catch (Exception e)
		{
			message="%358%<br>\n";
			return 0;
		}
		return totrecords;
	}
	/**
	*Returns the error message
	*/
	public String getMessage()
	{
		return message;
	}
}
