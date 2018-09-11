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

import ADaMSoft.keywords.Keywords;

/**
* This class reads the values contained into a table that is in a Postgres data base
* @author marco.scarno@gmail.com
* @date 03/09/2015
*/
public class Read_postgresql extends DataTableReader implements Runnable
{
	protected static String DATABASEDRIVER = "org.postgresql.Driver";

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
			Class.forName(DATABASEDRIVER).newInstance();
		}
		catch (Exception e)
		{
			message="%1402%<br>\n";
			return false;
		}
		rifvar="";
		try
		{
			for (int i=0; i<tablevariableinfo.size(); i++)
			{
				Hashtable<String, String> tempt=tablevariableinfo.get(i);
				String temprif=tempt.get(Keywords.PostgresqlVariableName.toLowerCase());
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
			port="5432";
		String user=tableinfo.get(Keywords.user.toLowerCase());
		String server=tableinfo.get(Keywords.server.toLowerCase());
		String password=tableinfo.get(Keywords.password.toLowerCase());
		if (password==null)
			password="";
		String db=tableinfo.get(Keywords.db.toLowerCase());

		try
		{
			String url = "jdbc:postgresql://"+server+":"+port+"/"+db.toLowerCase();
			Class.forName (DATABASEDRIVER).newInstance ();
			conn = DriverManager.getConnection (url, user, password);
			stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
		}
		catch (Exception e)
		{
			message="%1403%<br>\n";
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
			message="%1405%<br>\n";
			return false;
		}
		try
		{
			tr=new Thread(Read_postgresql.this);
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
		if(!checklast){
			try
			{
				while(!available){
					wait();
				}
				actualrecord=new String[waitingrecord.length];
				for (int i=0; i<waitingrecord.length; i++)
				{
					actualrecord[i]=waitingrecord[i];
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
			message="%1406%<br>\n";
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

			String url = "jdbc:postgresql://"+server+":"+port+"/"+db.toLowerCase();
			Class.forName (DATABASEDRIVER).newInstance ();
			Connection rconn = DriverManager.getConnection (url, user, password);
			Statement rstmt = rconn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);

			String query="select count(*) from "+table;

			ResultSet rrs = rstmt.executeQuery(query);
			rrs.absolute(1);
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
				String query="select "+rifvar+" from "+table+" limit "+String.valueOf(maxdatatoread)+" OFFSET "+String.valueOf(actualobs);
				rs = stmt.executeQuery(query);
				while (rs.next())
				{
					for (int i=0; i<waitingrecord.length; i++)
					{
						String tempv="";
						try
						{
							Object ob = rs.getObject(i+1);
							if(ob!=null){
								tempv=rs.getObject(i+1).toString();
							}
							else{
								tempv="";
							}
						}
						catch (Exception eval) {}
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
			message="%1407%<br>\n";
			checklast=true;
			notify();
		}
	}
}
