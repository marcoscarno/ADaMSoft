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
* This class contains the detail the ADaMSoft license and of its author
* @author marco.scarno@gmail.com
* @date 07/09/2015
*/
public class License
{
	public License() {}
	public String getLicense()
	{
		String license="<br><br><b>ADaMSoft UserXperience</b><br><br>\n";
		license=license+"Copyright (c) 2015 MS<br><br>\n";
		license=license+"<i>This program is free software; you can redistribute it and/or ";
		license=license+"modify it under the terms of the GNU General Public License ";
		license=license+"as published by the Free Software Foundation; either version 3 ";
		license=license+"of the License, or (at your option) any later version.<br><br>\n";
		license=license+"This program is distributed in the hope that it will be useful, ";
		license=license+"but WITHOUT ANY WARRANTY; without even the implied warranty of ";
		license=license+"MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the ";
		license=license+"GNU General Public License for more details";
		license=license+"<br>";
		license=license+"You should have received a copy of the GNU General Public License along";
		license=license+"with this program. In any case refer to: <http://www.gnu.org/licenses/</i><br><br>";
		return license;
	}
	/**
	*Contains the information on the authors
	*/
	public String getAuthors()
	{
		String license="<br><br><b>ADaMSoft UserXperience</b><br><br>\n";
		license=license+"Copyright (c) 2015 MS<br>\n";
		license=license+"\n";
		license=license+"Creator, main developer maintener: Marco Scarnò (marco.scarno@gmail.com)<br>";
		license=license+"People that in the past collaborated in the development: Cristiano Trani; Antonio Budano<br><br>";
		return license;
	}
}
