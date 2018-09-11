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

import ADaMSoft.algorithms.MeanEvaluator;
import ADaMSoft.algorithms.STDEvaluator;
import ADaMSoft.algorithms.VarGroupModalities;
import ADaMSoft.dataaccess.DataReader;
import ADaMSoft.dataaccess.DataWriter;
import ADaMSoft.dataaccess.DictionaryReader;
import ADaMSoft.keywords.Keywords;
import ADaMSoft.utilities.DataSetUtilities;
import ADaMSoft.utilities.GetRequiredParameters;
import ADaMSoft.utilities.ValuesParser;
import ADaMSoft.utilities.VariableUtilities;
import ADaMSoft.utilities.ObjectTransformer;
import ADaMSoft.utilities.StepUtilities;

/**
* This is the procedure that evaluate the Cronbach alpha on a series of items
* @author marco.scarno@gmail.com
* @date 13/02/2017
*/
public class ProcCronbachalpha extends ObjectTransformer implements RunStep
{
	/**
	* Starts the execution of Cronbachalpha
	*/
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		String [] requiredparameters=new String[] {Keywords.OUT.toLowerCase(), Keywords.dict, Keywords.var};
		String [] optionalparameters=new String[] {Keywords.where, Keywords.standardize, Keywords.vargroup, Keywords.itemweight, Keywords.replace};
		Keywords.percentage_total=0;
		Keywords.percentage_done=0;
		StepUtilities steputilities=new StepUtilities();
		if (!steputilities.checkParameters(requiredparameters, optionalparameters, parameters))
		{
			return new Result(steputilities.getMessage(), false, null);
		}
		Vector<StepResult> result = new Vector<StepResult>();

		boolean standardize =(parameters.get(Keywords.standardize)!=null);

		String replace =(String)parameters.get(Keywords.replace);
		DataWriter dw=new DataWriter(parameters, Keywords.OUT.toLowerCase());
		if (!dw.getmessage().equals(""))
			return new Result(dw.getmessage(), false, null);

		String tempvar=(String)parameters.get(Keywords.var);
		String tempitemweight=(String)parameters.get(Keywords.itemweight);
		String[] itemweight=null;
		double[] weights=null;
		String[] vars=tempvar.split(" ");
		if (vars.length<3)
			return new Result("%3107%<br>\n", false, null);
		double tempd=0;
		double sumwgt=0;
		double numitems=vars.length;
		if (tempitemweight!=null)
		{
			tempitemweight=tempitemweight.trim().replaceAll("\\s+", " ");
			itemweight=tempitemweight.split(" ");
			if (vars.length!=itemweight.length)
			{
				return new Result("%3105%<br>\n", false, null);
			}
			weights=new double[itemweight.length];
			for (int i=0; i<itemweight.length; i++)
			{
				tempd=Double.NaN;
				try
				{
					tempd=Double.parseDouble(itemweight[i]);
					if (tempd<=0) tempd=Double.NaN;
				}
				catch (Exception e) {}
				if (!Double.isNaN(tempd))
				{
					weights[i]=tempd;
				}
				else
				{
					return new Result("%3106% ("+itemweight[i]+")<br>\n", false, null);
				}
				sumwgt=sumwgt+tempd;
			}
			for (int i=0; i<itemweight.length; i++)
			{
				weights[i]=weights[i]/sumwgt;
			}
		}
		else
		{
			weights=new double[vars.length];
			for (int i=0; i<weights.length; i++)
			{
				weights[i]=1.0;
				sumwgt=sumwgt+weights[i];
			}
		}
		DictionaryReader dict = (DictionaryReader)parameters.get(Keywords.dict);
		String tempvargroup=(String)parameters.get(Keywords.vargroup.toLowerCase());

		VariableUtilities varu=new VariableUtilities(dict, tempvargroup, tempvar, null, null, null);
		if (varu.geterror())
			return new Result(varu.getmessage(), false, null);

		String[] var=varu.getanalysisvar();
		String[] varg=varu.getgroupvar();
		String[] reqvar=varu.getreqvar();

		int[] replacerule=varu.getreplaceruleforsel(replace);

		DataReader data = new DataReader(dict);

		if (!data.open(reqvar, replacerule, false))
			return new Result(data.getmessage(), false, null);
		String where=(String)parameters.get(Keywords.where.toLowerCase());
		if (where!=null)
		{
			if (!data.setcondition(where)) return new Result(data.getmessage(), false, null);
		}

		int[] utilvarstype=varu.getnormalruleforsel();

		ValuesParser vp=new ValuesParser(utilvarstype, null, null, null, null, null);

		int validgroup=0;
		Vector<String> vargroupvalues=new Vector<String>();
		String[] values=null;
		VarGroupModalities vgm=new VarGroupModalities();
		MeanEvaluator emean=new MeanEvaluator();
		STDEvaluator estd=new STDEvaluator(true);
		Hashtable<Vector<String>, double[]> mean=null;
		Hashtable<Vector<String>, double[]> std=null;
		int totalgroupmodalities=1;
		while (!data.isLast())
		{
			values = data.getRecord();
			if (values!=null)
			{
				vargroupvalues=vp.getvargroup(values);
				double[] varvalues=vp.getanalysisvarasdouble(values);
				if (vp.vargroupisnotmissing(vargroupvalues))
				{
					validgroup++;
					vgm.updateModalities(vargroupvalues);
					estd.setValue(vargroupvalues, varvalues, 1);
					emean.setValue(vargroupvalues, varvalues, 1);
				}
			}
		}
		data.close();
		if ((validgroup==0) && (where!=null))
			return new Result("%2804%<br>\n", false, null);
		if ((validgroup==0) && (where==null))
			return new Result("%666%<br>\n", false, null);
		if (!data.open(reqvar, replacerule, false))
			return new Result(data.getmessage(), false, null);
		emean.calculate();
		estd.calculate();
		vgm.calculate();
		totalgroupmodalities=vgm.getTotal();
		if (totalgroupmodalities==0)
			totalgroupmodalities=1;
		mean=emean.getresult();
		std=estd.getresult();

		STDEvaluator alphastd=new STDEvaluator(false);
		double[] tm=null;
		double[] ts=null;
		double[] finalvarvalues=null;
		double sum=0;
		while (!data.isLast())
		{
			values = data.getRecord();
			if (values!=null)
			{
				vargroupvalues=vp.getvargroup(values);
				double[] varvalues=vp.getanalysisvarasdouble(values);
				if (standardize)
				{
					tm=mean.get(vargroupvalues);
					ts=std.get(vargroupvalues);
					for (int i=0; i<varvalues.length; i++)
					{
						if (ts[i]!=0.0)
							varvalues[i]=(varvalues[i]-tm[i])/ts[i];
						else
							varvalues[i]=Double.NaN;
					}
				}
				finalvarvalues=new double[2*varvalues.length+1];
				sum=0;
				for (int i=0; i<varvalues.length; i++)
				{
					finalvarvalues[i]=varvalues[i];
					if (weights!=null)
					{
						if (!Double.isNaN(finalvarvalues[i])) sum=sum+finalvarvalues[i]*weights[i];
					}
				}
				finalvarvalues[varvalues.length]=sum;
				for (int i=0; i<varvalues.length; i++)
				{
					sum=0;
					for (int j=0; j<varvalues.length; j++)
					{
						if (i!=j && !Double.isNaN(varvalues[j])) sum=sum+varvalues[j]*weights[j];
					}
					finalvarvalues[varvalues.length+1+i]=sum;
				}
				if (vp.vargroupisnotmissing(vargroupvalues))
				{
					alphastd.setValue(vargroupvalues, finalvarvalues, 1);
				}
			}
		}
		data.close();
		alphastd.calculate();
		Hashtable<Vector<String>, double[]> finalstd=alphastd.getresult();

		Hashtable<String, String> tempmd=new Hashtable<String, String>();

		Vector<Hashtable<String, String>> groupcodelabels=vgm.getgroupcodelabels();

		DataSetUtilities dsu=new DataSetUtilities();
		dsu.setreplace(replace);
		for (int j=0; j<varg.length; j++)
		{
			dsu.addnewvarfromolddict(dict, varg[j], groupcodelabels.get(j), tempmd, "g_"+varg[j]);
		}
		Hashtable<String, String> clvar=new Hashtable<String, String>();
		clvar.put("gca", "%3108%");
		for (int j=0; j<var.length; j++)
		{
			clvar.put("gca_"+var[j], "%3109% "+dict.getvarlabelfromname(var[j]));
		}

		dsu.addnewvar("ref", "%3110%", Keywords.TEXTSuffix, clvar, tempmd);
		dsu.addnewvar("cronbach_alpha", "%3111%", Keywords.NUMSuffix, tempmd, tempmd);
		dsu.addnewvar("sem", "%3118%", Keywords.NUMSuffix, tempmd, tempmd);

		if (!dw.opendatatable(dsu.getfinalvarinfo()))
			return new Result(dw.getmessage(), false, null);

		String[] valuestowrite=null;
		double tempindex=0;
		double tempsecpart=0;
		double calpha=0;
		double sem=0;
		for (int i=0; i<totalgroupmodalities; i++)
		{
			valuestowrite=new String[varg.length+3];
			Vector<String> rifmodgroup=vgm.getvectormodalities(i);
			for (int j=0; j<rifmodgroup.size(); j++)
			{
				String groupvalue=rifmodgroup.get(j);
				if (groupvalue!=null)
				{
					valuestowrite[j]=groupvalue;
				}
			}
			double[] tempeval=finalstd.get(rifmodgroup);
			valuestowrite[varg.length]="gca";
			tempsecpart=0;
			for (int j=0; j<vars.length; j++)
			{
				tempsecpart=tempsecpart+Math.pow(tempeval[j],2)*weights[j];
			}
			tempsecpart=tempsecpart/(Math.pow(tempeval[vars.length],2));
			tempindex=numitems/(numitems-1.0)*(1-tempsecpart);
			calpha=tempindex;
			sem=tempeval[vars.length]*Math.sqrt(1-tempindex);
			if (!Double.isNaN(calpha)) valuestowrite[varg.length+1]=String.valueOf(calpha);
			else valuestowrite[varg.length+1]="";
			if (!Double.isNaN(sem)) valuestowrite[varg.length+2]=String.valueOf(sem);
			else valuestowrite[varg.length+2]="";
			dw.write(valuestowrite);
			for (int j=0; j<vars.length; j++)
			{
				tempsecpart=0;
				valuestowrite[varg.length]="gca_"+vars[j].toLowerCase();
				for (int k=0; k<vars.length; k++)
				{
					if (k!=j) tempsecpart=tempsecpart+Math.pow(tempeval[k],2)*weights[k];
				}
				tempsecpart=tempsecpart/(Math.pow(tempeval[vars.length+j+1],2));
				tempindex=(numitems-1.0)/(numitems-2.0)*(1-tempsecpart);
				calpha=tempindex;
				sem=tempeval[vars.length+j+1]*Math.sqrt(1-tempindex);
				if (!Double.isNaN(calpha)) valuestowrite[varg.length+1]=String.valueOf(calpha);
				else valuestowrite[varg.length+1]="";
				if (!Double.isNaN(sem)) valuestowrite[varg.length+2]=String.valueOf(sem);
				else valuestowrite[varg.length+2]="";
				dw.write(valuestowrite);
			}
		}

		String keyword="Cronbach alpha "+dict.getkeyword();
		String description="Cronbach alpha "+dict.getdescription();
		String author=(String) parameters.get(Keywords.client_host.toLowerCase());

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
		parameters.add(new GetRequiredParameters(Keywords.var, "vars=all", true, 3114, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.vargroup, "vars=all", false, 655, dep, "", 2));
		dep = new String[0];
		parameters.add(new GetRequiredParameters(Keywords.where,"text", false, 2805,dep,"",2));
		parameters.add(new GetRequiredParameters("", "note", false, 2806, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.standardize, "checkbox", false, 3115, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.itemweight,"text", false, 3116,dep,"",2));
		parameters.add(new GetRequiredParameters("", "note", false, 3117, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.replace, "listsingle=530_NULL,531_"+Keywords.replaceall+",532_"+Keywords.replaceformat+",533_"+Keywords.replacemissing,false, 534, dep, "", 2));
		return parameters;
	}
	/**
	*Returns the group and the name of the procedure
	*/
	public String[] getstepinfo()
	{
		String[] retprocinfo=new String[2];
		retprocinfo[0]="3112";
		retprocinfo[1]="3113";
		return retprocinfo;
	}
}
