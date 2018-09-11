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

import ADaMSoft.algorithms.RegEvaluator;
import ADaMSoft.algorithms.VarGroupModalities;
import ADaMSoft.dataaccess.DictionaryReader;
import ADaMSoft.dataaccess.DataReader;
import ADaMSoft.dataaccess.DataWriter;
import ADaMSoft.utilities.StepUtilities;

import ADaMSoft.utilities.GetRequiredParameters;
import ADaMSoft.utilities.ValuesParser;
import ADaMSoft.utilities.VariableUtilities;
import ADaMSoft.utilities.DataSetUtilities;
import ADaMSoft.utilities.ObjectTransformer;

import ADaMSoft.keywords.Keywords;

import java.util.*;

import org.apache.commons.math3.fitting.GaussianCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoints;
import org.apache.commons.math3.analysis.function.Gaussian.*;
import org.apache.commons.math3.analysis.function.*;

/**
* This is the procedure that interpolate with a Gaussian a series of data
* @author marco.scarno@gmail.com
* @date 17/10/2017
*/
public class ProcGaussianinterpolation extends ObjectTransformer implements RunStep
{
	/**
	* Starts the execution of Proc Polynomialinterpolation
	*/
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		Vector<StepResult> result = new Vector<StepResult>();
		String [] requiredparameters=new String[] {Keywords.dictxy, Keywords.dictxint, Keywords.varx, Keywords.varxint, Keywords.vary, Keywords.OUT.toLowerCase()};
		String [] optionalparameters=new String[] {Keywords.OUTEST.toLowerCase(), Keywords.where, Keywords.replace};
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

		boolean isoute =(parameters.get(Keywords.OUTEST.toLowerCase())!=null);
		DataWriter dwe=null;
		if (isoute)
		{
			dwe=new DataWriter(parameters, Keywords.OUTEST.toLowerCase());
			if (!dwe.getmessage().equals(""))
				return new Result(dwe.getmessage(), false, null);
		}

		DictionaryReader dictxy = (DictionaryReader)parameters.get(Keywords.dictxy);
		String tempvarx=(String)parameters.get(Keywords.varx.toLowerCase());
		String tempvary=(String)parameters.get(Keywords.vary.toLowerCase());

		String[] testindepvar=tempvarx.split(" ");
		if (testindepvar.length>1)
			return new Result("%4204%<br>\n", false, null);

		VariableUtilities varu=new VariableUtilities(dictxy, null, null, null, tempvarx, tempvary);
		if (varu.geterror())
			return new Result(varu.getmessage(), false, null);

		String keyword="Gaussianinterpolation "+dictxy.getkeyword();
		String description="Gaussianinterpolation "+dictxy.getdescription();
		String edescription="Gaussianinterpolation: parameters estimation "+dictxy.getdescription();
		String author=(String)parameters.get(Keywords.client_host.toLowerCase());

		String[] varx=varu.getrowvar();
		String[] vary=varu.getcolvar();
		String[] reqvar=varu.getreqvar();
		int[] replacerule=varu.getreplaceruleforsel(replace);

		DataReader data = new DataReader(dictxy);

		if (!data.open(reqvar, replacerule, false))
			return new Result(data.getmessage(), false, null);
		String where=(String)parameters.get(Keywords.where.toLowerCase());
		if (where!=null)
		{
			if (!data.setcondition(where)) return new Result(data.getmessage(), false, null);
		}

		int[] rowrule=varu.getrowruleforsel();
		int[] colrule=varu.getcolruleforsel();
		int[] weightrule=varu.getweightruleforsel();

		ValuesParser vp=new ValuesParser(null, null, null, rowrule, colrule, weightrule);

		int validgroup=0;
		String[] values=null;
		double[] varxvalues=null;
		double[] varyvalues=null;
		double weightvalue=1;
		Vector<WeightedObservedPoints> obs_vect = new Vector<WeightedObservedPoints>();
		for (int i=0; i<vary.length; i++)
		{
			obs_vect.add(new WeightedObservedPoints());
		}
		while (!data.isLast())
		{
			values = data.getRecord();
			if (values!=null)
			{
				varxvalues=vp.getrowvarasdouble(values);
				varyvalues=vp.getcolvarasdouble(values);
				weightvalue=vp.getweight(values);
				if ((!Double.isNaN(varxvalues[0])) && (!Double.isNaN(weightvalue)))
				{
					for (int i=0; i<varyvalues.length; i++)
					{
						if (!Double.isNaN(varyvalues[i]))
						{
							WeightedObservedPoints temp=obs_vect.get(i);
							temp.add(weightvalue, varxvalues[0], varyvalues[i]);
						}
					}
				}
			}
		}
		data.close();
		Vector<double[]> pcv=new Vector<double[]>();
		Vector<String> not_valid_var=new Vector<String>();
		try
		{
			for (int i=0; i<obs_vect.size(); i++)
			{
				double[] temp_coeff = GaussianCurveFitter.create().fit(obs_vect.get(i).toList());
				int diffent=0;
				for (int j=0; j<temp_coeff.length; j++)
				{
					if (Double.isNaN(temp_coeff[j])) diffent++;
				}
				if (diffent!=0) not_valid_var.add(vary[i]);
				pcv.add(temp_coeff);
			}
			obs_vect.clear();
			obs_vect=null;
		}
		catch (Exception es)
		{
			return new Result(es.toString()+"<br>\n", false, null);
		}
		if (not_valid_var.size()>0)
		{
			String message_error="%4206%<br>";
			for (int i=0; i<not_valid_var.size(); i++)
			{
				message_error=message_error+not_valid_var.get(i)+"<br>";
			}
			return new Result(message_error+"\n", false, null);
		}
		tempvarx=(String)parameters.get(Keywords.varxint.toLowerCase());
		testindepvar=tempvarx.split(" ");
		if (testindepvar.length>1)
		{
			return new Result("%4207%<br>\n", false, null);
		}
		DictionaryReader dictxint = (DictionaryReader)parameters.get(Keywords.dictxint);
		int pos_x_var=-1;
		for (int i=0; i<dictxint.gettotalvar() ; i++)
		{
			if (dictxint.getvarname(i).equalsIgnoreCase(tempvarx)) pos_x_var=i;
		}
		if (pos_x_var<0)
		{
			return new Result("%4208% ("+tempvarx.toUpperCase()+")<br>\n", false, null);
		}
		DataSetUtilities dsue=new DataSetUtilities();
		if (isoute)
		{
			dsue.setreplace(replace);
			Hashtable<String, String> tempmde=new Hashtable<String, String>();
			Hashtable<String, String> clvare=new Hashtable<String, String>();
			for (int i=0; i<vary.length; i++)
			{
				clvare.put(vary[i], dictxy.getvarlabelfromname(vary[i]));
			}
			dsue.addnewvar("dep_var", "%4209%", Keywords.TEXTSuffix, clvare, tempmde);
			Hashtable<String, String> tempmd=new Hashtable<String, String>();
			Hashtable<String, String> clvar=new Hashtable<String, String>();
			dsue.addnewvar("coeff_norm", "%4221% ", Keywords.NUMSuffix, clvar, tempmd);
			dsue.addnewvar("coeff_mean", "%4222% ", Keywords.NUMSuffix, clvar, tempmd);
			dsue.addnewvar("coeff_std", "%4223% ", Keywords.NUMSuffix, clvar, tempmd);
			if (!dwe.opendatatable(dsue.getfinalvarinfo()))
				return new Result(dwe.getmessage(), false, null);
			String[] values_est=new String[4];
			for (int i=0; i<vary.length; i++)
			{
				values_est[0]=vary[i];
				double[] temp_coeff=pcv.get(i);
				for (int j=0; j<temp_coeff.length; j++)
				{
					values_est[1+j]=String.valueOf(temp_coeff[j]);
				}
				dwe.write(values_est);
			}
		}
		Hashtable<String, String> tempmd=new Hashtable<String, String>();
		Hashtable<String, String> clvar=new Hashtable<String, String>();
		DataSetUtilities dsu=new DataSetUtilities();
		dsu.setreplace(replace);
		dsu.addnewvar("var_"+tempvarx, dictxint.getvarlabelfromname(tempvarx), Keywords.NUMSuffix, clvar, tempmd);
		for (int i=0; i<vary.length; i++)
		{
			dsu.addnewvar("var_"+vary[i], dictxy.getvarlabelfromname(vary[i]), Keywords.NUMSuffix, clvar, tempmd);
		}
		if (!dw.opendatatable(dsu.getfinalvarinfo()))
			return new Result(dw.getmessage(), false, null);
		String[] values_pred=new String[vary.length+1];
		String xs="";
		double xd=0;
		double currval=0;
		DataReader dataxint = new DataReader(dictxint);
		if (!dataxint.open(null, 0, false))
			return new Result(data.getmessage(), false, null);
		while (!dataxint.isLast())
		{
			values = dataxint.getRecord();
			if (values!=null)
			{
				xs=values[pos_x_var];
				try
				{
					xd=Double.parseDouble(xs);
					if (!Double.isNaN(xd))
					{
						values_pred[0]=xs;
						for (int i=0; i<vary.length; i++)
						{
							double[] temp_coeff=pcv.get(i);
							try
							{
								Gaussian.Parametric gp=new Gaussian.Parametric();
								currval=gp.value(xd, temp_coeff);
								values_pred[i+1]=String.valueOf(currval);
							}
							catch (Exception egp)
							{
								values_pred[i+1]="";
							}
						}
						dw.write(values_pred);
					}
				}
				catch (Exception exs){}
			}
		}
		dataxint.close();
		boolean resclose=dw.close();
		if (!resclose)
			return new Result(dw.getmessage(), false, null);
		if (isoute)
		{
			resclose=dwe.close();
			if (!resclose)
				return new Result(dwe.getmessage(), false, null);
			Vector<Hashtable<String, String>> ctablevariableinfo=dwe.getVarInfo();
			Hashtable<String, String> cdatatableinfo=dwe.getTableInfo();
			result.add(new LocalDictionaryWriter(dwe.getdictpath(), keyword, edescription, author, dwe.gettabletype(),
			cdatatableinfo, dsue.getfinalvarinfo(), ctablevariableinfo, dsue.getfinalcl(), dsue.getfinalmd(), null));
		}

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
		String[] depa ={""};
		String[] depb ={""};
		parameters.add(new GetRequiredParameters(Keywords.dictxy+"=", "dict", true, 4213, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.dictxint+"=", "dict", true, 4214, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.OUT.toLowerCase()+"=", "setting=out", true, 4215, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.OUTEST.toLowerCase()+"=", "setting=out", false, 4216, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.viewout, "checkbox", false, 1612, dep, "", 1));
		depa = new String[1];
		depa[0]=Keywords.dictxy;
		depb = new String[1];
		depb[0]=Keywords.dictxint;
		parameters.add(new GetRequiredParameters(Keywords.varx, "var=all", true, 4217, depa, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.vary, "vars=all", true, 4218, depa, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.varxint, "var=all", true, 4219, depb, "", 2));
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
		retprocinfo[0]="4211";
		retprocinfo[1]="4224";
		return retprocinfo;
	}
}
