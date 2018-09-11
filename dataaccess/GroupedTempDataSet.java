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

import java.io.Serializable;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Vector;
import java.io.File;

/**
* This class implements a random access two-dimensional array of String by using a temporary files stored on disk
* @author marco.scarno@gmail.com
* @date 03/09/2015
*/
public class GroupedTempDataSet implements Serializable
{
	protected static final long serialVersionUID = 1L;
	protected Hashtable<Vector<String>, TempDataSet> container;
	protected int vars;
	protected String message;
	protected String workdir;
	protected TempDataSet basefile;
	/**
	 * Constructor
	 * @param workdir: Working directory were store the file
	 * @param var: Number of variable, i.e. the number of columns of the array
	 */
	public GroupedTempDataSet(String workdir, int var)
	{
		basefile=null;
		container = new Hashtable<Vector<String>, TempDataSet>();
		this.vars=var;
		message = "";
		this.workdir=workdir;
	}
	/**
	 * Append an array of String to the grouped file, the array must have the same elements of variable var
	 * @param values: The array to append
	 * @return True if the operation is completed successfully, false otherwise
	 */
	public boolean write(Vector<String> groupValues, String[] values)
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
			TempDataSet ar = getTempDataSetFromKey(groupValues);
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
	*Return the number of records when a variable represents a record weight
	*/
	public double getweight(Vector<String> groupValues, int wvar)
	{
		if (basefile==null)
		{
			TempDataSet ar = getTempDataSetFromKey(groupValues);
			if(ar== null)
				return 0;
			return ar.getweight(wvar);
		}
		else
		{
			return basefile.getweight(wvar);
		}
	}
	/**
	*Return the number of records when a variable represents a record weight
	*/
	public double getweight(Vector<String> groupValues, int wvar, int rifvar)
	{
		if (basefile==null)
		{
			TempDataSet ar = getTempDataSetFromKey(groupValues);
			if(ar== null)
				return 0;
			return ar.getweight(wvar, rifvar);
		}
		else
		{
			return basefile.getweight(wvar, rifvar);
		}
	}

	/**
	*Return the number of rows that were written
	*/
	public int getRows(Vector<String> groupValues)
	{
		if (basefile==null)
		{
			TempDataSet ar = getTempDataSetFromKey(groupValues);
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
	*Sort all the temporary data set
	*/
	public boolean sortAll(int usevars)
	{
		basefile=null;
		for (Enumeration<Vector<String>> e = container.keys() ; e.hasMoreElements() ;)
		{
			Vector<String> par =e.nextElement();
			if (!sortdataset(par, usevars))
			{
				message = "%1797%\n";
				return false;
			}
		}
		return true;
	}
	/**
	*Sorts a single temporary data set
	*/
	public boolean sortdataset(Vector<String> groupValues, int usevars)
	{
		if (basefile==null)
		{
			TempDataSet ar = getTempDataSetFromKey(groupValues);
			if(ar== null)
				return false;
			return ar.sortdata(usevars);
		}
		else
		{
			return basefile.sortdata(usevars);
		}
	}
	/**
	*Return true if it is the last record of the temporary data set
	*/
	public boolean isLast(Vector<String> groupValues)
	{
		if (basefile==null)
		{
			TempDataSet ar = getTempDataSetFromKey(groupValues);
			if(ar== null)
				return false;
			return ar.isLast();
		}
		else
		{
			return basefile.isLast();
		}
	}
	/**
	*Return true in case of an error in writing one of the temporary data set
	*/
	public boolean geterrors()
	{
		message="";
		basefile=null;
		for (Enumeration<Vector<String>> e = container.keys() ; e.hasMoreElements() ;)
		{
			Vector<String> par =e.nextElement();
			TempDataSet ar = getTempDataSetFromKey(par);
			if(ar==null)
				return true;
			if (ar.geterror())
			{
				message=ar.getMessage();
				return true;
			}
		}
		return false;
	}

	/**
	*Sort all the temporary data set
	*/
	public boolean finalizeWriteAll()
	{
		basefile=null;
		for (Enumeration<Vector<String>> e = container.keys() ; e.hasMoreElements() ;)
		{
			Vector<String> par =e.nextElement();
			if (!finalizeWrite(par))
			{
				message = "%1798%<br>\n";
				return false;
			}
		}
		return true;
	}
	/**
	*Close a temporary data set in order to start to read its values
	*/
	public boolean finalizeWrite(Vector<String> groupValues)
	{
		if (basefile==null)
		{
			TempDataSet ar = getTempDataSetFromKey(groupValues);
			if(ar== null)
				return false;
			if (!ar.finalizeWrite())
			{
				message=ar.getMessage();
				return false;
			}
		}
		else
		{
			if (!basefile.finalizeWrite())
			{
				message=basefile.getMessage();
				return false;
			}
		}
		return true;
	}
	/**
	*Return the number of columns that were written
	*/
	public int getColumns()
	{
		return vars;
	}
	/**
	 * Read a row of values
	 * @param row: The row were read the value
	 * @return The value read or null if an error occurs
	 */
	public String[] read(Vector<String> groupValues)
	{
		if (basefile==null)
		{
			TempDataSet ar = getTempDataSetFromKey(groupValues);
			String[] values =new String[vars];
			if(ar== null)
			{
				for (int i=0; i<vars; i++)
				{
					values[i]="";
				}
				return values;
			}
			values = ar.read();
			return values;
		}
		else
		{
			String[] values = basefile.read();
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
	public boolean deletetempdata(Vector<String> groupValues)
	{
		basefile=null;
		boolean resdel=getTempDataSetFromKey(groupValues).deletetempdata();
		if (!resdel)
		{
			TempDataSet m2df=getTempDataSetFromKey(groupValues);
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
	public boolean deletetempdataAll()
	{
		basefile=null;
		boolean result = true;
		for (Enumeration<Vector<String>> e = container.keys() ; e.hasMoreElements() ;)
		{
			Vector<String> par =e.nextElement();
			if (!(container.get(par)).deletetempdata())
			{
				TempDataSet m2df=getTempDataSetFromKey(par);
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
			message = "%1804%<br>\n";
		}
		container.clear();
		return result;
	}

	/**
	 * Close the stream associated to the group and cancel the temporary file
	 * @return True if the operation is completed successfully, false otherwise
	 */
	public void endread(Vector<String> groupValues)
	{
		getTempDataSetFromKey(groupValues).endread();
	}
	/**
	 * Close all streams and cancel the temporary file
	 * @return True if the operation is completed successfully, false otherwise
	 */
	public void endreadAll()
	{
		for (Enumeration<Vector<String>> e = container.keys() ; e.hasMoreElements() ;)
		{
			Vector<String> par =e.nextElement();
			container.get(par).endread();
		}
	}

	/**
	*Gives back the TempDataSet to use
	*/
	protected TempDataSet getTempDataSetFromKey(Vector<String> groupValues)
	{
		Vector<String> newg=new Vector<String>();
		for (int i=0; i<groupValues.size(); i++)
		{
			newg.add(groupValues.get(i));
		}
		if(container.get(newg)!=null)
		{
			TempDataSet ar = container.get(newg);
			return ar;
		}
		else
		{
			TempDataSet ar = new TempDataSet(workdir, vars);
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
