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
import ADaMSoft.dataaccess.GroupedFastTempDataSet;
import ADaMSoft.keywords.Keywords;
import ADaMSoft.utilities.GetRequiredParameters;
import ADaMSoft.utilities.DataSetUtilities;
import ADaMSoft.utilities.ObjectTransformer;
import ADaMSoft.utilities.StringDistances;
import ADaMSoft.utilities.StepUtilities;

/**
* This is the procedure that implements the cross products of two data sets for record linkage
* @author marco.scarno@gmail.com
* @date 14/02/2017
*/
public class ProcRlcrossproduct extends ObjectTransformer implements RunStep
{
	/**
	* Starts the execution of Proc Rlcrossproduct
	*/
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		Vector<StepResult> result = new Vector<StepResult>();
		String [] requiredparameters=new String[] {Keywords.OUT.toLowerCase()+"maxdist", Keywords.dict+"a", Keywords.dict+"b", Keywords.link};
		String [] optionalparameters=new String[] {Keywords.OUT.toLowerCase()+"crossp", Keywords.OUT.toLowerCase()+"anob", Keywords.OUT.toLowerCase()+"bnoa", Keywords.idvar+"a", Keywords.idvar+"b", Keywords.blockvar+"a", Keywords.blockvar+"b", Keywords.considerpartofb, Keywords.replace};
		Keywords.percentage_total=0;
		Keywords.percentage_done=0;
		StepUtilities steputilities=new StepUtilities();
		if (!steputilities.checkParameters(requiredparameters, optionalparameters, parameters))
		{
			return new Result(steputilities.getMessage(), false, null);
		}
		boolean outcp=false;
		if (parameters.get(Keywords.OUT.toLowerCase()+"crossp")!=null) outcp=true;

		boolean considerpartofb=(parameters.get(Keywords.considerpartofb)!=null);
		String replace =(String)parameters.get(Keywords.replace);
		int rifrep=0;
		if (replace==null)
			rifrep=0;
		else if (replace.equalsIgnoreCase(Keywords.replaceall))
			rifrep=1;
		else if (replace.equalsIgnoreCase(Keywords.replaceformat))
			rifrep=2;
		else if (replace.equalsIgnoreCase(Keywords.replacemissing))
			rifrep=3;
		int[] idvara=null;
		int[] idvarb=null;
		int[] blockvara=null;
		int[] blockvarb=null;
		String[] nameidvara=new String[0];
		String[] nameidvarb=new String[0];
		String[] nameblockvara=new String[0];
		String[] nameblockvarb=new String[0];
		DictionaryReader dicta = (DictionaryReader)parameters.get(Keywords.dict+"a");
		DictionaryReader dictb = (DictionaryReader)parameters.get(Keywords.dict+"b");
		Hashtable<String, Integer> posvardicta=new Hashtable<String, Integer>();
		Hashtable<String, Integer> posvardictb=new Hashtable<String, Integer>();
		for (int i=0; i<dicta.gettotalvar(); i++)
		{
			posvardicta.put((dicta.getvarname(i)).toUpperCase(), new Integer(i));
		}
		for (int i=0; i<dictb.gettotalvar(); i++)
		{
			posvardictb.put((dictb.getvarname(i)).toUpperCase(), new Integer(i));
		}
		String sidvara =(String)parameters.get(Keywords.idvar+"a");
		String sidvarb =(String)parameters.get(Keywords.idvar+"b");
		String sblockvara =(String)parameters.get(Keywords.blockvar+"a");
		String sblockvarb =(String)parameters.get(Keywords.blockvar+"b");
		if (sidvara!=null)
		{
			String notex="";
			try
			{
				nameidvara=sidvara.split(" ");
				idvara=new int[nameidvara.length];
				for (int i=0; i<nameidvara.length; i++)
				{
					if (posvardicta.get(nameidvara[i].toUpperCase())==null) notex=notex+nameidvara[i].toUpperCase()+" ";
					else idvara[i]=(posvardicta.get(nameidvara[i].toUpperCase())).intValue();
				}
				if (!notex.equals("")) return new Result("%2937%: "+notex.trim()+"<br>\n", false, null);
			}
			catch (Exception ev)
			{
				return new Result("%2938%<br>\n", false, null);
			}
		}
		if (sidvarb!=null)
		{
			String notex="";
			try
			{
				nameidvarb=sidvarb.split(" ");
				idvarb=new int[nameidvarb.length];
				for (int i=0; i<nameidvarb.length; i++)
				{
					if (posvardictb.get(nameidvarb[i].toUpperCase())==null) notex=notex+nameidvarb[i].toUpperCase()+" ";
					else idvarb[i]=(posvardictb.get(nameidvarb[i].toUpperCase())).intValue();
				}
				if (!notex.equals("")) return new Result("%2939%: "+notex.trim()+"<br>\n", false, null);
			}
			catch (Exception ev)
			{
				return new Result("%2940%<br>\n", false, null);
			}
		}
		if (sblockvara!=null)
		{
			String notex="";
			try
			{
				nameblockvara=sblockvara.split(" ");
				blockvara=new int[nameblockvara.length];
				for (int i=0; i<nameblockvara.length; i++)
				{
					if (posvardicta.get(nameblockvara[i].toUpperCase())==null) notex=notex+nameblockvara[i].toUpperCase()+" ";
					else blockvara[i]=(posvardicta.get(nameblockvara[i].toUpperCase())).intValue();
				}
				if (!notex.equals("")) return new Result("%2941%: "+notex.trim()+"<br>\n", false, null);
			}
			catch (Exception ev)
			{
				return new Result("%2942%<br>\n", false, null);
			}
		}
		if (sblockvarb!=null)
		{
			String notex="";
			try
			{
				nameblockvarb=sblockvarb.split(" ");
				blockvarb=new int[nameblockvarb.length];
				for (int i=0; i<nameblockvarb.length; i++)
				{
					if (posvardictb.get(nameblockvarb[i].toUpperCase())==null) notex=notex+nameblockvarb[i].toUpperCase()+" ";
					else blockvarb[i]=(posvardictb.get(nameblockvarb[i].toUpperCase())).intValue();
				}
				if (!notex.equals("")) return new Result("%2943%: "+notex.trim()+"<br>\n", false, null);
			}
			catch (Exception ev)
			{
				return new Result("%2944%<br>\n", false, null);
			}
		}
		if ((sblockvara!=null) && (sblockvarb==null))  return new Result("%2945%<br>\n", false, null);
		if ((sblockvara==null) && (sblockvarb!=null))  return new Result("%2945%<br>\n", false, null);
		if ((sblockvara!=null) && (sblockvarb!=null))
		{
			if (blockvara.length!=blockvarb.length) return new Result("%2946%<br>\n", false, null);
		}
		if ((sblockvara==null) && (parameters.get(Keywords.OUT.toLowerCase()+"anob")!=null))
			return new Result("%2963%<br>\n", false, null);
		if ((sblockvara==null) && (parameters.get(Keywords.OUT.toLowerCase()+"bnoa")!=null))
			return new Result("%2964%<br>\n", false, null);
		int numgv=0;
		if (sblockvara!=null) numgv=blockvara.length;
		String link=(String)parameters.get(Keywords.link);
		String[] linka=new String[0];
		String[] linkb=new String[0];
		int[] metric=new int[0];
		int[] poslinka=new int[0];
		int[] poslinkb=new int[0];
		String[] metricnames=new String[0];
		try
		{
			String[] partlink=link.split(";");
			linka=new String[partlink.length];
			linkb=new String[partlink.length];
			String[] tempmet=new String[partlink.length];
			metric=new int[partlink.length];
			metricnames=new String[partlink.length];
			for (int i=0; i<partlink.length; i++)
			{
				metric[i]=-1;
				String[] vars=partlink[i].split(",");
				if (vars.length!=2) return new Result("%2947%<br>\n", false, null);
				String[] met=vars[1].split("=");
				if (met.length!=2) return new Result("%2947%<br>\n", false, null);
				linka[i]=vars[0].trim();
				linkb[i]=met[0].trim();
				if (met[1].trim().equalsIgnoreCase("ChapmanLengthDeviation")) metric[i]=0;
				if (met[1].trim().equalsIgnoreCase("CosineSimilarity")) metric[i]=1;
				if (met[1].trim().equalsIgnoreCase("MatchingCoefficient")) metric[i]=2;
				if (met[1].trim().equalsIgnoreCase("OverlapCoefficient")) metric[i]=3;
				if (met[1].trim().equalsIgnoreCase("DiceSimilarity")) metric[i]=4;
				if (met[1].trim().equalsIgnoreCase("JaroWinkler")) metric[i]=5;
				if (met[1].trim().equalsIgnoreCase("JaccardSimilarity")) metric[i]=6;
				if (met[1].trim().equalsIgnoreCase("QGramsDistance")) metric[i]=7;
				if (met[1].trim().equalsIgnoreCase("Levenshtein")) metric[i]=8;
				if (met[1].trim().equalsIgnoreCase("BlockDistance")) metric[i]=9;
				if (met[1].trim().equalsIgnoreCase("MongeElkan")) metric[i]=10;
				if (met[1].trim().equalsIgnoreCase("ChapmanOrderedNameCompoundSimilarity")) metric[i]=11;
				if (met[1].trim().equalsIgnoreCase("Jaro")) metric[i]=12;
				if (met[1].trim().equalsIgnoreCase("EuclideanDistance")) metric[i]=16;
				if (met[1].trim().equalsIgnoreCase("SoundEx")) metric[i]=14;
				if (met[1].trim().equalsIgnoreCase("NeedlemanWunch")) metric[i]=15;
				if (met[1].trim().equalsIgnoreCase("AbsDistance")) metric[i]=17;
				if (met[1].trim().equalsIgnoreCase("Equals")) metric[i]=18;
				if (met[1].trim().equalsIgnoreCase("EqualsIgnoreCase")) metric[i]=19;
				tempmet[i]=met[1].trim();
				metricnames[i]=tempmet[i].toUpperCase();
			}
			String nexa="";
			String nexb="";
			String nmet="";
			poslinka=new int[linka.length];
			poslinkb=new int[linkb.length];
			for (int i=0; i<linka.length; i++)
			{
				if (posvardicta.get(linka[i].toUpperCase())==null) nexa=nexa+linka[i].toUpperCase()+" ";
				else poslinka[i]=(posvardicta.get(linka[i].toUpperCase())).intValue();
				if (posvardictb.get(linkb[i].toUpperCase())==null) nexb=nexb+linkb[i].toUpperCase()+" ";
				else poslinkb[i]=(posvardictb.get(linkb[i].toUpperCase())).intValue();
				if (metric[i]==-1) nmet=nmet+tempmet[i]+" ";
			}
			String errorlinks="";
			if (!nexa.equals("")) errorlinks=errorlinks+"%2948% ("+nexa.trim()+")<br>\n";
			if (!nexb.equals("")) errorlinks=errorlinks+"%2949% ("+nexb.trim()+")<br>\n";
			if (!nmet.equals(""))
			{
				errorlinks=errorlinks+"%2950% ("+nmet.trim()+")<br>\n";
				errorlinks=errorlinks+"%2951%:<br>\n";
				errorlinks=errorlinks+"ChapmanLengthDeviation<br>\n";
				errorlinks=errorlinks+"MatchingCoefficient<br>\n";
				errorlinks=errorlinks+"OverlapCoefficient<br>\n";
				errorlinks=errorlinks+"DiceSimilarity<br>\n";
				errorlinks=errorlinks+"JaroWinkler<br>\n";
				errorlinks=errorlinks+"QGramsDistance<br>\n";
				errorlinks=errorlinks+"Levenshtein<br>\n";
				errorlinks=errorlinks+"BlockDistance<br>\n";
				errorlinks=errorlinks+"MongeElkan<br>\n";
				errorlinks=errorlinks+"ChapmanOrderedNameCompoundSimilarity<br>\n";
				errorlinks=errorlinks+"Jaro<br>\n";
				errorlinks=errorlinks+"SoundEx<br>\n";
				errorlinks=errorlinks+"NeedlemanWunch<br>\n";
				errorlinks=errorlinks+"Equals<br>\n";
				errorlinks=errorlinks+"EqualsIgnoreCase<br>\n";
				errorlinks=errorlinks+"EuclideanDistance<br>\n";
				errorlinks=errorlinks+"AbsDistance<br>\n";
			}
			if (!errorlinks.equals("")) return new Result(errorlinks+"<br>\n", false, null);
		}
		catch (Exception elink)
		{
			return new Result("%2947%<br>\n", false, null);
		}
		Vector<Integer> pos_num=new Vector<Integer>();
		int nummet=0;
		for (int i=0; i<metric.length; i++)
		{
			if ((metric[i]==16) || (metric[i]==17))
			{
				nummet++;
				pos_num.add(new Integer(i));
			}
		}
		double[] min_a=new double[nummet];
		double[] min_b=new double[nummet];
		double[] max_a=new double[nummet];
		double[] max_b=new double[nummet];
		if (nummet>0)
		{
			for (int i=0; i<nummet; i++)
			{
				min_a[i]=Double.MAX_VALUE;
				min_b[i]=Double.MAX_VALUE;
				max_a[i]=-1.7976931348623157E308;
				max_b[i]=-1.7976931348623157E308;
			}
		}
		DataWriter dw=null;
		if (outcp)
		{
			dw=new DataWriter(parameters, Keywords.OUT.toLowerCase()+"crossp");
			if (!dw.getmessage().equals(""))
				return new Result(dw.getmessage(), false, null);
		}

		DataWriter dwmaxdist=new DataWriter(parameters, Keywords.OUT.toLowerCase()+"maxdist");
		if (!dwmaxdist.getmessage().equals(""))
			return new Result(dwmaxdist.getmessage(), false, null);

		DataWriter dwbnoa=null;
		DataWriter dwanob=null;

		DataReader dataa = new DataReader(dicta);
		if (!dataa.open(null, rifrep, false))
			return new Result(dataa.getmessage(), false, null);
		String tempdir=(String)parameters.get(Keywords.WorkDir);
		GroupedFastTempDataSet ftdsa=new GroupedFastTempDataSet(tempdir, numgv);
		if (ftdsa.geterror())
			return new Result(ftdsa.getmessage(), false, null);
		String where=(String)parameters.get(Keywords.where.toLowerCase());
		if (where!=null)
		{
			if (!dataa.setcondition(where)) return new Result(dataa.getmessage(), false, null);
		}
		int varina=0;
		if (sidvara!=null) varina=varina+idvara.length;
		varina=varina+poslinka.length;
		String[] values=new String[0];
		String[] valtowrite=new String[varina];
		String[] group=null;
		if (sblockvara!=null) group=new String[numgv];
		int posvar=0;
		double tempdouble;
		int total_record_b=0;
		while (!dataa.isLast())
		{
			values = dataa.getRecord();
			if (values!=null)
			{
				posvar=0;
				if (sidvara!=null)
				{
					for (int i=0; i<idvara.length; i++)
					{
						valtowrite[posvar]=values[idvara[i]];
						posvar++;
					}
				}
				if (sblockvara!=null)
				{
					for (int i=0; i<blockvara.length; i++)
					{
						group[i]=values[blockvara[i]];
					}
				}
				for (int i=0; i<poslinka.length; i++)
				{
					valtowrite[posvar]=values[poslinka[i]];
					posvar++;
				}
				if (nummet>0)
				{
					for (int i=0; i<nummet; i++)
					{
						posvar=(pos_num.get(i)).intValue();
						try
						{
							tempdouble=Double.parseDouble(values[poslinka[posvar]]);
							if (!Double.isNaN(tempdouble))
							{
								if (tempdouble<min_a[i]) min_a[i]=tempdouble;
								if (tempdouble>max_a[i]) max_a[i]=tempdouble;
							}
						}
						catch (Exception et) {}
					}
				}
				if (!ftdsa.write(group, valtowrite))
				{
					dataa.close();
					ftdsa.endwrite();
					ftdsa.deletefile();
					return new Result(ftdsa.getmessage(), false, null);
				}
			}
		}
		dataa.close();
		if (!ftdsa.endwrite())
		{
			String msgnotg=ftdsa.getmessage();
			ftdsa.deletefile();
			return new Result(msgnotg, false, null);
		}

		DataReader datab = new DataReader(dictb);
		if (!datab.open(null, rifrep, false))
		{
			ftdsa.deletefile();
			return new Result(datab.getmessage(), false, null);
		}
		GroupedFastTempDataSet ftdsb=new GroupedFastTempDataSet(tempdir, numgv);
		if (ftdsb.geterror())
		{
			ftdsa.deletefile();
			return new Result(ftdsb.getmessage(), false, null);
		}
		if (where!=null)
		{
			if (!datab.setcondition(where)) return new Result(datab.getmessage(), false, null);
		}
		int varinb=0;
		if (sidvarb!=null) varinb=varinb+idvarb.length;
		varinb=varinb+poslinkb.length;
		valtowrite=new String[varinb];
		while (!datab.isLast())
		{
			values = datab.getRecord();
			if (values!=null)
			{
				total_record_b++;
				posvar=0;
				if (sidvarb!=null)
				{
					for (int i=0; i<idvarb.length; i++)
					{
						valtowrite[posvar]=values[idvarb[i]];
						posvar++;
					}
				}
				if (sblockvarb!=null)
				{
					for (int i=0; i<blockvarb.length; i++)
					{
						group[i]=values[blockvarb[i]];
					}
				}
				for (int i=0; i<poslinkb.length; i++)
				{
					valtowrite[posvar]=values[poslinkb[i]];
					posvar++;
				}
				if (nummet>0)
				{
					for (int i=0; i<nummet; i++)
					{
						posvar=(pos_num.get(i)).intValue();
						try
						{
							tempdouble=Double.parseDouble(values[poslinkb[posvar]]);
							if (!Double.isNaN(tempdouble))
							{
								if (tempdouble<min_b[i]) min_b[i]=tempdouble;
								if (tempdouble>max_b[i]) max_b[i]=tempdouble;
							}
						}
						catch (Exception et) {}
					}
				}
				if (!ftdsb.write(group, valtowrite))
				{
					String notmsgg=ftdsb.getmessage();
					datab.close();
					ftdsb.endwrite();
					ftdsb.deletefile();
					ftdsa.deletefile();
					return new Result(notmsgg, false, null);
				}
			}
		}
		datab.close();
		if (!ftdsb.endwrite())
		{
			String notmsgg=ftdsb.getmessage();
			ftdsa.deletefile();
			ftdsb.deletefile();
			return new Result(notmsgg, false, null);
		}
		double[] refmaxed=new double[0];
		double[] refmaxad=new double[0];
		if (nummet>0)
		{
			refmaxed=new double[nummet];
			refmaxad=new double[nummet];
			for (int i=0; i<nummet; i++)
			{
				if ( Math.sqrt(Math.pow(min_b[i]-max_a[i],2))>Math.sqrt(Math.pow(min_a[i]-max_b[i],2))) refmaxed[i]=Math.sqrt(Math.pow(min_b[i]-max_a[i],2));
				else refmaxed[i]=Math.sqrt(Math.pow(min_a[i]-max_b[i],2));
				if ( Math.abs(min_b[i]-max_a[i]) > Math.abs(min_a[i]-max_b[i])) refmaxad[i]=Math.abs(min_b[i]-max_a[i]);
				else refmaxad[i]=Math.abs(min_a[i]-max_b[i]);
			}
		}
		DataSetUtilities dsu=new DataSetUtilities();
		dsu.setreplace(replace);
		DataSetUtilities dsubnoa=new DataSetUtilities();
		dsubnoa.setreplace(replace);
		DataSetUtilities dsuanob=new DataSetUtilities();
		dsuanob.setreplace(replace);
		DataSetUtilities dsumaxdist=new DataSetUtilities();
		dsumaxdist.setreplace(replace);

		Hashtable<String, String> tempmd=new Hashtable<String, String>();

		int outvars=0;

		if (sblockvara!=null)
		{
			for (int i=0; i<nameblockvara.length; i++)
			{
				if (rifrep==1)
				{
					dsu.addnewvar("blockvar_"+nameblockvara[i]+"_"+nameblockvarb[i], "%2952% "+dicta.getvarlabelfromname(nameblockvara[i])+"_"+dictb.getvarlabelfromname(nameblockvarb[i]), Keywords.TEXTSuffix, tempmd, tempmd);
					dsumaxdist.addnewvar("blockvar_"+nameblockvara[i]+"_"+nameblockvarb[i], "%2952% "+dicta.getvarlabelfromname(nameblockvara[i])+"_"+dictb.getvarlabelfromname(nameblockvarb[i]), Keywords.TEXTSuffix, tempmd, tempmd);
				}
				else if (rifrep==0)
				{
					Hashtable<String, String> tempcla=dicta.getcodelabelfromname(nameblockvara[i]);
					Hashtable<String, String> tempclb=dictb.getcodelabelfromname(nameblockvarb[i]);
					for (Enumeration<String> en=tempclb.keys(); en.hasMoreElements();)
					{
						String tmpcl=(String)en.nextElement();
						tempcla.put(tmpcl, tempclb.get(tmpcl));
					}
					Hashtable<String, String> tempmda=dicta.getmissingdatafromname(nameblockvara[i]);
					Hashtable<String, String> tempmdb=dictb.getmissingdatafromname(nameblockvarb[i]);
					for (Enumeration<String> en=tempmdb.keys(); en.hasMoreElements();)
					{
						String tmpmd=(String)en.nextElement();
						tempmda.put(tmpmd, tempmdb.get(tmpmd));
					}
					dsu.addnewvar("blockvar_"+nameblockvara[i]+"_"+nameblockvarb[i], "%2952% "+dicta.getvarlabelfromname(nameblockvara[i])+"_"+dictb.getvarlabelfromname(nameblockvarb[i]), Keywords.TEXTSuffix, tempcla, tempmda);
					dsumaxdist.addnewvar("blockvar_"+nameblockvara[i]+"_"+nameblockvarb[i], "%2952% "+dicta.getvarlabelfromname(nameblockvara[i])+"_"+dictb.getvarlabelfromname(nameblockvarb[i]), Keywords.TEXTSuffix, tempcla, tempmda);
				}
				else if (rifrep==3)
				{
					Hashtable<String, String> tempcla=dicta.getcodelabelfromname(nameblockvara[i]);
					Hashtable<String, String> tempclb=dictb.getcodelabelfromname(nameblockvarb[i]);
					for (Enumeration<String> en=tempclb.keys(); en.hasMoreElements();)
					{
						String tmpcl=(String)en.nextElement();
						tempcla.put(tmpcl, tempclb.get(tmpcl));
					}
					dsu.addnewvar("blockvar_"+nameblockvara[i]+"_"+nameblockvarb[i], "%2952% "+dicta.getvarlabelfromname(nameblockvara[i])+"_"+dictb.getvarlabelfromname(nameblockvarb[i]), Keywords.TEXTSuffix, tempcla, tempmd);
					dsumaxdist.addnewvar("blockvar_"+nameblockvara[i]+"_"+nameblockvarb[i], "%2952% "+dicta.getvarlabelfromname(nameblockvara[i])+"_"+dictb.getvarlabelfromname(nameblockvarb[i]), Keywords.TEXTSuffix, tempcla, tempmd);
				}
				else
				{
					Hashtable<String, String> tempmda=dicta.getmissingdatafromname(nameblockvara[i]);
					Hashtable<String, String> tempmdb=dictb.getmissingdatafromname(nameblockvarb[i]);
					for (Enumeration<String> en=tempmdb.keys(); en.hasMoreElements();)
					{
						String tmpmd=(String)en.nextElement();
						tempmda.put(tmpmd, tempmdb.get(tmpmd));
					}
					dsu.addnewvar("blockvar_"+nameblockvara[i]+"_"+nameblockvarb[i], "%2952% "+dicta.getvarlabelfromname(nameblockvara[i])+"_"+dictb.getvarlabelfromname(nameblockvarb[i]), Keywords.TEXTSuffix, tempmd, tempmda);
					dsumaxdist.addnewvar("blockvar_"+nameblockvara[i]+"_"+nameblockvarb[i], "%2952% "+dicta.getvarlabelfromname(nameblockvara[i])+"_"+dictb.getvarlabelfromname(nameblockvarb[i]), Keywords.TEXTSuffix, tempmd, tempmda);
				}
				outvars++;
			}
		}

		int nunmgb=ftdsb.getnumg()+1;
		int nunmga=ftdsa.getnumg()+1;
		String[] vala=null;
		String[] valb=null;
		double distances=Double.NaN;
		StringDistances sd=new StringDistances();
		int pointera=0;
		int pointerb=0;
		if (sidvara!=null)
		{
			pointera=idvara.length;
			for (int i=0; i<nameidvara.length; i++)
			{
				if (rifrep==1)
					dsu.addnewvar("idvara_"+nameidvara[i], "%2953%: "+dicta.getvarlabelfromname(nameidvara[i]), dicta.getvarformatfromname(nameidvara[i]), tempmd, tempmd);
				else if (rifrep==0)
					dsu.addnewvar("idvara_"+nameidvara[i], "%2953%: "+dicta.getvarlabelfromname(nameidvara[i]), dicta.getvarformatfromname(nameidvara[i]), dicta.getcodelabelfromname(nameidvara[i]), dicta.getmissingdatafromname(nameidvara[i]));
				else if (rifrep==3)
					dsu.addnewvar("idvara_"+nameidvara[i], "%2953%: "+dicta.getvarlabelfromname(nameidvara[i]), dicta.getvarformatfromname(nameidvara[i]), dicta.getcodelabelfromname(nameidvara[i]), tempmd);
				else
					dsu.addnewvar("idvara_"+nameidvara[i], "%2953%: "+dicta.getvarlabelfromname(nameidvara[i]), dicta.getvarformatfromname(nameidvara[i]), tempmd, dicta.getmissingdatafromname(nameidvara[i]));
				outvars++;
				if (rifrep==1)
					dsumaxdist.addnewvar("idvara_"+nameidvara[i], "%2953%: "+dicta.getvarlabelfromname(nameidvara[i]), dicta.getvarformatfromname(nameidvara[i]), tempmd, tempmd);
				else if (rifrep==0)
					dsumaxdist.addnewvar("idvara_"+nameidvara[i], "%2953%: "+dicta.getvarlabelfromname(nameidvara[i]), dicta.getvarformatfromname(nameidvara[i]), dicta.getcodelabelfromname(nameidvara[i]), dicta.getmissingdatafromname(nameidvara[i]));
				else if (rifrep==3)
					dsumaxdist.addnewvar("idvara_"+nameidvara[i], "%2953%: "+dicta.getvarlabelfromname(nameidvara[i]), dicta.getvarformatfromname(nameidvara[i]), dicta.getcodelabelfromname(nameidvara[i]), tempmd);
				else
					dsumaxdist.addnewvar("idvara_"+nameidvara[i], "%2953%: "+dicta.getvarlabelfromname(nameidvara[i]), dicta.getvarformatfromname(nameidvara[i]), tempmd, dicta.getmissingdatafromname(nameidvara[i]));
			}
		}
		if (sidvarb!=null)
		{
			pointerb=idvarb.length;
			for (int i=0; i<nameidvarb.length; i++)
			{
				if (rifrep==1)
					dsu.addnewvar("idvarb_"+nameidvarb[i], "%2954%: "+dictb.getvarlabelfromname(nameidvarb[i]), dictb.getvarformatfromname(nameidvarb[i]), tempmd, tempmd);
				else if (rifrep==0)
					dsu.addnewvar("idvarb_"+nameidvarb[i], "%2954%: "+dictb.getvarlabelfromname(nameidvarb[i]), dictb.getvarformatfromname(nameidvarb[i]), dictb.getcodelabelfromname(nameidvarb[i]), dictb.getmissingdatafromname(nameidvarb[i]));
				else if (rifrep==3)
					dsu.addnewvar("idvarb_"+nameidvarb[i], "%2954%: "+dictb.getvarlabelfromname(nameidvarb[i]), dictb.getvarformatfromname(nameidvarb[i]), dictb.getcodelabelfromname(nameidvarb[i]), tempmd);
				else
					dsu.addnewvar("idvarb_"+nameidvarb[i], "%2954%: "+dictb.getvarlabelfromname(nameidvarb[i]), dictb.getvarformatfromname(nameidvarb[i]), tempmd, dictb.getmissingdatafromname(nameidvarb[i]));
				outvars++;
				if (rifrep==1)
					dsumaxdist.addnewvar("idvarb_"+nameidvarb[i], "%2954%: "+dictb.getvarlabelfromname(nameidvarb[i]), dictb.getvarformatfromname(nameidvarb[i]), tempmd, tempmd);
				else if (rifrep==0)
					dsumaxdist.addnewvar("idvarb_"+nameidvarb[i], "%2954%: "+dictb.getvarlabelfromname(nameidvarb[i]), dictb.getvarformatfromname(nameidvarb[i]), dictb.getcodelabelfromname(nameidvarb[i]), dictb.getmissingdatafromname(nameidvarb[i]));
				else if (rifrep==3)
					dsumaxdist.addnewvar("idvarb_"+nameidvarb[i], "%2954%: "+dictb.getvarlabelfromname(nameidvarb[i]), dictb.getvarformatfromname(nameidvarb[i]), dictb.getcodelabelfromname(nameidvarb[i]), tempmd);
				else
					dsumaxdist.addnewvar("idvarb_"+nameidvarb[i], "%2954%: "+dictb.getvarlabelfromname(nameidvarb[i]), dictb.getvarformatfromname(nameidvarb[i]), tempmd, dictb.getmissingdatafromname(nameidvarb[i]));
			}
		}
		for (int i=0; i<metric.length; i++)
		{
				outvars=outvars+3;
				if (rifrep==1)
				{
					dsu.addnewvar("link"+String.valueOf(i+1)+"a_"+linka[i], "%2955% "+dicta.getvarlabelfromname(linka[i]), dicta.getvarformatfromname(linka[i]), tempmd, tempmd);
					dsu.addnewvar("link"+String.valueOf(i+1)+"b_"+linkb[i], "%2956% "+dictb.getvarlabelfromname(linkb[i]), dictb.getvarformatfromname(linkb[i]), tempmd, tempmd);
					dsumaxdist.addnewvar("link"+String.valueOf(i+1)+"a_"+linka[i], "%2955% "+dicta.getvarlabelfromname(linka[i]), dicta.getvarformatfromname(linka[i]), tempmd, tempmd);
					dsumaxdist.addnewvar("link"+String.valueOf(i+1)+"b_"+linkb[i], "%2956% "+dictb.getvarlabelfromname(linkb[i]), dictb.getvarformatfromname(linkb[i]), tempmd, tempmd);
				}
				else if (rifrep==0)
				{
					dsu.addnewvar("link"+String.valueOf(i+1)+"a_"+linka[i], "%2955% "+dicta.getvarlabelfromname(linka[i]), dicta.getvarformatfromname(linka[i]), dicta.getcodelabelfromname(linka[i]), dicta.getmissingdatafromname(linka[i]));
					dsu.addnewvar("link"+String.valueOf(i+1)+"b_"+linkb[i], "%2956% "+dictb.getvarlabelfromname(linkb[i]), dictb.getvarformatfromname(linkb[i]), dictb.getcodelabelfromname(linkb[i]), dictb.getmissingdatafromname(linkb[i]));
					dsumaxdist.addnewvar("link"+String.valueOf(i+1)+"a_"+linka[i], "%2955% "+dicta.getvarlabelfromname(linka[i]), dicta.getvarformatfromname(linka[i]), dicta.getcodelabelfromname(linka[i]), dicta.getmissingdatafromname(linka[i]));
					dsumaxdist.addnewvar("link"+String.valueOf(i+1)+"b_"+linkb[i], "%2956% "+dictb.getvarlabelfromname(linkb[i]), dictb.getvarformatfromname(linkb[i]), dictb.getcodelabelfromname(linkb[i]), dictb.getmissingdatafromname(linkb[i]));
				}
				else if (rifrep==3)
				{
					dsu.addnewvar("link"+String.valueOf(i+1)+"a_"+linka[i], "%2955% "+dicta.getvarlabelfromname(linka[i]), dicta.getvarformatfromname(linka[i]), dicta.getcodelabelfromname(linka[i]), tempmd);
					dsu.addnewvar("link"+String.valueOf(i+1)+"b_"+linkb[i], "%2956% "+dictb.getvarlabelfromname(linkb[i]), dictb.getvarformatfromname(linkb[i]), dictb.getcodelabelfromname(linkb[i]), tempmd);
					dsumaxdist.addnewvar("link"+String.valueOf(i+1)+"a_"+linka[i], "%2955% "+dicta.getvarlabelfromname(linka[i]), dicta.getvarformatfromname(linka[i]), dicta.getcodelabelfromname(linka[i]), tempmd);
					dsumaxdist.addnewvar("link"+String.valueOf(i+1)+"b_"+linkb[i], "%2956% "+dictb.getvarlabelfromname(linkb[i]), dictb.getvarformatfromname(linkb[i]), dictb.getcodelabelfromname(linkb[i]), tempmd);
				}
				else
				{
					dsu.addnewvar("link"+String.valueOf(i+1)+"a_"+linka[i], "%2955% "+dicta.getvarlabelfromname(linka[i]), dicta.getvarformatfromname(linka[i]), tempmd, dicta.getmissingdatafromname(linka[i]));
					dsu.addnewvar("link"+String.valueOf(i+1)+"b_"+linkb[i], "%2956% "+dictb.getvarlabelfromname(linkb[i]), dictb.getvarformatfromname(linkb[i]), tempmd, dictb.getmissingdatafromname(linkb[i]));
					dsumaxdist.addnewvar("link"+String.valueOf(i+1)+"a_"+linka[i], "%2955% "+dicta.getvarlabelfromname(linka[i]), dicta.getvarformatfromname(linka[i]), tempmd, dicta.getmissingdatafromname(linka[i]));
					dsumaxdist.addnewvar("link"+String.valueOf(i+1)+"b_"+linkb[i], "%2956% "+dictb.getvarlabelfromname(linkb[i]), dictb.getvarformatfromname(linkb[i]), tempmd, dictb.getmissingdatafromname(linkb[i]));
				}
				dsu.addnewvar("distance"+String.valueOf(i+1)+"_"+linka[i]+"_"+linkb[i], "%2957% ("+metricnames[i]+")", Keywords.NUMSuffix, tempmd, tempmd);
				dsumaxdist.addnewvar("distance_"+String.valueOf(i+1)+"_LA_"+linka[i]+"_LB_"+linkb[i]+"_M_"+metricnames[i].toLowerCase(), "%2957%: "+dicta.getvarlabelfromname(linka[i])+","+dictb.getvarlabelfromname(linkb[i])+" ("+metricnames[i]+")", Keywords.NUMSuffix, tempmd, tempmd);
		}
		if (outcp)
		{
			if (!dw.opendatatable(dsu.getfinalvarinfo()))
			{
				ftdsa.deletefile();
				ftdsb.deletefile();
				return new Result(dw.getmessage(), false, null);
			}
		}
		if (!dwmaxdist.opendatatable(dsumaxdist.getfinalvarinfo()))
		{
			ftdsa.deletefile();
			ftdsb.deletefile();
			return new Result(dwmaxdist.getmessage(), false, null);
		}
		int refoutv=0;
		int numbnoa=0;
		int numanob=0;
		double tempda;
		double tempdb;
		int refnummet=0;
		int numra=0;
		int numrb=0;
		if (parameters.get(Keywords.OUT.toLowerCase()+"bnoa")!=null)
		{
			dwbnoa=new DataWriter(parameters, Keywords.OUT.toLowerCase()+"bnoa");
			if (!dwbnoa.getmessage().equals(""))
			{
				ftdsa.deletefile();
				ftdsb.deletefile();
				return new Result(dwbnoa.getmessage(), false, null);
			}
			for (int i=0; i<nameblockvarb.length; i++)
			{
				if (rifrep==1)
					dsubnoa.addnewvar("blockvar_"+nameblockvarb[i], "%2952% "+dictb.getvarlabelfromname(nameblockvarb[i]), Keywords.TEXTSuffix, tempmd, tempmd);
				else if (rifrep==0)
				{
					Hashtable<String, String> tempclb=dictb.getcodelabelfromname(nameblockvarb[i]);
					Hashtable<String, String> tempmdb=dictb.getmissingdatafromname(nameblockvarb[i]);
					dsubnoa.addnewvar("blockvar_"+nameblockvarb[i], "%2952% "+dictb.getvarlabelfromname(nameblockvarb[i]), Keywords.TEXTSuffix, tempclb, tempmdb);
				}
				else if (rifrep==3)
				{
					Hashtable<String, String> tempclb=dictb.getcodelabelfromname(nameblockvarb[i]);
					dsubnoa.addnewvar("blockvar_"+nameblockvarb[i], "%2952% "+dictb.getvarlabelfromname(nameblockvarb[i]), Keywords.TEXTSuffix, tempclb, tempmd);
				}
				else
				{
					Hashtable<String, String> tempmdb=dictb.getmissingdatafromname(nameblockvarb[i]);
					dsubnoa.addnewvar("blockvar_"+nameblockvarb[i], "%2952% "+dictb.getvarlabelfromname(nameblockvarb[i]), Keywords.TEXTSuffix, tempmd, tempmdb);
				}
			}
			int tempointr=0;
			if (sidvarb!=null)
			{
				for (int i=0; i<nameidvarb.length; i++)
				{
					tempointr++;
					if (rifrep==1)
						dsubnoa.addnewvar("idvarb_"+nameidvarb[i], "%2954%: "+dictb.getvarlabelfromname(nameidvarb[i]), dictb.getvarformatfromname(nameidvarb[i]), tempmd, tempmd);
					else if (rifrep==0)
						dsubnoa.addnewvar("idvarb_"+nameidvarb[i], "%2954%: "+dictb.getvarlabelfromname(nameidvarb[i]), dictb.getvarformatfromname(nameidvarb[i]), dictb.getcodelabelfromname(nameidvarb[i]), dictb.getmissingdatafromname(nameidvarb[i]));
					else if (rifrep==3)
						dsubnoa.addnewvar("idvarb_"+nameidvarb[i], "%2954%: "+dictb.getvarlabelfromname(nameidvarb[i]), dictb.getvarformatfromname(nameidvarb[i]), dictb.getcodelabelfromname(nameidvarb[i]), tempmd);
					else
						dsubnoa.addnewvar("idvarb_"+nameidvarb[i], "%2954%: "+dictb.getvarlabelfromname(nameidvarb[i]), dictb.getvarformatfromname(nameidvarb[i]), tempmd, dictb.getmissingdatafromname(nameidvarb[i]));
				}
			}
			for (int i=0; i<metric.length; i++)
			{
				if (rifrep==1)
				{
					dsubnoa.addnewvar("link"+String.valueOf(i+1)+"b_"+linkb[i], "%2956% "+dictb.getvarlabelfromname(linkb[i]), dictb.getvarformatfromname(linkb[i]), tempmd, tempmd);
				}
				else if (rifrep==0)
				{
					dsubnoa.addnewvar("link"+String.valueOf(i+1)+"b_"+linkb[i], "%2956% "+dictb.getvarlabelfromname(linkb[i]), dictb.getvarformatfromname(linkb[i]), dictb.getcodelabelfromname(linkb[i]), dictb.getmissingdatafromname(linkb[i]));
				}
				else if (rifrep==3)
				{
					dsubnoa.addnewvar("link"+String.valueOf(i+1)+"b_"+linkb[i], "%2956% "+dictb.getvarlabelfromname(linkb[i]), dictb.getvarformatfromname(linkb[i]), dictb.getcodelabelfromname(linkb[i]), tempmd);
				}
				else
				{
					dsubnoa.addnewvar("link"+String.valueOf(i+1)+"b_"+linkb[i], "%2956% "+dictb.getvarlabelfromname(linkb[i]), dictb.getvarformatfromname(linkb[i]), tempmd, dictb.getmissingdatafromname(linkb[i]));
				}
			}
			if (!dwbnoa.opendatatable(dsubnoa.getfinalvarinfo()))
			{
				ftdsa.deletefile();
				ftdsb.deletefile();
				return new Result(dwbnoa.getmessage(), false, null);
			}
			String[] outbnoa=new String[nameblockvarb.length+metric.length];
			if (sidvarb!=null)
				outbnoa=new String[nameblockvarb.length+metric.length+nameidvarb.length];
			if (numgv>0)
			{
				for (int i=0; i<nunmgb; i++)
				{
					group=ftdsb.getnextgroup();
					if (ftdsb.isreading()) ftdsb.endread();
					if (group!=null)
					{
						ftdsb.opentoread(group);
						numrb=ftdsb.getrecords();
						if (numrb>0)
						{
							for (int rb=0; rb<numrb; rb++)
							{
								valb=ftdsb.read();
								if (ftdsa.isreading()) ftdsa.endread();
								ftdsa.opentoread(group);
								numra=ftdsa.getrecords();
								if (numra<=0)
								{
									refoutv=0;
									for (int v=0; v<group.length; v++)
									{
										outbnoa[refoutv]=group[v];
										refoutv++;
									}
									if (sidvarb!=null)
									{
										for (int v=0; v<idvarb.length; v++)
										{
											outbnoa[refoutv]=valb[v];
											refoutv++;
										}
									}
									for (int j=0; j<metric.length; j++)
									{
										outbnoa[refoutv]=valb[j+tempointr];
										refoutv++;
									}
									numbnoa++;
									dwbnoa.write(outbnoa);
								}
							}
						}
					}
				}
			}
			if (numbnoa==0)
			{
				result.add(new LocalMessageGetter("%2965%<br>\n"));
				dwbnoa.deletetmp();
			}
			else result.add(new LocalMessageGetter("%2966% ("+numbnoa+")<br>\n"));
			if (ftdsb.isreading()) ftdsb.endread();
			if (ftdsa.isreading()) ftdsa.endread();
			ftdsa.reinitgroups();
			ftdsb.reinitgroups();
		}
		if (parameters.get(Keywords.OUT.toLowerCase()+"anob")!=null)
		{
			dwanob=new DataWriter(parameters, Keywords.OUT.toLowerCase()+"anob");
			if (!dwanob.getmessage().equals(""))
			{
				ftdsa.deletefile();
				ftdsb.deletefile();
				return new Result(dwanob.getmessage(), false, null);
			}
			for (int i=0; i<nameblockvara.length; i++)
			{
				if (rifrep==1)
					dsuanob.addnewvar("blockvar_"+nameblockvara[i], "%2952% "+dicta.getvarlabelfromname(nameblockvara[i]), Keywords.TEXTSuffix, tempmd, tempmd);
				else if (rifrep==0)
				{
					Hashtable<String, String> tempcla=dicta.getcodelabelfromname(nameblockvara[i]);
					Hashtable<String, String> tempmda=dicta.getmissingdatafromname(nameblockvara[i]);
					dsuanob.addnewvar("blockvar_"+nameblockvara[i], "%2952% "+dicta.getvarlabelfromname(nameblockvara[i]), Keywords.TEXTSuffix, tempcla, tempmda);
				}
				else if (rifrep==3)
				{
					Hashtable<String, String> tempcla=dicta.getcodelabelfromname(nameblockvara[i]);
					dsuanob.addnewvar("blockvar_"+nameblockvara[i], "%2952% "+dicta.getvarlabelfromname(nameblockvara[i]), Keywords.TEXTSuffix, tempcla, tempmd);
				}
				else
				{
					Hashtable<String, String> tempmda=dicta.getmissingdatafromname(nameblockvara[i]);
					dsuanob.addnewvar("blockvar_"+nameblockvara[i], "%2952% "+dicta.getvarlabelfromname(nameblockvara[i]), Keywords.TEXTSuffix, tempmd, tempmda);
				}
			}
			int tempointr=0;
			if (sidvara!=null)
			{
				for (int i=0; i<nameidvara.length; i++)
				{
					tempointr++;
					if (rifrep==1)
						dsuanob.addnewvar("idvara_"+nameidvara[i], "%2954%: "+dicta.getvarlabelfromname(nameidvara[i]), dicta.getvarformatfromname(nameidvara[i]), tempmd, tempmd);
					else if (rifrep==0)
						dsuanob.addnewvar("idvara_"+nameidvara[i], "%2954%: "+dicta.getvarlabelfromname(nameidvara[i]), dicta.getvarformatfromname(nameidvara[i]), dicta.getcodelabelfromname(nameidvara[i]), dicta.getmissingdatafromname(nameidvara[i]));
					else if (rifrep==3)
						dsuanob.addnewvar("idvara_"+nameidvara[i], "%2954%: "+dicta.getvarlabelfromname(nameidvara[i]), dicta.getvarformatfromname(nameidvara[i]), dicta.getcodelabelfromname(nameidvara[i]), tempmd);
					else
						dsuanob.addnewvar("idvara_"+nameidvara[i], "%2954%: "+dicta.getvarlabelfromname(nameidvara[i]), dicta.getvarformatfromname(nameidvara[i]), tempmd, dicta.getmissingdatafromname(nameidvara[i]));
				}
			}
			for (int i=0; i<metric.length; i++)
			{
				if (rifrep==1)
				{
					dsuanob.addnewvar("link"+String.valueOf(i+1)+"a_"+linka[i], "%2956% "+dicta.getvarlabelfromname(linka[i]), dicta.getvarformatfromname(linka[i]), tempmd, tempmd);
				}
				else if (rifrep==0)
				{
					dsuanob.addnewvar("link"+String.valueOf(i+1)+"a_"+linka[i], "%2956% "+dicta.getvarlabelfromname(linka[i]), dicta.getvarformatfromname(linka[i]), dicta.getcodelabelfromname(linka[i]), dicta.getmissingdatafromname(linka[i]));
				}
				else if (rifrep==3)
				{
					dsuanob.addnewvar("link"+String.valueOf(i+1)+"a_"+linka[i], "%2956% "+dicta.getvarlabelfromname(linka[i]), dicta.getvarformatfromname(linka[i]), dicta.getcodelabelfromname(linka[i]), tempmd);
				}
				else
				{
					dsuanob.addnewvar("link"+String.valueOf(i+1)+"a_"+linka[i], "%2956% "+dicta.getvarlabelfromname(linka[i]), dicta.getvarformatfromname(linka[i]), tempmd, dicta.getmissingdatafromname(linka[i]));
				}
			}
			if (!dwanob.opendatatable(dsuanob.getfinalvarinfo()))
			{
				ftdsa.deletefile();
				ftdsb.deletefile();
				return new Result(dwanob.getmessage(), false, null);
			}
			String[] outanob=new String[nameblockvara.length+metric.length];
			if (sidvara!=null)
				outanob=new String[nameblockvara.length+metric.length+nameidvara.length];
			if (numgv>0)
			{
				for (int i=0; i<nunmga; i++)
				{
					group=ftdsa.getnextgroup();
					if (ftdsa.isreading()) ftdsa.endread();
					if (group!=null)
					{
						ftdsa.opentoread(group);
						numra=ftdsa.getrecords();
						if (numra>0)
						{
							for (int rb=0; rb<numra; rb++)
							{
								vala=ftdsa.read();
								if (ftdsb.isreading()) ftdsb.endread();
								ftdsb.opentoread(group);
								numrb=ftdsb.getrecords();
								if (numrb<=0)
								{
									refoutv=0;
									for (int v=0; v<group.length; v++)
									{
										outanob[refoutv]=group[v];
										refoutv++;
									}
									if (sidvarb!=null)
									{
										for (int v=0; v<idvarb.length; v++)
										{
											outanob[refoutv]=vala[v];
											refoutv++;
										}
									}
									for (int j=0; j<metric.length; j++)
									{
										outanob[refoutv]=vala[j+tempointr];
										refoutv++;
									}
									numanob++;
									dwanob.write(outanob);
								}
							}
						}
					}
				}
			}
			if (numanob==0)
			{
				result.add(new LocalMessageGetter("%2967%<br>\n"));
				dwanob.deletetmp();
			}
			else result.add(new LocalMessageGetter("%2968% ("+numanob+")<br>\n"));
			if (ftdsb.isreading()) ftdsb.endread();
			if (ftdsa.isreading()) ftdsa.endread();
			ftdsa.reinitgroups();
			ftdsb.reinitgroups();
		}
		String keyword="RLCrossProduct "+dicta.getkeyword()+" "+dictb.getkeyword();
		String description="RLCrossProduct "+dicta.getdescription()+" "+dictb.getdescription();
		String author=(String) parameters.get(Keywords.client_host.toLowerCase());
		String[] outvalues=new String[outvars];
		double[] metmaxdist=new double[metric.length];
		String[] valmaxdist=new String[outvars];
		Keywords.percentage_total=total_record_b;
		Keywords.percentage_done=0;
		if (numgv>0)
		{
			for (int i=0; i<nunmgb; i++)
			{
				group=ftdsb.getnextgroup();
				if (ftdsb.isreading()) ftdsb.endread();
				if (group!=null)
				{
					ftdsb.opentoread(group);
					numrb=ftdsb.getrecords();
					if (numrb>0)
					{
						for (int rb=0; rb<numrb; rb++)
						{
							Keywords.percentage_done++;
							valb=ftdsb.read();
							if (ftdsa.isreading()) ftdsa.endread();
							ftdsa.opentoread(group);
							numra=ftdsa.getrecords();
							if (numra>0)
							{
								for (int m=0; m<metric.length; m++)
								{
									metmaxdist[m]=-1.7976931348623157E308;
								}
								for (int ra=0; ra<numra; ra++)
								{
									vala=ftdsa.read();
									refoutv=0;
									if (sblockvara!=null)
									{
										for (int v=0; v<group.length; v++)
										{
											outvalues[refoutv]=group[v];
											valmaxdist[refoutv]=outvalues[refoutv];
											refoutv++;
										}
									}
									if (sidvara!=null)
									{
										for (int v=0; v<idvara.length; v++)
										{
											outvalues[refoutv]=vala[v];
											valmaxdist[refoutv]=outvalues[refoutv];
											refoutv++;
										}
									}
									if (sidvarb!=null)
									{
										for (int v=0; v<idvarb.length; v++)
										{
											outvalues[refoutv]=valb[v];
											valmaxdist[refoutv]=outvalues[refoutv];
											refoutv++;
										}
									}
									refnummet=0;
									for (int j=0; j<metric.length; j++)
									{
										outvalues[refoutv]=vala[j+pointera];
										refoutv++;
										outvalues[refoutv]=valb[j+pointerb];
										refoutv++;
										distances=Double.NaN;
										if ((!valb[j+pointerb].equals("")) && (!vala[j+pointera].equals("")))
										{
											if (metric[j]<16)
											{
												if (!considerpartofb) distances=sd.getDistance(valb[j+pointerb], vala[j+pointera], metric[j]);
												else distances=sd.getDistanceForParts(valb[j+pointerb], vala[j+pointera], metric[j]);
											}
											else if (metric[j]==16)
											{
												refnummet++;
												try
												{
													tempdb=Double.parseDouble(valb[j+pointerb]);
													tempda=Double.parseDouble(vala[j+pointera]);
													if ((!Double.isNaN(tempdb)) && (!Double.isNaN(tempda)))
													{
														if (tempdb==tempda) distances=1;
														else distances=1-(Math.sqrt(Math.pow(tempdb-tempda,2)))/(refmaxed[refnummet]);
													}
												}
												catch (Exception tab) {}
											}
											else if (metric[j]==17)
											{
												refnummet++;
												try
												{
													tempdb=Double.parseDouble(valb[j+pointerb]);
													tempda=Double.parseDouble(vala[j+pointera]);
													if ((!Double.isNaN(tempdb)) && (!Double.isNaN(tempda)))
													{
														if (tempdb==tempda) distances=1;
														else distances=1-(Math.abs(tempdb-tempda))/(refmaxad[refnummet]);
													}
												}
												catch (Exception tab) {}
											}
											else if (metric[j]==18)
											{
												distances=0;
												if (valb[j+pointerb].equals(vala[j+pointera])) distances=1;
											}
											else
											{
												distances=0;
												if (valb[j+pointerb].equalsIgnoreCase(vala[j+pointera])) distances=1;
											}
										}
										if (Double.isNaN(distances)) outvalues[refoutv]="0.000000001";
										else if (distances<0.000000001) outvalues[refoutv]="0.000000001";
										else if (distances>1) outvalues[refoutv]="1";
										else outvalues[refoutv]=String.valueOf(distances);
										refoutv++;
										if (distances>metmaxdist[j])
										{
											valmaxdist[refoutv-3]=outvalues[refoutv-3];
											valmaxdist[refoutv-2]=outvalues[refoutv-2];
											valmaxdist[refoutv-1]=outvalues[refoutv-1];
											metmaxdist[j]=distances;
										}
									}
									if (outcp) dw.write(outvalues);
								}
								dwmaxdist.write(valmaxdist);
							}
						}
					}
				}
			}
			if (ftdsb.isreading()) ftdsb.endread();
			if (ftdsa.isreading()) ftdsa.endread();
			ftdsa.deletefile();
			ftdsb.deletefile();
		}
		else
		{
			ftdsb.opentoread(null);
			numrb=ftdsb.getrecords();
			if (numrb>0)
			{
				for (int rb=0; rb<numrb; rb++)
				{
					Keywords.percentage_done++;
					valb=ftdsb.read();
					if (ftdsa.isreading()) ftdsa.endread();
					ftdsa.opentoread(null);
					numra=ftdsa.getrecords();
					if (numra>0)
					{
						for (int m=0; m<metric.length; m++)
						{
							metmaxdist[m]=-1000000000.0;
						}
						for (int ra=0; ra<numra; ra++)
						{
							vala=ftdsa.read();
							refoutv=0;
							if (sidvara!=null)
							{
								for (int v=0; v<idvara.length; v++)
								{
									outvalues[refoutv]=vala[v];
									valmaxdist[refoutv]=outvalues[refoutv];
									refoutv++;
								}
							}
							if (sidvarb!=null)
							{
								for (int v=0; v<idvarb.length; v++)
								{
									outvalues[refoutv]=valb[v];
									valmaxdist[refoutv]=outvalues[refoutv];
									refoutv++;
								}
							}
							refnummet=0;
							for (int j=0; j<metric.length; j++)
							{
								outvalues[refoutv]=vala[j+pointera];
								refoutv++;
								outvalues[refoutv]=valb[j+pointerb];
								refoutv++;
								distances=Double.NaN;
								if ((!valb[j+pointerb].equals("")) && (!vala[j+pointera].equals("")))
								{
									if (metric[j]<16) distances=sd.getDistance(vala[j+pointera], valb[j+pointerb], metric[j]);
									else if (metric[j]==16)
									{
										refnummet++;
										try
										{
											tempdb=Double.parseDouble(valb[j+pointerb]);
											tempda=Double.parseDouble(vala[j+pointera]);
											if ((!Double.isNaN(tempdb)) && (!Double.isNaN(tempda)))
											{
												if (tempdb==tempda) distances=1;
												else distances=1-(Math.sqrt(Math.pow(tempdb-tempda,2)))/(refmaxed[refnummet]);
											}
										}
										catch (Exception tab) {}
									}
									else if (metric[j]==17)
									{
										refnummet++;
										try
										{
											tempdb=Double.parseDouble(valb[j+pointerb]);
											tempda=Double.parseDouble(vala[j+pointera]);
											if ((!Double.isNaN(tempdb)) && (!Double.isNaN(tempda)))
											{
												if (tempdb==tempda) distances=1;
												else distances=1-(Math.abs(tempdb-tempda))/(refmaxad[refnummet]);
											}
										}
										catch (Exception tab) {}
									}
									else if (metric[j]==18)
									{
										distances=0;
										if (valb[j+pointerb].equals(vala[j+pointera])) distances=1;
									}
									else
									{
										distances=0;
										if (valb[j+pointerb].equalsIgnoreCase(vala[j+pointera])) distances=1;
									}
								}
								if (Double.isNaN(distances)) outvalues[refoutv]="0.000000001";
								else if (distances<0.000000001) outvalues[refoutv]="0.000000001";
								else if (distances>1) outvalues[refoutv]="1";
								else outvalues[refoutv]=String.valueOf(distances);
								refoutv++;
								if (distances>metmaxdist[j])
								{
									valmaxdist[refoutv-1]=outvalues[refoutv-1];
									valmaxdist[refoutv-2]=outvalues[refoutv-2];
									valmaxdist[refoutv-3]=outvalues[refoutv-3];
									metmaxdist[j]=distances;
								}
							}
							if (outcp) dw.write(outvalues);
						}
						dwmaxdist.write(valmaxdist);
					}
				}
			}
			if (ftdsb.isreading()) ftdsb.endread();
			if (ftdsa.isreading()) ftdsa.endread();
			ftdsa.deletefile();
			ftdsb.deletefile();
		}
		Keywords.percentage_total=0;
		Keywords.percentage_done=0;
		if (outcp)
		{
			boolean resclose=dw.close();
			if (!resclose)
				return new Result(dw.getmessage(), false, null);
			Vector<Hashtable<String, String>> tablevariableinfo=dw.getVarInfo();
			Hashtable<String, String> datatableinfo=dw.getTableInfo();
			result.add(new LocalDictionaryWriter(dw.getdictpath(), keyword, description, author, dw.gettabletype(),
			datatableinfo, dsu.getfinalvarinfo(), tablevariableinfo, dsu.getfinalcl(), dsu.getfinalmd(), null));
		}
		boolean resclosemaxdist=dwmaxdist.close();
		if (!resclosemaxdist)
			return new Result(dwmaxdist.getmessage(), false, null);
		Vector<Hashtable<String, String>> tablevariableinfomaxdist=dwmaxdist.getVarInfo();
		Hashtable<String, String> datatableinfomaxdist=dwmaxdist.getTableInfo();
		result.add(new LocalDictionaryWriter(dwmaxdist.getdictpath(), keyword, description, author, dwmaxdist.gettabletype(),
		datatableinfomaxdist, dsumaxdist.getfinalvarinfo(), tablevariableinfomaxdist, dsumaxdist.getfinalcl(), dsumaxdist.getfinalmd(), null));
		if (numanob>0)
		{
			boolean rescloseanob=dwanob.close();
			if (!rescloseanob)
				return new Result(dwanob.getmessage(), false, null);
			Vector<Hashtable<String, String>> tablevariableinfoanob=dwanob.getVarInfo();
			Hashtable<String, String> datatableinfoanob=dwanob.getTableInfo();
			result.add(new LocalDictionaryWriter(dwanob.getdictpath(), keyword, description, author, dwanob.gettabletype(),
			datatableinfoanob, dsuanob.getfinalvarinfo(), tablevariableinfoanob, dsuanob.getfinalcl(), dsuanob.getfinalmd(), null));
		}
		if (numbnoa>0)
		{
			boolean resclosebnoa=dwbnoa.close();
			if (!resclosebnoa)
				return new Result(dwbnoa.getmessage(), false, null);
			Vector<Hashtable<String, String>> tablevariableinfobnoa=dwbnoa.getVarInfo();
			Hashtable<String, String> datatableinfobnoa=dwbnoa.getTableInfo();
			result.add(new LocalDictionaryWriter(dwbnoa.getdictpath(), keyword, description, author, dwbnoa.gettabletype(),
			datatableinfobnoa, dsubnoa.getfinalvarinfo(), tablevariableinfobnoa, dsubnoa.getfinalcl(), dsubnoa.getfinalmd(), null));
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
		String[] depa =new String[1];
		String[] depb =new String[1];
		parameters.add(new GetRequiredParameters(Keywords.dict+"a=", "dict", true, 2921, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.dict+"b=", "dict", true, 2922, dep, "", 1));
		parameters.add(new GetRequiredParameters("", "note", false, 2958, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.OUT.toLowerCase()+"maxdist=", "setting=out", true, 2961, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.OUT.toLowerCase()+"crossp=", "setting=out", false, 2923, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.OUT.toLowerCase()+"anob=", "setting=out", false, 2959, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.OUT.toLowerCase()+"bnoa=", "setting=out", false, 2960, dep, "", 1));
		parameters.add(new GetRequiredParameters("", "note", false, 2962, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.viewout, "checkbox", false, 1612, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.link, "multipletext", true, 2924, dep, "", 2));
		parameters.add(new GetRequiredParameters("", "note", false, 2925, dep, "", 2));
		depa[0]=Keywords.dict+"a";
		parameters.add(new GetRequiredParameters(Keywords.idvar+"a", "vars=all", false, 2926, depa, "", 2));
		depb[0]=Keywords.dict+"b";
		parameters.add(new GetRequiredParameters(Keywords.idvar+"b", "vars=all", false, 2927, depb, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.blockvar+"a", "vars=all", false, 2928, depa, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.blockvar+"b", "vars=all", false, 2929, depb, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.where,"text", false, 2805,dep,"",2));
		parameters.add(new GetRequiredParameters(Keywords.considerpartofb,"checkbox", false, 3049,dep,"",2));
		parameters.add(new GetRequiredParameters(Keywords.replace, "listsingle=530_NULL,531_"+Keywords.replaceall+",532_"+Keywords.replaceformat+",533_"+Keywords.replacemissing,false, 534, dep, "", 2));
		return parameters;
	}
	/**
	*Returns the group and the name of the procedure
	*/
	public String[] getstepinfo()
	{
		String[] retprocinfo=new String[2];
		retprocinfo[0]="2919";
		retprocinfo[1]="2920";
		return retprocinfo;
	}
}
