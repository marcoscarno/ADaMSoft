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

import ADaMSoft.algorithms.CrossedConnectionsEvaluator;
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

import java.util.LinkedList;
import java.util.Hashtable;
import java.util.Vector;

/**
* This is the procedure that creates clusters of variables
* @author marco.scarno@gmail.com
* @date 14/02/2017
*/
public class ProcQualvarclusters extends ObjectTransformer implements RunStep
{
	/**
	* Starts the execution of Proc Qualvarclusters
	*/
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		Vector<StepResult> result = new Vector<StepResult>();
		boolean novgconvert=false;
		boolean noclforvg=false;
		boolean orderclbycode=false;
		String [] requiredparameters=new String[] {Keywords.OUT.toLowerCase(), Keywords.dict, Keywords.var};
		String [] optionalparameters=new String[] {Keywords.vargroup, Keywords.weight, Keywords.where, Keywords.replace, Keywords.novgconvert, Keywords.noclforvg, Keywords.orderclbycode};
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

		novgconvert =(parameters.get(Keywords.novgconvert)!=null);
		orderclbycode =(parameters.get(Keywords.orderclbycode)!=null);
		noclforvg =(parameters.get(Keywords.noclforvg)!=null);

		DictionaryReader dict = (DictionaryReader)parameters.get(Keywords.dict);
		String tempvar=(String)parameters.get(Keywords.var.toLowerCase());
		String weight=(String)parameters.get(Keywords.weight.toLowerCase());
		String tempvargroup=(String)parameters.get(Keywords.vargroup.toLowerCase());

		VariableUtilities varu=new VariableUtilities(dict, tempvargroup, tempvar, weight, null, null);
		if (varu.geterror())
			return new Result(varu.getmessage(), false, null);

		String keyword="QualVarClusters "+dict.getkeyword();
		String description="QualVarClusters "+dict.getdescription();
		String author=(String) parameters.get(Keywords.client_host.toLowerCase());

		String[] var=varu.getanalysisvar();
		if (var.length<3)
			return new Result("%1474%<br>\n", false, null);

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

		CrossedConnectionsEvaluator cpse=new CrossedConnectionsEvaluator(2);
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
				String[] varvalues=vp.getanalysisvar(values);
				double weightvalue=vp.getweight(values);
				if ((vp.vargroupisnotmissing(vargroupvalues)) && (!Double.isNaN(weightvalue)))
				{
					validgroup++;
					vgm.updateModalities(vargroupvalues);
					cpse.estimate(vargroupvalues, varvalues, weightvalue);
				}
			}
		}
		data.close();
		if ((validgroup==0) && (where!=null))
			return new Result("%2804%<br>\n", false, null);
		if ((validgroup==0) && (where==null))
			return new Result("%666%<br>\n", false, null);
		vgm.calculate();
		cpse.estimateconnections();

		Hashtable<Vector<String>, Vector<String[]>> res=cpse.estimateclusters(var);

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
		for (int j=0; j<var.length; j++)
		{
			clvar.put("v_"+var[j], dict.getvarlabelfromname(var[j]));
			if (j<(var.length-1))
				clvar.put("CL"+String.valueOf(j+var.length+1), "Cluster: "+String.valueOf(j+var.length+1));
		}
		dsu.addnewvar("step", "%1070%", Keywords.NUMSuffix, tempmd, tempmd);
		dsu.addnewvar("first", "%1071%", Keywords.TEXTSuffix, clvar, tempmd);
		dsu.addnewvar("second", "%1072%", Keywords.TEXTSuffix, clvar, tempmd);
		dsu.addnewvar("newcluster", "%1073%", Keywords.TEXTSuffix, clvar, tempmd);
		dsu.addnewvar("numvars", "%2392%", Keywords.TEXTSuffix, clvar, tempmd);
		dsu.addnewvar("distance", "%1476%", Keywords.NUMSuffix, tempmd, tempmd);
		dsu.addnewvar("real_distance", "%1475%", Keywords.NUMSuffix, tempmd, tempmd);

		if (!dw.opendatatable(dsu.getfinalvarinfo()))
			return new Result(dw.getmessage(), false, null);

		if (totalgroupmodalities==0)
			totalgroupmodalities=1;

		String[] valuestowrite=new String[varg.length+7];
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
			Vector<String[]> tempres=res.get(rifmodgroup);
			double lastdist=0;
			for (int j=0; j<tempres.size(); j++)
			{
				String[] tv=tempres.get(j);
				valuestowrite[varg.length]=String.valueOf(j+1);
				valuestowrite[varg.length+1]=tv[0];
				valuestowrite[varg.length+2]=tv[1];
				if ((tv[0].startsWith("CL")) && (!tv[1].startsWith("CL")))
				{
					valuestowrite[varg.length+1]=tv[1];
					valuestowrite[varg.length+2]=tv[0];
				}
				if ((tv[0].startsWith("CL")) && (tv[1].startsWith("CL")))
				{
					double f1=Double.valueOf(tv[0].replaceAll("CL",""));
					double f2=Double.valueOf(tv[1].replaceAll("CL",""));
					if (f1>f2)
					{
						valuestowrite[varg.length+1]=tv[1];
						valuestowrite[varg.length+2]=tv[0];
					}
				}
				valuestowrite[varg.length+3]=tv[4];
				valuestowrite[varg.length+4]=tv[3];
				double dist=Double.parseDouble(tv[2]);
				valuestowrite[varg.length+6]=tv[2];
				lastdist+=dist;
				valuestowrite[varg.length+5]=String.valueOf(lastdist);
				dw.write(valuestowrite);
			}
		}

		cpse=null;

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
		parameters.add(new GetRequiredParameters(Keywords.OUT.toLowerCase()+"=", "setting=out", true, 1468, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.viewout, "checkbox", false, 1612, dep, "", 1));
		dep = new String[1];
		dep[0]=Keywords.dict;
		parameters.add(new GetRequiredParameters(Keywords.var, "vars=all", true, 1469, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.vargroup, "vars=all", false, 655, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.weight, "vars=all", false, 656, dep, "", 2));
		dep = new String[0];
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
		retprocinfo[0]="1089";
		retprocinfo[1]="1467";
		return retprocinfo;
	}
}
