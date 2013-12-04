package com.luxsoft.siipap.swing.controls;

import java.awt.EventQueue;
import java.awt.event.FocusEvent;
import java.awt.event.MouseEvent;

import javax.swing.JFormattedTextField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.plaf.UIResource;
import javax.swing.text.DefaultCaret;
import javax.swing.text.JTextComponent;

public class PlasticFieldCaret extends DefaultCaret implements UIResource {

	public PlasticFieldCaret() {
		super();
	}

	private boolean isKeyboardFocusEvent = true;

	public void focusGained(FocusEvent e) {
		if (getComponent().isEnabled()) {
			setVisible(true);
			setSelectionVisible(true);
		}

		final JTextComponent c = getComponent();
		if (c.isEnabled() && isKeyboardFocusEvent) {
			if ( (c instanceof JFormattedTextField) || (c instanceof JTextField)) {
				EventQueue.invokeLater(new Runnable() {
					public void run() {
						PlasticFieldCaret.super.setDot(0);
						PlasticFieldCaret.super.moveDot(c.getDocument()
								.getLength());
					}
				});
			} else {
				super.setDot(0);
				super.moveDot(c.getDocument().getLength());
			}
		}
	}

	public void focusLost(FocusEvent e) {
		super.focusLost(e);
		if (!e.isTemporary()) {
			isKeyboardFocusEvent = true;
		}
	}

	public void mousePressed(MouseEvent e) {
		if (SwingUtilities.isLeftMouseButton(e) || e.isPopupTrigger()) {
			isKeyboardFocusEvent = false;
		}
		super.mousePressed(e);

	}

	public void mouseReleased(MouseEvent e) {
		super.mouseReleased(e);
		if (e.isPopupTrigger()) {
			isKeyboardFocusEvent = false;
			if ((getComponent() != null) && getComponent().isEnabled()
					&& getComponent().isRequestFocusEnabled()) {
				getComponent().requestFocus();
			}
		}
	}

}
