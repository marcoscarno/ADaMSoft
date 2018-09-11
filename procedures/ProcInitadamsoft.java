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

package ADaMSoft.procedures;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Vector;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import ADaMSoft.keywords.Keywords;
import ADaMSoft.utilities.GetRequiredParameters;
import ADaMSoft.utilities.StepUtilities;
import ADaMSoft.utilities.CheckJavac;

/**
* This is the procedure that creates the ADaMSoft ini file and the ADaMSoft lancher
* @author marco.scarno@gmail.com
* @date 20/02/2017
*/
public class ProcInitadamsoft implements RunStep
{
	/**
	* Starts the execution of Proc Initadamsoft
	*/
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		Keywords.percentage_total=0;
		Keywords.percentage_done=0;
		String [] requiredparameters=new String[] {Keywords.WorkDir, Keywords.javamemory, Keywords.launcherlocation};
		String [] optionalparameters=new String[] {Keywords.iniproxyHost, Keywords.iniproxyPort, Keywords.iniproxyUser, Keywords.iniproxyPassword, Keywords.iniMaxDBRecords, Keywords.iniMaxDataBuffered,Keywords.iniFileBufferDim, Keywords.path_javac};
		StepUtilities steputilities=new StepUtilities();
		if (!steputilities.checkParameters(requiredparameters, optionalparameters, parameters))
		{
			return new Result(steputilities.getMessage(), false, null);
		}
		String WorkDir=(String)parameters.get(Keywords.WorkDir.toLowerCase());
		String java_memory=(String)parameters.get(Keywords.javamemory.toLowerCase());
		String launcher_location=(String)parameters.get(Keywords.launcherlocation.toLowerCase());
		String proxyHost=(String)parameters.get(Keywords.iniproxyHost.toLowerCase());
		String proxyPort=(String)parameters.get(Keywords.iniproxyPort.toLowerCase());
		String proxyUser=(String)parameters.get(Keywords.iniproxyUser.toLowerCase());
		String proxyPassword=(String)parameters.get(Keywords.iniproxyPassword.toLowerCase());
		String MaxDBRecords=(String)parameters.get(Keywords.iniMaxDBRecords.toLowerCase());
		String MaxDataBuffered=(String)parameters.get(Keywords.iniMaxDataBuffered.toLowerCase());
		String FileBufferDim=(String)parameters.get(Keywords.iniFileBufferDim.toLowerCase());
		String path_javac=(String)parameters.get(Keywords.path_javac.toLowerCase());

		String inifile=System.getProperty("main_directory");

		inifile=inifile+Keywords.SoftwareName+Keywords.IniExtension;

		String osversion=System.getProperty("os.name").toString();
		boolean iswindows=false;

		if ((osversion.toUpperCase()).startsWith("WIN")) iswindows=true;

		String javaversion=System.getProperty("java.version").toString();
		String ADaMSoft_exe="";
		if (javaversion.startsWith("1.10")) ADaMSoft_exe="ADaMSoft_JRE9.jar";
		if (javaversion.startsWith("1.9")) ADaMSoft_exe="ADaMSoft_JRE9.jar";
		if (javaversion.startsWith("1.8")) ADaMSoft_exe="ADaMSoft_JRE8.jar";
		if (javaversion.startsWith("1.7")) ADaMSoft_exe="ADaMSoft_JRE7.jar";
		if (javaversion.startsWith("1.6")) ADaMSoft_exe="ADaMSoft_JRE6.jar";

		if (javaversion.startsWith("10")) ADaMSoft_exe="ADaMSoft_JRE9.jar";
		if (javaversion.startsWith("9")) ADaMSoft_exe="ADaMSoft_JRE9.jar";
		if (javaversion.startsWith("8")) ADaMSoft_exe="ADaMSoft_JRE8.jar";
		if (javaversion.startsWith("7")) ADaMSoft_exe="ADaMSoft_JRE7.jar";
		if (javaversion.startsWith("6")) ADaMSoft_exe="ADaMSoft_JRE6.jar";

		if (ADaMSoft_exe.equals("ADaMSoft_JRE9") && path_javac==null)
		{
			return new Result("%4233%<br>\n", false, null);
		}
		if (ADaMSoft_exe.equals("ADaMSoft_JRE9") && path_javac!=null)
		{
			boolean javac_ok=(new CheckJavac()).simple_check_executable("");
			if (!javac_ok)
				return new Result("%4234%<br>\n", false, null);
		}
		try
		{
			 BufferedWriter writerini = new BufferedWriter(new FileWriter(new File(inifile)));
			 writerini.write("#ADaMSoft init file release 4");
			 writerini.newLine();
			 writerini.write("#DO NOT REMOVE THE ABOVE LINE");
			 writerini.newLine();
			try
			{
				WorkDir=WorkDir.replaceAll("\\\\","/");
			}
			catch (Exception fs){}
			 writerini.write("WorkDir="+WorkDir);
			 writerini.newLine();
			 if (proxyHost!=null)
			 {
				 writerini.write(Keywords.proxyHost+"="+proxyHost);
				 writerini.newLine();
			 }
			 if (proxyPort!=null)
			 {
				 writerini.write(Keywords.proxyPort+"="+proxyPort);
				 writerini.newLine();
			 }
			 if (proxyUser!=null)
			 {
				 writerini.write(Keywords.proxyUser+"="+proxyUser);
				 writerini.newLine();
			 }
			 if (proxyPassword!=null)
			 {
				 writerini.write(Keywords.proxyPassword+"="+proxyPassword);
				 writerini.newLine();
			 }
			 if (MaxDBRecords!=null)
			 {
				 writerini.write(Keywords.MaxDBRecords+"="+MaxDBRecords);
				 writerini.newLine();
			 }
			 if (MaxDataBuffered!=null)
			 {
				 writerini.write(Keywords.MaxDataBuffered+"="+MaxDataBuffered);
				 writerini.newLine();
			 }
			 if (FileBufferDim!=null)
			 {
				 writerini.write(Keywords.FileBufferDim+"="+FileBufferDim);
				 writerini.newLine();
			 }
			 if (path_javac!=null)
			 {
				 writerini.write("javac_path="+path_javac);
				 writerini.newLine();
			 }
			 writerini.close();
		}
		catch (Exception e)
		{
			(new File(inifile)).delete();
			return new Result("%3987% /"+inifile+")<br>\n"+e.toString()+"<br>\n", false, null);
		}
		if (!iswindows)
		{
			try
			{
				launcher_location=launcher_location.replaceAll("\\\\","/");
			}
			catch (Exception fs){}
			try
			{
				if (!launcher_location.endsWith(System.getProperty("file.separator")))
					launcher_location=launcher_location+System.getProperty("file.separator");
				launcher_location=launcher_location+Keywords.SoftwareName+"_launcher.sh";
				String md=(String)System.getProperty("main_directory");
				BufferedWriter writerini = new BufferedWriter(new FileWriter(new File(launcher_location)));
				writerini.write("adamsoftjar="+md+ADaMSoft_exe);
				writerini.newLine();
				writerini.write("java -Xmx"+java_memory +" -classpath .:$adamsoftjar: ADaMSoft.ADaMSoft "+inifile+" $1 $2 $3 $4");
				writerini.close();
				Process p = Runtime.getRuntime().exec("chmod 755 "+launcher_location);
				p.waitFor();
			}
			catch (Exception e)
			{
				(new File(inifile)).delete();
				(new File(launcher_location)).delete();
				return new Result("%3988% ("+launcher_location+")<br>\n"+e.toString()+"<br>\n", false, null);
			}
		}
		else
		{
			try
			{
				launcher_location=launcher_location.replaceAll("\\\\","/");
			}
			catch (Exception fs){}
			try
			{
				if (!launcher_location.endsWith(System.getProperty("file.separator")))
					launcher_location=launcher_location+System.getProperty("file.separator");
				launcher_location=launcher_location+Keywords.SoftwareName+"_launcher.bat";
				String md=(String)System.getProperty("main_directory");
				BufferedWriter writerini = new BufferedWriter(new FileWriter(new File(launcher_location)));
				writerini.write("set adamsoftjar=\""+md+ADaMSoft_exe+"\"");
				writerini.newLine();
				writerini.write("java -Xmx"+java_memory +" -classpath .;%adamsoftjar%; ADaMSoft.ADaMSoft \""+inifile+"\" %1 %2 %3 %4");
				writerini.close();
			}
			catch (Exception e)
			{
				(new File(inifile)).delete();
				(new File(launcher_location)).delete();
				return new Result("%3988% /"+launcher_location+")<br>\n"+e.toString()+"<br>\n", false, null);
			}
		}
		Vector<StepResult> result = new Vector<StepResult>();
		result.add(new LocalMessageGetter("%4000%: "+inifile+"<br>\n"));
		result.add(new LocalMessageGetter("%4001%: "+launcher_location+"<br>\n"));
		result.add(new LocalMessageGetter("%4002%<br>\n"));
		result.add(new LocalMessageGetter("%4003%<br>\n"));
		result.add(new LocalMessageGetter("%4005%<br>\n"));
		result.add(new LocalMessageGetter("%4016%<br>\n"));
		return new Result("", true, result);
	}
	/**
	*Returns the parameters for the procedure
	*/
	public LinkedList<GetRequiredParameters> getparameters()
	{
		LinkedList<GetRequiredParameters> parameters=new LinkedList<GetRequiredParameters>();
		String[] dep ={""};
		parameters.add(new GetRequiredParameters(Keywords.WorkDir, "Dir", true, 3990, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.javamemory, "text", true, 3991, dep, "", 2));
		parameters.add(new GetRequiredParameters("", "note", false, 4004, dep, "", 2));
		parameters.add(new GetRequiredParameters("", "note", false, 4015, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.launcherlocation, "Dir", true, 3992, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.path_javac, "Dir", false, 4232, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.iniproxyHost, "text", false, 3993, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.iniproxyPort, "text", false, 3994, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.iniproxyUser, "text", false, 3995, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.iniproxyPassword, "text", false, 3996, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.iniMaxDBRecords, "text", false, 3997, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.iniMaxDataBuffered, "text", false, 3998, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.iniFileBufferDim, "text", false, 3999, dep, "", 2));
		return parameters;
	}
	/**
	*Returns the group and the name of the procedure
	*/
	public String[] getstepinfo()
	{
		String[] retprocinfo=new String[2];
		retprocinfo[0]="4173";
		retprocinfo[1]="3989";
		return retprocinfo;
	}
}
