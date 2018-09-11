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
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.Vector;

import ADaMSoft.dataaccess.DataReader;
import ADaMSoft.dataaccess.DataWriter;
import ADaMSoft.dataaccess.DictionaryReader;
import ADaMSoft.dataaccess.Matrix2DFile;
import ADaMSoft.keywords.Keywords;
import ADaMSoft.utilities.GetRequiredParameters;
import ADaMSoft.utilities.ValuesParser;
import ADaMSoft.utilities.VariableUtilities;
import ADaMSoft.utilities.DataSetUtilities;
import ADaMSoft.utilities.SortRequestedVar;
import ADaMSoft.utilities.ObjectTransformer;
import ADaMSoft.algorithms.IpfEvaluator;
import ADaMSoft.utilities.StepUtilities;


/**
* This is the procedure that estimates the weight for a observation according to the iterative proportional fitting
* @author marco.scarno@gmail.com
* @date 14/02/2017
*/
public class ProcIpf extends ObjectTransformer implements RunStep
{
	boolean nocheckvalues;
	Hashtable<String, Hashtable<String, Double>> freqv;
	/**
	* Starts the execution of Proc Ipf
	*/
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		boolean todisk=false;
		boolean novgconvert=false;
		int toistvw=-1;
		String [] requiredparameters=new String[] {Keywords.OUT.toLowerCase(), Keywords.dict, Keywords.dict+"w",
		Keywords.varname, Keywords.varval, Keywords.varfreq, Keywords.iterations};
		String [] optionalparameters=new String[] {Keywords.vargroup, Keywords.accuracy, Keywords.variniweight,
		Keywords.replace, Keywords.weightname, Keywords.weightmax, Keywords.weightmin, Keywords.novgconvert, Keywords.nocheckvalues, Keywords.where};
		Keywords.percentage_total=0;
		Keywords.percentage_done=0;
		StepUtilities steputilities=new StepUtilities();
		if (!steputilities.checkParameters(requiredparameters, optionalparameters, parameters))
		{
			return new Result(steputilities.getMessage(), false, null);
		}
		novgconvert =(parameters.get(Keywords.novgconvert)!=null);
		nocheckvalues=(parameters.get(Keywords.nocheckvalues)!=null);
		todisk =(parameters.get(Keywords.todisk)!=null);

		DataWriter dw=new DataWriter(parameters, Keywords.OUT.toLowerCase());
		if (!dw.getmessage().equals(""))
			return new Result(dw.getmessage(), false, null);

		DictionaryReader dictw = (DictionaryReader)parameters.get(Keywords.dict+"w");
		DictionaryReader dict = (DictionaryReader)parameters.get(Keywords.dict);

		String accuracy=(String)parameters.get(Keywords.accuracy.toLowerCase());
		String weightname=(String)parameters.get(Keywords.weightname.toLowerCase());
		String weightmax=(String)parameters.get(Keywords.weightmax.toLowerCase());
		String weightmin=(String)parameters.get(Keywords.weightmin.toLowerCase());
		if (weightname==null)
			weightname="weight";
		weightname=weightname.trim();
		String[] tw=weightname.split(" ");
		if (tw.length>1)
			return new Result("%1292%<br>\n", false, null);

		String iter=(String)parameters.get(Keywords.iterations.toLowerCase());
		double ac=0.001;
		if (accuracy!=null)
		{
			ac=string2double(accuracy);
			if (ac==0)
				return new Result("%1291%<br>\n", false, null);
		}

		double wmax=Double.NaN;
		if (weightmax!=null)
		{
			wmax=string2double(weightmax);
			if (wmax==0)
				return new Result("%1296%<br>\n", false, null);
		}
		double wmin=Double.NaN;
		if (weightmin!=null)
		{
			wmin=string2double(weightmin);
			if (wmin==0)
				return new Result("%1297%<br>\n", false, null);
		}

		if (!Double.isNaN(wmax) && !Double.isNaN(wmin))
		{
			if (wmax<wmin)
				return new Result("%4024%<br>\n", false, null);
		}

		int niter=string2int(iter);

		String varname=(String)parameters.get(Keywords.varname.toLowerCase());
		String varval=(String)parameters.get(Keywords.varval.toLowerCase());
		String varfreq=(String)parameters.get(Keywords.varfreq.toLowerCase());
		String vargroup=(String)parameters.get(Keywords.vargroup.toLowerCase());
		String[] tvn=varname.split(" ");
		String[] tvl=varval.split(" ");
		String[] tvf=varfreq.split(" ");
		if (tvn.length!=1)
			return new Result("%1276%<br>\n", false, null);
		if (tvl.length!=1)
			return new Result("%1277%<br>\n", false, null);
		if (tvf.length!=1)
			return new Result("%1278%<br>\n", false, null);

		String vartemp=varname+" "+varval+" "+varfreq;

		VariableUtilities varw=new VariableUtilities(dictw, null, vartemp, null, null, null);
		if (varw.geterror())
			return new Result(varw.getmessage(), false, null);

		String[] vartoread=varw.getreqvar();

		String replace=(String)parameters.get(Keywords.replace);

		int[] replacerulew=varw.getreplaceruleforsel(replace);

		int[] ra=varw.getanalysisruleforsel();

		DataReader dataw = new DataReader(dictw);

		if (!dataw.open(vartoread, replacerulew, false))
			return new Result(dataw.getmessage(), false, null);

		ValuesParser vpw=new ValuesParser(null, null, ra, null, null, null);

		freqv=new Hashtable<String, Hashtable<String, Double>>();
		Hashtable<String, String> vartouseinw=new Hashtable<String, String>();

		boolean repvar=false;
		if (replace==null)
			repvar=false;
		else if (replace.equalsIgnoreCase(Keywords.replaceall))
			repvar=true;
		else if (replace.equalsIgnoreCase(Keywords.replaceformat))
			repvar=true;

		int validgroup=0;
		while (!dataw.isLast())
		{
			String[] values = dataw.getRecord();
			String[] varvalues=vpw.getanalysisvar(values);
			String varn=(varvalues[0].toLowerCase()).trim();
			String varl=varvalues[1].trim();
			double fr=Double.NaN;
			try
			{
				fr=Double.parseDouble(varvalues[2].trim());
			}
			catch (Exception e) {}
			if (!Double.isNaN(fr))
			{
				if (!repvar)
					vartouseinw.put(varn,"");
				validgroup++;
				Hashtable<String, Double> ft=freqv.get(varn);
				if (ft==null)
				{
					ft=new Hashtable<String, Double>();
					ft.put(varl, new Double(fr));
				}
				else
					ft.put(varl, new Double(fr));
				freqv.put(varn, ft);
			}
		}
		dataw.close();
		if (validgroup==0)
			return new Result("%1279%<br>\n", false, null);

		if (!repvar)
		{
			for (int i=0; i<replacerulew.length; i++)
			{
				replacerulew[i]=0;
			}
			if (!dataw.open(vartoread, replacerulew, false))
				return new Result(dataw.getmessage(), false, null);
			while (!dataw.isLast())
			{
				String[] values = dataw.getRecord();
				String[] varvalues=vpw.getanalysisvar(values);
				String varn=(varvalues[0].toLowerCase()).trim();
				double fr=Double.NaN;
				try
				{
					fr=Double.parseDouble(varvalues[2].trim());
				}
				catch (Exception e) {}
				if (!Double.isNaN(fr))
				{
					vartouseinw.put(varn,"");
				}
			}
			dataw.close();
		}

		String newvarname="";
		for (Enumeration<String> e = vartouseinw.keys() ; e.hasMoreElements() ;)
		{
			String par =e.nextElement();
			newvarname=newvarname+par+" ";
		}
		newvarname=newvarname.trim();
		String iniw=(String)parameters.get(Keywords.variniweight.toLowerCase());

		VariableUtilities varu=new VariableUtilities(dict, vargroup, newvarname, iniw, null, null);
		if (varu.geterror())
			return new Result(varu.getmessage(), false, null);

		String[] var=varu.getanalysisvar();
		String[] totalvar=varu.getallvar();

		var=SortRequestedVar.getreqsorted(var, totalvar);

		int[] replacerule=varu.getreplaceruleforall(replace);

		DataReader data = new DataReader(dict);

		if (!data.open(totalvar, replacerule, false))
			return new Result(data.getmessage(), false, null);
		String where=(String)parameters.get(Keywords.where.toLowerCase());
		if (where!=null)
		{
			if (!data.setcondition(where)) return new Result(data.getmessage(), false, null);
		}

		int[] allvarstype=varu.getnormalruleforall();

		ValuesParser vp=new ValuesParser(allvarstype, null, null, null, null, null);

		String tempdir=(String)parameters.get(Keywords.WorkDir);
		Matrix2DFile weightsvalues=null;
		Vector<Double> vweightsvalues=new Vector<Double>();
		if (todisk)
			weightsvalues=new Matrix2DFile(tempdir, 1);

		int pointer=0;

		String[] values=null;
		Vector<String> vargroupvalues=new Vector<String>();
		double weightvalue=Double.NaN;
		double[] tew=new double[1];
		validgroup=0;
		while (!data.isLast())
		{
			values = data.getRecord();
			if (values!=null)
			{
				validgroup++;
				if (novgconvert)
					vargroupvalues=vp.getorigvargroup(values);
				else
					vargroupvalues=vp.getvargroup(values);
				weightvalue=Double.NaN;
				if (vp.vargroupisnotmissing(vargroupvalues))
					weightvalue=vp.getweight(values);
				tew[0]=weightvalue;
				if (todisk)
					weightsvalues.write(tew);
				else
					vweightsvalues.add(weightvalue);
			}
		}
		data.close();
		if ((validgroup==0) && (where!=null))
		{
			if (todisk) weightsvalues.close();
			return new Result("%2804%<br>\n", false, null);
		}
		if ((validgroup==0) && (where==null))
		{
			if (todisk) weightsvalues.close();
			return new Result("%666%<br>\n", false, null);
		}

		IpfEvaluator we=new IpfEvaluator(freqv);
		double[] dweightsvalues=new double[0];
		if (!todisk)
		{
			dweightsvalues=new double[vweightsvalues.size()];
			for (int i=0; i<vweightsvalues.size(); i++)
			{
				dweightsvalues[i]=(vweightsvalues.get(i)).doubleValue();
			}
			vweightsvalues.clear();
		}

		int realiter=0;
		boolean errorweights=false;
		double mer=0;
		Keywords.percentage_total=niter;
		for (int n=0; n<niter; n++)
		{
			Keywords.percentage_done=n;
			mer=0;
			for (int i=0; i<var.length; i++)
			{
				Hashtable<Vector<String>, Hashtable<String, Double>> tempfreqr=new Hashtable<Vector<String>, Hashtable<String, Double>>();
				if (!data.open(totalvar, replacerule, false))
					return new Result(data.getmessage(), false, null);
				if (where!=null)
				{
					if (!data.setcondition(where)) return new Result(data.getmessage(), false, null);
				}
				pointer=0;
				String[] varvalues=null;
				while (!data.isLast())
				{
					values = data.getRecord();
					if (values!=null)
					{
						if (novgconvert)
							vargroupvalues=vp.getorigvargroup(values);
						else
							vargroupvalues=vp.getvargroup(values);
						varvalues=vp.getanalysisvar(values);
						if (todisk)
							weightvalue=weightsvalues.read(pointer, 0);
						else
							weightvalue=dweightsvalues[pointer];
						if (vp.vargroupisnotmissing(vargroupvalues))
						{
							Hashtable<String, Double> tempfo=tempfreqr.get(vargroupvalues);
							if (tempfo==null)
							{
								tempfo=new Hashtable<String, Double>();
								tempfo.put(varvalues[i], new Double(weightvalue));
							}
							else
							{
								if (tempfo.get(varvalues[i])==null)
									tempfo.put(varvalues[i], new Double(weightvalue));
								else
								{
									double temp=(tempfo.get(varvalues[i])).doubleValue();
									tempfo.put(varvalues[i], new Double(weightvalue+temp));
								}
							}
							tempfreqr.put(vargroupvalues, tempfo);
						}
						pointer++;
					}
				}
				data.close();
				boolean test=we.evaluator(tempfreqr, var[i], nocheckvalues);
				if (!test)
				{
					if (todisk)
						weightsvalues.close();
					return new Result("%1293% ("+var[i]+"="+we.getnotexistentval()+")<br>\n", false, null);
				}
				Hashtable<Vector<String>, Hashtable<String, Double>> realw=we.getfactor();
				mer=mer+we.getmeanerror();
				pointer=0;
				if (!data.open(totalvar, replacerule, false))
					return new Result(data.getmessage(), false, null);
				if (where!=null)
				{
					if (!data.setcondition(where)) return new Result(data.getmessage(), false, null);
				}
				while (!data.isLast())
				{
					values = data.getRecord();
					if (values!=null)
					{
						if (novgconvert)
							vargroupvalues=vp.getorigvargroup(values);
						else
							vargroupvalues=vp.getvargroup(values);
						varvalues=vp.getanalysisvar(values);
						if (todisk)
							weightvalue=weightsvalues.read(pointer, 0);
						else
							weightvalue=dweightsvalues[pointer];
						if (vp.vargroupisnotmissing(vargroupvalues))
						{
							if (realw.get(vargroupvalues)!=null)
							{
								Hashtable<String, Double> tempfo=realw.get(vargroupvalues);
								if (tempfo.get(varvalues[i])!=null)
								{
									double temp=(tempfo.get(varvalues[i])).doubleValue();
									weightvalue=temp*weightvalue;
								}
								if (!Double.isNaN(wmax))
								{
									if (weightvalue>wmax)
										weightvalue=wmax;
								}
								if (!Double.isNaN(wmin))
								{
									if (weightvalue<wmin)
										weightvalue=wmin;
								}
								tempfreqr.put(vargroupvalues, tempfo);
								if (Double.isNaN(weightvalue))
									errorweights=true;
							}
						}
						if (todisk)
							weightsvalues.write(weightvalue, pointer, 0);
						else
							dweightsvalues[pointer]=weightvalue;
						pointer++;
					}
				}
				data.close();
				tempfreqr.clear();
				tempfreqr=null;
				realw.clear();
				realw=null;
			}
			mer=mer/(var.length);
			if (mer<ac)
				break;
			realiter++;
			if (errorweights)
				break;
		}
		if (errorweights)
			return new Result("%1299%<br>\n", false, null);

		Vector<StepResult> result = new Vector<StepResult>();
		String riter=String.valueOf(realiter);
		result.add(new LocalMessageGetter("%1298% " +riter+"<br>\n"));
		result.add(new LocalMessageGetter("%2413%: " +String.valueOf(mer)+"<br>\n"));

		String keyword="Ipf "+dict.getkeyword();
		String description="Ipf "+dict.getdescription();
		String author=(String) parameters.get(Keywords.client_host.toLowerCase());

		DataSetUtilities dsu=new DataSetUtilities();

		dsu.setreplace(replace);

		dsu.defineolddict(dict);

		for (int i=0; i<dict.gettotalvar(); i++)
		{
			if (weightname.equalsIgnoreCase(dict.getvarname(i)))
				toistvw=i;
		}
		if (toistvw>=0)
			result.add(new LocalMessageGetter("%2673%<br>\n"));
		else
		{
			Hashtable<String, String> temph=new Hashtable<String, String>();
			dsu.addnewvartoolddict(weightname, weightname, Keywords.NUMSuffix, temph, temph);
		}

		if (replace!=null)
		{
			if (replace.equalsIgnoreCase(Keywords.replaceall))
			{
				dsu.setempycodelabels();
				dsu.setempymissingdata();
			}
			else if (replace.equalsIgnoreCase(Keywords.replaceformat))
				dsu.setempycodelabels();
			else if (replace.equalsIgnoreCase(Keywords.replacemissing))
				dsu.setempymissingdata();
		}

		if (!dw.opendatatable(dsu.getfinalvarinfo()))
			return new Result(dw.getmessage(), false, null);

		pointer=0;
		if (!data.open(totalvar, replacerule, false))
			return new Result(data.getmessage(), false, null);
		String[] newv=null;
		if (toistvw>=0)
			newv=new String[values.length];
		else
			newv=new String[values.length+1];
		while (!data.isLast())
		{
			values = data.getRecord();
			if (todisk)
				weightvalue=weightsvalues.read(pointer, 0);
			else
				weightvalue=dweightsvalues[pointer];
			for (int i=0; i<values.length; i++)
			{
				newv[i]=values[i];
			}
			if (toistvw>=0)
				newv[toistvw]=double2String(weightvalue);
			else
				newv[values.length]=double2String(weightvalue);
			dw.write(newv);
			pointer++;
		}
		data.close();
		if (todisk)
			weightsvalues.close();
		else
			dweightsvalues=new double[0];

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
		parameters.add(new GetRequiredParameters(Keywords.dict+"=", "dict", true, 1281, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.dict+"w=", "dict", true, 1282, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.OUT.toLowerCase()+"=", "setting=out", true, 1283, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.viewout, "checkbox", false, 1612, dep, "", 1));
		dep = new String[1];
		dep[0]=Keywords.dict+"w";
		parameters.add(new GetRequiredParameters(Keywords.varname, "vars=all", true, 1284, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.varval, "vars=all", true, 1285, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.varfreq, "vars=all", true, 1286, dep, "", 2));
		String[] ndep = new String[1];
		ndep[0]=Keywords.dict;
		parameters.add(new GetRequiredParameters(Keywords.vargroup, "vars=all", false, 655, ndep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.variniweight, "vars=all", false, 1287, ndep, "", 2));
		dep = new String[0];
		ndep = new String[0];
		parameters.add(new GetRequiredParameters(Keywords.where,"text", false, 2809,dep,"",2));
		parameters.add(new GetRequiredParameters("", "note", false, 2806, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.iterations, "text", true, 1288, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.accuracy, "text", false, 1289, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.weightname, "text", false, 1290, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.weightmax, "text", false, 1294, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.weightmin, "text", false, 1295, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.todisk, "checkbox", false, 1088, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.nocheckvalues, "checkbox", false, 4022, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.novgconvert, "checkbox", false, 2227, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.replace, "listsingle=530_NULL,531_"+Keywords.replaceall+",532_"+Keywords.replaceformat+",533_"+Keywords.replacemissing,false, 534, dep, "", 2));
		return parameters;
	}
	/**
	*Returns the group and the name of the procedure
	*/
	public String[] getstepinfo()
	{
		String[] retprocinfo=new String[2];
		retprocinfo[0]="1560";
		retprocinfo[1]="1280";
		return retprocinfo;
	}
}
