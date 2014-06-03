package com.luxsoft.siipap.pos.ui.selectores;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;

import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.ListSelection;
import ca.odell.glazedlists.gui.TableFormat;

import com.jgoodies.uif.AbstractDialog;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.swing.dialog.AbstractSelector;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.siipap.ventas.model.Asociado;
import com.luxsoft.sw3.services.Services;
import com.luxsoft.sw3.ventas.Pedido;

public class SelectorDePedidosPendientes extends AbstractSelector<Pedido>{

	private static Cliente cliente;

	
	public SelectorDePedidosPendientes( Cliente c) {
		super(Pedido.class, "Pedidos Pendientes de Facturar");
		cliente=c;
	}

	@Override
	protected List<Pedido> getData() {
		
		List<Pedido> pedidos;
		Calendar c1 = GregorianCalendar.getInstance();
        c1.setTime(new Date());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy//MM/dd");
        c1.add(Calendar.DATE, -15);
		Date fecha=c1.getTime();
        String hql="from Pedido a where a.cliente.id=?  and a.totalFacturado=0 and a.fecha >=?";
		Object[] args={cliente.getId(),fecha};
		pedidos=Services.getInstance().getHibernateTemplate().find(hql,args);
		return pedidos;
		
	}

	@Override
	protected TableFormat<Pedido> getTableFormat() {
		return GlazedLists.tableFormat(Pedido.class
				,new String[]{"folio","fecha","subTotal","total"}
				,new String[]{"Folio","Fecha","Subtotal","Total"}
		);
	}
	
	
	public static Pedido seleccionar(Cliente c){
		
		final SelectorDePedidosPendientes selector=new SelectorDePedidosPendientes(c);
		
		selector.open();
		selector.setSelectionMode(ListSelection.SINGLE_SELECTION);
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
				//System.out.println(ToStringBuilder.reflectionToString(seleccionar()));
				System.exit(0);
			}

		});
	}

}
