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

package ADaMSoft.dataaccess;

import java.io.Serializable;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Vector;
import java.io.File;

/**
* This class implements a random access two-dimensional array by using a temporary files stored on disk
* and sorted by group
* @author marco.scarno@gmail.com
* @date 13/02/2017
*/
public class GroupedMatrix2Dfile implements Serializable
{
	protected static final long serialVersionUID = 1L;
	protected Hashtable<Vector<String>, Matrix2DFile> container;
	protected int vars;
	protected String message;
	protected String workdir;
	protected Matrix2DFile basefile;
	/**
	 * Constructor
	 * @param workdir: Working directory were store the file
	 * @param var: Number of variable, i.e. the number of columns of the array
	 */
	public GroupedMatrix2Dfile(String workdir, int var)
	{
		basefile=null;
		container = new Hashtable<Vector<String>, Matrix2DFile>();
		this.vars=var;
		message = "";
		this.workdir=workdir;
	}
	/**
	 * Append an array to the grouped file, the array must have the same elements of variable var
	 * @param values: The array to append
	 * @return True if the operation is completed successfully, false otherwise
	 */
	public boolean write(Vector<String> groupValues, double[] values)
	{
		if (basefile!=null)
		{
			if(!basefile.write(values))
			{
				message=basefile.getMessage();
				basefile=null;
				return false;
			}
			return true;
		}
		else
		{
			Matrix2DFile ar = getMatrix2DFileFromKey(groupValues);
			if(ar==null)
			{
				return false;
			}
			if(!ar.write(values))
			{
				message=ar.getMessage();
				return false;
			}
			return true;
		}
	}
	/**
	 * Write a value into the group-array, but control that the indicies are
	 * compliant whith the current dimention of array
	 * @param value: The value to write
	 * @param column: The column where wrte the value
	 * @param row: The row were write the value
	 * @return True if the operation is completed successfully, false otherwise
	 */
	public boolean safewrite(Vector<String> groupValues, double value, int row, int column)
	{
		if (basefile==null)
		{
			Matrix2DFile ar = getMatrix2DFileFromKey(groupValues);
			if(ar== null)
			{
				return false;
			}
			if(!ar.safewrite(value, row, column))
			{
				message=ar.getMessage();
				return false;
			}
			return true;
		}
		else
		{
			if(!basefile.safewrite(value, row, column))
			{
				message=basefile.getMessage();
				basefile=null;
				return false;
			}
			return true;
		}
	}
	/**
	 * Write a value into the group-array, but do not control that the indicies are
	 * compliant whith the current dimention of array
	 * @param value: The value to write
	 * @param column: The column where wrte the value
	 * @param row: The row were write the value
	 * @return True if the operation is completed successfully, false otherwise
	 */
	public boolean write(Vector<String> groupValues, double value, int row, int column)
	{
		if (basefile!=null)
		{
			if(!basefile.write(value, row, column))
			{
				message=basefile.getMessage();
				basefile=null;
				return false;
			}
		}
		else
		{
			Matrix2DFile ar = getMatrix2DFileFromKey(groupValues);
			if(ar== null)
			{
				return false;
			}
			if(!ar.write(value, row, column))
			{
				message=ar.getMessage();
				return false;
			}
		}
		return true;
	}
	/**
	 * Write a row at specified index, but control that the indicies are
	 * compliant whith the current dimention of array
	 * @param values: The values to write
	 * @param index: The index where write the values
	 * @return True if the operation is completed successfully, false otherwise
	 */
	public boolean safewrite(Vector<String> groupValues, double[] values, int index)
	{
		if (basefile==null)
		{
			Matrix2DFile ar = getMatrix2DFileFromKey(groupValues);
			if(ar== null)
			{
				return false;
			}
			if(!ar.safewrite(values, index))
			{
				message=ar.getMessage();
				return false;
			}
			return true;
		}
		else
		{
			if(!basefile.safewrite(values, index))
			{
				message=basefile.getMessage();
				basefile=null;
				return false;
			}
			return true;
		}
	}
	/**
	 * Write a row at specified index
	 * @param values: The values to write
	 * @param index: The index where write the values
	 * @return True if the operation is completed successfully, false otherwise
	 */
	public boolean write(Vector<String> groupValues, double[] values, int index)
	{
		if (basefile==null)
		{
			Matrix2DFile ar = getMatrix2DFileFromKey(groupValues);
			if(ar== null)
			{
				return false;
			}
			if(!ar.write(values, index))
			{
				message=ar.getMessage();
				return false;
			}
			return true;
		}
		else
		{
			if(!basefile.write(values, index))
			{
				message=basefile.getMessage();
				basefile=null;
				return false;
			}
			return true;
		}
	}
	/**
	*Return the number of rows that were written
	*/
	public int getRows(Vector<String> groupValues)
	{
		if (basefile==null)
		{
			Matrix2DFile ar = getMatrix2DFileFromKey(groupValues);
			if(ar== null)
				return 0;
			return ar.getRows();
		}
		else
		{
			return basefile.getRows();
		}
	}
	/**
	*Return the number of columns that were written
	*/
	public int getColumns()
	{
		return vars;
	}

	/**
	 * Read a values at the specified indices
	 * @param column: The column where wrte the value
	 * @param row: The row were read the value
	 * @return The value read or NaN if an error occurs
	 */
	public double read(Vector<String> groupValues, int row, int column)
	{
		if (basefile!=null)
		{
			return basefile.read(row, column);
		}
		else
		{
			Matrix2DFile ar = getMatrix2DFileFromKey(groupValues);
			if(ar== null)
			{
				return Double.NaN;
			}
			return ar.read(row, column);
		}
	}
	/**
	 * Read a row of values at the specified indices
	 * @param row: The row were read the value
	 * @return The value read or null if an error occurs
	 */
	public double[] readRow(Vector<String> groupValues, int row)
	{
		if (basefile==null)
		{
			Matrix2DFile ar = getMatrix2DFileFromKey(groupValues);
			if(ar== null)
			{
				return null;
			}
			double[] values = ar.readRow(row);
			return values;
		}
		else
		{
			double[] values = basefile.readRow(row);
			return values;
		}
	}
	/**
	 * Return a message if an error occurs
	 * @return The (eventual) error message
	 */
	public String getMessage()
	{
		return message;
	}
	/**
	 * Close the stream associated to the group and cancel the temporary file
	 * @return True if the operation is completed successfully, false otherwise
	 */
	public boolean close(Vector<String> groupValues)
	{
		basefile=null;
		boolean resdel=getMatrix2DFileFromKey(groupValues).close();
		if (!resdel)
		{
			Matrix2DFile m2df=getMatrix2DFileFromKey(groupValues);
			String filen=m2df.getfilename();
			m2df=null;
			System.gc();
			container.remove(groupValues);
			return (new File(filen)).delete();
		}
		container.remove(groupValues);
		return resdel;
	}
	/**
	 * Close all streams and cancel the temporary file
	 * @return True if the operation is completed successfully, false otherwise
	 */
	public boolean closeAll()
	{
		basefile=null;
		boolean result = true;
		for (Enumeration<Vector<String>> e = container.keys() ; e.hasMoreElements() ;)
		{
			Vector<String> par =e.nextElement();
			if (!(container.get(par)).close())
			{
				Matrix2DFile m2df=getMatrix2DFileFromKey(par);
				String filen=m2df.getfilename();
				m2df=null;
				System.gc();
				boolean tempresult=(new File(filen)).delete();
				int counter=0;
				while (!tempresult)
				{
					tempresult=(new File(filen)).delete();
					if (counter==100)
						tempresult=true;
					counter++;
				}
			}
		}
		if(!result)
		{
			message = "%933%<br>\n";
		}
		container.clear();
		return result;
	}
	/**
	*Gives back the matrix2dfile to use
	*/
	protected Matrix2DFile getMatrix2DFileFromKey(Vector<String> groupValues)
	{
		Vector<String> newg=new Vector<String>();
		for (int i=0; i<groupValues.size(); i++)
		{
			newg.add(groupValues.get(i));
		}
		if(container.get(newg)!=null)
		{
			Matrix2DFile ar = container.get(newg);
			return ar;
		}
		else
		{
			Matrix2DFile ar = new Matrix2DFile(workdir, vars);
			if(!ar.getMessage().equals(""))
			{
				message = ar.getMessage();
				return null;
			}
			container.put(newg,ar);
			return ar;
		}
	}
	/**
	*Return false if the grouped file does not exist, otherwise true
	*/
	public boolean existgroupfile(Vector<String> newg)
	{
		if(container.get(newg)==null)
			return false;
		return true;
	}
	/**
	*Assing the default file
	*/
	public boolean assignbasefile(Vector<String> groupValues)
	{
		Vector<String> newg=new Vector<String>();
		for (int i=0; i<groupValues.size(); i++)
		{
			newg.add(groupValues.get(i));
		}
		if(container.get(newg)==null)
			return false;
		basefile=container.get(newg);
		return true;
	}
	/**
	*Deassign the basefile
	*/
	public void deassignbasefile()
	{
		basefile=null;
	}
}
