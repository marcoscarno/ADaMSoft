/**
* Copyright © 2006-2013 CASPUR
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
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.JLabel;

import javax.swing.JOptionPane;
import javax.swing.JDialog;

import javax.swing.border.TitledBorder;

import ADaMSoft.keywords.Keywords;

/**
* This class add the possibility to select multiple setting out
* the procedure parameters selection
* @author marco.scarno@caspur.it
* @version 1.0.0, rev.: 06/03/13 by marco
*/
public class ParameterMultipleSettingOut implements ActionListener
{
	JComboBox listpath;
	String parametername;
	JTextField outdictname;
	JComboBox listallselected;
	JPanel pathdictname;
	JPanel defpathdictname;
	JPanel secpathdictname;
	JPanel nampathdictname;
	JPanel listdictname;
	JButton selother;
	JButton delsel;
	boolean required;
	String[] dicts;
	Vector<String> paths;
	Vector<String> selecteddicts;
	JTextField textfield;

	/**
	*Builds a GUI that permits to select multiple setting out
	*/
	public ParameterMultipleSettingOut (String parametername, int messagecode, JPanel actualpanel, boolean required, Object secmsg)
	{
		int refmsg=3239;
		if (secmsg!=null)
		{
			try
			{
				String secmsgs=(String)secmsg.toString();
				refmsg=Integer.parseInt(secmsgs);
			}
			catch (Exception ec)
			{
				ec.printStackTrace();
				refmsg=3239;
			}
		}
		textfield=new JTextField();
		JLabel labrefd=new JLabel(Keywords.Language.getMessage(refmsg)+": ");
		selecteddicts=new Vector<String>();
		this.parametername=parametername;
		this.required=required;
		paths=Keywords.project.getSettingNames("out");
		paths.add(0, Keywords.Language.getMessage(190));
		pathdictname=new JPanel(new GridLayout(1,2));
		defpathdictname=new JPanel(new GridLayout(4,1));
		secpathdictname=new JPanel(new GridLayout(1,1));
		nampathdictname=new JPanel(new GridLayout(1,2));
		listdictname=new JPanel(new GridLayout(1,2));
		delsel=new JButton(Keywords.Language.getMessage(3240));
		delsel.setEnabled(false);
		delsel.addActionListener(this);
		listpath=new JComboBox(paths);
		listallselected=new JComboBox(selecteddicts);
		listallselected.setEnabled(false);
		listdictname.add(listallselected);
		listdictname.add(delsel);
		listpath.setToolTipText(parametername);
		listpath.setSelectedIndex(0);
		pathdictname.add(listpath);
		outdictname=new JTextField();
		listpath.addActionListener(this);
		pathdictname.add(outdictname);
		selother=new JButton(Keywords.Language.getMessage(3250));
		selother.addActionListener(this);
		nampathdictname.add(labrefd);
		nampathdictname.add(textfield);
		secpathdictname.add(selother);
		defpathdictname.add(pathdictname);
		defpathdictname.add(nampathdictname);
		defpathdictname.add(secpathdictname);
		defpathdictname.add(listdictname);
		TitledBorder title = BorderFactory.createTitledBorder(Keywords.Language.getMessage(messagecode));
		defpathdictname.setBorder(title);
		if (!required)
			defpathdictname.setBackground(Color.LIGHT_GRAY);
		actualpanel.add(defpathdictname);
	}
	/**
	*Return the selected parameter
	*/
	public String toString()
	{
		if (selecteddicts.size()==0) return "";
		String value="";
		for (int i=0; i<selecteddicts.size(); i++)
		{
			value=value+selecteddicts.get(i)+" ";
		}
		return value.trim();
	}
	/**
	*Fill the JComboBox with the dictionaries name
	*/
	public void actionPerformed(ActionEvent e)
	{
		Object Source = e.getSource();
		if (Source == selother)
		{
			String value = outdictname.getText();
			if (value.trim().equals(""))
			{
				JOptionPane pane = new JOptionPane(Keywords.Language.getMessage(3246));
				JDialog dialog = pane.createDialog(MainGUI.desktop, Keywords.Language.getMessage(166));
				dialog.validate();
				dialog.setVisible(true);
				return;
			}
			if (!((String)listpath.getSelectedItem()).equals(Keywords.Language.getMessage(190)))
				value=(String)listpath.getSelectedItem()+"."+value;
			String namedse=textfield.getText();
			if (namedse.trim().equals(""))
			{
				JOptionPane pane = new JOptionPane(Keywords.Language.getMessage(3247));
				JDialog dialog = pane.createDialog(MainGUI.desktop, Keywords.Language.getMessage(166));
				dialog.validate();
				dialog.setVisible(true);
				return;
			}
			namedse=parametername.toUpperCase()+namedse+"="+value;
			selecteddicts.add(namedse);
			delsel.setEnabled(true);
			listdictname.remove(listallselected);
			listdictname.remove(delsel);
			listallselected=new JComboBox(selecteddicts);
			listallselected.setEnabled(true);
			listdictname.add(listallselected);
			listdictname.add(delsel);
			listdictname.validate();
			textfield.setText("");
			outdictname.setText("");
		}
		if (Source == delsel)
		{
			if (selecteddicts.size()==0)
			{
				delsel.setEnabled(false);
				listallselected.setEnabled(false);
				return;
			}
			String value = listallselected.getSelectedItem().toString();
			for (int i=0; i<selecteddicts.size(); i++)
			{
				if (selecteddicts.get(i).equals(value))
				{
					selecteddicts.remove(i);
					break;
				}
			}
			delsel.setEnabled(true);
			listdictname.remove(listallselected);
			listdictname.remove(delsel);
			listallselected=new JComboBox(selecteddicts);
			if (selecteddicts.size()>0) listallselected.setEnabled(true);
			else
			{
				listallselected.setEnabled(false);
				delsel.setEnabled(false);
			}
			listdictname.add(listallselected);
			listdictname.add(delsel);
			listdictname.validate();
			textfield.setText("");
			outdictname.setText("");
		}
	}
}