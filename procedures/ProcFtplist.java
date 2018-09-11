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

import java.io.*;
import java.net.*;
import java.util.LinkedList;
import java.util.Hashtable;
import java.util.Vector;

import ADaMSoft.keywords.Keywords;
import ADaMSoft.utilities.StepUtilities;
import ADaMSoft.utilities.GetRequiredParameters;
import ADaMSoft.utilities.DataSetUtilities;
import ADaMSoft.dataaccess.DataWriter;

/**
* This is the procedure that lists all the files and all the directories in a ftp server
* @author marco.scarno@gmail.com
* @date 19/02/2017
*/
public class ProcFtplist implements RunStep
{
	Vector<String[]> dircontent;
	/**
	* Starts the execution of Proc Listdir
	*/
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		dircontent=new Vector<String[]>();
		String [] requiredparameters=new String[] {Keywords.OUT.toLowerCase(), Keywords.ftp};
		String [] optionalparameters=new String[] {Keywords.ftpuser, Keywords.ftppassword, Keywords.subdir, Keywords.filterfile};
		Keywords.percentage_total=0;
		Keywords.percentage_done=0;
		StepUtilities steputilities=new StepUtilities();
		if (!steputilities.checkParameters(requiredparameters, optionalparameters, parameters))
		{
			return new Result(steputilities.getMessage(), false, null);
		}

		String filterfile=(String)parameters.get(Keywords.filterfile);
		String ftp=(String)parameters.get(Keywords.ftp);
		if (ftp.startsWith("ftp://")) ftp=ftp.substring(6);
		String user=(String)parameters.get(Keywords.ftpuser);
		String password=(String)parameters.get(Keywords.ftppassword);
		String subdir=(String)parameters.get(Keywords.subdir);

		DataWriter dw=new DataWriter(parameters, Keywords.OUT.toLowerCase());
		if (!dw.getmessage().equals(""))
		{
			return new Result(dw.getmessage(), false, null);
		}

		String author=(String) parameters.get(Keywords.client_host.toLowerCase());

		String url = "ftp://";
		if (user!=null)
			url=url+user;
		if (password!=null)
			url=url+":"+password;
		if ((user!=null) || (password!=null))
			url=url+"@";
		url=url+ftp;
		if (subdir!=null)
		{
			if (!subdir.endsWith("/"))
				subdir=subdir+"/";
			url=url+"/"+subdir;
		}

		String keyword="Listftp "+url;
		String description="Lisftp "+url;

		Keywords.percentage_done=1;

		try
		{
			InputStream input = (new URL(url)).openStream();
			BufferedReader file = new BufferedReader(new InputStreamReader(input));
			while (file.ready())
			{
				String line = file.readLine();
				String typefile="";
				boolean unixstyle=false;
				if (!line.toLowerCase().startsWith("total"))
				{
					if (line.toLowerCase().startsWith("d"))
					{
						typefile="0";
						unixstyle=true;
					}
					else if (line.toLowerCase().startsWith("l"))
					{
						typefile="2";
						unixstyle=true;
					}
					else if (line.toLowerCase().startsWith("-"))
					{
						typefile="1";
						unixstyle=true;
					}
					if (!unixstyle)
					{
						typefile="1";
						if (line.indexOf("DIR")>0)
							typefile="2";
					}
					String filename=line.substring(line.lastIndexOf(" "));
					String[] result=new String[2];
					result[0]=typefile;
					result[1]=filename;
					dircontent.add(result);
				}
			}
			file.close();
			input.close();
		}
		catch (Exception e)
		{
			return new Result("%1260% ("+url+")<br>\n"+e.toString()+"<br>\n", false, null);
		}
		DataSetUtilities dsu=new DataSetUtilities();

		Hashtable<String, String> tempmd=new Hashtable<String, String>();

		Hashtable<String, String> tempcl=new Hashtable<String, String>();
		tempcl.put("0", "%1263%");
		tempcl.put("1", "%1264%");
		tempcl.put("2", "%1265%");

		dsu.addnewvar("type", "%1261%", Keywords.TEXTSuffix, tempcl, tempmd);
		dsu.addnewvar("name", "%1262%", Keywords.TEXTSuffix, tempmd, tempmd);

		if (!dw.opendatatable(dsu.getfinalvarinfo()))
		{
			Keywords.procedure_error=true;
			return new Result(dw.getmessage(), false, null);
		}

		String[] valuestowrite=new String[2];

		Keywords.percentage_done=2;
		for (int i=0; i<dircontent.size(); i++)
		{
			String[] tempname=dircontent.get(i);
			boolean writefile=true;
			if (filterfile!=null)
			{
				if (tempname[1].toLowerCase().indexOf(filterfile.toLowerCase())<0)
					writefile=false;
			}
			if (tempname[1].equals("."))
				writefile=false;
			if (tempname[1].equals(".."))
				writefile=false;
			if (writefile)
			{
				valuestowrite[0]=tempname[0];
				valuestowrite[1]=tempname[1];
				dw.write(valuestowrite);
			}
		}
		Vector<StepResult> result = new Vector<StepResult>();
		boolean resclose=dw.close();
		if (!resclose)
			return new Result(dw.getmessage(), false, null);
		Vector<Hashtable<String, String>> tablevariableinfo=dw.getVarInfo();
		Hashtable<String, String> datatableinfo=dw.getTableInfo();
		result.add(new LocalDictionaryWriter(dw.getdictpath(), keyword, description, author, dw.gettabletype(),
		datatableinfo, dsu.getfinalvarinfo(), tablevariableinfo, dsu.getfinalcl(), dsu.getfinalmd(), null));
		return new Result("", true, result);
	}
	/**
	*Returns the parameters for the procedure
	*/
	public LinkedList<GetRequiredParameters> getparameters()
	{
		LinkedList<GetRequiredParameters> parameters=new LinkedList<GetRequiredParameters>();
		String[] dep ={""};
		parameters.add(new GetRequiredParameters(Keywords.OUT.toLowerCase()+"=", "setting=out", true, 1254, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.ftp, "text", true, 1266, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.ftpuser, "text", false, 1267, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.ftppassword, "text", false, 1268, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.subdir, "text", false, 1269, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.filterfile, "text", false, 1256, dep, "", 2));
		parameters.add(new GetRequiredParameters("", "note", false, 1257, dep, "", 2));
		return parameters;
	}
	/**
	*Returns the group and the name of the procedure
	*/
	public String[] getstepinfo()
	{
		String[] retprocinfo=new String[2];
		retprocinfo[0]="4173";
		retprocinfo[1]="1259";
		return retprocinfo;
	}
}
