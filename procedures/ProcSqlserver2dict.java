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
import java.sql.ResultSetMetaData;

import java.util.Hashtable;
import java.util.Vector;
import java.util.LinkedList;
import ADaMSoft.utilities.StepUtilities;

import ADaMSoft.keywords.Keywords;
import ADaMSoft.utilities.GetRequiredParameters;
import ADaMSoft.utilities.CheckVarNames;

/**
* This procedure creates a dictionary for a data table in a SQLSERVER DB
* @author marco.scarno@gmail.com
* @date 14/02/2017
*/
public class ProcSqlserver2dict implements RunStep
{
	/**
	* Creates a dictionary for a data table in a SQLSERVER DB
	*/
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		String [] requiredparameters=new String[] {Keywords.sqlserver_db, Keywords.sqlserver_server, Keywords.sqlserver_user, Keywords.table};
		String [] optionalparameters=new String[] {Keywords.sqlserver_port, Keywords.sqlserver_password};
		Keywords.percentage_total=0;
		Keywords.percentage_done=0;
		StepUtilities steputilities=new StepUtilities();
		if (!steputilities.checkParameters(requiredparameters, optionalparameters, parameters))
		{
			return new Result(steputilities.getMessage(), false, null);
		}

		String db      = (String) parameters.get(Keywords.sqlserver_db);
		String server  = (String) parameters.get(Keywords.sqlserver_server);
		String port    = (String) parameters.get(Keywords.sqlserver_port);
		String password= (String) parameters.get(Keywords.sqlserver_password);
		String user    = (String) parameters.get(Keywords.sqlserver_user);
		String table   = (String) parameters.get(Keywords.table);
		String path       = (String) parameters.get(Keywords.outdict);

		if (port==null)
			port="1433";

		String keyword=Keywords.sqlserver+" "+table;
		String description=Keywords.sqlserver+" "+table;
		String author=(String) parameters.get(Keywords.client_host.toLowerCase());
		String datatabletype=Keywords.sqlserver;
		Hashtable<String, String> datatableinfo=new Hashtable<String, String>();
		if (password==null)
			password="";
		else
			datatableinfo.put(Keywords.password.toLowerCase(), password);
		datatableinfo.put(Keywords.table.toLowerCase(), table);
		datatableinfo.put(Keywords.port.toLowerCase(), port);
		datatableinfo.put(Keywords.user.toLowerCase(), user);
		datatableinfo.put(Keywords.db.toLowerCase(), db);
		datatableinfo.put(Keywords.server.toLowerCase(), server);

		Connection conn=null;
		Statement stmt=null;
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
				stmt=null;
				conn.close();
				conn=null;
			}
			catch (Exception ex) {}
			return new Result("%2749%<br>\n", false, null);
		}
		Hashtable<String, String> numtype=new Hashtable<String, String>();
		numtype.put("BIGINT",Keywords.NUMSuffix);
		numtype.put("DECIMAL",Keywords.NUMSuffix);
		numtype.put("MONEY",Keywords.NUMSuffix);
		numtype.put("SMALLMONEY",Keywords.NUMSuffix);
		numtype.put("FLOAT",Keywords.NUMSuffix);
		numtype.put("INT",Keywords.NUMSuffix);
		numtype.put("NUMERIC",Keywords.NUMSuffix);
		numtype.put("REAL",Keywords.NUMSuffix);
		numtype.put("SMALLINT",Keywords.NUMSuffix);

		Vector<Hashtable<String, String>> fixedvariableinfo=new Vector<Hashtable<String, String>>();
		Vector<Hashtable<String, String>> tablevariableinfo=new Vector<Hashtable<String, String>>();
		Vector<Hashtable<String, String>> codelabel=new Vector<Hashtable<String, String>>();
		Vector<Hashtable<String, String>> missingdata=new Vector<Hashtable<String, String>>();
		try
		{
			String query = "SELECT TOP 1 * from " + table;
			ResultSet rs = stmt.executeQuery(query);
			ResultSetMetaData rsmd = rs.getMetaData();
			int cols = rsmd.getColumnCount();
			for (int i = 1; i <= cols; i++)
			{
				String varname=rsmd.getColumnName(i);
				String name=varname;
				String type=rsmd.getColumnTypeName(i).trim();
				if (varname.startsWith("0")) varname="_"+varname;
				if (varname.startsWith("1")) varname="_"+varname;
				if (varname.startsWith("2")) varname="_"+varname;
				if (varname.startsWith("3")) varname="_"+varname;
				if (varname.startsWith("4")) varname="_"+varname;
				if (varname.startsWith("5")) varname="_"+varname;
				if (varname.startsWith("6")) varname="_"+varname;
				if (varname.startsWith("7")) varname="_"+varname;
				if (varname.startsWith("8")) varname="_"+varname;
				if (varname.startsWith("9")) varname="_"+varname;
				try
				{
					varname=varname.replaceAll(" ","_");
				}
				catch(Exception ename){}
				try
				{
					varname=varname.replaceAll("#","_");
				}
				catch(Exception ename){}
				try
				{
					varname=varname.replaceAll("\\*","_");
				}
				catch(Exception ename){}
				try
				{
					varname=varname.replaceAll("\\+","_");
				}
				catch(Exception ename){}
				try
				{
					varname=varname.replaceAll("-","_");
				}
				catch(Exception ename){}
				try
				{
					varname=varname.replaceAll("&","_");
				}
				catch(Exception ename){}
				try
				{
					varname=varname.replaceAll("%","_");
				}
				catch(Exception ename){}
				try
				{
					varname=varname.replaceAll("\\|","_");
				}
				catch(Exception ename){}
				try
				{
					varname=varname.replaceAll("!","_");
				}
				catch(Exception ename){}
				try
				{
					varname=varname.replaceAll("\\$","_");
				}
				catch(Exception ename){}
				try
				{
					varname=varname.replaceAll("/","_");
				}
				catch(Exception ename){}
				try
				{
					varname=varname.replaceAll("\\(","_");
				}
				catch(Exception ename){}
				try
				{
					varname=varname.replaceAll("\\)","_");
				}
				catch(Exception ename){}
				try
				{
					varname=varname.replaceAll("=","_");
				}
				catch(Exception ename){}
				try
				{
					varname=varname.replaceAll("\\?","_");
				}
				catch(Exception ename){}
				try
				{
					varname=varname.replaceAll("<","_");
				}
				catch(Exception ename){}
				try
				{
					varname=varname.replaceAll(">","_");
				}
				catch(Exception ename){}
				Hashtable<String, String> tempfixedvariableinfo=new Hashtable<String, String>();
				Hashtable<String, String> temptablevariableinfo=new Hashtable<String, String>();
				Hashtable<String, String> tempcodelabel=new Hashtable<String, String>();
				Hashtable<String, String> tempmissingdata=new Hashtable<String, String>();
				tempfixedvariableinfo.put(Keywords.VariableName.toLowerCase(), varname);
				tempfixedvariableinfo.put(Keywords.LabelOfVariable.toLowerCase(), name);

				if (numtype.get(type.toUpperCase())!=null)
					tempfixedvariableinfo.put(Keywords.VariableFormat.toLowerCase(), Keywords.NUMSuffix);
				else
					tempfixedvariableinfo.put(Keywords.VariableFormat.toLowerCase(), Keywords.TEXTSuffix);

				temptablevariableinfo.put(Keywords.SqlServerVariableName.toLowerCase(), name);
				fixedvariableinfo.add(tempfixedvariableinfo);
				tablevariableinfo.add(temptablevariableinfo);
				codelabel.add(tempcodelabel);
				missingdata.add(tempmissingdata);
			}
			stmt.close();
		}
		catch (Exception e)
		{
			try
			{
				stmt=null;
				conn.close();
				conn=null;
			}
			catch (Exception ex) {}
			return new Result("%2757%<br>\n", false, null);
		}
		try
		{
			stmt=null;
			conn.close();
			conn=null;
		}
		catch (Exception ex) {}
		String workdir=(String)parameters.get(Keywords.WorkDir);
		if (!CheckVarNames.getResultCheck(fixedvariableinfo, workdir).equals(""))
			return new Result(CheckVarNames.getResultCheck(fixedvariableinfo, workdir), false, null);
		Vector<StepResult> result = new Vector<StepResult>();
		result.add(new LocalDictionaryWriter(path, keyword, description, author, datatabletype,
		datatableinfo, fixedvariableinfo, tablevariableinfo, codelabel, missingdata, null));
		return new Result("", true, result);
	}
	public LinkedList<GetRequiredParameters> getparameters()
	{
		LinkedList<GetRequiredParameters> parameters=new LinkedList<GetRequiredParameters>();
		String[] dep ={""};
		parameters.add(new GetRequiredParameters(Keywords.outdict+"=", "outdictreport", true, 249, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.sqlserver + "=","setting=sqlserver", true, 2760, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.table, "text", false, 706, dep, "", 2));
		return parameters;
	}
	public String[] getstepinfo()
	{
		String[] retstepinfo=new String[2];
		retstepinfo[0]="247";
		retstepinfo[1]="2759";
		return retstepinfo;
	}
}
