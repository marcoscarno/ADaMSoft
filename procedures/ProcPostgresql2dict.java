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
import java.sql.ResultSetMetaData;
import java.sql.Statement;

import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Vector;
import java.util.LinkedList;

import ADaMSoft.utilities.StepUtilities;
import ADaMSoft.keywords.Keywords;
import ADaMSoft.utilities.GetRequiredParameters;
import ADaMSoft.utilities.CheckVarNames;

/**
* This procedure creates a dictionary for a POstgreSQL data table
* @author marco.scarno@gmail.com
* @date 14/02/2017
*/
public class ProcPostgresql2dict implements RunStep
{
	/**
	* Creates a dictionary for a PostgreSQL data table
	*/
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		String [] requiredparameters=new String[] {Keywords.postgresql_db, Keywords.postgresql_server, Keywords.postgresql_user, Keywords.table};
		String [] optionalparameters=new String[] {Keywords.postgresql_port, Keywords.postgresql_password};
		Keywords.percentage_total=0;
		Keywords.percentage_done=0;
		StepUtilities steputilities=new StepUtilities();
		if (!steputilities.checkParameters(requiredparameters, optionalparameters, parameters))
		{
			return new Result(steputilities.getMessage(), false, null);
		}

		String db      = (String) parameters.get(Keywords.postgresql_db);
		String server  = (String) parameters.get(Keywords.postgresql_server);
		String port    = (String) parameters.get(Keywords.postgresql_port);
		String password= (String) parameters.get(Keywords.postgresql_password);
		String user    = (String) parameters.get(Keywords.postgresql_user);
		String table   = (String) parameters.get(Keywords.table);
		String path       = (String) parameters.get(Keywords.outdict);

		if (port==null)
			port="5432";

		String keyword=Keywords.postgresql+" "+table;
		String description=Keywords.postgresql+" "+table;
		String author=(String) parameters.get(Keywords.client_host.toLowerCase());
		String datatabletype=Keywords.postgresql;
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
			return new Result("%1404%<br>\n"+e.toString()+"<br>\n", false, null);
		}
		Hashtable<String, String> numtype=new Hashtable<String, String>();
		numtype.put("TINYINT",Keywords.NUMSuffix);
		numtype.put("BIT",Keywords.NUMSuffix);
		numtype.put("BOOL",Keywords.NUMSuffix);
		numtype.put("BOOLEAN",Keywords.NUMSuffix);
		numtype.put("SMALLINT",Keywords.NUMSuffix);
		numtype.put("MEDIUMINT",Keywords.NUMSuffix);
		numtype.put("INT",Keywords.NUMSuffix);
		numtype.put("INTEGER",Keywords.NUMSuffix);
		numtype.put("BIGINT",Keywords.NUMSuffix);
		numtype.put("FLOAT",Keywords.NUMSuffix);
		numtype.put("DOUBLE",Keywords.NUMSuffix);
		numtype.put("REAL",Keywords.NUMSuffix);
		numtype.put("DECIMAL",Keywords.NUMSuffix);
		numtype.put("DEC",Keywords.NUMSuffix);
		numtype.put("NUMERIC",Keywords.NUMSuffix);
		numtype.put("FIXED",Keywords.NUMSuffix);
		numtype.put("ENUM",Keywords.NUMSuffix);
		numtype.put("SET",Keywords.NUMSuffix);

		Vector<Hashtable<String, String>> fixedvariableinfo=new Vector<Hashtable<String, String>>();
		Vector<Hashtable<String, String>> tablevariableinfo=new Vector<Hashtable<String, String>>();
		Vector<Hashtable<String, String>> codelabel=new Vector<Hashtable<String, String>>();
		Vector<Hashtable<String, String>> missingdata=new Vector<Hashtable<String, String>>();
		try
		{
			String query = "SELECT * FROM " + table;
			ResultSet rs = stmt.executeQuery(query);
			ResultSetMetaData meta = rs.getMetaData();
			if (meta.getColumnCount()==0)
			{
				try
				{
					rs=null;
					conn=null;
					stmt=null;
				}
				catch (Exception ex) {}
				return new Result("%1407%\n", false, null);
			}
			for(int i=0;i<meta.getColumnCount();i++)
			{
				String name=meta.getColumnLabel(i+1);
				String varname=name;
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
					varname=varname.replaceAll("\\.","_");
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
				String type=meta.getColumnTypeName(i+1);
				Hashtable<String, String> tempfixedvariableinfo=new Hashtable<String, String>();
				Hashtable<String, String> temptablevariableinfo=new Hashtable<String, String>();
				Hashtable<String, String> tempcodelabel=new Hashtable<String, String>();
				Hashtable<String, String> tempmissingdata=new Hashtable<String, String>();
				tempfixedvariableinfo.put(Keywords.VariableName.toLowerCase(), varname);
				tempfixedvariableinfo.put(Keywords.LabelOfVariable.toLowerCase(), name);

				String typevar="";
				for (Enumeration<String> e = numtype.keys() ; e.hasMoreElements() ;)
				{
					String typename = ((String) e.nextElement());
					String formatname = numtype.get(typename);
					if ((typename.toLowerCase()).startsWith(type.toLowerCase()))
						typevar=formatname;
				}
				if (!typevar.equals(""))
					tempfixedvariableinfo.put(Keywords.VariableFormat.toLowerCase(), Keywords.NUMSuffix);
				else
					tempfixedvariableinfo.put(Keywords.VariableFormat.toLowerCase(), Keywords.TEXTSuffix);

				temptablevariableinfo.put(Keywords.PostgresqlVariableName.toLowerCase(), name);
				fixedvariableinfo.add(tempfixedvariableinfo);
				tablevariableinfo.add(temptablevariableinfo);
				codelabel.add(tempcodelabel);
				missingdata.add(tempmissingdata);
			}
			try
			{
				stmt.close();
				conn.close();
				stmt=null;
				conn=null;
			}
			catch (Exception ex) {}
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
			return new Result("%1407%<br>\n", false, null);
		}
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
		parameters.add(new GetRequiredParameters(Keywords.postgresql + "=","setting=postgresql", true, 1415, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.table, "text", true, 1416, dep, "", 2));
		return parameters;
	}
	public String[] getstepinfo()
	{
		String[] retstepinfo=new String[2];
		retstepinfo[0]="247";
		retstepinfo[1]="1417";
		return retstepinfo;
	}
}
