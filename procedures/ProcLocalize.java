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
import java.util.TreeMap;
import java.util.Iterator;
import java.util.Random;

import ADaMSoft.dataaccess.DataReader;
import ADaMSoft.dataaccess.DataWriter;
import ADaMSoft.dataaccess.DictionaryReader;
import ADaMSoft.keywords.Keywords;
import ADaMSoft.utilities.GetRequiredParameters;
import ADaMSoft.utilities.DataSetUtilities;
import ADaMSoft.utilities.ObjectTransformer;
import ADaMSoft.utilities.StepUtilities;

import ADaMSoft.algorithms.EditsAnalyzer;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
* This is the procedure that localizes the variables to change
* @author marco.scarno@gmail.com
* @date 14/02/2017
*/
public class ProcLocalize extends ObjectTransformer implements RunStep
{
	String formatsol;
	/**
	* Starts the execution of Proc Localize
	*/
	@SuppressWarnings("unused")
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		String [] requiredparameters=new String[] {Keywords.OUT.toLowerCase(), Keywords.dict, Keywords.dict+"e", Keywords.OUTI.toLowerCase()};
		String [] optionalparameters=new String[] {Keywords.casual, Keywords.considerall, Keywords.tolerance, Keywords.weight, Keywords.noforcemd, Keywords.maxvar, Keywords.varsonestepsol, Keywords.secondstep, Keywords.nomaxdetsol, Keywords.logfile, Keywords.maxcardfordetsol, Keywords.useonlyintdetsol, Keywords.setzero, Keywords.replace};
		Keywords.percentage_total=0;
		Keywords.percentage_done=0;
		StepUtilities steputilities=new StepUtilities();
		if (!steputilities.checkParameters(requiredparameters, optionalparameters, parameters))
		{
			return new Result(steputilities.getMessage(), false, null);
		}
		Keywords.percentage_total=5;

		boolean noforcemd=(parameters.get(Keywords.noforcemd.toLowerCase())!=null);
		boolean selcasual=(parameters.get(Keywords.casual.toLowerCase())!=null);
		boolean considerall=(parameters.get(Keywords.considerall.toLowerCase())!=null);
		boolean nomaxdetsol=(parameters.get(Keywords.nomaxdetsol.toLowerCase())!=null);
		boolean useonlyintdetsol=(parameters.get(Keywords.useonlyintdetsol.toLowerCase())!=null);

		String tempmaxcardfordetsol=(String)parameters.get(Keywords.maxcardfordetsol.toLowerCase());

		boolean secondstep=(parameters.get(Keywords.secondstep.toLowerCase())!=null);

		if ((selcasual) && (considerall))
			return new Result("%2633%<br>\n", false, null);

		String replace =(String)parameters.get(Keywords.replace);

		String debugfile=(String)parameters.get("debugfile");

		String weight=(String)parameters.get(Keywords.weight);
		Hashtable<String, Double> weights=new Hashtable<String, Double>();
		if (weight!=null)
		{
			try
			{
				String[] tempw=weight.split(";");
				for (int i=0; i<tempw.length; i++)
				{
					String[] partsw=tempw[i].split("=");
					if (partsw.length!=2)
						return new Result("%2542%\n", false, null);
					double weightval=Double.parseDouble(partsw[1].trim());
					partsw[0]=partsw[0].trim();
					if (Double.isNaN(weightval))
						return new Result("%2542%\n", false, null);
					weights.put(partsw[0].toUpperCase(), new Double(weightval));
				}
			}
			catch (Exception e)
			{
				return new Result("%2542%<br>\n", false, null);
			}
		}
		String varsonestepsol =(String)parameters.get(Keywords.varsonestepsol);
		String logfile =(String)parameters.get(Keywords.logfile);
		if (logfile!=null)
		{
			try
			{
				BufferedWriter out = new BufferedWriter(new FileWriter(logfile));
				out.write("Starting the localization procedure");
				out.close();
			}
			catch (Exception e)
			{
				return new Result("%3167% ("+e.toString()+")<br>\n", false, null);
			}
		}

		String tempsetzero=(String)parameters.get(Keywords.setzero);
		double setzero=0.00000000001;
		if (tempsetzero!=null)
		{
			try
			{
				setzero=Double.parseDouble(tempsetzero);
			}
			catch (Exception en)
			{
				return new Result("%3212%<br>\n", false, null);
			}
			if (setzero>0.01)
				return new Result("%3212%<br>\n", false, null);
		}

		int maxcardfordetsol=10;
		if (tempmaxcardfordetsol!=null)
		{
			try
			{
				maxcardfordetsol=Integer.parseInt(tempmaxcardfordetsol);
			}
			catch (Exception en)
			{
				return new Result("%3172%<br>\n", false, null);
			}
			if (maxcardfordetsol<0)
				return new Result("%3172%<br>\n", false, null);
		}

		int ivosts=10;
		if (varsonestepsol!=null)
		{
			try
			{
				ivosts=Integer.parseInt(varsonestepsol);
			}
			catch (Exception en)
			{
				return new Result("%3059%<br>\n", false, null);
			}
			if (ivosts<4)
				return new Result("%3059%<br>\n", false, null);
		}
		String smaxvar =(String)parameters.get(Keywords.maxvar);
		int maxvar=50;
		if (smaxvar!=null)
		{
			try
			{
				maxvar=Integer.parseInt(smaxvar);
			}
			catch (Exception en)
			{
				return new Result("%2870%<br>\n", false, null);
			}
			if (maxvar<10)
				return new Result("%2870%<br>\n", false, null);
		}


		String stolerance =(String)parameters.get(Keywords.tolerance);
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

		DictionaryReader dicte = (DictionaryReader)parameters.get(Keywords.dict+"e");
		DictionaryReader dict = (DictionaryReader)parameters.get(Keywords.dict);
		String tempvname="";

		int issecondstep=0;
		int pos_sol_type=-1;

		for (int i=0; i<dict.gettotalvar(); i++)
		{
			tempvname=dict.getvarname(i);
			if (tempvname.equalsIgnoreCase("cardinality_localize")) issecondstep++;
			if (tempvname.equalsIgnoreCase("solution_type")) issecondstep++;
			if (tempvname.equalsIgnoreCase("result_localize")) issecondstep++;
			if (tempvname.equalsIgnoreCase("result_localize_all")) issecondstep++;
			if (tempvname.equalsIgnoreCase("deterministic_locvars")) issecondstep++;
			if (tempvname.equalsIgnoreCase("solution_localize"))
			{
				issecondstep++;
				pos_sol_type=i;
			}
		}

		if (issecondstep!=5 && secondstep)
				return new Result("3080%<br>\n", false, null);

		int refvar=dicte.gettotalvar()-2;

		String[] varref=new String[refvar];
		int posv=0;
		String realvname="";
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
				varref[posv]=realvname;
				vartotake[posv+2]=tempvname;
				if (weights.get(realvname.toUpperCase())==null)
					weights.put(realvname.toUpperCase(), new Double(1.0));
				reprule[posv]=1;
				posv++;
			}
		}
		vartotake[0]="_sign_";
		reprule[0]=1;
		reprule[1]=1;
		vartotake[1]="_b_";

		Keywords.percentage_done=1;

		Hashtable<Integer, Integer> positionvars=new Hashtable<Integer, Integer>();
		Hashtable<Integer, Double> valueweights=new Hashtable<Integer, Double>();
		boolean found=false;
		for (int i=0; i<refvar; i++)
		{
			found=false;
			for (int j=0; j<dict.gettotalvar(); j++)
			{
				if (varref[i].equalsIgnoreCase(dict.getvarname(j)))
				{
					found=true;
					positionvars.put(new Integer(i), new Integer(j));
					valueweights.put(new Integer(i), weights.get(varref[i].toUpperCase()));
					weights.remove(varref[i].toUpperCase());
					break;
				}
			}
			if (!found)
				return new Result("%2469%: "+varref[i]+"<br>\n", false, null);
		}
		if (weights.size()!=0)
		{
			tempvname="";
			for (Enumeration<String> ew = weights.keys() ; ew.hasMoreElements() ;)
			{
				tempvname =tempvname+" "+ew.nextElement();
			}
			tempvname=tempvname.trim();
			return new Result("%2545% ("+tempvname+")<br>\n", false, null);
		}

		DataWriter dw=new DataWriter(parameters, Keywords.OUT.toLowerCase());
		if (!dw.getmessage().equals(""))
			return new Result(dw.getmessage(), false, null);

		DataWriter dwi=new DataWriter(parameters, Keywords.OUTI.toLowerCase());
		if (!dwi.getmessage().equals(""))
			return new Result(dwi.getmessage(), false, null);

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

		Keywords.percentage_done=2;

		String tempdir=(String)parameters.get(Keywords.WorkDir);

		EditsAnalyzer ea=new EditsAnalyzer(tempdir);
		ea.setEdits(coeffvaliq, coeffvaleq);
		ea.setWeights(valueweights);
		ea.setVarName(varref);
		ea.setvarsonestepsol(ivosts);
		ea.setzeroalg(setzero);

		ea.setuseonlyintdetsol(useonlyintdetsol);
		ea.setmaxcardfordetsol(maxcardfordetsol);

		if (debugfile!=null)
			ea.printonfile(debugfile);
		int groups=ea.checkDisjointEdits();
		String keyword="Localize "+dict.getkeyword();
		String description="Localize "+dict.getdescription();
		String author=(String) parameters.get(Keywords.client_host.toLowerCase());
		int rifrep=0;
		if (replace==null)
			rifrep=0;
		else if (replace.equalsIgnoreCase(Keywords.replaceall))
			rifrep=1;
		else if (replace.equalsIgnoreCase(Keywords.replaceformat))
			rifrep=2;
		else if (replace.equalsIgnoreCase(Keywords.replacemissing))
			rifrep=3;
		DataReader data = new DataReader(dict);
		if (!data.open(null, rifrep, false))
			return new Result(data.getmessage(), false, null);
		Hashtable<String, String> tempmd=new Hashtable<String, String>();
		Hashtable<String, String> fmtvar=new Hashtable<String, String>();
		fmtvar.put("9","%2499%");
		fmtvar.put("99","%2580%");
		fmtvar.put("999","%2583%");
		fmtvar.put("9999","%2467%");
		fmtvar.put("1","%2501%");

		Hashtable<String, String> fmtsol=new Hashtable<String, String>();
		fmtsol.put("1","%2817%");
		fmtsol.put("2","%2818%");
		fmtsol.put("3","%2839%");
		fmtsol.put("4","%2836%");
		fmtsol.put("5","%2837%");
		fmtsol.put("6","%2838%");
		fmtsol.put("7","%2840%");
		fmtsol.put("8","%2841%");
		fmtsol.put("9","%2854%");
		fmtsol.put("10","%2867%");

		DataSetUtilities dsu=new DataSetUtilities();
		dsu.setreplace(replace);
		dsu.defineolddict(dict);
		dsu.addnewvartoolddict("cardinality_localize", "%2566%", Keywords.NUMSuffix, tempmd, tempmd);
		dsu.addnewvartoolddict("solution_type", "%2819%", Keywords.TEXTSuffix, fmtsol, tempmd);
		if (!considerall)
			dsu.addnewvartoolddict("result_localize", "%2498%", Keywords.TEXTSuffix, fmtvar, tempmd);
		else
			dsu.addnewvartoolddict("result_localize_all", "%2543%", Keywords.TEXTSuffix, fmtvar, tempmd);
		dsu.addnewvartoolddict("deterministic_locvars", "%2852%", Keywords.TEXTSuffix, fmtvar, tempmd);
		dsu.addnewvartoolddict("solution_localize", "%2544%", Keywords.TEXTSuffix, tempmd, tempmd);
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

		String[] newvalues=new String[5];
		Vector<Integer> treatedmiss=new Vector<Integer>();
		String[] cvalues=null;
		Vector<Integer> refeditvars=new Vector<Integer>();
		double[] actualvalues=null;
		double tempdouble=Double.NaN;
		ea.setTolerance(tolerance);
		ea.setMaxvar(maxvar);
		boolean resultrecord=false;
		int totalrecord=0;
		int okrecord=0;
		int missingrecord=0;
		int missingtreated=0;
		int localizedrecord=0;
		int notlocalizedrecord=0;
		int mixedrecord=0;
		int detrecord=0;
		boolean misstreated;
		double currcard=0;
		double tempcard;
		boolean notloc;
		int corrected=0;
		Vector<Vector<double[]>> cursolval=new Vector<Vector<double[]>>();
		Vector<Vector<String[]>> cursolnam=new Vector<Vector<String[]>>();
		Vector<Vector<String[]>> cursoldet=new Vector<Vector<String[]>>();
		Vector<double[]> tempcursolval=null;
		Vector<String[]> tempcursolnam=null;
		Vector<String[]> tempcursoldet=null;
		String[] temptempcursolnam=null;
		String[] temptempcursoldet=null;
		double[] temptempcursolval=null;
		Random rndint=new Random();
		int tempint=0;
		int totaldet=0;
		int totalloc=0;
		double timetolocalize=0;
		long startTime=0;
		long endTime=0;
		TreeMap<String, Double> infovars=new TreeMap<String, Double>();
		TreeMap<Double, Integer> infocard=new TreeMap<Double, Integer>();
		Hashtable<String, Double> tempinfovars=new Hashtable<String, Double>();
		String tempname;
		double usedsolutions=0;
		double[] tta=null;
		String[] ttb=null;
		String temptest="";
		String transdet="";
		int maxtestedvar=0;
		int numvalmis=0;
		int num_rec_not_treated_bp=0;
		boolean to_do=false;
		double maxtime=0;
		long startTimeLog=0;
		long endTimeLog=0;
		int refmaxtime=0;

		Keywords.percentage_done=3;

		while (!data.isLast())
		{
			if (logfile!=null)
			{
				if ((double)(endTimeLog-startTimeLog)>maxtime)
				{
					maxtime=(double)(endTimeLog-startTimeLog);
					refmaxtime=totalrecord;
				}
				startTimeLog = System.currentTimeMillis();
				try
				{
					BufferedWriter out = new BufferedWriter(new FileWriter(logfile));
					out.write("Reading the record: "+String.valueOf(totalrecord)+"\n");
					out.write("Reference for max time: "+String.valueOf(refmaxtime)+", time needed: "+String.valueOf(maxtime/1000)+"\n");
					out.close();
				}
				catch (Exception e) {}
			}
			to_do=true;
			treatedmiss.clear();
			totalrecord++;
			values = data.getRecord();
			for (int i=0; i<5; i++)
			{
				newvalues[i]="";
			}
			found=false;
			misstreated=false;
			for (int i=0; i<refvar; i++)
			{
				posv=(positionvars.get(new Integer(i))).intValue();
				if (values[posv].equals("")) found=true;
				else
				{
					tempdouble=Double.NaN;
					try
					{
						tempdouble=Double.parseDouble(values[posv]);
					}
					catch (Exception e) {}
				}
				if (Double.isNaN(tempdouble)) found=true;
				if ( (!noforcemd) && (found))
				{
					treatedmiss.add(new Integer(posv));
					values[posv]="-1.0";
					found=false;
					misstreated=true;
				}
			}
			if (misstreated) missingtreated++;
			if (found)
			{
				missingrecord++;
				newvalues[2]="9";
				to_do=false;
			}
			if (secondstep)
			{
				if (!values[pos_sol_type].equals(""))
				{
					to_do=false;
					num_rec_not_treated_bp++;
				}
			}
			if (to_do)
			{
				currcard=0;
				notloc=false;
				corrected=0;
				cursolval.clear();
				cursolnam.clear();
				cursoldet.clear();
				for (int i=0; i<groups; i++)
				{
					refeditvars=ea.getvarinde(i);
					actualvalues=new double[refeditvars.size()];
					numvalmis=0;
					for (int j=0; j<refeditvars.size(); j++)
					{
						posv=positionvars.get(refeditvars.get(j));
						actualvalues[j]=0.0;
						try
						{
							actualvalues[j]=Double.parseDouble(values[posv]);
						}
						catch (Exception e) {}
						if (actualvalues[j]==-1.0) numvalmis++;
					}
					resultrecord=ea.recordVerifier(i, actualvalues);
					if (resultrecord) corrected++;
					if (!resultrecord && refeditvars.size()==numvalmis)
					{
						startTime = System.currentTimeMillis();
						ea.localizeallmiss(i);
						endTime= System.currentTimeMillis() - startTime;
						timetolocalize=timetolocalize+(double)endTime;
						tempcard=ea.getcardinality();
						currcard=currcard+tempcard;
						if (tempcard<0)
						{
							if ((ea.ismaxvar()) && (maxtestedvar<ea.getactualmaxvar())) maxtestedvar=ea.getactualmaxvar();
							notloc=true;
							break;
						}
						else
						{
							Vector<double[]> ta=new Vector<double[]>();
							Vector<String[]> tb=new Vector<String[]>();
							Vector<String[]> tc=new Vector<String[]>();
							if (!nomaxdetsol) ea.maximizeDet();
							tempcursolval=ea.getSolutions();
							for (int j=0; j<tempcursolval.size(); j++)
							{
								tta=tempcursolval.get(j);
								double[] ttta=new double[tta.length];
								for (int k=0; k<ttta.length; k++)
								{
									ttta[k]=tta[k];
								}
								ta.add(ttta);
							}
							cursolval.add(ta);
							tempcursolval.clear();
							tempcursolnam=ea.getSolutionsNames();
							for (int j=0; j<tempcursolnam.size(); j++)
							{
								ttb=tempcursolnam.get(j);
								String[] tttb=new String[tta.length];
								for (int k=0; k<tttb.length; k++)
								{
									tttb[k]=ttb[k];
								}
								tb.add(tttb);
							}
							cursolnam.add(tb);
							tempcursolnam.clear();
							tempcursoldet=ea.getDeterministicSolutions();
							for (int j=0; j<tempcursoldet.size(); j++)
							{
								if (tempcursoldet.get(j)!=null)
								{
									ttb=tempcursoldet.get(j);
									String[] tttb=new String[ttb.length];
									for (int k=0; k<tttb.length; k++)
									{
										tttb[k]=ttb[k];
									}
									tc.add(tttb);
								}
								else tc.add(null);
							}
							cursoldet.add(tc);
							tempcursoldet.clear();
						}
					}
					else if (!resultrecord && refeditvars.size()!=numvalmis)
					{
						startTime = System.currentTimeMillis();
						ea.localize(i);
						endTime= System.currentTimeMillis() - startTime;
						timetolocalize=timetolocalize+(double)endTime;
						tempcard=ea.getcardinality();
						currcard=currcard+tempcard;
						if (tempcard<0)
						{
							if ((ea.ismaxvar()) && (maxtestedvar<ea.getactualmaxvar())) maxtestedvar=ea.getactualmaxvar();
							notloc=true;
							break;
						}
						else
						{
							Vector<double[]> ta=new Vector<double[]>();
							Vector<String[]> tb=new Vector<String[]>();
							Vector<String[]> tc=new Vector<String[]>();
							if (!nomaxdetsol) ea.maximizeDet();
							tempcursolval=ea.getSolutions();
							for (int j=0; j<tempcursolval.size(); j++)
							{
								tta=tempcursolval.get(j);
								double[] ttta=new double[tta.length];
								for (int k=0; k<ttta.length; k++)
								{
									ttta[k]=tta[k];
								}
								ta.add(ttta);
							}
							cursolval.add(ta);
							tempcursolval.clear();
							tempcursolnam=ea.getSolutionsNames();
							for (int j=0; j<tempcursolnam.size(); j++)
							{
								ttb=tempcursolnam.get(j);
								String[] tttb=new String[ttb.length];
								for (int k=0; k<tttb.length; k++)
								{
									tttb[k]=ttb[k];
								}
								tb.add(tttb);
							}
							cursolnam.add(tb);
							tempcursolnam.clear();
							tempcursoldet=ea.getDeterministicSolutions();
							for (int j=0; j<tempcursoldet.size(); j++)
							{
								if (tempcursoldet.get(j)!=null)
								{
									ttb=tempcursoldet.get(j);
									String[] tttb=new String[ttb.length];
									for (int k=0; k<tttb.length; k++)
									{
										tttb[k]=ttb[k];
									}
									tc.add(tttb);
								}
								else tc.add(null);
							}
							cursoldet.add(tc);
							tempcursoldet.clear();
						}
					}
				}
				if (corrected==groups)
				{
					okrecord++;
					newvalues[2]="1";
				}
				else
				{
					if (notloc)
					{
						newvalues[2]="9999";
						notlocalizedrecord++;
					}
					else
					{
						if (!infocard.containsKey(new Double(currcard))) infocard.put(new Double(currcard), new Integer(1));
						else
						{
							tempint=(infocard.get(new Double(currcard))).intValue();
							infocard.put(new Double(currcard), new Integer(tempint+1));
						}
						usedsolutions=0;
						tempinfovars.clear();
						totaldet=0;
						totalloc=0;
						localizedrecord++;
						newvalues[0]=String.valueOf(currcard);
						if (selcasual)
						{
							usedsolutions=1;
							for (int i=0; i<cursolval.size(); i++)
							{
								tempcursolval=cursolval.get(i);
								tempcursolnam=cursolnam.get(i);
								tempcursoldet=cursoldet.get(i);
								tempint=rndint.nextInt(tempcursolval.size());
								temptempcursolnam=tempcursolnam.get(tempint);
								temptempcursolval=tempcursolval.get(tempint);
								totalloc=totalloc+temptempcursolnam.length;
								for (int j=0; j<temptempcursolnam.length; j++)
								{
									if (tempinfovars.get(temptempcursolnam[j].toUpperCase())==null)
										tempinfovars.put(temptempcursolnam[j].toUpperCase(), new Double(1.0));
									else
									{
										tempdouble=(tempinfovars.get(temptempcursolnam[j].toUpperCase())).doubleValue();
										tempdouble=tempdouble+1.0;
										tempinfovars.put(temptempcursolnam[j].toUpperCase(), new Double(tempdouble));
									}
									newvalues[2]=newvalues[2]+temptempcursolnam[j]+" ";
									newvalues[4]=newvalues[4]+String.valueOf(temptempcursolval[j])+" ";
								}
								if (tempcursoldet.get(tempint)!=null)
								{
									temptempcursoldet=tempcursoldet.get(tempint);
									if (temptempcursoldet!=null)
									{
										totaldet=totaldet+temptempcursoldet.length;
										for (int j=0; j<temptempcursoldet.length; j++)
										{
											newvalues[3]=newvalues[3]+temptempcursoldet[j]+" ";
										}
									}
									else newvalues[3]=newvalues[3]+"";
								}
								else newvalues[3]=newvalues[3]+"";
								newvalues[2]=newvalues[2].trim();
								newvalues[3]=newvalues[3].trim();
								newvalues[4]=newvalues[4].trim();
								if (i<(cursolval.size()-1))
								{
									newvalues[2]=newvalues[2]+" ";
									if (!newvalues[3].equals("")) newvalues[3]=newvalues[3]+" ";
									newvalues[4]=newvalues[4]+" ";
								}
							}
						}
						else if (!considerall)
						{
							usedsolutions=1;
							for (int i=0; i<cursolval.size(); i++)
							{
								tempcursolval=cursolval.get(i);
								tempcursolnam=cursolnam.get(i);
								tempcursoldet=cursoldet.get(i);
								temptempcursolnam=tempcursolnam.get(0);
								temptempcursolval=tempcursolval.get(0);
								totalloc=totalloc+temptempcursolnam.length;
								for (int j=0; j<temptempcursolnam.length; j++)
								{
									if (tempinfovars.get(temptempcursolnam[j].toUpperCase())==null)
										tempinfovars.put(temptempcursolnam[j].toUpperCase(), new Double(1.0));
									else
									{
										tempdouble=(tempinfovars.get(temptempcursolnam[j].toUpperCase())).doubleValue();
										tempdouble=tempdouble+1.0;
										tempinfovars.put(temptempcursolnam[j].toUpperCase(), new Double(tempdouble));
									}
									newvalues[2]=newvalues[2]+temptempcursolnam[j]+" ";
									newvalues[4]=newvalues[4]+String.valueOf(temptempcursolval[j])+" ";
								}
								if (tempcursoldet.get(0)!=null)
								{
									temptempcursoldet=tempcursoldet.get(0);
									if (temptempcursoldet!=null)
									{
										totaldet=totaldet+temptempcursoldet.length;
										for (int j=0; j<temptempcursoldet.length; j++)
										{
											newvalues[3]=newvalues[3]+temptempcursoldet[j]+" ";
										}
									}
									else newvalues[3]=newvalues[3]+"";
								}
								else newvalues[3]=newvalues[3]+"";
								newvalues[2]=newvalues[2].trim();
								newvalues[3]=newvalues[3].trim();
								newvalues[4]=newvalues[4].trim();
								if (i<(cursolval.size()-1))
								{
									newvalues[2]=newvalues[2]+" ";
									if (!newvalues[3].equals("")) newvalues[3]=newvalues[3]+" ";
									newvalues[4]=newvalues[4]+" ";
								}
							}
						}
						else
						{
							usedsolutions=cursolval.size();
							for (int i=0; i<cursolval.size(); i++)
							{
								tempcursolval=cursolval.get(i);
								tempcursolnam=cursolnam.get(i);
								tempcursoldet=cursoldet.get(i);
								for (int k=0; k<tempcursolnam.size(); k++)
								{
									temptempcursolnam=tempcursolnam.get(k);
									temptempcursolval=tempcursolval.get(k);
									totalloc=totalloc+temptempcursolnam.length;
									for (int j=0; j<temptempcursolnam.length; j++)
									{
										if (tempinfovars.get(temptempcursolnam[j].toUpperCase())==null)
											tempinfovars.put(temptempcursolnam[j].toUpperCase(), new Double(1.0));
										else
										{
											tempdouble=(tempinfovars.get(temptempcursolnam[j].toUpperCase())).doubleValue();
											tempdouble=tempdouble+1.0;
											tempinfovars.put(temptempcursolnam[j].toUpperCase(), new Double(tempdouble));
										}
										newvalues[2]=newvalues[2]+temptempcursolnam[j]+" ";
										newvalues[4]=newvalues[4]+String.valueOf(temptempcursolval[j])+" ";
									}
									if (tempcursoldet.get(k)!=null)
									{
										temptempcursoldet=tempcursoldet.get(k);
										if (temptempcursoldet!=null)
										{
											totaldet=totaldet+temptempcursoldet.length;
											for (int j=0; j<temptempcursoldet.length; j++)
											{
												newvalues[3]=newvalues[3]+temptempcursoldet[j]+" ";
											}
										}
										else newvalues[3]=newvalues[3]+"- ";
									}
									else newvalues[3]=newvalues[3]+"- ";
									newvalues[2]=newvalues[2].trim();
									newvalues[3]=newvalues[3].trim();
									newvalues[4]=newvalues[4].trim();
									if (k<(tempcursolnam.size()-1))
									{
										newvalues[2]=newvalues[2]+", ";
										newvalues[3]=newvalues[3]+", ";
										newvalues[4]=newvalues[4]+", ";
									}
								}
								if (i<(cursolval.size()-1))
								{
									newvalues[2]=newvalues[2].trim()+";";
									newvalues[3]=newvalues[3].trim()+";";
									newvalues[4]=newvalues[4].trim()+";";
								}
							}
						}
						for (Enumeration<String> en=tempinfovars.keys(); en.hasMoreElements();)
						{
							tempname=en.nextElement();
							tempdouble=(tempinfovars.get(tempname)).doubleValue();
							if (!infovars.containsKey(tempname))
								infovars.put(tempname, new Double(tempdouble/usedsolutions));
							else
							{
								tempdouble=(tempdouble/usedsolutions)+(infovars.get(tempname)).doubleValue();
								infovars.put(tempname, new Double(tempdouble));
							}
						}
						if ((totalloc>0) && (totalloc==totaldet))
						{
							detrecord++;
							newvalues[1]="1";
						}
						if ((totaldet>0) && (totalloc>totaldet))
						{
							newvalues[1]="3";
							mixedrecord++;
						}
						if ((totalloc>0) && (totaldet==0)) newvalues[1]="2";
						try
						{
							transdet=newvalues[3].trim();
							transdet=transdet.replaceAll(";","");
							transdet=transdet.replaceAll(",","");
							transdet=transdet.replaceAll("-","");
							transdet=transdet.trim();
							if (transdet.equals("")) newvalues[3]="";
						}
						catch (Exception ed) {}
					}
				}
			}
			if (treatedmiss.size()>0)
			{
				for (int i=0; i<treatedmiss.size(); i++)
				{
					tempint=(treatedmiss.get(i)).intValue();
					values[tempint]="";
				}
				treatedmiss.clear();
			}
			cvalues=dsu.getnewvalues(values, newvalues);
			dw.writenoapprox(cvalues);
			System.gc();
			if (logfile!=null) endTimeLog = System.currentTimeMillis();
		}
		data.close();

		Keywords.percentage_done=4;

		Vector<StepResult> results = new Vector<StepResult>();
		results.add(new LocalMessageGetter("%2643%: "+String.valueOf(totalrecord)+"<br>\n"));
		if (totalrecord-okrecord-num_rec_not_treated_bp>0)
			results.add(new LocalMessageGetter("%2608%: "+String.valueOf(totalrecord-okrecord-num_rec_not_treated_bp)+"<br>\n"));
		if (totalrecord==okrecord+num_rec_not_treated_bp)
			results.add(new LocalMessageGetter("%2565%<br>\n"));
		if ((totalrecord>okrecord+num_rec_not_treated_bp) && (okrecord>0))
			results.add(new LocalMessageGetter("%2635% "+okrecord+"<br>\n"));
		if (notlocalizedrecord>0)
			results.add(new LocalMessageGetter("%2632% ("+notlocalizedrecord+")<br>\n"));
		if (missingrecord>0)
			results.add(new LocalMessageGetter("%2634% ("+missingrecord+")<br>\n"));
		if (localizedrecord>0)
			results.add(new LocalMessageGetter("%2636%: "+localizedrecord+"<br>\n"));
		if (detrecord>0)
			results.add(new LocalMessageGetter("%2815%: "+detrecord+"<br>\n"));
		int numimputation=localizedrecord-detrecord;
		if (numimputation>0)
			results.add(new LocalMessageGetter("%2816%: "+numimputation+"<br>\n"));
		if (mixedrecord>0)
			results.add(new LocalMessageGetter("%2851%: "+mixedrecord+"<br>\n"));
		if (num_rec_not_treated_bp>0)
			results.add(new LocalMessageGetter("%3081%: "+num_rec_not_treated_bp+"<br>\n"));
		if (localizedrecord>0)
		{
			NumberFormat formatter = new DecimalFormat("######.##");
			String ssec = formatter.format(timetolocalize/(localizedrecord*1000));
			results.add(new LocalMessageGetter("%2505%: "+ssec+"<br>\n"));
		}
		if ((localizedrecord==0) && (notlocalizedrecord>0))
			results.add(new LocalMessageGetter("%2584%<br>\n"));
		if (maxtestedvar>0 && maxvar<maxtestedvar)
			results.add(new LocalMessageGetter("%2871% ("+String.valueOf(maxtestedvar)+")<br>\n"));

		DataSetUtilities dsui=new DataSetUtilities();

		Hashtable<String, String> tempcl=new Hashtable<String, String>();
		tempcl.put("tot","%2602%");
		tempcl.put("ok","%2509%");
		tempcl.put("md","%2510%");
		tempcl.put("nt","%2511%");
		tempcl.put("loc","%2636%");
		tempcl.put("numdet","%2815%");
		tempcl.put("nummix","%2851%");
		tempcl.put("numloc","%2816%");
		int refcardi=1;
		Iterator<Double> itresc =  infocard.keySet().iterator();
		while(itresc.hasNext())
		{
			double a=(itresc.next()).doubleValue();
			tempcl.put("card_"+String.valueOf(refcardi), "%2512% "+String.valueOf(a));
			refcardi++;
		}
		Iterator<String> itvars =  infovars.keySet().iterator();
		while(itvars.hasNext())
		{
			tempname=itvars.next();
			tempcl.put("index_var_"+tempname, "%2558% "+(dict.getvarlabelfromname(tempname)).toUpperCase());
		}
		dsui.addnewvar("info", "%2507%", Keywords.TEXTSuffix, tempcl, tempmd);
		dsui.addnewvar("number", "%2508%", Keywords.NUMSuffix, tempmd, tempmd);
		if (!dwi.opendatatable(dsui.getfinalvarinfo()))
			return new Result(dwi.getmessage(), false, null);
		newvalues=new String[2];
		newvalues[0]="tot";
		newvalues[1]=String.valueOf(totalrecord);
		dwi.write(newvalues);
		newvalues[0]="ok";
		newvalues[1]=String.valueOf(okrecord);
		dwi.write(newvalues);
		newvalues[0]="md";
		newvalues[1]=String.valueOf(missingrecord);
		dwi.write(newvalues);
		newvalues[0]="nt";
		newvalues[1]=String.valueOf(notlocalizedrecord);
		dwi.write(newvalues);
		newvalues[0]="loc";
		newvalues[1]=String.valueOf(localizedrecord);
		dwi.write(newvalues);
		newvalues[0]="numdet";
		newvalues[1]=String.valueOf(detrecord);
		dwi.write(newvalues);
		newvalues[0]="nummix";
		newvalues[1]=String.valueOf(mixedrecord);
		dwi.write(newvalues);
		newvalues[0]="numloc";
		newvalues[1]=String.valueOf(numimputation);
		dwi.write(newvalues);
		itresc =  infocard.keySet().iterator();
		refcardi=1;
		while(itresc.hasNext())
		{
			double a=(itresc.next()).doubleValue();
			tempint=(infocard.get(new Double(a))).intValue();
			newvalues[0]="card_"+String.valueOf(refcardi);
			newvalues[1]=String.valueOf(tempint);
			dwi.write(newvalues);
			refcardi++;
		}
		itvars =  infovars.keySet().iterator();
		while(itvars.hasNext())
		{
			tempname=itvars.next();
			double a=(infovars.get(tempname)).doubleValue();
			newvalues[0]="index_var_"+tempname;
			newvalues[1]=String.valueOf(100*a/localizedrecord);
			dwi.write(newvalues);
		}

		boolean resclose=dw.close();
		if (!resclose)
			return new Result(dw.getmessage(), false, null);
		Vector<Hashtable<String, String>> tablevariableinfo=dw.getVarInfo();
		Hashtable<String, String> datatableinfo=dw.getTableInfo();
		results.add(new LocalDictionaryWriter(dw.getdictpath(), keyword, description, author, dw.gettabletype(),
		datatableinfo, dsu.getfinalvarinfo(), tablevariableinfo, dsu.getfinalcl(), dsu.getfinalmd(), null));
		resclose=dwi.close();
		if (!resclose)
			return new Result(dwi.getmessage(), false, null);
		Vector<Hashtable<String, String>> tablevariableinfoi=dwi.getVarInfo();
		Hashtable<String, String> datatableinfoi=dwi.getTableInfo();
		results.add(new LocalDictionaryWriter(dwi.getdictpath(), keyword, description, author, dwi.gettabletype(),
		datatableinfoi, dsui.getfinalvarinfo(), tablevariableinfoi, dsui.getfinalcl(), dsui.getfinalmd(), null));

		return new Result("", true, results);
	}
	/**
	*Returns the parameters for the procedure
	*/
	public LinkedList<GetRequiredParameters> getparameters()
	{
		LinkedList<GetRequiredParameters> parameters=new LinkedList<GetRequiredParameters>();
		String[] dep ={""};
		parameters.add(new GetRequiredParameters(Keywords.dict+"=", "dict", true, 2474, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.dict+"e=", "dict", true, 2475, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.OUT.toLowerCase()+"=", "setting=out", true, 2601, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.OUTI.toLowerCase()+"=", "setting=out", true, 2496, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.viewout, "checkbox", false, 1612, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.weight, "multipletext", false, 2540, dep, "", 2));
		parameters.add(new GetRequiredParameters("", "note", false, 2541, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.considerall, "checkbox", false, 2539, dep, "", 2));
		parameters.add(new GetRequiredParameters("", "note", false, 2593, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.casual, "checkbox", false, 2589, dep, "", 2));
		parameters.add(new GetRequiredParameters("", "note", false, 2593, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.noforcemd, "checkbox", false, 2518, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.tolerance, "text", false, 2595, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.maxvar, "text", false, 2869, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.setzero, "text", false, 3211, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.varsonestepsol, "text", false, 3058, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.secondstep, "checkbox", false, 3078, dep, "", 2));
		parameters.add(new GetRequiredParameters("", "note", false, 3079, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.nomaxdetsol, "checkbox", false, 3104, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.maxcardfordetsol, "text", false, 3171, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.useonlyintdetsol, "checkbox", false, 3170, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.logfile, "filesave=.txt", false, 3166, dep, "", 2));
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
		retprocinfo[1]="2576";
		return retprocinfo;
	}
}
