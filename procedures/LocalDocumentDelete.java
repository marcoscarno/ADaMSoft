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

import java.io.File;
import java.io.Serializable;

import ADaMSoft.keywords.Keywords;

/**
 * This class deletes, locally, a document
 * @author marco.scarno@gmail.com
 * @date 13/02/2017
 */
public class LocalDocumentDelete implements StepResult, Serializable
{
	/**
	 * This is the default serial versione UID
	 */
	private static final long serialVersionUID = 1L;
	/**
	*This is the path of the document to delete
	*/
	String path;
	/**
	*This is the name of the document
	*/
	String name;
	/**
	*Constructor
	*/
	public LocalDocumentDelete(String path, String documentname)
	{
		this.path=path;
		this.name=documentname;
	}
	/*Not used*/
	public void exportOutput()
	{
	}
	/**
	*Delete the document
	*/
	public String action()
	{
		if (path==null)
			path=System.getProperty(Keywords.WorkDir);
		path=path+name;

		if(!path.endsWith(Keywords.DocExtension))
			path=path+Keywords.DocExtension;

		boolean documentdeleted=(new File(path)).delete();
		if (documentdeleted)
			return "1 %193% ("+path+")<br>\n";
		else
			return "0 %194% ("+path+")<br>\n";
	}
}
