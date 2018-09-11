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
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Vector;
import javax.swing.text.Document;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;

import ADaMSoft.keywords.Keywords;
import ADaMSoft.supervisor.ExecuteRunner;
import ADaMSoft.utilities.GetRequiredParameters;

/**
* This class builds the GUI that permits to generate the script for a step
* @author marco.scarno@gmail.com
* @date 03/09/2015
*/
public class ScriptGenerator
{
	Vector<Object[]> mainstatement=new Vector<Object[]>();
	Vector<Object[]> parameterstatement=new Vector<Object[]>();
	Hashtable<String, String> dictionaries=new Hashtable<String, String>();
	JInternalFrame FirstLevelPanel;
	JInternalFrame SecondaryLevelPanel;
	ImageIcon iconSet;
	LinkedList<?> parameters;
	String typestep;
	String stepname;
	String commandscript="";
	JButton exestep, retryexecute;
	int numparamters_level_1;
	/**
	*Build the GUI for the step sintax generator
	*/
	public ScriptGenerator(String actualname, LinkedList<?> parameters) throws Exception
	{
		numparamters_level_1=0;
		Keywords.currentExecutedStep="Script generator";
		mainstatement=new Vector<Object[]>();
		this.parameters=parameters;
		stepname=actualname;
		if (actualname.toUpperCase().startsWith(Keywords.PROC.toUpperCase()))
		{
			typestep=(Keywords.PROC.substring(0,1)).toUpperCase()+((Keywords.PROC.substring(1)).toLowerCase());
			stepname=stepname.substring(Keywords.PROC.length());
		}
		else if (actualname.toUpperCase().startsWith(Keywords.DOCUMENT.toUpperCase()))
		{
			typestep=(Keywords.DOCUMENT.substring(0,1)).toUpperCase()+((Keywords.DOCUMENT.substring(1)).toLowerCase());
			stepname=stepname.substring(Keywords.DOCUMENT.length());
		}
		else if (actualname.toUpperCase().startsWith(Keywords.TOC.toUpperCase()))
		{
			typestep=(Keywords.TOC.substring(0,1)).toUpperCase()+((Keywords.TOC.substring(1)).toLowerCase());
			stepname=stepname.substring(Keywords.TOC.length());
		}
		else
		{
			Keywords.currentExecutedStep="";
			return;
		}
        JPanel firstpanel = new JPanel(false);
        firstpanel.setLayout(new BoxLayout(firstpanel,BoxLayout.Y_AXIS));

		Iterator<?> i = parameters.iterator();
		while(i.hasNext())
		{
			GetRequiredParameters par = (GetRequiredParameters)i.next();
			if(par.getLevel()==1)
			{
				numparamters_level_1++;
				String parametertype= par.getType();
				String parametername= par.getName();
				int messagecode= par.getLabel();
				boolean isequal=(parametertype.indexOf("=")>0);
				if((parametertype.equalsIgnoreCase("path")) && (!isequal))
				{
					ParameterPath pp=new ParameterPath(parametername, messagecode, firstpanel, par.isMandatory());
					Object [] infoparameter=new Object[3];
					infoparameter[0]=pp;
					infoparameter[1]=new Boolean(par.isMandatory());
					infoparameter[2]=new Integer(messagecode);
					mainstatement.add(infoparameter);
				}
				if((parametertype.equalsIgnoreCase("note")) && (!isequal))
				{
					new ParameterNote(messagecode, firstpanel);
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
				if((parametertype.equalsIgnoreCase("CheckBox")) && (!isequal))
				{
					ParameterCheckBox cb=new ParameterCheckBox(parametername, messagecode, firstpanel, par.isMandatory());
					Object [] infoparameter=new Object[3];
					infoparameter[0]=cb;
					infoparameter[1]=new Boolean(par.isMandatory());
					infoparameter[2]=new Integer(messagecode);
					mainstatement.add(infoparameter);
				}
				if((parametertype.equalsIgnoreCase("Dict")) && (!isequal))
				{
					ParameterDict dict=new ParameterDict(parametername, messagecode, firstpanel, par.isMandatory());
					Object [] infoparameter=new Object[3];
					infoparameter[0]=dict;
					infoparameter[1]=new Boolean(par.isMandatory());
					infoparameter[2]=new Integer(messagecode);
					mainstatement.add(infoparameter);
				}
				if((parametertype.equalsIgnoreCase("MultipleDict")) && (!isequal))
				{
					ParameterMultipleDict multdict=new ParameterMultipleDict(parametername, messagecode, firstpanel, par.isMandatory(), par.getDefaultValue());
					Object [] infoparameter=new Object[3];
					infoparameter[0]=multdict;
					infoparameter[1]=new Boolean(par.isMandatory());
					infoparameter[2]=new Integer(messagecode);
					mainstatement.add(infoparameter);
				}
				if((parametertype.equalsIgnoreCase("MultipleSettingOut")) && (!isequal))
				{
					ParameterMultipleSettingOut multsetout=new ParameterMultipleSettingOut(parametername, messagecode, firstpanel, par.isMandatory(), par.getDefaultValue());
					Object [] infoparameter=new Object[3];
					infoparameter[0]=multsetout;
					infoparameter[1]=new Boolean(par.isMandatory());
					infoparameter[2]=new Integer(messagecode);
					mainstatement.add(infoparameter);
				}
				if((parametertype.equalsIgnoreCase("FreeDict")) && (!isequal))
				{
					ParameterFreeDict dict=new ParameterFreeDict(parametername, messagecode, firstpanel, par.isMandatory());
					Object [] infoparameter=new Object[3];
					infoparameter[0]=dict;
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
					if (parameterparts[0].equalsIgnoreCase("MultipleSettings"))
					{
						ParameterMultipleSettings settings=new ParameterMultipleSettings(parameterparts[1], messagecode, firstpanel, par.isMandatory());
						Object [] infoparameter=new Object[3];
						infoparameter[0]=settings;
						infoparameter[1]=new Boolean(par.isMandatory());
						infoparameter[2]=new Integer(messagecode);
						mainstatement.add(infoparameter);
					}
					if((parametertype.equalsIgnoreCase("file")))
					{
						ParameterFile pp=new ParameterFile(parameterparts[1],parametername, messagecode, firstpanel, par.isMandatory());
						Object [] infoparameter=new Object[3];
						infoparameter[0]=pp;
						infoparameter[1]=new Boolean(par.isMandatory());
						infoparameter[2]=new Integer(messagecode);
						mainstatement.add(infoparameter);
					}
					if((parametertype.equalsIgnoreCase("filesave")))
					{
						ParameterFileSave pp=new ParameterFileSave(parameterparts[1],parametername, messagecode, firstpanel, par.isMandatory());
						Object [] infoparameter=new Object[3];
						infoparameter[0]=pp;
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
				commandscript=typestep+" "+stepname+" ";
				for (int i=0; i<mainstatement.size(); i++)
				{
					Object[] infoparameter=mainstatement.get(i);
					String selection=infoparameter[0].toString();
					if (selection.startsWith(Keywords.dict))
					{
						String[] infodict=selection.split("=");
						dictionaries.put(infodict[0],infodict[1]);
					}
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
		String titleframe =Keywords.Language.getMessage(167)+" ("+typestep+" "+stepname+")";
		FirstLevelPanel = new JInternalFrame(titleframe, resizable, closeable, maximizable, iconifiable);
		FirstLevelPanel.addInternalFrameListener(new InternalFrameListener()
		{
			public void internalFrameClosing(InternalFrameEvent e)
			{
				Keywords.currentExecutedStep="";
			}
			public void internalFrameClosed(InternalFrameEvent e)
			{
				Keywords.currentExecutedStep="";
			}
			public void internalFrameOpened(InternalFrameEvent e) {}
			public void internalFrameIconified(InternalFrameEvent e) {}
			public void internalFrameDeiconified(InternalFrameEvent e) {}
			public void internalFrameActivated(InternalFrameEvent e) {}
			public void internalFrameDeactivated(InternalFrameEvent e){}
		});
		java.net.URL    url   = ScriptGenerator.class.getResource(Keywords.simpleicon);
		iconSet = new ImageIcon(url);
		int numrow = new Double(MainGUI.frame.getHeight()).intValue() - 52;
		int numcol = new Double(MainGUI.frame.getWidth()).intValue() - 14;
		FirstLevelPanel.setSize(numcol, numrow);
		FirstLevelPanel.setFrameIcon(iconSet);
		FirstLevelPanel.getContentPane().add(scrollfirstpanel);
		FirstLevelPanel.repaint();
		if (numparamters_level_1>0)
		{
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
		else
		{
			commandscript=typestep+" "+stepname+";";
			secondselection();
		}
	}
	public void secondselection()
	{
		parameterstatement=new Vector<Object[]>();
		FirstLevelPanel.setVisible(false);
		boolean resizable = true;
		boolean closeable = true;
		boolean maximizable  = true;
		boolean iconifiable = true;
		String title =Keywords.Language.getMessage(169)+" ("+typestep+" "+stepname+")";
		SecondaryLevelPanel = new JInternalFrame(title, resizable, closeable, maximizable, iconifiable);

        JPanel secondpanel = new JPanel(false);
		secondpanel.setLayout(new BoxLayout(secondpanel,BoxLayout.Y_AXIS));

		Iterator<?> i = parameters.iterator();
		while(i.hasNext())
		{
			GetRequiredParameters par = (GetRequiredParameters)i.next();
			if(par.getLevel()==2)
			{
				String parametertype= par.getType();
				String parametername= par.getName();
				int messagecode= par.getLabel();
				boolean isequal=(parametertype.indexOf("=")>0);
				String[] dep=par.getDependences();
				if((parametertype.equalsIgnoreCase("Text")) && (!isequal))
				{
					ParameterText text=new ParameterText(parametername, messagecode, secondpanel, par.isMandatory());
					Object [] infoparameter=new Object[3];
					infoparameter[0]=text;
					infoparameter[1]=new Boolean(par.isMandatory());
					infoparameter[2]=new Integer(messagecode);
					parameterstatement.add(infoparameter);
				}
				if((parametertype.equalsIgnoreCase("multipletext")) && (!isequal))
				{
					ParameterMultipleText text=new ParameterMultipleText(parametername, messagecode, secondpanel, par.isMandatory());
					Object [] infoparameter=new Object[3];
					infoparameter[0]=text;
					infoparameter[1]=new Boolean(par.isMandatory());
					infoparameter[2]=new Integer(messagecode);
					parameterstatement.add(infoparameter);
				}
				if((parametertype.equalsIgnoreCase("longtext")) && (!isequal))
				{
					ParameterLongText text=new ParameterLongText(parametername, messagecode, secondpanel, par.isMandatory());
					Object [] infoparameter=new Object[3];
					infoparameter[0]=text;
					infoparameter[1]=new Boolean(par.isMandatory());
					infoparameter[2]=new Integer(messagecode);
					parameterstatement.add(infoparameter);
				}
				if((parametertype.equalsIgnoreCase("note")) && (!isequal))
				{
					new ParameterNote(messagecode, secondpanel);
				}
				if((parametertype.equalsIgnoreCase("CheckBox")) && (!isequal))
				{
					ParameterCheckBox cb=new ParameterCheckBox(parametername, messagecode, secondpanel, par.isMandatory());
					Object [] infoparameter=new Object[3];
					infoparameter[0]=cb;
					infoparameter[1]=new Boolean(par.isMandatory());
					infoparameter[2]=new Integer(messagecode);
					parameterstatement.add(infoparameter);
				}
				if((parametertype.equalsIgnoreCase("Dir")) && (!isequal))
				{
					ParameterDir sdir=new ParameterDir(parametername, messagecode, secondpanel, par.isMandatory());
					Object [] infoparameter=new Object[3];
					infoparameter[0]=sdir;
					infoparameter[1]=new Boolean(par.isMandatory());
					infoparameter[2]=new Integer(messagecode);
					parameterstatement.add(infoparameter);
				}
				if((parametertype.equalsIgnoreCase("Dict")) && (!isequal))
				{
					ParameterDict dict=new ParameterDict(parametername, messagecode, secondpanel, par.isMandatory());
					Object [] infoparameter=new Object[3];
					infoparameter[0]=dict;
					infoparameter[1]=new Boolean(par.isMandatory());
					infoparameter[2]=new Integer(messagecode);
					parameterstatement.add(infoparameter);
				}
				if(parametertype.equalsIgnoreCase("TextVars"))
				{
					ParameterTextVars textvarsel=new ParameterTextVars(parametername, messagecode, secondpanel, dictionaries, dep, par.isMandatory());
					if (!textvarsel.getErrorSel())
					{
						Object [] infoparameter=new Object[3];
						infoparameter[0]=textvarsel;
						infoparameter[1]=new Boolean(par.isMandatory());
						infoparameter[2]=new Integer(messagecode);
						parameterstatement.add(infoparameter);
					}
					else
					{
						JOptionPane pane = new JOptionPane(textvarsel.getmsg());
						JDialog dialog = pane.createDialog(MainGUI.desktop, Keywords.Language.getMessage(166));
						dialog.validate();
						dialog.setVisible(true);
						SecondaryLevelPanel.dispose();
					}
				}
				if(parametertype.equalsIgnoreCase("TextVarsWs"))
				{
					ParameterTextVarsWs textvarsel=new ParameterTextVarsWs(parametername, messagecode, secondpanel, dictionaries, dep, par.isMandatory());
					if (!textvarsel.getErrorSel())
					{
						Object [] infoparameter=new Object[3];
						infoparameter[0]=textvarsel;
						infoparameter[1]=new Boolean(par.isMandatory());
						infoparameter[2]=new Integer(messagecode);
						parameterstatement.add(infoparameter);
					}
					else
					{
						JOptionPane pane = new JOptionPane(textvarsel.getmsg());
						JDialog dialog = pane.createDialog(MainGUI.desktop, Keywords.Language.getMessage(166));
						dialog.validate();
						dialog.setVisible(true);
						SecondaryLevelPanel.dispose();
					}
				}
				if (isequal)
				{
					String[] parameterparts=parametertype.split("=");
					if (parameterparts[0].equalsIgnoreCase("File"))
					{
						ParameterFile file=new ParameterFile(parameterparts[1], parametername, messagecode, secondpanel, par.isMandatory());
						Object [] infoparameter=new Object[3];
						infoparameter[0]=file;
						infoparameter[1]=new Boolean(par.isMandatory());
						infoparameter[2]=new Integer(messagecode);
						parameterstatement.add(infoparameter);
					}
					if((parameterparts[0].equalsIgnoreCase("filesave")))
					{
						ParameterFileSave pfs=new ParameterFileSave(parameterparts[1],parametername, messagecode, secondpanel, par.isMandatory());
						Object [] infoparameter=new Object[3];
						infoparameter[0]=pfs;
						infoparameter[1]=new Boolean(par.isMandatory());
						infoparameter[2]=new Integer(messagecode);
						parameterstatement.add(infoparameter);
					}
					if(parameterparts[0].equalsIgnoreCase("ListMultiple"))
					{
						ParameterListMultiple optsel=new ParameterListMultiple(parameterparts[1], parametername, messagecode, secondpanel, SecondaryLevelPanel, par.isMandatory());
						Object [] infoparameter=new Object[3];
						infoparameter[0]=optsel;
						infoparameter[1]=new Boolean(par.isMandatory());
						infoparameter[2]=new Integer(messagecode);
						parameterstatement.add(infoparameter);
					}
					if(parameterparts[0].equalsIgnoreCase("Vars"))
					{
						ParameterVars varsel=new ParameterVars(parameterparts[1], parametername, messagecode, secondpanel, dictionaries, dep, true, SecondaryLevelPanel, par.isMandatory());
						if (!varsel.getErrorSel())
						{
							Object [] infoparameter=new Object[3];
							infoparameter[0]=varsel;
							infoparameter[1]=new Boolean(par.isMandatory());
							infoparameter[2]=new Integer(messagecode);
							parameterstatement.add(infoparameter);
						}
						else
						{
							JOptionPane pane = new JOptionPane(varsel.getmsg());
							JDialog dialog = pane.createDialog(MainGUI.desktop, Keywords.Language.getMessage(166));
							dialog.validate();
							dialog.setVisible(true);
							SecondaryLevelPanel.dispose();
						}
					}
					if(parameterparts[0].equalsIgnoreCase("Var"))
					{
						ParameterVars varsel=new ParameterVars(parameterparts[1], parametername, messagecode, secondpanel, dictionaries, dep, false, SecondaryLevelPanel, par.isMandatory());
						if (!varsel.getErrorSel())
						{
							Object [] infoparameter=new Object[3];
							infoparameter[0]=varsel;
							infoparameter[1]=new Boolean(par.isMandatory());
							infoparameter[2]=new Integer(messagecode);
							parameterstatement.add(infoparameter);
						}
						else
						{
							JOptionPane pane = new JOptionPane(varsel.getmsg());
							JDialog dialog = pane.createDialog(MainGUI.desktop, Keywords.Language.getMessage(166));
							dialog.validate();
							dialog.setVisible(true);
							SecondaryLevelPanel.dispose();
						}
					}
					if(parameterparts[0].equalsIgnoreCase("ListSingle"))
					{
						ParameterListSingle pls=new ParameterListSingle(parameterparts[1], parametername, messagecode, secondpanel, par.isMandatory());
						Object [] infoparameter=new Object[3];
						infoparameter[0]=pls;
						infoparameter[1]=new Boolean(par.isMandatory());
						infoparameter[2]=new Integer(messagecode);
						parameterstatement.add(infoparameter);
					}

				}
			}
		}
		Action executestep = new AbstractAction(Keywords.Language.getMessage(170))
		{
			private static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent evt)
			{
				String actualcommand=commandscript;
				for (int i=0; i<parameterstatement.size(); i++)
				{
					Object[] infoparameter=parameterstatement.get(i);
					String values=infoparameter[0].toString();
					if (!values.equals(""))
						actualcommand=actualcommand+values+";\n";
					boolean mandatory=((Boolean)infoparameter[1]).booleanValue();
					int messagecode=((Integer)infoparameter[2]).intValue();
					if ((mandatory) && (values.equals("")))
					{
						JOptionPane pane = new JOptionPane(Keywords.Language.getMessage(messagecode));
						JDialog dialog = pane.createDialog(MainGUI.desktop, Keywords.Language.getMessage(166));
						dialog.validate();
						dialog.setVisible(true);
						return;
					}
				}
				actualcommand=actualcommand+"run;\n";
				exestep.setEnabled(false);
				retryexecute.setEnabled(true);
				ExecuteStep es=new ExecuteStep(actualcommand);
				es.start();
				Keywords.currentExecutedStep="Script generator";
			}
		};
		Action tryexecute = new AbstractAction(Keywords.Language.getMessage(3968))
		{
			private static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent evt)
			{
				if (Keywords.currentExecutedStep.equals(""))
				{
					exestep.setEnabled(true);
					retryexecute.setEnabled(false);
				}
			}
		};
		Action putstep = new AbstractAction(Keywords.Language.getMessage(172))
		{
			private static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent evt)
			{
				String actualcommand=commandscript;
				for (int i=0; i<parameterstatement.size(); i++)
				{
					Object[] infoparameter=parameterstatement.get(i);
					String values=infoparameter[0].toString();
					if (!values.equals(""))
						actualcommand=actualcommand+values+";\n";
					boolean mandatory=((Boolean)infoparameter[1]).booleanValue();
					int messagecode=((Integer)infoparameter[2]).intValue();
					if ((mandatory) && (values.equals("")))
					{
						JOptionPane pane = new JOptionPane(Keywords.Language.getMessage(messagecode));
						JDialog dialog = pane.createDialog(MainGUI.desktop, Keywords.Language.getMessage(166));
						dialog.validate();
						dialog.setVisible(true);
						return;
					}
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
		exestep = new JButton(executestep);
		if (!Keywords.currentExecutedStep.equals("Script generator")) exestep.setEnabled(false);
		JButton savestep = new JButton(putstep);
		JButton exitsecond = new JButton(backtofirst);
		JButton exittogui = new JButton(backtogui);
		retryexecute=new JButton(tryexecute);
		retryexecute.setEnabled(false);

		JPanel secondgrid=new JPanel(new GridLayout(5,1));

		secondgrid.add(exestep);
		secondgrid.add(retryexecute);
		secondgrid.add(savestep);
		secondgrid.add(exitsecond);
		if (numparamters_level_1==0) exitsecond.setEnabled(false);
		secondgrid.add(exittogui);

		Border loweredbevel = BorderFactory.createLoweredBevelBorder();
		TitledBorder titlefirstpanel = BorderFactory.createTitledBorder(loweredbevel, Keywords.Language.getMessage(189));
		secondgrid.setBorder(titlefirstpanel);

   		secondpanel.add(secondgrid);

		int numrow = new Double(MainGUI.frame.getHeight()).intValue() - 52;
		int numcol = new Double(MainGUI.frame.getWidth()).intValue() - 14;
		secondpanel.setSize(numcol, numrow);

		JScrollPane scrollsecondpanel = new JScrollPane(secondpanel);

		scrollsecondpanel.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollsecondpanel.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);


		SecondaryLevelPanel.addInternalFrameListener(new InternalFrameListener()
		{
			public void internalFrameClosing(InternalFrameEvent e)
			{
				if (numparamters_level_1>0) FirstLevelPanel.setVisible(true);
				else FirstLevelPanel.dispose();
			}
			public void internalFrameClosed(InternalFrameEvent e)
			{
				if (numparamters_level_1>0) FirstLevelPanel.setVisible(true);
				else FirstLevelPanel.dispose();
			}
			public void internalFrameOpened(InternalFrameEvent e) {}
			public void internalFrameIconified(InternalFrameEvent e) {}
			public void internalFrameDeiconified(InternalFrameEvent e) {}
			public void internalFrameActivated(InternalFrameEvent e) {}
			public void internalFrameDeactivated(InternalFrameEvent e){}
		});
		java.net.URL    url   = ScriptGenerator.class.getResource(Keywords.simpleicon);
		iconSet = new ImageIcon(url);

		SecondaryLevelPanel.setFrameIcon(iconSet);
		SecondaryLevelPanel.getContentPane().add(scrollsecondpanel);
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
	private class ExecuteStep extends Thread
	{
		String text;
		public ExecuteStep(String text)
		{
			Keywords.general_percentage_total=0;
			Keywords.general_percentage_done=0;
			Keywords.numread=0.0;
			Keywords.operationReading=false;
			Keywords.numwrite=0.0;
			Keywords.operationWriting=false;
			this.text=text;
			Keywords.currentExecutedStep="Processing statements";
		}
		public void run() throws NullPointerException
		{
			new ExecuteRunner(2, text);
		}
	}
}
