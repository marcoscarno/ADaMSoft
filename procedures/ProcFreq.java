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

import ADaMSoft.algorithms.VarGroupModalities;
import ADaMSoft.algorithms.frequencies.FrequenciesTabulator;
import ADaMSoft.dataaccess.DataReader;
import ADaMSoft.dataaccess.DataWriter;
import ADaMSoft.dataaccess.DictionaryReader;
import ADaMSoft.keywords.Keywords;
import ADaMSoft.utilities.StepUtilities;
import ADaMSoft.utilities.DataSetUtilities;
import ADaMSoft.utilities.GetRequiredParameters;
import ADaMSoft.utilities.ValuesParser;
import ADaMSoft.utilities.VariableUtilities;
import ADaMSoft.utilities.ObjectTransformer;

/**
* This is the procedure that evaluate the frequencies of row and column variables
* @author marco.scarno@gmail.com
* @date 14/02/2017
*/
public class ProcFreq extends ObjectTransformer implements RunStep
{
	/**
	* Starts the execution of Proc Freq
	*/
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		Vector<StepResult> result = new Vector<StepResult>();
		boolean novgconvert=false;
		boolean noclforvg=false;
		boolean orderclbycode=false;
		boolean noclforvarrow=false;
		boolean totrow, totcol;
		boolean onerow=false;
		boolean orderbycolcodes=false;
		boolean orderbyrowcodes=false;
		boolean usewritefmt=false;
		boolean nozerorows=false;
		String [] requiredparameters=new String[] {Keywords.OUT.toLowerCase(), Keywords.dict};
		String [] optionalparameters=new String[] {Keywords.varcol, Keywords.where, Keywords.varrow, Keywords.vargroup, Keywords.weight,
		Keywords.totalonrows, Keywords.totaloncols, Keywords.replace, Keywords.freqtype, Keywords.mdhandling, Keywords.mdsubst,
		Keywords.novgconvert, Keywords.noclforvg, Keywords.orderclbycode, Keywords.noclforvarrow, Keywords.orderbyrowcodes, Keywords.orderbycolcodes, Keywords.usewritefmt, Keywords.nozerorows};
		Keywords.percentage_total=0;
		Keywords.percentage_done=0;
		StepUtilities steputilities=new StepUtilities();
		if (!steputilities.checkParameters(requiredparameters, optionalparameters, parameters))
		{
			return new Result(steputilities.getMessage(), false, null);
		}
		totrow =(parameters.get(Keywords.totalonrows)!=null);
		nozerorows=(parameters.get(Keywords.nozerorows)!=null);
		totcol =(parameters.get(Keywords.totaloncols)!=null);
		String replace =(String)parameters.get(Keywords.replace);
		DataWriter dw=new DataWriter(parameters, Keywords.OUT.toLowerCase());
		if (!dw.getmessage().equals(""))
			return new Result(dw.getmessage(), false, null);

		novgconvert =(parameters.get(Keywords.novgconvert)!=null);
		orderclbycode =(parameters.get(Keywords.orderclbycode)!=null);
		noclforvg =(parameters.get(Keywords.noclforvg)!=null);
		noclforvarrow =(parameters.get(Keywords.noclforvarrow)!=null);
		usewritefmt=(parameters.get(Keywords.usewritefmt)!=null);

		orderbycolcodes =(parameters.get(Keywords.orderbycolcodes)!=null);
		orderbyrowcodes =(parameters.get(Keywords.orderbyrowcodes)!=null);

		String freqtype =(String)parameters.get(Keywords.freqtype);

		if (freqtype==null)
			freqtype=Keywords.simplecounts;

		String[] ft=new String[] {Keywords.simplecounts, Keywords.rowfreq, Keywords.rowpercentfreq,
		Keywords.colfreq, Keywords.colpercentfreq, Keywords.relfreq, Keywords.relpercentfreq};
		int tabletype=steputilities.CheckOption(ft, freqtype);
		if (tabletype==0)
			return new Result("%1775% "+Keywords.freqtype.toUpperCase()+"<br>\n"+steputilities.getMessage(), false, null);

		String mdsubst=(String)parameters.get(Keywords.mdsubst);
		if (mdsubst==null)
			mdsubst="-";

		String mdh=(String)parameters.get(Keywords.mdhandling);
		if (mdh==null)
			mdh=Keywords.pairwisenomd;
		String[] mdt=new String[] {Keywords.pairwisenomd, Keywords.pairwisewithmd, Keywords.casewise};
		int mdhand=steputilities.CheckOption(mdt, mdh);
		if (mdhand==0)
			return new Result("%1775% "+Keywords.mdhandling.toUpperCase()+"<br>\n"+steputilities.getMessage(), false, null);

		DictionaryReader dict = (DictionaryReader)parameters.get(Keywords.dict);
		String tempvarrow=(String)parameters.get(Keywords.varrow.toLowerCase());
		String tempvarcol=(String)parameters.get(Keywords.varcol.toLowerCase());
		String tempvargroup=(String)parameters.get(Keywords.vargroup.toLowerCase());
		String weight=(String)parameters.get(Keywords.weight.toLowerCase());

		if (tempvarrow!=null)
		{
			String[] testnrow=tempvarrow.split(" ");
			if (testnrow.length==1)
				onerow=true;
		}

		boolean norow=false;
		boolean nocol=false;
		if (tempvarrow==null)
		{
			norow=true;
			if (orderbyrowcodes)
			{
				orderbyrowcodes=false;
				result.add(new LocalMessageGetter("%2243%<br>\n"));
			}

		}
		if (tempvarcol==null)
		{
			nocol=true;
			if (orderbycolcodes)
			{
				orderbycolcodes=false;
				result.add(new LocalMessageGetter("%2244%<br>\n"));
			}
		}

		if ((noclforvarrow) && (norow))
			result.add(new LocalMessageGetter("%2240%<br>\n"));

		if(tempvarcol==null && tempvarrow==null)
			return new Result("%783%<br>\n", false, null);

		if ((tabletype==2) && (tempvarrow!=null) && (tempvarcol==null))
			return new Result("%1625%<br>\n", false, null);
		if ((tabletype==3) && (tempvarrow!=null) && (tempvarcol==null))
			return new Result("%1626%<br>\n", false, null);

		if ((tabletype==4) && (tempvarcol!=null) && (tempvarrow==null))
			return new Result("%1625%<br>\n", false, null);
		if ((tabletype==5) && (tempvarcol!=null) && (tempvarrow==null))
			return new Result("%1626%<br>\n", false, null);

		if ((tabletype==4) && (tempvarcol==null) && (tempvarrow!=null))
		{
			if (!totrow) result.add(new LocalMessageGetter("%3272%<br>\n"));
			totrow=true;
		}
		if ((tabletype==5) && (tempvarcol==null) && (tempvarrow!=null))
		{
			if (!totrow) result.add(new LocalMessageGetter("%3272%<br>\n"));
			totrow=true;
		}

		if ((tabletype==2) && (tempvarcol!=null) && (tempvarrow==null))
		{
			if (!totcol) result.add(new LocalMessageGetter("%3271%<br>\n"));
			totcol=true;
		}
		if ((tabletype==3) && (tempvarcol!=null) && (tempvarrow==null))
		{
			if (!totcol) result.add(new LocalMessageGetter("%3271%<br>\n"));
			totcol=true;
		}

		if ((totrow) && (tempvarrow==null))
		{
			result.add(new LocalMessageGetter("%1623%<br>\n"));
			totrow=false;
		}

		if ((totcol) && (tempvarcol==null))
		{
			result.add(new LocalMessageGetter("%1624%<br>\n"));
			totcol=false;
		}

		VariableUtilities varu=new VariableUtilities(dict, tempvargroup, null, weight, tempvarrow, tempvarcol);
		if (varu.geterror())
			return new Result(varu.getmessage(), false, null);

		String keyword="Freq "+dict.getkeyword();
		String description="Freq "+dict.getdescription();
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

		if (!data.open(reqvar, replacerule, usewritefmt))
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

		String[] varRowLabels = new String[varrow.length];
		String[] varColLabels = new String[varcol.length];

		for (int i=0; i<varrow.length; i++)
		{
			varRowLabels[i]=dict.getvarlabelfromname(varrow[i]);
		}
		for (int i=0; i<varcol.length; i++)
		{
			varColLabels[i]=dict.getvarlabelfromname(varcol[i]);
		}

		FrequenciesTabulator fr = new FrequenciesTabulator(totcol, totrow, tabletype);

		if (noclforvarrow)
			fr.setNoClRow();

		if (orderbycolcodes)
			fr.orderbycolcodes();
		if (orderbyrowcodes)
			fr.orderbyrowcodes();

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

		fr.setTableVars(norow, nocol, onerow);

		int validgroup=0;
		boolean usecurrent=true;
		Vector<String> vargroupvalues;
		String[][] values=new String[0][0];
		String[][] varrowvalues=new String[0][0];
		String[][] varcolvalues=new String[0][0];
		double weightvalue=0;
		while (!data.isLast())
		{
			values = data.getOriginalTransformedRecord();
			if (values!=null)
			{
				usecurrent=true;
				if (novgconvert)
					vargroupvalues=vp.getorigvargroup(values);
				else
					vargroupvalues=vp.getvargroup(values);
				varrowvalues = vp.getrowvar(values);
				varcolvalues=vp.getcolvar(values);
				weightvalue=vp.getweight(values);
				if (!vp.vargroupisnotmissing(vargroupvalues))
				{
					usecurrent=false;
					if ((varg.length>0) && (mdhand==2))
					{
						for (int i=0; i<vargroupvalues.size(); i++)
						{
							if (vargroupvalues.get(i).equals("")) vargroupvalues.set(i, mdsubst);
						}
						usecurrent=true;
					}
				}
				if (Double.isNaN(weightvalue))
					usecurrent=false;
				if (mdhand==3)
				{
					if (!norow)
					{
						for (int i=0; i<varrowvalues.length; i++)
						{
							if (varrowvalues[i][1].equals(""))
							{
								usecurrent=false;
								break;
							}
						}
					}
					if (!nocol)
					{
						for (int i=0; i<varcolvalues.length; i++)
						{
							if (varcolvalues[i][1].equals(""))
							{
								usecurrent=false;
								break;
							}
						}
					}
				}
				else if (mdhand==2)
				{
					if (!norow)
					{
						for (int i=0; i<varrowvalues.length; i++)
						{
							if (varrowvalues[i][1].equals(""))
								varrowvalues[i][1]=mdsubst;
						}
					}
					if (!nocol)
					{
						for (int i=0; i<varcolvalues.length; i++)
						{
							if (varcolvalues[i][1].equals(""))
								varcolvalues[i][1]=mdsubst;
						}
					}
				}
				else
				{
					if ((!nocol) && (varcolvalues.length==1) && (varcolvalues[0][1].equals("")))
						usecurrent=false;
					if ((!norow) && (varrowvalues.length==1) && (varrowvalues[0][1].equals("")))
						usecurrent=false;
				}
				if (usecurrent)
				{
					validgroup++;
					vgm.updateModalities(vargroupvalues);
					fr.evaluate(vargroupvalues,varrowvalues,varcolvalues,weightvalue);
				}
			}
		}
		data.close();
		if ((validgroup==0) && (where!=null))
			return new Result("%2804%<br>\n", false, null);
		if ((validgroup==0) && (where==null))
			return new Result("%666%<br>\n", false, null);

		fr.calculate();
		vgm.calculate();

		String[][] newlabels=fr.getcolnames(varcol, varColLabels);
		String[][] newcodelabels=fr.getrownames(varrow, varRowLabels);
		int finalcols=fr.getnumcols();
		Hashtable<Vector<String>, double[][]> table=fr.gettable();
		Hashtable<String, String> tempmd=new Hashtable<String, String>();
		DataSetUtilities dsu=new DataSetUtilities();
		dsu.setreplace(replace);
		Vector<Hashtable<String, String>> groupcodelabels=vgm.getgroupcodelabels();
		for (int i=0; i<varg.length; i++)
		{
			if (!noclforvg)
				dsu.addnewvarfromolddict(dict, varg[i], groupcodelabels.get(i), tempmd, "g_"+varg[i]);
			else
				dsu.addnewvarfromolddict(dict, varg[i], tempmd, tempmd, "g_"+varg[i]);
		}
		if (!norow)
		{
			Hashtable<String, String> tempcl=new Hashtable<String, String>();
			if (!noclforvarrow)
			{
				for (int i=0; i<newcodelabels.length; i++)
				{
					tempcl.put(newcodelabels[i][0], newcodelabels[i][1]);
				}
			}
			if (!onerow)
				dsu.addnewvar("rowvars", "%1622%", Keywords.TEXTSuffix, tempcl, tempmd);
			else
				dsu.addnewvar("rowvars", varRowLabels[0], Keywords.TEXTSuffix, tempcl, tempmd);
		}

		if (nocol)
			dsu.addnewvar("rowtotal", newlabels[0][1], Keywords.NUMSuffix, tempmd, tempmd);

		else
		{
			for (int i=0; i<newlabels.length; i++)
			{
				dsu.addnewvar("rowtotal_"+String.valueOf(i+1), newlabels[i][1], Keywords.NUMSuffix, tempmd, tempmd);
			}
		}
		if (!dw.opendatatable(dsu.getfinalvarinfo()))
			return new Result(dw.getmessage(), false, null);

		int totalgroupmodalities=vgm.getTotal();

		if (totalgroupmodalities==0)
			totalgroupmodalities=1;

		int adder=1;
		if (norow)
			adder=0;
		String[] valuestowrite=new String[varg.length+adder+finalcols];
		double sum=0;
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
			double[][] temptable=table.get(rifmodgroup);
			for (int j=0; j<temptable.length; j++)
			{
				sum=0;
				if (!norow)
				{
					if (noclforvarrow)
						valuestowrite[varg.length]=newcodelabels[j][1];
					else
						valuestowrite[varg.length]=newcodelabels[j][0];
				}
				for (int k=0; k<temptable[0].length; k++)
				{
					sum=sum+Math.abs(temptable[j][k]);
					valuestowrite[varg.length+k+adder]=double2String(temptable[j][k]);
				}
				if (nozerorows)
				{
					if (sum>0)
						dw.write(valuestowrite);
				}
				else
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
		parameters.add(new GetRequiredParameters(Keywords.varrow, "vars=all", false, 667, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.varcol, "vars=all", false, 668, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.vargroup, "vars=all", false, 655, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.weight, "vars=all", false, 656, dep, "", 2));
		dep = new String[0];
		parameters.add(new GetRequiredParameters(Keywords.where,"text", false, 2805,dep,"",2));
		parameters.add(new GetRequiredParameters("", "note", false, 2806, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.freqtype, "listsingle=1145_" + Keywords.simplecounts+",1146_"+Keywords.rowfreq+",1147_"+Keywords.rowpercentfreq+",1148_"+Keywords.colfreq+",1149_"+Keywords.colpercentfreq+",1150_"+Keywords.relfreq+",1151_"+Keywords.relpercentfreq,false, 1144, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.totalonrows, "checkbox", false, 669, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.totaloncols, "checkbox", false, 670, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.mdhandling, "listsingle=1555_"+Keywords.pairwisenomd+",1556_"+Keywords.pairwisewithmd+",1557_"+Keywords.casewise, false, 1554, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.mdsubst, "text", false, 1558, dep, "", 2));
		parameters.add(new GetRequiredParameters("", "note", false, 1559, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.novgconvert, "checkbox", false, 2227, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.noclforvg, "checkbox", false, 2229, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.orderclbycode, "checkbox", false, 2231, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.noclforvarrow, "checkbox", false, 2239, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.orderbyrowcodes, "checkbox", false, 2241, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.orderbycolcodes, "checkbox", false, 2242, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.usewritefmt, "checkbox", false, 2275, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.nozerorows, "checkbox", false, 2329, dep, "", 2));
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
		retprocinfo[1]="760";
		return retprocinfo;
	}
}
