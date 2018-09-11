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
import java.awt.event.MouseEvent;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

import ADaMSoft.keywords.Keywords;

/**
* This class adds the possibility to select one or more option in the GUI related to
* the procedure parameters selection
* @author marco.scarno@caspur.it, c.trani@caspur.it
* @version 1.0.0, rev.: 17/06/09 by marco
*/
public class ParameterListMultiple implements ActionListener
{
	JList listsel;
	Vector<String> textlist;
	Vector<String> paralist;
	JTextField fieldval;
	JInternalFrame ActualLevelPanel;
	String parametername;
	int messagecode;
	public ParameterListMultiple (String listinfo, String parametername, int messagecode, JPanel actualpanel, JInternalFrame ActualLevelPanel, boolean required)
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
		this.parametername=parametername;
		this.messagecode=messagecode;
		this.ActualLevelPanel=ActualLevelPanel;
		JPanel panellist=new JPanel(new GridLayout(2,1));
		JButton b = new JButton(Keywords.Language.getMessage(639));
		b.addActionListener(this);
		fieldval=new JTextField();
		fieldval.setToolTipText(parametername);
		panellist.add(fieldval);
		panellist.add(b);
		TitledBorder title = BorderFactory.createTitledBorder(Keywords.Language.getMessage(messagecode));
		panellist.setBorder(title);
		if (!required)
			panellist.setBackground(Color.LIGHT_GRAY);
		actualpanel.add(panellist);
	}
	public void actionPerformed(ActionEvent e)
	{
		boolean resizableg = true;
		boolean closeableg = true;
		boolean maximizableg  = true;
		boolean iconifiableg = false;
		final JInternalFrame cp = new JInternalFrame(Keywords.Language.getMessage(messagecode), resizableg, closeableg, maximizableg, iconifiableg);
		ActualLevelPanel.hide();
		listsel=new JList(textlist)
        {
			private static final long serialVersionUID = 1L;
			public String getToolTipText(MouseEvent evt)
			{
				int index = locationToIndex(evt.getPoint());
				return paralist.get(index);
			}
		};
		listsel.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		listsel.setVisibleRowCount(5);

		JScrollPane scroll = new JScrollPane(listsel);
		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		Action actionoption = new AbstractAction(Keywords.Language.getMessage(640))
		{
			private static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent evt)
			{
	            Object [] selectedoptions=listsel.getSelectedValues();
	            String value="";
	            for (int i=0; i<selectedoptions.length; i++)
	            {
					String tempname=(String)selectedoptions[i];
					for (int j=0; j<textlist.size(); j++)
					{
						if (tempname.equalsIgnoreCase(textlist.get(j)))
							value=value+" "+paralist.get(j);
					}
				}
				fieldval.setText(value.trim());
				cp.dispose();
			}
		};

		JPanel paneloptions=new JPanel(false);
		paneloptions.setLayout(new BoxLayout(paneloptions,BoxLayout.Y_AXIS));
		JButton aceptoption = new JButton(actionoption);

		paneloptions.add(scroll);
		paneloptions.add(aceptoption);

		JScrollPane scrollpanelop = new JScrollPane(paneloptions);
		scrollpanelop.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scrollpanelop.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		int numrow = new Double(MainGUI.frame.getHeight()).intValue() - 52;
		int numcol = new Double(MainGUI.frame.getWidth()).intValue() - 14;
		scrollpanelop.setSize(numcol, numrow);

		cp.addInternalFrameListener(new InternalFrameListener()
		{
			public void internalFrameClosing(InternalFrameEvent e)
			{
				ActualLevelPanel.show();
			}
			public void internalFrameClosed(InternalFrameEvent e)
			{
				ActualLevelPanel.show();
			}
			public void internalFrameOpened(InternalFrameEvent e) {}
			public void internalFrameIconified(InternalFrameEvent e) {}
			public void internalFrameDeiconified(InternalFrameEvent e) {}
			public void internalFrameActivated(InternalFrameEvent e) {}
			public void internalFrameDeactivated(InternalFrameEvent e){}
		});

		cp.setSize(numcol, numrow);
		java.net.URL    url   = ParameterListMultiple.class.getResource(Keywords.simpleicon);
		ImageIcon iconSet = new ImageIcon(url);
		cp.setFrameIcon(iconSet);

		cp.getContentPane().add(scrollpanelop);
		cp.repaint();
		cp.pack();
		cp.setVisible(true);
		MainGUI.desktop.add(cp);
		MainGUI.desktop.repaint();
		cp.moveToFront();
		try
		{
			cp.setEnabled(true);
			cp.toFront();
			cp.show();
			cp.setSelected(true);
		}
		catch (Exception ex){}
	}
	public String toString()
	{
		String value=fieldval.getText();
		value=value.trim();
		if (!value.equals(""))
			value=parametername+" "+value;
		return value;
	}
}
