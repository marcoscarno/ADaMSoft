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

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Hashtable;
import java.util.Vector;
import java.util.LinkedList;

import ADaMSoft.keywords.Keywords;
import ADaMSoft.utilities.GetRequiredParameters;
import ADaMSoft.utilities.StepUtilities;

import ADaMSoft.dataaccess.DataWriter;
import ADaMSoft.utilities.DataSetUtilities;

/**
* This procedure creates a data set from a SCIA data file
* @author marco.scarno@gmail.com
* @date 14/02/2017
*/
public class ProcImportscia2ds implements RunStep
{
	/**
	* Creates a data set from a SCIA file
	*/
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		String [] requiredparameters=new String[] {Keywords.sciadatafile, Keywords.sciadatainfo, Keywords.OUT.toLowerCase()};
		String [] optionalparameters=new String[] {};
		Keywords.percentage_total=0;
		Keywords.percentage_done=0;
		StepUtilities steputilities=new StepUtilities();
		if (!steputilities.checkParameters(requiredparameters, optionalparameters, parameters))
		{
			return new Result(steputilities.getMessage(), false, null);
		}
		String sciadatafile    = (String) parameters.get(Keywords.sciadatafile);
		String sciadatainfo       = (String) parameters.get(Keywords.sciadatainfo);

		Keywords.percentage_total=100;
		Keywords.percentage_done=1;

		DataWriter dw=new DataWriter(parameters, Keywords.OUT.toLowerCase());
		if (!dw.getmessage().equals(""))
			return new Result(dw.getmessage(), false, null);

		java.net.URL filesciadatafile;
		try
		{
			if((sciadatafile.toLowerCase()).startsWith("http"))
				filesciadatafile =  new java.net.URL(sciadatafile);
			else
			{
				File file=new File(sciadatafile);
				filesciadatafile = file.toURI().toURL();
			}
		}
		catch (Exception e)
		{
			return new Result("%3819% ("+e.toString()+")<br>\n", false, null);
		}
		java.net.URL filesciadatainfo;
		try
		{
			if((sciadatainfo.toLowerCase()).startsWith("http"))
				filesciadatainfo =  new java.net.URL(sciadatainfo);
			else
			{
				File file=new File(sciadatainfo);
				filesciadatainfo = file.toURI().toURL();
			}
		}
		catch (Exception e)
		{
			return new Result("%3820% ("+e.toString()+")<br>\n", false, null);
		}

		String keyword="Importscia2ds "+sciadatafile;
		String description="Importscia2ds "+sciadatafile;
		String author=(String) parameters.get(Keywords.client_host.toLowerCase());

		Vector<String[]> active_vars=new Vector<String[]>();
		Vector<String[]> retained_vars=new Vector<String[]>();
		int position_identifier=-1;
		int length_identifier=-1;
		Hashtable<String, Hashtable<String, String>> code_values=new Hashtable<String, Hashtable<String, String>>();

		BufferedReader in = null;

		try
		{
	        in = new BufferedReader(new InputStreamReader(filesciadatainfo.openStream()));
        	String str;
        	String tempstr="";
        	String[] p1=null;
        	String[] p2=null;
        	while ((str = in.readLine()) != null)
        	{
				if (!str.trim().equals(""))
				{
					tempstr=str;
					if (str.startsWith("Position of active variable and length:"))
					{
						tempstr=tempstr.replaceAll("Position of active variable and length:","");
						tempstr=tempstr.trim();
						p1=tempstr.split("=");
						if (p1.length==2)
						{
							p2=p1[1].split("-");
							if (p2.length==2)
							{
								String[] av=new String[3];
								av[0]=(p1[0].trim()).toUpperCase();
								av[1]=p2[0].trim();
								av[2]=p2[1].trim();
								active_vars.add(av);
							}
						}
					}
					if (str.startsWith("Position of retained variable and length:"))
					{
						tempstr=tempstr.replaceAll("Position of retained variable and length:","");
						tempstr=tempstr.trim();
						p1=tempstr.split("=");
						if (p1.length==2)
						{
							p2=p1[1].split("-");
							if (p2.length==2)
							{
								String[] av=new String[3];
								av[0]=(p1[0].trim()).toUpperCase();
								av[1]=p2[0].trim();
								av[2]=p2[1].trim();
								retained_vars.add(av);
							}
						}
					}
					if (str.startsWith("Position of IDENTIFIER and length:"))
					{
						tempstr=tempstr.replaceAll("Position of IDENTIFIER and length:","");
						tempstr=tempstr.trim();
						p1=tempstr.split("-");
						if (p1.length==2)
						{
							position_identifier=Integer.parseInt(p1[0].trim());
							length_identifier=Integer.parseInt(p1[1].trim());
						}
					}
					if (str.startsWith("Code value for: "))
					{
						tempstr=tempstr.replaceAll("Code value for: ","");
						tempstr=tempstr.trim();
						p1=tempstr.split("=");
						if (p1.length==3)
						{
							if (code_values.get((p1[0].trim()).toUpperCase())==null)
							{
								Hashtable<String, String> temp_code_values=new Hashtable<String, String>();
								temp_code_values.put(p1[2].trim(), p1[1].trim());
								code_values.put( (p1[0].trim()).toUpperCase(), temp_code_values);
							}
							else
							{
								Hashtable<String, String> temp_code_values=code_values.get((p1[0].trim()).toUpperCase());
								temp_code_values.put(p1[2].trim(), p1[1].trim());
								code_values.put((p1[0].trim()).toUpperCase(), temp_code_values);
							}
						}
					}
				}
	        }
			in.close();
		}
		catch (Exception e)
		{
			if (in!=null)
			try
			{
				in.close();
			}
			catch (Exception ee){}
			String msgexc=e.toString();
			return new Result("%3821% ("+sciadatainfo+")<br>\n"+msgexc+"<br>\n", false, null);
		}
		DataSetUtilities dsu=new DataSetUtilities();
		int total_vars=0;
		for (int i=0; i<active_vars.size(); i++)
		{
			total_vars++;
			Hashtable<String, String> tempmd=new Hashtable<String, String>();
			String[] av=active_vars.get(i);
			dsu.addnewvar("scia_"+av[0], "%3822%"+av[0], Keywords.TEXTSuffix, tempmd, tempmd);
		}
		for (int i=0; i<retained_vars.size(); i++)
		{
			total_vars++;
			Hashtable<String, String> tempmd=new Hashtable<String, String>();
			String[] av=retained_vars.get(i);
			dsu.addnewvar(av[0], av[0], Keywords.TEXTSuffix, tempmd, tempmd);
		}
		if (position_identifier!=-1)
		{
			total_vars++;
			Hashtable<String, String> tempmd=new Hashtable<String, String>();
			dsu.addnewvar("Identifier", "Record identifier", Keywords.TEXTSuffix, tempmd, tempmd);
		}

		if (!dw.opendatatable(dsu.getfinalvarinfo()))
			return new Result(dw.getmessage(), false, null);

		String[] valuestowrite=new String[total_vars];

		Keywords.percentage_done=2;

		try
		{
	        in = new BufferedReader(new InputStreamReader(filesciadatafile.openStream()));
        	String str;
        	int tempstart=0;
        	int templength=0;
        	int refvars=0;
        	String vname="";
        	while ((str = in.readLine()) != null)
        	{
				refvars=0;
				if (!str.trim().equals(""))
				{
					for (int i=0; i<active_vars.size(); i++)
					{
						String[] av=active_vars.get(i);
						vname=av[0];
						tempstart=Integer.parseInt(av[1]);
						templength=Integer.parseInt(av[2]);
						valuestowrite[refvars]=str.substring(tempstart-1, tempstart+templength-1);
						if (code_values.get(vname)!=null)
						{
							Hashtable<String, String> temp_code_values=code_values.get(vname);
							valuestowrite[refvars]=temp_code_values.get(valuestowrite[refvars]);
						}
						refvars++;
					}
					for (int i=0; i<retained_vars.size(); i++)
					{
						String[] av=retained_vars.get(i);
						tempstart=Integer.parseInt(av[1]);
						templength=Integer.parseInt(av[2]);
						valuestowrite[refvars]=str.substring(tempstart-1, tempstart+templength-1);
						refvars++;
					}
					if (position_identifier!=-1)
					{
						valuestowrite[refvars]=str.substring(position_identifier-1, position_identifier+length_identifier-1);
					}
					dw.write(valuestowrite);
				}
			}
		}
		catch (Exception e)
		{
			if (in!=null)
			try
			{
				in.close();
			}
			catch (Exception ee){}
			String msgexc=e.toString();
			return new Result("%3819% ("+sciadatafile+")<br>\n"+msgexc+"<br>\n", false, null);
		}
		Keywords.percentage_done=3;
		Keywords.percentage_done=0;
		Keywords.percentage_total=0;
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
	public LinkedList<GetRequiredParameters> getparameters()
	{
		LinkedList<GetRequiredParameters> parameters=new LinkedList<GetRequiredParameters>();
		String[] dep ={""};
		parameters.add(new GetRequiredParameters(Keywords.OUT.toLowerCase()+"=", "setting=out", true, 3816, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.sciadatafile, "file=all", true, 3817, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.sciadatainfo, "file=all", true, 3818, dep, "", 2));
		return parameters;
	}
	public String[] getstepinfo()
	{
		String[] retstepinfo=new String[2];
		retstepinfo[0]="4167";
		retstepinfo[1]="3815";
		return retstepinfo;
	}
}
