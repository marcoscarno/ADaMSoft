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
import java.util.Enumeration;

import ADaMSoft.algorithms.clusters.EvaluateDistance;
import ADaMSoft.algorithms.CovariancesEvaluator;
import ADaMSoft.dataaccess.DataReader;
import ADaMSoft.dataaccess.DataWriter;
import ADaMSoft.dataaccess.DictionaryReader;
import ADaMSoft.keywords.Keywords;
import ADaMSoft.utilities.DataSetUtilities;
import ADaMSoft.utilities.GetRequiredParameters;
import ADaMSoft.utilities.VariableUtilities;
import ADaMSoft.utilities.ObjectTransformer;
import ADaMSoft.utilities.ValuesParser;
import ADaMSoft.algorithms.VarGroupModalities;
import ADaMSoft.utilities.StepUtilities;

/**
* This is the procedure implements a hierarchical clusters analysis
* @author marco.scarno@gmail.com
* @version 1.0.0, rev.: 13/02/17 by marco
*/
public class ProcClusters extends ObjectTransformer implements RunStep
{
	double min, nextCluster1, nextCluster2;
	/**
	* Starts the execution of Proc Clusters
	*/
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		Vector<StepResult> result = new Vector<StepResult>();
		boolean novgconvert=false;
		boolean noclforvg=false;
		boolean orderclbycode=false;
		String [] requiredparameters=new String[] {Keywords.OUT.toLowerCase(), Keywords.dict, Keywords.var};
		String [] optionalparameters=new String[] {Keywords.vargroup, Keywords.where, Keywords.varid, Keywords.replace, Keywords.Distance, Keywords.linktype, Keywords.novgconvert, Keywords.noclforvg, Keywords.orderclbycode};
		Keywords.percentage_total=0;
		Keywords.percentage_done=0;
		StepUtilities steputilities=new StepUtilities();
		if (!steputilities.checkParameters(requiredparameters, optionalparameters, parameters))
		{
			return new Result(steputilities.getMessage(), false, null);
		}
		DataWriter dw=new DataWriter(parameters, Keywords.OUT.toLowerCase());
		if (!dw.getmessage().equals(""))
			return new Result(dw.getmessage(), false, null);

		novgconvert =(parameters.get(Keywords.novgconvert)!=null);
		orderclbycode =(parameters.get(Keywords.orderclbycode)!=null);
		noclforvg =(parameters.get(Keywords.noclforvg)!=null);

		String replace =(String)parameters.get(Keywords.replace);
		String author=(String) parameters.get(Keywords.client_host.toLowerCase());
		DictionaryReader dict = (DictionaryReader)parameters.get(Keywords.dict);
		String tempvars=(String)parameters.get(Keywords.var.toLowerCase());
		String tempvargroup=(String)parameters.get(Keywords.vargroup.toLowerCase());
		String link=(String)parameters.get(Keywords.linktype.toLowerCase());
		String label=(String)parameters.get(Keywords.varid.toLowerCase());
		String distanceType = (String)parameters.get(Keywords.Distance.toLowerCase());
		if (label!=null)
		{
			String[] testl=(label.trim()).split(" ");
			if (testl.length!=1)
				return new Result("%2077%<br>\n", false, null);
		}

		if (distanceType==null)
			distanceType=Keywords.EuclideanDistance;
		if (link==null)
			link=Keywords.LCentroid;

		String[] dtype=new String[] {Keywords.EuclideanDistance, Keywords.SquaredEuclideanDistance, Keywords.ManhattanDistance, Keywords.ChebyshevDistance, Keywords.MahalanobisDistance};
		int vdt=steputilities.CheckOption(dtype, distanceType);
		if (vdt==0)
			return new Result("%1775% "+Keywords.Distance.toUpperCase()+"<br>\n"+steputilities.getMessage(), false, null);

		EvaluateDistance ed=new EvaluateDistance(vdt);

		dtype=new String[] {Keywords.LSingle, Keywords.LComplete, Keywords.LAverage, Keywords.LCentroid};
		int vdl=steputilities.CheckOption(dtype, link);
		if (vdl==0)
			return new Result("%1775% "+Keywords.linktype.toUpperCase()+"<br>\n"+steputilities.getMessage(), false, null);

		VariableUtilities varu=new VariableUtilities(dict, tempvargroup, null, null, tempvars, label);
		if (varu.geterror())
			return new Result(varu.getmessage(), false, null);

		String keyword="Clusters "+dict.getkeyword();
		String description="Clusters "+dict.getdescription();

		String[] reqvar=varu.getreqvar();
		int[] replacerule=varu.getreplaceruleforsel(replace);
		String[] varg=varu.getgroupvar();

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

		DataReader data = new DataReader(dict);
		if (!data.open(reqvar, replacerule, false))
			return new Result(data.getmessage(), false, null);
		String where=(String)parameters.get(Keywords.where.toLowerCase());
		if (where!=null)
		{
			if (!data.setcondition(where)) return new Result(data.getmessage(), false, null);
		}

		int[] grouprule=varu.getgroupruleforsel();
		int[] rowrule=varu.getrowruleforsel();
		int[] colrule=null;
		if (label!=null)
			colrule=varu.getcolruleforsel();

		ValuesParser vp=new ValuesParser(null, grouprule, null, rowrule, colrule, null);
		Hashtable<Vector<String>, Vector<double[]>> realvalues=new Hashtable<Vector<String>, Vector<double[]>>();
		Hashtable<Vector<String>, Vector<String>> realid=new Hashtable<Vector<String>, Vector<String>>();
		CovariancesEvaluator cove=new CovariancesEvaluator(true, true);

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

		int validgroup=0;
		String[] values=null;
		double[] varrowvalues=null;
		String[] varcolvalues=null;
		boolean isnan=false;
		Vector<String> vargroupvalues=null;
		while (!data.isLast())
		{
			values = data.getRecord();
			if (values!=null)
			{
				if (novgconvert)
					vargroupvalues=vp.getorigvargroup(values);
				else
					vargroupvalues=vp.getvargroup(values);
				varrowvalues=vp.getrowvarasdouble(values);
				if (label!=null)
					varcolvalues=vp.getcolvar(values);
				if (vp.vargroupisnotmissing(vargroupvalues))
				{
					isnan=false;
					double[] valuetoinsert=new double[varrowvalues.length];
					for (int i=0; i<varrowvalues.length; i++)
					{
						valuetoinsert[i]=varrowvalues[i];
						if (Double.isNaN(varrowvalues[i]))
							isnan=true;
					}
					if (label!=null)
					{
						if (varcolvalues[0].equals(""))
							isnan=true;
					}
					if (!isnan)
					{
						Vector<double[]> trealvalues=realvalues.get(vargroupvalues);
						if (trealvalues==null)
							trealvalues=new Vector<double[]>();
						trealvalues.add(valuetoinsert);
						Vector<String> trealid=realid.get(vargroupvalues);
						if (trealid==null)
							trealid=new Vector<String>();
						if (label!=null)
						{
							trealid.add(varcolvalues[0]);
						}
						else
							trealid.add("OBS_"+String.valueOf(trealid.size()+1));
						if (vdt==5)
							cove.setValue(vargroupvalues, valuetoinsert, valuetoinsert, 1);
						validgroup++;
						vgm.updateModalities(vargroupvalues);
						realvalues.put(vargroupvalues, trealvalues);
						realid.put(vargroupvalues, trealid);
					}
				}
			}
		}
		data.close();
		if ((validgroup==0) && (where!=null))
			return new Result("%2804%<br>\n", false, null);
		if ((validgroup==0) && (where==null))
			return new Result("%666%<br>\n", false, null);

		vgm.calculate();

		if (vdt==5)
		{
			cove.calculate();
			ed.setweights(cove.getresult());
		}
		int totalgroupmodalities=vgm.getTotal();
		if (totalgroupmodalities==0)
			totalgroupmodalities=1;

		Vector<Hashtable<String, String>> groupcodelabels=vgm.getgroupcodelabels();
		DataSetUtilities dsu=new DataSetUtilities();
		dsu.setreplace(replace);
		Hashtable<String, String> tempmd=new Hashtable<String, String>();
		for (int j=0; j<varg.length; j++)
		{
			if (!noclforvg)
				dsu.addnewvarfromolddict(dict, varg[j], groupcodelabels.get(j), tempmd, "g_"+varg[j]);
			else
				dsu.addnewvarfromolddict(dict, varg[j], tempmd, tempmd, "g_"+varg[j]);
		}
		dsu.addnewvar("step", "%1070%", Keywords.NUMSuffix, tempmd, tempmd);
		dsu.addnewvar("first", "%1071%", Keywords.TEXTSuffix, tempmd, tempmd);
		dsu.addnewvar("second", "%1072%", Keywords.TEXTSuffix, tempmd, tempmd);
		dsu.addnewvar("newcluster", "%1073%", Keywords.TEXTSuffix, tempmd, tempmd);
		dsu.addnewvar("freq", "%2079%", Keywords.NUMSuffix, tempmd, tempmd);
		dsu.addnewvar("distance", "%1074%", Keywords.NUMSuffix, tempmd, tempmd);
		dsu.addnewvar("RMSSTD", "%1075%", Keywords.NUMSuffix, tempmd, tempmd);
		dsu.addnewvar("PseudoF", "%1076%", Keywords.NUMSuffix, tempmd, tempmd);
		dsu.addnewvar("PseudoT2", "%1077%", Keywords.NUMSuffix, tempmd, tempmd);

		if (!dw.opendatatable(dsu.getfinalvarinfo()))
			return new Result(dw.getmessage(), false, null);

		String[] valuestowrite = new String[9+varg.length];
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
			Vector<double[]> trealvalues=realvalues.get(rifmodgroup);
			double[] m=new double[varrowvalues.length];
			for (int j=0; j<varrowvalues.length; j++)
			{
				m[j]=0;
			}
			for (int h=0; h<trealvalues.size(); h++)
			{
				varrowvalues=trealvalues.get(h);
				for (int j=0; j<varrowvalues.length; j++)
				{
					m[j]=m[j]+varrowvalues[j];
				}
			}
			for (int j=0; j<varrowvalues.length; j++)
			{
				m[j]=m[j]/trealvalues.size();
			}
			double T=0;
			double N=trealvalues.size();
			for (int h=0; h<trealvalues.size(); h++)
			{
				varrowvalues=trealvalues.get(h);
				for (int j=0; j<varrowvalues.length; j++)
				{
					T=T+Math.pow((varrowvalues[j]-m[j]),2);
				}
			}

			Vector<String> trealid=realid.get(rifmodgroup);
			Hashtable<String, Vector<double[]>> units=new Hashtable<String, Vector<double[]>>();
			int dimvar=varrowvalues.length;
			if (vdl==4)
			{
				double[] aCoord=null;
				double[] bCoord=null;
				int cycles=trealvalues.size()-1;
				double WK=0;
				double[] tempV=null;
				if (vdt==5)
					ed.setGroup(rifmodgroup);
				for (int c=0; c<cycles; c++)
				{
					double minDist=Double.MAX_VALUE;
					int refIndexI=0;
					int refIndexJ=0;
					double distm=0;
					for (int h=0; h<trealvalues.size()-1; h++)
					{
						for (int j=h+1; j<trealvalues.size(); j++)
						{
							distm=ed.getdistance(trealvalues.get(h), trealvalues.get(j));
							if (distm<minDist)
							{
								minDist=distm;
								refIndexI=h;
								refIndexJ=j;
							}
						}
					}
					String refc1=trealid.get(refIndexI);
					String refc2=trealid.get(refIndexJ);
					String newclus="CL"+String.valueOf(c+1);
					aCoord=trealvalues.get(refIndexI);
					bCoord=trealvalues.get(refIndexJ);
					double RMSSTD=0;
					double pseudot2=Double.NaN;
					Vector<double[]> tunits=new Vector<double[]>();
					double[] vmeans=new double[dimvar];
					double sum=2;
					if ((!refc1.startsWith("CL")) && (!refc2.startsWith("CL")))
					{
						for (int h=0; h<dimvar; h++)
						{
							RMSSTD+=(Math.pow((aCoord[h]-((aCoord[h]+bCoord[h])/(2))),2)+Math.pow((bCoord[h]-((aCoord[h]+bCoord[h])/(2))),2));
						}
						RMSSTD=Math.sqrt(RMSSTD/(dimvar));
						double[] ic=new double[dimvar];
						double[] jc=new double[dimvar];
						for (int h=0; h<dimvar; h++)
						{
							ic[h]=aCoord[h];
							jc[h]=bCoord[h];
						}
						tunits.add(ic);
						tunits.add(jc);
						for (int h=0; h<dimvar; h++)
						{
							vmeans[h]=(aCoord[h]+bCoord[h])/2;
						}
					}
					else
					{
						for (int j=0; j<dimvar; j++)
						{
							vmeans[j]=0;
						}
						Vector<double[]> f1=new Vector<double[]>();
						if (refc1.startsWith("CL"))
							f1=units.get(refc1);
						else
							f1.add(aCoord);
						Vector<double[]> f2=new Vector<double[]>();
						if (refc2.startsWith("CL"))
							f2=units.get(refc2);
						else
							f2.add(bCoord);
						for (int j=0; j<f1.size(); j++)
						{
							double[] tf1=f1.get(j);
							double[] tu=new double[dimvar];
							for (int h=0; h<dimvar; h++)
							{
								vmeans[h]=vmeans[h]+tf1[h];
								tu[h]=tf1[h];
							}
							tunits.add(tu);
						}
						for (int j=0; j<f2.size(); j++)
						{
							double[] tf2=f2.get(j);
							double[] tu=new double[dimvar];
							for (int h=0; h<dimvar; h++)
							{
								vmeans[h]=vmeans[h]+tf2[h];
								tu[h]=tf2[h];
							}
							tunits.add(tu);
						}
						sum=tunits.size();
						for (int h=0; h<dimvar; h++)
						{
							vmeans[h]=vmeans[h]/(sum);
						}
						for (int h=0; h<tunits.size(); h++)
						{
							double[] tf1=tunits.get(h);
							for (int j=0; j<dimvar; j++)
							{
								RMSSTD+=Math.pow(tf1[j]-vmeans[j],2);
							}
						}
						//CHECK IF CORRECT
						for (int h=0; h<dimvar; h++)
						{
							vmeans[h]=(aCoord[h]*f1.size()+bCoord[h]*f2.size())/(sum);
						}
						//END CODE TO CHECK
						double WC=0;
						double WL=0;
						for (int h=0; h<dimvar; h++)
						{
							aCoord[h]=0;
							bCoord[h]=0;
						}
						for (int j=0; j<f1.size(); j++)
						{
							tempV=f1.get(j);
							for (int h=0; h<dimvar; h++)
							{
								aCoord[h]=aCoord[h]+tempV[h]/f1.size();
							}
						}
						for (int j=0; j<f1.size(); j++)
						{
							tempV=f1.get(j);
							for (int h=0; h<dimvar; h++)
							{
								WC+=Math.pow((tempV[h]-aCoord[h]),2);
							}
						}
						for (int j=0; j<f2.size(); j++)
						{
							tempV=f2.get(j);
							for (int h=0; h<dimvar; h++)
							{
								bCoord[h]=bCoord[h]+tempV[h]/f2.size();
							}
						}
						for (int j=0; j<f2.size(); j++)
						{
							tempV=f2.get(j);
							for (int h=0; h<dimvar; h++)
							{
								WL+=Math.pow((tempV[h]-bCoord[h]),2);
							}
						}
						pseudot2=(RMSSTD-WC-WL)/((WC+WL)/(f1.size()+f2.size()-2));
						RMSSTD=Math.sqrt(RMSSTD/(dimvar*(sum-1)));
						if (refc1.startsWith("CL"))
							units.remove(refc1);
						if (refc2.startsWith("CL"))
							units.remove(refc2);
					}
					units.put(newclus, tunits);
					WK=0;
					for (Enumeration<String> en=units.keys(); en.hasMoreElements();)
					{
						String clusname=en.nextElement();
						Vector<double[]> ve=units.get(clusname);
						double[] mm=new double[dimvar];
						for (int cl=0; cl<ve.size(); cl++)
						{
							double[] vet=ve.get(cl);
							for (int h=0; h<dimvar; h++)
							{
								mm[h]=mm[h]+vet[h]/ve.size();
							}
						}
						for (int cl=0; cl<ve.size(); cl++)
						{
							double[] vet=ve.get(cl);
							for (int h=0; h<dimvar; h++)
							{
								WK+=Math.pow((vet[h]-mm[h]),2);
							}
						}
					}
					trealvalues.remove(refIndexJ);
					trealvalues.remove(refIndexI);
					trealid.remove(refIndexJ);
					trealid.remove(refIndexI);
					trealvalues.add(vmeans);
					trealid.add(newclus);
					valuestowrite[varg.length]=String.valueOf(c+1);
					valuestowrite[varg.length+1]=refc1;
					valuestowrite[varg.length+2]=refc2;
					valuestowrite[varg.length+3]=newclus;
					valuestowrite[varg.length+4]=double2String(sum);
					valuestowrite[varg.length+5]=double2String(minDist);
					valuestowrite[varg.length+6]=double2String(RMSSTD);
					valuestowrite[varg.length+7]=double2String((T-WK)/(N-c-2)/(WK/(c+1)));
					valuestowrite[varg.length+8]=double2String(pseudot2);
					dw.write(valuestowrite);
				}
			}
			else
			{
				double[] aCoord=null;
				double[] bCoord=null;
				int cycles=trealvalues.size()-1;
				double WK=0;
				double[] tempV=null;
				if (vdt==5)
					ed.setGroup(rifmodgroup);
				for (int c=0; c<cycles; c++)
				{
					double minDist=Double.MAX_VALUE;
					int refIndexI=0;
					int refIndexJ=0;
					double distm=0;
					String refc1="";
					String refc2="";
					for (int h=0; h<trealvalues.size()-1; h++)
					{
						for (int j=h+1; j<trealvalues.size(); j++)
						{
							refc1=trealid.get(h);
							refc2=trealid.get(j);
							if ((!refc1.startsWith("CL")) && (!refc2.startsWith("CL")))
								distm=ed.getdistance(trealvalues.get(h), trealvalues.get(j));
							else
							{
								Vector<double[]> f1=new Vector<double[]>();
								if (refc1.startsWith("CL"))
									f1=units.get(refc1);
								else
									f1.add(trealvalues.get(h));
								Vector<double[]> f2=new Vector<double[]>();
								if (refc2.startsWith("CL"))
									f2=units.get(refc2);
								else
									f2.add(trealvalues.get(j));
								if (vdl==1)
								{
									distm=Double.MAX_VALUE;
									for (int c1=0; c1<f1.size(); c1++)
									{
										for (int c2=0; c2<f2.size(); c2++)
										{
											double adistm=ed.getdistance(f1.get(c1), f2.get(c2));
											if (adistm<distm)
												distm=adistm;
										}
									}
								}
								else if (vdl==2)
								{
									distm=-1.7976931348623157E308;
									for (int c1=0; c1<f1.size(); c1++)
									{
										for (int c2=0; c2<f2.size(); c2++)
										{
											double adistm=ed.getdistance(f1.get(c1), f2.get(c2));
											if (adistm>distm)
												distm=adistm;
										}
									}
								}
								else
								{
									distm=0;
									for (int c1=0; c1<f1.size(); c1++)
									{
										for (int c2=0; c2<f2.size(); c2++)
										{
											distm+=ed.getdistance(f1.get(c1), f2.get(c2));

										}
									}
									distm=distm/(f1.size()+f2.size());
								}
							}
							if (distm<minDist)
							{
								minDist=distm;
								refIndexI=h;
								refIndexJ=j;
							}
						}
					}
					refc1=trealid.get(refIndexI);
					refc2=trealid.get(refIndexJ);
					String newclus="CL"+String.valueOf(c+1);
					double RMSSTD=0;
					double pseudot2=Double.NaN;
					Vector<double[]> tunits=new Vector<double[]>();
					double[] vmeans=new double[dimvar];
					double sum=2;
					if ((!refc1.startsWith("CL")) && (!refc2.startsWith("CL")))
					{
						aCoord=trealvalues.get(refIndexI);
						bCoord=trealvalues.get(refIndexJ);
						for (int h=0; h<dimvar; h++)
						{
							RMSSTD+=(Math.pow((aCoord[h]-((aCoord[h]+bCoord[h])/(2))),2)+Math.pow((bCoord[h]-((aCoord[h]+bCoord[h])/(2))),2));
						}
						RMSSTD=Math.sqrt(RMSSTD/(dimvar));
						double[] ic=new double[dimvar];
						double[] jc=new double[dimvar];
						for (int h=0; h<dimvar; h++)
						{
							ic[h]=aCoord[h];
							jc[h]=bCoord[h];
						}
						tunits.add(ic);
						tunits.add(jc);
					}
					else
					{
						for (int j=0; j<dimvar; j++)
						{
							vmeans[j]=0;
						}
						Vector<double[]> f1=new Vector<double[]>();
						if (refc1.startsWith("CL"))
							f1=units.get(refc1);
						else
							f1.add(trealvalues.get(refIndexI));
						Vector<double[]> f2=new Vector<double[]>();
						if (refc2.startsWith("CL"))
							f2=units.get(refc2);
						else
							f2.add(trealvalues.get(refIndexJ));
						for (int j=0; j<f1.size(); j++)
						{
							double[] tf1=f1.get(j);
							double[] tu=new double[dimvar];
							for (int h=0; h<dimvar; h++)
							{
								vmeans[h]=vmeans[h]+tf1[h];
								tu[h]=tf1[h];
							}
							tunits.add(tu);
						}
						for (int j=0; j<f2.size(); j++)
						{
							double[] tf2=f2.get(j);
							double[] tu=new double[dimvar];
							for (int h=0; h<dimvar; h++)
							{
								vmeans[h]=vmeans[h]+tf2[h];
								tu[h]=tf2[h];
							}
							tunits.add(tu);
						}
						sum=f1.size()+f2.size();
						for (int h=0; h<dimvar; h++)
						{
							vmeans[h]=vmeans[h]/(sum);
						}
						for (int h=0; h<tunits.size(); h++)
						{
							double[] tf1=tunits.get(h);
							for (int j=0; j<dimvar; j++)
							{
								RMSSTD+=Math.pow(tf1[j]-vmeans[j],2);
							}
						}
						double WC=0;
						double WL=0;
						for (int h=0; h<dimvar; h++)
						{
							aCoord[h]=0;
							bCoord[h]=0;
						}
						for (int j=0; j<f1.size(); j++)
						{
							tempV=f1.get(j);
							for (int h=0; h<dimvar; h++)
							{
								aCoord[h]=aCoord[h]+tempV[h]/f1.size();
							}
						}
						for (int j=0; j<f1.size(); j++)
						{
							tempV=f1.get(j);
							for (int h=0; h<dimvar; h++)
							{
								WC+=Math.pow((tempV[h]-aCoord[h]),2);
							}
						}
						for (int j=0; j<f2.size(); j++)
						{
							tempV=f2.get(j);
							for (int h=0; h<dimvar; h++)
							{
								bCoord[h]=bCoord[h]+tempV[h]/f2.size();
							}
						}
						for (int j=0; j<f2.size(); j++)
						{
							tempV=f2.get(j);
							for (int h=0; h<dimvar; h++)
							{
								WL+=Math.pow((tempV[h]-bCoord[h]),2);
							}
						}
						pseudot2=(RMSSTD-WC-WL)/((WC+WL)/(f1.size()+f2.size()-2));
						RMSSTD=Math.sqrt(RMSSTD/(dimvar*(sum-1)));
						if (refc1.startsWith("CL"))
							units.remove(refc1);
						if (refc2.startsWith("CL"))
							units.remove(refc2);
					}
					units.put(newclus, tunits);
					WK=0;
					for (Enumeration<String> en=units.keys(); en.hasMoreElements();)
					{
						String clusname=en.nextElement();
						Vector<double[]> ve=units.get(clusname);
						double[] mm=new double[dimvar];
						for (int cl=0; cl<ve.size(); cl++)
						{
							double[] vet=ve.get(cl);
							for (int h=0; h<dimvar; h++)
							{
								mm[h]=mm[h]+vet[h]/ve.size();
							}
						}
						for (int cl=0; cl<ve.size(); cl++)
						{
							double[] vet=ve.get(cl);
							for (int h=0; h<dimvar; h++)
							{
								WK+=Math.pow((vet[h]-mm[h]),2);
							}
						}
					}
					trealvalues.remove(refIndexJ);
					trealvalues.remove(refIndexI);
					trealid.remove(refIndexJ);
					trealid.remove(refIndexI);
					trealvalues.add(vmeans);
					trealid.add(newclus);
					valuestowrite[varg.length]=String.valueOf(c+1);
					valuestowrite[varg.length+1]=refc1;
					valuestowrite[varg.length+2]=refc2;
					valuestowrite[varg.length+3]=newclus;
					valuestowrite[varg.length+4]=double2String(sum);
					valuestowrite[varg.length+5]=double2String(minDist);
					valuestowrite[varg.length+6]=double2String(RMSSTD);
					valuestowrite[varg.length+7]=double2String((T-WK)/(N-c-2)/(WK/(c+1)));
					valuestowrite[varg.length+8]=double2String(pseudot2);
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
		parameters.add(new GetRequiredParameters(Keywords.var, "vars=all", true, 1078, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.vargroup, "vars=all", false, 655, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.where,"text", false, 2805,dep,"",2));
		parameters.add(new GetRequiredParameters("", "note", false, 2806, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.varid, "var=all",	false, 817, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.linktype, "listsingle=1080_" + Keywords.LSingle+",1081_"+Keywords.LComplete+",1082_"+Keywords.LAverage+",2078_"+Keywords.LCentroid,false, 1079, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.Distance, "listsingle=1084_" + Keywords.EuclideanDistance+",1085_"+Keywords.SquaredEuclideanDistance+",1086_"+Keywords.ManhattanDistance+",1087_"+Keywords.ChebyshevDistance+",1777_"+Keywords.MahalanobisDistance,false, 1083, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.novgconvert, "checkbox", false, 2227, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.noclforvg, "checkbox", false, 2229, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.orderclbycode, "checkbox", false, 2231, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.replace, "listsingle=530_NULL,531_"+Keywords.replaceall+",532_"+Keywords.replaceformat+",533_"+Keywords.replacemissing,false, 534, dep, "", 2));
		return parameters;
	}
	/**
	*Returns the group and the name of the procedure
	*/
	public String[] getstepinfo()
	{
		String[] retprocinfo=new String[2];
		retprocinfo[0]="1089";
		retprocinfo[1]="1090";
		return retprocinfo;
	}

}
