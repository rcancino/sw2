package com.luxsoft.siipap.cxc.ui.command;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComponent;
import javax.swing.JPanel;

import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.binding.list.SelectionInList;
import com.jgoodies.binding.value.ValueHolder;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.cxc.model.Cargo;
import com.luxsoft.siipap.cxc.model.NotaDeCargo;
import com.luxsoft.siipap.cxc.ui.consultas.CargoView;
import com.luxsoft.siipap.cxc.ui.consultas.FacturaForm;
import com.luxsoft.siipap.cxc.ui.selectores.SelectorDeCargos;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.controls.SXAbstractDialog;
import com.luxsoft.siipap.ventas.model.Venta;

public class BuscadorDeCargos extends AbstractAction{

	public void actionPerformed(ActionEvent e) {
		final BuscarCargoForm form=new BuscarCargoForm();
		form.open();
		if(!form.hasBeenCanceled()){
			final Cargo res=form.commit();			
			List<String> props=new ArrayList<String>();
			
			if((res.getDocumento()!=null)&& (res.getDocumento().intValue()>0))
				props.add("documento");
			if((res.getNumeroFiscal()!=null)&& (res.getNumeroFiscal().intValue()>0))
				props.add("numeroFiscal");
			if(res.getSucursal()!=null)
				props.add("sucursal");
			Cargo found=null;
			if(res.getDocumento()!=null && res.getSucursal()!=null){
				 found=SelectorDeCargos.buscar(res.getDocumento(), res.getSucursal().getId(), res.getNumeroFiscal());
			}else{
				 found=SelectorDeCargos.buscar(res, props.toArray(new String[1]));
				//Cargo found=SelectorDeCargos.buscar(res.getSucursal().getId(),res.getDocumento(),res.getNumeroFiscal());
			}
			
			
			if(found instanceof Venta){
				FacturaForm.show(found.getId());
			}else if(found instanceof NotaDeCargo){
				CargoView.show(found.getId());
			}else
				return;
				
		}
	}
	
	public static class BuscarCargoForm extends SXAbstractDialog{
		
		private ValueHolder sucursalHolder=new ValueHolder(null);
		private ValueHolder numeroHolder=new ValueHolder(null);
		private ValueHolder fiscalHolder=new ValueHolder(null);
		

		public BuscarCargoForm() {
			super("Cargo/Factura");
		}

		@Override
		protected JComponent buildContent() {
			JPanel content=new JPanel(new BorderLayout());
			content.add(buildForm(),BorderLayout.CENTER);
			content.add(buildButtonBarWithOKCancel(),BorderLayout.SOUTH);
			return content;
		}
		
		private JComponent buildForm(){
			
			FormLayout layout=new  FormLayout("p,3dlu,70dlu");			
			DefaultFormBuilder builder=new DefaultFormBuilder(layout);
			builder.append("Número",getControl("numero"));
			builder.append("Fiscal",getControl("numeroFiscal"));
			builder.append("Sucursal",getControl("sucursal"));
			
			return builder.getPanel();
		}
		
		private JComponent getControl(String property){
			if("numero".equals(property)){
				return BasicComponentFactory.createLongField(buffer(numeroHolder),0);
			}else if("numeroFiscal".equals(property)){
				return BasicComponentFactory.createIntegerField(buffer(fiscalHolder),0);
			}else if("sucursal".equals(property)){
				 List<Sucursal> sucursales=ServiceLocator2.getLookupManager().getSucursales();
				 sucursales.add(null);
				 SelectionInList sl=new SelectionInList(sucursales,buffer(sucursalHolder));
				 return BasicComponentFactory.createComboBox(sl,new SucursalRenderer());
			}else
				return null;
		}
		
		public Cargo commit(){
			Venta v=new Venta();
			v.setDocumento((Long)numeroHolder.getValue());
			v.setSucursal((Sucursal)sucursalHolder.getValue());
			v.setNumeroFiscal((Integer)fiscalHolder.getValue());
			return v;
		}
		
		
		private class SucursalRenderer extends DefaultListCellRenderer{
			
			public void setText(String t){
				if(t.isEmpty())
					super.setText("TODAS");
				else
					super.setText(t);
			}
		}
		
		
		
	}
	
	public static void main(String[] args) {
		new BuscadorDeCargos().actionPerformed(null);
	}

}
