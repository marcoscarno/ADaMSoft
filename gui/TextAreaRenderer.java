/**
* Copyright © 2006-2010 CASPUR
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
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.*;
import java.util.Map.Entry;
/**
* This is the render of the class that creates a table with cells that can be wrapped
* @author marco.scarno@caspur.it
* @version 1.0.0, rev.: 5/11/10 by marco
*/
public class TextAreaRenderer extends JTextArea implements TableCellRenderer
{
	private final DefaultTableCellRenderer adaptee = new DefaultTableCellRenderer();
	private final HashMap<JTable,HashMap<Integer, HashMap<Integer,Integer>>> cellSizes = new HashMap<JTable,HashMap<Integer, HashMap<Integer,Integer>>>();
	private static final long serialVersionUID = 1L;
	Color bkgndColor, fgndColor;
	public TextAreaRenderer()
	{
		setLineWrap(true);
		setWrapStyleWord(true);
	}
	public void setBkgnd(Color tc)
	{
		bkgndColor = tc;
	}
	public void setForegnd(Color tc)
	{
		fgndColor = tc;
	}
	public Component getTableCellRendererComponent(JTable table, Object obj, boolean isSelected, boolean hasFocus, int row, int column)
	{
		adaptee.getTableCellRendererComponent(table, obj, isSelected, hasFocus, row, column);
		if (bkgndColor!=null)
			setBackground( bkgndColor );
		else
			setBackground(adaptee.getBackground());
		if (fgndColor!=null)
			setForeground( fgndColor );
		else
			setForeground(adaptee.getForeground());
		setBorder(adaptee.getBorder());
		setFont(adaptee.getFont());
		setText(adaptee.getText());
		TableColumnModel columnModel = table.getColumnModel();
		setSize(columnModel.getColumn(column).getWidth(), 100000);
		int height_wanted = (int) getPreferredSize().getHeight();
		addSize(table, row, column, height_wanted);
		height_wanted = findTotalMaximumRowSize(table, row);
		if (height_wanted != table.getRowHeight(row))
		{
			table.setRowHeight(row, height_wanted);
		}
		return this;
	}
	private void addSize(JTable table, int row, int column, int height)
	{
		HashMap<Integer, HashMap<Integer,Integer>> rows=cellSizes.get(table);
		if (rows == null)
		{
			cellSizes.put(table, rows = new HashMap<Integer, HashMap<Integer,Integer>>());
		}
		HashMap<Integer,Integer> rowheights = rows.get(new Integer(row));
		if (rowheights == null)
		{
			rows.put(new Integer(row), rowheights = new HashMap<Integer,Integer>());
		}
		rowheights.put(new Integer(column), new Integer(height));
	}
	private int findTotalMaximumRowSize(JTable table, int row)
	{
		int maximum_height = 0;
		Enumeration<TableColumn> columns = table.getColumnModel().getColumns();
		while (columns.hasMoreElements())
		{
			TableColumn tc = columns.nextElement();
			TableCellRenderer cellRenderer = tc.getCellRenderer();
			if (cellRenderer instanceof TextAreaRenderer)
			{
				TextAreaRenderer tar = (TextAreaRenderer) cellRenderer;
				maximum_height = Math.max(maximum_height,
				tar.findMaximumRowSize(table, row));
			}
		}
		return maximum_height;
	}
	private int findMaximumRowSize(JTable table, int row)
	{
		HashMap<Integer, HashMap<Integer,Integer>> rows=cellSizes.get(table);
		if (rows == null) return 0;
		HashMap<Integer,Integer> rowheights = rows.get(new Integer(row));
		if (rowheights == null) return 0;
		int maximum_height = 0;
		for (Iterator<Entry<Integer, Integer>> it = rowheights.entrySet().iterator(); it.hasNext();)
		{
			Entry<Integer, Integer> entry = it.next();
			int cellHeight = ((Integer) entry.getValue()).intValue();
			maximum_height = Math.max(maximum_height, cellHeight);
		}
		return maximum_height;
	}
}