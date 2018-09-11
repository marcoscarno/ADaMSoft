/**
* Copyright (c) 2018 MS
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
import java.lang.Runtime;
import java.lang.Process;
import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
* This class is used to check the existence of the Java compiler (javac)<p>
* @author marco.scarno@gmail.com
* @date 11/06/18
*/
public class CheckJavac
{
	public boolean simple_check_executable(String javac_executable)
	{
		if (javac_executable==null)
			javac_executable="";
		if (!javac_executable.equals(""))
		{
			try
			{
				javac_executable=javac_executable.replaceAll("\\\\","/");
			}
			catch (Exception fs){}
		}
		try
		{
			boolean iswindows=false;
			String osversion=System.getProperty("os.name").toString();
			if ((osversion.toUpperCase()).startsWith("WIN")) iswindows=true;
			String[] command=new String[1];
			command[0]=javac_executable;
			if (javac_executable.indexOf("javac")<0)
			{
				if (!javac_executable.equals("") && !javac_executable.endsWith(System.getProperty("file.separator")))
					javac_executable=javac_executable+System.getProperty("file.separator");
				command[0]=javac_executable+"javac";
			}
			if (iswindows)
				command[0]="\""+command[0]+"\"";
			Process p = Runtime.getRuntime().exec(command);
			p.waitFor();
			Keywords.javac_path=command[0];
			return true;
		}
		catch (Exception e)
		{
			return false;
		}
	}
}