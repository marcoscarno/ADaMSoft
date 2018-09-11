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

import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Vector;

/**
* This class sort an Hashtable and returns its values sorted according the value that contains double numbers and not by the key
* @author marco.scarno@gmail.com
* @date 07/09/2015
*/
public class SortIntegerDoubleHashtable
{
	int []outputI;
	double []outputD;
	/**
	 * Starts the method receiving the Hashtable
	 */
	public SortIntegerDoubleHashtable(Hashtable<Integer, Double> table)
	{
		outputI= new int[table.size()];
		outputD= new double[table.size()];
		int j=0;
		for (Enumeration<Integer> tempenum = table.keys() ; tempenum.hasMoreElements() ;)
		{
			outputI[j]= (tempenum.nextElement()).intValue();
			outputD[j]= (table.get(new Integer(outputI[j]))).doubleValue();
			j++;
		}
		if (outputI.length==2)
		{
			if (outputD[0]>outputD[1])
				swap(0, 1);
		}
		if (outputI.length>2)
		{
			quickSort(0, outputI.length-1);
		}
	}
	/**
	*Return the index associated to the smallest value
	*/
	public int getsmallest()
	{
		return outputI[0];
	}
	/**
	*Returns the sorted values as a vector
	*/
	public Vector<Integer> getsortedvaluesvect()
	{
		Vector<Integer> retvect=new Vector<Integer>();
		for (int i=0; i<outputI.length; i++)
		{
			retvect.add(new Integer(outputI[i]));
		}
		return retvect;
	}
	/**
	*Returns the sorted values
	*/
	public int[] getsortedvalues()
	{
		return outputI;
	}
	/**
	*Sorts the values
	*/
	private void quickSort(int start, int end)
	{
        int i = start;
        int j = end;
		double center=outputD[(start+end)/2];
		do
		{
			while( (i < end) && (compareDouble(center, outputD[i]) > 0) )
				i++;
			while( (j > start) && (compareDouble(center, outputD[j]) < 0) )
				j--;
			if (i < j)
				swap(i, j);
			if (i <= j)
			{
				i++;
				j--;
			}
		}
		while(i <= j);
		if (start < j) quickSort(start, j);
		if (i < end) quickSort(i, end);
	}
	/**
	*Swap the values
	*/
	private void swap(int index1, int index2)
	{
		int temp = outputI[index1];
		double tempd = outputD[index1];
		outputI[index1] = outputI[index2];
		outputI[index2] = temp;
		outputD[index1] = outputD[index2];
		outputD[index2] = tempd;
	}
	/**
	*Returns the compare result
	*/
	private int compareDouble(double a, double b)
	{
		if (a>b)
			return 1;
		else if(a<b)
			return -1;
		else
			return 0;
	}
}
