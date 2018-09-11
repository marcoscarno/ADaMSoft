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

import ADaMSoft.algorithms.VarGroupModalities;
import ADaMSoft.dataaccess.DictionaryReader;
import ADaMSoft.dataaccess.DataReader;
import ADaMSoft.dataaccess.DataWriter;

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
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.ml.clustering.Clusterer;
import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer;
import org.apache.commons.math3.ml.clustering.CentroidCluster;
import org.apache.commons.math3.ml.clustering.DoublePoint;
import org.apache.commons.math3.ml.*;
import org.apache.commons.math3.random.*;
import org.apache.commons.math3.ml.distance.*;
import org.apache.commons.math3.ml.clustering.*;
import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer.*;

/**
* This is the procedure that assign a record to a cluster according to the kmeans++ alghoritm
* @author marco.scarno@gmail.com
* @date 15/10/2017
*/
public class ProcMultikmeansplusplus extends ObjectTransformer implements RunStep
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
		String [] requiredparameters=new String[] {Keywords.OUT.toLowerCase(), Keywords.dict, Keywords.var, Keywords.ngroup, Keywords.numtrials};
		String [] optionalparameters=new String[] {Keywords.vargroup, Keywords.where, Keywords.replace, Keywords.Distance,
		Keywords.emptyclusterstrategy, Keywords.novgconvert, Keywords.noclforvg, Keywords.orderclbycode, Keywords.iterations};
		Keywords.percentage_total=0;
		Keywords.percentage_done=0;
		StepUtilities steputilities=new StepUtilities();
		if (!steputilities.checkParameters(requiredparameters, optionalparameters, parameters))
		{
			return new Result(steputilities.getMessage(), false, null);
		}
		String ngroup=(String)parameters.get(Keywords.ngroup.toLowerCase());
		String iter=(String)parameters.get(Keywords.iterations.toLowerCase());
		if (iter==null)
			iter="1000";

		String nt=(String)parameters.get(Keywords.numtrials.toLowerCase());
		int numtrials=string2int(nt);
		if (numtrials<=1)
			return new Result("%4199%<br>\n", false, null);

		String distanceType = (String)parameters.get(Keywords.Distance.toLowerCase());
		if (distanceType==null)
			distanceType=Keywords.EuclideanDistance;
		String[] dtype=new String[] {Keywords.CanberraDistance, Keywords.ChebyshevDistance, Keywords.EarthMoversDistance,
		Keywords.EuclideanDistance, Keywords.ManhattanDistance};
		int vdt=steputilities.CheckOption(dtype, distanceType);
		if (vdt==0)
			return new Result("%1775% "+Keywords.Distance.toUpperCase()+"<br>\n"+steputilities.getMessage(), false, null);

		String emptyclusterstrategy = (String)parameters.get(Keywords.emptyclusterstrategy.toLowerCase());
		int edt=-1;
		if (emptyclusterstrategy!=null)
		{
			String[] etype=new String[] {Keywords.generateerror, Keywords.farthestpoint, Keywords.largestpointsnumber,
			Keywords.largestvariance};
			edt=steputilities.CheckOption(etype, emptyclusterstrategy);
			if (edt==0)
				return new Result("%1775% "+Keywords.emptyclusterstrategy.toUpperCase()+"<br>\n"+steputilities.getMessage(), false, null);
		}

		novgconvert =(parameters.get(Keywords.novgconvert)!=null);
		orderclbycode =(parameters.get(Keywords.orderclbycode)!=null);
		noclforvg =(parameters.get(Keywords.noclforvg)!=null);

		int nseeds=string2int(ngroup);
		if (nseeds==0)
			return new Result("%875%<br>\n", false, null);

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

		String keyword="KMeansplusplus "+dict.getkeyword();
		String description="KMeansplusplus "+dict.getdescription();

		String author=(String) parameters.get(Keywords.client_host.toLowerCase());

		DataReader data =null;

		int[] utilvarstype=varu.getnormalruleforsel();

		ValuesParser vp=new ValuesParser(utilvarstype, null, null, null, null, null);
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

		data = new DataReader(dict);
		Vector<String> vargroupvalues=new Vector<String>();
		String where=(String)parameters.get(Keywords.where.toLowerCase());

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
		Hashtable<Vector<String>, java.util.ArrayList<DoublePoint>> vg_points=new Hashtable<Vector<String>, java.util.ArrayList<DoublePoint>>();
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
				double[] temp_p=new double[varvalues.length];
				for (int j=0; j<varvalues.length; j++)
				{
					if (Double.isNaN(varvalues[j]))
						ismd=true;
					temp_p[j]=varvalues[j];
				}
				if ((vp.vargroupisnotmissing(vargroupvalues)) && (!ismd))
				{
					validgroup++;
					vgm.updateModalities(vargroupvalues);
					Vector<String> temp_vargroupvalues=new Vector<String>();
					for (int i=0; i<vargroupvalues.size(); i++)
					{
						temp_vargroupvalues.add(vargroupvalues.get(i));
					}
					if (vg_points.get(temp_vargroupvalues)!=null)
					{
						java.util.ArrayList<DoublePoint> temp_points=vg_points.get(vargroupvalues);
						temp_points.add(new DoublePoint(temp_p));
						vg_points.put(temp_vargroupvalues, temp_points);
					}
					if (vg_points.get(temp_vargroupvalues)==null)
					{
						java.util.ArrayList<DoublePoint> temp_points=new java.util.ArrayList<DoublePoint>();
						temp_points.add(new DoublePoint(temp_p));
						vg_points.put(temp_vargroupvalues, temp_points);
					}
				}
			}
		}
		data.close();
		if ((validgroup==0) && (where!=null))
		{
			vg_points.clear();
			vg_points=null;
			return new Result("%2804%\n", false, null);
		}
		if ((validgroup==0) && (where==null))
		{
			vg_points.clear();
			vg_points=null;
			return new Result("%666%\n", false, null);
		}
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
		for (int i=0; i<var.length; i++)
		{
			dsu.addnewvar("v_"+var[i], "%1135%: "+dict.getvarlabelfromname(var[i]), Keywords.NUMSuffix, tempmd, tempmd);
		}

		if (!dw.opendatatable(dsu.getfinalvarinfo()))
			return new Result(dw.getmessage(), false, null);

		if (totalgroupmodalities==0)
			totalgroupmodalities=1;

		String error_procedure="";

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
			try
			{
				java.util.ArrayList<DoublePoint> temp_points=vg_points.get(rifmodgroup);
				KMeansPlusPlusClusterer clusterer=null;
				if (edt==-1)
				{
					if (vdt==1)
						clusterer = new KMeansPlusPlusClusterer<DoublePoint>(nseeds, niter, new CanberraDistance());
					else if (vdt==2)
						clusterer = new KMeansPlusPlusClusterer<DoublePoint>(nseeds, niter, new ChebyshevDistance());
					else if (vdt==3)
						clusterer = new KMeansPlusPlusClusterer<DoublePoint>(nseeds, niter, new  EarthMoversDistance());
					else if (vdt==5)
						clusterer = new KMeansPlusPlusClusterer<DoublePoint>(nseeds, niter, new  ManhattanDistance());
					else
						clusterer = new KMeansPlusPlusClusterer<DoublePoint>(nseeds, niter);
				}
				else
				{
					if (vdt==1 && edt==1)
						clusterer = new KMeansPlusPlusClusterer<DoublePoint>(nseeds, niter, new CanberraDistance(), new JDKRandomGenerator(),EmptyClusterStrategy.ERROR);
					else if (vdt==1 && edt==2)
						clusterer = new KMeansPlusPlusClusterer<DoublePoint>(nseeds, niter, new CanberraDistance(), new JDKRandomGenerator(),EmptyClusterStrategy.FARTHEST_POINT);
					else if (vdt==1 && edt==3)
						clusterer = new KMeansPlusPlusClusterer<DoublePoint>(nseeds, niter, new CanberraDistance(), new JDKRandomGenerator(),EmptyClusterStrategy.LARGEST_POINTS_NUMBER);
					else if (vdt==1 && edt==4)
						clusterer = new KMeansPlusPlusClusterer<DoublePoint>(nseeds, niter, new CanberraDistance(), new JDKRandomGenerator(),EmptyClusterStrategy.LARGEST_VARIANCE);
					else if (vdt==2 && edt==1)
						clusterer = new KMeansPlusPlusClusterer<DoublePoint>(nseeds, niter, new ChebyshevDistance(), new JDKRandomGenerator(),EmptyClusterStrategy.ERROR);
					else if (vdt==2 && edt==2)
						clusterer = new KMeansPlusPlusClusterer<DoublePoint>(nseeds, niter, new ChebyshevDistance(), new JDKRandomGenerator(),EmptyClusterStrategy.FARTHEST_POINT);
					else if (vdt==2 && edt==3)
						clusterer = new KMeansPlusPlusClusterer<DoublePoint>(nseeds, niter, new ChebyshevDistance(), new JDKRandomGenerator(),EmptyClusterStrategy.LARGEST_POINTS_NUMBER);
					else if (vdt==2 && edt==4)
						clusterer = new KMeansPlusPlusClusterer<DoublePoint>(nseeds, niter, new ChebyshevDistance(), new JDKRandomGenerator(),EmptyClusterStrategy.LARGEST_VARIANCE);
					else if (vdt==3 && edt==1)
						clusterer = new KMeansPlusPlusClusterer<DoublePoint>(nseeds, niter, new  EarthMoversDistance(), new JDKRandomGenerator(),EmptyClusterStrategy.ERROR);
					else if (vdt==3 && edt==2)
						clusterer = new KMeansPlusPlusClusterer<DoublePoint>(nseeds, niter, new  EarthMoversDistance(), new JDKRandomGenerator(),EmptyClusterStrategy.FARTHEST_POINT);
					else if (vdt==3 && edt==3)
						clusterer = new KMeansPlusPlusClusterer<DoublePoint>(nseeds, niter, new  EarthMoversDistance(), new JDKRandomGenerator(),EmptyClusterStrategy.LARGEST_POINTS_NUMBER);
					else if (vdt==3 && edt==4)
						clusterer = new KMeansPlusPlusClusterer<DoublePoint>(nseeds, niter, new  EarthMoversDistance(), new JDKRandomGenerator(),EmptyClusterStrategy.LARGEST_VARIANCE);
					else if (vdt==5 && edt==1)
						clusterer = new KMeansPlusPlusClusterer<DoublePoint>(nseeds, niter, new  ManhattanDistance(), new JDKRandomGenerator(),EmptyClusterStrategy.ERROR);
					else if (vdt==5 && edt==2)
						clusterer = new KMeansPlusPlusClusterer<DoublePoint>(nseeds, niter, new  ManhattanDistance(), new JDKRandomGenerator(),EmptyClusterStrategy.FARTHEST_POINT);
					else if (vdt==5 && edt==3)
						clusterer = new KMeansPlusPlusClusterer<DoublePoint>(nseeds, niter, new  ManhattanDistance(), new JDKRandomGenerator(),EmptyClusterStrategy.LARGEST_POINTS_NUMBER);
					else if (vdt==5 && edt==4)
						clusterer = new KMeansPlusPlusClusterer<DoublePoint>(nseeds, niter, new  ManhattanDistance(), new JDKRandomGenerator(),EmptyClusterStrategy.LARGEST_VARIANCE);
					else if (vdt==4 && edt==1)
						clusterer = new KMeansPlusPlusClusterer<DoublePoint>(nseeds, niter, new  EuclideanDistance(), new JDKRandomGenerator(),EmptyClusterStrategy.ERROR);
					else if (vdt==4 && edt==2)
						clusterer = new KMeansPlusPlusClusterer<DoublePoint>(nseeds, niter, new  EuclideanDistance(), new JDKRandomGenerator(),EmptyClusterStrategy.FARTHEST_POINT);
					else if (vdt==4 && edt==3)
						clusterer = new KMeansPlusPlusClusterer<DoublePoint>(nseeds, niter, new  EuclideanDistance(), new JDKRandomGenerator(),EmptyClusterStrategy.LARGEST_POINTS_NUMBER);
					else
						clusterer = new KMeansPlusPlusClusterer<DoublePoint>(nseeds, niter, new  EuclideanDistance(), new JDKRandomGenerator(),EmptyClusterStrategy.LARGEST_VARIANCE);
				}
				MultiKMeansPlusPlusClusterer multi_clusterer=new MultiKMeansPlusPlusClusterer(clusterer, numtrials);
				java.util.List<CentroidCluster<DoublePoint>> clusterResults = multi_clusterer.cluster(temp_points);
				for (int j=0; j<clusterResults.size(); j++)
				{
					valuestowrite[varg.length]=String.valueOf(j+1);
					double[] actseed=clusterResults.get(j).getCenter().getPoint();
					for (int k=0; k<actseed.length; k++)
					{
						valuestowrite[varg.length+1+k]=String.valueOf(actseed[k]);
					}
					dw.write(valuestowrite);
				}
				vg_points.remove(rifmodgroup);
				clusterer=null;
				clusterResults=null;
			}
			catch (Exception ec)
			{
				error_procedure=ec.toString()+"<br>";
			}
		}
		if (!error_procedure.equals(""))
		{
			dw.deletetmp();
			return new Result(error_procedure+"\n", false, null);
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
		parameters.add(new GetRequiredParameters(Keywords.Distance, "listsingle=1084_" + Keywords.EuclideanDistance+",4191_"+Keywords.CanberraDistance+",1086_"+Keywords.ManhattanDistance+",1087_"+Keywords.ChebyshevDistance+",4192_"+Keywords.EarthMoversDistance,false, 1083, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.iterations, "text", false, 4202, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.ngroup,"text", true, 879,dep,"",2));
		parameters.add(new GetRequiredParameters(Keywords.numtrials,"text", true, 4201,dep,"",2));
		parameters.add(new GetRequiredParameters(Keywords.emptyclusterstrategy, "listsingle=4194_" + Keywords.generateerror+",4195_"+Keywords.farthestpoint+",4196_"+Keywords.largestpointsnumber+",4197_"+Keywords.largestvariance,false, 4193, dep, "", 2));
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
		retprocinfo[1]="4200";
		return retprocinfo;
	}
}
