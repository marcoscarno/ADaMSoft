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
* This class add the possibility to select a value from a list in the GUI related to
* the procedure parameters selection
* @author marco.scarno@caspur.it, c.trani@caspur.it
* @version 1.0.0, rev.: 17/06/09 by marco
*/
public class ParameterListSingle
{
	JComboBox listsel;
	String parametername;
	Vector<String> textlist;
	Vector<String> paralist;
	public ParameterListSingle (String listinfo, String parametername, int messagecode, JPanel actualpanel, boolean required)
	{
		textlist=new Vector<String>();
		paralist=new Vector<String>();
		this.parametername=parametername;
		String [] items=listinfo.split(",");
		for (int i=0; i<items.length; i++)
		{
			String[] temp=items[i].split("_");
			int msgcode=Integer.valueOf(temp[0]);
			textlist.add(Keywords.Language.getMessage(msgcode));
			paralist.add(temp[1]);
		}
		JPanel listsingle=new JPanel(new GridLayout(1,1));
		TitledBorder title = BorderFactory.createTitledBorder(Keywords.Language.getMessage(messagecode));
		listsingle.setBorder(title);
		listsel=new JComboBox(textlist);
		listsel.setToolTipText(parametername);
		listsingle.add(listsel);
		if (!required)
			listsingle.setBackground(Color.LIGHT_GRAY);
		actualpanel.add(listsingle);
	}
	public String toString()
	{
		String temp=(String)listsel.getSelectedItem();
		String value="";
		for (int i=0; i<textlist.size(); i++)
		{
			if (temp.equalsIgnoreCase(textlist.get(i)))
				value=paralist.get(i);
		}
		if (value.equalsIgnoreCase("NULL"))
			return "";
		else
			return parametername+" "+value;
	}
}
