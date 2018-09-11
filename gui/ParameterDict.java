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
import java.io.File;
import java.io.FilenameFilter;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import ADaMSoft.keywords.Keywords;

/**
* This class add the possibility to select a path and a dictionary in the GUI related to
* the procedure parameters selection
* @author marco.scarno@caspur.it, c.trani@caspur.it
* @version 1.0.0, rev.: 17/06/09 by marco
*/
public class ParameterDict implements ActionListener
{
	JComboBox listpath;
	String parametername;
	JComboBox outdictname;
	JPanel pathdictname;
	boolean required;
	String[] dicts;
	/**
	*Builds a GUI that permits to select a PATH and a DICTIONARY
	*/
	public ParameterDict (String parametername, int messagecode, JPanel actualpanel, boolean required)
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
		pathdictname.add(listpath);
		String selPath = listpath.getSelectedItem().toString();
		if (selPath.equals(Keywords.Language.getMessage(174)))
			selPath=System.getProperty(Keywords.WorkDir);
		else
			selPath = Keywords.project.getPath(selPath);
		dicts= new String[0];
		try
		{
			File f = new File(selPath);
			String[] newdicts= f.list(new FilenameFilter()
			{
				public boolean accept(File dir, String name)
				{
					return name.endsWith(Keywords.DictionaryExtension);
				}
			});
			if(newdicts!=null)
			{
				int lundict=newdicts.length;
				if (!required)
					lundict++;
				dicts=new String[lundict];
				int pos=0;
				if (!required)
				{
					dicts[0]=Keywords.Language.getMessage(190);
					pos++;
				}
				for(int i=0;i<newdicts.length;i++)
				{
					dicts[i+pos]=newdicts[i].replaceAll(Keywords.DictionaryExtension,"");
				}
				outdictname=new JComboBox(dicts);
			}
			else
			{
				outdictname=new JComboBox();
				outdictname.setEnabled(false);
			}
		}
		catch (Exception ex)
		{
			outdictname=new JComboBox();
			outdictname.setEnabled(false);
		}
		listpath.addActionListener(this);
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
		if(!outdictname.isEnabled())
		{
			return "";
		}
		if (outdictname.getSelectedItem()==null)
			return "";
		String value = outdictname.getSelectedItem().toString();
		if (value.equalsIgnoreCase(Keywords.Language.getMessage(190)))
			return "";
		if (!((String)listpath.getSelectedItem()).equals(Keywords.Language.getMessage(174)))
			value=(String)listpath.getSelectedItem()+"."+value;
		if (!value.trim().equals(""))
			value=parametername+value;
		return value;
	}
	/**
	*Fill the JComboBox with the dictionaries name
	*/
	public void actionPerformed(ActionEvent e)
	{
		String selPath = listpath.getSelectedItem().toString();
		if (selPath.equals(Keywords.Language.getMessage(174)))
			selPath=System.getProperty(Keywords.WorkDir);
		else
			selPath = Keywords.project.getPath(selPath);
		try
		{
			File f = new File(selPath);
			String[] newdicts= f.list(new FilenameFilter()
			{
				public boolean accept(File dir, String name)
				{
					return name.endsWith(Keywords.DictionaryExtension);
				}
			});
			if(newdicts!=null)
			{
				pathdictname.remove(outdictname);
				int lundict=newdicts.length;
				if (!required)
					lundict++;
				dicts=new String[lundict];
				int pos=0;
				if (!required)
				{
					dicts[0]=Keywords.Language.getMessage(190);
					pos++;
				}
				for(int i=0;i<newdicts.length;i++)
				{
					dicts[i+pos]=newdicts[i].replaceAll(Keywords.DictionaryExtension,"");
				}
				outdictname=new JComboBox(dicts);
				pathdictname.add(outdictname);
				pathdictname.validate();
			}
			else
			{
				pathdictname.remove(outdictname);
				outdictname=new JComboBox();
				outdictname.setEnabled(false);
				pathdictname.add(outdictname);
				pathdictname.validate();
			}
		}
		catch (Exception exx)
		{
			pathdictname.remove(outdictname);
			outdictname=new JComboBox();
			outdictname.setEnabled(false);
			pathdictname.add(outdictname);
			pathdictname.validate();
		}
	}
}