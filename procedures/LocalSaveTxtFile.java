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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.Serializable;

import ADaMSoft.keywords.Keywords;

/**
 * This class writes locally an html file
 * @author marco.scarno@gmail.com
 * @date 13/02/2017
 */
public class LocalSaveTxtFile implements StepResult, Serializable
{
	/**
	 * This is the defult serial version UID
	 */
	private static final long serialVersionUID = 1L;
	String content, path, name;
	/**
	*Constructor
	*/
	public LocalSaveTxtFile(String content, String path, String name)
	{
		this.content=content;
		this.path=path;
		this.name=name;
	}
	/*Not used*/
	public void exportOutput()
	{
	}
	/**
	*Returns the message
	*/
	public String action()
	{
		BufferedWriter fileouthtml=null;
		if (path.equals(Keywords.WorkDir))
			path=System.getProperty(Keywords.WorkDir);
		String outreport=path+name;
		boolean exist=(new File(outreport)).exists();
		if (exist)
		{
			boolean success = (new File(outreport)).delete();
			if (!success)
				return "0 %2488%<br>\n";
		}
		try
		{
			fileouthtml = new BufferedWriter(new FileWriter(outreport, true));
		}
		catch (Exception e)
		{
			return "1 %2489%<br>\n";
		}
		try
		{
			fileouthtml.write(content);
			content="";
			fileouthtml.close();
			return "0 %2490% ("+outreport+")<br>\n";
		}
		catch (Exception e)
		{
			return "0 %2491%\n"+e.toString()+"<br>\n";
		}
	}
}
