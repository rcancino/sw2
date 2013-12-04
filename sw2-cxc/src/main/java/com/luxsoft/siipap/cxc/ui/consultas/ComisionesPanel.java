package com.luxsoft.siipap.cxc.ui.consultas;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.apache.commons.lang.StringUtils;

import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.cxc.CXCActions;
import com.luxsoft.siipap.cxc.rules.CXCUtils;

import com.luxsoft.siipap.cxc.ui.selectores.SelectorDeFacturasComisionables2;
import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.reports.RelacionDeComisiones;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.browser.FilteredBrowserPanel;
import com.luxsoft.siipap.swing.controls.AbstractControl;
import com.luxsoft.siipap.swing.utils.CommandUtils;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.siipap.ventas.model.ComisionVenta;

public class ComisionesPanel extends FilteredBrowserPanel<ComisionVenta>{

	public ComisionesPanel() {
		super(ComisionVenta.class);
	}
	
	protected void init(){
		addProperty(
				"venta.documento"
				,"venta.origen"
				,"nombre"
				,"sucursal"
				,"venta.fecha"
				,"venta.total"
				,"venta.saldoCalculado"
				,"atraso"
				,"pagoComisionable"
				,"vendedorNombre"
				,"comisionVendedor"
				,"impComisionVend"
				,"cobradorNombre"
				,"comisionCobrador"
				,"impComisionCob"
				,"periodo"
				,"fechaCancelacionVendedor"
				,"comentarioCancelacionVen"
				,"fechaCancelacionCobrador"
				,"comentarioCancelacionCob"
				
				);
		addLabels(
				"Docto"
				,"Org"	
				,"Cliente"
				,"Suc"
				,"Fecha"
				,"Total"
				,"Saldo"
				,"Atraso"
				,"Imp Comision"
				,"Vendedor"
				,"Com(Vend)"
				,"Imp Com(Vend)"
				,"Cobrador"
				,"Com(Cob)"
				,"Imp Com(Cob)"
				,"Periodo"
				,"FCancel(Vend)"
				,"Cancelacion (Vend) "
				,"FCancel(Cob)"
				,"Cancelacion (Cob) "
				);
		installTextComponentMatcherEditor("Vendedor", "vendedorNombre");
		installTextComponentMatcherEditor("Cobrador", "cobradorNombre");
		installTextComponentMatcherEditor("Cliente", "nombre","clave");
		installTextComponentMatcherEditor("Docto", "venta.documento");
		installTextComponentMatcherEditor("Fiscal", "venta.numeroFiscal");
		installTextComponentMatcherEditor("Origen", "venta.origen");
		manejarPeriodo();
		
		addActions(getLoadAction()
				,getSecuredInsertAction(CXCActions.MantenimientoDeComisiones.getId())
				,addAction(CXCActions.MantenimientoDeComisiones.getId(), "mostrarVenta", "Mostrar venta")
				,getSecuredDeleteAction(CXCActions.MantenimientoDeComisiones.getId())
				,addAction(CXCActions.MantenimientoDeComisiones.getId(), "cancelarComisionVen", "Cancelar comision (Vend)")
				,addAction(CXCActions.MantenimientoDeComisiones.getId(), "cancelarComisionCob", "Cancelar comision (Cob)")
				,CommandUtils.createPrintAction(this, "print")
				);
	}


	

	public void mostrarVenta(){
		if(getSelectedObject()!=null){
			ComisionVenta v=(ComisionVenta)getSelectedObject();
			FacturaForm.show(v.getVenta().getId());
		}
	}
	
	public void insert(){
		List<ComisionVenta> comisiones=SelectorDeFacturasComisionables2.buscarVentas(periodo);
		if(!comisiones.isEmpty()){
			//ComisionVe
			for(ComisionVenta c:comisiones){
				try {
					c=(ComisionVenta)ServiceLocator2.getUniversalDao().save(c);
					source.add(c);
				} catch (Exception e) {
					logger.error(e);
				}
			}
		}
	}
	
	public void delete(){
		if(getSelected().isEmpty()) return;
		if(MessageUtils.showConfirmationMessage("Eliminar : "+getSelected().size()+" comisiones?", "Comisiones de venta")){
			List data=new ArrayList();
			data.addAll(getSelected());
			for(Object o:data){
				ComisionVenta c=(ComisionVenta)o;
				int index=source.indexOf(c);
				if( (index!=-1 )&& (doDelete(c)) ){
					source.remove(index);
				}
			}
		}
			
	}
	
	public boolean doDelete(ComisionVenta bean){
		try {
			ServiceLocator2.getUniversalDao().remove(ComisionVenta.class,bean.getId());
			return true;
		} catch (Exception e) {
			logger.error(e);
			return false;
		}
		
	}
	
	public void cancelarComisionVen(){
		for(Object o:getSelected()){
			try {
				ComisionVenta c=(ComisionVenta)o;
				int index=source.indexOf(c);
				if(index!=-1){
					String msg=JOptionPane.showInputDialog(getControl(), "Motivo","Cancelación",JOptionPane.INFORMATION_MESSAGE);
					c.setComentarioCancelacionVen(msg);
					c.setFechaCancelacionVendedor(new Date());
					c=(ComisionVenta)ServiceLocator2.getUniversalDao().save(c);
					source.set(index, c);
				}
				
			} catch (Exception e) {
				e.printStackTrace();
			}			
		}
	}
	
	public void cancelarComisionCob(){
		for(Object o:getSelected()){
			try {
				ComisionVenta c=(ComisionVenta)o;
				int index=source.indexOf(c);
				if(index!=-1){
					String msg=JOptionPane.showInputDialog(getControl(), "Motivo","Cancelación",JOptionPane.INFORMATION_MESSAGE);
					c.setComentarioCancelacionCob(msg);
					c.setFechaCancelacionCobrador(new Date());
					c=(ComisionVenta)ServiceLocator2.getUniversalDao().save(c);
					source.set(index, c);
				}
				
			} catch (Exception e) {
				e.printStackTrace();
			}			
		}
	}
	
	public void print(){
		RelacionDeComisiones.run();
		
	}

	@Override
	protected List<ComisionVenta> findData() {
		String hql="from ComisionVenta c where c.fechaInicial=? and c.fechaFinal=?";
		return ServiceLocator2.getHibernateTemplate().find(hql, new Object[]{periodo.getFechaInicial(),periodo.getFechaFinal()});
	}	
	
private TotalesPanel totalPanel;
	
	public JPanel getTotalesPanel(){
		if(totalPanel==null){
			totalPanel=new TotalesPanel();
		}
		return (JPanel)totalPanel.getControl();
	}
	
	private class TotalesPanel extends AbstractControl implements ListEventListener{
		
		private JLabel totalCobrado=new JLabel();
		private JLabel totalComisionVendedor=new JLabel();
		private JLabel totalComisionCobrador=new JLabel();
		

		@Override
		protected JComponent buildContent() {
			final FormLayout layout=new FormLayout("p,2dlu,f:p:g","");
			DefaultFormBuilder builder=new DefaultFormBuilder(layout);
			totalCobrado.setHorizontalAlignment(SwingConstants.RIGHT);
			totalComisionVendedor.setHorizontalAlignment(SwingConstants.RIGHT);
			totalComisionCobrador.setHorizontalAlignment(SwingConstants.RIGHT);
			builder.append("Total cobrado",totalCobrado);
			builder.append("Tot com vendedor",totalComisionVendedor);
			builder.append("Tot com cobrador",totalComisionCobrador);
			builder.getPanel().setOpaque(false);
			getFilteredSource().addListEventListener(this);
			updateTotales();
			return builder.getPanel();
		}
		
		public void listChanged(ListEvent listChanges) {
			if(listChanges.next()){
				
			}
			updateTotales();
		}
		
		public void updateTotales(){
			BigDecimal pagos=BigDecimal.ZERO;
			BigDecimal comisionVendedor=BigDecimal.ZERO;
			BigDecimal comisionCobrador=BigDecimal.ZERO;
			for(Object o:getFilteredSource()){
				ComisionVenta bean=(ComisionVenta)o;
				pagos=pagos.add(bean.getPagoComisionable());
				comisionVendedor=comisionVendedor.add(bean.getImpComisionVend());
				comisionCobrador=comisionCobrador.add(bean.getImpComisionCob());
			}
			String pattern="{0}";
			totalCobrado.setText(MessageFormat.format(pattern, pagos));
			totalComisionVendedor.setText(MessageFormat.format(pattern, comisionVendedor));
			totalComisionCobrador.setText(MessageFormat.format(pattern, comisionCobrador));
		}
		
		private NumberFormat nf=NumberFormat.getPercentInstance();
		
	}

}
