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

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Vector;
import java.util.HashSet;

import ADaMSoft.dataaccess.DataReader;
import ADaMSoft.dataaccess.DataWriter;
import ADaMSoft.dataaccess.DictionaryReader;
import ADaMSoft.keywords.Keywords;
import ADaMSoft.utilities.DataSetUtilities;
import ADaMSoft.utilities.GetRequiredParameters;
import ADaMSoft.utilities.ValuesParser;
import ADaMSoft.utilities.VariableUtilities;
import ADaMSoft.utilities.ObjectTransformer;
import ADaMSoft.utilities.SortRequestedVar;
import ADaMSoft.utilities.StepUtilities;

/**
* This is the procedure that evaluates the predicted probability for a linear logistic regression
* @author marco.scarno@gmail.com
* @date 14/02/2017
*/
public class ProcLogisticeval extends ObjectTransformer implements RunStep
{
	/**
	* Starts the execution of Proc Logisticeval
	*/
	@SuppressWarnings("unused")
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		boolean novgconvert=false;
		String [] requiredparameters=new String[] {Keywords.OUT.toLowerCase(), Keywords.dict, Keywords.dict+"e"};
		String [] optionalparameters=new String[] {Keywords.replace, Keywords.where, Keywords.novgconvert};
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

		DictionaryReader dicte = (DictionaryReader)parameters.get(Keywords.dict+"e");
		novgconvert =(parameters.get(Keywords.novgconvert)!=null);

		Hashtable<String, String> tinfo=dicte.getdatatableinfo();
		String successvalue=tinfo.get(Keywords.successvalue);
		if (successvalue!=null)
			successvalue=successvalue.trim();

		String vary=tinfo.get(Keywords.vary);

		int numvar=dicte.gettotalvar();
		String var="";
		boolean iscorrect=true;
		HashSet<String> usedmone=new HashSet<String>();
		for (int i=0; i<numvar; i++)
		{
			String tempname=dicte.getvarname(i);
			if ((tempname.toLowerCase()).startsWith("g_"))
			{
				try
				{
					String tc=tempname.substring(2);
					var=var+" "+tc;
				}
				catch (Exception ec)
				{
					iscorrect=false;
				}
			}
		}
		if (!iscorrect)
			return new Result("%898%<br>\n", false, null);

		String groupname=var.trim();

		var=var.trim()+" varx parameter";
		var=var.trim();

		String[] groupvar=new String[0];
		if (!groupname.equals(""))
			groupvar=groupname.split(" ");

		DictionaryReader dict = (DictionaryReader)parameters.get(Keywords.dict);

		String[] alldsvars=new String[dict.gettotalvar()];
		for (int i=0; i<dict.gettotalvar(); i++)
		{
			alldsvars[i]=dict.getvarname(i);
		}
		groupvar=SortRequestedVar.getreqsorted(groupvar, alldsvars);

		if (successvalue!=null)
		{
			boolean existdepvar=false;
			vary=vary.trim();
			for (int i=0; i<dict.gettotalvar(); i++)
			{
				if (dict.getvarname(i).equalsIgnoreCase(vary))
					existdepvar=true;
			}
			if (!existdepvar)
				successvalue=null;
		}

		String[] vartoread=new String[groupvar.length+2];
		for (int i=0; i<groupvar.length; i++)
		{
			vartoread[i]="g_"+groupvar[i];
		}
		vartoread[groupvar.length]="varx";
		vartoread[groupvar.length+1]="parameter";

		int[] replacerule=new int[vartoread.length];
		for (int i=0; i<vartoread.length; i++)
		{
			replacerule[i]=0;
		}

		DataReader datae = new DataReader(dicte);
		if (!datae.open(vartoread, replacerule, false))
			return new Result(datae.getmessage(), false, null);

		Hashtable<Vector<String>, Hashtable<String, Double>> tempparameters=new Hashtable<Vector<String>, Hashtable<String, Double>>();
		Hashtable<Vector<String>, Double> tempintercepts=new Hashtable<Vector<String>, Double>();
		Hashtable<String, Integer> tempvarname=new Hashtable<String, Integer>();

		boolean errorinpara=false;

		boolean noint=true;

		String[] pvg=null;
		String ton="";

		while (!datae.isLast())
		{
			String[] values = datae.getRecord();
			Vector<String> groupval=new Vector<String>();
			if (groupvar.length==0)
				groupval.add(null);
			else
			{
				for (int i=0; i<groupvar.length; i++)
				{
					String realvgval=values[i].trim();
					if (!novgconvert)
					{
						try
						{
							double realnumvgval=Double.parseDouble(realvgval);
							if (!Double.isNaN(realnumvgval))
								realvgval=String.valueOf(realnumvgval);
						}
						catch (Exception e) {}
					}
					groupval.add(realvgval);
				}
			}
			try
			{
				String tempname=values[groupvar.length];
				boolean isint=false;
				try
				{
					double testint=Double.valueOf(tempname);
					if (testint==0)
					{
						isint=true;
						noint=false;
						double tempparaint=Double.valueOf(values[groupvar.length+1]);
						tempintercepts.put(groupval, tempparaint);
					}
				}
				catch (Exception eint) {}
				if (!isint)
				{
					ton="";
					if (tempname.indexOf("$EC")>=0)
					{
						tempname=tempname.replaceAll("\\$EC_","");
						ton=tempname;
					}
					if (tempname.indexOf("$G_")>=0)
					{
						pvg=tempname.split("_");
						if (pvg.length==3)
							tempname=pvg[2];
						else
						{
							tempname="";
							for (int i=2; i<pvg.length; i++)
							{
								tempname=tempname+pvg[i];
							}
						}
					}
					tempvarname.put(tempname, new Integer(tempvarname.size()));
					double temppara=Double.valueOf(values[groupvar.length+1]);
					Hashtable<String, Double> tempp=tempparameters.get(groupval);
					if (tempp==null)
						tempp=new Hashtable<String, Double>();
					tempp.put(tempname, temppara);
					tempparameters.put(groupval, tempp);
					if (!ton.equals(""))
					{
						pvg=tempname.split("=");
						usedmone.add(pvg[0].toLowerCase());
					}
				}
			}
			catch (Exception e)
			{
				errorinpara=true;
			}
		}
		datae.close();
		if (errorinpara)
			return new Result("%899%<br>\n", false, null);

		int coeffsize=tempvarname.size();

		Vector<String> real_var_names=new Vector<String>();
		String[] temp_strings=null;
		Vector<String> class_var_names=new Vector<String>();
		for (Enumeration<String> es = tempvarname.keys() ; es.hasMoreElements() ;)
		{
			String tempvname=es.nextElement();
			if (tempvname.indexOf("=")>=0)
			{
				temp_strings=tempvname.split("=");
				if (!real_var_names.contains(temp_strings[0])) real_var_names.add(temp_strings[0].toLowerCase());
				if (!class_var_names.contains(temp_strings[0])) class_var_names.add(temp_strings[0].toLowerCase());
			}
			else if (!real_var_names.contains(tempvname)) real_var_names.add(tempvname.toLowerCase());
		}

		String[] usedvarnames=new String[real_var_names.size()];
		for (int i=0; i<real_var_names.size(); i++)
		{
			usedvarnames[i]=real_var_names.get(i);
		}

		usedvarnames=SortRequestedVar.getreqsorted(usedvarnames, alldsvars);
		int[] is_class=new int[usedvarnames.length];

		Hashtable<Vector<String>, Vector<Hashtable<String, Double>>> coeff_for_class=new Hashtable<Vector<String>, Vector<Hashtable<String, Double>>>();
		Hashtable<Vector<String>, Vector<Double>> coeff_for_normal=new Hashtable<Vector<String>, Vector<Double>>();
		Hashtable<String, Integer> position_for_class=new Hashtable<String, Integer>();
		Hashtable<String, Integer> position_for_normal=new Hashtable<String, Integer>();
		int ref_position=0;
		int ref_position_normal=0;
		for (int i=0; i<usedvarnames.length; i++)
		{
			if (class_var_names.indexOf(usedvarnames[i].toLowerCase())>=0)
			{
				position_for_class.put(usedvarnames[i].toLowerCase(), new Integer(ref_position));
				ref_position++;
			}
			else
			{
				position_for_normal.put(usedvarnames[i].toLowerCase(), new Integer(ref_position_normal));
				ref_position_normal++;
			}
		}
		for (int i=0; i<usedvarnames.length; i++)
		{
			if (class_var_names.indexOf(usedvarnames[i].toLowerCase())>=0)
			{
				for (Enumeration<Vector<String>> e = tempparameters.keys() ; e.hasMoreElements() ;)
				{
					Vector<String> gv= e.nextElement();
					Hashtable<String, Double> actualpar=tempparameters.get(gv);
					for (Enumeration<String> ei = actualpar.keys() ; ei.hasMoreElements() ;)
					{
						String vp=ei.nextElement();
						if (vp.indexOf("=")>=0)
						{
							temp_strings=vp.split("=");
							if (temp_strings[0].equalsIgnoreCase(usedvarnames[i]))
							{
								if (coeff_for_class.get(gv)==null)
								{
									Vector<Hashtable<String, Double>> first_level=new Vector<Hashtable<String, Double>>();
									for (int j=0; j<class_var_names.size(); j++)
									{
										Hashtable<String, Double> second_level=new Hashtable<String, Double>();
										first_level.add(second_level);
									}
									coeff_for_class.put(gv, first_level);
								}
								ref_position=(position_for_class.get(temp_strings[0].toLowerCase())).intValue();
								Vector<Hashtable<String, Double>> first_level_def=coeff_for_class.get(gv);
								Hashtable<String, Double> second_level_def=first_level_def.get(ref_position);
								second_level_def.put(temp_strings[1], actualpar.get(vp));
								first_level_def.set(ref_position, second_level_def);
								coeff_for_class.put(gv, first_level_def);
							}
						}
					}
				}
				if (usedmone.contains(usedvarnames[i].toLowerCase()))
					is_class[i]=2;
				else
					is_class[i]=1;
			}
			else
			{
				for (Enumeration<Vector<String>> e = tempparameters.keys() ; e.hasMoreElements() ;)
				{
					Vector<String> gv= e.nextElement();
					Hashtable<String, Double> actualpar=tempparameters.get(gv);
					for (Enumeration<String> ei = actualpar.keys() ; ei.hasMoreElements() ;)
					{
						String vp=ei.nextElement();
						if (vp.equalsIgnoreCase(usedvarnames[i]))
						{
							if (coeff_for_normal.get(gv)==null)
							{
								Vector<Double> first_level=new Vector<Double>();
								for (int j=0; j<position_for_normal.size(); j++)
								{
									first_level.add(new Double(0.0));
								}
								coeff_for_normal.put(gv, first_level);
							}
							Vector<Double> first_level_def=coeff_for_normal.get(gv);
							ref_position_normal=(position_for_normal.get(usedvarnames[i].toLowerCase())).intValue();
							first_level_def.set(ref_position_normal, actualpar.get(vp));

						}
					}
				}
				is_class[i]=0;
			}
		}

		String varx="";
		for (int i=0; i<usedvarnames.length; i++)
		{
			varx=varx+" "+usedvarnames[i];
		}
		varx=varx.trim();
		String reqvary=null;
		if (successvalue!=null)
			reqvary=vary;

		String keyword="Logistic Eval "+dict.getkeyword();
		String description="Logistic Eval "+dict.getdescription();
		String author=(String) parameters.get(Keywords.client_host.toLowerCase());

		DataSetUtilities dsu=new DataSetUtilities();
		dsu.setreplace(replace);

		Hashtable<String, String> temph=new Hashtable<String, String>();

		dsu.defineolddict(dict);

		numvar=dict.gettotalvar();

		dsu.addnewvartoolddict("predprob", "%1033%", Keywords.NUMSuffix, temph, temph);
		if (successvalue!=null)
			dsu.addnewvartoolddict("successvalue", "%1756%", Keywords.NUMSuffix, temph, temph);

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

		if (groupname.equals(""))
			groupname=null;

		VariableUtilities varu=new VariableUtilities(dict, groupname, varx, null, null, null);
		VariableUtilities varur=null;
		if (successvalue!=null)
		{
			varur=new VariableUtilities(dict, null, null, null, reqvary, null);
			if (varur.geterror())
				return new Result(varur.getmessage(), false, null);
		}

		if (varu.geterror())
			return new Result(varu.getmessage(), false, null);

		String[] totalvar=varu.getallvar();

		int[] replacer=varu.getreplaceruleforall(replace);

		int[] rprow=new int[0];
		if (successvalue!=null)
			rprow=varur.getrowruleforall();

		DataReader data = new DataReader(dict);
		if (!data.open(totalvar, replacer, false))
			return new Result(data.getmessage(), false, null);
		String where=(String)parameters.get(Keywords.where.toLowerCase());
		if (where!=null)
		{
			if (!data.setcondition(where)) return new Result(data.getmessage(), false, null);
		}

		int[] allvarstype=varu.getnormalruleforall();

		ValuesParser vp=new ValuesParser(allvarstype, null, null, null, null, null);
		ValuesParser vprow=null;
		if (successvalue!=null)
			vprow=new ValuesParser(null, null, null, rprow, null, null);

		int validgroup=0;
		boolean noterror=true;
		String[] values = null;
		Vector<String> vargroupvalues=new Vector<String>();
		String[] rrealval=null;
		String[] srealval=new String[0];
		String[] newvalues=new String[1];
		double[] par=null;
		double predictedval=0;
		boolean isnull=false;
		boolean ismissingx=false;
		String[] wvalues=null;
		double interceptvalue=0;
		double param=0;
		Vector<Double> tv1=new Vector<Double>();
		Vector<Hashtable<String, Double>> tv2=new Vector<Hashtable<String, Double>>();
		Hashtable<String, Double> tv3=new Hashtable<String, Double>();
		String tempvalc="";
		boolean foundmo=false;
		while ((!data.isLast()) && (noterror))
		{
			ismissingx=false;
			values = data.getRecord();
			ref_position=0;
			ref_position_normal=0;
			predictedval=0;
			if (values!=null)
			{
				if (novgconvert)
					vargroupvalues=vp.getorigvargroup(values);
				else
					vargroupvalues=vp.getvargroup(values);
				rrealval=vp.getanalysisvar(values);
				for (int i=0; i<rrealval.length; i++)
				{
					if (rrealval[i].equals("")) ismissingx=true;
				}
				if (!ismissingx)
				{
					for (int i=0; i<rrealval.length; i++)
					{
						if (is_class[i]==0)
						{
							try
							{
								tv1=coeff_for_normal.get(vargroupvalues);
								param=(tv1.get(ref_position_normal)).doubleValue();
								predictedval=predictedval+Double.parseDouble(rrealval[i])*param;
							}
							catch (Exception en)
							{
								ismissingx=true;
							}
							ref_position_normal++;
						}
						else
						{
							if (is_class[i]==1)
							{
								try
								{
									tv2=coeff_for_class.get(vargroupvalues);
									tv3=tv2.get(ref_position);
									for (Enumeration<String> ei = tv3.keys() ; ei.hasMoreElements() ;)
									{
										tempvalc=ei.nextElement();
										param=(tv3.get(tempvalc)).doubleValue();
										if (tempvalc.equals(rrealval[i]))
											predictedval=predictedval+param;
									}
								}
								catch (Exception en)
								{
									ismissingx=true;
								}
							}
							else
							{
								try
								{
									foundmo=false;
									tv2=coeff_for_class.get(vargroupvalues);
									tv3=tv2.get(ref_position);
									for (Enumeration<String> ei = tv3.keys() ; ei.hasMoreElements() ;)
									{
										tempvalc=ei.nextElement();
										param=(tv3.get(tempvalc)).doubleValue();
										if (tempvalc.equals(rrealval[i]))
										{
											foundmo=true;
											predictedval=predictedval+param;
										}
									}
									if (!foundmo)
									{
										for (Enumeration<String> ei = tv3.keys() ; ei.hasMoreElements() ;)
										{
											tempvalc=ei.nextElement();
											param=(tv3.get(tempvalc)).doubleValue();
											predictedval=predictedval-param;
										}
									}
								}
								catch (Exception en)
								{
									ismissingx=true;
								}
							}
							ref_position++;
						}
					}
				}
				if (successvalue!=null)
				{
					newvalues=new String[2];
					newvalues[0]="";
					srealval=vprow.getrowvar(values);
					if (!srealval[0].equals(""))
					{
						if (srealval[0].trim().equalsIgnoreCase(successvalue))
							newvalues[1]="1";
						else
							newvalues[1]="0";
					}
				}
				else
				{
					newvalues=new String[1];
					newvalues[0]="";
				}
				if ((vp.vargroupisnotmissing(vargroupvalues)) && (!ismissingx))
				{
					validgroup++;
					if (!noint)
					{
						interceptvalue=(tempintercepts.get(vargroupvalues)).doubleValue();
						predictedval=predictedval+interceptvalue;
					}
					if(!Double.isNaN(predictedval))
					{
						predictedval=(Math.exp(predictedval))/(1+(Math.exp(predictedval)));
						newvalues[0]=double2String(predictedval);
					}
				}
				else
				{
					if (successvalue!=null)
						newvalues[1]="";
				}
				wvalues=dsu.getnewvalues(values, newvalues);
				dw.write(wvalues);
			}
		}
		data.close();
		if ((validgroup==0) && (where!=null))
		{
			dw.deletetmp();
			return new Result("%2804%<br>\n", false, null);
		}
		if ((validgroup==0) && (where==null))
		{
			dw.deletetmp();
			return new Result("%666%<br>\n", false, null);
		}

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
		parameters.add(new GetRequiredParameters(Keywords.dict+"=", "dict", true, 900, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.dict+"e=", "dict", true, 901, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.OUT.toLowerCase()+"=", "setting=out", true, 904, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.viewout, "checkbox", false, 1612, dep, "", 1));
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
		retprocinfo[0]="996";
		retprocinfo[1]="1032";
		return retprocinfo;
	}
}
