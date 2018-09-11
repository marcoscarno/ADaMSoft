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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.MessageFormat;
import java.util.Date;
import java.util.Vector;

import ADaMSoft.keywords.Keywords;
import ADaMSoft.utilities.MessagesReplacer;

/**
* Executes all the steps defined inside a MACROSTEP
* @author marco.scarno@gmail.com
* @date 02/09/2015
*/
public class MacroStepExecutor
{
	String message;
	String kvfile;
	boolean steperror;
	public MacroStepExecutor (String KeywordValue)
	{
		message="";
		steperror=false;
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
				message=Keywords.Language.getMessage(2205)+"<br>\n";
				steperror=true;
				return;
			}
		}
		if (name.equals(""))
		{
			message=Keywords.Language.getMessage(2205)+"<br>\n";
			steperror=true;
			return;
		}
		Vector<String> allsteps=Keywords.project.getMacroStep(name);
		if (allsteps==null)
		{
			message=Keywords.Language.getMessage(2206)+" ("+name+")<br>\n";
			steperror=true;
			return;
		}
		if (allsteps.size()==0)
		{
			message=Keywords.Language.getMessage(2206)+" ("+name+")<br>\n";
			steperror=true;
			return;
		}
		int numberOfActions=0;
		Vector<Vector<String>> steps=new Vector<Vector<String>>();
		Vector<String> step=new Vector<String>();
		String startkeyword="";
		String oldkeyword="";
		addtolog("<i>"+Keywords.Language.getMessage(2207)+" ("+name+")<br>\n");
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
					addtolog(scriptparts+";<br>\n");
					startkeyword=actualKeyword[0];
				}
			}
			for (int i=0; i<Keywords.KeywordsWithRun.length; i++)
			{
				if (actualKeyword[0].equalsIgnoreCase(Keywords.KeywordsWithRun[i]))
				{
					numberOfActions++;
					addtolog(scriptparts+";<br>\n");
					startkeyword=actualKeyword[0];
				}
			}
			for (int i=0; i<Keywords.SimpleKeywords.length; i++)
			{
				if (actualKeyword[0].equalsIgnoreCase(Keywords.SimpleKeywords[i]))
				{
					numberOfActions++;
					addtolog(scriptparts+";<br>\n");
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
				addtolog("</i>\n");
				message=Keywords.Language.getMessage(2208)+" "+oldkeyword.toUpperCase()+"</i><br><br>\n";
				steperror=true;
				return;
			}
			for (int i=0; i<Keywords.KeywordsWithEnd.length; i++)
			{
				if ((oldkeyword.equalsIgnoreCase(Keywords.KeywordsWithEnd[i])) && (actualKeyword[0].equalsIgnoreCase(Keywords.END)))
				{
					addtolog(scriptparts+";<br>\n");
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
					addtolog(scriptparts+";<br>\n");
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
			addtolog("</i>\n");
			message=Keywords.Language.getMessage(2208)+oldkeyword.toUpperCase()+"</i><br><br>\n";
			steperror=true;
			return;
		}
		addtolog(Keywords.Language.getMessage(2209)+String.valueOf(numberOfActions)+"</i><br><br>\n");
		try
		{
			Object[] paramsdate = new Object[]{new Date(), new Date(0)};
			Date dateProcedure;
			double timeProcedure;
			boolean halton = true;
			boolean writelog = true;
			boolean steperror = false;
			Keywords.general_percentage_total=Keywords.general_percentage_total+numberOfActions+1;
			for (int currentproc=0; currentproc<numberOfActions; currentproc++)
			{
				Keywords.currentExecutedStep="Executing step: "+Keywords.general_percentage_done;
				Keywords.percentage_total=0;
				Keywords.percentage_done=0;
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
						addtolog("<b>"+currentresult+"</b><br><br>\n");
					}
					else
					{
						currentresult=MessagesReplacer.replaceMessages(currentresult);
						addtolog(currentresult+"\n");
						addtolog("<i>"+Keywords.Language.getMessage(11)+" "+String.format("%.2f", timeProcedure)+"</i><br><br>\n");
					}
				}
				Keywords.percentage_total=0;
				Keywords.percentage_done=0;
				Keywords.general_percentage_done++;
			}
			paramsdate = new Object[]{new Date(), new Date(0)};
			addtolog("<i>"+MessageFormat.format(Keywords.Language.getMessage(12)+" {0}", paramsdate)+"</i><br>\n");
		}
		catch (Exception ex)
		{
			addtolog("<font color=red>"+Keywords.Language.getMessage(2210)+"<br>"+ex.toString()+"</font><br>\n");
			steperror=true;
		}
	}
	private void addtolog(String text)
	{
		File filelog = new File(System.getProperty("out_logfile"));
		try
		{
			Keywords.semwritelog.acquire();
	        BufferedWriter logwriter = new BufferedWriter(new FileWriter(filelog,true));
	        logwriter.write(text);
	        logwriter.close();
			Keywords.semwritelog.release();
		}
		catch (Exception e){}
	}
	public String getMessage()
	{
		return message;
	}
	public boolean getError()
	{
		return steperror;
	}
}
