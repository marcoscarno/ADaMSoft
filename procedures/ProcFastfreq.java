/**
* Copyright (c) MS
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
import java.util.Enumeration;

import ADaMSoft.dataaccess.DataReader;
import ADaMSoft.dataaccess.DataWriter;
import ADaMSoft.dataaccess.DictionaryReader;
import ADaMSoft.keywords.Keywords;
import ADaMSoft.utilities.DataSetUtilities;
import ADaMSoft.utilities.GetRequiredParameters;
import ADaMSoft.utilities.ObjectTransformer;
import ADaMSoft.utilities.StepUtilities;

/**
* This is the procedure that evaluate in a fast and easy way the frequencies of rows variables
* @author marco.scarno@gmail.com
* @date 14/02/2017
*/
public class ProcFastfreq extends ObjectTransformer implements RunStep
{
	/**
	* Starts the execution of Proc Fast freq
	*/
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		Vector<StepResult> result = new Vector<StepResult>();
		String [] requiredparameters=new String[] {Keywords.OUT.toLowerCase(), Keywords.dict, Keywords.var};
		String [] optionalparameters=new String[] {Keywords.weight, Keywords.where, Keywords.replace, Keywords.mdsubst};
		Keywords.percentage_total=0;
		Keywords.percentage_done=0;
		StepUtilities steputilities=new StepUtilities();
		if (!steputilities.checkParameters(requiredparameters, optionalparameters, parameters))
		{
			return new Result(steputilities.getMessage(), false, null);
		}
		String replace =(String)parameters.get(Keywords.replace);
		DataWriter dw=new DataWriter(parameters, Keywords.OUT.toLowerCase());
		if (!dw.getmessage().equals(""))
			return new Result(dw.getmessage(), false, null);

		String mdsubst=(String)parameters.get(Keywords.mdsubst);

		DictionaryReader dict = (DictionaryReader)parameters.get(Keywords.dict);
		String tempvar=(String)parameters.get(Keywords.var.toLowerCase());
		String weight=(String)parameters.get(Keywords.weight.toLowerCase());

		tempvar=tempvar.trim();
		if (weight!=null)
		{
			tempvar=tempvar+" "+weight;
		}

		String[] var=tempvar.split(" ");
		int nvar=var.length;

		int[] replacerule=new int[var.length];
		int rifrep=0;
		if (replace==null)
			rifrep=0;
		else if (replace.equalsIgnoreCase(Keywords.replaceall))
			rifrep=1;
		else if (replace.equalsIgnoreCase(Keywords.replaceformat))
			rifrep=2;
		else if (replace.equalsIgnoreCase(Keywords.replacemissing))
			rifrep=3;
		for (int i=0; i<var.length; i++)
		{
			replacerule[i]=rifrep;
		}
		if (weight!=null)
		{
			replacerule[var.length-1]=1;
			nvar=nvar-1;
		}

		String keyword="FastFreq "+dict.getkeyword();
		String description="FastFreq "+dict.getdescription();
		String author=(String) parameters.get(Keywords.client_host.toLowerCase());

		DataReader data = new DataReader(dict);

		if (!data.open(var, replacerule, false))
			return new Result(data.getmessage(), false, null);
		String where=(String)parameters.get(Keywords.where.toLowerCase());
		if (where!=null)
		{
			if (!data.setcondition(where)) return new Result(data.getmessage(), false, null);
		}

		String[] values = null;
		Hashtable<Vector<String>, Double> frequencies=new Hashtable<Vector<String>, Double>();
		boolean treatobs=true;
		double wei=1;
		double validgroup=0;
		while (!data.isLast())
		{
			values = data.getRecord();
			if (values!=null)
			{
				Vector<String> temp=new Vector<String>();
				treatobs=true;
				for (int i=0; i<nvar; i++)
				{
					if (values[i].trim().equals(""))
					{
						if (mdsubst!=null)
							values[i]=mdsubst;
						else
							treatobs=false;
					}
					temp.add(values[i]);
				}
				if (weight!=null)
				{
					wei=Double.parseDouble(values[var.length-1]);
					if (Double.isNaN(wei))
						treatobs=false;
				}
				if (treatobs)
				{
					validgroup=validgroup+wei;
					if (frequencies.get(temp)==null)
					{
						frequencies.put(temp, new Double(wei));
					}
					else
					{
						double nwei=frequencies.get(temp);
						frequencies.put(temp, new Double(wei+nwei));
					}
				}
			}
		}
		data.close();
		if ((validgroup==0) && (where!=null))
			return new Result("%2804%<br>\n", false, null);
		if ((validgroup==0) && (where==null))
			return new Result("%666%<br>\n", false, null);

		DataSetUtilities dsu=new DataSetUtilities();
		Hashtable<String,String> tempcl=new Hashtable<String,String>();
		for (int i=0; i<nvar; i++)
		{
			dsu.addnewvar(var[i], dict.getvarlabelfromname(var[i]), Keywords.TEXTSuffix, tempcl, tempcl);
		}

		dsu.addnewvar("count", "%2342%", Keywords.NUMSuffix, tempcl, tempcl);
		dsu.addnewvar("rel_count", "%2343%", Keywords.NUMSuffix, tempcl, tempcl);

		if (!dw.opendatatable(dsu.getfinalvarinfo()))
			return new Result(dw.getmessage(), false, null);

		String[] valuestowrite=new String[nvar+2];
		for (Enumeration<Vector<String>> en=frequencies.keys(); en.hasMoreElements();)
		{
			Vector<String> temp=en.nextElement();
			double freq=frequencies.get(temp);
			for (int i=0; i<temp.size(); i++)
			{
				valuestowrite[i]=temp.get(i);
			}
			valuestowrite[nvar]=double2String(freq);
			valuestowrite[nvar+1]=double2String(freq/validgroup);
			dw.write(valuestowrite);
		}
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
		parameters.add(new GetRequiredParameters(Keywords.dict+"=", "dict", true, 721, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.OUT.toLowerCase()+"=", "setting=out", true, 440, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.viewout, "checkbox", false, 1612, dep, "", 1));
		dep = new String[1];
		dep[0]=Keywords.dict;
		parameters.add(new GetRequiredParameters(Keywords.var, "vars=all", true, 2344, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.weight, "vars=all", false, 656, dep, "", 2));
		dep = new String[0];
		parameters.add(new GetRequiredParameters(Keywords.where,"text", false, 2805,dep,"",2));
		parameters.add(new GetRequiredParameters("", "note", false, 2806, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.mdsubst, "text", false, 2345, dep, "", 2));
		parameters.add(new GetRequiredParameters("", "note", false, 2346, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.replace, "listsingle=530_NULL,531_"+Keywords.replaceall+",532_"+Keywords.replaceformat+",533_"+Keywords.replacemissing,false, 534, dep, "", 2));
		return parameters;
	}
	/**
	*Returns the group and the name of the procedure
	*/
	public String[] getstepinfo()
	{
		String[] retprocinfo=new String[2];
		retprocinfo[0]="759";
		retprocinfo[1]="2341";
		return retprocinfo;
	}
}
