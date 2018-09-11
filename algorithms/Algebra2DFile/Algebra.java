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

package ADaMSoft.algorithms.Algebra2DFile;

import ADaMSoft.dataaccess.GroupedMatrix2Dfile;
import ADaMSoft.dataaccess.Matrix2DFile;
import ADaMSoft.algorithms.VarGroupModalities;

import java.io.StringWriter;
import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.io.FileWriter;

import java.util.Vector;
import java.util.Hashtable;

/**
* These are methods for the linear algebra related to the 2D matrix files
* @author marco.scarno@gmail.com
* @date 13/02/2017
*/
public class Algebra
{
	String tempdir;
	VarGroupModalities vgm;
	String message;
	boolean iserror;
	/**
	* Linear Algebra methods for 2D Matrix files: receives the path of the temporary directory and the var group modalities
	*/
	public Algebra(String tempdir, VarGroupModalities vgm)
	{
		this.tempdir=tempdir;
		this.vgm=vgm;
		message="";
		iserror=false;
	}
	/**
	* Returns true in case of error
	*/
	public boolean getState()
	{
		return iserror;
	}
	/**
	* Returns the error message
	*/
	public String getMess()
	{
		return message;
	}
	/**
	* Sum two matrices (the result is a new matrix with elements that are the sum of the elements of the two matrices);
	*/
	public GroupedMatrix2Dfile sum (GroupedMatrix2Dfile A, GroupedMatrix2Dfile B)
	{
		GroupedMatrix2Dfile C=null;
		try
		{
			int numgroup=vgm.getTotal();
			if (numgroup==0)
				numgroup=1;
			int nvar=A.getColumns();
			int nvartest=B.getColumns();
			if (nvar!=nvartest)
			{
				iserror=true;
				message="Error in sum: matrices have a different number of columns";
				return null;
			}
			C=new GroupedMatrix2Dfile(tempdir, nvar);
			for (int g=0; g<numgroup; g++)
			{
				Vector<String> tempgroup=vgm.getvectormodalities(g);
				int nobs=A.getRows(tempgroup);
				int nobstest=B.getRows(tempgroup);
				if (nobs!=nobstest)
				{
					C.closeAll();
					iserror=true;
					message="Error in sum: matrices have a different number of rows";
					return null;
				}
				for (int a=0; a<nobs; a++)
				{
					double[] values=new double[nvar];
					for (int b=0; b<nvar; b++)
					{
						double vala=A.read(tempgroup, a, b);
						double valb=B.read(tempgroup, a, b);
						values[b]=vala+valb;
					}
					C.write(tempgroup, values);
				}
			}
			return C;
		}
		catch (Exception e)
		{
			C.closeAll();
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw, true);
			e.printStackTrace(pw);
			pw.flush();
			sw.flush();
			iserror=true;
			message="Error in sum:\n"+sw.toString();
			return null;
		}
	}
	/**
	* Substract two matrices (the result is a new matrix with elements that are the difference of the elements of the two matrices);
	*/
	public GroupedMatrix2Dfile sub (GroupedMatrix2Dfile A, GroupedMatrix2Dfile B)
	{
		GroupedMatrix2Dfile C=null;
		try
		{
			int numgroup=vgm.getTotal();
			if (numgroup==0)
				numgroup=1;
			int nvar=A.getColumns();
			int nvartest=B.getColumns();
			if (nvar!=nvartest)
			{
				iserror=true;
				message="Error in sub: matrices have a different number of columns";
				return null;
			}
			C=new GroupedMatrix2Dfile(tempdir, nvar);
			for (int g=0; g<numgroup; g++)
			{
				Vector<String> tempgroup=vgm.getvectormodalities(g);
				int nobs=A.getRows(tempgroup);
				int nobstest=A.getRows(tempgroup);
				if (nobs!=nobstest)
				{
					C.closeAll();
					iserror=true;
					message="Error in sub: matrices have a different number of rows";
					return null;
				}
				for (int a=0; a<nobs; a++)
				{
					double[] values=new double[nvar];
					for (int b=0; b<nvar; b++)
					{
						double vala=A.read(tempgroup, a, b);
						double valb=B.read(tempgroup, a, b);
						values[b]=vala-valb;
					}
					C.write(tempgroup, values);
				}
			}
			return C;
		}
		catch (Exception e)
		{
			C.closeAll();
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw, true);
			e.printStackTrace(pw);
			pw.flush();
			sw.flush();
			iserror=true;
			message="Error in sub:\n"+sw.toString();
			return null;
		}
	}
	/**
	* Sum five row-vectors (eventually by multipliyng each vector for a constant)<p>
	* The result is a new vector (i.e. a matrix 2d file with only one column)
	*/
	public GroupedMatrix2Dfile sumfivev (GroupedMatrix2Dfile A, GroupedMatrix2Dfile B, GroupedMatrix2Dfile C,
	GroupedMatrix2Dfile D, GroupedMatrix2Dfile E, double c1, double c2, double c3, double c4, double c5)
	{
		GroupedMatrix2Dfile R=null;
		try
		{
			int numgroup=vgm.getTotal();
			if (numgroup==0)
				numgroup=1;
			int nvarA=A.getColumns();
			int nvarB=B.getColumns();
			int nvarC=C.getColumns();
			int nvarD=D.getColumns();
			int nvarE=E.getColumns();
			if ((nvarA+nvarB+nvarC+nvarD+nvarE)!=5)
			{
				iserror=true;
				message="Error in sumfivev: it is required to use 5 row vectors";
				return null;
			}
			R=new GroupedMatrix2Dfile(tempdir, 1);
			for (int g=0; g<numgroup; g++)
			{
				Vector<String> tempgroup=vgm.getvectormodalities(g);
				int nobsA=A.getRows(tempgroup);
				int nobsB=B.getRows(tempgroup);
				int nobsC=C.getRows(tempgroup);
				int nobsD=D.getRows(tempgroup);
				int nobsE=E.getRows(tempgroup);
				if ((nobsA+nobsB+nobsC+nobsD+nobsE)!=(nobsA*5))
				{
					R.closeAll();
					iserror=true;
					message="Error in sumfivev: the vectors have a different number of rows";
					return null;
				}
				for (int a=0; a<nobsA; a++)
				{
					double[] values=new double[1];
					double vala=A.read(tempgroup, a, 0);
					double valb=B.read(tempgroup, a, 0);
					double valc=C.read(tempgroup, a, 0);
					double vald=D.read(tempgroup, a, 0);
					double vale=E.read(tempgroup, a, 0);
					values[0]=vala*c1+valb*c2+valc*c3+vald*c4+vale*c5;
					R.write(tempgroup, values);
				}
			}
			return R;
		}
		catch (Exception e)
		{
			R.closeAll();
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw, true);
			e.printStackTrace(pw);
			pw.flush();
			sw.flush();
			iserror=true;
			message="Error in sumfivev:\n"+sw.toString();
			return null;
		}
	}
	/**
	* Sum four row-vectors (eventually by multipliyng each vector for a constant) <p>
	* The result is a new vector (i.e. a matrix 2d file with only one column)
	*/
	public GroupedMatrix2Dfile sumfourv (GroupedMatrix2Dfile A, GroupedMatrix2Dfile B, GroupedMatrix2Dfile C,
	GroupedMatrix2Dfile D, double c1, double c2, double c3, double c4)
	{
		GroupedMatrix2Dfile R=null;
		try
		{
			int numgroup=vgm.getTotal();
			if (numgroup==0)
				numgroup=1;
			int nvarA=A.getColumns();
			int nvarB=B.getColumns();
			int nvarC=C.getColumns();
			int nvarD=D.getColumns();
			if ((nvarA+nvarB+nvarC+nvarD)!=4)
			{
				iserror=true;
				message="Error in sumfourv: it is required to use 4 row vectors";
				return null;
			}
			R=new GroupedMatrix2Dfile(tempdir, 1);
			for (int g=0; g<numgroup; g++)
			{
				Vector<String> tempgroup=vgm.getvectormodalities(g);
				int nobsA=A.getRows(tempgroup);
				int nobsB=B.getRows(tempgroup);
				int nobsC=C.getRows(tempgroup);
				int nobsD=D.getRows(tempgroup);
				if ((nobsA+nobsB+nobsC+nobsD)!=(nobsA*4))
				{
					R.closeAll();
					iserror=true;
					message="Error in sumfourv: the vectors have a different number of rows";
					return null;
				}
				for (int a=0; a<nobsA; a++)
				{
					double[] values=new double[1];
					double vala=A.read(tempgroup, a, 0);
					double valb=B.read(tempgroup, a, 0);
					double valc=C.read(tempgroup, a, 0);
					double vald=D.read(tempgroup, a, 0);
					values[0]=vala*c1+valb*c2+valc*c3+vald*c4;
					R.write(tempgroup, values);
				}
			}
			return R;
		}
		catch (Exception e)
		{
			R.closeAll();
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw, true);
			e.printStackTrace(pw);
			pw.flush();
			sw.flush();
			iserror=true;
			message="Error in sumfourv:\n"+sw.toString();
			return null;
		}
	}
	/**
	* Sum four row-vectors<p>
	* Each vector can be multiplied by a constant and, or, for another constant that depends to the current values of the grouping variables<p>
	* If the cc1, cc2, cc3, cc4 objects are null, than these last constants are 1
	*/
	public GroupedMatrix2Dfile sumfourv (GroupedMatrix2Dfile A, GroupedMatrix2Dfile B, GroupedMatrix2Dfile C,
	GroupedMatrix2Dfile D, double c1, double c2, double c3, double c4,  Hashtable<Vector<String>, Double> cc1,
	Hashtable<Vector<String>, Double> cc2,  Hashtable<Vector<String>, Double> cc3,
	Hashtable<Vector<String>, Double> cc4)
	{
		GroupedMatrix2Dfile R=null;
		try
		{
			int numgroup=vgm.getTotal();
			if (numgroup==0)
				numgroup=1;
			int nvarA=A.getColumns();
			int nvarB=B.getColumns();
			int nvarC=C.getColumns();
			int nvarD=D.getColumns();
			if ((nvarA+nvarB+nvarC+nvarD)!=4)
			{
				iserror=true;
				message="Error in sumfourv: it is required to use 4 row vectors";
				return null;
			}
			R=new GroupedMatrix2Dfile(tempdir, 1);
			for (int g=0; g<numgroup; g++)
			{
				Vector<String> tempgroup=vgm.getvectormodalities(g);
				int nobsA=A.getRows(tempgroup);
				int nobsB=B.getRows(tempgroup);
				int nobsC=C.getRows(tempgroup);
				int nobsD=D.getRows(tempgroup);
				if ((nobsA+nobsB+nobsC+nobsD)!=(nobsA*4))
				{
					R.closeAll();
					iserror=true;
					message="Error in sumfourv: the vectors have a different number of rows";
					return null;
				}
				double cc1v=1;
				double cc2v=1;
				double cc3v=1;
				double cc4v=1;
				if (cc1!=null)
				{
					if (cc1.get(tempgroup)==null)
					{
						R.closeAll();
						iserror=true;
						message="Error in sumfourv: one value of the grouping variables was not found in the constants to be used";
						return null;
					}
					cc1v=(cc1.get(tempgroup)).doubleValue();
				}
				if (cc2!=null)
				{
					if (cc2.get(tempgroup)==null)
					{
						R.closeAll();
						iserror=true;
						message="Error in sumfourv: one value of the grouping variables was not found in the constants to be used";
						return null;
					}
					cc2v=(cc2.get(tempgroup)).doubleValue();
				}
				if (cc3!=null)
				{
					if (cc3.get(tempgroup)==null)
					{
						R.closeAll();
						iserror=true;
						message="Error in sumfourv: one value of the grouping variables was not found in the constants to be used";
						return null;
					}
					cc3v=(cc2.get(tempgroup)).doubleValue();
				}
				if (cc4!=null)
				{
					if (cc4.get(tempgroup)==null)
					{
						R.closeAll();
						iserror=true;
						message="Error in sumfourv: one value of the grouping variables was not found in the constants to be used";
						return null;
					}
					cc4v=(cc4.get(tempgroup)).doubleValue();
				}
				for (int a=0; a<nobsA; a++)
				{
					double[] values=new double[1];
					double vala=A.read(tempgroup, a, 0);
					double valb=B.read(tempgroup, a, 0);
					double valc=C.read(tempgroup, a, 0);
					double vald=D.read(tempgroup, a, 0);
					values[0]=vala*c1*cc1v+valb*c2*cc2v+valc*c3*cc3v+vald*c4*cc4v;
					R.write(tempgroup, values);
				}
			}
			return R;
		}
		catch (Exception e)
		{
			R.closeAll();
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw, true);
			e.printStackTrace(pw);
			pw.flush();
			sw.flush();
			iserror=true;
			message="Error in sumfourv\n"+sw.toString();
			return null;
		}
	}

	/**
	* Substract a constant to all the elements of a matrix or of a row-vector
	*/
	public GroupedMatrix2Dfile subc (GroupedMatrix2Dfile A, double constant)
	{
		GroupedMatrix2Dfile C=null;
		try
		{
			int numgroup=vgm.getTotal();
			if (numgroup==0)
				numgroup=1;
			int nvar=A.getColumns();
			C=new GroupedMatrix2Dfile(tempdir, nvar);
			for (int g=0; g<numgroup; g++)
			{
				Vector<String> tempgroup=vgm.getvectormodalities(g);
				int nobs=A.getRows(tempgroup);
				for (int a=0; a<nobs; a++)
				{
					double[] values=new double[nvar];
					for (int b=0; b<nvar; b++)
					{
						double vala=A.read(tempgroup, a, b);
						values[b]=vala-constant;
					}
					C.write(tempgroup, values);
				}
			}
			return C;
		}
		catch (Exception e)
		{
			C.closeAll();
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw, true);
			e.printStackTrace(pw);
			pw.flush();
			sw.flush();
			iserror=true;
			message="Error in subc\n"+sw.toString();
			return null;
		}
	}
	/**
	* Sum two vectors (by multiplying the first for a constant) and, then,
	* by substracting a constant to all the elements<p>
	* I.e. (cA+B)-d <p>
	* where c=constmult and d=constant
	*/
	public GroupedMatrix2Dfile sumvmultcsubcost (GroupedMatrix2Dfile A, GroupedMatrix2Dfile B, double constmult, double constant)
	{
		GroupedMatrix2Dfile C=null;
		try
		{
			int numgroup=vgm.getTotal();
			if (numgroup==0)
				numgroup=1;
			int nvarA=A.getColumns();
			int nvarB=B.getColumns();
			if (nvarA+nvarB!=2)
			{
				iserror=true;
				message="Error in sumvmultcsubcost: it is required to use 2 row vectors";
				return null;
			}
			C=new GroupedMatrix2Dfile(tempdir, 1);
			for (int g=0; g<numgroup; g++)
			{
				Vector<String> tempgroup=vgm.getvectormodalities(g);
				int nobsA=A.getRows(tempgroup);
				int nobsB=B.getRows(tempgroup);
				if (nobsA!=nobsB)
				{
					C.closeAll();
					iserror=true;
					message="Error in sumvmultcsubcost: the vectors have a different number of rows";
					return null;
				}
				for (int a=0; a<nobsA; a++)
				{
					double[] values=new double[1];
					double vala=A.read(tempgroup, a, 0);
					double valb=B.read(tempgroup, a, 0);
					values[0]=vala*constmult+valb-constant;
					C.write(tempgroup, values);
				}
			}
			return C;
		}
		catch (Exception e)
		{
			C.closeAll();
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw, true);
			e.printStackTrace(pw);
			pw.flush();
			sw.flush();
			iserror=true;
			message="Error in sumvmultcsubcost\n"+sw.toString();
			return null;
		}
	}
	/**
	* Sum two vectors (by multiplying the first for a constant) and, then, by substracting a constant to all the elements<p>
	* I.e. (cA+B)-d <p>
	* where c=constmult and d=constant <p>
	* Note that c and d are different for each different value of the grouping variables
	*/
	public GroupedMatrix2Dfile sumvmultcsubcost (GroupedMatrix2Dfile A, GroupedMatrix2Dfile B, Hashtable<Vector<String>, Double> constmult, Hashtable<Vector<String>, Double> constant)
	{
		GroupedMatrix2Dfile C=null;
		try
		{
			int numgroup=vgm.getTotal();
			if (numgroup==0)
				numgroup=1;
			int nvarA=A.getColumns();
			int nvarB=B.getColumns();
			if (nvarA+nvarB!=2)
			{
				iserror=true;
				message="Error in sumvmultcsubcost: it is required to use 2 row vectors";
				return null;
			}
			C=new GroupedMatrix2Dfile(tempdir, 1);
			for (int g=0; g<numgroup; g++)
			{
				Vector<String> tempgroup=vgm.getvectormodalities(g);
				if ( (constmult.get(tempgroup)==null) || (constant.get(tempgroup)==null) )
				{
					C.closeAll();
					iserror=true;
					message="Error in sumvmultcsubcost: one value of the grouping variables was not found in the constants to be used";
					return null;
				}
				double constmultv=(constmult.get(tempgroup)).doubleValue();
				double constantv=(constant.get(tempgroup)).doubleValue();
				int nobsA=A.getRows(tempgroup);
				int nobsB=B.getRows(tempgroup);
				if (nobsA!=nobsB)
				{
					C.closeAll();
					iserror=true;
					message="Error in sumvmultcsubcost: the vectors have a different number of rows";
					return null;
				}
				for (int a=0; a<nobsA; a++)
				{
					double[] values=new double[1];
					double vala=A.read(tempgroup, a, 0);
					double valb=B.read(tempgroup, a, 0);
					values[0]=vala*constmultv+valb-constantv;
					C.write(tempgroup, values);
				}
			}
			return C;
		}
		catch (Exception e)
		{
			C.closeAll();
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw, true);
			e.printStackTrace(pw);
			pw.flush();
			sw.flush();
			iserror=true;
			message="Error in sumvmultcsubcost\n"+sw.toString();
			return null;
		}
	}

	/**
	* Multiply all the elements of a matrix or of a row-vector with a constant; then substract to all the elements the
	* product of the two constants (that are different for each value of the grouping variables) <p>
	* cV-c1*c2
	*/
	public GroupedMatrix2Dfile subvcost (GroupedMatrix2Dfile A, Hashtable<Vector<String>, Double> constmult,
	Hashtable<Vector<String>, Double> constant1, Hashtable<Vector<String>, Double> constant2)
	{
		GroupedMatrix2Dfile C=null;
		try
		{
			int numgroup=vgm.getTotal();
			if (numgroup==0)
				numgroup=1;
			int nvar=A.getColumns();
			C=new GroupedMatrix2Dfile(tempdir, nvar);
			for (int g=0; g<numgroup; g++)
			{
				Vector<String> tempgroup=vgm.getvectormodalities(g);
				if ( (constant1.get(tempgroup)==null) || (constant2.get(tempgroup)==null) || (constmult.get(tempgroup)==null) )
				{
					C.closeAll();
					iserror=true;
					message="Error in subvcost: one value of the grouping variables was not found in the constants to be used";
					return null;
				}
				double constant1v=(constant1.get(tempgroup)).doubleValue();
				double constant2v=(constant2.get(tempgroup)).doubleValue();
				double constmultv=(constmult.get(tempgroup)).doubleValue();
				int nobs=A.getRows(tempgroup);
				for (int a=0; a<nobs; a++)
				{
					double[] values=new double[nvar];
					for (int b=0; b<nvar; b++)
					{
						double vala=A.read(tempgroup, a, b);
						values[b]=vala*constmultv-constant1v*constant2v;
					}
					C.write(tempgroup, values);
				}
			}
			return C;
		}
		catch (Exception e)
		{
			C.closeAll();
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw, true);
			e.printStackTrace(pw);
			pw.flush();
			sw.flush();
			iserror=true;
			message="Error in subvcost\n"+sw.toString();
			return null;
		}
	}
	/**
	* Evaluate the product of two matrices (eventually by multiplying the product for a given constant)
	* c(AB)
	*/
	public GroupedMatrix2Dfile mult (GroupedMatrix2Dfile A, GroupedMatrix2Dfile B, double constant)
	{
		GroupedMatrix2Dfile C=null;
		try
		{
			int numgroup=vgm.getTotal();
			if (numgroup==0)
				numgroup=1;
			int nvarA=A.getColumns();
			int nvarB=B.getColumns();
			C=new GroupedMatrix2Dfile(tempdir, nvarB);
			for (int g=0; g<numgroup; g++)
			{
				Vector<String> tempgroup=vgm.getvectormodalities(g);
				int nobsA=A.getRows(tempgroup);
				int nobsB=B.getRows(tempgroup);
				if (nobsB!=nvarA)
				{
					C.closeAll();
					iserror=true;
					message="Error in mult: the number of rows in the second matrix are different from the number of columns in the first matrix";
					return null;
				}
				for (int a=0; a<nobsA; a++)
				{
					double[] values=new double[nvarB];
					for (int b=0; b<nvarB; b++)
					{
						values[b]=0;
						for (int i=0; i<nobsB; i++)
						{
							double vala=A.read(tempgroup, a, i);
							double valb=B.read(tempgroup, i, b);
							values[b]+=vala*valb;
						}
						values[b]=constant*values[b];
					}
					C.write(tempgroup, values);
				}
			}
			return C;
		}
		catch (Exception e)
		{
			C.closeAll();
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw, true);
			e.printStackTrace(pw);
			pw.flush();
			sw.flush();
			iserror=true;
			message="Error in mult\n"+sw.toString();
			return null;
		}
	}
	/**
	* Evaluate the product of a transposed matrix for itself (not transposed) <p>
	* (eventually by multiplying the product for a given constant)
	*/
	public GroupedMatrix2Dfile ATAmult (GroupedMatrix2Dfile A, double constant)
	{
		GroupedMatrix2Dfile C=null;
		try
		{
			int numgroup=vgm.getTotal();
			if (numgroup==0)
				numgroup=1;
			int nvar=A.getColumns();
			C=new GroupedMatrix2Dfile(tempdir, nvar);
			for (int g=0; g<numgroup; g++)
			{
				Vector<String> tempgroup=vgm.getvectormodalities(g);
				int nobsA=A.getRows(tempgroup);
				for (int a=0; a<nvar; a++)
				{
					double[] values=new double[nvar];
					for (int b=0; b<nvar; b++)
					{
						values[b]=0;
						for (int i=0; i<nobsA; i++)
						{
							double vala=A.read(tempgroup, i, a);
							double valb=A.read(tempgroup, i, b);
							values[b]+=vala*valb;
						}
						values[b]=constant*values[b];
					}
					C.write(tempgroup, values);
				}
			}
			return C;
		}
		catch (Exception e)
		{
			C.closeAll();
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw, true);
			e.printStackTrace(pw);
			pw.flush();
			sw.flush();
			iserror=true;
			message="Error in ATAmult\n"+sw.toString();
			return null;
		}
	}
	/**
	* Evaluate the product of a transposed matrix for a vector <p>
	* (eventually elevated to the exponent) <p>
	* and for the original matrix itself (not transposed)
	* (eventually by multiplying the product for a given constant).
	* Obviously the vector is to be considered as a square matrix diagonal matrix
	*/
	public GroupedMatrix2Dfile ATVAmult (GroupedMatrix2Dfile A, GroupedMatrix2Dfile B, double constant, double exponent)
	{
		GroupedMatrix2Dfile C=null;
		try
		{
			int numgroup=vgm.getTotal();
			if (numgroup==0)
				numgroup=1;
			int nvar=A.getColumns();
			int nvarB=B.getColumns();
			if (nvarB!=1)
			{
				iserror=true;
				message="Error in ATVAmult: it is required to use a matrix and a row-vector";
				return null;
			}
			C=new GroupedMatrix2Dfile(tempdir, nvar);
			for (int g=0; g<numgroup; g++)
			{
				Vector<String> tempgroup=vgm.getvectormodalities(g);
				int nobsA=A.getRows(tempgroup);
				int nobsB=B.getRows(tempgroup);
				if (nobsB!=nobsA)
				{
					C.closeAll();
					iserror=true;
					message="Error in ATVAmult: the matrix and the vector does not have the same number of rows";
					return null;
				}
				for (int a=0; a<nvar; a++)
				{
					double[] values=new double[nvar];
					for (int b=0; b<nvar; b++)
					{
						values[b]=0;
						for (int i=0; i<nobsA; i++)
						{
							double vala=A.read(tempgroup, i, a);
							double valb=A.read(tempgroup, i, b);
							double valc=B.read(tempgroup, i, 0);
							values[b]+=vala*valb*Math.pow(valc, exponent);
						}
						values[b]=constant*values[b];
					}
					C.write(tempgroup, values);
				}
			}
			return C;
		}
		catch (Exception e)
		{
			C.closeAll();
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw, true);
			e.printStackTrace(pw);
			pw.flush();
			sw.flush();
			iserror=true;
			message="Error in ATVAmult\n"+sw.toString();
			return null;
		}
	}
	/**
	* Evaluate the product of a transposed matrix for a vector (to be considered as a diagonal matrix)<p>
	* and for the original matrix itself (not transposed)
	* (eventually by multiplying the product for a given constant that depends from the grouping variables),
	* and for another constant<p>
	* cATVAb
	*/
	public GroupedMatrix2Dfile ATVAmult (GroupedMatrix2Dfile A, GroupedMatrix2Dfile B, Hashtable<Vector<String>, Double> constant, double mult)
	{
		GroupedMatrix2Dfile C=null;
		try
		{
			int numgroup=vgm.getTotal();
			if (numgroup==0)
				numgroup=1;
			int nvar=A.getColumns();
			int nvarB=B.getColumns();
			if (nvarB!=1)
			{
				iserror=true;
				message="Error in ATVAmult: it is required to use a matrix and a row-vector";
				return null;
			}
			C=new GroupedMatrix2Dfile(tempdir, nvar);
			for (int g=0; g<numgroup; g++)
			{
				Vector<String> tempgroup=vgm.getvectormodalities(g);
				int nobsA=A.getRows(tempgroup);
				int nobsB=A.getRows(tempgroup);
				if (nobsB!=nobsA)
				{
					C.closeAll();
					iserror=true;
					message="Error in ATVAmult: the matrix and the vector do not have the same number of rows";
					return null;
				}
				if (constant.get(tempgroup)==null)
				{
					C.closeAll();
					iserror=true;
					message="Error in ATVAmult: one value of the grouping variables was not found in the constants to be used";
					return null;
				}
				double constantv=(constant.get(tempgroup)).doubleValue();
				for (int a=0; a<nvar; a++)
				{
					double[] values=new double[nvar];
					for (int b=0; b<nvar; b++)
					{
						values[b]=0;
						for (int i=0; i<nobsA; i++)
						{
							double vala=A.read(tempgroup, i, a);
							double valb=A.read(tempgroup, i, b);
							double valc=B.read(tempgroup, i, 0);
							values[b]+=vala*valb*valc;
						}
						values[b]=mult*constantv*values[b];
					}
					C.write(tempgroup, values);
				}
			}
			return C;
		}
		catch (Exception e)
		{
			C.closeAll();
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw, true);
			e.printStackTrace(pw);
			pw.flush();
			sw.flush();
			iserror=true;
			message="Error in ATVAmult\n"+sw.toString();
			return null;
		}
	}
	/**
	* Evaluate the product of a transposed matrix for the square vector (to be considered as a diagonal matrix)<p>
	* and for the original matrix itself (not transposed)
	* (eventually by multiplying the product for a given constant that depends from the grouping variables), and for another constant<p>
	* cATVVAb (elevated at the exponent)
	*/
	public GroupedMatrix2Dfile ATVVAmult (GroupedMatrix2Dfile A, GroupedMatrix2Dfile B, Hashtable<Vector<String>, Double> constant, double exponent)
	{
		GroupedMatrix2Dfile C=null;
		try
		{
			int numgroup=vgm.getTotal();
			if (numgroup==0)
				numgroup=1;
			int nvar=A.getColumns();
			int nvarB=B.getColumns();
			if (nvarB!=1)
			{
				iserror=true;
				message="Error in ATVVAmult: it is required to use a matrix and a row-vector";
				return null;
			}
			C=new GroupedMatrix2Dfile(tempdir, nvar);
			for (int g=0; g<numgroup; g++)
			{
				Vector<String> tempgroup=vgm.getvectormodalities(g);
				int nobsA=A.getRows(tempgroup);
				if (constant.get(tempgroup)==null)
				{
					C.closeAll();
					iserror=true;
					message="Error in ATVVAmult: one value of the grouping variables was not found in the constants to be used";
					return null;
				}
				double constantv=(constant.get(tempgroup)).doubleValue();
				for (int a=0; a<nvar; a++)
				{
					double[] values=new double[nvar];
					for (int b=0; b<nvar; b++)
					{
						values[b]=0;
						for (int i=0; i<nobsA; i++)
						{
							double vala=A.read(tempgroup, i, a);
							double valb=A.read(tempgroup, i, b);
							double valc=B.read(tempgroup, i, 0);
							values[b]+=vala*valb*Math.pow(valc, 2);
						}
						values[b]=Math.pow(constantv, exponent)*values[b];
					}
					C.write(tempgroup, values);
				}
			}
			return C;
		}
		catch (Exception e)
		{
			C.closeAll();
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw, true);
			e.printStackTrace(pw);
			pw.flush();
			sw.flush();
			iserror=true;
			message="Error in ATVVAmult\n"+sw.toString();
			return null;
		}
	}
	/**
	* Evaluate the product of a transposed matrix for a vector
	*/
	public GroupedMatrix2Dfile ATVmult (GroupedMatrix2Dfile A, GroupedMatrix2Dfile B)
	{
		GroupedMatrix2Dfile C=null;
		try
		{
			int numgroup=vgm.getTotal();
			if (numgroup==0)
				numgroup=1;
			int nvarA=A.getColumns();
			int nvarB=B.getColumns();
			if (nvarB!=1)
			{
				iserror=true;
				message="Error in ATVmult: it is required to use a matrix and a row-vector";
				return null;
			}
			C=new GroupedMatrix2Dfile(tempdir, 1);
			for (int g=0; g<numgroup; g++)
			{
				Vector<String> tempgroup=vgm.getvectormodalities(g);
				int nobsA=A.getRows(tempgroup);
				int nobsB=B.getRows(tempgroup);
				if (nobsA!=nobsB)
				{
					C.closeAll();
					iserror=true;
					message="Error in ATVmult: the number of the columns in the transposed matrix does not correspond to the number of rows of the vector";
					return null;
				}
				for (int a=0; a<nvarA; a++)
				{
					double[] values=new double[1];
					values[0]=0;
					for (int i=0; i<nobsA; i++)
					{
						double vala=A.read(tempgroup, i, a);
						double valb=B.read(tempgroup, i, 0);
						values[0]+=vala*valb;
					}
					C.write(tempgroup, values);
				}
			}
			return C;
		}
		catch (Exception e)
		{
			C.closeAll();
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw, true);
			e.printStackTrace(pw);
			pw.flush();
			sw.flush();
			iserror=true;
			message="Error in ATVmult\n"+sw.toString();
			return null;
		}
	}
	/**
	* Evaluate the inverted of a given square matrix
	*/
	public GroupedMatrix2Dfile inv (GroupedMatrix2Dfile A)
	{
		GroupedMatrix2Dfile invA=null;
		int[] row=new int[0];
		try
		{
			int numgroup=vgm.getTotal();
			if (numgroup==0)
				numgroup=1;
			int nvar=A.getColumns();
			invA=new GroupedMatrix2Dfile(tempdir, nvar);
			for (int g=0; g<numgroup; g++)
			{
				Vector<String> tempgroup=vgm.getvectormodalities(g);
				invA.assignbasefile(tempgroup);
				double determinant=0;
				for(int y = 0; y < A.getRows(tempgroup); y++)
				{
					double[] inv=new double[nvar];
					for(int x = 0; x < nvar; x++)
					{
						Matrix2DFile temp=new Matrix2DFile(tempdir, nvar-1);
						int n=A.getRows(tempgroup)-1;
						row=new int[n];
						for(int i = 0; i < A.getRows(tempgroup); i++)
						{
							double[] t=new double[nvar-1];
							for(int j = 0; j < nvar; j++)
							{
								if (j<y)
									t[j]=A.read(tempgroup, i, j);
								if (j>y)
									t[j-1]=A.read(tempgroup, i, j);
							}
							if (i!=x)
								temp.write(t);
							if (i<(nvar-1))
							{
								row[i]=i;
							}
						}
						double det=1;
						int hold , I_pivot;
						double pivot;
						double abs_pivot;
						for(int k=0; k<n-1; k++)
						{
							pivot = temp.read(row[k], k);
							abs_pivot = Math.abs(pivot);
							I_pivot = k;
							for(int i=k; i<n; i++)
							{
								if( Math.abs(temp.read(row[i], k)) > abs_pivot )
								{
									I_pivot = i;
									pivot = temp.read(row[i], k);
									abs_pivot = Math.abs(pivot);
								}
							}
							if(I_pivot != k)
							{
								hold = row[k];
								row[k]=row[I_pivot];
								row[I_pivot]=hold;
								det = - det;
							}
							if (abs_pivot < 1.0E-10)
							{
								det=0;
								k=n-1;
							}
							else
							{
								det = det * pivot;
								for(int j=k+1; j<n; j++)
								{
									temp.write(temp.read(row[k], j)/temp.read(row[k], k), row[k], j);
								}
								for(int i=0; i<n; i++)
								{
									if(i != k)
									{
										for(int j=k+1; j<n; j++)
										{
											double t=temp.read(row[i], j)- temp.read(row[i], k)*temp.read(row[k], j);
											temp.write(t, row[i], j);
										}
									}
								}
							}
						}
						det=det * temp.read(row[n-1], n-1);
						if (y==0)
							determinant+=A.read(tempgroup, x, 0)*Math.pow(-1, x)*det;
						inv[x]=Math.pow(-1, y+x)*det;
						temp.close();
					}
					for(int x = 0; x < nvar; x++)
					{
						inv[x]=inv[x] / determinant;
					}
					invA.write(tempgroup, inv);
				}
				invA.deassignbasefile();
			}
			return invA;
		}
		catch (Exception e)
		{
			invA.closeAll();
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw, true);
			e.printStackTrace(pw);
			pw.flush();
			sw.flush();
			iserror=true;
			message="Error in inv\n"+sw.toString();
			return null;
		}
	}
	/**
	* Evaluate the square of one matrix
	*/
	public GroupedMatrix2Dfile square (GroupedMatrix2Dfile A)
	{
		GroupedMatrix2Dfile C=null;
		try
		{
			int numgroup=vgm.getTotal();
			if (numgroup==0)
				numgroup=1;
			int nvar=A.getColumns();
			C=new GroupedMatrix2Dfile(tempdir, nvar);
			for (int g=0; g<numgroup; g++)
			{
				Vector<String> tempgroup=vgm.getvectormodalities(g);
				int nobsA=A.getRows(tempgroup);
				for (int a=0; a<nobsA; a++)
				{
					double[] values=new double[nvar];
					for (int b=0; b<nvar; b++)
					{
						values[b]=0;
						for (int i=0; i<nobsA; i++)
						{
							double vala=A.read(tempgroup, a, b);
							double valb=C.read(tempgroup, i, b);
							values[b]+=vala*valb;
						}
						values[b]=values[b];
					}
					C.write(tempgroup, values);
				}
			}
			return C;
		}
		catch (Exception e)
		{
			C.closeAll();
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw, true);
			e.printStackTrace(pw);
			pw.flush();
			sw.flush();
			iserror=true;
			message="Error in square\n"+sw.toString();
			return null;
		}
	}
	/**
	* Evaluate the determinant of one matrix
	*/
	public GroupedMatrix2Dfile det (GroupedMatrix2Dfile A)
	{
		GroupedMatrix2Dfile C=null;
		try
		{
			int numgroup=vgm.getTotal();
			if (numgroup==0)
				numgroup=1;
			int m=A.getColumns();
			C=new GroupedMatrix2Dfile(tempdir, 1);
			for (int g=0; g<numgroup; g++)
			{
				Vector<String> tempgroup=vgm.getvectormodalities(g);
				int n=A.getRows(tempgroup);
				int [] piv = new int[m];
				int pivsign=0;
				for (int i = 0; i < m; i++)
				{
					piv[i] = i;
				}
				Matrix2DFile LU=new Matrix2DFile(tempdir, m);
				double[] LUcolj = new double[m];
				for (int i=0; i<n; i++)
				{
					for (int j=0; j<m; j++)
					{
						LUcolj[j]=A.read(tempgroup, i, j);
					}
					LU.write(LUcolj);
				}
				double[] LUrowi=new double[n];
				double[] d=new double[1];
				for (int j = 0; j < n; j++)
				{
					for (int i = 0; i < m; i++)
					{
						LUcolj[i] = LU.read(i, j);
					}
					for (int i = 0; i < m; i++)
					{
						for (int h=0; h<n; h++)
						{
							LUrowi[h] = LU.read(h, i);
						}
						int kmax = Math.min(i,j);
						double s = 0.0;
						for (int k = 0; k < kmax; k++)
						{
							s += LUrowi[k]*LUcolj[k];
						}
						LUrowi[j] = LUcolj[i] -= s;
					}
					int p = j;
					for (int i = j+1; i < m; i++)
					{
						if (Math.abs(LUcolj[i]) > Math.abs(LUcolj[p]))
						{
							p = i;
						}
					}
					if (p != j)
					{
						for (int k = 0; k < n; k++)
						{
							double t = LU.read(p, k);
							LU.write(LU.read(j, k), p, k);
							LU.write(t, j, k);
						}
						int k = piv[p];
						piv[p] = piv[j];
						piv[j] = k;
						pivsign = -pivsign;
					}
					if (j < m & LU.read(j,j) != 0.0)
					{
						for (int i = j+1; i < m; i++)
						{
							double t=LU.read(i, j);
							LU.write(t/LU.read(j,j), i, j);
						}
					}
				}
				d[0]=(double)pivsign;
				for (int j = 0; j < n; j++)
				{
					d[0] *= LU.read(j,j);
				}
				C.write(tempgroup, d);
				LU.close();
			}
			return C;
		}
		catch (Exception e)
		{
			C.closeAll();
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw, true);
			e.printStackTrace(pw);
			pw.flush();
			sw.flush();
			iserror=true;
			message="Error in det\n"+sw.toString();
			return null;
		}
	}
	/**
	* Evaluate the product for two row-vectors with equal number of rows <p>
	* (eventually by multiplying the product for a given constant) <p>
	* Obviously the first vector is to be considered as a diagonal matrix
	*/
	public GroupedMatrix2Dfile multtv (GroupedMatrix2Dfile A, GroupedMatrix2Dfile B, double constant)
	{
		GroupedMatrix2Dfile C=null;
		try
		{
			int numgroup=vgm.getTotal();
			if (numgroup==0)
				numgroup=1;
			C=new GroupedMatrix2Dfile(tempdir, 1);
			for (int g=0; g<numgroup; g++)
			{
				Vector<String> tempgroup=vgm.getvectormodalities(g);
				int nobsA=A.getRows(tempgroup);
				int nobsB=B.getRows(tempgroup);
				if (nobsB!=nobsA)
				{
					C.closeAll();
					iserror=true;
					message="Error in multtv: the vectors do not have the same number of rows";
					return null;
				}
				for (int a=0; a<nobsA; a++)
				{
					double[] values=new double[1];
					double vala=A.read(tempgroup, a, 0);
					double valb=B.read(tempgroup, a, 0);
					values[0]=constant*vala*valb;
					C.write(tempgroup, values);
				}
			}
			return C;
		}
		catch (Exception e)
		{
			C.closeAll();
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw, true);
			e.printStackTrace(pw);
			pw.flush();
			sw.flush();
			iserror=true;
			message="Error in multtv\n"+sw.toString();
			return null;
		}
	}
	/**
	*Evaluate the product for two row-vectors with equal dimensionality (the first one is transposed)
	*/
	public GroupedMatrix2Dfile multvtv (GroupedMatrix2Dfile A, GroupedMatrix2Dfile B)
	{
		GroupedMatrix2Dfile C=null;
		try
		{
			int numgroup=vgm.getTotal();
			if (numgroup==0)
				numgroup=1;
			C=new GroupedMatrix2Dfile(tempdir, 1);
			for (int g=0; g<numgroup; g++)
			{
				Vector<String> tempgroup=vgm.getvectormodalities(g);
				int nobsA=A.getRows(tempgroup);
				int nobsB=B.getRows(tempgroup);
				if (nobsB!=nobsA)
				{
					C.closeAll();
					iserror=true;
					message="Error in multvtv: the vectors are not compatibles";
					return null;
				}
				double[] values=new double[1];
				values[0]=0;
				for (int a=0; a<nobsA; a++)
				{
					double vala=A.read(tempgroup, a, 0);
					double valb=B.read(tempgroup, a, 0);
					values[0]+=vala*valb;
				}
				C.write(tempgroup, values);
			}
			return C;
		}
		catch (Exception e)
		{
			C.closeAll();
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw, true);
			e.printStackTrace(pw);
			pw.flush();
			sw.flush();
			iserror=true;
			message="Error in multvtv\n"+sw.toString();
			return null;
		}
	}
	/**
	* Evaluate the product for two row-vectors with equal dimensionality
	* (eventually by powering the first one to a given exponent)<p>
	* Obviously the first vector is to be considered as a diagonal matrix
	*/
	public GroupedMatrix2Dfile multtvpow (GroupedMatrix2Dfile A, GroupedMatrix2Dfile B, double exponent)
	{
		GroupedMatrix2Dfile C=null;
		try
		{
			int numgroup=vgm.getTotal();
			if (numgroup==0)
				numgroup=1;
			C=new GroupedMatrix2Dfile(tempdir, 1);
			for (int g=0; g<numgroup; g++)
			{
				Vector<String> tempgroup=vgm.getvectormodalities(g);
				int nobsA=A.getRows(tempgroup);
				int nobsB=B.getRows(tempgroup);
				if (nobsB!=nobsA)
				{
					C.closeAll();
					iserror=true;
					message="Error in multtvpow: the vectors do not have the same number of rows";
					return null;
				}
				for (int a=0; a<nobsA; a++)
				{
					double[] values=new double[1];
					double vala=A.read(tempgroup, a, 0);
					double valb=B.read(tempgroup, a, 0);
					values[0]=Math.pow(vala, exponent)*valb;
					C.write(tempgroup, values);
				}
			}
			return C;
		}
		catch (Exception e)
		{
			C.closeAll();
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw, true);
			e.printStackTrace(pw);
			pw.flush();
			sw.flush();
			iserror=true;
			message="Error in multtvpow\n"+sw.toString();
			return null;
		}
	}
	/**
	* Evaluate the product for two matrices <p>
	* (eventually by multiplying the product for a given constant and by adding to the elements a value)
	*/
	public GroupedMatrix2Dfile multadd (GroupedMatrix2Dfile A, GroupedMatrix2Dfile B, double constant, double valtoadd)
	{
		GroupedMatrix2Dfile C=null;
		try
		{
			int numgroup=vgm.getTotal();
			if (numgroup==0)
				numgroup=1;
			int nvarA=A.getColumns();
			int nvarB=B.getColumns();
			C=new GroupedMatrix2Dfile(tempdir, nvarB);
			for (int g=0; g<numgroup; g++)
			{
				Vector<String> tempgroup=vgm.getvectormodalities(g);
				int nobsA=A.getRows(tempgroup);
				int nobsB=B.getRows(tempgroup);
				if (nobsB!=nvarA)
				{
					C.closeAll();
					iserror=true;
					message="Error in multadd: the number of rows in the second matrix are different from the number of columns in the first matrix";
					return null;
				}
				for (int a=0; a<nobsA; a++)
				{
					double[] values=new double[nvarB];
					for (int b=0; b<nvarB; b++)
					{
						values[b]=0;
						for (int i=0; i<nobsB; i++)
						{
							double vala=A.read(tempgroup, a, i);
							double valb=B.read(tempgroup, i, b);
							values[b]+=vala*valb;
						}
						values[b]=constant*values[b]+valtoadd;
					}
					C.write(tempgroup, values);
				}
			}
			return C;
		}
		catch (Exception e)
		{
			C.closeAll();
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw, true);
			e.printStackTrace(pw);
			pw.flush();
			sw.flush();
			iserror=true;
			message="Error in multadd\n"+sw.toString();
			return null;
		}
	}
	/**
	* Evaluate the product for two matrices <p>
	* (eventually by multiplying the product for a given constant, that depends from the grouping variables,
	* and by adding to the elements a value, that depends from the grouping variables)
	*/
	public GroupedMatrix2Dfile multadd (GroupedMatrix2Dfile A, GroupedMatrix2Dfile B, Hashtable<Vector<String>, Double> constant, Hashtable<Vector<String>, Double> valtoadd)
	{
		GroupedMatrix2Dfile C=null;
		try
		{
			int numgroup=vgm.getTotal();
			if (numgroup==0)
				numgroup=1;
			int nvarA=A.getColumns();
			int nvarB=B.getColumns();
			C=new GroupedMatrix2Dfile(tempdir, nvarB);
			for (int g=0; g<numgroup; g++)
			{
				Vector<String> tempgroup=vgm.getvectormodalities(g);
				int nobsA=A.getRows(tempgroup);
				int nobsB=B.getRows(tempgroup);
				if (nobsB!=nvarA)
				{
					C.closeAll();
					iserror=true;
					message="Error in multadd: the number of rows in the second matrix are different from the number of columns in the first matrix";
					return null;
				}
				if (constant.get(tempgroup)==null)
				{
					C.closeAll();
					iserror=true;
					message="Error in multadd: one value of the grouping variables was not found in the constants to be used";
					return null;
				}
				if (valtoadd.get(tempgroup)==null)
				{
					C.closeAll();
					iserror=true;
					message="Error in multadd: one value of the grouping variables was not found in the constants to be used";
					return null;
				}
				double constantv=(constant.get(tempgroup)).doubleValue();
				double valtoaddv=(valtoadd.get(tempgroup)).doubleValue();
				for (int a=0; a<nobsA; a++)
				{
					double[] values=new double[nvarB];
					for (int b=0; b<nvarB; b++)
					{
						values[b]=0;
						for (int i=0; i<nobsB; i++)
						{
							double vala=A.read(tempgroup, a, i);
							double valb=B.read(tempgroup, i, b);
							values[b]+=vala*valb;
						}
						values[b]=constantv*values[b]+valtoaddv;
					}
					C.write(tempgroup, values);
				}
			}
			return C;
		}
		catch (Exception e)
		{
			C.closeAll();
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw, true);
			e.printStackTrace(pw);
			pw.flush();
			sw.flush();
			iserror=true;
			message="Error in multadd\n"+sw.toString();
			return null;
		}
	}
	/**
	*Prints a given matrix on the standard output
	*/
	public void outprint (GroupedMatrix2Dfile A, String name)
	{
		int numgroup=vgm.getTotal();
		if (numgroup==0)
			numgroup=1;
		int nvar=A.getColumns();
		for (int g=0; g<numgroup; g++)
		{
			Vector<String> tempgroup=vgm.getvectormodalities(g);
			int nobsA=A.getRows(tempgroup);
			String vgmname="";
			for (int a=0; a<tempgroup.size(); a++)
			{
				vgmname=vgmname+tempgroup.get(a);
				if (a<(tempgroup.size()-1))
					vgmname=vgmname+"; ";
			}
			System.out.println("Matrix name: "+name+ "; Group: "+vgmname);
			System.out.println("Dimension: "+nobsA+" x "+nvar);
			for (int a=0; a<nobsA; a++)
			{
				String valtoprint="";
				for (int b=0; b<nvar; b++)
				{
					double vala=A.read(tempgroup, a, b);
					valtoprint=valtoprint+String.valueOf(vala)+" ";
				}
				System.out.println(valtoprint);
			}
		}
		return;
	}
	/**
	*Prints a given matrix on a given file (requires the full path of the file)
	*/
	public void outprintfile (GroupedMatrix2Dfile A, String name, String filepath)
	{
		try
		{
			BufferedWriter out = new BufferedWriter(new FileWriter(filepath));
			int numgroup=vgm.getTotal();
			if (numgroup==0)
				numgroup=1;
			int nvar=A.getColumns();
			for (int g=0; g<numgroup; g++)
			{
				Vector<String> tempgroup=vgm.getvectormodalities(g);
				int nobsA=A.getRows(tempgroup);
				String vgmname="";
				for (int a=0; a<tempgroup.size(); a++)
				{
					vgmname=vgmname+tempgroup.get(a);
					if (a<(tempgroup.size()-1))
						vgmname=vgmname+"; ";
				}
				out.write("Matrix name: "+name+ "; Group: "+vgmname+"\n");
				out.write("Dimension: "+nobsA+" x "+nvar+"\n");
				for (int a=0; a<nobsA; a++)
				{
					String valtoprint="";
					for (int b=0; b<nvar; b++)
					{
						double vala=A.read(tempgroup, a, b);
						valtoprint=valtoprint+String.valueOf(vala)+"\t";
					}
					out.write(valtoprint+"\n");
				}
			}
			out.close();
		}
		catch (Exception e) {}
		return;
	}
}