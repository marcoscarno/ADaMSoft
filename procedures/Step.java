/**
* Copyright © 2017 MS
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

import java.util.Hashtable;

/**
* This is the generic method to execute a step.
* @author marco.scarno@gmail.com
* @version 1.0.0, rev.: 13/02/17 by marco
*/
public interface Step
{
	/**
	* This is the method to execute a step; it returns object/s from the class Result
	*/
	Result executionresult(Hashtable<String, Object> parameters);
}
