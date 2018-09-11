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

import java.awt.GridLayout;
import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import ADaMSoft.keywords.Keywords;

/**
* This class adds the possibility to input a text that si to applied to more than one parameter in the GUI related to
* the procedure parameters selection
* @author marco.scarno@caspur.it
* @version 1.0.0, rev.: 17/06/09 by marco
*/
public class ParameterMultipleText
{
	JTextField textfield;
	String parametername;
	public ParameterMultipleText (String parametername, int messagecode, JPanel actualpanel, boolean required)
	{
		this.parametername=parametername;
		JPanel paneltext=new JPanel(new GridLayout(1,1));
		textfield=new JTextField();
		paneltext.add(textfield);
		TitledBorder title = BorderFactory.createTitledBorder(Keywords.Language.getMessage(messagecode));
		paneltext.setToolTipText(parametername);
		paneltext.setBorder(title);
		if (!required)
			paneltext.setBackground(Color.LIGHT_GRAY);
		actualpanel.add(paneltext);
	}
	public String toString()
	{
		String value=textfield.getText();
		if (!value.equals(""))
		{
			if (value.indexOf(";")>0)
			{
				String[] tevalue=value.split(";");
				value="";
				for (int i=0; i<tevalue.length; i++)
				{
					if (!tevalue[i].equals(""))
					{
						value=value+parametername+" "+tevalue[i];
						if (i<(tevalue.length-1))
							value=value+";\n";
					}
				}
			}
			else
				value=parametername+" "+value;
		}
		return value;
	}
}