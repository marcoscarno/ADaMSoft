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

import ADaMSoft.utilities.StepUtilities;
import ADaMSoft.utilities.GetRequiredParameters;
import ADaMSoft.utilities.VariableUtilities;
import ADaMSoft.utilities.DataSetUtilities;
import ADaMSoft.utilities.ObjectTransformer;
import ADaMSoft.utilities.ValuesParser;

import ADaMSoft.keywords.Keywords;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Vector;

/**
* This is the procedure that evaluate the score for each respondent by considering the answers for a series of items
* @author marco.scarno@gmail.com
* @date 14/02/2017
*/
public class ProcRespondentscore extends ObjectTransformer implements RunStep
{
	/**
	* Starts the execution of Proc Respondentscore and returns the corresponding message
	*/
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		String [] requiredparameters=new String[] {Keywords.OUT.toLowerCase(), Keywords.dict, Keywords.var, Keywords.correctanswers, Keywords.pointforcorrect, Keywords.pointforerrated, Keywords.pointformissing};
		String [] optionalparameters=new String[] {Keywords.itemweights, Keywords.where, Keywords.replace};
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

		String temppointforcorrect=(String)parameters.get(Keywords.pointforcorrect);
		String temppointforerrated=(String)parameters.get(Keywords.pointforerrated);
		String temppointformissing=(String)parameters.get(Keywords.pointformissing);

		String tempitemweights=(String)parameters.get(Keywords.itemweights);
		double[] itemw=null;
		if (tempitemweights!=null)
		{
			try
			{
				String[] itemweights=tempitemweights.split(" ");
				itemw=new double[itemweights.length];
				for (int i=0; i<itemweights.length; i++)
				{
					itemw[i]=Double.NaN;
					itemw[i]=Double.parseDouble(temppointformissing);
					if (Double.isNaN(itemw[i])) return new Result("%3160%\n", false, null);
				}
			}
			catch (Exception eiw)
			{
				return new Result("%3160%<br>\n", false, null);
			}
		}

		double pointforcorrect=Double.NaN;
		double pointforerrated=Double.NaN;
		double pointformissing=Double.NaN;
		try
		{
			pointforcorrect=Double.parseDouble(temppointforcorrect);
		}
		catch (Exception e) {}
		try
		{
			pointforerrated=Double.parseDouble(temppointforerrated);
		}
		catch (Exception e) {}
		try
		{
			pointformissing=Double.parseDouble(temppointformissing);
		}
		catch (Exception e) {}
		if (Double.isNaN(pointforcorrect)) return new Result("%3120%\n", false, null);
		if (Double.isNaN(pointforerrated)) return new Result("%3121%\n", false, null);
		if (Double.isNaN(pointformissing)) return new Result("%3122%\n", false, null);
		if (pointforcorrect<pointforerrated) return new Result("%3123%\n", false, null);
		if (pointforcorrect<pointformissing) return new Result("%3124%\n", false, null);

		String tempvar=(String)parameters.get(Keywords.var);
		String tempcorrectanswers=(String)parameters.get(Keywords.correctanswers);
		String[] correctanswers=null;
		String[] vars=tempvar.split(" ");
		if (vars.length<3)
			return new Result("%3107%<br>\n", false, null);
		tempcorrectanswers=tempcorrectanswers.trim().replaceAll("\\s+", " ");
		tempcorrectanswers=tempcorrectanswers.trim().replaceAll("& ", "&");
		correctanswers=tempcorrectanswers.split("\\|");
		for (int i=0; i<correctanswers.length; i++)
		{
			correctanswers[i]=correctanswers[i].trim();
		}
		if (vars.length!=correctanswers.length) return new Result("%3119%\n", false, null);
		if (itemw!=null)
		{
			if (vars.length!=itemw.length) return new Result("%3160%\n", false, null);
		}
		else
		{
			itemw=new double[vars.length];
			for (int i=0; i<vars.length; i++)
			{
				itemw[i]=1.0;
			}
		}

		DictionaryReader dict = (DictionaryReader)parameters.get(Keywords.dict);

		VariableUtilities varu=new VariableUtilities(dict, null, tempvar.trim(), null, null, null);
		if (varu.geterror())
			return new Result(varu.getmessage(), false, null);

		String[] totalvar=varu.getallvar();

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

		DataSetUtilities dsu=new DataSetUtilities();
		dsu.setreplace(replace);
		Hashtable<String, String> temph=new Hashtable<String, String>();

		dsu.defineolddict(dict);

		dsu.addnewvartoolddict("score", "%3149%", Keywords.NUMSuffix, temph, temph);

		if (!dw.opendatatable(dsu.getfinalvarinfo()))
			return new Result(dw.getmessage(), false, null);

		String[] values=null;
		String[] varvalues=null;

		double totalscore=0;
		String[] partc;
		String[] answ;
		int nummultc=0;
		boolean multcorrected=false;

		while (!data.isLast())
		{
			values = data.getRecord();
			totalscore=Double.NaN;
			if (values!=null)
			{
				varvalues=vp.getanalysisvar(values);
				totalscore=0;
				for (int i=0; i<varvalues.length; i++)
				{
					if (varvalues[i].equals("")) totalscore=totalscore+itemw[i]*pointformissing;
					else
					{
						if (varvalues[i].indexOf("&")>=0)
						{
							answ=varvalues[i].split("&");
							if (correctanswers[i].indexOf("&")>=0)
							{
								partc=correctanswers[i].split("&");
								nummultc=0;
								for (int j=0; j<partc.length; j++)
								{
									multcorrected=false;
									for (int k=0; k<answ.length; k++)
									{
										partc[j]=partc[j].trim();
										if (answ[k].equalsIgnoreCase(partc[j]))
										{
											multcorrected=true;
											nummultc++;
										}
									}
									if (multcorrected) totalscore=totalscore+itemw[i]*pointforcorrect;
								}
								if (nummultc!=partc.length)
								{
									totalscore=totalscore+itemw[i]*pointforerrated;
								}
							}
							else
							{
								for (int k=0; k<answ.length; k++)
								{
									if (answ[k].equalsIgnoreCase(correctanswers[i])) totalscore=totalscore+itemw[i]*pointforcorrect;
									else totalscore=totalscore+itemw[i]*pointforerrated;
								}
							}
						}
						else
						{
							if (correctanswers[i].indexOf("&")>=0)
							{
								partc=correctanswers[i].split("&");
								for (int j=0; j<partc.length; j++)
								{
									partc[j]=partc[j].trim();
									if (varvalues[i].equalsIgnoreCase(partc[j])) totalscore=totalscore+itemw[i]*pointforcorrect;
									else totalscore=totalscore+itemw[i]*pointforerrated;
								}
							}
							else
							{
								if (varvalues[i].equalsIgnoreCase(correctanswers[i])) totalscore=totalscore+itemw[i]*pointforcorrect;
								else totalscore=totalscore+itemw[i]*pointforerrated;
							}
						}
					}
				}
			}
			String[] newvalues=new String[1];
			newvalues[0]=String.valueOf(totalscore);
			String[] wvalues=dsu.getnewvalues(values, newvalues);
			dw.write(wvalues);
		}


		String keyword="Respondent Score "+dict.getkeyword();
		String description="Respondent Score "+dict.getdescription();
		String author=(String)parameters.get(Keywords.client_host.toLowerCase());

		Hashtable<String, String> othertableinfo=new Hashtable<String, String>();
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
		parameters.add(new GetRequiredParameters(Keywords.var, "vars=all", true, 3114, dep, "", 2));
		dep = new String[0];
		parameters.add(new GetRequiredParameters(Keywords.where,"text", false, 2805,dep,"",2));
		parameters.add(new GetRequiredParameters("", "note", false, 2806, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.correctanswers,"text", true, 3133,dep,"",2));
		parameters.add(new GetRequiredParameters("", "note", false, 3134, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.pointforcorrect,"text", true, 3135,dep,"",2));
		parameters.add(new GetRequiredParameters(Keywords.pointforerrated,"text", true, 3136,dep,"",2));
		parameters.add(new GetRequiredParameters(Keywords.pointformissing,"text", true, 3137,dep,"",2));
		parameters.add(new GetRequiredParameters(Keywords.itemweights,"text", false, 3159,dep,"",2));
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
		retprocinfo[1]="3150";
		return retprocinfo;
	}
}
