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

import java.util.Hashtable;
import java.util.Vector;

import ADaMSoft.dataaccess.GroupedMatrix2Dfile;
import ADaMSoft.utilities.MatrixSort;
import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.linalg.EigenvalueDecomposition;

/**
* This method implements the correspondence analysis
* @author marco.scarno@gmail.com
* @date 13/02/2017
*/
public class CorrespondenceEvaluator
{
	Hashtable<Vector<String>, double[][]> eigenvec;
	Hashtable<Vector<String>, double[]> eigenval;
	String errormsg;
	/**
	*Evaluate the eigenvectors and the eigenvalues of the correspondence analysis, by receiving the group modalities, the data array, the number of variables
	*/
	public CorrespondenceEvaluator (VarGroupModalities vgm, GroupedMatrix2Dfile filedata, int nvar, boolean usememory)
	{
		errormsg="";
		eigenvec=new Hashtable<Vector<String>, double[][]>();
		eigenval=new Hashtable<Vector<String>, double[]>();
		int numgroup=vgm.getTotal();
		if (numgroup==0)
			numgroup=1;
		boolean errorinvalues=false;
		for (int g=0; g<numgroup; g++)
		{
			Vector<String> tempgroup=vgm.getvectormodalities(g);
			int nobs=filedata.getRows(tempgroup);
			if (!usememory)
			{
				DoubleMatrix2D mat=null;
				try
				{
					mat=DoubleFactory2D.dense.make(nvar, nvar);
				}
				catch (Exception ex)
				{
					mat=null;
					errormsg=ex.toString();
					if (errormsg.startsWith("java.lang.IllegalArgumentException"))
						errormsg="Error: "+errormsg.substring("java.lang.IllegalArgumentException".length());
					errormsg=errormsg+"<br>\n";
					System.gc();
					return;
				}
				double[] sumrow=new double[nobs];
				double[] sumcol=new double[nvar];
				boolean coliszero=false;
				boolean rowiszero=false;
				for(int j = 0; j<nvar; j++)
				{
					sumcol[j]=0;
					for (int i=0; i<nobs; i++)
					{
						sumcol[j] +=filedata.read(tempgroup, i, j);
					}
					if (sumcol[j]==0)
						coliszero=true;
				}
				for(int i = 0; i<nobs; i++)
				{
					sumrow[i]=0;
					for (int j=0; j<nvar; j++)
					{
						sumrow[i] +=filedata.read(tempgroup, i, j);
					}
					if (sumrow[i]==0)
						rowiszero=true;
				}
				if (coliszero)
				{
					sumrow=new double[0];
					sumcol=new double[0];
					errormsg="%962%<br>\n";
					break;
				}
				if (rowiszero)
				{
					sumrow=new double[0];
					sumcol=new double[0];
					errormsg="%963%<br>\n";
					break;
				}
				for (int a=0; a<nvar; a++)
				{
					for (int b=0; b<nvar; b++)
					{
						double value=0;
						for (int i=0; i<nobs; i++)
						{
							double vala=filedata.read(tempgroup, i, a);
							double valb=filedata.read(tempgroup, i, b);
							if ((vala!=0) && (valb!=0))
								value +=vala*valb/(Math.sqrt(sumcol[b]*sumrow[i])*Math.sqrt(sumcol[a]*sumrow[i]));
						}
						if (Double.isNaN(value))
							errorinvalues=true;
						mat.set(a, b, value);
					}
				}
				if (errorinvalues)
				{
					errormsg="%2538%<br>\n";
					return;
				}
				try
				{
					EigenvalueDecomposition ed=new EigenvalueDecomposition(mat);
					DoubleMatrix2D vec=ed.getV();
					DoubleMatrix1D val=ed.getRealEigenvalues();
					double[] matval=val.toArray();
					double[][] matvec=vec.toArray();
					MatrixSort ms=new MatrixSort(matval, matvec);
					matval=ms.getorderedvector();
					matvec=ms.getorderedmatrix();
					double[] newmatval=new double[matval.length-1];
					double[][] newmatvec=new double[matvec.length][matvec[0].length-1];
					for (int i=1; i<matval.length; i++)
					{
						newmatval[i-1]=matval[i];
					}
					for (int i=0; i<matvec.length; i++)
					{
						for (int j=1; j<matvec[0].length; j++)
						{
							newmatvec[i][j-1]=matvec[i][j];
						}
					}
					eigenval.put(tempgroup, newmatval);
					eigenvec.put(tempgroup, newmatvec);
				}
				catch (Exception mate)
				{
					sumrow=new double[0];
					sumcol=new double[0];
					mat=null;
					errormsg=mate.toString();
					if (errormsg.startsWith("java.lang.IllegalArgumentException"))
						errormsg="Error "+errormsg.substring("java.lang.IllegalArgumentException".length());
					errormsg=errormsg+"<br>\n";
					System.gc();
					return;
				}
			}
			else
			{
				DoubleMatrix2D mat=null;
				double[][] values=null;
				try
				{
					mat=DoubleFactory2D.dense.make(nvar, nvar);
					values=new double[nobs][nvar];
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
				double[] sumrow=new double[nobs];
				double[] sumcol=new double[nvar];
				boolean coliszero=false;
				boolean rowiszero=false;
				for(int j = 0; j<nvar; j++)
				{
					sumcol[j]=0;
					for (int i=0; i<nobs; i++)
					{
						values[i][j]=filedata.read(tempgroup, i, j);
						sumcol[j] +=values[i][j];
					}
					if (sumcol[j]==0)
						coliszero=true;
				}
				for(int i = 0; i<nobs; i++)
				{
					sumrow[i]=0;
					for (int j=0; j<nvar; j++)
					{
						sumrow[i] +=values[i][j];
					}
					if (sumrow[i]==0)
						rowiszero=true;
				}
				if (coliszero)
				{
					sumrow=new double[0];
					sumcol=new double[0];
					errormsg="%962%<br>\n";
					break;
				}
				if (rowiszero)
				{
					sumrow=new double[0];
					sumcol=new double[0];
					errormsg="%963%<br>\n";
					break;
				}
				for (int a=0; a<nvar; a++)
				{
					for (int b=0; b<nvar; b++)
					{
						double value=0;
						for (int i=0; i<nobs; i++)
						{
							if ((values[i][a]!=0) && (values[i][b]!=0))
								value +=values[i][a]*values[i][b]/(Math.sqrt(sumcol[b]*sumrow[i])*Math.sqrt(sumcol[a]*sumrow[i]));
						}
						mat.set(a, b, value);
					}
				}
				values=new double[0][0];
				values=null;
				if (errorinvalues)
				{
					errormsg="%2538%<br>\n";
					return;
				}
				try
				{
					EigenvalueDecomposition ed=new EigenvalueDecomposition(mat);
					DoubleMatrix2D vec=ed.getV();
					DoubleMatrix1D val=ed.getRealEigenvalues();
					double[] matval=val.toArray();
					double[][] matvec=vec.toArray();
					MatrixSort ms=new MatrixSort(matval, matvec);
					matval=ms.getorderedvector();
					matvec=ms.getorderedmatrix();
					double[] newmatval=new double[matval.length-1];
					double[][] newmatvec=new double[matvec.length][matvec[0].length-1];
					for (int i=1; i<matval.length; i++)
					{
						newmatval[i-1]=matval[i];
					}
					for (int i=0; i<matvec.length; i++)
					{
						for (int j=1; j<matvec[0].length; j++)
						{
							newmatvec[i][j-1]=matvec[i][j];
						}
					}
					eigenval.put(tempgroup, newmatval);
					eigenvec.put(tempgroup, newmatvec);
				}
				catch (Exception mate)
				{
					sumrow=new double[0];
					sumcol=new double[0];
					mat=null;
					errormsg=mate.toString();
					if (errormsg.startsWith("java.lang.IllegalArgumentException"))
						errormsg="Error "+errormsg.substring("java.lang.IllegalArgumentException".length());
					errormsg=errormsg+"<br>\n";
					System.gc();
					return;
				}
			}
		}
	}
	/**
	*Returns the eigenvectors
	*/
	public Hashtable<Vector<String>, double[][]> geteigenvectors()
	{
		return eigenvec;
	}
	/**
	*Returns the eigenvalues
	*/
	public Hashtable<Vector<String>, double[]> geteigenvalues()
	{
		return eigenval;
	}
	/**
	*Returns the error
	*/
	public String geterror()
	{
		return errormsg;
	}
}
