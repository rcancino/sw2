package com.luxsoft.sw3.ui.selectores;

import java.awt.BorderLayout;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.JFormattedTextField.AbstractFormatterFactory;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.swing.controls.SXAbstractDialog;

public class SelectorDeDescuento extends SXAbstractDialog{
	
	private SelectorDeDescuento() {
		super("Asignacion de descuento");
		
	}

	private JFormattedTextField inputField;

	@Override
	protected JComponent buildContent() {
		inputField=createInputField();
		JPanel panel=new JPanel(new BorderLayout());
		PanelBuilder builder=new PanelBuilder(new FormLayout("p,3dlu,60dlu:g","p"));
		CellConstraints cc=new CellConstraints();
		builder.addLabel("Descuento ", cc.xy(1,1));
		builder.add(inputField,cc.xy(3, 1));
		panel.add(builder.getPanel(),BorderLayout.CENTER);
		panel.add(buildButtonBarWithOKCancel(),BorderLayout.SOUTH);
		return panel;
	}
	
	protected JFormattedTextField createInputField(){
		JFormattedTextField tf=new JFormattedTextField(getPorcentageFormatterFactory());
		return tf;
	}
	
	public double getDescuento(){
		if(inputField.getValue()!=null){
			return (Double)inputField.getValue();
		}
		return 0;
			
	}
	
	
	/**
	 * FormatterFactory para porcentajes
	 * 
	 * @return
	 */
	public  AbstractFormatterFactory getPorcentageFormatterFactory(){
		
		NumberFormat nf=NumberFormat.getPercentInstance();		
		((DecimalFormat)nf).setMultiplier(1);
		nf.setMaximumFractionDigits(2);
		nf.setMaximumIntegerDigits(2);
		NumberFormatter nff=new NumberFormatter(nf);
		nff.setCommitsOnValidEdit(true);
		nff.setValueClass(Double.class);
		
		NumberFormat ef=NumberFormat.getInstance();
		ef.setMaximumFractionDigits(2);
		ef.setMaximumIntegerDigits(2);
		
		NumberFormatter defaultFormatter=new NumberFormatter(nf);
		NumberFormatter editFormatter=new NumberFormatter(ef);
		editFormatter.setValueClass(Double.class);
		editFormatter.setOverwriteMode(true);
		
		AbstractFormatterFactory factory=new DefaultFormatterFactory(defaultFormatter,defaultFormatter,editFormatter);
		return factory;
	}
	
	public static double seleccionarEnBase100(){
		SelectorDeDescuento selector=new SelectorDeDescuento();
		selector.open();
		if(!selector.hasBeenCanceled()){
			return selector.getDescuento();
		}
		return 0.0;
	}
	
	public static void main(String[] args) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				com.luxsoft.siipap.swing.utils.SWExtUIManager.setup();
				SelectorDeDescuento selector=new SelectorDeDescuento();
				selector.open();
				if(!selector.hasBeenCanceled()){
					System.out.println("Valor: "+selector.inputField.getValue()+" Tipo: "+selector.inputField.getValue().getClass().getName());
				}
				System.exit(0);
			}

		});
	}

}
