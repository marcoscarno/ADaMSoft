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
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;

import ADaMSoft.dataaccess.DictionaryReader;
import ADaMSoft.keywords.Keywords;

/**
* This class adds the possibility to select one or more variables in the GUI related to
* the procedure parameters selection
* @author marco.scarno@caspur.it
* @version 1.0.0, rev.:7/12/08 by marco
*/
public class ParameterTextVars
{
	boolean errorcreating;
	boolean multiple;
	String msgerror;
	JTextField fieldvar;
	JList listvar;
	Vector<String> variables;
	Vector<String> varlabel;
	int messagecode;
	JInternalFrame ActualLevelPanel;
	String parametername;
	public ParameterTextVars (String parametername, int messagecode, JPanel actualpanel, Hashtable<String, String> dictionaries, String[] dep, boolean required)
	{
		this.parametername=parametername;
		this.messagecode=messagecode;
		String firstdict="";
		msgerror="";
		variables=new Vector<String>();
		String dictname=dictionaries.get(dep[0]);
		String[] partnamedict=dictname.split("\\.");
		if (partnamedict.length==1)
		{
			String diruwd=System.getProperty(Keywords.WorkDir);
			firstdict=diruwd+partnamedict[0];
		}
		else
		{
			partnamedict[0]=Keywords.project.getPath(partnamedict[0]);
			if (partnamedict[0].equals(""))
			{
				msgerror=Keywords.Language.getMessage(61)+" ("+partnamedict[0]+")\n";
				errorcreating=true;
				return;
			}
			firstdict=partnamedict[0]+partnamedict[1];
		}
		DictionaryReader dr=new DictionaryReader(firstdict);
		if (!dr.getmessageDictionaryReader().equals(""))
		{
			msgerror=dr.getmessageDictionaryReader();
			errorcreating=true;
			return;
		}
		int totalvar=dr.gettotalvar();
		for (int j=0; j<totalvar; j++)
		{
			variables.add(dr.getvarname(j));
		}
		if (variables.size()==0)
		{
			msgerror=Keywords.Language.getMessage(526)+"\n";
			errorcreating=true;
			return;
		}
		varlabel=new Vector<String>();
		for (int i=0; i<variables.size(); i++)
		{
			String tempname=variables.get(i);
			for (int j=0; j<totalvar; j++)
			{
				if (tempname.equalsIgnoreCase(dr.getvarname(j)))
					varlabel.add(dr.getvarlabel(j));
			}
		}
        JPanel panelvar = new JPanel(false);
		panelvar.setLayout(new BoxLayout(panelvar,BoxLayout.Y_AXIS));
		listvar=new JList(varlabel)
        {
			private static final long serialVersionUID = 1L;
			public String getToolTipText(MouseEvent evt)
			{
				int index = locationToIndex(evt.getPoint());
				return variables.get(index);
			}
		};
		listvar.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		JScrollPane scroll = new JScrollPane(listvar);
		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		Action actionvar = new AbstractAction(Keywords.Language.getMessage(2160))
		{
			private static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent evt)
			{
	            int[] selectedvariables=listvar.getSelectedIndices();
	            String valuevar="";
	            for (int i=0; i<selectedvariables.length; i++)
	            {
					String tempname=variables.get(selectedvariables[i]);
					valuevar=valuevar+tempname;
					if (i<(selectedvariables.length-1))
						valuevar=valuevar+" ";
				}
				fieldvar.setText(fieldvar.getText()+" "+valuevar.trim());
			}
		};

		JButton b = new JButton(actionvar);
		fieldvar=new JTextField();
		fieldvar.setText("");
		fieldvar.setToolTipText(parametername);
		panelvar.add(fieldvar);
		panelvar.add(scroll);
		panelvar.add(b);
		TitledBorder title = BorderFactory.createTitledBorder(Keywords.Language.getMessage(messagecode));
		panelvar.setBorder(title);
		if (!required)
			panelvar.setBackground(Color.LIGHT_GRAY);
		actualpanel.add(panelvar);
	}
	public String toString()
	{
		String value=fieldvar.getText();
		value=value.trim();
		if (!value.equals(""))
			value=parametername+" "+value;
		return value;
	}
	public boolean getErrorSel()
	{
		return errorcreating;
	}
	public String getmsg()
	{
		return msgerror;
	}
}
