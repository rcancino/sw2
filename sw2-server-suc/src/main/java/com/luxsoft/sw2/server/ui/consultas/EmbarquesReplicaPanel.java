package com.luxsoft.sw2.server.ui.consultas;

import java.util.List;

import org.jdesktop.swingx.JXTable;

import ca.odell.glazedlists.GlazedLists;


import com.luxsoft.sw3.embarque.Embarque;


/**
 * Panel para el mantenimiento de replicas de Compras
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class EmbarquesReplicaPanel extends DefaultSucursalReplicaPanel<Embarque> {
	
	private String sucursalName;
	

	public EmbarquesReplicaPanel() {
		super(Embarque.class);
	}
	
	public void init(){
		addProperty("documento","chofer","log.creado","comentario","salida","regreso","sucursal");
		addLabels("Embarque","Chofer","Registrado","Comentario","Salida","Regreso","Sucursal");
		
		
		installTextComponentMatcherEditor("Id", "id");
		installTextComponentMatcherEditor("Chofer", "transporte.chofer.nombre");
		installTextComponentMatcherEditor("Sucursal", "sucursal");
		setDefaultComparator(GlazedLists.beanPropertyComparator(Embarque.class, "log.modificado"));
		
	}
	
	@Override
	protected void adjustMainGrid(JXTable grid) {		
		//grid.getColumnExt("Importado").setVisible(false);
	}
	

	protected Object[] getDefaultParameters(){
		return new Object[]{
				getSucursalName()
				,periodo.getFechaInicial()
				,periodo.getFechaFinal()
		};
	}
	

	@Override
	protected List<Embarque> findData() {
		String hql="from Embarque e " +
				" where e.sucursal=? and date(e.fecha) between ? and ?";
		return getHibernateTemplate().find(hql,getDefaultParameters());
	}

	public String getSucursalName() {
		return sucursalName;
	}

	public void setSucursalName(String sucursalName) {
		this.sucursalName = sucursalName;
	}
	
	
	
	

}
