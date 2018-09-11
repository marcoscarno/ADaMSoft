/**
* Copyright © 2006-2009 CASPUR
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

import java.util.LinkedList;

import ADaMSoft.utilities.GetRequiredParameters;

/**
* This is the method to execute a step.
* @author marco.scarno@caspur.it; c.trani@aspur.it
* @version 1.0.0, rev.: 29/01/09 by marco
*/
public interface RunStep extends Step
{
	/**
	* This is the method that returns the list of required parameter for each step
	*/
	LinkedList<GetRequiredParameters> getparameters();
	/**
	* This is the method that returns the group to which each step belongs
	*/
	String[] getstepinfo();
}
