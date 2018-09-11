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

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.File;
import java.io.Serializable;

import ADaMSoft.keywords.Keywords;

/**
 * This class writes, locally, a document
 * @author marco.scarno@gmail.com
 * @date 13/02/2017
 */

public class LocalDocumentSave implements StepResult, Serializable
{
	/**
	 * This is the default serial version UID
	 */
	private static final long serialVersionUID = 1L;
	/**
	*This is the buffer size
	*/
	final static int buffSize= 1024;
	/**
	*This is the document, represented by a byte array
	*/
	byte[] doc;
	/**
	*This is the path
	*/
	String path;
	/**
	*This is the name of the document
	*/
	String name;
	/**
	*If false and the document exist, than an error is returned
	*/
	boolean restore;
	/**
	*Constructor
	*/
	public LocalDocumentSave(boolean restore, String path, String documentname, byte[] doc)
	{
		this.path=path;
		this.doc=doc;
		this.name=documentname;
		this.restore=restore;
	}
	/*Not used*/
	public void exportOutput()
	{
	}
	/**
	*Save the document
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
			if ((!restore) && (testexist))
				return "0 %85%<br>\n";
			FileOutputStream out = new FileOutputStream(path);
			out.write(doc);
			out.close();
			doc=new byte[0];
			doc=null;
			System.gc();
		}
		catch (FileNotFoundException e)
		{
			doc=new byte[0];
			doc=null;
			System.gc();
			return "0 %81%<br>\n";
		}
		catch (IOException e)
		{
			doc=new byte[0];
			doc=null;
			System.gc();
			return "0 %81%<br>\n";
		}
		return "1 %82% ("+path+")<br>\n";
	}
}
