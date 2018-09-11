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

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.border.Border;

import ADaMSoft.keywords.Keywords;

/**
* This class adds the possibility to select a checkbox in the GUI related to
* the procedure parameters selection
* @author marco.scarno@caspur.it, c.trani@caspur.it
* @version 1.0.0, rev.: 17/06/09 by marco
*/
public class ParameterCheckBox
{
	JCheckBox state;
	String parametername;
	/**
	*Create a checkbox in the actualpanel, related to the parametername, using the messagecode
	*/
	public ParameterCheckBox (String parametername, int messagecode, JPanel actualpanel, boolean required)
	{
		this.parametername=parametername;
		JPanel panelcheckbox=new JPanel(new GridLayout(1,1));
		state=new JCheckBox(Keywords.Language.getMessage(messagecode));
		state.setToolTipText(parametername);
		panelcheckbox.add(state);
		Border parameterborder = BorderFactory.createCompoundBorder(BorderFactory.createRaisedBevelBorder(), BorderFactory.createLoweredBevelBorder());
		panelcheckbox.setBorder(parameterborder);
		if (!required)
			panelcheckbox.setBackground(Color.LIGHT_GRAY);
		actualpanel.add(panelcheckbox);
	}
	/**
	*Returns the parameter
	*/
	public String toString()
	{
		String value="";
		if (state.isSelected())
			value=parametername+" "+value;
		return value;
	}
}