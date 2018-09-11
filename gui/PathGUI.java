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

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
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

import ADaMSoft.keywords.Keywords;
import ADaMSoft.supervisor.ExecuteRunner;

/**
* This class builds the GUI that permits to add, delete, modify a PATH
* @author marco.scarno@gmail.com
* @date 05/09/2015
*/
public class PathGUI
{
	JInternalFrame framepath;
	ImageIcon iconimage;
	JPanel panelpath;
	JTextField pathname;
	JTextField pathvalue=new JTextField(10);
	JPanel gridpanel;
	String namepath;
	boolean isnottodelete;
	/**
	*Permits to insert a new PATH (if innamepath is empty and isnottodelete is true);<p>
	*Permits to modify an existent PATH (if innamepath is not empty and isnottodelete is true);<p>
	*Permits to delete an existent PATH (if innamepath is not empty and isnottodelete is false);<p>
	*Permits to delete all the existent PATHs (if innamepath is not empty and isnottodelete is false).
	*/
	public PathGUI(String innamepath, boolean inisnottodelete)
	{
		this.isnottodelete=inisnottodelete;
		this.namepath=innamepath;

		boolean resizable = true;
		boolean closeable = true;
		boolean maximizable  = true;
		boolean iconifiable = true;

        panelpath = new JPanel();
		panelpath.setLayout(new BoxLayout(panelpath,BoxLayout.Y_AXIS));

		Action selectlocaldirectory = new AbstractAction(Keywords.Language.getMessage(227))
		{
			private static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent evt)
			{
				String lastopeneddir=(String)System.getProperty("lastOpenedDirectory");
				JFileChooser chooser = new JFileChooser(new File(lastopeneddir));
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				try
				{
					chooser.showOpenDialog(MainGUI.desktop);
					File openeddirectory= chooser.getSelectedFile();
					String selecteddirectory=openeddirectory.getAbsolutePath();
					lastopeneddir = openeddirectory.getPath();
					try
					{
						lastopeneddir=lastopeneddir.replaceAll("\\\\","/");
					}
					catch (Exception fs){}
					if (!lastopeneddir.endsWith(System.getProperty("file.separator")))
						lastopeneddir=lastopeneddir+System.getProperty("file.separator");
					System.setProperty("lastOpenedDirectory", lastopeneddir);
					pathvalue.setText(selecteddirectory);
				}
				catch (Exception e) {}
			}
		};


		String titleframe="";
		if ((!namepath.equals("")) && (!isnottodelete))
		{
			TitledBorder title = BorderFactory.createTitledBorder(Keywords.Language.getMessage(224));
			JLabel tempname=new JLabel(namepath);
			JPanel temppanel=new JPanel(new GridLayout(1,1));
			temppanel.add(tempname);
			panelpath.add(temppanel);
			panelpath.setBorder(title);
			titleframe=Keywords.Language.getMessage(219);
		}
		if ((!namepath.equals("")) && (isnottodelete))
		{
			TitledBorder title1 = BorderFactory.createTitledBorder(Keywords.Language.getMessage(225));
			JLabel tempname=new JLabel(namepath);
			JPanel temp1=new JPanel();
			temp1.add(tempname);
			temp1.setBorder(title1);
			panelpath.add(temp1);
			TitledBorder title2 = BorderFactory.createTitledBorder(Keywords.Language.getMessage(226));
			pathvalue=new JTextField(10);
			JPanel temp2=new JPanel(new GridLayout(2,1));
			JButton seldir=new JButton(selectlocaldirectory);
			temp2.add(seldir);
			temp2.add(pathvalue);
			temp2.setBorder(title2);
			panelpath.add(temp2);
			titleframe=Keywords.Language.getMessage(218)+" "+namepath;
		}
		if ((namepath.equals("")) && (isnottodelete))
		{
			TitledBorder title1 = BorderFactory.createTitledBorder(Keywords.Language.getMessage(228));
			pathname=new JTextField(10);
			JPanel temp1=new JPanel();
			temp1.add(pathname);
			temp1.setBorder(title1);
			panelpath.add(temp1);
			TitledBorder title2 = BorderFactory.createTitledBorder(Keywords.Language.getMessage(226));
			JPanel temp2=new JPanel(new GridLayout(2,1));
			JButton seldir=new JButton(selectlocaldirectory);
			temp2.add(seldir);
			pathvalue=new JTextField(10);
			temp2.add(pathvalue);
			temp2.setBorder(title2);
			panelpath.add(temp2);
			titleframe=Keywords.Language.getMessage(223);
		}
		if ((namepath.equals("")) && (!isnottodelete))
		{
			JLabel tempname=new JLabel(Keywords.Language.getMessage(222));
			JPanel temppanel=new JPanel(new GridLayout(1,1));
			temppanel.add(tempname);
			panelpath.add(temppanel);
			titleframe=Keywords.Language.getMessage(222);
		}

		framepath = new JInternalFrame(titleframe, resizable, closeable, maximizable, iconifiable);

		gridpanel=new JPanel(new GridLayout(4,1));

		Action executestep = new AbstractAction(Keywords.Language.getMessage(170))
		{
			private static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent evt)
			{
				String commandtoexecute="";
				if ((!namepath.equals("")) && (!isnottodelete))
				{
					commandtoexecute=Keywords.PATH+" "+namepath.trim()+"="+Keywords.clear+";";
				}
				if ((!namepath.equals("")) && (isnottodelete))
				{
					String tempvalue=pathvalue.getText();
					if (tempvalue.equals(""))
					{
						JOptionPane pane = new JOptionPane(Keywords.Language.getMessage(229));
						JDialog dialog = pane.createDialog(MainGUI.desktop, Keywords.Language.getMessage(134));
						dialog.validate();
						dialog.setVisible(true);
						return;
					}
					commandtoexecute=Keywords.PATH+" "+namepath.trim()+"="+tempvalue.trim()+";";
				}
				if ((namepath.equals("")) && (isnottodelete))
				{
					String tempname=pathname.getText();
					if (tempname.equals(""))
					{
						JOptionPane pane = new JOptionPane(Keywords.Language.getMessage(230));
						JDialog dialog = pane.createDialog(MainGUI.desktop, Keywords.Language.getMessage(134));
						dialog.validate();
						dialog.setVisible(true);
						return;
					}
					String tempvalue=pathvalue.getText();
					if (tempvalue.equals(""))
					{
						JOptionPane pane = new JOptionPane(Keywords.Language.getMessage(229));
						JDialog dialog = pane.createDialog(MainGUI.desktop, Keywords.Language.getMessage(134));
						dialog.validate();
						dialog.setVisible(true);
						return;
					}
					commandtoexecute=Keywords.PATH+" "+tempname.trim()+"="+tempvalue.trim()+";";
				}
				if ((namepath.equals("")) && (!isnottodelete))
					commandtoexecute=Keywords.PATH+" "+Keywords.clear+";";
				Keywords.general_percentage_total=0;
				Keywords.general_percentage_done=0;
				Keywords.numread=0.0;
				Keywords.operationReading=false;
				Keywords.numwrite=0.0;
				Keywords.operationWriting=false;
				Keywords.currentExecutedStep="Operations on PATH";
				new ExecuteRunner(2, commandtoexecute);
				Keywords.currentExecutedStep="Operations on PATH";
			}
		};
		Action putstep = new AbstractAction(Keywords.Language.getMessage(172))
		{
			private static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent evt)
			{
				String commandtoexecute="";
				if ((!namepath.equals("")) && (!isnottodelete))
				{
					commandtoexecute=Keywords.PATH+" "+namepath+"="+Keywords.clear+";";
				}
				if ((!namepath.equals("")) && (isnottodelete))
				{
					String tempvalue=pathvalue.getText();
					if (tempvalue.equals(""))
					{
						JOptionPane pane = new JOptionPane(Keywords.Language.getMessage(229));
						JDialog dialog = pane.createDialog(MainGUI.desktop, Keywords.Language.getMessage(134));
						dialog.validate();
						dialog.setVisible(true);
						return;
					}
					commandtoexecute=Keywords.PATH+" "+namepath+"="+tempvalue+";";
				}
				if ((namepath.equals("")) && (isnottodelete))
				{
					String tempname=pathname.getText();
					if (tempname.equals(""))
					{
						JOptionPane pane = new JOptionPane(Keywords.Language.getMessage(230));
						JDialog dialog = pane.createDialog(MainGUI.desktop, Keywords.Language.getMessage(134));
						dialog.validate();
						dialog.setVisible(true);
						return;
					}
					String tempvalue=pathvalue.getText();
					if (tempvalue.equals(""))
					{
						JOptionPane pane = new JOptionPane(Keywords.Language.getMessage(229));
						JDialog dialog = pane.createDialog(MainGUI.desktop, Keywords.Language.getMessage(134));
						dialog.validate();
						dialog.setVisible(true);
						return;
					}
					commandtoexecute=Keywords.PATH+" "+tempname+"="+tempvalue+";";
				}
				if ((namepath.equals("")) && (!isnottodelete))
					commandtoexecute=Keywords.PATH+" "+Keywords.clear+";";
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
				framepath.dispose();
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

		panelpath.add(secondgrid);

		framepath.addInternalFrameListener(new InternalFrameListener()
		{
			public void internalFrameClosing(InternalFrameEvent e)
			{
				Keywords.general_percentage_total=0;
				Keywords.general_percentage_done=0;
				Keywords.numread=0.0;
				Keywords.operationReading=false;
				Keywords.numwrite=0.0;
				Keywords.operationWriting=false;
				Keywords.currentExecutedStep="";
			}
			public void internalFrameClosed(InternalFrameEvent e)
			{
				Keywords.general_percentage_total=0;
				Keywords.general_percentage_done=0;
				Keywords.numread=0.0;
				Keywords.operationReading=false;
				Keywords.numwrite=0.0;
				Keywords.operationWriting=false;
				Keywords.currentExecutedStep="";
			}
			public void internalFrameOpened(InternalFrameEvent e) {}
			public void internalFrameIconified(InternalFrameEvent e) {}
			public void internalFrameDeiconified(InternalFrameEvent e) {}
			public void internalFrameActivated(InternalFrameEvent e) {}
			public void internalFrameDeactivated(InternalFrameEvent e){}
		});

		JScrollPane scrollpath= new JScrollPane(panelpath);
		scrollpath.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollpath.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		int numrow = new Double(MainGUI.frame.getHeight()).intValue() - 52;
		int numcol = new Double(MainGUI.frame.getWidth()).intValue() - 14;
		scrollpath.setSize(numcol, numrow);

		java.net.URL imageicon= PathGUI.class.getResource(Keywords.simpleicon);
		iconimage = new ImageIcon(imageicon);
		framepath.setSize(numcol, numrow);
		framepath.setFrameIcon(iconimage);
		framepath.getContentPane().add(scrollpath);
		framepath.repaint();
		framepath.setVisible(true);
		framepath.pack();
		MainGUI.desktop.add(framepath);
		MainGUI.desktop.repaint();
		framepath.moveToFront();
		try
		{
			framepath.setEnabled(true);
			framepath.toFront();
			framepath.show();
			framepath.setSelected(true);
		}
		catch (Exception e) {}
	}
}
