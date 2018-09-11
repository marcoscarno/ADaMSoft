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

import ADaMSoft.algorithms.LogisticEvaluator;
import ADaMSoft.algorithms.VarGroupModalities;
import ADaMSoft.dataaccess.DictionaryReader;
import ADaMSoft.dataaccess.DataReader;
import ADaMSoft.dataaccess.DataWriter;
import ADaMSoft.dataaccess.GroupedMatrix2Dfile;

import ADaMSoft.utilities.GetRequiredParameters;
import ADaMSoft.utilities.VariableUtilities;
import ADaMSoft.utilities.DataSetUtilities;
import ADaMSoft.utilities.ValuesParser;
import ADaMSoft.utilities.ObjectTransformer;
import ADaMSoft.utilities.StepUtilities;
import ADaMSoft.keywords.Keywords;


import java.util.Enumeration;
import java.util.Hashtable;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Vector;
import java.util.TreeSet;
import java.util.Iterator;

import cern.jet.stat.Probability;

/**
* This is the procedure that implements the linear logistic regression
* @author marco.scarno@gmail.com
* @date 14/02/2017
*/
public class ProcLogistic extends ObjectTransformer implements RunStep
{
	/**
	* Starts the execution of Proc Logistic and returns the corresponding message
	*/
	@SuppressWarnings("unused")
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		Vector<StepResult> result = new Vector<StepResult>();
		boolean novgconvert=false;
		boolean noclforvg=false;
		boolean orderclbycode=false;
		boolean noint=false;
		String [] requiredparameters=new String[] {Keywords.OUTE.toLowerCase(), Keywords.OUT.toLowerCase(), Keywords.dict, Keywords.varx, Keywords.vary};
		String [] optionalparameters=new String[] {Keywords.OUT.toLowerCase()+"odds", Keywords.varclasswitheffectcode, Keywords.varclass, Keywords.referencevalues, Keywords.vargroup, Keywords.weight, Keywords.where, Keywords.noint, Keywords.iterations, Keywords.successvalue, Keywords.replace, Keywords.novgconvert, Keywords.noclforvg, Keywords.orderclbycode, Keywords.usememory};
		Keywords.percentage_total=0;
		Keywords.percentage_done=0;
		StepUtilities steputilities=new StepUtilities();
		if (!steputilities.checkParameters(requiredparameters, optionalparameters, parameters))
		{
			return new Result(steputilities.getMessage(), false, null);
		}
		noint =(parameters.get(Keywords.noint)!=null);
		String iter=(String)parameters.get(Keywords.iterations.toLowerCase());
		String replace =(String)parameters.get(Keywords.replace);

		String tempvarx=(String)parameters.get(Keywords.varx.toLowerCase());
		String tempvary=(String)parameters.get(Keywords.vary.toLowerCase());
		String tempvargroup=(String)parameters.get(Keywords.vargroup.toLowerCase());
		String weight=(String)parameters.get(Keywords.weight.toLowerCase());
		String successvalue=(String)parameters.get(Keywords.successvalue.toLowerCase());
		String filetowrite_matrices=(String)parameters.get("filetowrite_matrices");
		if (iter==null)
			iter="25";

		novgconvert =(parameters.get(Keywords.novgconvert)!=null);
		orderclbycode =(parameters.get(Keywords.orderclbycode)!=null);
		noclforvg =(parameters.get(Keywords.noclforvg)!=null);

		boolean usememory=(parameters.get(Keywords.usememory)!=null);
		String varclasswitheffectcode=(String)parameters.get(Keywords.varclasswitheffectcode.toLowerCase());
		boolean onemodel=true;
		if (varclasswitheffectcode!=null)
			onemodel=false;

		if (successvalue!=null)
			successvalue=successvalue.trim();

		int niter=string2int(iter);

		DataWriter dw=new DataWriter(parameters, Keywords.OUTE.toLowerCase());
		if (!dw.getmessage().equals(""))
			return new Result(dw.getmessage(), false, null);

		DataWriter dwi=new DataWriter(parameters, Keywords.OUT.toLowerCase());
		if (!dwi.getmessage().equals(""))
			return new Result(dwi.getmessage(), false, null);

		DataWriter dwo=null;
		boolean isouto =(parameters.get(Keywords.OUT.toLowerCase()+"odds")!=null);
		if (isouto)
		{
			dwo=new DataWriter(parameters, Keywords.OUT.toLowerCase()+"odds");
			if (!dwo.getmessage().equals(""))
				return new Result(dwo.getmessage(), false, null);
		}
		if (!isouto) onemodel=true;

		DictionaryReader dict = (DictionaryReader)parameters.get(Keywords.dict);

		VariableUtilities varu=new VariableUtilities(dict, tempvargroup, null, weight, tempvarx, tempvary);
		if (varu.geterror())
			return new Result(varu.getmessage(), false, null);

		String[] varx=varu.getrowvar();
		Vector<Integer> varxinfo=new Vector<Integer>();
		for (int i=0; i<varx.length; i++)
		{
			varxinfo.add(new Integer(0));
		}

		String tempvarclass=(String)(parameters.get(Keywords.varclass));
		String tempreferencevalues=(String)(parameters.get(Keywords.referencevalues));
		if (tempvarclass==null && tempreferencevalues!=null)
			return new Result("%3292%<br>\n", false, null);
		if (isouto && tempvarclass==null)
			return new Result("%3318%<br>\n", false, null);

		String[] varclass=null;
		Vector<String> referencevalues=null;
		int posvc=0;
		if (tempvarclass!=null)
		{
			String[] tvarclass=null;
			try
			{
				tvarclass=tempvarclass.split(" ");
				varclass=new String[tvarclass.length];
			}
			catch (Exception e)
			{
				return new Result("%3293%<br>\n", false, null);
			}
			String notexistv="";
			posvc=0;
			for (int j=0; j<varx.length; j++)
			{
				for (int i=0; i<tvarclass.length; i++)
				{
					if (tvarclass[i].equalsIgnoreCase(varx[j]))
					{
						varxinfo.set(j, new Integer(1));
						varclass[posvc]=varx[j];
						posvc++;
						tvarclass[i]="";
						break;
					}
				}
			}
			for (int i=0; i<tvarclass.length; i++)
			{
				if (!tvarclass[i].equals("")) notexistv=notexistv+tvarclass[i]+" ";
			}
			if (!notexistv.equals("")) return new Result("%3296% ("+notexistv.trim()+")<br>\n", false, null);
			referencevalues=new Vector<String>();
			for (int i=0; i<varclass.length; i++)
			{
				referencevalues.add("");
			}
		}
		String[] vclassec=null;
		if (varclasswitheffectcode!=null)
		{
			String[] tvarclass=null;
			try
			{
				tvarclass=varclasswitheffectcode.split(" ");
				vclassec=new String[tvarclass.length];
			}
			catch (Exception e)
			{
				return new Result("%3376%<br>\n", false, null);
			}
			String notexistv="";
			boolean exvcec=false;
			for (int i=0; i<tvarclass.length; i++)
			{
				exvcec=false;
				for (int j=0; j<varclass.length; j++)
				{
					if (tvarclass[i].equalsIgnoreCase(varclass[j]))
					{
						exvcec=true;
						break;
					}
				}
				if (!exvcec) notexistv=notexistv+tvarclass[i]+" ";
			}
			if (!notexistv.equals(""))
				return new Result("%3378% ("+notexistv.trim()+")<br>\n", false, null);
			posvc=0;
			notexistv="";
			for (int j=0; j<varx.length; j++)
			{
				for (int i=0; i<tvarclass.length; i++)
				{
					if (tvarclass[i].equalsIgnoreCase(varx[j]))
					{
						varxinfo.set(j, new Integer(2));
						vclassec[posvc]=varx[j];
						posvc++;
						tvarclass[i]="";
						break;
					}
				}
			}
			for (int i=0; i<tvarclass.length; i++)
			{
				if (!tvarclass[i].equals("")) notexistv=notexistv+tvarclass[i]+" ";
			}
			if (!notexistv.equals("")) return new Result("%3377% ("+notexistv.trim()+")<br>\n", false, null);
		}
		HashSet<String> vclassece=null;
		if (vclassec!=null)
		{
			vclassece=new HashSet<String>();
			for (int i=0; i<vclassec.length; i++)
			{
				vclassece.add(vclassec[i].toLowerCase());
			}
		}

		if (tempreferencevalues!=null)
		{
			boolean existcv=false;
			String notexistv="";
			try
			{
				String[] tempv=tempreferencevalues.split(",");
				for (int i=0; i<tempv.length; i++)
				{
					tempv[i]=tempv[i].trim();
					String[] parts=tempv[i].split("=");
					String[] refval=new String[2];
					refval[0]=parts[0].trim();
					refval[1]="";
					for (int p=1; p<parts.length; p++)
					{
						refval[1]=refval[1]+parts[p];
						if (p<parts.length-1) refval[1]=refval[1]+"=";
					}
					refval[1]=refval[1].trim();
					existcv=false;
					for (int j=0; j<varclass.length; j++)
					{
						if (varclass[j].equalsIgnoreCase(refval[0]))
						{
							referencevalues.set(j, refval[1]);
							existcv=true;
							break;
						}
					}
					if (!existcv ) notexistv=notexistv+refval[0]+" ";
				}
			}
			catch (Exception e)
			{
				return new Result("%3294%<br>\n", false, null);
			}
			if (!notexistv.equals("")) return new Result("%3295% ("+notexistv.trim()+")<br>\n", false, null);
		}

		String[] varg=varu.getgroupvar();

		if (tempreferencevalues==null && varclasswitheffectcode!=null)
			result.add(new LocalMessageGetter("%3439%<br>\n"));

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
		if (tempvarclass!=null && tempreferencevalues==null)
		{
			result.add(new LocalMessageGetter("%3297%<br>\n"));
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

		String keyword="Logistic "+dict.getkeyword();
		String description="Logistic "+dict.getdescription();
		String idescription="Logistic information "+dict.getdescription();
		String author=(String) parameters.get(Keywords.client_host.toLowerCase());
		String descriptiono="Logistic odds"+dict.getdescription();

		int[] grouprule=varu.getgroupruleforsel();
		int[] rowrule=varu.getrowruleforsel();
		int[] colrule=varu.getcolruleforsel();
		int[] weightrule=varu.getweightruleforsel();

		int nvar=varx.length+1;
		if (noint)
			nvar=nvar-1;

		ValuesParser vp=new ValuesParser(null, grouprule, null, rowrule, colrule, weightrule);

		Hashtable<Vector<String>, Vector<Hashtable<String, Double>>> freq_for_class=new Hashtable<Vector<String>, Vector<Hashtable<String, Double>>>();

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

		String[] values=null;
		int validgroup=0;
		boolean yvalid=true;
		String[] varxvalues=null;
		double[] varyvalues=new double[1];;
		double weightvalue=Double.NaN;
		boolean ismissing=false;
		double[] ww=new double[1];
		String[] svaryvalues=new String[0];
		int nvalidy=0;
		boolean yok=false;
		int tempvx=0;
		double tempdv=0;
		int nvalidy0=0;
		int nvalidy1=0;
		int pointer_class=0;
		while (!data.isLast())
		{
			yok=false;
			varyvalues[0]=Double.NaN;
			values = data.getRecord();
			if (values!=null)
			{
				Vector<String> realvargroupvalues=new Vector<String>();
				if (novgconvert)
					realvargroupvalues=vp.getorigvargroup(values);
				else
					realvargroupvalues=vp.getvargroup(values);
				ismissing=false;
				varxvalues=vp.getrowvar(values);
				for (int i=0; i<varxinfo.size(); i++)
				{
					tempvx=(varxinfo.get(i)).intValue();
					if (varxvalues[i].equals("")) ismissing=true;
					else
					{
						if (tempvx==0)
						{
							tempdv=Double.NaN;
							try
							{
								tempdv=Double.parseDouble(varxvalues[i]);
							}
							catch (Exception ey)
							{
								ismissing=true;
							}
							if (Double.isNaN(tempdv)) ismissing=true;
						}
					}
				}
				svaryvalues=vp.getcolvar(values);
				if (!svaryvalues[0].equals(""))
				{
					if (successvalue==null)
					{
						try
						{
							varyvalues[0]=Double.parseDouble(svaryvalues[0]);
							nvalidy=1;
						}
						catch (Exception ey) {}
					}
					else
					{
						if (svaryvalues[0].equalsIgnoreCase(successvalue))
							varyvalues[0]=1;
						else
							varyvalues[0]=0;
					}
				}
				weightvalue=vp.getweight(values);
				if (!Double.isNaN(varyvalues[0]))
				{
					if (varyvalues[0]==0)
						yok=true;
					if (varyvalues[0]==1)
						yok=true;
					if (!yok)
						yvalid=false;
				}
				if ((vp.vargroupisnotmissing(realvargroupvalues)) && (!Double.isNaN(varyvalues[0])) && (!Double.isNaN(weightvalue)) && !ismissing && yvalid)
				{
					if (varyvalues[0]==0) nvalidy0++;
					if (varyvalues[0]==1) nvalidy1++;
					nvalidy++;
					validgroup++;
					vgm.updateModalities(realvargroupvalues);
					pointer_class=0;
					if (tempvarclass!=null)
					{
						if (freq_for_class.get(realvargroupvalues)==null)
						{
							Vector<Hashtable<String, Double>> temp_freq_for_classs=new Vector<Hashtable<String, Double>>();
							for (int i=0; i<varclass.length; i++)
							{
								Hashtable<String, Double> tt=new Hashtable<String, Double>();
								temp_freq_for_classs.add(tt);
							}
							for (int i=0; i<varxinfo.size(); i++)
							{
								tempvx=(varxinfo.get(i)).intValue();
								if (tempvx!=0)
								{
									Hashtable<String, Double> tt=temp_freq_for_classs.get(pointer_class);
									if (tt.get(varxvalues[i])==null) tt.put(varxvalues[i], new Double(weightvalue));
									else
									{
										tempdv=(tt.get(varxvalues[i])).doubleValue();
										tt.put(varxvalues[i], new Double(tempdv+weightvalue));
									}
									pointer_class++;
								}
							}
							freq_for_class.put(realvargroupvalues, temp_freq_for_classs);
						}
						else
						{
							Vector<Hashtable<String, Double>> temp_freq_for_class=freq_for_class.get(realvargroupvalues);
							for (int i=0; i<varxinfo.size(); i++)
							{
								tempvx=(varxinfo.get(i)).intValue();
								if (tempvx!=0)
								{
									Hashtable<String, Double> tt=temp_freq_for_class.get(pointer_class);
									if (tt.get(varxvalues[i])==null) tt.put(varxvalues[i], new Double(weightvalue));
									else
									{
										tempdv=(tt.get(varxvalues[i])).doubleValue();
										tempdv=tempdv+weightvalue;
										tt.put(varxvalues[i], new Double(tempdv));
									}
									pointer_class++;
								}
							}
						}
					}
				}
			}
		}
		data.close();
		if ((validgroup==0) && (where!=null))
		{
			return new Result("%894%<br>\n", false, null);
		}
		if ((validgroup==0) && (where==null))
		{
			return new Result("%894%<br>\n", false, null);
		}
		if (!yvalid)
		{
			return new Result("%998%<br>\n", false, null);
		}
		if (nvalidy==0)
		{
			return new Result("%1754%<br>\n", false, null);
		}
		if (nvalidy0==0)
		{
			return new Result("%3313%<br>\n", false, null);
		}
		if (nvalidy1==0)
		{
			return new Result("%3314%<br>\n", false, null);
		}

		vgm.calculate();

		int totalgroupmodalities=vgm.getTotal();

		if (totalgroupmodalities==0)
			totalgroupmodalities=1;

		Hashtable<Vector<String>, Vector<String>> refvalforg=new Hashtable<Vector<String>, Vector<String>>();
		Hashtable<Vector<String>, Vector<Vector<String>>> other_valforg=new Hashtable<Vector<String>, Vector<Vector<String>>>();

		String tempgf="";
		String msg_no_rv="";
		boolean foundrefval=false;
		double maxtv=-1.7976931348623157E308;
		String refmv="";
		String msg_class_var="";
		int normalnumclass=-1;
		Vector<Integer> numdvfc=new Vector<Integer>();
		String realrefv="";
		if (tempvarclass!=null)
		{
			msg_class_var="%3300%<br>\n";
			for (int i=0; i<varclass.length; i++)
			{
				numdvfc.add(new Integer(-1));
			}
			for (int i=0; i<totalgroupmodalities; i++)
			{
				Vector<String> rifmodgroup=vgm.getvectormodalities(i);
				refmv="";
				if (varg.length>0)
				{
					for (int j=0; j<rifmodgroup.size(); j++)
					{
						if (rifmodgroup.get(j)!=null)
						{
							refmv=refmv+rifmodgroup.get(j);
							if (j<(rifmodgroup.size()-1)) refmv=refmv+" ";
						}
					}
				}
				if (refmv==null)
					refmv="";
				if (!refmv.equals(""))
					msg_class_var=msg_class_var+"%3301%: "+refmv+"\n";
				Vector<Hashtable<String, Double>> tf=freq_for_class.get(rifmodgroup);
				Vector<String> real_ref=new Vector<String>();
				for (int j=0; j<tf.size(); j++)
				{
					Hashtable<String, Double> ttf=tf.get(j);
					normalnumclass=(numdvfc.get(j)).intValue();
					if (normalnumclass==-1)
						numdvfc.set(j, new Integer(ttf.size()));
					else if (normalnumclass!=ttf.size())
					{
						msg_no_rv="";
						if (varg.length>0)
						{
							for (int h=0; h<rifmodgroup.size(); h++)
							{
								msg_no_rv=msg_no_rv+rifmodgroup.get(h);
								if (h<(rifmodgroup.size()-1)) msg_no_rv=msg_no_rv+" ";
							}
						}
						if (!msg_no_rv.equals("")) msg_no_rv=" (%3299%: "+msg_no_rv.trim()+")";
						msg_no_rv="%3307%; %3308%: "+String.valueOf(ttf.size())+", %3309%: "+normalnumclass+", %3310%= "+varclass[j]+msg_no_rv;
						return new Result(msg_no_rv+"\n", false, null);
					}
					else if (normalnumclass==1)
					{
						msg_no_rv="";
						if (varg.length>0)
						{
							for (int h=0; h<rifmodgroup.size(); h++)
							{
								msg_no_rv=msg_no_rv+rifmodgroup.get(h);
								if (h<(rifmodgroup.size()-1)) msg_no_rv=msg_no_rv+" ";
							}
						}
						if (!msg_no_rv.equals("")) msg_no_rv=" (%3299%: "+msg_no_rv.trim()+")";
						msg_no_rv="%3311%; %3310%= "+varclass[j]+msg_no_rv;
						return new Result(msg_no_rv+"<br>\n", false, null);
					}
					tempgf=referencevalues.get(j);
					realrefv=tempgf;
					foundrefval=false;
					if (!tempgf.equals(""))
					{
						if (ttf.get(tempgf)==null)
						{
							msg_no_rv="";
							if (varg.length>0)
							{
								for (int h=0; h<rifmodgroup.size(); h++)
								{
									msg_no_rv=msg_no_rv+rifmodgroup.get(h);
									if (h<(rifmodgroup.size()-1)) msg_no_rv=msg_no_rv+" ";
								}
							}
							if (!msg_no_rv.equals("")) msg_no_rv=" (%3299%: "+msg_no_rv.trim()+")";
							msg_no_rv="%3298% "+varclass[j]+"="+tempgf+msg_no_rv;
							return new Result(msg_no_rv+"\n", false, null);
						}
						else
						{
							real_ref.add(tempgf);
							foundrefval=true;
							msg_class_var=msg_class_var+"%3302%: "+varclass[j].toUpperCase()+"\n";
							for (Enumeration<String> en=ttf.keys(); en.hasMoreElements();)
							{
								tempgf=en.nextElement();
								tempdv=(ttf.get(tempgf)).doubleValue();
								msg_class_var=msg_class_var+"%3303%: "+tempgf+", %3304%: "+String.valueOf(tempdv)+"\n";
							}
							msg_class_var=msg_class_var+"%3306%: "+realrefv+"\n";
							if (vclassece!=null)
							{
								if (vclassece.contains(varclass[j].toLowerCase()))
									msg_class_var=msg_class_var+"%3379%\n";
								else
									msg_class_var=msg_class_var+"%3380%\n";
							}
						}
					}
					if (!foundrefval)
					{
						maxtv=-1.7976931348623157E308;
						refmv="";
						msg_class_var=msg_class_var+"%3302%: "+varclass[j].toUpperCase()+"\n";
						for (Enumeration<String> en=ttf.keys(); en.hasMoreElements();)
						{
							tempgf=en.nextElement();
							tempdv=(ttf.get(tempgf)).doubleValue();
							if (tempdv>maxtv)
							{
								refmv=tempgf;
								maxtv=tempdv;
							}
							msg_class_var=msg_class_var+"%3303%: "+tempgf+", %3304%: "+String.valueOf(tempdv)+"\n";
						}
						real_ref.add(refmv);
						msg_class_var=msg_class_var+"%3305%: "+refmv+"\n";
						if (vclassece!=null)
						{
							if (vclassece.contains(varclass[j].toLowerCase()))
								msg_class_var=msg_class_var+"%3379%\n";
							else
								msg_class_var=msg_class_var+"%3380%\n";
						}
					}
				}
				refvalforg.put(rifmodgroup, real_ref);
			}
			result.add(new LocalMessageGetter(msg_class_var+"<br>"));
			TreeSet<String> usable=new TreeSet<String>();
			String tempgff="";
			for (int i=0; i<totalgroupmodalities; i++)
			{
				Vector<String> rifmodgroup=vgm.getvectormodalities(i);
				Vector<String> real_ref=refvalforg.get(rifmodgroup);
				Vector<Hashtable<String, Double>> tf=freq_for_class.get(rifmodgroup);
				Vector<Vector<String>> first_level=new Vector<Vector<String>>();
				for (int j=0; j<real_ref.size(); j++)
				{
					tempgf=real_ref.get(j);
					Hashtable<String, Double> ttf=tf.get(j);
					usable.clear();
					for (Enumeration<String> en=ttf.keys(); en.hasMoreElements();)
					{
						tempgff=en.nextElement();
						if (!tempgf.equals(tempgff))
							usable.add(tempgff);
					}
					Iterator<String> it_ts =usable.iterator();
					Vector<String> second_level=new Vector<String>();
					while(it_ts.hasNext())
					{
						tempgff = it_ts.next();
						second_level.add(tempgff);
					}
					first_level.add(second_level);
				}
				other_valforg.put(rifmodgroup, first_level);
			}
		}

		int totalvars=1;
		for (int i=0; i<varxinfo.size(); i++)
		{
			tempvx=(varxinfo.get(i)).intValue();
			if (tempvx==0) totalvars++;
		}

		for (int i=0; i<numdvfc.size(); i++)
		{
			normalnumclass=(numdvfc.get(i)).intValue();
			totalvars=totalvars+normalnumclass-1;
		}

		double[] doubletvar=new double[totalvars];
		double[] doubletvar_double=new double[totalvars];

		String[] valuestowrite=new String[varg.length+5];
		String tempdir=(String)parameters.get(Keywords.WorkDir);

		LogisticEvaluator el=new LogisticEvaluator(niter, totalvars);
		LogisticEvaluator el_double=null;
		if (!onemodel)
			el_double=new LogisticEvaluator(niter, totalvars);

		GroupedMatrix2Dfile valx=null;
		GroupedMatrix2Dfile valy=null;
		GroupedMatrix2Dfile valw=null;

		Hashtable<Vector<String>, Vector<double[]>> valxm=null;
		Hashtable<Vector<String>, Vector<Double>> valym=null;
		Hashtable<Vector<String>, Vector<Double>> valwm=null;

		GroupedMatrix2Dfile valx_double=null;

		Hashtable<Vector<String>, Vector<double[]>> valxm_double=null;

		if (!usememory)
		{
			valx=new GroupedMatrix2Dfile(tempdir, totalvars);
			valy=new GroupedMatrix2Dfile(tempdir, 1);
			valw=new GroupedMatrix2Dfile(tempdir, 1);
			if (!onemodel) valx_double=new GroupedMatrix2Dfile(tempdir, totalvars);
		}
		else
		{
			valxm=new Hashtable<Vector<String>, Vector<double[]>>();
			valym=new Hashtable<Vector<String>, Vector<Double>>();
			valwm=new Hashtable<Vector<String>, Vector<Double>>();
			if (!onemodel) valxm_double=new Hashtable<Vector<String>, Vector<double[]>>();
		}
		int posvcr=0;
		int ref_pos_other=0;

		if (!data.open(reqvar, replacerule, false))
			return new Result(data.getmessage(), false, null);
		if (where!=null)
		{
			if (!data.setcondition(where)) return new Result(data.getmessage(), false, null);
		}
		int records_for_gui=0;
		while (!data.isLast())
		{
			yok=false;
			varyvalues[0]=Double.NaN;
			values = data.getRecord();
			if (values!=null)
			{
				Vector<String> realvargroupvalues=null;
				if (novgconvert)
					realvargroupvalues=vp.getorigvargroup(values);
				else
					realvargroupvalues=vp.getvargroup(values);
				ismissing=false;
				varxvalues=vp.getrowvar(values);
				for (int i=0; i<varxinfo.size(); i++)
				{
					tempvx=(varxinfo.get(i)).intValue();
					if (varxvalues[i].equals("")) ismissing=true;
					else
					{
						if (tempvx==0)
						{
							tempdv=Double.NaN;
							try
							{
								tempdv=Double.parseDouble(varxvalues[i]);
							}
							catch (Exception ey)
							{
								ismissing=true;
							}
							if (Double.isNaN(tempdv)) ismissing=true;
						}
					}
				}
				svaryvalues=vp.getcolvar(values);
				if (!svaryvalues[0].equals(""))
				{
					if (successvalue==null)
					{
						try
						{
							varyvalues[0]=Double.parseDouble(svaryvalues[0]);
							nvalidy=1;
						}
						catch (Exception ey) {}
					}
					else
					{
						if (svaryvalues[0].equalsIgnoreCase(successvalue))
							varyvalues[0]=1;
						else
							varyvalues[0]=0;
					}
				}
				weightvalue=vp.getweight(values);
				if (!Double.isNaN(varyvalues[0]))
				{
					if (varyvalues[0]==0)
						yok=true;
					if (varyvalues[0]==1)
						yok=true;
					if (!yok)
						yvalid=false;
				}
				if ((vp.vargroupisnotmissing(realvargroupvalues)) && (!Double.isNaN(varyvalues[0])) && (!Double.isNaN(weightvalue)) && !ismissing && yvalid)
				{
					records_for_gui++;
					nvalidy++;
					if (tempvarclass==null)
					{
						for (int i=0; i<varxvalues.length; i++)
						{
							doubletvar[i]=Double.parseDouble(varxvalues[i]);
							if (!onemodel) doubletvar_double[i]=doubletvar[i];
						}
						if (!noint)
						{
							doubletvar[varxvalues.length]=1.0;
							if (!onemodel) doubletvar_double[varxvalues.length]=1.0;
						}
					}
					else
					{
						for (int i=0; i<totalvars; i++)
						{
							doubletvar[i]=0.0;
							if (!onemodel) doubletvar_double[i]=doubletvar[i];
						}
						if (!noint)
						{
							doubletvar[totalvars-1]=1.0;
							if (!onemodel) doubletvar_double[totalvars-1]=1.0;
						}
						posvc=0;
						posvcr=0;
						for (int i=0; i<varxinfo.size(); i++)
						{
							tempvx=(varxinfo.get(i)).intValue();
							if (tempvx==0)
							{
								doubletvar[posvc]=Double.parseDouble(varxvalues[i]);
								if (!onemodel) doubletvar_double[posvc]=doubletvar[posvc];
								posvc++;
							}
							else
							{
								Vector<Vector<String>> first_level=other_valforg.get(realvargroupvalues);
								Vector<String> second_level=first_level.get(posvcr);
								ref_pos_other=second_level.indexOf(varxvalues[i]);
								if (ref_pos_other>=0)
								{
									doubletvar[posvc+ref_pos_other]=1.0;
									if (!onemodel) doubletvar_double[posvc+ref_pos_other]=1.0;
								}
								else
								{
									for (int k=0; k<second_level.size(); k++)
									{
										if (tempvx==1) doubletvar[posvc+k]=0.0;
										else doubletvar[posvc+k]=-1.0;
										if (!onemodel) doubletvar_double[posvc+k]=0.0;
									}
								}
								posvcr++;
								posvc=posvc+second_level.size();
							}
						}
					}
					if (!usememory)
					{
						if (!valx.write(realvargroupvalues, doubletvar))
						{
							data.close();
							return new Result(valx.getMessage(), false, null);
						}
						if (!valy.write(realvargroupvalues, varyvalues))
						{
							data.close();
							return new Result(valy.getMessage(), false, null);
						}
						ww[0]=weightvalue;
						if (!valw.write(realvargroupvalues, ww))
						{
							data.close();
							return new Result(valw.getMessage(), false, null);
						}
						if (!onemodel)
						{
							if (!valx_double.write(realvargroupvalues, doubletvar_double))
							{
								data.close();
								return new Result(valx_double.getMessage(), false, null);
							}
						}
					}
					else
					{
						if (valxm.get(realvargroupvalues)==null)
						{
							Vector<double[]> temp_valxm=new Vector<double[]>();
							Vector<Double> temp_valym=new Vector<Double>();
							Vector<Double> temp_valwm=new Vector<Double>();
							double[] ttemp_valxm=new double[doubletvar.length];
							for (int i=0; i<doubletvar.length; i++)
							{
								ttemp_valxm[i]=doubletvar[i];
							}
							temp_valxm.add(ttemp_valxm);
							temp_valym.add(new Double(varyvalues[0]));
							temp_valwm.add(new Double(weightvalue));
							valxm.put(realvargroupvalues, temp_valxm);
							valym.put(realvargroupvalues, temp_valym);
							valwm.put(realvargroupvalues, temp_valwm);
							if (!onemodel)
							{
								Vector<double[]> temp_valxm_double=new Vector<double[]>();
								double[] ttemp_valxm_double=new double[doubletvar.length];
								for (int i=0; i<doubletvar_double.length; i++)
								{
									ttemp_valxm_double[i]=doubletvar_double[i];
								}
								temp_valxm_double.add(ttemp_valxm_double);
								valxm_double.put(realvargroupvalues, temp_valxm_double);
							}
						}
						else
						{
							Vector<double[]> temp_valxm=valxm.get(realvargroupvalues);
							Vector<Double> temp_valym=valym.get(realvargroupvalues);
							Vector<Double> temp_valwm=valwm.get(realvargroupvalues);
							double[] ttemp_valxm=new double[doubletvar.length];
							for (int i=0; i<doubletvar.length; i++)
							{
								ttemp_valxm[i]=doubletvar[i];
							}
							temp_valxm.add(ttemp_valxm);
							temp_valym.add(new Double(varyvalues[0]));
							temp_valwm.add(new Double(weightvalue));
							valxm.put(realvargroupvalues, temp_valxm);
							valym.put(realvargroupvalues, temp_valym);
							valwm.put(realvargroupvalues, temp_valwm);
							if (!onemodel)
							{
								Vector<double[]> temp_valxm_double=valxm_double.get(realvargroupvalues);
								double[] ttemp_valxm_double=new double[doubletvar.length];
								for (int i=0; i<doubletvar_double.length; i++)
								{
									ttemp_valxm_double[i]=doubletvar_double[i];
								}
								temp_valxm_double.add(ttemp_valxm_double);
								valxm_double.put(realvargroupvalues, temp_valxm_double);
							}
						}
					}
				}
			}
		}
		data.close();
		if ((validgroup==0) && (where!=null))
		{
			if (!usememory)
			{
				valx.closeAll();
				valy.closeAll();
				valw.closeAll();
				if (!onemodel)
				{
					valx_double.closeAll();
				}
			}
			else
			{
				valxm.clear();
				valym.clear();
				valwm.clear();
				valxm=null;
				valym=null;
				valwm=null;
				if (!onemodel)
				{
					valxm_double.clear();
					valxm_double=null;
				}
			}
			return new Result("%894%<br>\n", false, null);
		}
		if ((validgroup==0) && (where==null))
		{
			if (!usememory)
			{
				valx.closeAll();
				valy.closeAll();
				valw.closeAll();
				if (!onemodel)
				{
					valx_double.closeAll();
				}
			}
			else
			{
				valxm.clear();
				valym.clear();
				valwm.clear();
				valxm=null;
				valym=null;
				valwm=null;
				if (!onemodel)
				{
					valxm_double.clear();
					valxm_double=null;
				}
			}
			return new Result("%894%<br>\n", false, null);
		}
		if (!yvalid)
		{
			if (!usememory)
			{
				valx.closeAll();
				valy.closeAll();
				valw.closeAll();
				if (!onemodel)
				{
					valx_double.closeAll();
				}
			}
			else
			{
				valxm.clear();
				valym.clear();
				valwm.clear();
				valxm=null;
				valym=null;
				valwm=null;
				if (!onemodel)
				{
					valxm_double.clear();
					valxm_double=null;
				}
			}
			return new Result("%998%<br>\n", false, null);
		}
		if (nvalidy==0)
		{
			if (!usememory)
			{
				valx.closeAll();
				valy.closeAll();
				valw.closeAll();
				if (!onemodel)
				{
					valx_double.closeAll();
				}
			}
			else
			{
				valxm.clear();
				valym.clear();
				valwm.clear();
				valxm=null;
				valym=null;
				valwm=null;
				if (!onemodel)
				{
					valxm_double.clear();
					valxm_double=null;
				}
			}
			return new Result("%1754%<br>\n", false, null);
		}

		el.setRecords_for_gui(records_for_gui);
		if (filetowrite_matrices!=null && !usememory)
			el.printonfile(filetowrite_matrices, vgm, valx, valy, valw);

		String message="";
		if (!usememory) message=el.estimate(vgm, valx, valy, valw);
		else message=el.estimate(vgm, valxm, valym, valwm);
		if (!onemodel)
		{
			if (!usememory) el_double.estimate(vgm, valx_double, valy, valw);
			else el_double.estimate(vgm, valxm_double, valym, valwm);
		}

		if (!usememory)
		{
			valx.closeAll();
			valy.closeAll();
			valw.closeAll();
			if (!onemodel)
			{
				valx_double.closeAll();
			}
		}
		else
		{
			valxm.clear();
			valym.clear();
			valwm.clear();
			valxm=null;
			valym=null;
			valwm=null;
			if (!onemodel)
			{
				valxm_double.clear();
				valxm_double=null;
			}
		}

		if (!message.equals(""))
			return new Result(message, false, null);

		if (el.geterror())
			return new Result("%1020%<br>\n", false, null);

		if (el.getconverged())
		{
			result.add(new LocalMessageGetter("%1019%<br>\n"));
			result.add(new LocalMessageGetter("%3323%: "+String.valueOf(el.getmin_reached())+"<br>\n"));
		}

		int maxiter_done=el.getmaxiter();
		result.add(new LocalMessageGetter("%3315% "+String.valueOf(maxiter_done+1)+"/"+iter+"<br>\n"));

		Hashtable<Vector<String>, double[]> coeff=el.getfinalcoeff();
		Hashtable<Vector<String>, double[]> statistics=el.getstats();
		Hashtable<Vector<String>, double[]> finalse=el.getfinalse();
		Hashtable<Vector<String>, double[]> coeff_double=null;
		Hashtable<Vector<String>, double[]> finalse_double=null;
		if (!onemodel)
		{
			coeff_double=el_double.getfinalcoeff();
			finalse_double=el_double.getfinalse();
		}

		DataSetUtilities dsu=new DataSetUtilities();
		dsu.setreplace(replace);
		Vector<Hashtable<String, String>> groupcodelabels=vgm.getgroupcodelabels();

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
		if (!noint)
			clvar.put("0","%999%");
		if (tempvarclass!=null)
		{
			ref_pos_other=0;
			for (int i=0; i<totalgroupmodalities; i++)
			{
				Vector<String> rifmodgroup=vgm.getvectormodalities(i);
				Vector<Vector<String>> tempv=other_valforg.get(rifmodgroup);
				for (int j=0; j<tempv.size(); j++)
				{
					Vector<String> tempvv=tempv.get(j);
					for (int k=0; k<tempvv.size(); k++)
					{
						if (totalgroupmodalities>1)
						{
							if (vclassece!=null)
							{
								if (vclassece.contains(varclass[j].toLowerCase()))
									clvar.put("$G_"+String.valueOf(ref_pos_other)+"_$EC_"+varclass[j]+"="+tempvv.get(k), dict.getvarlabelfromname(varclass[j])+"= "+tempvv.get(k));
								else
									clvar.put("$G_"+String.valueOf(ref_pos_other)+"_"+varclass[j]+"="+tempvv.get(k), dict.getvarlabelfromname(varclass[j])+"= "+tempvv.get(k));
							}
							else
								clvar.put("$G_"+String.valueOf(ref_pos_other)+"_"+varclass[j]+"="+tempvv.get(k), dict.getvarlabelfromname(varclass[j])+"= "+tempvv.get(k));
						}
						else
						{
							if (vclassece!=null)
							{
								if (vclassece.contains(varclass[j].toLowerCase()))
									clvar.put("$EC_"+varclass[j]+"="+tempvv.get(k), dict.getvarlabelfromname(varclass[j])+"= "+tempvv.get(k));
								else
									clvar.put(varclass[j]+"="+tempvv.get(k), dict.getvarlabelfromname(varclass[j])+"= "+tempvv.get(k));
							}
							else clvar.put(varclass[j]+"="+tempvv.get(k), dict.getvarlabelfromname(varclass[j])+"= "+tempvv.get(k));
						}
					}
				}
				ref_pos_other++;
			}
		}

		dsu.addnewvar("varx", "%981%", Keywords.TEXTSuffix, clvar, tempmd);
		dsu.addnewvar("parameter", "%1000%", Keywords.NUMSuffix, tempmd, tempmd);
		dsu.addnewvar("se", "%1021%", Keywords.NUMSuffix, tempmd, tempmd);
		dsu.addnewvar("wchisq", "%1022%", Keywords.NUMSuffix, tempmd, tempmd);
		dsu.addnewvar("pchisq", "%1023%", Keywords.NUMSuffix, tempmd, tempmd);

		DataSetUtilities dsuo=null;
		if (isouto)
		{
			dsuo=new DataSetUtilities();
			dsuo.setreplace(replace);
			Hashtable<String, String> tempmdo=new Hashtable<String, String>();
			for (int j=0; j<varg.length; j++)
			{
				if (!noclforvg)
					dsuo.addnewvarfromolddict(dict, varg[j], groupcodelabels.get(j), tempmdo, "g_"+varg[j]);
				else
					dsuo.addnewvarfromolddict(dict, varg[j], tempmdo, tempmdo, "g_"+varg[j]);
			}
			Hashtable<String, String> clvaro=new Hashtable<String, String>();
			for (int j=0; j<varx.length; j++)
			{
				clvaro.put(varx[j], dict.getvarlabelfromname(varx[j]));
			}
			ref_pos_other=0;
			for (int i=0; i<totalgroupmodalities; i++)
			{
				Vector<String> rifmodgroup=vgm.getvectormodalities(i);
				Vector<Vector<String>> tempv=other_valforg.get(rifmodgroup);
				Vector<String> reftempv=refvalforg.get(rifmodgroup);
				for (int j=0; j<tempv.size(); j++)
				{
					Vector<String> tempvv=tempv.get(j);
					for (int k=0; k<tempvv.size(); k++)
					{
						if (totalgroupmodalities>1)
						{
							clvaro.put("G_"+String.valueOf(ref_pos_other)+"_"+varclass[j]+"="+tempvv.get(k), dict.getvarlabelfromname(varclass[j])+"= "+tempvv.get(k)+" (%3324%: "+reftempv.get(j)+")");
						}
						else
						{
							clvaro.put(varclass[j]+"="+tempvv.get(k), dict.getvarlabelfromname(varclass[j])+"= "+tempvv.get(k)+" (%3324%: "+reftempv.get(j)+")");
						}
					}
				}
				ref_pos_other++;
			}
			dsuo.addnewvar("effect", "%3319%", Keywords.TEXTSuffix, clvaro, tempmdo);
			dsuo.addnewvar("odds", "%3320%", Keywords.NUMSuffix, tempmdo, tempmdo);
			dsuo.addnewvar("low_int", "%3321%", Keywords.TEXTSuffix, tempmdo, tempmdo);
			dsuo.addnewvar("high_int", "%3322%", Keywords.TEXTSuffix, tempmdo, tempmdo);
			if (!dwo.opendatatable(dsuo.getfinalvarinfo()))
				return new Result(dwo.getmessage(), false, null);
		}

		if (!dw.opendatatable(dsu.getfinalvarinfo()))
			return new Result(dw.getmessage(), false, null);

		ref_pos_other=0;
		int ref_pos_vc=0;
		int pos_coeff=0;
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
			double[] tempcoeff=coeff.get(rifmodgroup);
			double[] tempse=finalse.get(rifmodgroup);
			ref_pos_vc=0;
			pos_coeff=0;
			for (int j=0; j<varxinfo.size(); j++)
			{
				tempvx=(varxinfo.get(j)).intValue();
				if (tempvx==0)
				{
					valuestowrite[varg.length]=varx[j];
					valuestowrite[varg.length+1]="";
					valuestowrite[varg.length+2]="";
					valuestowrite[varg.length+3]="";
					valuestowrite[varg.length+4]="";
					valuestowrite[varg.length+1]=double2String(tempcoeff[pos_coeff]);
					valuestowrite[varg.length+2]=double2String(tempse[pos_coeff]);
					if ((!Double.isNaN(tempse[j])) && (!Double.isNaN(tempcoeff[pos_coeff])))
					{
						double chisq=Math.pow((tempcoeff[pos_coeff]/tempse[pos_coeff]),2);
						if (!Double.isNaN(chisq))
						{
							try
							{
								valuestowrite[varg.length+3]=double2String(chisq);
								double pval=1-Probability.chiSquare(1, chisq);
								valuestowrite[varg.length+4]=double2String(pval);
							}
							catch (Exception ee) {}
						}
					}
					pos_coeff++;
					dw.write(valuestowrite);
				}
				else
				{
					Vector<Vector<String>> tempv=other_valforg.get(rifmodgroup);
					Vector<String> tempvv=tempv.get(ref_pos_vc);
					for (int h=0; h<tempvv.size(); h++)
					{
						if (totalgroupmodalities>1)
						{
							if (vclassece!=null)
							{
								if (vclassece.contains(varclass[ref_pos_vc].toLowerCase()))
									valuestowrite[varg.length]="$G_"+String.valueOf(ref_pos_other)+"_$EC_"+varclass[ref_pos_vc]+"="+tempvv.get(h);
								else
									valuestowrite[varg.length]="$G_"+String.valueOf(ref_pos_other)+"_"+varclass[ref_pos_vc]+"="+tempvv.get(h);
							}
							else
								valuestowrite[varg.length]="$G_"+String.valueOf(ref_pos_other)+"_"+varclass[ref_pos_vc]+"="+tempvv.get(h);
						}
						else
						{
							if (vclassece!=null)
							{
								if (vclassece.contains(varclass[ref_pos_vc].toLowerCase()))
									valuestowrite[varg.length]="$EC_"+varclass[ref_pos_vc]+"="+tempvv.get(h);
								else
									valuestowrite[varg.length]=varclass[ref_pos_vc]+"="+tempvv.get(h);
							}
							else
								valuestowrite[varg.length]=varclass[ref_pos_vc]+"="+tempvv.get(h);
						}
						valuestowrite[varg.length+1]="";
						valuestowrite[varg.length+2]="";
						valuestowrite[varg.length+3]="";
						valuestowrite[varg.length+4]="";
						valuestowrite[varg.length+1]=double2String(tempcoeff[pos_coeff]);
						valuestowrite[varg.length+2]=double2String(tempse[pos_coeff]);
						if ((!Double.isNaN(tempse[j])) && (!Double.isNaN(tempcoeff[pos_coeff])))
						{
							double chisq=Math.pow((tempcoeff[pos_coeff]/tempse[pos_coeff]),2);
							if (!Double.isNaN(chisq))
							{
								try
								{
									valuestowrite[varg.length+3]=double2String(chisq);
									double pval=1-Probability.chiSquare(1, chisq);
									valuestowrite[varg.length+4]=double2String(pval);
								}
								catch (Exception ee) {}
							}
						}
						dw.write(valuestowrite);
						pos_coeff++;
					}
					ref_pos_vc++;
				}
			}
			if (!noint)
			{
				valuestowrite[varg.length]="0";
				valuestowrite[varg.length+1]="";
				valuestowrite[varg.length+2]="";
				valuestowrite[varg.length+3]="";
				valuestowrite[varg.length+4]="";
				valuestowrite[varg.length+1]=double2String(tempcoeff[pos_coeff]);
				valuestowrite[varg.length+2]=double2String(tempse[pos_coeff]);
				if ((!Double.isNaN(tempse[varxinfo.size()])) && (!Double.isNaN(tempcoeff[pos_coeff])))
				{
					double chisq=Math.pow((tempcoeff[pos_coeff]/tempse[pos_coeff]),2);
					if (!Double.isNaN(chisq))
					{
						try
						{
							valuestowrite[varg.length+3]=double2String(chisq);
							double pval=1-Probability.chiSquare(1, chisq);
							valuestowrite[varg.length+4]=double2String(pval);
						}
						catch (Exception ee) {}
					}
				}
				dw.write(valuestowrite);
			}
			ref_pos_other++;
		}

		if (isouto)
		{
			String[] valuestowriteo=new String[varg.length+4];
			ref_pos_other=0;
			ref_pos_vc=0;
			int dim_coeff=0;
			int start_coeff=0;
			Vector<double[]> val_odds=new Vector<double[]>();
			Vector<String> ref_odds=new Vector<String>();
			double def_odds=0;
			double def_low=0;
			double def_high=0;
			for (int i=0; i<totalgroupmodalities; i++)
			{
				val_odds.clear();
				ref_odds.clear();
				Vector<String> rifmodgroup=vgm.getvectormodalities(i);
				for (int j=0; j<rifmodgroup.size(); j++)
				{
					String groupvalue=rifmodgroup.get(j);
					if (groupvalue!=null)
					{
						if (!noclforvg)
							valuestowriteo[j]=vgm.getcode(j, groupvalue);
						else
							valuestowriteo[j]=groupvalue;
					}
				}
				double[] tempcoeff=coeff.get(rifmodgroup);
				double[] tempse=finalse.get(rifmodgroup);
				if (!onemodel)
				{
					tempcoeff=coeff_double.get(rifmodgroup);
					tempse=finalse_double.get(rifmodgroup);
				}
				ref_pos_vc=0;
				pos_coeff=0;
				for (int j=0; j<varxinfo.size(); j++)
				{
					tempvx=(varxinfo.get(j)).intValue();
					dim_coeff=0;
					if (tempvx==0) pos_coeff++;
					else
					{
						Vector<Vector<String>> tempv=other_valforg.get(rifmodgroup);
						Vector<String> tempvv=tempv.get(ref_pos_vc);
						for (int h=0; h<tempvv.size(); h++)
						{
							if (totalgroupmodalities>1)
								ref_odds.add("G_"+String.valueOf(ref_pos_other)+"_"+varclass[ref_pos_vc]+"="+tempvv.get(h));
							else
								ref_odds.add(varclass[ref_pos_vc]+"="+tempvv.get(h));
							double[] t_v_o=new double[2];
							t_v_o[0]=tempcoeff[pos_coeff];
							t_v_o[1]=tempse[pos_coeff];
							val_odds.add(t_v_o);
							pos_coeff++;
						}
						for (int k=0; k<val_odds.size(); k++)
						{
							valuestowriteo[varg.length]=ref_odds.get(k);
							double[] ttvo=val_odds.get(k);
							def_odds=ttvo[0];
							def_low=ttvo[0]-ttvo[1]*1.96;
							def_high=ttvo[0]+ttvo[1]*1.96;
							valuestowriteo[varg.length+1]=double2String(Math.exp(def_odds));
							if (def_low>=-9)
								valuestowriteo[varg.length+2]=double2String(Math.exp(def_low));
							else
								valuestowriteo[varg.length+2]="<0.00001";
							if (def_high<=6.906)
								valuestowriteo[varg.length+3]=double2String(Math.exp(def_high));
							else
								valuestowriteo[varg.length+3]=">999.000";
							dwo.write(valuestowriteo);
						}
						val_odds.clear();
						ref_odds.clear();
					}
				}
				ref_pos_other++;
			}
		}

		DataSetUtilities idsu=new DataSetUtilities();
		idsu.setreplace(replace);
		for (int j=0; j<varg.length; j++)
		{
			idsu.addnewvarfromolddict(dict, varg[j], groupcodelabels.get(j), tempmd, "g_"+varg[j]);
		}
		Hashtable<String, String> clt=new Hashtable<String, String>();
		clt.put("0", "%1024%");
		clt.put("1", "%1025%");
		clt.put("2", "%1026%");
		clt.put("3", "%1027%");
		clt.put("4", "%1030%");
		clt.put("5", "%1031%");
		clt.put("6", "%1034%");
		clt.put("7", "%1035%");
		clt.put("8", "%2706%");
		clt.put("9", "%2707%");
		clt.put("10", "%2708%");
		idsu.addnewvar("Info", "%1028%", Keywords.TEXTSuffix, clt, tempmd);
		idsu.addnewvar("value", "%1029%", Keywords.NUMSuffix, tempmd, tempmd);

		if (!dwi.opendatatable(idsu.getfinalvarinfo()))
			return new Result(dwi.getmessage(), false, null);

		String[] ivaluestowrite=new String[varg.length+2];
		for (int i=0; i<totalgroupmodalities; i++)
		{
			Vector<String> rifmodgroup=vgm.getvectormodalities(i);
			for (int j=0; j<rifmodgroup.size(); j++)
			{
				String groupvalue=rifmodgroup.get(j);
				if (groupvalue!=null)
				{
					if (!noclforvg)
						ivaluestowrite[j]=vgm.getcode(j, groupvalue);
					else
						ivaluestowrite[j]=groupvalue;
				}
			}
			double[] stat=statistics.get(rifmodgroup);
			ivaluestowrite[varg.length]="0";
			ivaluestowrite[varg.length+1]=double2String(stat[3]);
			dwi.write(ivaluestowrite);
			ivaluestowrite[varg.length]="1";
			ivaluestowrite[varg.length+1]=double2String(stat[0]);
			dwi.write(ivaluestowrite);
			ivaluestowrite[varg.length]="2";
			ivaluestowrite[varg.length+1]=double2String(stat[1]);
			dwi.write(ivaluestowrite);
			ivaluestowrite[varg.length]="3";
			ivaluestowrite[varg.length+1]=double2String(stat[2]);
			dwi.write(ivaluestowrite);
			ivaluestowrite[varg.length]="4";
			ivaluestowrite[varg.length+1]=double2String(stat[4]);
			dwi.write(ivaluestowrite);
			ivaluestowrite[varg.length]="5";
			ivaluestowrite[varg.length+1]=double2String(stat[5]);
			dwi.write(ivaluestowrite);
			ivaluestowrite[varg.length]="6";
			ivaluestowrite[varg.length+1]=double2String(stat[6]);
			dwi.write(ivaluestowrite);
			ivaluestowrite[varg.length]="7";
			ivaluestowrite[varg.length+1]=double2String(stat[7]);
			dwi.write(ivaluestowrite);
			ivaluestowrite[varg.length]="8";
			ivaluestowrite[varg.length+1]=double2String(stat[8]);
			dwi.write(ivaluestowrite);
			ivaluestowrite[varg.length]="9";
			ivaluestowrite[varg.length+1]=double2String(stat[9]);
			dwi.write(ivaluestowrite);
			ivaluestowrite[varg.length]="10";
			ivaluestowrite[varg.length+1]=double2String(stat[10]);
			dwi.write(ivaluestowrite);
		}

		Hashtable<String, String> othertableinfo=new Hashtable<String, String>();
		othertableinfo.put(Keywords.vary, tempvary);
		if (successvalue!=null)
			othertableinfo.put(Keywords.successvalue, successvalue);

		boolean resclose=dw.close();
		if (!resclose)
			return new Result(dw.getmessage(), false, null);

		resclose=dwi.close();
		if (!resclose)
			return new Result(dwi.getmessage(), false, null);

		if (isouto)
		{
			boolean rescloseo=dwo.close();
			if (!rescloseo)
				return new Result(dwo.getmessage(), false, null);
		}

		Vector<Hashtable<String, String>> tablevariableinfo=dw.getVarInfo();
		Hashtable<String, String> datatableinfo=dw.getTableInfo();

		Vector<Hashtable<String, String>> itablevariableinfo=dwi.getVarInfo();
		Hashtable<String, String> idatatableinfo=dwi.getTableInfo();

		result.add(new LocalDictionaryWriter(dw.getdictpath(), keyword, description, author, dw.gettabletype(),
		datatableinfo, dsu.getfinalvarinfo(), tablevariableinfo, dsu.getfinalcl(), dsu.getfinalmd(), othertableinfo));

		result.add(new LocalDictionaryWriter(dwi.getdictpath(), keyword, idescription, author, dwi.gettabletype(),
		idatatableinfo, idsu.getfinalvarinfo(), itablevariableinfo, idsu.getfinalcl(), idsu.getfinalmd(), null));

		if (isouto)
		{
			Vector<Hashtable<String, String>> itablevariableinfoo=dwo.getVarInfo();
			Hashtable<String, String> idatatableinfoo=dwo.getTableInfo();
			result.add(new LocalDictionaryWriter(dwo.getdictpath(), keyword, descriptiono, author, dwo.gettabletype(),
			idatatableinfoo, dsuo.getfinalvarinfo(), itablevariableinfoo, dsuo.getfinalcl(), dsuo.getfinalmd(), null));
		}

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
		parameters.add(new GetRequiredParameters(Keywords.OUT.toLowerCase()+"=", "setting=out", true, 896, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.OUTE.toLowerCase()+"=", "setting=out", true, 890, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.OUT.toLowerCase()+"odds=", "setting=out", false, 3317, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.viewout, "checkbox", false, 1612, dep, "", 1));
		dep = new String[1];
		dep[0]=Keywords.dict;
		parameters.add(new GetRequiredParameters(Keywords.varx, "vars=all", true, 888, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.varclass, "vars=all", false, 3289, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.referencevalues, "text", false, 3290, dep, "", 2));
		parameters.add(new GetRequiredParameters("", "note", false, 3291, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.varclasswitheffectcode, "vars=all", false, 3374, dep, "", 2));
		parameters.add(new GetRequiredParameters("", "note", false, 3375, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.vary, "vars=all",  true, 982, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.successvalue, "text", false, 1753, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.vargroup, "vars=all", false, 655, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.weight, "vars=all", false, 656, dep, "", 2));
		dep = new String[0];
		parameters.add(new GetRequiredParameters(Keywords.where,"text", false, 2805,dep,"",2));
		parameters.add(new GetRequiredParameters("", "note", false, 2806, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.noint, "checkbox", false, 892, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.iterations, "text", false, 3316, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.novgconvert, "checkbox", false, 2227, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.noclforvg, "checkbox", false, 2229, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.orderclbycode, "checkbox", false, 2231, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.usememory, "checkbox", false, 3312, dep, "", 2));
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
		retprocinfo[1]="997";
		return retprocinfo;
	}
}
