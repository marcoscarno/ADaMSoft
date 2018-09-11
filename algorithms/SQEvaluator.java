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

/**
* This method evaluates the sum of squares for several variables, also according to the values of the grouping variables
* @author marco.scarno@gmail.com
* @date 13/02/2017
*/
public class SQEvaluator
{
	Hashtable<Vector<String>, double[]> sq;
	/**
	*Initialise the main Objects, that will contains the sum of squares
	*/
	public SQEvaluator ()
	{
		sq=new Hashtable<Vector<String>, double[]>();
	}
	/**
	* Evaluate the corrected sum of squares
	*/
	public void setValue(Vector<String> groupval, double[] val, double w)
	{
		double[] test=sq.get(groupval);
		if (test==null)
		{
			test =new double[val.length];
			if (!Double.isNaN(w))
			{
				for (int i=0; i<val.length; i++)
				{
					if (!Double.isNaN(val[i]))
					{
						test[i]=val[i]*val[i]*w;
					}
					else
					{
						test[i]=Double.NaN;
					}
				}
			}
			else
			{
				for (int i=0; i<val.length; i++)
				{
					test[i]=Double.NaN;
				}
			}
		}
		else
		{
			if (!Double.isNaN(w))
			{
				for (int i=0; i<val.length; i++)
				{
					if ((!Double.isNaN(test[i])) && (!Double.isNaN(val[i])))
					{
						test[i]=test[i]+val[i]*val[i]*w;
					}
					else if (!Double.isNaN(val[i]))
					{
						test[i]=val[i]*val[i]*w;
					}
				}
			}
		}
		sq.put(groupval, test);
	}
	/**
	*Gives back the sum of squares
	*/
	public Hashtable<Vector<String>, double[]> getresult()
	{
		return sq;
	}
}
