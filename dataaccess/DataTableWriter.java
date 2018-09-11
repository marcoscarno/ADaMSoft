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

import java.util.Hashtable;
import java.util.Vector;
import java.util.LinkedList;

import ADaMSoft.utilities.GetSettingParameters;
/**
* This is the interface to write a data table
* @author marco.scarno@gmail.com
* @date 13/02/2017
*/
public interface DataTableWriter
{
	/**
	*Gets the hashtable of the required parameters to initializes the data table
	*/
	public LinkedList<GetSettingParameters> initialize();
	/**
	*This initializes the data table.<p>
	*Returns true if the initialization is ok, otherwise false.<p>
	*/
	public boolean open(Hashtable<String, String> tableinfo, Vector<Hashtable<String, String>> tempvarinfo);
	/**
	*Write the string array of values.<p>
	*Returns false if the write phase didn't go ok.
	*/
	public boolean writevalues(String[] values);
	/**
	*Close the data table<p>
	*Returns false if the close operation didn't go ok.
	*/
	public boolean close();
	/**
	*Returns the error message
	*/
	public String getmessage();
	/**
	*Returns the information on the data table that will be written in the dictionary associated to the data table
	*/
	public Hashtable<String, String> getTableInfo();
	/**
	*Each element of the returned vector contains the hashtable of the variable information that will be written in the dictionary associated to the data table.<p>
	*The number of element of such vector is equal to the number of created variables.
	*/
	public Vector<Hashtable<String, String>> getVariablesInfo();
	/**
	*Delete temporary file<p>
	*Returns false if the deletion was not possible
	*/
	public boolean deletetmp();
}
