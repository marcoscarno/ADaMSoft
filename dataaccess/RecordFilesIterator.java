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

import java.io.IOException;

/**
* This is the interface that iterates on the records of a file
* @author marco.scarno@gmail.com
* @date 03/09/2015
*/
public interface RecordFilesIterator
{
	/**
	 *Return true if there is more records to read
	 */
	public abstract boolean hasNext();
	/**
	 *Return the next available record
	 */
	public abstract Object next(int totalvar);
	/**
	 *Close all file handlers used by this file iterator (must be used when iteration was complete).
	 */
	public abstract void close() throws IOException;
}