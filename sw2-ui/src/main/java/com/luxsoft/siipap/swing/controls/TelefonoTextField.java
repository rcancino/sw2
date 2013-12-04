package com.luxsoft.siipap.swing.controls;

import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

public class TelefonoTextField extends JTextField {

	public TelefonoTextField() {
		super();
		// TODO Auto-generated constructor stub
	}

	public TelefonoTextField(String text) {
		super(text);
		// TODO Auto-generated constructor stub
	}

	public TelefonoTextField(int columns) {
		super(columns);
		// TODO Auto-generated constructor stub
	}

	public TelefonoTextField(String text, int columns) {
		super(text, columns);
		// TODO Auto-generated constructor stub
	}

	public TelefonoTextField(Document doc, String text, int columns) {
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
            if (str == null) {
                return;
            }
            char[] input = str.toCharArray();
            StringBuffer digits=new StringBuffer();
            for (int i = 0; i < input.length; i++) {
            	char current=input[i];
            	if(Character.isDigit(current) || '-'==current)
            		digits.append(current);
                //input[i] = Character.toUpperCase(input[i]);
            }
            super.insertString(offs, digits.toString(), a);
            
        }
    }

}
