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

/**
* This method evaluates the principal component for several variables, also according do the values of the grouping variables
* @author marco.scarno@gmail.com
* @date 13/02/2017
*/
public class PCAEvaluator
{
	Hashtable<Vector<String>, double[][]> meanx;
	Hashtable<Vector<String>, double[][]> meany;
	Hashtable<Vector<String>, double[][]> ssqxy;
	Hashtable<Vector<String>, double[][]> ssqx;
	Hashtable<Vector<String>, double[][]> ssqy;
	Hashtable<Vector<String>, double[][]> numxy;
	Hashtable<Vector<String>, double[]> eigenvalues;
	Hashtable<Vector<String>, double[][]> eigenvectors;
	boolean pairwise;
	boolean samplevariance;
	String errormsg;
	/**
	*Initialise the main Objects, that will contains the means, the number of valid cases, the sum of squares, etc.
	*/
	public PCAEvaluator (boolean pairwise, boolean samplevariance)
	{
		errormsg="";
		this.pairwise=pairwise;
		this.samplevariance=samplevariance;
		meanx=new Hashtable<Vector<String>, double[][]>();
		meany=new Hashtable<Vector<String>, double[][]>();
		ssqxy=new Hashtable<Vector<String>, double[][]>();
		ssqx=new Hashtable<Vector<String>, double[][]>();
		ssqy=new Hashtable<Vector<String>, double[][]>();
		numxy=new Hashtable<Vector<String>, double[][]>();
		eigenvalues=new Hashtable<Vector<String>, double[]>();
		eigenvectors=new Hashtable<Vector<String>, double[][]>();
	}
	/**
	* Evaluate the variance components
	*/
	public void setValue(Vector<String> groupval, double[] valrow, double w)
	{
		double[][] mx=meanx.get(groupval);
		boolean ismissing=false;
		if (!pairwise)
		{
			for (int i=0; i<valrow.length; i++)
			{
				if (Double.isNaN(valrow[i]))
					ismissing=true;
			}
			if (ismissing)
			{
				for (int i=0; i<valrow.length; i++)
				{
					valrow[i]=Double.NaN;
				}
			}
		}
		if (mx==null)
		{
			mx=new double[valrow.length][valrow.length];
			double[][] my=new double[valrow.length][valrow.length];
			double[][] sx=new double[valrow.length][valrow.length];
			double[][] sy=new double[valrow.length][valrow.length];
			double[][] sxy=new double[valrow.length][valrow.length];
			double[][] nxy=new double[valrow.length][valrow.length];
			for (int i=0; i<valrow.length; i++)
			{
				for (int j=0; j<valrow.length; j++)
				{
					if ((!Double.isNaN(valrow[i])) && (!Double.isNaN(valrow[j])))
					{
						sxy[i][j]=valrow[i]*valrow[j]*w;
						nxy[i][j]=w;
						mx[i][j]=valrow[i]*w;
						sx[i][j]=valrow[i]*valrow[i]*w;
						my[i][j]=valrow[j]*w;
						sy[i][j]=valrow[j]*valrow[j]*w;
					}
					else
					{
						sxy[i][j]=Double.NaN;
						nxy[i][j]=Double.NaN;
						mx[i][j]=Double.NaN;
						sx[i][j]=Double.NaN;
						my[i][j]=Double.NaN;
						sy[i][j]=Double.NaN;
					}
				}
			}
			meanx.put(groupval, mx);
			meany.put(groupval, my);
			ssqx.put(groupval, sx);
			ssqy.put(groupval, sy);
			ssqxy.put(groupval, sxy);
			numxy.put(groupval, nxy);
		}
		else
		{
			double[][] my=meany.get(groupval);
			double[][] sx=ssqx.get(groupval);
			double[][] sy=ssqy.get(groupval);
			double[][] sxy=ssqxy.get(groupval);
			double[][] nxy=numxy.get(groupval);
			for (int i=0; i<valrow.length; i++)
			{
				for (int j=0; j<valrow.length; j++)
				{
					if ((!Double.isNaN(valrow[i])) && (!Double.isNaN(valrow[j])))
					{
						if (!Double.isNaN(sxy[i][j]))
							sxy[i][j]=sxy[i][j]+valrow[i]*valrow[j]*w;
						else
							sxy[i][j]=valrow[i]*valrow[j]*w;
						if (!Double.isNaN(nxy[i][j]))
							nxy[i][j]=nxy[i][j]+w;
						else
							nxy[i][j]=w;
						if (!Double.isNaN(mx[i][j]))
							mx[i][j]=mx[i][j]+valrow[i]*w;
						else
							mx[i][j]=valrow[i]*w;
						if (!Double.isNaN(sx[i][j]))
							sx[i][j]=sx[i][j]+valrow[i]*valrow[i]*w;
						else
							sx[i][j]=valrow[i]*valrow[i]*w;
						if (!Double.isNaN(my[i][j]))
							my[i][j]=my[i][j]+valrow[j]*w;
						else
							my[i][j]=valrow[j]*w;
						if (!Double.isNaN(sy[i][j]))
							sy[i][j]=sy[i][j]+valrow[j]*valrow[j]*w;
						else
							sy[i][j]=valrow[j]*valrow[j]*w;
					}
				}
			}
			meanx.put(groupval, mx);
			meany.put(groupval, my);
			ssqx.put(groupval, sx);
			ssqy.put(groupval, sy);
			ssqxy.put(groupval, sxy);
			numxy.put(groupval, nxy);
		}
	}
	/**
	*Finalizes the calculation
	*/
	public void calculate()
	{
		for (Enumeration<Vector<String>> e = meany.keys() ; e.hasMoreElements() ;)
		{
			Vector<String> gv= e.nextElement();
			double[][] mx=meanx.get(gv);
			double[][] my=meany.get(gv);
			double[][] sxy=ssqxy.get(gv);
			double[][] nxy=numxy.get(gv);
			DoubleMatrix2D mat=null;
			try
			{
				mat=DoubleFactory2D.dense.make(mx.length, mx[0].length);
			}
			catch (Exception ex)
			{
				mat=null;
				errormsg=ex.toString();
				if (errormsg.startsWith("java.lang.IllegalArgumentException"))
					errormsg="Error "+errormsg.substring("java.lang.IllegalArgumentException".length());
				errormsg=errormsg+"<br>\n";
				System.gc();
				return;
			}
			for (int i=0; i<mx.length; i++)
			{
				for (int j=0; j<mx[0].length; j++)
				{
					try
					{
						double temp=sxy[i][j]/nxy[i][j]-((mx[i][j]/nxy[i][j])*(my[i][j]/nxy[i][j]));
						if ((!samplevariance) && (nxy[i][j]>1))
							temp=(temp*nxy[i][j])/(nxy[i][j]-1);
						if (Double.isNaN(temp))
						{
							errormsg="%1631%<br>\n";
							return;
						}
						mat.set(i, j, temp);
					}
					catch (Exception ex)
					{
						errormsg="%746%<br>\n";
						return;
					}
				}
			}
			try
			{
				EigenvalueDecomposition ed=new EigenvalueDecomposition(mat);
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
				eigenvalues.put(gv, matval);
				eigenvectors.put(gv, matvec);
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
		meanx.clear();
		meany.clear();
		ssqx.clear();
		ssqy.clear();
		ssqxy.clear();
		numxy.clear();
		meanx=null;
		meany=null;
		ssqx=null;
		ssqy=null;
		ssqxy=null;
		numxy=null;
	}
	/**
	*Gives back the eigenvalues
	*/
	public Hashtable<Vector<String>, double[]> geteigenvalues()
	{
		return eigenvalues;
	}
	/**
	*Gives back the eigenvectors
	*/
	public Hashtable<Vector<String>, double[][]> geteigenvectors()
	{
		return eigenvectors;
	}
	public String geterror()
	{
		return errormsg;
	}
}
