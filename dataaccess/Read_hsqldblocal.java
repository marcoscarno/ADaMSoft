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
* This class reads the values contained into a table that is in a hsqldb local data base
* @author marco.scarno@gmail.com
* @date 13/06/2018
*/
public class Read_hsqldblocal extends DataTableReader
{
	Connection conn;
	ResultSet rs;
	Statement stmt;
	String table;
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
			for (int i=0; i<fixedvariableinfo.size(); i++)
			{
				Hashtable<String, String> tempt=tablevariableinfo.get(i);
				String temprif=tempt.get(Keywords.HsqldbLocalVariableName.toLowerCase());
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
		String user=tableinfo.get(Keywords.user.toLowerCase());
		String password=tableinfo.get(Keywords.password.toLowerCase());
		if (password==null)
			password="";
		String db=tableinfo.get(Keywords.dbfile.toLowerCase());

		try
		{
			String url = "jdbc:hsqldb:file:"+db.toLowerCase();
			Class.forName ("org.hsqldb.jdbcDriver").newInstance ();
			if (user!=null)
				conn = DriverManager.getConnection (url, user, password);
			else
				conn = DriverManager.getConnection (url);
			stmt = conn.createStatement();
		}
		catch (Exception e)
		{
			message="%1443%<br>\n";
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
			message="%1445%<br>\n";
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
	*Delete the data table
	*/
	public boolean deletetable()
	{
		try
		{
			stmt.executeUpdate("DROP TABLE " + table);
			stmt.executeQuery("SHUTDOWN");
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
			stmt.executeQuery("SHUTDOWN");
			stmt.close();
			stmt=null;
			conn.close();
			conn=null;
			return true;
		}
		catch (Exception e)
		{
			message="%1444%\n";
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
			String user=tableinfo.get(Keywords.user.toLowerCase());
			String dbpath=tableinfo.get(Keywords.dbfile.toLowerCase());
			String password=tableinfo.get(Keywords.password.toLowerCase());
			if (password==null)
				password="";

			String url = "jdbc:hsqldb:file:"+dbpath.toLowerCase();
			Class.forName ("org.hsqldb.jdbcDriver").newInstance ();
			Connection rconn;
			if (user!=null)
				rconn = DriverManager.getConnection (url, user, password);
			else
				rconn = DriverManager.getConnection (url);
			System.out.println(url);
			System.out.println(table);
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
			e.printStackTrace();
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
			message="%1445%<br>\n";
			checklast=true;
		}
	}
}
