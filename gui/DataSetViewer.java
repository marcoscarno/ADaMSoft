/**
* Copyright (c) 2017 MS
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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.BufferedWriter;
import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileWriter;
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
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.MenuElement;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;

import ADaMSoft.dataaccess.DataReader;
import ADaMSoft.dataaccess.DictionaryReader;
import ADaMSoft.keywords.Keywords;
import ADaMSoft.supervisor.ExecuteRunner;
import ADaMSoft.utilities.MessagesReplacer;
import ADaMSoft.utilities.NewWriteFormat;
import ADaMSoft.utilities.StringComparatorNoC;

/**
* This is the GUI to view a Data Set (both the dictionary and the data table)
* @author marco.scarno@gmail.com
* @date 13/02/2017
*/
public class DataSetViewer implements ActionListener
{
	JTextField condition;
	JScrollPane scroll;
	JPanel panelviewdt;
	Border loweredbevel;
	JTable table;
	JTable tableds;
	JInternalFrame viewDataSet;
	JScrollPane scrollPane;
	JScrollPane scrollPaneds;
	JComboBox typeview;
	MenuElement [] menuElement;
	JMenuBar menuDataSetViewer;
	JMenu menuFile, saveHtmldt;
	JMenuItem saveHtmlDict, saveHtmldt1, saveHtmldt2, exitmenu;
	JTextField description, keyword, minvalue, maxvalue;
	public static String DictionaryCommand="";
	JTextField[] fieldvarname;
	JTextField[] fieldvarlabel;
	JTextField[] fieldvarformat;
	JButton[] modifyname;
	JButton[] modifylabel;
	JButton[] modifyformat;
	int pointvar;
	JTabbedPane tabVariable;
	JScrollPane[] scrollVar;
	JPanel[] currentVar;
	JComboBox listpath;
	JTextField outdictname;
	String dictionary;
	String namepath;
	String dictname;
	public static Vector<Hashtable<String, String>> codelabel;
	public static Vector<Hashtable<String, String>> missingdata;
	int records;
	DataReader data;
	Vector<Vector<?>> cellData;
	Vector<String> columnnames;
	Vector<String> origcolumnnames;
	int replacer=0;
	JButton view;
	String[] values=null;
	JTabbedPane tabbedPane;
	int maxdatabuffer;
	DictionaryReader dr;
	boolean dictmodified;
	boolean tableopened;
	String errorreadingtable;
	JButton[] mstat;
	JButton[] mfreq;
	int reftotvar;
	JCheckBox onlytwodec;
	JCheckBox numasos;
	boolean zerorecord;
	protected String[] columnToolTips;
	int maxr;
	JCheckBox wraptcell;
	private BufferedWriter out;
	String dsSelection;
	/**
	* This is the method that visualize a dictionary by receiving its name and the path where it is stored.
	*/
	public DataSetViewer(String dictin, String path, String dictinname, final String dsSelection)
    {
		this.dsSelection=dsSelection;
		maxr=0;
		zerorecord=false;
		reftotvar=0;
		errorreadingtable="";
		tableopened=false;
		dictmodified=false;
		columnnames=new Vector<String>();
		origcolumnnames=new Vector<String>();
		cellData=new Vector<Vector<?>>();
		tabbedPane = new JTabbedPane();
		DictionaryCommand="";
		this.dictionary=dictin.trim();
		this.namepath=path;
		this.dictname=dictinname;
		dr=new DictionaryReader(dictionary);
		if (!dr.getmessageDictionaryReader().equals(""))
		{
			JOptionPane pane = new JOptionPane(dr.getmessageDictionaryReader());
			JDialog dialog = pane.createDialog(MainGUI.desktop, Keywords.Language.getMessage(134));
			dialog.validate();
			dialog.setVisible(true);
			Keywords.currentExecutedStep="";
			Keywords.opened_datasets.remove(dsSelection);
			return;
		}
		data=new DataReader(dr);
		records=data.getRecords();
		if (records==0)
		{
			zerorecord=true;
			errorreadingtable=data.getmessage();
			JOptionPane pane = new JOptionPane(MessagesReplacer.replaceMessages(errorreadingtable));
			JDialog dialog = pane.createDialog(MainGUI.desktop, Keywords.Language.getMessage(134));
			dialog.validate();
			dialog.setVisible(true);
		}
		tableds = new JTable()
		{
			private static final long serialVersionUID = 1L;
			public boolean isCellEditable(int rowIndex, int colIndex)
			{
				return false;
			}
		};
		try
		{
			reftotvar=dr.gettotalvar();
		}
		catch (Exception e) {}
		tableds.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		tableds.setColumnSelectionAllowed(false);
		tableds.setRowSelectionAllowed(false);
		tableds.setCellSelectionEnabled(false);
		tableds.setFocusable(false);
		scrollPaneds = new JScrollPane(tableds);
		scrollPaneds.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPaneds.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

		codelabel=dr.getcodelabel();
		missingdata=dr.getmissingdata();
		java.net.URL    url   = DataSetViewer.class.getResource(Keywords.simpleicon);
		ImageIcon iconSet = new ImageIcon(url);
		JLabel labeldate=new JLabel(Keywords.Language.getMessage(280));
		JLabel labelauthor=new JLabel(Keywords.Language.getMessage(281));
		JLabel labeldatatype=new JLabel(Keywords.Language.getMessage(282));
		JLabel labeldescription=new JLabel(Keywords.Language.getMessage(283));
		JLabel labelkeyword=new JLabel(Keywords.Language.getMessage(284));

		JLabel date=new JLabel(dr.getcreationdate());
		JLabel author=new JLabel(dr.getauthor());
		description=new JTextField(10);
		description.setText(dr.getdescription());
		description.setToolTipText(dr.getdescription());
		keyword=new JTextField(10);
		keyword.setText(dr.getkeyword());
		keyword.setToolTipText(dr.getkeyword());
		keyword.setEditable(false);
		description.setEditable(false);
		JLabel datatype=new JLabel(dr.getdatatabletype());
		JPanel firstpanel=new JPanel(new GridLayout(5,3));

		Action visualize = new AbstractAction(Keywords.Language.getMessage(340))
		{
			private static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent evt)
			{
				if (dictmodified)
				{
					data=new DataReader(dr);
				}
				tabbedPane.setEnabledAt(1, false);
				dictmodified=false;
				tableopened=true;
				view.setVisible(false);
				int minobs=1;
				int maxobs=records;
				try
				{
					minobs=Integer.parseInt(minvalue.getText());
				}
				catch (Exception e) {}
				try
				{
					maxobs=Integer.parseInt(maxvalue.getText());
				}
				catch (Exception e) {}
				String textcondition=condition.getText();
				String[] varcondition=null;
				String[] valuescondit=null;
				int[] positioncondition=null;
				int[] typecondition=null;
				Vector<Hashtable<String, String>> var=dr.getfixedvariableinfo();
				if (!textcondition.trim().equals(""))
				{
					try
					{
						if (textcondition.indexOf("AND")>0)
						{
							textcondition=textcondition.replaceAll("AND",";");
						}
						String[] partconditio=textcondition.split(";");
						varcondition=new String[partconditio.length];
						valuescondit=new String[partconditio.length];
						typecondition=new int[partconditio.length];
						for (int i=0; i<partconditio.length; i++)
						{
							partconditio[i]=partconditio[i].trim();
							String[] temppartc=null;
							if (partconditio[i].indexOf("=")>0)
							{
								typecondition[i]=0;
								temppartc=partconditio[i].split("=");
							}
							if (partconditio[i].indexOf("!=")>0)
							{
								typecondition[i]=3;
								temppartc=partconditio[i].split("!=");
							}
							if (partconditio[i].indexOf(">")>0)
							{
								typecondition[i]=1;
								temppartc=partconditio[i].split(">");
							}
							if (partconditio[i].indexOf("<")>0)
							{
								typecondition[i]=-1;
								temppartc=partconditio[i].split("<");
							}
							if (partconditio[i].indexOf(">=")>0)
							{
								typecondition[i]=2;
								temppartc=partconditio[i].split(">=");
							}
							if (partconditio[i].indexOf("<=")>0)
							{
								typecondition[i]=-2;
								temppartc=partconditio[i].split("<=");
							}
							varcondition[i]=temppartc[0].trim();
							valuescondit[i]=temppartc[1].trim();
						}
						positioncondition=new int[partconditio.length];
						for (int i=0; i<partconditio.length; i++)
						{
							boolean varexistcond=false;
							for(int k=0; k<var.size(); k++)
							{
								Hashtable<String, String> currentvar=var.get(k);
								String tempnamec=(currentvar.get(Keywords.VariableName.toLowerCase())).trim();
								if (tempnamec.toLowerCase().equals(varcondition[i].toLowerCase()))
								{
									positioncondition[i]=k;
									varexistcond=true;
								}
							}
							if (!varexistcond)
							{
								view.setVisible(true);
								tabbedPane.setEnabledAt(1, true);
								JOptionPane pane = new JOptionPane(Keywords.Language.getMessage(2598)+": "+varcondition[i].toLowerCase());
								JDialog dialog = pane.createDialog(MainGUI.desktop, Keywords.Language.getMessage(134));
								dialog.validate();
								dialog.setVisible(true);
								return;
							}
						}
					}
					catch (Exception econd)
					{
						view.setVisible(true);
						tabbedPane.setEnabledAt(1, true);
						JOptionPane pane = new JOptionPane(Keywords.Language.getMessage(2597));
						JDialog dialog = pane.createDialog(MainGUI.desktop, Keywords.Language.getMessage(134));
						dialog.validate();
						dialog.setVisible(true);
						return;
					}
				}
				if ((maxobs-minobs)+1>maxr)
				{
					view.setVisible(true);
					tabbedPane.setEnabledAt(1, true);
					JOptionPane pane = new JOptionPane(Keywords.Language.getMessage(342));
					JDialog dialog = pane.createDialog(MainGUI.desktop, Keywords.Language.getMessage(134));
					dialog.validate();
					dialog.setVisible(true);
					return;
				}
				if (minobs<1)
				{
					view.setVisible(true);
					tabbedPane.setEnabledAt(1, true);
					JOptionPane pane = new JOptionPane(Keywords.Language.getMessage(343));
					JDialog dialog = pane.createDialog(MainGUI.desktop, Keywords.Language.getMessage(134));
					dialog.validate();
					dialog.setVisible(true);
					return;
				}
				String typeselection=(String)typeview.getSelectedItem();
				replacer=1;
				if (typeselection.equalsIgnoreCase(Keywords.Language.getMessage(345)))
					replacer=0;
				cellData=new Vector<Vector<?>>();
				columnnames=new Vector<String>();
				origcolumnnames=new Vector<String>();
				boolean istowrite=false;
				int[] replace=new int[var.size()];
				Vector<String> writeformat=new Vector<String>();
				columnToolTips=new String[var.size()+1];
				if (replacer==1)
				{
					columnToolTips[0]="NREAD";
					columnnames.addElement(Keywords.Language.getMessage(2226));
					for(int k=0; k<var.size(); k++)
					{
						Hashtable<String, String> currentvar=var.get(k);
						columnToolTips[1+k]=currentvar.get(Keywords.VariableName.toLowerCase());
						writeformat.add(dr.getvarformatfromname(currentvar.get(Keywords.VariableName.toLowerCase())));
						columnnames.addElement(currentvar.get(Keywords.LabelOfVariable.toLowerCase()));
						origcolumnnames.addElement(currentvar.get(Keywords.LabelOfVariable.toLowerCase()));
						replace[k]=1;
					}
					istowrite=true;
				}
				else
				{
					columnToolTips[0]=Keywords.Language.getMessage(2226);
					columnnames.addElement("NREAD");
					for(int k=0; k<var.size(); k++)
					{
						Hashtable<String, String> currentvar=var.get(k);
						columnToolTips[1+k]=currentvar.get(Keywords.LabelOfVariable.toLowerCase());
						writeformat.add(dr.getvarformatfromname(currentvar.get(Keywords.VariableName.toLowerCase())));
						columnnames.addElement(currentvar.get(Keywords.VariableName.toLowerCase()));
						origcolumnnames.addElement(currentvar.get(Keywords.VariableName.toLowerCase()));
						replace[k]=0;
					}
				}
				boolean isselonlytwodec=onlytwodec.isSelected();
				boolean isselnumasos=numasos.isSelected();
				boolean newrep=false;
				if (isselonlytwodec)
					newrep=true;
				if (isselnumasos)
					newrep=true;
				if (newrep)
					istowrite=false;
				int defdec=-1;
				if (isselonlytwodec)
					defdec=2;
				if (!data.open(null, replace, istowrite))
				{
					view.setVisible(true);
					tabbedPane.setEnabledAt(1, true);
					String errormsg=MessagesReplacer.replaceMessages(data.getmessage());
					JOptionPane pane = new JOptionPane(errormsg);
					JDialog dialog = pane.createDialog(MainGUI.desktop, Keywords.Language.getMessage(134));
					dialog.validate();
					dialog.setVisible(true);
					return;
				}
				int currentobs=0;
				int realobs=0;
				int recordviewed=0;
				while (!data.isLast()  && (recordviewed<maxobs))
				{
					realobs++;
					currentobs++;
					values=data.getRecord();
					if (values==null)
					{
						view.setVisible(true);
						tabbedPane.setEnabledAt(1, true);
						String errormsg=MessagesReplacer.replaceMessages(data.getmessage());
						JOptionPane pane = new JOptionPane(errormsg);
						JDialog dialog = pane.createDialog(MainGUI.desktop, Keywords.Language.getMessage(134));
						dialog.validate();
						dialog.setVisible(true);
						data.close();
						return;
					}
					if (positioncondition==null)
					{
						if ((currentobs>=minobs))
						{
							recordviewed++;
							if (newrep==true)
							{
								values=NewWriteFormat.getwriteformat(values, writeformat, defdec, isselnumasos);
							}
							Vector<Object> tempvector=new Vector<Object>();
							tempvector.addElement(currentobs);
							for (int i=0; i<values.length; i++)
							{
								tempvector.addElement(values[i]);
							}
							cellData.add(tempvector);
						}
					}
					else
					{
						int obstoadd=0;
						if (newrep==true)
						{
							values=NewWriteFormat.getwriteformat(values, writeformat, defdec, isselnumasos);
						}
						for (int i=0; i<positioncondition.length; i++)
						{
							valuescondit[i]=valuescondit[i].trim();
							if (!valuescondit[i].equals("MISSING"))
							{
								if ( (valuescondit[i].startsWith("\"")) && (valuescondit[i].endsWith("\"")) )
								{
									valuescondit[i]=valuescondit[i].replaceAll("\"","");
									if (typecondition[i]!=3)
									{
										if ((typecondition[i]==0) && (valuescondit[i].equalsIgnoreCase(values[positioncondition[i]].trim())))
											obstoadd++;
										else if ((typecondition[i]==-2) && (valuescondit[i].equalsIgnoreCase(values[positioncondition[i]].trim())))
											obstoadd++;
										else if ((typecondition[i]==2) && (valuescondit[i].equalsIgnoreCase(values[positioncondition[i]].trim())))
											obstoadd++;
										else
										{
											int tempcompare=values[positioncondition[i]].toUpperCase().compareTo(valuescondit[i].toUpperCase());
											if ((typecondition[i]<0) && (tempcompare<0))
												obstoadd++;
											if ((typecondition[i]>0) && (tempcompare>0))
												obstoadd++;
										}
									}
									else
									{
										if (!valuescondit[i].equalsIgnoreCase(values[positioncondition[i]].trim()))
											obstoadd++;
									}
								}
								else
								{
									double connum=Double.NaN;
									double valnum=Double.NaN;
									try
									{
										connum=Double.parseDouble(valuescondit[i]);
										valnum=Double.parseDouble(values[positioncondition[i]].trim());
									}
									catch (Exception enumco){}
									if ( (!Double.isNaN(connum)) && (!Double.isNaN(valnum)) )
									{
										if ((typecondition[i]==0) && (connum==valnum))
											obstoadd++;
										else if ((typecondition[i]==1) && (valnum>connum))
											obstoadd++;
										else if ((typecondition[i]==2) && (valnum>=connum))
											obstoadd++;
										else if ((typecondition[i]==-1) && (valnum<connum))
											obstoadd++;
										else if ((typecondition[i]==-2) && (valnum<=connum))
											obstoadd++;
										else if ((typecondition[i]==3) && (valnum!=connum))
											obstoadd++;
									}
									else
									{
										if (typecondition[i]!=3)
										{
											if ((typecondition[i]==0) && (valuescondit[i].equalsIgnoreCase(values[positioncondition[i]].trim())))
												obstoadd++;
											else if ((typecondition[i]==-2) && (valuescondit[i].equalsIgnoreCase(values[positioncondition[i]].trim())))
												obstoadd++;
											else if ((typecondition[i]==2) && (valuescondit[i].equalsIgnoreCase(values[positioncondition[i]].trim())))
												obstoadd++;
											else
											{
												int tempcompare=values[positioncondition[i]].toUpperCase().compareTo(valuescondit[i].toUpperCase());
												if ((typecondition[i]<0) && (tempcompare<0))
													obstoadd++;
												if ((typecondition[i]>0) && (tempcompare>0))
													obstoadd++;
											}
										}
										else
										{
											if (!valuescondit[i].equalsIgnoreCase(values[positioncondition[i]].trim()))
												obstoadd++;
										}
									}
								}
							}
							else
							{
								if ((values[positioncondition[i]].equals("")) && (typecondition[i]==0))
									obstoadd++;
								if ((!values[positioncondition[i]].equals("")) && (typecondition[i]==3))
									obstoadd++;
							}
						}
						if (obstoadd==positioncondition.length)
						{
							if ((currentobs>=minobs))
							{
								recordviewed++;
								Vector<Object> tempvector=new Vector<Object>();
								tempvector.addElement(new Integer(realobs));
								for (int i=0; i<values.length; i++)
								{
									tempvector.addElement(values[i]);
								}
								cellData.add(tempvector);
							}
						}
					}
				}
				data.close();
				if ((recordviewed==0) && (positioncondition==null))
				{
					view.setVisible(true);
					tabbedPane.setEnabledAt(1, true);
					String errormsg=MessagesReplacer.replaceMessages(data.getmessage());
					JOptionPane pane = new JOptionPane(Keywords.Language.getMessage(2599)+"\n"+errormsg);
					JDialog dialog = pane.createDialog(MainGUI.desktop, Keywords.Language.getMessage(134));
					dialog.validate();
					dialog.setVisible(true);
					return;
				}
				if ((recordviewed==0) && (positioncondition!=null))
				{
					view.setVisible(true);
					tabbedPane.setEnabledAt(1, true);
					String errormsg=MessagesReplacer.replaceMessages(data.getmessage());
					JOptionPane pane = null;
					if (minobs==1)
						pane=new JOptionPane(Keywords.Language.getMessage(2600)+"\n"+errormsg);
					else
						pane=new JOptionPane(Keywords.Language.getMessage(2626));
					JDialog dialog = pane.createDialog(MainGUI.desktop, Keywords.Language.getMessage(134));
					dialog.validate();
					dialog.setVisible(true);
					return;
				}
				tableds=new JTable(cellData, columnnames)
				{
					private static final long serialVersionUID = 1L;
					protected JTableHeader createDefaultTableHeader()
					{
						return new JTableHeader(columnModel)
						{
							private static final long serialVersionUID = 1L;
							public String getToolTipText(MouseEvent e)
							{
								java.awt.Point p = e.getPoint();
								int index = columnModel.getColumnIndexAtX(p.x);
								try
								{
									int realIndex = columnModel.getColumn(index).getModelIndex();
									return columnToolTips[realIndex];
								}
								catch (Exception ettp)
								{
									return "";
								}
							}
						};
					}
				};
				tableds.setAutoCreateRowSorter(true);

				TableColumnModel cmodel = tableds.getColumnModel();
				TableColumn tm = cmodel.getColumn(0);

				boolean isselwraptcell=wraptcell.isSelected();

				tm.setCellRenderer(new ColorColumnRenderer(Color.gray, Color.black));

				TextAreaRenderer textAreaRendererNormal = new TextAreaRenderer();
				TextAreaRenderer textAreaRendererColored = new TextAreaRenderer();
				textAreaRendererColored.setBkgnd(Color.white);
				textAreaRendererColored.setForegnd(Color.blue);

				if (replacer==1)
					istowrite=true;
				for(int k=0; k<var.size(); k++)
				{
					Hashtable<String, String> currentvar=var.get(k);
					String cf=currentvar.get(Keywords.VariableFormat.toLowerCase());
					if (cf.toUpperCase().startsWith(Keywords.TEXTSuffix))
					{
						tm = cmodel.getColumn(k+1);
						if (isselwraptcell)
							tm.setCellRenderer(textAreaRendererColored);
						else
							tm.setCellRenderer(new ColorColumnRenderer(Color.white, Color.blue));
					}
					else
					{
						if (isselwraptcell)
							tm.setCellRenderer(textAreaRendererNormal);
					}
				}

				tableds.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
				tableds.setColumnSelectionAllowed(false);
				tableds.setRowSelectionAllowed(false);
				tableds.setCellSelectionEnabled(false);
				tableds.setFocusable(false);
				panelviewdt.remove(scrollPaneds);
				scrollPaneds = new JScrollPane(tableds);
				int numrow = new Double(MainGUI.frame.getHeight()).intValue() - 52;
				int numcol = new Double(MainGUI.frame.getWidth()).intValue() - 14;
				tableds.setPreferredScrollableViewportSize(new Dimension(numcol, numrow));
				scrollPaneds.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
				scrollPaneds.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
				tableds.validate();
				panelviewdt.add(scrollPaneds);
				panelviewdt.validate();
				scrollPaneds.validate();
				viewDataSet.validate();
				MainGUI.desktop.validate();
				view.setVisible(true);
				tabbedPane.setEnabledAt(1, true);
				saveHtmldt1.setEnabled(true);
				saveHtmldt2.setEnabled(true);
				Keywords.numread=0.0;
				Keywords.operationReading=false;
				Keywords.numwrite=0.0;
				Keywords.operationWriting=false;
				if (positioncondition!=null)
				{
					JOptionPane pane1 = null;
					if (minobs==1)
						pane1=new JOptionPane(Keywords.Language.getMessage(2627)+": "+String.valueOf(recordviewed));
					else
						pane1=new JOptionPane(Keywords.Language.getMessage(2628)+": "+String.valueOf(recordviewed));
					JDialog dialog = pane1.createDialog(MainGUI.desktop, Keywords.Language.getMessage(2091));
					dialog.validate();
					dialog.setVisible(true);
					if (realobs<records)
					{
						JOptionPane pane2 =new JOptionPane(Keywords.Language.getMessage(2629));
						JDialog dialog1 = pane2.createDialog(MainGUI.desktop, Keywords.Language.getMessage(2091));
						dialog1.validate();
						dialog1.setVisible(true);
					}
				}
			}
		};

		view=new JButton(visualize);
		minvalue=new JTextField(5);
		minvalue.setText("1");
		maxdatabuffer = Integer.parseInt(System.getProperty(Keywords.MaxDataBuffered));
		int totalnumofrecords=records;
		if (reftotvar!=0)
		{
			int testgendim=records*reftotvar;
			if (testgendim>(maxdatabuffer*10))
			{
				maxdatabuffer=Math.round(maxdatabuffer/reftotvar);
				if (maxdatabuffer<=1)
					maxdatabuffer=2;
			}
		}
		JPanel panelcondition=new JPanel();
		panelcondition.setLayout(new BoxLayout(panelcondition,BoxLayout.X_AXIS));
		panelcondition.add(view);
		JSeparator sepcondition=new JSeparator();
		JLabel conditionlabel=new JLabel(Keywords.Language.getMessage(2585));
		panelcondition.add(sepcondition);
		panelcondition.add(conditionlabel);
		condition=new JTextField();
		panelcondition.add(condition);
		maxr=records;
		if (maxr>maxdatabuffer)
			maxr=maxdatabuffer;
		String max=String.valueOf(maxr);
		maxvalue=new JTextField(5);
		maxvalue.setText(max);
		JLabel label1=new JLabel(Keywords.Language.getMessage(346));
		JLabel label2=new JLabel(Keywords.Language.getMessage(347));
		JLabel label3=new JLabel(Keywords.Language.getMessage(2606)+" "+max+")");
		String[] choice=new String[2];
		choice[0]=Keywords.Language.getMessage(345);
		choice[1]=Keywords.Language.getMessage(348);
		typeview=new JComboBox(choice);

		Action changedescription = new AbstractAction(Keywords.Language.getMessage(285))
		{
			private static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent evt)
			{
				String tempvalue= JOptionPane.showInputDialog(Keywords.Language.getMessage(286), description.getText());
				if (tempvalue!=null)
				{
					if (!tempvalue.equals(""))
					{
						DictionaryCommand=DictionaryCommand+Keywords.descriptiondictionary+" "+tempvalue+";\n";
						description.setText(tempvalue);
						description.setToolTipText(tempvalue);
					}
				}
			}
		};
		Action changekeyword = new AbstractAction(Keywords.Language.getMessage(287))
		{
			private static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent evt)
			{
				String tempvalue = JOptionPane.showInputDialog(Keywords.Language.getMessage(288), keyword.getText());
				if (tempvalue!=null)
				{
					if (!tempvalue.equals(""))
					{
						DictionaryCommand=DictionaryCommand+Keywords.keyworddictionary+" "+tempvalue+";\n";
						keyword.setText(tempvalue);
						keyword.setToolTipText(tempvalue);
					}
				}
			}
		};
		firstpanel.add(labeldatatype);
		firstpanel.add(datatype);
		JButton b0=new JButton("");
		b0.setVisible(false);
		firstpanel.add(b0);
		firstpanel.add(labeldate);
		firstpanel.add(date);
		JButton b1=new JButton("");
		b1.setVisible(false);
		firstpanel.add(b1);
		firstpanel.add(labelauthor);
		firstpanel.add(author);
		JButton b2=new JButton("");
		b2.setVisible(false);
		firstpanel.add(b2);
		firstpanel.add(labeldescription);
		firstpanel.add(description);
		JButton cd=new JButton(changedescription);
		firstpanel.add(cd);
		firstpanel.add(labelkeyword);
		firstpanel.add(keyword);
		JButton ck=new JButton(changekeyword);
		firstpanel.add(ck);

		JPanel dictionarypanel=new JPanel();
		dictionarypanel.setLayout(new BoxLayout(dictionarypanel,BoxLayout.Y_AXIS));
		dictionarypanel.add(firstpanel);
		JButton b3=new JButton("empty");
		b3.setVisible(false);
		dictionarypanel.add(b3);
		JSeparator sep1=new JSeparator();
		dictionarypanel.add(sep1);
		JButton b4=new JButton("empty");
		b4.setVisible(false);
		dictionarypanel.add(b4);

		loweredbevel = BorderFactory.createLoweredBevelBorder();
		TitledBorder titlepanelvar = BorderFactory.createTitledBorder(loweredbevel, Keywords.Language.getMessage(289));
		titlepanelvar.setTitlePosition(TitledBorder.ABOVE_TOP);

		Vector<Hashtable<String, String>> var=dr.getfixedvariableinfo();

		JPanel varcontainer=new JPanel();
		varcontainer.setBorder(titlepanelvar);
		tabVariable=new JTabbedPane();
		fieldvarname=new JTextField[var.size()];
		fieldvarlabel=new JTextField[var.size()];
		fieldvarformat=new JTextField[var.size()];
		modifyname=new JButton[var.size()];
		modifylabel=new JButton[var.size()];
		modifyformat=new JButton[var.size()];
		scrollVar=new JScrollPane[var.size()];
		currentVar=new JPanel[var.size()];
		mstat=new JButton[var.size()];
		mfreq=new JButton[var.size()];
		for (int i=0; i<var.size(); i++)
		{
			Hashtable<String, String> currentvar=var.get(i);
			Action changename = new AbstractAction(Keywords.Language.getMessage(290))
			{
				private static final long serialVersionUID = 1L;
				public void actionPerformed(ActionEvent evt)
				{
					String inputValue = JOptionPane.showInputDialog(Keywords.Language.getMessage(291), fieldvarname[tabVariable.getSelectedIndex()].getText());
					if (inputValue!=null)
					{
						if (!inputValue.equals(""))
						{
							String oldname=fieldvarname[tabVariable.getSelectedIndex()].getText();
							DictionaryCommand=DictionaryCommand+Keywords.rename+" "+oldname+"="+inputValue+";\n";
							fieldvarname[tabVariable.getSelectedIndex()].setText(inputValue);
							fieldvarname[tabVariable.getSelectedIndex()].setToolTipText(inputValue);
						}
					}
				}
			};
			Action codelabel = new AbstractAction(Keywords.Language.getMessage(292))
			{
				private static final long serialVersionUID = 1L;
				public void actionPerformed(ActionEvent evt)
				{
					String tempname=fieldvarname[tabVariable.getSelectedIndex()].getText();
					new DictionaryCodeLabel(tabVariable.getSelectedIndex(), tempname, viewDataSet);
				}
			};
			Action missingdata = new AbstractAction(Keywords.Language.getMessage(293))
			{
				private static final long serialVersionUID = 1L;
				public void actionPerformed(ActionEvent evt)
				{
					String tempname=fieldvarname[tabVariable.getSelectedIndex()].getText();
					new DictionaryMissingData(tabVariable.getSelectedIndex(), tempname, viewDataSet);
				}
			};

			Action changelabel = new AbstractAction(Keywords.Language.getMessage(294))
			{
				private static final long serialVersionUID = 1L;
				public void actionPerformed(ActionEvent evt)
				{
					String inputValue = JOptionPane.showInputDialog(Keywords.Language.getMessage(295), fieldvarlabel[tabVariable.getSelectedIndex()].getText());
					if (inputValue!=null)
					{
						if (!inputValue.equals(""))
						{
							String oldname=fieldvarname[tabVariable.getSelectedIndex()].getText();
							DictionaryCommand=DictionaryCommand+Keywords.labeldictionary+" "+oldname+"="+inputValue+";\n";
							fieldvarlabel[tabVariable.getSelectedIndex()].setText(inputValue);
							fieldvarlabel[tabVariable.getSelectedIndex()].setToolTipText(inputValue);
						}
					}
				}
			};
			Action changeformat = new AbstractAction(Keywords.Language.getMessage(296))
			{
				private static final long serialVersionUID = 1L;
				public void actionPerformed(ActionEvent evt)
				{
					String inputValue = JOptionPane.showInputDialog(Keywords.Language.getMessage(297), fieldvarformat[tabVariable.getSelectedIndex()].getText());
					if (inputValue!=null)
					{
						if (!inputValue.equals(""))
						{
							String oldname=fieldvarname[tabVariable.getSelectedIndex()].getText();
							DictionaryCommand=DictionaryCommand+Keywords.writefmtdictionary+" "+oldname+"="+inputValue+";\n";
							fieldvarformat[tabVariable.getSelectedIndex()].setText(inputValue);
							fieldvarformat[tabVariable.getSelectedIndex()].setToolTipText(inputValue);
							if (fieldvarformat[tabVariable.getSelectedIndex()].getText().toLowerCase().startsWith(Keywords.TEXTSuffix.toLowerCase()))
							{
								mstat[tabVariable.getSelectedIndex()].setVisible(false);
								mfreq[tabVariable.getSelectedIndex()].setVisible(true);
							}
							else
							{
								mstat[tabVariable.getSelectedIndex()].setVisible(true);
								mfreq[tabVariable.getSelectedIndex()].setVisible(false);
							}
						}
					}
				}
			};
			Action makestats = new AbstractAction(Keywords.Language.getMessage(1758))
			{
				private static final long serialVersionUID = 1L;
				public void actionPerformed(ActionEvent evt)
				{
					String oldname=fieldvarname[tabVariable.getSelectedIndex()].getText();
					MakeStats(oldname);
				}
			};
			Action makefreq = new AbstractAction(Keywords.Language.getMessage(1763))
			{
				private static final long serialVersionUID = 1L;
				public void actionPerformed(ActionEvent evt)
				{
					String oldname=fieldvarname[tabVariable.getSelectedIndex()].getText();
					MakeFreq(oldname);
				}
			};
			String rifvar=String.valueOf(i);
			currentVar[i]=new JPanel(new GridLayout(3,3));
			JLabel labelname=new JLabel(Keywords.Language.getMessage(298));
			JLabel labellabel=new JLabel(Keywords.Language.getMessage(299));
			JLabel labelformat=new JLabel(Keywords.Language.getMessage(300));
			fieldvarname[i]=new JTextField(10);
			fieldvarlabel[i]=new JTextField(10);
			fieldvarformat[i]=new JTextField(10);
			fieldvarname[i].setEditable(false);
			fieldvarlabel[i].setEditable(false);
			fieldvarformat[i].setEditable(false);
			fieldvarname[i].setText(currentvar.get(Keywords.VariableName.toLowerCase()));
			fieldvarname[i].setToolTipText(currentvar.get(Keywords.VariableName.toLowerCase()));
			fieldvarlabel[i].setText(currentvar.get(Keywords.LabelOfVariable.toLowerCase()));
			fieldvarformat[i].setText(currentvar.get(Keywords.VariableFormat.toLowerCase()));
			fieldvarlabel[i].setToolTipText(currentvar.get(Keywords.LabelOfVariable.toLowerCase()));
			fieldvarformat[i].setToolTipText(currentvar.get(Keywords.VariableFormat.toLowerCase()));
			currentVar[i].add(labelname);
			currentVar[i].add(fieldvarname[i]);
			JButton bcn=new JButton(changename);
			currentVar[i].add(bcn);
			currentVar[i].add(labellabel);
			currentVar[i].add(fieldvarlabel[i]);
			JButton bcl=new JButton(changelabel);
			currentVar[i].add(bcl);
			currentVar[i].add(labelformat);
			currentVar[i].add(fieldvarformat[i]);
			JButton bcf=new JButton(changeformat);
			currentVar[i].add(bcf);
			JPanel panelmodvar=new JPanel();
			panelmodvar.setLayout(new BoxLayout(panelmodvar,BoxLayout.Y_AXIS));
			panelmodvar.add(currentVar[i]);
			JSeparator sepv1=new JSeparator();
			panelmodvar.add(sepv1);
			JPanel cola=new JPanel(new GridLayout(2,1));
			JButton bccl=new JButton(codelabel);
			cola.add(bccl);
			JButton bcmd=new JButton(missingdata);
			cola.add(bcmd);
			mstat[i]=new JButton(makestats);
			mfreq[i]=new JButton(makefreq);
			if (fieldvarformat[i].getText().toLowerCase().startsWith(Keywords.TEXTSuffix.toLowerCase()))
				mstat[i].setVisible(false);
			else
				mfreq[i].setVisible(false);
			cola.add(mstat[i]);
			cola.add(mfreq[i]);
			panelmodvar.add(cola);
			tabVariable.addTab(rifvar,panelmodvar);
			tabVariable.setToolTipTextAt(i, currentvar.get(Keywords.LabelOfVariable.toLowerCase()));
		}
		varcontainer.add(tabVariable);
		dictionarypanel.add(varcontainer);
		JSeparator sepf1=new JSeparator();
		dictionarypanel.add(sepf1);

		Vector<String> paths=Keywords.project.getPaths();
		paths.add(0, Keywords.Language.getMessage(174));
		listpath=new JComboBox(paths);
		JLabel msgseloutdict=new JLabel(Keywords.Language.getMessage(301));
		JPanel outdict=new JPanel(new GridLayout(1,3));
		outdict.add(msgseloutdict);
		outdict.add(listpath);
		outdictname=new JTextField(10);
		outdict.add(outdictname);
		dictionarypanel.add(outdict);
		JSeparator sepf2=new JSeparator();
		dictionarypanel.add(sepf2);

		Action executestep = new AbstractAction(Keywords.Language.getMessage(170))
		{
			private static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent evt)
			{
				String actualcommand=Keywords.DICTIONARY+" "+Keywords.dict+"="+namepath.trim()+"."+dictname.trim();
				String value=" "+Keywords.outdict+"=";
				if (!((String)listpath.getSelectedItem()).equals(Keywords.Language.getMessage(174)))
					value=value+((String)listpath.getSelectedItem()).trim()+".";
				if (!outdictname.getText().equals(""))
					value=value+outdictname.getText().trim();
				else
					value="";
				actualcommand=actualcommand+value+";\n"+DictionaryCommand+"run;\n";
				new ExecuteRunner(2, actualcommand);
				DictionaryCommand="";
				dr=new DictionaryReader(dictionary);
				dictmodified=true;
			}
		};
		Action putstep = new AbstractAction(Keywords.Language.getMessage(172))
		{
			private static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent evt)
			{
				String actualcommand=Keywords.DICTIONARY+" "+Keywords.dict.trim()+"="+namepath.trim()+"."+dictname.trim();
				String value=" "+Keywords.outdict+"=";
				if (!((String)listpath.getSelectedItem()).equals(Keywords.Language.getMessage(174)))
					value=value+((String)listpath.getSelectedItem()).trim()+".";
				if (!outdictname.getText().equals(""))
					value=value+outdictname.getText().trim();
				else
					value="";
				actualcommand=actualcommand+value+";\n"+DictionaryCommand+"run;\n";
				try
				{
					Document doc = MainGUI.EditorArea.getDocument();
					Document blank = new DefaultStyledDocument();
					MainGUI. EditorArea.setDocument(blank);
					try
					{
						doc.insertString(doc.getLength(), "\n"+actualcommand, null);
					}
					catch (BadLocationException e) {}
					MainGUI.EditorArea.setDocument(doc);
					MainGUI.EditorArea.setCaretPosition(MainGUI.EditorArea.getDocument().getLength());
				}
				catch (Exception e) {}
				DictionaryCommand="";
			}
		};
		Action backtogui = new AbstractAction(Keywords.Language.getMessage(168))
		{
			private static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent evt)
			{
				viewDataSet.dispose();
				System.gc();
			}
		};

		JSeparator sepf3=new JSeparator();
		dictionarypanel.add(sepf3);

		JButton okexe = new JButton(executestep);
		JButton oksave = new JButton(putstep);
		JButton exit = new JButton(backtogui);

		JPanel secondlastline=new JPanel(new GridLayout(3,1));

		secondlastline.add(okexe);
		secondlastline.add(oksave);
		secondlastline.add(exit);

		dictionarypanel.add(secondlastline);

		scroll= new JScrollPane(dictionarypanel);
		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		int numrow = new Double(MainGUI.frame.getHeight()).intValue() - 52;
		int numcol = new Double(MainGUI.frame.getWidth()).intValue() - 14;
		scroll.setSize(numcol, numrow);

		boolean resizable = true;
		boolean closeable = true;
		boolean maximizable  = true;
		boolean iconifiable = true;
		String title =Keywords.Language.getMessage(1602)+" "+path+"."+dictname;
		viewDataSet = new JInternalFrame(title, resizable, closeable, maximizable, iconifiable);
		viewDataSet.addInternalFrameListener(new InternalFrameListener()
		{
			public void internalFrameClosing(InternalFrameEvent e)
			{
				Keywords.currentExecutedStep="";
				Keywords.general_percentage_total=0;
				Keywords.general_percentage_done=0;
				Keywords.numread=0.0;
				Keywords.operationReading=false;
				Keywords.numwrite=0.0;
				Keywords.operationWriting=false;
				try
				{
					Keywords.opened_datasets.remove(dsSelection);
				}
				catch (Exception ed){}
			}
			public void internalFrameClosed(InternalFrameEvent e)
			{
				Keywords.currentExecutedStep="";
				Keywords.general_percentage_total=0;
				Keywords.general_percentage_done=0;
				Keywords.numread=0.0;
				Keywords.operationReading=false;
				Keywords.numwrite=0.0;
				Keywords.operationWriting=false;
				try
				{
					Keywords.opened_datasets.remove(dsSelection);
				}
				catch (Exception ed){}
			}
			public void internalFrameOpened(InternalFrameEvent e) {}
			public void internalFrameIconified(InternalFrameEvent e) {}
			public void internalFrameDeiconified(InternalFrameEvent e) {}
			public void internalFrameActivated(InternalFrameEvent e) {}
			public void internalFrameDeactivated(InternalFrameEvent e){}
		});

		menuDataSetViewer=new JMenuBar();
		menuFile=new JMenu(Keywords.Language.getMessage(303));
		saveHtmlDict=new JMenuItem(Keywords.Language.getMessage(304));
		saveHtmlDict.addActionListener(this);
		exitmenu=new JMenuItem(Keywords.Language.getMessage(168));
		exitmenu.addActionListener(this);
		menuFile.add(saveHtmlDict);
		saveHtmldt=new JMenu(Keywords.Language.getMessage(349));
		saveHtmldt1=new JMenuItem(Keywords.Language.getMessage(350));
		saveHtmldt1.addActionListener(this);
		saveHtmldt2=new JMenuItem(Keywords.Language.getMessage(351));
		saveHtmldt2.addActionListener(this);
		saveHtmldt.add(saveHtmldt1);
		saveHtmldt.add(saveHtmldt2);
		menuFile.add(saveHtmldt);
		menuFile.add(exitmenu);
		menuDataSetViewer.add(menuFile);
		saveHtmldt1.setEnabled(false);
		saveHtmldt2.setEnabled(false);
		saveHtmlDict.setEnabled(false);

		viewDataSet.setJMenuBar(menuDataSetViewer);

		numrow = new Double(MainGUI.frame.getHeight()).intValue() - 52;
		numcol = new Double(MainGUI.frame.getWidth()).intValue() - 14;
		viewDataSet.setSize(numcol, numrow);
		viewDataSet.setFrameIcon(iconSet);

		JPanel panel=new JPanel();
		panel.setLayout(new BoxLayout(panel,BoxLayout.Y_AXIS));

		JLabel msgsremtable=new JLabel(Keywords.Language.getMessage(1457));
		JPanel recordinfo=new JPanel();
		JPanel ppanel=new JPanel();
		JPanel pan=new JPanel();
		pan.add(label1);
		pan.add(typeview);
		ppanel.add(pan);
		ppanel.add(label2);
		ppanel.add(minvalue);
		ppanel.add(label3);
		ppanel.add(maxvalue);
		panel.add(ppanel);
		onlytwodec = new JCheckBox(Keywords.Language.getMessage(2220));
		numasos = new JCheckBox(Keywords.Language.getMessage(2221));
		wraptcell = new JCheckBox(Keywords.Language.getMessage(2729));

		panel.add(onlytwodec, BorderLayout.WEST);
		panel.add(numasos, BorderLayout.WEST);
		panel.add(wraptcell, BorderLayout.WEST);

		panel.add(panelcondition);

		JLabel recordlabel=new JLabel(Keywords.Language.getMessage(1055)+"=");
		JLabel numberofrecord=new JLabel(String.valueOf(totalnumofrecords));
		recordinfo.add(recordlabel);
		recordinfo.add(numberofrecord);
		panelviewdt=new JPanel();
		panelviewdt.setLayout( new BorderLayout() );
		panelviewdt.add(panel, BorderLayout.NORTH );
		panelviewdt.add(scrollPaneds, BorderLayout.CENTER );
		panelviewdt.add(recordinfo, BorderLayout.SOUTH );

		tabbedPane.addTab(Keywords.Language.getMessage(1599), null, panelviewdt, Keywords.Language.getMessage(221));
		tabbedPane.addTab(Keywords.Language.getMessage(1598), null, scroll, Keywords.Language.getMessage(220));

		tabbedPane.addChangeListener(new ChangeListener()
		{
			public void stateChanged(ChangeEvent e)
			{
				if ((tabbedPane.getSelectedComponent()==panelviewdt) && (dictmodified))
				{
					JOptionPane pane = new JOptionPane(Keywords.Language.getMessage(1600));
					JDialog dialog = pane.createDialog(MainGUI.desktop, Keywords.Language.getMessage(134));
					dialog.validate();
					dialog.setVisible(true);
				}
				if (tabbedPane.getSelectedComponent()==scroll)
				{
					saveHtmlDict.setEnabled(true);
					saveHtmldt1.setEnabled(false);
					saveHtmldt2.setEnabled(false);
				}
				if ((tabbedPane.getSelectedComponent()==panelviewdt) && (tableopened))
				{
					saveHtmldt1.setEnabled(true);
					saveHtmldt2.setEnabled(true);
				}
				if (tabbedPane.getSelectedComponent()==panelviewdt)
				{
					saveHtmlDict.setEnabled(false);
				}
			}
		});

		viewDataSet.getContentPane().add(tabbedPane);
		viewDataSet.repaint();
		viewDataSet.setVisible(true);
		viewDataSet.pack();
		MainGUI.desktop.add(viewDataSet);
		MainGUI.desktop.repaint();
		try
		{
			viewDataSet.moveToFront();
			viewDataSet.setEnabled(true);
			viewDataSet.toFront();
			viewDataSet.show();
			viewDataSet.setSelected(true);
			viewDataSet.setMaximum(true);
		}
		catch (Exception e) {}
	}
	/**
	* Checks for user actions (save dictionary in HTML or exit)
	*/
	public void actionPerformed(ActionEvent Event)
	{
		Object Source=Event.getSource();
		if(Source==saveHtmlDict)
		{
			String filesave=selectfile();
			if (filesave.equals(""))
				return;
			if(!filesave.endsWith(".html"))
				filesave=filesave+".html";
			File filehtml = new File(filesave);
			boolean exists = (filehtml.exists());
			int questionexist=-1;
			try
			{
				if (!exists)
				{
					filehtml.createNewFile();
				}
				else
				{
					Object[] options = { Keywords.Language.getMessage(362), Keywords.Language.getMessage(363)};
					questionexist =JOptionPane.showOptionDialog(MainGUI.desktop, Keywords.Language.getMessage(361), Keywords.Language.getMessage(134), JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,null, options, options[0]);
				}
			}
			catch (Exception e)
			{
					JOptionPane pane = new JOptionPane(Keywords.Language.getMessage(364));
					JDialog dialog = pane.createDialog(MainGUI.desktop, Keywords.Language.getMessage(134));
					dialog.validate();
					dialog.setVisible(true);
					return;
			}
			if (questionexist==1)
				return;
			if ((exists) && (questionexist==0))
			{
				boolean success = filehtml.delete();
				if (!success)
				{
					JOptionPane pane = new JOptionPane(Keywords.Language.getMessage(365));
					JDialog dialog = pane.createDialog(MainGUI.desktop, Keywords.Language.getMessage(134));
					dialog.validate();
					dialog.setVisible(true);
					return;
			    }
			}
			try
			{
				DictionaryReader newdr=new DictionaryReader(dictionary);

				BufferedWriter out = new BufferedWriter(new FileWriter(filesave, true));
				out.write("<html>\n");
				out.write("<head>\n");
				out.write("<meta name=\"GENERATOR\" content=\"ADaMSoft Exporting tool\">\n");
				String title=Keywords.Language.getMessage(302)+" "+dictionary;
				out.write("<title>"+title+"</title>\n");
				out.write("</head>\n");
				out.write("<body>");
				out.write("<p>&nbsp;</p>");
				out.write("<center>");
				String titledatatable=Keywords.Language.getMessage(366);
				out.write("<table border=\"1\"><caption>"+titledatatable+"</caption>\n");
				out.write("<tr>");
				out.write("<th>"+Keywords.Language.getMessage(367)+"</th>\n");
				out.write("<th>"+Keywords.Language.getMessage(368)+"</th>\n");
				out.write("</tr>\n");
				out.write("<tr>\n");
				out.write("<td>"+Keywords.Language.getMessage(280)+"</td>\n");
				out.write("<td>"+newdr.getcreationdate()+"</td>\n");
				out.write("</tr>\n");
				out.write("<tr>\n");
				out.write("<td>"+Keywords.Language.getMessage(281)+"</td>\n");
				out.write("<td>"+newdr.getauthor()+"</td>\n");
				out.write("</tr>\n");
				out.write("<tr>\n");
				out.write("<td>"+Keywords.Language.getMessage(282)+"</td>\n");
				out.write("<td>"+newdr.getdatatabletype()+"</td>\n");
				out.write("</tr>\n");
				out.write("<tr>\n");
				out.write("<td>"+Keywords.Language.getMessage(283)+"</td>\n");
				out.write("<td>"+newdr.getdescription()+"</td>\n");
				out.write("</tr>\n");
				out.write("<tr>\n");
				out.write("<td>"+Keywords.Language.getMessage(284)+"</td>\n");
				out.write("<td>"+newdr.getkeyword()+"</td>\n");
				out.write("</tr>\n");
				out.write("</table>\n");
				out.write("<hr noshade>\n");
				titledatatable=Keywords.Language.getMessage(369);
				out.write("<table border=\"1\"><caption>"+titledatatable+"</caption>\n");
				out.write("<tr>");
				out.write("<th>"+Keywords.Language.getMessage(298)+"</th>\n");
				out.write("<th>"+Keywords.Language.getMessage(299)+"</th>\n");
				out.write("<th>"+Keywords.Language.getMessage(300)+"</th>\n");
				out.write("<th>"+Keywords.Language.getMessage(370)+"</th>\n");
				out.write("<th>"+Keywords.Language.getMessage(371)+"</th>\n");
				out.write("</tr>\n");
				Vector<Hashtable<String, String>> newvar=newdr.getfixedvariableinfo();
				boolean ecl=false;
				boolean emd=false;
				for (int i=0; i<newvar.size(); i++)
				{
					Hashtable<String, String> currentvar=newvar.get(i);
					Hashtable<String, String> tempcodelabel=codelabel.get(i);
					Hashtable<String, String> tempmissingdata=missingdata.get(i);
					out.write("<tr>\n");
					out.write("<td>"+currentvar.get(Keywords.VariableName.toLowerCase())+"</td>\n");
					out.write("<td>"+currentvar.get(Keywords.LabelOfVariable.toLowerCase())+"</td>\n");
					out.write("<td>"+currentvar.get(Keywords.VariableFormat.toLowerCase())+"</td>\n");
					if (tempcodelabel.size()==0)
						out.write("<td>"+Keywords.Language.getMessage(363)+"</td>\n");
					else
					{
						ecl=true;
						out.write("<td>"+Keywords.Language.getMessage(362)+"</td>\n");
					}
					if (tempmissingdata.size()==0)
						out.write("<td>"+Keywords.Language.getMessage(363)+"</td>\n");
					else
					{
						emd=true;
						out.write("<td>"+Keywords.Language.getMessage(362)+"</td>\n");
					}
					out.write("</tr>\n");
				}
				out.write("</table>\n");
				out.write("<hr noshade>\n");
				if (ecl)
				{
					for (int i=0; i<newvar.size(); i++)
					{
						Hashtable<String, String> currentvar=newvar.get(i);
						Hashtable<String, String> tempcodelabel=codelabel.get(i);
						if (tempcodelabel.size()>0)
						{
							String varname=currentvar.get(Keywords.VariableName.toLowerCase());
							titledatatable=Keywords.Language.getMessage(372)+" "+varname;
							out.write("<table border=\"1\"><caption>"+titledatatable+"</caption>\n");
							out.write("<tr>");
							out.write("<th>"+Keywords.Language.getMessage(307)+"</th>\n");
							out.write("<th>"+Keywords.Language.getMessage(308)+"</th>\n");
							out.write("</tr>\n");
							for (Enumeration<String> e = tempcodelabel.keys() ; e.hasMoreElements() ;)
							{
								String code = e.nextElement();
								String value= tempcodelabel.get(code);
								out.write("<tr>\n");
								out.write("<td>"+code+"</td>\n");
								out.write("<td>"+value+"</td>\n");
								out.write("</tr>\n");
							}
							out.write("</table>\n");
							out.write("<hr width=50%>\n");
							out.write("<hr noshade>\n");
						}
					}
				}
				if (emd)
				{
					for (int i=0; i<newvar.size(); i++)
					{
						Hashtable<String, String> currentvar=newvar.get(i);
						Hashtable<String, String> tempmissingdata=missingdata.get(i);
						if (tempmissingdata.size()>0)
						{
							String varname=currentvar.get(Keywords.VariableName.toLowerCase());
							titledatatable=Keywords.Language.getMessage(373)+" "+varname;
							out.write("<table border=\"1\"><caption>"+titledatatable+"</caption>\n");
							out.write("<tr>");
							out.write("<th>"+Keywords.Language.getMessage(327)+"</th>\n");
							out.write("</tr>\n");
							for (Enumeration<String> e = tempmissingdata.keys() ; e.hasMoreElements() ;)
							{
								String code = e.nextElement();
								out.write("<tr>\n");
								out.write("<td>"+code+"</td>\n");
								out.write("</tr>\n");
							}
							out.write("</table>\n");
							out.write("<hr width=50%>\n");
						}
					}
				}
				out.write("</center>\n");
				out.write("<p>&nbsp;</p>\n");
				out.write("</body>\n");
				out.write("</html>\n");
				out.close();
				JOptionPane pane = new JOptionPane(Keywords.Language.getMessage(374));
				JDialog dialog = pane.createDialog(MainGUI.desktop, Keywords.Language.getMessage(375));
				dialog.validate();
				dialog.setVisible(true);
			}
			catch (Exception e)
			{
					filehtml.delete();
					JOptionPane pane = new JOptionPane(Keywords.Language.getMessage(364));
					JDialog dialog = pane.createDialog(MainGUI.desktop, Keywords.Language.getMessage(134));
					dialog.validate();
					dialog.setVisible(true);
					return;
			}
		}
		if(Source==saveHtmldt1)
		{
			if (cellData.size()==0)
			{
				JOptionPane pane = new JOptionPane(Keywords.Language.getMessage(2803));
				JDialog dialog = pane.createDialog(MainGUI.desktop, Keywords.Language.getMessage(134));
				dialog.validate();
				dialog.setVisible(true);
				return;
			}
			String filesave=selectfiledata();
			if (filesave.equals(""))
				return;
			if(!filesave.endsWith(".html"))
				filesave=filesave+".html";
			File filehtml = new File(filesave);
			boolean exists = (filehtml.exists());
			int questionexist=-1;
			try
			{
				if (!exists)
				{
					filehtml.createNewFile();
				}
				else
				{
					Object[] options = { Keywords.Language.getMessage(362), Keywords.Language.getMessage(363),Keywords.Language.getMessage(377)};
					questionexist =JOptionPane.showOptionDialog(MainGUI.desktop, Keywords.Language.getMessage(378), Keywords.Language.getMessage(134), JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,null, options, options[0]);
				}
			}
			catch (Exception e)
			{
					JOptionPane pane = new JOptionPane(Keywords.Language.getMessage(364));
					JDialog dialog = pane.createDialog(MainGUI.desktop, Keywords.Language.getMessage(134));
					dialog.validate();
					dialog.setVisible(true);
					return;
			}
			if (questionexist==2)
				return;
			if ((exists) && (questionexist==0))
			{
				boolean success = filehtml.delete();
				if (!success)
				{
					JOptionPane pane = new JOptionPane(Keywords.Language.getMessage(364));
					JDialog dialog = pane.createDialog(MainGUI.desktop, Keywords.Language.getMessage(134));
					dialog.validate();
					dialog.setVisible(true);
					return;
			    }
			}
			try
			{
				BufferedWriter out = new BufferedWriter(new FileWriter(filesave, true));
				out.write("<html>\n");
				out.write("<head>\n");
				out.write("<meta name=\"GENERATOR\" content=\"ADaMSoft Exporting tool\">\n");
				String titleTab=Keywords.Language.getMessage(339)+": "+dictionary;
				out.write("<title>"+titleTab+"</title>\n");
				out.write("</head>\n");
				out.write("<body>");
				out.write("<p>&nbsp;</p>");
				out.write("<center>");
				out.write("<table border=\"1\"><caption>"+titleTab+"</caption>\n");
				out.write("<tr>");
				for(int i=0;i<columnnames.size();i++)
				{
					out.write("<th>"+columnnames.get(i)+"</th>\n");
				}
				out.write("</tr>\n");
				for(int i=0;i<cellData.size();i++)
				{
					out.write("<tr>\n");
					Vector<?> tempvector=cellData.get(i);
					for(int j=0;j<tempvector.size();j++)
					{
						if (j==0)
						{
							int tempvaluei=(Integer)tempvector.get(j);
							out.write("<td>"+String.valueOf(tempvaluei)+"</td>\n");
						}
						else
						{
							String tempvalue=(String)tempvector.get(j);
							if (tempvalue.equals(""))
								tempvalue="&nbsp;";
							out.write("<td>"+tempvalue+"</td>\n");
						}
					}
					out.write("</tr>\n");
				}
				out.write("</table>\n");
				out.write("</center>\n");
				out.write("<p>&nbsp;</p>\n");
				out.write("</body>\n");
				out.write("</html>\n");
				out.close();
			}
			catch (Exception e)
			{
				filehtml.delete();
				JOptionPane pane = new JOptionPane(Keywords.Language.getMessage(364));
				JDialog dialog = pane.createDialog(MainGUI.desktop, Keywords.Language.getMessage(134));
				dialog.validate();
				dialog.setVisible(true);
				return;
			}
		}
		if(Source==saveHtmldt2)
		{
			String filesave=selectfiledata();
			if (filesave.equals(""))
				return;
			if(!filesave.endsWith(".html"))
				filesave=filesave+".html";
			File filehtml = new File(filesave);
			boolean exists = (filehtml.exists());
			int questionexist=-1;
			try
			{
				if (!exists)
				{
					filehtml.createNewFile();
				}
				else
				{
					Object[] options = { Keywords.Language.getMessage(362), Keywords.Language.getMessage(363),Keywords.Language.getMessage(377)};
					questionexist =JOptionPane.showOptionDialog(MainGUI.desktop, Keywords.Language.getMessage(378), Keywords.Language.getMessage(134), JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,null, options, options[0]);
				}
			}
			catch (Exception e)
			{
					JOptionPane pane = new JOptionPane(Keywords.Language.getMessage(364));
					JDialog dialog = pane.createDialog(MainGUI.desktop, Keywords.Language.getMessage(134));
					dialog.validate();
					dialog.setVisible(true);
					return;
			}
			if (questionexist==2)
				return;
			if ((exists) && (questionexist==0))
			{
				boolean success = filehtml.delete();
				if (!success)
				{
					JOptionPane pane = new JOptionPane(Keywords.Language.getMessage(364));
					JDialog dialog = pane.createDialog(MainGUI.desktop, Keywords.Language.getMessage(134));
					dialog.validate();
					dialog.setVisible(true);
					return;
			    }
			}
			try
			{
				out = new BufferedWriter(new FileWriter(filesave, true));
				out.write("<html>\n");
				out.write("<head>\n");
				out.write("<meta name=\"GENERATOR\" content=\"ADaMSoft Exporting tool\">\n");
				String titleTab=Keywords.Language.getMessage(339)+": "+dictionary;
				out.write("<title>"+titleTab+"</title>\n");
				out.write("</head>\n");
				out.write("<body>");
				out.write("<p>&nbsp;</p>");
				out.write("<center>");
				out.write("<table border=\"1\"><caption>"+titleTab+"</caption>\n");
				out.write("<tr>");
				String typeselection=(String)typeview.getSelectedItem();
				replacer=1;
				if (typeselection.equalsIgnoreCase(Keywords.Language.getMessage(345)))
					replacer=0;
				int[] repvaluehtml=new int[columnnames.size()];
				for(int i=0;i<origcolumnnames.size();i++)
				{
					out.write("<th>"+origcolumnnames.get(i)+"</th>\n");
					repvaluehtml[i]=replacer;
				}
				boolean writefmt=false;
				if (replacer==1)
					writefmt=true;
				DataReader datahtml=new DataReader(dr);
				boolean isselonlytwodec=onlytwodec.isSelected();
				boolean isselnumasos=numasos.isSelected();
				boolean newrep=false;
				if (isselonlytwodec)
					newrep=true;
				if (isselnumasos)
					newrep=true;
				if (newrep)
					writefmt=false;
				int defdec=-1;
				if (isselonlytwodec)
					defdec=2;
				Vector<String> writeformat=new Vector<String>();
				for(int k=0; k<dr.gettotalvar(); k++)
				{
					writeformat.add(dr.getvarformat(k));
				}
				if (!datahtml.open(null, repvaluehtml, writefmt))
				{
					String errormsg=MessagesReplacer.replaceMessages(datahtml.getmessage());
					JOptionPane pane = new JOptionPane(errormsg);
					JDialog dialog = pane.createDialog(MainGUI.desktop, Keywords.Language.getMessage(134));
					dialog.validate();
					dialog.setVisible(true);
					return;
				}
				while (!datahtml.isLast())
				{
					out.write("<tr>\n");
					values=datahtml.getRecord();
					if (values==null)
					{
						String errormsg=MessagesReplacer.replaceMessages(datahtml.getmessage());
						JOptionPane pane = new JOptionPane(errormsg);
						JDialog dialog = pane.createDialog(MainGUI.desktop, Keywords.Language.getMessage(134));
						dialog.validate();
						dialog.setVisible(true);
						return;
					}
					if (newrep==true)
					{
						values=NewWriteFormat.getwriteformat(values, writeformat, defdec, isselnumasos);
					}
					for (int i=0; i<values.length; i++)
					{
						String tempvalue=values[i].trim();
						if (tempvalue.equals(""))
							tempvalue="&nbsp;";
						out.write("<td>"+tempvalue+"</td>\n");
					}
					out.write("</tr>\n");
				}
				datahtml.close();
				out.write("</table>\n");
				out.write("</center>\n");
				out.write("<p>&nbsp;</p>\n");
				out.write("</body>\n");
				out.write("</html>\n");
				out.close();
			}
			catch (Exception e)
			{
					filehtml.delete();
					JOptionPane pane = new JOptionPane(Keywords.Language.getMessage(172));
					JDialog dialog = pane.createDialog(MainGUI.desktop, Keywords.Language.getMessage(66));
					dialog.validate();
					dialog.setVisible(true);
					return;
			}
		}
		if(Source==exitmenu)
		{
			viewDataSet.dispose();
		}
	}
	/**
	* Permits the selection of the html file in which the dictionary will be saved
	*/
	public String selectfile()
	{
		String htmlFile="";
		String lastopeneddir=(String)System.getProperty("lastOpenedDirectory");
		JFileChooser sb = new JFileChooser(new File(lastopeneddir));
		sb.setFileFilter(new javax.swing.filechooser.FileFilter()
		{
			public boolean accept(File f)
			{
				return (f.getName().toLowerCase().endsWith(".html") || f.isDirectory());
			}
			public String getDescription()
			{
				return Keywords.Language.getMessage(360);
			}
		});
		sb.setAcceptAllFileFilterUsed(false);
		try
		{
			sb.showSaveDialog(null);
			File save = sb.getSelectedFile();
			htmlFile = save.getAbsolutePath();
			lastopeneddir=save.getParent();
			try
			{
				lastopeneddir=lastopeneddir.replaceAll("\\\\","/");
			}
			catch (Exception fs){}
			if (!lastopeneddir.endsWith(System.getProperty("file.separator")))
				lastopeneddir=lastopeneddir+System.getProperty("file.separator");
			System.setProperty("lastOpenedDirectory", lastopeneddir);
		}
		catch (Exception e) {}
		return htmlFile;
	}
	/**
	* Permits the selection of the html file in which the dictionary will be saved
	*/
	public String selectfiledata()
	{
		String htmlFile="";
		String lastopeneddir=(String)System.getProperty("lastOpenedDirectory");
		JFileChooser sb = new JFileChooser(new File(lastopeneddir));
		sb.setFileFilter(new javax.swing.filechooser.FileFilter()
		{
			public boolean accept(File f)
			{
				return (f.getName().toLowerCase().endsWith(".html") || f.isDirectory());
			}
			public String getDescription()
			{
				return Keywords.Language.getMessage(376);
			}
		});
		sb.setAcceptAllFileFilterUsed(false);
		try
		{
			sb.showSaveDialog(null);
			File save = sb.getSelectedFile();
			htmlFile = save.getAbsolutePath();
			lastopeneddir=save.getParent();
			try
			{
				lastopeneddir=lastopeneddir.replaceAll("\\\\","/");
			}
			catch (Exception fs){}
			if (!lastopeneddir.endsWith(System.getProperty("file.separator")))
				lastopeneddir=lastopeneddir+System.getProperty("file.separator");
			System.setProperty("lastOpenedDirectory", lastopeneddir);
		}
		catch (Exception e) {}
		return htmlFile;
	}
	public void MakeStats(String varname)
	{
		String typeselection=(String)typeview.getSelectedItem();
		replacer=1;
		if (typeselection.equalsIgnoreCase(Keywords.Language.getMessage(345)))
			replacer=0;
		boolean istowrite=false;
		String[] vtoread=new String[1];
		vtoread[0]=varname;
		int[] replace=new int[1];
		if (replacer==1)
			replace[0]=1;
		else
			replace[0]=0;
		if (!data.open(vtoread, replace, istowrite))
		{
			String errormsg=MessagesReplacer.replaceMessages(data.getmessage());
			JOptionPane pane = new JOptionPane(errormsg);
			JDialog dialog = pane.createDialog(MainGUI.desktop, Keywords.Language.getMessage(134));
			dialog.validate();
			dialog.setVisible(true);
			return;
		}
		double N=0;
		double NMISS=0;
		double MIN=Double.NaN;
		double MAX=Double.NaN;
		double MEAN=0;
		double DEVSTD=0;
		double val=Double.NaN;
		while (!data.isLast())
		{
			val=Double.NaN;
			values=data.getRecord();
			try
			{
				val=Double.parseDouble(values[0]);
			}
			catch (Exception e) {}
			if ((!Double.isNaN(val)) && (!Double.isInfinite(val)))
			{
				N++;
				MEAN=MEAN+val;
				DEVSTD=DEVSTD+val*val;
				if (Double.isNaN(MIN))
					MIN=val;
				if (Double.isNaN(MAX))
					MAX=val;
				if (val<MIN)
					MIN=val;
				if (val>MAX)
					MAX=val;
			}
			else
				NMISS++;
		}
		data.close();

		String table="<table border=\"1\"><caption>"+MessagesReplacer.replaceMessages("%1760%")+" "+dr.getvarlabelfromname(varname)+"</caption>\n";
		table=table+"<tr><th>"+MessagesReplacer.replaceMessages("%683%")+"</th>\n";
		table=table+"<th>"+MessagesReplacer.replaceMessages("%684%")+"</th>\n";
		table=table+"<th>"+MessagesReplacer.replaceMessages("%682%")+"</th>\n";
		table=table+"<th>"+MessagesReplacer.replaceMessages("%680%")+"</th>\n";
		table=table+"<th>"+MessagesReplacer.replaceMessages("%681%")+"</th>\n";
		table=table+"<th>"+MessagesReplacer.replaceMessages("%686%")+"</th></tr>\n";
		table=table+"<tr><td>"+String.valueOf(N)+"</td>\n";
		table=table+"<td>"+String.valueOf(NMISS)+"</td>\n";
		if (!Double.isNaN(MIN))
			table=table+"<td>"+String.valueOf(MIN)+"</td>\n";
		else
			table=table+"<td>&nbsp;</td>\n";
		if (!Double.isNaN(MAX))
			table=table+"<td>"+String.valueOf(MAX)+"</td>\n";
		else
			table=table+"<td>&nbsp;</td>\n";
		if (!Double.isNaN(MEAN))
			table=table+"<td>"+String.valueOf(MEAN/N)+"</td>\n";
		else
			table=table+"<td>&nbsp;</td>\n";
		DEVSTD=Math.sqrt((DEVSTD/N)-(MEAN/N)*(MEAN/N));
		if (!Double.isNaN(DEVSTD))
			table=table+"<td>"+String.valueOf(DEVSTD)+"</td>\n";
		else
			table=table+"<td>&nbsp;</td>";
		table=table+"</tr></table>\n\n\n";

		Document dc = MainGUI.OutputArea.getDocument();
		EditorKit ed = MainGUI.OutputArea.getEditorKit();
		try
		{
			CharArrayWriter cw = new CharArrayWriter();
			ed.write(cw, dc, 0, dc.getLength());
			String text = cw.toString();
			text = text.replace("</body>",table+"<hr></body>");
			MainGUI.OutputArea.setText(text);
			MainGUI.OutputArea.setCaretPosition(MainGUI.OutputArea.getDocument().getLength());
		}
		catch (Exception eee) {}
		JOptionPane pane = new JOptionPane(MessagesReplacer.replaceMessages("%1761%"));
		JDialog dialog = pane.createDialog(MainGUI.desktop, MessagesReplacer.replaceMessages("%1762%"));
		dialog.validate();
		dialog.setVisible(true);
	}
	public void MakeFreq(String varname)
	{
		String typeselection=(String)typeview.getSelectedItem();
		replacer=1;
		if (typeselection.equalsIgnoreCase(Keywords.Language.getMessage(345)))
			replacer=0;
		boolean istowrite=false;
		String[] vtoread=new String[1];
		vtoread[0]=varname;
		int[] replace=new int[1];
		istowrite=true;
		if (replacer==1)
			replace[0]=1;
		else
			replace[0]=0;
		if (!data.open(vtoread, replace, istowrite))
		{
			String errormsg=MessagesReplacer.replaceMessages(data.getmessage());
			JOptionPane pane = new JOptionPane(errormsg);
			JDialog dialog = pane.createDialog(MainGUI.desktop, Keywords.Language.getMessage(134));
			dialog.validate();
			dialog.setVisible(true);
			return;
		}
		TreeMap<String, Integer> freqs=new TreeMap<String, Integer>(new StringComparatorNoC());
		while (!data.isLast())
		{
			values=data.getRecord();
			if (!values[0].equals(""))
			{
				if (!freqs.containsKey(values[0]))
					freqs.put(values[0], new Integer(1));
				else
				{
					int csize=(freqs.get(values[0])).intValue()+1;
					freqs.put(values[0], new Integer(csize));
				}
			}
		}
		data.close();

		if (freqs.size()>500)
		{
			Object[] optionslg = {Keywords.Language.getMessage(1628), Keywords.Language.getMessage(1629)};
			int largeds =JOptionPane.showOptionDialog(MainGUI.desktop, Keywords.Language.getMessage(1627)+" ("+Keywords.Language.getMessage(1768)+" "+String.valueOf(freqs.size())+")", Keywords.Language.getMessage(134), JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,null, optionslg, optionslg[0]);
			if (largeds==1)
				return;
		}

		String table="<table border=\"1\"><caption>"+MessagesReplacer.replaceMessages("%1764%")+" "+dr.getvarlabelfromname(varname)+"</caption>\n";
		table=table+"<tr><th>"+MessagesReplacer.replaceMessages("%1765%")+"</th>\n";
		table=table+"<th>"+MessagesReplacer.replaceMessages("%1766%")+"</th></tr>\n";
		Iterator<String> ifreqs = freqs.keySet().iterator();
		while(ifreqs.hasNext())
		{
			String mod=ifreqs.next();
			int rif=(freqs.get(mod)).intValue();
			table=table+"<tr><td>"+mod+"</td>\n";
			table=table+"<td>"+String.valueOf(rif)+"</td></tr>\n";
		}
		table=table+"</table>\n\n\n";

		Document dc = MainGUI.OutputArea.getDocument();
		EditorKit ed = MainGUI.OutputArea.getEditorKit();
		try
		{
			CharArrayWriter cw = new CharArrayWriter();
			ed.write(cw, dc, 0, dc.getLength());
			String text = cw.toString();
			text = text.replace("</body>",table+"<hr></body>");
			MainGUI.OutputArea.setText(text);
			MainGUI.OutputArea.setCaretPosition(MainGUI.OutputArea.getDocument().getLength());
		}
		catch (Exception eee) {}
		JOptionPane pane = new JOptionPane(MessagesReplacer.replaceMessages("%1767%"));
		JDialog dialog = pane.createDialog(MainGUI.desktop, MessagesReplacer.replaceMessages("%1762%"));
		dialog.validate();
		dialog.setVisible(true);
	}
}

class ColorColumnRenderer extends DefaultTableCellRenderer
{
	private static final long serialVersionUID = 1L;
	Color bkgndColor, fgndColor;
	public ColorColumnRenderer(Color bkgnd, Color foregnd)
	{
		super();
		bkgndColor = bkgnd;
		fgndColor = foregnd;
	}
	public Component getTableCellRendererComponent (JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
	{
		Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		cell.setBackground( bkgndColor );
		cell.setForeground( fgndColor );
		return cell;
	}
}