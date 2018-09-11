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
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Vector;

import ADaMSoft.keywords.Keywords;
import ADaMSoft.utilities.GetSettingParameters;
/**
* This class writes the values into a table in an odbc data source
* @author marco.scarno@gmail.com
* @date 03/09/2015
*/
public class Write_msodbc implements Serializable, DataTableWriter
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
	String tableschema;
	private String db;
	boolean writingerror;
	boolean[] ftmtext;
	double tv;
	/**
	*Returns the parameter required to create a data table into a odbc data source.<p>
	*These are DB, DICT, USER and PASSWORD
	*/
	public LinkedList<GetSettingParameters> initialize()
	{
		LinkedList<GetSettingParameters> tabpar=new LinkedList<GetSettingParameters>();
		tabpar.add(new GetSettingParameters(Keywords.db, true, 695));
		tabpar.add(new GetSettingParameters(Keywords.user, false, 1399));
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
			Class.forName("sun.jdbc.odbc.JdbcOdbcDriver").newInstance();
		}
		catch (Exception e)
		{
			message="%1421%<br>\n";
			return false;
		}
		totalrecord=0;
		tabinfo=new Hashtable<String, String>();
		String table=tableinfo.get(Keywords.table.toLowerCase());
		if (table==null)
			table=tableinfo.get(Keywords.tablename);
		newtable=table;
		String user=tableinfo.get(Keywords.user.toLowerCase());
		String password=tableinfo.get(Keywords.password.toLowerCase());
		db=tableinfo.get(Keywords.db.toLowerCase());
		if (password!=null){
			tabinfo.put(Keywords.password.toLowerCase(), password);
		}
		tabinfo.put(Keywords.table.toLowerCase(), newtable);
		if(user!=null){
			tabinfo.put(Keywords.user.toLowerCase(), user);
		}
		tabinfo.put(Keywords.db.toLowerCase(), db);
		oldtable=newtable+"tmp";
		varinfo=new Vector<Hashtable<String, String>>();
		try
		{
			String url = "jdbc:odbc:"+db.toLowerCase();
			Class.forName ("sun.jdbc.odbc.JdbcOdbcDriver").newInstance ();
			conn = getConnection (url, user, password);
			stmt= conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
		}
		catch (Exception e)
		{
			message="%1422%<br>\n";
			return false;
		}
		try
		{
			stmt.executeUpdate("DROP TABLE " + oldtable);
		}
		catch (Exception e) {}
		query="CREATE TABLE "+oldtable;
		queryvar=" (";
		tableschema=" (";
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
				tableschema+=tempname.trim()+" TEXT";
			else
				tableschema+=tempname.trim()+" DOUBLE";
			queryvar=queryvar+tempname.trim()+" ";
			if (i<(tempvarinfo.size())-1)
			{
				queryvar=queryvar+", " ;
				tableschema+=", " ;
			}
			Hashtable<String, String> tempv=new Hashtable<String, String>();
			tempv.put(Keywords.MsodbcVariableName.toLowerCase(), tempname);
			varinfo.add(tempv);
		}
		queryvar=queryvar+")";
		tableschema+=")";
		try
		{
			stmt.executeUpdate(query+tableschema);
		}
		catch (Exception e)
		{
			message="%1429%<br>\n";
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
			message="%1430%<br>\n";
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
		if (writingerror)
		{
			message="%1429%<br>\n";
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
			try{
				stmt.executeUpdate("DROP TABLE " + newtable);
			}
			catch (Exception e) {}
			stmt.executeUpdate("CREATE TABLE " + newtable+tableschema);
			stmt.executeUpdate("INSERT INTO "+ newtable +" SELECT * from "+oldtable);
			stmt.executeUpdate("DROP TABLE "+ oldtable);
			stmt.close();
			stmt=null;
			conn.close();
			conn=null;
			return true;
		}
		catch (Exception e)
		{
			message="%1431%<br>\n";
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
	/**
	*This return the connection using the parameters
	*/
	private Connection getConnection(String url, String user, String password) throws SQLException
	{
		if(password==null && user==null)
		{
			return DriverManager.getConnection (url);
		}
		else
		{
			if (password==null)
				password="";
			return DriverManager.getConnection (url, user, password);
		}
	}
}
