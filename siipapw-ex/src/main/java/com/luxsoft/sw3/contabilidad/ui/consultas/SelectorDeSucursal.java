package com.luxsoft.sw3.contabilidad.ui.consultas;

import java.awt.BorderLayout;
import java.util.Date;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.jdesktop.swingx.JXDatePicker;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.controls.SXAbstractDialog;
import com.luxsoft.siipap.swing.dialog.SelectorDeFecha;

public class SelectorDeSucursal extends SXAbstractDialog{
	
	private String defaultSucursal="%";
	
	public SelectorDeSucursal() {
		super("Seleccion de Sucursal");
	}
	
	
	
	private JComboBox sucursalControl;

	@Override
	protected JComponent buildContent() {
		JPanel panel=new JPanel(new BorderLayout());
		
		sucursalControl=sucursalControl=createSucursalControl();;
		
		FormLayout layout=new FormLayout("p,2dlu,60dlu","");
		DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		builder.append("Sucursal",sucursalControl);
		panel.add(builder.getPanel(),BorderLayout.CENTER);
		panel.add(buildButtonBarWithOKCancel(),BorderLayout.SOUTH);
		return panel;
	
	
	}
	
	private JComboBox createSucursalControl() {			
		final JComboBox box = new JComboBox(ServiceLocator2.getLookupManager().getSucursalesOperativas().toArray());
		Sucursal local=ServiceLocator2.getConfiguracion().getSucursal();
		for(int index=0;index<box.getModel().getSize();index++){
			Sucursal s=(Sucursal)box.getModel().getElementAt(index);
			if(s.equals(local)){
				box.setSelectedIndex(index);
				break;
			}
		}
		return box;
	}
	
	private String getSucursal(){
		Sucursal selected=(Sucursal)sucursalControl.getSelectedItem();
		return selected.getNombre();
	}
	public static String seleccionar(){
		SelectorDeSucursal selector=new SelectorDeSucursal();
		selector.open();
		if(!selector.hasBeenCanceled()){
			return selector.getSucursal();
		}
		return null;
	}
	
	public static void main(String[] args) {
		SelectorDeSucursal selector=new SelectorDeSucursal();
		selector.seleccionar();
		
	}
	
	

}
