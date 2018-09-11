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

package ADaMSoft.algorithms.NLFitting;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Hashtable;
import java.util.Vector;

import ADaMSoft.dataaccess.DictionaryReader;
import ADaMSoft.keywords.Keywords;
import ADaMSoft.utilities.Compile_java_sdk;

/**
* This is the procedure that created a function to minimize
* @author marco.scarno@gmail.com
* @date 13/02/2017
*/
public class NormalFitting
{
	String retmess;
	String tempjava;
	String tempclass;
	String temperror;
	String workdir;
	boolean iserror;
	ErrorFunction ef;
	FunctionEval fe;
	boolean evaltype;
	String reffile;
	DictionaryReader dict;
	double[] start;
	private ClassLoader classtoexecute;
	/**
	*Initialize the file by receiving the function, the wordir, the info on the variables, the parameter list and the name of the dependent variable
	*/
	public NormalFitting(String function, String workdir, Vector<Hashtable<String, String>> infovar, String[] parameterList, String depvar)
	{
		Keywords.numfitting=Keywords.numfitting+1;
		reffile=String.valueOf(Keywords.numfitting);
		iserror=false;
		retmess="";
		tempjava=workdir+"EvaluateErrorFunction"+reffile+".java";
		tempclass=workdir+"EvaluateErrorFunction"+reffile+".class";
		temperror=workdir+"EvaluateErrorFunction"+reffile+".pop";
		BufferedWriter fun=null;
		try
		{
		    (new File(tempjava)).delete();
			(new File(tempclass)).delete();
			fun = new BufferedWriter(new FileWriter(tempjava, true));
			fun.write("import java.io.*;\n");
			fun.write("import java.util.*;\n");
			fun.write("import java.text.*;\n");
			fun.write("import java.lang.*;\n");
			fun.write("import java.net.*;\n");
			fun.write("import ADaMSoft.dataaccess.*;\n");
			fun.write("import ADaMSoft.keywords.*;\n");
			fun.write("import ADaMSoft.procedures.*;\n");
			fun.write("import ADaMSoft.utilities.*;\n");
			fun.write("import ADaMSoft.algorithms.*;\n");
			fun.write("import ADaMSoft.algorithms.NLFitting.*;\n");
			fun.write("public class EvaluateErrorFunction"+reffile+" extends ADaMSoftFunctions implements ErrorFunction, Serializable {\n");
			fun.write("	private static final long serialVersionUID = 1L;\n");
			for (int h=0; h<infovar.size(); h++)
			{
				String varname=(infovar.get(h)).get(Keywords.VariableName.toLowerCase());
				String vartype=(infovar.get(h)).get(Keywords.VariableFormat.toLowerCase());
				if (vartype.toLowerCase().startsWith(Keywords.NUMSuffix.toLowerCase()))
					fun.write("	double "+varname.toLowerCase()+"=Double.NaN;\n");
				else
					fun.write("	String "+varname.toLowerCase()+"=\"\";\n");
			}
			fun.write("	DictionaryReader dIct=null;\n");
			fun.write("	DataReader dAta=null;\n");
			fun.write("	String mEssage=\"\";\n");
			fun.write("	String[] vAlues=new String[0];\n");
			fun.write("	boolean errorFunction=false;\n");
			fun.write("	double functionValue=Double.NaN;\n");
			fun.write("	double tempfunctionValue=Double.NaN;\n");
			fun.write("	double numObs=Double.NaN;\n");
			fun.write("	public double evaluate(double[] coeff, DictionaryReader dIct){\n");
			for (int i=0; i<parameterList.length; i++)
			{
				fun.write("		double "+parameterList[i]+"=coeff["+String.valueOf(i)+"];\n");
			}
			fun.write("		functionValue=0;\n");
			fun.write("		numObs=0;\n");
			fun.write("		dAta = new DataReader(dIct);\n");
			fun.write("		if (!dAta.open(null, null, false)){\n");
			fun.write("			errorFunction=true;\n");
			fun.write("			mEssage=\"\";\n");
			fun.write("		}\n");
			fun.write("		while (!dAta.isLast()){\n");
			for (int h=0; h<infovar.size(); h++)
			{
				String varname=(infovar.get(h)).get(Keywords.VariableName.toLowerCase());
				String vartype=(infovar.get(h)).get(Keywords.VariableFormat.toLowerCase());
				if (vartype.toLowerCase().startsWith(Keywords.NUMSuffix.toLowerCase()))
					fun.write("			"+varname.toLowerCase()+"=Double.NaN;\n");
				else
					fun.write("			"+varname.toLowerCase()+"=\"\";\n");
			}
			fun.write("			vAlues = dAta.getRecord();\n");
			for (int j=0; j<infovar.size(); j++)
			{
				String varname=(infovar.get(j)).get(Keywords.VariableName.toLowerCase());
				String vartype=(infovar.get(j)).get(Keywords.VariableFormat.toLowerCase());
				if (vartype.toLowerCase().startsWith(Keywords.NUMSuffix.toLowerCase()))
				{
					fun.write("			try{\n");
					fun.write("				"+varname.toLowerCase()+"=Double.parseDouble(vAlues["+j+"].trim());\n");
					fun.write("			}catch (Exception eN) {"+varname.toLowerCase()+"=Double.NaN;}\n");
				}
				else
					fun.write("			"+varname.toLowerCase()+"=vAlues["+j+"].trim();\n");
			}
			fun.write("			tempfunctionValue="+function+";\n");
			fun.write("			if ((!Double.isNaN(tempfunctionValue)) && (!Double.isInfinite(tempfunctionValue))){\n");
			fun.write("				if ((!Double.isNaN("+depvar.toLowerCase()+")) && (!Double.isInfinite("+depvar.toLowerCase()+"))){\n");
			fun.write("					numObs++;\n");
			fun.write("					functionValue=functionValue+("+depvar.toLowerCase()+"-tempfunctionValue)*("+depvar.toLowerCase()+"-tempfunctionValue);\n");
			fun.write("				}\n");
			fun.write("			}\n");
			fun.write("		}dAta.close();\n");
			fun.write("		double valToreturn=Math.sqrt(functionValue/numObs);\n");
			fun.write("		return valToreturn;\n");

			fun.write("	}\n");

			fun.write("	public double test(double[] coeff, DictionaryReader dIct){\n");
			for (int i=0; i<parameterList.length; i++)
			{
				fun.write("		double "+parameterList[i]+"=coeff["+String.valueOf(i)+"];\n");
			}
			fun.write("		functionValue=0;\n");
			fun.write("		numObs=0;\n");
			fun.write("		dAta = new DataReader(dIct);\n");
			fun.write("		if (!dAta.open(null, null, false)){\n");
			fun.write("			errorFunction=true;\n");
			fun.write("			mEssage=\"\";\n");
			fun.write("		}\n");
			fun.write("		while (!dAta.isLast()){\n");
			for (int h=0; h<infovar.size(); h++)
			{
				String varname=(infovar.get(h)).get(Keywords.VariableName.toLowerCase());
				String vartype=(infovar.get(h)).get(Keywords.VariableFormat.toLowerCase());
				if (vartype.toLowerCase().startsWith(Keywords.NUMSuffix.toLowerCase()))
					fun.write("			"+varname.toLowerCase()+"=Double.NaN;\n");
				else
					fun.write("			"+varname.toLowerCase()+"=\"\";\n");
			}
			fun.write("			vAlues = dAta.getRecord();\n");
			for (int j=0; j<infovar.size(); j++)
			{
				String varname=(infovar.get(j)).get(Keywords.VariableName.toLowerCase());
				String vartype=(infovar.get(j)).get(Keywords.VariableFormat.toLowerCase());
				if (vartype.toLowerCase().startsWith(Keywords.NUMSuffix.toLowerCase()))
				{
					fun.write("			try{\n");
					fun.write("				"+varname.toLowerCase()+"=Double.parseDouble(vAlues["+j+"].trim());\n");
					fun.write("			}catch (Exception eN) {"+varname.toLowerCase()+"=Double.NaN;}\n");
				}
				else
					fun.write("			"+varname.toLowerCase()+"=vAlues["+j+"].trim();\n");
			}
			fun.write("			tempfunctionValue="+function+";\n");
			fun.write("			if ((!Double.isNaN(tempfunctionValue)) && (!Double.isInfinite(tempfunctionValue))){\n");
			fun.write("				if ((!Double.isNaN("+depvar.toLowerCase()+")) && (!Double.isInfinite("+depvar.toLowerCase()+"))){\n");
			fun.write("					numObs++;\n");
			fun.write("					functionValue=functionValue+("+depvar.toLowerCase()+"-tempfunctionValue)*("+depvar.toLowerCase()+"-tempfunctionValue);\n");
			fun.write("				}\n");
			fun.write("			}\n");
			fun.write("		}dAta.close();\n");
			fun.write("		double valToreturn=Math.sqrt(functionValue/numObs);\n");
			fun.write("		return valToreturn;\n");
			fun.write("	}\n");

			fun.write("}\n");
			fun.close();

		}
		catch (Exception e)
		{
			try
			{
				fun.close();
			}
			catch (Exception ec) {}
			retmess="%1644%<br>\n";
		    (new File(tempjava)).delete();
			(new File(tempclass)).delete();
			iserror=true;
		}
	}
	/**
	*Initialize the file by receiving the function, the wordir, the info on the variables, the vector of realparameters and the parameter list and the name of the dependent variable
	*/
	public NormalFitting(String function, String workdir, Vector<Hashtable<String, String>> infovar, Vector<String[]> realparameter, String[] parameterList, String depvar)
	{
		Keywords.numfitting=Keywords.numfitting+1;
		reffile=String.valueOf(Keywords.numfitting);
		iserror=false;
		retmess="";
		tempjava=workdir+"EvaluateFunction"+reffile+".java";
		tempclass=workdir+"EvaluateFunction"+reffile+".class";
		temperror=workdir+"EvaluateFunction"+reffile+".pop";
		BufferedWriter fun=null;
		try
		{
		    (new File(tempjava)).delete();
			(new File(tempclass)).delete();
			fun = new BufferedWriter(new FileWriter(tempjava, true));
			fun.write("import java.io.*;\n");
			fun.write("import java.util.*;\n");
			fun.write("import java.text.*;\n");
			fun.write("import java.lang.*;\n");
			fun.write("import java.net.*;\n");
			fun.write("import ADaMSoft.dataaccess.*;\n");
			fun.write("import ADaMSoft.keywords.*;\n");
			fun.write("import ADaMSoft.procedures.*;\n");
			fun.write("import ADaMSoft.utilities.*;\n");
			fun.write("import ADaMSoft.algorithms.*;\n");
			fun.write("import ADaMSoft.algorithms.NLFitting.*;\n");
			fun.write("public class EvaluateFunction"+reffile+" extends ADaMSoftFunctions implements FunctionEval, Serializable {\n");
			fun.write("	private static final long serialVersionUID = 1L;\n");
			for (int h=0; h<infovar.size(); h++)
			{
				String varname=(infovar.get(h)).get(Keywords.VariableName.toLowerCase());
				String vartype=(infovar.get(h)).get(Keywords.VariableFormat.toLowerCase());
				if (vartype.toLowerCase().startsWith(Keywords.NUMSuffix.toLowerCase()))
					fun.write("	double "+varname.toLowerCase()+"=Double.NaN;\n");
				else
					fun.write("	String "+varname.toLowerCase()+"=\"\";\n");
			}
			fun.write("	DictionaryReader dIct=null;\n");
			fun.write("	DataReader dAta=null;\n");
			fun.write("	String mEssage=\"\";\n");
			fun.write("	String[] vAlues=new String[0];\n");
			fun.write("	double functionValue=Double.NaN;\n");
			for (int i=0; i<realparameter.size(); i++)
			{
				String[] tp=realparameter.get(i);
				fun.write("	double "+tp[0].toLowerCase()+"="+tp[1]+";\n");
			}
			fun.write("	public String evaluate(DataSetUtilities dSu, DictionaryReader dIct, DataWriter dW){\n");
			fun.write("		functionValue=0;\n");
			fun.write("		dAta = new DataReader(dIct);\n");
			fun.write("		if (!dAta.open(null, null, false)){\n");
			fun.write("			return dAta.getmessage();\n");
			fun.write("		}\n");
			fun.write("		while (!dAta.isLast()){\n");
			for (int h=0; h<infovar.size(); h++)
			{
				String varname=(infovar.get(h)).get(Keywords.VariableName.toLowerCase());
				String vartype=(infovar.get(h)).get(Keywords.VariableFormat.toLowerCase());
				if (vartype.toLowerCase().startsWith(Keywords.NUMSuffix.toLowerCase()))
					fun.write("			"+varname.toLowerCase()+"=Double.NaN;\n");
				else
					fun.write("			"+varname.toLowerCase()+"=\"\";\n");
			}
			fun.write("			vAlues = dAta.getRecord();\n");
			for (int j=0; j<infovar.size(); j++)
			{
				String varname=(infovar.get(j)).get(Keywords.VariableName.toLowerCase());
				String vartype=(infovar.get(j)).get(Keywords.VariableFormat.toLowerCase());
				if (vartype.toLowerCase().startsWith(Keywords.NUMSuffix.toLowerCase()))
				{
					fun.write("			try{\n");
					fun.write("				"+varname.toLowerCase()+"=Double.parseDouble(vAlues["+j+"].trim());\n");
					fun.write("			}catch (Exception eN) {"+varname.toLowerCase()+"=Double.NaN;}\n");
				}
				else
					fun.write("			"+varname.toLowerCase()+"=vAlues["+j+"].trim();\n");
			}
			fun.write("			functionValue="+function+";\n");

			if (depvar!=null)
			{
				fun.write("			String[] newValues=new String[3];\n");
				fun.write("			newValues[0]=\"\";\n");
				fun.write("			newValues[1]=\"\";\n");
				fun.write("			newValues[2]=\"\";\n");
			}
			else
			{
				fun.write("			String[] newValues=new String[1];\n");
				fun.write("			newValues[0]=\"\";\n");
			}

			fun.write("			if ((!Double.isNaN(functionValue)) && (!Double.isInfinite(functionValue))){\n");
			if (depvar!=null)
			{
				fun.write("				newValues[0]=NUM2TEXT(functionValue);\n");
				fun.write("				double rEs="+depvar.toLowerCase()+"-functionValue;\n");
				fun.write("				newValues[1]=NUM2TEXT(rEs);\n");
				fun.write("				newValues[2]=NUM2TEXT(rEs*rEs);\n");
			}
			else
			{
				fun.write("				newValues[0]=NUM2TEXT(functionValue);\n");
			}
			fun.write("			}\n");
			fun.write("			String[] wValues=dSu.getnewvalues(vAlues, newValues);\n");
			fun.write("			dW.write(wValues);\n");
			fun.write("		}dAta.close();\n");
			fun.write("		return \"\";\n");

			fun.write("	}\n");
			fun.write("}\n");
			fun.close();
		}
		catch (Exception e)
		{
			try
			{
				fun.close();
			}
			catch (Exception ec) {}
			retmess="%1644%<br>\n";
		    (new File(tempjava)).delete();
			(new File(tempclass)).delete();
			iserror=true;
		}
	}
	/**
	*Receives the dictionary (in order to test the function)
	*/
	public void setdict(DictionaryReader dict)
	{
		this.dict=dict;
	}
	/**
	*Receive the initial value of the parameters
	*/
	public void setstart(double[] start)
	{
		this.start=start;
	}
	/**
	*If the method returns false then an error occours
	*/
	public boolean geterror()
	{
		return iserror;
	}
	/**
	*Returns the error message
	*/
	public String getretmess()
	{
		return retmess;
	}
	/**
	*Compile the function and returns false in case of error
	*/
	public boolean compilefunc(boolean evaltype)
	{
		this.evaltype=evaltype;
		String osversion=System.getProperty("os.name").toString();
		osversion=osversion.trim();
		String classpath=System.getProperty ("java.class.path").toString();
		String javaversion=System.getProperty("java.version").toString();
		String[] command=new String[4];
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
			retmess="%1645%<br>\n";
			return false;
		}
		if (!is_java_sdk)
		{
			command=new String[3];
			command[0]="-classpath";
			command[1]=classpath;
			command[2]=tempjava;
			try
			{
				PrintWriter pw = new PrintWriter(new FileOutputStream(temperror));
				int errorCode=0;
				try
				{
					errorCode = com.sun.tools.javac.Main.compile(command, pw);
				}
				catch (UnsupportedClassVersionError ue)
				{
					retmess="%3052%<br>\n";
					try
					{
						pw.flush();
						pw.close();
					}
					catch (Exception epw) {}
					return false;
				}
				catch (Exception eee)
				{
					retmess="%3052%<br>\n";
					try
					{
						pw.flush();
						pw.close();
					}
					catch (Exception epw) {}
					return false;
				}
				pw.flush();
				pw.close();
				if (errorCode==0)
					(new File(temperror)).delete();
				else
				{
					(new File(tempjava)).delete();
					try
					{
						retmess="%1646%<br>\n";
						BufferedReader in = new BufferedReader(new FileReader(temperror));
						String str;
						while ((str = in.readLine()) != null)
						{
							retmess=retmess+str+"<br>\n";
						}
						in.close();
						(new File(temperror)).delete();
					}
					catch (Exception e)
					{
						(new File(temperror)).delete();
					}
					return false;
				}
			}
			catch (Exception ee)
			{
				retmess="%1647%<br>\n";
				(new File(tempjava)).delete();
				(new File(tempclass)).delete();
				return false;
			}
		}
		else
		{
			String[] res_compiler=(new Compile_java_sdk()).compile_java_sdk(tempjava);
			if (!res_compiler[0].equals("0"))
			{
				retmess="%1646%<br>\n";
				retmess=retmess+res_compiler[1]+"<br>";
				(new File(tempjava)).delete();
				return false;
			}
			(new File(tempjava)).delete();
		}
		if (!evaltype)
		{
			ef=null;
			try
			{
				File fileclass = new File(System.getProperty(Keywords.WorkDir));
				URL url = fileclass.toURI().toURL();
				URL[] urls = new URL[]{url};
				classtoexecute = new URLClassLoader(urls);
				Class<?> cls = classtoexecute.loadClass("EvaluateErrorFunction"+reffile);
     			ef = (ErrorFunction)cls.newInstance();
     		}
     		catch (Exception e)
     		{
				retmess="%1648%<br>\n";
				ef=null;
			    (new File(tempjava)).delete();
				(new File(tempclass)).delete();
				return false;
			}
     		try
     		{
				double testfunc=ef.test(start, dict);
				if (Double.isNaN(testfunc))
				{
					retmess="%1661%<br>\n";
					ef=null;
				   	(new File(tempjava)).delete();
					(new File(tempclass)).delete();
					return false;
				}
			}
			catch (Exception e)
			{
				retmess="%1651%<br>\n";
				ef=null;
			    (new File(tempjava)).delete();
				(new File(tempclass)).delete();
				return false;
			}
		}
		else
		{
			fe=null;
			try
			{
				File fileclass = new File(System.getProperty(Keywords.WorkDir));
				URL url = fileclass.toURI().toURL();
				URL[] urls = new URL[]{url};
				classtoexecute = new URLClassLoader(urls);
				Class<?> cls = classtoexecute.loadClass("EvaluateFunction"+reffile);
	     		fe = (FunctionEval)cls.newInstance();
	     	}
	     	catch (Exception e)
	     	{
				retmess="%1661%<br>\n";
				fe=null;
			    (new File(tempjava)).delete();
				(new File(tempclass)).delete();
				return false;
			}
		}
		return true;
	}
	/**
	*Returns the compiled error function
	*/
	public ErrorFunction getEF()
	{
		return ef;
	}
	/**
	*Returns the compiled function used to evaluate the predicted value
	*/
	public FunctionEval getFE()
	{
		return fe;
	}
	/**
	*Clean the memory
	*/
	public void clearmem()
	{
		try
		{
			if (!evaltype)
				ef=null;
			else
				fe=null;
	    	(new File(tempjava)).delete();
			(new File(tempclass)).delete();
		}
		catch (Exception ee) {}
	}
}
