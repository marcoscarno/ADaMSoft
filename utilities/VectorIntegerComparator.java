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

import java.util.Comparator;
import java.util.Vector;

/**
* This class implements a comparator for a Vector of Integer
* @author marco.scarno@gmail.com
* @date 07/09/2015
*/
public class VectorIntegerComparator implements Comparator<Vector<Integer>>
{
	@SuppressWarnings("unused")
	public int compare(Vector<Integer> ta, Vector<Integer> tb)
	{
		if (ta==null && tb==null)
			return 0;
		if (ta==null && tb!=null)
			return 1;
		if (tb==null && ta!=null)
			return -1;
		int sizea=ta.size();
		int sizeb=tb.size();
		if (sizeb<sizea)
			sizea=sizeb;
		for (int i=0; i<sizea; i++)
		{
			int a=(ta.get(i)).intValue();
			int b=(tb.get(i)).intValue();
			if (a>b) return 1;
			if (a<b) return -1;
			else return -1;
		}
		return 0;
	}
	public boolean equals(Object obj)
	{
		return this==obj;
	}
}
