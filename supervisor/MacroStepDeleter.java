/**
* Copyright (c) 2015 ADaMSoft
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

package ADaMSoft.supervisor;

import ADaMSoft.keywords.Keywords;

/**
* Delete a single MACROSTEP
* @author marco.scarno@gmail.com
* @date 02/09/2015
*/
public class MacroStepDeleter
{
	String message;
	boolean steperror;
	/**
	*Constructor
	*/
	public MacroStepDeleter (String KeywordValue)
	{
		message="";
		steperror=false;
		String name="";
		String actualvalue=KeywordValue.trim();
		if (actualvalue.toUpperCase().startsWith(Keywords.DELMACROSTEP))
		{
			try
			{
				name=actualvalue.substring(actualvalue.indexOf(" "));
				name=name.trim();
			}
			catch (Exception e)
			{
				message=Keywords.Language.getMessage(2200)+"<br>\n";
				steperror=true;
				return;
			}
		}
		if (name.equals(""))
		{
			message=Keywords.Language.getMessage(2200)+"<br>\n";
			steperror=true;
			return;
		}
		if (name.equalsIgnoreCase("ALL"))
		{
			Keywords.project.clearMacroStep();
			message=Keywords.Language.getMessage(2201)+"<br>\n";
			return;
		}
		else
		{
			boolean res=Keywords.project.delMacroStep(name.toLowerCase());
			if (!res)
			{
				message=Keywords.Language.getMessage(2202)+" ("+name+")<br>\n";
				steperror=true;
				return;
			}
			else
			{
				message=Keywords.Language.getMessage(2203)+" ("+name+")<br>\n";
			}
		}
	}
	/**
	*Returns the result
	*/
	public String getMessage()
	{
		return message;
	}
	public boolean getError()
	{
		return steperror;
	}
}
