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
import java.sql.PreparedStatement;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Vector;
import java.util.Properties;
import com.ibm.db2.jcc.DB2Driver;

import ADaMSoft.keywords.Keywords;
import ADaMSoft.utilities.GetSettingParameters;
/**
* This class writes the values into a table in a BigSQL server
* @author marco.scarno@gmail.com
* @date 03/09/2015
*/
@SuppressWarnings("unused")
public class Write_bigsql implements Serializable, DataTableWriter
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
	String cuser;
	boolean writingerror;
	boolean[] ftmtext;
	double tv;
	String query;
	String insert_query;
	PreparedStatement pstmt;
	String schema;
	/**
	*Returns the parameter required to create a data table into a sql server<p>
	*These are SERVER, PORT, USER, PASSWORD, and DICT.
	*/
	public LinkedList<GetSettingParameters> initialize()
	{
		LinkedList<GetSettingParameters> tabpar=new LinkedList<GetSettingParameters>();
		tabpar.add(new GetSettingParameters(Keywords.server, true, 3824));
		tabpar.add(new GetSettingParameters(Keywords.port, false, 3825));
		tabpar.add(new GetSettingParameters(Keywords.user, true, 3826));
		tabpar.add(new GetSettingParameters(Keywords.password, false, 3827));
		tabpar.add(new GetSettingParameters(Keywords.schema, false, 4019));
		tabpar.add(new GetSettingParameters(Keywords.dict, false, 405));
		return tabpar;
	}
	/**
	*Opens the data file and returns false for the error (in this case with getmessage() it is possibile to see the error
	*/
	public boolean open(Hashtable<String, String> tableinfo, Vector<Hashtable<String, String>> tempvarinfo)
	{
		pstmt=null;
		ftmtext=new boolean[tempvarinfo.size()];
		writingerror=false;
		try
		{
			Class.forName("com.ibm.db2.jcc.DB2Driver").newInstance();
		}
		catch (Exception e)
		{
			message="%3828%<br>\n";
			return false;
		}
		totalrecord=0;
		tabinfo=new Hashtable<String, String>();
		String table=tableinfo.get(Keywords.table.toLowerCase());
		if (table==null)
			table=tableinfo.get(Keywords.tablename);
		newtable=table.toUpperCase();
		String port=tableinfo.get(Keywords.port.toLowerCase());
		if (port==null)
			port="51000";
		cuser=tableinfo.get(Keywords.user.toLowerCase());
		schema=tableinfo.get(Keywords.schema.toLowerCase());
		String server=tableinfo.get(Keywords.server.toLowerCase());
		String password=tableinfo.get(Keywords.password.toLowerCase());
		if (password==null)
			password="";
		else
			tabinfo.put(Keywords.password.toLowerCase(), password);
		tabinfo.put(Keywords.table.toLowerCase(), newtable);
		tabinfo.put(Keywords.port.toLowerCase(), port);
		tabinfo.put(Keywords.user.toLowerCase(), cuser);
		tabinfo.put(Keywords.server.toLowerCase(), server);
		oldtable=newtable+"tmp";
		oldtable=oldtable.toUpperCase();
		varinfo=new Vector<Hashtable<String, String>>();
		try
		{
			Class.forName("com.ibm.db2.jcc.DB2Driver");
			String connectionUrl = "jdbc:db2://"+server+":"+port+"/BIGSQL";
			Properties cp = new Properties();
			cp.put("user", cuser);
			if (password!=null) cp.put("password", password);
			conn = DriverManager.getConnection(connectionUrl, cp);
			conn.setAutoCommit(false);
			stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
		}
		catch (Exception e)
		{
			message="%3829% "+e.toString()+"<br>\n";
			return false;
		}
		ResultSet rs;
		boolean ExistTable = false;
		try
		{
			if (schema==null)
				query="SELECT TABNAME FROM SYSHADOOP.HCAT_TABLES WHERE TABSCHEMA='"+cuser.toUpperCase()+"'";
			else
			{
				query="SELECT TABNAME FROM SYSHADOOP.HCAT_TABLES WHERE TABSCHEMA='"+schema.toUpperCase()+"'";
			}
			rs=stmt.executeQuery(query);
			if(rs!=null)
			{
				while(rs.next())
				{

					if(rs.getString(1).equalsIgnoreCase(oldtable))
					{
						ExistTable=true;
						break;
					}
				}
			}
		}
		catch (Exception sqle)
		{
			message="%3830% ("+sqle.toString()+")<br>\n";
			return false;
		}
		if (schema!=null)
		{
			try
			{
				stmt.executeUpdate("USE " + schema.toUpperCase());
			}
			catch (Exception e){}
		}
		if (ExistTable)
		{
			try
			{
				stmt.executeUpdate("DROP TABLE " + oldtable.toUpperCase());
			}
			catch (Exception e)
			{
				message="%3831% ("+e.toString()+")<br>\n";
				return false;
			}
		}
		query="CREATE HADOOP TABLE "+oldtable.toUpperCase();
		String second_part="";
		insert_query="INSERT INTO "+oldtable.toUpperCase()+" (";
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
				query+=tempname.trim().toUpperCase()+" VARCHAR(4000)";
			else
				query+=tempname.trim().toUpperCase()+" DOUBLE";
			second_part+="?";
			insert_query+=tempname.trim().toUpperCase();
			if (i<(tempvarinfo.size())-1)
			{
				second_part+=", ";
				insert_query+=", ";
				query+=", ";
			}
			Hashtable<String, String> tempv=new Hashtable<String, String>();
			tempv.put(Keywords.BigSQLVariableName.toLowerCase(), tempname);
			varinfo.add(tempv);
		}
		query=query+") ROW FORMAT DELIMITED FIELDS TERMINATED BY '\\t'  LINES TERMINATED BY '\\n' STORED AS TEXTFILE";
		insert_query=insert_query+") VALUES ("+second_part+")";
		try
		{
			stmt.executeUpdate(query);
			stmt.executeUpdate("COMMIT");
			pstmt = conn.prepareStatement(insert_query);
		}
		catch (Exception e)
		{
			message="%3832% ("+e.toString()+")<br>\n";
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
			for(int i=0;i<values.length;i++)
			{
				if (!ftmtext[i])
				{
					if (values[i].equals(""))
						pstmt.setNull(i+1, java.sql.Types.DOUBLE);
					else
					{
						tv=Double.NaN;
						try
						{
							tv=Double.parseDouble(values[i]);
						}
						catch (Exception e) {}
						if (!Double.isNaN(tv))
							pstmt.setDouble(i+1, tv);
						else
							pstmt.setNull(i+1, java.sql.Types.DOUBLE);
					}
				}
				else
				{
					if (values[i].equals(""))
						pstmt.setNull(i+1, java.sql.Types.VARCHAR);
					else
						pstmt.setString(i+1, values[i].trim());
				}
			}
			pstmt.executeUpdate();
		}
		catch(Exception ex)
		{
			writingerror=true;
			message="%3833% ("+ex.toString()+")<br>\n";
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
			stmt.executeUpdate("DROP TABLE " + oldtable.toUpperCase());
			pstmt.close();
			pstmt=null;
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
		try
		{
			pstmt.close();
			pstmt=null;
		}
		catch (Exception e) {}
		if (writingerror)
		{
			message="%3833%<br>\n";
			try
			{
				stmt.executeUpdate("DROP TABLE " + oldtable.toUpperCase());
				stmt.executeUpdate("COMMIT");
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
				stmt.executeUpdate("DROP TABLE " + oldtable.toUpperCase());
				stmt.executeUpdate("COMMIT");
				message="%786%<br>\n";
				stmt.close();
				stmt=null;
				conn.close();
				conn=null;
				return false;
			}
			if (schema==null)
				query="SELECT TABNAME FROM SYSHADOOP.HCAT_TABLES WHERE TABSCHEMA='"+cuser.toUpperCase()+"'";
			else
				query="SELECT TABNAME FROM SYSHADOOP.HCAT_TABLES WHERE TABSCHEMA='"+schema.toUpperCase()+"'";
			rs=stmt.executeQuery(query);
			if(rs==null)
			{
				message="%3831%<br>\n";
				return false;
			}
			while(rs.next())
			{

				if(rs.getString(1).equalsIgnoreCase(newtable)){
					ExistTable=true;
					break;
				}
			}
			if (ExistTable)
			{
				stmt.executeUpdate("DROP TABLE " + newtable.toUpperCase());
			}
			stmt.executeUpdate("COMMIT");
			stmt.executeUpdate("RENAME TABLE "+ oldtable.toUpperCase() +" TO "+newtable.toUpperCase());
			stmt.executeUpdate("COMMIT");
			stmt.close();
			stmt=null;
			conn.close();
			conn=null;
			return true;
		}
		catch (Exception e)
		{
			try
			{
				stmt.close();
				stmt=null;
				conn.close();
				conn=null;
			}
			catch (Exception ee){}
			message="%3834% ("+e.toString()+")<br>\n";
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
