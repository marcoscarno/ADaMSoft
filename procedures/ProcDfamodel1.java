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

import ADaMSoft.algorithms.CovariancesEvaluator;
import ADaMSoft.dataaccess.DictionaryReader;
import ADaMSoft.dataaccess.DataReader;
import ADaMSoft.dataaccess.DataWriter;
import ADaMSoft.utilities.StepUtilities;

import ADaMSoft.utilities.GetRequiredParameters;
import ADaMSoft.utilities.VariableUtilities;
import ADaMSoft.utilities.DataSetUtilities;
import ADaMSoft.utilities.ObjectTransformer;
import ADaMSoft.utilities.StringComparator;
import ADaMSoft.utilities.MatrixSort;

import ADaMSoft.keywords.Keywords;

import java.util.Hashtable;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.LinkedList;
import java.util.Vector;
import java.util.Iterator;

import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;
import cern.colt.matrix.linalg.EigenvalueDecomposition;

/**
* This is the procedure that implements the dynamic factorial analysis
* @author marco.scarno@gmail.com
* @date 13/02/2017
*/
public class ProcDfamodel1 extends ObjectTransformer implements RunStep
{
	/**
	* Starts the execution of Proc Dfamodel1
	*/
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		boolean samplevariance=false;
		boolean writecov=false;
		String [] requiredparameters=new String[] {Keywords.dict, Keywords.OUT.toLowerCase(), Keywords.OUTSTMD.toLowerCase(), Keywords.OUTSTREG.toLowerCase(), Keywords.var, Keywords.varobs, Keywords.vartime};
		String [] optionalparameters=new String[] {Keywords.OUTC.toLowerCase(), Keywords.where, Keywords.weight, Keywords.samplevariance, Keywords.transform.toLowerCase(), Keywords.replace};
		Keywords.percentage_total=0;
		Keywords.percentage_done=0;
		StepUtilities steputilities=new StepUtilities();
		if (!steputilities.checkParameters(requiredparameters, optionalparameters, parameters))
		{
			return new Result(steputilities.getMessage(), false, null);
		}
		samplevariance =(parameters.get(Keywords.samplevariance)!=null);

		String transform=(String)parameters.get(Keywords.transform.toLowerCase());
		if (transform==null)
			transform=Keywords.notransform;

		String[] transformation=new String[] {Keywords.notransform, Keywords.standardize, Keywords.divformax,
		Keywords.normalize01, Keywords.meannormalize, Keywords.sumnormalize,};
		int selectedoption=steputilities.CheckOption(transformation, transform);
		if (selectedoption==0)
			return new Result(steputilities.getMessage(), false, null);

		String replace =(String)parameters.get(Keywords.replace);
		DataWriter dwc=null;
		if ((String)parameters.get(Keywords.OUTC.toLowerCase())!=null)
		{
			writecov=true;
			dwc=new DataWriter(parameters, Keywords.OUTC.toLowerCase());
			if (!dwc.getmessage().equals(""))
				return new Result(dwc.getmessage(), false, null);
		}

		DataWriter dwstmd=new DataWriter(parameters, Keywords.OUTSTMD.toLowerCase());
		if (!dwstmd.getmessage().equals(""))
			return new Result(dwstmd.getmessage(), false, null);

		DataWriter dw=new DataWriter(parameters, Keywords.OUT.toLowerCase());
		if (!dw.getmessage().equals(""))
			return new Result(dw.getmessage(), false, null);

		DataWriter dwreg=new DataWriter(parameters, Keywords.OUTSTREG.toLowerCase());
		if (!dwreg.getmessage().equals(""))
			return new Result(dwreg.getmessage(), false, null);

		DictionaryReader dict = (DictionaryReader)parameters.get(Keywords.dict);
		String tempvar=(String)parameters.get(Keywords.var.toLowerCase());
		String tempvarobs=(String)parameters.get(Keywords.varobs.toLowerCase());
		String tempvartime=(String)parameters.get(Keywords.vartime.toLowerCase());
		String weight=(String)parameters.get(Keywords.weight.toLowerCase());

		String[] tempvarname=tempvar.split(" ");
		boolean invarisobs=false;
		boolean invaristime=false;
		boolean invarisweight=false;
		for (int i=0; i<tempvarname.length; i++)
		{
			if ((tempvarname[i].trim()).equalsIgnoreCase(tempvarobs.trim()))
				invarisobs=true;
			if ((tempvarname[i].trim()).equalsIgnoreCase(tempvartime.trim()))
				invarisobs=true;
			if (weight!=null)
			{
				if ((tempvarname[i].trim()).equalsIgnoreCase(weight.trim()))
					invarisweight=true;
			}
		}
		if (invarisobs)
			return new Result("%2024%<br>\n", false, null);
		if (invaristime)
			return new Result("%2025%<br>\n", false, null);
		if (invarisweight)
			return new Result("%2026%<br>\n", false, null);

		String[] testvar=tempvarobs.split(" ");
		if (testvar.length>1)
			return new Result("%1336%<br>\n", false, null);

		testvar=tempvartime.split(" ");
		if (testvar.length>1)
			return new Result("%1337%<br>\n", false, null);

		String allvar=tempvartime+" "+tempvarobs+" "+tempvar;

		if (weight!=null)
			allvar=allvar+" "+weight;

		VariableUtilities varu=new VariableUtilities(dict, null, null, null, allvar, null);
		if (varu.geterror())
			return new Result(varu.getmessage(), false, null);

		String author=(String)parameters.get(Keywords.client_host.toLowerCase());

		testvar=allvar.split(" ");
		int lastcol=testvar.length;

		int[] replacerule=new int[testvar.length];
		int rifrep=0;
		if (replace==null)
			rifrep=0;
		else if (replace.equalsIgnoreCase(Keywords.replaceall))
			rifrep=1;
		else if (replace.equalsIgnoreCase(Keywords.replaceformat))
			rifrep=2;
		else if (replace.equalsIgnoreCase(Keywords.replacemissing))
			rifrep=3;
		for (int i=0; i<testvar.length; i++)
		{
			replacerule[i]=rifrep;
		}

		DataReader data = new DataReader(dict);

		if (!data.open(testvar, replacerule, false))
			return new Result(data.getmessage(), false, null);
		String where=(String)parameters.get(Keywords.where.toLowerCase());
		if (where!=null)
		{
			if (!data.setcondition(where)) return new Result(data.getmessage(), false, null);
		}

		if (weight!=null)
			lastcol=lastcol-1;
		double[] means=new double[lastcol-2];
		double[] sum=new double[lastcol-2];
		double[] max=new double[lastcol-2];
		double[] min=new double[lastcol-2];
		double[] sumsq=new double[lastcol-2];
		for (int i=0; i<lastcol-2; i++)
		{
			means[i]=0;
			sum[i]=0;
			max[i]=-1.7976931348623157E308;
			min[i]=Double.MAX_VALUE;
			sumsq[i]=0;
		}

		TreeSet<String> timen=new TreeSet<String>(new StringComparator());
		TreeSet<String> obsn=new TreeSet<String>(new StringComparator());
		HashSet<String[]> testobsfortime=new HashSet<String[]>();

		double nvalid=0;
		String obs="";
		String[] values=null;
		double obsw=1;
		boolean ismissing=false;
		double currentvalue=Double.NaN;
		double time=Double.NaN;
		while (!data.isLast())
		{
			values = data.getRecord();
			if (values!=null)
			{
				ismissing=false;
				obsw=1;
				if (weight!=null)
				{
					try
					{
						obsw=Double.parseDouble(values[values.length-1]);
					}
					catch (Exception ee)
					{
						ismissing=true;
					}
					if (Double.isNaN(obsw))
						ismissing=true;
				}
				time=Double.NaN;
				try
				{
					time=Double.parseDouble(values[0]);
				}
				catch (Exception eee) {}
				if (Double.isNaN(time))
					ismissing=true;
				for (int i=2; i<lastcol; i++)
				{
					try
					{
						currentvalue=Double.parseDouble(values[i]);
						if (Double.isNaN(currentvalue))
							ismissing=true;
					}
					catch (Exception ee)
					{
						ismissing=true;
					}
				}
				obs=values[1];
				if (obs.equals(""))
					ismissing=true;
				if (!ismissing)
				{
					for (int i=2; i<lastcol; i++)
					{
						currentvalue=Double.parseDouble(values[i]);
						means[i-2]+=currentvalue*obsw;
						sum[i-2]+=currentvalue*obsw;
						sumsq[i-2]+=currentvalue*currentvalue*obsw;
						if (currentvalue<min[i-2])
							min[i-2]=currentvalue;
						if (currentvalue>max[i-2])
							max[i-2]=currentvalue;
					}
					nvalid+=obsw;
				}
			}
		}
		data.close();
		if ((nvalid==0) && (where!=null))
			return new Result("%2804%<br>\n", false, null);
		if ((nvalid==0) && (where==null))
			return new Result("%1338%<br>\n", false, null);

		for (int i=0; i<testvar.length-2; i++)
		{
			means[i]=means[i]/nvalid;
			sumsq[i]=sumsq[i]/nvalid-Math.pow(means[i],2);
			if (!samplevariance)
				sumsq[i]=sumsq[i]*nvalid/(nvalid-1);
		}

		boolean testifallobs=true;
		Iterator<String> itt = timen.iterator();
		while(itt.hasNext())
		{
			Iterator<String> ito = obsn.iterator();
			String[] testot=new String[2];
			testot[0]=itt.next();
			while(ito.hasNext())
			{
				testot[1]=ito.next();
				if (!testobsfortime.contains(testot))
					testifallobs=false;
			}
		}
		if (!testifallobs)
			return new Result("%1339%<br>\n", false, null);

		testobsfortime.clear();
		testobsfortime=null;

		double[] valuerow=new double[means.length];

		CovariancesEvaluator s=new CovariancesEvaluator(true, samplevariance);

		if (!data.open(testvar, replacerule, false))
			return new Result(data.getmessage(), false, null);
		TreeMap<Double, double[]> timemeans=new TreeMap<Double, double[]>();
		TreeMap<String, double[]> obsmeans=new TreeMap<String, double[]>(new StringComparator());
		TreeMap<Double, Double> ntime=new TreeMap<Double, Double>();
		TreeMap<String, Double> nobs=new TreeMap<String, Double>(new StringComparator());

		Vector<String> all=new Vector<String>();
		all.add(null);

		while (!data.isLast())
		{
			values = data.getRecord();
			ismissing=false;
			obsw=1;
			if (weight!=null)
			{
				try
				{
					obsw=Double.parseDouble(values[values.length-1]);
				}
				catch (Exception ee)
				{
					ismissing=true;
				}
				if (Double.isNaN(obsw))
					ismissing=true;
			}
			time=Double.NaN;
			try
			{
				time=Double.parseDouble(values[0]);
			}
			catch (Exception eee) {}
			if (Double.isNaN(time))
				ismissing=true;
			for (int i=2; i<lastcol; i++)
			{
				try
				{
					currentvalue=Double.parseDouble(values[i]);
					if (Double.isNaN(currentvalue))
						ismissing=true;
				}
				catch (Exception ee)
				{
					ismissing=true;
				}
			}
			obs=values[1];
			if (obs.equals(""))
				ismissing=true;
			if (!ismissing)
			{
				double[] temptimemeans=timemeans.get(new Double(time));
				if (temptimemeans==null)
				{
					temptimemeans=new double[lastcol-2];
					for (int i=0; i<lastcol-2; i++)
						temptimemeans[i]=0;
				}
				double[] tempobsmeans=obsmeans.get(obs);
				if (tempobsmeans==null)
				{
					tempobsmeans=new double[lastcol-2];
					for (int i=0; i<lastcol-2; i++)
						tempobsmeans[i]=0;
				}
				double temptimen=0;
				if (ntime.get(time)!=null)
					temptimen=(ntime.get(new Double(time))).doubleValue();
				double tempobsn=0;
				if (nobs.get(obs)!=null)
					tempobsn=(nobs.get(obs)).doubleValue();
				for (int i=2; i<lastcol; i++)
				{
					valuerow[i-2]=Double.parseDouble(values[i]);
					if(selectedoption==2)
						valuerow[i-2]=(valuerow[i-2]-means[i-2])/Math.pow(sumsq[i-2],0.5);
					if(selectedoption==3)
						valuerow[i-2]=valuerow[i-2]/max[i-2];
					if(selectedoption==4)
						valuerow[i-2]=(valuerow[i-2]-min[i-2])/(max[i-2]-min[i-2]);
					if(selectedoption==5)
						valuerow[i-2]=valuerow[i-2]/means[i-2];
					if(selectedoption==6)
						valuerow[i-2]=valuerow[i-2]/sum[i-2];
					temptimemeans[i-2]+=valuerow[i-2]*obsw;
					tempobsmeans[i-2]+=valuerow[i-2]*obsw;
				}
				temptimen+=obsw;
				tempobsn+=obsw;
				timemeans.put(new Double(time), temptimemeans);
				obsmeans.put(obs, tempobsmeans);
				ntime.put(new Double(time), new Double(temptimen));
				nobs.put(obs, new Double(tempobsn));
				s.setValue(all, valuerow, valuerow, obsw);
			}
		}
		data.close();

		CovariancesEvaluator sit=new CovariancesEvaluator(true, samplevariance);
		CovariancesEvaluator si=new CovariancesEvaluator(true, samplevariance);
		CovariancesEvaluator st=new CovariancesEvaluator(true, samplevariance);

		double[] tm=new double[0];
		double tn=0;
		double[] im=new double[0];
		double in=0;

		data = new DataReader(dict);
		if (!data.open(testvar, replacerule, false))
			return new Result(data.getmessage(), false, null);

		while (!data.isLast())
		{
			values = data.getRecord();
			ismissing=false;
			obsw=1;
			if (weight!=null)
			{
				try
				{
					obsw=Double.parseDouble(values[values.length-1]);
				}
				catch (Exception ee)
				{
					ismissing=true;
				}
				if (Double.isNaN(obsw))
					ismissing=true;
			}
			time=Double.NaN;
			try
			{
				time=Double.parseDouble(values[0]);
			}
			catch (Exception eee) {}
			if (Double.isNaN(time))
				ismissing=true;
			for (int i=2; i<lastcol; i++)
			{
				try
				{
					currentvalue=Double.parseDouble(values[i]);
					if (Double.isNaN(currentvalue))
						ismissing=true;
				}
				catch (Exception ee)
				{
					ismissing=true;
				}
			}
			obs=values[1];
			if (obs.equals(""))
				ismissing=true;
			if (!ismissing)
			{
				tm=timemeans.get(new Double(time));
				tn=(ntime.get(new Double(time))).doubleValue();

				im=obsmeans.get(obs);
				in=(nobs.get(obs)).doubleValue();

				double[] srt=new double[lastcol-2];
				for (int i=2; i<lastcol; i++)
				{
					valuerow[i-2]=Double.parseDouble(values[i]);
					if(selectedoption==2)
						valuerow[i-2]=(valuerow[i-2]-means[i-2])/Math.pow(sumsq[i-2],0.5);
					if(selectedoption==3)
						valuerow[i-2]=valuerow[i-2]/max[i-2];
					if(selectedoption==4)
						valuerow[i-2]=(valuerow[i-2]-min[i-2])/(max[i-2]-min[i-2]);
					if(selectedoption==5)
						valuerow[i-2]=valuerow[i-2]/means[i-2];
					if(selectedoption==6)
						valuerow[i-2]=valuerow[i-2]/sum[i-2];
					srt[i-2]=valuerow[i-2]-tm[i-2]/tn-im[i-2]/in;
				}
				sit.setValue(all, srt, srt, obsw);
			}
		}
		data.close();

		double[] meanc=new double[tempvarname.length];
		for (int i=0; i<meanc.length; i++)
		{
			meanc[i]=0;
		}
		double meant=0;
		double tott=0;

		Iterator<Double> intime = ntime.keySet().iterator();
		while(intime.hasNext())
		{
			double tntime=intime.next().doubleValue();
			tn=ntime.get(new Double(tntime)).doubleValue();
			tm=timemeans.get(new Double(tntime));
			for (int i=0; i<tm.length; i++)
			{
				meanc[i]=meanc[i]+tm[i]*tn;
				tm[i]=tm[i]/tn;
			}
			st.setValue(all, tm, tm, tn);
			meant=meant+tntime*tn;
			tott=tott+tn;
		}

		for (int i=0; i<meanc.length; i++)
		{
			meanc[i]=meanc[i]/tott;
		}
		meant=meant/tott;

		Iterator<String> inobs = nobs.keySet().iterator();
		while(inobs.hasNext())
		{
			String tnobs=inobs.next();
			in=nobs.get(tnobs);
			im=obsmeans.get(tnobs);
			for (int i=0; i<im.length; i++)
			{
				im[i]=im[i]/in;
			}
			si.setValue(all, im, im, in);
		}

		s.calculate();
		si.calculate();
		st.calculate();
		sit.calculate();

		Hashtable<Vector<String>, double[][]> cs=s.getresult();
		Hashtable<Vector<String>, double[][]> csi=si.getresult();
		Hashtable<Vector<String>, double[][]> cst=st.getresult();
		Hashtable<Vector<String>, double[][]> csit=sit.getresult();

		DataSetUtilities dsucorr=null;
		Vector<StepResult> result = new Vector<StepResult>();

		double[][] allS=cs.get(all);
		double[][] allSI=csi.get(all);
		double[][] allST=cst.get(all);
		double[][] allSIT=csit.get(all);
		DoubleMatrix2D matMST=null;
		double[] matval=null;
		double[][] matvec=null;
		double inertia=0;
		DoubleMatrix2D vec=null;
		DoubleMatrix2D matPSTM=null;
		double variabilityS=0;
		double variabilitySI=0;
		double variabilityST=0;
		double variabilitySIT=0;
		double variabilityMST=0;
		double[] indexii=new double[tempvarname.length];
		double[] indexit=new double[tempvarname.length];
		double[] indexiit=new double[tempvarname.length];
		Algebra algebra=new Algebra();
		try
		{
			DoubleMatrix2D SItemp=DoubleFactory2D.dense.make(allSI.length, allSI[0].length);
			DoubleMatrix2D MSTtemp=DoubleFactory2D.dense.make(allST.length, allST[0].length);
			DoubleMatrix2D SITtemp=DoubleFactory2D.dense.make(allSIT.length, allSIT[0].length);
			matMST=DoubleFactory2D.dense.make(allSIT.length, allSIT[0].length);
			for (int i=0; i<tempvarname.length; i++)
			{
				variabilityS+=allS[i][i];
				variabilitySI+=allSI[i][i];
				variabilityST+=allST[i][i];
				variabilitySIT+=allSIT[i][i];
				variabilityMST+=allSIT[i][i]+allSI[i][i];
				for (int j=0; j<tempvarname.length; j++)
				{
					matMST.set(i, j, allSIT[i][j]+allSI[i][j]);
					SItemp.set(i, j, allSI[i][j]);
					SITtemp.set(i, j, allSIT[i][j]);
					MSTtemp.set(i, j, allSIT[i][j]+allSI[i][j]);
				}
			}
			EigenvalueDecomposition ed=new EigenvalueDecomposition(matMST);
			vec=ed.getV();
			DoubleMatrix1D val=ed.getRealEigenvalues();
			matval=val.toArray();
			matvec=vec.toArray();
			MatrixSort ms=new MatrixSort(matval, matvec);
			matval=ms.getorderedvector();
			matvec=ms.getorderedmatrix();
			for (int i=0; i<matvec.length; i++)
			{
				for (int j=0; j<matvec[0].length; j++)
				{
					vec.set(i, j, matvec[i][j]);
				}
			}

			DoubleMatrix2D matvect=algebra.transpose(vec);
			DoubleMatrix2D matvectSItemp=algebra.mult(matvect, SItemp);
			SItemp=algebra.mult(matvectSItemp, vec);

			DoubleMatrix2D matvectMSTtemp=algebra.mult(matvect, MSTtemp);
			MSTtemp=algebra.mult(matvectMSTtemp, vec);

			DoubleMatrix2D matvectSITtemp=algebra.mult(matvect, SITtemp);
			SITtemp=algebra.mult(matvectSITtemp, vec);

			for (int i=0; i<tempvarname.length; i++)
			{
				indexii[i]=SItemp.get(i, i);
				indexit[i]=MSTtemp.get(i, i);
				indexiit[i]=SITtemp.get(i, i);
			}
		}
		catch (Exception ex)
		{
			return new Result("%2030%<br>\n", false, null);
		}

		Hashtable<String, String> tempmd=new Hashtable<String, String>();
		DataSetUtilities dsustmd=new DataSetUtilities();
		Hashtable<String, String> clvarstmd=new Hashtable<String, String>();
		clvarstmd.put("1", "%968%");
		clvarstmd.put("2", "%969%");
		for (int j=0; j<tempvarname.length; j++)
		{
			clvarstmd.put("evec_"+tempvarname[j], "%970_ "+dict.getvarlabelfromname(tempvarname[j]));
		}
		inobs = nobs.keySet().iterator();
		while(inobs.hasNext())
		{
			String tnobs=inobs.next();
			clvarstmd.put("p_"+tnobs, "%2032% "+tnobs);
		}
		for (int j=0; j<tempvarname.length; j++)
		{
			clvarstmd.put("c_"+tempvarname[j], "%2034% "+dict.getvarlabelfromname(tempvarname[j]));
		}
		dsustmd.addnewvar("Type", "%971%", Keywords.TEXTSuffix, clvarstmd, tempmd);
		for (int i=0; i<tempvarname.length; i++)
		{
			dsustmd.addnewvar("c_"+String.valueOf(i+1), "%972% "+String.valueOf(i+1), Keywords.TEXTSuffix, tempmd, tempmd);
		}

		String[] valuestowrite=new String[1+tempvarname.length];

		if (!dwstmd.opendatatable(dsustmd.getfinalvarinfo()))
			return new Result(dwstmd.getmessage(), false, null);

		valuestowrite[0]="1";
		for (int i=0; i<matval.length; i++)
		{
			inertia=inertia+matval[i];
			valuestowrite[i+1]=double2String(matval[i]);
		}
		dwstmd.write(valuestowrite);
		valuestowrite[0]="2";
		for (int i=0; i<matval.length; i++)
		{
			valuestowrite[i+1]=double2String(matval[i]/inertia);
		}
		dwstmd.write(valuestowrite);
		for (int j=0; j<tempvarname.length; j++)
		{
			valuestowrite[0]="evec_"+tempvarname[j];
			for (int k=0; k<matvec[0].length; k++)
			{
				valuestowrite[1+k]=double2String(matvec[j][k]);
			}
			dwstmd.write(valuestowrite);
		}

		DataSetUtilities dsu=new DataSetUtilities();

		double[] meanu=null;

		try
		{
			int pointer=0;
			matMST=DoubleFactory2D.dense.make(nobs.size(), tempvarname.length);
			meanu=new double[im.length];
			double[] totu=new double[im.length];
			for (int i=0; i<meanu.length; i++)
			{
				meanu[i]=0;
				totu[i]=0;
			}
			inobs = nobs.keySet().iterator();
			while(inobs.hasNext())
			{
				String tnobs=inobs.next();
				in=nobs.get(tnobs);
				im=obsmeans.get(tnobs);
				for (int i=0; i<im.length; i++)
				{
					meanu[i]=meanu[i]+im[i]*in;
					totu[i]=totu[i]+in;
				}
			}
			for (int i=0; i<meanu.length; i++)
			{
				meanu[i]=meanu[i]/totu[i];
			}

			inobs = nobs.keySet().iterator();
			double[] meanmatMST=new double[im.length];
			double[] stdmatMST=new double[im.length];
			double[] nummatMST=new double[im.length];
			for (int i=0; i<im.length; i++)
			{
				meanmatMST[i]=0;
				stdmatMST[i]=0;
				nummatMST[i]=0;
			}
			double[] meanmatPSTM=new double[im.length];
			double[] stdmatPSTM=new double[im.length];
			double[] nummatPSTM=new double[im.length];
			for (int i=0; i<im.length; i++)
			{
				meanmatPSTM[i]=0;
				stdmatPSTM[i]=0;
				nummatPSTM[i]=0;
			}
			while(inobs.hasNext())
			{
				String tnobs=inobs.next();
				in=nobs.get(tnobs);
				im=obsmeans.get(tnobs);
				for (int i=0; i<im.length; i++)
				{
					matMST.set(pointer, i, im[i]-meanu[i]);
					nummatMST[i]=nummatMST[i]+1;
					stdmatMST[i]=stdmatMST[i]+Math.pow(matMST.get(pointer,i),2);
					meanmatMST[i]=meanmatMST[i]+matMST.get(pointer,i);
				}
				pointer++;
			}
			for (int i=0; i<im.length; i++)
			{
				meanmatMST[i]=meanmatMST[i]/nummatMST[i];
				stdmatMST[i]=Math.sqrt((stdmatMST[i]/nummatMST[i])-Math.pow(meanmatMST[i],2));
			}
			matPSTM=algebra.mult(matMST, vec);
			inobs = nobs.keySet().iterator();
			pointer=0;
			while(inobs.hasNext())
			{
				String tnobs=inobs.next();
				valuestowrite[0]="p_"+tnobs;
				for (int i=0; i<im.length; i++)
				{
					valuestowrite[1+i]=double2String(matPSTM.get(pointer,i));
					nummatPSTM[i]=nummatPSTM[i]+1;
					stdmatPSTM[i]=stdmatPSTM[i]+Math.pow(matPSTM.get(pointer,i),2);
					meanmatPSTM[i]=meanmatPSTM[i]+matPSTM.get(pointer,i);

				}
				dwstmd.write(valuestowrite);
				pointer++;
			}
			for (int i=0; i<im.length; i++)
			{
				meanmatPSTM[i]=meanmatPSTM[i]/nummatPSTM[i];
				stdmatPSTM[i]=Math.sqrt((stdmatPSTM[i]/nummatPSTM[i])-Math.pow(meanmatPSTM[i],2));
			}
			DoubleMatrix2D matMSTT=algebra.transpose(matMST);
			DoubleMatrix2D matSTCORR=algebra.mult(matMSTT, matPSTM);
			for (int i=0; i<tempvarname.length; i++)
			{
				valuestowrite[0]="c_"+tempvarname[i];
				for (int j=0; j<tempvarname.length; j++)
				{
					valuestowrite[1+j]=double2String(matSTCORR.get(i,j)/(pointer*stdmatMST[i]*stdmatPSTM[j]));
				}
				dwstmd.write(valuestowrite);
			}
			Hashtable<String, String> clvarsg=new Hashtable<String, String>();
			clvarsg.put("1", "%1349%");
			clvarsg.put("2", "%1348%");
			clvarsg.put("3", "%1347%");
			clvarsg.put("4", "%2036%");
			clvarsg.put("5", "%2037%");
			clvarsg.put("6", "%2038%");
			clvarsg.put("7", "%2039%");
			clvarsg.put("8", "%2040%");
			clvarsg.put("9", "%2041%");
			clvarsg.put("10", "%2042%");
			clvarsg.put("11", "%2043%");
			clvarsg.put("12", "%2044%");
			clvarsg.put("13", "%2045%");
			clvarsg.put("14", "%656%");
			clvarsg.put("15", "%2064%");
			for (int j=0; j<tempvarname.length; j++)
			{
				clvarsg.put("c_"+tempvarname[j], "%2046% "+dict.getvarlabelfromname(tempvarname[j]));
			}
			for (int j=0; j<tempvarname.length; j++)
			{
				clvarsg.put("ii_"+String.valueOf(j+1), "%2061% "+String.valueOf(j+1));
				clvarsg.put("it_"+String.valueOf(j+1), "%2062% "+String.valueOf(j+1));
				clvarsg.put("iit_"+String.valueOf(j+1), "%2063% "+String.valueOf(j+1));
				clvarsg.put("ig_"+String.valueOf(j+1), "%2065_ "+String.valueOf(j+1));
			}

			dsu.addnewvar("ref", "%2047%", Keywords.TEXTSuffix, clvarsg, tempmd);
			dsu.addnewvar("value", "%2048%", Keywords.TEXTSuffix, tempmd, tempmd);
			if (!dw.opendatatable(dsu.getfinalvarinfo()))
				return new Result(dw.getmessage(), false, null);
			valuestowrite=new String[2];
			valuestowrite[0]="1";
			valuestowrite[1]=tempvartime.trim();
			dw.write(valuestowrite);
			valuestowrite[0]="2";
			valuestowrite[1]=tempvarobs.trim();
			dw.write(valuestowrite);
			valuestowrite[0]="3";
			valuestowrite[1]=tempvar.trim();
			dw.write(valuestowrite);
			if (weight!=null)
			{
				valuestowrite[0]="14";
				valuestowrite[1]=weight.trim();
				dw.write(valuestowrite);
			}
			valuestowrite[0]="4";
			valuestowrite[1]=transform.trim();
			dw.write(valuestowrite);
			valuestowrite[0]="5";
			valuestowrite[1]=double2String(variabilityS);
			dw.write(valuestowrite);
			valuestowrite[0]="6";
			valuestowrite[1]=double2String(variabilitySI);
			dw.write(valuestowrite);
			valuestowrite[0]="7";
			valuestowrite[1]=double2String(variabilityST);
			dw.write(valuestowrite);
			valuestowrite[0]="8";
			valuestowrite[1]=double2String(variabilitySIT);
			dw.write(valuestowrite);
			valuestowrite[0]="9";
			valuestowrite[1]=double2String(variabilitySI+variabilitySIT);
			dw.write(valuestowrite);
			valuestowrite[0]="10";
			valuestowrite[1]=double2String(variabilityST+variabilitySIT);
			dw.write(valuestowrite);
			valuestowrite[0]="11";
			valuestowrite[1]=double2String(100*variabilitySI/variabilityS);
			dw.write(valuestowrite);
			valuestowrite[0]="12";
			valuestowrite[1]=double2String(100*variabilityST/variabilityS);
			dw.write(valuestowrite);
			valuestowrite[0]="13";
			valuestowrite[1]=double2String(100*variabilitySIT/variabilityS);
			dw.write(valuestowrite);
			for (int j=0; j<tempvarname.length; j++)
			{
				valuestowrite[0]="c_"+tempvarname[j];
				valuestowrite[1]=double2String(meanu[j]);
				dw.write(valuestowrite);
			}


		}
		catch (Exception ex)
		{
			return new Result("%2033%<br>\n", false, null);
		}

		double[] creg=new double[tempvarname.length];
		double[] ireg=new double[tempvarname.length];
		double[] sbeta0=new double[creg.length];
		double[] sbeta1=new double[creg.length];
		for (int i=0; i<meanc.length; i++)
		{
			creg[i]=0;
			ireg[i]=0;
			sbeta0[i]=0;
			sbeta1[i]=0;
		}
		intime = ntime.keySet().iterator();
		while(intime.hasNext())
		{
			double tntime=intime.next().doubleValue();
			tn=ntime.get(new Double(tntime)).doubleValue();
			tm=timemeans.get(new Double(tntime));
			for (int i=0; i<tm.length; i++)
			{
				creg[i]=creg[i]+(tm[i]-1-meanu[i])*(tntime-meant);
				ireg[i]=ireg[i]+Math.pow((tntime-meant),2);
			}
		}
		double[] rsquarenum=new double[meanc.length];
		double[] rsquareden=new double[meanc.length];

		DataSetUtilities dsureg=new DataSetUtilities();
		Hashtable<String, String> clvareg=new Hashtable<String, String>();
		for (int j=0; j<tempvarname.length; j++)
		{
			clvareg.put(tempvarname[j], dict.getvarlabelfromname(tempvarname[j]));
		}
		dsureg.addnewvar("ref", "%2050%", Keywords.TEXTSuffix, clvareg, tempmd);
		dsureg.addnewvar("param", "%1000%", Keywords.TEXTSuffix, tempmd, tempmd);
		dsureg.addnewvar("intercept", "%1108%", Keywords.TEXTSuffix, tempmd, tempmd);
		dsureg.addnewvar("devstpar", "%2051%", Keywords.TEXTSuffix, tempmd, tempmd);
		dsureg.addnewvar("devstint", "%2052%", Keywords.TEXTSuffix, tempmd, tempmd);
		dsureg.addnewvar("rsquare", "%1122%", Keywords.TEXTSuffix, tempmd, tempmd);
		if (!dwreg.opendatatable(dsureg.getfinalvarinfo()))
			return new Result(dwreg.getmessage(), false, null);

		valuestowrite=new String[6];
		for (int i=0; i<meanc.length; i++)
		{
			creg[i]=creg[i]/ireg[i];
			ireg[i]=1-creg[i]*meant;
			rsquarenum[i]=0;
			rsquareden[i]=0;
		}
		double sxs2=0;
		double numo=0;
		intime = ntime.keySet().iterator();
		while(intime.hasNext())
		{
			numo++;
			double tntime=intime.next().doubleValue();
			sxs2=sxs2+Math.pow(tntime-meant,2);
			tm=timemeans.get(new Double(tntime));
			for (int i=0; i<tm.length; i++)
			{
				double predicted=ireg[i]+creg[i]*tntime;
				rsquarenum[i]=rsquarenum[i]+Math.pow(predicted-1,2);
				rsquareden[i]=rsquareden[i]+Math.pow(tm[i]-meanu[i],2);
				sbeta0[i]=sbeta0[i]+Math.pow((tm[i]-meanu[i]+1-predicted),2);
				sbeta1[i]=sbeta1[i]+Math.pow((tm[i]-meanu[i]+1-predicted),2);
			}
		}
		for (int i=0; i<meanc.length; i++)
		{
			sbeta0[i]=Math.sqrt( (sbeta0[i]/(numo-2))*(1/numo+((meant*meant)/sxs2)));
			sbeta1[i]=Math.sqrt( (sbeta1[i]/(numo-2))/sxs2);
			valuestowrite[0]=tempvarname[i];
			valuestowrite[1]=double2String(creg[i]);
			valuestowrite[2]=double2String(ireg[i]);
			valuestowrite[3]=double2String(sbeta1[i]);
			valuestowrite[4]=double2String(sbeta0[i]);
			valuestowrite[5]=double2String(rsquarenum[i]/rsquareden[i]);
			dwreg.write(valuestowrite);
		}


		if (writecov)
		{
			dsucorr=new DataSetUtilities();
			Hashtable<String, String> clvar=new Hashtable<String, String>();
			clvar.put("0", "%1362%");
			clvar.put("1", "%1363%");
			clvar.put("2", "%1364%");
			clvar.put("3", "%1365%");
			clvar.put("4", "%2028%");
			clvar.put("5", "%2029%");
			Hashtable<String, String> clvarn=new Hashtable<String, String>();
			dsucorr.addnewvar("rif", "%2027%", Keywords.TEXTSuffix, clvar, tempmd);
			for (int j=0; j<tempvarname.length; j++)
			{
				clvarn.put(tempvarname[j], dict.getvarlabelfromname(tempvarname[j]));
			}
			dsucorr.addnewvar("var", "%1355%", Keywords.TEXTSuffix, clvarn, tempmd);
			for (int j=0; j<tempvarname.length; j++)
			{
				dsucorr.addnewvar("v"+String.valueOf(j), dict.getvarlabelfromname(tempvarname[j]), Keywords.NUMSuffix, tempmd, tempmd);
			}
			if (!dwc.opendatatable(dsucorr.getfinalvarinfo()))
				return new Result(dwc.getmessage(), false, null);

			valuestowrite=new String[tempvarname.length+2];
			valuestowrite[0]="0";
			for (int i=0; i<tempvarname.length; i++)
			{
				valuestowrite[1]=tempvarname[i];
				for (int j=0; j<tempvarname.length; j++)
				{
					valuestowrite[j+2]=double2String(allS[i][j]);
				}
				dwc.write(valuestowrite);
			}

			valuestowrite[0]="2";
			for (int i=0; i<tempvarname.length; i++)
			{
				valuestowrite[1]=tempvarname[i];
				for (int j=0; j<tempvarname.length; j++)
				{
					valuestowrite[j+2]=double2String(allSI[i][j]);
				}
				dwc.write(valuestowrite);
			}

			valuestowrite[0]="3";
			for (int i=0; i<tempvarname.length; i++)
			{
				valuestowrite[1]=tempvarname[i];
				for (int j=0; j<tempvarname.length; j++)
				{
					valuestowrite[j+2]=double2String(allST[i][j]);
				}
				dwc.write(valuestowrite);
			}

			valuestowrite[0]="1";
			for (int i=0; i<tempvarname.length; i++)
			{
				valuestowrite[1]=tempvarname[i];
				for (int j=0; j<tempvarname.length; j++)
				{
					valuestowrite[j+2]=double2String(allSIT[i][j]);
				}
				dwc.write(valuestowrite);
			}

			valuestowrite[0]="4";
			for (int i=0; i<tempvarname.length; i++)
			{
				valuestowrite[1]=tempvarname[i];
				for (int j=0; j<tempvarname.length; j++)
				{
					valuestowrite[j+2]=double2String(allSIT[i][j]+allSI[i][j]);
				}
				dwc.write(valuestowrite);
			}

			valuestowrite[0]="5";
			for (int i=0; i<tempvarname.length; i++)
			{
				valuestowrite[1]=tempvarname[i];
				for (int j=0; j<tempvarname.length; j++)
				{
					valuestowrite[j+2]=double2String(allSIT[i][j]+allST[i][j]);
				}
				dwc.write(valuestowrite);
			}
		}
		valuestowrite=new String[2];
		for (int i=0; i<tempvarname.length; i++)
		{
			valuestowrite[0]="ii_"+String.valueOf(i+1);
			valuestowrite[1]=double2String(indexii[i]/variabilitySI);
			dw.write(valuestowrite);
		}
		for (int i=0; i<tempvarname.length; i++)
		{
			valuestowrite[0]="it_"+String.valueOf(i+1);
			valuestowrite[1]=double2String(indexit[i]/variabilityMST);
			dw.write(valuestowrite);
		}
		for (int i=0; i<tempvarname.length; i++)
		{
			valuestowrite[0]="iit_"+String.valueOf(i+1);
			valuestowrite[1]=double2String(indexiit[i]/variabilitySIT);
			dw.write(valuestowrite);
		}
		double iqreg=0;
		for (int i=0; i<meanc.length; i++)
		{
			iqreg=iqreg+(Math.pow(creg[i],2))*sxs2;
		}
		iqreg=(iqreg/numo);
		valuestowrite[0]="15";
		valuestowrite[1]=double2String(iqreg/variabilityST);
		dw.write(valuestowrite);
		for (int i=0; i<tempvarname.length; i++)
		{
			valuestowrite[0]="ig_"+String.valueOf(i+1);
			valuestowrite[1]=double2String((indexit[i]+iqreg)/variabilityS);
			dw.write(valuestowrite);
		}

		boolean resclo2=dw.close();
		if (!resclo2)
			return new Result(dw.getmessage(), false, null);
		Vector<Hashtable<String, String>> ctablevariableinfo2=dw.getVarInfo();
		Hashtable<String, String> cdatatableinfo2=dw.getTableInfo();
		result.add(new LocalDictionaryWriter(dw.getdictpath(), "DfaModel1 general info "+dict.getkeyword(), "DfaModel1 general info "+dict.getdescription(), author, dw.gettabletype(),
		cdatatableinfo2, dsu.getfinalvarinfo(), ctablevariableinfo2, dsu.getfinalcl(), dsu.getfinalmd(), null));
		boolean resclo1=dwstmd.close();
		if (!resclo1)
			return new Result(dwstmd.getmessage(), false, null);
		Vector<Hashtable<String, String>> ctablevariableinfo1=dwstmd.getVarInfo();
		Hashtable<String, String> cdatatableinfo1=dwstmd.getTableInfo();
		result.add(new LocalDictionaryWriter(dwstmd.getdictpath(), "DfaModel1 decomposition on ST mean "+dict.getkeyword(), "DfaModel1 decomposition on ST mean "+dict.getdescription(), author, dwstmd.gettabletype(),
		cdatatableinfo1, dsustmd.getfinalvarinfo(), ctablevariableinfo1, dsustmd.getfinalcl(), dsustmd.getfinalmd(), null));

		boolean resclo3=dwreg.close();
		if (!resclo3)
			return new Result(dwreg.getmessage(), false, null);
		Vector<Hashtable<String, String>> ctablevariableinfo3=dwreg.getVarInfo();
		Hashtable<String, String> cdatatableinfo3=dwreg.getTableInfo();
		result.add(new LocalDictionaryWriter(dwreg.getdictpath(), "DfaModel1 info on ST regression "+dict.getkeyword(), "DfaModel1 info on ST regression  "+dict.getdescription(), author, dwreg.gettabletype(),
		cdatatableinfo3, dsureg.getfinalvarinfo(), ctablevariableinfo3, dsureg.getfinalcl(), dsureg.getfinalmd(), null));
		if (writecov)
		{
			boolean resclosem=dwc.close();
			if (!resclosem)
				return new Result(dwc.getmessage(), false, null);
			Vector<Hashtable<String, String>> ctablevariableinfo=dwc.getVarInfo();
			Hashtable<String, String> cdatatableinfo=dwc.getTableInfo();
			result.add(new LocalDictionaryWriter(dwc.getdictpath(), "DfaModel1 Covariances matrices "+dict.getkeyword(), "DfaModel1 Covariances matrices "+dict.getdescription(), author, dwc.gettabletype(),
			cdatatableinfo, dsucorr.getfinalvarinfo(), ctablevariableinfo, dsucorr.getfinalcl(), dsucorr.getfinalmd(), null));
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
		parameters.add(new GetRequiredParameters(Keywords.dict+"=", "dict", true, 721, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.OUT.toLowerCase()+"=", "setting=out", true, 2035, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.OUTSTMD.toLowerCase()+"=", "setting=out", true, 2031, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.OUTSTREG.toLowerCase()+"=", "setting=out", true, 2049, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.OUTC.toLowerCase()+"=", "setting=out", false, 1361, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.viewout, "checkbox", false, 1612, dep, "", 1));
		dep = new String[1];
		dep[0]=Keywords.dict;
		parameters.add(new GetRequiredParameters(Keywords.var, "vars=all", true, 1347, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.varobs, "var=all", true, 1348, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.vartime, "vars=all", false, 1349, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.weight, "vars=all", false, 656, dep, "", 2));
		dep = new String[0];
		parameters.add(new GetRequiredParameters(Keywords.where,"text", false, 2805,dep,"",2));
		parameters.add(new GetRequiredParameters("", "note", false, 2806, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.samplevariance, "checkbox", false, 861, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.transform, "listsingle=1357_"+Keywords.notransform+",647_"+Keywords.standardize+",648_"+Keywords.divformax+",649_"+Keywords.normalize01
		+",650_"+Keywords.meannormalize+",651_"+Keywords.sumnormalize,false, 654, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.replace, "listsingle=530_NULL,531_"+Keywords.replaceall+",532_"+Keywords.replaceformat+",533_"+Keywords.replacemissing,false, 534, dep, "", 2));
		return parameters;
	}
	/**
	*Returns the group and the name of the procedure
	*/
	public String[] getstepinfo()
	{
		String[] retprocinfo=new String[2];
		retprocinfo[0]="1344";
		retprocinfo[1]="1360";
		return retprocinfo;
	}
}
