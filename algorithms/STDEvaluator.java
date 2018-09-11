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
import java.util.Enumeration;
import java.lang.Math;

/**
* This method evaluates the standard deviations for several variables, also according do the values of the grouping variables
* @author marco.scarno@gmail.com
* @date 13/02/2017
*/
public class STDEvaluator
{
	Hashtable<Vector<String>, double[]> mean;
	Hashtable<Vector<String>, double[]> ssq;
	Hashtable<Vector<String>, double[]> n;
	boolean samplevariance;
	/**
	*Initialise the main Objects, that will contains the means, the number of valid cases, the sum of squares
	*/
	public STDEvaluator (boolean samplevariance)
	{
		this.samplevariance=samplevariance;
		mean=new Hashtable<Vector<String>, double[]>();
		n=new Hashtable<Vector<String>, double[]>();
		ssq=new Hashtable<Vector<String>, double[]>();
	}
	/**
	* Evaluate the means
	*/
	public void setValue(Vector<String> groupval, double[] val, double w)
	{
		double[] test=mean.get(groupval);
		double[] valid=new double[0];
		double[] sumsq=new double[0];
		if (test==null)
		{
			test=new double[val.length];
			valid=new double[val.length];
			sumsq=new double[val.length];
			if (!Double.isNaN(w))
			{
				for (int i=0; i<val.length; i++)
				{
					if (!Double.isNaN(val[i]))
					{
						test[i]=val[i]*w;
						valid[i]=w;
						sumsq[i]=val[i]*val[i]*w;
					}
					else
					{
						test[i]=Double.NaN;
						valid[i]=Double.NaN;
						sumsq[i]=Double.NaN;
					}
				}
			}
			else
			{
				for (int i=0; i<val.length; i++)
				{
					test[i]=Double.NaN;
					valid[i]=Double.NaN;
					sumsq[i]=Double.NaN;
				}
			}
		}
		else
		{
			valid=n.get(groupval);
			sumsq=ssq.get(groupval);
			if (!Double.isNaN(w))
			{
				for (int i=0; i<val.length; i++)
				{
					if ((!Double.isNaN(test[i])) && (!Double.isNaN(val[i])))
					{
						sumsq[i]=sumsq[i]+val[i]*val[i]*w;
						test[i]=test[i]+val[i]*w;
						valid[i]=valid[i]+w;
					}
					else if (!Double.isNaN(val[i]))
					{
						sumsq[i]=val[i]*val[i]*w;
						test[i]=val[i]*w;
						valid[i]=w;
					}
				}
			}
		}
		mean.put(groupval, test);
		n.put(groupval, valid);
		ssq.put(groupval, sumsq);
	}
	/**
	*Finalizes the standard deviations
	*/
	public void calculate()
	{
		for (Enumeration<Vector<String>> e = mean.keys() ; e.hasMoreElements() ;)
		{
			Vector<String> gv=e.nextElement();
			double[] v = mean.get(gv);
			double[] s = ssq.get(gv);
			double[] num =n.get(gv);
			double[] stdval=new double[v.length];
			for (int i=0; i<v.length; i++)
			{
				stdval[i]=Double.NaN;
				try
				{
					stdval[i]=(s[i]/num[i])-(v[i]/num[i])*(v[i]/num[i]);
					if ((!samplevariance) && (num[i]>1))
						stdval[i]=(stdval[i]*num[i])/(num[i]-1);
					if (stdval[i]<0) stdval[i]=0.0;
					stdval[i]=Math.sqrt(stdval[i]);
				}
				catch (Exception ex) {}
			}
			ssq.put(gv, stdval);
		}
		mean.clear();
		n.clear();
		mean=null;
		n=null;
	}
	/**
	*Gives back the standard deviation
	*/
	public Hashtable<Vector<String>, double[]> getresult()
	{
		return ssq;
	}
}
