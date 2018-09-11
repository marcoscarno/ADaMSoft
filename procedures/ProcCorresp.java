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

import ADaMSoft.algorithms.CorrespondenceEvaluator;
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
* This is the procedure that implements the correspondence analysis
* @author marco.scarno@gmail.com
* @version 1.0.0, rev.: 13/02/17 by marco
*/
public class ProcCorresp extends ObjectTransformer implements RunStep
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
		String [] requiredparameters=new String[] {Keywords.OUT.toLowerCase(), Keywords.dict, Keywords.var};
		String [] optionalparameters=new String[] {Keywords.vargroup, Keywords.where, Keywords.replace, Keywords.novgconvert, Keywords.noclforvg, Keywords.orderclbycode, Keywords.usememory};
		Keywords.percentage_total=0;
		Keywords.percentage_done=0;
		StepUtilities steputilities=new StepUtilities();
		if (!steputilities.checkParameters(requiredparameters, optionalparameters, parameters))
		{
			return new Result(steputilities.getMessage(), false, null);
		}
		DataWriter dw=new DataWriter(parameters, Keywords.OUT.toLowerCase());
		if (!dw.getmessage().equals(""))
			return new Result(dw.getmessage(), false, null);

		DictionaryReader dict = (DictionaryReader)parameters.get(Keywords.dict);

		String vartemp=(String)parameters.get(Keywords.var.toLowerCase());
		String vargroup=(String)parameters.get(Keywords.vargroup.toLowerCase());

		boolean usememory =(parameters.get(Keywords.usememory)!=null);
		novgconvert =(parameters.get(Keywords.novgconvert)!=null);
		orderclbycode =(parameters.get(Keywords.orderclbycode)!=null);
		noclforvg =(parameters.get(Keywords.noclforvg)!=null);

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

		String keyword="Corresp "+dict.getkeyword();
		String description="Corresp "+dict.getdescription();

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

		String tempdir=(String)parameters.get(Keywords.WorkDir);

		GroupedMatrix2Dfile filedata=new GroupedMatrix2Dfile(tempdir, var.length);

		data = new DataReader(dict);
		if (!data.open(totalvar, replacerule, false))
			return new Result(data.getmessage(), false, null);
		String where=(String)parameters.get(Keywords.where.toLowerCase());
		if (where!=null)
		{
			if (!data.setcondition(where)) return new Result(data.getmessage(), false, null);
		}
		int validgroup=0;
		String[] values=null;
		Vector<String> vargroupvalues=new Vector<String>();
		double[] varvalues=null;
		boolean ismd=false;
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
				if (vp.vargroupisnotmissing(vargroupvalues))
				{
					ismd=false;
					for (int i=0; i<varvalues.length; i++)
					{
						if ( (Double.isNaN(varvalues[i])) || (Double.isInfinite(varvalues[i])) )
							ismd=true;
					}
					if (!ismd)
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
		}
		data.close();
		if ((validgroup==0) && (where==null))
		{
			filedata.closeAll();
			return new Result("%666%<br>\n", false, null);
		}
		if ((validgroup==0) && (where!=null))
		{
			filedata.closeAll();
			return new Result("%2804%<br>\n", false, null);
		}

		vgm.calculate();

		CorrespondenceEvaluator evalcorresp=new CorrespondenceEvaluator(vgm, filedata, var.length, usememory);

		if (!evalcorresp.geterror().equals(""))
		{
			filedata.closeAll();
			return new Result(evalcorresp.geterror(), false, null);
		}

		Hashtable<Vector<String>, double[][]> eigenvec=evalcorresp.geteigenvectors();
		Hashtable<Vector<String>, double[]> eigenval=evalcorresp.geteigenvalues();

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
		clvar.put("1", "%968%");
		clvar.put("2", "%969%");
		for (int j=0; j<var.length; j++)
		{
			clvar.put(var[j], "%970% "+dict.getvarlabelfromname(var[j]));
		}

		dsu.addnewvar("Type", "%971%", Keywords.TEXTSuffix, clvar, tempmd);

		for (int i=0; i<var.length-1; i++)
		{
			dsu.addnewvar("c_"+String.valueOf(i+1), "%972% "+String.valueOf(i+1), Keywords.TEXTSuffix, tempmd, tempmd);
		}

		if (!dw.opendatatable(dsu.getfinalvarinfo()))
			return new Result(dw.getmessage(), false, null);

		if (totalgroupmodalities==0)
			totalgroupmodalities=1;

		String[] valuestowrite=new String[varg.length+var.length];
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
			double inertia=0;
			double[] tempeval=eigenval.get(rifmodgroup);
			valuestowrite[varg.length]=String.valueOf(1);
			for (int j=0; j<tempeval.length; j++)
			{
				if ( (!Double.isNaN(tempeval[j])) && (!Double.isInfinite(tempeval[j])) )
				{
					inertia=inertia+tempeval[j];
					valuestowrite[varg.length+1+j]=double2String(tempeval[j]);
				}
			}
			dw.write(valuestowrite);
			valuestowrite[varg.length]=String.valueOf(2);
			for (int j=0; j<tempeval.length; j++)
			{
				valuestowrite[varg.length+1+j]="";
				if ( (!Double.isNaN(tempeval[j])) && (!Double.isInfinite(tempeval[j])) )
					valuestowrite[varg.length+1+j]=double2String(tempeval[j]/inertia);
			}
			dw.write(valuestowrite);
			double[][] tempevec=eigenvec.get(rifmodgroup);
			for (int j=0; j<var.length; j++)
			{
				valuestowrite[varg.length]=var[j];
				for (int k=0; k<tempevec[0].length; k++)
				{
					if ( (!Double.isNaN(tempevec[j][k])) && (!Double.isInfinite(tempevec[j][k])) )
						valuestowrite[varg.length+1+k]=double2String(tempevec[j][k]);
					else
						valuestowrite[varg.length+1+k]="";
				}
				dw.write(valuestowrite);
			}
		}

		filedata.closeAll();

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
		parameters.add(new GetRequiredParameters(Keywords.OUT.toLowerCase()+"=", "setting=out", true, 952, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.viewout, "checkbox", false, 1612, dep, "", 1));
		dep = new String[1];
		dep[0]=Keywords.dict;
		parameters.add(new GetRequiredParameters(Keywords.var, "vars=all", true, 641, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.vargroup, "vars=all", false, 655, dep, "", 2));
		dep = new String[0];
		parameters.add(new GetRequiredParameters(Keywords.where,"text", false, 2805,dep,"",2));
		parameters.add(new GetRequiredParameters("", "note", false, 2806, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.novgconvert, "checkbox", false, 2227, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.noclforvg, "checkbox", false, 2229, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.orderclbycode, "checkbox", false, 2231, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.usememory, "checkbox", false, 3312, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.replace, "listsingle=530_NULL,531_"+Keywords.replaceall+",532_"+Keywords.replaceformat+",533_"+Keywords.replacemissing,false, 534, dep, "", 2));
		return parameters;
	}
	/**
	*Returns the group and the name of the procedure
	*/
	public String[] getstepinfo()
	{
		String[] retprocinfo=new String[2];
		retprocinfo[0]="950";
		retprocinfo[1]="951";
		return retprocinfo;
	}
}
