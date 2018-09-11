/**
* Copyright (c) 2017 MS
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

package ADaMSoft.procedures;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Properties;

import ADaMSoft.keywords.Keywords;
import ADaMSoft.utilities.StepUtilities;
import ADaMSoft.utilities.GetRequiredParameters;

/**
* This procedure executes a query
* @author marco.scarno@gmail.com
* @date 14/02/2017
*/
public class ProcSql implements RunStep
{
	/**
	* Executes a query
	*/
	@SuppressWarnings("resource")
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		String [] requiredparameters=new String[] {Keywords.query};
		String [] optionalparameters=new String[0];
		Keywords.percentage_total=0;
		Keywords.percentage_done=0;
		StepUtilities steputilities=new StepUtilities();
		if (!steputilities.checkParameters(requiredparameters, optionalparameters, parameters))
		{
			return new Result(steputilities.getMessage(), false, null);
		}
		String[] dbtype=new String[Keywords.KeywordsForDBSettings.length];
		String[] dbtest=new String[Keywords.KeywordsForDBSettings.length];
		for (int i=0; i<Keywords.KeywordsForDBSettings.length; i++)
		{
			dbtype[i]="";
			dbtest[i]=Keywords.KeywordsForDBSettings[i].toUpperCase();
			for (Enumeration<String> e = parameters.keys() ; e.hasMoreElements() ;)
			{
				String parname = ((String) e.nextElement());
				parname=(parname.trim()).toUpperCase();
				if (parname.startsWith(dbtest[i]+"_"))
					dbtype[i]=dbtest[i];
			}

		}

		int ndb=0;
		String type="";
		for (int i=0; i<dbtype.length; i++)
		{
			if (!dbtype[i].equals(""))
			{
				ndb++;
				type=dbtype[i].toLowerCase();;
			}
		}
		if (ndb==0)
			return new Result("%1737%<br>\n", false, null);

		if (ndb>1)
			return new Result("%1738%<br>\n", false, null);

		Connection conn=null;
		Statement stmt=null;

		String query  = (String) parameters.get(Keywords.query);
		String[] queries=query.split(";");

		String mess="";

		if (type.equals("oracle"))
		{
			String server  = (String) parameters.get(Keywords.oracle_server);
			String port    = (String) parameters.get(Keywords.oracle_port);
			String password= (String) parameters.get(Keywords.oracle_password);
			String user    = (String) parameters.get(Keywords.oracle_user);
			String service   = (String) parameters.get(Keywords.oracle_service);
			String sid   = (String) parameters.get(Keywords.oracle_sid);
			if (port==null)
				port="1521";
			if (service==null)
				service="";
			if (sid==null)
				sid="";
			if (user==null)
				user="";
			if (password==null)
				password="";
			try
			{
				String credential="";
				if(!user.equals(""))
					credential=user+"/"+password;
				String url="";
				if(!sid.equals(""))
					url = "jdbc:oracle:thin:"+"@//"+server+":"+port+"/"+sid;
				else
					url = "jdbc:oracle:thin:"+credential+"@//"+server+":"+port+(service.equals("")?"":"/")+service;
				Class.forName ("oracle.jdbc.driver.OracleDriver").newInstance ();
				conn = DriverManager.getConnection (url, user, password);
				stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
			}
			catch (Exception e)
			{
				try
				{
					stmt.close();
					conn.close();
					stmt=null;
					conn=null;
				}
				catch (Exception ex) {}
				return new Result("%1693%<br>\n", false, null);
			}
			for (int i=0; i<queries.length; i++)
			{
				queries[i]=queries[i].trim();
				mess=mess+"%1741% "+queries[i];
				try
				{
					if (queries[i].toLowerCase().startsWith(Keywords.exeupdate))
					{
						queries[i]=queries[i].substring(Keywords.exeupdate.length());
						stmt.executeUpdate(queries[i]);
					}
					else
						stmt.executeQuery(queries[i]);
					mess=mess+" %1742%<br>\n";
				}
				catch (Exception eq)
				{
					String errorq=eq.toString()+"<br>\n";
					mess=mess+"<br>\n%1743%<br>\n"+errorq+"<br>\n";
					try
					{
						stmt.close();
						conn.close();
						stmt=null;
						conn=null;
					}
					catch (Exception ex) {}
					return new Result(mess, false, null);
				}
			}
		}

		if (type.equals("mysql"))
		{
			String server  = (String) parameters.get(Keywords.mysql_server);
			String port    = (String) parameters.get(Keywords.mysql_port);
			String password= (String) parameters.get(Keywords.mysql_password);
			String user    = (String) parameters.get(Keywords.mysql_user);
			if (port==null)
				port="3306";
			if (password==null)
				password="";
			try
			{
				String url = "jdbc:mysql://"+server+":"+port+"/";
				Class.forName ("com.mysql.jdbc.Driver").newInstance ();
				conn = DriverManager.getConnection (url, user, password);
				stmt = conn.createStatement();
			}
			catch (Exception e)
			{
				try
				{
					stmt.close();
					conn.close();
					stmt=null;
					conn=null;
				}
				catch (Exception ex) {}
				return new Result("%694%<br>\n", false, null);
			}
			for (int i=0; i<queries.length; i++)
			{
				queries[i]=queries[i].trim();
				mess=mess+"%1741% "+queries[i];
				try
				{
					if (queries[i].toLowerCase().startsWith(Keywords.exeupdate))
					{
						queries[i]=queries[i].substring(Keywords.exeupdate.length());
						stmt.executeUpdate(queries[i]);
					}
					else
						stmt.executeQuery(queries[i]);
					mess=mess+" %1742%<br>\n";
				}
				catch (Exception eq)
				{
					String errorq=eq.toString()+"<br>\n";
					mess=mess+"<br>\n%1743%<br>\n"+errorq+"<br>\n";
					try
					{
						stmt.close();
						conn.close();
						stmt=null;
						conn=null;
					}
					catch (Exception ex) {}
					return new Result(mess, false, null);
				}
			}
		}

		if (type.equals("postgresql"))
		{
			String db      = (String) parameters.get(Keywords.postgresql_db);
			String server  = (String) parameters.get(Keywords.postgresql_server);
			String port    = (String) parameters.get(Keywords.postgresql_port);
			String password= (String) parameters.get(Keywords.postgresql_password);
			String user    = (String) parameters.get(Keywords.postgresql_user);
			if (port==null)
				port="5432";
			if (password==null)
				password="";
			try
			{
				String url = "jdbc:postgresql://"+server+":"+port+"/"+db.toLowerCase();
				Class.forName ("org.postgresql.Driver").newInstance ();
				conn = DriverManager.getConnection (url, user, password);
				stmt = conn.createStatement();
			}
			catch (Exception e)
			{
				try
				{
					stmt.close();
					conn.close();
					stmt=null;
					conn=null;
				}
				catch (Exception ex) {}
				return new Result("%1404%<br>\n", false, null);
			}
			for (int i=0; i<queries.length; i++)
			{
				queries[i]=queries[i].trim();
				mess=mess+"%1741% "+queries[i];
				try
				{
					if (queries[i].toLowerCase().startsWith(Keywords.exeupdate))
					{
						queries[i]=queries[i].substring(Keywords.exeupdate.length());
						stmt.executeUpdate(queries[i]);
					}
					else
						stmt.executeQuery(queries[i]);
					mess=mess+" %1742%<br>\n";
				}
				catch (Exception eq)
				{
					String errorq=eq.toString()+"<br>\n";
					mess=mess+"<br>\n%1743%<br>\n"+errorq+"<br>\n";
					try
					{
						stmt.close();
						conn.close();
						stmt=null;
						conn=null;
					}
					catch (Exception ex) {}
					return new Result(mess, false, null);
				}
			}
		}

		if (type.equals("sqlserver"))
		{
			String db      = (String) parameters.get(Keywords.sqlserver_db);
			String server  = (String) parameters.get(Keywords.sqlserver_server);
			String port    = (String) parameters.get(Keywords.sqlserver_port);
			String password= (String) parameters.get(Keywords.sqlserver_password);
			String user    = (String) parameters.get(Keywords.sqlserver_user);
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
				try
				{
					stmt.close();
					conn.close();
					stmt=null;
					conn=null;
				}
				catch (Exception ex) {}
				return new Result("%2749%<br>\n", false, null);
			}
			for (int i=0; i<queries.length; i++)
			{
				queries[i]=queries[i].trim();
				mess=mess+"%1741% "+queries[i];
				try
				{
					if (queries[i].toLowerCase().startsWith(Keywords.exeupdate))
					{
						queries[i]=queries[i].substring(Keywords.exeupdate.length());
						stmt.executeUpdate(queries[i]);
					}
					else
						stmt.executeQuery(queries[i]);
					mess=mess+" %1742%<br>\n";
				}
				catch (Exception eq)
				{
					String errorq=eq.toString()+"<br>\n";
					mess=mess+"<br>\n%1743%<br>\n"+errorq+"<br>\n";
					try
					{
						stmt.close();
						conn.close();
						stmt=null;
						conn=null;
					}
					catch (Exception ex) {}
					return new Result(mess, false, null);
				}
			}
		}

		if (type.equals("msodbc"))
		{
			String db      = (String) parameters.get(Keywords.msodbc_db);
			String password= (String) parameters.get(Keywords.msodbc_password);
			String user    = (String) parameters.get(Keywords.msodbc_user);
			if (password==null)
				password="";
			try
			{
				String url = "jdbc:odbc:"+db.toLowerCase();
				Class.forName ("sun.jdbc.odbc.JdbcOdbcDriver").newInstance ();
				conn = DriverManager.getConnection (url, user, password);
				stmt = conn.createStatement();
			}
			catch (Exception e)
			{
				return new Result("%1422%<br>\n", false, null);
			}
			for (int i=0; i<queries.length; i++)
			{
				queries[i]=queries[i].trim();
				mess=mess+"%1741% "+queries[i];
				try
				{
					if (queries[i].toLowerCase().startsWith(Keywords.exeupdate))
					{
						queries[i]=queries[i].substring(Keywords.exeupdate.length());
						stmt.executeUpdate(queries[i]);
					}
					else
						stmt.executeQuery(queries[i]);
					mess=mess+" %1742%<br>\n";
				}
				catch (Exception eq)
				{
					String errorq=eq.toString()+"<br>\n";
					mess=mess+"<br>\n%1743%<br>\n"+errorq+"<br>\n";
					try
					{
						stmt.close();
						conn.close();
						stmt=null;
						conn=null;
					}
					catch (Exception ex) {}
					return new Result(mess, false, null);
				}
			}
		}

		if (type.equals("bigsql"))
		{
			String server  = (String) parameters.get(Keywords.bigsql_server);
			String port    = (String) parameters.get(Keywords.bigsql_port);
			String password= (String) parameters.get(Keywords.bigsql_password);
			String user    = (String) parameters.get(Keywords.bigsql_user);
			if (port==null)
				port="51000";
			if (password==null)
				password="";
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
				try
				{
					stmt.close();
					conn.close();
					stmt=null;
					conn=null;
				}
				catch (Exception ex) {}
				return new Result("%3829% "+e.toString()+")<br>\n", false, null);
			}
			for (int i=0; i<queries.length; i++)
			{
				queries[i]=queries[i].trim();
				mess=mess+"%1741% "+queries[i];
				try
				{
					if (queries[i].toLowerCase().startsWith(Keywords.exeupdate))
					{
						queries[i]=queries[i].substring(Keywords.exeupdate.length());
						stmt.executeUpdate(queries[i]);
					}
					else
						stmt.executeQuery(queries[i]);
					mess=mess+" %1742%<br>\n";
				}
				catch (Exception eq)
				{
					String errorq=eq.toString()+"<br>\n";
					mess=mess+"<br>\n%1743%<br>\n"+errorq+"<br>\n";
					try
					{
						stmt.close();
						conn.close();
						stmt=null;
						conn=null;
					}
					catch (Exception ex) {}
					return new Result(mess, false, null);
				}
			}
		}

		if (type.equals("hsqldblocal"))
		{
			String db      = (String) parameters.get(Keywords.hsqldblocal_dbfile);
			String password= (String) parameters.get(Keywords.hsqldblocal_password);
			String user    = (String) parameters.get(Keywords.hsqldblocal_user);
			if (password==null)
				password="";
			try
			{
				String url = "jdbc:hsqldb:file:"+db.toLowerCase();
				Class.forName ("org.hsqldb.jdbcDriver").newInstance ();
				conn = DriverManager.getConnection (url, user, password);
				stmt = conn.createStatement();
			}
			catch (Exception e)
			{
				return new Result("%1447%<br>\n", false, null);
			}
			for (int i=0; i<queries.length; i++)
			{
				queries[i]=queries[i].trim();
				mess=mess+"%1741% "+queries[i];
				try
				{
					if (queries[i].toLowerCase().startsWith(Keywords.exeupdate))
					{
						queries[i]=queries[i].substring(Keywords.exeupdate.length());
						stmt.executeUpdate(queries[i]);
					}
					else
						stmt.executeQuery(queries[i]);
					mess=mess+" %1742%<br>\n";
				}
				catch (Exception eq)
				{
					String errorq=eq.toString()+"<br>\n";
					mess=mess+"<br>\n%1743%<br>\n"+errorq+"<br>\n";
					try
					{
						stmt.close();
						conn.close();
						stmt=null;
						conn=null;
					}
					catch (Exception ex) {}
					return new Result(mess, false, null);
				}
			}
		}

		if (type.equals("hsqldbremote"))
		{
			String db      = (String) parameters.get(Keywords.hsqldbremote_db);
			String server  = (String) parameters.get(Keywords.hsqldbremote_server);
			String port    = (String) parameters.get(Keywords.hsqldbremote_port);
			String password= (String) parameters.get(Keywords.hsqldbremote_password);
			String user    = (String) parameters.get(Keywords.hsqldbremote_user);
			if (password==null)
				password="";
			if (port==null)
				port="9001";
			if (user==null)
				user="";
			try
			{
				String url = "jdbc:hsqldb:hsql://"+server+":"+port+"/"+db.toLowerCase();
				Class.forName ("org.hsqldb.jdbcDriver").newInstance ();
				if (!user.equals(""))
					conn = DriverManager.getConnection (url, user, password);
				else
					conn = DriverManager.getConnection (url);
				stmt = conn.createStatement();
			}
			catch (Exception e)
			{
				return new Result("%1439%<br>\n", false, null);
			}
			for (int i=0; i<queries.length; i++)
			{
				queries[i]=queries[i].trim();
				mess=mess+"%1741% "+queries[i];
				try
				{
					if (queries[i].toLowerCase().startsWith(Keywords.exeupdate))
					{
						queries[i]=queries[i].substring(Keywords.exeupdate.length());
						stmt.executeUpdate(queries[i]);
					}
					else
						stmt.executeQuery(queries[i]);
					mess=mess+" %1742%<br>\n";
				}
				catch (Exception eq)
				{
					String errorq=eq.toString()+"<br>\n";
					mess=mess+"<br>\n%1743%<br>\n"+errorq+"<br>\n";
					try
					{
						stmt.close();
						conn.close();
						stmt=null;
						conn=null;
					}
					catch (Exception ex) {}
					return new Result(mess, false, null);
				}
			}
		}

		try
		{
			stmt.executeUpdate("COMMIT");
		}
		catch (Exception ecommi) {}

		try
		{
			stmt.close();
			conn.close();
			stmt=null;
			conn=null;
		}
		catch (Exception ex) {}
		return new Result(mess, true, null);
	}
	public LinkedList<GetRequiredParameters> getparameters()
	{
		LinkedList<GetRequiredParameters> parameters=new LinkedList<GetRequiredParameters>();
		String[] dep ={""};
		parameters.add(new GetRequiredParameters("=", "MultipleSettings="+Keywords.dbset, true, 1749, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.query, "multipletext", true, 1740, dep, "", 2));
		parameters.add(new GetRequiredParameters("", "note", false, 1755, dep, "", 2));
		return parameters;
	}
	public String[] getstepinfo()
	{
		String[] retstepinfo=new String[2];
		retstepinfo[0]="4164";
		retstepinfo[1]="1739";
		return retstepinfo;
	}
}
