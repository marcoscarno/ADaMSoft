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

/**
* This implements the call back of the polco adapter used to find the solutions
* @author marco.scarno@gmail.com
* @date 13/02/2017
*/
class PolcoSolAdapter<Num extends Number, Arr> implements ExtremeRayCallback<Num, Arr>
{
	@SuppressWarnings("unused")
	private final ArrayOperations<Arr> arrayOps;
	int numsolutions=0;
	/**
	 * Constructor for <code>AdapterCallback</code> with array operations to
	 * create the output matrix
	 *
	 * @param arrayOps	array operations to create output matrix
	 */
	public PolcoSolAdapter(ArrayOperations<Arr> arrayOps)
	{
		this.arrayOps = arrayOps;
	}
	/**
	*Initialize the call back algorithm
	*/
	public boolean initialize(ExtremeRayEvent<Num, Arr> event) throws IOException
	{
		final long count = event.getRayCount();
		numsolutions=(int)count;
		if (count > Integer.MAX_VALUE)
		{
			throw new IndexOutOfBoundsException("too many extreme rays (out of int range): " + count);
		}
		return false;
	}
	//inherit javadoc
	public void outputExtremeRay(ExtremeRayEvent<Num,Arr> event, long index, Arr extremeRay) throws IOException
	{
		//nothing to do
	}
	//inherit javadoc
	public void terminate(ExtremeRayEvent<Num, Arr> event) throws IOException
	{
		//nothing to do
	}
	public int yeld()
	{
		return numsolutions;
	}
}
