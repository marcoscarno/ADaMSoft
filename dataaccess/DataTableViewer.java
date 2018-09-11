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

import ADaMSoft.utilities.ADaMSoftFunctions;

/**
* This is the interface to view a data table
* @author marco.scarno@gmail.com
* @date 13/02/2017
*/
public abstract class DataTableViewer extends ADaMSoftFunctions
{
	/**
	*Sets the required parameters
	*/
	public abstract void setparameters(Hashtable<String, Object> oldparameters);
	/**
	*Opens a data table
	*/
	public abstract boolean open (Hashtable<String, String> tableinfo,
	Vector<Hashtable<String, String>> fixedvariableinfo,
	Vector<Hashtable<String, String>> tablevariableinfo);
	/**
	*Gets a record
	*/
	public abstract String[] getRecord();
	/**
	*True if it is the last record
	*/
	public abstract boolean isLast();
	/**
	*Close the opened data table
	*/
	public abstract boolean close();
	/**
	*Delete the data table
	*/
	public abstract boolean deletetable();
	/**
	*Returns the total number of the records stored in the data table
	*/
	public abstract int getRecords(Hashtable<String, String> tableinfo);
	/**
	*Returns the error message
	*/
	public abstract String getMessage();
}
