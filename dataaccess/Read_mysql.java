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
public class Read_mysql extends DataTableReader implements Runnable
{
	Connection conn;
	ResultSet rs;
	Statement stmt;
	String table;
	int maxdatatoread=1000;
	/**
	*This is the reader tread
	*/
	Thread tr;
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
	private boolean available= false;
	boolean threadexecute;
	/**
	*Opens the delimited text file and return false in case of errors
	*/
	public boolean open (Hashtable<String, String> tableinfo, Vector<Hashtable<String, String>> fixedvariableinfo,
	Vector<Hashtable<String, String>> tablevariableinfo)
	{
		threadexecute=true;
		try
		{
			maxdatatoread = Integer.parseInt(System.getProperty(Keywords.MaxDBRecords));
		}
		catch(NumberFormatException  nfe){}
		try
		{
			Class.forName("com.mysql.jdbc.Driver").newInstance();
		}
		catch (Exception e)
		{
			message="%693%<br>\n";
			return false;
		}
		rifvar="";
		try
		{
			for (int i=0; i<tablevariableinfo.size(); i++)
			{
				Hashtable<String, String> tempt=tablevariableinfo.get(i);
				String temprif=tempt.get(Keywords.MysqlVariableName.toLowerCase());
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
		waitingrecord=new String[fixedvariableinfo.size()];
		table=tableinfo.get(Keywords.table.toLowerCase());
		String port=tableinfo.get(Keywords.port.toLowerCase());
		if (port==null)
			port="3306";
		String user=tableinfo.get(Keywords.user.toLowerCase());
		String server=tableinfo.get(Keywords.server.toLowerCase());
		String password=tableinfo.get(Keywords.password.toLowerCase());
		if (password==null)
			password="";
		String db=tableinfo.get(Keywords.db.toLowerCase());

		try
		{
			String url = "jdbc:mysql://"+server+":"+port+"/";
			Class.forName ("com.mysql.jdbc.Driver").newInstance ();
			conn = DriverManager.getConnection (url, user, password);
			stmt = conn.createStatement();
		}
		catch (Exception e)
		{
			message="%694%<br>\n";
			return false;
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
		String query="select count(*) from "+table;
		totalobs=0;
		actualobs=0;
		try
		{
			rs = stmt.executeQuery(query);
			rs.first();
			totalobs = rs.getInt(1);
		}
		catch (Exception sqle)
		{
			message="%701%<br>\n";
			return false;
		}
		try
		{
			tr=new Thread(Read_mysql.this);
			tr.setPriority(10);
			tr.start();
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
	public synchronized boolean deletetable()
	{
		try
		{
			threadexecute=false;
			notify();
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
		if(!checklast)
		{
			try
			{
				while(!available)
				{
					wait();
				}
				actualrecord=new String[waitingrecord.length];
				for (int i=0; i<waitingrecord.length; i++)
				{
					actualrecord[i]=waitingrecord[i];
					try
					{
						actualrecord[i]=actualrecord[i].replaceAll("\t"," ");
						actualrecord[i]=actualrecord[i].replaceAll("\n"," ");
					}
					catch (Exception ero) {}
				}
				available=false;
				notify();
			}
			catch (Exception t) {}
		}
		return actualrecord;
	}
	/**
	*Returns true if the last observations was reached
	*/
	public synchronized boolean isLast()
	{
		while(!available){
			try {
				wait(1000);
			} catch (InterruptedException e) {
			}
		}
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
				threadexecute=false;
				notify();
			}
			catch (Exception ec) {}
			stmt.close();
			stmt=null;
			conn.close();
			conn=null;
			return true;
		}
		catch (Exception e)
		{
			message="%703%<br>\n";
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
			if (password==null)
				password="";
			String db=tableinfo.get(Keywords.db.toLowerCase());

			String url = "jdbc:mysql://"+server+":"+port+"/";
			Class.forName ("com.mysql.jdbc.Driver").newInstance ();
			Connection rconn = DriverManager.getConnection (url, user, password);
			Statement rstmt = rconn.createStatement();

			rstmt.executeUpdate("USE "+ db);
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
	public synchronized void run()
	{
		try
		{
			while((actualobs<totalobs) && (threadexecute))
			{
				String query="select "+rifvar+" from "+table+" limit "+String.valueOf(actualobs)+" , "+String.valueOf(maxdatatoread);
				rs = stmt.executeQuery(query);
				while (rs.next())
				{
					for (int i=0; i<waitingrecord.length; i++)
					{
						String tempv="";
						try
						{
							Object ob = rs.getObject(i+1);
							if(ob!=null) tempv=rs.getObject(i+1).toString();
							else tempv="";
						}
						catch (SQLException eval) {}
						if (tempv==null)
							tempv="";
						if ((tempv.trim().equals("")) || (tempv.equalsIgnoreCase("NULL")))
							tempv="";
						waitingrecord[i]=tempv.trim();
					}
					actualobs++;
					available=true;
					notify();
					wait();
				}
			}
			available=true;
			checklast=true;
			notify();
		}
		catch (Exception e)
		{
			available=true;
			message="%692%<br>\n";
			checklast=true;
			notify();
		}
	}

}
