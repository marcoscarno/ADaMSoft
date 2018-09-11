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

package ADaMSoft.algorithms.clusters;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;
import org.apache.commons.math3.ml.distance.*;

/**
* This interface specifies the behaviour of distance class
* @author marco.scarno@gmail.com
* @date 13/02/2017
*/
public class EvaluateDistance
{
	int type;
	Hashtable<Vector<String>, double[][]> weights;
	Vector<String> v;
	CanberraDistance cd;
	EarthMoversDistance emd;
	public EvaluateDistance(int type)
	{
		this.type=type;
		if (type==6) cd=new CanberraDistance();
		if (type==7) emd=new EarthMoversDistance();
	}
	public void setweights(Hashtable<Vector<String>, double[][]> varweights)
	{
		weights=new Hashtable<Vector<String>, double[][]>();
		for (Enumeration<Vector<String>> e = varweights.keys() ; e.hasMoreElements() ;)
		{
			Vector<String> tempg=e.nextElement();
			double[][] xx=varweights.get(tempg);
			DoubleMatrix2D MatXX=DoubleFactory2D.dense.make(xx.length, xx[0].length);
			for (int i=0; i<xx.length; i++)
			{
				for (int j=0; j<xx[0].length; j++)
				{
					MatXX.set(i, j, xx[i][j]);
				}
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
			weights.put(tempg, tempinvmatxx);
		}
	}
	public void setGroup(Vector<String> v)
	{
		this.v=v;
	}
	public double getdistance(double[] aCoord, double[] bCoord)
	{
		double distance =0;
		if (type==1)
		{
			for(int i=0; i<aCoord.length;i++)
			{
				if ((!Double.isNaN(aCoord[i])) && (!Double.isNaN(bCoord[i])))
					distance+=Math.pow(aCoord[i]-bCoord[i],2);
			}
			return Math.sqrt(distance);
		}
		else if (type==2)
		{
			for(int i=0; i<aCoord.length;i++)
			{
				if ((!Double.isNaN(aCoord[i])) && (!Double.isNaN(bCoord[i])))
					distance+=Math.pow(aCoord[i]-bCoord[i],2);
			}
			return distance;
		}
		else if (type==3)
		{
			for(int i=0; i<aCoord.length;i++)
			{
				if ((!Double.isNaN(aCoord[i])) && (!Double.isNaN(bCoord[i])))
					distance+=Math.abs(aCoord[i]-bCoord[i]);
			}
			return distance;
		}
		else if (type==4)
		{
			distance =-1.7976931348623157E308;
			for(int i=0; i<aCoord.length;i++)
			{
				if ((!Double.isNaN(aCoord[i])) && (!Double.isNaN(bCoord[i])))
				{
					double value = Math.abs(aCoord[i]-bCoord[i]);
					if(distance<value)
						distance = value;
				}
			}
			return distance;
		}
		else if (type==6)
		{
			return cd.compute(aCoord, bCoord);
		}
		else if (type==7)
		{
			return emd.compute(aCoord, bCoord);
		}
		else
		{
			double[][] tempinvmatxx=weights.get(v);
			double td=0;
			for(int i=0; i<aCoord.length;i++)
			{
				if (!Double.isNaN(aCoord[i]))
				{
					td=0;
					for (int j=0; j<aCoord.length; j++)
					{
						if (!Double.isNaN(bCoord[i]))
							td=td+(aCoord[i]-bCoord[i])*tempinvmatxx[j][i];
					}
					if (!Double.isNaN(bCoord[i]))
						distance+=(aCoord[i]-bCoord[i])*td;
				}
			}
			distance=Math.sqrt(distance);
		}
		return Math.sqrt(distance);
	}
}
