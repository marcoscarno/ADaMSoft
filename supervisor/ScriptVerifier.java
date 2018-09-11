/**
* Copyright (c) ADaMSoft
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
* Checks the Command script and returns, also, the number of executable statements
* @author marco.scarno@gmail.com
* @date 02/09/2015
*/
public class ScriptVerifier
{
	Vector<Vector<String>> steps;
	String message;
	int numberOfActions;
	boolean verifierError;
	public ScriptVerifier(String CurrentScript)
	{
		verifierError=false;
		message="";
		numberOfActions=0;
		Keywords.currentExecutedStep="Interpreting the script";
		CurrentScript = CurrentScript.replaceAll("\\n", " ");
		CurrentScript = CurrentScript.replaceAll("\\r", " ");
		steps=new Vector<Vector<String>>();
		Vector<String> step=new Vector<String>();
		try
		{
			CurrentScript=CurrentScript.replaceAll("%;",Keywords.SeMiCoLoN);
		}
		catch (Exception e) {}
		int prescom=CurrentScript.indexOf("/*");
		try
		{
			while (prescom>=0)
			{
				int posend=CurrentScript.indexOf("*/");
				if (posend<0)
				{
					message="<font color=red>"+Keywords.Language.getMessage(2110)+"</font><br>\n";
					verifierError=true;
					Keywords.currentExecutedStep="";
					return;
				}
				CurrentScript=CurrentScript.substring(0, prescom)+CurrentScript.substring(posend+2);
				prescom=CurrentScript.indexOf("/*");
			}
		}
		catch (Exception e)
		{
			message="<font color=red>"+Keywords.Language.getMessage(2110)+"</font><br>\n";
			verifierError=true;
			Keywords.currentExecutedStep="";
			return;
		}
		String[] scriptparts=CurrentScript.split(";");
		String startkeyword="";
		String oldkeyword="";
		for (int s=0; s<scriptparts.length; s++)
		{
			startkeyword="";
			scriptparts[s]=scriptparts[s].trim();
			if (!scriptparts[s].equals(""))
				step.add(scriptparts[s]);
			String [] actualKeyword=scriptparts[s].split(" ");
			if (actualKeyword[0].equalsIgnoreCase(Keywords.MACROSTEP))
			{
				numberOfActions++;
				message=message+scriptparts[s]+";<br>\n";
				Vector<String> tempv=new Vector<String>();
				int starttouse=s;
				boolean foundmend=false;
				for (int m=starttouse; m<scriptparts.length; m++)
				{
					s=m;
					scriptparts[m]=scriptparts[m].trim();
					if (!scriptparts[m].equals(""))
					{
						if (scriptparts[m].toUpperCase().startsWith(Keywords.MEND))
						{
							foundmend=true;
							break;
						}
						else
							tempv.add(scriptparts[m]);
					}
				}
				if (!foundmend)
				{
					message=message+"<font color=red>"+Keywords.Language.getMessage(2193)+"</font><br><br>\n";
					verifierError=true;
					Keywords.currentExecutedStep="";
					return;
				}
				steps.add(tempv);
				step.clear();
			}
			else if (actualKeyword[0].equalsIgnoreCase(Keywords.JAVACODE))
			{
				numberOfActions++;
				message=message+scriptparts[s]+";<br>\n";
				Vector<String> tempv=new Vector<String>();
				int starttouse=s;
				boolean foundrun=false;
				for (int m=starttouse; m<scriptparts.length; m++)
				{
					s=m;
					scriptparts[m]=scriptparts[m].trim();
					if (!scriptparts[m].equals(""))
					{
						if (scriptparts[m].toUpperCase().startsWith(Keywords.RUN))
						{
							foundrun=true;
							break;
						}
						else
							tempv.add(scriptparts[m]+";");
					}
				}
				if (!foundrun)
				{
					message=message+"<font color=red>"+Keywords.Language.getMessage(2321)+"</font><br><br>\n";
					verifierError=true;
					Keywords.currentExecutedStep="";
					return;
				}
				steps.add(tempv);
				step.clear();
			}
			else
			{
				for (int i=0; i<Keywords.KeywordsWithEnd.length; i++)
				{
					if (actualKeyword[0].equalsIgnoreCase(Keywords.KeywordsWithEnd[i]))
					{
						numberOfActions++;
						message=message+scriptparts[s]+";<br>\n";
						startkeyword=actualKeyword[0];
					}
				}
				for (int i=0; i<Keywords.KeywordsWithRun.length; i++)
				{
					if (actualKeyword[0].equalsIgnoreCase(Keywords.KeywordsWithRun[i]))
					{
						numberOfActions++;
						message=message+scriptparts[s]+";<br>\n";
						startkeyword=actualKeyword[0];
					}
				}
				for (int i=0; i<Keywords.SimpleKeywords.length; i++)
				{
					if (actualKeyword[0].equalsIgnoreCase(Keywords.SimpleKeywords[i]))
					{
						numberOfActions++;
						message=message+scriptparts[s]+";<br>\n";
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
					message=message+"<font color=red>"+Keywords.Language.getMessage(8)+" "+oldkeyword.toUpperCase()+"</font><br><br>\n";
					verifierError=true;
					Keywords.currentExecutedStep="";
					return;
				}
				for (int i=0; i<Keywords.KeywordsWithEnd.length; i++)
				{
					if ((oldkeyword.equalsIgnoreCase(Keywords.KeywordsWithEnd[i])) && (actualKeyword[0].equalsIgnoreCase(Keywords.END)))
					{
						message=message+scriptparts[s]+";<br>\n";
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
						message=message+scriptparts[s]+";<br>\n";
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
		}
		if (!oldkeyword.equals(""))
		{
			message=message+"<font color=red>"+Keywords.Language.getMessage(8)+oldkeyword.toUpperCase()+"</font><br><br>\n";
			verifierError=true;
		}
		Keywords.currentExecutedStep="";
	}
	public boolean getError()
	{
		return verifierError;
	}
	/**
	*Returns the number of actions
	*/
	public int getnumberOfActions()
	{
		return numberOfActions;
	}
	/**
	*Returns the result for the script validation
	*/
	public String getMessage()
	{
		return message;
	}
	/**
	*Returns the i-th step code
	*/
	public Vector<String> getstep(int i)
	{
		return steps.get(i);
	}
}
