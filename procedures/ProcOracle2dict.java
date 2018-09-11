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
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Vector;
import ADaMSoft.utilities.StepUtilities;

import ADaMSoft.keywords.Keywords;
import ADaMSoft.utilities.CheckVarNames;
import ADaMSoft.utilities.GetRequiredParameters;

/**
* This procedure creates a dictionary for an Oracle data table
* @author marco.scarno@gmail.com
* @date 14/02/2017
*/
public class ProcOracle2dict implements RunStep
{
	/**
	* Creates a dictionary for an Oracle data table
	*/
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		String [] requiredparameters=new String[] {Keywords.oracle_server, Keywords.table};
		String [] optionalparameters=new String[] {Keywords.oracle_port, Keywords.oracle_user, Keywords.oracle_password, Keywords.oracle_service, Keywords.oracle_sid};
		Keywords.percentage_total=0;
		Keywords.percentage_done=0;
		StepUtilities steputilities=new StepUtilities();
		if (!steputilities.checkParameters(requiredparameters, optionalparameters, parameters))
		{
			return new Result(steputilities.getMessage(), false, null);
		}

		String server  = (String) parameters.get(Keywords.oracle_server);
		String port    = (String) parameters.get(Keywords.oracle_port);
		String password= (String) parameters.get(Keywords.oracle_password);
		String user    = (String) parameters.get(Keywords.oracle_user);
		String table   = (String) parameters.get(Keywords.table);
		String path       = (String) parameters.get(Keywords.outdict);
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

		String keyword="Oracle"+" "+table;
		String description="Oracle"+" "+table;
		String author=(String) parameters.get(Keywords.client_host.toLowerCase());
		String datatabletype=Keywords.oracle;
		Hashtable<String, String> datatableinfo=new Hashtable<String, String>();
		datatableinfo.put(Keywords.password.toLowerCase(), password);
		datatableinfo.put(Keywords.table.toLowerCase(), table);
		datatableinfo.put(Keywords.port.toLowerCase(), port);
		datatableinfo.put(Keywords.user.toLowerCase(), user);
		datatableinfo.put(Keywords.server.toLowerCase(), server);
		if ((service.equals("")) && (!sid.equals("")))
			datatableinfo.put(Keywords.sid.toLowerCase(), sid);
		else
			datatableinfo.put(Keywords.service, service);

		Connection conn=null;
		Statement stmt=null;
		try
		{
			String credential="";
			if(!user.equals(""))
				credential=user+"/"+password;
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
		Hashtable<String, String> numtype=new Hashtable<String, String>();
		numtype.put("BOOLEAN",Keywords.NUMSuffix);
		numtype.put("SMALLINT",Keywords.NUMSuffix);
		numtype.put("INT",Keywords.NUMSuffix);
		numtype.put("INTEGER",Keywords.NUMSuffix);
		numtype.put("FLOAT",Keywords.NUMSuffix);
		numtype.put("DOUBLE PRECISION",Keywords.NUMSuffix);
		numtype.put("DECIMAL",Keywords.NUMSuffix);
		numtype.put("REAL",Keywords.NUMSuffix);
		numtype.put("DEC",Keywords.NUMSuffix);
		numtype.put("NUMERIC",Keywords.NUMSuffix);
		numtype.put("SET",Keywords.NUMSuffix);
		numtype.put("BINARY_DOUBLE",Keywords.NUMSuffix);
		numtype.put("BINARY_FLOAT",Keywords.NUMSuffix);
		numtype.put("BINARY_INTEGER",Keywords.NUMSuffix);
		numtype.put("NATURAL",Keywords.NUMSuffix);
		numtype.put("NATURALN",Keywords.NUMSuffix);
		numtype.put("NUMBER",Keywords.NUMSuffix);
		numtype.put("PLS_INTEGER",Keywords.NUMSuffix);
		numtype.put("POSITIVE",Keywords.NUMSuffix);
		numtype.put("POSITIVEN",Keywords.NUMSuffix);
		numtype.put("SIGNTYPE",Keywords.NUMSuffix);

		Vector<Hashtable<String, String>> fixedvariableinfo=new Vector<Hashtable<String, String>>();
		Vector<Hashtable<String, String>> tablevariableinfo=new Vector<Hashtable<String, String>>();
		Vector<Hashtable<String, String>> codelabel=new Vector<Hashtable<String, String>>();
		Vector<Hashtable<String, String>> missingdata=new Vector<Hashtable<String, String>>();
		try
		{
			//get the first row only
			String query="select * from (select a.*, ROWNUM r_n_u_m_ from (select * from "+table+")a where ROWNUM<10) where r_n_u_m_ >=0";
			ResultSet rs = stmt.executeQuery(query);
			ResultSetMetaData metadata = rs.getMetaData();
			if(metadata.getColumnCount()==0)
			{
				try
				{
					rs=null;
					conn=null;
					stmt=null;
				}
				catch (Exception ex) {}
				return new Result("%1928%<br>\n", false, null);
			}
			for(int i=0;i<metadata.getColumnCount()-1;i++)
			{
				String name=metadata.getColumnName(i+1);
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
					varname=varname.replaceAll("\\.","_");
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
				String type=metadata.getColumnTypeName(i+1);
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

				temptablevariableinfo.put(Keywords.oraclevariablename.toLowerCase(), name);
				fixedvariableinfo.add(tempfixedvariableinfo);
				tablevariableinfo.add(temptablevariableinfo);
				codelabel.add(tempcodelabel);
				missingdata.add(tempmissingdata);
			};
			stmt.close();
			stmt=null;
			conn.close();
			conn=null;
		}
		catch (Exception e)
		{
			try
			{
				stmt.close();
				conn.close();
			}
			catch (Exception ee) {}
			stmt=null;
			conn=null;
			return new Result("%1694% ("+e.toString()+")<br>\n", false, null);
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
		parameters.add(new GetRequiredParameters(Keywords.oracle + "=","setting=oracle", true, 1696, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.table, "text", true, 1697, dep, "", 2));
		return parameters;
	}
	public String[] getstepinfo()
	{
		String[] retstepinfo=new String[2];
		retstepinfo[0]="247";
		retstepinfo[1]="1695";
		return retstepinfo;
	}
}
