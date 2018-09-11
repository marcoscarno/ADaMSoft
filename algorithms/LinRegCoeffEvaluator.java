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

/**
* This method evaluates the linear regression coefficients for several variables, also according do the values of the grouping variables
* @author marco.scarno@gmail.com
* @date 13/02/2017
*/
public class LinRegCoeffEvaluator
{
	Hashtable<Vector<String>, double[][]> meanx;
	Hashtable<Vector<String>, double[][]> meany;
	Hashtable<Vector<String>, double[][]> ssqxy;
	Hashtable<Vector<String>, double[][]> ssqy;
	Hashtable<Vector<String>, double[][]> numxy;
	Hashtable<Vector<String>, double[][]> linregcoeff;
	boolean pairwise;
	boolean samevars=false;
	/**
	*Initialise the main Objects, that will contains the means, the number of valid cases, the sum of squares, etc.
	*/
	public LinRegCoeffEvaluator (boolean pairwise, boolean samevars)
	{
		this.pairwise=pairwise;
		this.samevars=samevars;
		meanx=new Hashtable<Vector<String>, double[][]>();
		meany=new Hashtable<Vector<String>, double[][]>();
		ssqxy=new Hashtable<Vector<String>, double[][]>();
		ssqy=new Hashtable<Vector<String>, double[][]>();
		numxy=new Hashtable<Vector<String>, double[][]>();
		linregcoeff=new Hashtable<Vector<String>, double[][]>();
	}
	/**
	* Evaluate the variance components
	*/
	public void setValue(Vector<String> groupval, double[] valrow, double[] valcol, double w)
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
			for (int i=0; i<valcol.length; i++)
			{
				if (Double.isNaN(valcol[i]))
					ismissing=true;
			}
			if (ismissing)
			{
				for (int i=0; i<valrow.length; i++)
				{
					valrow[i]=Double.NaN;
				}
				for (int i=0; i<valcol.length; i++)
				{
					valcol[i]=Double.NaN;
				}
			}
		}
		if (mx==null)
		{
			mx=new double[valrow.length][valcol.length];
			double[][] my=new double[valrow.length][valcol.length];
			double[][] sy=new double[valrow.length][valcol.length];
			double[][] sxy=new double[valrow.length][valcol.length];
			double[][] nxy=new double[valrow.length][valcol.length];
			for (int i=0; i<valrow.length; i++)
			{
				for (int j=0; j<valcol.length; j++)
				{
					if ((!Double.isNaN(valrow[i])) && (!Double.isNaN(valcol[j])))
					{
						sxy[i][j]=valrow[i]*valcol[j]*w;
						nxy[i][j]=w;
						mx[i][j]=valrow[i]*w;
						my[i][j]=valcol[j]*w;
						sy[i][j]=valcol[j]*valcol[j]*w;
					}
					else
					{
						sxy[i][j]=Double.NaN;
						nxy[i][j]=Double.NaN;
						mx[i][j]=Double.NaN;
						my[i][j]=Double.NaN;
						sy[i][j]=Double.NaN;
					}
				}
			}
			meanx.put(groupval, mx);
			meany.put(groupval, my);
			ssqy.put(groupval, sy);
			ssqxy.put(groupval, sxy);
			numxy.put(groupval, nxy);
		}
		else
		{
			double[][] my=meany.get(groupval);
			double[][] sy=ssqy.get(groupval);
			double[][] sxy=ssqxy.get(groupval);
			double[][] nxy=numxy.get(groupval);
			for (int i=0; i<valrow.length; i++)
			{
				for (int j=0; j<valcol.length; j++)
				{
					if ((!Double.isNaN(valrow[i])) && (!Double.isNaN(valcol[j])))
					{
						if (!Double.isNaN(sxy[i][j]))
							sxy[i][j]=sxy[i][j]+valrow[i]*valcol[j]*w;
						else
							sxy[i][j]=valrow[i]*valcol[j]*w;
						if (!Double.isNaN(nxy[i][j]))
							nxy[i][j]=nxy[i][j]+w;
						else
							nxy[i][j]=w;
						if (!Double.isNaN(mx[i][j]))
							mx[i][j]=mx[i][j]+valrow[i]*w;
						else
							mx[i][j]=valrow[i]*w;
						if (!Double.isNaN(my[i][j]))
							my[i][j]=my[i][j]+valcol[j]*w;
						else
							my[i][j]=valcol[j]*w;
						if (!Double.isNaN(sy[i][j]))
							sy[i][j]=sy[i][j]+valcol[j]*valcol[j]*w;
						else
							sy[i][j]=valcol[j]*valcol[j]*w;
					}
				}
			}
			meanx.put(groupval, mx);
			meany.put(groupval, my);
			ssqy.put(groupval, sy);
			ssqxy.put(groupval, sxy);
			numxy.put(groupval, nxy);
		}
	}
	/**
	*Finalizes the variances
	*/
	public void calculate()
	{
		for (Enumeration<Vector<String>> e = meany.keys() ; e.hasMoreElements() ;)
		{
			Vector<String> gv= e.nextElement();
			double[][] mx=meanx.get(gv);
			double[][] my=meany.get(gv);
			double[][] sy=ssqy.get(gv);
			double[][] sxy=ssqxy.get(gv);
			double[][] nxy=numxy.get(gv);
			double[][] linregcoeffval=new double[mx.length][mx[0].length];
			for (int i=0; i<mx.length; i++)
			{
				for (int j=0; j<mx[0].length; j++)
				{
					if ((i==j) && (samevars))
						linregcoeffval[i][j]=1;
					else
					{
						try
						{
							double tempcovar=sxy[i][j]/nxy[i][j]-((mx[i][j]/nxy[i][j])*(my[i][j]/nxy[i][j]));
							double tempvary=sy[i][j]/nxy[i][j]-((my[i][j]/nxy[i][j])*(my[i][j]/nxy[i][j]));
							linregcoeffval[i][j]=tempcovar/tempvary;
						}
						catch (Exception ex)
						{
							linregcoeffval[i][j]=Double.NaN;
						}
					}
				}
			}
			linregcoeff.put(gv, linregcoeffval);
		}
		meanx.clear();
		meany.clear();
		ssqy.clear();
		ssqxy.clear();
		numxy.clear();
		meanx=null;
		meany=null;
		ssqy=null;
		ssqxy=null;
		numxy=null;
		System.gc();
	}
	/**
	*Gives back the correlations
	*/
	public Hashtable<Vector<String>, double[][]> getresult()
	{
		return linregcoeff;
	}
}
