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

import java.io.ByteArrayOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import ADaMSoft.keywords.Keywords;
import ADaMSoft.utilities.ADaMSdoc;
import ADaMSoft.utilities.GetRequiredParameters;
import ADaMSoft.utilities.StepUtilities;

/**
* Restore an ADaMSoft document by creating a file
* @author marco.scarno@gmail.com
* @date 12/05/2017
*/
public class AdamsdocRestore implements RunStep
{
	/**
	*This is the dimension of the buffer
	*/
	private static final int buffSize=1024;
	/**
	*This is the extension of the original file
	*/
	String realext=null;
	/**
	*Restore a document (file) contained into an ADaMSoft document file
	*/
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		Keywords.percentage_total=2;
		Keywords.percentage_done=0;
		String [] requiredparameters=new String[] {Keywords.docname};
		String [] optionalparameters=new String[] {Keywords.docpath, Keywords.decryptwith, Keywords.outpath, Keywords.outname};
		StepUtilities steputilities=new StepUtilities();
		if (!steputilities.checkParameters(requiredparameters, optionalparameters, parameters))
		{
			Keywords.percentage_total=0;
			return new Result(steputilities.getMessage(), false, null);
		}
		String path        =(String)parameters.get(Keywords.docpath);
		String passwd      =(String)parameters.get(Keywords.decryptwith);
		String documentname=(String)parameters.get(Keywords.docname);
		String outpath     =(String)parameters.get(Keywords.outpath);
		String outname     =(String)parameters.get(Keywords.outname);
		String workdir     =(String)parameters.get(Keywords.WorkDir);

		documentname=documentname.toLowerCase();

		String firstChar=documentname.substring(0,1);
		documentname = documentname.replaceFirst(firstChar,firstChar.toUpperCase());

		if (path==null)
			path=workdir;
		path=path+documentname;

		if(!path.endsWith(Keywords.DocExtension))
			path=path+Keywords.DocExtension;

		File file= new File(path);
		if(!file.exists())
		{
			Keywords.percentage_total=0;
			return new Result("%83% ("+path+")<br>\n", false, null);
		}

		ADaMSdoc doc = doRestore(file);
		if(doc==null)
		{
			Keywords.percentage_total=0;
			return new Result("%84% ("+path+")<br>\n", false, null);
		}

		Vector<StepResult> result = new Vector<StepResult>();

		if (outname==null)
			outname=documentname;

		if(outpath==null)
			outpath=workdir;

		if (realext!=null)
			outname=outname+"."+realext;

		result.add(new LocalDocumentRestore(outpath, outname, doc));

		Keywords.percentage_total=0;
		if (passwd!=null)
			System.setProperty(Keywords.docpwd,passwd);
		else
			System.setProperty(Keywords.docpwd, "");
		String res=result.get(0).action();
		if(res.startsWith("1"))
		{
			return new Result((res.substring(2)).trim()+"<br>\n", true, null);
		}
		else
		{
			return new Result((res.substring(2)).trim()+"<br>\n", false, null);
		}
	}
	/**
	*Loads the ADaMSoft document into a byte array
	*/
	private ADaMSdoc doRestore(File file)
	{
		byte[] buffer;
		try
		{
			ZipInputStream zipIn = new ZipInputStream(file.toURI().toURL().openStream());
			ZipEntry ze;
			ByteArrayOutputStream memOut=null;
			while ((ze = zipIn.getNextEntry()) != null)
			{
				if (ze.getName().equalsIgnoreCase("Info"))
				{
					BufferedReader filebuffered= new BufferedReader(new InputStreamReader(zipIn));
					String line=filebuffered.readLine();
					String [] temp=line.split("\t");
					realext=temp[5];
					try
					{
						String[] tempext=realext.split("\\.");
						if (tempext.length>0)
							realext=tempext[tempext.length-1];
					}
					catch (Exception ee)
					{
						realext=null;
					}
					zipIn.closeEntry();
				}
				else if (ze.getName().equalsIgnoreCase("Document"))
				{
					memOut=new ByteArrayOutputStream();
					buffer = new byte[buffSize];
					int byter=0;
					while((byter=zipIn.read(buffer))!=-1)
					{
						memOut.write(buffer,0,byter);
					}
					zipIn.closeEntry();
				}
			}
			zipIn.close();
			if (memOut==null)
				return null;
			memOut.flush();
			memOut.close();
			return new ADaMSdoc(memOut.toByteArray());
		}
		catch(IOException e)
		{
			return null;
		}
	}
	public LinkedList<GetRequiredParameters> getparameters()
	{
		LinkedList<GetRequiredParameters> parameters=new LinkedList<GetRequiredParameters>();
		String[] dep ={""};
		parameters.add(new GetRequiredParameters(Keywords.docpath+"=", "path", false, 157, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.outpath+"=", "path", false, 158, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.docname, "text", true,  159, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.outname, "text", false, 160, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.decryptwith, "text", false, 161, dep, "", 2));
		return parameters;
	}
	public String[] getstepinfo()
	{
		String[] info=new String[2];
		info[0]="163";
		info[1]="3971";
		return info;
	}
}
