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

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;

import ADaMSoft.keywords.Keywords;

/**
* This class adds the possibility to add a Note in the GUI related to the parameter selection
* @author marco.scarno@caspur.it, c.trani@caspur.it
* @version 1.0.0, rev.: 17/06/09 by marco
*/
public class ParameterNote
{
	public ParameterNote (int messagecode, JPanel actualpanel)
	{
		JPanel paneltext=new JPanel(new GridLayout(2,1));
		JLabel textfield=new JLabel(Keywords.Language.getMessage(messagecode));
		JSeparator s2=new JSeparator();
		paneltext.add(textfield);
		paneltext.add(s2);
		paneltext.setBackground(Color.LIGHT_GRAY);
		actualpanel.add(paneltext);
	}
}