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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Vector;

import ADaMSoft.algorithms.VarGroupModalities;
import ADaMSoft.dataaccess.GroupedMatrix2Dfile;

/**
* Used to evaluate the eigenvalue decomposition of a 2D matrix files (it must be a symmetric file)
* @author marco.scarno@gmail.com
* @date 13/02/2017
*/
public class EigenvalueDecomposition
{
	String tempdir;
	VarGroupModalities vgm;
	String message;
	boolean iserror;
	GroupedMatrix2Dfile V=null;
	GroupedMatrix2Dfile d=null;
	GroupedMatrix2Dfile e=null;
	/**
	* Eigenvalue decomposition 2D Matrix files: receives the path of the temporary directory and the var group modalities
	*/
	public EigenvalueDecomposition(String tempdir, VarGroupModalities vgm)
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
	* Execute the decomposition
	*/
	public void evaluate(GroupedMatrix2Dfile A)
	{
		try
		{
			int numgroup=vgm.getTotal();
			if (numgroup==0)
				numgroup=1;
			int nvar=A.getColumns();
			V=new GroupedMatrix2Dfile(tempdir, nvar);
			d=new GroupedMatrix2Dfile(tempdir, 1);
			e=new GroupedMatrix2Dfile(tempdir, 1);
			for (int gr=0; gr<numgroup; gr++)
			{
				Vector<String> tempgroup=vgm.getvectormodalities(gr);
				int n=A.getRows(tempgroup);
				if (n!=nvar)
				{
					if (V!=null)
						V.closeAll();
					if (d!=null)
						d.closeAll();
					if (e!=null)
						e.closeAll();
					iserror=true;
					message="Error in EigenValue Decomposition: the matrix is not square\n";
					return;
				}
				boolean issymmetric = true;
				for (int a=0; (a<n) & issymmetric; a++)
				{
					double[] values=new double[nvar];
					for (int b=0; (b < n) & issymmetric; b++)
					{
						values[b]=A.read(tempgroup, a, b);
						issymmetric = (A.read(tempgroup, a, b) == A.read(tempgroup, b, a));
					}
					V.write(tempgroup, values);
				}
				if (!issymmetric)
				{
					if (V!=null)
						V.closeAll();
					if (d!=null)
						d.closeAll();
					if (e!=null)
						e.closeAll();
					iserror=true;
					message="Error in EigenValue Decomposition: the matrix is not symmetric\n";
					return;
				}
				for (int j = 0; j < n; j++)
				{
					double[] dt=new double[1];
					dt[0] = V.read(tempgroup, n-1, j);
					d.write(tempgroup, dt);
					e.write(tempgroup, dt);
				}
				for (int i = n-1; i > 0; i--)
				{
					double scale = 0.0;
					double h = 0.0;
					for (int k = 0; k < i; k++)
					{
						scale = scale + Math.abs(d.read(tempgroup, k, 0));
					}
					if (scale == 0.0)
					{
						e.write(tempgroup, d.read(tempgroup, i-1, 0), i, 0);
						for (int j = 0; j < i; j++)
						{
							d.write(tempgroup, V.read(tempgroup, i-1, j), j, 0);
							V.write(tempgroup, 0, i, j);
							V.write(tempgroup, 0, j, i);
						}
					}
					else
					{
						for (int k = 0; k < i; k++)
						{
							d.write(tempgroup, d.read(tempgroup, k, 0)/scale, k, 0);
							h += Math.pow(d.read(tempgroup, k, 0),2);
						}
						double f = d.read(tempgroup, i-1, 0);
						double g = Math.sqrt(h);
						if (f > 0)
						{
							g = -g;
						}
						e.write(tempgroup, scale*g, i, 0);
						h = h - f * g;
						d.write(tempgroup, f-g, i-1, 0);
						for (int j = 0; j < i; j++)
						{
							e.write(tempgroup, 0, j, 0);
						}
						for (int j = 0; j < i; j++)
						{
							f = d.read(tempgroup, j, 0);
							V.write(tempgroup, f, j, i);
							g = e.read(tempgroup, j, 0) + V.read(tempgroup, j, j) * f;
							for (int k = j+1; k <= i-1; k++)
							{
								g += V.read(tempgroup, k, j) * d.read(tempgroup, k, 0);
								e.write(tempgroup, e.read(tempgroup, k, 0)+V.read(tempgroup, k, j)*f, k, 0);
							}
							e.write(tempgroup, g, j, 0);
						}
						f = 0.0;
						for (int j = 0; j < i; j++)
						{
							e.write(tempgroup, e.read(tempgroup, j, 0)/h, j, 0);
							f += e.read(tempgroup, j, 0) * d.read(tempgroup, j, 0);
						}
						double hh = f / (h + h);
						for (int j = 0; j < i; j++)
						{
							e.write(tempgroup, e.read(tempgroup, j, 0) - hh * d.read(tempgroup, j, 0), j, 0);
						}
						for (int j = 0; j < i; j++)
						{
							f = d.read(tempgroup, j, 0);
							g = e.read(tempgroup, j, 0);
							for (int k = j; k <= i-1; k++)
							{
								V.write(tempgroup, V.read(tempgroup, k, j)-(f * e.read(tempgroup, k, 0) + g * d.read(tempgroup, k, 0)), k, j);
							}
							d.write(tempgroup, V.read(tempgroup, i-1, j), j, 0);
							V.write(tempgroup, 0, i, j);
						}
					}
					d.write(tempgroup, h, i, 0);
				}
				for (int i = 0; i < n-1; i++)
				{
					V.write(tempgroup, V.read(tempgroup, i, i), n-1, i);
					V.write(tempgroup, 1, i, i);
					double h = d.read(tempgroup, i+1, 0);
					if (h != 0.0)
					{
						for (int k = 0; k <= i; k++)
						{
							d.write(tempgroup, V.read(tempgroup, k, i+1)/h, k, 0);
						}
						for (int j = 0; j <= i; j++)
						{
							double g = 0.0;
							for (int k = 0; k <= i; k++)
							{
								g += V.read(tempgroup, k, i+1)*V.read(tempgroup, k, j);
							}
							for (int k = 0; k <= i; k++)
							{
								V.write(tempgroup, V.read(tempgroup, k, j)-g*d.read(tempgroup, k, 0), k, j);
							}
						}
					}
					for (int k = 0; k <= i; k++)
					{
						V.write(tempgroup, 0, k, i+1);
					}
				}
				for (int j = 0; j < n; j++)
				{
					d.write(tempgroup, V.read(tempgroup, n-1, j), j, 0);
					V.write(tempgroup, 0, n-1, j);
				}
				V.write(tempgroup, 1, n-1, n-1);
				e.write(tempgroup, 0, 0, 0);

				for (int i = 1; i < n; i++)
				{
					e.write(tempgroup, e.read(tempgroup, i, 0), i-1, 0);
				}
				e.write(tempgroup, 0, n-1, 0);

				double f = 0.0;
				double tst1 = 0.0;
				double eps = Math.pow(2.0,-52.0);
				for (int l = 0; l < n; l++)
				{
					tst1 = Math.max(tst1,Math.abs(d.read(tempgroup, l, 0)) + Math.abs(e.read(tempgroup, l, 0)));
					int m = l;
					while (m < n)
					{
						if (Math.abs(e.read(tempgroup, m, 0)) <= eps*tst1)
						{
            			   break;
						}
						m++;
					}
					if (m > l)
					{
						int iter = 0;
						do
						{
							iter = iter + 1;
							double g = d.read(tempgroup, l, 0);
							double p = (d.read(tempgroup, l+1, 0) - g) / (2.0 * e.read(tempgroup, l, 0));
							double r = hypot(p,1.0);
							if (p < 0)
							{
								r = -r;
							}
							d.write(tempgroup, e.read(tempgroup, l, 0)/(p+r), l, 0);
							d.write(tempgroup, e.read(tempgroup, l, 0)*(p+r), l+1, 0);
							double dl1 = d.read(tempgroup, l+1, 0);
							double h = g - d.read(tempgroup, l, 0);
							for (int i = l+2; i < n; i++)
							{
								d.write(tempgroup, d.read(tempgroup, i, 0)-h, i, 0);
							}
							f = f + h;
							p = d.read(tempgroup, m, 0);
							double c = 1.0;
							double c2 = c;
							double c3 = c;
							double el1 = e.read(tempgroup, l+1, 0);
							double s = 0.0;
							double s2 = 0.0;
							for (int i = m-1; i >= l; i--)
							{
								c3 = c2;
								c2 = c;
								s2 = s;
								g = c * e.read(tempgroup, i, 0);
								h = c * p;
								r = hypot(p,e.read(tempgroup, i, 0));
								e.write(tempgroup, s*r, i+1, 0);
								s = e.read(tempgroup, i, 0)/r;
								c = p / r;
								p = c * d.read(tempgroup, i, 0) - s * g;
								d.write(tempgroup, h + s * (c * g + s * d.read(tempgroup, i, 0)), i+1, 0);
								for (int k = 0; k < n; k++)
								{
									h = V.read(tempgroup, k, i+1);
									V.write(tempgroup, s * V.read(tempgroup, k, i) + c * h, k, i+1);
									V.write(tempgroup, c * V.read(tempgroup, k, i) - s * h, k, i);
								}
							}
							p = -s * s2 * c3 * el1 * e.read(tempgroup, l, 0) / dl1;
							e.write(tempgroup, s*p, l, 0);
							d.write(tempgroup, c*p, l, 0);
						} while (Math.abs(e.read(tempgroup, l, 0)) > eps*tst1);
					}
					d.write(tempgroup, d.read(tempgroup, l, 0)+f, l, 0);
					e.write(tempgroup, 0, l, 0);
				}
				for (int i = 0; i < n-1; i++)
				{
					int k = i;
					double p = d.read(tempgroup, i, 0);
					for (int j = i+1; j < n; j++)
					{
						if (d.read(tempgroup, j, 0) < p)
						{
							k = j;
							p = d.read(tempgroup, j, 0);
						}
					}
					if (k != i)
					{
						d.write(tempgroup, d.read(tempgroup, i, 0), k, 0);
						d.write(tempgroup, p, i, 0);
						for (int j = 0; j < n; j++)
						{
							p = V.read(tempgroup, j, i);
							V.write(tempgroup, V.read(tempgroup, j, k), j, i);
							V.write(tempgroup, p, j, k);
						}
					}
				}
			}
		}
		catch (Exception eed)
		{
			if (V!=null)
				V.closeAll();
			if (d!=null)
				d.closeAll();
			if (e!=null)
				e.closeAll();
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw, true);
			eed.printStackTrace(pw);
			pw.flush();
			sw.flush();
			iserror=true;
			message="Error in EigenValue Decomposition:\n"+sw.toString();
		}
	}
	/**
	*Return the eigenvectors
	*/
	public GroupedMatrix2Dfile getV()
	{
		return V;
	}
	/**
	*Return the eigenvalues
	*/
	public GroupedMatrix2Dfile getRealEigenvalues()
	{
		return d;
	}
	/**
	*Return the imaginary eigenvalues
	*/
	public GroupedMatrix2Dfile getImagEigenvalues ()
	{
		return e;
	}
	/**
	*Internal routine
	*/
	private double hypot(double a, double b)
	{
		double r;
		if (Math.abs(a) > Math.abs(b))
		{
			r = b/a;
			r = Math.abs(a)*Math.sqrt(1+r*r);
		}
		else if (b != 0)
		{
			r = a/b;
			r = Math.abs(b)*Math.sqrt(1+r*r);
		}
		else
		{
			r = 0.0;
		}
		return r;
	}
	public void closeAll()
	{
		if (V!=null)
			V.closeAll();
		if (d!=null)
			d.closeAll();
		if (e!=null)
			e.closeAll();
	}
}
