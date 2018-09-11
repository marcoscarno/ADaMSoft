/**
* Copyright (c) 2015 MS
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

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;
import javax.swing.table.DefaultTableModel;

import ADaMSoft.keywords.Keywords;

/**
* This is the GUI that permits to add/modify the missing data rule for a variable
* @author marco.scarno@gmail.com
* @date 06/09/2015
*/
public class DictionaryMissingData
{
	JPanel panelmd;
	Border loweredbevel;
	JTable table;
	Vector<Vector<?>> cellData;
	JInternalFrame viewMissingData;
	Vector<String> columnnames;
	JScrollPane scrollPane;
	JPanel panelview;
	String MissingDataAction;
	JInternalFrame viewDict;
	JButton deleteall;
	JButton deletesingle;
	JButton addmissingdata;
	JButton back;
	JButton backaction;
	JComboBox listdelsingle;
	JTextField textcode;
	JButton okdeletesingle;
	JButton nodeletesingle;
	JButton okdeleteall;
	JButton okadd;
	JButton noadd;
	JButton okaddsetting;
	JButton noaddsetting;
	JButton cancelaction;
	JLabel labelcode;
	Object[][] miss;
	String[] mis;
	String currentvarname;
	JScrollPane scrolldef;
	JScrollPane tablescroll;
	Hashtable<String, String> actualmissingdata;
	int currentvarrif;
	/**
	* This permits to add/modify the missing data rules
	*/
	public DictionaryMissingData(int varrif, String varname, JInternalFrame viewDictionary)
	{
		this.viewDict=viewDictionary;
		this.currentvarname=varname;
		currentvarrif=varrif;
		MissingDataAction="";
		viewDict=viewDictionary;
		viewDict.setVisible(false);
		table = new JTable()
		{
			private static final long serialVersionUID = 1L;
			public boolean isCellEditable(int rowIndex, int colIndex)
			{
				return false;
			}
		};
		actualmissingdata=DataSetViewer.missingdata.get(varrif);

		table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		table.setColumnSelectionAllowed(false);
		table.setRowSelectionAllowed(false);
		table.setCellSelectionEnabled(false);
		table.setFocusable(false);

		tablescroll = new JScrollPane(table);

		tablescroll.setSize(table.getPreferredSize());

		tablescroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		tablescroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		loweredbevel = BorderFactory.createLoweredBevelBorder();
		TitledBorder titlemissingdata1 = BorderFactory.createTitledBorder(loweredbevel, Keywords.Language.getMessage(327));
		titlemissingdata1.setTitlePosition(TitledBorder.ABOVE_TOP);
		panelmd=new JPanel();
		panelmd.setBorder(titlemissingdata1);
		panelmd.add(tablescroll);

		boolean resizable = true;
		boolean closeable = true;
		boolean maximizable  = true;
		boolean iconifiable = true;
		String title =Keywords.Language.getMessage(306)+" "+varname;
		viewMissingData = new JInternalFrame(title, resizable, closeable, maximizable, iconifiable);
		viewMissingData.addInternalFrameListener(new InternalFrameListener()
		{
			public void internalFrameClosing(InternalFrameEvent e)
			{
				viewDict.setVisible(true);
				if (cellData!=null)
				{
					cellData.clear();
					cellData=null;
				}
				System.gc();
			}
			public void internalFrameClosed(InternalFrameEvent e)
			{
				viewDict.setVisible(true);
				if (cellData!=null)
				{
					cellData.clear();
					cellData=null;
				}
				System.gc();
			}
			public void internalFrameOpened(InternalFrameEvent e) {}
			public void internalFrameIconified(InternalFrameEvent e) {}
			public void internalFrameDeiconified(InternalFrameEvent e) {}
			public void internalFrameActivated(InternalFrameEvent e) {}
			public void internalFrameDeactivated(InternalFrameEvent e){}
		});
		java.net.URL    url   = DictionaryMissingData.class.getResource(Keywords.simpleicon);
		ImageIcon iconSet = new ImageIcon(url);
		panelview=new JPanel();
		panelview.setLayout(new BoxLayout(panelview,BoxLayout.Y_AXIS));
		int numrow = new Double(MainGUI.frame.getHeight()).intValue() - 52;
		int numcol = new Double(MainGUI.frame.getWidth()).intValue() - 14;
		viewMissingData.setSize(numcol, numrow);
		viewMissingData.setFrameIcon(iconSet);

		columnnames=new Vector<String>();
		columnnames.addElement(Keywords.Language.getMessage(328));

		Action delall = new AbstractAction(Keywords.Language.getMessage(329))
		{
			private static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent evt)
			{
				if (actualmissingdata.size()==0)
				{
					JOptionPane pane = new JOptionPane(Keywords.Language.getMessage(330));
					JDialog dialog = pane.createDialog(MainGUI.desktop, Keywords.Language.getMessage(134));
					dialog.validate();
					dialog.setVisible(true);
					return;
				}
				MissingDataAction=MissingDataAction+Keywords.addmd+" "+currentvarname+"="+Keywords.delete+";\n";
				actualmissingdata.clear();
				modifytable();
			}
		};

		Action okdelsingle = new AbstractAction(Keywords.Language.getMessage(331))
		{
			private static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent evt)
			{
				int testsel=listdelsingle.getSelectedIndex();
				if (testsel>=0)
				{
					String selcode=(String)listdelsingle.getSelectedItem();
					selcode=selcode.trim();
					MissingDataAction=MissingDataAction+Keywords.addmd+" "+currentvarname+"="+selcode+"="+Keywords.delete+";\n";
					actualmissingdata.remove(selcode);
					modifytable();
				}
			}
		};

		Action okaddmd = new AbstractAction(Keywords.Language.getMessage(332))
		{
			private static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent evt)
			{
				String code=textcode.getText();
				if (code.equals(""))
				{
					JOptionPane pane = new JOptionPane(Keywords.Language.getMessage(333));
					JDialog dialog = pane.createDialog(MainGUI.desktop, Keywords.Language.getMessage(134));
					dialog.validate();
					dialog.setVisible(true);
					return;
				}
				MissingDataAction=MissingDataAction+Keywords.addmd+" "+currentvarname+"="+code+";\n";
				textcode.setText("");
				actualmissingdata.put(code,"");
				modifytable();
			}
		};

		TitledBorder titlemissingdata2 = BorderFactory.createTitledBorder(loweredbevel, Keywords.Language.getMessage(334));
		titlemissingdata2.setTitlePosition(TitledBorder.ABOVE_TOP);
		JPanel paneldelmd=new JPanel();
		paneldelmd.setBorder(titlemissingdata2);
		paneldelmd.setLayout(new BoxLayout(paneldelmd,BoxLayout.Y_AXIS));

		JTabbedPane tabpanedel=new JTabbedPane();

		JPanel pandel1=new JPanel(new GridLayout(1,1));
		deleteall=new JButton(delall);
		if (actualmissingdata.size()==0)
			deleteall.setEnabled(false);
		pandel1.add(deleteall);
		tabpanedel.addTab(Keywords.Language.getMessage(335),pandel1);

		JPanel pandel2=new JPanel(new GridLayout(1,2));
		listdelsingle=new JComboBox();
		for (Enumeration<String> en=actualmissingdata.keys(); en.hasMoreElements();)
		{
			String newcode=en.nextElement();
			listdelsingle.addItem(newcode);
		}
		okdeletesingle=new JButton(okdelsingle);
		if (actualmissingdata.size()==0)
		{
			listdelsingle.setVisible(false);
			okdeletesingle.setEnabled(false);
		}
		pandel2.add(listdelsingle);
		pandel2.add(okdeletesingle);
		tabpanedel.addTab(Keywords.Language.getMessage(336),pandel2);
		paneldelmd.add(tabpanedel);
		panelview.add(paneldelmd);

		TitledBorder titleaddmissingdata= BorderFactory.createTitledBorder(loweredbevel, Keywords.Language.getMessage(337));
		titleaddmissingdata.setTitlePosition(TitledBorder.ABOVE_TOP);
		JPanel paneladdmd=new JPanel();
		paneladdmd.setBorder(titleaddmissingdata);
		paneladdmd.setLayout(new BoxLayout(paneladdmd,BoxLayout.Y_AXIS));

		JPanel panadd1=new JPanel(new GridLayout(2,2));
		textcode=new JTextField(5);
		labelcode=new JLabel(Keywords.Language.getMessage(338));
		panadd1.add(labelcode);
		panadd1.add(textcode);
		okadd=new JButton(okaddmd);
		panadd1.add(okadd);

		paneladdmd.add(panadd1);
		panelview.add(paneladdmd);

		Action backa = new AbstractAction(Keywords.Language.getMessage(324))
		{
			private static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent evt)
			{
				MissingDataAction="";
				viewMissingData.dispose();
			}
		};

		Action backactiona = new AbstractAction(Keywords.Language.getMessage(325))
		{
			private static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent evt)
			{
				DataSetViewer.DictionaryCommand=DataSetViewer.DictionaryCommand+MissingDataAction;
				DataSetViewer.missingdata.set(currentvarrif, actualmissingdata);
				viewMissingData.dispose();
			}
		};

		JPanel panelendcode=new JPanel(new GridLayout(2,1));
		back=new JButton(backa);
		backaction=new JButton(backactiona);
		panelendcode.add(back);
		panelendcode.add(backaction);
		panelview.add(panelendcode);

		panelview.add(panelmd);

		modifytable();

		scrolldef = new JScrollPane(panelview);
		scrolldef.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrolldef.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		viewMissingData.getContentPane().add(scrolldef);
		viewMissingData.repaint();
		viewMissingData.setVisible(true);
		viewMissingData.pack();

		int h = viewMissingData.getHeight();
		int w = viewMissingData.getWidth();
		if(h>(MainGUI.desktop.getHeight() - (MainGUI.desktop.getHeight()/14))){
			h = MainGUI.desktop.getHeight() - (MainGUI.desktop.getHeight()/14);
		}

		w = 5*MainGUI.desktop.getWidth()/8;

		viewMissingData.setPreferredSize(new Dimension(w,h));
		viewMissingData.pack();

		MainGUI.desktop.add(viewMissingData);
		MainGUI.desktop.repaint();
		try
		{
			viewMissingData.moveToFront();
			viewMissingData.setEnabled(true);
			viewMissingData.toFront();
			viewMissingData.show();
			viewMissingData.setSelected(true);
		}
		catch (Exception e) {}
	}

	/**
	*Modify the table that contains the missing data rules
	*/
	void modifytable()
	{
		cellData=new Vector<Vector<?>>();
		for (Enumeration<String> e = actualmissingdata.keys() ; e.hasMoreElements() ;)
		{
			Vector<String> tempvector=new Vector<String>();
			String code = e.nextElement();
			tempvector.addElement(code);
			cellData.add(tempvector);
		}
		if (actualmissingdata.size()==0)
		{
			deleteall.setEnabled(false);
			listdelsingle.setVisible(false);
			okdeletesingle.setEnabled(false);
		}
		if (actualmissingdata.size()>0)
		{
			deleteall.setEnabled(true);
			listdelsingle.setVisible(true);
			okdeletesingle.setEnabled(true);
		}
		if (listdelsingle.getItemCount()>0)
			listdelsingle.removeAllItems();
		for (Enumeration<String> e = actualmissingdata.keys() ; e.hasMoreElements() ;)
		{
			String code = e.nextElement();
			listdelsingle.addItem(code);
		}

		DefaultTableModel model =new DefaultTableModel(cellData, columnnames);

		table.setModel(model);

		viewMissingData.pack();

	}
}
