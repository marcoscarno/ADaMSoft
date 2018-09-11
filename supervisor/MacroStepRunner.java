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

import java.util.Vector;

import ADaMSoft.keywords.Keywords;

/**
* Executes the MACROSTEP action
* @author marco.scarno@gmail.com
* @date 02/09/2015
*/
public class MacroStepRunner
{
	String message;
	boolean steperror;
	public MacroStepRunner (Vector<String> KeywordValue)
	{
		message="";
		steperror=false;
		String name="";
		Vector<String> stepsinfo=new Vector<String>();
		for (int i=0; i<KeywordValue.size(); i++)
		{
			String actualvalue=KeywordValue.get(i);
			actualvalue=actualvalue.trim();
			if (actualvalue.toUpperCase().startsWith(Keywords.MACROSTEP))
			{
				try
				{
					name=actualvalue.substring(actualvalue.indexOf(" "));
					name=name.trim();
				}
				catch (Exception e)
				{
					message=Keywords.Language.getMessage(2194)+"<br>\n";
					steperror=true;
					return;
				}
			}
			else
				stepsinfo.add(actualvalue);
		}
		if (name.toUpperCase().equals("ALL"))
		{
			message=Keywords.Language.getMessage(2204)+"<br>\n";
			steperror=true;
			return;
		}
		message=Keywords.Language.getMessage(2196)+" ("+name+")<br>\n";
		String startkeyword="";
		String scriptparts="";
		String oldkeyword="";
		for (int s=0; s<stepsinfo.size(); s++)
		{
			startkeyword="";
			scriptparts=stepsinfo.get(s).trim();
			String [] actualKeyword=scriptparts.split(" ");
			for (int i=0; i<Keywords.KeywordsWithEnd.length; i++)
			{
				if (actualKeyword[0].equalsIgnoreCase(Keywords.KeywordsWithEnd[i]))
				{
					startkeyword=actualKeyword[0];
				}
			}
			for (int i=0; i<Keywords.KeywordsWithRun.length; i++)
			{
				if (actualKeyword[0].equalsIgnoreCase(Keywords.KeywordsWithRun[i]))
				{
					startkeyword=actualKeyword[0];
				}
			}
			for (int i=0; i<Keywords.SimpleKeywords.length; i++)
			{
				if (actualKeyword[0].equalsIgnoreCase(Keywords.SimpleKeywords[i]))
				{
					startkeyword=actualKeyword[0];
				}
			}
			if ((!startkeyword.equals("")) && (!oldkeyword.equals("")))
			{
				message=message+Keywords.Language.getMessage(2197)+"<br>\n"+Keywords.Language.getMessage(8)+" "+oldkeyword.toUpperCase()+"<br>\n"+Keywords.Language.getMessage(2198)+"<br>\n";
				steperror=true;
				return;
			}
			for (int i=0; i<Keywords.KeywordsWithEnd.length; i++)
			{
				if ((oldkeyword.equalsIgnoreCase(Keywords.KeywordsWithEnd[i])) && (actualKeyword[0].equalsIgnoreCase(Keywords.END)))
				{
					oldkeyword="";
				}
			}
			for (int i=0; i<Keywords.KeywordsWithRun.length; i++)
			{
				if ((oldkeyword.equalsIgnoreCase(Keywords.KeywordsWithRun[i])) && (actualKeyword[0].equalsIgnoreCase(Keywords.RUN)))
				{
					oldkeyword="";
				}
			}
			if (!startkeyword.equals(""))
				oldkeyword=startkeyword;
			for (int i=0; i<Keywords.SimpleKeywords.length; i++)
			{
				if (oldkeyword.equalsIgnoreCase(Keywords.SimpleKeywords[i]))
					oldkeyword="";
			}
		}
		if (!oldkeyword.equals(""))
		{
			message=message+Keywords.Language.getMessage(2197)+"<br>\n"+Keywords.Language.getMessage(8)+" "+oldkeyword.toUpperCase()+"<br>\n"+Keywords.Language.getMessage(2198)+"<br>\n";
			steperror=true;
			return;
		}
		Keywords.project.addMacroStep(name.toLowerCase(), stepsinfo);
		message=message+Keywords.Language.getMessage(2195)+" ("+name+")<br>\n";
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
