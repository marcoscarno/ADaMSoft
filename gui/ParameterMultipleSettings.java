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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import ADaMSoft.keywords.Keywords;

/**
* This class add the possibility to select multiple settings
* @author marco.scarno@caspur.it
* @version 1.0.0, rev.: 17/06/09 by marco
*/
public class ParameterMultipleSettings implements ActionListener
{
	JComboBox listsettings;
	String parametername;
	JComboBox setname;
	JPanel patsetname;
	boolean required;
	String[] dicts;
	Vector<String> listset;
	Vector<String> nameset;
	/**
	*Builds a GUI that permits to select multiple settings
	*/
	public ParameterMultipleSettings (String parametername, int messagecode, JPanel actualpanel, boolean required)
	{
		this.parametername=parametername;
		this.required=required;
		listset=new Vector<String>();
		nameset=new Vector<String>();
		listset.add(Keywords.Language.getMessage(1750));
		nameset.add(Keywords.Language.getMessage(1751));
		if (parametername.equalsIgnoreCase(Keywords.dbset))
		{
			for (int i=0; i<Keywords.KeywordsForDBSettings.length; i++)
			{
				listset.add(Keywords.KeywordsForDBSettings[i].toUpperCase());
			}
		}
		patsetname=new JPanel(new GridLayout(1,2));
		listsettings=new JComboBox(listset);
		listsettings.setSelectedIndex(0);
		setname=new JComboBox(nameset);
		setname.setSelectedIndex(0);
		patsetname.add(listsettings);
		patsetname.add(setname);
		listsettings.addActionListener(this);
		TitledBorder title = BorderFactory.createTitledBorder(Keywords.Language.getMessage(messagecode));
		patsetname.setBorder(title);
		if (!required)
			patsetname.setBackground(Color.LIGHT_GRAY);
		actualpanel.add(patsetname);
	}
	/**
	*Return the selected parameter
	*/
	public String toString()
	{
		String valueset = listsettings.getSelectedItem().toString();
		if (valueset.equalsIgnoreCase(Keywords.Language.getMessage(1750)))
			return "";
		String valuesetname=setname.getSelectedItem().toString();
		if (valuesetname.equalsIgnoreCase(Keywords.Language.getMessage(1751)))
			return "";
		valueset=valueset+"="+valuesetname;
		return valueset;
	}
	/**
	*Fill the JComboBox with the setting names
	*/
	public void actionPerformed(ActionEvent e)
	{
		String selectedS = listsettings.getSelectedItem().toString();
		if (selectedS.equalsIgnoreCase(Keywords.Language.getMessage(1750)))
			return;
		Vector<String> infoset=Keywords.project.getSettingNames(selectedS);
		if (infoset.size()==0)
			return;
		nameset=new Vector<String>();
		nameset.add(Keywords.Language.getMessage(1751));
		for (int i=0; i<infoset.size(); i++)
		{
			nameset.add(infoset.get(i));
		}
		patsetname.remove(setname);
		setname=new JComboBox(nameset);
		setname.setSelectedIndex(0);
		patsetname.add(setname);
		patsetname.validate();
	}
}