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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Hashtable;
import java.util.Vector;
import ADaMSoft.keywords.Keywords;
import ADaMSoft.dataaccess.GroupedMatrix2Dfile;
import cern.colt.matrix.DoubleFactory1D;
import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;

/**
* This method implements the linear logistic regression
* @author marco.scarno@gmail.com
* @date 13/02/2017
*/
public class LogisticEvaluator
{
	int nvar;
	int niter;
	Hashtable<Vector<String>, double[]> finalcoeff;
	Hashtable<Vector<String>, double[]> statistic;
	Hashtable<Vector<String>, double[]> finalse;
	boolean converged;
	boolean nan;
	boolean onenotconverged;
	int maxiter;
	double min_reached;
	int records_for_gui;
	int records_done;
	/**
	*Initialise the main Objects, that will contains the seeds and the other values used to update these
	*/
	public LogisticEvaluator (int niter, int nvar)
	{
		this.nvar=nvar;
		this.niter=niter;
		finalcoeff=new Hashtable<Vector<String>, double[]>();
		finalse=new Hashtable<Vector<String>, double[]>();
		statistic=new Hashtable<Vector<String>, double[]>();
		converged=true;
		nan=false;
		onenotconverged=false;
		maxiter=0;
		min_reached=0;
		records_for_gui=-1;
		records_done=0;
	}
	public void setRecords_for_gui(int records_for_gui)
	{
		this.records_for_gui=records_for_gui;
	}
	/**
	*Prints the matrices in a file
	*/
	public void printonfile(String filetowrite, VarGroupModalities vgm, GroupedMatrix2Dfile valx, GroupedMatrix2Dfile valy, GroupedMatrix2Dfile valw)
	{
		try
		{
			File file = new File(filetowrite);
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			Vector<Vector<String>> groupref=vgm.getfinalmodalities();
			int numgroup=vgm.getTotal();
			if (numgroup==0)
				numgroup=1;
			double tc=0;
			double ww=0.0;
			double y=0.0;
			for (int g=0; g<groupref.size(); g++)
			{
				Vector<String> groupvalues=groupref.get(g);
				int nobs=valx.getRows(groupvalues);
				String towrite="";
				bw.write("Actual group\n");
				for (int h=0; h<nobs; h++)
				{
					towrite="";
					y=valy.read(groupvalues, h, 0);
					ww=valw.read(groupvalues, h, 0);
					for (int b=0; b<nvar; b++)
					{
						tc=valx.read(groupvalues, h, b);
						towrite=towrite+String.valueOf(tc)+"\t";
					}
					towrite=towrite+String.valueOf(y)+"\t";
					towrite=towrite+String.valueOf(ww)+"\n";
					bw.write(towrite);
				}
				bw.write("End actual group\n\n");
			}
			bw.close();
		}
		catch (Exception e) {}
	}
	/**
	*Estimate the parameters
	*/
	public String estimate(VarGroupModalities vgm, GroupedMatrix2Dfile valx, GroupedMatrix2Dfile valy, GroupedMatrix2Dfile valw)
	{
		if (records_for_gui!=-1)
		{
			Keywords.percentage_total=records_for_gui*niter;
		}
		min_reached=Double.MAX_VALUE;
		nan=false;
		String message="";
		Vector<Vector<String>> groupref=vgm.getfinalmodalities();
		int numgroup=vgm.getTotal();
		if (numgroup==0)
			numgroup=1;
		onenotconverged=false;
		for (int g=0; g<groupref.size(); g++)
		{
			converged=false;
			Vector<String> groupvalues=groupref.get(g);
			int nobs=valx.getRows(groupvalues);
			double[] coeff=new double[nvar];
			double[] oldcoeff=new double[nvar];
			try
			{
				DoubleMatrix2D MatXAXI=DoubleFactory2D.dense.make(nvar, nvar);
				DoubleMatrix2D MatXAX=DoubleFactory2D.dense.make(nvar, nvar);
				DoubleMatrix1D MatXAZ=DoubleFactory1D.dense.make(nvar);
				DoubleMatrix1D matcoeff=null;
				for (int i=0; i<nvar; i++)
				{
					coeff[i]=0;
					oldcoeff[i]=coeff[i];
				}
				double diff=0;
				double positive=0;
				double maxdiff=0;
				double ttobs=0;
				for (int i=0; i<niter; i++)
				{
					if (i>maxiter) maxiter=i;
					positive=0;
					ttobs=0;
					double[][] xax=new double[nvar][nvar];
					double[] xaz=new double[nvar];
					for (int a=0; a<nvar; a++)
					{
						for (int b=0; b<nvar; b++)
						{
							xax[a][b]=0.0;
						}
						xaz[a]=0.0;
					}

					double f=0.0;
					double ww=0.0;
					double y=0.0;
					double yp=0.0;
					double fw=0.0;

					double[] teco=new double[nvar];

					for (int h=0; h<nobs; h++)
					{
						records_done++;
						if (records_for_gui!=-1) Keywords.percentage_done=records_done;
						y=valy.read(groupvalues, h, 0);
						ww=valw.read(groupvalues, h, 0);
						ttobs=ttobs+ww;
						if (y>.9)
							positive=positive+ww;
						f=0.0;
						for (int b=0; b<nvar; b++)
						{
							teco[b]=valx.read(groupvalues, h, b);
							f=f+teco[b]*coeff[b];
						}
						//fw=ww*sigma(f)*(1-sigma(f));
						fw=sigma(f)*(1-sigma(f));
						if (i==0)
							fw=1;
						for (int a=0; a<nvar; a++)
						{
							for (int b=0; b<nvar; b++)
							{
								xax[a][b]+=teco[a]*teco[b]*fw*ww;
								//xax[a][b]+=teco[a]*teco[b]*fw;
							}
							yp=f+(y-sigma(f))/(sigma(f)*(1-sigma(f)));
							xaz[a]+=teco[a]*yp*fw*ww;
						}
					}

					Algebra al=new Algebra();
					MatXAX=DoubleFactory2D.dense.make(nvar, nvar);
					MatXAZ=DoubleFactory1D.dense.make(nvar);

					for (int a=0; a<nvar; a++)
					{
						for (int b=0; b<nvar; b++)
						{
							MatXAX.set(a, b, xax[a][b]);
						}
						MatXAZ.set(a, xaz[a]);
					}

					MatXAXI=al.inverse(MatXAX);
					matcoeff=al.mult(MatXAXI, MatXAZ);
					coeff=matcoeff.toArray();
					diff=0;
					maxdiff=0;
					for (int c=0; c<nvar; c++)
					{
						if (Double.isNaN(coeff[c])) nan=true;
						if (Math.abs(oldcoeff[c])<0.01)
						{
							diff=coeff[c]-oldcoeff[c];
						}
						else
						{
							diff=(coeff[c]-oldcoeff[c])/oldcoeff[c];
						}
						if (Math.abs(diff)>maxdiff)
						{
							maxdiff=Math.abs(diff);
						}
						oldcoeff[c]=coeff[c];
					}
					finalcoeff.put(groupvalues, coeff);
					if (nan) break;
					if (maxdiff<min_reached)
						min_reached=maxdiff;
					if (maxdiff<0.00000001)
					{
						converged=true;
						break;
					}
				}
				if (!converged)
					onenotconverged=true;
				if (!nan)
				{
					double[] stat=new double[11];
					stat[0]=0;
					stat[4]=positive;
					stat[8]=0;
					stat[9]=0;
					stat[10]=0;
					double ma=0;
					double mb=0;
					double mc=0;
					double md=0;
					double totalobs=0;
					for (int n=0; n<nobs; n++)
					{
						double unitweight=valw.read(groupvalues, n, 0);
						totalobs+=unitweight;
						double f=0;
						for (int j=0; j<nvar; j++)
						{
							double x=valx.read(groupvalues, n, j);
							f=f+x*coeff[j];
						}
						f=pred(f);
						double y=valy.read(groupvalues, n, 0);
						if (y>.5)
						{
							stat[0]=stat[0]+Math.log(f)*unitweight;
							if (f>(positive/ttobs))
								md+=unitweight;
							else
								mc+=unitweight;
						}
						else
						{
							stat[0]=stat[0]+Math.log(1-f)*unitweight;
							if (f>(positive/ttobs))
								mb+=unitweight;
							else
								ma+=unitweight;
						}
					}
					double[] se=new double[nvar];
					for (int i=0; i<nvar; i++)
					{
						se[i]=Math.sqrt(MatXAXI.get(i, i));
					}
					stat[0]=-2*stat[0];
					stat[1]=stat[0]+2*(nvar);
					stat[2]=stat[0]+(nvar)*Math.log(nobs);
					stat[3]=totalobs;
					stat[5]=Math.abs(((ma/(ma+mb))*(md/(md+mc)))-((mb/(ma+mb))*(mc/(md+mc))));
					stat[6]=100*(ma+md)/(ma+mb+mc+md);
					stat[7]=100*(mb+mc)/(ma+mb+mc+md);
					stat[8]=-2*(positive*Math.log(positive/totalobs)+(totalobs-positive)*Math.log(1-positive/totalobs));
					stat[9]=stat[8]+2; /*Akaike*/
					stat[10]=stat[8]+Math.log(nobs); /*Schwarz*/
					statistic.put(groupvalues, stat);
					finalse.put(groupvalues, se);
				}
			}
			catch (Exception e)
			{
				String emessage=e.toString();
				if (emessage.startsWith("java.lang.IllegalArgumentException"))
					emessage="Error "+emessage.substring("java.lang.IllegalArgumentException".length());
				message="%995%\n"+emessage+"\n";
			}
		}
		return message;
	}
	/**
	*Estimate the parameters
	*/
	public String estimate(VarGroupModalities vgm, Hashtable<Vector<String>, Vector<double[]>> valx, Hashtable<Vector<String>, Vector<Double>> valy, Hashtable<Vector<String>, Vector<Double>> valw)
	{
		if (records_for_gui!=-1)
		{
			Keywords.percentage_total=records_for_gui*niter;
		}
		min_reached=Double.MAX_VALUE;
		String message="";
		Vector<Vector<String>> groupref=vgm.getfinalmodalities();
		int numgroup=vgm.getTotal();
		if (numgroup==0)
			numgroup=1;
		onenotconverged=false;
		for (int g=0; g<groupref.size(); g++)
		{
			converged=false;
			Vector<String> groupvalues=groupref.get(g);
			Vector<double[]> tempmat=valx.get(groupvalues);
			int nobs=tempmat.size();
			Vector<Double> realy=valy.get(groupvalues);
			Vector<Double> realw=valw.get(groupvalues);
			double[] coeff=new double[nvar];
			double[] oldcoeff=new double[nvar];
			try
			{
				DoubleMatrix2D MatXAXI=DoubleFactory2D.dense.make(nvar, nvar);
				DoubleMatrix2D MatXAX=DoubleFactory2D.dense.make(nvar, nvar);
				DoubleMatrix1D MatXAZ=DoubleFactory1D.dense.make(nvar);
				DoubleMatrix1D matcoeff=null;
				for (int i=0; i<nvar; i++)
				{
					coeff[i]=0;
					oldcoeff[i]=coeff[i];
				}
				double diff=0;
				double positive=0;
				double ttobs=0;
				double maxdiff=0;
				for (int i=0; i<niter; i++)
				{
					if (i>maxiter) maxiter=i;
					positive=0;
					ttobs=0;
					double[][] xax=new double[nvar][nvar];
					double[] xaz=new double[nvar];
					for (int a=0; a<nvar; a++)
					{
						for (int b=0; b<nvar; b++)
						{
							xax[a][b]=0.0;
						}
						xaz[a]=0.0;
					}

					double f=0.0;
					double ww=0.0;
					double y=0.0;
					double yp=0.0;
					double fw=0.0;

					double[] teco=new double[nvar];

					for (int h=0; h<nobs; h++)
					{
						records_done++;
						if (records_for_gui!=-1) Keywords.percentage_done=records_done;
						y=(realy.get(h)).doubleValue();
						ww=(realw.get(h)).doubleValue();
						ttobs=ttobs+ww;
						if (y>.9)
							positive=positive+ww;
						f=0.0;
						teco=tempmat.get(h);
						for (int b=0; b<nvar; b++)
						{
							f=f+teco[b]*coeff[b];
						}
						fw=sigma(f)*(1-sigma(f));
						//fw=ww*sigma(f)*(1-sigma(f));
						if (i==0)
							fw=1;
						for (int a=0; a<nvar; a++)
						{
							for (int b=0; b<nvar; b++)
							{
								xax[a][b]+=teco[a]*teco[b]*fw*ww;
								//xax[a][b]+=teco[a]*teco[b]*fw;
							}
							yp=f+(y-sigma(f))/(sigma(f)*(1-sigma(f)));
							xaz[a]+=teco[a]*yp*fw*ww;
							//xaz[a]+=teco[a]*yp*fw;
						}
					}

					Algebra al=new Algebra();
					MatXAX=DoubleFactory2D.dense.make(nvar, nvar);
					MatXAZ=DoubleFactory1D.dense.make(nvar);

					for (int a=0; a<nvar; a++)
					{
						for (int b=0; b<nvar; b++)
						{
							MatXAX.set(a, b, xax[a][b]);
						}
						MatXAZ.set(a, xaz[a]);
					}

					MatXAXI=al.inverse(MatXAX);
					matcoeff=al.mult(MatXAXI, MatXAZ);
					coeff=matcoeff.toArray();
					diff=0;
					maxdiff=0;
					for (int c=0; c<nvar; c++)
					{
						if (Double.isNaN(coeff[c])) nan=true;
						if (Math.abs(oldcoeff[c])<0.01)
						{
							diff=coeff[c]-oldcoeff[c];
						}
						else
						{
							diff=(coeff[c]-oldcoeff[c])/oldcoeff[c];
						}
						if (Math.abs(diff)>maxdiff)
						{
							maxdiff=Math.abs(diff);
						}
						oldcoeff[c]=coeff[c];
					}
					finalcoeff.put(groupvalues, coeff);
					if (nan) break;
					if (maxdiff<min_reached)
						min_reached=maxdiff;
					if (maxdiff<0.00000001)
					{
						converged=true;
						break;
					}
				}
				if (!converged)
					onenotconverged=true;
				if (!nan)
				{
					double[] stat=new double[11];
					stat[0]=0;
					stat[4]=positive;
					stat[8]=0;
					stat[9]=0;
					stat[10]=0;
					double ma=0;
					double mb=0;
					double mc=0;
					double md=0;
					double totalobs=0;
					double[] teco;
					for (int n=0; n<nobs; n++)
					{
						double unitweight=(realw.get(n)).doubleValue();
						totalobs+=unitweight;
						double f=0;
						teco=tempmat.get(n);
						for (int j=0; j<nvar; j++)
						{
							f=f+teco[j]*coeff[j];
						}
						f=pred(f);
						double y=(realy.get(n)).doubleValue();
						if (y>.5)
						{
							stat[0]=stat[0]+Math.log(f)*unitweight;
							if (f>(positive/ttobs))
								md+=unitweight;
							else
								mc+=unitweight;
						}
						else
						{
							stat[0]=stat[0]+Math.log(1-f)*unitweight;
							if (f>(positive/ttobs))
								mb+=unitweight;
							else
								ma+=unitweight;
						}
					}
					double[] se=new double[nvar];
					for (int i=0; i<nvar; i++)
					{
						se[i]=Math.sqrt(MatXAXI.get(i, i));
					}
					stat[0]=-2*stat[0];
					stat[1]=stat[0]+2*(nvar);
					stat[2]=stat[0]+(nvar)*Math.log(nobs);
					stat[3]=totalobs;
					stat[5]=Math.abs(((ma/(ma+mb))*(md/(md+mc)))-((mb/(ma+mb))*(mc/(md+mc))));
					stat[6]=100*(ma+md)/(ma+mb+mc+md);
					stat[7]=100*(mb+mc)/(ma+mb+mc+md);
					stat[8]=-2*(positive*Math.log(positive/totalobs)+(totalobs-positive)*Math.log(1-positive/totalobs));
					stat[9]=stat[8]+2; /*Akaike*/
					//stat[10]=stat[8]+Math.log(totalobs); /*Schwarz*/
					stat[10]=stat[8]+Math.log(nobs); /*Schwarz*/
					statistic.put(groupvalues, stat);
					finalse.put(groupvalues, se);
				}
			}
			catch (Exception e)
			{
				String emessage=e.toString();
				if (emessage.startsWith("java.lang.IllegalArgumentException"))
					emessage="Error "+emessage.substring("java.lang.IllegalArgumentException".length());
				message="%995%\n"+emessage+"\n";
			}
		}
		return message;
	}
	/**
	*The sigma value
	*/
	private double sigma(double val)
	{
		return 1/(1+Math.exp(-1*val));
	}
	/**
	*The predicted probability
	*/
	private double pred(double val)
	{
		return (Math.exp(val))/(1+(Math.exp(val)));
	}
	/**
	*Returns the estimated coefficients
	*/
	public Hashtable<Vector<String>, double[]> getfinalcoeff()
	{
		return finalcoeff;
	}
	/**
	*Returns the standard error of the estimated coefficients
	*/
	public Hashtable<Vector<String>, double[]> getfinalse()
	{
		return finalse;
	}
	/**
	*Returns the fitting statistics of the model
	*/
	public Hashtable<Vector<String>, double[]> getstats()
	{
		return statistic;
	}
	/**
	*Returns the convergence status (false=not converged)
	*/
	public boolean getconverged()
	{
		return onenotconverged;
	}
	/**
	*Returns the error in parameter estimate (true=missing parameter)
	*/
	public boolean geterror()
	{
		return nan;
	}
	/**
	*Returns the maximum iterations
	*/
	public int getmaxiter()
	{
		return maxiter;
	}
	/**
	*Returns the minimum value of the convergence condition reached
	*/
	public double getmin_reached()
	{
		return min_reached;
	}
}
