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

import java.util.Vector;
import java.util.Hashtable;

/**
* Used to evaluate the singular value decomposition of a given 2D matrix file
* @author marco.scarno@gmail.com
* @date 13/02/2017
*/
public class SingularValueDecomposition
{
	String tempdir;
	VarGroupModalities vgm;
	String message;
	boolean iserror;
	GroupedMatrix2Dfile V=null;
	GroupedMatrix2Dfile U=null;
	GroupedMatrix2Dfile S=null;
	Matrix2DFile A=null;
	Matrix2DFile v=null;
	Matrix2DFile u=null;
	Hashtable<Vector<String>, Integer> maxvar;
	/**
	* Singular value decomposition of 2D Matrix files: receives the path of the temporary directory and the var group modalities
	*/
	public SingularValueDecomposition(String tempdir, VarGroupModalities vgm)
	{
		this.tempdir=tempdir;
		this.vgm=vgm;
		message="";
		iserror=false;
		maxvar=new Hashtable<Vector<String>, Integer>();
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
	public void evaluate(GroupedMatrix2Dfile M)
	{
		try
		{
			double[] e=new double[0];
			double[] work=new double[0];
			int numgroup=vgm.getTotal();
			if (numgroup==0)
				numgroup=1;
			int n=M.getColumns();
			S=new GroupedMatrix2Dfile(tempdir, 1);
			/*In mt there is the maximum number of rows*/
			int mt=0;
			for (int gr=0; gr<numgroup; gr++)
			{
				Vector<String> tempgroup=vgm.getvectormodalities(gr);
				int m=M.getRows(tempgroup);
				maxvar.put(tempgroup, new Integer(m));
				if (m>mt)
					mt=m;
			}
			int nu = Math.min(mt, n);
			V=new GroupedMatrix2Dfile(tempdir, n);
			U=new GroupedMatrix2Dfile(tempdir, nu);
			int dims=Math.min(mt+1, n);
			for (int gr=0; gr<numgroup; gr++)
			{
				Vector<String> tempgroup=vgm.getvectormodalities(gr);
				int m=M.getRows(tempgroup);
				M.assignbasefile(tempgroup);
				V.assignbasefile(tempgroup);
				U.assignbasefile(tempgroup);
				S.assignbasefile(tempgroup);

				A=new Matrix2DFile(tempdir, n);
				v=new Matrix2DFile(tempdir, n);
				u=new Matrix2DFile(tempdir, nu);

				int currentnu=Math.min(m, n);
				double[] ttt=new double[nu];
				for (int j=0; j<nu; j++)
				{
					ttt[j]=0;
				}
				for (int a=0; a<m; a++)
				{
					A.write(M.readRow(tempgroup, a));
					u.write(ttt);
				}
				e=new double[n];
				work=new double[m];
				ttt=new double[n];
				for (int j=0; j<n; j++)
				{
					ttt[j]=0;
				}
				for (int i=0; i<n; i++)
				{
					e[i]=0;
					v.write(ttt);
				}
				for (int i=0; i<m; i++)
				{
					work[i]=0;
				}
				double[] s=new double[dims];
				for (int i=0; i<dims; i++)
				{
					s[i]=0;
				}
				int nct = Math.min(m-1,n);
				int nrt = Math.max(0,Math.min(n-2,m));
				double tempdp=0;
				for (int k = 0; k < Math.max(nct,nrt); k++)
				{
					if (k < nct)
					{
						s[k]=0;
						for (int i = k; i < m; i++)
						{
							s[k]=hypot(s[k], A.read(i, k));
						}
						if (s[k] != 0.0)
						{
							if (A.read(k, k) < 0.0)
							{
								s[k]=-s[k];
							}
							for (int i = k; i < m; i++)
							{
								A.write(A.read(i, k)/s[k], i, k);
							}
							A.write(A.read(k, k)+1, k, k);
						}
						s[k]=-s[k];
					}
					for (int j = k+1; j < n; j++)
					{
						if ((k < nct) & (s[k] != 0.0))
						{
							double t = 0;
							for (int i = k; i < m; i++)
							{
								t += A.read(i, k)*A.read(i, j);
							}
							t = -t/A.read(k, k);
							for (int i = k; i < m; i++)
							{
								tempdp=A.read(i, j);
								A.write(tempdp + A.read(i, k)*t, i, j);
							}
						}
						e[j]=A.read(k, j);
					}
					if (k < nct)
					{
						for (int i = k; i < m; i++)
						{
							u.write(A.read(i, k), i, k);
						}
					}
					if (k < nrt)
					{
						e[k]=0;
						for (int i = k+1; i < n; i++)
						{
							e[k]=hypot(e[k], e[i]);
						}
						if (e[k] != 0.0)
						{
							if (e[k+1] < 0.0)
							{
								e[k]=-e[k];
							}
							for (int i = k+1; i < n; i++)
							{
								e[i]/=e[k];
							}
							e[k+1]+=1;
						}
						e[k]=-e[k];
						if ((k+1 < m) & (e[k] != 0.0))
						{
							for (int i = k+1; i < m; i++)
							{
								work[i]=0;
							}
							for (int j = k+1; j < n; j++)
							{
								for (int i = k+1; i < m; i++)
								{
									work[i]+=e[j]*A.read(i, j);
								}
							}
							for (int j = k+1; j < n; j++)
							{
								double t = -e[j]/e[k+1];
								for (int i = k+1; i < m; i++)
								{
									A.write(A.read(i, j)+t*work[i], i, j);
								}
							}
						}
						for (int i = k+1; i < n; i++)
						{
							v.write(e[i], i, k);
						}
					}
				}
				int p = Math.min(n,m+1);
				if (nct < n)
				{
					s[nct]=A.read(nct, nct);
				}
				if (m < p)
				{
					s[p-1]=0;
				}
				if (nrt+1 < p)
				{
					e[nrt]=A.read(nrt, p-1);
				}
				e[p-1]=0;
				for (int j = nct; j < currentnu; j++)
				{
					for (int i = 0; i < m; i++)
					{
						u.write(0, i, j);
					}
					u.write(1, j, j);
				}
				for (int k = nct-1; k >= 0; k--)
				{
					if (s[k] != 0.0)
					{
						for (int j = k+1; j < currentnu; j++)
						{
							double t = 0;
							for (int i = k; i < m; i++)
							{
								t += u.read(i, k)*u.read(i, j);
							}
							t = -t/u.read(k, k);
							for (int i = k; i < m; i++)
							{
								u.write(u.read(i, j)+t*u.read(i, k), i, j);
							}
						}
						for (int i = k; i < m; i++ )
						{
							u.write(-1*u.read(i, k), i, k);
						}
						u.write(1+u.read(k, k), k, k);
						for (int i = 0; i < k-1; i++)
						{
							u.write(0, i, k);
						}
					}
					else
					{
						for (int i = 0; i < m; i++)
						{
							u.write(0, i, k);
						}
						u.write(1, k, k);
					}
				}
				for (int k = n-1; k >= 0; k--)
				{
					if ((k < nrt) & (e[k] != 0.0))
					{
						for (int j = k+1; j < currentnu; j++)
						{
							double t = 0;
							for (int i = k+1; i < n; i++)
							{
								t += v.read(i, k)*v.read(i, j);
							}
							t = -t/v.read(k+1, k);
							for (int i = k+1; i < n; i++)
							{
								v.write(v.read(i, j)+t*v.read(i, k), i, j);
							}
						}
					}
					for (int i = 0; i < n; i++)
					{
						v.write(0, i, k);
					}
					v.write(1, k, k);
				}
				int pp = p-1;
				int iter = 0;
				double eps = Math.pow(2.0,-52.0);
				double tiny = Math.pow(2.0,-966.0);
				while (p > 0)
				{
					int k,kase;
					for (k = p-2; k >= -1; k--)
					{
						if (k == -1)
						{
							break;
						}
						if (Math.abs(e[k]) <= tiny + eps*(Math.abs(s[k]) + Math.abs(s[k+1])))
						{
							e[k]=0;
							break;
						}
					}
					if (k == p-2)
					{
						kase = 4;
					}
					else
					{
						int ks;
						for (ks = p-1; ks >= k; ks--)
						{
							if (ks == k)
							{
								break;
							}
							double t = (ks != p ? Math.abs(e[ks]) : 0.) + (ks != k+1 ? Math.abs(e[ks-1]) : 0.);
							if (Math.abs(s[ks]) <= tiny + eps*t)
							{
								s[ks]=0;
								break;
							}
						}
						if (ks == k)
						{
							kase = 3;
						}
						else if (ks == p-1)
						{
							kase = 1;
						}
						else
						{
							kase = 2;
							k = ks;
						}
					}
					k++;
					switch (kase)
					{
						case 1:
						{
							double f = e[p-2];
							e[p-2]=0;
							for (int j = p-2; j >= k; j--)
							{
								double t = hypot(s[j],f);
								double cs = s[j]/t;
								double sn = f/t;
								s[j]=t;
								if (j != k)
								{
									f = -sn*e[j-1];
									e[j-1]=cs*e[j-1];
								}
								for (int i = 0; i < n; i++)
								{
									t = cs*v.read(i, j) + sn*v.read(i, p-1);
									v.write(-sn*v.read(i, j) + cs*v.read(i, p-1), i, p-1);
									v.write(t, i, j);
								}
							}
						}
						break;
						case 2:
						{
							double f = e[k-1];
							e[k-1]=0;
							for (int j = k; j < p; j++)
							{
								double t = hypot(s[j],f);
								double cs = s[j]/t;
								double sn = f/t;
								s[j]=t;
								f = -sn*e[j];
								e[j]=cs*e[j];
								for (int i = 0; i < m; i++)
								{
									t = cs*u.read(i, j) + sn*u.read(i, k-1);
									u.write(-1*sn*u.read(i, j) + cs*u.read(i, k-1), i, k-1);
									u.write(t, i, j);
								}
							}
						}
						break;
						case 3:
						{
							double scale = Math.max(Math.max(Math.max(Math.max(
		                       Math.abs(s[p-1]),Math.abs(s[p-2])),Math.abs(e[p-2])),
		                       Math.abs(s[k])),Math.abs(e[k]));
							double sp = s[p-1]/scale;
							double spm1 = s[p-2]/scale;
							double epm1 = e[p-2]/scale;
							double sk = s[k]/scale;
							double ek = e[k]/scale;
							double b = ((spm1 + sp)*(spm1 - sp) + epm1*epm1)/2.0;
							double c = (sp*epm1)*(sp*epm1);
							double shift = 0.0;
							if ((b != 0.0) | (c != 0.0))
							{
								shift = Math.sqrt(b*b + c);
								if (b < 0.0)
								{
									shift = -shift;
								}
								shift = c/(b + shift);
							}
							double f = (sk + sp)*(sk - sp) + shift;
							double g = sk*ek;
							for (int j = k; j < p-1; j++)
							{
								double t = hypot(f,g);
								double cs = f/t;
								double sn = g/t;
								if (j != k)
								{
									e[j-1]=t;
								}
								f = cs*s[j] + sn*e[j];
								e[j]=cs*e[j] - sn*s[j];
								g = sn*s[j+1];
								s[j+1]=cs*s[j+1];
								for (int i = 0; i < n; i++)
								{
									t = cs*v.read(i, j) + sn*v.read(i, j+1);
									v.write(-1*sn*v.read(i, j) + cs*v.read(i, j+1), i, j+1);
									v.write(t, i, j);
								}
								t = hypot(f,g);
								cs = f/t;
								sn = g/t;
								s[j]=t;
								f = cs*e[j] + sn*s[j+1];
								s[j+1]=-sn*e[j] + cs*s[j+1];
								g = sn*e[j+1];
								e[j+1]=cs*e[j+1];
								if (j < m-1)
								{
									for (int i = 0; i < m; i++)
									{
										t = cs*u.read(i, j) + sn*u.read(i, j+1);
										u.write(-1*sn*u.read(i, j) + cs*u.read(i, j+1), i, j+1);
										u.write(t, i, j);
									}
								}
							}
							e[p-2]=f;
							iter = iter + 1;
						}
						break;
						case 4:
						{
							if (s[k] <= 0.0)
							{
								s[k]=(s[k] < 0.0 ? -s[k] : 0.0);
								for (int i = 0; i <= pp; i++)
								{
									v.write(-1*v.read(i, k), i, k);
								}
							}
							while (k < pp)
							{
								if (s[k] >= s[k+1])
								{
									break;
								}
								double t = s[k];
								s[k]=s[k+1];
								s[k+1]=t;
								if (k < n-1)
								{
									for (int i = 0; i < n; i++)
									{
										t = v.read(i, k+1);
										v.write(v.read(i, k), i, k+1);
										v.write(t, i, k);
									}
								}
								if ((k < m-1))
								{
									for (int i = 0; i < m; i++)
									{
										t = u.read(i, k+1);
										u.write(u.read(i, k), i, k+1);
										u.write(t, i, k);
									}
								}
								k++;
							}
							iter = 0;
							p--;
						}
						break;
					}
				}
				e=new double[0];
				work=new double[0];
				e=null;
				work=null;
				A.close();
				ttt=new double[1];
				for (int i=0; i<dims; i++)
				{
					ttt[0]=s[i];
					S.write(tempgroup, ttt);
				}
				s=new double[0];
				s=null;
				int rows=u.getRows();
				int columns=u.getColumns();
				for (int i=0; i<rows; i++)
				{
					for (int j=0; j<columns; j++)
					{
						U.write(tempgroup, u.read(i, j), i, j);
					}
				}
				rows=v.getRows();
				columns=v.getColumns();
				for (int i=0; i<rows; i++)
				{
					for (int j=0; j<columns; j++)
					{
						V.write(tempgroup, v.read(i, j), i, j);
					}
				}
				v.close();
				u.close();
				M.deassignbasefile();
				V.deassignbasefile();
				U.deassignbasefile();
				S.deassignbasefile();
			}
		}
		catch (Exception eed)
		{
			if (v!=null)
				v.close();
			if (u!=null)
				u.close();
			if (V!=null)
				V.closeAll();
			if (A!=null)
				A.close();
			if (U!=null)
				U.closeAll();
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw, true);
			eed.printStackTrace(pw);
			pw.flush();
			sw.flush();
			iserror=true;
			message="Error in SingularValue Decomposition:\n"+sw.toString()+"\n";
		}
		if (A!=null)
			A.close();
		if (v!=null)
			v.close();
		if (u!=null)
			u.close();
	}
	public GroupedMatrix2Dfile getV()
	{
		return V;
	}
	public GroupedMatrix2Dfile getU()
	{
		return U;
	}
	public GroupedMatrix2Dfile gets()
	{
		return S;
	}
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
		if (U!=null)
			U.closeAll();
		if (S!=null)
			S.closeAll();
		if (A!=null)
			A.close();
		if (v!=null)
			v.close();
		if (u!=null)
			u.close();
	}
	public Hashtable<Vector<String>, Integer> getDim()
	{
		return maxvar;
	}
}
