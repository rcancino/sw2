package com.luxsoft.siipap.pos.ui.commons;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.StringWriter;
import java.util.Properties;

import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.model.core.Producto;
import com.luxsoft.siipap.swing.controls.SXAbstractDialog;
import com.luxsoft.sw3.common.VelocityUtils;

/**
 * Panel para mostrar resultados de scripts procesados con Velocity
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class VelocityHTLPanel extends JPanel{
	
	protected JEditorPane editor;
	
	protected Logger logger=Logger.getLogger(getClass());
	
	private String templatePath;
	
	public VelocityHTLPanel(){
		setLayout(new BorderLayout());
		editor=new JEditorPane();
		editor.setContentType("text/html");
		editor.setEditable(false);
		JScrollPane sp=new JScrollPane(editor);
		editor.setComponentPopupMenu(buildPopupMenu());
		add(sp,BorderLayout.CENTER);
		
	}
	
	private JPopupMenu buildPopupMenu(){
		JPopupMenu pm=new JPopupMenu();
		pm.add(new JMenuItem("Actualizar")).addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				processTemplate();
			}			
		});
		return pm;
	}
	
	protected String processTemplate(){
		/*VelocityContext context=new VelocityContext();
		Cliente c= new Cliente("P000001","Cliente de preuba");
		context.put("cliente", c);
		String res=VelocityUtils.processResource(getTemplatePath(), context);
		*/
		VelocityContext context=new VelocityContext();
		context.put("producto", new Producto("POL74","SUPPOLART 2C BTE 61X90 130GR"));
		return VelocityUtils.processResource(getTemplatePath(), context);
	}
	
	public void updateEditor(){
		editor.setText(processTemplate());
	}
	
	
	public String getTemplatePath() {
		return templatePath;
	}

	public void setTemplatePath(String templatePath) {
		this.templatePath = templatePath;
	}

	/**
	 * Prueba local en el EDT
	 * 
	 * @param args
	 * 
	 */
	public static void main(String[] args) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				com.luxsoft.siipap.swing.utils.SWExtUIManager.setup();
				SXAbstractDialog dialog =new SXAbstractDialog("TEST VELOCITY VIEW HTML"){
					@Override
					protected JComponent buildContent() {
						VelocityHTLPanel panel=new VelocityHTLPanel();
						panel.setTemplatePath("templates/ProductoHeader.vm");
						//panel.setTemplatePath("pruebas/velocity/ProductoHeader.vm");
//																	  ProductoHeader.vm
						panel.updateEditor();
						panel.setPreferredSize(new Dimension(500,400));
						return panel;
					}
					
				};
				dialog.open();
				System.exit(0);
			}

		});
	}

}
