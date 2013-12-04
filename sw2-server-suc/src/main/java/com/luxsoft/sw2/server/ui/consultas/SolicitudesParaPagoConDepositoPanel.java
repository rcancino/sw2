package com.luxsoft.sw2.server.ui.consultas;

import java.util.List;

import org.jdesktop.swingx.JXTable;

import ca.odell.glazedlists.GlazedLists;

import com.luxsoft.sw3.tesoreria.model.SolicitudDeDeposito;

/**
 * Panel para el mantenimiento de replicas de solicituedes de deposito
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class SolicitudesParaPagoConDepositoPanel extends DefaultSucursalReplicaPanel<SolicitudDeDeposito> {
	

	public SolicitudesParaPagoConDepositoPanel() {
		super(SolicitudDeDeposito.class);
	}
	
	public void init(){
		addProperty(
				"sucursal.nombre"
				,"origen"
				,"clave"
				,"nombre"
				,"documento"
				,"fecha"
				,"fechaDeposito"
				,"comentario"
				,"referenciaBancaria"
				,"total"
				,"cuentaDestino.descripcion"
				,"bancoOrigen.clave"
				,"solicita"
				,"salvoBuenCobro"
				,"comentario"
				,"cancelacion"
				,"comentarioCancelacion"
				,"log.modificado"
				);
		addLabels(
				"Sucursal"
				,"Tipo"
				,"Cliente"
				,"Nombre"
				,"Folio"
				,"Fecha"
				,"Fecha (Dep)"
				,"Comentario"
				,"Referencia"
				,"Total"
				,"Cuenta Dest"
				,"Banco"
				,"Solicita"
				,"SBC"
				,"Comentario"
				,"Cancelacion"
				,"Comentario (Cancel)"
				,"Ultima Mod"				
				);
		setDefaultComparator(GlazedLists.beanPropertyComparator(SolicitudDeDeposito.class, "log.modificado"));
		
	}
	
	
	
	@Override
	protected void adjustMainGrid(JXTable grid) {		
		//grid.getColumnExt("Importado").setVisible(false);
	}
	
	

	@Override
	protected List<SolicitudDeDeposito> findData() {
		String hql="from SolicitudDeDeposito s left join fetch s.cliente c" +
				" left join fetch s.cuentaDestino c" +
				" left join fetch s.bancoOrigen b" +
				" left join fetch s.pago p " +
				" where s.sucursal.id=? and date(s.fecha) between ? and ?";
		return getHibernateTemplate().find(hql,getDefaultParameters());
	}
	
	
	
	

}
