/**
* Copyright (c) 2015 MS
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

package ADaMSoft.utilities;

import java.util.Arrays;


/**
* This class sorts the element of a matrix according to the sort order of a vector
* @author marco.scarno@gmail.com
* @date 07/09/2015
*/
public class MatrixSort
{
	double[][] orderedmatrix;
	double[] orderedvector;
	/**
	* Constructor (receives the vector and the matrix and sort the elements according to the values of the vector
	*/
	public MatrixSort(double[] infovector, double[][] infomatrix)
	{
		orderedvector=new double[infovector.length];
		orderedmatrix=new double[infomatrix[0].length][infomatrix.length];
		int[] neworder=new int[infovector.length];
		double[] temp=new double[infovector.length];
		for (int i=0; i<infovector.length; i++)
		{
			temp[i]=infovector[i];
		}
		Arrays.sort(temp);
		for (int i=0; i<infovector.length; i++)
		{
			double element=infovector[i];
			neworder[i]=Arrays.binarySearch(temp, element);
		}
		for (int i=(neworder.length-1); i>-1; i--)
		{
			orderedvector[neworder.length-i-1]=infovector[neworder[i]];
			for (int j=0; j<infomatrix.length; j++)
			{
				orderedmatrix[j][neworder.length-i-1]=infomatrix[j][neworder[i]];
			}
		}
	}
	/**
	*Returns the ordered matrix
	*/
	public double[][] getorderedmatrix()
	{
		return orderedmatrix;
	}
	/**
	*Returns the ordered vector
	*/
	public double[] getorderedvector()
	{
		return orderedvector;
	}
}
