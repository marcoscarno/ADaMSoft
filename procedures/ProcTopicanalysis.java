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

import java.util.TreeMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Vector;
import java.io.*;

import ADaMSoft.dataaccess.DictionaryReader;
import ADaMSoft.dataaccess.DataReader;
import ADaMSoft.dataaccess.DataWriter;
import ADaMSoft.keywords.Keywords;
import ADaMSoft.utilities.GetRequiredParameters;
import ADaMSoft.utilities.DataSetUtilities;
import ADaMSoft.utilities.StepUtilities;

import ADaMSoft.dataaccess.FastTempDataSet;


/**
* This is the procedure that evaluates the topics by means of the results of a previous lexical correspondence analysis
* @author marco.scarno@gmail.com
* @version 1.0.0, rev.: 23/06/17 by marco
*/
public class ProcTopicanalysis implements RunStep
{
	/**
	*Evaluate the frequencies of each word
	*/
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		Vector<StepResult> result = new Vector<StepResult>();
		String[] requiredparameters = new String[] {Keywords.dict+"projwords", Keywords.dict+"refdocs", Keywords.varwordsrefdocs, Keywords.varrefdocs, Keywords.OUT.toLowerCase()+"projtopics", Keywords.OUT.toLowerCase()+"wordstopics", Keywords.ngroup};
		String[] optionalparameters = new String[] {Keywords.ncomp, Keywords.tolerance, Keywords.replace};
		Keywords.percentage_total=5;
		Keywords.percentage_done=0;
		StepUtilities steputilities=new StepUtilities();
		if (!steputilities.checkParameters(requiredparameters, optionalparameters, parameters))
		{
			return new Result(steputilities.getMessage(), false, null);
		}
		String replace =(String)parameters.get(Keywords.replace);
		DataWriter dwp=new DataWriter(parameters, Keywords.OUT.toLowerCase()+"projtopics");
		if (!dwp.getmessage().equals(""))
		{
			return new Result(dwp.getmessage(), false, null);
		}
		DataWriter dww=new DataWriter(parameters, Keywords.OUT.toLowerCase()+"wordstopics");
		if (!dww.getmessage().equals(""))
		{
			return new Result(dww.getmessage(), false, null);
		}
		String stolerance =(String)parameters.get(Keywords.tolerance);
		double tolerance=0.00000001;
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
		String tempngroup =(String)parameters.get(Keywords.ngroup);
		int nseeds=0;
		try
		{
			nseeds=Integer.parseInt(tempngroup);
		}
		catch (Exception e){}
		if (nseeds==0)
			return new Result("%875%<br>\n", false, null);
		DictionaryReader dictprojwords = (DictionaryReader)parameters.get(Keywords.dict+"projwords");
		int ok=0;
		int anumaxes=0;
		String tempname="";
		int tempint=0;
		String[] tempnames;
		try
		{
			for (int i=0; i<dictprojwords.gettotalvar(); i++)
			{
				tempname=dictprojwords.getvarname(i);
				tempname=tempname.toLowerCase();
				if (tempname.equalsIgnoreCase("word")) ok++;
				if (tempname.equalsIgnoreCase("contr_axe")) ok++;
				if (tempname.indexOf("val_")>=0)
				{
					ok++;
					tempnames=tempname.split("_");
					tempint=Integer.parseInt(tempnames[1]);
					if (tempint>anumaxes) anumaxes=tempint;
				}
			}
		}
		catch (Exception ed)
		{
			return new Result("%3651%<br>\n", false, null);
		}
		if (ok!=anumaxes+2) return new Result("%3651%<br>\n", false, null);

		String tempncomp =(String)parameters.get(Keywords.ncomp);
		int ncomp=0;
		if (tempncomp!=null)
		{
			ncomp=-1;
			try
			{
				ncomp=Integer.parseInt(tempncomp);
			}
			catch (Exception e){}
		}
		if (ncomp<0 || ncomp>anumaxes)
		{
			return new Result("%4144% ("+String.valueOf(tempncomp)+")<br>\n", false, null);
		}
		if (ncomp>0) anumaxes=ncomp;

		TreeMap<String, double[]> projections=new TreeMap<String, double[]>();
		TreeMap<String, Double> contributions=new TreeMap<String, Double>();
		String[] vartoask=new String[anumaxes+2];
		int[] repcond=new int[anumaxes+2];
		vartoask[0]="word";
		repcond[0]=0;
		vartoask[1]="contr_axe";
		repcond[1]=0;
		for (int i=0; i<anumaxes; i++)
		{
			vartoask[i+2]="val_"+String.valueOf(i+1);
			repcond[i+2]=0;
		}
		DataReader dataprojwords = new DataReader(dictprojwords);
		if (!dataprojwords.open(vartoask, repcond, false))
			return new Result(dataprojwords.getmessage(), false, null);
		double cont=0;
		String[] values;
		while (!dataprojwords.isLast())
		{
			values = dataprojwords.getRecord();
			if (values!=null)
			{
				double[] tempvalue=new double[anumaxes];
				for (int i=0; i<anumaxes; i++)
				{
					try
					{
						tempvalue[i]=Double.parseDouble(values[i+2]);
					}
					catch (Exception epj){}
				}
				cont=0;
				try
				{
					cont=Double.parseDouble(values[1]);
				}
				catch (Exception epj){}
				projections.put(values[0], tempvalue);
				contributions.put(values[0], new Double(cont));
			}
		}
		dataprojwords.close();
		Keywords.percentage_done=1;
		String tempvarrefdocs=(String)parameters.get(Keywords.varrefdocs);
		String varwordsrefdocs=(String)parameters.get(Keywords.varwordsrefdocs);
		String[] varrefdocs=varwordsrefdocs.split(" ");
		if (varrefdocs.length!=1)
		{
			return new Result("%3653%<br>\n", false, null);
		}
		varrefdocs=tempvarrefdocs.split(" ");
		String[] labelrefdocs=new String[varrefdocs.length];
		int posword=-1;
		int[] pos_docs=new int[varrefdocs.length];
		DictionaryReader dictrefdocs = (DictionaryReader)parameters.get(Keywords.dict+"refdocs");
		for (int i=0; i<dictrefdocs.gettotalvar(); i++)
		{
			tempname=dictrefdocs.getvarname(i);
			if (tempname.equalsIgnoreCase(varwordsrefdocs)) posword=i;
		}
		if (posword==-1) return new Result("%3654% ("+varwordsrefdocs+")<br>\n", false, null);
		for (int j=0; j<varrefdocs.length; j++)
		{
			pos_docs[j]=-1;
			for (int i=0; i<dictrefdocs.gettotalvar(); i++)
			{
				tempname=dictrefdocs.getvarname(i);
				if (tempname.equalsIgnoreCase(varrefdocs[j]))
				{
					labelrefdocs[j]=dictrefdocs.getvarlabel(i);
					pos_docs[j]=i;
				}
			}
		}
		String nev="";
		for (int i=0; i<pos_docs.length; i++)
		{
			if (pos_docs[i]==-1) nev=nev+varrefdocs[i]+" ";
		}
		if (!nev.equals(""))
		{
			return new Result("%3655% ("+nev.trim()+")<br>\n", false, null);
		}
		if (nseeds>pos_docs.length-1)
		{
			return new Result("%4091%<br>\n", false, null);
		}
		TreeMap<String, Double> freqwrd=new TreeMap<String, Double>();
		int rifrep=0;
		if (replace==null)
			rifrep=0;
		else if (replace.equalsIgnoreCase(Keywords.replaceall))
			rifrep=1;
		else if (replace.equalsIgnoreCase(Keywords.replaceformat))
			rifrep=2;
		else if (replace.equalsIgnoreCase(Keywords.replacemissing))
			rifrep=3;
		DataReader datarefdocs = new DataReader(dictrefdocs);
		if (!datarefdocs.open(null, rifrep, false))
			return new Result(datarefdocs.getmessage(), false, null);
		double gran_total=0;
		double tval=0.0;
		while (!datarefdocs.isLast())
		{
			values = datarefdocs.getRecord();
			if (values!=null)
			{
				tempname=values[posword];
				if (projections.get(tempname)!=null)
				{
					tval=0.0;
					for (int i=0; i<pos_docs.length; i++)
					{
						if (!values[pos_docs[i]].equals(""))
						{
							try
							{
								tval+=Double.parseDouble(values[pos_docs[i]]);
							}
							catch (Exception etvc){}
						}
					}
					gran_total=gran_total+tval;
					if (tval>0) freqwrd.put(tempname, new Double(tval));
					else projections.remove(tempname);
				}
				else projections.remove(tempname);
			}
		}
		datarefdocs.close();
		double[] temp_val;
		Keywords.percentage_done=2;
		Vector<double[]> seeds=new Vector<double[]>();
		int ref_g=0;
		int ref_diff_0=anumaxes;
		if (anumaxes>2) ref_diff_0=anumaxes/2;
		int curr_0=0;
		for (Iterator<String> f = projections.keySet().iterator(); f.hasNext();)
		{
			String tgroup=f.next();
			if (freqwrd.get(tgroup)==null) projections.remove(tgroup);
			else
			{
				if (ref_g==0)
				{
					curr_0=0;
					temp_val=projections.get(tgroup);
					double[] temp_seed=new double[anumaxes];
					for (int j=0; j<anumaxes; j++)
					{
						if (Math.abs(temp_val[j])<tolerance) curr_0++;
						temp_seed[j]=temp_val[j];
					}
					if (curr_0<ref_diff_0)
					{
						seeds.add(temp_seed);
						ref_g++;
					}
				}
			}
		}
		if (seeds.size()==0)
		{
			ref_g=0;
			for (Iterator<String> f = projections.keySet().iterator(); f.hasNext();)
			{
				String tgroup=f.next();
				if (ref_g==0)
				{
					temp_val=projections.get(tgroup);
					double[] temp_seed=new double[anumaxes];
					for (int j=0; j<anumaxes; j++)
					{
						temp_seed[j]=temp_val[j];
					}
					seeds.add(temp_seed);
					ref_g++;
					break;
				}
			}
		}
		double[] temp_proj;
		double actual_dist_seeds=0;
		double ref_max_dist=0;
		double ccdist=0;
		for (int i=1; i<nseeds; i++)
		{
			ref_max_dist=0;
			for (Iterator<String> f = projections.keySet().iterator(); f.hasNext();)
			{
				String tgroup=f.next();
				temp_proj=projections.get(tgroup);
				actual_dist_seeds=0;
				for (int s=0; s<i; s++)
				{
					double[] temp_seed=seeds.get(s);
					ccdist=0.0;
					for (int j=0; j<anumaxes; j++)
					{
						ccdist=ccdist+Math.pow( (temp_seed[j]-temp_proj[j]),2);
					}
					actual_dist_seeds=actual_dist_seeds+ccdist;
				}
				if (actual_dist_seeds>ref_max_dist)
				{
					ref_max_dist=actual_dist_seeds;
					double[] temp_seed=new double[anumaxes];
					for (int j=0; j<anumaxes; j++)
					{
						temp_seed[j]=temp_proj[j];
					}
					if (seeds.size()-1<i)
					{
						seeds.add(temp_seed);
					}
					else
					{
						seeds.set(i, temp_seed);
					}
				}
			}
		}
		Keywords.percentage_done=3;
		double mvardist=1;
		Vector<double[]> new_seeds=new Vector<double[]>();
		int ref_rec=0;
		double[] coeff=new double[nseeds];
		int[] nwords=new int[nseeds];
		double temp_mvardist=0;
		double actual_mvardist=0;
		double distance=0;
		int real_it=0;
		int real_seeds_found=0;
		double max_dist=0;
		int ref_seed_max=0;
		int num_words_seed=0;
		Vector<Integer> wseed=new Vector<Integer>();
		TreeMap<Integer, Integer> ref_found_seed=new TreeMap<Integer, Integer>();
		boolean not_added=false;
		Vector<double[]> temp_assigned=new Vector<double[]>();
		while (mvardist>tolerance)
		{
			real_it++;
			new_seeds.clear();
			for (int i=0; i<nseeds; i++)
			{
				coeff[i]=0;
				nwords[i]=0;
				double[] temp_seed=new double[anumaxes];
				for (int j=0; j<anumaxes; j++)
				{
					temp_seed[j]=0.0;
				}
				new_seeds.add(temp_seed);
			}
			for (Iterator<String> f = projections.keySet().iterator(); f.hasNext();)
			{
				String tgroup=f.next();
				temp_val=projections.get(tgroup);
				tval=(freqwrd.get(tgroup)).doubleValue();
				max_dist=Double.MAX_VALUE;
				ref_rec=0;
				for (int i=0; i<nseeds; i++)
				{
					double[] temps=seeds.get(i);
					distance=0;
					for (int j=0; j<temps.length; j++)
					{
						distance+=Math.pow((temps[j]-temp_val[j]),2);
					}
					if (distance<max_dist)
					{
						ref_rec=i;
						max_dist=distance;
					}
				}
				double[] add_seed=new_seeds.get(ref_rec);
				coeff[ref_rec]=coeff[ref_rec]+tval;
				for (int i=0; i<anumaxes; i++)
				{
					add_seed[i]=add_seed[i]+temp_val[i]*tval;
				}
				new_seeds.set(ref_rec, add_seed);
			}
			temp_mvardist=0;
			for (int i=0; i<nseeds; i++)
			{
				actual_mvardist=0;
				double[] temp_seed=new_seeds.get(i);
				double[] old_seeds=seeds.get(i);
				if (coeff[i]>0)
				{
					for (int j=0; j<anumaxes; j++)
					{
						temp_seed[j]=temp_seed[j]/coeff[i];
						actual_mvardist=actual_mvardist+Math.pow((temp_seed[j]-old_seeds[j]),2);
					}
					actual_mvardist=actual_mvardist/anumaxes;
					temp_mvardist=temp_mvardist+actual_mvardist;
				}
			}
			seeds.clear();
			for (int i=0; i<nseeds; i++)
			{
				double[] temp_seed=new double[anumaxes];
				double[] old_seeds=new_seeds.get(i);
				for (int j=0; j<anumaxes; j++)
				{
					temp_seed[j]=old_seeds[j];
				}
				if (coeff[i]>0) seeds.add(temp_seed);
			}
			mvardist=temp_mvardist/nseeds;
			if (seeds.size()<nseeds)
			{
				wseed.clear();
				for (int i=0; i<seeds.size(); i++)
				{
					wseed.add(new Integer(0));
				}
				if (real_seeds_found==0) real_seeds_found=seeds.size();
				for (Iterator<String> f = projections.keySet().iterator(); f.hasNext();)
				{
					String tgroup=f.next();
					temp_val=projections.get(tgroup);
					max_dist=Double.MAX_VALUE;
					ref_rec=0;
					for (int i=0; i<seeds.size(); i++)
					{
						double[] temps=seeds.get(i);
						distance=0;
						for (int j=0; j<temps.length; j++)
						{
							distance+=Math.pow((temps[j]-temp_val[j]),2);
						}
						if (distance<max_dist)
						{
							ref_rec=i;
							max_dist=distance;
						}
					}
					ref_seed_max=(wseed.get(ref_rec)).intValue();
					wseed.set(ref_rec, new Integer(ref_seed_max+1));
				}
				ref_found_seed.clear();
				for (int i=0; i<wseed.size(); i++)
				{
					ref_found_seed.put(wseed.get(i), new Integer(i));
				}
				ref_seed_max=0;
				for (Iterator<Integer> fw = ref_found_seed.keySet().iterator(); fw.hasNext();)
				{
					num_words_seed=(fw.next()).intValue();
					ref_seed_max=(ref_found_seed.get(new Integer(num_words_seed))).intValue();
				}
				if (num_words_seed>(nseeds-seeds.size()))
				{
					temp_assigned.clear();
					for (Iterator<String> f = projections.keySet().iterator(); f.hasNext();)
					{
						String tgroup=f.next();
						temp_val=projections.get(tgroup);
						max_dist=Double.MAX_VALUE;
						ref_rec=0;
						for (int i=0; i<seeds.size(); i++)
						{
							double[] temps=seeds.get(i);
							distance=0;
							for (int j=0; j<temps.length; j++)
							{
								distance+=Math.pow((temps[j]-temp_val[j]),2);
							}
							if (distance<max_dist)
							{
								ref_rec=i;
								max_dist=distance;
							}
						}
						if (ref_rec==ref_seed_max)
						{
							double[] ttemp=new double[anumaxes];
							for (int i=0; i<anumaxes; i++)
							{
								ttemp[i]=temp_val[i];
							}
							temp_assigned.add(ttemp);
						}
					}
					seeds.remove(ref_seed_max);
					new_seeds.clear();
					double[] tfs=temp_assigned.get(0);
					double[] ttfs=new double[tfs.length];
					for (int i=0; i<tfs.length; i++)
					{
						ttfs[i]=tfs[i];
					}
					new_seeds.add(ttfs);
					int misseed=nseeds-seeds.size();
					for (int i=1; i<misseed; i++)
					{
						ref_max_dist=0;
						for (int j=0; j<temp_assigned.size(); j++)
						{
							temp_proj=temp_assigned.get(j);
							actual_dist_seeds=0;
							for (int s=0; s<i; s++)
							{
								double[] temp_seed=new_seeds.get(s);
								ccdist=0.0;
								for (int h=0; h<anumaxes; h++)
								{
									ccdist=ccdist+Math.pow( (temp_seed[h]-temp_proj[h]),2);
								}
								actual_dist_seeds=actual_dist_seeds+ccdist;
							}
							if (actual_dist_seeds>ref_max_dist)
							{
								ref_max_dist=actual_dist_seeds;
								double[] temp_seed=new double[anumaxes];
								for (int h=0; h<anumaxes; h++)
								{
									temp_seed[h]=temp_proj[h];
								}
								if (new_seeds.size()-1<i)
								{
									new_seeds.add(temp_seed);
								}
								else
								{
									new_seeds.set(i, temp_seed);
								}
							}
						}
					}
					for (int i=0; i<new_seeds.size(); i++)
					{
						tfs=new_seeds.get(i);
						double[] tttfs=new double[tfs.length];
						for (int j=0; j<tfs.length; j++)
						{
							tttfs[j]=tfs[j];
						}
						seeds.add(tttfs);
					}
					mvardist=1;
				}
				else
				{
					not_added=true;
					nseeds=seeds.size();
					coeff=new double[nseeds];
					nwords=new int[nseeds];
				}
			}
		}
		result.add(new LocalMessageGetter("%4098% ("+String.valueOf(real_it)+")<br>\n"));
		if (real_seeds_found!=0)
			result.add(new LocalMessageGetter("%4145% ("+real_seeds_found+")<br>\n"));
		if (not_added)
			result.add(new LocalMessageGetter("%4146%<br>\n"));
		if (real_seeds_found!=0 && !not_added)
			result.add(new LocalMessageGetter("%4147%<br>\n"));

		Hashtable<String, String> tempmd=new Hashtable<String, String>();
		Hashtable<String, String> tempcl=new Hashtable<String, String>();
		Keywords.percentage_done=4;

		DataSetUtilities dsuw=new DataSetUtilities();
		dsuw.addnewvar("word", "%3582%", Keywords.TEXTSuffix, tempcl, tempmd);
		dsuw.addnewvar("topic", "%4092%", Keywords.TEXTSuffix, tempcl, tempmd);
		dsuw.addnewvar("contr_axe", "%3591%", Keywords.NUMSuffix, tempcl, tempmd);
		dsuw.addnewvar("freq_words", "%4101%", Keywords.NUMSuffix, tempcl, tempmd);
		if (!dww.opendatatable(dsuw.getfinalvarinfo()))
		{
			return new Result(dww.getmessage(), false, null);
		}
		String[] outvalues=new String[4];
		for (int i=0; i<nseeds; i++)
		{
			nwords[i]=0;
			coeff[i]=0;
		}
		String tempdir=(String)parameters.get(Keywords.WorkDir);
		FastTempDataSet tempftd=new FastTempDataSet(tempdir);
		for (Iterator<String> f = projections.keySet().iterator(); f.hasNext();)
		{
			String tgroup=f.next();
			outvalues[2]=tgroup;
			temp_val=projections.get(tgroup);
			tval=(freqwrd.get(tgroup)).doubleValue();
			max_dist=Double.MAX_VALUE;
			ref_rec=0;
			for (int i=0; i<nseeds; i++)
			{
				double[] temps=seeds.get(i);
				distance=0;
				for (int j=0; j<temps.length; j++)
				{
					distance+=Math.pow((temps[j]-temp_val[j]),2);
				}
				if (distance<max_dist)
				{
					ref_rec=i;
					max_dist=distance;
				}
			}
			nwords[ref_rec]=nwords[ref_rec]+1;
			coeff[ref_rec]=coeff[ref_rec]+tval;
			outvalues[1]=String.valueOf(ref_rec+1);
			outvalues[3]=String.valueOf(tval);
			tval=(contributions.get(tgroup)).doubleValue();
			outvalues[0]=String.valueOf(tval);
			tempftd.write(outvalues);
		}
		//tempftd.first_sort_num();
		tempftd.reverse_num();
		tempftd.sortwith(1,1,4);
		tempftd.openSortedFile();
		boolean itera_records=true;
		Object[] current=null;
		while (itera_records)
		{
			current=tempftd.readSortedRecord();
			if (current==null) itera_records=false;
			else
			{
				outvalues[0]=current[2].toString();
				outvalues[1]=current[1].toString();
				outvalues[2]=current[0].toString();
				outvalues[3]=current[3].toString();
				dww.write(outvalues);
			}
		}
		tempftd.closeSortedFile();
		tempftd.deletefile();

		DataSetUtilities dsup=new DataSetUtilities();
		dsup.addnewvar("topic", "%4092%", Keywords.TEXTSuffix, tempcl, tempmd);
		for (int i=0; i<anumaxes; i++)
		{
			dsup.addnewvar("val_"+String.valueOf(i+1), "%3581%: "+String.valueOf(i+1), Keywords.NUMSuffix, tempmd, tempmd);
		}
		dsup.addnewvar("num_words", "%4099%", Keywords.NUMSuffix, tempcl, tempmd);
		dsup.addnewvar("weight_words", "%4100%", Keywords.NUMSuffix, tempcl, tempmd);
		if (!dwp.opendatatable(dsup.getfinalvarinfo()))
		{
			return new Result(dwp.getmessage(), false, null);
		}
		outvalues=new String[anumaxes+3];
		for (int i=0; i<seeds.size(); i++)
		{
			outvalues[0]=String.valueOf(i+1);
			double[] temp_seeds=seeds.get(i);
			for (int j=0; j<anumaxes; j++)
			{
				outvalues[1+j]=String.valueOf(temp_seeds[j]);
			}
			outvalues[anumaxes+1]=String.valueOf(nwords[i]);
			outvalues[anumaxes+2]=String.valueOf(coeff[i]);
			dwp.write(outvalues);
		}
		String keyword="Topic "+dictprojwords.getkeyword();
		String description="Topic "+dictprojwords.getdescription();
		String author=(String)parameters.get(Keywords.client_host.toLowerCase());

		Keywords.percentage_done=0;
		Keywords.percentage_total=0;

		boolean resclose = false;
		resclose = dwp.close();
		if (!resclose)
			return new Result(dwp.getmessage(), false, null);

		resclose = dww.close();
		if (!resclose)
			return new Result(dww.getmessage(), false, null);

		Vector<Hashtable<String, String>> tablevariableinfo = dwp.getVarInfo();
		Hashtable<String, String> datatableinfo = dwp.getTableInfo();
		result.add(new LocalDictionaryWriter(dwp.getdictpath(), keyword, description, author,
		dwp.gettabletype(), datatableinfo, dsup.getfinalvarinfo(), tablevariableinfo, dsup.getfinalcl(), dsup.getfinalmd(), null));

		Vector<Hashtable<String, String>> tablevariableinfow = dww.getVarInfo();
		Hashtable<String, String> datatableinfow = dww.getTableInfo();
		result.add(new LocalDictionaryWriter(dww.getdictpath(), keyword, description, author,
		dww.gettabletype(), datatableinfow, dsuw.getfinalvarinfo(), tablevariableinfow, dsuw.getfinalcl(), dsuw.getfinalmd(), null));
		return new Result("", true, result);
	}
	/**
	*Returns the parameters for the procedure
	*/
	public LinkedList<GetRequiredParameters> getparameters() {
		LinkedList<GetRequiredParameters> parameters=new LinkedList<GetRequiredParameters>();
		String[] dep ={""};
		String[] depr ={""};
		parameters.add(new GetRequiredParameters(Keywords.dict+"refdocs=", "dict", true, 4132, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.dict+"projwords=", "dict", true, 3667, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.OUT.toLowerCase()+"projtopics=", "setting=out", true, 4093, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.OUT.toLowerCase()+"wordstopics=", "setting=out", true, 4094, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.viewout, "checkbox", false, 1612, dep, "", 1));
		depr[0]=Keywords.dict+"refdocs";
		parameters.add(new GetRequiredParameters(Keywords.varwordsrefdocs, "var=all", true, 3672, depr, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.varrefdocs, "vars=all", true, 3673, depr, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.ngroup, "text", true, 4095, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.tolerance, "text", false, 4096, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.ncomp, "text", false, 4143, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.replace, "listsingle=530_NULL,531_"+Keywords.replaceall+",532_"+Keywords.replaceformat+",533_"+Keywords.replacemissing,false, 534, dep, "", 2));
		return parameters;
	}
	/**
	*Return the name of the procedure and the group to which belongs
	*/
	public String[] getstepinfo() {
		String[] info=new String[2];
		info[0]="4169";
		info[1]="4097";
		return info;
	}
}
