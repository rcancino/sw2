package com.luxsoft.sw3.tesoreria.ui.forms;

import java.awt.BorderLayout;
import java.text.MessageFormat;
import java.util.Date;
import java.util.List;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.binding.list.SelectionInList;
import com.jgoodies.binding.value.ValueHolder;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.cxc.model.Cargo;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.controls.SXAbstractDialog;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.siipap.ventas.model.AsignacionVentaCE;
import com.luxsoft.siipap.ventas.model.Venta;

public class RegistroIngresoEfectivoForm extends SXAbstractDialog{


	private static ValueHolder fechaHolder=new ValueHolder(null);
	private static ValueHolder sucursalHolder=new ValueHolder(null);

	
	public RegistroIngresoEfectivoForm() {
		super("Ingreso Efectivo");
	}	

		@Override
	protected JComponent buildContent() {
		JPanel content=new JPanel(new BorderLayout());
		content.add(buildForm(),BorderLayout.CENTER);
		content.add(buildButtonBarWithOKCancel(),BorderLayout.SOUTH);
		return content;
	}
		
	private JComponent buildForm(){
			
		FormLayout layout=new  FormLayout("p,3dlu,150dlu:g");			
		DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		
		builder.append("Fecha",getControl("fecha"));
		builder.append("Sucursal",getControl("sucursal"));
	
		return builder.getPanel();
	}
		
	private JComponent getControl(String property){
		 if("fecha".equals(property)){
			return Binder.createDateComponent(fechaHolder);
		}else if("sucursal".equals(property)){
			 List<Sucursal> sucursales=ServiceLocator2.getLookupManager().getSucursales();
			 sucursales.add(null);
			 SelectionInList sl=new SelectionInList(sucursales,buffer(sucursalHolder));
			 return BasicComponentFactory.createComboBox(sl,new SucursalRenderer());
		}else 
			return null;
	}
		
	
		
		
	private class SucursalRenderer extends DefaultListCellRenderer{
			
		public void setText(String t){
			if(t.isEmpty())
				super.setText("");
			else
				super.setText(t);
		}
	}
	
	public static void registrar(){
		RegistroIngresoEfectivoForm app=new RegistroIngresoEfectivoForm();
		app.open();
		if(!app.hasBeenCanceled()){
			Sucursal sucursal = (Sucursal) sucursalHolder.getValue();
			Date fecha = (Date)fechaHolder.getValue();
			ServiceLocator2.getIngresosManager().registrarIngresoPorFichaEfectivo(fecha,sucursal);
		}
	}
	
	public static void main(String[] args) {
		registrar();
	}

}
