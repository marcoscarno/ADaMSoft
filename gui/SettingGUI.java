/**
* Copyright (c) 2015 ms
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
import java.awt.event.ActionEvent;
import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Vector;
import java.util.jar.JarFile;
import java.util.jar.JarEntry;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;

import ADaMSoft.dataaccess.DataTableWriter;
import ADaMSoft.keywords.Keywords;
import ADaMSoft.supervisor.ExecuteRunner;
import ADaMSoft.utilities.GetSettingParameters;
import ADaMSoft.utilities.SettingParameters;

/**
* This class builds the GUI that permits to work on settings
* @author marco.scarno@gmail.com
* @DATE 07/09/2015
*/
public class SettingGUI
{
	JInternalFrame FrameSetting;
	ImageIcon iconSet;
	JTextField[] singleparameter;
	boolean[] required;
	int[] message;
	String[] parname;
	JTextField namesetting;
	int totalparameter;
	String settingname;
	String settingtype;
	int typeaction;
	String selectedtype;
	Vector<String> codelabel;
	/**
	*Creates a GUI that permits to work on Settings
	*/
	public SettingGUI(String settype, String setname, int actiontype)
	{
		Keywords.currentExecutedStep="Setting generator";
		typeaction=actiontype;
		settingtype=settype;
		settingname=setname;

        JPanel panel=new JPanel();
		panel.setLayout(new BoxLayout(panel,BoxLayout.Y_AXIS));

		if (actiontype==1)
		{
			JPanel firstgrid=new JPanel(new GridLayout(1,1));
			JLabel delallsetting=new JLabel(Keywords.Language.getMessage(398));
			firstgrid.add(delallsetting);
			panel.add(firstgrid);
		}
		if (actiontype==2)
		{
			JPanel newname=new JPanel(new GridLayout(2,1));
			JLabel labelname=new JLabel(Keywords.Language.getMessage(409));
			namesetting=new JTextField();
			newname.add(labelname);
			newname.add(namesetting);
			panel.add(newname);
			if ((!settingtype.equalsIgnoreCase(Keywords.OUT)) && (!settingtype.equalsIgnoreCase(Keywords.CODELABEL)))
			{
				LinkedList<GetSettingParameters> setpar=SettingParameters.getsetpar(settingtype, null);
				JPanel firstgrid=new JPanel(new GridLayout(setpar.size(),1));
				singleparameter=new JTextField[setpar.size()];
				required=new boolean[setpar.size()];
				message=new int[setpar.size()];
				totalparameter=setpar.size();
				Iterator<GetSettingParameters> itpar = setpar.iterator();
				parname=new String[setpar.size()];
				int rif=0;
				while(itpar.hasNext())
				{
					JPanel pargrid=new JPanel(new GridLayout(1,2));
					GetSettingParameters par = (GetSettingParameters)itpar.next();
					singleparameter[rif]=new JTextField();
					JLabel label=new JLabel(Keywords.Language.getMessage(par.getLabel()));
					pargrid.add(label);
					pargrid.add(singleparameter[rif]);
					parname[rif]=(par.getName()).toLowerCase();
					label.setToolTipText(parname[rif]);
					singleparameter[rif].setToolTipText(parname[rif]);
					required[rif]=par.isMandatory();
					message[rif]=par.getLabel();
					firstgrid.add(pargrid);
					rif++;
				}
				panel.add(firstgrid);
			}
			if (settingtype.equalsIgnoreCase(Keywords.CODELABEL))
			{
				codelabel=new Vector<String>();
				JPanel firstgrid=new JPanel(new GridLayout(1,1));
				Action addcodelabel = new AbstractAction(Keywords.Language.getMessage(410))
				{
					private static final long serialVersionUID = 1L;
					public void actionPerformed(ActionEvent evt)
					{
						String newcl = JOptionPane.showInputDialog(Keywords.Language.getMessage(410));
						newcl=newcl.trim();
						if (!newcl.equals(""))
							codelabel.add(newcl);
					}
				};
				JButton addcl=new JButton(addcodelabel);
				firstgrid.add(addcl);
				panel.add(firstgrid);
			}
			if (settingtype.equalsIgnoreCase(Keywords.OUT))
			{
				Vector<Object> outtype=new Vector<Object>();
				try
				{
					URL fp = getClass().getProtectionDomain().getCodeSource().getLocation();
					URI ffp=new URI(fp.toURI().getScheme(), null, fp.toURI().getPath(), fp.toURI().getQuery(), fp.toURI().getFragment());
					String fpath=ffp.getPath();
					if (System.getProperty("execute_debug")!=null)
					{
						if (System.getProperty("execute_debug").equalsIgnoreCase("yes"))
							fpath="c:/ADaMSoft/ADaMSoft.jar";
					}
					File file = new File(fpath);
					JarFile procJar = new JarFile(file);
					for(Enumeration<JarEntry> en=procJar.entries(); en.hasMoreElements(); )
					{
						String nameProc = (en.nextElement()).toString();
						if ((nameProc.endsWith(".class")) && (nameProc.indexOf("Write_")>0) && (nameProc.indexOf("&")<0))
						{
							nameProc=nameProc.substring(nameProc.indexOf("dataaccess/Write_")+"dataaccess/Write_".length(),nameProc.indexOf(".class"));
							outtype.add(nameProc);
						}
					}
					Object[] possibledatatype =new Object[outtype.size()];
					for (int i=0; i<outtype.size(); i++)
					{
						possibledatatype[i]=outtype.get(i);
					}
					Object selectedDatatype = JOptionPane.showInputDialog(MainGUI.desktop,Keywords.Language.getMessage(404), Keywords.Language.getMessage(403),
					JOptionPane.INFORMATION_MESSAGE, null,possibledatatype, possibledatatype[0]);
					if (selectedDatatype==null)
					{
						Keywords.currentExecutedStep="";
						return;
					}
					selectedtype=selectedDatatype.toString();
					LinkedList<GetSettingParameters> setpar=new LinkedList<GetSettingParameters>();
					try
					{
						Class<?> classtowrite= Class.forName(Keywords.SoftwareName+".dataaccess.Write_"+selectedtype.toLowerCase());
						DataTableWriter datatable = (DataTableWriter)classtowrite.newInstance();
						setpar=datatable.initialize();
					}
					catch (Exception e)
					{
						JOptionPane pane = new JOptionPane(Keywords.Language.getMessage(406));
						JDialog dialog = pane.createDialog(MainGUI.desktop, Keywords.Language.getMessage(134));
						dialog.validate();
						dialog.setVisible(true);
						Keywords.currentExecutedStep="";
						return;
					}
					JPanel firstgrid=new JPanel(new GridLayout(setpar.size(),1));
					singleparameter=new JTextField[setpar.size()];
					required=new boolean[setpar.size()];
					message=new int[setpar.size()];
					totalparameter=setpar.size();
					Iterator<GetSettingParameters> itpar = setpar.iterator();
					parname=new String[setpar.size()];
					int rif=0;
					while(itpar.hasNext())
					{
						JPanel pargrid=new JPanel(new GridLayout(1,2));
						GetSettingParameters par = (GetSettingParameters)itpar.next();
						singleparameter[rif]=new JTextField();
						JLabel label=new JLabel(Keywords.Language.getMessage(par.getLabel()));
						pargrid.add(label);
						pargrid.add(singleparameter[rif]);
						parname[rif]=(par.getName()).toLowerCase();
						label.setToolTipText(parname[rif]);
						singleparameter[rif].setToolTipText(parname[rif]);
						required[rif]=par.isMandatory();
						firstgrid.add(pargrid);
						message[rif]=par.getLabel();
						rif++;
					}
					panel.add(firstgrid);
				}
				catch (Exception e)
				{
					JOptionPane pane = new JOptionPane(Keywords.Language.getMessage(407));
					JDialog dialog = pane.createDialog(MainGUI.desktop, Keywords.Language.getMessage(134));
					dialog.validate();
					dialog.setVisible(true);
					Keywords.currentExecutedStep="";
					return;
				}
			}
		}

		if (actiontype==3)
		{
			JPanel firstgrid=new JPanel(new GridLayout(1,1));
			JLabel delsetting=new JLabel(Keywords.Language.getMessage(400)+" "+settype);
			firstgrid.add(delsetting);
			panel.add(firstgrid);
		}

		if (actiontype==4)
		{
			Hashtable<String, String> settingvalue=Keywords.project.getSetting(settingtype, settingname);
			if (settingvalue.size()==0)
			{
				JOptionPane pane = new JOptionPane(Keywords.Language.getMessage(408));
				JDialog dialog = pane.createDialog(MainGUI.desktop, Keywords.Language.getMessage(134));
				dialog.validate();
				dialog.setVisible(true);
				Keywords.currentExecutedStep="";
				return;
			}
			JPanel firstgrid=new JPanel(new GridLayout(settingvalue.size(),1));
			singleparameter=new JTextField[settingvalue.size()];
			parname=new String[settingvalue.size()];
			totalparameter=settingvalue.size();
			int rif=0;
			for (Enumeration<String> e = settingvalue.keys() ; e.hasMoreElements() ;)
			{
				String par = e.nextElement();
				String val = settingvalue.get(par);
				JPanel pargrid=new JPanel(new GridLayout(1,2));
				JLabel label=new JLabel(par);
				parname[rif]=par;
				singleparameter[rif]=new JTextField(val);
				pargrid.add(label);
				pargrid.add(singleparameter[rif]);
				rif++;
				firstgrid.add(pargrid);
			}
			panel.add(firstgrid);
		}

		if (actiontype==5)
		{
			JPanel firstgrid=new JPanel(new GridLayout(1,1));
			JLabel delsinglesetting=new JLabel(Keywords.Language.getMessage(402)+" "+settype+" "+setname);
			firstgrid.add(delsinglesetting);
			panel.add(firstgrid);
		}

		Action executestep = new AbstractAction(Keywords.Language.getMessage(170))
		{
			private static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent evt)
			{
				String commandtoexecute="";
				if (typeaction==1)
				{
					commandtoexecute=Keywords.SETTING+" "+Keywords.clear+";\n"+"end;\n";
				}
				if (typeaction==2)
				{
					settingname=namesetting.getText();
					settingname.trim();
					if (settingname.equals(""))
					{
						JOptionPane pane = new JOptionPane(Keywords.Language.getMessage(48));
						JDialog dialog = pane.createDialog(MainGUI.desktop, Keywords.Language.getMessage(166));
						dialog.validate();
						dialog.setVisible(true);
						return;
					}
					commandtoexecute=Keywords.SETTING+" "+settingtype+";\n";
					commandtoexecute=commandtoexecute+Keywords.Name+" "+settingname+";\n";
					if (settingtype.equalsIgnoreCase(Keywords.CODELABEL))
					{
						if (codelabel.size()==0)
						{
							JOptionPane pane = new JOptionPane(Keywords.Language.getMessage(411));
							JDialog dialog = pane.createDialog(MainGUI.desktop, Keywords.Language.getMessage(134));
							dialog.validate();
							dialog.setVisible(true);
							return;
						}
						for (int i=0; i<codelabel.size(); i++)
						{
							String par=codelabel.get(i);
							commandtoexecute=commandtoexecute+par+" "+";\n";
						}
					}
					else
					{
						for (int i=0; i<totalparameter; i++)
						{
							String par=parname[i];
							String val=singleparameter[i].getText();
							if (par!=null)
							{
								par=par.trim();
								if (val!=null)
								{
									val=val.trim();
									if (!val.equals(""))
										commandtoexecute=commandtoexecute+par+" "+val+";\n";
									if ((val.equals("")) && (required[i]))
									{
										JOptionPane pane = new JOptionPane(Keywords.Language.getMessage(message[i]));
										JDialog dialog = pane.createDialog(MainGUI.desktop, Keywords.Language.getMessage(166));
										dialog.validate();
										dialog.setVisible(true);
										return;
									}
								}
								else
								{
									if (required[i])
									{
										JOptionPane pane = new JOptionPane(Keywords.Language.getMessage(message[i]));
										JDialog dialog = pane.createDialog(MainGUI.desktop, Keywords.Language.getMessage(166));
										dialog.validate();
										dialog.setVisible(true);
										return;
									}
								}
							}
						}
						if (settingtype.equalsIgnoreCase(Keywords.OUT))
							commandtoexecute=commandtoexecute+Keywords.datatype+" "+selectedtype+";\n";
					}
					commandtoexecute=commandtoexecute+"end;\n";
				}
				if (typeaction==3)
				{
					commandtoexecute=Keywords.SETTING+" "+settingtype+";\n"+Keywords.clear+";\n"+"end;\n";
				}
				if (typeaction==4)
				{
					commandtoexecute=Keywords.SETTING+" "+settingtype+";\n";
					commandtoexecute=commandtoexecute+Keywords.Name+" "+settingname+";\n";
					for (int i=0; i<totalparameter; i++)
					{
						String par=parname[i].trim();
						String val=singleparameter[i].getText();
						val=val.trim();
						if (!par.equals(""))
						{
							if (!val.equals(""))
								commandtoexecute=commandtoexecute+par+" "+val+";\n";
						}
					}
					commandtoexecute=commandtoexecute+"end;\n";
				}
				if (typeaction==5)
				{
					commandtoexecute=Keywords.SETTING+" "+settingtype+";\n"+Keywords.Name+" "+settingname+";\n"+Keywords.clear+";\n"+"end;\n";
				}
				new ExecuteRunner(2, commandtoexecute);
			}
		};
		Action putstep = new AbstractAction(Keywords.Language.getMessage(172))
		{
			private static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent evt)
			{
				String commandtoexecute="";
				if (typeaction==1)
				{
					commandtoexecute=Keywords.SETTING+" "+Keywords.clear+";\n"+"end;\n";
				}
				if (typeaction==2)
				{
					settingname=namesetting.getText();
					settingname.trim();
					if (settingname.equals(""))
					{
						JOptionPane pane = new JOptionPane(Keywords.Language.getMessage(48));
						JDialog dialog = pane.createDialog(MainGUI.desktop, Keywords.Language.getMessage(166));
						dialog.validate();
						dialog.setVisible(true);
						return;
					}
					commandtoexecute=Keywords.SETTING+" "+settingtype+";\n";
					commandtoexecute=commandtoexecute+Keywords.Name+" "+settingname+";\n";
					if (settingtype.equalsIgnoreCase(Keywords.CODELABEL))
					{
						if (codelabel.size()==0)
						{
							JOptionPane pane = new JOptionPane(Keywords.Language.getMessage(411));
							JDialog dialog = pane.createDialog(MainGUI.desktop, Keywords.Language.getMessage(134));
							dialog.validate();
							dialog.setVisible(true);
							return;
						}
						for (int i=0; i<codelabel.size(); i++)
						{
							String par=codelabel.get(i);
							commandtoexecute=commandtoexecute+par+" "+";\n";
						}
					}
					else
					{
						for (int i=0; i<totalparameter; i++)
						{
							String par=parname[i];
							String val=singleparameter[i].getText();
							if (par!=null)
							{
								par=par.trim();
								if (val!=null)
								{
									val=val.trim();
									if (!val.equals(""))
										commandtoexecute=commandtoexecute+par+" "+val+";\n";
									if ((val.equals("")) && (required[i]))
									{
										JOptionPane pane = new JOptionPane(Keywords.Language.getMessage(message[i]));
										JDialog dialog = pane.createDialog(MainGUI.desktop, Keywords.Language.getMessage(166));
										dialog.validate();
										dialog.setVisible(true);
										return;
									}
								}
								else
								{
									if (required[i])
									{
										JOptionPane pane = new JOptionPane(Keywords.Language.getMessage(message[i]));
										JDialog dialog = pane.createDialog(MainGUI.desktop, Keywords.Language.getMessage(166));
										dialog.validate();
										dialog.setVisible(true);
										return;
									}
								}
							}
						}
						if (settingtype.equalsIgnoreCase(Keywords.OUT))
							commandtoexecute=commandtoexecute+Keywords.datatype+" "+selectedtype+";\n";
					}
					commandtoexecute=commandtoexecute+"end;\n";
				}
				if (typeaction==3)
				{
					commandtoexecute=Keywords.SETTING+" "+settingtype+";\n"+Keywords.clear+";\n"+"end;\n";
				}
				if (typeaction==4)
				{
					commandtoexecute=Keywords.SETTING+" "+settingtype+";\n";
					commandtoexecute=commandtoexecute+Keywords.Name+" "+settingname+";\n";
					for (int i=0; i<totalparameter; i++)
					{
						String par=parname[i].trim();
						String val=singleparameter[i].getText();
						val=val.trim();
						if (!par.equals(""))
						{
							if (!val.equals(""))
								commandtoexecute=commandtoexecute+par+" "+val+";\n";
						}
					}
					commandtoexecute=commandtoexecute+"end;\n";
				}
				if (typeaction==5)
				{
					commandtoexecute=Keywords.SETTING+" "+settingtype+";\n"+Keywords.Name+" "+settingname+";\n"+Keywords.clear+";\n"+"end;\n";
				}
				Document doc = MainGUI.EditorArea.getDocument();
				Document blank = new DefaultStyledDocument();
				MainGUI.EditorArea.setDocument(blank);
				try
				{
					doc.insertString(doc.getLength(), "\n"+commandtoexecute+"\n", null);
				}
				catch (BadLocationException ef) {}
				MainGUI.EditorArea.setDocument(doc);
				MainGUI.EditorArea.setCaretPosition(MainGUI.EditorArea.getDocument().getLength());
			}
		};

		Action backtogui = new AbstractAction(Keywords.Language.getMessage(168))
		{
			private static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent evt)
			{
				FrameSetting.dispose();
			}
		};

		JButton exestep = new JButton(executestep);
		JButton savestep = new JButton(putstep);
		JButton exittogui = new JButton(backtogui);

		JPanel secondgrid=new JPanel(new GridLayout(3,1));

		secondgrid.add(exestep);
		secondgrid.add(savestep);
		secondgrid.add(exittogui);

		Border loweredbevel = BorderFactory.createLoweredBevelBorder();
		TitledBorder titlefirstpanel = BorderFactory.createTitledBorder(loweredbevel, Keywords.Language.getMessage(189));
		secondgrid.setBorder(titlefirstpanel);

		panel.add(secondgrid);

		boolean resizable = true;
		boolean closeable = true;
		boolean maximizable  = true;
		boolean iconifiable = true;
		String title="";
		if (actiontype==1)
			title =Keywords.Language.getMessage(398);
		if (actiontype==2)
			title =Keywords.Language.getMessage(399);
		if (actiontype==3)
			title =Keywords.Language.getMessage(400);
		if (actiontype==4)
			title =Keywords.Language.getMessage(401);
		if (actiontype==5)
			title =Keywords.Language.getMessage(402);
		FrameSetting = new JInternalFrame(title, resizable, closeable, maximizable, iconifiable);

		FrameSetting.addInternalFrameListener(new InternalFrameListener()
		{
			public void internalFrameClosing(InternalFrameEvent e)
			{
				Keywords.currentExecutedStep="";
				return;
			}
			public void internalFrameClosed(InternalFrameEvent e)
			{
				Keywords.currentExecutedStep="";
				return;
			}
			public void internalFrameOpened(InternalFrameEvent e) {}
			public void internalFrameIconified(InternalFrameEvent e) {}
			public void internalFrameDeiconified(InternalFrameEvent e) {}
			public void internalFrameActivated(InternalFrameEvent e) {}
			public void internalFrameDeactivated(InternalFrameEvent e){}
		});

		JScrollPane scroll= new JScrollPane(panel);
		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		int numrow = new Double(MainGUI.frame.getHeight()).intValue() - 52;
		int numcol = new Double(MainGUI.frame.getWidth()).intValue() - 14;
		scroll.setSize(numcol, numrow);

		java.net.URL    url   = SettingGUI.class.getResource(Keywords.simpleicon);
		iconSet = new ImageIcon(url);
		FrameSetting.setSize(numcol, numrow);
		FrameSetting.setFrameIcon(iconSet);
		FrameSetting.getContentPane().add(scroll);
		FrameSetting.repaint();
		FrameSetting.setVisible(true);
		FrameSetting.pack();
		MainGUI.desktop.add(FrameSetting);
		MainGUI.desktop.repaint();
		FrameSetting.moveToFront();
		try
		{
			FrameSetting.setEnabled(true);
			FrameSetting.toFront();
			FrameSetting.show();
			FrameSetting.setSelected(true);
		}
		catch (Exception e) {}
	}
}
