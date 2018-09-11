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

/**
* This class contains methods to transform Objects
* @author marco.scarno@gmail.com
* @date 07/09/2015
*/

public class ObjectTransformer
{
	/**
	*Return a String from a double (the string is empty is the double is NaN or Infinity)
	*/
	public String double2String(double value)
	{
		if (Double.isNaN(value))
			return "";
		else if (Double.isInfinite(value))
			return "";
		else
		{
			try
			{
				return String.valueOf(value);
			}
			catch (Exception e)
			{
				return "";
			}
		}
	}
	/**
	*Return an int from a String (the int is 0 if the string is not an int)
	*/
	public int string2int(String value)
	{
		int ret=0;
		try
		{
			ret=Integer.parseInt(value);
		}
		catch (Exception en) {}
		return ret;
	}
	/**
	*Return a double from a String (the double is 0 if the string is not a double)
	*/
	public double string2double(String value)
	{
		if (value.trim().equals(""))
			return Double.NaN;
		try
		{
			return Double.parseDouble(value);
		}
		catch (Exception en) 
		{
			return Double.NaN;
		}
	}
}
