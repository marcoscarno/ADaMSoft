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

import java.io.IOException;

import ch.javasoft.math.array.ArrayOperations;
import ch.javasoft.polco.xenum.ExtremeRayCallback;
import ch.javasoft.polco.xenum.ExtremeRayEvent;
import ch.javasoft.math.BigFraction;
import java.util.*;

/**
* This implements the call back of the polco adapter used to find the solutions
* @author marco.scarno@gmail.com
* @date 13/02/2017
*/
class PolcoSolutorAdapter<Num extends Number, Arr> implements ExtremeRayCallback<Num, Arr>
{
	@SuppressWarnings("unused")
	private final ArrayOperations<Arr> arrayOps;
	int numberofsolutions, dimension;
	double currentcard, maxcard, tempval, tolerance;
	double[] valweights;
	Vector<double[]> solutions;
	double[][] tempeq;
	double[][] tempiq;
	double[] recval;
	double[] tempdsol;
	int numsatisfied, actualsatisfied;
	double percmemory;
	Runtime runtime;
	boolean nomemory;
	double zeroalg=0.00000000001;
	/**
	 * Constructor for <code>AdapterCallback</code> with array operations to
	 * create the output matrix
	 *
	 * @param arrayOps	array operations to create output matrix
	 */
	public PolcoSolutorAdapter(ArrayOperations<Arr> arrayOps)
	{
		nomemory=false;
		runtime = Runtime.getRuntime();
		this.arrayOps = arrayOps;
		solutions=new Vector<double[]>();
	}
	/**
	*Receives the edits to check according to the actual solution
	*/
	public void setEdits(double[][] tempeq, double[][] tempiq, double[] recval, double tolerance)
	{
		this.tempeq=tempeq;
		this.tempiq=tempiq;
		this.recval=recval;
		this.tolerance=tolerance;
		numsatisfied=0;
		if (tempiq!=null) numsatisfied=numsatisfied+tempiq.length;
		if (tempeq!=null) numsatisfied=numsatisfied+tempeq.length;
	}
	/**
	*Receives the weights
	*/
	public void setWeights(Vector<Double> weight)
	{
		solutions.clear();
		currentcard=0;
		maxcard=0;
		dimension=weight.size();
		valweights=new double[dimension];
		for (int i=0; i<weight.size(); i++)
		{
			valweights[i]=(weight.get(i)).doubleValue();
			maxcard=maxcard+valweights[i];
		}
	}
	public void setzeroalg(double zeroalg)
	{
		this.zeroalg=zeroalg;
	}
	/**
	*Clean the memory
	*/
	public void cleanMem()
	{
		valweights=new double[0];
		solutions.clear();
		tempeq=new double[0][0];
		tempiq=new double[0][0];
		recval=new double[0];
		tempdsol=new double[0];
		valweights=null;
		solutions=null;
		tempeq=null;
		tempiq=null;
		recval=null;
		tempdsol=null;
	}
	/**
	*Initialize the call back algorithm
	*/
	public boolean initialize(ExtremeRayEvent<Num, Arr> event)
	{
		final long count = event.getRayCount();
		numberofsolutions=(int)count;
		return true; //if true we want the rays
	}
	/**
	*Outputs the extreme rays and evaluate the cardinality
	*/
	public void outputExtremeRay(ExtremeRayEvent<Num,Arr> event, long index, Arr extremeRay) throws IOException
	{
		percmemory=(runtime.totalMemory() - runtime.freeMemory())/(runtime.maxMemory());
		if (percmemory>0.8)
		{
			nomemory=true;
			terminate(event);
		}
		BigFraction[] temparr=(BigFraction[])extremeRay;
		if (temparr[0].isNonZero())
		{
			actualsatisfied=0;
			currentcard=0;
			tempdsol=new double[dimension];
			for (int m=0; m<dimension; m++)
			{
				tempdsol[m]=0.0;
				if ((temparr[m+1].isNonZero()) || (temparr[dimension+m+1].isNonZero()))
				{
					tempdsol[m]=((temparr[m+1].subtract(temparr[dimension+m+1])).divide(temparr[0])).doubleValue();
					if (Math.abs(tempdsol[m])>zeroalg) currentcard=currentcard+valweights[m];
					else tempdsol[m]=0.0;
				}
			}
			if ((tempiq!=null) && (currentcard<=maxcard))
			{
				for (int i=0; i<tempiq.length; i++)
				{
					tempval=0;
					for (int j=1; j<tempiq[0].length; j++)
					{
						tempval=tempval+(recval[j-1]+tempdsol[j-1])*tempiq[i][j];
					}
					tempval=tempval+tempiq[i][0];
					if (tempval>=(-1*tolerance)) actualsatisfied++;
				}
			}
			if ((tempeq!=null) && (currentcard<=maxcard))
			{
				for (int i=0; i<tempeq.length; i++)
				{
					tempval=0;
					for (int j=1; j<tempeq[0].length; j++)
					{
						tempval=tempval+(recval[j-1]+tempdsol[j-1])*tempeq[i][j];
					}
					tempval=tempval+tempeq[i][0];
					if (Math.abs(tempval)<=tolerance) actualsatisfied++;
				}
			}
			if (actualsatisfied==numsatisfied)
			{
				if (currentcard<maxcard)
				{
					solutions.clear();
					maxcard=currentcard;
					double[] newres=new double[dimension];
					for (int i=0; i<newres.length; i++)
					{
						newres[i]=tempdsol[i];
					}
					solutions.add(newres);
				}
				else if (currentcard==maxcard)
				{
					double[] newres=new double[dimension];
					for (int i=0; i<newres.length; i++)
					{
						newres[i]=tempdsol[i];
					}
					solutions.add(newres);
				}
			}
		}
	};
	//inherit javadoc
	public void terminate(ExtremeRayEvent<Num, Arr> event) throws IOException
	{
		if (nomemory)
		{
			maxcard=-1;
			solutions.clear();
			throw new IndexOutOfBoundsException("No more memory");
		}
	}
	public void yeld()
	{
		return;
	}
	/**
	*Return the number of solutions
	*/
	public int getnumsol()
	{
		if (solutions.size()==0)
			numberofsolutions=0;
		return numberofsolutions;
	}
	/**
	*Return the current cardinality
	*/
	public double getCardinality()
	{
		if (solutions.size()==0)
			maxcard=-1;
		return maxcard;
	}
	/**
	*Return the solutions
	*/
	public Vector<double[]> getSolutions()
	{
		return solutions;
	}
}
