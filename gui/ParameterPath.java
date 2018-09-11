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

package ADaMSoft.gui;

import java.awt.Color;
import java.awt.GridLayout;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import ADaMSoft.keywords.Keywords;

/**
* This class add the possibility to select a path in the GUI related to
* the procedure parameters selection
* @author marco.scarno@caspur.it, c.trani@caspur.it
* @version 1.0.0, rev.: 17/06/09 by marco
*/
public class ParameterPath
{
	JComboBox listpath;
	String parametername;
	/**
	*Builds a GUI that permits to select a PATH
	*/
	public ParameterPath (String parametername, int messagecode, JPanel actualpanel, boolean required)
	{
		this.parametername=parametername;
		Vector<String> paths=Keywords.project.getPaths();
		for (int i=0; i<paths.size(); i++)
		{
			String tempp=paths.get(i);
			if (tempp.equalsIgnoreCase("work"))
				paths.set(i, Keywords.Language.getMessage(174));
		}
		JPanel path=new JPanel(new GridLayout(1,1));
		listpath=new JComboBox(paths);
		listpath.setToolTipText(parametername);
		path.add(listpath);
		TitledBorder title = BorderFactory.createTitledBorder(Keywords.Language.getMessage(messagecode));
		path.setBorder(title);
		if (!required)
			path.setBackground(Color.LIGHT_GRAY);
		actualpanel.add(path);
	}
	/**
	*Returns the name of the path in a string form
	*/
	public String toString()
	{
		String value="";
		if (!((String)listpath.getSelectedItem()).equals(Keywords.Language.getMessage(174)))
			value=parametername+(String)listpath.getSelectedItem();
		return value;
	}
}