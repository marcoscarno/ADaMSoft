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
import ADaMSoft.algorithms.CanonicalEvaluator;
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
* This is the procedure that applies the canonical analysis to two groups of quantitative variables
* @author marco.scarno@gmail.com
* @version 1.0.0, rev.: 13/02/17 by marco
*/
public class ProcCanonical extends ObjectTransformer implements RunStep
{
	/**
	* Starts the execution of Proc Canonical
	*/
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		Vector<StepResult> result = new Vector<StepResult>();
		boolean novgconvert=false;
		boolean noclforvg=false;
		boolean orderclbycode=false;
		boolean usecov=false;
		boolean outinfo=false;
		boolean samplevariance;
		String [] requiredparameters=new String[] {Keywords.OUTC.toLowerCase(), Keywords.dict, Keywords.varx, Keywords.vary};
		String [] optionalparameters=new String[] {Keywords.OUTS.toLowerCase(), Keywords.where, Keywords.vargroup, Keywords.weight, Keywords.samplevariance, Keywords.replace, Keywords.usecov, Keywords.novgconvert, Keywords.noclforvg, Keywords.orderclbycode};
		Keywords.percentage_total=0;
		Keywords.percentage_done=0;
		StepUtilities steputilities=new StepUtilities();
		if (!steputilities.checkParameters(requiredparameters, optionalparameters, parameters))
		{
			return new Result(steputilities.getMessage(), false, null);
		}
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
		String tempvarx=(String)parameters.get(Keywords.varx.toLowerCase());
		String tempvary=(String)parameters.get(Keywords.vary.toLowerCase());
		String tempvargroup=(String)parameters.get(Keywords.vargroup.toLowerCase());
		String weight=(String)parameters.get(Keywords.weight.toLowerCase());

		VariableUtilities varu=new VariableUtilities(dict, tempvargroup, null, weight, tempvarx, tempvary);
		if (varu.geterror())
			return new Result(varu.getmessage(), false, null);

		String keyword="CANONICAL "+dict.getkeyword();
		String description="CANONICAL "+dict.getdescription();
		String author=(String) parameters.get(Keywords.client_host.toLowerCase());

		String[] varx=varu.getrowvar();
		String[] vary=varu.getcolvar();
		String[] varg=varu.getgroupvar();
		String[] reqvar=varu.getreqvar();

		if (varx.length<2)
			return new Result("%1876% ("+tempvarx+")<br>\n", false, null);

		if (vary.length<2)
			return new Result("%1876% ("+tempvary+")<br>\n", false, null);

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

		int[] grouprule=varu.getgroupruleforsel();
		int[] rowrule=varu.getrowruleforsel();
		int[] colrule=varu.getcolruleforsel();
		int[] weightrule=varu.getweightruleforsel();

		int[] replacerule=varu.getreplaceruleforsel(replace);

		DataReader data = new DataReader(dict);

		if (!data.open(reqvar, replacerule, false))
			return new Result(data.getmessage(), false, null);
		String where=(String)parameters.get(Keywords.where.toLowerCase());
		if (where!=null)
		{
			if (!data.setcondition(where)) return new Result(data.getmessage(), false, null);
		}

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

		MeanEvaluator emeanx=new MeanEvaluator();
		MeanEvaluator emeany=new MeanEvaluator();
		STDEvaluator estdx=new STDEvaluator(samplevariance);
		STDEvaluator estdy=new STDEvaluator(samplevariance);

		double[] varrowvalues=null;
		double[] varcolvalues=null;

		boolean notmissing=true;

		Vector<String> vargroupvalues=null;

		int validgroup=0;
		while (!data.isLast())
		{
			String[] values = data.getRecord();
			if (values!=null)
			{
				if (novgconvert)
					vargroupvalues=vp.getorigvargroup(values);
				else
					vargroupvalues=vp.getvargroup(values);

				varrowvalues=vp.getrowvarasdouble(values);
				varcolvalues=vp.getcolvarasdouble(values);

				double weightvalue=vp.getweight(values);
				if ((vp.vargroupisnotmissing(vargroupvalues)) && (!Double.isNaN(weightvalue)))
				{
					notmissing=true;
					for (int i=0; i<varrowvalues.length; i++)
					{
						if (Double.isNaN(varrowvalues[i]))
							notmissing=false;
					}
					for (int i=0; i<varcolvalues.length; i++)
					{
						if (Double.isNaN(varcolvalues[i]))
							notmissing=false;
					}
					if (notmissing)
					{
						validgroup++;
						vgm.updateModalities(vargroupvalues);
						estdx.setValue(vargroupvalues, varrowvalues, weightvalue);
						estdy.setValue(vargroupvalues, varcolvalues, weightvalue);
						emeanx.setValue(vargroupvalues, varrowvalues, weightvalue);
						emeany.setValue(vargroupvalues, varcolvalues, weightvalue);
					}
				}
			}
		}
		data.close();
		if ((validgroup==0) && (where!=null))
			return new Result("%2804%<br>\n", false, null);
		if ((validgroup==0) && (where==null))
			return new Result("%666%<br>\n", false, null);

		emeanx.calculate();
		emeany.calculate();
		estdx.calculate();
		estdy.calculate();
		vgm.calculate();

		Hashtable<Vector<String>, double[]> meanx=emeanx.getresult();
		Hashtable<Vector<String>, double[]> stdx=estdx.getresult();
		Hashtable<Vector<String>, double[]> meany=emeany.getresult();
		Hashtable<Vector<String>, double[]> stdy=estdy.getresult();

		int totalgroupmodalities=vgm.getTotal();

		if (outinfo)
		{
			String ikeyword="Standardization info "+dict.getkeyword();
			String idescription="Standardization info "+dict.getdescription();

			vgm.getTotal();
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

			for (int j=0; j<varx.length; j++)
			{
				idsu.addnewvarfromolddict(dict, varx[j].trim(), tempmd, tempmd, "vx_"+varx[j]);
			}
			for (int j=0; j<vary.length; j++)
			{
				idsu.addnewvarfromolddict(dict, vary[j].trim(), tempmd, tempmd, "vy_"+vary[j]);
			}
			if (!dwinfo.opendatatable(idsu.getfinalvarinfo()))
				return new Result(dwinfo.getmessage(), false, null);
			if (totalgroupmodalities==0)
				totalgroupmodalities=1;

			String[] valuestowrite=new String[varg.length+1+varx.length+vary.length];
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
				double[] tempmean=meanx.get(rifmodgroup);
				for (int j=0; j<varx.length; j++)
				{
					valuestowrite[varg.length+1+j]="";
					try
					{
						double tempval=tempmean[j];
						valuestowrite[varg.length+1+j]=double2String(tempval);
					}
					catch (Exception e) {}
				}
				tempmean=meany.get(rifmodgroup);
				for (int j=0; j<vary.length; j++)
				{
					valuestowrite[varg.length+1+varx.length+j]="";
					try
					{
						double tempval=tempmean[j];
						valuestowrite[varg.length+1+varx.length+j]=double2String(tempval);
					}
					catch (Exception e) {}
				}
				dwinfo.write(valuestowrite);
				valuestowrite[varg.length]=String.valueOf(2);
				double[] tempssq=stdx.get(rifmodgroup);
				for (int j=0; j<varx.length; j++)
				{
					valuestowrite[varg.length+1+j]="";
					try
					{
						double tempval=tempssq[j];
						valuestowrite[varg.length+1+j]=double2String(tempval);
					}
					catch (Exception e) {}
				}
				tempssq=stdy.get(rifmodgroup);
				for (int j=0; j<vary.length; j++)
				{
					valuestowrite[varg.length+1+varx.length+j]="";
					try
					{
						double tempval=tempssq[j];
						valuestowrite[varg.length+1+varx.length+j]=double2String(tempval);
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

		if (!data.open(reqvar, replacerule, false))
			return new Result(data.getmessage(), false, null);

		CanonicalEvaluator ecan=new CanonicalEvaluator(samplevariance, varx.length, vary.length);
		while (!data.isLast())
		{
			String[] values = data.getRecord();
			if (novgconvert)
				vargroupvalues=vp.getorigvargroup(values);
			else
				vargroupvalues=vp.getvargroup(values);

			varrowvalues=vp.getrowvarasdouble(values);
			varcolvalues=vp.getcolvarasdouble(values);

			double weightvalue=vp.getweight(values);
			if ((vp.vargroupisnotmissing(vargroupvalues)) && (!Double.isNaN(weightvalue)))
			{
				notmissing=true;
				for (int i=0; i<varrowvalues.length; i++)
				{
					if (Double.isNaN(varrowvalues[i]))
						notmissing=false;
				}
				for (int i=0; i<varcolvalues.length; i++)
				{
					if (Double.isNaN(varcolvalues[i]))
						notmissing=false;
				}
				if (notmissing)
				{
					double[] tempmeanx=meanx.get(vargroupvalues);
					double[] tempstdx=stdx.get(vargroupvalues);
					double[] tempmeany=meany.get(vargroupvalues);
					double[] tempstdy=stdy.get(vargroupvalues);
					if (!usecov)
					{
						for (int i=0; i<varrowvalues.length; i++)
						{
							varrowvalues[i]=(varrowvalues[i]-tempmeanx[i])/tempstdx[i];
						}
						for (int i=0; i<varcolvalues.length; i++)
						{
							varcolvalues[i]=(varcolvalues[i]-tempmeany[i])/tempstdy[i];
						}
					}
					else
					{
						for (int i=0; i<varrowvalues.length; i++)
						{
								varrowvalues[i]=varrowvalues[i]-tempmeanx[i];
						}
						for (int i=0; i<varcolvalues.length; i++)
						{
								varcolvalues[i]=varcolvalues[i]-tempmeany[i];
						}
					}
					ecan.setValue(vargroupvalues, varrowvalues, varcolvalues, weightvalue);
				}
			}
		}
		data.close();

		ecan.calculate();

		String errormsg=ecan.geterror();
		if (!errormsg.equals(""))
			return new Result(errormsg, false, null);

		Hashtable<Vector<String>, double[][]> eigena=ecan.geta();
		Hashtable<Vector<String>, double[][]> eigenb=ecan.getb();
		Hashtable<Vector<String>, double[]> eigenval=ecan.geteigenvalues();

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
		for (int j=0; j<varx.length; j++)
		{
			clvar.put(varx[j], "%970% "+dict.getvarlabelfromname(varx[j]));
		}
		for (int j=0; j<vary.length; j++)
		{
			clvar.put(vary[j], "%970% "+dict.getvarlabelfromname(vary[j]));
		}

		dsu.addnewvar("Type", "%971%", Keywords.TEXTSuffix, clvar, tempmd);

		int minrif=varx.length;
		if (vary.length<minrif)
			minrif=vary.length;

		for (int i=0; i<minrif; i++)
		{
			dsu.addnewvar("c_"+String.valueOf(i+1), "%972% "+String.valueOf(i+1), Keywords.TEXTSuffix, tempmd, tempmd);
		}

		if (!dw.opendatatable(dsu.getfinalvarinfo()))
			return new Result(dw.getmessage(), false, null);

		if (totalgroupmodalities==0)
			totalgroupmodalities=1;

		for (int i=0; i<totalgroupmodalities; i++)
		{
			String[] valuestowrite=new String[varg.length+1+minrif];
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
			double[][] tempevec=eigena.get(rifmodgroup);
			for (int j=0; j<varx.length; j++)
			{
				valuestowrite[varg.length]=varx[j];
				for (int k=0; k<tempevec[0].length; k++)
				{
					valuestowrite[varg.length+1+k]=double2String(tempevec[j][k]);
				}
				dw.write(valuestowrite);
			}
			tempevec=eigenb.get(rifmodgroup);
			for (int j=0; j<vary.length; j++)
			{
				valuestowrite[varg.length]=vary[j];
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
		parameters.add(new GetRequiredParameters(Keywords.varx, "vars=all", true, 1879, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.vary, "vars=all", true, 1880, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.vargroup, "vars=all", false, 655, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.weight, "vars=all", false, 656, dep, "", 2));
		dep = new String[0];
		parameters.add(new GetRequiredParameters(Keywords.where,"text", false, 2805,dep,"",2));
		parameters.add(new GetRequiredParameters("", "note", false, 2806, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.usecov, "checkbox", false, 745, dep, "", 2));
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
		retprocinfo[0]="1877";
		retprocinfo[1]="1878";
		return retprocinfo;
	}
}
