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

import java.util.Hashtable;
import java.util.LinkedList;
import java.io.File;
import java.io.BufferedWriter;
import java.io.FileWriter;

import ADaMSoft.keywords.Keywords;
import ADaMSoft.utilities.GetRequiredParameters;
import ADaMSoft.utilities.StepUtilities;

/**
* This procedure creates an ascii file from a MySQL data table
* @author marco.scarno@gmail.com
* @date 14/02/2017
*/
public class ProcMysqlcreateascii implements RunStep
{
	/**
	* Creates an ascii file from a MySQL data table
	*/
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		String [] requiredparameters=new String[] {Keywords.mysql_db, Keywords.mysql_server, Keywords.mysql_user, Keywords.table, Keywords.outfile};
		String [] optionalparameters=new String[] {Keywords.mysql_port, Keywords.mysql_password};
		Keywords.percentage_total=0;
		Keywords.percentage_done=0;
		StepUtilities steputilities=new StepUtilities();
		if (!steputilities.checkParameters(requiredparameters, optionalparameters, parameters))
		{
			return new Result(steputilities.getMessage(), false, null);
		}

		String db      = (String) parameters.get(Keywords.mysql_db);
		String server  = (String) parameters.get(Keywords.mysql_server);
		String port    = (String) parameters.get(Keywords.mysql_port);
		String password= (String) parameters.get(Keywords.mysql_password);
		String user    = (String) parameters.get(Keywords.mysql_user);
		String table   = (String) parameters.get(Keywords.table);
		String outfile = (String) parameters.get(Keywords.outfile);
		outfile=outfile.replaceAll("\\\\","/");
		if (!outfile.endsWith(".txt"))
			outfile=outfile+".txt";

		boolean exists = (new File(outfile)).exists();
		if (exists)
			return new Result("%3013%<br>\n", false, null);

		String maindir=(new File(outfile)).getParent();
		maindir=maindir.replaceAll("\\\\","/");
		exists = (new File(maindir)).exists();
		if (!exists) return new Result("%3036%<br>\n", false, null);

		BufferedWriter outtest=null;
		try
		{
			outtest = new BufferedWriter(new FileWriter(outfile));
			outtest.write("TEST\n");
			outtest.close();
			exists = (new File(outfile)).exists();
			if (!exists) return new Result("%3037%<br>\n", false, null);
			(new File(outfile)).delete();
		}
		catch (Exception et)
		{
			try
			{
				if (outtest!=null) outtest.close();
			}
			catch (Exception ee) {}
			exists = (new File(outfile)).delete();
			return new Result("%3037%<br>\n", false, null);
		}

		if (port==null)
			port="3306";

		Connection conn=null;
		Statement stmt=null;
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
				conn=null;
				stmt=null;
			}
			catch (Exception ex) {}
			return new Result("%694%<br>\n", false, null);
		}
		try
		{
			stmt.executeUpdate("USE "+ db);
		}
		catch (Exception sqle)
		{
			try
			{
				conn=null;
				stmt=null;
			}
			catch (Exception ex) {}
			return new Result("%698%<br>\n", false, null);
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

		String exportquery="select";
		try
		{
			String query = "DESCRIBE " + table;
			ResultSet rs = stmt.executeQuery(query);
			rs.last();
			int numvar=rs.getRow();
			if (numvar==0)
			{
				try
				{
					rs=null;
					conn=null;
					stmt=null;
				}
				catch (Exception ex) {}
				return new Result("%707%<br>\n", false, null);
			}
			rs.first();
			int cvar=1;
			do
			{
				String name=rs.getString(1);
				exportquery=exportquery+" '"+name+"'";
				if (cvar<numvar)
					exportquery=exportquery+",";
				cvar++;
			}while(rs.next());
			rs.first();
			exportquery=exportquery+" UNION SELECT";
			cvar=1;
			do
			{
				String name=rs.getString(1);
				exportquery=exportquery+" REPLACE(REPLACE(REPLACE(IFNULL("+name+",''),CHAR(13),' '), CHAR(10),' '), CHAR(9),' ') as "+name;
				if (cvar<numvar)
					exportquery=exportquery+",";
				cvar++;
			}while(rs.next());
			exportquery=exportquery+" INTO OUTFILE '"+outfile+"' from "+table;
			stmt.executeQuery(exportquery);
			stmt.close();
		}
		catch (Exception e)
		{
			(new File(outfile)).delete();
			return new Result("%3015% ("+e.toString()+")<br>\n", false, null);
		}
		return new Result("%3012% ("+outfile+")<br>\n", true, null);
	}
	public LinkedList<GetRequiredParameters> getparameters()
	{
		LinkedList<GetRequiredParameters> parameters=new LinkedList<GetRequiredParameters>();
		String[] dep ={""};
		parameters.add(new GetRequiredParameters(Keywords.mysql + "=","setting=mysql", true, 705, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.table, "text", true, 3011, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.outfile, "filesave=.txt", true, 3010, dep, "", 2));
		return parameters;
	}
	public String[] getstepinfo()
	{
		String[] retstepinfo=new String[2];
		retstepinfo[0]="4164";
		retstepinfo[1]="3009";
		return retstepinfo;
	}
}
