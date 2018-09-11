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

package ADaMSoft.dataaccess;

import java.math.BigDecimal;

/**
* This method returns an integer relative to the comparison of two Objects that contains values
* @author marco.scarno@gmail.com
* @date 13/02/2017
*/
public class CompareObjects
{
	int numberofvar;
	boolean order;
	boolean noconversion;
	/**
	*Initializes the method
	*/
	public CompareObjects (int numberofvar, boolean order)
	{
		this.numberofvar=numberofvar;
		this.order=order;
		noconversion=false;
	}
	public void setconversion(boolean noconversion)
	{
		this.noconversion=noconversion;
	}
	/**
	* Returns an integer that is the result of the comparison, according to the order, of the type of the values<p>
	*/
	public int getComparison(Object[] a, Object[] b)
	{
		int compareresult=0;
		for (int k=0; k<numberofvar; k++)
		{
			String avalues=a[k].toString();
			String bvalues=b[k].toString();
			compareresult=0;
			if (((avalues.trim()).equals("")) && (!(bvalues.trim()).equals("")))
				return -1;
			else if ((!(avalues.trim()).equals("")) && ((bvalues.trim()).equals("")))
				return 1;
			else
			{
				double anum=Double.NaN;
				double bnum=Double.NaN;
				if (!noconversion)
				{
					try
					{
						anum=Double.parseDouble(avalues);
					}
					catch (Exception nonnumber) {}
					try
					{
						bnum=Double.parseDouble(bvalues);
					}
					catch (Exception nonnumber) {}
				}
				if ((!Double.isNaN(anum)) && (!Double.isNaN(bnum)))
				{
					if ((anum>99999999999999.0) && (bnum>99999999999999.0))
					{
						BigDecimal abd=new BigDecimal(avalues);
						BigDecimal bbd=new BigDecimal(bvalues);
						if (abd.compareTo(bbd)>0)
							compareresult=1;
						else if (abd.compareTo(bbd)<0)
							compareresult=-1;
					}
					else
					{
						if (anum<bnum)
							compareresult=-1;
						else if (anum>bnum)
							compareresult=1;
					}
				}
				else
				{
					compareresult=avalues.compareTo(bvalues);
					if (compareresult<=-1)
						compareresult=-1;
					else if (compareresult>=1)
						compareresult=1;
				}
				if (!order)
					compareresult=-1*compareresult;
				if (compareresult!=0)
					return compareresult;
			}
		}
		return compareresult;
	}
}
