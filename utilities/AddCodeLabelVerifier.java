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
* This class verifies if the code for the codelabel are well written
* @author marco.scarno@gmail.com
* @date 02/09/2015
*/
public class AddCodeLabelVerifier
{
	
	String message;
	public AddCodeLabelVerifier(String code)
	{
		message="";
		code.trim();
		if ((code.toUpperCase()).startsWith(Keywords.IGNORECASE))
		{
			try
			{
				String testcode=code.substring(Keywords.IGNORECASE.length());
				testcode=testcode.substring(testcode.indexOf("(")+1,testcode.indexOf(")"));
				testcode=testcode.trim();
				if (testcode.equalsIgnoreCase(""))
				{
					message=Keywords.Language.getMessage(50)+"<br>\n";
				}
			}
			catch (Exception e)
			{
				message=Keywords.Language.getMessage(50)+"<br>\n";
			}
		}
		if ((code.toUpperCase()).startsWith(Keywords.STARTSWITH))
		{
			try
			{
				String testcode=code.substring(Keywords.STARTSWITH.length());
				testcode=testcode.substring(testcode.indexOf("(")+1,testcode.indexOf(")"));
				testcode=testcode.trim();
				if (testcode.equalsIgnoreCase(""))
				{
					message=Keywords.Language.getMessage(51)+"<br>\n";
				}
			}
			catch (Exception e)
			{
				message=Keywords.Language.getMessage(51)+"<br>\n";
			}
		}
		if ((code.toUpperCase()).startsWith(Keywords.ENDSWITH))
		{
			try
			{
				String testcode=code.substring(Keywords.ENDSWITH.length());
				testcode=testcode.substring(testcode.indexOf("(")+1,testcode.indexOf(")"));
				testcode=testcode.trim();
				if (testcode.equalsIgnoreCase(""))
				{
					message=Keywords.Language.getMessage(52)+"<br>\n";
				}
			}
			catch (Exception e)
			{
				message=Keywords.Language.getMessage(52)+"<br>\n";
			}
		}
		if ((code.startsWith("(")) || (code.startsWith("[")))
		{
			if ((code.endsWith(")")) || (code.endsWith("]")))
			{
				try
				{
					code.replaceAll("\\(","");
					code.replaceAll("\\[","");
					code.replaceAll("\\)","");
					code.replaceAll("\\]","");
					String [] codelimit=code.split(":");
					if (codelimit.length!=2)
					{
						message=Keywords.Language.getMessage(53)+"<br>\n";
					}

				}
				catch (Exception e)
				{
					message=Keywords.Language.getMessage(53)+"<br>\n";
				}
			}
		}
	}
	public String getMessage()
	{
		return message;
	}
}
