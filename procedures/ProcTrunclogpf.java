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
import ADaMSoft.dataaccess.GroupedMatrix2Dfile;

import ADaMSoft.keywords.Keywords;

import ADaMSoft.utilities.StepUtilities;
import ADaMSoft.utilities.GetRequiredParameters;
import ADaMSoft.utilities.ValuesParser;
import ADaMSoft.utilities.VariableUtilities;
import ADaMSoft.utilities.DataSetUtilities;
import ADaMSoft.utilities.ObjectTransformer;

import ADaMSoft.algorithms.VarGroupModalities;

import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.linalg.SingularValueDecomposition;

/**
* This is the procedure that estimates the weight for each unit according to the rim-weighting vincolated logaritmic truncated function
* @author marco.scarno@gmail.com
* @date 14/02/2017
*/
public class ProcTrunclogpf extends ObjectTransformer implements RunStep
{
	/**
	* Starts the execution of Proc Trunclogpf
	*/
	@SuppressWarnings("unused")
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		boolean novgconvert=false;
		int toistvw=-1;
		String [] requiredparameters=new String[] {Keywords.OUT.toLowerCase(), Keywords.dict, Keywords.dict+"w",
		Keywords.varname, Keywords.varfreq};
		String [] optionalparameters=new String[] {Keywords.varval, Keywords.vargroup, Keywords.varincprob,
		Keywords.variniweight, Keywords.replace, Keywords.weightname, Keywords.todisk, Keywords.iterations,
		Keywords.accuracy, Keywords.tablewithfreq, Keywords.uweight, Keywords.lweight, Keywords.vargroupinuniverse,
		Keywords.popdim, Keywords.novgconvert, Keywords.where};
		Keywords.percentage_total=0;
		Keywords.percentage_done=0;
		StepUtilities steputilities=new StepUtilities();
		if (!steputilities.checkParameters(requiredparameters, optionalparameters, parameters))
		{
			return new Result(steputilities.getMessage(), false, null);
		}
		novgconvert =(parameters.get(Keywords.novgconvert)!=null);

		String weightmax=(String)parameters.get(Keywords.uweight.toLowerCase());
		String weightmin=(String)parameters.get(Keywords.lweight.toLowerCase());
		double U=1.5;
		if (weightmax!=null)
		{
			U=string2double(weightmax);
		}
		double L=0.5;
		if (weightmin!=null)
		{
			L=string2double(weightmin);
		}
		if (U==0)
			return new Result("%1296%<br>\n", false, null);
		if (U<=1)
			return new Result("%1575%<br>\n", false, null);
		if (L<=0)
			return new Result("%1297%<br>\n", false, null);
		if (L>=1)
			return new Result("%1574%<br>\n", false, null);

		DataWriter dw=new DataWriter(parameters, Keywords.OUT.toLowerCase());
		if (!dw.getmessage().equals(""))
			return new Result(dw.getmessage(), false, null);

		DictionaryReader dictw = (DictionaryReader)parameters.get(Keywords.dict+"w");
		DictionaryReader dict = (DictionaryReader)parameters.get(Keywords.dict);

		boolean todisk=parameters.get(Keywords.todisk.toLowerCase())!=null;
		boolean withfreq=parameters.get(Keywords.tablewithfreq.toLowerCase())!=null;

		String accuracy=(String)parameters.get(Keywords.accuracy.toLowerCase());
		double ac=0.000001;
		if (accuracy!=null)
		{
			ac=string2double(accuracy);
			if (ac==0)
				return new Result("%1291%<br>\n", false, null);
		}
		String siter=(String)parameters.get(Keywords.iterations.toLowerCase());
		int niter=20;
		if (siter!=null)
		{
			niter=string2int(siter);
			if (niter<=0)
				return new Result("%1569%<br>\n", false, null);
		}

		String spopdim=(String)parameters.get(Keywords.popdim.toLowerCase());
		String weightname=(String)parameters.get(Keywords.weightname.toLowerCase());
		if (weightname==null)
			weightname="weight";
		weightname=weightname.trim();
		String[] tw=weightname.split(" ");
		if (tw.length>1)
			return new Result("%1292%<br>\n", false, null);

		String varname=(String)parameters.get(Keywords.varname.toLowerCase());
		String varval=(String)parameters.get(Keywords.varval.toLowerCase());
		if ((!withfreq) && (varval==null))
			return new Result("%1579%<br>\n", false, null);
		if ((withfreq) && (varval!=null))
			return new Result("%1584%<br>\n", false, null);

		String varfreq=(String)parameters.get(Keywords.varfreq.toLowerCase());

		String vargroup=(String)parameters.get(Keywords.vargroup.toLowerCase());
		String[] tvn=varname.split(" ");
		String[] tvf=varfreq.split(" ");
		if (tvn.length!=1)
			return new Result("%1276%<br>\n", false, null);
		if (tvf.length!=1)
			return new Result("%1278%<br>\n", false, null);

		String[] tvl=new String[0];
		if (varval!=null)
		{
			tvl=varval.split(" ");
			if (tvl.length!=1)
				return new Result("%1277%<br>\n", false, null);
		}

		String vargroupinuniverse=(String)parameters.get(Keywords.vargroupinuniverse.toLowerCase());
		if (vargroupinuniverse!=null)
		{
			if (vargroup!=null)
				return new Result("%1589%<br>\n", false, null);
			String[] tvvgu=vargroupinuniverse.split(" ");
			if (tvvgu.length!=1)
				return new Result("%1588%<br>\n", false, null);
			vargroup=vargroupinuniverse;
		}
		else
			vargroupinuniverse="";

		String vartemp=vargroupinuniverse+" "+varname;
		if (varval!=null)
			vartemp=vartemp+" "+varval;

		vartemp=vartemp+" "+varfreq;

		vartemp=vartemp.trim();

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

		Hashtable<Vector<String>, double[]> freqv=new Hashtable<Vector<String>, double[]>();
		Hashtable<String, String> vartouseinw=new Hashtable<String, String>();
		Hashtable<String, Integer> vectornames=new Hashtable<String, Integer>();

		boolean repvar=false;
		if (replace==null)
			repvar=false;
		else if (replace.equalsIgnoreCase(Keywords.replaceall))
			repvar=true;
		else if (replace.equalsIgnoreCase(Keywords.replaceformat))
			repvar=true;

		/*Let's looking for pointers of variables*/
		int validgroup=0;
		int rifobs=0;
		String vvarname="";
		String mvarname="";
		String fvarname="";
		int startpos=0;
		if (!vargroupinuniverse.equals(""))
			startpos=1;
		while (!dataw.isLast())
		{
			String[] values = dataw.getRecord();
			String[] varvalues=vpw.getanalysisvar(values);
			vvarname=(varvalues[startpos].toLowerCase()).trim();
			if (!withfreq)
			{
				mvarname=varvalues[startpos+1].trim();
				fvarname=varvalues[startpos+2].trim();
			}
			else
				fvarname=varvalues[startpos+1].trim();
			double fr=Double.NaN;
			try
			{
				fr=Double.parseDouble(fvarname);
			}
			catch (Exception e) {}
			if (!Double.isNaN(fr))
			{
				if (!repvar)
					vartouseinw.put(vvarname,"");
				validgroup++;
				if (vectornames.get(vvarname+"_"+mvarname)==null)
				{
					vectornames.put(vvarname+"_"+mvarname, new Integer(rifobs));
					rifobs++;
				}
			}
		}
		dataw.close();
		if (validgroup==0)
			return new Result("%1279%<br>\n", false, null);
		String realvgval="";
		double realnumvgval=Double.NaN;
		/*Now the universes are loaded*/
		if (!dataw.open(vartoread, replacerulew, false))
			return new Result(dataw.getmessage(), false, null);
		while (!dataw.isLast())
		{
			String[] values = dataw.getRecord();
			String[] varvalues=vpw.getanalysisvar(values);
			vvarname=(varvalues[startpos].toLowerCase()).trim();
			if (!withfreq)
			{
				mvarname=varvalues[startpos+1].trim();
				fvarname=varvalues[startpos+2].trim();
			}
			else
				fvarname=varvalues[startpos+1].trim();
			double fr=Double.NaN;
			try
			{
				fr=Double.parseDouble(fvarname);
			}
			catch (Exception e) {}
			if (!Double.isNaN(fr))
			{
				Vector<String> tempgvu=new Vector<String>();
				if (startpos==0)
					tempgvu.add(null);
				else
				{
					realvgval=varvalues[0].trim();
					if (!novgconvert)
					{
						try
						{
							realnumvgval=Double.parseDouble(realvgval);
							if (!Double.isNaN(realnumvgval))
								realvgval=String.valueOf(realnumvgval);
						}
						catch (Exception e) {}
					}
					tempgvu.add(realvgval);
				}
				if (freqv.get(tempgvu)==null)
				{
					double[] tempfrequ=new double[vectornames.size()];
					for (int i=0; i<vectornames.size(); i++)
					{
						tempfrequ[i]=0;
					}
					freqv.put(tempgvu, tempfrequ);
				}
				int posvnw=(vectornames.get(vvarname+"_"+mvarname)).intValue();
				double[] realfrequ=freqv.get(tempgvu);
				double[] temprealfrequ=new double[vectornames.size()];
				for (int i=0; i<vectornames.size(); i++)
				{
					temprealfrequ[i]=realfrequ[i];
				}
				temprealfrequ[posvnw]=fr;
				freqv.put(tempgvu, temprealfrequ);
			}
		}
		dataw.close();
		if (repvar)
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
				vvarname=(varvalues[startpos].toLowerCase()).trim();
				if (!withfreq)
				{
					fvarname=varvalues[startpos+2].trim();
				}
				else
					fvarname=varvalues[startpos+1].trim();
				double fr=Double.NaN;
				try
				{
					fr=Double.parseDouble(fvarname);
				}
				catch (Exception e) {}
				if (!Double.isNaN(fr))
				{
					vartouseinw.put(vvarname,"");
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
		if ((iniw==null) && (spopdim==null))
			return new Result("%1590%<br>\n", false, null);

		double popdim=0;
		if (spopdim!=null)
		{
			popdim=string2double(spopdim);
			if (popdim==0)
				return new Result("%1564%<br>\n", false, null);
		}
		String incprob=(String)parameters.get(Keywords.varincprob.toLowerCase());

		String[] vntouse=newvarname.split(" ");

		int piw=-1;
		int pip=-1;
		int[] vars=new int[vntouse.length];

		for (int i=0; i<vntouse.length; i++)
		{
			for (int j=0; j<dict.gettotalvar(); j++)
			{
				if (!repvar)
				{
					if (vntouse[i].equalsIgnoreCase(dict.getvarname(j)))
						vars[i]=j;
				}
				else
				{
					if (vntouse[i].equalsIgnoreCase(dict.getvarlabel(j)))
						vars[i]=j;
				}
			}
		}
		if (iniw!=null)
		{
			for (int j=0; j<dict.gettotalvar(); j++)
			{
				if (iniw.equalsIgnoreCase(dict.getvarname(j)))
					piw=j;
			}
		}
		if (incprob!=null)
		{
			for (int j=0; j<dict.gettotalvar(); j++)
			{
				if (incprob.equalsIgnoreCase(dict.getvarname(j)))
					pip=j;
			}
		}

		VariableUtilities varu=new VariableUtilities(dict, vargroup, newvarname, iniw, incprob, null);
		if (varu.geterror())
			return new Result(varu.getmessage(), false, null);

		String[] totalvar=varu.getallvar();

		int[] replacerule=varu.getreplaceruleforall(replace);

		DataReader data = new DataReader(dict);

		int[] allvarstype=varu.getnormalruleforall();

		ValuesParser vp=new ValuesParser(allvarstype, null, null, null, null, null);

		Hashtable<Vector<String>, double[]> lambda=new Hashtable<Vector<String>, double[]>();
		Hashtable<Vector<String>, double[]> oldlambda=new Hashtable<Vector<String>, double[]>();
		Hashtable<Vector<String>, double[]> ef=new Hashtable<Vector<String>, double[]>();
		Hashtable<Vector<String>, Double> efp=new Hashtable<Vector<String>, Double>();
		Hashtable<Vector<String>, Integer> pointerr=new Hashtable<Vector<String>, Integer>();
		Hashtable<Vector<String>, double[]> dxexpqxlm1=new Hashtable<Vector<String>, double[]>();
		double[] difft=new double[rifobs];

		int actualrecord=0;
		String[] values=null;
		Vector<String> vargroupvalues=new Vector<String>();
		double d=0;
		boolean dataerror=false;
		double tempd=0;
		boolean pipok=true;
		boolean testvar=true;
		double q=1;
		double tempq=0;
		double[] actualv=new double[rifobs];
		double[] nactualv=new double[rifobs];
		String tempdir=(String)parameters.get(Keywords.WorkDir);
		double vala=Double.NaN;
		double valb=Double.NaN;
		double tempdiff=0;
		double[] ttw=new double[1];
		ttw[0]=0;
		int posvn=0;

		VarGroupModalities vgm=new VarGroupModalities();
		GroupedMatrix2Dfile neww=new GroupedMatrix2Dfile(tempdir, 1);
		if (novgconvert)
			vgm.noconversion();

		boolean converged=false;

		if (!data.open(totalvar, replacerule, false))
			return new Result(data.getmessage(), false, null);
		String where=(String)parameters.get(Keywords.where.toLowerCase());
		if (where!=null)
		{
			if (!data.setcondition(where)) return new Result(data.getmessage(), false, null);
		}
		while (!data.isLast())
		{
			pipok=true;
			values = data.getRecord();
			if (values!=null)
			{
				if (novgconvert)
					vargroupvalues=vp.getorigvargroup(values);
				else
					vargroupvalues=vp.getvargroup(values);
				if (vp.vargroupisnotmissing(vargroupvalues))
				{
					neww.write(vargroupvalues, ttw);
					if (lambda.get(vargroupvalues)==null)
					{
						double[] tempdxexpqxlm1=new double[rifobs];
						double[] templambda=new double[rifobs];
						double[] tempdefn=new double[rifobs];
						for (int i=0; i<rifobs; i++)
						{
							templambda[i]=0;
							tempdxexpqxlm1[i]=0;
							actualv[i]=0;
							tempdefn[i]=0;
							actualv[i]=0;
						}
						lambda.put(vargroupvalues, templambda);
						ef.put(vargroupvalues, tempdefn);
						efp.put(vargroupvalues, new Double(0));
						dxexpqxlm1.put(vargroupvalues, tempdxexpqxlm1);
					}
					d=1;
					if(piw>-1)
					{
						if (!values[piw].equals(""))
						{
							try
							{
								d=Double.parseDouble(values[piw]);
							}
							catch (Exception ee)
							{
								dataerror=true;
							}
						}
						else
							pipok=false;
					}
					q=1;
					if(pip>-1)
					{
						if (!values[pip].equals(""))
						{
							try
							{
								q=Double.parseDouble(values[pip]);
							}
							catch (Exception ee)
							{
								dataerror=true;
							}
						}
						else
							pipok=false;
					}
					if (pipok)
					{
						vgm.updateModalities(vargroupvalues);
						tempd=(efp.get(vargroupvalues)).doubleValue();
						efp.put(vargroupvalues, new Double(tempd+d));
						double[] temptef=new double[rifobs];
						double[] tef=ef.get(vargroupvalues);
						for (int i=0; i<rifobs; i++)
						{
							actualv[i]=0;
							temptef[i]=tef[i];
						}
						for (int i=0; i<vars.length; i++)
						{
							if (!values[vars[i]].equals(""))
							{
								if (!withfreq)
								{
									if (vectornames.get(vntouse[i]+"_"+values[vars[i]])!=null)
									{
										posvn=(vectornames.get(vntouse[i]+"_"+values[vars[i]])).intValue();
										actualv[posvn]=1;
									}
								}
								else
								{
									posvn=(vectornames.get(vntouse[i]+"_")).intValue();
									actualv[posvn]=string2double(values[vars[i]]);
								}
								temptef[posvn]=temptef[posvn]+actualv[posvn]*d;
							}
						}
						ef.put(vargroupvalues, temptef);
					}
				}
			}
		}
		data.close();
		if ((validgroup==0) && (where!=null))
		{
			neww.closeAll();
			return new Result("%2804%<br>\n", false, null);
		}
		if ((validgroup==0) && (where==null))
		{
			neww.closeAll();
			return new Result("%666%<br>\n", false, null);
		}
		vgm.calculate();

		GroupedMatrix2Dfile fxxt=null;

		Vector<StepResult> result = new Vector<StepResult>();

		Hashtable<Vector<String>, Double> GL=new Hashtable<Vector<String>, Double>();
		Hashtable<Vector<String>, Double> GU=new Hashtable<Vector<String>, Double>();
		Hashtable<Vector<String>, Double> GA=new Hashtable<Vector<String>, Double>();

		for (Enumeration<Vector<String>> en=efp.keys(); en.hasMoreElements();)
		{
			double Lmax=Double.MAX_VALUE;
			double Umin=-1.7976931348623157E308;
			Vector<String> nv=en.nextElement();
			double[] tempreff=ef.get(nv);
			double cw=(efp.get(nv)).doubleValue();
			cw=popdim/cw;
			double[] universe=new double[0];
			if (vargroupinuniverse.equals(""))
			{
				Vector<String> tempgfreq=new Vector<String>();
				tempgfreq.add(null);
				universe=freqv.get(tempgfreq);
			}
			else
				universe=freqv.get(nv);
			for (int i=0; i<rifobs; i++)
			{
				if ((universe[i]!=0) && (tempreff[i]!=0))
				{
					double currentr=universe[i]/tempreff[i];
					if (piw<0)
						currentr=currentr/cw;
					if (Lmax>currentr)
						Lmax=currentr;
					if (Umin<currentr)
						Umin=currentr;
				}
			}
			Lmax=L*Lmax;
			Umin=U*Umin;
			GL.put(nv, new Double(Lmax));
			GU.put(nv, new Double(Umin));
			double A=(Umin-Lmax)/((Umin-1)*(1-Lmax));
			GA.put(nv, new Double(A));
			if (efp.size()==1)
			{
				result.add(new LocalMessageGetter("%1596% ("+String.valueOf(Lmax)+")<br>\n"));
				result.add(new LocalMessageGetter("%1597% ("+String.valueOf(Umin)+")<br>\n"));
			}
			else
			{
				String tgroup="";
				for (int i=0; i<nv.size(); i++)
				{
					tgroup=tgroup+" "+nv.get(i);
				}
				tgroup=tgroup.trim();
				result.add(new LocalMessageGetter("%1596% "+String.valueOf(Lmax)+" ("+Keywords.vargroup+":"+tgroup+")<br>\n"));
				result.add(new LocalMessageGetter("%1597% "+String.valueOf(Umin)+" ("+Keywords.vargroup+":"+tgroup+")<br>\n"));
			}
		}

		int aiter=0;
		double A=Double.NaN;
		double[] zerovalues=new double[rifobs];
		for (int i=0; i<rifobs; i++)
		{
			zerovalues[i]=0;
		}

		for (int iter=0; iter<niter; iter++)
		{
			aiter=iter;
			if (iter>0)
			{
				double maxdiff=0;
				for (Enumeration<Vector<String>> enlambda=lambda.keys(); enlambda.hasMoreElements();)
				{
					Vector<String> nv=enlambda.nextElement();
					double[] templambda=lambda.get(nv);
					double[] tactualv=oldlambda.get(nv);
					double[] tempdxexpqxlm1=new double[rifobs];
					for (int i=0; i<rifobs; i++)
					{
						tempdxexpqxlm1[i]=0;
						tempd=(templambda[i]-tactualv[i])/tactualv[i];
						tempd=Math.abs(tempd);
						if (tempd>maxdiff)
							maxdiff=tempd;
					}
					dxexpqxlm1.put(nv, tempdxexpqxlm1);
				}
				if (maxdiff<ac)
				{
					result.add(new LocalMessageGetter("%880% ("+iter+")<br>\n"));
					iter=niter;
					converged=true;
					break;
				}
				oldlambda.clear();
				pointerr.clear();
			}
			if (!data.open(totalvar, replacerule, false))
				return new Result(data.getmessage(), false, null);
			fxxt=new GroupedMatrix2Dfile(tempdir, rifobs);
			for (Enumeration<Vector<String>> en=efp.keys(); en.hasMoreElements();)
			{
				Vector<String> nv=en.nextElement();
				fxxt.assignbasefile(nv);
				for (int i=0; i<rifobs; i++)
				{
					fxxt.write(nv, zerovalues);
				}
				fxxt.deassignbasefile();
			}
			actualrecord=0;
			boolean algerr=false;
			while (!data.isLast())
			{
				pipok=true;
				values = data.getRecord();
				if (novgconvert)
					vargroupvalues=vp.getorigvargroup(values);
				else
					vargroupvalues=vp.getvargroup(values);
				if (vp.vargroupisnotmissing(vargroupvalues))
				{
					L=(GL.get(vargroupvalues)).doubleValue();
					U=(GU.get(vargroupvalues)).doubleValue();
					A=(GA.get(vargroupvalues)).doubleValue();
					d=1;
					if(piw>-1)
					{
						if (!values[piw].equals(""))
						{
							try
							{
								d=Double.parseDouble(values[piw]);
							}
							catch (Exception ee)
							{
								dataerror=true;
							}
						}
						else
							pipok=false;
					}
					q=1;
					if(pip>-1)
					{
						if (!values[pip].equals(""))
						{
							try
							{
								q=Double.parseDouble(values[pip]);
							}
							catch (Exception ee)
							{
								dataerror=true;
							}
						}
						else
							pipok=false;
					}
					if (pipok)
					{
						for (int i=0; i<rifobs; i++)
						{
							actualv[i]=0;
						}
						for (int i=0; i<vars.length; i++)
						{
							if (!values[vars[i]].equals(""))
							{
								if (!withfreq)
								{
									if (vectornames.get(vntouse[i]+"_"+values[vars[i]])!=null)
									{
										posvn=(vectornames.get(vntouse[i]+"_"+values[vars[i]])).intValue();
										actualv[posvn]=1;
									}
								}
								else
								{
									posvn=(vectornames.get(vntouse[i]+"_")).intValue();
									actualv[posvn]=string2double(values[vars[i]]);
								}
							}
						}
						double[] templambda=lambda.get(vargroupvalues);
						double[] tactualv=dxexpqxlm1.get(vargroupvalues);
						fxxt.assignbasefile(vargroupvalues);
						for (int i=0; i<rifobs; i++)
						{
							tempq=0;
							if (actualv[i]!=0)
							{
								for (int j=0; j<rifobs; j++)
								{
									if ((actualv[j]!=0) && templambda[j]!=0)
										tempq=tempq+actualv[j]*templambda[j];
								}
								tempq=Math.exp(A*q*tempq);
								for (int j=0; j<rifobs; j++)
								{
									if (actualv[j]!=0)
									{
										tempd=fxxt.read(vargroupvalues, i, j);
										tempdiff=(U-L)*(U-L)*q*tempq;
										tempdiff=tempdiff/(Math.pow(((U-1)+(1-L)*tempq),2));
										valb=tempd+actualv[i]*actualv[j]*d*tempdiff;
										if (Double.isNaN(valb))
											algerr=true;
										else if (Double.isInfinite(valb))
											algerr=true;
										fxxt.write(vargroupvalues, valb, i, j);
									}
								}
								vala=(L*(U-1)+U*(1-L)*tempq)/((U-1)+(1-L)*tempq);
								tactualv[i]=tactualv[i]+d*actualv[i]*(vala-1);
							}
						}
						fxxt.deassignbasefile();
						dxexpqxlm1.put(vargroupvalues, tactualv);
					}
				}
			}
			data.close();
			if (validgroup==0)
			{
				fxxt.closeAll();
				neww.closeAll();
				return new Result("%1279%<br>\n", false, null);
			}
			if (dataerror)
			{
				fxxt.closeAll();
				neww.closeAll();
				return new Result("%1565%<br>\n", false, null);
			}
			if (algerr)
			{
				fxxt.closeAll();
				neww.closeAll();
				return new Result("%1576%<br>\n", false, result);
			}

			ADaMSoft.algorithms.Algebra2DFile.SingularValueDecomposition svdf=null;
			GroupedMatrix2Dfile matU=null;
			GroupedMatrix2Dfile matV=null;
			GroupedMatrix2Dfile matS=null;
			if (todisk)
			{
				svdf=new ADaMSoft.algorithms.Algebra2DFile.SingularValueDecomposition(tempdir, vgm);
				svdf.evaluate(fxxt);
				if (svdf.getState())
				{
					fxxt.closeAll();
					svdf.closeAll();
					neww.closeAll();
					return new Result(svdf.getMess(), false, null);
				}
				matU=svdf.getV();
				matV=svdf.getU();
				matS=svdf.gets();
			}

			for (Enumeration<Vector<String>> enlambda=lambda.keys(); enlambda.hasMoreElements();)
			{
				Vector<String> nv=enlambda.nextElement();
				double[] templambda=lambda.get(nv);
				double[] tactualv=new double[actualv.length];
				for (int i=0; i<actualv.length; i++)
				{
					tactualv[i]=templambda[i];
				}
				oldlambda.put(nv, tactualv);
			}
			for (Enumeration<Vector<String>> en=efp.keys(); en.hasMoreElements();)
			{
				Vector<String> nv=en.nextElement();
				double[] tef=ef.get(nv);
				double[] tempa=dxexpqxlm1.get(nv);
				double[] vecdiff=new double[rifobs];
				for (int i=0; i<rifobs; i++)
				{
					vecdiff[i]=0;
					difft[i]=0;
				}
				double cw=(efp.get(nv)).doubleValue();
				cw=popdim/cw;
				double[] universe=new double[0];
				if (vargroupinuniverse.equals(""))
				{
					Vector<String> tempgfreq=new Vector<String>();
					tempgfreq.add(null);
					universe=freqv.get(tempgfreq);
				}
				else
					universe=freqv.get(nv);
				for (int i=0; i<rifobs; i++)
				{
					if (piw<0)
					{
						if (universe[i]!=0)
							difft[i]=universe[i]-tef[i]*cw-tempa[i]*cw;
						}
					else
					{
						if (universe[i]!=0)
							difft[i]=universe[i]-tef[i]-tempa[i];
					}
				}
				double[] templambda=lambda.get(nv);
				try
				{
					if (!todisk)
					{
						fxxt.assignbasefile(nv);
						DoubleMatrix2D matrix=DoubleFactory2D.dense.make(rifobs, rifobs);
						for (int a=0; a<rifobs; a++)
						{
							for (int b=0; b<rifobs; b++)
							{
								matrix.set(a,b, fxxt.read(nv, a, b));
							}
						}
						SingularValueDecomposition svd=new SingularValueDecomposition(matrix);
						DoubleMatrix2D matu=svd.getV();
						DoubleMatrix2D matv=svd.getU();
						DoubleMatrix2D mats=svd.getS();
						for (int i=0; i<rifobs; i++)
						{
							if (mats.get(i, i)>0.000001)
							{
							mats.set(i,i, 1/mats.get(i, i));
							}
							else
								mats.set(i,i, 0);
						}
						for (int a=0; a<rifobs; a++)
						{
							for (int b=0; b<rifobs; b++)
							{
								tempdiff=0;
								for (int i=0; i<rifobs; i++)
								{
									vala=matv.get(a, i);
									valb=mats.get(b, i);
									tempdiff+=vala*valb;
								}
								matrix.set(a,b,tempdiff);
							}
						}
						matv=null;
						mats=null;
						for (int a=0; a<rifobs; a++)
						{
							vecdiff[a]=0;
							for (int b=0; b<rifobs; b++)
							{
								tempdiff=0;
								for (int i=0; i<rifobs; i++)
								{
									vala=matrix.get(a,i);
									valb=matu.get(b, i);
									tempdiff+=vala*valb;
								}
								if (piw<0)
									vecdiff[a]=vecdiff[a]+(1/cw)*difft[b]*tempdiff;
								else
									vecdiff[a]=vecdiff[a]+difft[b]*tempdiff;
							}
							vecdiff[a]=vecdiff[a]+templambda[a];
						}
						matrix=null;
						matu=null;
						lambda.put(nv, vecdiff);
						fxxt.deassignbasefile();
					}
					else
					{
						matU.assignbasefile(nv);
						matV.assignbasefile(nv);
						matS.assignbasefile(nv);
						for (int a=0; a<rifobs; a++)
						{
							for (int b=0; b<rifobs; b++)
							{
								actualv[b]=0;
								for (int i=0; i<rifobs; i++)
								{
									if (b==i)
									{
										vala=matV.read(nv, a, i);
										valb=matS.read(nv, b, 0);
										if (valb>0.000001)
											valb=1/valb;
										actualv[b]=actualv[b]+vala*valb;
									}
								}
							}
							vecdiff[a]=0;
							for (int b=0; b<rifobs; b++)
							{
								tempdiff=0;
								for (int i=0; i<rifobs; i++)
								{
									vala=actualv[i];
									valb=matU.read(nv, b, i);
									tempdiff+=vala*valb;
								}
								if (piw<0)
									vecdiff[a]=vecdiff[a]+(1/cw)*difft[b]*tempdiff;
								else
									vecdiff[a]=vecdiff[a]+difft[b]*tempdiff;
							}
							vecdiff[a]=vecdiff[a]+templambda[a];
						}
						lambda.put(nv, vecdiff);
						matU.deassignbasefile();
						matV.deassignbasefile();
						matS.deassignbasefile();
					}
				}
				catch (Exception e)
				{
					String error=e.toString();
					if (error.startsWith("java.lang.IllegalArgumentException"))
						error="Error "+error.substring("java.lang.IllegalArgumentException".length());
					System.gc();
					error=error+"\n";
					return new Result("%1566%<br>\n"+error+"<br>\n", false, null);
				}
			}
			fxxt.closeAll();
			if (todisk)
			{
				svdf.closeAll();
			}

			if (!data.open(totalvar, replacerule, false))
				return new Result(data.getmessage(), false, null);

			double weightvalue=Double.NaN;
			actualrecord=0;

			boolean errorinnotexistentval=false;

			String notexistentval="";

			while (!data.isLast())
			{
				weightvalue=Double.NaN;
				values = data.getRecord();
				if (novgconvert)
					vargroupvalues=vp.getorigvargroup(values);
				else
					vargroupvalues=vp.getvargroup(values);
				if (vp.vargroupisnotmissing(vargroupvalues))
				{
						L=(GL.get(vargroupvalues)).doubleValue();
						U=(GU.get(vargroupvalues)).doubleValue();
						A=(GA.get(vargroupvalues)).doubleValue();
						d=1;
						q=1;
						if(piw>-1)
						{
							if (!values[piw].equals(""))
							{
								try
								{
									d=Double.parseDouble(values[piw]);
								}
								catch (Exception ee) {}
							}
							else
								pipok=false;
						}
						else
						{
							tempd=(efp.get(vargroupvalues)).doubleValue();
							d=popdim/tempd;
						}
						if(pip>-1)
						{
							if (!values[pip].equals(""))
							{
								try
								{
									q=Double.parseDouble(values[pip]);
								}
								catch (Exception ee) {}
							}
							else
								pipok=false;
						}
						if (pipok)
						{
							weightvalue=0;
							double[] templambda=lambda.get(vargroupvalues);
							for (int i=0; i<vars.length; i++)
							{
								if (!values[vars[i]].equals(""))
								{
									if (!withfreq)
									{
										if (vectornames.get(vntouse[i]+"_"+values[vars[i]])==null)
										{
											errorinnotexistentval=true;
											notexistentval=vntouse[i]+"="+values[vars[i]];
										}
										else
										{
											posvn=(vectornames.get(vntouse[i]+"_"+values[vars[i]])).intValue();
											actualv[posvn]=1;
										}
									}
									else
									{
										posvn=(vectornames.get(vntouse[i]+"_")).intValue();
										actualv[posvn]=string2double(values[vars[i]]);
									}
									if (!errorinnotexistentval)
										weightvalue=weightvalue+actualv[posvn]*templambda[posvn];
								}
							}
							if (!errorinnotexistentval)
							{
								weightvalue=(L*(U-1)+U*(1-L)*Math.exp(A*q*weightvalue))/((U-1)+(1-L)*Math.exp(A*q*weightvalue));
								weightvalue=d*weightvalue;
							}
						}
						if (!errorinnotexistentval)
						{
							if(pointerr.get(vargroupvalues)==null)
							{
								pointerr.put(vargroupvalues, new Integer(0));
							}
							else
							{
								int trecord=(pointerr.get(vargroupvalues)).intValue()+1;
								pointerr.put(vargroupvalues, new Integer(trecord));
							}
							int precord=(pointerr.get(vargroupvalues)).intValue();
							neww.write(vargroupvalues, weightvalue, precord, 0);
						}
				}
				actualrecord++;
			}
			data.close();
			if (errorinnotexistentval)
			{
				neww.closeAll();
				return new Result("%1953% ("+notexistentval+")<br>\n", false, null);
			}
		}

		if (!converged)
			result.add(new LocalMessageGetter("%1583% ("+aiter+")<br>\n"));

		String keyword="TruncLogpf "+dict.getkeyword();
		String description="TruncLogpf "+dict.getdescription();
		String author=(String)parameters.get(Keywords.client_host.toLowerCase());

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

		if (!data.open(totalvar, replacerule, false))
			return new Result(data.getmessage(), false, null);
		String[] newv=null;
		if (toistvw>=0)
			newv=new String[values.length];
		else
			newv=new String[values.length+1];
		double weightvalue=Double.NaN;
		pointerr.clear();
		while (!data.isLast())
		{
			weightvalue=Double.NaN;
			values = data.getRecord();
			if (novgconvert)
				vargroupvalues=vp.getorigvargroup(values);
			else
				vargroupvalues=vp.getvargroup(values);
			if (vp.vargroupisnotmissing(vargroupvalues))
			{
				if(pointerr.get(vargroupvalues)==null)
				{
					pointerr.put(vargroupvalues, new Integer(0));
				}
				else
				{
					int trecord=(pointerr.get(vargroupvalues)).intValue()+1;
					pointerr.put(vargroupvalues, new Integer(trecord));
				}
				int precord=(pointerr.get(vargroupvalues)).intValue();
				weightvalue=neww.read(vargroupvalues, precord, 0);
			}
			for (int i=0; i<values.length; i++)
			{
				newv[i]=values[i];
			}
			if (toistvw>=0)
				newv[toistvw]=double2String(weightvalue);
			else
				newv[values.length]=double2String(weightvalue);
			dw.write(newv);
		}
		data.close();
		neww.closeAll();
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
		parameters.add(new GetRequiredParameters(Keywords.varval, "vars=all", false, 1285, dep, "", 2));
		parameters.add(new GetRequiredParameters("", "note", false, 1578, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.varfreq, "vars=all", true, 1286, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.vargroupinuniverse, "vars=all", false, 1586, dep, "", 2));
		parameters.add(new GetRequiredParameters("", "note", false, 1587, dep, "", 2));
		String[] ndep = new String[1];
		ndep[0]=Keywords.dict;
		parameters.add(new GetRequiredParameters(Keywords.vargroup, "vars=all", false, 655, ndep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.variniweight, "vars=all", false, 1287, ndep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.varincprob, "vars=all", false, 1562, ndep, "", 2));
		dep = new String[0];
		ndep = new String[0];
		parameters.add(new GetRequiredParameters(Keywords.popdim, "text", false, 1563, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.iterations, "text", false, 1288, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.accuracy, "text", false, 1585, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.weightname, "text", false, 1290, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.uweight, "text", false, 1573, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.lweight, "text", false, 1572, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.tablewithfreq, "checkbox", false, 1577, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.where,"text", false, 2805,dep,"",2));
		parameters.add(new GetRequiredParameters("", "note", false, 2806, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.todisk, "checkbox", false, 1088, dep, "", 2));
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
		retprocinfo[1]="1571";
		return retprocinfo;
	}
}
