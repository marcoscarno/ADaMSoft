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
import ADaMSoft.keywords.Keywords;
import ADaMSoft.utilities.StepUtilities;
import ADaMSoft.utilities.GetRequiredParameters;
import ADaMSoft.utilities.ValuesParser;
import ADaMSoft.utilities.VariableUtilities;
import ADaMSoft.utilities.DataSetUtilities;
import ADaMSoft.utilities.ObjectTransformer;
import ADaMSoft.utilities.CheckVarNames;

import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;

import cern.jet.stat.Probability;

/**
* This is the procedure that evaluates the predicted value for a variable using a linear regression model
* @author marco.scarno@gmail.com
* @date 14/02/2017
*/
public class ProcRegeval extends ObjectTransformer implements RunStep
{
	/**
	* Starts the execution of Proc Ldaeval
	*/
	@SuppressWarnings("unused")
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		boolean novgconvert=false;
		boolean iscovb=false;
		boolean isinfo=false;
		String [] requiredparameters=new String[] {Keywords.OUT.toLowerCase(), Keywords.dict, Keywords.dict+"e"};
		String [] optionalparameters=new String[] {Keywords.dict+"i", Keywords.dict+"c", Keywords.varoutname, Keywords.where, Keywords.alpha, Keywords.replace, Keywords.novgconvert};
		Keywords.percentage_total=0;
		Keywords.percentage_done=0;
		StepUtilities steputilities=new StepUtilities();
		if (!steputilities.checkParameters(requiredparameters, optionalparameters, parameters))
		{
			return new Result(steputilities.getMessage(), false, null);
		}
		iscovb =(parameters.get(Keywords.dict+"c")!=null);
		isinfo =(parameters.get(Keywords.dict+"i")!=null);
		if (isinfo!=iscovb)
			return new Result("%909%<br>\n", false, null);
		novgconvert =(parameters.get(Keywords.novgconvert)!=null);

		String varoutname=(String)parameters.get(Keywords.varoutname);
		if (varoutname==null)
			varoutname="pred";
		varoutname=varoutname.replaceAll("\\s", "");
		if (!CheckVarNames.getResultCheck(varoutname, (String)parameters.get(Keywords.WorkDir)).equals(""))
			return new Result("%2096%"+" "+varoutname+"<br>\n", false, null);

		String replace =(String)parameters.get(Keywords.replace);
		String proba=(String)parameters.get(Keywords.alpha.toLowerCase());

		double alpha=0.95;
		if (proba!=null)
		{
			try
			{
				alpha=Double.parseDouble(proba);
			}
			catch (NumberFormatException en)
			{
				return new Result("%662%<br>\n", false, null);
			}
		}
		if ((alpha>1) || (alpha<0))
			return new Result("%662%<br>\n", false, null);

		DataWriter dw=new DataWriter(parameters, Keywords.OUT.toLowerCase());
		if (!dw.getmessage().equals(""))
			return new Result(dw.getmessage(), false, null);

		DictionaryReader dictc = null;
		if (iscovb)
			dictc=(DictionaryReader)parameters.get(Keywords.dict+"c");

		DictionaryReader dicti = null;
		if (isinfo)
			dicti=(DictionaryReader)parameters.get(Keywords.dict+"i");

		DictionaryReader dicte = (DictionaryReader)parameters.get(Keywords.dict+"e");

		Hashtable<String, String> tinfo=dicte.getdatatableinfo();
		String tempvary="";
		for (Enumeration<String> e = tinfo.keys() ; e.hasMoreElements() ;)
		{
			String par = (String) e.nextElement();
			String val = tinfo.get(par);
			if (par.equalsIgnoreCase(Keywords.vary))
				tempvary=val;
		}

		int numvar=dicte.gettotalvar();
		String var="";
		boolean iscorrect=true;
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

		var=var+" varx value";
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

		String[] vartoread=new String[groupvar.length+2];
		int[] replacerule=new int[groupvar.length+2];
		String[] vartoreadinfo=new String[groupvar.length+2];
		for (int i=0; i<groupvar.length; i++)
		{
			vartoread[i]="g_"+groupvar[i];
			vartoreadinfo[i]="g_"+groupvar[i];
			replacerule[i]=1;
		}
		vartoread[groupvar.length]="varx";
		vartoreadinfo[groupvar.length]="info";
		vartoread[groupvar.length+1]="value";
		vartoreadinfo[groupvar.length+1]="value";

		for (int i=groupvar.length; i<vartoread.length; i++)
		{
			replacerule[i]=0;
		}

		DataReader datae = new DataReader(dicte);
		if (!datae.open(vartoread, replacerule, false))
			return new Result(datae.getmessage(), false, null);

		Hashtable<Vector<String>, Hashtable<Integer, Double>> tempparameters=new Hashtable<Vector<String>, Hashtable<Integer, Double>>();
		Hashtable<String, Integer> tempvarname=new Hashtable<String, Integer>();

		boolean errorinpara=false;

		boolean noint=true;
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
				Hashtable<Integer, Double> tp=tempparameters.get(groupval);
				if (tp==null)
					tp=new Hashtable<Integer, Double>();
				double temppara=Double.valueOf(values[groupvar.length+1]);
				int rifvarname=-1;
				String tempname=values[groupvar.length].toLowerCase();
				if ((tempvarname.get(tempname)==null) && (!tempname.equals("0")))
					tempvarname.put(tempname, new Integer(tempvarname.size()));
				if (tempname.equalsIgnoreCase("0"))
					noint=false;
				else
				{
					for (int i=0; i<alldsvars.length; i++)
					{
						if (tempname.equalsIgnoreCase(alldsvars[i]))
						{
							rifvarname=i;
							break;
						}
					}
				}
				tp.put(new Integer(rifvarname), new Double(temppara));
				tempparameters.put(groupval, tp);
			}
			catch (Exception e)
			{
				errorinpara=true;
			}
		}
		datae.close();
		if (errorinpara)
			return new Result("%899%<br>\n", false, null);


		String varx="";
		for (Enumeration<String> ep = tempvarname.keys() ; ep.hasMoreElements() ;)
		{
			String vx=ep.nextElement();
			varx=varx+" "+vx;
		}
		varx=varx.trim();

		Hashtable<Vector<String>, Integer> numdf=new Hashtable<Vector<String>, Integer>();

		if (isinfo)
		{
			boolean errorininfo=false;
			DataReader datainfo = new DataReader(dicti);
			if (!datainfo.open(vartoreadinfo, replacerule, false))
				return new Result(datainfo.getmessage(), false, null);
			while (!datainfo.isLast())
			{
				String[] values = datainfo.getRecord();
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
					int pointer=Integer.parseInt(values[groupvar.length]);
					if (pointer==2)
					{
						double dtempdf=Double.valueOf(values[groupvar.length+1]);
						int tempdf=((Double)dtempdf).intValue();
						numdf.put(groupval, new Integer(tempdf));
					}
				}
				catch (Exception e)
				{
					errorininfo=true;
				}
			}
			datainfo.close();
			if (errorininfo)
				return new Result("%910%<br>\n", false, null);
		}

		Hashtable<Vector<String>, Hashtable<Vector<Integer>, Double>> covb=new Hashtable<Vector<String>, Hashtable<Vector<Integer>, Double>>();
		if (iscovb)
		{
			String varnamesincovb="";
			for (int i=0; i<groupvar.length; i++)
			{
				varnamesincovb=varnamesincovb+" g_"+groupvar[i];
			}
			varnamesincovb=varnamesincovb.trim()+" varx";
			int numvarcovb=dictc.gettotalvar();
			String varcovb="";
			for (int i=0; i<numvarcovb; i++)
			{
				String tempname=dictc.getvarname(i);
				if ((tempname.toLowerCase()).startsWith("v_"))
				{
					try
					{
						String tc=tempname.substring(2);
						varcovb=varcovb+" "+tc;
						varnamesincovb=varnamesincovb+" "+tempname;
					}
					catch (Exception ec) {}
				}
			}
			varcovb=varcovb.trim();
			varnamesincovb=varnamesincovb.trim();
			String[] varscovb=varnamesincovb.split(" ");
			String[] namvc=varcovb.split(" ");
			int[] repcovb=new int[varscovb.length];
			for (int i=0; i<groupvar.length; i++)
			{
				repcovb[i]=1;
			}
			for (int i=groupvar.length; i<varscovb.length; i++)
			{
				repcovb[i]=0;
			}

			DataReader datacovb = new DataReader(dictc);
			if (!datacovb.open(varscovb, repcovb, false))
				return new Result(datacovb.getmessage(), false, null);
			while (!datacovb.isLast())
			{
				String[] values = datacovb.getRecord();
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
					Hashtable<Vector<Integer>, Double> tcovb=covb.get(groupval);
					if (tcovb==null)
						tcovb=new Hashtable<Vector<Integer>, Double>();
					String tempname=values[groupvar.length].toLowerCase();
					double[] tempcb=new double[values.length-1-groupvar.length];
					for (int i=0; i<(values.length-1-groupvar.length); i++)
					{
						tempcb[i]=Double.valueOf(values[i+1+groupvar.length]);
					}
					int rifvarname=-1;
					if (!tempname.equalsIgnoreCase("0"))
					{
						for (int i=0; i<alldsvars.length; i++)
						{
							if (tempname.equalsIgnoreCase(alldsvars[i]))
							{
								rifvarname=i;
								break;
							}
						}
					}
					for (int i=0; i<tempcb.length; i++)
					{
						Vector<Integer> rifvcname=new Vector<Integer>();
						rifvcname.add(new Integer(rifvarname));
						int rifvarnamecol=-1;
						if (!namvc[i].equals("0"))
						{
							for (int j=0; j<alldsvars.length; j++)
							{
								if (namvc[i].equalsIgnoreCase(alldsvars[j]))
								{
									rifvarnamecol=j;
									break;
								}
							}
						}
						rifvcname.add(new Integer(rifvarnamecol));
						tcovb.put(rifvcname, new Double(tempcb[i]));
					}
					covb.put(groupval, tcovb);
				}
				catch (Exception e) {}
			}
			datacovb.close();
		}

		String keyword="RegEval "+dict.getkeyword();
		String description="RegEval "+dict.getdescription();
		String author=(String) parameters.get(Keywords.client_host.toLowerCase());

		DataSetUtilities dsu=new DataSetUtilities();
		dsu.setreplace(replace);
		Hashtable<String, String> temph=new Hashtable<String, String>();

		dsu.defineolddict(dict);

		numvar=dict.gettotalvar();
		boolean isdepvar=false;
		int positiondepvar=-1;
		for (int i=0; i<numvar; i++)
		{
			String tempname=dict.getvarname(i);
			if (tempname.equalsIgnoreCase(tempvary))
			{
				positiondepvar=i;
				isdepvar=true;
				break;
			}
		}
		if (!iscorrect)
			return new Result("%898%<br>\n", false, null);

		dsu.addnewvartoolddict(varoutname, "%1126%", Keywords.NUMSuffix, temph, temph);
		if (isdepvar)
		{
			dsu.addnewvartoolddict(varoutname+"_res", "%1127%", Keywords.NUMSuffix, temph, temph);
			dsu.addnewvartoolddict(varoutname+"_sqres", "%1128%", Keywords.NUMSuffix, temph, temph);
		}
		if (iscovb)
		{
			dsu.addnewvartoolddict(varoutname+"_lowv", "%1129% "+String.valueOf(alpha)+"% %1130%", Keywords.NUMSuffix, temph, temph);
			dsu.addnewvartoolddict(varoutname+"_uppv", "%1131% "+String.valueOf(alpha)+"% %1130%", Keywords.NUMSuffix, temph, temph);
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

		if (groupname.equals(""))
			groupname=null;

		int outlength=1;

		if (isdepvar)
		{
			outlength=3;
			varx=varx+" "+tempvary;
		}
		if (iscovb)
			outlength=outlength+2;

		VariableUtilities varu=new VariableUtilities(dict, groupname, varx, null, null, null);
		if (varu.geterror())
			return new Result(varu.getmessage(), false, null);

		String[] totalvar=varu.getallvar();

		int[] replacer=varu.getreplaceruleforall(replace);

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

		int validgroup=0;
		boolean noterror=true;
		Vector<String> vargroupvalues=new Vector<String>();
		while ((!data.isLast()) && (noterror))
		{
			String[] values = data.getRecord();
			if (values!=null)
			{
				if (novgconvert)
					vargroupvalues=vp.getorigvargroup(values);
				else
					vargroupvalues=vp.getvargroup(values);
				String[] newvalues=new String[outlength];
				for (int i=0; i<newvalues.length; i++)
				{
					newvalues[i]="";
				}
				if (vp.vargroupisnotmissing(vargroupvalues))
				{
					validgroup++;
					if (tempparameters.get(vargroupvalues)!=null)
					{
						Hashtable<Integer, Double> par=tempparameters.get(vargroupvalues);
						double predictedval=0;
						double residual=0;
						double squareresidual=0;
						double lowint=Double.NaN;
						double uppint=Double.NaN;
						int outpointer=0;
						for (Enumeration<Integer> ep = par.keys() ; ep.hasMoreElements() ;)
						{
							Integer ai=ep.nextElement();
							int posref=ai.intValue();
							double coeffv=(par.get(ai)).doubleValue();
							if (posref==-1)
								predictedval=predictedval+coeffv;
							else
							{
								if (!values[posref].equals(""))
								{
									try
									{
										predictedval=predictedval+Double.parseDouble(values[posref])*coeffv;
									}
									catch (Exception ex)
									{
										predictedval=Double.NaN;
									}
								}
								else
									predictedval=Double.NaN;
							}
						}

						boolean isnull=false;
						if(Double.isNaN(predictedval))
							isnull=true;

						if (!isnull)
						{
							try
							{
								newvalues[outpointer]=double2String(predictedval);
							}
							catch (Exception e) {}
							outpointer++;
							if (isdepvar)
							{
								try
								{
									residual=Double.parseDouble(values[positiondepvar])-predictedval;
									squareresidual=residual*residual;
									newvalues[outpointer]=double2String(residual);
									outpointer++;
									newvalues[outpointer]=double2String(squareresidual);
									outpointer++;
								}
								catch(Exception e)
								{
									newvalues[outpointer]="";
									outpointer++;
									newvalues[outpointer]="";
									outpointer++;
								}
							}
							if (iscovb)
							{
								try
								{
									DoubleMatrix2D mat=null;
									DoubleMatrix2D matx=null;
									DoubleMatrix2D matxt=null;

									Hashtable<Vector<Integer>, Double> tcovb=covb.get(vargroupvalues);
									Hashtable<Integer, Integer> posvx=new Hashtable<Integer, Integer>();
									Hashtable<Integer, Integer> posvt=new Hashtable<Integer, Integer>();

									int elements=0;

									for (Enumeration<Vector<Integer>> ep = tcovb.keys() ; ep.hasMoreElements() ;)
									{
										Vector<Integer> ai=ep.nextElement();
										int posi=(ai.get(0)).intValue();
										if (posvt.get(new Integer(posi))==null)
										{
											posvx.put(new Integer(elements), new Integer(posi));
											posvt.put(new Integer(posi), new Integer(elements));
											elements++;
										}
									}

									mat=DoubleFactory2D.dense.make(elements, elements);
									matx=DoubleFactory2D.dense.make(1, elements);

									for (int i=0; i<elements; i++)
									{
										int posx=(posvx.get(new Integer(i))).intValue();
										for (int j=0; j<elements; j++)
										{
											int posy=(posvx.get(new Integer(j))).intValue();
											Vector<Integer> poselements=new Vector<Integer>();
											poselements.add(new Integer(posx));
											poselements.add(new Integer(posy));
											double valco=(tcovb.get(poselements)).doubleValue();
											mat.set(i, j, valco);
										}
										if (posx==-1)
											matx.set(0, i, 1);
										else
										{
											double realvalforcov=Double.parseDouble(values[posx]);
											matx.set(0, i, realvalforcov);
										}
									}
									Algebra algebra=new Algebra();
									matxt=algebra.transpose(matx);
									DoubleMatrix2D res=algebra.mult(matx, mat);
									DoubleMatrix2D result=algebra.mult(res, matxt);
									double interval=Math.sqrt(result.get(0,0));
									int tempdf=(numdf.get(vargroupvalues)).intValue();
									double talpha=Probability.studentTInverse(1-alpha, tempdf);
									lowint=predictedval-talpha*interval;
									uppint=predictedval+talpha*interval;
								}
								catch (Exception e) {}
								newvalues[outpointer]=double2String(lowint);
								outpointer++;
								newvalues[outpointer]=double2String(uppint);
							}
						}
					}
				}
				String[] wvalues=dsu.getnewvalues(values, newvalues);
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
		parameters.add(new GetRequiredParameters(Keywords.dict+"c=", "dict", false, 902, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.dict+"i=", "dict", false, 908, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.OUT.toLowerCase()+"=", "setting=out", true, 904, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.viewout, "checkbox", false, 1612, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.varoutname, "text", true, 2005, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.where,"text", false, 2809,dep,"",2));
		parameters.add(new GetRequiredParameters("", "note", false, 2806, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.alpha,"text", false, 905,dep,"",2));
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
		retprocinfo[0]="886";
		retprocinfo[1]="903";
		return retprocinfo;
	}
}
