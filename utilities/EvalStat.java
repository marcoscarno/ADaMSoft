/**
* Copyright (c) MS
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


/**
* This method creates an object that can be used to eval several statistics on ore or more numerical values
* @author marco.scarno@gmail.com
* @date 07/09/2015
*/
public class EvalStat
{
	double min;
	double max;
	double num;
	double mean;
	double sum;
	double std;
	double var;
	double ssq;
	/**
	* Initializes the objects
	*/
	public EvalStat ()
	{
		min=Double.MAX_VALUE;
		max=-1.7976931348623157E308;
		num=0;
		sum=0;
		ssq=0;
	}
	/**
	*Adds a numeric value to the objects
	*/
	public void addval(double tval)
	{
		if ( (!Double.isNaN(tval)) && (!Double.isInfinite(tval)) )
		{
			sum=sum+tval;
			if (tval>max)
				max=tval;
			if (tval<min)
				min=tval;
			ssq=ssq+tval*tval;
			num=num+1;
		}
	}
	/**
	*Adds a numeric value to the objects by considering a weight
	*/
	public void addval(double tval, double weight)
	{
		if ( (!Double.isNaN(tval)) && (!Double.isInfinite(tval)) )
		{
			if ( (!Double.isNaN(weight)) && (!Double.isInfinite(weight)) )
			{
				sum=sum+tval*weight;
				if (tval>max)
					max=tval;
				if (tval<min)
					min=tval;
				ssq=ssq+tval*tval*weight;
				num=num+weight;
			}
		}
	}
	/**
	*Returns the mean
	*/
	public double getmean()
	{
		return sum/num;
	}
	/**
	*Returns the minimum
	*/
	public double getmin()
	{
		return min;
	}
	/**
	*Returns the maximum
	*/
	public double getmax()
	{
		return max;
	}
	/**
	*Returns the number of values
	*/
	public double getnum()
	{
		return num;
	}
	/**
	*Returns the sum of the values
	*/
	public double getsum()
	{
		return sum;
	}
	/**
	*Returns the sum of squares
	*/
	public double getssq()
	{
		return ssq;
	}
	/**
	*Returns the sample standard deviation
	*/
	public double getsamplestd()
	{
		double std=(ssq/num)-(sum/num)*(sum/num);
		return Math.sqrt(std);
	}
	/**
	*Returns the standard deviation
	*/
	public double getstd()
	{
		double std=(ssq/num)-(sum/num)*(sum/num);
		std=std*num/(num-1);
		return Math.sqrt(std);
	}
	/**
	*Returns the sample variance
	*/
	public double getsamplevar()
	{
		double var=(ssq/num)-(sum/num)*(sum/num);
		return var;
	}
	/**
	*Returns the variance
	*/
	public double getvar()
	{
		double var=(ssq/num)-(sum/num)*(sum/num);
		var=var*num/(num-1);
		return var;
	}
	/**
	*Clears the objects
	*/
	public void cleares()
	{
		min=Double.MAX_VALUE;
		max=Double.MIN_VALUE;
		num=0;
		sum=0;
		ssq=0;
	}
}
