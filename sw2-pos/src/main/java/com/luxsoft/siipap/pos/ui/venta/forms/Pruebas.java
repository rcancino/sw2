package com.luxsoft.siipap.pos.ui.venta.forms;

import java.awt.Dialog;
import java.awt.event.ActionListener;
import java.beans.EventHandler;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;

import org.apache.commons.lang.time.DateUtils;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Binding;
import org.jdesktop.beansbinding.Bindings;
import org.jdesktop.beansbinding.Property;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.swing.controls.SXAbstractDialog;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.sw3.ventas.Pedido;

public class Pruebas extends SXAbstractDialog{
	
	private JFormattedTextField fechaField;
	private Pedido pedido;
	
	public Pruebas(Pedido pedido) {
		super("Prueba de BetterBeansBinding");
		this.pedido=pedido;
	}
	
	private JButton cambiarFecha;
	
	private void initComponents(){
		fechaField=new JFormattedTextField(new SimpleDateFormat("dd/MM/yyyy"));
		cambiarFecha=new JButton("...");
		cambiarFecha.addActionListener(EventHandler.create(ActionListener.class, this, "cambiar"));
	}
	
	public void cambiar(){
		Date fecha=pedido.getFecha();
		pedido.setFecha(DateUtils.addDays(fecha, 1));
	}
	
	private void bindComponents(){
		Property fechaProperty=BeanProperty.create("fecha");
		Property valueProperty=BeanProperty.create("value");
		Binding binding=Bindings.createAutoBinding(UpdateStrategy.READ, pedido, fechaProperty,fechaField,valueProperty );
		binding.bind();
	}
	
	@Override
	protected JComponent buildContent() {
		initComponents();
		bindComponents();
		FormLayout layout=new FormLayout("p,3dlu,p:g,2dlu,p","");
		final DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		builder.append("Fecha",this.fechaField,cambiarFecha);
		return builder.getPanel();
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
				//Pruebas form=new Pruebas(new Pedido());
				//form.open();
				double val=9.00001;
				System.out.println(Math.ceil(val));
				System.exit(0);
			}

		});
	}

	

}
