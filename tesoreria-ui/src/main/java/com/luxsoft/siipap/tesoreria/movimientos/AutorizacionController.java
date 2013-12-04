package com.luxsoft.siipap.tesoreria.movimientos;

import java.awt.BorderLayout;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.commons.lang.StringUtils;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.uifextras.panel.HeaderPanel;
import com.luxsoft.siipap.model.Autorizacion;
import com.luxsoft.siipap.service.AutorizacionManager;
import com.luxsoft.siipap.swing.controls.SXAbstractDialog;

public class AutorizacionController {
	
	
	public static Autorizacion autorizar(String comentario){
		Autorizacion a=AutorizacionManager.getInstance().generarAutorizacion();
		a.setComentario(comentario);
		return autorizar(a);
	}
	
	
	public static Autorizacion autorizar(final Autorizacion a){
		final AutorizacionForm form=new AutorizacionForm(a);
		form.open();
		if(!form.hasBeenCanceled()){
			return a;
		}
		return null;
	}
	
	
	public static class AutorizacionForm extends SXAbstractDialog{
		
		private JTextField usuarioField;
		private JTextField comentarioField;
		private final Autorizacion aut;

		public AutorizacionForm(final Autorizacion aut) {
			super("Autorización");
			this.aut=aut;
			
		}
		
		private void initComponents(){
			usuarioField=new JTextField(aut.getAutorizo().getUsername());
			usuarioField.setEditable(false);
			comentarioField=new JTextField(20);
		}
		
		

		/* (non-Javadoc)
		 * @see com.luxsoft.siipap.swing.controls.SXAbstractDialog#onWindowOpened()
		 */
		@Override
		protected void onWindowOpened() {
			comentarioField.setText(aut.getComentario());
			comentarioField.selectAll();
			comentarioField.requestFocusInWindow();
		}		
		
		protected JComponent buildContent() {
			JPanel p=new JPanel(new BorderLayout());
			p.add(buildForm(),BorderLayout.CENTER);
			p.add(buildButtonBarWithOKCancel(),BorderLayout.SOUTH);
			return p;
		}

		protected JComponent buildForm() {
			initComponents();
			final FormLayout layout=new FormLayout("p,2dlu,f:p:g","");
			final DefaultFormBuilder builder=new DefaultFormBuilder(layout);
			builder.append("Usuario",usuarioField);
			builder.append("Comentario",comentarioField);
			return builder.getPanel();
		}
		
		
		@Override
		protected JComponent buildHeader() {
			return new HeaderPanel("Sistema de seguridad","Autorización dinamica para pago de requisiciones");
		}

		public void doApply(){
			super.doApply();
			String s=comentarioField.getText();
			aut.setComentario(StringUtils.substring(s, 0, 80));
		}
		
	}
	 
	
}
