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

package ADaMSoft.algorithms;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleFactory1D;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;

/**
* This method evaluates the parameter values for a linear regression model
* @author marco.scarno@gmail.com
* @date 13/02/2017
*/
public class RegEvaluator
{
	Hashtable<Vector<String>, double[][]> matxx;
	Hashtable<Vector<String>, double[][]> invmatxx;
	Hashtable<Vector<String>, double[]> matxy;
	Hashtable<Vector<String>, double[]> coeff;
	Hashtable<Vector<String>, double[]> standarderror;
	Hashtable<Vector<String>, Double> modelss;
	Hashtable<Vector<String>, Double> models;
	Hashtable<Vector<String>, Double> sse;
	Hashtable<Vector<String>, Double> n;
	Hashtable<Vector<String>, Double> mean;
	Hashtable<Vector<String>, Double> ssq;
	Hashtable<Vector<String>, Double> realvar;

	Hashtable<Vector<String>, String> errormsg;

	boolean noint;
	boolean pairwise;

	int numgroup;
	int erroronproc;

	/**
	*Initialise the main Objects
	*/
	public RegEvaluator (boolean noint, boolean pairwise)
	{
		erroronproc=0;
		numgroup=1;
		this.noint=noint;
		this.pairwise=pairwise;
		matxx=new Hashtable<Vector<String>, double[][]>();
		invmatxx=new Hashtable<Vector<String>, double[][]>();
		matxy=new Hashtable<Vector<String>, double[]>();
		coeff=new Hashtable<Vector<String>, double[]>();
		standarderror=new Hashtable<Vector<String>, double[]>();
		sse=new Hashtable<Vector<String>, Double>();
		n=new Hashtable<Vector<String>, Double>();
		ssq=new Hashtable<Vector<String>, Double>();
		modelss=new Hashtable<Vector<String>, Double>();
		models=new Hashtable<Vector<String>, Double>();
		mean=new Hashtable<Vector<String>, Double>();
		realvar=new Hashtable<Vector<String>, Double>();
		errormsg=new Hashtable<Vector<String>, String>();
	}
	/**
	*Receive the already estimate parameters
	*/
	public void setcoeff(Hashtable<Vector<String>, double[]> coeff)
	{
		this.coeff=coeff;
	}
	/**
	*Used in order to estimate, for each record, the sum of double products between the independent and dependent variables
	*/
	public void setValue(Vector<String> groupval, double[] valx, double[] valy, double w)
	{
		boolean ismissing=false;
		if (Double.isNaN(valy[0]))
			ismissing=true;
		if (!pairwise)
		{
			for (int i=0; i<valx.length; i++)
			{
				if (Double.isNaN(valx[i]))
					ismissing=true;
			}
		}
		if (Double.isNaN(w))
			ismissing=true;
		if (ismissing)
			return;
		double[][] sxx=matxx.get(groupval);
		double[] sxy=matxy.get(groupval);
		if (sxx==null)
		{
			if (noint)
			{
				sxx=new double[valx.length][valx.length];
				sxy=new double[valx.length];
				for (int i=0; i<valx.length; i++)
				{
					for (int j=0; j<valx.length; j++)
					{
						if ((!Double.isNaN(valx[i])) && (!Double.isNaN(valx[j])) && (!Double.isNaN(w)))
							sxx[i][j]=valx[i]*valx[j]*w;
						else
							sxx[i][j]=0;
					}
					if ((!Double.isNaN(valx[i])) && (!Double.isNaN(w)) && (!Double.isNaN(valy[0])))
						sxy[i]=valx[i]*valy[0]*w;
					else
						sxy[i]=0;
				}
			}
			else
			{
				sxx=new double[valx.length+1][valx.length+1];
				sxy=new double[valx.length+1];
				for (int i=0; i<valx.length+1; i++)
				{
					double tempx=1;
					if (i<valx.length)
						tempx=valx[i];
					for (int j=0; j<valx.length+1; j++)
					{
						double tempxx=1;
						if (j<valx.length)
							tempxx=valx[j];
						if ((!Double.isNaN(tempx)) && (!Double.isNaN(tempxx)) && (!Double.isNaN(w)) && (!Double.isNaN(valy[0])))
							sxx[i][j]=tempx*tempxx*w;
						else
							sxx[i][j]=0;
					}
					if ((!Double.isNaN(tempx)) && (!Double.isNaN(w)) && (!Double.isNaN(valy[0])))
						sxy[i]=tempx*valy[0]*w;
					else
						sxy[i]=0;
				}
			}
		}
		else
		{
			if (noint)
			{
				for (int i=0; i<valx.length; i++)
				{
					for (int j=0; j<valx.length; j++)
					{
						if ((!Double.isNaN(valx[i])) && (!Double.isNaN(valx[j])) && (!Double.isNaN(w)) && (!Double.isNaN(valy[0])))
							sxx[i][j]=sxx[i][j]+valx[i]*valx[j]*w;
					}
					if ((!Double.isNaN(valx[i])) && (!Double.isNaN(w)) && (!Double.isNaN(valy[0])))
						sxy[i]=sxy[i]+valx[i]*valy[0]*w;
				}
			}
			else
			{
				for (int i=0; i<valx.length+1; i++)
				{
					double tempx=1;
					if (i<valx.length)
						tempx=valx[i];
					for (int j=0; j<valx.length+1; j++)
					{
						double tempxx=1;
						if (j<valx.length)
							tempxx=valx[j];
						if ((!Double.isNaN(tempx)) && (!Double.isNaN(tempxx)) && (!Double.isNaN(w)) && (!Double.isNaN(valy[0])))
							sxx[i][j]=sxx[i][j]+tempx*tempxx*w;
					}
					if ((!Double.isNaN(tempx)) && (!Double.isNaN(w)) && (!Double.isNaN(valy[0])))
						sxy[i]=sxy[i]+tempx*valy[0]*w;
				}
			}
		}
		matxx.put(groupval,sxx);
		matxy.put(groupval,sxy);
	}
	/**
	*Used to estimate the parameters; returns false in case of error
	*/
	public void estimate()
	{
		numgroup=matxx.size();
		for (Enumeration<Vector<String>> e = matxx.keys() ; e.hasMoreElements() ;)
		{
			Vector<String> gv= e.nextElement();
			double[][] xx=matxx.get(gv);
			double[] xy=matxy.get(gv);
			DoubleMatrix2D MatXX=null;
			DoubleMatrix1D MatXY=null;
			try
			{
				if (xx.length>1)
				{
					MatXX=DoubleFactory2D.dense.make(xx.length, xx[0].length);
					MatXY=DoubleFactory1D.dense.make(xy.length);
					for (int i=0; i<xx.length; i++)
					{
						for (int j=0; j<xx[0].length; j++)
						{
							MatXX.set(i, j, xx[i][j]);
						}
						MatXY.set(i, xy[i]);
					}
					Algebra algebra=new Algebra();
					DoubleMatrix2D MatXXI=algebra.inverse(MatXX);
					double[][] tempinvmatxx=new double[xx.length][xx[0].length];
					for (int i=0; i<xx.length; i++)
					{
						for (int j=0; j<xx[0].length; j++)
						{
							tempinvmatxx[i][j]=MatXXI.get(i, j);
						}
					}
					invmatxx.put(gv, tempinvmatxx);
					DoubleMatrix1D MatCoeff=algebra.mult(MatXXI, MatXY);
					double[] tempcoeff=MatCoeff.toArray();
					coeff.put(gv, tempcoeff);
				}
				else
				{
					double[] tempcoeff=new double[1];
					tempcoeff[0]=xy[0]/xx[0][0];
					double[][] tempinvmatxx=new double[1][1];
					tempinvmatxx[0][0]=1/xx[0][0];
					invmatxx.put(gv, tempinvmatxx);
					coeff.put(gv, tempcoeff);
				}
			}
			catch (Exception es)
			{
				String errormsgs=es.toString();
				if (errormsgs.startsWith("java.lang.IllegalArgumentException:"))
				{
					errormsgs="Error: "+errormsgs.substring("java.lang.IllegalArgumentException:".length());
					errormsgs=errormsgs+"\n";
				}
				else
					errormsgs="%893% "+es.toString()+"\n";
				MatXX=null;
				MatXY=null;
				errormsg.put(gv, errormsgs);
				erroronproc++;
			}
		}
	}
	/**
	*Contains the string of the error message for each grouping variables
	*/
	public Hashtable<Vector<String>, String> geterrormsgs()
	{
		return errormsg;
	}
	/**
	*Return true if there was en error in the procedure
	*/
	public int getError()
	{
		if (erroronproc==numgroup)
			return 1;
		else if (erroronproc>0)
			return 0;
		else return 0;
	}
	/**
	*Return the number of different groups
	*/
	public int getnumg()
	{
		return numgroup;
	}
	/**
	*Gives back the vector of parameters
	*/
	public Hashtable<Vector<String>, double[]> getresult()
	{
		return coeff;
	}
	/**
	*Used to evaluate several statistics by using the estimate parameters and the values of the dependent variables; return false in case of a not recognized values of the grouping variables
	*/
	public void regstat(Vector<String> groupval, double[] valx, double[] valy, double w)
	{
		if (errormsg.get(groupval)==null)
		{
			double[] realcoeff=coeff.get(groupval);
			if (realcoeff==null)
			{
				errormsg.put(groupval, "%895%\n");
				return;
			}
			boolean ismissing=false;
			for (int i=0; i<valx.length; i++)
			{
				if (Double.isNaN(valx[i]))
					ismissing=true;
			}
			if (ismissing)
				return;
			if (Double.isNaN(valy[0]))
				return;

			double pred=0;
			if (!noint)
				pred=realcoeff[realcoeff.length-1];

			for (int i=0; i<valx.length; i++)
			{
				pred=pred+valx[i]*realcoeff[i];
			}
			double deviation=(pred-valy[0])*(pred-valy[0])*w;
			double ss=pred*pred*w;
			double s=pred*w;

			boolean esse=false;
			boolean en=false;
			boolean isss=false;
			boolean iss=false;

			esse=(sse.get(groupval)!=null);
			en=(n.get(groupval)!=null);
			isss=(modelss.get(groupval)!=null);
			iss=(models.get(groupval)!=null);

			if (isss)
				ss=ss+(modelss.get(groupval)).doubleValue();
			modelss.put(groupval, new Double(ss));

				if (iss)
				s=s+(models.get(groupval)).doubleValue();
			models.put(groupval, new Double(s));

			double actualsse=deviation;
			double actualn=w;

			if (esse)
				actualsse=actualsse+(sse.get(groupval)).doubleValue();
			sse.put(groupval, new Double(actualsse));

			if (en)
				actualn=actualn+(n.get(groupval)).doubleValue();
			n.put(groupval, new Double(actualn));

			boolean ey=false;
			ey=(mean.get(groupval)!=null);
			if (!ey)
			{
				double tempmean=0;
				double tempssq=0;
				if ((!Double.isNaN(w)) && (!Double.isNaN(valy[0])))
				{
					tempmean=valy[0]*w;
					tempssq=valy[0]*valy[0]*w;
				}
				mean.put(groupval, new Double(tempmean));
				ssq.put(groupval, new Double(tempssq));
			}
			else
			{
				double tempmean=(mean.get(groupval)).doubleValue();
				double tempssq=(ssq.get(groupval)).doubleValue();
				if ((!Double.isNaN(w)) && (!Double.isNaN(valy[0])))
				{
					tempmean=tempmean+valy[0]*w;
					tempssq=tempssq+valy[0]*valy[0]*w;
				}
				mean.put(groupval, new Double(tempmean));
				ssq.put(groupval, new Double(tempssq));
			}
			return;
		}
	}
	/**
	*Evaluate several statistics for the regression (r-square, sum of squares of the residual, etc.)
	*/
	public boolean evaluatestat(int npar)
	{
		for (Enumeration<Vector<String>> e = sse.keys() ; e.hasMoreElements() ;)
		{
			Vector<String> gv= e.nextElement();
			if (errormsg.get(gv)==null)
			{
				double actualss=(modelss.get(gv)).doubleValue();
				double actualsu=(models.get(gv)).doubleValue();
				double actualsse=(sse.get(gv)).doubleValue();
				double actualn=(n.get(gv)).doubleValue();
				double actualmean=(mean.get(gv)).doubleValue();
				double actualssq=(ssq.get(gv)).doubleValue();
				double actuals=actualsse/(actualn-npar-1);
				double[][] actualcovb=invmatxx.get(gv);
				actualmean=actualmean/actualn;
				actualss=actualss-(2*actualmean*actualsu)+actualn*(actualmean*actualmean);
				actualssq=(actualssq/actualn)-(actualmean*actualmean);
				double[] actualstd=new double[actualcovb.length];
				for (int i=0; i<actualcovb.length; i++)
				{
					for (int j=0; j<actualcovb[0].length; j++)
					{
						if (i==j)
							actualstd[i]=Math.sqrt(actualcovb[i][j]*actuals);
						actualcovb[i][j]=actualcovb[i][j]*actuals;
					}
				}
				invmatxx.put(gv, actualcovb);
				mean.put(gv, actualmean);
				standarderror.put(gv, actualstd);
				ssq.put(gv, actualssq);
				modelss.put(gv, actualss);
			}
		}
		return true;
	}
	/**
	*Returns the matrix of covariance for the parameters
	*/
	public Hashtable<Vector<String>, double[][]> getcovb()
	{
		return invmatxx;
	}
	/**
	*Returns the sum of squares of the errors
	*/
	public Hashtable<Vector<String>, Double> getsse()
	{
		return sse;
	}
	/**
	*Returns the mean of the dependent variable
	*/
	public Hashtable<Vector<String>, Double> getymean()
	{
		return mean;
	}
	/**
	*Returns the variance of the dependent variable
	*/
	public Hashtable<Vector<String>, Double> getyvar()
	{
		return ssq;
	}
	/**
	*Returns the number of valid cases
	*/
	public Hashtable<Vector<String>, Double> getn()
	{
		return n;
	}
	/**
	*Returns the model sum of squares
	*/
	public Hashtable<Vector<String>, Double> getmodelss()
	{
		return modelss;
	}
	/**
	*Returns the standard error for the parameters
	*/
	public Hashtable<Vector<String>, double[]> getsdterr()
	{
		return standarderror;
	}
}
