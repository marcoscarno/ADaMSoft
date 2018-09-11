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

/**
* This class iterates over the parameters required to write a data table and gives back the information on them
* @author marco.scarno@gmail.com
* @date 07/09/2015
*/
public class GetSettingParameters
{
	/**
	*This is the name of the parameter
	*/
	private String name;
	/**
	*If true means that the parameter is mandatory
	*/
	private boolean mandatory;
	/**
	*This is the label that specifies the parameter meaning (used in the GUI)
	*/
	private int label;
	/**
	*Set a new table parameter
	*/
	public GetSettingParameters (String name, boolean mandatory, int label)
	{
		this.name = name;
		this.mandatory = mandatory;
		this.label = label;
	}
	/**
	*Gets the label of the parameter
	*/
	public int getLabel()
	{
		return label;
	}
	/**
	*Returns true if the parameter is mandatory
	*/
	public boolean isMandatory()
	{
		return mandatory;
	}
	/**
	*Returns the name of the parameter
	*/
	public String getName()
	{
		return name;
	}
}
