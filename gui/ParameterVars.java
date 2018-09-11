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
import java.awt.event.MouseEvent;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JInternalFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

import ADaMSoft.dataaccess.DictionaryReader;
import ADaMSoft.keywords.Keywords;

/**
* This class adds the possibility to select one or more variables in the GUI related to
* the procedure parameters selection
* @author marco.scarno@caspur.it, c.trani@caspur.it
* @version 1.0.0, rev.:20/05/13 by marco
*/
public class ParameterVars implements ActionListener
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
	String[] depdicts;
	Hashtable<String, String> defdictionaries;
	public ParameterVars (String typevar, String parametername, int messagecode, JPanel actualpanel, Hashtable<String, String> dictionaries, String[] dep, boolean multiple, JInternalFrame ActualLevelPanel, boolean required)
	{
		this.depdicts=dep;
		this.defdictionaries=dictionaries;
		if (dep==null) return;
		if (dep[0]==null) return;
		if (dep[0].equals("")) return;
		if (dictionaries.get(dep[0])==null) return;
		this.parametername=parametername;
		this.multiple=multiple;
		this.messagecode=messagecode;
		this.ActualLevelPanel=ActualLevelPanel;
		String firstdict="";
		msgerror="";
		variables=new Vector<String>();
		if (typevar.equalsIgnoreCase("common"))
		{
			for (int i=0; i<dep.length; i++)
			{
				String namedict="";
				String dictname=dictionaries.get(dep[i]);
				String[] partnamedict=dictname.split("\\.");
				if (partnamedict.length==1)
				{
					String diruwd=System.getProperty(Keywords.WorkDir);
					namedict=diruwd+partnamedict[0];
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
					namedict=partnamedict[0]+partnamedict[1];
				}
				if (firstdict.equals(""))
					firstdict=namedict;
				DictionaryReader dr=new DictionaryReader(namedict);
				if (!dr.getmessageDictionaryReader().equals(""))
				{
					msgerror=dr.getmessageDictionaryReader();
					errorcreating=true;
					return;
				}
				if (i==0)
				{
					int totalvar=dr.gettotalvar();
					for (int j=0; j<totalvar; j++)
					{
						variables.add(dr.getvarname(j));
					}
				}
				else
				{
					Vector<String> tempcommonvar=new Vector<String>();
					int totalvar=dr.gettotalvar();
					for (int j=0; j<totalvar; j++)
					{
						String tempname=dr.getvarname(j);
						boolean check=false;
						for (int k=0; k<variables.size(); k++)
						{
							String tempcommonname=variables.get(k);
							if (tempname.equalsIgnoreCase(tempcommonname))
								check=true;
						}
						if (check)
							tempcommonvar.add(dr.getvarname(j));
					}
					variables.clear();
					for (int j=0; j<tempcommonvar.size(); j++)
					{
						String tempcommonname=tempcommonvar.get(j);
						variables.add(tempcommonname);
					}
				}
				if (variables.size()==0)
				{
					msgerror=Keywords.Language.getMessage(525)+"\n";
					errorcreating=true;
					return;
				}
			}
		}
		if (typevar.equalsIgnoreCase("all"))
		{
			firstdict="";
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
		}
		if (typevar.equalsIgnoreCase("num"))
		{
			firstdict="";
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
				if (dr.getvarformat(j).toUpperCase().startsWith(Keywords.NUMSuffix))
					variables.add(dr.getvarname(j));
			}
		}
		if (variables.size()==0)
		{
			msgerror=Keywords.Language.getMessage(526)+"\n";
			errorcreating=true;
			return;
		}
		DictionaryReader dr=new DictionaryReader(firstdict);
		if (!dr.getmessageDictionaryReader().equals(""))
		{
			msgerror=dr.getmessageDictionaryReader();
			errorcreating=true;
			return;
		}
		varlabel=new Vector<String>();
		int totalvar=dr.gettotalvar();
		for (int i=0; i<variables.size(); i++)
		{
			String tempname=variables.get(i);
			for (int j=0; j<totalvar; j++)
			{
				if (tempname.equalsIgnoreCase(dr.getvarname(j)))
					varlabel.add(dr.getvarlabel(j));
			}
		}
		JPanel panelvar=new JPanel(new GridLayout(2,1));
		JButton b = new JButton(Keywords.Language.getMessage(527));
		b.addActionListener(this);
		fieldvar=new JTextField();
		fieldvar.setToolTipText(parametername);
		panelvar.add(fieldvar);
		panelvar.add(b);
		TitledBorder title = BorderFactory.createTitledBorder(Keywords.Language.getMessage(messagecode));
		panelvar.setBorder(title);
		if (!required)
			panelvar.setBackground(Color.LIGHT_GRAY);
		actualpanel.add(panelvar);
	}
	public void actionPerformed(ActionEvent e)
	{
		boolean resizableg = true;
		boolean closeableg = true;
		boolean maximizableg  = true;
		boolean iconifiableg = false;
		final JInternalFrame cp = new JInternalFrame(Keywords.Language.getMessage(messagecode), resizableg, closeableg, maximizableg, iconifiableg);
		ActualLevelPanel.hide();
		listvar=new JList(varlabel)
        {
			private static final long serialVersionUID = 1L;
			public String getToolTipText(MouseEvent evt)
			{
				int index = locationToIndex(evt.getPoint());
				return variables.get(index);
			}
		};

		if (multiple)
			listvar.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		if (!multiple)
			listvar.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		listvar.setVisibleRowCount(5);

		JScrollPane scroll = new JScrollPane(listvar);
		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		Action actionvar = new AbstractAction(Keywords.Language.getMessage(528))
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
				fieldvar.setText(valuevar.trim());
				cp.dispose();
			}
		};

		JPanel panelvariables=new JPanel(false);
		panelvariables.setLayout(new BoxLayout(panelvariables,BoxLayout.Y_AXIS));
		JButton aceptvar = new JButton(actionvar);

		panelvariables.add(scroll);
		panelvariables.add(aceptvar);

		JScrollPane scrollpanelvariables = new JScrollPane(panelvariables);
		scrollpanelvariables.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scrollpanelvariables.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		int numrow = new Double(MainGUI.frame.getHeight()).intValue() - 52;
		int numcol = new Double(MainGUI.frame.getWidth()).intValue() - 14;
		scrollpanelvariables.setSize(numcol, numrow);

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
		java.net.URL    url   = ParameterVars.class.getResource(Keywords.simpleicon);
		ImageIcon iconSet = new ImageIcon(url);
		cp.setFrameIcon(iconSet);

		cp.getContentPane().add(scrollpanelvariables);
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
		if (depdicts==null) return "";
		if (depdicts[0]==null) return "";
		if (depdicts[0].equals("")) return "";
		if (defdictionaries.get(depdicts[0])==null) return "";
		String value=fieldvar.getText();
		value=value.trim();
		if (!value.equals(""))
		{
			String [] varselected=(fieldvar.getText()).split(" ");
			if ((!multiple) && (varselected.length>1))
			{
				value="";
				JOptionPane pane = new JOptionPane(Keywords.Language.getMessage(529));
				JDialog dialog = pane.createDialog(MainGUI.desktop, Keywords.Language.getMessage(166));
				dialog.validate();
				dialog.setVisible(true);
			}
		}
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
