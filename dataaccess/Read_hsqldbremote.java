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
* This class reads the values contained into a table that is in a hsqldb remote data base
* @author marco.scarno@gmail.com
* @date 03/09/2015
*/
public class Read_hsqldbremote extends DataTableReader
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
		try
		{
			maxdatatoread = Integer.parseInt(System.getProperty(Keywords.MaxDBRecords));
		}
		catch(NumberFormatException  nfe){}
		try
		{
			Class.forName("org.hsqldb.jdbcDriver").newInstance();
		}
		catch (Exception e)
		{
			message="%1438%<br>\n";
			return false;
		}
		rifvar="";
		try
		{
			for (int i=0; i<tablevariableinfo.size(); i++)
			{
				Hashtable<String, String> tempt=tablevariableinfo.get(i);
				String temprif=tempt.get(Keywords.HsqldbRemoteVariableName.toLowerCase());
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
			port="9001";
		String user=tableinfo.get(Keywords.user.toLowerCase());
		String server=tableinfo.get(Keywords.server.toLowerCase());
		String password=tableinfo.get(Keywords.password.toLowerCase());
		if (password==null)
			password="";
		String db=tableinfo.get(Keywords.db.toLowerCase());

		try
		{
			String url = "jdbc:hsqldb:hsql://"+server+":"+port+"/"+db.toLowerCase();
			Class.forName ("org.hsqldb.jdbcDriver").newInstance ();
			if (user!=null)
				conn = DriverManager.getConnection (url, user, password);
			else
				conn = DriverManager.getConnection (url);
			stmt = conn.createStatement();
		}
		catch (Exception e)
		{
			message="%1439%<br>\n";
			return false;
		}
		String query="select count(*) from "+table;
		totalobs=0;
		actualobs=0;
		try
		{
			rs = stmt.executeQuery(query);
			rs.next();
			totalobs = rs.getInt(1);
		}
		catch (Exception sqle)
		{
			message="%1440%<br>\n";
			return false;
		}
		waitingrecord=new String[fixedvariableinfo.size()];
		try
		{
			getNextRecord();
		}
		catch (Exception e)
		{
			message="%357%<br>\n";
			checklast=true;
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
	public String[] getRecord()
	{
		String[] actualrecord=new String[waitingrecord.length];
		for (int i=0; i<waitingrecord.length; i++)
		{
			actualrecord[i]=waitingrecord[i];
		}
		getNextRecord();
		return actualrecord;
	}
	/**
	*Returns true if the last observations was reached
	*/
	public boolean isLast()
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
			message="%1441%<br>\n";
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
			if(port==null)
			{
				port="9001";
			}
			String user=tableinfo.get(Keywords.user.toLowerCase());
			String server=tableinfo.get(Keywords.server.toLowerCase());
			String password=tableinfo.get(Keywords.password.toLowerCase());
			if (password==null)
				password="";
			String db=tableinfo.get(Keywords.db.toLowerCase());

			String url = "jdbc:hsqldb:hsql://"+server+":"+port+"/"+db.toLowerCase();
			Class.forName ("org.hsqldb.jdbcDriver").newInstance ();
			Connection rconn;
			if (user!=null)
				rconn = DriverManager.getConnection (url, user, password);
			else
				rconn = DriverManager.getConnection (url);
			Statement rstmt = rconn.createStatement();

			String query="select count(*) from "+table;

			ResultSet rrs = rstmt.executeQuery(query);
			rrs.next();
			totrecords = rrs.getInt(1);
			rstmt.close();
			rconn.close();
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
	public void getNextRecord()
	{
		try
		{
			if (actualobs<=totalobs)
			{
				String query="select limit "+actualobs+" "+(actualobs+1)+" "+rifvar+" from "+table;
				rs = stmt.executeQuery(query);
				rs.next();
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
				actualobs++;
			}
			else
			checklast=true;
		}
		catch (Exception e)
		{
			message="%1442%<br>\n";
			checklast=true;
		}
	}
}
