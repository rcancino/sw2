package com.luxsoft.siipap.pos.ui.selectores;

import java.awt.Dimension;
import java.util.Date;
import java.util.List;

import javax.swing.JComponent;

import org.apache.commons.lang.time.DateUtils;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.matchers.Matcher;
import ca.odell.glazedlists.matchers.MatcherEditor;

import com.luxsoft.siipap.cxc.model.Abono;
import com.luxsoft.siipap.cxc.model.PagoConDeposito;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.swing.dialog.AbstractSelector;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.sw3.services.Services;

public class SelectorDeDisponibles extends AbstractSelector<Abono>{
	
	private Cliente cliente;
	protected Sucursal sucursal;
	protected Date fecha;

	public SelectorDeDisponibles() {
		super(Abono.class,"Disponibles para aplicar");
		this.sucursal=Services.getInstance().getConfiguracion().getSucursal();
		this.fecha=Services.getInstance().obtenerFechaDelSistema();
		
	}
	
	protected TextFilterator<Abono> getBasicTextFilter(){
		return GlazedLists.textFilterator("nombre");
	}

	@Override
	protected void installEditors(EventList<MatcherEditor<Abono>> editors) {
		super.installEditors(editors);
		Matcher<Abono> m1=new Matcher<Abono>(){
			public boolean matches(Abono item) {
				if(item instanceof PagoConDeposito){
					return item.getAutorizacion()!=null;
				}
				return true;
			}
		};
		MatcherEditor<Abono> e1=GlazedLists.fixedMatcherEditor(m1);
		
		Matcher<Abono> m2=new Matcher<Abono>(){
			
			public boolean matches(Abono item) {
				if(item.getLiberado()!=null){
					return true ;//DateUtils.isSameDay(fecha, item.getLiberado());
				}else
					return true;
				
				
			}
			
		};
		MatcherEditor<Abono> e2=GlazedLists.fixedMatcherEditor(m2);
		editors.add(e1);
		editors.add(e2);
	}



	@Override
	protected List<Abono> getData() {
		if(getCliente()==null){
			String hql="from Abono p  where p.fecha>=\'2011-10-31\'  and p.sucursal.id=? and p.total-p.diferencia-p.aplicado>0 and (p.tipo LIKE \'PAGO%\' or (p.tipo=\'NOTA_DEV\' AND p.origen=\'MOS\' and p.total-p.diferencia-p.aplicado>0) )";
			//String hql="from Abono p  where p.sucursal.id=? and p.total-p.aplicado>0)";
			return Services.getInstance().getHibernateTemplate().find(hql,sucursal.getId());
		}else{
			String hql="from Abono p  where p.fecha>=\'2011-10-31\' and p.sucursal.id=? and p.total-p.diferencia-p.aplicado>0 and (p.tipo LIKE \'PAGO%\' or (p.tipo=\'NOTA_DEV\' AND p.origen=\'MOS\' and p.total-p.diferencia-p.aplicado>0) ) and p.cliente.id=? ";
			return Services.getInstance().getHibernateTemplate().find(hql
					,new Object[]{sucursal.getId(),getCliente().getId()});
		}
		
	}

	@Override
	protected TableFormat<Abono> getTableFormat() {
		String props[]={"nombre","tipo","folio","fecha","total","disponible","autorizacionInfo","comentario"};
		String labels[]={"Cliente","Tipo","Folio","Fecha","Total","Disponible","Autorizado","Comentario"};
		return GlazedLists.tableFormat(Abono.class,props,labels);
	}
	
	@Override
	protected void setPreferedDimension(JComponent gridComponent){
		gridComponent.setPreferredSize(new Dimension(750,400));
	}
	
	@Override
	protected void onWindowOpened() {
		load();
	}
	
	public Cliente getCliente() {
		return cliente;
	}

	public void setCliente(Cliente cliente) {
		this.cliente = cliente;
	}
	

	public static Abono buscarPago(){
		final SelectorDeDisponibles selector=new SelectorDeDisponibles();
		selector.open();
		if(!selector.hasBeenCanceled()){
			return selector.getSelected();
		}
		return null;
	}
	
	public static Abono buscarPago(final Cliente cliente){
		System.out.println("Buscando abonos para cliente: "+cliente+ "Clave: "+cliente.getClave());
		final SelectorDeDisponibles selector=new SelectorDeDisponibles();
		selector.setCliente(cliente);
		selector.open();
		if(!selector.hasBeenCanceled()){
			return selector.getSelected();
		}
		return null;
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
				buscarPago();
				System.exit(0);
			}

		});
	}

}
