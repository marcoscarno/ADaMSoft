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

import ADaMSoft.algorithms.KMeansEvaluator;
import ADaMSoft.algorithms.VarGroupModalities;
import ADaMSoft.dataaccess.DictionaryReader;
import ADaMSoft.dataaccess.DataReader;
import ADaMSoft.dataaccess.DataWriter;
import ADaMSoft.algorithms.CovariancesEvaluator;

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
* This is the procedure that assign a record to a cluster according to the kmeans alghoritm
* @author marco.scarno@gmail.com
* @date 14/02/2017
*/
public class ProcKmeans extends ObjectTransformer implements RunStep
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
		String [] requiredparameters=new String[] {Keywords.OUT.toLowerCase(), Keywords.dict, Keywords.var, Keywords.iterations, Keywords.ngroup};
		String [] optionalparameters=new String[] {Keywords.vargroup, Keywords.where, Keywords.replace, Keywords.Distance, Keywords.minunits, Keywords.novgconvert, Keywords.noclforvg, Keywords.orderclbycode};
		Keywords.percentage_total=0;
		Keywords.percentage_done=0;
		StepUtilities steputilities=new StepUtilities();
		if (!steputilities.checkParameters(requiredparameters, optionalparameters, parameters))
		{
			return new Result(steputilities.getMessage(), false, null);
		}
		String ngroup=(String)parameters.get(Keywords.ngroup.toLowerCase());
		String iter=(String)parameters.get(Keywords.iterations.toLowerCase());
		String distanceType = (String)parameters.get(Keywords.Distance.toLowerCase());

		if (distanceType==null)
			distanceType=Keywords.EuclideanDistance;
		String[] dtype=new String[] {Keywords.EuclideanDistance, Keywords.SquaredEuclideanDistance, Keywords.ManhattanDistance, Keywords.ChebyshevDistance, Keywords.MahalanobisDistance};
		int vdt=steputilities.CheckOption(dtype, distanceType);
		if (vdt==0)
			return new Result("%1775% "+Keywords.Distance.toUpperCase()+"<br>\n"+steputilities.getMessage(), false, null);

		CovariancesEvaluator cove=new CovariancesEvaluator(true, true);

		novgconvert =(parameters.get(Keywords.novgconvert)!=null);
		orderclbycode =(parameters.get(Keywords.orderclbycode)!=null);
		noclforvg =(parameters.get(Keywords.noclforvg)!=null);

		int nseeds=string2int(ngroup);
		if (nseeds==0)
			return new Result("%875%<br>\n", false, null);

		double minunits=0;
		String sminunits=(String)parameters.get(Keywords.minunits.toLowerCase());
		if (sminunits!=null)
		{
			try
			{
				minunits=Double.parseDouble(sminunits);
			}
			catch (Exception emu)
			{
				return new Result("%3407%<br>\n", false, null);
			}
			if (minunits<0) return new Result("%3407%<br>\n", false, null);
		}

		int niter=string2int(iter);
		if (niter<0)
			return new Result("%1461%<br>\n", false, null);

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

		String keyword="KMeans "+dict.getkeyword();
		String description="KMeans "+dict.getdescription();

		String author=(String) parameters.get(Keywords.client_host.toLowerCase());

		DataReader data =null;

		int[] utilvarstype=varu.getnormalruleforsel();

		ValuesParser vp=new ValuesParser(utilvarstype, null, null, null, null, null);

		KMeansEvaluator ekmeans=new KMeansEvaluator(nseeds, vdt);
		ekmeans.setminimum(minunits);
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
		boolean state=false;
		Hashtable<Vector<String>, Vector<Double>> numforseed=new Hashtable<Vector<String>, Vector<Double>>();
		Hashtable<Vector<String>, Vector<Double>> distance=new Hashtable<Vector<String>, Vector<Double>>();
		data = new DataReader(dict);
		Vector<String> vargroupvalues=new Vector<String>();
		String where=(String)parameters.get(Keywords.where.toLowerCase());
		if (vdt==5)
		{
			boolean ismd=false;
			int validgroup=0;
			if (!data.open(totalvar, replacerule, false))
				return new Result(data.getmessage(), false, null);
			if (where!=null)
			{
				if (!data.setcondition(where)) return new Result(data.getmessage(), false, null);
			}
			String[] values=null;
			double[] varvalues=null;
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
			ekmeans.setmahalanobis(cove.getresult());
		}

		for (int i=0; i<niter; i++)
		{
			if (!data.open(totalvar, replacerule, false))
				return new Result(data.getmessage(), false, null);
			if (where!=null)
			{
				if (!data.setcondition(where)) return new Result(data.getmessage(), false, null);
			}
			int validgroup=0;
			String[] values=null;
			double[] varvalues=null;
			boolean ismd=false;
			ekmeans.setactualiter(i);
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
					for (int j=0; j<varvalues.length; j++)
					{
						if (Double.isNaN(varvalues[j]))
							ismd=true;
					}
					if ((vp.vargroupisnotmissing(vargroupvalues)) && (!ismd))
					{
						validgroup++;
						if (i==0)
							vgm.updateModalities(vargroupvalues);
						ekmeans.evaluateandupdate(vargroupvalues, varvalues);
					}
				}
			}
			data.close();
			if ((validgroup==0) && (where!=null))
				return new Result("%2804%\n", false, null);
			if ((validgroup==0) && (where==null))
				return new Result("%666%\n", false, null);
			state=ekmeans.getstate();
			if (state)
				break;
			if ((i<(niter-1)) && (!state))
				ekmeans.updateseeds();
		}
		if (state)
			result.add(new LocalMessageGetter("%880%<br>\n"));

		numforseed=ekmeans.getnum();
		distance=ekmeans.getdistance();
		vgm.calculate();

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
			clvar.put(String.valueOf(j), "Group: "+String.valueOf(j+1));
		}
		dsu.addnewvar("group", "%1132%", Keywords.TEXTSuffix, clvar, tempmd);
		dsu.addnewvar("num", "%1133%", Keywords.NUMSuffix+"I", tempmd, tempmd);
		dsu.addnewvar("distance", "%1134%", Keywords.NUMSuffix, tempmd, tempmd);
		for (int i=0; i<var.length; i++)
		{
			dsu.addnewvar("v_"+var[i], "%1135%: "+dict.getvarlabelfromname(var[i]), Keywords.NUMSuffix, tempmd, tempmd);
		}

		if (!dw.opendatatable(dsu.getfinalvarinfo()))
			return new Result(dw.getmessage(), false, null);

		if (totalgroupmodalities==0)
			totalgroupmodalities=1;

		Hashtable<Vector<String>, Vector<double[]>> seeds=ekmeans.getseeds();

		String[] valuestowrite=new String[varg.length+3+var.length];
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
			Vector<double[]> currentseed=seeds.get(rifmodgroup);
			Vector<Double> currentnum=numforseed.get(rifmodgroup);
			Vector<Double> currentdist=distance.get(rifmodgroup);
			int refg=0;
			for (int j=0; j<currentseed.size(); j++)
			{
				if (currentseed.get(j)!=null)
				{
					valuestowrite[varg.length]=String.valueOf(refg);
					double numofrec=(currentnum.get(j)).doubleValue();
					valuestowrite[varg.length+1]="";
					try
					{
						if (!Double.isNaN(numofrec))
							valuestowrite[varg.length+1]=String.valueOf(numofrec);
					}
					catch (Exception en) {}
					valuestowrite[varg.length+2]="";
					try
					{
						double disofrec=(currentdist.get(j)).doubleValue();
						disofrec=Math.sqrt(disofrec/numofrec);
						valuestowrite[varg.length+2]=double2String(disofrec);
					}
					catch (Exception en) {}
					double[] seedval=currentseed.get(j);
					for (int h=0; h<var.length; h++)
					{
						valuestowrite[varg.length+3+h]=double2String(seedval[h]);
					}
					dw.write(valuestowrite);
					refg++;
				}
			}
		}

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
		parameters.add(new GetRequiredParameters(Keywords.Distance, "listsingle=1084_" + Keywords.EuclideanDistance+",1085_"+Keywords.SquaredEuclideanDistance+",1086_"+Keywords.ManhattanDistance+",1087_"+Keywords.ChebyshevDistance+",1777_"+Keywords.MahalanobisDistance,false, 1083, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.iterations, "text", true, 878, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.ngroup,"text", true, 879,dep,"",2));
		parameters.add(new GetRequiredParameters(Keywords.minunits,"text", false, 3406,dep,"",2));
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
		retprocinfo[0]="876";
		retprocinfo[1]="877";
		return retprocinfo;
	}
}
