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

import ADaMSoft.algorithms.MeanEvaluator;
import ADaMSoft.algorithms.PCAEvaluator;
import ADaMSoft.algorithms.STDEvaluator;
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
* This is the procedure that evaluate the principal component analysis for several quantitative variables
* @author marco.scarno@gmail.com
* @date 14/02/2017
*/
public class ProcPca extends ObjectTransformer implements RunStep
{
	/**
	* Starts the execution of Proc PCA
	*/
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		Vector<StepResult> result = new Vector<StepResult>();
		boolean novgconvert=false;
		boolean noclforvg=false;
		boolean orderclbycode=false;
		boolean pairwise=false;
		boolean usecov=false;
		boolean outinfo=false;
		boolean samplevariance;
		String [] requiredparameters=new String[] {Keywords.OUTC.toLowerCase(), Keywords.dict, Keywords.var};
		String [] optionalparameters=new String[] {Keywords.OUTS.toLowerCase(), Keywords.where, Keywords.vargroup, Keywords.weight, Keywords.pairwise, Keywords.samplevariance, Keywords.replace, Keywords.usecov, Keywords.novgconvert, Keywords.noclforvg, Keywords.orderclbycode};
		Keywords.percentage_total=0;
		Keywords.percentage_done=0;
		StepUtilities steputilities=new StepUtilities();
		if (!steputilities.checkParameters(requiredparameters, optionalparameters, parameters))
		{
			return new Result(steputilities.getMessage(), false, null);
		}
		pairwise =(parameters.get(Keywords.pairwise)!=null);
		samplevariance =(parameters.get(Keywords.samplevariance)!=null);
		usecov =(parameters.get(Keywords.usecov)!=null);
		String replace =(String)parameters.get(Keywords.replace);
		DataWriter dw=new DataWriter(parameters, Keywords.OUTC.toLowerCase());
		if (!dw.getmessage().equals(""))
			return new Result(dw.getmessage(), false, null);
		DataWriter dwinfo=null;
		if (parameters.get(Keywords.OUTS.toLowerCase())!=null)
		{
			outinfo=true;
			dwinfo=new DataWriter(parameters, Keywords.OUTS.toLowerCase());
			if (!dwinfo.getmessage().equals(""))
				return new Result(dwinfo.getmessage(), false, null);
		}

		novgconvert =(parameters.get(Keywords.novgconvert)!=null);
		orderclbycode =(parameters.get(Keywords.orderclbycode)!=null);
		noclforvg =(parameters.get(Keywords.noclforvg)!=null);

		DictionaryReader dict = (DictionaryReader)parameters.get(Keywords.dict);
		String tempvar=(String)parameters.get(Keywords.var.toLowerCase());
		String tempvargroup=(String)parameters.get(Keywords.vargroup.toLowerCase());
		String weight=(String)parameters.get(Keywords.weight.toLowerCase());

		VariableUtilities varu=new VariableUtilities(dict, tempvargroup, tempvar, weight, null, null);
		if (varu.geterror())
			return new Result(varu.getmessage(), false, null);

		String keyword="PCA "+dict.getkeyword();
		String description="PCA "+dict.getdescription();
		String author=(String) parameters.get(Keywords.client_host.toLowerCase());

		String[] var=varu.getanalysisvar();
		String[] varg=varu.getgroupvar();
		String[] reqvar=varu.getreqvar();

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

		VarGroupModalities vgm=new VarGroupModalities();
		MeanEvaluator emean=new MeanEvaluator();
		STDEvaluator estd=new STDEvaluator(samplevariance);

		if (varg.length>0)
		{
			vgm.setvarnames(varg);
			vgm.setdictionary(dict);
			if (orderclbycode)
				vgm.setorderbycode();
			if (novgconvert)
				vgm.noconversion();
		}

		int validgroup=0;
		Vector<String> vargroupvalues=new Vector<String>();
		while (!data.isLast())
		{
			String[] values = data.getRecord();
			if (values!=null)
			{
				if (novgconvert)
					vargroupvalues=vp.getorigvargroup(values);
				else
					vargroupvalues=vp.getvargroup(values);
				double[] varvalues=vp.getanalysisvarasdouble(values);
				double weightvalue=vp.getweight(values);
				if ((vp.vargroupisnotmissing(vargroupvalues)) && (!Double.isNaN(weightvalue)))
				{
					validgroup++;
					vgm.updateModalities(vargroupvalues);
					estd.setValue(vargroupvalues, varvalues, weightvalue);
					emean.setValue(vargroupvalues, varvalues, weightvalue);
				}
			}
		}
		data.close();
		if ((validgroup==0) && (where!=null))
			return new Result("%2804%<br>\n", false, null);
		if ((validgroup==0) && (where==null))
			return new Result("%666%<br>\n", false, null);

		emean.calculate();
		estd.calculate();
		vgm.calculate();

		Hashtable<Vector<String>, double[]> mean=emean.getresult();
		Hashtable<Vector<String>, double[]> std=estd.getresult();

		int totalgroupmodalities=vgm.getTotal();
		if (totalgroupmodalities==0)
			totalgroupmodalities=1;

		if (outinfo)
		{
			String ikeyword="Standardization info "+dict.getkeyword();
			String idescription="Standardization info "+dict.getdescription();

			Vector<Hashtable<String, String>> igroupcodelabels=vgm.getgroupcodelabels();

			DataSetUtilities idsu=new DataSetUtilities();
			idsu.setreplace(replace);

			Hashtable<String, String> tempmd=new Hashtable<String, String>();

			for (int j=0; j<varg.length; j++)
			{
				if (!noclforvg)
					idsu.addnewvarfromolddict(dict, varg[j], igroupcodelabels.get(j), tempmd, "g_"+varg[j]);
				else
					idsu.addnewvarfromolddict(dict, varg[j], tempmd, tempmd, "g_"+varg[j]);
			}

			Hashtable<String, String> icodelabelstat=new Hashtable<String, String>();
			icodelabelstat.put("1", "%966%");
			icodelabelstat.put("2", "%967%");

			idsu.addnewvar("Statistic", "%965%", Keywords.TEXTSuffix, icodelabelstat, tempmd);

			for (int j=0; j<var.length; j++)
			{
				idsu.addnewvarfromolddict(dict, var[j].trim(), tempmd, tempmd, "v_"+var[j]);
			}
			if (!dwinfo.opendatatable(idsu.getfinalvarinfo()))
				return new Result(dwinfo.getmessage(), false, null);

			String[] valuestowrite=new String[varg.length+1+var.length];
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
				valuestowrite[varg.length]=String.valueOf(1);
				double[] tempmean=mean.get(rifmodgroup);
				for (int j=0; j<var.length; j++)
				{
					valuestowrite[varg.length+1+j]="";
					try
					{
						double tempval=tempmean[j];
						valuestowrite[varg.length+1+j]=double2String(tempval);
					}
					catch (Exception e) {}
				}
				dwinfo.write(valuestowrite);
				valuestowrite[varg.length]=String.valueOf(2);
				double[] tempssq=std.get(rifmodgroup);
				for (int j=0; j<var.length; j++)
				{
					valuestowrite[varg.length+1+j]="";
					try
					{
						double tempval=tempssq[j];
						valuestowrite[varg.length+1+j]=double2String(tempval);
					}
					catch (Exception e) {}
				}
				dwinfo.write(valuestowrite);
			}
			boolean resclose=dwinfo.close();
			if (!resclose)
				return new Result(dwinfo.getmessage(), false, null);
			Vector<Hashtable<String, String>> tablevariableinfo=dwinfo.getVarInfo();
			Hashtable<String, String> datatableinfo=dwinfo.getTableInfo();
			result.add(new LocalDictionaryWriter(dwinfo.getdictpath(), ikeyword, idescription, author, dwinfo.gettabletype(),
			datatableinfo, idsu.getfinalvarinfo(), tablevariableinfo, idsu.getfinalcl(), idsu.getfinalmd(), null));
		}

		DataReader datapca = new DataReader(dict);

		if (!datapca.open(reqvar, replacerule, false))
			return new Result(datapca.getmessage(), false, null);

		VarGroupModalities vgmpca=new VarGroupModalities();
		PCAEvaluator epca=new PCAEvaluator(pairwise, samplevariance);
		while (!datapca.isLast())
		{
			String[] values = datapca.getRecord();
			if (novgconvert)
				vargroupvalues=vp.getorigvargroup(values);
			else
				vargroupvalues=vp.getvargroup(values);
			double[] varvalues=vp.getanalysisvarasdouble(values);
			double weightvalue=vp.getweight(values);
			if ((vp.vargroupisnotmissing(vargroupvalues)) && (!Double.isNaN(weightvalue)))
			{
				double[] tempmean=mean.get(vargroupvalues);
				double[] tempstd=std.get(vargroupvalues);
				if (!usecov)
				{
					for (int i=0; i<varvalues.length; i++)
					{
						varvalues[i]=(varvalues[i]-tempmean[i])/tempstd[i];
					}
				}
				else
				{
					for (int i=0; i<varvalues.length; i++)
					{
						varvalues[i]=varvalues[i]-tempmean[i];
					}
				}
				vgmpca.updateModalities(vargroupvalues);
				epca.setValue(vargroupvalues, varvalues, weightvalue);
			}
		}
		datapca.close();

		epca.calculate();

		String errormsg=epca.geterror();
		if (!errormsg.equals(""))
			return new Result(errormsg, false, null);

		Hashtable<Vector<String>, double[][]> eigenvec=epca.geteigenvectors();
		Hashtable<Vector<String>, double[]> eigenval=epca.geteigenvalues();

		totalgroupmodalities=vgmpca.getTotal();
		if (totalgroupmodalities==0)
			totalgroupmodalities=1;

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
		clvar.put("1", "%968%");
		clvar.put("2", "%969%");
		for (int j=0; j<var.length; j++)
		{
			clvar.put(var[j], "%970% "+dict.getvarlabelfromname(var[j]));
		}

		dsu.addnewvar("Type", "%971%", Keywords.TEXTSuffix, clvar, tempmd);

		for (int i=0; i<var.length; i++)
		{
			dsu.addnewvar("c_"+String.valueOf(i+1), "%972% "+String.valueOf(i+1), Keywords.NUMSuffix, tempmd, tempmd);
		}

		if (!dw.opendatatable(dsu.getfinalvarinfo()))
			return new Result(dw.getmessage(), false, null);

		for (int i=0; i<totalgroupmodalities; i++)
		{
			String[] valuestowrite=new String[varg.length+1+var.length];
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
			double inertia=0;
			double[] tempeval=eigenval.get(rifmodgroup);
			valuestowrite[varg.length]=String.valueOf(1);
			for (int j=0; j<tempeval.length; j++)
			{
				valuestowrite[varg.length+1+j]=double2String(tempeval[j]);
				if (!Double.isNaN(tempeval[j]))
					inertia=inertia+tempeval[j];
			}
			dw.write(valuestowrite);
			valuestowrite[varg.length]=String.valueOf(2);
			for (int j=0; j<tempeval.length; j++)
			{
				valuestowrite[varg.length+1+j]=double2String(tempeval[j]/inertia);
			}
			dw.write(valuestowrite);
			double[][] tempevec=eigenvec.get(rifmodgroup);
			for (int j=0; j<var.length; j++)
			{
				valuestowrite[varg.length]=var[j];
				for (int k=0; k<tempevec[0].length; k++)
				{
					valuestowrite[varg.length+1+k]=double2String(tempevec[j][k]);
				}
				dw.write(valuestowrite);
			}
		}
		Hashtable<String, String> pcainfo=new Hashtable<String, String>();
		if (!usecov)
			pcainfo.put("matrix","corr");
		else
			pcainfo.put("matrix","cov");
		boolean resclose=dw.close();
		if (!resclose)
			return new Result(dw.getmessage(), false, null);
		Vector<Hashtable<String, String>> tablevariableinfo=dw.getVarInfo();
		Hashtable<String, String> datatableinfo=dw.getTableInfo();
		result.add(new LocalDictionaryWriter(dw.getdictpath(), keyword, description, author, dw.gettabletype(),
		datatableinfo, dsu.getfinalvarinfo(), tablevariableinfo, dsu.getfinalcl(), dsu.getfinalmd(), pcainfo));
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
		parameters.add(new GetRequiredParameters(Keywords.OUTC.toLowerCase()+"=", "setting=out", true, 747, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.OUTS.toLowerCase()+"=", "setting=out", false, 748, dep, "", 1));
		parameters.add(new GetRequiredParameters("", "note", false, 1630, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.viewout, "checkbox", false, 1612, dep, "", 1));
		dep = new String[1];
		dep[0]=Keywords.dict;
		parameters.add(new GetRequiredParameters(Keywords.var, "vars=all", true, 641, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.vargroup, "vars=all", false, 655, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.weight, "vars=all", false, 656, dep, "", 2));
		dep = new String[0];
		parameters.add(new GetRequiredParameters(Keywords.usecov, "checkbox", false, 745, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.pairwise, "checkbox", false, 736, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.samplevariance, "checkbox", false, 861, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.where,"text", false, 2805,dep,"",2));
		parameters.add(new GetRequiredParameters("", "note", false, 2806, dep, "", 2));
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
		retprocinfo[0]="743";
		retprocinfo[1]="744";
		return retprocinfo;
	}
}
