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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URLConnection;
import java.text.MessageFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import ADaMSoft.keywords.Keywords;
import ADaMSoft.utilities.GetRequiredParameters;
import ADaMSoft.utilities.StepUtilities;

/**
* Update the information contained into an ADaMSoft document file
* @author marco.scarno@gmail.com
* @date 12/05/2017
*/
public class AdamsdocUpdateinfo implements RunStep
{
	/**
	*This contains the full path of the saved document
	*/
	String fullpath="";
	/**
	*This contains the document name that will be updated
	*/
	String documentname;
	private ZipOutputStream zipOut;
	/**
	*Update the Document.<p>
	*/
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		Keywords.percentage_total=0;
		Keywords.percentage_done=0;
		String [] requiredparameters=new String[] {Keywords.docname};
		String [] optionalparameters=new String[] {Keywords.docpath, Keywords.keyword, Keywords.description, Keywords.author, Keywords.encryptwith};
		StepUtilities steputilities=new StepUtilities();
		if (!steputilities.checkParameters(requiredparameters, optionalparameters, parameters))
		{
			return new Result(steputilities.getMessage(), false, null);
		}
		String path       =(String)parameters.get(Keywords.docpath);
		String keyword    =(String)parameters.get(Keywords.keyword);
		String description=(String)parameters.get(Keywords.description);
		String author     =(String)parameters.get(Keywords.author);
		documentname      =(String)parameters.get(Keywords.docname);
		String workdir    =(String)parameters.get(Keywords.WorkDir);

		documentname=documentname.toLowerCase();
		String firstChar=documentname.substring(0,1);
		documentname = documentname.replaceFirst(firstChar,firstChar.toUpperCase());

		String[] information=new String[6];
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
		if (path==null)
			path=workdir;

		fullpath=path+documentname;
		if(!fullpath.endsWith(Keywords.DocExtension))
			fullpath=fullpath+Keywords.DocExtension;

		boolean testexist=(new File(fullpath)).exists();
		if (!testexist)
		{
			return new Result("%234%<br>\n", false, null);
		}
		Object[] paramsdate = new Object[]{new Date(), new Date(0)};
		information[4]=MessageFormat.format(" {0}", paramsdate);
		Vector<StepResult> result= new Vector<StepResult>();
		try
		{
			doUpdate(information);
			return new Result("%235% ("+fullpath+")<br>\n", true, result);
		}
		catch (IOException e)
		{
			return new Result("%236%<br>\n",false,null);
		}
	}
	/**
	* Update a local document
	*/
	private void doUpdate(String[] information) throws IOException
	{
		java.net.URL documentfile=(new File(fullpath)).toURI().toURL();
		URLConnection urlConn = documentfile.openConnection();
		urlConn.setDoInput(true);
		urlConn.setUseCaches(false);
		ZipInputStream indata= new ZipInputStream(urlConn.getInputStream());
		ZipEntry entry;
		OutputStream out = new FileOutputStream(fullpath+".tmp");
		zipOut = new ZipOutputStream(out);
		while ((entry = indata.getNextEntry()) != null)
		{
			String nameFile = entry.getName();
			if (nameFile.equalsIgnoreCase("Info"))
			{
				BufferedReader filebuffered= new BufferedReader(new InputStreamReader(indata));
				String line=filebuffered.readLine();
				String [] temp=line.split("\t");
				information[3]=temp[3];
				information[5]=temp[5];
				String info="";
				for (int i=0; i<information.length; i++)
				{
					info=info+information[i];
					if (i<information.length-1)
						info=info+"\t";
				}
				zipOut.putNextEntry(new ZipEntry("Info"));
				PrintStream outP = new PrintStream(zipOut);
				outP.print(info);
				zipOut.closeEntry();
			}
			if (nameFile.equalsIgnoreCase("Document"))
			{
				try
				{
					zipOut.putNextEntry(new ZipEntry("Document"));
					int accessed=0;
					byte[] buffer=new byte[1024];
					while((accessed=indata.read(buffer))!=-1)
					{
						zipOut.write(buffer,0 , accessed);
					}
					zipOut.closeEntry();
				}
				catch(IOException e)
				{
					throw e;
				}
			}
			else zipOut.closeEntry();
        }
		indata.close();
		zipOut.finish();
		zipOut.close();
		out.close();
		new File(fullpath).delete();
		new File(fullpath+".tmp").renameTo(new File(fullpath));
	}
	public LinkedList<GetRequiredParameters> getparameters()
	{
		LinkedList<GetRequiredParameters> parameters=new LinkedList<GetRequiredParameters>();
		String[] dep ={""};
		parameters.add(new GetRequiredParameters(Keywords.docpath+"=", "path", false, 237, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.docname, "text", true,  233, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.keyword, "text", false, 179, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.description, "text", false, 180, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.author, "text", false, 181, dep, "", 2));
		return parameters;
	}
	public String[] getstepinfo()
	{
		String[] info=new String[2];
		info[0]="231";
		info[1]="232";
		return info;
	}
}
