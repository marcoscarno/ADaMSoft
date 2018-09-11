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
* This class reads the values contained into a table that is in an oracle data base
* @author marco.scarno@gmail.com
* @date 03/09/2015
*/
public class Read_oracle extends DataTableReader
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
	*This is the vector of the records
	*/
	Vector<String[]> readrecords;
	/**
	*This is the message that will be returned in case of error
	*/
	String message="";
	/**
	*These are the actual number of observation read and the total number
	*/
	int actualobs, totalobs, reqvars;
	/**
	*Contains the number of the columns of the requested variables
	*/
	String rifvar;
	String query;
	/**
	*Opens the database and return false in case of errors
	*/
	public boolean open (Hashtable<String, String> tableinfo, Vector<Hashtable<String, String>> fixedvariableinfo,
	Vector<Hashtable<String, String>> tablevariableinfo)
	{
		readrecords=new Vector<String[]>();
		reqvars=0;
		query="";
		try
		{
			maxdatatoread = Integer.parseInt(System.getProperty(Keywords.MaxDBRecords));
		}
		catch(NumberFormatException  nfe)
		{
			message="%2657%<br>\n";
			return false;
		}
		try
		{
			Class.forName("oracle.jdbc.driver.OracleDriver").newInstance();
		}
		catch (Exception e)
		{
			message="%1692%<br>\n";
			return false;
		}
		rifvar="";
		reqvars=fixedvariableinfo.size();
		try
		{
			for (int i=0; i<tablevariableinfo.size(); i++)
			{
				Hashtable<String, String> tempt=tablevariableinfo.get(i);
				String temprif=tempt.get(Keywords.oraclevariablename.toLowerCase());
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
			port="1521";
		if (port.equals(""))
			port="1521";
		String user=tableinfo.get(Keywords.user.toLowerCase());
		String server=tableinfo.get(Keywords.server.toLowerCase());
		String password=tableinfo.get(Keywords.password.toLowerCase());
		String service=tableinfo.get(Keywords.service.toLowerCase());
		String sid=tableinfo.get(Keywords.sid.toLowerCase());
		if (password==null)
			password="";
		if (user==null)
			user="";
		if (service==null)
			service="";
		if (sid==null)
			sid="";
		try
		{
			String credential="";
			if(!user.equals(""))
			{
				credential=user+"/"+password;
			}
			String url="";
			if(!sid.equals(""))
			{
				url = "jdbc:oracle:thin:"+"@//"+server+":"+port+"/"+sid;
			}
			else
			{
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
		query="select count(*) from "+table;
		totalobs=0;
		actualobs=0;
		try
		{
			rs = stmt.executeQuery(query);
			rs.first();
			totalobs = rs.getInt(1);
			readrec();
		}
		catch (Exception sqle)
		{
			message="%1709%<br>\n";
			return false;
		}
		return true;
	}
	/**
	*Returns the current record
	*/
	public String[] getRecord()
	{
		if (readrecords.size()==0)
			readrec();
		if (readrecords.size()>0)
		{
			actualrecord=readrecords.get(0);
			readrecords.remove(0);
			actualobs++;
			if (actualobs>=totalobs)
				checklast=true;
			return actualrecord;
		}
		else
		{
			checklast=true;
			return null;
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
			message="%1708%<br>\n";
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
			String service=tableinfo.get(Keywords.service.toLowerCase());
			String sid=tableinfo.get(Keywords.sid.toLowerCase());
			if (port==null)
				port="1521";
			if (port.equals(""))
				port="1521";
			if (password==null)
				password="";
			if (user==null)
				user="";
			if (service==null)
				service="";
			if (sid==null)
				sid="";
			String credential="";
			if(!user.equals(""))
			{
				credential=user+"/"+password;
			}
			String url="";
			if(!sid.equals(""))
			{
				url = "jdbc:oracle:thin:"+"@//"+server+":"+port+"/"+sid;
			}
			else
			{
				url = "jdbc:oracle:thin:"+credential+"@//"+server+":"+port+(service.equals("")?"":"/")+service;
			}
			Class.forName ("oracle.jdbc.driver.OracleDriver").newInstance ();
			Connection rconn = DriverManager.getConnection (url, user, password);
			Statement rstmt = rconn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
			String query="select count(*) from "+table;
			ResultSet rrs = rstmt.executeQuery(query);
			rrs.first();
			totrecords = rrs.getInt(1);
		}
		catch (Exception e)
		{
			message="%1709%<br>\n";
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
	public void readrec()
	{
		readrecords.clear();
		try
		{
			query="SELECT "+rifvar+" FROM (select a.*, ROWNUM r_n_u_m_ from (select * from "+table+")a where ROWNUM<"+String.valueOf(maxdatatoread+actualobs+1)+") where r_n_u_m_ >="+String.valueOf(actualobs+1);
			rs = stmt.executeQuery(query);
			while (rs.next())
			{
				String[] waitingrecord=new String[reqvars];
				for (int i=0; i<reqvars; i++)
				{
					waitingrecord[i]="";
					try
					{
						Object ob = rs.getObject(i+1);
						if(ob!=null)
						{
							waitingrecord[i]=rs.getObject(i+1).toString();
						}
						else
						{
							waitingrecord[i]="";
						}
					}
					catch (SQLException eval) {}
					if (waitingrecord[i]==null)
						waitingrecord[i]="";
					if (waitingrecord[i].equalsIgnoreCase("NULL"))
						waitingrecord[i]="";
					try
					{
						waitingrecord[i]=waitingrecord[i].replaceAll("\n"," ");
					}
					catch (Exception ero) {}
					waitingrecord[i]=waitingrecord[i].trim();
				}
				readrecords.add(waitingrecord);
			}
		}
		catch (Exception e)
		{
			message="%1701%<br>\n";
			checklast=true;
		}
	}
}
