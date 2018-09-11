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
* This is the procedure that evaluates the distances between docs by using the results of the lexical correspondence analysis
* @author marco.scarno@gmail.com
* @version 1.0.0, rev.: 14/02/17 by marco
*/
public class ProcEvaldistforprojdocs implements RunStep
{
	/**
	*Evaluate the frequencies of each word
	*/
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		Vector<StepResult> result = new Vector<StepResult>();
		String[] requiredparameters = new String[] {Keywords.dict+"resca", Keywords.dict+"projwords", Keywords.dict+"refdocs", Keywords.dict+"newdocs", Keywords.varwordsrefdocs, Keywords.varrefdocs, Keywords.varwordsnewdocs, Keywords.varnewdocs, Keywords.OUT.toLowerCase()};
		String[] optionalparameters = new String[] {Keywords.OUT.toLowerCase()+"proj", Keywords.ncomp, Keywords.replace};
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

		boolean isdwp=false;
		DataWriter dwp=null;
		if (parameters.get(Keywords.OUT.toLowerCase()+"proj")!=null)
		{
			isdwp=true;
			dwp=new DataWriter(parameters, Keywords.OUT.toLowerCase()+"proj");
			if (!dwp.getmessage().equals(""))
			{
				return new Result(dwp.getmessage(), false, null);
			}
		}

		String tempncomp =(String)parameters.get(Keywords.ncomp);
		DictionaryReader dictresca = (DictionaryReader)parameters.get(Keywords.dict+"resca");
		String tempname="";
		String[] tempnames=null;
		int ok=0;
		int numaxes=0;
		int tempint=0;
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
		int ncomp=numaxes;
		if (tempncomp!=null)
		{
			ncomp=-1;
			try
			{
				ncomp=Integer.parseInt(tempncomp);
			}
			catch (Exception e){}
		}
		if (ncomp<0 || ncomp>numaxes)
		{
			return new Result("%3650% ("+String.valueOf(numaxes)+")<br>\n", false, null);
		}

		double[] eigenvalues=new double[numaxes];
		String[] vartoask=new String[numaxes+1];
		int[] repcond=new int[numaxes+1];
		vartoask[0]="parameter";
		repcond[0]=0;
		for (int i=0; i<numaxes; i++)
		{
			vartoask[i+1]="val_"+String.valueOf(i+1);
			repcond[i+1]=0;
		}
		int realnumeigen=0;
		String[] values=null;
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
					for (int i=0; i<numaxes; i++)
					{
						eigenvalues[i]=Double.parseDouble(values[i+1]);
						if (eigenvalues[i]>0.00001) realnumeigen=i+1;
					}
				}
			}
		}
		if (realnumeigen<ncomp)
		{
			ncomp=realnumeigen;
			result.add(new LocalMessageGetter("%3689% ("+String.valueOf(realnumeigen)+")<br>\n"));
		}
		Keywords.percentage_done=1;
		dataresca.close();
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
		if (anumaxes!=numaxes) return new Result("%3652%<br>\n", false, null);
		TreeMap<String, double[]> projections=new TreeMap<String, double[]>();
		DataReader dataprojwords = new DataReader(dictprojwords);
		if (!dataprojwords.open(vartoask, repcond, false))
			return new Result(dataprojwords.getmessage(), false, null);
		while (!dataprojwords.isLast())
		{
			values = dataprojwords.getRecord();
			if (values!=null)
			{
				double[] tempvalue=new double[numaxes];
				for (int i=0; i<numaxes; i++)
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
		DataReader datarefdocs = new DataReader(dictrefdocs);
		if (!datarefdocs.open(null, rifrep, false))
			return new Result(datarefdocs.getmessage(), false, null);
		double[] ttval=null;
		while (!datarefdocs.isLast())
		{
			values = datarefdocs.getRecord();
			if (values!=null)
			{
				tempname=values[posword];
				if (projections.get(tempname)!=null)
				{
					double[] tval=new double[pos_docs.length];
					for (int i=0; i<tval.length; i++)
					{
						tval[i]=0.0;
						if (!values[pos_docs[i]].equals(""))
						try
						{
							tval[i]=Double.parseDouble(values[pos_docs[i]]);
							totalwrd[i]=totalwrd[i]+tval[i];
						}
						catch (Exception etvc){}
					}
					if (freqwrd.get(tempname)!=null)
					{
						ttval=freqwrd.get(tempname);
						for (int i=0; i<ttval.length; i++)
						{
							tval[i]=ttval[i]+tval[i];
						}
					}
					freqwrd.put(tempname, tval);
				}
			}
		}
		datarefdocs.close();
		Keywords.percentage_done=3;

		String tempvarnewdocs=(String)parameters.get(Keywords.varnewdocs);
		String varwordsnewdocs=(String)parameters.get(Keywords.varwordsnewdocs);
		String[] varnewdocs=varwordsnewdocs.split(" ");
		if (varnewdocs.length!=1)
		{
			return new Result("%3656%<br>\n", false, null);
		}
		varnewdocs=tempvarnewdocs.split(" ");
		String[] labelnewdocs=new String[varnewdocs.length];
		posword=-1;
		pos_docs=new int[varnewdocs.length];
		DictionaryReader dictnewdocs = (DictionaryReader)parameters.get(Keywords.dict+"newdocs");
		for (int i=0; i<dictnewdocs.gettotalvar(); i++)
		{
			tempname=dictnewdocs.getvarname(i);
			if (tempname.equalsIgnoreCase(varwordsnewdocs)) posword=i;
		}
		if (posword==-1) return new Result("%3657% ("+varwordsnewdocs+")<br>\n", false, null);
		for (int j=0; j<varnewdocs.length; j++)
		{
			pos_docs[j]=-1;
			for (int i=0; i<dictnewdocs.gettotalvar(); i++)
			{
				tempname=dictnewdocs.getvarname(i);
				if (tempname.equalsIgnoreCase(varnewdocs[j]))
				{
					labelnewdocs[j]=dictnewdocs.getvarlabel(i);
					pos_docs[j]=i;
				}
			}
		}
		nev="";
		for (int i=0; i<pos_docs.length; i++)
		{
			if (pos_docs[i]==-1) nev=nev+varnewdocs[i]+" ";
		}
		if (!nev.equals(""))
		{
			return new Result("%3658% ("+nev.trim()+")<br>\n", false, null);
		}
		TreeMap<String, double[]> freqwnd=new TreeMap<String, double[]>();
		double[] totalwnd=new double[pos_docs.length];
		for (int i=0; i<pos_docs.length; i++)
		{
			totalwnd[i]=0.0;
		}
		DataReader datanewdocs = new DataReader(dictnewdocs);
		if (!datanewdocs.open(null, rifrep, false))
		{
			return new Result(datanewdocs.getmessage(), false, null);
		}
		while (!datanewdocs.isLast())
		{
			values = datanewdocs.getRecord();
			if (values!=null)
			{
				tempname=values[posword];
				if (projections.get(tempname)!=null)
				{
					double[] tval=new double[pos_docs.length];
					for (int i=0; i<tval.length; i++)
					{
						tval[i]=0.0;
						if (!values[pos_docs[i]].equals(""))
						try
						{
							tval[i]=Double.parseDouble(values[pos_docs[i]]);
							totalwnd[i]=totalwnd[i]+tval[i];
						}
						catch (Exception etvc){}
					}
					if (freqwnd.get(tempname)!=null)
					{
						ttval=freqwnd.get(tempname);
						for (int i=0; i<ttval.length; i++)
						{
							tval[i]=ttval[i]+tval[i];
						}
					}
					freqwnd.put(tempname, tval);
				}
			}
		}
		datanewdocs.close();
		Keywords.percentage_done=4;
		if (freqwrd.size()==0)
		{
			Keywords.procedure_error=true;
			return new Result("%3659%<br>\n", false, null);
		}
		if (freqwnd.size()==0)
		{
			Keywords.procedure_error=true;
			return new Result("%3660%<br>\n", false, null);
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
		Vector<double[]> def_proj_new=new Vector<double[]>();
		for (int i=0; i<varnewdocs.length; i++)
		{
			double[] temp=new double[ncomp];
			for (int j=0; j<ncomp; j++)
			{
				temp[j]=0.0;
			}
			def_proj_new.add(temp);
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
		for (Iterator<String> f = freqwnd.keySet().iterator(); f.hasNext();)
		{
			String tgroup=f.next();
			double[] tempval=freqwnd.get(tgroup);
			double[] tempvalproj=projections.get(tgroup);
			for (int i=0; i<tempval.length; i++)
			{
				double[] temp=def_proj_new.get(i);
				tempval[i]=tempval[i]/totalwnd[i];
				for (int j=0; j<temp.length; j++)
				{
					temp[j]=temp[j]+tempval[i]*tempvalproj[j];
				}
			}
		}
		Vector<Integer> excluded_ref=new Vector<Integer>();
		Vector<Integer> excluded_new=new Vector<Integer>();
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
		for (int i=0; i<varnewdocs.length; i++)
		{
			double[] temp=def_proj_new.get(i);
			excdoc=false;
			for (int j=0; j<ncomp; j++)
			{
				temp[j]=temp[j]*(1/Math.sqrt(eigenvalues[j]));
				if (Double.isNaN(temp[j])) excdoc=true;
			}
			if (excdoc) excluded_new.add(new Integer(i));
		}
		if (excluded_ref.size()==varrefdocs.length)
		{
			return new Result("%3690%<br>\n", false, null);
		}
		if (excluded_new.size()==varnewdocs.length)
		{
			return new Result("%3691%<br>\n", false, null);
		}

		Hashtable<String, String> tempmd=new Hashtable<String, String>();
		String dwp_keyword="Supplementary projection"+dictrefdocs.getkeyword()+" "+dictnewdocs.getkeyword();
		String dwp_description="Supplementary projection"+dictrefdocs.getdescription()+" "+dictnewdocs.getdescription();
		String author=(String) parameters.get(Keywords.client_host.toLowerCase());
		String keyword="Distance "+dictrefdocs.getkeyword()+" "+dictnewdocs.getkeyword();
		String description="Distance "+dictrefdocs.getdescription()+" "+dictnewdocs.getdescription();
		DataSetUtilities dsup=new DataSetUtilities();

		if (isdwp)
		{
			Hashtable<String, String> cldp=new Hashtable<String, String>();
			cldp.put("1", "%3662%");
			cldp.put("2", "%3663%");
			dsup.addnewvar("doc_type", "%3661%", Keywords.TEXTSuffix, cldp, tempmd);
			dsup.addnewvar("doc_name", "%3664%", Keywords.TEXTSuffix, tempmd, tempmd);
			dsup.addnewvar("doc_label", "%3665%", Keywords.TEXTSuffix, tempmd, tempmd);
			for (int i=0; i<ncomp; i++)
			{
				dsup.addnewvar("val_"+String.valueOf(i+1), "%3581%: "+String.valueOf(i+1), Keywords.NUMSuffix, tempmd, tempmd);
			}
			if (!dwp.opendatatable(dsup.getfinalvarinfo()))
			{
				Keywords.procedure_error=true;
				return new Result(dwp.getmessage(), false, null);
			}
			String[] outpv=new String[3+ncomp];
			for (int i=0; i<varrefdocs.length; i++)
			{
				outpv[0]="1";
				outpv[1]=varrefdocs[i];
				outpv[2]=labelrefdocs[i];
				double[] temp=def_proj_ref.get(i);
				if (!excluded_ref.contains(new Integer(i)))
				{
					for (int j=0; j<ncomp; j++)
					{
						outpv[3+j]=String.valueOf(temp[j]);
					}
					dwp.write(outpv);
				}
			}
			for (int i=0; i<varnewdocs.length; i++)
			{
				outpv[0]="2";
				outpv[1]=varnewdocs[i];
				outpv[2]=labelnewdocs[i];
				double[] temp=def_proj_new.get(i);
				if (!excluded_new.contains(new Integer(i)))
				{
					for (int j=0; j<ncomp; j++)
					{
						outpv[3+j]=String.valueOf(temp[j]);
					}
					dwp.write(outpv);
				}
			}
		}
		DataSetUtilities dsu=new DataSetUtilities();
		dsu.addnewvar("doc_name", "%3663%", Keywords.TEXTSuffix, tempmd, tempmd);
		dsu.addnewvar("doc_label", "%3677%", Keywords.TEXTSuffix, tempmd, tempmd);
		dsu.addnewvar("ref_doc_name", "%3662%", Keywords.TEXTSuffix, tempmd, tempmd);
		dsu.addnewvar("ref_doc_label", "%3678%", Keywords.TEXTSuffix, tempmd, tempmd);
		dsu.addnewvar("similarity", "%3679%", Keywords.NUMSuffix, tempmd, tempmd);
		Hashtable<String, String> cldms=new Hashtable<String, String>();
		cldms.put("1", "%3587%");
		cldms.put("0", "%3588%");
		dsu.addnewvar("most_similar", "%3680%", Keywords.TEXTSuffix, cldms, tempmd);
		if (!dw.opendatatable(dsu.getfinalvarinfo()))
		{
			Keywords.procedure_error=true;
			if (isdwp) dwp.deletetmp();
			return new Result(dw.getmessage(), false, null);
		}
		String[] outv=new String[6];
		double sumsq_new=0;
		double sumsq_ref=0;
		double numerator=0;
		int ref_is_sim=0;
		for (int i=0; i<varnewdocs.length; i++)
		{
			if (!excluded_new.contains(new Integer(i)))
			{
				outv[0]=varnewdocs[i];
				outv[1]=labelnewdocs[i];
				outv[5]="0";
				double[] proj_new=def_proj_new.get(i);
				sumsq_new=0.0;
				for (int j=0; j<ncomp; j++)
				{
					sumsq_new=sumsq_new+Math.pow(proj_new[j],2);
				}
				TreeMap<Double, String[]> orderdist=new TreeMap<Double, String[]>();
				for (int j=0; j<varrefdocs.length; j++)
				{
					if (!excluded_ref.contains(new Integer(j)))
					{
						double[] proj_ref=def_proj_ref.get(j);
						sumsq_ref=0.0;
						numerator=0.0;
						for (int k=0; k<ncomp; k++)
						{
							sumsq_ref=sumsq_ref+Math.pow(proj_ref[k],2);
							numerator=numerator+proj_new[k]*proj_ref[k];
						}
						String[] ref_info=new String[2];
						ref_info[0]=varrefdocs[j];
						ref_info[1]=labelrefdocs[j];
						orderdist.put(new Double(numerator/( (Math.sqrt(sumsq_new))*(Math.sqrt(sumsq_ref)) )), ref_info);
					}
				}
				ref_is_sim=0;
				for (Iterator<Double> fd = orderdist.keySet().iterator(); fd.hasNext();)
				{
					ref_is_sim++;
					double di=(fd.next()).doubleValue();
					String[] ref_info=orderdist.get(new Double(di));
					outv[2]=ref_info[0];
					outv[3]=ref_info[1];
					outv[4]=String.valueOf(di);
					if (ref_is_sim==orderdist.size()) outv[5]="1";
					dw.write(outv);
				}
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
		if (excluded_new.size()>0)
		{
			for (int i=0; i<excluded_new.size(); i++)
			{
				tinte=(excluded_new.get(i)).intValue();
				result.add(new LocalMessageGetter("%3693%: "+varnewdocs[tinte]+"\n"));
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
		if (isdwp)
		{
			resclose = dwp.close();
			if (!resclose)
				return new Result(dwp.getmessage(), false, null);
			Vector<Hashtable<String, String>> tablevariableinfop = dwp.getVarInfo();
			Hashtable<String, String> datatableinfop = dwp.getTableInfo();
			result.add(new LocalDictionaryWriter(dwp.getdictpath(), dwp_keyword, dwp_description, author,
			dwp.gettabletype(), datatableinfop, dsup.getfinalvarinfo(), tablevariableinfop, dsup.getfinalcl(),dsup.getfinalmd(), null));
		}
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
		parameters.add(new GetRequiredParameters(Keywords.dict+"refdocs=", "dict", true, 3668, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.dict+"newdocs=", "dict", true, 3669, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.OUT.toLowerCase()+"=", "setting=out", true, 3670, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.OUT.toLowerCase()+"proj=", "setting=out", false, 3671, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.viewout, "checkbox", false, 1612, dep, "", 1));
		depr[0]=Keywords.dict+"refdocs";
		parameters.add(new GetRequiredParameters(Keywords.varwordsrefdocs, "var=all", true, 3672, depr, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.varrefdocs, "vars=all", true, 3673, depr, "", 2));
		depn[0]=Keywords.dict+"newdocs";
		parameters.add(new GetRequiredParameters(Keywords.varwordsnewdocs, "var=all", true, 3674, depn, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.varnewdocs, "vars=all", true, 3675, depn, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.ncomp, "text", false, 3676, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.replace, "listsingle=530_NULL,531_"+Keywords.replaceall+",532_"+Keywords.replaceformat+",533_"+Keywords.replacemissing,false, 534, dep, "", 2));
		return parameters;
	}
	/**
	*Return the name of the procedure and the group to which belongs
	*/
	public String[] getstepinfo() {
		String[] info=new String[2];
		info[0]="4169";
		info[1]="3648";
		return info;
	}
}
