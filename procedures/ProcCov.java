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

import ADaMSoft.algorithms.CovariancesEvaluator;
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

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Vector;

/**
* This is the procedure that evaluate the matrix of variances and covariances between row and column variables
* @author marco.scarno@gmai.com
* @version 1.0.0, rev.: 13/02/17 by marco
*/
public class ProcCov extends ObjectTransformer implements RunStep
{
	/**
	* Starts the execution of Proc Cov
	*/
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		Vector<StepResult> result = new Vector<StepResult>();
		boolean novgconvert=false;
		boolean noclforvg=false;
		boolean orderclbycode=false;
		boolean pairwise=false;
		boolean samplevariance=false;
		String [] requiredparameters=new String[] {Keywords.OUT.toLowerCase(), Keywords.dict, Keywords.varrow};
		String [] optionalparameters=new String[] {Keywords.varcol, Keywords.where, Keywords.vargroup, Keywords.weight, Keywords.samplevariance, Keywords.pairwise, Keywords.replace, Keywords.novgconvert, Keywords.noclforvg, Keywords.orderclbycode};
		Keywords.percentage_total=0;
		Keywords.percentage_done=0;
		StepUtilities steputilities=new StepUtilities();
		if (!steputilities.checkParameters(requiredparameters, optionalparameters, parameters))
		{
			return new Result(steputilities.getMessage(), false, null);
		}
		pairwise =(parameters.get(Keywords.pairwise)!=null);
		samplevariance =(parameters.get(Keywords.samplevariance)!=null);
		String replace =(String)parameters.get(Keywords.replace);
		DataWriter dw=new DataWriter(parameters, Keywords.OUT.toLowerCase());
		if (!dw.getmessage().equals(""))
			return new Result(dw.getmessage(), false, null);

		novgconvert =(parameters.get(Keywords.novgconvert)!=null);
		orderclbycode =(parameters.get(Keywords.orderclbycode)!=null);
		noclforvg =(parameters.get(Keywords.noclforvg)!=null);

		DictionaryReader dict = (DictionaryReader)parameters.get(Keywords.dict);
		String tempvarrow=(String)parameters.get(Keywords.varrow.toLowerCase());
		String tempvarcol=(String)parameters.get(Keywords.varcol.toLowerCase());
		if (tempvarcol==null)
			tempvarcol=tempvarrow;
		String tempvargroup=(String)parameters.get(Keywords.vargroup.toLowerCase());
		String weight=(String)parameters.get(Keywords.weight.toLowerCase());

		VariableUtilities varu=new VariableUtilities(dict, tempvargroup, null, weight, tempvarrow, tempvarcol);
		if (varu.geterror())
			return new Result(varu.getmessage(), false, null);

		String keyword="Cov "+dict.getkeyword();
		String description="Cov "+dict.getdescription();
		String author=(String) parameters.get(Keywords.client_host.toLowerCase());

		String[] varrow=varu.getrowvar();
		String[] varcol=varu.getcolvar();
		String[] varg=varu.getgroupvar();

		if ((varg.length==0) && (novgconvert))
		{
			result.add(new LocalMessageGetter("%2228%<br>\n"));
		}
		if ((varg.length==0) && (noclforvg))
		{
			result.add(new LocalMessageGetter("%2230%<br>\n"));
		}
		if ((varg.length==0) && (orderclbycode))
		{
			result.add(new LocalMessageGetter("%2232%<br>\n"));
		}

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

		int[] grouprule=varu.getgroupruleforsel();
		int[] rowrule=varu.getrowruleforsel();
		int[] colrule=varu.getcolruleforsel();
		int[] weightrule=varu.getweightruleforsel();

		ValuesParser vp=new ValuesParser(null, grouprule, null, rowrule, colrule, weightrule);

		VarGroupModalities vgm=new VarGroupModalities();
		if (varg.length>0)
		{
			vgm.setvarnames(varg);
			vgm.setdictionary(dict);
			if (orderclbycode)
				vgm.setorderbycode();
			if (novgconvert)
				vgm.noconversion();
		}
		CovariancesEvaluator ec=new CovariancesEvaluator(pairwise, samplevariance);
		int validgroup=0;
		String[] values=null;
		Vector<String> vargroupvalues=new Vector<String>();
		double[] varrowvalues=null;
		double[] varcolvalues=null;
		double weightvalue=1;
		while (!data.isLast())
		{
			values = data.getRecord();
			if (values!=null)
			{
				if (novgconvert)
					vargroupvalues=vp.getorigvargroup(values);
				else
					vargroupvalues=vp.getvargroup(values);
				varrowvalues=vp.getrowvarasdouble(values);
				varcolvalues=vp.getcolvarasdouble(values);
				weightvalue=vp.getweight(values);
				if ((vp.vargroupisnotmissing(vargroupvalues)) && (!Double.isNaN(weightvalue)))
				{
					validgroup++;
					vgm.updateModalities(vargroupvalues);
					ec.setValue(vargroupvalues, varrowvalues, varcolvalues, weightvalue);
				}
			}
		}
		data.close();
		if ((validgroup==0) && (where!=null))
			return new Result("%2804%<br>\n", false, null);
		if ((validgroup==0) && (where==null))
			return new Result("%666%<br>\n", false, null);
		ec.calculate();
		vgm.calculate();
		Hashtable<Vector<String>, double[][]> corr=ec.getresult();

		int totalgroupmodalities=vgm.getTotal();

		Vector<Hashtable<String, String>> groupcodelabels=vgm.getgroupcodelabels();

		DataSetUtilities dsu=new DataSetUtilities();

		dsu.setreplace(replace);

		Hashtable<String, String> tempmd=new Hashtable<String, String>();

		for (int j=0; j<varg.length; j++)
		{
			if (!noclforvg)
				dsu.addnewvarfromolddict(dict, varg[j], groupcodelabels.get(j), tempmd, "g_"+varg[j]);
			else
				dsu.addnewvarfromolddict(dict, varg[j], tempmd, tempmd, "g_"+varg[j]);
		}

		Hashtable<String, String> clvar=new Hashtable<String, String>();
		for (int j=0; j<varrow.length; j++)
		{
			clvar.put(varrow[j], dict.getvarlabelfromname(varrow[j]));
		}
		dsu.addnewvar("ref", "%1056%", Keywords.TEXTSuffix, clvar, tempmd);

		for (int i=0; i<varcol.length; i++)
		{
			dsu.addnewvar("v_"+varcol[i], dict.getvarlabelfromname(varcol[i]), Keywords.NUMSuffix, tempmd, tempmd);
		}

		if (!dw.opendatatable(dsu.getfinalvarinfo()))
			return new Result(dw.getmessage(), false, null);

		if (totalgroupmodalities==0)
			totalgroupmodalities=1;

		String[] valuestowrite=new String[varg.length+1+varcol.length];
		for (int i=0; i<totalgroupmodalities; i++)
		{
			Vector<String> rifmodgroup=vgm.getvectormodalities(i);
			for (int j=0; j<rifmodgroup.size(); j++)
			{
				String groupvalue=rifmodgroup.get(j);
				if (groupvalue!=null)
				{
					if (!noclforvg)
						valuestowrite[j]=vgm.getcode(j, groupvalue);
					else
						valuestowrite[j]=groupvalue;
				}
			}
			double[][] tempcorr=corr.get(rifmodgroup);
			for (int j=0; j<tempcorr.length; j++)
			{
				valuestowrite[varg.length]=varrow[j];
				for (int k=0; k<tempcorr[0].length; k++)
				{
					valuestowrite[varg.length+1+k]=double2String(tempcorr[j][k]);
				}
				dw.write(valuestowrite);
			}
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
		parameters.add(new GetRequiredParameters(Keywords.varrow, "vars=all", true, 734, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.varcol, "vars=all", false, 735, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.vargroup, "vars=all", false, 655, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.weight, "vars=all", false, 656, dep, "", 2));
		dep = new String[0];
		parameters.add(new GetRequiredParameters(Keywords.where,"text", false, 2805,dep,"",2));
		parameters.add(new GetRequiredParameters("", "note", false, 2806, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.pairwise, "checkbox", false, 736, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.samplevariance, "checkbox", false, 861, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.novgconvert, "checkbox", false, 2227, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.noclforvg, "checkbox", false, 2229, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.orderclbycode, "checkbox", false, 2231, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.replace, "listsingle=530_NULL,531_"+Keywords.replaceall+",532_"+Keywords.replaceformat+",533_"+Keywords.replacemissing,false, 534, dep, "", 2));
		return parameters;
	}
	/**
	*Returns the group and the name of the procedure
	*/
	public String[] getstepinfo()
	{
		String[] retprocinfo=new String[2];
		retprocinfo[0]="671";
		retprocinfo[1]="738";
		return retprocinfo;
	}
}
