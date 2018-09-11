/**
* Copyright © 2017 MS
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
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.JOptionPane;

import ADaMSoft.keywords.Keywords;

/**
* This class adds the possibility to select a SETTING in the GUI related to
* the procedure parameters selection
* @author marco.scarno@gmail.com
* @version 1.0.0, rev.: 13/02/2017
*/
public class ParameterSetting
{
	JComboBox listsetting;
	JTextField nameds;
	String parametername;
	String settingtype;
	public ParameterSetting (String settingtype, String parametername, int messagecode, JPanel actualpanel, boolean required)
	{
		this.parametername=parametername;
		this.settingtype=settingtype;
		Vector<String> infoset=Keywords.project.getSettingNames(settingtype);
		infoset.add(0, Keywords.Language.getMessage(190));
		JPanel setting = new JPanel(false);
		setting.setLayout(new BoxLayout(setting,BoxLayout.Y_AXIS));
		JPanel selectsetting=new JPanel(new GridLayout(1,1));
		listsetting=new JComboBox(infoset);
		listsetting.setToolTipText(parametername.toUpperCase());
		selectsetting.add(listsetting);
		setting.add(selectsetting);
		if (settingtype.equalsIgnoreCase("out"))
		{
			JPanel nameoutds=new JPanel(new GridLayout(1,2));
			JLabel labeloutds=new JLabel(Keywords.Language.getMessage(191));
			nameds=new JTextField(10);
			nameoutds.add(labeloutds);
			nameoutds.add(nameds);
			if (!required)
				nameoutds.setBackground(Color.LIGHT_GRAY);
			setting.add(nameoutds);
		}
		TitledBorder title = BorderFactory.createTitledBorder(Keywords.Language.getMessage(messagecode));
		setting.setBorder(title);
		setting.setToolTipText(settingtype.toUpperCase()+"=");
		if (!required)
			setting.setBackground(Color.LIGHT_GRAY);
		actualpanel.add(setting);
	}
	public String toString()
	{
		String selectedsetting=(String)listsetting.getSelectedItem();
		if (selectedsetting.equalsIgnoreCase(Keywords.Language.getMessage(190)))
			selectedsetting="";
		if (settingtype.equalsIgnoreCase("out"))
		{
			String namedataset=(nameds.getText()).trim();
			if (!namedataset.equals(""))
			{
				if (namedataset.indexOf(".")>=0)
				{
					JOptionPane pane = new JOptionPane(Keywords.Language.getMessage(1619));
					JDialog dialog = pane.createDialog(MainGUI.desktop, Keywords.Language.getMessage(166));
					dialog.validate();
					dialog.setVisible(true);
					nameds.setText("");
					return "";
				}
				if (!selectedsetting.equalsIgnoreCase(""))
					selectedsetting=selectedsetting+"."+namedataset;
				else
					selectedsetting=namedataset;
			}
		}
		selectedsetting=selectedsetting.trim();
		if (!selectedsetting.equals(""))
		{
			if (settingtype.equalsIgnoreCase("out"))
			{
				if (selectedsetting.indexOf(" ")>=0)
				{
					JOptionPane pane = new JOptionPane(Keywords.Language.getMessage(1619));
					JDialog dialog = pane.createDialog(MainGUI.desktop, Keywords.Language.getMessage(166));
					dialog.validate();
					dialog.setVisible(true);
					nameds.setText("");
					return "";
				}
			}
			return parametername+selectedsetting;
		}
		else
			return "";
	}
}
