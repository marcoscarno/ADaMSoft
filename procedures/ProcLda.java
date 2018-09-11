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
import ADaMSoft.algorithms.LdaEvaluator;
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
* This is the procedure that implements a linear discriminant analysis (according to Fisher theory)
* @author marco.scarno@gmail.com
* @date 14/02/2017
*/
public class ProcLda extends ObjectTransformer implements RunStep
{
	/**
	* Starts the execution of Proc Lda
	*/
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		Vector<StepResult> result = new Vector<StepResult>();
		boolean novgconvert=false;
		boolean noclforvg=false;
		boolean orderclbycode=false;
		String [] requiredparameters=new String[] {Keywords.OUT.toLowerCase(), Keywords.dict, Keywords.varx, Keywords.vary};
		String [] optionalparameters=new String[] {Keywords.vargroup, Keywords.where, Keywords.weight, Keywords.replace, Keywords.novgconvert, Keywords.noclforvg, Keywords.orderclbycode, Keywords.mainvalue};
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

		DictionaryReader dict = (DictionaryReader)parameters.get(Keywords.dict);
		String tempvarx=(String)parameters.get(Keywords.varx.toLowerCase());
		String tempvary=(String)parameters.get(Keywords.vary.toLowerCase());
		String tempvargroup=(String)parameters.get(Keywords.vargroup.toLowerCase());
		String weight=(String)parameters.get(Keywords.weight.toLowerCase());

		String[] testdepvar=tempvary.split(" ");
		if (testdepvar.length>1)
			return new Result("%1125%<br>\n", false, null);

		VariableUtilities varu=new VariableUtilities(dict, tempvargroup, null, weight, tempvarx, tempvary);
		if (varu.geterror())
			return new Result(varu.getmessage(), false, null);

		novgconvert =(parameters.get(Keywords.novgconvert)!=null);
		orderclbycode =(parameters.get(Keywords.orderclbycode)!=null);
		noclforvg =(parameters.get(Keywords.noclforvg)!=null);

		String mainvalue=(String)parameters.get(Keywords.mainvalue.toLowerCase());

		String keyword="LDA "+dict.getkeyword();
		String description="LDA"+dict.getdescription();
		String author=(String)parameters.get(Keywords.client_host.toLowerCase());

		String[] varx=varu.getrowvar();
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

		if (!data.open(reqvar, replacerule, false))
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

		LdaEvaluator lda=new LdaEvaluator(varx.length);

		int validgroup=0;
		String[] values=null;
		Vector<String> vargroupvalues=new Vector<String>();
		double[] varxvalues=null;
		String[] varyvalues=null;
		double weightvalue=1;
		Hashtable<String, Integer> testclass=new Hashtable<String, Integer>();
		boolean ismissing=false;
		while (!data.isLast())
		{
			values = data.getRecord();
			if (values!=null)
			{
				if (novgconvert)
					vargroupvalues=vp.getorigvargroup(values);
				else
					vargroupvalues=vp.getvargroup(values);
				varxvalues=vp.getrowvarasdouble(values);
				varyvalues=vp.getcolvar(values);
				if (mainvalue!=null)
				{
					if (varyvalues[0].equals(mainvalue)) varyvalues[0]=mainvalue;
					else
					{
						if (!varyvalues[0].equals("")) varyvalues[0]="other";
					}
				}
				weightvalue=vp.getweight(values);
				ismissing=false;
				for (int i=0; i<varxvalues.length; i++)
				{
					if (Double.isNaN(varxvalues[i]))
						ismissing=true;
				}
				if ((vp.vargroupisnotmissing(vargroupvalues)) && (!varyvalues[0].equals("")) && (!Double.isNaN(weightvalue)) && (!ismissing))
				{
					lda.addvalue(vargroupvalues, varyvalues, varxvalues, weightvalue);
					if (testclass.get(varyvalues[0])==null)
						testclass.put(varyvalues[0], new Integer(testclass.size()));
					validgroup++;
					vgm.updateModalities(vargroupvalues);
				}
			}
		}
		data.close();
		if ((validgroup==0) && (where!=null))
			return new Result("%2804%<br>\n", false, null);
		if ((validgroup==0) && (where==null))
			return new Result("%666%<br>\n", false, null);
		if (testclass.size()!=2)
			return new Result("%1933%<br>\n", false, null);

		vgm.calculate();
		lda.calculate();

		Hashtable<Vector<String>, double[]> coeff=lda.getcoeff();
		Hashtable<Vector<String>, String> errormsg=lda.geterrormsg();
		Hashtable<Vector<String>, Double> eigenind=lda.getIndexOfEigen();

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
		for (int j=0; j<varx.length; j++)
		{
			clvar.put(varx[j], dict.getvarlabelfromname(varx[j]));
		}
		dsu.addnewvar("varx", "%1109%", Keywords.TEXTSuffix, clvar, tempmd);
		dsu.addnewvar("value", "%1110%", Keywords.NUMSuffix, tempmd, tempmd);

		if (!dw.opendatatable(dsu.getfinalvarinfo()))
			return new Result(dw.getmessage(), false, null);

		if (totalgroupmodalities==0)
			totalgroupmodalities=1;

		String actualgroupref="";
		String messageind="%2912%\n";
		boolean isvg=false;
		double indexval=0;
		for (int i=0; i<totalgroupmodalities; i++)
		{
			actualgroupref="";
			String[] valuestowrite=new String[varg.length+2];
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
					isvg=true;
				}
			}
			indexval=(eigenind.get(rifmodgroup)).doubleValue();
			if (!Double.isNaN(indexval))
			{
				if (indexval>100) indexval=100;
				if (isvg)
					messageind=messageind+String.valueOf(indexval)+" (%2913%: "+actualgroupref+")\n";
				else
					messageind=messageind+"%2914%: "+String.valueOf(indexval)+"\n";
			}
			else
			{
				if (isvg)
					messageind=messageind+"- (%2913%: "+actualgroupref+")\n";
				else
					messageind=messageind+"%2914%: -"+"\n";
			}
			actualgroupref=actualgroupref.trim();
			double[] tempcoeff=coeff.get(rifmodgroup);
			String temperrormsg=errormsg.get(rifmodgroup);
			if (!temperrormsg.equals(""))
			{
				result.add(new LocalMessageGetter(temperrormsg+" ("+actualgroupref+")\n"));
			}
			for (int j=0; j<tempcoeff.length; j++)
			{
				valuestowrite[varg.length]=varx[j];
				valuestowrite[varg.length+1]=double2String(tempcoeff[j]);
				dw.write(valuestowrite);
			}
		}
		result.add(new LocalMessageGetter(messageind));


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
		parameters.add(new GetRequiredParameters(Keywords.OUT.toLowerCase()+"=", "setting=out", true, 890, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.viewout, "checkbox", false, 1612, dep, "", 1));
		dep = new String[1];
		dep[0]=Keywords.dict;
		parameters.add(new GetRequiredParameters(Keywords.varx, "vars=all", true, 888, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.vary, "var=all", true, 1932, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.mainvalue, "text", false, 3440, dep, "", 2));
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
		retprocinfo[0]="1929";
		retprocinfo[1]="1930";
		return retprocinfo;
	}
}
