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

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Vector;
import ADaMSoft.utilities.StepUtilities;

import ADaMSoft.dataaccess.DataReader;
import ADaMSoft.dataaccess.DataWriter;
import ADaMSoft.dataaccess.DictionaryReader;
import ADaMSoft.keywords.Keywords;
import ADaMSoft.utilities.DataSetUtilities;
import ADaMSoft.utilities.GetRequiredParameters;
import ADaMSoft.utilities.SortRequestedVar;
import ADaMSoft.utilities.ValuesParser;
import ADaMSoft.utilities.VariableUtilities;

/**
* This is the procedure that splits one or more variables
* @author marco.scarno@gmail.com
* @date 14/02/2017
*/
public class ProcVarsplit implements RunStep
{
	/**
	* Starts the execution of Proc varsplit and returns the corresponding message
	*/
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		String [] requiredparameters=new String[] {Keywords.OUT.toLowerCase(), Keywords.dict, Keywords.var};
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

		String[] var=varu.getanalysisvar();
		String[] totalvar=varu.getallvar();

		var=SortRequestedVar.getreqsorted(var, totalvar);

		String replace=(String)parameters.get(Keywords.replace);
		int[] replacerule=varu.getreplaceruleforsel(replace);
		int[] allreplacerule=varu.getreplaceruleforall(null);

		String keyword="Varsplit "+dict.getkeyword();
		String description="Varsplit "+dict.getdescription();
		String author=(String) parameters.get(Keywords.client_host.toLowerCase());

		Vector<Hashtable<String, Integer>> varsplit=new Vector<Hashtable<String, Integer>>();
		Vector<Hashtable<Integer, String>> varrif=new Vector<Hashtable<Integer, String>>();
		for (int i=0; i<var.length; i++)
		{
			Hashtable<String, Integer> temp=new Hashtable<String, Integer>();
			Hashtable<Integer, String> tempr=new Hashtable<Integer, String>();
			varsplit.add(temp);
			varrif.add(tempr);
		}

		int[] checkvar=new int[var.length];
		for (int i=0; i<var.length; i++)
		{
			checkvar[i]=0;
		}

		DataReader data = new DataReader(dict);
		if (!data.open(var, replacerule, false))
			return new Result(data.getmessage(), false, null);
		String where=(String)parameters.get(Keywords.where.toLowerCase());
		if (where!=null)
		{
			if (!data.setcondition(where)) return new Result(data.getmessage(), false, null);
		}
		String[] values=null;
		while (!data.isLast())
		{
			values = data.getRecord();
			if (values!=null)
			{
				for (int i=0; i<var.length; i++)
				{
					Hashtable<String, Integer> temp=varsplit.get(i);
					Hashtable<Integer, String> tempr=varrif.get(i);
					if (!values[i].equals(""))
					{
						checkvar[i] +=1;
						if (temp.get(values[i])==null)
						{
							int csize=temp.size()+1;
							temp.put(values[i], new Integer(csize));
							tempr.put(new Integer(csize), values[i]);
						}
					}
				}
			}
		}
		data.close();
		boolean checkvalues=true;
		for (int i=0; i<var.length; i++)
		{
			if (checkvar[i]==0)
				checkvalues=false;
		}
		if (!checkvalues)
			return new Result("%1004%<br>\n", false, null);


		DataSetUtilities dsu=new DataSetUtilities();
		dsu.setreplace(replace);
		Hashtable<String, String> temph=new Hashtable<String, String>();
		dsu.defineolddict(dict);

		Hashtable<String, String> newcl=new Hashtable<String, String>();
		newcl.put("0", "%1002%");
		newcl.put("1", "%990%");

		int totalnewvar=0;
		int[][] riflenvar=new int[2][var.length];
		for (int i=0; i<var.length; i++)
		{
			riflenvar[0][i]=totalnewvar;
			Hashtable<Integer, String> tempr=varrif.get(i);
			Hashtable<String, Integer> temp=varsplit.get(i);
			for (int j=0; j<temp.size(); j++)
			{
				String par = tempr.get(new Integer(j+1));
				temp.put(par, new Integer(totalnewvar));
				String varname=var[i]+"_"+String.valueOf(j+1);
				String varlabel=dict.getvarlabelfromname(var[i])+": "+par;
				Hashtable<String, String> cl=dict.getcodelabelfromname(var[i]);
				if (cl.size()>0)
				{
					String currentcl=cl.get(par);
					if (currentcl!=null)
						varlabel=dict.getvarlabelfromname(var[i])+": "+currentcl;
				}
				dsu.addnewvartoolddict(varname, varlabel, Keywords.NUMSuffix, newcl, temph);
				totalnewvar++;
			}
			riflenvar[1][i]=totalnewvar;
		}

		if (!data.open(totalvar, allreplacerule, false))
			return new Result(data.getmessage(), false, null);

		if (!dw.opendatatable(dsu.getfinalvarinfo()))
			return new Result(dw.getmessage(), false, null);

		int[] allvarstype=varu.getnormalruleforall();

		ValuesParser vp=new ValuesParser(allvarstype, null, null, null, null, null);

		String[] newvalues=new String[totalnewvar];
		String[] varvalues=null;
		String[] wvalues=null;
		while (!data.isLast())
		{
			for (int i=0; i<totalnewvar; i++)
			{
				newvalues[i]="";
			}
			values = data.getRecord();
			varvalues=vp.getanalysisvar(values);
			for (int i=0; i<varvalues.length; i++)
			{
				if (!varvalues[i].equals(""))
				{
					for (int j=riflenvar[0][i]; j<riflenvar[1][i]; j++)
					{
						newvalues[j]="0";
					}
					Hashtable<String, Integer> tempr=varsplit.get(i);
					if (tempr.get(varvalues[i])!=null)
					{
						int position=(tempr.get(varvalues[i])).intValue();
						newvalues[position]="1";
					}
				}
			}
			wvalues=dsu.getnewvalues(values, newvalues);
			dw.write(wvalues);
		}
		data.close();

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
		parameters.add(new GetRequiredParameters(Keywords.OUT.toLowerCase()+"=", "setting=out", true, 440, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.viewout, "checkbox", false, 1612, dep, "", 1));
		dep = new String[1];
		dep[0]=Keywords.dict;
		parameters.add(new GetRequiredParameters(Keywords.var, "vars=all", true, 1005, dep, "", 2));
		dep = new String[0];
		parameters.add(new GetRequiredParameters(Keywords.where,"text", false, 2805,dep,"",2));
		parameters.add(new GetRequiredParameters("", "note", false, 2806, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.replace, "listsingle=530_NULL,531_"+Keywords.replaceall+",532_"+Keywords.replaceformat+",533_"+Keywords.replacemissing,false, 1006, dep, "", 2));
		parameters.add(new GetRequiredParameters("", "note", false, 1007, dep, "", 2));
		return parameters;
	}
	/**
	*Returns the group and the name of the procedure
	*/
	public String[] getstepinfo()
	{
		String[] retprocinfo=new String[2];
		retprocinfo[0]="4163";
		retprocinfo[1]="1003";
		return retprocinfo;
	}
}
