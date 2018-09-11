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
class PolcoSolutorAdapterSimple<Num extends Number, Arr> implements ExtremeRayCallback<Num, Arr>
{
	@SuppressWarnings("unused")
	private final ArrayOperations<Arr> arrayOps;
	double tempval;
	Vector<double[]> solutions;
	double[][] tempeq;
	double[][] tempiq;
	double percmemory;
	Runtime runtime;
	boolean nomemory;
	int dimension, numberofsolutions;
	/**
	 * Constructor for <code>AdapterCallback</code> with array operations to
	 * create the output matrix
	 *
	 * @param arrayOps	array operations to create output matrix
	 */
	public PolcoSolutorAdapterSimple(ArrayOperations<Arr> arrayOps)
	{
		nomemory=false;
		runtime = Runtime.getRuntime();
		this.arrayOps = arrayOps;
		solutions=new Vector<double[]>();
	}
	/**
	*Receives the edits to check according to the actual solution
	*/
	public void setEdits(double[][] tempeq, double[][] tempiq)
	{
		this.tempeq=tempeq;
		this.tempiq=tempiq;
		dimension=tempiq[0].length-1;
	}
	/**
	*Clean the memory
	*/
	public void cleanMem()
	{
		solutions.clear();
		solutions=null;
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
			double[] newres=new double[dimension];
			for (int m=0; m<dimension; m++)
			{
				newres[m]=0.0;
				if (temparr[m+1].isNonZero())
				{
					newres[m]=(temparr[m+1].divide(temparr[0])).doubleValue();
				}
			}
			solutions.add(newres);
		}
	};
	//inherit javadoc
	public void terminate(ExtremeRayEvent<Num, Arr> event) throws IOException
	{
		if (nomemory)
		{
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
	*Return the solutions
	*/
	public Vector<double[]> getSolutions()
	{
		return solutions;
	}
}
