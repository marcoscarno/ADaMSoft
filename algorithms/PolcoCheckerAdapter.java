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
class PolcoCheckerAdapter<Num extends Number, Arr> implements ExtremeRayCallback<Num, Arr>
{
	@SuppressWarnings("unused")
	private final ArrayOperations<Arr> arrayOps;
	int numberofsolutions, dimension, actualdim;
	Vector<Integer> candidates;
	Runtime runtime;
	double percmemory;
	boolean nomemory;
	boolean maxreached;
	/**
	 * Constructor for <code>AdapterCallback</code> with array operations to
	 * create the output matrix
	 *
	 * @param arrayOps	array operations to create output matrix
	 */
	public PolcoCheckerAdapter(ArrayOperations<Arr> arrayOps)
	{
		nomemory=false;
		runtime = Runtime.getRuntime();
		this.arrayOps = arrayOps;
		candidates=new Vector<Integer>();
		maxreached=false;
		dimension=Integer.MAX_VALUE;
	}
	/**
	*Clean the memory
	*/
	public void cleanMem()
	{
		candidates.clear();
		candidates=null;
	}
	/**
	*Initialize the call back algorithm
	*/
	public boolean initialize(ExtremeRayEvent<Num, Arr> event)
	{
		final long count = event.getRayCount();
		numberofsolutions=(int)count;
		return true;
	}
	//inherit javadoc
	public void outputExtremeRay(ExtremeRayEvent<Num,Arr> event, long index, Arr extremeRay) throws IOException
	{
		percmemory=(runtime.totalMemory() - runtime.freeMemory())/(runtime.maxMemory());
		if (percmemory>0.8)
		{
			nomemory=true;
			terminate(event);
		}
		BigFraction[] temparr=(BigFraction[])extremeRay;
		actualdim=0;
		if (temparr[0].isNonZero())
		{
			for (int m=1; m<temparr.length; m++)
			{
				if (temparr[m].isNonZero()) actualdim++;
			}
			if (actualdim>0 && actualdim<dimension)
			{
				dimension=actualdim;
				candidates.clear();
				maxreached=false;
				for (int m=1; m<temparr.length; m++)
				{
					if ((temparr[m].isNonZero()) && (!candidates.contains(new Integer(m-1)))) candidates.add(new Integer(m-1));
				}
			}
			if (actualdim>0 && actualdim==dimension)
			{
				for (int m=1; m<temparr.length; m++)
				{
					if ((temparr[m].isNonZero()) && (!candidates.contains(new Integer(m-1)))) candidates.add(new Integer(m-1));
				}
			}
			if (candidates.size()>15) maxreached=true;
		}
	}
	//inherit javadoc
	public void terminate(ExtremeRayEvent<Num, Arr> event) throws IOException
	{
		if (nomemory)
		{
			candidates.clear();
			numberofsolutions=0;
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
		return numberofsolutions;
	}
	/**
	*Return the current cardinality
	*/
	public Vector<Integer> getCandidates()
	{
		return candidates;
	}
}
