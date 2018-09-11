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
import ADaMSoft.utilities.StepUtilities;

import ADaMSoft.algorithms.VarGroupModalities;
import ADaMSoft.dataaccess.DataReader;
import ADaMSoft.dataaccess.DataWriter;
import ADaMSoft.dataaccess.DictionaryReader;
import ADaMSoft.keywords.Keywords;
import ADaMSoft.utilities.DataSetUtilities;
import ADaMSoft.utilities.GetRequiredParameters;
import ADaMSoft.utilities.ValuesParser;
import ADaMSoft.utilities.VariableUtilities;
import ADaMSoft.utilities.ObjectTransformer;
import ADaMSoft.dataaccess.GroupedTempDataSet;

/**
* This is the procedure that evaluate the Discriminationindexes on a series of items
* @author marco.scarno@gmail.com
* @date 13/02/2017
*/
public class ProcDiscriminationindexes extends ObjectTransformer implements RunStep
{
	/**
	* Starts the execution of Proc Discriminationindices
	*/
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		String [] requiredparameters=new String[] {Keywords.OUT.toLowerCase(), Keywords.dict, Keywords.var, Keywords.correctanswers, Keywords.pointforcorrect, Keywords.pointforerrated, Keywords.pointformissing};
		String [] optionalparameters=new String[] {Keywords.divideby3, Keywords.lowlimit, Keywords.uplimit, Keywords.itemweights, Keywords.where, Keywords.vargroup, Keywords.replace};
		Keywords.percentage_total=0;
		Keywords.percentage_done=0;
		StepUtilities steputilities=new StepUtilities();
		if (!steputilities.checkParameters(requiredparameters, optionalparameters, parameters))
		{
			return new Result(steputilities.getMessage(), false, null);
		}
		Vector<StepResult> result = new Vector<StepResult>();

		boolean divideby3=(parameters.get(Keywords.divideby3)!=null);

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

		String templowlimit=(String)parameters.get(Keywords.lowlimit);
		String tempuplimit=(String)parameters.get(Keywords.uplimit);

		double deflowlimit=Double.NaN;
		double defuplimit=Double.NaN;

		if (templowlimit!=null && tempuplimit==null) return new Result("%3141%\n", false, null);
		if (templowlimit==null && tempuplimit!=null) return new Result("%3141%\n", false, null);

		boolean evallimit=true;
		if (templowlimit!=null)
		{
			try
			{
				deflowlimit=Double.parseDouble(templowlimit);
			}
			catch (Exception e) {}
			if (Double.isNaN(deflowlimit)) return new Result("%3142%<br>\n", false, null);
			if (deflowlimit<0) return new Result("%3142%<br>\n", false, null);
		}
		if (tempuplimit!=null)
		{
			try
			{
				defuplimit=Double.parseDouble(tempuplimit);
			}
			catch (Exception e) {}
			if (Double.isNaN(defuplimit)) return new Result("%3143%<br>\n", false, null);
			if (defuplimit<0) return new Result("%3143%<br>\n", false, null);
			if (defuplimit<deflowlimit) return new Result("%3144%<br>\n", false, null);
			evallimit=false;
		}

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
		if (vars.length!=correctanswers.length) return new Result("%3119%<br>\n", false, null);
		if (itemw!=null)
		{
			if (vars.length!=itemw.length) return new Result("%3160%<br>\n", false, null);
		}
		else
		{
			itemw=new double[vars.length];
			for (int i=0; i<vars.length; i++)
			{
				itemw[i]=1.0;
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
		if (Double.isNaN(pointforcorrect)) return new Result("%3120%<br>\n", false, null);
		if (Double.isNaN(pointforerrated)) return new Result("%3121%<br>\n", false, null);
		if (Double.isNaN(pointformissing)) return new Result("%3122%<br>\n", false, null);
		if (pointforcorrect<pointforerrated) return new Result("%3123%<br>\n", false, null);
		if (pointforcorrect<pointformissing) return new Result("%3124%<br>\n", false, null);

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

		String tempdir=(String)parameters.get(Keywords.WorkDir);
		GroupedTempDataSet gtds=null;
		int validgroup=0;
		Vector<String> vargroupvalues=new Vector<String>();
		String[] values=null;
		String[] varvalues=null;
		double totalscore=0;
		String[] partc=null;
		String[] answ=null;
		boolean multcorrected=false;
		int nummultc=0;

		if (evallimit) gtds=new GroupedTempDataSet(tempdir, 1);
		while (!data.isLast())
		{
			values = data.getRecord();
			if (values!=null)
			{
				vargroupvalues=vp.getvargroup(values);
				varvalues=vp.getanalysisvar(values);
				if (vp.vargroupisnotmissing(vargroupvalues))
				{
					validgroup++;
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
					vgm.updateModalities(vargroupvalues);
					String[] row=new String[1];
					row[0]=String.valueOf(totalscore);
					if (evallimit) gtds.write(vargroupvalues, row);
				}
			}
		}
		data.close();
		if ((validgroup==0) && (where!=null))
			return new Result("%2804%<br>\n", false, null);
		if ((validgroup==0) && (where==null))
			return new Result("%666%<br>\n", false, null);
		if (evallimit) gtds.finalizeWriteAll();
		vgm.calculate();
		int totalgroupmodalities=vgm.getTotal();
		if (totalgroupmodalities==0)
			totalgroupmodalities=1;

		if (evallimit)
		{
			boolean verify=gtds.sortAll(0);
			if (!verify) return new Result(gtds.getMessage()+"\n", false, null);
		}

		Hashtable<Vector<String>, Double> low=new Hashtable<Vector<String>, Double>();
		Hashtable<Vector<String>, Double> high=new Hashtable<Vector<String>, Double>();

		if (evallimit)
		{
			for (int i=0; i<totalgroupmodalities; i++)
			{
				String actualmod="";
				Vector<String> rifmodgroup=vgm.getvectormodalities(i);
				for (int j=0; j<rifmodgroup.size(); j++)
				{
					String groupvalue=rifmodgroup.get(j);
					if (groupvalue!=null)
					{
						actualmod=actualmod+groupvalue+" ";
					}
				}
				if (!actualmod.equals(""))
					actualmod="%3147%("+actualmod+"): ";
				else
					actualmod="%3148%: ";
				int records=gtds.getRows(rifmodgroup);
				int lowlimit=records/4;
				int uplimit=lowlimit*3;
				int current=0;
				if (lowlimit<0) lowlimit=0;
				if (uplimit>records) uplimit=records-1;
				if (uplimit<0) uplimit=0;
				String value[];
				gtds.assignbasefile(rifmodgroup);
				for (int j=0; j<uplimit+1; j++)
				{
					value=gtds.read(rifmodgroup);
					if (current==lowlimit)
					{
						double temp=0;
						try
						{
							temp=Double.parseDouble(value[0]);
						}
						catch (Exception e) {}
						actualmod=actualmod+String.valueOf(temp)+"-";
						low.put(rifmodgroup, new Double(temp));
					}
					if (current==uplimit)
					{
						double temp=0;
						try
						{
							temp=Double.parseDouble(value[0]);
						}
						catch (Exception e) {}
						actualmod=actualmod+String.valueOf(temp)+"\n";
						high.put(rifmodgroup, new Double(temp));
					}
					current++;
				}
				result.add(new LocalMessageGetter(actualmod));
				gtds.deassignbasefile();
				gtds.endread(rifmodgroup);
			}
			gtds.deletetempdataAll();
		}
		else
		{
			result.add(new LocalMessageGetter("%3146% ("+String.valueOf(deflowlimit)+"-"+String.valueOf(defuplimit)+")<br>\n"));
		}

		if (!data.open(reqvar, replacerule, false))
			return new Result(data.getmessage(), false, null);
		where=(String)parameters.get(Keywords.where.toLowerCase());
		if (where!=null)
		{
			if (!data.setcondition(where)) return new Result(data.getmessage(), false, null);
		}
		Hashtable<Vector<String>, CorrectAnswer[]> pos=new Hashtable<Vector<String>, CorrectAnswer[]>();
		Hashtable<Vector<String>, NoCorrectAnswer[]> neg=new Hashtable<Vector<String>, NoCorrectAnswer[]>();
		Hashtable<Vector<String>, Double> num=new Hashtable<Vector<String>, Double>();

		double reflow=0;
		double refhigh=0;
		double refnum=0;
		boolean type=false;
		boolean possible;

		while (!data.isLast())
		{
			values = data.getRecord();
			if (values!=null)
			{
				vargroupvalues=vp.getvargroup(values);
				varvalues=vp.getanalysisvar(values);
				if (pos.get(vargroupvalues)==null)
				{
					CorrectAnswer[] ca=new CorrectAnswer[varvalues.length];
					for (int i=0; i<varvalues.length; i++)
					{
						ca[i]=new CorrectAnswer();
						ca[i].init(correctanswers[i]);
					}
					pos.put(vargroupvalues, ca);
				}
				if (neg.get(vargroupvalues)==null)
				{
					NoCorrectAnswer[] nca=new NoCorrectAnswer[varvalues.length];
					for (int i=0; i<varvalues.length; i++)
					{
						nca[i]=new NoCorrectAnswer();
					}
					neg.put(vargroupvalues, nca);
				}
				if (vp.vargroupisnotmissing(vargroupvalues))
				{
					validgroup++;
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
					if (num.get(vargroupvalues)==null) num.put(vargroupvalues, new Double(0));
					refnum=(num.get(vargroupvalues)).doubleValue();
					num.put(vargroupvalues, new Double(refnum+1));
					possible=true;
					if (evallimit)
					{
						try
						{
							reflow=(low.get(vargroupvalues)).doubleValue();
							refhigh=(high.get(vargroupvalues)).doubleValue();
						}
						catch (Exception enp)
						{
							possible=false;
						}
					}
					else
					{
						reflow=deflowlimit;
						refhigh=defuplimit;
					}
					if ((totalscore>=refhigh || totalscore<=reflow) && possible)
					{
						type=false;
						if (totalscore>=refhigh) type=true;
						CorrectAnswer[] tempca=pos.get(vargroupvalues);
						NoCorrectAnswer[] tempnca=neg.get(vargroupvalues);
						for (int i=0; i<varvalues.length; i++)
						{
							if (!varvalues[i].equals(""))
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
											if (multcorrected) tempca[i].addCorrected(partc[j], type);
										}
										if (nummultc!=partc.length)
										{
											for (int k=0; k<answ.length; k++)
											{
												multcorrected=false;
												for (int j=0; j<partc.length; j++)
												{
													partc[j]=partc[j].trim();
													if (answ[k].equalsIgnoreCase(partc[j]))
													{
														multcorrected=true;
													}
												}
												if (!multcorrected) tempnca[i].addErrated(answ[k], type);
											}
										}
									}
									else
									{
										for (int k=0; k<answ.length; k++)
										{
											if (answ[k].equalsIgnoreCase(correctanswers[i])) tempca[i].addCorrected(answ[k], type);
											else tempnca[i].addErrated(answ[k], type);
										}
									}
								}
								else
								{
									if (correctanswers[i].indexOf("&")>=0)
									{
										partc=correctanswers[i].split("&");
										multcorrected=false;
										for (int j=0; j<partc.length; j++)
										{
											partc[j]=partc[j].trim();
											if (varvalues[i].equalsIgnoreCase(partc[j]))
											{
												tempca[i].addCorrected(partc[j], type);
												multcorrected=true;
											}
										}
										if (!multcorrected) tempnca[i].addErrated(varvalues[i], type);
									}
									else
									{
										if (varvalues[i].equalsIgnoreCase(correctanswers[i])) tempca[i].addCorrected(correctanswers[i], type);
										else tempnca[i].addErrated(varvalues[i], type);
									}
								}
							}
						}
					}
				}
			}
		}
		data.close();

		String keyword="Discrimination index "+dict.getkeyword();
		String description="Discrimination index "+dict.getdescription();
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
		cltype.put("1", "%3129%");
		cltype.put("0", "%3130%");

		dsu.addnewvar("question", "%3126%", Keywords.TEXTSuffix, clvar, tempmd);
		dsu.addnewvar("answer", "%3127%", Keywords.TEXTSuffix, tempmd, tempmd);
		dsu.addnewvar("type", "%3128%", Keywords.TEXTSuffix, cltype, tempmd);
		dsu.addnewvar("index_value", "%3131%", Keywords.NUMSuffix, tempmd, tempmd);

		if (!dw.opendatatable(dsu.getfinalvarinfo()))
			return new Result(dw.getmessage(), false, null);

		String[] valuestowrite=null;
		double refrespo=0;
		double indexvalue=0;

		for (int i=0; i<totalgroupmodalities; i++)
		{
			valuestowrite=new String[varg.length+4];
			Vector<String> rifmodgroup=vgm.getvectormodalities(i);
			for (int j=0; j<rifmodgroup.size(); j++)
			{
				String groupvalue=rifmodgroup.get(j);
				if (groupvalue!=null)
				{
					valuestowrite[j]=groupvalue;
				}
			}
			CorrectAnswer[] tempca=pos.get(rifmodgroup);
			NoCorrectAnswer[] tempnca=neg.get(rifmodgroup);
			refrespo=(num.get(vargroupvalues)).doubleValue();
			for (int j=0; j<tempca.length; j++)
			{
				valuestowrite[varg.length]="ref_"+var[j];
				TreeMap<String, double[]> tca=tempca[j].getresult();
				TreeMap<String, double[]> tcb=tempnca[j].getresult();
				for (Iterator<String> it = tca.keySet().iterator(); it.hasNext();)
				{
					String key = it.next();
					double[] dval = tca.get(key);
					valuestowrite[varg.length+1]=key;
					valuestowrite[varg.length+2]="1";
					indexvalue=(dval[0]-dval[1]);
					if (divideby3) indexvalue=indexvalue/(refrespo/3);
					else indexvalue=indexvalue/(refrespo/4);
					if (indexvalue>1) indexvalue=1.0;
					if (indexvalue<-1) indexvalue=-1.0;
					valuestowrite[varg.length+3]=String.valueOf(indexvalue);
					dw.write(valuestowrite);
				}
				for (Iterator<String> it = tcb.keySet().iterator(); it.hasNext();)
				{
					String key = it.next();
					double[] dval = tcb.get(key);
					valuestowrite[varg.length+1]=key;
					valuestowrite[varg.length+2]="0";
					indexvalue=(dval[0]-dval[1]);
					if (divideby3) indexvalue=indexvalue/(refrespo/3);
					else indexvalue=indexvalue/(refrespo/4);
					if (indexvalue>1) indexvalue=1.0;
					if (indexvalue<-1) indexvalue=-1.0;
					valuestowrite[varg.length+3]=String.valueOf(indexvalue);
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
		parameters.add(new GetRequiredParameters(Keywords.correctanswers,"text", true, 3133,dep,"",2));
		parameters.add(new GetRequiredParameters("", "note", false, 3134, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.pointforcorrect,"text", true, 3135,dep,"",2));
		parameters.add(new GetRequiredParameters(Keywords.pointforerrated,"text", true, 3136,dep,"",2));
		parameters.add(new GetRequiredParameters(Keywords.pointformissing,"text", true, 3137,dep,"",2));
		parameters.add(new GetRequiredParameters(Keywords.lowlimit,"text", false, 3138,dep,"",2));
		parameters.add(new GetRequiredParameters(Keywords.uplimit,"text", false, 3139,dep,"",2));
		parameters.add(new GetRequiredParameters("", "note", false, 3140, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.divideby3, "checkbox", false, 3145, dep, "", 2));
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
		retprocinfo[1]="3132";
		return retprocinfo;
	}
}
class CorrectAnswer
{
	TreeMap<String, double[]> value;
	public CorrectAnswer()
	{
		value=new TreeMap<String, double[]>();
	}
	public void init(String cval)
	{
		if (cval.indexOf("&")>=0)
		{
			String[] partcval=cval.split("&");
			for (int i=0; i<partcval.length; i++)
			{
				partcval[i]=partcval[i].trim().toUpperCase();
				double[] tt=new double[2];
				tt[0]=0;
				tt[1]=0;
				value.put(partcval[i], tt);
			}
		}
		else
		{
			cval=cval.trim().toUpperCase();
			double[] tt=new double[2];
			tt[0]=0;
			tt[1]=0;
			value.put(cval, tt);
		}
	}
	public void addCorrected(String tempval, boolean isup)
	{
		if (isup)
		{
			double[] tempans=value.get(tempval.toUpperCase());
			tempans[0]=tempans[0]+1;
			value.put(tempval.toUpperCase(), tempans);
		}
		else
		{
			double[] tempans=value.get(tempval.toUpperCase());
			tempans[1]=tempans[1]+1;
			value.put(tempval.toUpperCase(), tempans);
		}
	}
	public TreeMap<String, double[]> getresult()
	{
		return value;
	}
}
class NoCorrectAnswer
{
	TreeMap<String, double[]> value;
	public NoCorrectAnswer()
	{
		value=new TreeMap<String, double[]>();
	}
	public void addErrated(String tempval, boolean isup)
	{
		if (value.get(tempval.toUpperCase())==null)
		{
			double[] tempans=new double[2];
			tempans[0]=0;
			tempans[1]=0;
			value.put(tempval.toUpperCase(), tempans);
		}
		if (isup)
		{
			double[] tempans=value.get(tempval.toUpperCase());
			tempans[0]=tempans[0]+1;
			value.put(tempval.toUpperCase(), tempans);
		}
		else
		{
			double[] tempans=value.get(tempval.toUpperCase());
			tempans[1]=tempans[1]+1;
			value.put(tempval.toUpperCase(), tempans);
		}
	}
	public TreeMap<String, double[]> getresult()
	{
		return value;
	}
}