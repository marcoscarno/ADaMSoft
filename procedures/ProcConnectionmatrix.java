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

import ADaMSoft.utilities.GetRequiredParameters;
import ADaMSoft.utilities.ValuesParser;
import ADaMSoft.utilities.VariableUtilities;
import ADaMSoft.utilities.DataSetUtilities;
import ADaMSoft.utilities.ObjectTransformer;
import ADaMSoft.utilities.StepUtilities;

import ADaMSoft.keywords.Keywords;

import java.util.LinkedList;
import java.util.Hashtable;
import java.util.Vector;

import java.text.DecimalFormatSymbols;
import java.text.DecimalFormat;

import cern.jet.stat.Probability;

/**
* This is the procedure that evaluate the matrix of crossed connection indeces (Chi square, Phi square, Cramer V) for qualitative variables
* @author marco.scarno@gmail.com
* @date 13/02/2017
*/
public class ProcConnectionmatrix extends ObjectTransformer implements RunStep
{
	/**
	* Starts the execution of Proc ConnectionMatrix
	*/
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		Vector<StepResult> result = new Vector<StepResult>();
		boolean novgconvert=false;
		boolean noclforvg=false;
		boolean orderclbycode=false;
		String [] requiredparameters=new String[] {Keywords.OUT.toLowerCase(), Keywords.dict, Keywords.var};
		String [] optionalparameters=new String[] {Keywords.vargroup, Keywords.where, Keywords.type, Keywords.weight, Keywords.replace, Keywords.novgconvert, Keywords.noclforvg, Keywords.orderclbycode};
		Keywords.percentage_total=0;
		Keywords.percentage_done=0;
		StepUtilities steputilities=new StepUtilities();
		if (!steputilities.checkParameters(requiredparameters, optionalparameters, parameters))
		{
			return new Result(steputilities.getMessage(), false, null);
		}
		String replace =(String)parameters.get(Keywords.replace);
		String type =(String)parameters.get(Keywords.type);
		DataWriter dw=new DataWriter(parameters, Keywords.OUT.toLowerCase());
		if (!dw.getmessage().equals(""))
			return new Result(dw.getmessage(), false, null);

		novgconvert =(parameters.get(Keywords.novgconvert)!=null);
		orderclbycode =(parameters.get(Keywords.orderclbycode)!=null);
		noclforvg =(parameters.get(Keywords.noclforvg)!=null);

		if (type==null)
			type=Keywords.chisquare;

		boolean ispchisquare=false;
		boolean ischisquareandp=false;

		String[] ft=new String[] {Keywords.chisquare, Keywords.phisquare, Keywords.cramerv, Keywords.pchisquare, Keywords.chisquareandp};
		int tabletype=steputilities.CheckOption(ft, type);
		if (tabletype==0)
			return new Result("%1775% "+Keywords.type.toUpperCase()+"<br>\n"+steputilities.getMessage(), false, null);

		if (tabletype==4)
		{
			tabletype=1;
			ispchisquare=true;
		}

		if (tabletype==5)
		{
			tabletype=1;
			ischisquareandp=true;
		}

		DictionaryReader dict = (DictionaryReader)parameters.get(Keywords.dict);
		String tempvar=(String)parameters.get(Keywords.var.toLowerCase());
		String weight=(String)parameters.get(Keywords.weight.toLowerCase());
		String tempvargroup=(String)parameters.get(Keywords.vargroup.toLowerCase());

		VariableUtilities varu=new VariableUtilities(dict, tempvargroup, tempvar, weight, null, null);
		if (varu.geterror())
			return new Result(varu.getmessage(), false, null);

		String keyword="ConnectionMatrix "+Keywords.type.toUpperCase()+"="+type.toUpperCase()+" "+dict.getkeyword();
		String description="ConnectionMatrix "+Keywords.type.toUpperCase()+"="+type.toUpperCase()+" "+dict.getdescription();
		String author=(String) parameters.get(Keywords.client_host.toLowerCase());

		String[] var=varu.getanalysisvar();
		if (var.length<2)
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

		CrossedConnectionsEvaluator cpse=new CrossedConnectionsEvaluator(tabletype);
		CrossedConnectionsEvaluator pcpse=null;
		if (ischisquareandp) pcpse=new CrossedConnectionsEvaluator(1);

		DecimalFormatSymbols symbdec = new DecimalFormatSymbols();
		symbdec.setDecimalSeparator('.');

		String f1 = "###.##";
		String f2 = "###.###";
		DecimalFormat formatchi = new DecimalFormat(f1, symbdec);
		DecimalFormat formatpchi = new DecimalFormat(f2, symbdec);

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
				double weightvalue=vp.getweight(values);
				String[] varvalues=vp.getanalysisvar(values);
				if ((vp.vargroupisnotmissing(vargroupvalues)) && (!Double.isNaN(weightvalue)))
				{
					validgroup++;
					cpse.estimate(vargroupvalues, varvalues, weightvalue);
					if (ischisquareandp) pcpse.estimate(vargroupvalues, varvalues, weightvalue);
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
		cpse.estimateconnections();
		if (ischisquareandp) pcpse.estimateconnections();

		Hashtable<Vector<String>, double[][]> allphi=cpse.getmatrix();
		Hashtable<Vector<String>, double[][]> alldimphi=cpse.getdimmatrix();

		Hashtable<Vector<String>, double[][]> allphit=null;
		Hashtable<Vector<String>, double[][]> alldimphit=null;

		if (ischisquareandp)
		{
			allphit=pcpse.getmatrix();
			alldimphit=pcpse.getdimmatrix();
		}

		int totalgroupmodalities=vgm.getTotal();

		Vector<Hashtable<String, String>> groupcodelabels=vgm.getgroupcodelabels();

		DataSetUtilities dsu=new DataSetUtilities();

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
			clvar.put(var[j], dict.getvarlabelfromname(var[j]));
		}
		dsu.addnewvar("v"+String.valueOf(varg.length), "%1056%", Keywords.TEXTSuffix, clvar, tempmd);

		for (int i=0; i<var.length; i++)
		{
			if (!ischisquareandp) dsu.addnewvar("v"+String.valueOf(varg.length+i+1), dict.getvarlabelfromname(var[i]), Keywords.NUMSuffix, tempmd, tempmd);
			else dsu.addnewvar("v"+String.valueOf(varg.length+i+1), dict.getvarlabelfromname(var[i]), Keywords.TEXTSuffix, tempmd, tempmd);
		}

		if (!dw.opendatatable(dsu.getfinalvarinfo()))
			return new Result(dw.getmessage(), false, null);

		if (totalgroupmodalities==0)
			totalgroupmodalities=1;

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
			double[][] tempconn=allphi.get(rifmodgroup);
			if (!ischisquareandp)
			{
				if (ispchisquare)
				{
					double[][] tempdimpm=alldimphi.get(rifmodgroup);
					for (int h=0; h<tempconn.length; h++)
					{
						for (int k=0; k<tempconn[0].length; k++)
						{
							try
							{
								tempconn[h][k]=1 - Probability.chiSquare(tempdimpm[h][k], tempconn[h][k]);
							}
							catch (Exception e)
							{
								tempconn[h][k]=Double.NaN;
							}
						}
					}
				}
				for (int j=0; j<tempconn.length; j++)
				{
					valuestowrite[varg.length]=var[j];
					for (int k=0; k<tempconn[0].length; k++)
					{
						valuestowrite[varg.length+1+k]=double2String(tempconn[j][k]);
					}
					dw.write(valuestowrite);
				}
			}
			else
			{
				double[][] ta=allphit.get(rifmodgroup);
				double[][] tb=alldimphit.get(rifmodgroup);
				for (int h=0; h<tempconn.length; h++)
				{
					for (int k=0; k<tempconn[0].length; k++)
					{
						try
						{
							ta[h][k]=1 - Probability.chiSquare(tb[h][k], ta[h][k]);
						}
						catch (Exception e)
						{
							ta[h][k]=Double.NaN;
						}
					}
				}
				for (int j=0; j<tempconn.length; j++)
				{
					valuestowrite[varg.length]=var[j];
					for (int k=0; k<tempconn[0].length; k++)
					{
						valuestowrite[varg.length+1+k]=formatchi.format(tempconn[j][k])+" ("+formatpchi.format(ta[j][k])+")";
					}
					dw.write(valuestowrite);
				}
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
		parameters.add(new GetRequiredParameters(Keywords.OUT.toLowerCase()+"=", "setting=out", true, 440, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.viewout, "checkbox", false, 1612, dep, "", 1));
		dep = new String[1];
		dep[0]=Keywords.dict;
		parameters.add(new GetRequiredParameters(Keywords.var, "vars=all", true, 1469, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.vargroup, "vars=all", false, 655, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.weight, "vars=all", false, 656, dep, "", 2));
		dep = new String[0];
		parameters.add(new GetRequiredParameters(Keywords.where,"text", false, 2805,dep,"",2));
		parameters.add(new GetRequiredParameters("", "note", false, 2806, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.type, "listsingle=855_" + Keywords.chisquare+",1471_"+Keywords.phisquare+",1472_"+Keywords.cramerv+",1016_"+Keywords.pchisquare+",3169_"+Keywords.chisquareandp,false, 1473, dep, "", 2));
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
		retprocinfo[0]="759";
		retprocinfo[1]="1470";
		return retprocinfo;
	}
}
