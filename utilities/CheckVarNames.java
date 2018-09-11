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

import java.util.Hashtable;
import java.util.Vector;
import java.util.Enumeration;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;

/**
* This class is used to check if the used variable names are corrected
* @author marco.scarno@gmail.com
* @date 11/06/2018
*/
public class CheckVarNames
{
	/**
	* Receive the information on the variables
	*/
	public static String getResultCheck (Vector<Hashtable<String, String>> fixedvariableinfo, String workdir)
	{
		String message="";
		if (fixedvariableinfo.size()==0)
			return message;

		BufferedWriter fun=null;
		String tempjava=workdir+"CheckVN.java";
		String tempclass=workdir+"CheckVN.class";
		String temperror=workdir+"CheckVN.pop";

		try
		{
		    (new File(tempjava)).delete();
			(new File(tempclass)).delete();
		    (new File(temperror)).delete();
			fun = new BufferedWriter(new FileWriter(tempjava, true));
			fun.write("public class CheckVN{\n");
			fun.write("	public static void main(String[] argv){\n");
		}
		catch (Exception e)
		{
			message="%1769%\n";
			return message;
		}
		boolean emptyvar=false;
		for (int i=0; i<fixedvariableinfo.size(); i++)
		{
			Hashtable<String, String> tempfixedvariableinfo=fixedvariableinfo.get(i);
			String vn=tempfixedvariableinfo.get(Keywords.VariableName.toLowerCase());
			vn=vn.trim();
			if (vn.equals(""))
				emptyvar=true;
		}
		if (emptyvar)
		{
			try
			{
				fun.close();
			}
			catch (Exception ee) {}
		    (new File(tempjava)).delete();
			(new File(tempclass)).delete();
		    (new File(temperror)).delete();
			message="%1796%<br>\n";
			return message;
		}

		try
		{
			for (int i=0; i<fixedvariableinfo.size(); i++)
			{
				Hashtable<String, String> tempfixedvariableinfo=fixedvariableinfo.get(i);
				String vn=tempfixedvariableinfo.get(Keywords.VariableName.toLowerCase());
				fun.write("String "+vn+"=\"\";\n");
			}
			fun.write("}}\n");
			fun.close();
		}
		catch (Exception e)
		{
			message="%1769%<br>\n";
			return message;
		}
		String classpath=System.getProperty ("java.class.path").toString();
		String[] command=new String[4];
		command=new String[3];
		command[0]="-classpath";
		command[1]=classpath;
		command[2]=tempjava;
		String javaversion=System.getProperty("java.version").toString();
		boolean compok=false;
		boolean is_java_sdk=false;
		for (int i=0; i<Keywords.VersionJavaCompiler.length; i++)
		{
			if (javaversion.startsWith(Keywords.VersionJavaCompiler[i]))
			{
				is_java_sdk=true;
				compok=true;
			}
		}
		if (!compok)
		{
			if (javaversion.startsWith("1.8")) compok=true;
			if (javaversion.startsWith("1.7")) compok=true;
			if (javaversion.startsWith("1.6")) compok=true;
			if (javaversion.startsWith("8")) compok=true;
			if (javaversion.startsWith("7")) compok=true;
			if (javaversion.startsWith("6")) compok=true;
		}
		if (!compok)
		{
			message="%2318%<br>\n";
			return message;
		}
		if (!is_java_sdk)
		{
			try
			{
				PrintWriter pw = new PrintWriter(new FileOutputStream(temperror));
				int errorCode = com.sun.tools.javac.Main.compile(command, pw);
				pw.flush();
				pw.close();
				if (errorCode!=0)
				{
					try
					{
						message="%1770%:<br>\n";
						BufferedReader in = new BufferedReader(new FileReader(temperror));
						String str;
						Hashtable<String, String> nn=new Hashtable<String, String>();
						while ((str = in.readLine()) != null)
						{
							if (str.startsWith("String"))
							{
								str=str.substring("String".length());
								str=str.trim();
								String[] stra=str.split("=");
								nn.put(stra[0],"");
							}
						}
						for (Enumeration<String> e = nn.keys() ; e.hasMoreElements() ;)
						{
							String par = ((String) e.nextElement());
							message=message+"%306% "+par+"<br>\n";
						}
						in.close();
					}
					catch (Exception e)
					{
						message="%1771%<br>\n";
					}
				}
			}
			catch (Exception ee)
			{
				message="%1772%<br>\n";
			}
		}
		else
		{
			String[] res_compiler=(new Compile_java_sdk()).compile_java_sdk(tempjava);
			if (!res_compiler[0].equals("0"))
			{
				message=res_compiler[1]+"<br>";
			}
		}
		try
		{
			(new File(tempjava)).delete();
			(new File(tempclass)).delete();
			(new File(temperror)).delete();
		}
		catch (Exception e) {}
		return message;
	}
	/**
	* Receive the information on a variable name
	*/
	public static String getResultCheck(String varname, String workdir)
	{
		String message="";
		if (varname==null)
			return message;
		if (varname.equals(""))
			return message;

		BufferedWriter fun=null;
		String tempjava=workdir+"CheckVN.java";
		String tempclass=workdir+"CheckVN.class";
		String temperror=workdir+"CheckVN.pop";

		try
		{
		    (new File(tempjava)).delete();
			(new File(tempclass)).delete();
		    (new File(temperror)).delete();
			fun = new BufferedWriter(new FileWriter(tempjava, true));
			fun.write("public class CheckVN{\n");
			fun.write("	public static void main(String[] argv){\n");
		}
		catch (Exception e)
		{
			message="%1769%\n";
			return message;
		}
		try
		{
			fun.write("String "+varname+"=\"\";\n");
			fun.write("}}\n");
			fun.close();
		}
		catch (Exception e)
		{
			message="%1769%\n";
			return message;
		}

		String classpath=System.getProperty ("java.class.path").toString();
		String[] command=new String[4];
		command=new String[3];
		command[0]="-classpath";
		command[1]=classpath;
		command[2]=tempjava;
		String javaversion=System.getProperty("java.version").toString();
		boolean compok=false;
		boolean is_java_sdk=false;
		for (int i=0; i<Keywords.VersionJavaCompiler.length; i++)
		{
			if (javaversion.startsWith(Keywords.VersionJavaCompiler[i]))
			{
				is_java_sdk=true;
				compok=true;
			}
		}
		if (!compok)
		{
			if (javaversion.startsWith("1.8")) compok=true;
			if (javaversion.startsWith("1.7")) compok=true;
			if (javaversion.startsWith("1.6")) compok=true;
			if (javaversion.startsWith("8")) compok=true;
			if (javaversion.startsWith("7")) compok=true;
			if (javaversion.startsWith("6")) compok=true;
		}
		if (!compok)
		{
			message="%2318%<br>\n";
			return message;
		}
		if (!is_java_sdk)
		{
			try
			{
				PrintWriter pw = new PrintWriter(new FileOutputStream(temperror));
				int errorCode = com.sun.tools.javac.Main.compile(command, pw);
				pw.flush();
				pw.close();
				if (errorCode!=0)
				{
					try
					{
						message="%1770%:<br>\n";
						BufferedReader in = new BufferedReader(new FileReader(temperror));
						String str;
						Hashtable<String, String> nn=new Hashtable<String, String>();
						while ((str = in.readLine()) != null)
						{
							if (str.startsWith("String"))
							{
								str=str.substring("String".length());
								str=str.trim();
								String[] stra=str.split("=");
								nn.put(stra[0],"");
							}
						}
						for (Enumeration<String> e = nn.keys() ; e.hasMoreElements() ;)
						{
							String par = ((String) e.nextElement());
							message=message+"%306%: "+par+"\n";
						}
						in.close();
					}
					catch (Exception e)
					{
						message="%1771%<br>\n";
					}
				}
			}
			catch (Exception ee)
			{
				message="%1772%<br>\n";
			}
		}
		else
		{
			String[] res_compiler=(new Compile_java_sdk()).compile_java_sdk(tempjava);
			if (!res_compiler[0].equals("0"))
			{
				message=res_compiler[1]+"<br>";
			}
		}
		try
		{
			(new File(tempjava)).delete();
			(new File(tempclass)).delete();
			(new File(temperror)).delete();
		}
		catch (Exception e) {}
		return message;
	}
}
