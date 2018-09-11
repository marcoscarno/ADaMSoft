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

import ADaMSoft.utilities.StepUtilities;
import ADaMSoft.utilities.GetRequiredParameters;
import ADaMSoft.utilities.DataSetUtilities;
import ADaMSoft.utilities.ObjectTransformer;
import ADaMSoft.utilities.CheckVarNames;

import ADaMSoft.keywords.Keywords;

import ADaMSoft.algorithms.NLFitting.FunctionEval;
import ADaMSoft.algorithms.NLFitting.NormalFitting;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Vector;


/**
* This is the procedure that evaluate the predicted value using the parameters found by minimizing a function
* @author marco.scarno@gmail.com
* @date 14/02/2017
*/
public class ProcFittingeval extends ObjectTransformer implements RunStep
{
	/**
	* Starts the execution of Proc Fittingeval and returns the corresponding message
	*/
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		String [] requiredparameters=new String[] {Keywords.OUT.toLowerCase(), Keywords.dict, Keywords.dict+"e", Keywords.function, Keywords.parameter};
		String [] optionalparameters=new String[] {Keywords.varoutname, Keywords.depvar, Keywords.replace};
		Keywords.percentage_total=0;
		Keywords.percentage_done=0;
		StepUtilities steputilities=new StepUtilities();
		if (!steputilities.checkParameters(requiredparameters, optionalparameters, parameters))
		{
			return new Result(steputilities.getMessage(), false, null);
		}
		String varoutname=(String)parameters.get(Keywords.varoutname);
		if (varoutname==null)
			varoutname="pred";
		varoutname=varoutname.replaceAll("\\s", "");
		if (!CheckVarNames.getResultCheck(varoutname, (String)parameters.get(Keywords.WorkDir)).equals(""))
			return new Result("%2096%"+" "+varoutname+"<br>\n", false, null);

		String replace =(String)parameters.get(Keywords.replace);

		DataWriter dw=new DataWriter(parameters, Keywords.OUT.toLowerCase());
		if (!dw.getmessage().equals(""))
			return new Result(dw.getmessage(), false, null);

		DictionaryReader dict = (DictionaryReader)parameters.get(Keywords.dict);
		DictionaryReader dicte = (DictionaryReader)parameters.get(Keywords.dict+"e");

		int numvar=dicte.gettotalvar();
		int iscorrect=0;
		for (int i=0; i<numvar; i++)
		{
			String tempname=dicte.getvarname(i);
			if ((tempname.toLowerCase()).startsWith("parameter"))
				iscorrect++;
			if ((tempname.toLowerCase()).startsWith("value"))
				iscorrect++;
		}
		if (iscorrect!=2)
			return new Result("%1687%<br>\n", false, null);

		String[] vartoread=new String[2];
		vartoread[0]="parameter";
		vartoread[1]="value";

		int[] replacerule=new int[2];
		replacerule[0]=0;
		replacerule[1]=0;

		DataReader datae = new DataReader(dicte);
		if (!datae.open(vartoread, replacerule, false))
			return new Result(datae.getmessage(), false, null);

		Vector<String[]> realparameter=new Vector<String[]>();
		boolean errorinpara=false;
		while (!datae.isLast())
		{
			String[] values = datae.getRecord();
			realparameter.add(values);
			try
			{
				Double.parseDouble(values[1]);
			}
			catch (Exception e)
			{
				errorinpara=true;
			}
		}
		datae.close();
		if (errorinpara)
			return new Result("%1688%<br>\n", false, null);

		String par=(String)parameters.get(Keywords.parameter);
		String depvar=(String)parameters.get(Keywords.depvar);

		if (depvar!=null)
		{
			String[] testdepvar=depvar.split(" ");
			if (testdepvar.length>1)
				return new Result("%1125%<br>\n", false, null);
		}

		String[] parameterList=par.split(" ");

		boolean parameterisvar=false;
		for (int i=0; i<dict.gettotalvar(); i++)
		{
			for (int j=0; j<parameterList.length; j++)
			{
				parameterList[j]=parameterList[j].trim();
				if (dict.getvarname(i).equalsIgnoreCase(parameterList[j]))
					parameterisvar=true;
			}
		}
		if (parameterisvar)
			return new Result("%1650%<br>\n", false, null);

		for (int i=0; i<realparameter.size(); i++)
		{
			String[] tp=realparameter.get(i);
			boolean axv=false;
			for (int j=0; j<parameterList.length; j++)
			{
				if (tp[0].equalsIgnoreCase(parameterList[j]))
					axv=true;
			}
			if (!axv)
				return new Result("%1690%<br>\n", false, null);
		}

		String function=(String)parameters.get(Keywords.function);

		String workdir=(String)parameters.get(Keywords.WorkDir);

		if (parameterList.length!=realparameter.size())
			return new Result("%1689%<br>\n", false, null);

		DataSetUtilities dsu=new DataSetUtilities();
		dsu.setreplace(replace);
		Hashtable<String, String> temph=new Hashtable<String, String>();

		dsu.defineolddict(dict);

		dsu.addnewvartoolddict(varoutname, "%1126%", Keywords.NUMSuffix, temph, temph);
		if (depvar!=null)
		{
			dsu.addnewvartoolddict(varoutname+"_res", "%1127%", Keywords.NUMSuffix, temph, temph);
			dsu.addnewvartoolddict(varoutname+"_sqres", "%1128%", Keywords.NUMSuffix, temph, temph);
		}

		if (!dw.opendatatable(dsu.getfinalvarinfo()))
			return new Result(dw.getmessage(), false, null);

		Vector<Hashtable<String, String>> infovar=dict.getfixedvariableinfo();

		NormalFitting nf=new NormalFitting(function, workdir, infovar, realparameter, parameterList, depvar);

		if (nf.geterror())
		{
			String rm=nf.getretmess();
			nf.clearmem();
			nf=null;
			return new Result(rm, false, null);
		}

		if (!nf.compilefunc(true))
		{
			String rm=nf.getretmess();
			nf.clearmem();
			nf=null;
			return new Result(rm, false, null);
		}

		FunctionEval ef=nf.getFE();
		try
		{
			String evalfunc=ef.evaluate(dsu, dict, dw);
			if (!evalfunc.equals(""))
			{
				nf.clearmem();
				nf=null;
				return new Result(evalfunc, false, null);
			}
		}
		catch (Exception e)
		{
			nf.clearmem();
			nf=null;
			return new Result("%1691%<br>\n", false, null);
		}

		nf.clearmem();
		nf=null;

		String keyword="FittingEval "+dict.getkeyword();
		String description="FittingEval "+dict.getdescription();
		String author=(String)parameters.get(Keywords.client_host.toLowerCase());

		Hashtable<String, String> othertableinfo=new Hashtable<String, String>();

		Vector<StepResult> result = new Vector<StepResult>();

		boolean resclose=dw.close();
		if (!resclose)
			return new Result(dw.getmessage(), false, null);
		Vector<Hashtable<String, String>> tablevariableinfo=dw.getVarInfo();
		Hashtable<String, String> datatableinfo=dw.getTableInfo();
		result.add(new LocalDictionaryWriter(dw.getdictpath(), keyword, description, author, dw.gettabletype(),
		datatableinfo, dsu.getfinalvarinfo(), tablevariableinfo, dsu.getfinalcl(), dsu.getfinalmd(), othertableinfo));
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
		parameters.add(new GetRequiredParameters(Keywords.dict+"e=", "dict", true, 1686, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.OUT.toLowerCase()+"=", "setting=out", true, 1640, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.viewout, "checkbox", false, 1612, dep, "", 1));
		dep = new String[1];
		dep[0]=Keywords.dict;
		parameters.add(new GetRequiredParameters(Keywords.depvar, "var=all", false, 889, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.function, "text", true, 1641, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.parameter, "text", true, 1642, dep, "", 2));
		parameters.add(new GetRequiredParameters("", "note", false, 1649, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.varoutname, "text", true, 2005, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.replace, "listsingle=530_NULL,531_"+Keywords.replaceall+",532_"+Keywords.replaceformat+",533_"+Keywords.replacemissing,false, 534, dep, "", 2));
		return parameters;
	}
	/**
	*Returns the group and the name of the procedure
	*/
	public String[] getstepinfo()
	{
		String[] retprocinfo=new String[2];
		retprocinfo[0]="1638";
		retprocinfo[1]="1664";
		return retprocinfo;
	}
}
