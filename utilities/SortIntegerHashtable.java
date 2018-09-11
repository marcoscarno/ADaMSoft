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
import java.util.Iterator;

/**
*This class sort an Hashtable and returns its values sorted according the value and not the key
* @author marco.scarno@gmail.com
* @date 07/09/2015
*/
public class SortIntegerHashtable
{
	int [][]output;
	int index=1;
	/**
	 * Starts the method receiving the Hashtable
	 */
	public SortIntegerHashtable(Hashtable<Integer, Integer> table)
	{
		output= new int[table.size()][2];
		Iterator<Integer> it = table.keySet().iterator();
		int j=0;
		while(it.hasNext())
		{
			int el = (it.next()).intValue();
			output[j][0]= el;
			output[j][1]= (table.get(new Integer(el))).intValue();
			j++;
		}
	}
	/**
	*Excutes the sorting algorithm
	*/
	public void executesort()
	{
		if (output.length<2)
			return;
		else if (output.length<3)
		{
			if (output[0][index]>output[1][index])
			{
				swap(0, 1);
				return;
			}
		}
		else
			quickSort(0, output.length-1);
	}
	/**
	*Use this method to set the sort index (0 for key or 1 for value)
	*/
	public void setsortindex(int index)
	{
		this.index=index;
	}
	/**
	*Returns the sorted values
	*/
	public int[][] getsortedvalues()
	{
		return output;
	}
	/**
	*Sorts the values
	*/
	private void quickSort(int start, int end)
	{
        int i = start;
        int j = end;
		int center=output[(start+end)/2][index];
		do
		{
			while( (i < end) && (compareInteger(center, output[i][index]) > 0) )
				i++;
			while( (j > start) && (compareInteger(center, output[j][index]) < 0) )
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
		int[] temp = new int[2];
		temp[0]=output[index1][0];
		temp[1]=output[index1][1];
		output[index1][0] = output[index2][0];
		output[index1][1] = output[index2][1];
		output[index2][0] = temp[0];
		output[index2][1] = temp[1];
	}
	/**
	*Returns the compare result
	*/
	private int compareInteger(int a, int b)
	{
		if (a>b)
			return 1;
		else if(a<b)
			return -1;
		else
			return 0;
	}
}
