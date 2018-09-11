/**
* Copyright © 2015 ADaMSoft
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

import java.util.Date;
import java.util.Vector;

import ADaMSoft.keywords.Keywords;
import ADaMSoft.utilities.MessagesReplacer;

/**
* Executes all the steps defined inside a MACROSTEP
* @author marco.scarno@gmail.com
* @date 02/09/2015
*/
public class MacroStepVoidExecutor
{
	public MacroStepVoidExecutor (){}
	/**
	*Receive the statement and execute
	*/
	public void getresult(String KeywordValue)
	{
		String name="";
		String actualvalue=KeywordValue.trim();
		if (actualvalue.toUpperCase().startsWith(Keywords.EXEMACROSTEP))
		{
			try
			{
				name=actualvalue.substring(actualvalue.indexOf(" "));
				name=name.trim();
			}
			catch (Exception e)
			{
				return;
			}
		}
		if (name.equals(""))
		{
			return;
		}
		Vector<String> allsteps=Keywords.project.getMacroStep(name);
		if (allsteps==null)
		{
			return;
		}
		if (allsteps.size()==0)
		{
			return;
		}
		int numberOfActions=0;
		Vector<Vector<String>> steps=new Vector<Vector<String>>();
		Vector<String> step=new Vector<String>();
		String startkeyword="";
		String oldkeyword="";
		String scriptparts="";
		for (int s=0; s<allsteps.size(); s++)
		{
			startkeyword="";
			scriptparts=allsteps.get(s).trim();
			if (!scriptparts.equals(""))
				step.add(scriptparts);
			String [] actualKeyword=scriptparts.split(" ");
			for (int i=0; i<Keywords.KeywordsWithEnd.length; i++)
			{
				if (actualKeyword[0].equalsIgnoreCase(Keywords.KeywordsWithEnd[i]))
				{
					numberOfActions++;
					startkeyword=actualKeyword[0];
				}
			}
			for (int i=0; i<Keywords.KeywordsWithRun.length; i++)
			{
				if (actualKeyword[0].equalsIgnoreCase(Keywords.KeywordsWithRun[i]))
				{
					numberOfActions++;
					startkeyword=actualKeyword[0];
				}
			}
			for (int i=0; i<Keywords.SimpleKeywords.length; i++)
			{
				if (actualKeyword[0].equalsIgnoreCase(Keywords.SimpleKeywords[i]))
				{
					numberOfActions++;
					startkeyword=actualKeyword[0];
					Vector<String> tempv=new Vector<String>();
					for (int j=0; j<step.size(); j++)
					{
						String temps=step.get(j);
						tempv.add(temps);
					}
					steps.add(tempv);
					step.clear();
				}
			}
			if ((!startkeyword.equals("")) && (!oldkeyword.equals("")))
			{
				return;
			}
			for (int i=0; i<Keywords.KeywordsWithEnd.length; i++)
			{
				if ((oldkeyword.equalsIgnoreCase(Keywords.KeywordsWithEnd[i])) && (actualKeyword[0].equalsIgnoreCase(Keywords.END)))
				{
					oldkeyword="";
					Vector<String> tempv=new Vector<String>();
					for (int j=0; j<step.size(); j++)
					{
						String temps=step.get(j);
						tempv.add(temps);
					}
					steps.add(tempv);
					step.clear();
				}
			}
			for (int i=0; i<Keywords.KeywordsWithRun.length; i++)
			{
				if ((oldkeyword.equalsIgnoreCase(Keywords.KeywordsWithRun[i])) && (actualKeyword[0].equalsIgnoreCase(Keywords.RUN)))
				{
					oldkeyword="";
					Vector<String> tempv=new Vector<String>();
					for (int j=0; j<step.size(); j++)
					{
						String temps=step.get(j);
						tempv.add(temps);
					}
					steps.add(tempv);
					step.clear();
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
			return;
		}
		try
		{
			Date dateProcedure;
			long timeProcedure;
			boolean halton = true;
			boolean writelog = true;
			boolean steperror = false;
			for (int currentproc=0; currentproc<numberOfActions; currentproc++)
			{
				dateProcedure=new Date();
				timeProcedure=dateProcedure.getTime();
				ScriptRunner sr=new ScriptRunner(steps.get(currentproc));
				steperror = sr.getSteperror();
				halton=false;
				if ((System.getProperty("halton")).equals("yes"))
					halton=true;
				writelog=false;
				if ((System.getProperty("writelog")).equals("yes"))
					writelog=true;
				dateProcedure=new Date();
				timeProcedure=(dateProcedure.getTime()-timeProcedure)/1000;
				if (steperror && halton)
					currentproc=numberOfActions;
				if (Keywords.stop_script)
					currentproc=numberOfActions;
				if (writelog)
				{
					String currentresult=sr.getMessageexecution();
					if ((currentresult.toUpperCase()).startsWith(Keywords.MSG+" "))
					{
						currentresult=(currentresult.substring(Keywords.MSG.length())).trim();
					}
					else
					{
						currentresult=MessagesReplacer.replaceMessages(currentresult);
					}
				}
			}
			if (steperror && halton)
				return;
		}
		catch (Exception ex)
		{
			return;
		}
		return;
	}
}
