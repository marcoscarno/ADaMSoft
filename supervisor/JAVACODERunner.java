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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Vector;

import ADaMSoft.keywords.Keywords;
import ADaMSoft.utilities.ScriptParserUtilities;
import ADaMSoft.utilities.Compile_java_sdk;

/**
* Executes the JAVACODE step
* @author marco.scarno@gmail.com
* @date 11/06/2018
*/
public class JAVACODERunner extends ScriptParserUtilities
{
	String message;
	boolean steperror;
	public JAVACODERunner(Vector<String> KeywordValue)
	{
		message="";
		String firstline=KeywordValue.get(0);
		String jc="";
		try
		{
			String[] partfl=firstline.split(" ");
			if (partfl.length!=2)
			{
				message=Keywords.Language.getMessage(2309)+"<br>\n";
				steperror=true;
				return ;
			}
			jc=partfl[1].replaceAll(";","");
			jc=jc.trim();
		}
		catch (Exception e)
		{
			message=Keywords.Language.getMessage(2309)+"<br>\n";
			steperror=true;
			return ;
		}
		String refdir=System.getProperty(Keywords.WorkDir);
		try
		{
			String[] testispath=jc.split("\\.");
			if (testispath.length>2)
			{
				message=Keywords.Language.getMessage(2709)+"<br>\n";
				steperror=true;
				return ;
			}
			if (testispath.length==2)
			{
				refdir=Keywords.project.getPath(testispath[0]);
				jc=testispath[1];
				if (refdir==null)
				{
					message=Keywords.Language.getMessage(1062)+" ("+testispath[0]+")<br>\n";
					steperror=true;
					return ;
				}
				else if (refdir.equalsIgnoreCase(""))
				{
					message=Keywords.Language.getMessage(61)+" ("+testispath[0]+")<br>\n";
					steperror=true;
					return ;
				}
			}
		}
		catch (Exception e)
		{
			message=Keywords.Language.getMessage(2709)+"<br>\n";
			steperror=true;
			return ;
		}

		String tempjava=refdir+jc+".java";
		String tempclass=refdir+jc+".class";
		String temperror=refdir+jc+".pop";

		if((new File(tempclass)).exists())
		{
			System.gc();
			if (!(new File(tempclass)).delete())
			{
				message=Keywords.Language.getMessage(2310)+"<br>\n";
				steperror=true;
				return ;
			}
		}
		BufferedWriter ds=null;
		try
		{
		    (new File(tempjava)).delete();
			(new File(tempclass)).delete();
			ds = new BufferedWriter(new FileWriter(tempjava, true));
			boolean replaced=false;
			String actualrow="";
			for (int i=1; i<KeywordValue.size(); i++)
			{
				replaced=false;
				actualrow=KeywordValue.get(i);
				try
				{
					if (actualrow.indexOf(Keywords.SeMiCoLoN)>=0)
					{
						actualrow=actualrow.replaceAll(Keywords.SeMiCoLoN,";");
						replaced=true;
					}
				}
				catch (Exception es) {}
				if (replaced)
					ds.write(actualrow+"\n");
				else
				{
					if (i<KeywordValue.size()-1)
						ds.write(actualrow+";\n");
					else
						ds.write(actualrow+"\n");
				}
			}
			ds.close();
		}
		catch (Exception e)
		{
			try
			{
				ds.close();
			}
			catch (Exception ec) {}
			message=Keywords.Language.getMessage(2311)+"<br>\n";
			steperror=true;
			return ;
		}

		String osversion=System.getProperty("os.name").toString();
		osversion=osversion.trim();

		String classpath=System.getProperty ("java.class.path").toString();
		String[] command=new String[4];
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
			message=Keywords.Language.getMessage(2318)+"<br>\n";
			steperror=true;
			return ;
		}
		boolean appendcode=false;
		String testDEBUG=System.getProperty("DEBUG");
		if (!testDEBUG.equalsIgnoreCase("false"))
			appendcode=true;
		String codetoappend="";
		if (!is_java_sdk)
		{
			command=new String[3];
			command[0]="-classpath";
			command[1]=classpath;
			command[2]=tempjava;
			try
			{
				PrintWriter pw = new PrintWriter(new FileOutputStream(temperror));
				int errorCode = 0;
				try
				{
					errorCode = com.sun.tools.javac.Main.compile(command, pw);
				}
				catch (UnsupportedClassVersionError ue)
				{
					message=Keywords.Language.getMessage(3052)+"<br>\n";
					try
					{
						pw.flush();
						pw.close();
					}
					catch (Exception epw) {}
					steperror=true;
					return ;
				}
				catch (Exception eee)
				{
					message=Keywords.Language.getMessage(3052)+"<br>\n";
					try
					{
						pw.flush();
						pw.close();
					}
					catch (Exception epw) {}
					steperror=true;
					return ;
				}
				pw.flush();
				pw.close();
				if (errorCode==0)
					(new File(temperror)).delete();
				else
				{
					if (appendcode)
					{
						try
						{
							BufferedReader in = new BufferedReader(new FileReader(tempjava));
							String strcode="";
							int refrows=1;
							codetoappend="<i>";
							while ((strcode = in.readLine()) != null)
							{
								codetoappend=codetoappend+"("+String.valueOf(refrows)+") "+strcode+"<br>\n";
								refrows++;
							}
							in.close();
							codetoappend=codetoappend+"</i>";
						}
						catch (IOException e) {}
						codetoappend=Keywords.Language.getMessage(2187)+"<br>\n"+codetoappend+"<br>\n";
					}
					(new File(tempjava)).delete();
					try
					{
						message=codetoappend+Keywords.Language.getMessage(2319)+"<br>\n";
						BufferedReader in = new BufferedReader(new FileReader(temperror));
						String str;
						while ((str = in.readLine()) != null)
						{
							message=message+str+"<br>\n";
						}
						in.close();
						(new File(temperror)).delete();
					}
					catch (IOException e)
					{
						(new File(temperror)).delete();
					}
					steperror=true;
					return ;
				}
			}
			catch (Exception ee)
			{
				message=Keywords.Language.getMessage(2317)+"<br>\n";
				steperror=true;
				return ;
			}
		}
		else
		{
			String[] res_compiler=(new Compile_java_sdk()).compile_java_sdk(tempjava);
			if (!res_compiler[0].equals("0"))
			{
				message=Keywords.Language.getMessage(4235)+"<br>\n";
				message=message+res_compiler[1]+"<br>";
				if (appendcode)
				{
					String code9toappend="<i>";
					try
					{
						BufferedReader in = new BufferedReader(new FileReader(tempjava));
						String strcode="";
						int refrows=1;
						while ((strcode = in.readLine()) != null)
						{
							code9toappend=code9toappend+"("+String.valueOf(refrows)+") "+strcode+"<br>\n";
							refrows++;
						}
						in.close();
						code9toappend=code9toappend+"</i><br>";
					}
					catch (IOException e) {}
					code9toappend=Keywords.Language.getMessage(2187)+"\n"+code9toappend+"<br>\n";
					message=message+"<br>"+code9toappend;
				}
				steperror=true;
				(new File(tempjava)).delete();
				return;
			}
		}
		(new File(tempjava)).delete();
		message=Keywords.Language.getMessage(2320)+" ("+jc+")<br>\n";
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
