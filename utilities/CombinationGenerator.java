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

import java.math.BigInteger;

/**
* This class generates all the combination of n elments by r
* @author marco.scarno@gmail.com
* @date 07/09/2015
*/
public class CombinationGenerator
{
	private int[] a;
	private int n;
	private int r;
	private BigInteger numLeft;
	private BigInteger total;
	/**
	*Receives the information about the n elements and the r couples
	*/
	public CombinationGenerator (int n, int r)
	{
		this.n = n;
		this.r = r;
		a = new int[r];
		BigInteger nFact = getFactorial (n);
		BigInteger rFact = getFactorial (r);
		BigInteger nminusrFact = getFactorial (n - r);
		total = nFact.divide (rFact.multiply (nminusrFact));
		reset ();
	}
	/**
	*Reset
	*/
	public void reset ()
	{
		for (int i = 0; i < a.length; i++)
		{
			a[i] = i;
		}
		numLeft = new BigInteger (total.toString ());
	}
	/**
	*Return the amount of number to be generated
	*/
	public BigInteger getNumLeft ()
	{
		return numLeft;
	}
	/**
	*Return true if there are more number to generate
	*/
	public boolean hasMore ()
	{
		return numLeft.compareTo (BigInteger.ZERO) == 1;
	}
	/**
	*Return the total number of combination to be generated
	*/
	public BigInteger getTotal ()
	{
		return total;
	}
	/**
	*Return the factorial number
	*/
	private static BigInteger getFactorial (int n)
	{
		BigInteger fact = BigInteger.ONE;
		for (int i = n; i > 1; i--)
		{
			fact = fact.multiply (new BigInteger (Integer.toString (i)));
		}
		return fact;
	}
	/**
	*Return the next couple
	*/
	public int[] getNext ()
	{
		if (numLeft.equals (total))
		{
			numLeft = numLeft.subtract (BigInteger.ONE);
			return a;
		}
		int i = r - 1;
		while (a[i] == n - r + i)
		{
			i--;
		}
		a[i] = a[i] + 1;
		for (int j = i + 1; j < r; j++)
		{
			a[j] = a[i] + j - i;
		}
		numLeft = numLeft.subtract (BigInteger.ONE);
		return a;
	}
}
