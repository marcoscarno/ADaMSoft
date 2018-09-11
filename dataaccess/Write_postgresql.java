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

import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Vector;

import org.postgresql.util.PSQLException;

import ADaMSoft.keywords.Keywords;
import ADaMSoft.utilities.GetSettingParameters;
/**
* This class writes the values into a table in a postgres db server
* @author marco.scarno@gmail.com
* @date 03/09/2015
*/
public class Write_postgresql implements Serializable, DataTableWriter
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
	private String db;
	boolean writingerror;
	boolean[] ftmtext;
	double tv;
	/**
	*Returns the parameter required to create a data table into a postgres server.<p>
	*These are SERVER, DB, PORT, USER, PASSWORD, and DICT.
	*/
	public LinkedList<GetSettingParameters> initialize()
	{
		LinkedList<GetSettingParameters> tabpar=new LinkedList<GetSettingParameters>();
		tabpar.add(new GetSettingParameters(Keywords.server, true, 1398));
		tabpar.add(new GetSettingParameters(Keywords.db, true, 695));
		tabpar.add(new GetSettingParameters(Keywords.port, false, 1401));
		tabpar.add(new GetSettingParameters(Keywords.user, true, 1399));
		tabpar.add(new GetSettingParameters(Keywords.password, false, 1400));
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
		String query="";
		try
		{
			Class.forName("org.postgresql.Driver").newInstance();
		}
		catch (Exception e)
		{
			message="%1402%<br>\n";
			return false;
		}
		totalrecord=0;
		tabinfo=new Hashtable<String, String>();
		String table=tableinfo.get(Keywords.table.toLowerCase());
		if (table==null)
			table=tableinfo.get(Keywords.tablename);
		newtable=table;
		String port=tableinfo.get(Keywords.port.toLowerCase());
		String user=tableinfo.get(Keywords.user.toLowerCase());
		String server=tableinfo.get(Keywords.server.toLowerCase());
		String password=tableinfo.get(Keywords.password.toLowerCase());
		db=tableinfo.get(Keywords.db.toLowerCase());
		if (password==null)
			password="";
		else
			tabinfo.put(Keywords.password.toLowerCase(), password);
		tabinfo.put(Keywords.table.toLowerCase(), newtable);
		tabinfo.put(Keywords.port.toLowerCase(), port);
		if (port==null)
			port="5432";
		tabinfo.put(Keywords.user.toLowerCase(), user);
		tabinfo.put(Keywords.db.toLowerCase(), db);
		tabinfo.put(Keywords.server.toLowerCase(), server);
		oldtable=newtable+"tmp";
		varinfo=new Vector<Hashtable<String, String>>();
		try
		{
			String url = "jdbc:postgresql://"+server+":"+port+"/"+db.toLowerCase();
			Class.forName ("org.postgresql.Driver").newInstance ();
			conn = DriverManager.getConnection (url, user, password);
			stmt= conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
		}
		catch (PSQLException e) {
			String url = "jdbc:postgresql://"+server+":"+port+"/";
			try {
				conn = DriverManager.getConnection (url, user, password);
				stmt= conn.createStatement();
				query="CREATE DATABASE " + db.toLowerCase();
				try{
					stmt.executeUpdate(query);
				}
				catch (Exception e2)
				{
					message="%1409%<br>\n";
					return false;
				}
				conn.close();
				url = "jdbc:postgresql://"+server+":"+port+"/"+db.toLowerCase();
				conn = DriverManager.getConnection (url, user, password);
				stmt= conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
			} catch (Exception e1) {
				message="%1403%<br>\n";
				return false;
			}
		}
		catch (Exception e)
		{
			message="%1403%<br>\n";
			return false;
		}
		ResultSet rs;
		boolean ExistTable = false;
		try
		{
 			query="SELECT table_name FROM information_schema.tables WHERE table_schema = 'public'";
			rs=stmt.executeQuery(query);
			if(rs==null){
				message="%1410%<br>\n";
				return false;
			}
			rs.last();
			int tot = rs.getRow();
			for (int i=1; i<=tot; i++)
			{
				rs.absolute(i);
				if(rs.getString(1).equalsIgnoreCase(oldtable)){
					ExistTable=true;
					break;
				}
			}
		}
		catch (Exception sqle)
		{
			message="%1410&<br>\n";
			return false;
		}
		if (ExistTable)
		{
			try
			{
				stmt.executeUpdate("DROP TABLE " + oldtable);
			}
			catch (Exception e)
			{
				message="%1411%<br>\n";
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
				query+=tempname.trim()+" DOUBLE PRECISION";
			queryvar=queryvar+tempname.trim()+" ";
			if (i<(tempvarinfo.size())-1)
			{
				queryvar=queryvar+", " ;
				query+=", " ;
			}
			Hashtable<String, String> tempv=new Hashtable<String, String>();
			tempv.put(Keywords.PostgresqlVariableName.toLowerCase(), tempname);
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
			message="%1412%<br>\n";
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
			stmt.executeUpdate(query);
		}
		catch(Exception ex)
		{
			writingerror=true;
			message="%1413%<br>\n";
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
			message="%1412%<br>\n";
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
			query="SELECT table_name FROM information_schema.tables WHERE table_schema = 'public' AND table_catalog= '"+db.toLowerCase()+"'";
			rs=stmt.executeQuery(query);
			if(rs==null){
				message="%1414%\n";
				return false;
			}
			rs.last();
			for (int i=1; i<=rs.getRow(); i++)
			{
				rs.absolute(i);
				if(rs.getString(1).equalsIgnoreCase(newtable)){
					ExistTable=true;
					break;
				}
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
			message="%1414%<br>\n";
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
