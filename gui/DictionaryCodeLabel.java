/**
* Copyright (c) MS
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
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileFilter;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.TreeMap;
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

import ADaMSoft.dataaccess.DictionaryReader;
import ADaMSoft.keywords.Keywords;

/**
* This is the GUI that permits to add/modify the codelabels of a variable
* @author marco.scarno@gmail.com
* @date 06/09/2015
*/
public class DictionaryCodeLabel implements ActionListener
{
	Border loweredbevel;
	JTable table;
	Vector<Vector<String>> cellData;
	JInternalFrame viewCodeLabel;
	Vector<String> columnnames;
	JScrollPane scrollPane;
	JPanel panelview;
	String CodeLabelAction;
	JInternalFrame viewDict;
	JButton deleteall;
	JButton deletesingle;
	JButton addcodelabel;
	JButton addsetting;
	JButton addother;
	JButton back;
	JButton backaction;
	JComboBox listaddsetting;
	JComboBox listaddpath;
	JComboBox listadddict;
	JComboBox listaddvar;
	JComboBox listdelsingle;
	JTextField textcode;
	JTextField textlabel;
	JButton okdeletesingle;
	JButton nodeletesingle;
	JButton okdeleteall;
	JButton okadd;
	JButton noadd;
	JButton okaddsetting;
	JButton noaddsetting;
	JButton okaddother;
	JButton cancelaction;
	JLabel labelcode, labellabel;
	String currentvarname;
	JScrollPane scrolldef;
	JScrollPane tablescroll;
	JPanel panelcl;
	Hashtable<String, String> actualcodelabel;
	int currentvarrif;
	Vector<String> variables;
	/**
	* This permits to add/modify the codelabels of a variable
	*/
	public DictionaryCodeLabel(int varrif, String varname, JInternalFrame viewDictionary)
	{
		actualcodelabel=DataSetViewer.codelabel.get(varrif);
		variables=new Vector<String>();
		CodeLabelAction="";
		this.viewDict=viewDictionary;
		this.currentvarname=varname;
		currentvarrif=varrif;
		viewDict.setVisible(false);

		boolean resizable = true;
		boolean closeable = true;
		boolean maximizable  = true;
		boolean iconifiable = true;
		String title =Keywords.Language.getMessage(306)+" "+currentvarname;
		viewCodeLabel = new JInternalFrame(title, resizable, closeable, maximizable, iconifiable);
		viewCodeLabel.addInternalFrameListener(new InternalFrameListener()
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
		java.net.URL    url   = DictionaryCodeLabel.class.getResource(Keywords.simpleicon);
		ImageIcon iconSet = new ImageIcon(url);
		panelview=new JPanel();
		panelview.setLayout(new BoxLayout(panelview,BoxLayout.Y_AXIS));

		viewCodeLabel.setFrameIcon(iconSet);

		columnnames=new Vector<String>();
		columnnames.addElement(Keywords.Language.getMessage(307));
		columnnames.addElement(Keywords.Language.getMessage(308));

		Action delall = new AbstractAction(Keywords.Language.getMessage(309))
		{
			private static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent evt)
			{
				if (actualcodelabel.size()==0)
				{
					JOptionPane pane = new JOptionPane(Keywords.Language.getMessage(310));
					JDialog dialog = pane.createDialog(MainGUI.desktop, Keywords.Language.getMessage(134));
					dialog.validate();
					dialog.setVisible(true);
					return;
				}
				CodeLabelAction=CodeLabelAction+Keywords.addcodelabeldictionary+" "+currentvarname+"="+Keywords.delete+";\n";
				actualcodelabel.clear();
				modifytable();
			}
		};

		Action okdelsingle = new AbstractAction(Keywords.Language.getMessage(311))
		{
			private static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent evt)
			{
				int testsel=listdelsingle.getSelectedIndex();
				if (testsel>=0)
				{
					String selcode=(String)listdelsingle.getSelectedItem();
					selcode=selcode.trim();
					CodeLabelAction=CodeLabelAction+Keywords.addcodelabeldictionary+" "+currentvarname+"="+selcode+"="+Keywords.delete+";\n";
					actualcodelabel.remove(selcode);
					modifytable();
				}
			}
		};

		Action okaddcl = new AbstractAction(Keywords.Language.getMessage(312))
		{
			private static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent evt)
			{
				String code=textcode.getText();
				String codevalue=textlabel.getText();
				if (code.equals(""))
				{
					JOptionPane pane = new JOptionPane(Keywords.Language.getMessage(313));
					JDialog dialog = pane.createDialog(MainGUI.desktop, Keywords.Language.getMessage(134));
					dialog.validate();
					dialog.setVisible(true);
					return;
				}
				if (labelcode.equals(""))
				{
					JOptionPane pane = new JOptionPane(Keywords.Language.getMessage(314));
					JDialog dialog = pane.createDialog(MainGUI.desktop, Keywords.Language.getMessage(134));
					dialog.validate();
					dialog.setVisible(true);
					return;
				}
				textcode.setText("");
				textlabel.setText("");
				CodeLabelAction=CodeLabelAction+Keywords.addcodelabeldictionary+" "+currentvarname+"="+code+"="+codevalue+";\n";
				actualcodelabel.put(code, codevalue);
				modifytable();
			}
		};

		Action okaddset = new AbstractAction(Keywords.Language.getMessage(312))
		{
			private static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent evt)
			{
				int testsel=listaddsetting.getSelectedIndex();
				if (testsel>=0)
				{
					String selset=(String)listaddsetting.getSelectedItem();
					CodeLabelAction=CodeLabelAction+Keywords.addcodelabeldictionary+" "+currentvarname+"="+selset+";\n";
					Hashtable<String, String> codelabelset=Keywords.project.getSetting(Keywords.CODELABEL, selset);
					if (!codelabelset.isEmpty())
					{
						for (Enumeration<String> en=codelabelset.keys(); en.hasMoreElements();)
						{
							String newcode=en.nextElement();
							String newvalue=codelabelset.get(newcode);
							newcode=newcode.substring(newcode.indexOf("_")+1);
							actualcodelabel.put(newcode, newvalue);
						}
						modifytable();
					}
				}
			}
		};

		Action okaddo = new AbstractAction(Keywords.Language.getMessage(312))
		{
			private static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent evt)
			{
				okaddother.setEnabled(false);
				String selpath=(String)listaddpath.getSelectedItem();
				String seldict=(String)listadddict.getSelectedItem();
				String selvar=(String)listaddvar.getSelectedItem();
				CodeLabelAction=CodeLabelAction+Keywords.addcodelabeldictionary+" "+currentvarname+"="+selvar+"@"+selpath+"."+seldict+";\n";
				TreeMap<String, String> definedpath=Keywords.project.getNamesAndPaths();
				if (definedpath.size()>0)
				{
					for (Iterator<String> it = definedpath.keySet().iterator(); it.hasNext();) 
					{
						String namepath = it.next();
						String dir      = definedpath.get(namepath);
						if (selpath.equalsIgnoreCase(namepath))
						{
							try
							{
								String tempdict=dir+seldict;
								DictionaryReader readdict = new DictionaryReader(tempdict);
								if (!readdict.getmessageDictionaryReader().equals(""))
								{
									JOptionPane pane = new JOptionPane(readdict.getmessageDictionaryReader());
									JDialog dialog = pane.createDialog(MainGUI.desktop, Keywords.Language.getMessage(134));
									dialog.validate();
									dialog.setVisible(true);
									return;
								}
								Vector<Hashtable<String, String>> newfixedvariableinfo=readdict.getfixedvariableinfo();
								Vector<Hashtable<String, String>> newcodelabel=readdict.getcodelabel();
								for (int j=0; j<newfixedvariableinfo.size(); j++)
								{
									Hashtable<String, String> newtemp=newfixedvariableinfo.get(j);
									String newcheck=newtemp.get(Keywords.VariableName.toLowerCase());
									if (newcheck.equalsIgnoreCase(selvar))
									{
										Hashtable<String, String> tempnewcodelabel= newcodelabel.get(j);
										if (tempnewcodelabel.size()>0)
										{
											for (Enumeration<String> en=tempnewcodelabel.keys(); en.hasMoreElements();)
											{
												String newcode=en.nextElement();
												String newvalue=tempnewcodelabel.get(newcode);
												actualcodelabel.put(newcode, newvalue);
											}
										}
									}
								}
							}
							catch (Exception jj) {}
							modifytable();
						}
					}
				}
				if (listaddvar.getItemCount()>0)
				{
					variables.clear();
					listaddvar.removeAllItems();
				}
				if (listadddict.getItemCount()>0)
					listadddict.removeAllItems();
			}
		};

		TitledBorder titledelcodelabel = BorderFactory.createTitledBorder(loweredbevel, Keywords.Language.getMessage(315));
		titledelcodelabel.setTitlePosition(TitledBorder.ABOVE_TOP);
		JPanel paneldelcl=new JPanel();
		paneldelcl.setBorder(titledelcodelabel);
		paneldelcl.setLayout(new BoxLayout(paneldelcl,BoxLayout.Y_AXIS));

		JTabbedPane tabpanedel=new JTabbedPane();

		JPanel pandel1=new JPanel(new GridLayout(1,1));
		deleteall=new JButton(delall);
		if (actualcodelabel.size()==0)
			deleteall.setEnabled(false);
		pandel1.add(deleteall);
		tabpanedel.addTab(Keywords.Language.getMessage(316),pandel1);

		JPanel pandel2=new JPanel(new GridLayout(1,2));
		listdelsingle=new JComboBox();
		for (Enumeration<String> en=actualcodelabel.keys(); en.hasMoreElements();)
		{
			String newcode=en.nextElement();
			listdelsingle.addItem(newcode);
		}
		okdeletesingle=new JButton(okdelsingle);
		if (actualcodelabel.size()==0)
		{
			listdelsingle.setVisible(false);
			okdeletesingle.setEnabled(false);
		}
		pandel2.add(listdelsingle);
		pandel2.add(okdeletesingle);
		tabpanedel.addTab(Keywords.Language.getMessage(317),pandel2);
		paneldelcl.add(tabpanedel);
		panelview.add(paneldelcl);

		TitledBorder titleaddcodelabel = BorderFactory.createTitledBorder(loweredbevel, Keywords.Language.getMessage(318));
		titleaddcodelabel.setTitlePosition(TitledBorder.ABOVE_TOP);
		JPanel paneladdcl=new JPanel();
		paneladdcl.setBorder(titleaddcodelabel);
		paneladdcl.setLayout(new BoxLayout(paneladdcl,BoxLayout.Y_AXIS));

		JTabbedPane tabpaneadd=new JTabbedPane();

		JPanel panadd1=new JPanel(new GridLayout(3,2));
		textcode=new JTextField(5);
		textlabel=new JTextField(5);
		labelcode=new JLabel(Keywords.Language.getMessage(307));
		labellabel=new JLabel(Keywords.Language.getMessage(308));
		panadd1.add(labelcode);
		panadd1.add(textcode);
		panadd1.add(labellabel);
		panadd1.add(textlabel);
		okadd=new JButton(okaddcl);
		panadd1.add(okadd);
		tabpaneadd.addTab(Keywords.Language.getMessage(317),panadd1);

		JPanel panadd2=new JPanel(new GridLayout(1,2));
		listaddsetting=new JComboBox();
		Vector<String> infoset=Keywords.project.getSettingNames(Keywords.CODELABEL);
		for (int i=0; i<infoset.size(); i++)
		{
			listaddsetting.addItem(infoset.get(i));
		}
		okaddsetting=new JButton(okaddset);
		if (infoset.size()==0)
		{
			listaddsetting.setVisible(false);
			okaddsetting.setEnabled(false);
		}
		panadd2.add(listaddsetting);
		panadd2.add(okaddsetting);
		tabpaneadd.addTab(Keywords.Language.getMessage(319),panadd2);

		JPanel panadd3=new JPanel(new GridLayout(4,2));
		JLabel labaddpath=new JLabel(Keywords.Language.getMessage(320));
		JLabel labadddict=new JLabel(Keywords.Language.getMessage(321));
		JLabel labaddvar=new JLabel(Keywords.Language.getMessage(322));
		listaddpath=new JComboBox();

		listaddpath.addActionListener(this);
		listadddict=new JComboBox();
		listadddict.addActionListener(this);
		listaddvar=new JComboBox();
		listaddvar.addActionListener(this);

		panadd3.add(labaddpath);
		panadd3.add(listaddpath);
		panadd3.add(labadddict);
		panadd3.add(listadddict);
		panadd3.add(labaddvar);
		panadd3.add(listaddvar);
		okaddother=new JButton(okaddo);
		okaddother.setEnabled(false);
		panadd3.add(okaddother);
		tabpaneadd.addTab(Keywords.Language.getMessage(323),panadd3);

		TreeMap<String, String> definedpath=Keywords.project.getNamesAndPaths();
		if (definedpath.size()>0)
		{
			for (Iterator<String> it = definedpath.keySet().iterator(); it.hasNext();) 
			{
				String namepath = it.next();
				listaddpath.addItem(namepath);
			}
		}

		paneladdcl.add(tabpaneadd);
		panelview.add(paneladdcl);

		Action backa = new AbstractAction(Keywords.Language.getMessage(324))
		{
			private static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent evt)
			{
				CodeLabelAction="";
				viewCodeLabel.dispose();
			}
		};

		Action backactiona = new AbstractAction(Keywords.Language.getMessage(325))
		{
			private static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent evt)
			{
				DataSetViewer.DictionaryCommand=DataSetViewer.DictionaryCommand+CodeLabelAction;
				DataSetViewer.codelabel.set(currentvarrif, actualcodelabel);
				viewCodeLabel.dispose();
			}
		};

		JPanel panelendcode=new JPanel(new GridLayout(2,1));
		back=new JButton(backa);
		backaction=new JButton(backactiona);
		panelendcode.add(back);
		panelendcode.add(backaction);
		panelview.add(panelendcode);

		table = new JTable()
		{
			private static final long serialVersionUID = 1L;
			public boolean isCellEditable(int rowIndex, int colIndex)
			{
				return false;
			}
		};

		modifytable();

		table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		table.setColumnSelectionAllowed(false);
		table.setRowSelectionAllowed(false);
		table.setCellSelectionEnabled(false);
		table.setFocusable(false);

		tablescroll = new JScrollPane(table);

		tablescroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		tablescroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		loweredbevel = BorderFactory.createLoweredBevelBorder();
		TitledBorder titlecodelabel = BorderFactory.createTitledBorder(loweredbevel, Keywords.Language.getMessage(326));
		titlecodelabel.setTitlePosition(TitledBorder.ABOVE_TOP);
		panelcl=new JPanel();
		panelcl.setBorder(titlecodelabel);
		panelcl.add(tablescroll);

		panelview.add(panelcl);

		scrolldef = new JScrollPane(panelview);
		scrolldef.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrolldef.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		viewCodeLabel.getContentPane().add(scrolldef);
		viewCodeLabel.repaint();
		viewCodeLabel.setVisible(true);
		viewCodeLabel.pack();

		int h = viewCodeLabel.getHeight();
		int w = viewCodeLabel.getWidth();
		if(h>(MainGUI.desktop.getHeight() - (MainGUI.desktop.getHeight()/14))){
			h = MainGUI.desktop.getHeight() - (MainGUI.desktop.getHeight()/14);
		}

		w = 5*MainGUI.desktop.getWidth()/8;

		viewCodeLabel.setPreferredSize(new Dimension(w,h));
		viewCodeLabel.pack();

		MainGUI.desktop.add(viewCodeLabel);
		MainGUI.desktop.repaint();
		try
		{
			viewCodeLabel.moveToFront();
			viewCodeLabel.setEnabled(true);
			viewCodeLabel.toFront();
			viewCodeLabel.show();
			viewCodeLabel.setSelected(true);
		}
		catch (Exception e) {}
	}
	public void actionPerformed(ActionEvent Event)
	{
		Object Source=Event.getSource();
		if (Source==listaddpath)
		{
			okaddother.setEnabled(false);
			if (listaddvar.getItemCount()>0)
			{
				variables.clear();
				listaddvar.removeAllItems();
			}
			if (listadddict.getItemCount()>0)
				listadddict.removeAllItems();
			String selpath=(String)listaddpath.getSelectedItem();
			TreeMap<String, String> definedpath=Keywords.project.getNamesAndPaths();
			if (definedpath.size()>0)
			{
				for (Iterator<String> it = definedpath.keySet().iterator(); it.hasNext();) 
				{
					String namepath = it.next();
					String dir      = definedpath.get(namepath);
					if (selpath.equalsIgnoreCase(namepath))
					{
						try
						{
							File dirFile = new File(dir);
							FileFilter fileFilter = new FileFilter()
							{
								public boolean accept(File f)
								{
									return f.getName().toLowerCase().endsWith(Keywords.DictionaryExtension.toLowerCase());
								}
							};
							File[] children = dirFile.listFiles(fileFilter);
							if (children != null)
							{
							    for (int f=0; f<children.length; f++)
							    {
							        String filename = children[f].toString();
							        String parentPath = children[f].getParent();
							        String sName=filename.substring(parentPath.length()+1,filename.indexOf(Keywords.DictionaryExtension));
									listadddict.addItem(sName);
							    }
							}
						}
						catch (Exception ex)  {}
					}
				}
			}
		}
		if (Source==listadddict)
		{
			okaddother.setEnabled(false);
			if (listaddvar.getItemCount()>0)
			{
				listaddvar.removeAllItems();
				variables.clear();
			}
			String selpath=(String)listaddpath.getSelectedItem();
			String seldict=(String)listadddict.getSelectedItem();
			if (seldict!=null)
			{
				TreeMap<String, String> definedpath=Keywords.project.getNamesAndPaths();
				if (definedpath.size()>0)
				{
					for (Iterator<String> it = definedpath.keySet().iterator(); it.hasNext();) 
					{
						String namepath = it.next();
						String dir      = definedpath.get(namepath);
						if (selpath.equalsIgnoreCase(namepath))
						{
							try
							{
								seldict=dir+seldict;
								DictionaryReader readdict = new DictionaryReader(seldict);
								if (!readdict.getmessageDictionaryReader().equals(""))
								{
									JOptionPane pane = new JOptionPane(readdict.getmessageDictionaryReader());
									JDialog dialog = pane.createDialog(MainGUI.desktop, Keywords.Language.getMessage(134));
									dialog.validate();
									dialog.setVisible(true);
									return;
								}
								int totalvar=readdict.gettotalvar();
								for (int i=0; i<totalvar; i++)
								{
									listaddvar.addItem(readdict.getvarname(i));
								}
							}
							catch (Exception ex)  {}
						}
					}
				}
			}
		}
		if (Source==listaddvar)
		{
			if (listaddvar.getItemCount()>0)
				okaddother.setEnabled(true);
		}
	}
	/**
	*Modify the table that contains the code labels
	*/
	void modifytable()
	{
		cellData=new Vector<Vector<String>>();
		for (Enumeration<String> e = actualcodelabel.keys() ; e.hasMoreElements() ;)
		{
			Vector<String> tempvector=new Vector<String>();
			String code =e.nextElement();
			String value= actualcodelabel.get(code);
			tempvector.addElement(code);
			tempvector.addElement(value);
			cellData.add(tempvector);
		}
		if (actualcodelabel.size()==0)
		{
			deleteall.setEnabled(false);
			listdelsingle.setVisible(false);
			okdeletesingle.setEnabled(false);
		}
		if (actualcodelabel.size()>0)
		{
			deleteall.setEnabled(true);
			listdelsingle.setVisible(true);
			okdeletesingle.setEnabled(true);
		}
		if (listdelsingle.getItemCount()>0)
			listdelsingle.removeAllItems();
		for (Enumeration<String> e = actualcodelabel.keys() ; e.hasMoreElements() ;)
		{
			String code = e.nextElement();
			listdelsingle.addItem(code);
		}
		DefaultTableModel model =new DefaultTableModel(cellData, columnnames);

		table.setModel(model);

		viewCodeLabel.pack();
	}

}
