package com.luxsoft.sw3.cfd.ui.validaciones;

import java.awt.BorderLayout;
import java.awt.print.PrinterException;

import javax.swing.Action;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.apache.commons.lang.StringUtils;

import com.jgoodies.binding.beans.PropertyConnector;
import com.jgoodies.uif.component.UIFButton;
import com.luxsoft.siipap.swing.utils.CommandUtils;
import com.luxsoft.siipap.swing.utils.MessageUtils;

public class ValidacionResultPanel extends JPanel {
	
	private final CFDValidacionController controller;
	
	public ValidacionResultPanel(CFDValidacionController controller){
		this.controller=controller;
		init();
	}
	
	JEditorPane editor;
	
	private void init(){
		setLayout(new BorderLayout());
		editor=new JEditorPane();
		editor.setEditable(false);
		editor.setContentType("text/html");
		editor.setText("");
		add(new JScrollPane(editor),BorderLayout.CENTER);
		PropertyConnector.connect(editor, "text"
				, this.controller, CFDValidacionController.VALIDACION_HTML_RESULT_PROPERTY);
	}

	public void print(){
		try {
			if(StringUtils.isNotBlank(editor.getText()))
				editor.print();
		} catch (PrinterException e) {
			e.printStackTrace();
			MessageUtils.showError("Error imprimiendo",e);
		}
	}
	
	private Action printAction;
	
	public Action getPrintAction(){
		if(printAction==null){
			printAction=CommandUtils.createPrintAction(this, "print");
		}
		return printAction;
	}

}
