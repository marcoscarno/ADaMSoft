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

import java.util.TreeMap;
import java.util.Iterator;

/**
*This class sort an Treemap and returns its values sorted according the value and not the key
* @author marco.scarno@gmail.com
* @date 07/09/2015
*/
public class SortTreemap
{
	String [][]output;
	int index=1;
	/**
	 * Starts the method receiving the Hashtable
	 */
	public SortTreemap(TreeMap<String, String> table)
	{
		output= new String[table.size()][2];
		Iterator<String> it = table.keySet().iterator();
		int j=0;
		while(it.hasNext())
		{
			String el = it.next();
			output[j][0]= el;
			output[j][1]= table.get(el);
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
		if (output.length<3)
		{
			if ((output[0][index]==null) && (output[1][index]==null))
				return;
			else if (output[0][index]==null)
			{
				swap(0, 1);
				return;
			}
			if ((output[0][index].equals("")) && (output[1][index].equals("")))
				return;
			else if (output[0][index].equals(""))
			{
				swap(0, 1);
				return;
			}
			double anum=Double.NaN;
			double bnum=Double.NaN;
			try
			{
				anum=Double.parseDouble(output[0][index]);
				bnum=Double.parseDouble(output[1][index]);
			}
			catch (Exception nonnumber) {}
			if ((!Double.isNaN(anum)) && (!Double.isNaN(bnum)))
			{
				if (anum>bnum)
				{
					swap(0, 1);
					return;
				}
			}
			else
			{
				if (output[0][index].compareTo(output[1][index])>0)
				{
					swap(0, 1);
					return;
				}
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
	public String[][] getsortedvalues()
	{
		return output;
	}
	/**
	*Returns an Treemap that contains the keys and the integer that refers to its position according to the value
	*/
	public TreeMap<String, Integer> getsortedtreemap()
	{
		TreeMap<String, Integer> position=new TreeMap<String, Integer>();
		for (int i=0; i<output.length; i++)
		{
			position.put(output[i][0], new Integer(i));
		}
		return position;
	}
	/**
	*Sorts the values
	*/
	private void quickSort(int start, int end)
	{
        int i = start;
        int j = end;
		String center=output[(start+end)/2][index];
		do
		{
			while( (i < end) && (compareString(center, output[i][index]) > 0) )
				i++;
			while( (j > start) && (compareString(center, output[j][index]) < 0) )
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
		String[] temp = output[index1];
		output[index1] = output[index2];
		output[index2] = temp;
	}
	/**
	*Returns the compare result
	*/
	private int compareString(String a, String b)
	{
		if ((a==null) && (b==null))
			return 0;
		else if (b==null)
			return -1;
		else if (a==null)
			return 1;
		if ((a.equals("")) && (b.equals("")))
			return 0;
		else if (b.equals(""))
			return -1;
		else if (a.equals(""))
			return 1;

		double anum=Double.NaN;
		double bnum=Double.NaN;
		try
		{
			anum=Double.parseDouble(a);
			bnum=Double.parseDouble(b);
		}
		catch (Exception nonnumber) {}
		if ((!Double.isNaN(anum)) && (!Double.isNaN(bnum)))
		{
			if (anum>bnum)
				return 1;
			else if(anum<bnum)
				return -1;
			else
				return 0;
		}
		else
		{
			return a.compareTo(b);
		}
	}
}
