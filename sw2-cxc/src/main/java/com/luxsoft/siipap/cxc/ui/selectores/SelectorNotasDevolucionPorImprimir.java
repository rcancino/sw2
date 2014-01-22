package com.luxsoft.siipap.cxc.ui.selectores;

import java.util.List;

import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.gui.TableFormat;

import com.luxsoft.siipap.cxc.model.NotaDeCreditoDevolucion;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.dialog.AbstractSelector;
import com.luxsoft.siipap.util.DateUtil;

public class SelectorNotasDevolucionPorImprimir extends AbstractSelector<NotaDeCreditoDevolucion>{
	
	
	
	public SelectorNotasDevolucionPorImprimir() {
		super(NotaDeCreditoDevolucion.class, "Notas de crédito (Pendientes de Imprimir)");
	}	

	@Override
	protected List<NotaDeCreditoDevolucion> getData() {
		String hql3="from NotaDeCreditoDevolucion n where " +
		" n.fecha>?" +
		" and n.origen=\'MOS\' " +
		" and n.id not in (select cf.origen from CFDI cf where cf.tipo=?)";		
		return ServiceLocator2
			.getHibernateTemplate()
			.find(hql3,new Object[]{DateUtil.toDate("28/02/2010"),"NOTA_CREDITO"});
	}

	@Override
	protected TableFormat<NotaDeCreditoDevolucion> getTableFormat() {
		String props[]={
				"sucursal.nombre"
				,"clave"
				,"cliente.nombre"
				,"fecha"
				,"devolucion.numero"
				,"total"
				};
		String labels[]={
				"Sucursal"
				,"Cliente"
				,"Nombre"
				,"Fecha"
				,"RMD"
				,"Total"
				};
		return GlazedLists.tableFormat(NotaDeCreditoDevolucion.class,props,labels);
	}

	
	
	@Override
	protected TextFilterator<NotaDeCreditoDevolucion> getBasicTextFilter() {
		return GlazedLists.textFilterator("cliente.nombre","devolucion.numero");
	}

	public static NotaDeCreditoDevolucion seleccionar(){
		SelectorNotasDevolucionPorImprimir selector=new SelectorNotasDevolucionPorImprimir();
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
				System.out.println(seleccionar());
				System.exit(0);
			}

		});
	}

}
