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

/**
* This is the matrix2d file interface
* @author marco.scarno@gmail.com
* @date 03/09/2015
*/
public interface Matrix2D {

	/**
	 * This method return the name of the file
	 */
	public abstract String getfilename();
	/**
	 * This method return a message if an error occurs
	 * @return The (eventual) error message
	 */
	public abstract String getMessage();

	/**
	 * This method close the stream and cancel the temporary file
	 * @return True if the operation is completed successfully, false otherwise
	 */
	public abstract boolean close();

	/**
	 * This method write a value into the array, but control that the indicies are
	 * compliant whith the current dimention of array
	 * @param value: The value to write
	 * @param column: The column where wrte the value
	 * @param row: The row were write the value
	 * @return True if the operation is completed successfully, false otherwise
	 */
	public abstract boolean safewrite(double value, int row, int column);
	public abstract boolean safewrite(double[] value, int index);

	/**
	 * This method write a value into the array, but do not control that the indicies are
	 * compliant whith the current dimention of array
	 * @param value: The value to write
	 * @param column: The column where wrte the value
	 * @param row: The row were write the value
	 * @return True if the operation is completed successfully, false otherwise
	 */
	public abstract boolean write(double value, int row, int column);
	public abstract boolean write(double[] values, int index);
	public abstract boolean write(double[] values);
	/**
	*Return the double at row, column
	*/
	public abstract double read(int row, int column);
	public abstract double[] readRow(int row);
	/**
	*Return the number of written records
	*/
	public abstract int getRows();
	/**
	*Return the number columns
	*/
	public abstract int getColumns();

}