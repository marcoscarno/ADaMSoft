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

import java.io.Serializable;
import java.io.BufferedWriter;
import java.io.FileWriter;

import ADaMSoft.keywords.Keywords;

/**
 * This class returns a message written into the StepResult by a procedure
 * @author marco.scarno@gmail.com
 * @date 13/02/2017
 */
public class LocalOutputGetter implements StepResult, Serializable
{
	/**
	 * This is the defult serial version UID
	 */
	private static final long serialVersionUID = 1L;
	/**
	*This is the message that will be returned
	*/
	String msg;
	/**
	*Constructor
	*/
	public LocalOutputGetter(String msg)
	{
		this.msg=msg;
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
		try
		{
			Keywords.semwriteOut.acquire();
	        BufferedWriter outwriter = new BufferedWriter(new FileWriter(System.getProperty("out_outfile"),true));
	        outwriter.write(msg);
	        outwriter.close();
			Keywords.semwriteOut.release();
		}
		catch (Exception eee){}
		return "";
	}
}
