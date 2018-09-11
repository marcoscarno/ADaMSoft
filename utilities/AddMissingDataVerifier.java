/**
* Copyright © 2015 MS
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
* This class verifies if the rule for the missing data are well written
* @author marco.scarno@gmail.com
* @date 02/09/2015
*/
public class AddMissingDataVerifier
{
	/**
	*Contains the result message in case of error in writing the rule
	*/
	public static String messageAddMissingDataVerifier;
	/**
	* Checks if the user wrote correctly the keyword for the rule in the missing data.<p>
	* Returns false if the user doesn't write correctly the condition.
	*/
	public static boolean getresult(String rule)
	{
		rule=rule.trim();
		if ((rule.toUpperCase()).startsWith(Keywords.IGNORECASE))
		{
			try
			{
				String testrule=rule.substring(Keywords.IGNORECASE.length());
				testrule=testrule.substring(testrule.indexOf("(")+1,testrule.indexOf(")"));
				testrule=testrule.trim();
				if (testrule.equalsIgnoreCase(""))
				{
					messageAddMissingDataVerifier=Keywords.Language.getMessage(273)+"<br>\n";
					return false;
				}
			}
			catch (Exception e)
			{
				messageAddMissingDataVerifier=Keywords.Language.getMessage(273)+"<br>\n";
				return false;
			}
		}
		if ((rule.toUpperCase()).startsWith(Keywords.STARTSWITH))
		{
			try
			{
				String testrule=rule.substring(Keywords.STARTSWITH.length());
				testrule=testrule.substring(testrule.indexOf("(")+1,testrule.indexOf(")"));
				testrule=testrule.trim();
				if (testrule.equalsIgnoreCase(""))
				{
					messageAddMissingDataVerifier=Keywords.Language.getMessage(274)+"<br>\n";
					return false;
				}
			}
			catch (Exception e)
			{
				messageAddMissingDataVerifier=Keywords.Language.getMessage(274)+"<br>\n";
				return false;
			}
		}
		if ((rule.toUpperCase()).startsWith(Keywords.ENDSWITH))
		{
			try
			{
				String testrule=rule.substring(Keywords.ENDSWITH.length());
				testrule=testrule.substring(testrule.indexOf("(")+1,testrule.indexOf(")"));
				testrule=testrule.trim();
				if (testrule.equalsIgnoreCase(""))
				{
					messageAddMissingDataVerifier=Keywords.Language.getMessage(275)+"<br>\n";
					return false;
				}
			}
			catch (Exception e)
			{
				messageAddMissingDataVerifier=Keywords.Language.getMessage(275)+"<br>\n";
				return false;
			}
		}
		if ((rule.startsWith("(")) || (rule.startsWith("[")))
		{
			if ((rule.endsWith(")")) || (rule.endsWith("]")))
			{
				try
				{
					rule=rule.replaceAll("\\(","");
					rule=rule.replaceAll("\\[","");
					rule=rule.replaceAll("\\)","");
					rule=rule.replaceAll("\\]","");
					String [] rulelimit=rule.split(":");
					if (rulelimit.length!=2)
					{
						messageAddMissingDataVerifier=Keywords.Language.getMessage(276)+"<br>\n";
						return false;
					}
				}
				catch (Exception e)
				{
					messageAddMissingDataVerifier=Keywords.Language.getMessage(276)+"<br>\n";
					return false;
				}
			}
		}
		return true;
	}
}
