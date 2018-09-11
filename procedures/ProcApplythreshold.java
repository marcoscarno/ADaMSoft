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

import ADaMSoft.dataaccess.DictionaryReader;
import ADaMSoft.dataaccess.DataReader;
import ADaMSoft.dataaccess.DataWriter;

import ADaMSoft.utilities.GetRequiredParameters;
import ADaMSoft.utilities.DataSetUtilities;
import ADaMSoft.utilities.ObjectTransformer;
import ADaMSoft.utilities.SortRequestedVar;

import ADaMSoft.keywords.Keywords;

import java.util.Iterator;
import java.util.Hashtable;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Vector;
import ADaMSoft.utilities.StepUtilities;

/**
* This is the procedure that evaluates the values of the estimated classification using the already saved threshold
* @author marco.scarno@gmail.com
* @date 13/02/2017
*/
public class ProcApplythreshold extends ObjectTransformer implements RunStep
{
	/**
	* Starts the execution of Proc Applythreshold
	*/
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		boolean novgconvert=false;
		String [] requiredparameters=new String[] {Keywords.OUT.toLowerCase(), Keywords.dict, Keywords.dict+"t"};
		String [] optionalparameters=new String[] {Keywords.replace, Keywords.novgconvert, Keywords.where};
		Keywords.percentage_total=0;
		Keywords.percentage_done=0;
		StepUtilities steputilities=new StepUtilities();
		if (!steputilities.checkParameters(requiredparameters, optionalparameters, parameters))
		{
			return new Result(steputilities.getMessage(), false, null);
		}
		String replace =(String)parameters.get(Keywords.replace);
		Keywords.percentage_total=3;
		DataWriter dw=new DataWriter(parameters, Keywords.OUT.toLowerCase());
		if (!dw.getmessage().equals(""))
		{
			return new Result(dw.getmessage(), false, null);
		}

		DictionaryReader dict = (DictionaryReader)parameters.get(Keywords.dict);

		DictionaryReader dictt = (DictionaryReader)parameters.get(Keywords.dict+"t");
		novgconvert =(parameters.get(Keywords.novgconvert)!=null);
		String where=(String)parameters.get(Keywords.where.toLowerCase());

		int tottvars=dictt.gettotalvar();
		int[] rept=new int[tottvars];
		rept[tottvars-1]=0;
		rept[tottvars-2]=0;
		boolean eg=false;
		String[] vg=new String[0];
		if (tottvars>2)
		{
			vg=new String[tottvars-2];
			for (int i=0; i<(tottvars-2); i++)
			{
				rept[i]=1;
				try
				{
					vg[i]=(dictt.getvarname(i)).substring(2);
				}
				catch (Exception egg)
				{
					eg=true;
				}
			}
		}
		if (eg)
		{
			return new Result("%1858%<br>\n", false, null);
		}

		String[] vart={"v"+String.valueOf(tottvars-2)};
		int[] reptemp={0};

		DataReader datat = new DataReader(dictt);

		if (!datat.open(vart, reptemp, false))
		{
			Keywords.procedure_error=true;
			return new Result(datat.getmessage(), false, null);
		}

		HashSet<String> refvar=new HashSet<String>();
		while (!datat.isLast())
		{
			String[] values = datat.getRecord();
			refvar.add(values[0]);
		}
		datat.close();
		Keywords.percentage_done=1;

		String[] tvarx=new String[refvar.size()];
		Iterator<String> it=refvar.iterator();
		int pointer=0;
		while (it.hasNext())
		{
			tvarx[pointer]=it.next();
			pointer++;
		}
		String[] allvardict=new String[dict.gettotalvar()];
		for (int i=0; i<dict.gettotalvar(); i++)
		{
			allvardict[i]=dict.getvarname(i);
		}
		String tempvargroup=null;
		vart=new String[tottvars];
		if (vg.length!=0)
		{
			vg=SortRequestedVar.getreqsorted(vg,allvardict);
			tempvargroup="";
			for (int i=0; i<vg.length; i++)
			{
				tempvargroup=tempvargroup+vg[i]+" ";
				vart[i]=vg[i];
			}
			tempvargroup=tempvargroup.trim();

		}
		vart[tottvars-2]="v"+String.valueOf(tottvars-2);
		vart[tottvars-1]="v"+String.valueOf(tottvars-1);
		tvarx=SortRequestedVar.getreqsorted(tvarx,allvardict);
		String tempvarx="";
		for (int i=0; i<tvarx.length; i++)
		{
			tempvarx=tempvarx+tvarx[i]+" ";
		}
		tempvarx=tempvarx.trim();

		String keyword="Applythreshold "+dict.getkeyword();
		String description="Applythreshold "+dict.getdescription();
		String author=(String)parameters.get(Keywords.client_host.toLowerCase());

		Hashtable<Vector<String>, double[]> thresholdvalues=new Hashtable<Vector<String>, double[]>();
		if (!datat.open(vart, rept, false))
		{
			return new Result(datat.getmessage(), false, null);
		}

		double realnumvg=Double.NaN;
		String realvgval="";
		while (!datat.isLast())
		{
			String[] values = datat.getRecord();
			Vector<String> vgg=new Vector<String>();
			if (vg.length==0)
				vgg.add(null);
			else
			{
				realnumvg=Double.NaN;
				for (int i=0; i<vg.length; i++)
				{
					realvgval=values[i].trim();
					if (!novgconvert)
					{
						try
						{
							realnumvg=Double.parseDouble(realvgval);
							if (!Double.isNaN(realnumvg))
								realvgval=String.valueOf(realnumvg);
						}
						catch (Exception e) {}
					}
					vgg.add(realvgval);
				}
			}
			realvgval=values[vg.length].trim();
			if (thresholdvalues.get(vgg)==null)
			{
				double[] tt=new double[tvarx.length];
				for (int i=0; i<tvarx.length; i++)
				{
					tt[i]=Double.NaN;
				}
				thresholdvalues.put(vgg, tt);
			}
			double[] tempthre=thresholdvalues.get(vgg);
			for (int i=0; i<tvarx.length; i++)
			{
				if (tvarx[i].equalsIgnoreCase(realvgval))
				{
					try
					{
						tempthre[i]=Double.parseDouble(values[vg.length+1]);
					}
					catch (Exception egg) {}
				}
			}

		}
		datat.close();
		Keywords.percentage_done=2;

		DataReader data = new DataReader(dict);

		DataSetUtilities dsu=new DataSetUtilities();
		int tvar=dict.gettotalvar();
		int[] ordervg=new int[vg.length];
		int[] ordera=new int[tvarx.length];
		int[] replacevar=new int[tvar];
		int rifrep=0;
		if (replace==null)
			rifrep=0;
		else if (replace.equalsIgnoreCase(Keywords.replaceall))
			rifrep=1;
		else if (replace.equalsIgnoreCase(Keywords.replaceformat))
			rifrep=2;
		else if (replace.equalsIgnoreCase(Keywords.replacemissing))
			rifrep=3;
		for (int i=0; i<tvar; i++)
		{
			replacevar[i]=rifrep;
		}
		for (int i=0; i<vg.length; i++)
		{
			for (int j=0; j<tvar; j++)
			{
				if (vg[i].equalsIgnoreCase(dict.getvarname(j)))
				{
					ordervg[i]=j;
					replacevar[j]=1;
				}
			}
		}
		for (int i=0; i<tvarx.length; i++)
		{
			for (int j=0; j<tvar; j++)
			{
				if (tvarx[i].equalsIgnoreCase(dict.getvarname(j)))
					ordera[i]=j;
			}
		}
		dsu.setreplace(replace);
		Hashtable<String, String> temph=new Hashtable<String, String>();
		dsu.defineolddict(dict);
		for (int i=0; i<tvarx.length; i++)
		{
			dsu.addnewvartoolddict("pred_class_"+tvarx[i].trim(), "%1825% ("+dict.getvarlabelfromname(tvarx[i])+")", Keywords.NUMSuffix, temph, temph);
		}
		if (!dw.opendatatable(dsu.getfinalvarinfo()))
		{
			return new Result(dw.getmessage(), false, null);
		}

		boolean vgmiss=false;

		if (!data.open(null, replacevar, false))
		{
			return new Result(data.getmessage(), false, null);
		}
		if (where!=null)
		{
			if (!data.setcondition(where))
			{
				return new Result(data.getmessage(), false, null);
			}
		}
		realnumvg=Double.NaN;
		realvgval="";
		String[] values;
		int validgroup=0;
		while (!data.isLast())
		{
			vgmiss=false;
			values = data.getRecord();
			if (values!=null)
			{
				validgroup++;
				Vector<String> vgg=new Vector<String>();
				if (vg.length==0)
					vgg.add(null);
				else
				{
					realnumvg=Double.NaN;
					for (int i=0; i<ordervg.length; i++)
					{
						realvgval=values[ordervg[i]].trim();
						if (!novgconvert)
						{
							try
							{
								realnumvg=Double.parseDouble(realvgval);
								if (!Double.isNaN(realnumvg))
									realvgval=String.valueOf(realnumvg);
							}
							catch (Exception e) {}
						}
						if (realvgval.equals(""))
						vgmiss=true;
						vgg.add(realvgval);
					}
				}
				String[] newvalues=new String[tvarx.length];
				for (int i=0; i<tvarx.length; i++)
				{
					newvalues[i]="";
				}
				if (!vgmiss)
				{
					double[] thresholdval=thresholdvalues.get(vgg);
					for (int i=0; i<tvarx.length; i++)
					{
						double aval=Double.NaN;
						try
						{
							aval=Double.parseDouble(values[ordera[i]]);
						}
						catch (Exception e) {}
						if (!Double.isNaN(aval))
						{
							if (aval>thresholdval[i])
								newvalues[i]="1";
							else
								newvalues[i]="0";
						}
					}
				}
				String[] wvalues=dsu.getnewvalues(values, newvalues);
				dw.write(wvalues);
			}
		}
		data.close();
		Keywords.percentage_done=0;
		Keywords.percentage_total=0;
		if (validgroup==0)
		{
			dw.deletetmp();
			return new Result("%2807%<br>\n", false, null);
		}
		Vector<StepResult> result = new Vector<StepResult>();
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
		parameters.add(new GetRequiredParameters(Keywords.dict+"t=", "dict", true, 1856, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.dict+"=", "dict", true, 1857, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.OUT.toLowerCase()+"=", "setting=out", true, 1823, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.viewout, "checkbox", false, 1612, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.where,"text", false, 2808,dep,"",2));
		parameters.add(new GetRequiredParameters("", "note", false, 2806, dep, "", 2)); 		parameters.add(new GetRequiredParameters(Keywords.novgconvert, "checkbox", false, 2227, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.replace, "listsingle=530_NULL,531_"+Keywords.replaceall+",532_"+Keywords.replaceformat+",533_"+Keywords.replacemissing,false, 534, dep, "", 2));
		return parameters;
	}
	/**
	*Returns the group and the name of the procedure
	*/
	public String[] getstepinfo()
	{
		String[] retprocinfo=new String[2];
		retprocinfo[0]="1806";
		retprocinfo[1]="1855";
		return retprocinfo;
	}
}
