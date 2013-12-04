package com.luxsoft.siipap.swing.utils;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import org.jdesktop.swingx.JXTable;

/**
 * Proyecto para impresion de JTable, otra opcion podria ser Velocity o Freemarker
 * 
 * @author ruben
 *
 */
public class JTablePrintPreview extends JFrame implements Printable {

	protected int m_maxNumPage = 1;
	protected JLabel m_title;
	protected JXTable m_table;

	public int print(Graphics pg, PageFormat pageFormat, int pageIndex)
			throws PrinterException {
		if (pageIndex >= m_maxNumPage)
			return NO_SUCH_PAGE;

		pg.translate((int) pageFormat.getImageableX(), (int) pageFormat
				.getImageableY());

		int wPage = 0;
		int hPage = 0;

		if (pageFormat.getOrientation() == PageFormat.PORTRAIT) {

			wPage = (int) pageFormat.getImageableWidth();
			hPage = (int) pageFormat.getImageableHeight();

		} else {

			wPage = (int) pageFormat.getImageableWidth();

			wPage += wPage / 2;

			hPage = (int) pageFormat.getImageableHeight();

			pg.setClip(0, 0, wPage, hPage);

		}

		int y = 0;

		pg.setFont(m_title.getFont());

		pg.setColor(Color.black);
		Font fn = pg.getFont();
		FontMetrics fm = pg.getFontMetrics();
		y += fm.getAscent();
		pg.drawString(m_title.getText(), 0, y);

		y += 20; // space between title and table headers

		Font headerFont = m_table.getFont().deriveFont(Font.BOLD);

		pg.setFont(headerFont);

		fm = pg.getFontMetrics();

		TableColumnModel colModel = m_table.getColumnModel();

		int nColumns = colModel.getColumnCount();

		int x[] = new int[nColumns];

		x[0] = 0;

		int h = fm.getAscent();

		y += h; // add ascent of header font because of baseline

		// positioning (see figure 2.10)

		int nRow, nCol;

		for (nCol = 0; nCol < nColumns; nCol++) {

			TableColumn tk = colModel.getColumn(nCol);

			int width = tk.getWidth();

			if (x[nCol] + width > wPage) {

				nColumns = nCol;

				break;

			}

			if (nCol + 1 < nColumns)

				x[nCol + 1] = x[nCol] + width;

			String title = (String) tk.getIdentifier();

			pg.drawString(title, x[nCol], y);

		}

		pg.setFont(m_table.getFont());

		fm = pg.getFontMetrics();

		int header = y;

		h = fm.getHeight();

		int rowH = Math.max((int) (h * 1.5), 10);

		int rowPerPage = (hPage - header) / rowH;

		m_maxNumPage = Math.max((int) Math.ceil(m_table.getRowCount() /

		(double) rowPerPage), 1);

		TableModel tblModel = m_table.getModel();

		int iniRow = pageIndex * rowPerPage;

		int endRow = Math.min(m_table.getRowCount(),

		iniRow + rowPerPage);

		for (nRow = iniRow; nRow < endRow; nRow++) {

			y += h;

			for (nCol = 0; nCol < nColumns; nCol++) {
				int col = m_table.getColumnModel().getColumn(nCol)
						.getModelIndex();
				Object obj = tblModel.getValueAt(nRow, col);
				String str = obj.toString();
				pg.setColor(Color.black);
				pg.drawString(str, x[nCol], y);

			}

		}

		System.gc();

		return PAGE_EXISTS;

	}

}
