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

import ADaMSoft.dataaccess.DictionaryReader;
import ADaMSoft.dataaccess.DataReader;
import ADaMSoft.dataaccess.DataWriter;
import ADaMSoft.utilities.MatrixSort;
import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.linalg.EigenvalueDecomposition;

import ADaMSoft.utilities.StepUtilities;
import ADaMSoft.utilities.GetRequiredParameters;
import ADaMSoft.utilities.DataSetUtilities;
import ADaMSoft.utilities.ObjectTransformer;

import ADaMSoft.keywords.Keywords;

import java.util.Hashtable;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.Vector;
import java.util.Iterator;
import java.util.TreeMap;

import java.io.*;

/**
* This is the procedure that implements a the lexical correspondence analysis
* @author marco.scarno@gmail.com
* @date 23/06/2017
*/
public class ProcLexicalca extends ObjectTransformer implements RunStep
{
	/**
	* Starts the execution of Proc Lexicalca
	*/
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		Vector<StepResult> result = new Vector<StepResult>();
		String [] requiredparameters=new String[] {Keywords.OUT.toLowerCase(), Keywords.OUT.toLowerCase()+"words", Keywords.OUT.toLowerCase()+"docs", Keywords.OUT.toLowerCase()+"axes", Keywords.dict, Keywords.varwords, Keywords.varfreqs};
		String [] optionalparameters=new String[] {Keywords.explainedinertia, Keywords.where, Keywords.replace};
		Keywords.percentage_total=0;
		Keywords.percentage_done=0;
		StepUtilities steputilities=new StepUtilities();
		if (!steputilities.checkParameters(requiredparameters, optionalparameters, parameters))
		{
			return new Result(steputilities.getMessage(), false, null);
		}
		String tempexplainedinertia =(String)parameters.get(Keywords.explainedinertia);
		double explainedinertia=50;
		boolean useexp=false;
		if (tempexplainedinertia!=null)
		{
			explainedinertia=Double.NaN;
			try
			{
				explainedinertia=Double.parseDouble(tempexplainedinertia);
				useexp=true;
			}
			catch (Exception e) {}
		}
		if (Double.isNaN(explainedinertia)) return new Result("%3647%<br>\n", false, null);
		if (explainedinertia>100) return new Result("%3647%<br>\n", false, null);
		if (explainedinertia<=0) return new Result("%3647%<br>\n", false, null);

		String replace =(String)parameters.get(Keywords.replace);
		DataWriter dw=new DataWriter(parameters, Keywords.OUT.toLowerCase());
		if (!dw.getmessage().equals(""))
			return new Result(dw.getmessage(), false, null);

		DataWriter dww=new DataWriter(parameters, Keywords.OUT.toLowerCase()+"words");
		if (!dww.getmessage().equals(""))
			return new Result(dww.getmessage(), false, null);

		DataWriter dwd=new DataWriter(parameters, Keywords.OUT.toLowerCase()+"docs");
		if (!dwd.getmessage().equals(""))
			return new Result(dwd.getmessage(), false, null);

		DataWriter dwwd=new DataWriter(parameters, Keywords.OUT.toLowerCase()+"axes");
		if (!dwwd.getmessage().equals(""))
			return new Result(dwwd.getmessage(), false, null);

		DictionaryReader dict = (DictionaryReader)parameters.get(Keywords.dict);
		String tempvarwords=(String)parameters.get(Keywords.varwords.toLowerCase());
		String tempvarfreqs=(String)parameters.get(Keywords.varfreqs.toLowerCase());

		Hashtable<String, String> labelfor4=new Hashtable<String, String>();
		for (int i=0; i<dict.gettotalvar(); i++)
		{
			labelfor4.put(dict.getvarname(i).toLowerCase(), dict.getvarlabel(i));
		}

		tempvarwords=tempvarwords.trim();

		String[] varwords=tempvarwords.split(" ");
		if (varwords.length!=1)
			return new Result("%3557%<br>\n", false, null);
		String[] varfreqs=tempvarfreqs.split(" ");
		if (varfreqs.length<2)
			return new Result("%3558%<br>\n", false, null);

		Vector<String> docs_name=new Vector<String>();
		Vector<String> docs_label=new Vector<String>();
		Vector<Integer> docs_position=new Vector<Integer>();
		for (int i=0; i<varfreqs.length; i++)
		{
			docs_position.add(new Integer(-1));
			docs_name.add(varfreqs[i]);
			docs_label.add(varfreqs[i]);
		}
		int position_words=-1;

		for (int i=0; i<dict.gettotalvar(); i++)
		{
			if (tempvarwords.equalsIgnoreCase(dict.getvarname(i))) position_words=i;
		}
		if (position_words==-1)
			return new Result("%3559% ("+tempvarwords+")<br>\n", false, null);
		for (int i=0; i<varfreqs.length; i++)
		{
			for (int j=0; j<dict.gettotalvar(); j++)
			{
				if (varfreqs[i].equalsIgnoreCase(dict.getvarname(j)))
				{
					docs_position.set(i, new Integer(j));
					docs_name.set(i, dict.getvarname(j));
					docs_label.set(i, dict.getvarlabel(j));
				}
			}
		}
		int tempint=0;
		String ve="";
		double[] total_cols=new double[docs_position.size()];
		for (int i=0; i<docs_position.size(); i++)
		{
			tempint=(docs_position.get(i)).intValue();
			if (tempint==-1) ve=ve+docs_name.get(i)+" ";
			total_cols[i]=0.0;
		}
		ve=ve.trim();
		if (!ve.equals(""))
			return new Result("%3560% ("+ve+")<br>\n", false, null);

		TreeMap<String, double[]> tempmatrix=new TreeMap<String, double[]>();
		Hashtable<String, Double> total_rows=new Hashtable<String, Double>();

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
		String where=(String)parameters.get(Keywords.where.toLowerCase());
		if (where!=null)
		{
			if (!data.setcondition(where)) return new Result(data.getmessage(), false, null);
		}
		String[] values=null;
		double[] actualtw=null;

		int initial_words=0;

		double tempdouble=0;
		int totalrectow=0;
		while (!data.isLast())
		{
			values = data.getRecord();
			if (values!=null)
			{
				totalrectow++;
				initial_words++;
				tempdouble=0.0;
				double[] tw=new double[docs_position.size()];
				for (int i=0; i<docs_position.size(); i++)
				{
					tempint=(docs_position.get(i)).intValue();
					tw[i]=0;
					if (!values[tempint].equals(""))
					{
						try
						{
							tw[i]=Double.parseDouble(values[tempint]);
						}
						catch (Exception e){}
					}
					total_cols[i]=total_cols[i]+tw[i];
					tempdouble=tempdouble+tw[i];
				}
				if (tempmatrix.get(values[position_words])!=null)
				{
					tempdouble=tempdouble+(total_rows.get(values[position_words])).doubleValue();
					actualtw=tempmatrix.get(values[position_words]);
					for (int i=0; i<actualtw.length; i++)
					{
						tw[i]=tw[i]+actualtw[i];
					}
				}
				tempmatrix.put(values[position_words], tw);
				total_rows.put(values[position_words], new Double(tempdouble));
			}
		}
		data.close();
		if (tempmatrix.size()==0)
			return new Result("%3561%<br>\n", false, null);

		totalrectow=3*varfreqs.length+2*totalrectow;

		Keywords.percentage_total=totalrectow;
		Keywords.percentage_done=0;

		result.add(new LocalMessageGetter("%3562%: "+String.valueOf(tempmatrix.size())+"<br>\n"));
		if (tempmatrix.size()!=initial_words)
			result.add(new LocalMessageGetter("%3563% ("+String.valueOf(initial_words-tempmatrix.size())+")<br>\n"));

		Vector<Integer> excluded_cols=new Vector<Integer>();
		Vector<String> real_docs=new Vector<String>();
		Vector<String> real_docs_label=new Vector<String>();
		Vector<Integer> include_cols=new Vector<Integer>();
		Vector<Double> temp_totals_cols=new Vector<Double>();
		for (int i=0; i<total_cols.length; i++)
		{
			if (total_cols[i]==0.0) excluded_cols.add(new Integer(i));
			else
			{
				real_docs.add(docs_name.get(i));
				real_docs_label.add(docs_label.get(i));
				include_cols.add(new Integer(i));
				temp_totals_cols.add(new Double(total_cols[i]));
			}
		}
		result.add(new LocalMessageGetter("%3575%: "+String.valueOf(real_docs.size())+"<br>\n"));
		if (excluded_cols.size()>0)
		{
			String msg_ec="%3564%<br>\n";
			for (int i=0; i<excluded_cols.size(); i++)
			{
				tempint=(excluded_cols.get(i)).intValue();
				msg_ec=msg_ec+docs_name.get(tempint)+" ("+docs_label.get(tempint)+")<br>\n";
			}
			result.add(new LocalMessageGetter(msg_ec));
		}
		Vector<String> excluded_rows=new Vector<String>();
		for (Enumeration<String> et = total_rows.keys() ; et.hasMoreElements() ;)
		{
			String two = et.nextElement();
			tempdouble= (total_rows.get(two)).doubleValue();
			if (tempdouble==0.0) excluded_rows.add(two);
		}
		if (excluded_rows.size()>0)
		{
			String msg_er="%3565%<br>\n";
			for (int i=0; i<excluded_rows.size(); i++)
			{
				msg_er=msg_er+excluded_rows.get(i)+"<br>\n";
			}
			result.add(new LocalMessageGetter(msg_er));
		}
		int num_rows=total_rows.size()-excluded_rows.size();
		int num_cols=total_cols.length-excluded_cols.size();
		Vector<String> real_words=new Vector<String>();
		double[][] matrix=new double[num_rows][num_cols];
		int pointmatrix=0;
		double[] real_total_rows=new double[num_rows];
		Iterator<String> et = tempmatrix.keySet().iterator();
		while (et.hasNext())
		{
			String two = et.next();
			tempdouble= (total_rows.get(two)).doubleValue();
			if (tempdouble!=0)
			{
				actualtw =tempmatrix.get(two);
				real_words.add(two);
				for (int i=0; i<include_cols.size(); i++)
				{
					tempint=(include_cols.get(i)).intValue();
					matrix[pointmatrix][i]=actualtw[tempint];
				}
				real_total_rows[pointmatrix]=tempdouble;
				pointmatrix++;
			}
		}
		tempmatrix.clear();
		tempmatrix=null;
		double[] real_total_cols=new double[temp_totals_cols.size()];
		double sample_size=0.0;
		for (int i=0; i<temp_totals_cols.size(); i++)
		{
			real_total_cols[i]=(temp_totals_cols.get(i)).doubleValue();
			sample_size=sample_size+real_total_cols[i];
		}
		int ref_mat=num_rows;
		if (num_rows>num_cols) ref_mat=num_cols;
		DoubleMatrix2D usemat=null;
		try
		{
			usemat=DoubleFactory2D.dense.make(ref_mat, ref_mat);
		}
		catch (Exception ex)
		{
			usemat=null;
			return new Result("%3568% ("+ex.toString()+")\n", false, null);
		}
		double expected_freq=0;
		if (num_rows<num_cols)
		{
			for (int a=0; a<num_rows; a++)
			{
				for (int b=0; b<num_rows; b++)
				{
					usemat.set(a, b, 0.0);
					for (int i=0; i<num_cols; i++)
					{
						expected_freq=Math.sqrt(real_total_cols[i]*real_total_rows[b])*Math.sqrt(real_total_cols[i]*real_total_rows[a]);
						usemat.set(a, b, usemat.get(a, b)+matrix[a][i]*matrix[b][i]/expected_freq);
					}
				}
			}
		}
		else
		{
			for (int a=0; a<num_cols; a++)
			{
				for (int b=0; b<num_cols; b++)
				{
					usemat.set(a, b, 0.0);
					for (int i=0; i<num_rows; i++)
					{
						expected_freq=Math.sqrt(real_total_cols[b]*real_total_rows[i])*Math.sqrt(real_total_cols[a]*real_total_rows[i]);
						usemat.set(a, b, usemat.get(a, b)+matrix[i][a]*matrix[i][b]/expected_freq);
					}
				}
			}
		}
		double[] matval=null;
		double[][] matvec=null;
		try
		{
			EigenvalueDecomposition ed=new EigenvalueDecomposition(usemat);
			DoubleMatrix2D vec=ed.getV();
			DoubleMatrix1D val=ed.getRealEigenvalues();
			matval=val.toArray();
			matvec=vec.toArray();
			MatrixSort ms=new MatrixSort(matval, matvec);
			matval=ms.getorderedvector();
			matvec=ms.getorderedmatrix();
		}
		catch (Exception e)
		{
			return new Result("%3569% ("+e.toString()+")<br>\n", false, null);
		}
		int ref_valid_eigen=0;
		double sum_eigen=0;
		for (int i=1; i<matval.length; i++)
		{
			if (matval[i]>0.00000001) ref_valid_eigen=i;
			sum_eigen+=matval[i];
		}
		if (ref_valid_eigen!=matval.length-1)
			result.add(new LocalMessageGetter("%3694%<br>\n"));

		DataSetUtilities dsu=new DataSetUtilities();
		Hashtable<String, String> clp=new Hashtable<String, String>();
		Hashtable<String, String> tempmd=new Hashtable<String, String>();
		clp.put("1","%3576%");
		clp.put("2","%3577%");
		clp.put("3","%3578%");
		dsu.addnewvar("parameter", "%3574%", Keywords.TEXTSuffix, clp, tempmd);
		if (num_rows>=num_cols)
		{
			dsu.addnewvar("ref_doc_name", "%3579%", Keywords.TEXTSuffix, tempmd, tempmd);
			dsu.addnewvar("ref_doc_label", "%3580%", Keywords.TEXTSuffix, tempmd, tempmd);
			for (int i=0; i<ref_valid_eigen; i++)
			{
				dsu.addnewvar("val_"+String.valueOf(i+1), "%3581%: "+String.valueOf(i+1), Keywords.NUMSuffix, tempmd, tempmd);
			}
		}
		else
		{
			dsu.addnewvar("ref_words", "%3582%", Keywords.TEXTSuffix, tempmd, tempmd);
			for (int i=0; i<ref_valid_eigen; i++)
			{
				dsu.addnewvar("val_"+String.valueOf(i+1), "%3581%: "+String.valueOf(i+1), Keywords.NUMSuffix, tempmd, tempmd);
			}
		}
		if (!dw.opendatatable(dsu.getfinalvarinfo()))
		{
			return new Result(dw.getmessage(), false, null);
		}
		String dw_keyword="Lexicalca "+dict.getkeyword();
		String dw_description="Lexicalca "+dict.getdescription();
		String author=(String) parameters.get(Keywords.client_host.toLowerCase());
		String[] outvalues=new String[ref_valid_eigen+3];
		int ref_space=0;
		int nm1=-1;
		int n50=-1;
		double seed_sig=1.0/(ref_mat-1.0);
		double cum_exp=0.0;
		if (num_rows>=num_cols)
		{
			outvalues[0]="1";
			outvalues[1]="";
			outvalues[2]="";
			for (int i=1; i<ref_valid_eigen+1; i++)
			{
				if (matval[i]>seed_sig) nm1=i;
				outvalues[i+2]=String.valueOf(matval[i]);
			}
			dw.write(outvalues);
			outvalues[0]="2";
			outvalues[1]="";
			outvalues[2]="";
			for (int i=1; i<ref_valid_eigen+1; i++)
			{
				cum_exp+=100*matval[i]/sum_eigen;
				outvalues[i+2]=String.valueOf(cum_exp);
				if (cum_exp>50 && n50==-1) n50=i;
			}
			dw.write(outvalues);
			outvalues[0]="3";
			for (int i=0; i<matvec.length; i++)
			{
				outvalues[1]=real_docs.get(i);
				outvalues[2]=real_docs_label.get(i);
				for (int j=1; j<ref_valid_eigen+1; j++)
				{
					outvalues[j+2]=String.valueOf(matvec[i][j]);
				}
				Keywords.percentage_done++;
				dw.write(outvalues);
			}
		}
		else
		{
			outvalues=new String[matval.length+1];
			outvalues[0]="1";
			outvalues[1]="";
			for (int i=1; i<ref_valid_eigen+1; i++)
			{
				if (matval[i]>seed_sig) nm1=i;
				outvalues[i+1]=String.valueOf(matval[i]);
			}
			dw.write(outvalues);
			outvalues[0]="2";
			outvalues[1]="";
			for (int i=1; i<ref_valid_eigen+1; i++)
			{
				cum_exp+=100*matval[i]/sum_eigen;
				outvalues[i+1]=String.valueOf(cum_exp);
				if (cum_exp>explainedinertia && n50==-1) n50=i;
			}
			dw.write(outvalues);
			outvalues[0]="3";
			for (int i=0; i<ref_valid_eigen+1; i++)
			{
				outvalues[1]=real_words.get(i);
				for (int j=1; j<matvec[0].length; j++)
				{
					outvalues[j+1]=String.valueOf(matvec[i][j]);
				}
				Keywords.percentage_done++;
				dw.write(outvalues);
			}
		}
		if (ref_mat<=3) ref_space=ref_mat-1;
		else if (!useexp)
		{
			if (nm1>2 && n50>2)
			{
				if (nm1>=n50) ref_space=n50;
				else ref_space=nm1;
			}
			else if (nm1<=2 && n50>=2) ref_space=n50;
			else if (nm1>=2 && n50<=2) ref_space=nm1;
			else ref_space=ref_mat-1;
		}
		else ref_space=n50;
		result.add(new LocalMessageGetter("%3585%: "+String.valueOf(ref_space)+"<br>\n"));
		//double[][] words_proj=new double[num_rows][ref_space];

		Vector<TreeMap<String, Double>> axes_words=new Vector<TreeMap<String, Double>>();
		Vector<TreeMap<String, Double>> axes_docs=new Vector<TreeMap<String, Double>>();

		for (int i=0; i<ref_space; i++)
		{
			TreeMap<String, Double> ta=new TreeMap<String, Double>();
			TreeMap<String, Double> tb=new TreeMap<String, Double>();
			axes_words.add(ta);
			axes_docs.add(tb);
		}

		String dww_keyword="Lexicalca words "+dict.getkeyword();
		String dww_description="Lexicalca words "+dict.getdescription();
		DataSetUtilities dsuw=new DataSetUtilities();
		dsuw.addnewvar("word", "%3582%", Keywords.TEXTSuffix, tempmd, tempmd);
		dsuw.addnewvar("ref_axe", "%3583%", Keywords.NUMSuffix+Keywords.INTSuffix, tempmd, tempmd);
		dsuw.addnewvar("contr_axe", "%3591%", Keywords.NUMSuffix, tempmd, tempmd);
		Hashtable<String, String> clpw=new Hashtable<String, String>();
		clpw.put("0","%3588%");
		clpw.put("1","%3587%");
		dsuw.addnewvar("word_importance", "%3584%", Keywords.TEXTSuffix, clpw, tempmd);
		for (int i=0; i<ref_valid_eigen; i++)
		{
			dsuw.addnewvar("val_"+String.valueOf(i+1), "%3581%: "+String.valueOf(i+1), Keywords.NUMSuffix, tempmd, tempmd);
		}
		if (!dww.opendatatable(dsuw.getfinalvarinfo()))
		{
			dw.deletetmp();
			return new Result(dww.getmessage(), false, null);
		}
		outvalues=new String[ref_valid_eigen+4];
		int ref_axe=-1;
		double max_axe=-1.7976931348623157E308;
		double temp_proj=0;
		if (num_rows>=num_cols)
		{
			double projection=0;
			for (int i=0; i<matrix.length; i++)
			{
				ref_axe=-1;
				max_axe=-1.7976931348623157E308;
				temp_proj=0;
				outvalues[0]=real_words.get(i);
				for (int p=1; p<ref_valid_eigen+1; p++)
				{
					projection=0;
					for (int j=0; j<matrix[0].length; j++)
					{
						projection=projection+(matrix[i][j]/real_total_rows[i])*Math.sqrt(sample_size/real_total_cols[j])*matvec[j][p];
					}
					outvalues[3+p]=String.valueOf(projection);
					temp_proj=((real_total_rows[i]/sample_size)*Math.pow(projection,2))/matval[p];
					if (temp_proj>max_axe)
					{
						ref_axe=p;
						max_axe=temp_proj;
					}
				}
				outvalues[1]=String.valueOf(ref_axe);
				outvalues[2]=String.valueOf(max_axe);
				outvalues[3]="0";
				if (ref_axe-1<ref_space && ref_axe!=-1)
				{
					TreeMap<String, Double> ta=axes_words.get(ref_axe-1);
					ta.put(outvalues[0], new Double(max_axe));
					outvalues[3]="1";
				}
				Keywords.percentage_done++;
				dww.write(outvalues);
			}
		}
		else
		{
			double projection=0;
			for (int i=0; i<matrix.length; i++)
			{
				ref_axe=-1;
				max_axe=-1.7976931348623157E308;
				temp_proj=0;
				outvalues[0]=real_words.get(i);
				projection=0;
				for (int p=1; p<ref_valid_eigen+1; p++)
				{
					projection=(Math.sqrt(sample_size/real_total_rows[i]))*Math.sqrt(matval[p])*matvec[i][p];
					//if (p-1<ref_space)
					//	words_proj[i][p]=projection;
					outvalues[3+p]=String.valueOf(projection);
					temp_proj=(real_total_rows[i]/sample_size)*Math.pow(projection, 2)/matval[p];
					if (temp_proj>max_axe)
					{
						ref_axe=p;
						max_axe=temp_proj;
					}
				}
				outvalues[1]=String.valueOf(ref_axe);
				outvalues[2]=String.valueOf(max_axe);
				outvalues[3]="0";
				if (ref_axe-1<ref_space && ref_axe!=-1)
				{
					TreeMap<String, Double> ta=axes_words.get(ref_axe-1);
					ta.put(outvalues[0], new Double(max_axe));
					outvalues[3]="1";
				}
				Keywords.percentage_done++;
				dww.write(outvalues);
			}
		}
		String dwd_keyword="Lexicalca docs "+dict.getkeyword();
		String dwd_description="Lexicalca docs "+dict.getdescription();
		DataSetUtilities dsud=new DataSetUtilities();
		dsud.addnewvar("ref_doc_name", "%3579%", Keywords.TEXTSuffix, tempmd, tempmd);
		dsud.addnewvar("ref_doc_label", "%3580%", Keywords.TEXTSuffix, tempmd, tempmd);
		dsud.addnewvar("ref_axe", "%3589%", Keywords.NUMSuffix+Keywords.INTSuffix, tempmd, tempmd);
		dsud.addnewvar("contr_axe", "%3591%", Keywords.NUMSuffix, tempmd, tempmd);
		Hashtable<String, String> clpd=new Hashtable<String, String>();
		clpd.put("0","%3588%");
		clpd.put("1","%3587%");
		dsud.addnewvar("doc_importance", "%3590%", Keywords.TEXTSuffix, clpd, tempmd);
		for (int i=0; i<ref_valid_eigen; i++)
		{
			dsud.addnewvar("val_"+String.valueOf(i+1), "%3581%: "+String.valueOf(i+1), Keywords.NUMSuffix, tempmd, tempmd);
		}
		if (!dwd.opendatatable(dsud.getfinalvarinfo()))
		{
			dw.deletetmp();
			dww.deletetmp();
			return new Result(dwd.getmessage(), false, null);
		}
		outvalues=new String[ref_valid_eigen+5];
		if (num_rows>=num_cols)
		{
			double projection=0;
			for (int i=0; i<matrix[0].length; i++)
			{
				ref_axe=-1;
				max_axe=-1.7976931348623157E308;
				temp_proj=0;
				outvalues[0]=real_docs.get(i);
				outvalues[1]=real_docs_label.get(i);
				projection=0;
				for (int p=1; p<ref_valid_eigen+1; p++)
				{
					projection=(Math.sqrt(sample_size/real_total_cols[i]))*Math.sqrt(matval[p])*matvec[i][p];
					outvalues[4+p]=String.valueOf(projection);
					temp_proj=(real_total_cols[i]/sample_size)*Math.pow(projection, 2)/matval[p];
					if (temp_proj>max_axe)
					{
						ref_axe=p;
						max_axe=temp_proj;
					}
				}
				outvalues[2]=String.valueOf(ref_axe);
				outvalues[3]=String.valueOf(max_axe);
				outvalues[4]="0";
				if (ref_axe-1<ref_space && ref_axe!=-1)
				{
					TreeMap<String, Double> tb=axes_docs.get(ref_axe-1);
					tb.put(outvalues[0], new Double(max_axe));
					outvalues[4]="1";
				}
				Keywords.percentage_done++;
				dwd.write(outvalues);
			}
		}
		else
		{
			double projection=0;
			for (int i=0; i<matrix[0].length; i++)
			{
				ref_axe=-1;
				max_axe=-1.7976931348623157E308;
				temp_proj=0;
				outvalues[0]=real_docs.get(i);
				outvalues[1]=real_docs_label.get(i);
				for (int p=1; p<ref_valid_eigen+1; p++)
				{
					projection=0;
					for (int j=0; j<matrix.length; j++)
					{
						projection=projection+(matrix[i][j]/real_total_cols[i])*Math.sqrt(sample_size/real_total_rows[j])*matvec[j][p];
					}
					temp_proj=((real_total_cols[i]/sample_size)*Math.pow(projection,2))/matval[p];
					outvalues[3+p]=String.valueOf(projection);
					if (temp_proj>max_axe)
					{
						ref_axe=p;
						max_axe=temp_proj;
					}
				}
				outvalues[2]=String.valueOf(ref_axe);
				outvalues[3]=String.valueOf(max_axe);
				outvalues[4]="0";
				if (ref_axe-1<ref_space && ref_axe!=-1)
				{
					TreeMap<String, Double> tb=axes_docs.get(ref_axe-1);
					tb.put(outvalues[0], new Double(max_axe));
					outvalues[4]="1";
				}
				Keywords.percentage_done++;
				dwd.write(outvalues);
			}
		}

		String dwwd_keyword="Lexicalca axes"+dict.getkeyword();
		String dwwd_description="Lexicalca axes"+dict.getdescription();
		DataSetUtilities dsuwd=new DataSetUtilities();
		dsuwd.addnewvar("ref_axe", "%3592%", Keywords.TEXTSuffix, tempmd, tempmd);
		Hashtable<String, String> clpwd=new Hashtable<String, String>();
		clpwd.put("1","%3594%");
		clpwd.put("2","%3595%");
		dsuwd.addnewvar("obj_type", "%3593%", Keywords.TEXTSuffix, clpwd, tempmd);
		dsuwd.addnewvar("obj_value", "%3596%", Keywords.TEXTSuffix, tempmd, tempmd);
		dsuwd.addnewvar("obj_label", "%3873%", Keywords.TEXTSuffix, tempmd, tempmd);
		dsuwd.addnewvar("cont_value", "%3597%", Keywords.NUMSuffix, tempmd, tempmd);
		if (!dwwd.opendatatable(dsuwd.getfinalvarinfo()))
		{
			dw.deletetmp();
			dww.deletetmp();
			dwd.deletetmp();
			return new Result(dwwd.getmessage(), false, null);
		}
		outvalues=new String[5];
		for (int i=0; i<axes_words.size(); i++)
		{
			TreeMap<String, Double> ta=axes_words.get(i);
			if (ta.size()>0)
			{
				outvalues[0]=String.valueOf(i+1);
				outvalues[1]="1";
				Iterator<String> te = ta.keySet().iterator();
				while (te.hasNext())
				{
					outvalues[2] = te.next();
					outvalues[3] = outvalues[2];
					outvalues[4] = String.valueOf((ta.get(outvalues[2])).doubleValue());
					Keywords.percentage_done++;
					dwwd.write(outvalues);
				}
			}
			TreeMap<String, Double> tb=axes_docs.get(i);
			if (tb.size()>0)
			{
				outvalues[0]=String.valueOf(i+1);
				outvalues[1]="2";
				Iterator<String> te = tb.keySet().iterator();
				while (te.hasNext())
				{
					outvalues[2] = te.next();
					if (labelfor4.get(outvalues[2].toLowerCase())!=null)
						outvalues[3] = labelfor4.get(outvalues[2].toLowerCase());
					else
						outvalues[3] = outvalues[2];
					outvalues[4] = String.valueOf((tb.get(outvalues[2])).doubleValue());
					Keywords.percentage_done++;
					dwwd.write(outvalues);
				}
			}
		}

		boolean resclose=dw.close();
		if (!resclose)
			return new Result(dw.getmessage(), false, null);
		Vector<Hashtable<String, String>> tablevariableinfo=dw.getVarInfo();
		Hashtable<String, String> datatableinfo=dw.getTableInfo();
		result.add(new LocalDictionaryWriter(dw.getdictpath(), dw_keyword, dw_description, author, dw.gettabletype(),
		datatableinfo, dsu.getfinalvarinfo(), tablevariableinfo, dsu.getfinalcl(), dsu.getfinalmd(), null));
		resclose=dww.close();
		if (!resclose)
		{
			return new Result(dww.getmessage(), false, null);
		}
		Vector<Hashtable<String, String>> tablevariableinfow=dww.getVarInfo();
		Hashtable<String, String> datatableinfow=dww.getTableInfo();
		result.add(new LocalDictionaryWriter(dww.getdictpath(), dww_keyword, dww_description, author, dww.gettabletype(),
		datatableinfow, dsuw.getfinalvarinfo(), tablevariableinfow, dsuw.getfinalcl(), dsuw.getfinalmd(), null));
		resclose=dwd.close();
		if (!resclose)
		{
			return new Result(dwd.getmessage(), false, null);
		}
		Vector<Hashtable<String, String>> tablevariableinfod=dwd.getVarInfo();
		Hashtable<String, String> datatableinfod=dwd.getTableInfo();
		result.add(new LocalDictionaryWriter(dwd.getdictpath(), dwd_keyword, dwd_description, author, dwd.gettabletype(),
		datatableinfod, dsud.getfinalvarinfo(), tablevariableinfod, dsud.getfinalcl(), dsud.getfinalmd(), null));
		resclose=dwwd.close();
		if (!resclose)
		{
			return new Result(dwwd.getmessage(), false, null);
		}
		Vector<Hashtable<String, String>> tablevariableinfowd=dwwd.getVarInfo();
		Hashtable<String, String> datatableinfowd=dwwd.getTableInfo();
		result.add(new LocalDictionaryWriter(dwwd.getdictpath(), dwwd_keyword, dwwd_description, author, dwwd.gettabletype(),
		datatableinfowd, dsuwd.getfinalvarinfo(), tablevariableinfowd, dsuwd.getfinalcl(), dsuwd.getfinalmd(), null));
		return new Result("", true, result);
	}
	@SuppressWarnings("unused")
	private void printMatrix(String fp, double[][] mat)
	{
		try
		{
			BufferedWriter out = new BufferedWriter(new FileWriter(fp));
			for (int a=0; a<mat.length; a++)
			{
				String valtoprint="";
				for (int b=0; b<mat[0].length; b++)
				{
					valtoprint=valtoprint+String.valueOf(mat[a][b])+"\t";
				}
				out.write(valtoprint+"\n");
			}
			out.close();
		}
		catch (Exception e) {}
	}
	@SuppressWarnings("unused")
	private void printVector(String fp, double[] mat)
	{
		try
		{
			BufferedWriter out = new BufferedWriter(new FileWriter(fp));
			for (int a=0; a<mat.length; a++)
			{
				out.write(mat[a]+"\n");
			}
			out.close();
		}
		catch (Exception e) {}
	}
	/**
	*Returns the parameters for the procedure
	*/
	public LinkedList<GetRequiredParameters> getparameters()
	{
		LinkedList<GetRequiredParameters> parameters=new LinkedList<GetRequiredParameters>();
		String[] dep ={""};
		parameters.add(new GetRequiredParameters(Keywords.dict+"=", "dict", true, 3567, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.OUT.toLowerCase()+"=", "setting=out", true, 3570, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.OUT.toLowerCase()+"words=", "setting=out", true, 3571, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.OUT.toLowerCase()+"docs=", "setting=out", true, 3572, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.OUT.toLowerCase()+"axes=", "setting=out", true, 3573, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.viewout, "checkbox", false, 1612, dep, "", 1));
		dep = new String[1];
		dep[0]=Keywords.dict;
		parameters.add(new GetRequiredParameters(Keywords.varwords, "var=all", true, 3555, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.varfreqs, "vars=all", true, 3556, dep, "", 2));
		dep = new String[0];
		parameters.add(new GetRequiredParameters(Keywords.explainedinertia,"text", false, 3646,dep,"",2));
		parameters.add(new GetRequiredParameters(Keywords.where,"text", false, 2805,dep,"",2));
		parameters.add(new GetRequiredParameters("", "note", false, 2806, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.replace, "listsingle=530_NULL,531_"+Keywords.replaceall+",532_"+Keywords.replaceformat+",533_"+Keywords.replacemissing,false, 534, dep, "", 2));
		return parameters;
	}
	/**
	*Returns the group and the name of the procedure
	*/
	public String[] getstepinfo()
	{
		String[] retprocinfo=new String[2];
		retprocinfo[0]="4169";
		retprocinfo[1]="3566";
		return retprocinfo;
	}
}
