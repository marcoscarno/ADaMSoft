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

import ADaMSoft.dataaccess.DictionaryReader;
import ADaMSoft.dataaccess.DataReader;
import ADaMSoft.dataaccess.DataWriter;
import ADaMSoft.keywords.Keywords;
import ADaMSoft.utilities.GetRequiredParameters;
import ADaMSoft.utilities.DataSetUtilities;
import ADaMSoft.utilities.StepUtilities;


/**
* This is the procedure that evaluates the distances between docs and topics already assigned
* @author marco.scarno@gmail.com
* @version 1.0.0, rev.: 24/0/17 by marco
*/
public class ProcTopicassign implements RunStep
{
	/**
	*Evaluate the frequencies of each word
	*/
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		Vector<StepResult> result = new Vector<StepResult>();
		String[] requiredparameters = new String[] {Keywords.dict+"resca", Keywords.dict+"projwords", Keywords.dict+"docs", Keywords.dict+"topics", Keywords.varwordsdocs, Keywords.varrefdocs, Keywords.OUT.toLowerCase()};
		String[] optionalparameters = new String[] {Keywords.replace};
		Keywords.percentage_total=0;
		Keywords.percentage_done=0;
		StepUtilities steputilities=new StepUtilities();
		if (!steputilities.checkParameters(requiredparameters, optionalparameters, parameters))
		{
			return new Result(steputilities.getMessage(), false, null);
		}
		Keywords.percentage_total=5;
		Keywords.percentage_done=0;
		String replace =(String)parameters.get(Keywords.replace);
		DataWriter dw=new DataWriter(parameters, Keywords.OUT.toLowerCase());
		if (!dw.getmessage().equals(""))
		{
			return new Result(dw.getmessage(), false, null);
		}
		String tempname="";
		String[] tempnames=null;
		int ok=0;
		int numaxes=0;
		int tempint=0;
		int tnumaxes=0;
		DictionaryReader dicttopics = (DictionaryReader)parameters.get(Keywords.dict+"topics");
		for (int i=0; i<dicttopics.gettotalvar(); i++)
		{
			tempname=dicttopics.getvarname(i);
			tempname=tempname.toLowerCase();
			if (tempname.equalsIgnoreCase("topic")) ok++;
			if (tempname.indexOf("val_")>=0)
			{
				ok++;
				tempnames=tempname.split("_");
				tempint=Integer.parseInt(tempnames[1]);
				if (tempint>tnumaxes) tnumaxes=tempint;
			}
		}
		if (ok!=tnumaxes+1) return new Result("%4133%<br>\n", false, null);
		String[] vartoask=new String[tnumaxes+1];
		int[] repcond=new int[tnumaxes+1];
		vartoask[0]="topic";
		repcond[0]=0;
		for (int i=0; i<tnumaxes; i++)
		{
			vartoask[i+1]="val_"+String.valueOf(i+1);
			repcond[i+1]=0;
		}
		TreeMap<String, double[]> topic_proj=new TreeMap<String, double[]>();
		String[] values=null;
		int num_diff_0=0;
		DataReader datatopics = new DataReader(dicttopics);
		if (!datatopics.open(vartoask, repcond, false))
			return new Result(datatopics.getmessage(), false, null);
		while (!datatopics.isLast())
		{
			values = datatopics.getRecord();
			if (values!=null)
			{
				num_diff_0=0;
				double[] tempvalue=new double[tnumaxes];
				for (int i=0; i<tnumaxes; i++)
				{
					if (!values[i+1].equals(""))
					{
						num_diff_0++;
						try
						{
							tempvalue[i]=Double.parseDouble(values[i+1]);
						}
						catch (Exception epj){}
					}
				}
				if (num_diff_0==tnumaxes) topic_proj.put(values[0], tempvalue);
			}
		}
		datatopics.close();
		ok=0;
		DictionaryReader dictresca = (DictionaryReader)parameters.get(Keywords.dict+"resca");
		try
		{
			for (int i=0; i<dictresca.gettotalvar(); i++)
			{
				tempname=dictresca.getvarname(i);
				tempname=tempname.toLowerCase();
				if (tempname.equalsIgnoreCase("parameter")) ok++;
				if (tempname.indexOf("val_")>=0)
				{
					ok++;
					tempnames=tempname.split("_");
					tempint=Integer.parseInt(tempnames[1]);
					if (tempint>numaxes) numaxes=tempint;
				}
			}
		}
		catch (Exception ed)
		{
			return new Result("%3649%<br>\n", false, null);
		}
		if (ok!=numaxes+1) return new Result("%3649%<br>\n", false, null);
		if (numaxes<tnumaxes) return new Result("%4148%<br>\n", false, null);
		int ncomp=tnumaxes;
		double[] eigenvalues=new double[tnumaxes];
		vartoask[0]="parameter";
		DataReader dataresca = new DataReader(dictresca);
		if (!dataresca.open(vartoask, repcond, false))
			return new Result(dataresca.getmessage(), false, null);
		while (!dataresca.isLast())
		{
			values = dataresca.getRecord();
			if (values!=null)
			{
				if (values[0].equals("1"))
				{
					for (int i=0; i<tnumaxes; i++)
					{
						eigenvalues[i]=Double.parseDouble(values[i+1]);
					}
				}
			}
		}
		dataresca.close();
		Keywords.percentage_done=1;
		vartoask[0]="word";
		DictionaryReader dictprojwords = (DictionaryReader)parameters.get(Keywords.dict+"projwords");
		ok=0;
		int anumaxes=0;
		try
		{
			for (int i=0; i<dictprojwords.gettotalvar(); i++)
			{
				tempname=dictprojwords.getvarname(i);
				tempname=tempname.toLowerCase();
				if (tempname.equalsIgnoreCase("word")) ok++;
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
		if (ok!=anumaxes+1) return new Result("%3651%<br>\n", false, null);
		if (anumaxes<tnumaxes) return new Result("%4149%<br>\n", false, null);
		TreeMap<String, double[]> projections=new TreeMap<String, double[]>();
		DataReader dataprojwords = new DataReader(dictprojwords);
		if (!dataprojwords.open(vartoask, repcond, false))
			return new Result(dataprojwords.getmessage(), false, null);
		while (!dataprojwords.isLast())
		{
			values = dataprojwords.getRecord();
			if (values!=null)
			{
				double[] tempvalue=new double[tnumaxes];
				for (int i=0; i<tnumaxes; i++)
				{
					try
					{
						tempvalue[i]=Double.parseDouble(values[i+1]);
					}
					catch (Exception epj){}
				}
				projections.put(values[0], tempvalue);
			}
		}
		dataprojwords.close();
		Keywords.percentage_done=2;
		String tempvarrefdocs=(String)parameters.get(Keywords.varrefdocs);
		String varwordsrefdocs=(String)parameters.get(Keywords.varwordsdocs);
		String[] varrefdocs=varwordsrefdocs.split(" ");
		if (varrefdocs.length!=1)
		{
			return new Result("%3653%<br>\n", false, null);
		}
		varrefdocs=tempvarrefdocs.split(" ");
		String[] labelrefdocs=new String[varrefdocs.length];
		int posword=-1;
		int[] pos_docs=new int[varrefdocs.length];
		DictionaryReader dictrefdocs = (DictionaryReader)parameters.get(Keywords.dict+"docs");
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
		double[] totalwrd=new double[pos_docs.length];
		for (int i=0; i<pos_docs.length; i++)
		{
			totalwrd[i]=0.0;
		}
		TreeMap<String, double[]> freqwrd=new TreeMap<String, double[]>();
		int rifrep=0;
		if (replace==null)
			rifrep=0;
		else if (replace.equalsIgnoreCase(Keywords.replaceall))
			rifrep=1;
		else if (replace.equalsIgnoreCase(Keywords.replaceformat))
			rifrep=2;
		else if (replace.equalsIgnoreCase(Keywords.replacemissing))
			rifrep=3;
		double[] tval=new double[pos_docs.length];
		DataReader datarefdocs = new DataReader(dictrefdocs);
		if (!datarefdocs.open(null, rifrep, false))
			return new Result(datarefdocs.getmessage(), false, null);
		while (!datarefdocs.isLast())
		{
			values = datarefdocs.getRecord();
			if (values!=null)
			{
				tempname=values[posword];
				if (projections.get(tempname)!=null)
				{
					for (int i=0; i<tval.length; i++)
					{
						tval[i]=0.0;
						if (!values[pos_docs[i]].equals(""))
						{
							try
							{
								tval[i]=Double.parseDouble(values[pos_docs[i]]);
								totalwrd[i]=totalwrd[i]+tval[i];
							}
							catch (Exception etvc){}
						}
					}
					if (freqwrd.get(tempname)!=null)
					{
						double[] ttval=freqwrd.get(tempname);
						for (int i=0; i<ttval.length; i++)
						{
							ttval[i]=ttval[i]+tval[i];
						}
						freqwrd.put(tempname, ttval);
					}
					else
					{
						double[] ttval=new double[pos_docs.length];
						for (int i=0; i<ttval.length; i++)
						{
							ttval[i]=tval[i];
						}
						freqwrd.put(tempname, ttval);
					}
				}
			}
		}
		datarefdocs.close();
		Keywords.percentage_done=3;
		if (freqwrd.size()==0)
		{
			Keywords.procedure_error=true;
			return new Result("%3659%<br>\n", false, null);
		}
		Vector<double[]> def_proj_ref=new Vector<double[]>();
		for (int i=0; i<varrefdocs.length; i++)
		{
			double[] temp=new double[ncomp];
			for (int j=0; j<ncomp; j++)
			{
				temp[j]=0.0;
			}
			def_proj_ref.add(temp);
		}
		for (Iterator<String> f = freqwrd.keySet().iterator(); f.hasNext();)
		{
			String tgroup=f.next();
			double[] tempval=freqwrd.get(tgroup);
			double[] tempvalproj=projections.get(tgroup);
			for (int i=0; i<tempval.length; i++)
			{
				double[] temp=def_proj_ref.get(i);
				tempval[i]=tempval[i]/totalwrd[i];
				for (int j=0; j<temp.length; j++)
				{
					temp[j]=temp[j]+tempval[i]*tempvalproj[j];
				}
			}
		}
		Vector<Integer> excluded_ref=new Vector<Integer>();
		boolean excdoc=false;
		for (int i=0; i<varrefdocs.length; i++)
		{
			excdoc=false;
			double[] temp=def_proj_ref.get(i);
			for (int j=0; j<ncomp; j++)
			{
				temp[j]=temp[j]*(1/Math.sqrt(eigenvalues[j]));
				if (Double.isNaN(temp[j])) excdoc=true;
			}
			if (excdoc) excluded_ref.add(new Integer(i));
		}
		if (excluded_ref.size()==varrefdocs.length)
		{
			return new Result("%3690%<br>\n", false, null);
		}
		String author=(String) parameters.get(Keywords.client_host.toLowerCase());
		String keyword="Topic assing "+dictrefdocs.getkeyword();
		String description="Topic assing "+dictrefdocs.getdescription();
		DataSetUtilities dsu=new DataSetUtilities();
		Hashtable<String, String> tempmd=new Hashtable<String, String>();
		dsu.addnewvar("doc_name", "%3663%", Keywords.TEXTSuffix, tempmd, tempmd);
		dsu.addnewvar("doc_label", "%3677%", Keywords.TEXTSuffix, tempmd, tempmd);
		dsu.addnewvar("topic", "%4135%", Keywords.TEXTSuffix, tempmd, tempmd);
		dsu.addnewvar("similarity_distance", "%4136%", Keywords.NUMSuffix, tempmd, tempmd);
		if (!dw.opendatatable(dsu.getfinalvarinfo()))
		{
			Keywords.procedure_error=true;
			return new Result(dw.getmessage(), false, null);
		}
		String[] outv=new String[4];
		double sumsq_new=0;
		double sumsq_ref=0;
		double numerator=0;
		double distance=0;
		double ref_distance=0;
		int ref_is_sim=0;
		int projections_0=0;
		for (int i=0; i<varrefdocs.length; i++)
		{
			if (!excluded_ref.contains(new Integer(i)))
			{
				outv[0]=varrefdocs[i];
				outv[1]=labelrefdocs[i];
				double[] proj_ref=def_proj_ref.get(i);
				sumsq_new=0.0;
				projections_0=0;
				for (int j=0; j<ncomp; j++)
				{
					if (proj_ref[j]==0) projections_0++;
					sumsq_new=sumsq_new+Math.pow(proj_ref[j],2);
				}
				ref_distance=-1;
				if (projections_0==ncomp)
				{
					ref_distance=1000;
					for (Iterator<String> to = topic_proj.keySet().iterator(); to.hasNext();)
					{
						String ref_top=to.next();
						double[] temp_cp=topic_proj.get(ref_top);
						sumsq_ref=0.0;
						for (int k=0; k<ncomp; k++)
						{
							sumsq_ref=sumsq_ref+Math.abs(temp_cp[k]);
						}
						if (sumsq_ref<ref_distance)
						{
							outv[2]=ref_top;
							outv[3]=String.valueOf(1-distance);
							ref_distance=sumsq_ref;
						}
					}
				}
				else
				{
					for (Iterator<String> to = topic_proj.keySet().iterator(); to.hasNext();)
					{
						String ref_top=to.next();
						double[] temp_cp=topic_proj.get(ref_top);
						sumsq_ref=0.0;
						numerator=0.0;
						for (int k=0; k<ncomp; k++)
						{
							if (temp_cp[k]!=0)
							{
								sumsq_ref=sumsq_ref+Math.pow(temp_cp[k],2);
								numerator=numerator+proj_ref[k]*temp_cp[k];
							}
							else
							{
								sumsq_ref=sumsq_ref+Math.pow(0.000001,2);
								numerator=numerator+proj_ref[k]*0.000001;
							}
						}
						distance=numerator/(Math.sqrt(sumsq_new)*Math.sqrt(sumsq_ref));
						if (distance>ref_distance)
						{
							outv[2]=ref_top;
							outv[3]=String.valueOf(distance);
							ref_distance=distance;
						}
					}
				}
				dw.write(outv);
			}
		}
		int tinte=0;
		if (excluded_ref.size()>0)
		{
			for (int i=0; i<excluded_ref.size(); i++)
			{
				tinte=(excluded_ref.get(i)).intValue();
				result.add(new LocalMessageGetter("%3692%: "+varrefdocs[tinte]+"\n"));
			}
		}
		Keywords.percentage_done=0;
		Keywords.percentage_total=0;
		boolean resclose = false;
		resclose = dw.close();
		if (!resclose)
			return new Result(dw.getmessage(), false, null);
		Vector<Hashtable<String, String>> tablevariableinfo = dw.getVarInfo();
		Hashtable<String, String> datatableinfo = dw.getTableInfo();
		result.add(new LocalDictionaryWriter(dw.getdictpath(), keyword, description, author,
		dw.gettabletype(), datatableinfo, dsu.getfinalvarinfo(), tablevariableinfo, dsu.getfinalcl(),dsu.getfinalmd(), null));
		return new Result("", true, result);
	}
	/**
	*Returns the parameters for the procedure
	*/
	public LinkedList<GetRequiredParameters> getparameters() {
		LinkedList<GetRequiredParameters> parameters=new LinkedList<GetRequiredParameters>();
		String[] dep ={""};
		String[] depr ={""};
		String[] depn ={""};
		parameters.add(new GetRequiredParameters(Keywords.dict+"resca=", "dict", true, 3666, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.dict+"projwords=", "dict", true, 3667, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.dict+"topics=", "dict", true, 4137, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.dict+"docs=", "dict", true, 4138, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.OUT.toLowerCase()+"=", "setting=out", true, 4139, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.viewout, "checkbox", false, 1612, dep, "", 1));
		depr[0]=Keywords.dict+"docs";
		parameters.add(new GetRequiredParameters(Keywords.varwordsdocs, "var=all", true, 4140, depr, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.varrefdocs, "vars=all", true, 4141, depr, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.replace, "listsingle=530_NULL,531_"+Keywords.replaceall+",532_"+Keywords.replaceformat+",533_"+Keywords.replacemissing,false, 534, dep, "", 2));
		return parameters;
	}
	/**
	*Return the name of the procedure and the group to which belongs
	*/
	public String[] getstepinfo() {
		String[] info=new String[2];
		info[0]="4169";
		info[1]="4142";
		return info;
	}
}
