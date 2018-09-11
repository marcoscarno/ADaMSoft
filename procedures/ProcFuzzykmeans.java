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
import ADaMSoft.algorithms.FuzzyKMeansEvaluator;
import ADaMSoft.algorithms.VarGroupModalities;
import ADaMSoft.dataaccess.DictionaryReader;
import ADaMSoft.dataaccess.DataReader;
import ADaMSoft.dataaccess.DataWriter;
import ADaMSoft.dataaccess.GroupedMatrix2Dfile;
import ADaMSoft.utilities.StepUtilities;

import ADaMSoft.utilities.GetRequiredParameters;
import ADaMSoft.utilities.VariableUtilities;
import ADaMSoft.utilities.DataSetUtilities;
import ADaMSoft.utilities.ValuesParser;
import ADaMSoft.utilities.ObjectTransformer;

import ADaMSoft.keywords.Keywords;


import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Vector;

/**
* This is the procedure that assign a record to a cluster according to the fuzzy kmeans alghoritm
* @author marco.scarno@gmail.com
* @date 14/02/2017
*/
public class ProcFuzzykmeans extends ObjectTransformer implements RunStep
{
	/**
	* Starts the execution of Proc Kmeans and returns the corresponding message
	*/
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		Vector<StepResult> result = new Vector<StepResult>();
		boolean novgconvert=false;
		boolean noclforvg=false;
		boolean orderclbycode=false;

		String [] requiredparameters=new String[] {Keywords.OUT.toLowerCase(), Keywords.dict, Keywords.var, Keywords.iterations, Keywords.ngroup, Keywords.fuzzycoeff};
		String [] optionalparameters=new String[] {Keywords.vargroup, Keywords.where, Keywords.replace, Keywords.accuracy, Keywords.Distance, Keywords.novgconvert, Keywords.noclforvg, Keywords.orderclbycode};
		Keywords.percentage_total=0;
		Keywords.percentage_done=0;
		StepUtilities steputilities=new StepUtilities();
		if (!steputilities.checkParameters(requiredparameters, optionalparameters, parameters))
		{
			return new Result(steputilities.getMessage(), false, null);
		}
		String ngroup=(String)parameters.get(Keywords.ngroup.toLowerCase());
		String fuzzycoeff=(String)parameters.get(Keywords.fuzzycoeff.toLowerCase());
		String accuracy=(String)parameters.get(Keywords.accuracy.toLowerCase());
		String iter=(String)parameters.get(Keywords.iterations.toLowerCase());

		novgconvert =(parameters.get(Keywords.novgconvert)!=null);
		orderclbycode =(parameters.get(Keywords.orderclbycode)!=null);
		noclforvg =(parameters.get(Keywords.noclforvg)!=null);

		String distanceType = (String)parameters.get(Keywords.Distance.toLowerCase());
		if (distanceType==null)
			distanceType=Keywords.EuclideanDistance;
		String[] dtype=new String[] {Keywords.EuclideanDistance, Keywords.SquaredEuclideanDistance, Keywords.ManhattanDistance, Keywords.ChebyshevDistance, Keywords.MahalanobisDistance};
		int vdt=steputilities.CheckOption(dtype, distanceType);
		if (vdt==0)
			return new Result("%1775% "+Keywords.Distance.toUpperCase()+"<br>\n"+steputilities.getMessage(), false, null);

		int nseeds=string2int(ngroup);
		if (nseeds==0)
			return new Result("%875%<br>\n", false, null);

		double fcoeff=string2double(fuzzycoeff);
		if (fcoeff<1)
			return new Result("%932%<br>\n", false, null);

		double ac=0.0001;
		if (accuracy!=null)
		{
			ac=string2double(accuracy);
			if (ac==0)
				return new Result("%941%<br>\n", false, null);
		}

		int niter=string2int(iter);
		if (niter==0)
			return new Result("%875%<br>\n", false, null);

		DataWriter dw=new DataWriter(parameters, Keywords.OUT.toLowerCase());
		if (!dw.getmessage().equals(""))
			return new Result(dw.getmessage(), false, null);

		DictionaryReader dict = (DictionaryReader)parameters.get(Keywords.dict);

		String vartemp=(String)parameters.get(Keywords.var.toLowerCase());
		String vargroup=(String)parameters.get(Keywords.vargroup.toLowerCase());

		VariableUtilities varu=new VariableUtilities(dict, vargroup, vartemp, null, null, null);
		if (varu.geterror())
			return new Result(varu.getmessage(), false, null);

		String[] var=varu.getanalysisvar();
		String[] varg=varu.getgroupvar();
		String[] totalvar=varu.getreqvar();

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

		String replace=(String)parameters.get(Keywords.replace);
		int[] replacerule=varu.getreplaceruleforsel(replace);

		String keyword="FuzzyKMeans "+dict.getkeyword();
		String description="FuzzyKMeans "+dict.getdescription();

		String author=(String) parameters.get(Keywords.client_host.toLowerCase());

		DataReader data =null;

		int[] utilvarstype=varu.getnormalruleforsel();

		ValuesParser vp=new ValuesParser(utilvarstype, null, null, null, null, null);

		String tempdir=(String)parameters.get(Keywords.WorkDir);

		FuzzyKMeansEvaluator efuzzykmeans=new FuzzyKMeansEvaluator(nseeds, fcoeff, ac, tempdir, var.length, vdt);
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

		GroupedMatrix2Dfile filedata=new GroupedMatrix2Dfile(tempdir, var.length);

		CovariancesEvaluator cove=new CovariancesEvaluator(true, true);

		Vector<String> vargroupvalues=new Vector<String>();
		data = new DataReader(dict);
		String[] values;
		String where=(String)parameters.get(Keywords.where.toLowerCase());
		int validgroup=0;
		if (vdt==5)
		{
			boolean ismd=false;
			if (!data.open(totalvar, replacerule, false))
				return new Result(data.getmessage(), false, null);
			if (where!=null)
			{
				if (!data.setcondition(where)) return new Result(data.getmessage(), false, null);
			}
			double[] varvalues=null;
			while (!data.isLast())
			{
				values = data.getRecord();
				if (values!=null)
				{
					vargroupvalues=vp.getvargroup(values);
					if (novgconvert)
						vargroupvalues=vp.getorigvargroup(values);
					else
						vargroupvalues=vp.getvargroup(values);
					varvalues=vp.getanalysisvarasdouble(values);
					ismd=false;
					for (int j=0; j<varvalues.length; j++)
					{
						if (Double.isNaN(varvalues[j]))
							ismd=true;
					}
					if ((vp.vargroupisnotmissing(vargroupvalues)) && (!ismd))
					{
						validgroup++;
						cove.setValue(vargroupvalues, varvalues, varvalues, 1);
					}
				}
			}
			data.close();
			if ((validgroup==0) && (where!=null))
				return new Result("%2804%<br>\n", false, null);
			if ((validgroup==0) && (where==null))
				return new Result("%666%<br>\n", false, null);
			cove.calculate();
			efuzzykmeans.setmahalanobis(cove.getresult());
		}

		if (!data.open(totalvar, replacerule, false))
			return new Result(data.getmessage(), false, null);
		if (where!=null)
		{
			if (!data.setcondition(where)) return new Result(data.getmessage(), false, null);
		}
		double[] varvalues=null;
		boolean ismd=false;
		validgroup=0;
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
				ismd=false;
				for (int i=0; i<varvalues.length; i++)
				{
					if (Double.isNaN(varvalues[i]))
						ismd=true;
				}
				if ((vp.vargroupisnotmissing(vargroupvalues)) && (!ismd))
				{
					validgroup++;
					if (!filedata.write(vargroupvalues, varvalues))
					{
						data.close();
						return new Result(filedata.getMessage(), false, null);
					}
					vgm.updateModalities(vargroupvalues);
				}
			}
		}
		data.close();
		if ((validgroup==0) && (where!=null))
			return new Result("%2804%<br>\n", false, null);
		if ((validgroup==0) && (where==null))
			return new Result("%666%<br>\n", false, null);

		vgm.calculate();

		efuzzykmeans.initialise(vgm);

		if (!efuzzykmeans.evaluate(niter, filedata))
		{
			efuzzykmeans.endfuzzykmeans();
			return new Result(efuzzykmeans.getmessage(), false, null);
		}

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
		for (int j=0; j<nseeds; j++)
		{
			clvar.put(String.valueOf(j), "%1132%: "+String.valueOf(j+1));
		}
		dsu.addnewvar("group", "%1132%", Keywords.TEXTSuffix, clvar, tempmd);
		for (int i=0; i<var.length; i++)
		{
			dsu.addnewvar("v_"+var[i], "%1135%: "+dict.getvarlabelfromname(var[i]), Keywords.NUMSuffix, tempmd, tempmd);
		}

		if (!dw.opendatatable(dsu.getfinalvarinfo()))
			return new Result(dw.getmessage(), false, null);

		if (totalgroupmodalities==0)
			totalgroupmodalities=1;

		Hashtable<Vector<String>, String> lasterror=efuzzykmeans.getlasterror();
		Hashtable<Vector<String>, String> erroronevaluate=efuzzykmeans.geterroronevaluate();

		String[] valuestowrite=new String[varg.length+1+var.length];
		String actualgroupref="";
		for (int i=0; i<totalgroupmodalities; i++)
		{
			actualgroupref="";
			Vector<String> rifmodgroup=vgm.getvectormodalities(i);
			for (int j=0; j<rifmodgroup.size(); j++)
			{
				String groupvalue=rifmodgroup.get(j);
				if (groupvalue!=null)
				{
					actualgroupref=actualgroupref+" "+groupvalue;
					if (!noclforvg)
						valuestowrite[j]=vgm.getcode(j, groupvalue);
					else
						valuestowrite[j]=groupvalue;
				}
			}
			actualgroupref=actualgroupref.trim();
			if (!actualgroupref.equals(""))
			{
				actualgroupref="("+actualgroupref+")";
			}
			String resmsg=erroronevaluate.get(rifmodgroup);
			if (!resmsg.equals(""))
				result.add(new LocalMessageGetter(resmsg+" "+actualgroupref+"\n"));
			actualgroupref="%1917% "+lasterror.get(rifmodgroup)+" "+actualgroupref+"\n";
			actualgroupref=actualgroupref.trim();
			result.add(new LocalMessageGetter(actualgroupref));
			Vector<double[]> currentseed=efuzzykmeans.getseeds(rifmodgroup);
			for (int j=0; j<currentseed.size(); j++)
			{
				valuestowrite[varg.length]=String.valueOf(j);
				double[] seedval=currentseed.get(j);
				for (int h=0; h<var.length; h++)
				{
					valuestowrite[varg.length+1+h]=double2String(seedval[h]);
				}
				dw.write(valuestowrite);
			}
		}
		efuzzykmeans.endfuzzykmeans();
		filedata.closeAll();

		Hashtable<String, String> othertableinfo=new Hashtable<String, String>();
		othertableinfo.put(Keywords.Distance.toLowerCase(), distanceType);

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
		parameters.add(new GetRequiredParameters(Keywords.dict+"=", "dict", true, 721, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.OUT.toLowerCase()+"=", "setting=out", true, 440, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.viewout, "checkbox", false, 1612, dep, "", 1));
		dep = new String[1];
		dep[0]=Keywords.dict;
		parameters.add(new GetRequiredParameters(Keywords.var, "vars=all", true, 641, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.vargroup, "vars=all", false, 655, dep, "", 2));
		dep = new String[0];
		parameters.add(new GetRequiredParameters(Keywords.where,"text", false, 2805,dep,"",2));
		parameters.add(new GetRequiredParameters("", "note", false, 2806, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.iterations, "text", true, 878, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.ngroup,"text", true, 879,dep,"",2));
		parameters.add(new GetRequiredParameters(Keywords.fuzzycoeff,"text", true, 930,dep,"",2));
		parameters.add(new GetRequiredParameters(Keywords.Distance, "listsingle=1084_" + Keywords.EuclideanDistance+",1085_"+Keywords.SquaredEuclideanDistance+",1086_"+Keywords.ManhattanDistance+",1087_"+Keywords.ChebyshevDistance+",1777_"+Keywords.MahalanobisDistance,false, 1083, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.accuracy,"text", false, 940,dep,"",2));
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
		retprocinfo[0]="931";
		retprocinfo[1]="877";
		return retprocinfo;
	}
}
