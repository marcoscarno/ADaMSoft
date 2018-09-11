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
* This method evaluates the matrix of the number of valid observations for each couple of variables
* @author marco.scarno@gmail.com
* @date 13/02/2017
*/
public class NValidEvaluator
{
	Hashtable<Vector<String>, double[][]> numxy;
	boolean pairwise;
	/**
	*Initialise the main Objects, that will contains the means, the number of valid cases, the sum of squares, etc.
	*/
	public NValidEvaluator (boolean pairwise)
	{
		this.pairwise=pairwise;
		numxy=new Hashtable<Vector<String>, double[][]>();
	}
	/**
	* Evaluate the variance components
	*/
	public void setValue(Vector<String> groupval, double[] valrow, double[] valcol, double w)
	{
		double[][] nxy=numxy.get(groupval);
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
		if (nxy==null)
		{
			nxy=new double[valrow.length][valcol.length];
			for (int i=0; i<valrow.length; i++)
			{
				for (int j=0; j<valcol.length; j++)
				{
					if ((!Double.isNaN(valrow[i])) && (!Double.isNaN(valcol[j])))
						nxy[i][j]=w;
					else
						nxy[i][j]=Double.NaN;
				}
			}
			numxy.put(groupval, nxy);
		}
		else
		{
			for (int i=0; i<valrow.length; i++)
			{
				for (int j=0; j<valcol.length; j++)
				{
					if ((!Double.isNaN(valrow[i])) && (!Double.isNaN(valcol[j])))
					{
						if (!Double.isNaN(nxy[i][j]))
							nxy[i][j]=nxy[i][j]+w;
						else
							nxy[i][j]=+w;
					}
				}
			}
			numxy.put(groupval, nxy);
		}
	}
	/**
	*Gives back the matrix of valid observations
	*/
	public Hashtable<Vector<String>, double[][]> getresult()
	{
		return numxy;
	}
}
