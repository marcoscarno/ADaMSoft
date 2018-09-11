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
import java.util.Properties;

import ADaMSoft.keywords.Keywords;

import com.ibm.db2.jcc.DB2Driver;

/**
* This class reads the values contained into a table that is in a MySQL data base
* @author marco.scarno@gmail.com
* @date 03/09/2015
*/
@SuppressWarnings("unused")
public class Read_bigsql extends DataTableReader
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
			Class.forName("com.ibm.db2.jcc.DB2Driver").newInstance();
		}
		catch (Exception e)
		{
			message="%3828%<br>\n";
			return false;
		}
		rifvar="";
		try
		{
			for (int i=0; i<tablevariableinfo.size(); i++)
			{
				Hashtable<String, String> tempt=tablevariableinfo.get(i);
				String temprif=tempt.get(Keywords.BigSQLVariableName.toLowerCase());
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
			port="51000";
		String user=tableinfo.get(Keywords.user.toLowerCase());
		String server=tableinfo.get(Keywords.server.toLowerCase());
		String password=tableinfo.get(Keywords.password.toLowerCase());

		try
		{
			Class.forName("com.ibm.db2.jcc.DB2Driver");
			String connectionUrl = "jdbc:db2://"+server+":"+port+"/BIGSQL";
			Properties cp = new Properties();
			cp.put("user", user);
			if (password!=null) cp.put("password", password);
			conn = DriverManager.getConnection(connectionUrl, cp);
			stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
		}
		catch (Exception e)
		{
			message="%3829% "+e.toString()+"<br>\n";
			return false;
		}
		String query="SELECT "+rifvar.toUpperCase()+" FROM "+table;
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
			message="%3835% ("+sqle.toString()+")<br>\n";
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
			try
			{
				stmt.executeUpdate("COMMIT");
			}
			catch (Exception ecommi) {}
			stmt.close();
			stmt=null;
			conn.close();
			conn=null;
			return true;
		}
		catch (Exception e)
		{
			message="%3836% ("+e.toString()+")<br>\n";
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
			if (port==null)
				port="51000";
			Class.forName("com.ibm.db2.jcc.DB2Driver");
			String connectionUrl = "jdbc:db2://"+server+":"+port+"/BIGSQL";
			Properties cp = new Properties();
			cp.put("user", user);
			if (password!=null) cp.put("password", password);
			Connection rconn = DriverManager.getConnection(connectionUrl, cp);
			Statement rstmt = rconn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			String query="SELECT COUNT(*) FROM "+table;
			ResultSet rrs = rstmt.executeQuery(query);
			rrs.first();
			totrecords = rrs.getInt(1);
			rstmt.close();
			rconn.close();
			rstmt=null;
			rconn=null;
		}
		catch (Exception e)
		{
			message="%3837% ("+e.toString()+")<br>\n";
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
