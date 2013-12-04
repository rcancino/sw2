package com.luxsoft.siipap.swing.controls;

import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

public class UpperCaseField extends JTextField {

	public UpperCaseField() {
		super();
		// TODO Auto-generated constructor stub
	}

	public UpperCaseField(String text) {
		super(text);
		// TODO Auto-generated constructor stub
	}

	public UpperCaseField(int columns) {
		super(columns);
		// TODO Auto-generated constructor stub
	}

	public UpperCaseField(String text, int columns) {
		super(text, columns);
		// TODO Auto-generated constructor stub
	}

	public UpperCaseField(Document doc, String text, int columns) {
		super(doc, text, columns);
		// TODO Auto-generated constructor stub
	}
	
	

    protected Document createDefaultModel() {
        return new UpperCaseDocument();
    }
	
	static class UpperCaseDocument extends PlainDocument {
		 
        public void insertString(int offs, String str, AttributeSet a) 
            throws BadLocationException {

            if (str == null) {
                return;
            }
            char[] upper = str.toCharArray();
            for (int i = 0; i < upper.length; i++) {
                upper[i] = Character.toUpperCase(upper[i]);
            }
            super.insertString(offs, new String(upper), a);
        }
    }

}
