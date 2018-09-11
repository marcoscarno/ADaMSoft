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

import ADaMSoft.utilities.MatrixSort;
import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.linalg.EigenvalueDecomposition;
import cern.colt.matrix.linalg.Algebra;

/**
* This method evaluates the canonical components for two groups of variables, also according do the values of the grouping variables
* @author marco.scarno@gmail.com
* @date 13/02/2017
*/
public class CanonicalEvaluator
{
	Hashtable<Vector<String>, double[][]> meanxi;
	Hashtable<Vector<String>, double[][]> meanxj;
	Hashtable<Vector<String>, double[][]> meanyi;
	Hashtable<Vector<String>, double[][]> meanyj;
	Hashtable<Vector<String>, double[][]> ssqx;
	Hashtable<Vector<String>, double[][]> ssqy;
	Hashtable<Vector<String>, double[][]> numx;
	Hashtable<Vector<String>, double[][]> numy;
	Hashtable<Vector<String>, double[][]> meanxyi;
	Hashtable<Vector<String>, double[][]> meanxyj;
	Hashtable<Vector<String>, double[][]> ssqxy;
	Hashtable<Vector<String>, double[][]> numxy;
	Hashtable<Vector<String>, double[]> eigenvalues;
	Hashtable<Vector<String>, double[][]> a;
	Hashtable<Vector<String>, double[][]> b;
	boolean samplevariance;
	String errormsg;
	int p;
	int q;
	/**
	*Initialise the main Objects, that will contains the means, the number of valid cases, the sum of squares, etc.
	*/
	public CanonicalEvaluator (boolean samplevariance, int p, int q)
	{
		errormsg="";
		this.samplevariance=samplevariance;
		this.p=p;
		this.q=q;
		meanxyi=new Hashtable<Vector<String>, double[][]>();
		meanxyj=new Hashtable<Vector<String>, double[][]>();
		ssqxy=new Hashtable<Vector<String>, double[][]>();
		numxy=new Hashtable<Vector<String>, double[][]>();
		meanxi=new Hashtable<Vector<String>, double[][]>();
		meanyi=new Hashtable<Vector<String>, double[][]>();
		meanxj=new Hashtable<Vector<String>, double[][]>();
		meanyj=new Hashtable<Vector<String>, double[][]>();
		ssqx=new Hashtable<Vector<String>, double[][]>();
		ssqy=new Hashtable<Vector<String>, double[][]>();
		numy=new Hashtable<Vector<String>, double[][]>();
		numx=new Hashtable<Vector<String>, double[][]>();
		eigenvalues=new Hashtable<Vector<String>, double[]>();
		a=new Hashtable<Vector<String>, double[][]>();
		b=new Hashtable<Vector<String>, double[][]>();
	}
	/**
	* Evaluate the variance components
	*/
	public void setValue(Vector<String> groupval, double[] valrow, double[] valcol, double w)
	{
		if (meanxi.get(groupval)==null)
		{
			double[][] mxi=new double[p][p];
			double[][] mxj=new double[p][p];
			double[][] myi=new double[q][q];
			double[][] myj=new double[q][q];
			double[][] sx=new double[p][p];
			double[][] sy=new double[q][q];
			double[][] nx=new double[p][p];
			double[][] ny=new double[q][q];
			double[][] mxyi=new double[p][q];
			double[][] mxyj=new double[p][q];
			double[][] sxy=new double[p][q];
			double[][] nxy=new double[p][q];
			for (int i=0; i<p; i++)
			{
				for (int j=0; j<p; j++)
				{
					sx[i][j]=valrow[i]*valrow[j]*w;
					nx[i][j]=w;
					mxi[i][j]=valrow[i]*w;
					mxj[i][j]=valrow[j]*w;
				}
			}
			for (int i=0; i<q; i++)
			{
				for (int j=0; j<q; j++)
				{
					sy[i][j]=valcol[i]*valcol[j]*w;
					ny[i][j]=w;
					myi[i][j]=valcol[i]*w;
					myj[i][j]=valcol[j]*w;
				}
			}

			for (int i=0; i<p; i++)
			{
				for (int j=0; j<q; j++)
				{
					sxy[i][j]=valrow[i]*valcol[j]*w;
					nxy[i][j]=w;
					mxyi[i][j]=valrow[i]*w;
					mxyj[i][j]=valcol[j]*w;
				}
			}
			meanxi.put(groupval, mxi);
			meanxj.put(groupval, mxj);
			meanyi.put(groupval, myi);
			meanyj.put(groupval, myj);
			meanxyi.put(groupval, mxyi);
			meanxyj.put(groupval, mxyj);
			ssqxy.put(groupval, sxy);
			numxy.put(groupval, nxy);
			ssqx.put(groupval, sx);
			ssqy.put(groupval, sy);
			numx.put(groupval, nx);
			numy.put(groupval, ny);
		}
		else
		{
			double[][] mxi=meanxi.get(groupval);
			double[][] mxj=meanxj.get(groupval);
			double[][] myi=meanyi.get(groupval);
			double[][] myj=meanyj.get(groupval);
			double[][] mxyi=meanxyi.get(groupval);
			double[][] mxyj=meanxyj.get(groupval);
			double[][] sxy=ssqxy.get(groupval);
			double[][] nxy=numxy.get(groupval);
			double[][] sx=ssqx.get(groupval);
			double[][] sy=ssqy.get(groupval);
			double[][] nx=numx.get(groupval);
			double[][] ny=numy.get(groupval);
			for (int i=0; i<p; i++)
			{
				for (int j=0; j<p; j++)
				{
					sx[i][j]=sx[i][j]+valrow[i]*valrow[j]*w;
					nx[i][j]=nx[i][j]+w;
					mxi[i][j]=mxi[i][j]+valrow[i]*w;
					mxj[i][j]=mxj[i][j]+valrow[j]*w;
				}
			}
			for (int i=0; i<q; i++)
			{
				for (int j=0; j<q; j++)
				{
					sy[i][j]=sy[i][j]+valcol[i]*valcol[j]*w;
					ny[i][j]=ny[i][j]+w;
					myi[i][j]=myi[i][j]+valcol[i]*w;
					myj[i][j]=myj[i][j]+valcol[j]*w;
				}
			}

			for (int i=0; i<p; i++)
			{
				for (int j=0; j<q; j++)
				{
					sxy[i][j]=sxy[i][j]+valrow[i]*valcol[j]*w;
					nxy[i][j]=nxy[i][j]+w;
					mxyi[i][j]=mxyi[i][j]+valrow[i]*w;
					mxyj[i][j]=mxyj[i][j]+valcol[j]*w;
				}
			}
			meanxi.put(groupval, mxi);
			meanxj.put(groupval, mxj);
			meanyi.put(groupval, myi);
			meanyj.put(groupval, myj);
			meanxyi.put(groupval, mxyi);
			meanxyj.put(groupval, mxyj);
			ssqxy.put(groupval, sxy);
			numxy.put(groupval, nxy);
			ssqx.put(groupval, sx);
			ssqy.put(groupval, sy);
			numx.put(groupval, nx);
			numy.put(groupval, ny);
		}
	}
	/**
	*Finalizes the calculation
	*/
	public void calculate()
	{
		for (Enumeration<Vector<String>> e = meanxi.keys() ; e.hasMoreElements() ;)
		{
			Vector<String> groupval= e.nextElement();
			double[][] mxi=meanxi.get(groupval);
			double[][] mxj=meanxj.get(groupval);
			double[][] myi=meanyi.get(groupval);
			double[][] myj=meanyj.get(groupval);
			double[][] mxyi=meanxyi.get(groupval);
			double[][] mxyj=meanxyj.get(groupval);
			double[][] sxy=ssqxy.get(groupval);
			double[][] nxy=numxy.get(groupval);
			double[][] sx=ssqx.get(groupval);
			double[][] sy=ssqy.get(groupval);
			double[][] nx=numx.get(groupval);
			double[][] ny=numy.get(groupval);
			DoubleMatrix2D v11=null;
			DoubleMatrix2D v22=null;
			DoubleMatrix2D v12=null;
			DoubleMatrix2D v21=null;
			try
			{
				v11=DoubleFactory2D.dense.make(p, p);
				v22=DoubleFactory2D.dense.make(q, q);
				v12=DoubleFactory2D.dense.make(p, q);
				v21=DoubleFactory2D.dense.make(q, p);
			}
			catch (Exception ex)
			{
				v11=null;
				v22=null;
				v12=null;
				v21=null;
				errormsg=ex.toString();
				if (errormsg.startsWith("java.lang.IllegalArgumentException"))
					errormsg="Error "+errormsg.substring("java.lang.IllegalArgumentException".length());
				errormsg=errormsg+"<br>\n";
				System.gc();
				return;
			}
			for (int i=0; i<p; i++)
			{
				for (int j=0; j<p; j++)
				{
					try
					{
						double temp=sx[i][j]/nx[i][j]-((mxi[i][j]/nx[i][j])*(mxj[i][j]/nx[i][j]));
						if ((!samplevariance) && (nx[i][j]>1))
							temp=(temp*nx[i][j])/(nx[i][j]-1);
						if (Double.isNaN(temp))
						{
							errormsg="%1631%<br>\n";
							return;
						}
						v11.set(i, j, temp);
					}
					catch (Exception ex)
					{
						errormsg="%746%<br>\n";
						return;
					}
				}
				for (int j=0; j<q; j++)
				{
					try
					{
						double temp=sxy[i][j]/nxy[i][j]-((mxyi[i][j]/nxy[i][j])*(mxyj[i][j]/nxy[i][j]));
						if ((!samplevariance) && (nxy[i][j]>1))
							temp=(temp*nxy[i][j])/(nxy[i][j]-1);
						if (Double.isNaN(temp))
						{
							errormsg="%1631%<br>\n";
							return;
						}
						v12.set(i, j, temp);
					}
					catch (Exception ex)
					{
						errormsg="%746%<br>\n";
						return;
					}
				}
			}
			for (int i=0; i<q; i++)
			{
				for (int j=0; j<q; j++)
				{
					try
					{
						double temp=sy[i][j]/ny[i][j]-((myi[i][j]/ny[i][j])*(myj[i][j]/ny[i][j]));
						if ((!samplevariance) && (ny[i][j]>1))
							temp=(temp*ny[i][j])/(ny[i][j]-1);
						if (Double.isNaN(temp))
						{
							errormsg="%1631%<br>\n";
							return;
						}
						v22.set(i, j, temp);
					}
					catch (Exception ex)
					{
						errormsg="%746%<br>\n";
						return;
					}
				}
				for (int j=0; j<p; j++)
				{
					v21.set(i, j, v12.get(j, i));
				}
			}
			try
			{
				Algebra algebra=new Algebra();
				DoubleMatrix2D iv11=algebra.inverse(v11);
				DoubleMatrix2D iv22=algebra.inverse(v22);
				if (p<=q)
				{
					DoubleMatrix2D matm1=algebra.mult(iv11, v12);
					DoubleMatrix2D matm2=algebra.mult(iv22, v21);
					DoubleMatrix2D matres=algebra.mult(matm1, matm2);
					EigenvalueDecomposition ed=new EigenvalueDecomposition(matres);
					DoubleMatrix2D vec=ed.getV();
					DoubleMatrix1D val=ed.getRealEigenvalues();
					double[] matval=val.toArray();
					for (int i=0; i<matval.length; i++)
					{
						if (matval[i]<0)
							matval[i]=0;
					}
					double[][] matvec=vec.toArray();
					MatrixSort ms=new MatrixSort(matval, matvec);
					matval=ms.getorderedvector();
					matvec=ms.getorderedmatrix();
					eigenvalues.put(groupval, matval);
					a.put(groupval, matvec);
					for (int i=0; i<matvec.length; i++)
					{
						for (int j=0; j<matvec[0].length; j++)
						{
							vec.set(i, j,matvec[i][j]);
						}
					}
					DoubleMatrix2D b1=algebra.mult(iv22, v21);
					DoubleMatrix2D bm=algebra.mult(b1, vec);
					double[][] matb=bm.toArray();
					double[][] realb=new double[q][p];
					for (int i=0; i<q; i++)
					{
						for (int j=0; j<p; j++)
						{
							try
							{
								realb[i][j]=(1/Math.sqrt(matval[j]))*matb[i][j];
							}
							catch (Exception eb)
							{
								realb[i][j]=Double.NaN;
							}
						}
					}
					b.put(groupval, realb);
				}
				else
				{
					DoubleMatrix2D matm1=algebra.mult(iv22, v21);
					DoubleMatrix2D matm2=algebra.mult(iv11, v12);
					DoubleMatrix2D matres=algebra.mult(matm1, matm2);
					EigenvalueDecomposition ed=new EigenvalueDecomposition(matres);
					DoubleMatrix2D vec=ed.getV();
					DoubleMatrix1D val=ed.getRealEigenvalues();
					double[] matval=val.toArray();
					for (int i=0; i<matval.length; i++)
					{
						if (matval[i]<0)
							matval[i]=0;
					}
					double[][] matvec=vec.toArray();
					MatrixSort ms=new MatrixSort(matval, matvec);
					matval=ms.getorderedvector();
					matvec=ms.getorderedmatrix();
					eigenvalues.put(groupval, matval);
					b.put(groupval, matvec);
					for (int i=0; i<matvec.length; i++)
					{
						for (int j=0; j<matvec[0].length; j++)
						{
							vec.set(i, j,matvec[i][j]);
						}
					}
					DoubleMatrix2D a1=algebra.mult(iv11, v12);
					DoubleMatrix2D am=algebra.mult(a1, vec);
					double[][] mata=am.toArray();
					double[][] reala=new double[p][q];
					for (int i=0; i<p; i++)
					{
						for (int j=0; j<q; j++)
						{
							try
							{
								reala[i][j]=(1/Math.sqrt(matval[j]))*mata[i][j];
							}
							catch (Exception eb)
							{
								reala[i][j]=Double.NaN;
							}
						}
					}
					a.put(groupval, reala);
				}
			}
			catch (Exception ex)
			{
				errormsg=ex.toString();
				if (errormsg.startsWith("java.lang.IllegalArgumentException"))
					errormsg="Error\n"+errormsg.substring("java.lang.IllegalArgumentException".length());
				errormsg=errormsg+"<br>\n";
				System.gc();
				return;
			}
		}
		meanxi.clear();
		meanxj.clear();
		meanyi.clear();
		meanyj.clear();
		ssqx.clear();
		ssqy.clear();
		numx.clear();
		numy.clear();
		meanxyi.clear();
		meanxyj.clear();
		numxy.clear();
		ssqxy.clear();

		meanxi=null;
		meanxj=null;
		meanyi=null;
		meanyj=null;
		ssqx=null;
		ssqy=null;
		numx=null;
		numy=null;
		meanxyi=null;
		meanxyj=null;
		numxy=null;
		ssqxy=null;

		System.gc();
	}
	/**
	*Gives back the eigenvalues
	*/
	public Hashtable<Vector<String>, double[]> geteigenvalues()
	{
		return eigenvalues;
	}
	/**
	*Gives back the a vectors
	*/
	public Hashtable<Vector<String>, double[][]> geta()
	{
		return a;
	}
	/**
	*Gives back the b vectors
	*/
	public Hashtable<Vector<String>, double[][]> getb()
	{
		return b;
	}
	public String geterror()
	{
		return errormsg;
	}
}
