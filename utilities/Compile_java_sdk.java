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
* This class is used to compile a java file<p>
* @author marco.scarno@gmail.com
* @date 12/06/2018
*/
public class Compile_java_sdk
{
	public String[] compile_java_sdk(String tempjava)
	{
		String[] result_compilation=new String[2];
		try
		{
			String classpath=System.getProperty ("java.class.path").toString();
			String[] command=new String[4];
			command[0]=Keywords.javac_path;
			command[1]="-classpath";
			command[2]=classpath;
			command[3]=tempjava;
			Runtime rt = Runtime.getRuntime();
			Process proc = rt.exec(command);
			BufferedReader stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
			String str = null;
			result_compilation[1]="";
			while ((str = stdError.readLine()) != null)
			{
				result_compilation[1]=result_compilation[1]+str+"<br>\n";
			}
			int exitVal = proc.waitFor();
			result_compilation[0]=String.valueOf(exitVal);
		}
		catch (Exception e)
		{
			result_compilation[0]="-1";
			result_compilation[1]=e.toString();
		}
		return result_compilation;
	}
}