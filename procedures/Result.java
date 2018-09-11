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

import java.util.Vector;
import java.io.Serializable;

/**
* This class contains the results of the steps execution
* @author c.trani@caspur.it
* @version 1.0.0, rev.: 29/01/09 by marco
*/
public class Result implements Serializable
{
	/**
	 * This is the default serial version UID
	 */
	private static final long serialVersionUID = 1L;
	/**
	*Contains the string of the message
	*/
	private String message;
	/**
	*False if the execution failed, else true
	*/
	private boolean flag;
	/**
	*Vector that contains the different kind of the results
	*/
	Vector<StepResult> results;
	/**
	*Constructor
	*/
	public Result(String mess, boolean flag, Vector<StepResult> res)
	{
		message=mess;
		this.flag=flag;
		results=res;
	}
	/**
	*Returns the error
	*/
	public boolean isCorrect()
	{
		return flag;
	}
	/**
	*Gets the message
	*/
	public String getMessage()
	{
		return message;
	}
	/**
	*Gets the results
	*/
	public Vector<StepResult> getResults()
	{
		return results;
	}
}
