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
import java.io.Serializable;

import ADaMSoft.utilities.AdamsCompliant;
import ADaMSoft.keywords.Keywords;

/**
 * This class writes, locally, a file
 * @author marco.scarno@gmail.com
 * @date 13/02/2017
 */

public class LocalFileSave extends AdamsCompliant implements StepResult, Serializable
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
	byte[] file;
	/**
	*This is the path
	*/
	String path;
	/**
	*Constructor
	*/
	public LocalFileSave(String path, byte[] doc)
	{
		this.path=path;
		this.file=doc;
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
		if (path.startsWith(Keywords.WorkDir))
			path=System.getProperty(Keywords.WorkDir)+path.substring(Keywords.WorkDir.length());
		path=toAdamsFormat(path);
		try
		{
			FileOutputStream out = new FileOutputStream(path);
			out.write(file);
			out.close();
			file=new byte[0];
			file=null;
			System.gc();
		}
		catch (FileNotFoundException e)
		{
			file=new byte[0];
			file=null;
			System.gc();
			return "0 %790%<br>\n";
		}
		catch (IOException e)
		{
			file=new byte[0];
			file=null;
			System.gc();
			return "0 %790%<br>\n";
		}
		catch (Exception e)
		{
			file=new byte[0];
			file=null;
			System.gc();
			return "0 %790%<br>\n";
		}
		return "1 %791% ("+path+")<br>\n";
	}
}
