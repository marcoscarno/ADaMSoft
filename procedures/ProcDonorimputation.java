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

import ADaMSoft.dataaccess.DataReader;
import ADaMSoft.dataaccess.DataWriter;
import ADaMSoft.dataaccess.DictionaryReader;
import ADaMSoft.dataaccess.FastTempDataSet;
import ADaMSoft.dataaccess.GroupedFastTempDataSet;
import ADaMSoft.keywords.Keywords;
import ADaMSoft.utilities.GetRequiredParameters;
import ADaMSoft.utilities.DataSetUtilities;
import ADaMSoft.utilities.ObjectTransformer;
import ADaMSoft.utilities.StepUtilities;

import ADaMSoft.algorithms.EditsAnalyzer;


/**
* This is the procedure that implements the donor imputation
* @author marco.scarno@gmail.com
* @date 13/02/2017
*/
public class ProcDonorimputation extends ObjectTransformer implements RunStep
{
	String formatsol;
	/**
	* Starts the execution of Proc Donorimputation
	*/
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		String [] requiredparameters=new String[] {Keywords.OUT.toLowerCase(), Keywords.dict, Keywords.dict+"e"};
		String [] optionalparameters=new String[] {Keywords.dict+"r", Keywords.vargroup, Keywords.maxdonors, Keywords.mindonors, Keywords.varid, Keywords.mustmatch, Keywords.consideralsoall, Keywords.tolerance, Keywords.where, Keywords.useeuclidean, Keywords.norandomimpute, Keywords.reducedforsecondary, Keywords.OUTS.toLowerCase(), Keywords.OUTD.toLowerCase(), Keywords.replace};
		Keywords.percentage_total=0;
		Keywords.percentage_done=0;
		StepUtilities steputilities=new StepUtilities();
		if (!steputilities.checkParameters(requiredparameters, optionalparameters, parameters))
		{
			return new Result(steputilities.getMessage(), false, null);
		}
		Keywords.percentage_total=5;
		Keywords.percentage_done=0;

		boolean consideralsoall =(parameters.get(Keywords.consideralsoall)!=null);
		boolean useeuclidean=(parameters.get(Keywords.useeuclidean)!=null);
		boolean isouts =(parameters.get(Keywords.OUTS.toLowerCase())!=null);
		boolean isoutd =(parameters.get(Keywords.OUTD.toLowerCase())!=null);
		boolean reducedforsecondary =(parameters.get(Keywords.reducedforsecondary)!=null);
		if ((parameters.get(Keywords.dict+"r")==null) && (reducedforsecondary))
				return new Result("%3188%<br>\n", false, null);

		String varid =(String)parameters.get(Keywords.varid);
		if (varid!=null && !isoutd)
				return new Result("%3213%<br>\n", false, null);
		if (varid==null && isoutd)
				return new Result("%3214%<br>\n", false, null);

		DataWriter dws=null;
		boolean statcreated=false;
		if (isouts)
		{
			statcreated=true;
			dws=new DataWriter(parameters, Keywords.OUTS.toLowerCase());
			if (!dws.getmessage().equals(""))
				return new Result(dws.getmessage(), false, null);
		}

		DataWriter dwd=null;
		boolean donorcreated=false;
		if (isoutd)
		{
			donorcreated=true;
			dwd=new DataWriter(parameters, Keywords.OUTD.toLowerCase());
			if (!dwd.getmessage().equals(""))
				return new Result(dwd.getmessage(), false, null);
		}

		String[] varsid=null;
		int[] posvarid=null;
		if (varid!=null)
		{
			varsid=varid.split(" ");
			posvarid=new int[varsid.length];
		}

		boolean norandomimpute=(parameters.get(Keywords.norandomimpute)!=null);

		String replace =(String)parameters.get(Keywords.replace);

		String smaxdonors=(String)parameters.get(Keywords.maxdonors);
		String smindonors=(String)parameters.get(Keywords.mindonors);
		String mustmatch=(String)parameters.get(Keywords.mustmatch);
		String stolerance =(String)parameters.get(Keywords.tolerance);
		String vg=(String)parameters.get(Keywords.vargroup);
		String[] vargroup=new String[0];
		if (vg!=null)
		{
			vargroup=vg.split(" ");
		}
		int maxdonors=0;
		if (smaxdonors!=null)
		{
			try
			{
				maxdonors=Integer.parseInt(smaxdonors);
			}
			catch (Exception en)
			{
				return new Result("%2829%<br>\n", false, null);
			}
			if (maxdonors<0)
				return new Result("%2829%<br>\n", false, null);
		}
		int mindonors=0;
		if (smindonors!=null)
		{
			try
			{
				mindonors=Integer.parseInt(smindonors);
			}
			catch (Exception en)
			{
				return new Result("%3077%<br>\n", false, null);
			}
			if (mindonors<0)
				return new Result("%3077%<br>\n", false, null);
		}
		if (mindonors>maxdonors)
			return new Result("%3077%<br>\n", false, null);
		double tolerance=0.00001;
		if (stolerance!=null)
		{
			try
			{
				tolerance=Double.parseDouble(stolerance);
			}
			catch (Exception en)
			{
				return new Result("%2596%<br>\n", false, null);
			}
			if ((tolerance>1) || (tolerance<0))
				return new Result("%2596%<br>\n", false, null);
		}
		String[] varmatch=new String[0];
		if (mustmatch!=null)
		{
			varmatch=mustmatch.split(" ");
		}
		DictionaryReader dict = (DictionaryReader)parameters.get(Keywords.dict);
		Hashtable<String, Integer> varrefdict=new Hashtable<String, Integer>();
		int posloc=-1;
		int possol=-1;
		int postyp=-1;
		int detpos=-1;
		int testvarid=0;
		boolean type=false;
		for (int i=0; i<dict.gettotalvar(); i++)
		{
			varrefdict.put((dict.getvarname(i)).toUpperCase(), new Integer(i));
			if (dict.getvarname(i).equalsIgnoreCase("result_localize"))
			{
				posloc=i;
			}
			if (dict.getvarname(i).equalsIgnoreCase("result_localize_all"))
			{
				type=true;
				posloc=i;
			}
			if (dict.getvarname(i).equalsIgnoreCase("solution_localize"))
			{
				possol=i;
			}
			if (dict.getvarname(i).equalsIgnoreCase("solution_type"))
			{
				postyp=i;
			}
			if (dict.getvarname(i).equalsIgnoreCase("deterministic_locvars"))
			{
				detpos=i;
			}
			if (isoutd)
			{
				for (int j=0; j<varsid.length; j++)
				{
					if (dict.getvarname(i).equalsIgnoreCase(varsid[j]))
					{
						posvarid[j]=i;
						testvarid++;
						break;
					}
				}
			}
		}
		if (posloc==-1)
			return new Result("%2514%<br>\n", false, null);
		if (possol==-1)
			return new Result("%2603%<br>\n", false, null);
		if (postyp==-1)
			return new Result("%2821%<br>\n", false, null);
		if (detpos==-1)
			return new Result("%2855%<br>\n", false, null);
		if (isoutd && testvarid!=varsid.length)
		{
			String vidnotfound="";
			boolean vidnf=false;
			for (int j=0; j<varsid.length; j++)
			{
				vidnf=false;
				for (int i=0; i<dict.gettotalvar(); i++)
				{
					if (dict.getvarname(i).equalsIgnoreCase(varsid[j])) vidnf=true;
				}
				if (!vidnf) vidnotfound=vidnotfound+varsid[j]+" ";
			}
			return new Result("%3215% ("+vidnotfound.trim()+")<br>\n", false, null);
		}
		String notvg="";
		boolean noexvg=false;
		if (vargroup.length>0)
		{
			for (int j=0; j<vargroup.length; j++)
			{
				vargroup[j]=vargroup[j].toUpperCase();
				noexvg=false;
				if (varrefdict.get(vargroup[j])!=null) noexvg=true;
				if (!noexvg) notvg=notvg+" "+vargroup[j];
			}
			if (!notvg.equals("")) return new Result("%2847% ("+notvg.trim()+")<br>\n", false, null);
		}
		if (varmatch.length>0)
		{
			for (int j=0; j<varmatch.length; j++)
			{
				varmatch[j]=varmatch[j].toUpperCase();
				noexvg=false;
				if (varrefdict.get(varmatch[j])!=null) noexvg=true;
				if (!noexvg) notvg=notvg+" "+varmatch[j];
			}
			if (!notvg.equals("")) return new Result("%2834% ("+notvg.trim()+")<br>\n", false, null);
		}

		DictionaryReader dicte = (DictionaryReader)parameters.get(Keywords.dict+"e");
		int refvar=dicte.gettotalvar()-2;
		String[] varref=new String[refvar];
		int posv=0;
		String realvname="";
		String tempvname="";
		String[] tempsarray=null;
		String[] vartotake=new String[dicte.gettotalvar()];
		int[] reprule=new int[dicte.gettotalvar()];
		for (int i=0; i<dicte.gettotalvar(); i++)
		{
			tempvname=dicte.getvarname(i);
			if ( (!tempvname.equalsIgnoreCase("_sign_")) && (!tempvname.equalsIgnoreCase("_b_")) )
			{
				tempsarray=tempvname.split("_");
				realvname="";
				for (int j=1; j<tempsarray.length; j++)
				{
					realvname=realvname+tempsarray[j];
					if (j<(tempsarray.length-1))
						realvname=realvname+"_";
				}
				realvname=realvname.trim();
				varref[posv]=realvname.toUpperCase();
				vartotake[posv+2]=tempvname;
				reprule[posv]=1;
				posv++;
			}
		}
		for (int j=0; j<varref.length; j++)
		{
			noexvg=false;
			if (varrefdict.get(varref[j])!=null) noexvg=true;
			if (!noexvg) notvg=notvg+" "+varref[j];
		}
		if (!notvg.equals("")) return new Result("%2858% ("+notvg.trim()+")<br>\n", false, null);
		vartotake[0]="_sign_";
		reprule[0]=1;
		reprule[1]=1;
		vartotake[1]="_b_";
		Vector<double[]> coeffvaleq=new Vector<double[]>();
		Vector<double[]> coeffvaliq=new Vector<double[]>();
		DataReader datae = new DataReader(dicte);
		if (!datae.open(vartotake, reprule, false))
			return new Result(datae.getmessage(), false, null);
		String[] values=null;
		boolean[] testsingle=new boolean[refvar];
		for (int i=0; i<refvar; i++)
		{
			testsingle[i]=true;
		}
		int numdzero=0;
		while (!datae.isLast())
		{
			values = datae.getRecord();
			double[] tempcoeff=new double[refvar+1];
			numdzero=0;
			for (int i=1; i<values.length; i++)
			{
				tempcoeff[i-1]=0.0;
				try
				{
					tempcoeff[i-1]=Double.parseDouble(values[i]);
				}
				catch (Exception cnum) {}
				if (Double.isNaN(tempcoeff[i-1])) tempcoeff[i-1]=0.0;
				if ( (i>1) && (tempcoeff[i-1]!=0.0) ) numdzero++;
			}
			if (numdzero==1)
			{
				for (int i=1; i<tempcoeff.length; i++)
				{
					if ((tempcoeff[i]>0.0) && (tempcoeff[0]<=0.0)) testsingle[i-1]=false;
				}
			}
			if (values[0].equals(">=")) coeffvaliq.add(tempcoeff);
			else coeffvaleq.add(tempcoeff);
		}
		for (int i=0; i<testsingle.length; i++)
		{
			if (testsingle[i])
			{
				double[] tempcoeff=new double[refvar+1];
				for (int j=0; j<refvar+1; j++)
				{
					tempcoeff[j]=0.0;
					if ((j-1)==i) tempcoeff[j]=1.0;
				}
				coeffvaliq.add(tempcoeff);
			}
		}
		datae.close();
		String tempdir=(String)parameters.get(Keywords.WorkDir);
		EditsAnalyzer ea=new EditsAnalyzer(tempdir);
		ea.setEdits(coeffvaliq, coeffvaleq);
		ea.setVarName(varref);
		ea.setTolerance(tolerance);
		String[] varrefr=null;
		DictionaryReader dictr = null;
		EditsAnalyzer ear=new EditsAnalyzer(tempdir);
		if (parameters.get(Keywords.dict+"r")!=null)
		{
			dictr=(DictionaryReader)parameters.get(Keywords.dict+"r");
			int refvarr=dictr.gettotalvar()-2;
			varrefr=new String[refvarr];
			posv=0;
			realvname="";
			tempvname="";
			String[] vartotaker=new String[dictr.gettotalvar()];
			int[] repruler=new int[dictr.gettotalvar()];
			for (int i=0; i<dictr.gettotalvar(); i++)
			{
				tempvname=dictr.getvarname(i);
				if ( (!tempvname.equalsIgnoreCase("_sign_")) && (!tempvname.equalsIgnoreCase("_b_")) )
				{
					tempsarray=tempvname.split("_");
					realvname="";
					for (int j=1; j<tempsarray.length; j++)
					{
						realvname=realvname+tempsarray[j];
						if (j<(tempsarray.length-1))
							realvname=realvname+"_";
					}
					realvname=realvname.trim();
					varrefr[posv]=realvname.toUpperCase();
					vartotaker[posv+2]=tempvname;
					repruler[posv]=1;
					posv++;
				}
			}
			for (int j=0; j<varrefr.length; j++)
			{
				noexvg=false;
				if (varrefdict.get(varrefr[j])!=null) noexvg=true;
				if (!noexvg) notvg=notvg+" "+varref[j];
			}
			if (!notvg.equals("")) return new Result("%2859% ("+notvg.trim()+")<br>\n", false, null);
			vartotaker[0]="_sign_";
			repruler[0]=1;
			repruler[1]=1;
			vartotaker[1]="_b_";
			Vector<double[]> coeffvaleqr=new Vector<double[]>();
			Vector<double[]> coeffvaliqr=new Vector<double[]>();
			DataReader datar = new DataReader(dictr);
			if (!datar.open(vartotaker, repruler, false))
				return new Result(datar.getmessage(), false, null);
			boolean[] testsingler=new boolean[refvarr];
			for (int i=0; i<refvarr; i++)
			{
				testsingler[i]=true;
			}
			while (!datar.isLast())
			{
				values = datar.getRecord();
				double[] tempcoeff=new double[refvarr+1];
				numdzero=0;
				for (int i=1; i<values.length; i++)
				{
					tempcoeff[i-1]=0.0;
					try
					{
						tempcoeff[i-1]=Double.parseDouble(values[i]);
					}
					catch (Exception cnum) {}
					if (Double.isNaN(tempcoeff[i-1])) tempcoeff[i-1]=0.0;
					if ( (i>1) && (tempcoeff[i-1]!=0.0) ) numdzero++;
				}
				if (numdzero==1)
				{
					for (int i=1; i<tempcoeff.length; i++)
					{
						if ((tempcoeff[i]>0.0) && (tempcoeff[0]<=0.0)) testsingle[i-1]=false;
					}
				}
				if (values[0].equals(">=")) coeffvaliqr.add(tempcoeff);
				else coeffvaleqr.add(tempcoeff);
			}
			for (int i=0; i<testsingle.length; i++)
			{
				if (testsingle[i])
				{
					double[] tempcoeff=new double[refvar+1];
					for (int j=0; j<refvar+1; j++)
					{
						tempcoeff[j]=0.0;
						if ((j-1)==i) tempcoeff[j]=1.0;
					}
					coeffvaliqr.add(tempcoeff);
				}
			}
			datar.close();
			for (int i=0; i<varrefr.length; i++)
			{
				noexvg=false;
				for (int j=0; j<varref.length; j++)
				{
					if (varrefr[i].equalsIgnoreCase(varref[j]))
					{
						noexvg=true;
					}
				}
				if (!noexvg) notvg=notvg+" "+varrefr[i];
			}
			if (!notvg.equals("")) return new Result("%2860% ("+notvg.trim()+")<br>\n", false, null);
			ear.setEdits(coeffvaliqr, coeffvaleqr);
			ear.setVarName(varrefr);
			ear.setTolerance(tolerance);
		}
		else
		{
			ear.setEdits(coeffvaliq, coeffvaleq);
			ear.setVarName(varref);
			ear.setTolerance(tolerance);
			varrefr=new String[varref.length];
			for (int i=0; i<varref.length; i++)
			{
				varrefr[i]=varref[i];
			}
		}
		int rifrep=0;
		if (replace==null)
			rifrep=0;
		else if (replace.equalsIgnoreCase(Keywords.replaceall))
		{
			rifrep=1;
		}
		else if (replace.equalsIgnoreCase(Keywords.replaceformat))
		{
			rifrep=2;
		}
		else if (replace.equalsIgnoreCase(Keywords.replacemissing))
		{
			rifrep=3;
		}
		DataReader data = new DataReader(dict);
		if (!data.open(null, rifrep, false))
			return new Result(data.getmessage(), false, null);
		String where=(String)parameters.get(Keywords.where.toLowerCase());
		if (where!=null)
		{
			if (!data.setcondition(where)) return new Result(data.getmessage(), false, null);
		}
		Hashtable<String, Double> min_vars=new Hashtable<String, Double>();
		Hashtable<String, Double> max_vars=new Hashtable<String, Double>();
		for (int i=0; i<varref.length; i++)
		{
			min_vars.put(varref[i], new Double(Double.MAX_VALUE));
			max_vars.put(varref[i], new Double(-1.7976931348623157E308));
		}
		for (int i=0; i<varmatch.length; i++)
		{
			min_vars.put(varmatch[i], new Double(Double.MAX_VALUE));
			max_vars.put(varmatch[i], new Double(-1.7976931348623157E308));
		}
		int toimpute=0;
		int validgroup=0;
		double tempvalue, tempref;
		String[] group=new String[vargroup.length];
		int[] refgroup=new int[vargroup.length];
		if (vargroup.length>0)
		{
			for (int i=0; i<vargroup.length; i++)
			{
				refgroup[i]=(varrefdict.get(vargroup[i])).intValue();
			}
		}
		Hashtable<Vector<String>, Integer> num_elements=new Hashtable<Vector<String>, Integer>();
		String currloc="";
		int actualnumrec=0;
		int finrecords=0;
		while (!data.isLast())
		{
			values = data.getRecord();
			if (values!=null)
			{
				finrecords++;
				currloc=values[posloc].trim();
				if (currloc.equals("1"))
				{
					validgroup++;
					toimpute++;
					for (int i=0; i<varref.length; i++)
					{
						posv=(varrefdict.get(varref[i])).intValue();
						tempvalue=Double.NaN;
						try
						{
							tempvalue=Double.parseDouble(values[posv]);
						}
						catch (Exception e) {}
						if (!Double.isNaN(tempvalue))
						{
							tempref=(min_vars.get(varref[i])).doubleValue();
							if (tempvalue<tempref) min_vars.put(varref[i], new Double(tempvalue));
							tempref=(max_vars.get(varref[i])).doubleValue();
							if (tempvalue>tempref) max_vars.put(varref[i], new Double(tempvalue));
						}
					}
					for (int i=0; i<varmatch.length; i++)
					{
						posv=(varrefdict.get(varmatch[i])).intValue();
						tempvalue=Double.NaN;
						try
						{
							tempvalue=Double.parseDouble(values[posv]);
						}
						catch (Exception e) {}
						if (!Double.isNaN(tempvalue))
						{
							tempref=(min_vars.get(varmatch[i])).doubleValue();
							if (tempvalue<tempref) min_vars.put(varmatch[i], new Double(tempvalue));
							tempref=(max_vars.get(varmatch[i])).doubleValue();
							if (tempvalue>tempref) max_vars.put(varmatch[i], new Double(tempvalue));
						}
					}
					Vector<String> tempgv=new Vector<String>();
					if (vargroup.length>0)
					{
						for (int i=0; i<vargroup.length; i++)
						{
							tempgv.add(values[refgroup[i]].toUpperCase());
						}
					}
					else
						tempgv.add("");
					if (num_elements.get(tempgv)==null)
						num_elements.put(tempgv,new Integer(1));
					else
					{
						actualnumrec=(num_elements.get(tempgv)).intValue();
						num_elements.put(tempgv,new Integer(actualnumrec+1));
					}
				}
			}
		}
		data.close();
		if (validgroup==0)
			return new Result("%3075%<br>\n", false, null);

		Keywords.percentage_total=finrecords*2;
		Keywords.percentage_done=0;

		Hashtable<Vector<String>, Integer> step_num_elements=new Hashtable<Vector<String>, Integer>();
		int tempanr=0;
		Vector<String> tempvgv=new Vector<String>();
		for (Enumeration<Vector<String>> en=num_elements.keys(); en.hasMoreElements();)
		{
			tempvgv=en.nextElement();
			Vector<String> newtempvgv=new Vector<String>();
			for (int i=0; i<tempvgv.size(); i++)
			{
				newtempvgv.add(tempvgv.get(i));
			}
			actualnumrec=(num_elements.get(tempvgv)).intValue();
			if (actualnumrec<mindonors)
			{
				step_num_elements.put(newtempvgv, new Integer(1));
			}
			else
			{
				if (maxdonors==0)
					step_num_elements.put(newtempvgv, new Integer(1));
				else
				{
					tempanr=actualnumrec-mindonors;
					if (tempanr<=0) step_num_elements.put(newtempvgv, new Integer(1));
					else
					{
						int res=tempanr/maxdonors;
						if (res<=0) step_num_elements.put(newtempvgv, new Integer(1));
						else step_num_elements.put(newtempvgv, new Integer(res));
					}
				}
			}
		}
		int general_step=0;
		if (num_elements.size()>0)
		{
			if (maxdonors!=0) maxdonors=maxdonors*num_elements.size();
			if (mindonors!=0) mindonors=mindonors*num_elements.size();
		}
		if (validgroup<mindonors) general_step=1;
		else
		{
			if (maxdonors==0) general_step=1;
			else
			{
				tempanr=validgroup-mindonors;
				if (tempanr<=0) general_step=1;
				else
				{
					int res=tempanr/maxdonors;
					if (res<=0) general_step=1;
					else general_step=res;
				}
			}
		}
		num_elements.clear();
		for (Enumeration<Vector<String>> en=step_num_elements.keys(); en.hasMoreElements();)
		{
			tempvgv=en.nextElement();
			Vector<String> newtempvgv=new Vector<String>();
			for (int i=0; i<tempvgv.size(); i++)
			{
				newtempvgv.add(tempvgv.get(i));
			}
			num_elements.put(newtempvgv, step_num_elements.get(tempvgv));
		}
		int actual_general_step=general_step;

		data = new DataReader(dict);
		if (!data.open(null, rifrep, false))
			return new Result(data.getmessage(), false, null);
		where=(String)parameters.get(Keywords.where.toLowerCase());
		if (where!=null)
		{
			if (!data.setcondition(where)) return new Result(data.getmessage(), false, null);
		}
		FastTempDataSet ftds=new FastTempDataSet(tempdir);
		GroupedFastTempDataSet gftd=null;
		if (ftds.geterror())
			return new Result(ftds.getmessage(), false, null);
		if (vargroup.length>0)
		{
			gftd=new GroupedFastTempDataSet(tempdir, vargroup.length);
			if (gftd.geterror())
				return new Result(gftd.getmessage(), false, null);
		}

		int refa=0;
		int refb=0;
		validgroup=0;
		while (!data.isLast())
		{
			values = data.getRecord();
			if (values!=null)
			{
				Keywords.percentage_done++;
				validgroup++;
				currloc=values[posloc].trim();
				if (currloc.equals("1"))
				{
					if (actual_general_step==0) actual_general_step=general_step;
					if (actual_general_step==general_step)
					{
						if (!ftds.write(values))
						{
							data.close();
							ftds.deletefile();
							if (vargroup.length>0)
							{
								gftd.endwrite();
								gftd.deletefile();
							}
							return new Result(ftds.getmessage(), false, null);
						}
					}
					actual_general_step--;
					if (vargroup.length>0)
					{
						Vector<String> tempgv=new Vector<String>();
						for (int i=0; i<vargroup.length; i++)
						{
							group[i]=values[refgroup[i]].toUpperCase();
							tempgv.add(group[i]);
						}
						refa=(num_elements.get(tempgv)).intValue();
						refb=(step_num_elements.get(tempgv)).intValue();
						if (refa==0) refa=refb;
						if (refa==refb)
						{
							if (!gftd.write(group, values))
							{
								ftds.endwrite();
								ftds.deletefile();
								data.close();
								gftd.endwrite();
								gftd.deletefile();
								return new Result(gftd.getmessage(), false, null);
							}
						}
						num_elements.put(tempgv,new Integer(refa-1));
					}
				}
			}
		}
		data.close();
		if (!ftds.endwrite())
		{
			if (vargroup.length>0)
			{
				gftd.endwrite();
				gftd.deletefile();
			}
			return new Result(ftds.getmessage(), false, null);
		}
		if (vargroup.length>0)
		{
			if (!gftd.endwrite())
			{
				String msgnotg=gftd.getmessage();
				gftd.deletefile();
				ftds.deletefile();
				return new Result(msgnotg, false, null);
			}
		}
		if ((validgroup==0) && (where!=null))
		{
			ftds.deletefile();
			if (vargroup.length>0) gftd.deletefile();
			return new Result("%2845%<br>\n", false, null);
		}
		if ((validgroup==0) && (where==null))
		{
			ftds.deletefile();
			if (vargroup.length>0) gftd.deletefile();
			return new Result("%2846%<br>\n", false, null);
		}
		if (toimpute==validgroup)
		{
			ftds.deletefile();
			if (vargroup.length>0) gftd.deletefile();
			return new Result("%2850%<br>\n", true, null);
		}
		DataSetUtilities dsu=new DataSetUtilities();
		dsu.setreplace(replace);
		dsu.defineolddict(dict);
		DataWriter dw=new DataWriter(parameters, Keywords.OUT.toLowerCase());
		if (!dw.getmessage().equals(""))
		{
			ftds.deletefile();
			if (vargroup.length>0) gftd.deletefile();
			return new Result(dw.getmessage(), false, null);
		}
		if (!dw.opendatatable(dsu.getfinalvarinfo()))
		{
			ftds.deletefile();
			if (vargroup.length>0) gftd.deletefile();
			return new Result(dw.getmessage(), false, null);
		}
		if (!data.open(null, 0, false))
		{
			dw.deletetmp();
			ftds.deletefile();
			if (vargroup.length>0) gftd.deletefile();
			return new Result(data.getmessage(), false, null);
		}
		Vector<StepResult> results = new Vector<StepResult>();
		if (type)
			results.add(new LocalMessageGetter("%2560%<br>\n"));
		if (where!=null)
		{
			if (!data.setcondition(where))
			{
				dw.deletetmp();
				ftds.deletefile();
				if (vargroup.length>0) gftd.deletefile();
				return new Result(data.getmessage(), false, null);
			}
		}

		int recordds=ftds.getrecords();
		double actualdistance=0;
		double maxdistance=Double.MAX_VALUE;

		double maxdistancetotal=Double.MAX_VALUE;

		boolean dovargroup, wascorrected, dorecord;
		boolean nottreatable=false;
		validgroup=0;
		Vector<String> tempvect=new Vector<String>();
		Vector<String> tempvectsol=new Vector<String>();
		double[] currtempdonors=new double[0];
		double[] currtempdonorstotal=new double[0];
		String[] locs;
		String[] sols;
		String[] clocs;
		String[] csols;
		String[] clocss;
		String[] csolss;
		String currsol="";
		int insertat, notvalidall, recordtoconsider, numnotnull, recordgnott, solvedgroups, recordnott, solved, numnotnulltotal;
		Vector<String> useddistances;
		int[] refforeval;
		int[] refforposi;
		int[] reffordist;

		int[] refforevaltotal;
		int[] refforpositotal;

		notvalidall=0;
		int numsolequals=0;
		double[] curmax;
		double[] curmin;
		double[] currvaltoe;
		double[] currvaltot;
		double tempct, tempcr;
		String[] actualrecordtds;
		Hashtable<String, Double> correctedvars=new Hashtable<String, Double>();
		Hashtable<String, Double> correctedvarstotal=new Hashtable<String, Double>();
		Vector<Double> valuestocorrect=new Vector<Double>();
		Vector<Double> valuestocorrecttotal=new Vector<Double>();
		recordgnott=0;
		solvedgroups=0;
		recordnott=0;
		solved=0;
		int solvedgfin=0;
		boolean groupnotsolved;
		double tval=0;
		boolean corrected, correctedtotal;
		int errated=0;
		String cvn="";
		String tempvrt;
		int treated=0;
		int tempint;
		wascorrected=false;
		boolean totreatlater=false;
		int nottreatedbnd=0;
		int solvedgroupsnott=0;
		int recordgnottnott=0;
		int solvednot=0;
		int solvedgfinnot=0;
		int recordnottnot=0;

		int correctedingws=0;
		int correctedingwds=0;
		int correctedingor=0;

		int scorrectedingws=0;
		int scorrectedingwds=0;
		int scorrectedingor=0;

		int correctedingwsnot=0;
		int correctedingwdsnot=0;
		int correctedingornot=0;

		int scorrectedingwsnot=0;
		int scorrectedingwdsnot=0;
		int scorrectedingornot=0;

		boolean exitcycle=false;

		DataSetUtilities dsud=new DataSetUtilities();
		String[] outford=new String[0];
		if (isoutd)
		{
			int numvoutd=2;
			Hashtable<String, String> tempmdd=new Hashtable<String, String>();
			for (int i=0; i<varsid.length; i++)
			{
				numvoutd++;
				dsud.addnewvar("id_recip_"+varsid[i], "%3216%: "+dict.getvarlabelfromname(varsid[i]), Keywords.TEXTSuffix, tempmdd, tempmdd);
			}
			for (int i=0; i<varsid.length; i++)
			{
				numvoutd++;
				dsud.addnewvar("id_donor_"+varsid[i], "%3217%: "+dict.getvarlabelfromname(varsid[i]), Keywords.TEXTSuffix, tempmdd, tempmdd);
			}
			if (vargroup.length>0)
			{
				for (int i=0; i<vargroup.length; i++)
				{
					numvoutd++;
					dsud.addnewvar("vargroup_recip_"+vargroup[i], "%3219%: "+dict.getvarlabelfromname(vargroup[i]), Keywords.TEXTSuffix, tempmdd, tempmdd);
				}
				for (int i=0; i<vargroup.length; i++)
				{
					numvoutd++;
					dsud.addnewvar("vargroup_donor_"+vargroup[i], "%3220%: "+dict.getvarlabelfromname(vargroup[i]), Keywords.TEXTSuffix, tempmdd, tempmdd);
				}
			}
			Hashtable<String, String> fmtfordon=new Hashtable<String, String>();

			dsud.addnewvar("donor_type", "%3221%", Keywords.TEXTSuffix, fmtfordon, tempmdd);
			fmtfordon.put("1","%3226%");
			fmtfordon.put("2","%3227%");
			fmtfordon.put("3","%3228%");
			fmtfordon.put("4","%3229%");
			fmtfordon.put("5","%3230%");
			fmtfordon.put("6","%3231%");
			fmtfordon.put("7","%3232%");
			fmtfordon.put("8","%3233%");
			fmtfordon.put("9","%3234%");
			fmtfordon.put("10","%3235%");
			if (reducedforsecondary)
			{
				numvoutd++;
				dsud.addnewvar("distance_reduced", "%3222%", Keywords.NUMSuffix, tempmdd, tempmdd);
				dsud.addnewvar("distance_primary", "%3223%", Keywords.NUMSuffix, tempmdd, tempmdd);
			}
			else
				dsud.addnewvar("distance", "%3218%", Keywords.NUMSuffix, tempmdd, tempmdd);
			if (!dwd.opendatatable(dsud.getfinalvarinfo()))
			{
				dw.deletetmp();
				ftds.deletefile();
				if (vargroup.length>0) gftd.deletefile();
				return new Result(dwd.getmessage(), false, null);
			}
			outford=new String[numvoutd];
		}

		int writtenind=0;

		validgroup=0;
		Hashtable<String, double[]> donoreffects=new Hashtable<String, double[]>();
		while (!data.isLast())
		{
			values = data.getRecord();
			nottreatable=false;
			if (values!=null)
			{
				Keywords.percentage_done++;
				validgroup++;
				currloc=values[posloc].trim();
				currsol=values[possol].trim();
				if ((!currloc.equals("1")) && (!currloc.equals("9")) && (!currloc.equals("99")) && (!currloc.equals("999")) && (!currloc.equals("9999")) && (!currloc.equals("99999")))
				{
					if (isoutd)
					{
						for (int i=0; i<outford.length; i++)
						{
							outford[i]="";
						}
					}
					tempvect.clear();
					tempvectsol.clear();
					if (currloc.indexOf(";")>=0)
					{
						locs=currloc.split(";");
						sols=currsol.split(";");
						for (int i=0; i<locs.length; i++)
						{
							if (locs[i].trim().indexOf(",")>=0)
							{
								clocs=locs[i].trim().split(",");
								csols=sols[i].trim().split(",");
								clocss=clocs[0].trim().split(" ");
								csolss=csols[0].trim().split(" ");
								for (int j=0; j<clocss.length; j++)
								{
									if (!tempvect.contains(clocss[j].trim().toUpperCase()))
									{
										tempvect.add(clocss[j].trim().toUpperCase());
										tempvectsol.add(csolss[j].trim());
									}
								}
							}
							else
							{
								clocs=locs[i].trim().split(" ");
								csols=sols[i].trim().split(" ");
								for (int j=0; j<clocs.length; j++)
								{
									if (!tempvect.contains(clocs[j].trim().toUpperCase()))
									{
										tempvect.add(clocs[j].trim().toUpperCase());
										tempvectsol.add(csols[j].trim());
									}
								}
							}
						}
					}
					else
					{
						if (currloc.trim().indexOf(",")>=0)
						{
							clocs=currloc.trim().split(",");
							csols=currsol.trim().split(",");
							clocss=clocs[0].trim().split(" ");
							csolss=csols[0].trim().split(" ");
							for (int j=0; j<clocss.length; j++)
							{
								if (!tempvect.contains(clocss[j].trim().toUpperCase()))
								{
									tempvect.add(clocss[j].trim().toUpperCase());
									tempvectsol.add(csolss[j].trim());
								}
							}
						}
						else
						{
							clocss=currloc.trim().split(" ");
							csolss=currsol.trim().split(" ");
							for (int j=0; j<clocss.length; j++)
							{
								if (!tempvect.contains(clocss[j].trim().toUpperCase()))
								{
									tempvect.add(clocss[j].trim().toUpperCase());
									tempvectsol.add(csolss[j].trim());
								}
							}
						}
					}
					if (tempvect.size()>0)
					{
						correctedvars.clear();
						correctedvarstotal.clear();
						valuestocorrect.clear();
						useddistances=ea.getdepvars(tempvect, varmatch);
						currtempdonors=new double[varrefr.length];
						for (int i=0; i<varrefr.length; i++)
						{
							insertat=(varrefdict.get(varrefr[i])).intValue();
							currtempdonors[i]=Double.NaN;
							try
							{
								currtempdonors[i]=Double.parseDouble(values[insertat]);
							}
							catch (Exception e) {}
						}
						if (reducedforsecondary)
						{
							currtempdonorstotal=new double[varref.length];
							for (int i=0; i<varref.length; i++)
							{
								insertat=(varrefdict.get(varref[i])).intValue();
								currtempdonorstotal[i]=Double.NaN;
								try
								{
									currtempdonorstotal[i]=Double.parseDouble(values[insertat]);
								}
								catch (Exception e) {}
							}
						}
						refforevaltotal=new int[tempvect.size()];
						refforpositotal=new int[tempvect.size()];
						refforeval=new int[tempvect.size()];
						refforposi=new int[tempvect.size()];
						for (int i=0; i<tempvect.size(); i++)
						{
							refforeval[i]=(varrefdict.get(tempvect.get(i))).intValue();
							refforevaltotal[i]=(varrefdict.get(tempvect.get(i))).intValue();
							for (int j=0; j<varrefr.length; j++)
							{
								if (varrefr[j].equalsIgnoreCase(tempvect.get(i)))
								{
									refforposi[i]=j;
									break;
								}
							}
							if (reducedforsecondary)
							{
								for (int j=0; j<varref.length; j++)
								{
									if (varref[j].equalsIgnoreCase(tempvect.get(i)))
									{
										refforpositotal[i]=j;
										break;
									}
								}
							}
						}
						totreatlater=false;
						if (useddistances.size()==0 && norandomimpute)
						{
							nottreatable=true;
							notvalidall++;
						}
						else if (useddistances.size()==0 && !norandomimpute)
						{
							totreatlater=true;
						}
						else
						{
							currvaltoe=new double[useddistances.size()];
							reffordist=new int[useddistances.size()];
							curmax=new double[useddistances.size()];
							curmin=new double[useddistances.size()];
							numnotnull=0;
							for (int j=0; j<useddistances.size(); j++)
							{
								currvaltoe[j]=Double.NaN;
								reffordist[j]=(varrefdict.get(useddistances.get(j))).intValue();
								if (!values[reffordist[j]].equals(""))
								{
									try
									{
										currvaltoe[j]=Double.parseDouble(values[reffordist[j]]);
									}
									catch (Exception e) {}
								}
								if (!Double.isNaN(currvaltoe[j])) numnotnull++;
								curmin[j]=(min_vars.get(useddistances.get(j).toUpperCase())).doubleValue();
								curmax[j]=(max_vars.get(useddistances.get(j).toUpperCase())).doubleValue();
							}
							if (numnotnull==0) totreatlater=true;
							else
							{
								dovargroup=false;
								recordtoconsider=0;
								if (vargroup.length>0)
								{
									dovargroup=true;
									recordtoconsider=-1;
									for (int j=0; j<vargroup.length; j++)
									{
										group[j]=values[refgroup[j]].toUpperCase();
									}
									try
									{
										gftd.opentoread(group);
										recordtoconsider=gftd.getrecords();
									}
									catch (Exception e)
									{
										if (consideralsoall) dovargroup=false;
									}
									if (recordtoconsider<1 && consideralsoall) dovargroup=false;
								}
								groupnotsolved=false;
								if (dovargroup)
								{
									maxdistance=Double.MAX_VALUE;
									maxdistancetotal=Double.MAX_VALUE;
									wascorrected=false;
									exitcycle=false;
									for (int k=0; k<recordtoconsider; k++)
									{
										actualrecordtds=gftd.read();
										currvaltot=new double[reffordist.length];
										dorecord=false;
										actualdistance=0;
										for (int j=0; j<reffordist.length; j++)
										{
											currvaltot[j]=Double.NaN;
											try
											{
												currvaltot[j]=Double.parseDouble(actualrecordtds[reffordist[j]]);
											}
											catch (Exception e) {}
											if ((!Double.isNaN(currvaltot[j])) || (!Double.isNaN(currvaltoe[j])))
											{
												dorecord=true;
												if (!useeuclidean)
												{
													if (curmax[j]!=curmin[j])
														tval=((currvaltot[j]-currvaltoe[j])-curmin[j])/(curmax[j]-curmin[j]);
													else
														tval=0;
													if (Math.abs(tval)>actualdistance) actualdistance=Math.abs(tval);
												}
												else
												{
													if (curmax[j]!=curmin[j])
														tval=((currvaltot[j]-currvaltoe[j])-curmin[j])/(curmax[j]-curmin[j]);
													else
														tval=0;
													actualdistance=actualdistance+Math.pow(tval,2);
												}
											}
										}
										if (dorecord && actualdistance<maxdistance)
										{
											numnotnull=0;
											valuestocorrect.clear();
											for (int j=0; j<refforposi.length; j++)
											{
												currtempdonors[refforposi[j]]=Double.NaN;
												try
												{
													currtempdonors[refforposi[j]]=Double.parseDouble(actualrecordtds[refforeval[j]]);
													numnotnull++;
												}
												catch (Exception e) {}
												valuestocorrect.add(new Double(currtempdonors[refforposi[j]]));
											}
											if (numnotnull==refforposi.length)
											{
												corrected=ear.recordVerifierNoG(currtempdonors);
												if (corrected)
												{
													if (isoutd)
													{
														for (int h=0; h<vargroup.length; h++)
														{
															outford[h+posvarid.length]=actualrecordtds[posvarid[h]];
														}
														if (vargroup.length>0)
														{
															for (int h=0; h<vargroup.length; h++)
															{
																outford[h+posvarid.length+posvarid.length+vargroup.length]=actualrecordtds[refgroup[h]];
															}
														}
														outford[posvarid.length+posvarid.length+vargroup.length+vargroup.length]="1";
														outford[posvarid.length+posvarid.length+vargroup.length+vargroup.length+1]=String.valueOf(actualdistance);
													}
													correctedvars.clear();
													for (int i=0; i<tempvect.size(); i++)
													{
														correctedvars.put(tempvect.get(i), new Double(valuestocorrect.get(i)));
													}
													maxdistance=actualdistance;
													wascorrected=true;
													if (actualdistance==0) exitcycle=true;
												}
											}
										}
										if (dorecord && actualdistance<maxdistancetotal && reducedforsecondary)
										{
											exitcycle=false;
											numnotnulltotal=0;
											valuestocorrecttotal.clear();
											for (int j=0; j<refforpositotal.length; j++)
											{
												currtempdonorstotal[refforpositotal[j]]=Double.NaN;
												try
												{
													currtempdonorstotal[refforpositotal[j]]=Double.parseDouble(actualrecordtds[refforevaltotal[j]]);
													numnotnulltotal++;
												}
												catch (Exception e) {}
												valuestocorrecttotal.add(new Double(currtempdonorstotal[refforpositotal[j]]));
											}
											if (numnotnulltotal==refforpositotal.length)
											{
												correctedtotal=ea.recordVerifierNoG(currtempdonorstotal);
												if (correctedtotal)
												{
													correctedvarstotal.clear();
													for (int i=0; i<tempvect.size(); i++)
													{
														correctedvarstotal.put(tempvect.get(i), new Double(valuestocorrecttotal.get(i)));
													}
													maxdistancetotal=actualdistance;
													if (isoutd)
													{
														for (int h=0; h<vargroup.length; h++)
														{
															outford[h+posvarid.length]=actualrecordtds[posvarid[h]];
														}
														if (vargroup.length>0)
														{
															for (int h=0; h<vargroup.length; h++)
															{
																outford[h+posvarid.length+posvarid.length+vargroup.length]=actualrecordtds[refgroup[h]];
															}
														}
														outford[posvarid.length+posvarid.length+vargroup.length+vargroup.length]="2";
														outford[posvarid.length+posvarid.length+vargroup.length+vargroup.length+2]=String.valueOf(actualdistance);
													}
													if (actualdistance==0) exitcycle=true;
												}
											}
										}
										if (exitcycle) break;
									}
									if (recordtoconsider>=0) gftd.endread();
									if (wascorrected)
									{
										if (correctedvarstotal.size()>0)
										{
											numsolequals=0;
											for (Enumeration<String> en=correctedvarstotal.keys(); en.hasMoreElements();)
											{
												tempvrt=en.nextElement();
												tempct=(correctedvarstotal.get(tempvrt)).doubleValue();
												if (correctedvars.get(tempvrt)!=null)
												{
													tempcr=(correctedvars.get(tempvrt)).doubleValue();
													if (tempcr==tempct) numsolequals++;
												}
											}
											if (numsolequals==correctedvarstotal.size()) correctedingws++;
											else correctedingwds++;
										}
										else correctedingor++;
										solvedgroups++;
										groupnotsolved=true;
									}
									else
									{
										recordgnott++;
										if (consideralsoall) dovargroup=false;
										groupnotsolved=false;
										if (isoutd)
										{
											for (int h=0; h<vargroup.length; h++)
											{
												outford[h+posvarid.length]="";
											}
											if (vargroup.length>0)
											{
												for (int h=0; h<vargroup.length; h++)
												{
													outford[h+posvarid.length+posvarid.length+vargroup.length]="";
												}
											}
											outford[posvarid.length+posvarid.length+vargroup.length+vargroup.length]="3";
											if (reducedforsecondary) outford[posvarid.length+posvarid.length+vargroup.length+vargroup.length+2]="";
										}
									}
								}
								if (!dovargroup)
								{
									maxdistance=Double.MAX_VALUE;
									maxdistancetotal=Double.MAX_VALUE;
									wascorrected=false;
									ftds.opentoread();
									exitcycle=false;
									for (int k=0; k<recordds; k++)
									{
										actualrecordtds=ftds.read();
										currvaltot=new double[reffordist.length];
										dorecord=false;
										actualdistance=0;
										for (int j=0; j<reffordist.length; j++)
										{
											currvaltot[j]=Double.NaN;
											try
											{
												currvaltot[j]=Double.parseDouble(actualrecordtds[reffordist[j]]);
											}
											catch (Exception e) {}
											if ((!Double.isNaN(currvaltot[j])) || (!Double.isNaN(currvaltoe[j])))
											{
												dorecord=true;
												if (!useeuclidean)
												{
													if (curmax[j]!=curmin[j])
														tval=((currvaltot[j]-currvaltoe[j])-curmin[j])/(curmax[j]-curmin[j]);
													else
														tval=0;
													if (Math.abs(tval)>actualdistance) actualdistance=Math.abs(tval);
												}
												else
												{
													if (curmax[j]!=curmin[j])
														tval=((currvaltot[j]-currvaltoe[j])-curmin[j])/(curmax[j]-curmin[j]);
													else
														tval=0;
													actualdistance=actualdistance+Math.pow(tval,2);
												}
											}
										}
										if (dorecord && actualdistance<maxdistance)
										{
											numnotnull=0;
											valuestocorrect.clear();
											for (int j=0; j<refforposi.length; j++)
											{
												currtempdonors[refforposi[j]]=Double.NaN;
												try
												{
													currtempdonors[refforposi[j]]=Double.parseDouble(actualrecordtds[refforeval[j]]);
													numnotnull++;
												}
												catch (Exception e) {}
												valuestocorrect.add(new Double(currtempdonors[refforposi[j]]));
											}
											if (numnotnull==refforposi.length)
											{
												corrected=ear.recordVerifierNoG(currtempdonors);
												if (corrected)
												{
													if (isoutd)
													{
														for (int h=0; h<vargroup.length; h++)
														{
															outford[h+posvarid.length]=actualrecordtds[posvarid[h]];
														}
														if (vargroup.length>0)
														{
															for (int h=0; h<vargroup.length; h++)
															{
																outford[h+posvarid.length+posvarid.length+vargroup.length]=actualrecordtds[refgroup[h]];
															}
														}
														outford[posvarid.length+posvarid.length+vargroup.length+vargroup.length]="4";
														outford[posvarid.length+posvarid.length+vargroup.length+vargroup.length+1]=String.valueOf(actualdistance);
													}
													correctedvars.clear();
													for (int i=0; i<tempvect.size(); i++)
													{
														correctedvars.put(tempvect.get(i), new Double(valuestocorrect.get(i)));
													}
													maxdistance=actualdistance;
													wascorrected=true;
													if (actualdistance==0) exitcycle=true;
												}
											}
										}
										if (dorecord && actualdistance<maxdistancetotal && reducedforsecondary)
										{
											exitcycle=false;
											numnotnulltotal=0;
											valuestocorrecttotal.clear();
											for (int j=0; j<refforpositotal.length; j++)
											{
												currtempdonorstotal[refforpositotal[j]]=Double.NaN;
												try
												{
													currtempdonorstotal[refforpositotal[j]]=Double.parseDouble(actualrecordtds[refforevaltotal[j]]);
													numnotnulltotal++;
												}
												catch (Exception e) {}
												valuestocorrecttotal.add(new Double(currtempdonorstotal[refforpositotal[j]]));
											}
											if (numnotnulltotal==refforpositotal.length)
											{
												correctedtotal=ea.recordVerifierNoG(currtempdonorstotal);
												if (correctedtotal)
												{
													if (isoutd)
													{
														for (int h=0; h<vargroup.length; h++)
														{
															outford[h+posvarid.length]=actualrecordtds[posvarid[h]];
														}
														if (vargroup.length>0)
														{
															for (int h=0; h<vargroup.length; h++)
															{
																outford[h+posvarid.length+posvarid.length+vargroup.length]=actualrecordtds[refgroup[h]];
															}
														}
														outford[posvarid.length+posvarid.length+vargroup.length+vargroup.length]="5";
														outford[posvarid.length+posvarid.length+vargroup.length+vargroup.length+2]=String.valueOf(actualdistance);
													}
													correctedvarstotal.clear();
													for (int i=0; i<tempvect.size(); i++)
													{
														correctedvarstotal.put(tempvect.get(i), new Double(valuestocorrecttotal.get(i)));
													}
													maxdistancetotal=actualdistance;
													if (actualdistance==0) exitcycle=true;
												}
											}
										}
										if (exitcycle) break;
									}
									ftds.endread();
									if (wascorrected)
									{
										if (correctedvarstotal.size()>0)
										{
											numsolequals=0;
											for (Enumeration<String> en=correctedvarstotal.keys(); en.hasMoreElements();)
											{
												tempvrt=en.nextElement();
												tempct=(correctedvarstotal.get(tempvrt)).doubleValue();
												if (correctedvars.get(tempvrt)!=null)
												{
													tempcr=(correctedvars.get(tempvrt)).doubleValue();
													if (tempcr==tempct) numsolequals++;
												}
											}
											if (numsolequals==correctedvarstotal.size()) scorrectedingws++;
											else scorrectedingwds++;
										}
										else scorrectedingor++;
										solved++;
										if (vargroup.length>0 && !groupnotsolved) solvedgfin++;
									}
									else
									{
										recordnott++;
										if (isoutd)
										{
											for (int h=0; h<vargroup.length; h++)
											{
												outford[h+posvarid.length]="";
											}
											if (vargroup.length>0)
											{
												for (int h=0; h<vargroup.length; h++)
												{
													outford[h+posvarid.length+posvarid.length+vargroup.length]="";
												}
											}
											outford[posvarid.length+posvarid.length+vargroup.length+vargroup.length]="6";
											if (reducedforsecondary) outford[posvarid.length+posvarid.length+vargroup.length+vargroup.length+2]="";
										}
									}
								}
							}
						}
						if (totreatlater && norandomimpute)
						{
							nottreatable=true;
							notvalidall++;
						}
						if (totreatlater && !norandomimpute)
						{
							nottreatedbnd++;
							dovargroup=false;
							recordtoconsider=0;
							if (vargroup.length>0)
							{
								dovargroup=true;
								recordtoconsider=-1;
								for (int j=0; j<vargroup.length; j++)
								{
									group[j]=values[refgroup[j]].toUpperCase();
								}
								try
								{
									dovargroup=gftd.opentoread(group);
									recordtoconsider=gftd.getrecords();
								}
								catch (Exception e)
								{
									dovargroup=false;
								}
								if (recordtoconsider<1) dovargroup=false;
							}
							groupnotsolved=false;
							if (dovargroup)
							{
								wascorrected=false;
								for (int k=0; k<recordtoconsider; k++)
								{
									actualrecordtds=gftd.read();
									numnotnull=0;
									numnotnulltotal=0;
									valuestocorrect.clear();
									valuestocorrecttotal.clear();
									for (int j=0; j<refforposi.length; j++)
									{
										currtempdonors[refforposi[j]]=Double.NaN;
										try
										{
											currtempdonors[refforposi[j]]=Double.parseDouble(actualrecordtds[refforeval[j]]);
											numnotnull++;
										}
										catch (Exception e) {}
										valuestocorrect.add(new Double(currtempdonors[refforposi[j]]));
									}
									if (numnotnull==refforposi.length)
									{
										corrected=ear.recordVerifierNoG(currtempdonors);
										if (corrected)
										{
											correctedvars.clear();
											for (int i=0; i<tempvect.size(); i++)
											{
												correctedvars.put(tempvect.get(i), new Double(valuestocorrect.get(i)));
											}
											if (isoutd)
											{
												for (int h=0; h<vargroup.length; h++)
												{
													outford[h+posvarid.length]=actualrecordtds[posvarid[h]];
												}
												if (vargroup.length>0)
												{
													for (int h=0; h<vargroup.length; h++)
													{
														outford[h+posvarid.length+posvarid.length+vargroup.length]=actualrecordtds[refgroup[h]];
													}
												}
												outford[posvarid.length+posvarid.length+vargroup.length+vargroup.length]="7";
												outford[posvarid.length+posvarid.length+vargroup.length+vargroup.length+1]="";
											}
											wascorrected=true;
											if (!reducedforsecondary) break;
										}
									}
									if (reducedforsecondary)
									{
										for (int j=0; j<refforpositotal.length; j++)
										{
											currtempdonorstotal[refforpositotal[j]]=Double.NaN;
											try
											{
												currtempdonorstotal[refforpositotal[j]]=Double.parseDouble(actualrecordtds[refforevaltotal[j]]);
												numnotnulltotal++;
											}
											catch (Exception e) {}
											valuestocorrecttotal.add(new Double(currtempdonorstotal[refforpositotal[j]]));
										}
										if (numnotnulltotal==refforpositotal.length)
										{
											correctedtotal=ea.recordVerifierNoG(currtempdonorstotal);
											if (isoutd)
											{
												for (int h=0; h<vargroup.length; h++)
												{
													outford[h+posvarid.length]=actualrecordtds[posvarid[h]];
												}
												if (vargroup.length>0)
												{
													for (int h=0; h<vargroup.length; h++)
													{
														outford[h+posvarid.length+posvarid.length+vargroup.length]=actualrecordtds[refgroup[h]];
													}
												}
												outford[posvarid.length+posvarid.length+vargroup.length+vargroup.length]="8";
												outford[posvarid.length+posvarid.length+vargroup.length+vargroup.length+2]="";
											}
											if (correctedtotal)
											{
												correctedvarstotal.clear();
												for (int i=0; i<tempvect.size(); i++)
												{
													correctedvarstotal.put(tempvect.get(i), new Double(valuestocorrecttotal.get(i)));
												}

												break;
											}
										}
									}
								}
								gftd.endread();
								if (wascorrected)
								{
									if (correctedvarstotal.size()>0)
									{
										numsolequals=0;
										for (Enumeration<String> en=correctedvarstotal.keys(); en.hasMoreElements();)
										{
											tempvrt=en.nextElement();
											tempct=(correctedvarstotal.get(tempvrt)).doubleValue();
											if (correctedvars.get(tempvrt)!=null)
											{
												tempcr=(correctedvars.get(tempvrt)).doubleValue();
												if (tempcr==tempct) numsolequals++;
											}
										}
										if (numsolequals==correctedvarstotal.size()) scorrectedingwsnot++;
										else scorrectedingwdsnot++;
									}
									else scorrectedingornot++;
									solvedgroupsnott++;
									groupnotsolved=true;
								}
								else
								{
									recordgnottnott++;
									if (consideralsoall) dovargroup=false;
									groupnotsolved=false;
								}
							}
							if (!dovargroup)
							{
								wascorrected=false;
								ftds.opentoread();
								for (int k=0; k<recordds; k++)
								{
									actualrecordtds=ftds.read();
									numnotnull=0;
									numnotnulltotal=0;
									valuestocorrect.clear();
									for (int j=0; j<refforposi.length; j++)
									{
										currtempdonors[refforposi[j]]=Double.NaN;
										try
										{
											currtempdonors[refforposi[j]]=Double.parseDouble(actualrecordtds[refforeval[j]]);
											numnotnull++;
										}
										catch (Exception e) {}
										valuestocorrect.add(new Double(currtempdonors[refforposi[j]]));
									}
									if (numnotnull==refforposi.length)
									{
										corrected=ear.recordVerifierNoG(currtempdonors);
										if (corrected)
										{
											if (isoutd)
											{
												for (int h=0; h<vargroup.length; h++)
												{
													outford[h+posvarid.length]=actualrecordtds[posvarid[h]];
												}
												if (vargroup.length>0)
												{
													for (int h=0; h<vargroup.length; h++)
													{
														outford[h+posvarid.length+posvarid.length+vargroup.length]=actualrecordtds[refgroup[h]];
													}
												}
												outford[posvarid.length+posvarid.length+vargroup.length+vargroup.length]="9";
												outford[posvarid.length+posvarid.length+vargroup.length+vargroup.length+1]="";
											}
											correctedvars.clear();
											for (int i=0; i<tempvect.size(); i++)
											{
												correctedvars.put(tempvect.get(i), new Double(valuestocorrect.get(i)));
											}
											wascorrected=true;
											if (!reducedforsecondary) break;
										}
									}
									if (reducedforsecondary)
									{
										for (int j=0; j<refforpositotal.length; j++)
										{
											currtempdonorstotal[refforpositotal[j]]=Double.NaN;
											try
											{
												currtempdonorstotal[refforpositotal[j]]=Double.parseDouble(actualrecordtds[refforevaltotal[j]]);
												numnotnulltotal++;
											}
											catch (Exception e) {}
											valuestocorrecttotal.add(new Double(currtempdonorstotal[refforpositotal[j]]));
										}
										if (numnotnulltotal==refforpositotal.length)
										{
											correctedtotal=ea.recordVerifierNoG(currtempdonorstotal);
											if (correctedtotal)
											{
												if (isoutd)
												{
													for (int h=0; h<vargroup.length; h++)
													{
														outford[h+posvarid.length]=actualrecordtds[posvarid[h]];
													}
													if (vargroup.length>0)
													{
														for (int h=0; h<vargroup.length; h++)
														{
															outford[h+posvarid.length+posvarid.length+vargroup.length]=actualrecordtds[refgroup[h]];
														}
													}
													outford[posvarid.length+posvarid.length+vargroup.length+vargroup.length]="10";
													outford[posvarid.length+posvarid.length+vargroup.length+vargroup.length+1]="";
												}
												correctedvarstotal.clear();
												for (int i=0; i<tempvect.size(); i++)
												{
													correctedvarstotal.put(tempvect.get(i), new Double(valuestocorrecttotal.get(i)));
												}
												break;
											}
										}
									}
								}
								ftds.endread();
								if (wascorrected)
								{
									if (correctedvarstotal.size()>0)
									{
										numsolequals=0;
										for (Enumeration<String> en=correctedvarstotal.keys(); en.hasMoreElements();)
										{
											tempvrt=en.nextElement();
											tempct=(correctedvarstotal.get(tempvrt)).doubleValue();
											if (correctedvars.get(tempvrt)!=null)
											{
												tempcr=(correctedvars.get(tempvrt)).doubleValue();
												if (tempcr==tempct) numsolequals++;
											}
										}
										if (numsolequals==correctedvarstotal.size()) scorrectedingwsnot++;
										else scorrectedingwdsnot++;
									}
									else scorrectedingornot++;
									solvednot++;
									if (vargroup.length>0 && !groupnotsolved) solvedgfinnot++;
								}
								else recordnottnot++;
							}
						}
						errated++;
						if (!nottreatable && wascorrected)
						{
							if (correctedvarstotal.size()==0)
							{
								for (Enumeration<String> en=correctedvars.keys(); en.hasMoreElements();)
								{
									double[] tempeffects=new double[8];
									cvn=en.nextElement();
									tval=(correctedvars.get(cvn)).doubleValue();
									tempint=(varrefdict.get(cvn)).intValue();
									if (!values[tempint].equals(""))
									{
										tempeffects[0]=1;
										tempeffects[1]=1;
										tempeffects[2]=Double.parseDouble(values[tempint]);
										tempeffects[3]=tval;
										tempeffects[4]=tval-Double.parseDouble(values[tempint]);
										tempeffects[5]=Math.abs(tval-Double.parseDouble(values[tempint]));
										tempeffects[6]=0;
										tempeffects[7]=0;
									}
									else
									{
										tempeffects[0]=1;
										tempeffects[1]=0;
										tempeffects[2]=0;
										tempeffects[3]=0;
										tempeffects[4]=0;
										tempeffects[5]=0;
										tempeffects[6]=1;
										tempeffects[7]=tval;
									}
									values[tempint]=String.valueOf(tval);
									if (donoreffects.get(cvn.toUpperCase())==null)
									{
										donoreffects.put(cvn.toUpperCase(), tempeffects);
									}
									else
									{
										double[] temptempeffects=donoreffects.get(cvn.toUpperCase());
										temptempeffects[0]=temptempeffects[0]+tempeffects[0];
										temptempeffects[1]=temptempeffects[1]+tempeffects[1];
										temptempeffects[2]=temptempeffects[2]+tempeffects[2];
										temptempeffects[3]=temptempeffects[3]+tempeffects[3];
										temptempeffects[4]=temptempeffects[4]+tempeffects[4];
										temptempeffects[5]=temptempeffects[5]+tempeffects[5];
										temptempeffects[6]=temptempeffects[6]+tempeffects[6];
										temptempeffects[7]=temptempeffects[7]+tempeffects[7];
										donoreffects.put(cvn.toUpperCase(), temptempeffects);
									}
								}
								treated++;
								values[posloc]="1";
								values[detpos]="";
								values[possol]="";
								values[postyp]="5";
							}
							else
							{
								for (Enumeration<String> en=correctedvarstotal.keys(); en.hasMoreElements();)
								{
									double[] tempeffects=new double[8];
									cvn=en.nextElement();
									tval=(correctedvarstotal.get(cvn)).doubleValue();
									tempint=(varrefdict.get(cvn)).intValue();
									if (!values[tempint].equals(""))
									{
										tempeffects[0]=1;
										tempeffects[1]=1;
										tempeffects[2]=Double.parseDouble(values[tempint]);
										tempeffects[3]=tval;
										tempeffects[4]=tval-Double.parseDouble(values[tempint]);
										tempeffects[5]=Math.abs(tval-Double.parseDouble(values[tempint]));
										tempeffects[6]=0;
										tempeffects[7]=0;
									}
									else
									{
										tempeffects[0]=1;
										tempeffects[1]=0;
										tempeffects[2]=0;
										tempeffects[3]=0;
										tempeffects[4]=0;
										tempeffects[5]=0;
										tempeffects[6]=1;
										tempeffects[7]=tval;
									}
									values[tempint]=String.valueOf(tval);
									if (donoreffects.get(cvn.toUpperCase())==null)
									{
										donoreffects.put(cvn.toUpperCase(), tempeffects);
									}
									else
									{
										double[] temptempeffects=donoreffects.get(cvn.toUpperCase());
										temptempeffects[0]=temptempeffects[0]+tempeffects[0];
										temptempeffects[1]=temptempeffects[1]+tempeffects[1];
										temptempeffects[2]=temptempeffects[2]+tempeffects[2];
										temptempeffects[3]=temptempeffects[3]+tempeffects[3];
										temptempeffects[4]=temptempeffects[4]+tempeffects[4];
										temptempeffects[5]=temptempeffects[5]+tempeffects[5];
										temptempeffects[6]=temptempeffects[6]+tempeffects[6];
										temptempeffects[7]=temptempeffects[7]+tempeffects[7];
										donoreffects.put(cvn.toUpperCase(), temptempeffects);
									}
								}
								treated++;
								values[posloc]="1";
								values[detpos]="";
								values[possol]="";
								values[postyp]="5";
							}
						}
					}
					if (isoutd)
					{
						for (int i=0; i<posvarid.length; i++)
						{
							outford[i]=values[posvarid[i]];
						}
						if (vargroup.length>0)
						{
							for (int i=0; i<vargroup.length; i++)
							{
								outford[i+posvarid.length+posvarid.length]=values[refgroup[i]];
							}
						}
						writtenind++;
						dwd.writenoapprox(outford);
					}
				}
				dw.writenoapprox(values);
			}
		}
		data.close();
		ftds.deletefile();
		if (vargroup.length>0) gftd.deletefile();
		if (errated==0)
			results.add(new LocalMessageGetter("%2862%<br>\n"));
		if ((treated==0) && (errated>0))
			results.add(new LocalMessageGetter("%2863%<br>\n"));
		if ((treated>0) && (errated>0))
		{
			results.add(new LocalMessageGetter("%2864%: "+String.valueOf(errated)+"<br>\n"));
			results.add(new LocalMessageGetter("%2865%: "+String.valueOf(treated)+"<br>\n"));
			if (errated>treated)
				results.add(new LocalMessageGetter("%3045%: "+String.valueOf(errated-treated)+"<br>\n"));
		}
		if (solvedgroups>0)
			results.add(new LocalMessageGetter("%2868%: "+String.valueOf(solvedgroups)+"<br>\n"));
		if (recordgnott>0)
			results.add(new LocalMessageGetter("%3046%: "+String.valueOf(recordgnott)+"<br>\n"));
		if (solved>0)
			results.add(new LocalMessageGetter("%3047%: "+String.valueOf(solved)+"<br>\n"));
		if (recordnott>0)
			results.add(new LocalMessageGetter("%3094%: "+String.valueOf(recordnott)+"<br>\n"));
		if (notvalidall>0)
			results.add(new LocalMessageGetter("%3095%: "+String.valueOf(notvalidall)+"<br>\n%3096%<br>\n"));
		if (solvedgfin>0)
			results.add(new LocalMessageGetter("%3102%: "+String.valueOf(solvedgfin)+"<br>\n"));
		if (nottreatedbnd>0)
			results.add(new LocalMessageGetter("%3097%: "+String.valueOf(notvalidall)+"<br>\n"));
		if (solvedgroupsnott>0)
			results.add(new LocalMessageGetter("%3098%: "+String.valueOf(solvedgroupsnott)+"<br>\n"));
		if (recordgnottnott>0)
			results.add(new LocalMessageGetter("%3099%: "+String.valueOf(recordgnottnott)+"<br>\n"));
		if (solvednot>0)
			results.add(new LocalMessageGetter("%3100%: "+String.valueOf(solvednot)+"<br>\n"));
		if (recordnottnot>0)
			results.add(new LocalMessageGetter("%3101%: "+String.valueOf(recordnottnot)+"<br>\n"));
		if (solvedgfinnot>0)
			results.add(new LocalMessageGetter("%3103%: "+String.valueOf(solvedgfinnot)+"<br>\n"));

		if (reducedforsecondary)
		{
			results.add(new LocalMessageGetter("%3189%<br>\n"));
			if (vargroup.length>0 && (correctedingws+correctedingwds+correctedingor>0))
			{
				if (correctedingws==0) results.add(new LocalMessageGetter("%3190%<br>\n"));
				if (correctedingws>0) results.add(new LocalMessageGetter("%3191%: "+String.valueOf(correctedingws)+"<br>\n"));
				if (correctedingwds>0) results.add(new LocalMessageGetter("%3192%: "+String.valueOf(correctedingwds)+"<br>\n"));
				if (correctedingor>0) results.add(new LocalMessageGetter("%3193%: "+String.valueOf(correctedingor)+"<br>\n"));
			}
			if (scorrectedingws+scorrectedingwds+scorrectedingor>0)
			{
				if (scorrectedingws==0) results.add(new LocalMessageGetter("%3194%<br>\n"));
				if (scorrectedingws>0) results.add(new LocalMessageGetter("%3195%: "+String.valueOf(scorrectedingws)+"<br>\n"));
				if (scorrectedingwds>0) results.add(new LocalMessageGetter("%3196%: "+String.valueOf(scorrectedingwds)+"<br>\n"));
				if (scorrectedingor>0) results.add(new LocalMessageGetter("%3197%: "+String.valueOf(scorrectedingor)+"<br>\n"));
			}
			if (vargroup.length>0 && (correctedingwsnot+correctedingwdsnot+correctedingornot>0))
			{
				if (correctedingwsnot==0) results.add(new LocalMessageGetter("%3198%<br>\n"));
				if (correctedingwsnot>0) results.add(new LocalMessageGetter("%3199%: "+String.valueOf(correctedingwsnot)+"<br>\n"));
				if (correctedingwdsnot>0) results.add(new LocalMessageGetter("%3200%: "+String.valueOf(correctedingwdsnot)+"<br>\n"));
				if (correctedingornot>0) results.add(new LocalMessageGetter("%3201%: "+String.valueOf(correctedingornot)+"<br>\n"));
			}
			if (scorrectedingwsnot+scorrectedingwdsnot+scorrectedingornot>0)
			{
				if (scorrectedingwsnot==0) results.add(new LocalMessageGetter("%3202%<br>\n"));
				if (scorrectedingwsnot>0) results.add(new LocalMessageGetter("%3203%: "+String.valueOf(scorrectedingwsnot)+"<br>\n"));
				if (scorrectedingwdsnot>0) results.add(new LocalMessageGetter("%3204%: "+String.valueOf(scorrectedingwdsnot)+"<br>\n"));
				if (scorrectedingornot>0) results.add(new LocalMessageGetter("%3205%: "+String.valueOf(scorrectedingornot)+"<br>\n"));
			}
		}
		if (donoreffects.size()==0) statcreated=false;

		DataSetUtilities dsus=new DataSetUtilities();
		if (isouts)
		{
			Hashtable<String, String> tempmd=new Hashtable<String, String>();
			Hashtable<String, String> varfmt=new Hashtable<String, String>();
			for (Enumeration<String> en=donoreffects.keys(); en.hasMoreElements();)
			{
				String tvname=en.nextElement();
				varfmt.put(tvname, dict.getvarlabelfromname(tvname));
			}
			dsus.addnewvar("refvar", "%3173%", Keywords.TEXTSuffix, varfmt, tempmd);
			dsus.addnewvar("tot_donated", "%3175%", Keywords.NUMSuffix+Keywords.INTSuffix, tempmd, tempmd);
			dsus.addnewvar("tot_don_no_mis", "%3176%", Keywords.NUMSuffix+Keywords.INTSuffix, tempmd, tempmd);
			dsus.addnewvar("orig_sum_no_mis", "%3177%", Keywords.NUMSuffix, tempmd, tempmd);
			dsus.addnewvar("new_sum_no_mis", "%3178%", Keywords.NUMSuffix, tempmd, tempmd);
			dsus.addnewvar("diff_no_mis", "%3179%", Keywords.NUMSuffix, tempmd, tempmd);
			dsus.addnewvar("abs_diff_no_mis", "%3180%", Keywords.NUMSuffix, tempmd, tempmd);
			dsus.addnewvar("av_diff_no_mis", "%3181%", Keywords.NUMSuffix, tempmd, tempmd);
			dsus.addnewvar("av_abs_diff_no_mis", "%3182%", Keywords.NUMSuffix, tempmd, tempmd);
			dsus.addnewvar("tot_don_min", "%3183%", Keywords.NUMSuffix+Keywords.INTSuffix, tempmd, tempmd);
			dsus.addnewvar("sum_mis", "%3184%", Keywords.NUMSuffix, tempmd, tempmd);
			dsus.addnewvar("av_mis", "%3185%", Keywords.NUMSuffix, tempmd, tempmd);
			dsus.addnewvar("tot_sum", "%3186%", Keywords.NUMSuffix, tempmd, tempmd);
			if (!dws.opendatatable(dsus.getfinalvarinfo()))
				return new Result(dws.getmessage(), false, null);
			if (statcreated)
			{
				for (Enumeration<String> en=donoreffects.keys(); en.hasMoreElements();)
				{
					String tvname=en.nextElement();
					double[] temptempeffects=donoreffects.get(tvname);
					String[] writeinstat=new String[13];
					writeinstat[0]=tvname;
					writeinstat[1]=String.valueOf(temptempeffects[0]);
					writeinstat[2]=String.valueOf(temptempeffects[1]);
					writeinstat[3]=String.valueOf(temptempeffects[2]);
					writeinstat[4]=String.valueOf(temptempeffects[3]);
					writeinstat[5]=String.valueOf(temptempeffects[4]);
					writeinstat[6]=String.valueOf(temptempeffects[5]);
					writeinstat[7]=String.valueOf(temptempeffects[4]/temptempeffects[1]);
					writeinstat[8]=String.valueOf(temptempeffects[5]/temptempeffects[1]);
					writeinstat[9]=String.valueOf(temptempeffects[6]);
					writeinstat[10]=String.valueOf(temptempeffects[7]);
					writeinstat[11]=String.valueOf(temptempeffects[7]/temptempeffects[6]);
					writeinstat[12]=String.valueOf(temptempeffects[3]+temptempeffects[7]);
					dws.write(writeinstat);
				}
			}
			else
			{
				results.add(new LocalMessageGetter("%3210%<br>\n"));
				dws.deletetmp();
			}
		}
		if (writtenind==0 && donorcreated)
		{
			results.add(new LocalMessageGetter("%3224%<br>\n"));
			dwd.deletetmp();
		}


		String keyword="Donor imputation "+dict.getkeyword();
		String description="Donor imputation "+dict.getdescription();
		String author=dict.getauthor();

		Keywords.percentage_total=0;
		Keywords.percentage_done=0;

		boolean resclose=dw.close();
		if (!resclose)
			return new Result(dw.getmessage(), false, null);
		if (isouts && statcreated)
		{
			boolean rescloses=dws.close();
			if (!rescloses)
				return new Result(dws.getmessage(), false, null);
		}
		if (isoutd && writtenind>0)
		{
			boolean resclosed=dwd.close();
			if (!resclosed)
				return new Result(dwd.getmessage(), false, null);
		}
		Vector<Hashtable<String, String>> tablevariableinfo=dw.getVarInfo();
		Hashtable<String, String> datatableinfo=dw.getTableInfo();
		results.add(new LocalDictionaryWriter(dw.getdictpath(), keyword, description, author, dw.gettabletype(),
		datatableinfo, dsu.getfinalvarinfo(), tablevariableinfo, dsu.getfinalcl(), dsu.getfinalmd(), null));
		if (isouts && statcreated)
		{
			Vector<Hashtable<String, String>> tablevariableinfos=dws.getVarInfo();
			Hashtable<String, String> datatableinfos=dws.getTableInfo();
			results.add(new LocalDictionaryWriter(dws.getdictpath(), keyword, description, author, dws.gettabletype(),
			datatableinfos, dsus.getfinalvarinfo(), tablevariableinfos, dsus.getfinalcl(), dsus.getfinalmd(), null));
		}
		if (isoutd && writtenind>0)
		{
			Vector<Hashtable<String, String>> tablevariableinfod=dwd.getVarInfo();
			Hashtable<String, String> datatableinfod=dwd.getTableInfo();
			results.add(new LocalDictionaryWriter(dwd.getdictpath(), keyword, description, author, dwd.gettabletype(),
			datatableinfod, dsud.getfinalvarinfo(), tablevariableinfod, dsud.getfinalcl(), dsud.getfinalmd(), null));
		}
		return new Result("", true, results);
	}
	/**
	*Returns the parameters for the procedure
	*/
	public LinkedList<GetRequiredParameters> getparameters()
	{
		LinkedList<GetRequiredParameters> parameters=new LinkedList<GetRequiredParameters>();
		String[] dep ={""};
		parameters.add(new GetRequiredParameters(Keywords.dict+"=", "dict", true, 2831, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.dict+"e=", "dict", true, 2475, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.dict+"r=", "dict", false, 2857, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.OUT.toLowerCase()+"=", "setting=out", true, 2830, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.OUTS.toLowerCase()+"=", "setting=out", false, 3174, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.OUTD.toLowerCase()+"=", "setting=out", false, 3225, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.viewout, "checkbox", false, 1612, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.maxdonors, "text", false, 2832, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.mindonors, "text", false, 3076, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.mustmatch, "text", false, 2833, dep, "", 2));
		dep = new String[1];
		dep[0]=Keywords.dict;
		parameters.add(new GetRequiredParameters(Keywords.vargroup, "vars=all", false, 655, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.varid, "vars=all", false, 3236, dep, "", 2));
		dep = new String[0];
		parameters.add(new GetRequiredParameters(Keywords.tolerance, "text", false, 2595, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.consideralsoall, "checkbox", false, 2866, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.reducedforsecondary, "checkbox", false, 3187, dep, "", 2));
		parameters.add(new GetRequiredParameters("", "note", false, 3079, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.useeuclidean, "checkbox", false, 2872, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.norandomimpute, "checkbox", false, 3092, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.where,"text", false, 2805,dep,"",2));
		parameters.add(new GetRequiredParameters(Keywords.replace, "listsingle=530_NULL,531_"+Keywords.replaceall+",532_"+Keywords.replaceformat+",533_"+Keywords.replacemissing,false, 534, dep, "", 2));
		return parameters;
	}
	/**
	*Returns the group and the name of the procedure
	*/
	public String[] getstepinfo()
	{
		String[] retprocinfo=new String[2];
		retprocinfo[0]="2439";
		retprocinfo[1]="2835";
		return retprocinfo;
	}
}
