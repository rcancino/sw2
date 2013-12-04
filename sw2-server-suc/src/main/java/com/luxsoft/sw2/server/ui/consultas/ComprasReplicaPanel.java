package com.luxsoft.sw2.server.ui.consultas;

import java.util.List;

import org.jdesktop.swingx.JXTable;

import ca.odell.glazedlists.GlazedLists;

import com.luxsoft.siipap.compras.model.Compra2;


/**
 * Panel para el mantenimiento de replicas de Compras
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class ComprasReplicaPanel extends DefaultCentralReplicaPanel<Compra2> {
	

	public ComprasReplicaPanel() {
		super(Compra2.class);
	}
	
	public void init(){
		addProperty("folio","sucursal.nombre","clave","nombre","fecha","moneda","tc","total","descuentoEspecial","depuracion","consolidada","comentario");
		addLabels("Folio","Sucursal","Prov","Nombre","Fecha","Mon","TC","Total","Dscto","Depuracion","Con","Comentario");
		
		installTextComponentMatcherEditor("Sucursal", "sucursal.nombre");
		installTextComponentMatcherEditor("Proveedor", "clave","nombre");		
		installTextComponentMatcherEditor("Folio", "folio");
		setDefaultComparator(GlazedLists.beanPropertyComparator(Compra2.class, "log.modificado"));
		
	}
	
	@Override
	protected void adjustMainGrid(JXTable grid) {		
		//grid.getColumnExt("Importado").setVisible(false);
	}
	
	

	@Override
	protected List<Compra2> findData() {
		String hql="from Compra2 s " +
				" where s.sucursal.id=? and date(s.fecha) between ? and ?";
		return getHibernateTemplate().find(hql,getDefaultParameters());
	}
	
	
	
	

}
