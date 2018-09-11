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

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;

/**
* This class creates a compressed file for the dstransport or pathtransport
* @author marco.scarno@gmail.com
* @date 13/02/2017
*/
public class LocalTransport implements StepResult, Serializable
{
	/**
	 * This is the default static version UID
	 */
	private static final long serialVersionUID = 1L;
	private String filename;
	private byte[] buffer;
	/*Not used*/
	public void exportOutput()
	{
	}
	/**
	*Create locally the compressed file with the data set
	*/
	public String action()
	{
		try
		{
			BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(filename));
			out.write(buffer);
			out.close();
			return "1 %501% ("+filename+")<br>\n";
		}
		catch (IOException e)
		{
			return "0 %500%<br>\n";
		}
	}
	/**
	*Used to write directly the compressed file
	*/
	public LocalTransport(String filename, byte[] buff)
	{
		this.filename = filename;
		buffer = buff;
	}
}
