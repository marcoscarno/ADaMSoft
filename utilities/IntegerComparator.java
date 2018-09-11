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

/**
* This class implements a comparator for two integers
* @author marco.scarno@gmail.com
* @date 07/09/2015
*/
public class IntegerComparator implements Comparator<Integer>
{
	/**
	*Return the result of two integer comparation
	*/
	public int compare(Integer ta, Integer tb)
	{
		if(tb==null)
			return -1;
		if(ta==null)
			return 1;
		int ia=ta.intValue();
		int ib=tb.intValue();
		if (ia<ib) return -1;
		else if (ia>ib) return 1;
		else return 0;
	}
	/**
	*Implements the equal method
	*/
	public boolean equals(Object obj)
	{
		return this==obj;
	}
}
