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

import javax.swing.ImageIcon;
import javax.swing.JInternalFrame;
import javax.swing.JMenuBar;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

import ADaMSoft.keywords.Keywords;

/**
* This is the GUI to view a graph
* @author marco.scarno@gmail.com
* @date 05/09/2015
*/
public class GraphViewer
{
	JInternalFrame viewTable;
	JMenuBar menuDataViewer;
	/**
	* This is the method that visualize a graph
	*/
	public GraphViewer(JFreeChart chart)
    {
		boolean resizable = true;
		boolean closeable = true;
		boolean maximizable  = true;
		boolean iconifiable = true;
		viewTable = new JInternalFrame(chart.getTitle().getText(), resizable, closeable, maximizable, iconifiable);
		viewTable.addInternalFrameListener(new InternalFrameListener()
		{
			public void internalFrameClosing(InternalFrameEvent e)
			{
				System.gc();
			}
			public void internalFrameClosed(InternalFrameEvent e) {}
			public void internalFrameOpened(InternalFrameEvent e) {}
			public void internalFrameIconified(InternalFrameEvent e) {}
			public void internalFrameDeiconified(InternalFrameEvent e) {}
			public void internalFrameActivated(InternalFrameEvent e) {}
			public void internalFrameDeactivated(InternalFrameEvent e){}
		});

		java.net.URL    url   = GraphViewer.class.getResource(Keywords.simpleicon);
		ImageIcon iconSet = new ImageIcon(url);
		ChartPanel chartPanel = new ChartPanel(chart, false);

		int numrow = new Double(MainGUI.frame.getHeight()).intValue() - 52;
		int numcol = new Double(MainGUI.frame.getWidth()).intValue() - 14;

		viewTable.setSize(numcol, numrow);
		viewTable.setFrameIcon(iconSet);
		viewTable.setContentPane(chartPanel);

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
}
