/**
* Copyright (c) 2015 MS
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

package ADaMSoft.dataaccess;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.zip.ZipInputStream;

/**
* This is the method that reads the record by record from a compressed file (used in DataSorter)
* @author marco.scarno@gmail.com
* @date 03/09/2015
*/
public class SerializedObjectsRecordsFileIterator implements RecordFilesIterator
{
	private ZipInputStream fis=null;
	private ObjectInputStream ois=null;
	private String[] tempval;
	private boolean isclosed;

	/**
	 *Receives the file Path to the files with serialized objects
	 */
	public SerializedObjectsRecordsFileIterator(String file) throws IOException
	{
		fis=new ZipInputStream(new FileInputStream(file));
		fis.getNextEntry();
		ois=new ObjectInputStream(new BufferedInputStream(fis, 2048));
		isclosed=false;
	}
	/**
	 *Iterates over the record of the file
	 */
	public boolean hasNext()
	{
		try
		{
			return (fis.available()!=0);
		}
		catch (Exception e) {}
		return false;
	}
	/**
	*Returns the current record in the Object
	*/
	public Object next(int totalvar)
	{
		Object record=null;
		try
		{
			tempval=new String[totalvar];
			for (int i=0; i<totalvar; i++)
			{
				tempval[i]=(ois.readObject()).toString();
			}
			record=tempval;
			return record;
		}
		catch(Exception ex) {}
		return null;
	}
	/**
	* Close the opened file
	*/
	public void close() throws IOException
	{
		if (!isclosed)
		{
			ois.close();
			fis.close();
			ois=null;
			fis=null;
			System.gc();
			isclosed=true;
		}
	}
}
