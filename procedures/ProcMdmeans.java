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

import ADaMSoft.dataaccess.DataReader;
import ADaMSoft.dataaccess.DataWriter;
import ADaMSoft.dataaccess.DictionaryReader;
import ADaMSoft.keywords.Keywords;
import ADaMSoft.utilities.GetRequiredParameters;
import ADaMSoft.utilities.ValuesParser;
import ADaMSoft.utilities.VariableUtilities;
import ADaMSoft.utilities.DataSetUtilities;
import ADaMSoft.utilities.ObjectTransformer;
import ADaMSoft.utilities.SortRequestedVar;
import ADaMSoft.algorithms.MeanEvaluator;
import ADaMSoft.utilities.StepUtilities;

/**
* This is the procedure that replace a missing data value with the mean taken from the other valid values
* @author marco.scarno@gmail.com
* @date 14/02/2017
*/
public class ProcMdmeans extends ObjectTransformer implements RunStep
{
	/**
	* Starts the execution of Proc Mdmeans
	*/
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		boolean novgconvert=false;
		String [] requiredparameters=new String[] {Keywords.OUT.toLowerCase(), Keywords.dict, Keywords.var};
		String [] optionalparameters=new String[] {Keywords.vargroup, Keywords.weight, Keywords.where, Keywords.replace, Keywords.novgconvert};
		Keywords.percentage_total=0;
		Keywords.percentage_done=0;
		StepUtilities steputilities=new StepUtilities();
		if (!steputilities.checkParameters(requiredparameters, optionalparameters, parameters))
		{
			return new Result(steputilities.getMessage(), false, null);
		}
		novgconvert =(parameters.get(Keywords.novgconvert)!=null);

		DataWriter dw=new DataWriter(parameters, Keywords.OUT.toLowerCase());
		if (!dw.getmessage().equals(""))
			return new Result(dw.getmessage(), false, null);

		DictionaryReader dict = (DictionaryReader)parameters.get(Keywords.dict);

		String vartemp=(String)parameters.get(Keywords.var.toLowerCase());
		String vargroup=(String)parameters.get(Keywords.vargroup.toLowerCase());
		String weight=(String)parameters.get(Keywords.weight.toLowerCase());

		VariableUtilities varu=new VariableUtilities(dict, vargroup, vartemp, weight, null, null);
		if (varu.geterror())
			return new Result(varu.getmessage(), false, null);

		String[] var=varu.getanalysisvar();
		String[] totalvar=varu.getallvar();

		var=SortRequestedVar.getreqsorted(var, totalvar);

		String replace=(String)parameters.get(Keywords.replace);

		int[] replacerule=varu.getreplaceruleforall(replace);

		String keyword="Mdmeans "+dict.getkeyword();
		String description="Mdmeans "+dict.getdescription();

		String author=(String) parameters.get(Keywords.client_host.toLowerCase());

		DataReader data = new DataReader(dict);

		if (!data.open(totalvar, replacerule, false))
			return new Result(data.getmessage(), false, null);

		int[] allvarstype=varu.getnormalruleforall();

		ValuesParser vp=new ValuesParser(allvarstype, null, null, null, null, null);

		MeanEvaluator emean=new MeanEvaluator();

		int validgroup=0;
		String[] values=null;
		Vector<String> vargroupvalues=new Vector<String>();
		while (!data.isLast())
		{
			values = data.getRecord();
			if (novgconvert)
				vargroupvalues=vp.getorigvargroup(values);
			else
				vargroupvalues=vp.getvargroup(values);
			double[] varvalues=vp.getanalysisvarasdouble(values);
			double weightvalue=vp.getweight(values);
			if (vp.vargroupisnotmissing(vargroupvalues))
			{
				validgroup++;
				emean.setValue(vargroupvalues, varvalues, weightvalue);
			}
		}
		data.close();
		if (validgroup==0)
			return new Result("%666%<br>\n", false, null);

		emean.calculate();

		Hashtable<Vector<String>, double[]> mean=emean.getresult();

		DataSetUtilities dsu=new DataSetUtilities();
		dsu.setreplace(replace);

		dsu.defineolddict(dict);

		if (replace!=null)
		{
			if (replace.equalsIgnoreCase(Keywords.replaceall))
			{
				dsu.setempycodelabels();
				dsu.setempymissingdata();
			}
			else if (replace.equalsIgnoreCase(Keywords.replaceformat))
				dsu.setempycodelabels();
			else if (replace.equalsIgnoreCase(Keywords.replacemissing))
				dsu.setempymissingdata();
		}

		if (!dw.opendatatable(dsu.getfinalvarinfo()))
			return new Result(dw.getmessage(), false, null);

		data = new DataReader(dict);

		if (!data.open(totalvar, replacerule, false))
			return new Result(data.getmessage(), false, null);
		String where=(String)parameters.get(Keywords.where.toLowerCase());
		if (where!=null)
		{
			if (!data.setcondition(where)) return new Result(data.getmessage(), false, null);
		}

		double[] varvalues=null;
		vargroupvalues.clear();

		while (!data.isLast())
		{
			values = data.getRecord();
			if (values!=null)
			{
				if (novgconvert)
					vargroupvalues=vp.getorigvargroup(values);
				else
					vargroupvalues=vp.getvargroup(values);
				varvalues=vp.getanalysisvarasdouble(values);
				for (int i=0; i<varvalues.length; i++)
				{
					if (Double.isNaN(varvalues[i]))
					{
						for (int j=0; j<totalvar.length; j++)
						{
							if (totalvar[j].equalsIgnoreCase(var[i]))
							{
								double[] tempvalsub=mean.get(vargroupvalues);
								values[j]=double2String(tempvalsub[i]);
							}
						}
					}
				}
				dw.write(values);
			}
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
		parameters.add(new GetRequiredParameters(Keywords.var, "vars=all", true, 643, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.vargroup, "vars=all", false, 655, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.weight, "vars=all", false, 656, dep, "", 2));
		dep = new String[0];
		parameters.add(new GetRequiredParameters(Keywords.where,"text", false, 2805,dep,"",2));
		parameters.add(new GetRequiredParameters("", "note", false, 2806, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.novgconvert, "checkbox", false, 2227, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.replace, "listsingle=530_NULL,531_"+Keywords.replaceall+",532_"+Keywords.replaceformat+",533_"+Keywords.replacemissing,false, 534, dep, "", 2));

		return parameters;
	}
	/**
	*Returns the group and the name of the procedure
	*/
	public String[] getstepinfo()
	{
		String[] retprocinfo=new String[2];
		retprocinfo[0]="1228";
		retprocinfo[1]="1234";
		return retprocinfo;
	}
}
