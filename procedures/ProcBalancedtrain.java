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
import ADaMSoft.utilities.ValuesParser;
import ADaMSoft.utilities.DataSetUtilities;
import ADaMSoft.utilities.ObjectTransformer;
import ADaMSoft.utilities.StepUtilities;

import ADaMSoft.keywords.Keywords;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Vector;

/**
* This is the procedure that creates a balanced data set
* @author marco.scarno@gmail.com
* @date 13/02/2017
*/
public class ProcBalancedtrain extends ObjectTransformer implements RunStep
{
	/**
	* Starts the execution of Proc Balancedtrain and returns the corresponding message
	*/
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		boolean istest=false;
		String [] requiredparameters=new String[] {Keywords.OUTTRAIN.toLowerCase(), Keywords.dict, Keywords.var, Keywords.cases};
		String [] optionalparameters=new String[] {Keywords.OUTTEST.toLowerCase(), Keywords.replace, Keywords.where};
		Keywords.percentage_total=0;
		Keywords.percentage_done=0;
		StepUtilities steputilities=new StepUtilities();
		if (!steputilities.checkParameters(requiredparameters, optionalparameters, parameters))
		{
			return new Result(steputilities.getMessage(), false, null);
		}
		istest =(parameters.get(Keywords.OUTTEST.toLowerCase())!=null);
		boolean nomd =(parameters.get(Keywords.nomd)!=null);
		DataWriter dw=new DataWriter(parameters, Keywords.OUTTRAIN.toLowerCase());
		if (!dw.getmessage().equals(""))
			return new Result(dw.getmessage(), false, null);

		int ncases=string2int((String)parameters.get(Keywords.cases));
		if (ncases<2)
			return new Result("%924%<br>\n", false, null);

		DataWriter testdw=null;
		if (istest)
		{
			testdw=new DataWriter(parameters, Keywords.OUTTEST.toLowerCase());
			if (!testdw.getmessage().equals(""))
				return new Result(testdw.getmessage(), false, null);
		}

		DictionaryReader dict = (DictionaryReader)parameters.get(Keywords.dict);
		String vartemp=(String)parameters.get(Keywords.var.toLowerCase());
		String[] requiredv=vartemp.split(" ");
		if (requiredv.length!=1)
			return new Result("%923%<br>\n", false, null);

		VariableUtilities varu=new VariableUtilities(dict, null, vartemp, null, null, null);
		if (varu.geterror())
			return new Result(varu.getmessage(), false, null);

		String[] var=varu.getallvar();

		String replace=(String)parameters.get(Keywords.replace);

		String keyword="Balanced train "+dict.getkeyword();
		String description="Balanced train"+dict.getdescription();
		String testkeyword="Balanced test "+dict.getkeyword();
		String testdescription="Balanced test"+dict.getdescription();

		String author=dict.getauthor();

		DataSetUtilities dsu=new DataSetUtilities();
		DataSetUtilities testdsu=new DataSetUtilities();

		dsu.setreplace(replace);
		testdsu.setreplace(replace);

		dsu.defineolddict(dict);

		if (istest)
			testdsu.defineolddict(dict);

		int[] replacerule=varu.getreplaceruleforall(replace);
		if (replace!=null)
		{
			if (replace.equalsIgnoreCase(Keywords.replaceall))
			{
				dsu.setempycodelabels();
				dsu.setempymissingdata();
				if (istest)
				{
					testdsu.setempycodelabels();
					testdsu.setempymissingdata();
				}
			}
			else if (replace.equalsIgnoreCase(Keywords.replaceformat))
			{
				dsu.setempycodelabels();
				if (istest)
					testdsu.setempycodelabels();
			}
			else if (replace.equalsIgnoreCase(Keywords.replacemissing))
			{
				dsu.setempymissingdata();
				if (istest)
					testdsu.setempymissingdata();
			}
		}

		int[] ruleforvar=varu.getnormalruleforall();

		ValuesParser vp=new ValuesParser(ruleforvar, null, null, null, null, null);

		DataReader data = new DataReader(dict);

		if (!data.open(var, replacerule, false))
			return new Result(data.getmessage(), false, null);
		String where=(String)parameters.get(Keywords.where.toLowerCase());
		if (where!=null)
		{
			if (!data.setcondition(where)) return new Result(data.getmessage(), false, null);
		}

		if (!dw.opendatatable(dsu.getfinalvarinfo()))
			return new Result(dw.getmessage(), false, null);

		if (istest)
		{
			if (!testdw.opendatatable(testdsu.getfinalvarinfo()))
				return new Result(testdw.getmessage(), false, null);
		}

		Hashtable<String, Integer> actualfreq=new Hashtable<String, Integer>();

		String[] values=null;
		String[] val=null;
		int pointer=0;
		boolean noadd=false;
		int validgroup=0;
		while (!data.isLast())
		{
			values = data.getRecord();
			if (values!=null)
			{
				val=vp.getanalysisvar(values);
				noadd=false;
				if ((val[0].equals("")) && (nomd))
					noadd=true;
				if (!noadd)
				{
					if (actualfreq.get(val[0])==null)
					{
						validgroup++;
						dw.write(values);
						pointer=1;
						actualfreq.put(val[0], new Integer(pointer));
					}
					else
					{
						pointer=(actualfreq.get(val[0])).intValue();
						if (pointer<ncases)
							dw.write(values);
						else if (istest)
							testdw.write(values);
						pointer++;
						actualfreq.put(val[0], new Integer(pointer));
					}
				}
			}
		}
		data.close();
		if (validgroup!=actualfreq.size())
		{
			dw.deletetmp();
			if (istest) testdw.deletetmp();
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
		if (istest)
		{
			resclose=testdw.close();
			if (!resclose)
				return new Result(testdw.getmessage(), false, null);
			Vector<Hashtable<String, String>> testtablevariableinfo=testdw.getVarInfo();
			Hashtable<String, String> testdatatableinfo=testdw.getTableInfo();
			result.add(new LocalDictionaryWriter(testdw.getdictpath(), testkeyword, testdescription, author, testdw.gettabletype(),
			testdatatableinfo, testdsu.getfinalvarinfo(), testtablevariableinfo, testdsu.getfinalcl(), testdsu.getfinalmd(), null));
		}
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
		parameters.add(new GetRequiredParameters(Keywords.OUTTRAIN.toLowerCase()+"=", "setting=out", true, 925, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.OUTTEST.toLowerCase()+"=", "setting=out", false, 926, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.viewout, "checkbox", false, 1612, dep, "", 1));
		dep = new String[1];
		dep[0]=Keywords.dict;
		parameters.add(new GetRequiredParameters(Keywords.var, "vars=all", true, 927, dep, "", 2));
		dep = new String[0];
		parameters.add(new GetRequiredParameters(Keywords.where,"text", false, 2805,dep,"",2));
		parameters.add(new GetRequiredParameters("", "note", false, 2806, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.cases, "text", true, 928, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.nomd, "checkbox", false, 1553, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.replace, "listsingle=530_NULL,531_"+Keywords.replaceall+",532_"+Keywords.replaceformat+",533_"+Keywords.replacemissing,false, 534, dep, "", 2));
		return parameters;
	}
	/**
	*Returns the group and the name of the procedure
	*/
	public String[] getstepinfo()
	{
		String[] retprocinfo=new String[2];
		retprocinfo[0]="4165";
		retprocinfo[1]="929";
		return retprocinfo;
	}
}
