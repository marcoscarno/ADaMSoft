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

import java.util.Vector;
import java.util.Comparator;
import java.math.BigDecimal;

/**
* This class implements a comparator for a Vector of Strings
* @author marco.scarno@gmail.com
* @date 07/09/2015
*/
public class VectorStringComparatorWithMd implements Comparator<Vector<String>>
{
	public int compare(Vector<String> ta, Vector<String> tb)
	{
		if((ta==null) && (tb==null))
			return 0;
		if(ta==null)
			return 1;
		if(tb==null)
			return -1;
		int sizea=ta.size();
		int sizeb=tb.size();
		if (sizea<sizeb)
			return -1;
		else if (sizea>sizeb)
			return 1;
		for (int i=0; i<sizea; i++)
		{
			String a=ta.get(i);
			String b=tb.get(i);
			int resultcheck=compareref(a,b);
			if (resultcheck!=0)
				return resultcheck;
		}
		return 0;
	}
	public boolean equals(Object obj)
	{
		return this==obj;
	}
	private int compareref(String a, String b)
	{
		if ((a==null) && (b!=null))
			return -1;
		else if ((a!=null) && (b==null))
			return 1;
		else if ((a==null) && (b==null))
			return 0;
		else if ((a.equals("")) && (!b.equals("")))
			return 1;
		else if ((!a.equals("")) && (b.equals("")))
			return -1;
		else if ((a.equals("")) && (b.equals("")))
			return 0;
		double anum=Double.NaN;
		double bnum=Double.NaN;
		try
		{
			anum=Double.parseDouble(a);
			bnum=Double.parseDouble(b);
		}
		catch (Exception nonnumber)
		{
			anum=Double.NaN;
			bnum=Double.NaN;
		}
		if ((!Double.isNaN(anum)) && (!Double.isNaN(bnum)))
		{
			if ((anum>99999999999999.0) && (bnum>99999999999999.0))
			{
				BigDecimal abd=new BigDecimal(a);
				BigDecimal bbd=new BigDecimal(b);
				if (abd.compareTo(bbd)>0)
					return 1;
				else if (abd.compareTo(bbd)<0)
					return -1;
			}
			else
			{
				if (anum>bnum)
					return 1;
				else if(anum<bnum)
					return -1;
			}
			return 0;
		}
		else
		{
			int testc=a.compareTo(b);
			if (testc<0)
				return -1;
			else if (testc>0)
				return 1;
			return 0;
		}
	}
}
