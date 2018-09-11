/**
* Copyright (c) 2016 MS
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

import java.util.LinkedList;
import ADaMSoft.keywords.Keywords;

/**
* This class returns the possible keywords and values for each kind of setting
* @author marco.scarno@gmail.com
* @date 25/07/2016
*/
public class SettingParameters
{
	/**
	*Returns a linked list of type GetSetting parameters for the different kind of settings
	*/
	public static LinkedList<GetSettingParameters> getsetpar(String settingtype, String subtype)
	{
		LinkedList<GetSettingParameters> setpar=new LinkedList<GetSettingParameters>();
		if (settingtype.equalsIgnoreCase(Keywords.MYSQL))
		{
			setpar.add(new GetSettingParameters(Keywords.server, true, 429));
			setpar.add(new GetSettingParameters(Keywords.user, true, 430));
			setpar.add(new GetSettingParameters(Keywords.password, false, 431));
			setpar.add(new GetSettingParameters(Keywords.port, false, 432));
			setpar.add(new GetSettingParameters(Keywords.db, true, 695));
		}
		else if (settingtype.equalsIgnoreCase(Keywords.ORACLE))
		{
			setpar.add(new GetSettingParameters(Keywords.server, true, 1698));
			setpar.add(new GetSettingParameters(Keywords.port, false, 1699));
			setpar.add(new GetSettingParameters(Keywords.user, true, 1700));
			setpar.add(new GetSettingParameters(Keywords.password, false, 1701));
			setpar.add(new GetSettingParameters(Keywords.service, false, 1702));
			setpar.add(new GetSettingParameters(Keywords.sid, false, 1724));
		}
		else if (settingtype.equalsIgnoreCase(Keywords.SQLSERVER))
		{
			setpar.add(new GetSettingParameters(Keywords.server, true, 2753));
			setpar.add(new GetSettingParameters(Keywords.port, false, 2754));
			setpar.add(new GetSettingParameters(Keywords.user, true, 2755));
			setpar.add(new GetSettingParameters(Keywords.password, false, 2756));
			setpar.add(new GetSettingParameters(Keywords.db, true, 2757));
		}
		else if (settingtype.equalsIgnoreCase(Keywords.HSQLDBREMOTE))
		{
			setpar.add(new GetSettingParameters(Keywords.server, true, 1432));
			setpar.add(new GetSettingParameters(Keywords.user, false, 1433));
			setpar.add(new GetSettingParameters(Keywords.password, false, 1434));
			setpar.add(new GetSettingParameters(Keywords.port, false, 1435));
			setpar.add(new GetSettingParameters(Keywords.db, true, 1436));
		}
		else if (settingtype.equalsIgnoreCase(Keywords.HSQLDBLOCAL))
		{
			setpar.add(new GetSettingParameters(Keywords.user, false, 1433));
			setpar.add(new GetSettingParameters(Keywords.password, false, 1434));
			setpar.add(new GetSettingParameters(Keywords.dbfile, true, 1437));
		}
		else if (settingtype.equalsIgnoreCase(Keywords.MSODBC))
		{
			setpar.add(new GetSettingParameters(Keywords.user, false, 1426));
			setpar.add(new GetSettingParameters(Keywords.password, false, 1427));
			setpar.add(new GetSettingParameters(Keywords.db, true, 1428));
		}
		else if (settingtype.equalsIgnoreCase(Keywords.BIGSQL))
		{
			setpar.add(new GetSettingParameters(Keywords.server, true, 3824));
			setpar.add(new GetSettingParameters(Keywords.port, false, 3825));
			setpar.add(new GetSettingParameters(Keywords.user, true, 3826));
			setpar.add(new GetSettingParameters(Keywords.password, true, 3827));
			setpar.add(new GetSettingParameters(Keywords.schema, false, 4019));
		}
		else if (settingtype.equalsIgnoreCase(Keywords.POSTGRESQL))
		{
			setpar.add(new GetSettingParameters(Keywords.server, true, 1398));
			setpar.add(new GetSettingParameters(Keywords.user, true, 1399));
			setpar.add(new GetSettingParameters(Keywords.password, false, 1400));
			setpar.add(new GetSettingParameters(Keywords.port, false, 1401));
			setpar.add(new GetSettingParameters(Keywords.db, true, 695));
		}
		else if (settingtype.equalsIgnoreCase(Keywords.htmllayout))
		{
			setpar.add(new GetSettingParameters(Keywords.htmltitle, false, 485));
			setpar.add(new GetSettingParameters(Keywords.background, false, 486));
			setpar.add(new GetSettingParameters(Keywords.div, false, 761));
			setpar.add(new GetSettingParameters(Keywords.csspath, false, 2186));
			setpar.add(new GetSettingParameters(Keywords.headerfile, false, 2281));
			setpar.add(new GetSettingParameters(Keywords.footerfile, false, 2282));
		}
		else if (settingtype.equalsIgnoreCase(Keywords.xlslayout))
		{
			setpar.add(new GetSettingParameters(Keywords.onesheetfords, false, 2154));
		}
		else if (settingtype.equalsIgnoreCase(Keywords.xlsdslayout))
		{
			setpar.add(new GetSettingParameters(Keywords.title, false, 2155));
		}
		else if (settingtype.equalsIgnoreCase(Keywords.pdflayout))
		{
			setpar.add(new GetSettingParameters(Keywords.pdftitle, false, 1214));
			setpar.add(new GetSettingParameters(Keywords.paper, false, 1215));
			setpar.add(new GetSettingParameters(Keywords.titlealign, false, 1216));
			setpar.add(new GetSettingParameters(Keywords.writepage, false, 1217));
			setpar.add(new GetSettingParameters(Keywords.titlefonts, false, 1218));
			setpar.add(new GetSettingParameters(Keywords.titlesize, false, 1219));
			setpar.add(new GetSettingParameters(Keywords.pagefonts, false, 1220));
			setpar.add(new GetSettingParameters(Keywords.pagesize, false, 1221));
		}
		else if (settingtype.equalsIgnoreCase(Keywords.htmldslayout))
		{
			setpar.add(new GetSettingParameters(Keywords.nocaption, false, 712));
			setpar.add(new GetSettingParameters(Keywords.captionalign, false, 717));
			setpar.add(new GetSettingParameters(Keywords.captionfont, false, 718));
			setpar.add(new GetSettingParameters(Keywords.captionsize, false, 719));
			setpar.add(new GetSettingParameters(Keywords.captioncolor, false, 720));
			setpar.add(new GetSettingParameters(Keywords.borderwidth, false, 487));
			setpar.add(new GetSettingParameters(Keywords.background, false, 488));
			setpar.add(new GetSettingParameters(Keywords.cellspacing, false, 713));
			setpar.add(new GetSettingParameters(Keywords.cellpadding, false, 714));
			setpar.add(new GetSettingParameters(Keywords.width, false, 715));
			setpar.add(new GetSettingParameters(Keywords.height, false, 716));
			setpar.add(new GetSettingParameters(Keywords.tablealign, false, 2323));
			setpar.add(new GetSettingParameters(Keywords.labelcolor, false, 722));
			setpar.add(new GetSettingParameters(Keywords.labelhalign, false, 723));
			setpar.add(new GetSettingParameters(Keywords.labelvalign, false, 724));
			setpar.add(new GetSettingParameters(Keywords.labelfont, false, 725));
			setpar.add(new GetSettingParameters(Keywords.labelsize, false, 726));
			setpar.add(new GetSettingParameters(Keywords.labelbgcolor, false, 727));
			setpar.add(new GetSettingParameters(Keywords.varcolor, false, 728));
			setpar.add(new GetSettingParameters(Keywords.varhalign, false, 729));
			setpar.add(new GetSettingParameters(Keywords.varvalign, false, 730));
			setpar.add(new GetSettingParameters(Keywords.varfont, false, 731));
			setpar.add(new GetSettingParameters(Keywords.varsize, false, 732));
			setpar.add(new GetSettingParameters(Keywords.varbgcolor, false, 733));
			setpar.add(new GetSettingParameters(Keywords.uselocalefornumbers, false, 2184));
			setpar.add(new GetSettingParameters(Keywords.numdecimals, false, 2185));
			setpar.add(new GetSettingParameters(Keywords.firstrowanchor, false, 2326));
			setpar.add(new GetSettingParameters(Keywords.firstcolanchor, false, 2327));
			setpar.add(new GetSettingParameters(Keywords.cellanchor, false, 2328));
			setpar.add(new GetSettingParameters(Keywords.numalign, false, 2690));
			setpar.add(new GetSettingParameters(Keywords.textalign, false, 2691));
		}
		else if (settingtype.equalsIgnoreCase(Keywords.pdfdslayout))
		{
			setpar.add(new GetSettingParameters(Keywords.nocaption, false, 712));
			setpar.add(new GetSettingParameters(Keywords.captionalign, false, 717));
			setpar.add(new GetSettingParameters(Keywords.captionfont, false, 718));
			setpar.add(new GetSettingParameters(Keywords.captionsize, false, 719));
			setpar.add(new GetSettingParameters(Keywords.borderwidth, false, 487));
			setpar.add(new GetSettingParameters(Keywords.labelalign, false, 1222));
			setpar.add(new GetSettingParameters(Keywords.labelfont, false, 725));
			setpar.add(new GetSettingParameters(Keywords.labelsize, false, 726));
			setpar.add(new GetSettingParameters(Keywords.varalign, false, 1223));
			setpar.add(new GetSettingParameters(Keywords.varfont, false, 731));
			setpar.add(new GetSettingParameters(Keywords.varsize, false, 732));
			setpar.add(new GetSettingParameters(Keywords.uselocalefornumbers, false, 2184));
			setpar.add(new GetSettingParameters(Keywords.numdecimals, false, 2185));
		}
		else if (settingtype.equalsIgnoreCase(Keywords.mailserver))
		{
			setpar.add(new GetSettingParameters(Keywords.address, false, 1239));
			setpar.add(new GetSettingParameters(Keywords.port, false, 1240));
			setpar.add(new GetSettingParameters(Keywords.username, false, 1241));
			setpar.add(new GetSettingParameters(Keywords.password, false, 3980));
		}
		return setpar;
	}
}
