/**
* Copyright (c) 2018 MS
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

import javax.swing.ImageIcon;
import javax.swing.JInternalFrame;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JFileChooser;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;

import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

import ADaMSoft.keywords.Keywords;

/**
* This is the GUI to view an image
* @author marco.scarno@gmail.com
* @date 22/02/2018
*/
public class ImageViewer implements ActionListener
{
	JInternalFrame viewTable;
	JMenuBar menuDataViewer;
	JMenuItem save_img;
	public final String def_chart_path;
	String title;
	JLabel wordCloudLabel;
	/**
	* This is the method that visualize a graph
	*/
	public ImageViewer(String chart_path, String title)
    {
		this.def_chart_path=chart_path;
		this.title=title;
		boolean resizable = true;
		boolean closeable = true;
		boolean maximizable  = true;
		boolean iconifiable = true;
		viewTable = new JInternalFrame(title, resizable, closeable, maximizable, iconifiable);
		viewTable.addInternalFrameListener(new InternalFrameListener()
		{
			public void internalFrameClosing(InternalFrameEvent e)
			{
				(new File(def_chart_path)).delete();
				System.gc();
			}
			public void internalFrameClosed(InternalFrameEvent e) {}
			public void internalFrameOpened(InternalFrameEvent e) {}
			public void internalFrameIconified(InternalFrameEvent e) {}
			public void internalFrameDeiconified(InternalFrameEvent e) {}
			public void internalFrameActivated(InternalFrameEvent e) {}
			public void internalFrameDeactivated(InternalFrameEvent e){}
		});

		menuDataViewer = new JMenuBar();
		JMenu menu = new JMenu("File");
		save_img = new JMenuItem("Save image");
		save_img.addActionListener(this);
		menu.add(save_img);
		menuDataViewer.add(menu);

		viewTable.setJMenuBar(menuDataViewer);

		JPanel panel = new JPanel();
		wordCloudLabel = new JLabel(new ImageIcon(def_chart_path));
		panel.add(wordCloudLabel);

		java.net.URL    url   = GraphViewer.class.getResource(Keywords.simpleicon);
		ImageIcon iconSet = new ImageIcon(url);

		int numrow = new Double(MainGUI.frame.getHeight()).intValue() - 52;
		int numcol = new Double(MainGUI.frame.getWidth()).intValue() - 14;

		JScrollPane scroller = new JScrollPane(panel);
		scroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

 		viewTable.setSize(numcol, numrow);
		viewTable.setFrameIcon(iconSet);
		viewTable.setContentPane(scroller);

		try
		{
			viewTable.setSelected(true);
		}
		catch (Exception e1) {}
		viewTable.repaint();
		viewTable.setVisible(true);
		viewTable.pack();
		MainGUI.desktop.add(viewTable);
		MainGUI.desktop.repaint();
		try
		{
			viewTable.moveToFront();
			viewTable.setEnabled(true);
			viewTable.toFront();
			viewTable.show();
			viewTable.setSelected(true);
		}
		catch (Exception e) {}
	}
	public void actionPerformed(ActionEvent Event)
	{
		Object Source = Event.getSource();
		if (Source==save_img)
		{
			JFileChooser fileChooser = new JFileChooser();
			if (fileChooser.showSaveDialog(MainGUI.desktop) == JFileChooser.APPROVE_OPTION)
			{
				try
				{
					File file_excel = fileChooser.getSelectedFile();
					if (file_excel!=null)
					{
						String path=file_excel.toString();
						if (!path.toLowerCase().endsWith(".png")) path=path+".png";
						try
						{
							ByteArrayOutputStream baos = new ByteArrayOutputStream();
							FileInputStream fin = new FileInputStream(new File(def_chart_path));
							byte buffer[] = new byte[4096];
							int read = 0;
							do
							{
								read = fin.read(buffer);
								if(read != -1)
								{
									baos.write(buffer, 0, read);
								}
							} while(read != -1);
							fin.close();
							FileOutputStream out = new FileOutputStream(path);
							baos.writeTo(out);
							out.close();
						}
						catch (Exception e){}
					}
				}
				catch (Exception ee){}
			}
		}
	}
}
