package com.luxsoft.siipap.pos.ui.selectores;

import java.awt.Dimension;
import java.util.Date;
import java.util.List;

import javax.swing.JComponent;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.matchers.Matcher;
import ca.odell.glazedlists.matchers.MatcherEditor;

import com.luxsoft.siipap.cxc.model.Abono;
import com.luxsoft.siipap.cxc.model.PagoConDeposito;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.swing.dialog.AbstractSelector;
import com.luxsoft.sw3.services.Services;

public class SelectorDeAbonosDisponibles extends AbstractSelector<Abono>{
	
	private Cliente cliente;
	private Sucursal sucursal;
	

	public SelectorDeAbonosDisponibles() {
		super(Abono.class,"Pagos disponibles para aplicar");
		this.sucursal=Services.getInstance().getConfiguracion().getSucursal();
		
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
		editors.add(e1);
	}



	@Override
	protected List<Abono> getData() {
		if(getCliente()==null){
			String hql="from Abono p  where p.sucursal.id=? and p.total-p.aplicado>0";
			return Services.getInstance().getHibernateTemplate().find(hql,sucursal.getId());
		}else{
			//String hql="from Abono p  where p.sucursal.id=? and p.total-p.aplicado>0 and p.cliente.clave=? and p.tipo not like 'NOTA%'";
			String hql="from Abono p  where p.sucursal.id=? and p.total-p.aplicado>0 and p.cliente.clave=? and (p.tipo LIKE \'PAGO%\' or (p.tipo=\'NOTA_DEV\' AND p.origen=\'MOS\' and p.total-p.diferencia-p.aplicado>0) )";
			return Services.getInstance().getHibernateTemplate().find(hql
					,new Object[]{sucursal.getId(),getCliente().getClave()});
		}
		
	}

	@Override
	protected TableFormat<Abono> getTableFormat() {
		String props[]={"origen","nombre","tipo","folio","fecha","total","disponible","autorizacionInfo","comentario","liberado"};
		String labels[]={"Origen","Cliente","Tipo","Folio","Fecha","Total","Disponible","Autorizado","Comentario","Liberado"};
		return GlazedLists.tableFormat(Abono.class,props,labels);
	}
	
	@Override
	protected void setPreferedDimension(JComponent gridComponent){
		gridComponent.setPreferredSize(new Dimension(850,400));
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
		final SelectorDeAbonosDisponibles selector=new SelectorDeAbonosDisponibles();
		selector.open();
		if(!selector.hasBeenCanceled()){
			return selector.getSelected();
		}
		return null;
	}
	
	public static Abono buscarPago(final Cliente cliente){
		final SelectorDeAbonosDisponibles selector=new SelectorDeAbonosDisponibles();
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
