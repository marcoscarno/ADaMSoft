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
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileFilter;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
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
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.MenuElement;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;

import ADaMSoft.dataaccess.DictionaryReader;
import ADaMSoft.keywords.Keywords;
import ADaMSoft.supervisor.ExecuteRunner;
import ADaMSoft.utilities.GetRequiredParameters;

/**
* This class builds the GUI that permits to generate the script for a report
* @author marco.scarno@gmail.com
* @date 03/09/2015
*/
public class ReportScriptGenerator implements ActionListener
{
	JTable table;
	Vector<Vector<?>> cellData;
	Vector<String> columnnames;
	Vector<String[]> infotable;
	Hashtable<String, String> definedpath;
	Vector<Object[]> mainstatement=new Vector<Object[]>();
	Vector<Object[]> parameterstatement=new Vector<Object[]>();
	MenuElement [] menuEl;
	JInternalFrame FirstLevelPanel;
	JInternalFrame SecondaryLevelPanel;
	ImageIcon iconSet;
	LinkedList<?> parameters;
	String stepname;
	String commandscript="";
	JComboBox listaddpath;
	JComboBox listadddict;
	JComboBox listreplace;
	JComboBox listdslayout;
	Vector<String[]> actualds;
	JButton selvar;
	JList listvar;
	String valuevar;
	String[] varnames;
	String[] varlabels;
	Vector<String[]> selecteddict;
	JTextField textvarnames;
	JTextField httpdictname;
	JScrollPane tablescroll;
	JPanel secondpanel;
	int rifdict;
	JButton acceptdict;
	JPanel tablepane;
	boolean ishttpdict;
	/**
	*Starts the GUI for the report syntax generator
	*/
	public ReportScriptGenerator(String actualname, LinkedList<?> parameters) throws Exception
	{
		MainGUI.tabsResults.setEnabled(false);
		MainGUI.tabsExecutor.setEnabled(false);
		MainGUI.tabsEnvironment.setEnabled(false);
		Keywords.currentExecutedStep="Report generator";
		ishttpdict=false;
		TreeMap<String, String> temp=Keywords.project.getNamesAndPaths();
		definedpath=new Hashtable<String, String>();
		if (temp.size()>0)
		{
			for (Iterator<String> it = temp.keySet().iterator(); it.hasNext();)
			{
				String namepath = it.next();
				String dir      = temp.get(namepath);
				definedpath.put(namepath, dir);
			}
			String name=definedpath.get("work");
			if (name!=null)
			{
				definedpath.remove("work");
				definedpath.put(Keywords.Language.getMessage(174), System.getProperty(Keywords.WorkDir));
			}
		}
		rifdict=0;
		selecteddict=new Vector<String[]>();
		valuevar="";
		actualds=new Vector<String[]>();

		this.parameters=parameters;
		stepname=actualname;

        JPanel firstpanel = new JPanel(false);
        firstpanel.setLayout(new BoxLayout(firstpanel,BoxLayout.Y_AXIS));

		Iterator<?> i = parameters.iterator();
		while(i.hasNext())
		{
			GetRequiredParameters par =(GetRequiredParameters)i.next();
			if(par.getLevel()==1)
			{
				String parametertype= par.getType();
				String parametername= par.getName();
				int messagecode= par.getLabel();
				boolean isequal=(parametertype.indexOf("=")>0);
				if((parametertype.equalsIgnoreCase("CheckBox")) && (!isequal))
				{
					ParameterCheckBox cb=new ParameterCheckBox(parametername, messagecode, firstpanel, par.isMandatory());
					Object [] infoparameter=new Object[3];
					infoparameter[0]=cb;
					infoparameter[1]=new Boolean(par.isMandatory());
					infoparameter[2]=new Integer(messagecode);
					mainstatement.add(infoparameter);
				}
				if((parametertype.equalsIgnoreCase("path")) && (!isequal))
				{
					ParameterPath pp=new ParameterPath(parametername, messagecode, firstpanel, par.isMandatory());
					Object [] infoparameter=new Object[3];
					infoparameter[0]=pp;
					infoparameter[1]=new Boolean(par.isMandatory());
					infoparameter[2]=new Integer(messagecode);
					mainstatement.add(infoparameter);
				}
				if((parametertype.equalsIgnoreCase("Outdictreport")) && (!isequal))
				{
					ParameterOutdictreport outdict=new ParameterOutdictreport(parametername, messagecode, firstpanel, par.isMandatory());
					Object [] infoparameter=new Object[3];
					infoparameter[0]=outdict;
					infoparameter[1]=new Boolean(par.isMandatory());
					infoparameter[2]=new Integer(messagecode);
					mainstatement.add(infoparameter);
				}
				if (isequal)
				{
					String[] parameterparts=parametertype.split("=");
					if (parameterparts[0].equalsIgnoreCase("Setting"))
					{
						ParameterSetting setting=new ParameterSetting(parameterparts[1], parametername, messagecode, firstpanel, par.isMandatory());
						Object [] infoparameter=new Object[3];
						infoparameter[0]=setting;
						infoparameter[1]=new Boolean(par.isMandatory());
						infoparameter[2]=new Integer(messagecode);
						mainstatement.add(infoparameter);
					}
				}
			}
		}
		Action acceptfirst = new AbstractAction(Keywords.Language.getMessage(165))
		{
			private static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent evt)
			{
				commandscript="Report "+stepname+" ";
				for (int i=0; i<mainstatement.size(); i++)
				{
					Object[] infoparameter=mainstatement.get(i);
					String selection=infoparameter[0].toString();
					boolean mandatory=((Boolean)infoparameter[1]).booleanValue();
					int messagecode=((Integer)infoparameter[2]).intValue();
					if ((mandatory) && (selection.equals("")))
					{
						JOptionPane pane = new JOptionPane(Keywords.Language.getMessage(messagecode));
						JDialog dialog = pane.createDialog(MainGUI.desktop, Keywords.Language.getMessage(166));
						dialog.validate();
						dialog.setVisible(true);
						return;
					}
					commandscript=commandscript+selection+" ";
				}
				commandscript=commandscript.trim()+";\n";
				secondselection();
			}
		};
		Action returnfirst = new AbstractAction(Keywords.Language.getMessage(168))
		{
			private static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent evt)
			{
				FirstLevelPanel.dispose();
			}
		};

		JButton ok = new JButton(acceptfirst);
		JButton back = new JButton(returnfirst);
		JPanel firstgrid=new JPanel(new GridLayout(2,1));

        firstgrid.add(ok);
        firstgrid.add(back);

		Border loweredbevel = BorderFactory.createLoweredBevelBorder();
		TitledBorder titlefirstpanel = BorderFactory.createTitledBorder(loweredbevel, Keywords.Language.getMessage(189));
		firstgrid.setBorder(titlefirstpanel);

        firstpanel.add(firstgrid);

		JScrollPane scrollfirstpanel = new JScrollPane(firstpanel);
		scrollfirstpanel.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollfirstpanel.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		boolean resizable = true;
		boolean closeable = true;
		boolean maximizable  = true;
		boolean iconifiable = true;
		String titleframe =Keywords.Language.getMessage(167)+" (Report "+stepname+")";
		FirstLevelPanel = new JInternalFrame(titleframe, resizable, closeable, maximizable, iconifiable);
		FirstLevelPanel.addInternalFrameListener(new InternalFrameListener()
		{
			public void internalFrameClosing(InternalFrameEvent e)
			{
				for (int j=0; j<menuEl.length; j++)
				{
					MainGUI.tabsResults.setEnabled(true);
					MainGUI.tabsExecutor.setEnabled(true);
					MainGUI.tabsEnvironment.setEnabled(true);
					Keywords.currentExecutedStep="";
				}
			}
			public void internalFrameClosed(InternalFrameEvent e)
			{
				MainGUI.tabsResults.setEnabled(true);
				MainGUI.tabsExecutor.setEnabled(true);
				MainGUI.tabsEnvironment.setEnabled(true);
				Keywords.currentExecutedStep="";
			}
			public void internalFrameOpened(InternalFrameEvent e) {}
			public void internalFrameIconified(InternalFrameEvent e) {}
			public void internalFrameDeiconified(InternalFrameEvent e) {}
			public void internalFrameActivated(InternalFrameEvent e) {}
			public void internalFrameDeactivated(InternalFrameEvent e){}
		});
		java.net.URL    url   = ReportScriptGenerator.class.getResource(Keywords.simpleicon);
		iconSet = new ImageIcon(url);
		int numrow = new Double(MainGUI.frame.getHeight()).intValue() - 52;
		int numcol = new Double(MainGUI.frame.getWidth()).intValue() - 14;
		FirstLevelPanel.setSize(numcol, numrow);
		FirstLevelPanel.setFrameIcon(iconSet);
		FirstLevelPanel.getContentPane().add(scrollfirstpanel);
		FirstLevelPanel.repaint();
		FirstLevelPanel.setVisible(true);
		FirstLevelPanel.pack();
		MainGUI.desktop.add(FirstLevelPanel);
		MainGUI.desktop.repaint();
		try
		{
			FirstLevelPanel.setEnabled(true);
			FirstLevelPanel.toFront();
			FirstLevelPanel.show();
			FirstLevelPanel.setSelected(true);
		}
		catch (Exception e) {}
	}
	public void secondselection()
	{
		FirstLevelPanel.setVisible(false);
		boolean resizable = true;
		boolean closeable = true;
		boolean maximizable  = true;
		boolean iconifiable = true;
		String title =Keywords.Language.getMessage(169)+" (Report "+stepname+")";
		SecondaryLevelPanel = new JInternalFrame(title, resizable, closeable, maximizable, iconifiable);

		table = new JTable()
		{
			private static final long serialVersionUID = 1L;
			public boolean isCellEditable(int rowIndex, int colIndex)
			{
				return false;
			}
		};
		table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		table.setColumnSelectionAllowed(false);
		table.setRowSelectionAllowed(false);
		table.setCellSelectionEnabled(false);
		table.setFocusable(false);

		tablescroll = new JScrollPane(table);
		tablescroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		tablescroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		columnnames=new Vector<String>();
		columnnames.addElement(Keywords.Language.getMessage(472));
		columnnames.addElement(Keywords.Language.getMessage(473));
		columnnames.addElement(Keywords.Language.getMessage(474));
		columnnames.addElement(Keywords.Language.getMessage(475));

		secondpanel = new JPanel(false);
		secondpanel.setLayout(new BoxLayout(secondpanel,BoxLayout.Y_AXIS));

		JPanel dspanel=new JPanel(new GridLayout(6,1));

		listaddpath=new JComboBox();
		listaddpath.addActionListener(this);
		listadddict=new JComboBox();
		listadddict.setVisible(false);
		listadddict.addActionListener(this);
		httpdictname=new JTextField(10);
		httpdictname.setVisible(false);
		JLabel labpath=new JLabel(Keywords.Language.getMessage(320));
		JLabel labdict=new JLabel(Keywords.Language.getMessage(321));

		selvar=new JButton(Keywords.Language.getMessage(452));
		selvar.addActionListener(this);

		textvarnames=new JTextField(10);
		textvarnames.setVisible(false);

		Action Accept = new AbstractAction(Keywords.Language.getMessage(471))
		{
			private static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent evt)
			{
				String selpath=(String)listaddpath.getSelectedItem();
				selpath=selpath.trim();
				if (selpath.equalsIgnoreCase(Keywords.Language.getMessage(174)))
					selpath="";
				else
					selpath=selpath+".";
				String dictname="";
				if (!ishttpdict)
					dictname=(String)listadddict.getSelectedItem();
				else
					dictname=httpdictname.getText();
				if ((ishttpdict) && (dictname.equals("")))
				{
					JOptionPane pane = new JOptionPane(Keywords.Language.getMessage(1212));
					JDialog dialog = pane.createDialog(MainGUI.desktop, Keywords.Language.getMessage(166));
					dialog.validate();
					dialog.setVisible(true);
					return;
				}
				String var=textvarnames.getText();
				String replace=(String)listreplace.getSelectedItem();
				if (replace.equalsIgnoreCase(Keywords.Language.getMessage(454)))
					replace="replace="+Keywords.replaceall;
				else if (replace.equalsIgnoreCase(Keywords.Language.getMessage(455)))
					replace="replace="+Keywords.replaceformat;
				else if (replace.equalsIgnoreCase(Keywords.Language.getMessage(456)))
					replace="replace="+Keywords.replacemissing;
				else
					replace="";
				String sellay=(String)listdslayout.getSelectedItem();
				if (sellay==null)
					sellay="";
				if (sellay.equalsIgnoreCase(Keywords.Language.getMessage(190)))
					sellay="";
				String[] infods=new String[4];
				infods[0]=selpath+dictname.trim();
				infods[1]=var.trim();
				infods[2]=replace.trim();
				infods[3]=sellay.trim();
				selecteddict.add(rifdict, infods);
				modifytable();
				rifdict++;
				acceptdict.setVisible(false);
			}
		};

		if (definedpath.size()>0)
		{
			String firstpath="";
			for (Enumeration<String> e = definedpath.keys() ; e.hasMoreElements() ;)
			{
				String namepath = e.nextElement();
				if (firstpath.equals(""))
					firstpath=namepath;
				listaddpath.addItem(namepath);
			}
			if (!firstpath.startsWith("http://"))
			{
				File dirFile = new File(firstpath);
				FileFilter fileFilter = new FileFilter()
				{
					public boolean accept(File f)
					{
						return f.getName().toLowerCase().endsWith(Keywords.DictionaryExtension.toLowerCase());
					}
				};
				File[] newdicts = dirFile.listFiles(fileFilter);
				if(newdicts!=null)
				{
					for(int i=0;i<newdicts.length;i++)
					{
						listadddict.addItem(newdicts[i]);
					}
					listadddict.setVisible(true);
					textvarnames.setVisible(true);
					httpdictname.setVisible(false);
				}
				ishttpdict=false;
			}
			else
			{
				ishttpdict=true;
				httpdictname.setVisible(true);
				textvarnames.setVisible(true);
			}
		}

		dspanel.add(labpath);
		dspanel.add(listaddpath);
		dspanel.add(labdict);
		dspanel.add(listadddict);
		dspanel.add(httpdictname);

		JPanel varpanel=new JPanel(new GridLayout(2,1));

		JPanel reppanel=new JPanel(new GridLayout(1,1));

		listreplace=new JComboBox();
		listreplace.addItem(Keywords.Language.getMessage(453));
		listreplace.addItem(Keywords.Language.getMessage(454));
		listreplace.addItem(Keywords.Language.getMessage(455));
		listreplace.addItem(Keywords.Language.getMessage(456));

		reppanel.add(listreplace);

		listdslayout=new JComboBox();
		Vector<String> infoset=Keywords.project.getSettingNames(stepname+"dslayout");
		if (infoset.size()>0)
			infoset.add(0, Keywords.Language.getMessage(190));
		listdslayout=new JComboBox(infoset);

		varpanel.add(selvar);
		varpanel.add(textvarnames);

		secondpanel.add(dspanel);
		secondpanel.add(varpanel);

		Border loweredrepbevel = BorderFactory.createLoweredBevelBorder();
		TitledBorder titlereppanel = BorderFactory.createTitledBorder(loweredrepbevel, Keywords.Language.getMessage(458));
		reppanel.setBorder(titlereppanel);

		secondpanel.add(reppanel);

		JPanel lay=new JPanel(new GridLayout(1,1));

		lay.add(listdslayout);

		Border loweredlaybevel = BorderFactory.createLoweredBevelBorder();
		TitledBorder titlelaypanel = BorderFactory.createTitledBorder(loweredlaybevel, Keywords.Language.getMessage(459));
		lay.setBorder(titlelaypanel);

		secondpanel.add(lay);

		Action executestep = new AbstractAction(Keywords.Language.getMessage(170))
		{
			private static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent evt)
			{
				String actualcommand=commandscript;
				for (int i=0; i<selecteddict.size(); i++)
				{
					String[] tempdict=selecteddict.get(i);
					actualcommand=actualcommand+"dict="+tempdict[0]+" ";
					if (!tempdict[1].equals(""))
						actualcommand=actualcommand+"var="+tempdict[1]+" ";
					if (!tempdict[2].equals(""))
						actualcommand=actualcommand+tempdict[2]+" ";
					if (!tempdict[3].equals(""))
						actualcommand=actualcommand+stepname+"dslayout="+tempdict[3]+" ";
					actualcommand=actualcommand.trim()+";\n";
				}
				actualcommand=actualcommand+"run;\n";
				new ExecuteRunner(2, actualcommand);
				Keywords.currentExecutedStep="Report generator";
			}
		};
		Action putstep = new AbstractAction(Keywords.Language.getMessage(172))
		{
			private static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent evt)
			{
				String actualcommand=commandscript;
				for (int i=0; i<selecteddict.size(); i++)
				{
					String[] tempdict=selecteddict.get(i);
					actualcommand=actualcommand+"dict="+tempdict[0]+" ";
					if (!tempdict[1].equals(""))
						actualcommand=actualcommand+"var="+tempdict[1]+" ";
					if (!tempdict[2].equals(""))
						actualcommand=actualcommand+tempdict[2]+" ";
					if (!tempdict[3].equals(""))
						actualcommand=actualcommand+stepname+"dslayout="+tempdict[3]+" ";
					actualcommand=actualcommand.trim()+";\n";
				}
				actualcommand=actualcommand+"run;\n";
				Document doc = MainGUI.EditorArea.getDocument();
				Document blank = new DefaultStyledDocument();
				MainGUI.EditorArea.setDocument(blank);
				try
				{
					doc.insertString(doc.getLength(), "\n"+actualcommand+"\n", null);
				}
				catch (BadLocationException ef) {}
				MainGUI.EditorArea.setDocument(doc);
				MainGUI.EditorArea.setCaretPosition(MainGUI.EditorArea.getDocument().getLength());
			}
		};
		Action backtofirst = new AbstractAction(Keywords.Language.getMessage(173))
		{
			private static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent evt)
			{
				SecondaryLevelPanel.dispose();
			}
		};
		Action backtogui = new AbstractAction(Keywords.Language.getMessage(168))
		{
			private static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent evt)
			{
				SecondaryLevelPanel.dispose();
				FirstLevelPanel.dispose();
			}
		};

		JPanel acceptgrid=new JPanel(new GridLayout(1,1));
		acceptdict=new JButton(Accept);
		acceptdict.setVisible(false);
		acceptgrid.add(acceptdict);

		secondpanel.add(acceptgrid);

		JButton exestep = new JButton(executestep);
		JButton savestep = new JButton(putstep);
		JButton exitsecond = new JButton(backtofirst);
		JButton exittogui = new JButton(backtogui);

		JPanel secondgrid=new JPanel(new GridLayout(4,1));

		secondgrid.add(exestep);
		secondgrid.add(savestep);
		secondgrid.add(exitsecond);
		secondgrid.add(exittogui);

		Border loweredbevel = BorderFactory.createLoweredBevelBorder();
		TitledBorder titlefirstpanel = BorderFactory.createTitledBorder(loweredbevel, Keywords.Language.getMessage(189));
		secondgrid.setBorder(titlefirstpanel);

   		secondpanel.add(secondgrid);

		tablepane=new JPanel();

		Border loweredtableevel = BorderFactory.createLoweredBevelBorder();
		TitledBorder titletablepanel = BorderFactory.createTitledBorder(loweredtableevel, Keywords.Language.getMessage(476));
		tablepane.setBorder(titletablepanel);

		tablepane.add(tablescroll);

		secondpanel.add(tablepane);

		int numrow = new Double(MainGUI.frame.getHeight()).intValue() - 52;
		int numcol = new Double(MainGUI.frame.getWidth()).intValue() - 14;

		secondpanel.setSize(numcol, numrow);

		JScrollPane scrollsecondpanel = new JScrollPane(secondpanel);

		scrollsecondpanel.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollsecondpanel.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollsecondpanel.setSize(numcol, numrow);

		SecondaryLevelPanel.addInternalFrameListener(new InternalFrameListener()
		{
			public void internalFrameClosing(InternalFrameEvent e)
			{
				FirstLevelPanel.setVisible(true);
			}
			public void internalFrameClosed(InternalFrameEvent e)
			{
				FirstLevelPanel.setVisible(true);
			}
			public void internalFrameOpened(InternalFrameEvent e) {}
			public void internalFrameIconified(InternalFrameEvent e) {}
			public void internalFrameDeiconified(InternalFrameEvent e) {}
			public void internalFrameActivated(InternalFrameEvent e) {}
			public void internalFrameDeactivated(InternalFrameEvent e){}
		});
		java.net.URL    url   = ReportScriptGenerator.class.getResource(Keywords.simpleicon);
		iconSet = new ImageIcon(url);
		SecondaryLevelPanel.setSize(numcol, numrow);
		SecondaryLevelPanel.setFrameIcon(iconSet);
		SecondaryLevelPanel.getContentPane().add(scrollsecondpanel);
		SecondaryLevelPanel.repaint();
		SecondaryLevelPanel.setVisible(true);
		SecondaryLevelPanel.pack();

		int h = SecondaryLevelPanel.getHeight();
		int w = SecondaryLevelPanel.getWidth();
		if(h>(MainGUI.desktop.getHeight() - (MainGUI.desktop.getHeight()/14))){
			h = MainGUI.desktop.getHeight() - (MainGUI.desktop.getHeight()/14);
		}
		if(w>(5*MainGUI.desktop.getWidth()/7)){
			w = 5*MainGUI.desktop.getWidth()/7;
		}

		scrollsecondpanel.setPreferredSize(new Dimension(w,h));
		SecondaryLevelPanel.pack();

		MainGUI.desktop.add(SecondaryLevelPanel);
		MainGUI.desktop.repaint();
		try
		{
			SecondaryLevelPanel.setEnabled(true);
			SecondaryLevelPanel.toFront();
			SecondaryLevelPanel.show();
			SecondaryLevelPanel.setSelected(true);
		}
		catch (Exception e) {}
	}
	/**
	*Modify the table that contains the dictionaries to be exported
	*/
	void modifytable()
	{
		cellData=new Vector<Vector<?>>();
		for (int i=0; i<selecteddict.size(); i++)
		{
			Vector<String> tempvector=new Vector<String>();
			String [] tempinfotable= selecteddict.get(i);
			for (int j=0; j< tempinfotable.length; j++)
			{
				tempvector.add(tempinfotable[j]);
			}
			cellData.add(tempvector);
		}
		DefaultTableModel model = new DefaultTableModel(cellData,columnnames);

		table.setModel(model);

		SecondaryLevelPanel.pack();
	}
	public void actionPerformed(ActionEvent Event)
	{
		Object Source=Event.getSource();
		if (Source==listaddpath)
		{
			if (listadddict.getItemCount()>0)
				listadddict.removeAllItems();
			String selpath=(String)listaddpath.getSelectedItem();
			if (selpath.equalsIgnoreCase(Keywords.Language.getMessage(174)))
			{
				try
				{
					File dirFile = new File(System.getProperty(Keywords.WorkDir));
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
						listadddict.setVisible(true);
					}
					ishttpdict=false;
					httpdictname.setVisible(false);
					httpdictname.setText("");
				}
				catch (Exception ex)  {}
			}
			else
			{
				if (definedpath.size()>0)
				{
					for (Enumeration<String> e = definedpath.keys() ; e.hasMoreElements() ;)
					{
						String namepath = e.nextElement();
						String dir      = definedpath.get(namepath);
						if ((selpath.equalsIgnoreCase(namepath)) && (!dir.toLowerCase().startsWith("http")))
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
									listadddict.setVisible(true);
								}
								ishttpdict=false;
								selvar.setVisible(true);
								textvarnames.setVisible(true);
								acceptdict.setVisible(true);
								httpdictname.setVisible(false);
								httpdictname.setText("");
							}
							catch (Exception ex)  {}
						}
						else
						{
							listadddict.setVisible(false);
							ishttpdict=true;
							httpdictname.setVisible(true);
							selvar.setVisible(true);
							textvarnames.setVisible(true);
							httpdictname.setText("");
						}
					}
				}
			}
		}
		if (Source==selvar)
		{
			valuevar="";
			String dictpath=(String)listaddpath.getSelectedItem();
			if (dictpath.equalsIgnoreCase(Keywords.Language.getMessage(174)))
				dictpath=System.getProperty(Keywords.WorkDir);
			else
			{
				for (Enumeration<String> e = definedpath.keys() ; e.hasMoreElements() ;)
				{
					String namepath = e.nextElement();
					String dir      = definedpath.get(namepath);
					if (dictpath.equalsIgnoreCase(namepath))
						dictpath=dir;
				}
			}
			String dictname=(String)listadddict.getSelectedItem();
			String hdictname=httpdictname.getText();
			if (!ishttpdict)
			{
				if (!hdictname.equals(""))
					dictname=hdictname;
				httpdictname.setText("");
			}
			if ((ishttpdict) && (hdictname.equals("")))
			{
				JOptionPane pane = new JOptionPane(Keywords.Language.getMessage(1212));
				JDialog dialog = pane.createDialog(MainGUI.desktop, Keywords.Language.getMessage(166));
				dialog.validate();
				dialog.setVisible(true);
				return;
			}
			DictionaryReader dr=new DictionaryReader(dictpath+dictname);
			if (!dr.getmessageDictionaryReader().equals(""))
			{
				JOptionPane pane = new JOptionPane(dr.getmessageDictionaryReader());
				JDialog dialog = pane.createDialog(MainGUI.desktop, Keywords.Language.getMessage(134));
				dialog.validate();
				dialog.setVisible(true);
				return;
			}
			acceptdict.setVisible(true);
			Vector<String> varlabel=new Vector<String>();
			varnames=new String[dr.gettotalvar()];
			varlabels=new String[dr.gettotalvar()];
			for (int i=0; i<dr.gettotalvar(); i++)
			{
				varnames[i]=dr.getvarname(i);
				varlabels[i]=dr.getvarlabel(i);
				varlabel.add(dr.getvarlabel(i));
			}
			boolean resizableg = true;
			boolean closeableg = true;
			boolean maximizableg  = true;
			boolean iconifiableg = false;
			final JInternalFrame var = new JInternalFrame(Keywords.Language.getMessage(452), resizableg, closeableg, maximizableg, iconifiableg);
			SecondaryLevelPanel.hide();
			listvar=new JList(varlabel)
	        {
				private static final long serialVersionUID = 1L;
				public String getToolTipText(MouseEvent evt)
				{
					int index = locationToIndex(evt.getPoint());
					return varnames[index];
				}
			};

			listvar.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

			listvar.setVisibleRowCount(5);

			JScrollPane scroll = new JScrollPane(listvar);
			scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
			scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

			Action actionvar = new AbstractAction(Keywords.Language.getMessage(457))
			{
				private static final long serialVersionUID = 1L;
				public void actionPerformed(ActionEvent evt)
				{
					Object [] selectedvariables=listvar.getSelectedValues();
		            valuevar="";
		            for (int i=0; i<selectedvariables.length; i++)
		            {
						for (int j=0; j<varlabels.length; j++)
						{
							String tempname=(String)selectedvariables[i];
							if (tempname.equalsIgnoreCase(varlabels[j]))
								valuevar=valuevar+" "+varnames[j];
						}
					}
					valuevar.trim();
					textvarnames.setText(valuevar);
					var.dispose();
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

			var.addInternalFrameListener(new InternalFrameListener()
			{
				public void internalFrameClosing(InternalFrameEvent e)
				{
					SecondaryLevelPanel.show();
				}
				public void internalFrameClosed(InternalFrameEvent e)
				{
					SecondaryLevelPanel.show();
				}
				public void internalFrameOpened(InternalFrameEvent e) {}
				public void internalFrameIconified(InternalFrameEvent e) {}
				public void internalFrameDeiconified(InternalFrameEvent e) {}
				public void internalFrameActivated(InternalFrameEvent e) {}
				public void internalFrameDeactivated(InternalFrameEvent e){}
			});

			var.setSize(numcol, numrow);
			java.net.URL    url   = ReportScriptGenerator.class.getResource(Keywords.simpleicon);
			ImageIcon iconSet = new ImageIcon(url);
			var.setFrameIcon(iconSet);

			var.getContentPane().add(scrollpanelvariables);
			var.repaint();
			var.pack();
			var.setVisible(true);
			MainGUI.desktop.add(var);
			MainGUI.desktop.repaint();
			try
			{
				var.setEnabled(true);
				var.toFront();
				var.show();
				var.setSelected(true);
			}
			catch (Exception e) {}
		}
	}
}
