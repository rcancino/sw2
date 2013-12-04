package com.luxsoft.siipap.swing.controls;

import java.net.URL;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

public class BooleanCellRenderer extends DefaultTableCellRenderer {

	private static Icon yesIcon;

	private static Icon noIcon;

	public BooleanCellRenderer() {
		getYesIcon();
		getNoIcon();
		setHorizontalAlignment(SwingConstants.CENTER);
	}

	@Override
	protected void setValue(Object value) {
		if (value instanceof Boolean) {

			Boolean v = (Boolean) value;
			setIcon(getYesIcon());
			if (v) {
			} else
				setIcon(getNoIcon());
		} else
			super.setValue(value);
	}

	private Icon getYesIcon() {
		if (yesIcon == null) {
			URL url = Thread.currentThread().getContextClassLoader()
					.getResource("images2/tick.png");
			yesIcon = new ImageIcon(url);
		}
		return yesIcon;
	}

	public Icon getNoIcon() {
		if (noIcon == null) {
			URL url = Thread.currentThread().getContextClassLoader()
					.getResource("images2/stop.png");
			noIcon = new ImageIcon(url);
		}
		return noIcon;
	}

}
