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

import ADaMSoft.dataaccess.DictionaryReader;
import ADaMSoft.dataaccess.DataReader;
import ADaMSoft.dataaccess.DataWriter;

import ADaMSoft.utilities.GetRequiredParameters;
import ADaMSoft.utilities.VariableUtilities;
import ADaMSoft.utilities.DataSetUtilities;
import ADaMSoft.utilities.StepUtilities;

import ADaMSoft.keywords.Keywords;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Vector;
import java.util.Enumeration;

/**
* This is the procedure that clear the values of the localized variables
* @author marco.scarno@gmail.com
* @date 13/02/2017
*/
public class ProcClearlocvars implements RunStep
{
	/**
	* Starts the execution of Proc Clearlocvars and returns the corresponding message
	*/
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		String [] requiredparameters=new String[] {Keywords.OUT.toLowerCase(), Keywords.dict};
		String [] optionalparameters=new String[] {Keywords.replace, Keywords.where};
		Keywords.percentage_total=0;
		Keywords.percentage_done=0;
		StepUtilities steputilities=new StepUtilities();
		if (!steputilities.checkParameters(requiredparameters, optionalparameters, parameters))
		{
			return new Result(steputilities.getMessage(), false, null);
		}
		DataWriter dw=new DataWriter(parameters, Keywords.OUT.toLowerCase());
		if (!dw.getmessage().equals(""))
			return new Result(dw.getmessage(), false, null);

		DictionaryReader dict = (DictionaryReader)parameters.get(Keywords.dict);
		String vartemp=(String)parameters.get(Keywords.var.toLowerCase());
		VariableUtilities varu=new VariableUtilities(dict, null, vartemp, null, null, null);
		if (varu.geterror())
			return new Result(varu.getmessage(), false, null);

		String replace=(String)parameters.get(Keywords.replace);

		String keyword="Clearlocvars "+dict.getkeyword();
		String description="Clearlocvars "+dict.getdescription();
		String author=dict.getauthor();

		DataSetUtilities dsu=new DataSetUtilities();
		dsu.setreplace(replace);
		dsu.defineolddict(dict);

		int rifrep=0;
		if (replace==null)
			rifrep=0;
		else if (replace.equalsIgnoreCase(Keywords.replaceall))
		{
			dsu.setempycodelabels();
			dsu.setempymissingdata();
			rifrep=1;
		}
		else if (replace.equalsIgnoreCase(Keywords.replaceformat))
		{
			dsu.setempycodelabels();
			rifrep=2;
		}
		else if (replace.equalsIgnoreCase(Keywords.replacemissing))
		{
			dsu.setempymissingdata();
			rifrep=3;
		}
		Hashtable<String, Integer> varref=new Hashtable<String, Integer>();
		int posloc=-1;
		int possol=-1;
		boolean type=false;
		for (int i=0; i<dict.gettotalvar(); i++)
		{
			varref.put((dict.getvarname(i)).toUpperCase(), new Integer(i));
			if (dict.getvarname(i).equalsIgnoreCase("result_localize"))
			{
				posloc=i;
			}
			if (dict.getvarname(i).equalsIgnoreCase("result_localize_all"))
			{
				type=true;
				posloc=i;
			}
			if (dict.getvarname(i).equalsIgnoreCase("solution_localize"))
			{
				possol=i;
			}
		}
		if (posloc==-1)
			return new Result("%2514%<br>\n", false, null);
		Vector<StepResult> results = new Vector<StepResult>();
		if ((type) && (possol==-1))
			results.add(new LocalMessageGetter("%2559%<br>\n"));
		if ((type) && (possol!=-1))
			results.add(new LocalMessageGetter("%2560%<br>\n"));

		DataReader data = new DataReader(dict);

		if (!data.open(null, rifrep, false))
			return new Result(data.getmessage(), false, null);
		String where=(String)parameters.get(Keywords.where.toLowerCase());
		if (where!=null)
		{
			if (!data.setcondition(where)) return new Result(data.getmessage(), false, null);
		}

		if (!dw.opendatatable(dsu.getfinalvarinfo()))
			return new Result(dw.getmessage(), false, null);

		Hashtable<String, String> allvalues=new Hashtable<String, String>();

		String[] values=null;
		String currloc="";
		String currsol="";

		String[] locs=new String[0];
		String[] sols=new String[0];

		String[] clocs=new String[0];
		String[] csols=new String[0];

		String[] clocss=new String[0];
		String[] csolss=new String[0];
		int validgroup=0;
		String varname;
		int refdetvars;
		while (!data.isLast())
		{
			values = data.getRecord();
			allvalues.clear();
			if (values!=null)
			{
				validgroup++;
				currloc=values[posloc].trim();
				currsol=values[possol].trim();
				if ((!currloc.equals("1")) && (!currloc.equals("9")) && (!currloc.equals("99")) && (!currloc.equals("999")) && (!currloc.equals("9999")) && (!currloc.equals("99999")))
				{
					if (currloc.indexOf(";")>=0)
					{
						locs=currloc.split(";");
						sols=currsol.split(";");
						for (int i=0; i<locs.length; i++)
						{
							if (locs[i].trim().indexOf(",")>=0)
							{
								clocs=locs[i].trim().split(",");
								csols=sols[i].trim().split(",");
								clocss=clocs[0].trim().split(" ");
								csolss=csols[0].trim().split(" ");
								for (int j=0; j<clocss.length; j++)
								{
									allvalues.put(clocss[j].trim().toUpperCase(), csolss[j].trim());
								}
							}
							else
							{
								clocs=locs[i].trim().split(" ");
								csols=sols[i].trim().split(" ");
								for (int j=0; j<clocs.length; j++)
								{
									allvalues.put(clocs[j].trim().toUpperCase(), csols[j].trim());
								}
							}
						}
					}
					else
					{
						if (currloc.trim().indexOf(",")>=0)
						{
							clocs=currloc.trim().split(",");
							csols=currsol.trim().split(",");
							clocss=clocs[0].trim().split(" ");
							csolss=csols[0].trim().split(" ");
							for (int j=0; j<clocss.length; j++)
							{
								allvalues.put(clocss[j].trim().toUpperCase(), csolss[j].trim());
							}
						}
						else
						{
							clocss=currloc.trim().split(" ");
							csolss=currsol.trim().split(" ");
							for (int j=0; j<clocss.length; j++)
							{
								allvalues.put(clocss[j].trim().toUpperCase(), csolss[j].trim());
							}
						}
					}
					if (allvalues.size()>0)
					{
						for (Enumeration<String> en=allvalues.keys(); en.hasMoreElements();)
						{
							varname=en.nextElement();
							refdetvars=(varref.get(varname.toUpperCase())).intValue();
							values[refdetvars]="";
						}
					}
				}
				dw.write(values);
			}
		}
		data.close();
		if (validgroup==0)
		{
			dw.deletetmp();
			return new Result("%2807%<br>\n", false, null);
		}

		boolean resclose=dw.close();
		if (!resclose)
			return new Result(dw.getmessage(), false, null);
		Vector<Hashtable<String, String>> tablevariableinfo=dw.getVarInfo();
		Hashtable<String, String> datatableinfo=dw.getTableInfo();
		Vector<StepResult> result = new Vector<StepResult>();
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
		parameters.add(new GetRequiredParameters(Keywords.dict+"=", "dict", true, 541, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.OUT.toLowerCase()+"=", "setting=out", true, 2515, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.viewout, "checkbox", false, 1612, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.where,"text", false, 2805,dep,"",2));
		parameters.add(new GetRequiredParameters("", "note", false, 2806, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.replace, "listsingle=530_NULL,531_"+Keywords.replaceall+",532_"+Keywords.replaceformat+",533_"+Keywords.replacemissing,false, 534, dep, "", 2));
		return parameters;
	}
	/**
	*Returns the group and the name of the procedure
	*/
	public String[] getstepinfo()
	{
		String[] retprocinfo=new String[2];
		retprocinfo[0]="2439";
		retprocinfo[1]="2513";
		return retprocinfo;
	}
}
