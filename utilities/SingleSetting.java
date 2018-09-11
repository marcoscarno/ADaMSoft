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

package ADaMSoft.utilities;

import java.util.Enumeration;
import java.util.Hashtable;
import java.io.Serializable;

/**
* This class is used to add the setting parameters
* @author marco.scarno@gmail.com
* @date 07/09/2015
*/
public class SingleSetting implements Serializable
{
	/**
	 * This is the default static version UID
	 */
	private static final long serialVersionUID = 1L;
	/**
	*Contains the type and the name of each SETTING
	*/
	String type, name;
	/**
	*Contains the parameters for each SETTING
	*/
	Hashtable<String, String> parameters;
	/**
	*Initializes the object that will contains the parameters for each SETTING
	*/
	public SingleSetting(String type, String name)
	{
		this.type = type;
		this.name = name;
		parameters = new Hashtable<String, String>();
	}
	/**
	*Returns the hashtable that contains all the parameters
	*/
	public Hashtable<String, String> getParameters()
	{
		return parameters;
	}
	/**
	*Adds a parameter, and a value, to a SETTING
	*/
	public void addParameter(String parameter, String value)
	{
		parameters.put(parameter, value);
	}
	/**
	*Adds a parameter to the passed hashtable
	*/
	public void addParameter(Hashtable<String, String> hashtable)
	{
		for (Enumeration<String> e = hashtable.keys() ; e.hasMoreElements() ;)
		{
			String actualparameter=((String) e.nextElement());
			String actualvalue=hashtable.get(actualparameter);
			parameters.put(actualparameter, actualvalue);
		}
	}
	/**
	*Returns the name of the SETTING
	*/
	public String getName()
	{
		return name;
	}
	/**
	*Returns the type of the SETTING
	*/
	public String getType()
	{
		return type;
	}
	/**
	*Implements the equals method
	*/
	public boolean equals(Object o)
	{
		if(((SingleSetting)o).name.equalsIgnoreCase(name) && ((SingleSetting)o).type.equalsIgnoreCase(type))
		{
			return true;
		}
		return false;
	}
}