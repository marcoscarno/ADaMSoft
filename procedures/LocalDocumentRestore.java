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

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.Serializable;

import ADaMSoft.keywords.Keywords;
import ADaMSoft.utilities.ADaMSdoc;

/**
* Restore locally, a document
* @author marco.scarno@gmail.com
* @date 13/02/2017
*/
public class LocalDocumentRestore implements StepResult, Serializable
{
	/**
	 *This is the default serial version UID
	 */
	private static final long serialVersionUID = 1L;
	/**
	*This is the dimension of the buffer
	*/
	final static int buffSize= 512;
	/**
	*This contains the document
	*/
	ADaMSdoc doc;
	/**
	*This contains the full path (path and name) of the document that will be restored
	*/
	String path;
	/**
	*This contains the name of the document that will be created
	*/
	String outname;
	/**
	*Constructor
	*/
	public LocalDocumentRestore(String path, String outname, ADaMSdoc doc)
	{
		this.path=path;
		this.doc=doc;
		this.outname=outname;
	}
	/*Not used*/
	public void exportOutput()
	{
	}
	/**
	*Write the document that is stored in a InputStream.<p>
	*Such InputStream is returned by using doc.getData().
	*/
	public String action()
	{
		if (path==null)
			path=System.getProperty(Keywords.WorkDir);
		path=path+outname;
		byte[] buffer=new byte[buffSize];
		try
		{
			String password = System.getProperty(Keywords.docpwd);
			if (password==null)
				password="";
			InputStream in;
			if(password.equals(""))
				in= doc.getData();
			else
				in=doc.getData(password);
			DataOutputStream out = new DataOutputStream(new FileOutputStream(path));
			if (in==null)
			{
				out.close();
				return "1 %86% ("+path+")<br>\n";
			}
			int accessed=0;
			while((accessed=in.read(buffer))!=-1)
			{
				out.write(buffer,0 , accessed);
			}
			out.close();
			in.close();
			return "1 %86% ("+path+")<br>\n";
		}
		catch(Exception e)
		{
			return "0 %84%<br>\n";
		}
	}
}
