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

/**
* This this class divides the values in a set of n bin between min and max
* @author marco.scarno@gmail.com
* @date 13/02/2017
*/
public class BinDivider
{
	private int total;
	private double min;
	private double max;
	private double[] binFill;
	private double lBin;
	public BinDivider(int binCount, double min, double max)
	{
		this.min = min;
		this.max = max;
		lBin=Math.abs((max-min)/(binCount-1));
		binFill = new double[binCount];
		for (int i=0; i<binCount; i++)
		{
			binFill[i]=0;
		}
	}
	public int getTotal()
	{
		return total;
	}
	public void addValue(double value, double weight)
	{
		if ((!Double.isNaN(value)) && (!Double.isNaN(weight)))
		{
			if((value<=max) && (value>=min))
			{
				int pos=calcPos(value);
				total+=weight;
				binFill[pos]+=weight;
			}
		}
	}
	public double getBinValue(int bin)
	{
		return binFill[bin];
	}
	public double getBinValuePercent(int bin)
	{
		return binFill[bin]/total;
	}
	public double getBinUpperEdge(int bin)
	{
		return min+(bin+1)*lBin;
	}
	public double getBinLowerEdge(int bin)
	{
		return min+(bin)*lBin;
	}
	public double getBinCentre(int bin)
	{
		return (getBinUpperEdge(bin)+getBinLowerEdge(bin))/2;
	}
	private int calcPos(double value)
	{
		int pos =(int)Math.floor(((value-min)/lBin));
		return pos;
	}
}
