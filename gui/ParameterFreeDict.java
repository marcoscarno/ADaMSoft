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
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import ADaMSoft.keywords.Keywords;

/**
* This class add the possibility to select a path and a dictionary in the GUI related to
* the procedure parameters selection
* @author marco.scarno@caspur.it, c.trani@caspur.it
* @version 1.0.0, rev.: 21/08/09 by marco
*/
public class ParameterFreeDict
{
	JComboBox listpath;
	String parametername;
	JTextField outdictname;
	JPanel pathdictname;
	boolean required;
	String[] dicts;
	/**
	*Builds a GUI that permits to select a PATH and a DICTIONARY by writing its name
	*/
	public ParameterFreeDict (String parametername, int messagecode, JPanel actualpanel, boolean required)
	{
		this.parametername=parametername;
		this.required=required;
		Vector<String> paths=Keywords.project.getPaths();
		for (int i=0; i<paths.size(); i++)
		{
			String tempp=paths.get(i);
			if (tempp.equalsIgnoreCase("work"))
				paths.set(i, Keywords.Language.getMessage(174));
		}
		pathdictname=new JPanel(new GridLayout(1,2));
		listpath=new JComboBox(paths);
		listpath.setToolTipText(parametername);
		listpath.setSelectedIndex(0);
		outdictname=new JTextField(10);
		pathdictname.add(listpath);
		pathdictname.add(outdictname);
		TitledBorder title = BorderFactory.createTitledBorder(Keywords.Language.getMessage(messagecode));
		pathdictname.setBorder(title);
		if (!required)
			pathdictname.setBackground(Color.LIGHT_GRAY);
		actualpanel.add(pathdictname);
	}
	/**
	*Return the selected parameter
	*/
	public String toString()
	{
		String value = outdictname.getText();
		if (value==null)
			value="";
		if (value.equals(""))
			return "";
		if (!((String)listpath.getSelectedItem()).equals(Keywords.Language.getMessage(174)))
			value=(String)listpath.getSelectedItem()+"."+value;
		value=parametername+value;
		return value;
	}
}