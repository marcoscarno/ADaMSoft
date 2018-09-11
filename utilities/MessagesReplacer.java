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
* This class substitutes the message code into the valid language text messages
* @author marco.scarno@gmail.com
* @date 07/09/2015
*/
public class MessagesReplacer
{
	/**
	* Substitutes the message code into the valid language text messages.<p>
	* It searches into the textInput String the _#_, where # is a number, and substitutes it with the
	* corresponding language message.
	*/
	public static String replaceMessages(String textInput)
	{
		String[] split=textInput.split("%");
		if(split.length==0)
		{
			return textInput;
		}
		int i=1;
		int test=0;
		while(split.length>1 && i<split.length)
		{
			try
			{
				int valMessage=Integer.parseInt(split[i]);
				test=textInput.indexOf("%"+split[i]+"%");
				if (test>=0)
					textInput=textInput.replace("%"+split[i]+"%", Keywords.Language.getMessage(valMessage));
				else
					return textInput;
			}
			catch (Exception e)
			{
				i++;
			}
			split=textInput.split("%");
		}
		return textInput;
	}
}
