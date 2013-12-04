package com.luxsoft.siipap.cxc.ui.selectores;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.ListUtils;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.UniqueList;
import ca.odell.glazedlists.gui.TableFormat;

import com.jgoodies.uif.builder.ToolBarBuilder;
import com.luxsoft.siipap.cxc.model.OrigenDeOperacion;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.dialog.AbstractSelector;
import com.luxsoft.siipap.swing.reports.ReportUtils;
import com.luxsoft.siipap.swing.utils.CommandUtils;
import com.luxsoft.siipap.ventas.model.Devolucion;

public class SelectorDeRMD2 extends AbstractSelector<Devolucion>{
	
	private OrigenDeOperacion origen=OrigenDeOperacion.CRE;
	
	private Cliente cliente;

	public SelectorDeRMD2() {
		super(Devolucion.class, "Devoluciones pendientes");
		//setModal(false);
	}
	
	public void initGlazedLists(){
		source=GlazedLists.threadSafeList(new BasicEventList<Devolucion>());
		source=new UniqueList<Devolucion>(source,GlazedLists.beanPropertyComparator(Devolucion.class, "id"));
		sortedSource=new SortedList<Devolucion>(buildFilterList(),getComparator());
	}

	@Override
	protected List<Devolucion> getData() {
		if(getOrigen()!=null){
			String hql="select d.devolucion from DevolucionDeVenta d " +
			" where d.ventaDet.venta.clave=?" +
			"  and d.nota is null " +
			"  and year(d.ventaDet.venta.fecha)>=2010" +
			"  and d.ventaDet.venta.origen=\'"+getOrigen().name()+"\'";
			return ServiceLocator2.getHibernateTemplate().find(hql,cliente.getClave());
		}else{
			String hql="select d.devolucion from DevolucionDeVenta d " +
					" left join fetch d.devolucion devo" +
			" where d.ventaDet.venta.clave=?" +
			" d.nota is null " +
			" and year(d.ventaDet.venta.fecha)>=2010";
			return ServiceLocator2.getHibernateTemplate().find(hql,cliente.getClave());
		}
		
	}

	@Override
	protected TableFormat<Devolucion> getTableFormat() {
		String props[]={"numero","venta.sucursal.nombre","venta.nombre","fecha","venta.documento","venta.numeroFiscal","venta.origen","total","venta.descuentos","autorizada","venta.saldoCalculado"};
		String labels[]={"Folio","Sucursal","Cliente","Fecha","Factura","Fiscal","Origen","Importe","Descuentos","Autorizada","Saldo"};
		return GlazedLists.tableFormat(Devolucion.class,props,labels);
	}
	
	protected void addButton(ToolBarBuilder builder){
		builder.add(CommandUtils.createPrintAction(this, "imprimir"));
	}
	
	public void imprimir(){
		if(getSelected()!=null){
			Devolucion d=getSelected();
			final Map parameters=new HashMap();
			parameters.put("DEVOLUCION", d.getId());
			parameters.put("SUCURSAL", String.valueOf(d.getVenta().getSucursal().getId()));
			ReportUtils.viewReport(ReportUtils.toReportesPath("invent/Devoluciones.jasper"), parameters);
		}
	}

	public Cliente getCliente() {
		return cliente;
	}

	public void setCliente(Cliente cliente) {
		this.cliente = cliente;
		if(cliente!=null)
			setTitle("Devoluciones (RMD) para :"+cliente.getNombreRazon());
		else
			setTitle("Debe seleccionar un cliente");
	}

	public OrigenDeOperacion getOrigen() {
		return origen;
	}

	public void setOrigen(OrigenDeOperacion origen) {
		this.origen = origen;
	}
	
	public static List<Devolucion> seleccionar(OrigenDeOperacion origen){
		Cliente c=SelectorDeClientes.seleccionar();
		//System.out.println("Cliente seleccionado: "+c.getNombreRazon());
		
		if(c!=null){
			final SelectorDeRMD2 selector=new SelectorDeRMD2();
			selector.setOrigen(origen);
			selector.setCliente(c);
			selector.open();
			if(!selector.hasBeenCanceled()){
				List<Devolucion> res=new ArrayList<Devolucion>(selector.getSelectedList());
				return res;
			}
		}
		return ListUtils.EMPTY_LIST;
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
				List data=seleccionar(OrigenDeOperacion.CRE);
				System.out.println(data);
				System.exit(0);
			}

		});
	}
}
