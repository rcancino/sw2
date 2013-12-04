/**
 * 
 */
package com.luxsoft.siipap.swing.matchers;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JTextField;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

import ca.odell.glazedlists.matchers.AbstractMatcherEditor;
import ca.odell.glazedlists.matchers.Matcher;



public abstract class RangoMatcherEditor<T> extends AbstractMatcherEditor implements ActionListener{
	
	private JTextField field;
	private boolean mayorA=false;
	
	public RangoMatcherEditor(){
		field=new JTextField(7);
		field.addActionListener(this);
	}

	public void actionPerformed(ActionEvent e) {
		String text=field.getText();
		if(StringUtils.isNotBlank(text)){
			if(NumberUtils.isNumber(text)){
				fireChanged(new MayorMatcher());
			}
		}
		else
			fireMatchAll();
	}
	
	public JTextField getField() {
		return field;
	}


	public class MayorMatcher implements Matcher<T>{
		public boolean matches(T item) {
			return evaluar(item);
		}
	}
	
	public  abstract boolean evaluar(T item);

	public boolean isMayorA() {
		return mayorA;
	}

	public void setMayorA(boolean mayorA) {
		this.mayorA = mayorA;
	}
	
	public double getDoubleValue(){
		return NumberUtils.toDouble(field.getText());
	}
	
	
}