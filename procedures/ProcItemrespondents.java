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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Vector;
import java.util.TreeMap;

import ADaMSoft.algorithms.VarGroupModalities;
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
* This is the procedure that evaluate the respondents for each item on a series of items
* @author marco.scarno@gmail.com
* @date 14/02/2017
*/
public class ProcItemrespondents extends ObjectTransformer implements RunStep
{
	/**
	* Starts the execution of Proc Itemrespondents
	*/
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		String [] requiredparameters=new String[] {Keywords.OUT.toLowerCase(), Keywords.dict, Keywords.var};
		String [] optionalparameters=new String[] {Keywords.where, Keywords.vargroup, Keywords.replace};
		Keywords.percentage_total=0;
		Keywords.percentage_done=0;
		StepUtilities steputilities=new StepUtilities();
		if (!steputilities.checkParameters(requiredparameters, optionalparameters, parameters))
		{
			return new Result(steputilities.getMessage(), false, null);
		}
		Vector<StepResult> result = new Vector<StepResult>();

		String replace =(String)parameters.get(Keywords.replace);
		DataWriter dw=new DataWriter(parameters, Keywords.OUT.toLowerCase());
		if (!dw.getmessage().equals(""))
			return new Result(dw.getmessage(), false, null);

		String tempvar=(String)parameters.get(Keywords.var);
		String[] vars=tempvar.split(" ");
		if (vars.length<3)
			return new Result("%3107%<br>\n", false, null);

		DictionaryReader dict = (DictionaryReader)parameters.get(Keywords.dict);
		String tempvargroup=(String)parameters.get(Keywords.vargroup.toLowerCase());

		VariableUtilities varu=new VariableUtilities(dict, tempvargroup, tempvar, null, null, null);
		if (varu.geterror())
			return new Result(varu.getmessage(), false, null);

		String[] var=varu.getanalysisvar();
		String[] varg=varu.getgroupvar();
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

		int[] utilvarstype=varu.getnormalruleforsel();

		ValuesParser vp=new ValuesParser(utilvarstype, null, null, null, null, null);
		VarGroupModalities vgm=new VarGroupModalities();

		Hashtable<Vector<String>, ItemAnswer[]> pos=new Hashtable<Vector<String>, ItemAnswer[]>();
		Hashtable<Vector<String>, MissingAnswer[]> mis=new Hashtable<Vector<String>, MissingAnswer[]>();
		Hashtable<Vector<String>, Double> num=new Hashtable<Vector<String>, Double>();

		double refnum=0;
		int validgroup=0;
		Vector<String> vargroupvalues=new Vector<String>();
		String[] values=null;
		String[] varvalues=null;
		String[] answ=null;
		while (!data.isLast())
		{
			values = data.getRecord();
			if (values!=null)
			{
				vargroupvalues=vp.getvargroup(values);
				varvalues=vp.getanalysisvar(values);
				if (mis.get(vargroupvalues)==null)
				{
					MissingAnswer[] mi=new MissingAnswer[varvalues.length];
					for (int i=0; i<varvalues.length; i++)
					{
						mi[i]=new MissingAnswer();
					}
					mis.put(vargroupvalues, mi);
				}
				if (pos.get(vargroupvalues)==null)
				{
					ItemAnswer[] ca=new ItemAnswer[varvalues.length];
					for (int i=0; i<varvalues.length; i++)
					{
						ca[i]=new ItemAnswer();
					}
					pos.put(vargroupvalues, ca);
				}
				if (vp.vargroupisnotmissing(vargroupvalues))
				{
					vgm.updateModalities(vargroupvalues);
					validgroup++;
					if (num.get(vargroupvalues)==null) num.put(vargroupvalues, new Double(0));
					refnum=(num.get(vargroupvalues)).doubleValue();
					num.put(vargroupvalues, new Double(refnum+1));
					ItemAnswer[] tempca=pos.get(vargroupvalues);
					MissingAnswer[] tempmis=mis.get(vargroupvalues);
					for (int i=0; i<varvalues.length; i++)
					{
						if (varvalues[i].equals("")) tempmis[i].addMissing("-");
						else
						{
							if (varvalues[i].indexOf("&")>=0)
							{
								answ=varvalues[i].split("&");
								for (int k=0; k<answ.length; k++)
								{
									tempca[i].addAnswer(answ[k]);
								}
							}
							else tempca[i].addAnswer(varvalues[i]);
						}
					}
				}
			}
		}
		data.close();
		if ((validgroup==0) && (where!=null))
			return new Result("%2804%<br>\n", false, null);
		if ((validgroup==0) && (where==null))
			return new Result("%666%<br>\n", false, null);
		vgm.calculate();
		int totalgroupmodalities=vgm.getTotal();
		if (totalgroupmodalities==0)
			totalgroupmodalities=1;

		String keyword="Item respondents "+dict.getkeyword();
		String description="Item respondents "+dict.getdescription();
		String author=(String) parameters.get(Keywords.client_host.toLowerCase());

		Hashtable<String, String> tempmd=new Hashtable<String, String>();
		Vector<Hashtable<String, String>> groupcodelabels=vgm.getgroupcodelabels();

		DataSetUtilities dsu=new DataSetUtilities();
		dsu.setreplace(replace);
		for (int j=0; j<varg.length; j++)
		{
			dsu.addnewvarfromolddict(dict, varg[j], groupcodelabels.get(j), tempmd, "g_"+varg[j]);
		}
		Hashtable<String, String> clvar=new Hashtable<String, String>();
		for (int j=0; j<var.length; j++)
		{
			clvar.put("ref_"+var[j], "%3125%: "+dict.getvarlabelfromname(var[j]));
		}

		Hashtable<String, String> cltype=new Hashtable<String, String>();
		cltype.put("1", "%3161%");
		cltype.put("0", "%3162%");

		dsu.addnewvar("question", "%3126%", Keywords.TEXTSuffix, clvar, tempmd);
		dsu.addnewvar("answer", "%3127%", Keywords.TEXTSuffix, tempmd, tempmd);
		dsu.addnewvar("type", "%3128%", Keywords.TEXTSuffix, cltype, tempmd);
		dsu.addnewvar("respondents", "%3163%", Keywords.NUMSuffix, tempmd, tempmd);
		dsu.addnewvar("respondents_perc", "%3164%", Keywords.NUMSuffix, tempmd, tempmd);

		if (!dw.opendatatable(dsu.getfinalvarinfo()))
			return new Result(dw.getmessage(), false, null);

		String[] valuestowrite=null;
		double refrespo=0;

		for (int i=0; i<totalgroupmodalities; i++)
		{
			valuestowrite=new String[varg.length+5];
			Vector<String> rifmodgroup=vgm.getvectormodalities(i);
			for (int j=0; j<rifmodgroup.size(); j++)
			{
				String groupvalue=rifmodgroup.get(j);
				if (groupvalue!=null)
				{
					valuestowrite[j]=groupvalue;
				}
			}
			ItemAnswer[] tempca=pos.get(rifmodgroup);
			MissingAnswer[] tempmis=mis.get(rifmodgroup);
			refrespo=(num.get(vargroupvalues)).doubleValue();
			for (int j=0; j<tempca.length; j++)
			{
				valuestowrite[varg.length]="ref_"+var[j];
				TreeMap<String, Double> tca=tempca[j].getresult();
				TreeMap<String, Double> tcm=tempmis[j].getresult();
				for (Iterator<String> it = tca.keySet().iterator(); it.hasNext();)
				{
					String key = it.next();
					double dval = (tca.get(key)).doubleValue();
					valuestowrite[varg.length+1]=key;
					valuestowrite[varg.length+2]="1";
					valuestowrite[varg.length+3]=String.valueOf(dval);
					valuestowrite[varg.length+4]=String.valueOf(100*dval/refrespo);
					dw.write(valuestowrite);
				}
				for (Iterator<String> it = tcm.keySet().iterator(); it.hasNext();)
				{
					String key = it.next();
					double dval = (tcm.get(key)).doubleValue();
					valuestowrite[varg.length+1]=key;
					valuestowrite[varg.length+2]="0";
					valuestowrite[varg.length+3]=String.valueOf(dval);
					valuestowrite[varg.length+4]=String.valueOf(100*dval/refrespo);
					dw.write(valuestowrite);
				}
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
		parameters.add(new GetRequiredParameters(Keywords.var, "vars=all", true, 3114, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.vargroup, "vars=all", false, 655, dep, "", 2));
		dep = new String[0];
		parameters.add(new GetRequiredParameters(Keywords.where,"text", false, 2805,dep,"",2));
		parameters.add(new GetRequiredParameters("", "note", false, 2806, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.replace, "listsingle=530_NULL,531_"+Keywords.replaceall+",532_"+Keywords.replaceformat+",533_"+Keywords.replacemissing,false, 534, dep, "", 2));
		return parameters;
	}
	/**
	*Returns the group and the name of the procedure
	*/
	public String[] getstepinfo()
	{
		String[] retprocinfo=new String[2];
		retprocinfo[0]="3112";
		retprocinfo[1]="3165";
		return retprocinfo;
	}
}
class ItemAnswer
{
	TreeMap<String, Double> value;
	public ItemAnswer()
	{
		value=new TreeMap<String, Double>();
	}
	public void addAnswer(String tempval)
	{
		if (value.get(tempval.toUpperCase())==null)
		{
			value.put(tempval.toUpperCase(), new Double(1));
		}
		else
		{
			double tempans=(value.get(tempval.toUpperCase())).doubleValue();
			value.put(tempval.toUpperCase(), new Double(tempans+1));
		}
	}
	public TreeMap<String, Double> getresult()
	{
		return value;
	}
}
class MissingAnswer
{
	TreeMap<String, Double> value;
	public MissingAnswer()
	{
		value=new TreeMap<String, Double>();
	}
	public void addMissing(String tempval)
	{
		if (value.get(tempval.toUpperCase())==null)
		{
			value.put(tempval.toUpperCase(), new Double(1));
		}
		else
		{
			double tempans=(value.get(tempval.toUpperCase())).doubleValue();
			value.put(tempval.toUpperCase(), new Double(tempans+1));
		}
	}
	public TreeMap<String, Double> getresult()
	{
		return value;
	}
}