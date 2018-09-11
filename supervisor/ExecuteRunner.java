/**
* Copyright (c) 2017 MS
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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.text.MessageFormat;
import java.util.Date;

import ADaMSoft.keywords.Keywords;
import ADaMSoft.utilities.MessagesReplacer;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.InputStreamReader;

/**
* Execute a script
* @author marco.scarno@gmail.com
* @date 05/06/2017
*/
public class ExecuteRunner
{
	File filelog;
	boolean steperror;
	String actualstatement, actualfileref;
	public ExecuteRunner(int type, String actualstatement)
	{
		filelog = new File(System.getProperty("out_logfile"));
		steperror=false;
		this.actualstatement=actualstatement;
		if (type==0)
		{
			this.actualfileref=actualstatement;
			getResultFromFile();
			if (!steperror) getresult();
		}
		if (type==1)
		{
			this.actualfileref=actualstatement;
			getResultFromScript();
			if (!steperror) getresult();
		}
		if (type==2)
		{
			getresult();
		}
	}
	public boolean getError()
	{
		return steperror;
	}
	private void getResultFromScript()
	{
		steperror=false;
		Keywords.currentExecutedStep="Parsing script";
		int positionexecute=(actualfileref.toUpperCase()).indexOf(Keywords.EXECUTE);
		actualstatement=actualfileref.substring(positionexecute+Keywords.EXECUTE.length());
		actualstatement=actualstatement.trim();
		int level = actualstatement.indexOf(".");
		String filename;
		if(level>0)
		{
			String path = actualstatement.substring(0,actualstatement.indexOf("."));
			filename=Keywords.project.getPath(path);
			if (filename.equals(""))
			{
				addtolog("<font color=red>"+Keywords.Language.getMessage(1)+" ("+path+")</font><br>\n");
				Keywords.currentExecutedStep="";
				steperror=true;
				return;
			}
			filename=filename+actualstatement.substring(actualstatement.indexOf(".")+1);
		}
		else
			filename=System.getProperty(Keywords.WorkDir)+actualstatement;
		if (!filename.endsWith(Keywords.ScriptExtension))
			filename=filename+Keywords.ScriptExtension;
		try
		{
			java.net.URL fileUrl;
			if((filename.toLowerCase()).startsWith("http://"))
				fileUrl =  new java.net.URL(filename);
			else
			{
				File file=new File(filename);
				fileUrl = file.toURI().toURL();
			}
			BufferedReader in = new BufferedReader(new InputStreamReader(fileUrl.openStream()));
	        String str;
	        this.actualstatement="";
	        while ((str = in.readLine()) != null)
	        {
				if (!str.endsWith(";")) str=str+" ";
				this.actualstatement=this.actualstatement+str;
	        }
	        in.close();
		}
		catch (Exception ex)
		{
			addtolog("<font color=red>"+Keywords.Language.getMessage(13)+" ("+filename+")<br>\n"+ex.toString()+"</font><br>\n");
			Keywords.currentExecutedStep="";
			steperror=true;
		}
	}
	private void getResultFromFile()
	{
		steperror=false;
		Keywords.currentExecutedStep="Parsing script file";
		if (!actualfileref.toLowerCase().startsWith("http://"))
		{
			boolean exists = (new File(actualfileref)).exists();
			if (!exists)
			{
				addtolog("<font color=red>"+Keywords.Language.getMessage(2)+" ("+actualfileref+")</font><br>\n");
				Keywords.currentExecutedStep="";
				steperror=true;
				return;
			}
		}
		try
		{
			java.net.URL fileUrl;
			if((actualfileref.toLowerCase()).startsWith("http://"))
				fileUrl =  new java.net.URL(actualfileref);
			else
			{
				File file=new File(actualfileref);
				fileUrl = file.toURI().toURL();
			}
			BufferedReader in = new BufferedReader(new InputStreamReader(fileUrl.openStream()));
	        String str;
	        this.actualstatement="";
	        while ((str = in.readLine()) != null)
	        {
				if (!str.endsWith(";")) str=str+" ";
				this.actualstatement=this.actualstatement+str;
	        }
	        in.close();
		}
		catch (Exception ex)
		{
			addtolog("<font color=red>"+Keywords.Language.getMessage(13)+" ("+actualfileref+")<br>\n"+ex.toString()+"</font><br>\n");
			Keywords.currentExecutedStep="";
			steperror=true;
		}
	}
	private void getresult()
	{
		boolean currentwritelog=false;
		if ((System.getProperty("writelog")).equals("yes"))
			currentwritelog=true;
		Keywords.currentExecutedStep="Parsing script";
		actualstatement=actualstatement.trim();
		try
		{
			Object[] paramsdate = new Object[]{new Date(), new Date(0)};
			if (currentwritelog) addtolog("<i>"+MessageFormat.format(Keywords.Language.getMessage(4)+" {0}", paramsdate)+"</i><br>\n");
			if (currentwritelog) addtolog("<i>"+Keywords.Language.getMessage(5)+"<br>\n");
			ScriptVerifier sv=new ScriptVerifier(actualstatement);
			String msg=sv.getMessage();
			if (sv.getError())
			{
				addtolog(msg);
				steperror=true;
				addtolog("</i>");
				return;
			}
			Keywords.currentExecutedStep="Executing steps";
			if (!msg.equals("") && currentwritelog) addtolog(msg+Keywords.Language.getMessage(7)+"<br>\n");
			int totalactions=sv.getnumberOfActions();
			if (currentwritelog) addtolog(Keywords.Language.getMessage(10)+" "+totalactions+"</i><br><br>\n");
			Date dateProcedure;
			double timeProcedure;
			boolean halton = true;
			boolean steperror = false;
			if (Keywords.general_percentage_total>0)
				Keywords.general_percentage_total=Keywords.general_percentage_total+totalactions;
			else
			{
				Keywords.general_percentage_total=totalactions;
				Keywords.general_percentage_done=0;
			}
			if (totalactions==0)
			{
				Keywords.currentExecutedStep="";
				addtolog("<font color=red>"+Keywords.Language.getMessage(4018)+"</font><br><br>\n");
				steperror=true;
				return;
			}
			for (int currentproc=0; currentproc<totalactions; currentproc++)
			{
				Keywords.currentExecutedStep="Executing step: "+Keywords.general_percentage_done;
				Keywords.percentage_total=0;
				Keywords.percentage_done=0;
				dateProcedure=new Date();
				timeProcedure=dateProcedure.getTime();
				ScriptRunner sr=new ScriptRunner(sv.getstep(currentproc));
				steperror = sr.getSteperror();
				halton=false;
				if ((System.getProperty("halton")).equals("yes"))
					halton=true;
				dateProcedure=new Date();
				timeProcedure=(dateProcedure.getTime()-timeProcedure)/100;
				if (steperror && halton)
					currentproc=totalactions;
				if (Keywords.stop_script)
					currentproc=totalactions;
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
					if (currentwritelog) addtolog("<i>"+Keywords.Language.getMessage(11)+" "+String.format("%.2f",timeProcedure/10)+"</i><br><br>\n");
					//addtolog("<i>"+Keywords.Language.getMessage(11)+String.valueOf(timeProcedure/10)+"</i><br><br>\n");
				}
				Keywords.percentage_total=0;
				Keywords.percentage_done=0;
				Keywords.general_percentage_done++;
				currentwritelog=false;
				if ((System.getProperty("writelog")).equals("yes"))
					currentwritelog=true;
			}
			Keywords.currentExecutedStep="";
			paramsdate = new Object[]{new Date(), new Date(0)};
			if (currentwritelog) addtolog("<i>"+MessageFormat.format(Keywords.Language.getMessage(12)+" {0}", paramsdate)+"</i><br>\n");
		}
		catch (Exception ex)
		{
			addtolog("<font color=red>"+Keywords.Language.getMessage(13)+"<br>"+ex.toString()+"</font><br>\n");
			steperror=true;
		}
	}
	private void addtolog(String text)
	{
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
}
