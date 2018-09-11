/**
* Copyright © (c) 2017 MS
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
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.text.MessageFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import ADaMSoft.keywords.Keywords;
import ADaMSoft.utilities.ADaMSdoc;
import ADaMSoft.utilities.GetRequiredParameters;
import ADaMSoft.utilities.StepUtilities;

/**
* Save a document (a file) in the ADaMSoft compressed form
* @author marco.scarno@gmail.com
* @date 12/05/2017
*/
public class AdamsdocSave implements RunStep
{
	/**
	*If not true and the document exist, than the procedure returns an error. It is true if it is present,
	*between theparameters, the keywords replace.
	*/
	boolean replace=false;
	/**
	*This contains the full path of the saved document
	*/
	String fullpath="";
	/**
	*This contains the document name that will be created
	*/
	String documentname;
	/**
	*Save the Document (contained into the received Hashtable) in the ADaMSoft compressed form.<p>
	*/
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		String [] requiredparameters=new String[] {Keywords.docfile, Keywords.docname};
		String [] optionalparameters=new String[] {Keywords.docpath, Keywords.keyword, Keywords.description, Keywords.author, Keywords.encryptwith};
		Keywords.percentage_total=0;
		Keywords.percentage_done=0;
		StepUtilities steputilities=new StepUtilities();
		if (!steputilities.checkParameters(requiredparameters, optionalparameters, parameters))
		{
			return new Result(steputilities.getMessage(), false, null);
		}
		String path       =(String)parameters.get(Keywords.docpath);
		String keyword    =(String)parameters.get(Keywords.keyword);
		String description=(String)parameters.get(Keywords.description);
		String author     =(String)parameters.get(Keywords.author);
		String encrypted  =(String)parameters.get(Keywords.encryptwith);
		documentname      =(String)parameters.get(Keywords.docname);
		ADaMSdoc doc      =(ADaMSdoc)parameters.get(Keywords.document);
		replace           =(parameters.get(Keywords.replace)!=null);
		String workdir    =(String)parameters.get(Keywords.WorkDir);

		String realname=(new File((String)parameters.get(Keywords.docfile))).getName();
		documentname=documentname.toLowerCase();
		String firstChar=documentname.substring(0,1);
		documentname = documentname.replaceFirst(firstChar,firstChar.toUpperCase());

		String[] information=new String[6];
		information[5]=realname;
		if (author!=null)
			information[0]=author;
		else
			information[0]=(String)parameters.get(Keywords.client_host.toLowerCase());
		if (keyword==null)
			keyword="";
		information[1]=keyword;
		if (description==null)
			description="";
		information[2]=description;
		if (encrypted==null)
			information[3]="0";
		else
			information[3]="1";

		if (path==null)
			path=workdir;
		fullpath=path+documentname;

		if(!fullpath.endsWith(Keywords.DocExtension))
			fullpath=fullpath+Keywords.DocExtension;

		boolean testexist=(new File(fullpath)).exists();
		if ((!replace) && (testexist))
		{
			return new Result("%85%<br>\n", false, null);
		}
		Object[] paramsdate = new Object[2];
		paramsdate[0]=new Date();
		paramsdate[1]=new Date(0);

		information[4]=MessageFormat.format(" {0}", paramsdate);
		if(doc==null)
			return new Result("%80%<br>\n", false, null);
		Vector<StepResult> result;
		try
		{
			result = doUpload(doc, information, path, workdir);
			return new Result("%82% ("+fullpath+")<br>\n", true, result);
		}
		catch (IOException e)
		{
			String msgerror="%81%<br>\n";
			if (path!=null)
				msgerror=msgerror+" ("+path+")";
			msgerror=msgerror+"<br>\n";
			return new Result(msgerror,false,null);
		}
	}
	/**
	*Creates the ADaMSoft document
	* to the client
	*/
	private Vector<StepResult> doUpload(ADaMSdoc doc, String[] information, String path, String workdir) throws IOException
	{
		byte[] buffer;
		String info="";
		for (int i=0; i<information.length; i++)
		{
			info=info+information[i];
			if (i<information.length-1)
				info=info+"\t";
		}
		try
		{
			buffer= doc.getZippedArray();
			OutputStream out;
			out = new FileOutputStream(fullpath);
			ZipOutputStream zipOut = new ZipOutputStream(out);
			zipOut.putNextEntry(new ZipEntry("Document"));
			zipOut.write(buffer);
			zipOut.closeEntry();
			zipOut.putNextEntry(new ZipEntry("Info"));
			PrintStream outP = new PrintStream(zipOut);
			outP.print(info);
			zipOut.closeEntry();
			zipOut.finish();
			zipOut.close();
			out.close();
			return null;
		}
		catch(IOException e)
		{
			throw e;
		}

	}
	public LinkedList<GetRequiredParameters> getparameters()
	{
		LinkedList<GetRequiredParameters> parameters=new LinkedList<GetRequiredParameters>();
		String[] dep ={""};
		parameters.add(new GetRequiredParameters(Keywords.docpath+"=", "path", false, 176, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.docfile, "file=all", true,  177, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.docname, "text", true,  175, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.keyword, "text", false, 179, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.description, "text", false, 180, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.author, "text", false, 181, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.encryptwith, "text", false, 178, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.replace, "checkbox", false, 184, dep, "", 2));
		return parameters;
	}
	public String[] getstepinfo()
	{
		String[] info=new String[2];
		info[0]="182";
		info[1]="3969";
		return info;
	}
}
