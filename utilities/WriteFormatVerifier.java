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

import ADaMSoft.keywords.Keywords;

/**
* This class verifies if the write formats are well written
* @author marco.scarno@gmail.com
* @date 07/09/2015
*/
public class WriteFormatVerifier
{
	/**
	*Checks if the write formats are well written<p>
	*Possibilities:<p>
	*NUM (generic number)<p>
	*NUMDATETIME (date and time)<p>
	*NUMDATE (date )<p>
	*NUMTIME (time)<p>
	*NUMI (integer part)<p>
	*NUMD# (number with # digits of decimals)<p>
	*NUME (Exponential representation of the number)<p>
	*TEXT (a generic text)<p>
	*TEXT# (a text with # characters)<p>
	*Such method returns 0 if the check went OK, otherwise 5
	*/
	public static boolean getresult(String formatname)
	{
		if ((formatname.length()==3)&& (formatname.toUpperCase().startsWith(Keywords.NUMSuffix.toUpperCase())))
			return true;
		if ((formatname.length()>3)&& (formatname.toUpperCase().startsWith(Keywords.NUMSuffix.toUpperCase())))
		{
			String num=formatname.substring(Keywords.NUMSuffix.length());
			if ((num.toUpperCase()).startsWith(Keywords.DTSuffix))
				return true;
			if ((num.toUpperCase()).startsWith(Keywords.TIMESuffix))
				return true;
			if ((num.toUpperCase()).startsWith(Keywords.DATESuffix))
				return true;
			if ((num.toUpperCase()).startsWith(Keywords.INTSuffix))
				return true;
			if ((num.toUpperCase()).startsWith(Keywords.DECSuffix))
				return true;
			if ((num.toUpperCase()).startsWith(Keywords.EXPSuffix))
				return true;
		}
		if ((formatname.length()==4)&& (formatname.toUpperCase().startsWith(Keywords.TEXTSuffix.toUpperCase())))
			return true;
		if ((formatname.length()>4)&& (formatname.toUpperCase().startsWith(Keywords.TEXTSuffix.toUpperCase())))
		{
			return true;
		}
		return false;
	}
}
