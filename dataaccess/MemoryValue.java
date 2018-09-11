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

package ADaMSoft.dataaccess;


/**
* Contains a generic value for the memory dataset
* @author marco.scarno@gmail.com
* @date 03/09/2015
*/
public class MemoryValue
{
	double valNum;
	String stringValue;
	/**
	*Store in memory a double
	*/
	public MemoryValue(double valNum)
	{
		this.valNum = valNum;
	}
	/**
	*Store in memory a string
	*/
	public MemoryValue(String stringValue)
	{
		this.stringValue = stringValue;
	}
	/**
	*Retrieve the value
	*/
	public String toString()
	{
		if(stringValue!=null)
		{
			return stringValue;
		}
		else
		{
			return String.valueOf(valNum);
		}
	}
}
