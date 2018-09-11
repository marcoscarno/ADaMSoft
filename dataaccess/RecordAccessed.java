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
* This class contains the record accessed
* @author marco.scarno@gmail.com
* @date 06/09/2015
*/
public class RecordAccessed
{
	String[] rec;
	boolean islast;
	public RecordAccessed(Object[] record, boolean islast)
	{
		if (record!=null)
		{
			rec =new String[record.length];
			for (int i=0; i<record.length; i++)
			{
				rec[i]=record[i].toString().trim();
			}
		}
		else rec=null;
		this.islast=islast;
	}
	public String[] getRecord()
	{
		return rec;
	}
	public boolean getIsLast()
	{
		return islast;
	}
}