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

import ADaMSoft.algorithms.MaxEvaluator;
import ADaMSoft.algorithms.MeanEvaluator;
import ADaMSoft.algorithms.MinEvaluator;
import ADaMSoft.algorithms.NEvaluator;
import ADaMSoft.algorithms.STDEvaluator;
import ADaMSoft.algorithms.SumEvaluator;
import ADaMSoft.algorithms.VarGroupModalities;
import ADaMSoft.dataaccess.DataReader;
import ADaMSoft.dataaccess.DataWriter;
import ADaMSoft.dataaccess.DictionaryReader;
import ADaMSoft.keywords.Keywords;
import ADaMSoft.utilities.StepUtilities;
import ADaMSoft.utilities.GetRequiredParameters;
import ADaMSoft.utilities.ValuesParser;
import ADaMSoft.utilities.VariableUtilities;
import ADaMSoft.utilities.DataSetUtilities;
import ADaMSoft.utilities.ObjectTransformer;
import ADaMSoft.utilities.SortRequestedVar;

/**
* This is the procedure that transform several numerical variables
* @author marco.scarno@gmail.com
* @date 14/02/2017
*/
public class ProcVartransform extends ObjectTransformer implements RunStep
{
	/**
	* Starts the execution of Proc vartransform and returns the corresponding message
	*/
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		boolean novgconvert=false;
		boolean samplevariance=false;
		String [] requiredparameters=new String[] {Keywords.OUT.toLowerCase(), Keywords.dict, Keywords.var, Keywords.transform};
		String [] optionalparameters=new String[] {Keywords.vargroup, Keywords.where, Keywords.weight, Keywords.replace, Keywords.keepallvars, Keywords.replacevars, Keywords.samplevariance, Keywords.novgconvert};
		Keywords.percentage_total=0;
		Keywords.percentage_done=0;
		StepUtilities steputilities=new StepUtilities();
		if (!steputilities.checkParameters(requiredparameters, optionalparameters, parameters))
		{
			return new Result(steputilities.getMessage(), false, null);
		}
		samplevariance =(parameters.get(Keywords.samplevariance)!=null);
		boolean keepallvars=(parameters.get(Keywords.keepallvars)!=null);
		boolean replacevars=(parameters.get(Keywords.replacevars)!=null);
		String transform=(String)parameters.get(Keywords.transform.toLowerCase());
		if (transform==null)
			return new Result("%637%\n", false, null);
		String[] transformation=new String[] {Keywords.devfrommean, Keywords.absdevfrommean, Keywords.squaredevfrommean,
		Keywords.standardize, Keywords.divformax, Keywords.normalize01, Keywords.meannormalize, Keywords.sumnormalize,
		Keywords.sumpctnormalize, Keywords.devfrommeandivradqn, Keywords.highlow, Keywords.highmediumlow, Keywords.highermediumlower};
		int selectedoption=steputilities.CheckOption(transformation, transform);
		if (selectedoption==0)
			return new Result("%1775% "+Keywords.transform.toUpperCase()+"<br>\n"+steputilities.getMessage(), false, null);
		novgconvert =(parameters.get(Keywords.novgconvert)!=null);

		DataWriter dw=new DataWriter(parameters, Keywords.OUT.toLowerCase());
		if (!dw.getmessage().equals(""))
			return new Result(dw.getmessage(), false, null);

		DictionaryReader dict = (DictionaryReader)parameters.get(Keywords.dict);

		String vartemp=(String)parameters.get(Keywords.var.toLowerCase());
		String vargroup=(String)parameters.get(Keywords.vargroup.toLowerCase());
		String weight=(String)parameters.get(Keywords.weight.toLowerCase());

		VariableUtilities varu=new VariableUtilities(dict, vargroup, vartemp, weight, null, null);
		if (varu.geterror())
			return new Result(varu.getmessage(), false, null);

		String[] var=varu.getanalysisvar();
		String[] varg=varu.getgroupvar();
		String[] totalvar=varu.getallvar();

		var=SortRequestedVar.getreqsorted(var, totalvar);

		String replace=(String)parameters.get(Keywords.replace);

		int[] replacerule=varu.getreplaceruleforall(replace);

		String keyword="Vartransform "+dict.getkeyword();
		String description="Vartransform "+dict.getdescription();

		String author=(String) parameters.get(Keywords.client_host.toLowerCase());

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

		MeanEvaluator emean=new MeanEvaluator();
		STDEvaluator estd=new STDEvaluator(samplevariance);
		SumEvaluator esum=new SumEvaluator();
		MaxEvaluator emax=new MaxEvaluator();
		MinEvaluator emin=new MinEvaluator();
		NEvaluator en=new NEvaluator();
		VarGroupModalities vgm=new VarGroupModalities();

		int validgroup=0;
		String[] values = null;
		Vector<String> vargroupvalues=null;
		double[] varvalues=null;
		double weightvalue=Double.NaN;
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
				weightvalue=vp.getweight(values);
				if (vp.vargroupisnotmissing(vargroupvalues))
				{
					validgroup++;
					vgm.updateModalities(vargroupvalues);
					estd.setValue(vargroupvalues, varvalues, weightvalue);
					emean.setValue(vargroupvalues, varvalues, weightvalue);
					esum.setValue(vargroupvalues, varvalues, weightvalue);
					emax.setValue(vargroupvalues, varvalues);
					emin.setValue(vargroupvalues, varvalues);
					en.setValue(vargroupvalues, varvalues, weightvalue);
				}
			}
		}
		data.close();
		if ((validgroup==0) && (where!=null))
			return new Result("%2804%<br>\n", false, null);
		if ((validgroup==0) && (where==null))
			return new Result("%666%<br>\n", false, null);

		emean.calculate();
		estd.calculate();
		vgm.calculate();

		Hashtable<Vector<String>, double[]> mean=emean.getresult();
		Hashtable<Vector<String>, double[]> std=estd.getresult();
		Hashtable<Vector<String>, double[]> sum=esum.getresult();
		Hashtable<Vector<String>, double[]> max=emax.getresult();
		Hashtable<Vector<String>, double[]> min=emin.getresult();
		Hashtable<Vector<String>, double[]> n=en.getresult();

		Vector<Hashtable<String, String>> groupcodelabels=vgm.getgroupcodelabels();
		DataSetUtilities dsu=new DataSetUtilities();
		dsu.setreplace(replace);
		Hashtable<String, String> temph=new Hashtable<String, String>();
		Hashtable<String, String> tempmdh=new Hashtable<String, String>();

		if (selectedoption==11)
		{
			temph.put("0", "%1616%");
			temph.put("1", "%1618%");
		}
		if (selectedoption==12)
		{
			temph.put("0", "%1616%");
			temph.put("1", "%1617%");
			temph.put("2", "%1618%");
		}
		if (selectedoption==13)
		{
			temph.put("0", "%1616%");
			temph.put("1", "%1617%");
			temph.put("2", "%1618%");
		}

		if (!keepallvars)
		{
			for (int j=0; j<varg.length; j++)
			{
				dsu.addnewvarfromolddict(dict, varg[j], groupcodelabels.get(j), temph, varg[j]);
			}
			for (int j=0; j<var.length; j++)
			{
				if (!replacevars)
					dsu.addnewvarfromolddict(dict, var[j], temph, tempmdh, var[j]);
				else
					dsu.addnewvarfromolddict(dict, var[j], temph, tempmdh, "new"+var[j]);
			}
		}
		else
		{
			dsu.defineolddict(dict);
			for (int j=0; j<varg.length; j++)
			{
				dsu.addnewvartoolddict(varg[j], dict.getvarlabelfromname(varg[j]), dict.getvarformatfromname(varg[j]), groupcodelabels.get(j), temph);
			}
			for (int j=0; j<var.length; j++)
			{
				if (!replacevars)
					dsu.addnewvartoolddict("new"+var[j], "%994% "+dict.getvarlabelfromname(var[j]), dict.getvarformatfromname(var[j]), temph, tempmdh);
				else
					dsu.addnewvartoolddict(var[j], "%994% "+dict.getvarlabelfromname(var[j]), dict.getvarformatfromname(var[j]), temph, tempmdh);
			}
		}

		if (replace!=null)
		{
			if (replace.equalsIgnoreCase(Keywords.replaceall))
			{
				dsu.setempycodelabels();
				dsu.setempymissingdata();
			}
			else if (replace.equalsIgnoreCase(Keywords.replaceformat))
			{
				dsu.setempycodelabels();
			}
			else if (replace.equalsIgnoreCase(Keywords.replacemissing))
				dsu.setempymissingdata();
		}

		if (!dw.opendatatable(dsu.getfinalvarinfo()))
			return new Result(dw.getmessage(), false, null);

		int totalanalysysvars=var.length;
		int totalgroupvars=varg.length;
		String[] newvalues = new String[totalanalysysvars+totalgroupvars];
		String[] newgroupvalues = new String[totalgroupvars];

		data = new DataReader(dict);

		if (!data.open(totalvar, replacerule, false))
			return new Result(data.getmessage(), false, null);

		String[] oldvalues=null;
		String[] writevalues=null;
		while (!data.isLast())
		{
			oldvalues = data.getRecord();
			if (novgconvert)
				vargroupvalues=vp.getorigvargroup(oldvalues);
			else
				vargroupvalues=vp.getvargroup(oldvalues);
			varvalues=vp.getanalysisvarasdouble(oldvalues);
			for (int i=0; i<newvalues.length; i++)
			{
				newvalues[i]="";
			}
			for (int i=0; i<newgroupvalues.length; i++)
			{
				newgroupvalues[i]="";
			}
			weightvalue=vp.getweight(oldvalues);
			if (vp.vargroupisnotmissing(vargroupvalues))
			{
				for (int j=0; j<vargroupvalues.size(); j++)
				{
					String groupvalue=vargroupvalues.get(j);
					if (groupvalue!=null)
						newgroupvalues[j]=vgm.getcode(j, groupvalue);
				}
				if (selectedoption==1)
				{
					try
					{
						double[] temp=mean.get(vargroupvalues);
						for (int i=0; i<temp.length; i++)
						{
							if ((!Double.isNaN(weightvalue)) && (!Double.isNaN(varvalues[i])) && (!Double.isNaN(temp[i])))
								varvalues[i]=varvalues[i]-temp[i];
							else
								varvalues[i]=Double.NaN;
						}
					}
					catch (Exception e) {}
				}
				if (selectedoption==2)
				{
					try
					{
						double[] temp=mean.get(vargroupvalues);
						for (int i=0; i<temp.length; i++)
						{
							if ((!Double.isNaN(weightvalue)) && (!Double.isNaN(varvalues[i])) && (!Double.isNaN(temp[i])))
								varvalues[i]=Math.abs(varvalues[i]-temp[i]);
							else
								varvalues[i]=Double.NaN;
						}
					}
					catch (Exception e) {}
				}
				if (selectedoption==3)
				{
					try
					{
						double[] temp=mean.get(vargroupvalues);
						for (int i=0; i<temp.length; i++)
						{
							if ((!Double.isNaN(weightvalue)) && (!Double.isNaN(varvalues[i])) && (!Double.isNaN(temp[i])))
								varvalues[i]=(varvalues[i]-temp[i])*(varvalues[i]-temp[i]);
							else
								varvalues[i]=Double.NaN;
						}
					}
					catch (Exception e) {}
				}
				if (selectedoption==4)
				{
					try
					{
						double[] tempm=mean.get(vargroupvalues);
						double[] temps=std.get(vargroupvalues);
						for (int i=0; i<tempm.length; i++)
						{
							if ((!Double.isNaN(weightvalue)) && (!Double.isNaN(varvalues[i])) && (!Double.isNaN(tempm[i])) && (!Double.isNaN(temps[i])))
								varvalues[i]=(varvalues[i]-tempm[i])/temps[i];
							else
								varvalues[i]=Double.NaN;
						}
					}
					catch (Exception e) {}
				}
				if (selectedoption==5)
				{
					try
					{
						double[] tempm=max.get(vargroupvalues);
						for (int i=0; i<tempm.length; i++)
						{
							if ((!Double.isNaN(weightvalue)) && (!Double.isNaN(varvalues[i])) && (!Double.isNaN(tempm[i])))
								varvalues[i]=varvalues[i]/tempm[i];
							else
								varvalues[i]=Double.NaN;
						}
					}
					catch (Exception e) {}
				}
				if (selectedoption==6)
				{
					try
					{
						double[] tempmax=max.get(vargroupvalues);
						double[] tempmin=min.get(vargroupvalues);
						for (int i=0; i<tempmax.length; i++)
						{
							if ((!Double.isNaN(weightvalue)) && (!Double.isNaN(varvalues[i])) && (!Double.isNaN(tempmax[i])) && (!Double.isNaN(tempmin[i])))
								varvalues[i]=(varvalues[i]-tempmin[i])/(tempmax[i]-tempmin[i]);
							else
								varvalues[i]=Double.NaN;
						}
					}
					catch (Exception e) {}
				}
				if (selectedoption==7)
				{
					try
					{
						double[] temp=mean.get(vargroupvalues);
						for (int i=0; i<temp.length; i++)
						{
							if ((!Double.isNaN(weightvalue)) && (!Double.isNaN(varvalues[i])) && (!Double.isNaN(temp[i])))
								varvalues[i]=varvalues[i]/temp[i];
							else
								varvalues[i]=Double.NaN;
						}
					}
					catch (Exception e) {}
				}
				if (selectedoption==8)
				{
					try
					{
						double[] temp=sum.get(vargroupvalues);
						for (int i=0; i<temp.length; i++)
						{
							if ((!Double.isNaN(weightvalue)) && (!Double.isNaN(varvalues[i])) && (!Double.isNaN(temp[i])))
								varvalues[i]=varvalues[i]/temp[i];
							else
								varvalues[i]=Double.NaN;
						}
					}
					catch (Exception e) {}
				}
				if (selectedoption==9)
				{
					try
					{
						double[] temp=sum.get(vargroupvalues);
						for (int i=0; i<temp.length; i++)
						{
							if ((!Double.isNaN(weightvalue)) && (!Double.isNaN(varvalues[i])) && (!Double.isNaN(temp[i])))
								varvalues[i]=varvalues[i]*100/temp[i];
							else
								varvalues[i]=Double.NaN;
						}
					}
					catch (Exception e) {}
				}
				if (selectedoption==10)
				{
					try
					{
						double[] tempm=mean.get(vargroupvalues);
						double[] tempn=n.get(vargroupvalues);
						for (int i=0; i<tempm.length; i++)
						{
							if ((!Double.isNaN(weightvalue)) && (!Double.isNaN(varvalues[i])) && (!Double.isNaN(tempm[i])) && (!Double.isNaN(tempn[i])))
								varvalues[i]=(varvalues[i]-tempm[i])/Math.sqrt(tempn[i]);
							else
								varvalues[i]=Double.NaN;
						}
					}
					catch (Exception e) {}
				}
				if (selectedoption==11)
				{
					try
					{
						double[] tempm=mean.get(vargroupvalues);
						for (int i=0; i<tempm.length; i++)
						{
							if ((!Double.isNaN(weightvalue)) && (!Double.isNaN(varvalues[i])) && (!Double.isNaN(tempm[i])))
							{
								if (varvalues[i]>=tempm[i])
									varvalues[i]=1;
								else
									varvalues[i]=0;
							}
							else
								varvalues[i]=Double.NaN;
						}
					}
					catch (Exception e) {}
				}
				if (selectedoption==12)
				{
					try
					{
						double[] tempm=mean.get(vargroupvalues);
						double[] temps=std.get(vargroupvalues);
						for (int i=0; i<tempm.length; i++)
						{
							if ((!Double.isNaN(weightvalue)) && (!Double.isNaN(varvalues[i])) && (!Double.isNaN(tempm[i])) && (!Double.isNaN(temps[i])))
							{
								if (varvalues[i]>=(tempm[i]+temps[i]))
									varvalues[i]=2;
								else if (varvalues[i]<=(tempm[i]-temps[i]))
									varvalues[i]=0;
								else
									varvalues[i]=1;
							}
							else
								varvalues[i]=Double.NaN;
						}
					}
					catch (Exception e) {}
				}
				if (selectedoption==13)
				{
					try
					{
						double[] tempm=mean.get(vargroupvalues);
						double[] temps=std.get(vargroupvalues);
						for (int i=0; i<tempm.length; i++)
						{
							if ((!Double.isNaN(weightvalue)) && (!Double.isNaN(varvalues[i])) && (!Double.isNaN(tempm[i])) && (!Double.isNaN(temps[i])))
							{
								if (varvalues[i]>=(tempm[i]+2*temps[i]))
									varvalues[i]=2;
								else if (varvalues[i]<=(tempm[i]-2*temps[i]))
									varvalues[i]=0;
								else
									varvalues[i]=1;
							}
							else
								varvalues[i]=Double.NaN;
						}
					}
					catch (Exception e) {}
				}
			}
			for (int i=0; i<totalgroupvars; i++)
			{
				newvalues[i]=newgroupvalues[i];
			}
			for (int i=0; i<totalanalysysvars; i++)
			{
				newvalues[i+totalgroupvars]=double2String(varvalues[i]);
			}
			writevalues=dsu.getnewvalues(oldvalues, newvalues);
			dw.write(writevalues);
		}
		data.close();

		boolean resclose=dw.close();
		if (!resclose)
			return new Result(dw.getmessage(), false, null);
		Vector<Hashtable<String, String>> tablevariableinfo=dw.getVarInfo();
		Hashtable<String, String> datatableinfo=dw.getTableInfo();
		Vector<StepResult> result = new Vector<StepResult>();
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
		parameters.add(new GetRequiredParameters(Keywords.dict+"=", "dict", true, 541, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.OUT.toLowerCase()+"=", "setting=out", true, 440, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.viewout, "checkbox", false, 1612, dep, "", 1));
		dep = new String[1];
		dep[0]=Keywords.dict;
		parameters.add(new GetRequiredParameters(Keywords.var, "vars=all", true, 643, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.vargroup, "vars=all", false, 655, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.weight, "vars=all", false, 656, dep, "", 2));
		dep = new String[0];
		parameters.add(new GetRequiredParameters(Keywords.transform, "listsingle=644_"+Keywords.devfrommean+",645_"+Keywords.absdevfrommean+",646_"+Keywords.squaredevfrommean
		+",647_"+Keywords.standardize+",648_"+Keywords.divformax+",649_"+Keywords.normalize01
		+",650_"+Keywords.meannormalize+",651_"+Keywords.sumnormalize+",652_"+Keywords.sumpctnormalize
		+",653_"+Keywords.devfrommeandivradqn+",1613_"+Keywords.highlow+",1614_"+Keywords.highmediumlow+",1615_"+Keywords.highermediumlower,true, 654, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.replacevars,"checkbox",false,657, dep,"",2));
		parameters.add(new GetRequiredParameters(Keywords.keepallvars,"checkbox",false,658, dep,"",2));
		parameters.add(new GetRequiredParameters(Keywords.samplevariance, "checkbox", false, 861, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.where,"text", false, 2805,dep,"",2));
		parameters.add(new GetRequiredParameters("", "note", false, 2806, dep, "", 2));
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
		retprocinfo[0]="4163";
		retprocinfo[1]="659";
		return retprocinfo;
	}
}
