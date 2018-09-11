/**
* Copyright (c) MS
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
* This class contains methods to apply the adamsoft standard file name
* @author marco.scarno@gmail.com
* @date 02/09/2015
*/

public class AdamsCompliant
{
	public String toAdamsFormat(String path)
	{
		String separator=System.getProperty("file.separator");
		if(!path.toLowerCase().startsWith("http://"))
		{
			separator=System.getProperty("file.separator");
		}
		String file = "";
		if (path.lastIndexOf(".")<=0)
			file = path.substring(path.lastIndexOf(separator)+1);
		else
			file = path.substring(path.lastIndexOf(separator)+1,path.lastIndexOf("."));

		file = file.toLowerCase();
		String firstChar=file.substring(0,1);
		file = file.replaceFirst(firstChar,firstChar.toUpperCase());

		String dir="";
		if (path.lastIndexOf(separator)>=0)
			dir = path.substring(0,path.lastIndexOf(separator)+1);

		String ext="";
		if (path.lastIndexOf(".")>0)
			ext = path.substring(path.lastIndexOf("."));
		return dir+file+ext;
	}
}
