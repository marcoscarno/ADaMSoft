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
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.net.URLConnection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import ADaMSoft.keywords.Keywords;

/**
 * This class updates a local document
 * @author marco.scarno@gmail.com
 * @date 13/02/2017
 */
public class LocalDocumentUpdateinfo implements StepResult, Serializable
{
	/**
	 * This is the defult serial version UID
	 */
	private static final long serialVersionUID = 1L;
	/**
	*This is the path
	*/
	String path;
	/**
	*This is the name of the document
	*/
	String name;
	/**
	*Here are contained the information
	*/
	String[] information;
	/**
	*Constructor
	*/
	public LocalDocumentUpdateinfo(String path, String documentname, String[] information)
	{
		this.path=path;
		this.information=information;
		this.name=documentname;
	}
	/*Not used*/
	public void exportOutput()
	{
	}
	/**
	*Do the update
	*/
	public String action()
	{
		if (path==null)
			path=System.getProperty(Keywords.WorkDir);
		path=path+name;

		if(!path.endsWith(Keywords.DocExtension))
			path=path+Keywords.DocExtension;

		try
		{
			boolean testexist=(new File(path)).exists();
			if (!testexist)
				return "0 %234%<br>\n";
			java.net.URL documentfile=(new File(path)).toURI().toURL();
			URLConnection urlConn = documentfile.openConnection();
			urlConn.setDoInput(true);
			urlConn.setUseCaches(false);
			ZipInputStream indata= new ZipInputStream(urlConn.getInputStream());
			ZipEntry entry;
			OutputStream out = new FileOutputStream(path+".tmp");
			ZipOutputStream zipOut = new ZipOutputStream(out);
			while ((entry = indata.getNextEntry()) != null)
			{
				String nameFile = entry.getName();
				if (nameFile.equalsIgnoreCase("Info"))
				{
					BufferedReader filebuffered= new BufferedReader(new InputStreamReader(indata));
					String line=filebuffered.readLine();
					String [] temp=line.split("\t");
					information[3]=temp[3];
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
					zipOut.putNextEntry(new ZipEntry("Document"));
					int accessed=0;
					byte[] buffer=new byte[1024];
					while((accessed=indata.read(buffer))!=-1)
					{
						zipOut.write(buffer,0 , accessed);
					}
					zipOut.closeEntry();
				}
	        }
			indata.close();
			zipOut.finish();
			zipOut.close();
			out.close();
			new File(path).delete();
			new File(path+".tmp").renameTo(new File(path));
		}
		catch (Exception e)
		{
			return "0 %236% ("+path+")<br>\n";
		}
		return "1 %235% ("+path+")<br>\n";
	}
}
