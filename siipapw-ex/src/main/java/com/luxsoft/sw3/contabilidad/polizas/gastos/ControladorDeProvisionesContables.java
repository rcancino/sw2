package com.luxsoft.sw3.contabilidad.polizas.gastos;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.ui.ModelMap;

import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.gastos.ConceptoDeGasto;
import com.luxsoft.siipap.model.gastos.GCompra;
import com.luxsoft.siipap.model.gastos.GCompraDet;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.sw3.contabilidad.model.Poliza;
import com.luxsoft.sw3.contabilidad.model.Poliza.Tipo;
import com.luxsoft.sw3.contabilidad.polizas.ControladorDinamico;
import com.luxsoft.sw3.contabilidad.polizas.PolizaDetFactory;

/**
 * Controlador para el mantenimiento de polizas de anticipo de gastos
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class ControladorDeProvisionesContables extends ControladorDinamico{
	
	
	public  ControladorDeProvisionesContables() {
		setClase("PROVISION_CARGA_SOCIAL");
	}
	
	public List<Poliza> generar(final Date fecha) {
		final List<Poliza> polizas=new ArrayList<Poliza>();
		getHibernateTemplate().execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException,SQLException {
				final List<GCompra> facs=session.createQuery(
						"from GCompra c where c.fecha= ? and c.tipo like \'PROVISION%\'")
						.setParameter(0, fecha,Hibernate.DATE)						
						.list();
				ListIterator<GCompra> iter=facs.listIterator();				
				while(iter.hasNext()){
					GCompra compra=iter.next();
					Poliza p=generarPoliza(compra,fecha);
					p.actualizar();
					polizas.add(p);
				}
				return null;
			}
		});
		return polizas;
	}
	
	private Poliza generarPoliza(GCompra compra,Date fecha){
		String tipo=compra.getTipo().name();
		final String asiento="PROVISION";
		final Poliza p=new Poliza();
		p.setTipo(Tipo.DIARIO);
		p.setFecha(fecha);
		p.setClase(getClase());
		p.setReferencia(compra.getId().toString());
		p.setDescripcion(tipo+" "+compra.getComentario());
		
		
		for(GCompraDet det:compra.getPartidas()){
			String proveedor=compra.getProveedor().getNombre();
			String cuenta=det.getRubro().getRubroCuentaOrigen().getCuentaOrigen();
			cuenta=StringUtils.substring(cuenta,0, 3);
			ConceptoDeGasto conceptoGasto=det.getRubro().getRubroSegundoNivel(det.getRubro());
			boolean cargo=det.getImporte().doubleValue()>0?true:false;
			BigDecimal importe=det.getImporte().abs();
			

/*
			String descripcion2	=	det.getRubro().getCuentaContable().equals("110-V001-000") ? det.getComentario() :
						det.getRubro().getCuentaContable().equals("118-0002-000") ? det.getComentario() : det.getRubro().getDescripcion();
			
			PolizaDetFactory.generarPolizaDet(p, cuenta, conceptoGasto.getId().toString(), cargo, importe,descripcion2, proveedor,det.getSucursal().getNombre(), asiento);					
*/	
					
			PolizaDetFactory.generarPolizaDet(p, cuenta, conceptoGasto.getId().toString(), cargo, importe,det.getRubro().getDescripcion(), proveedor,det.getSucursal().getNombre(), asiento);
		}
		
		return p;
	}
	


	public static void main(String[] args) {
		ControladorDeProvisionesContables c=new ControladorDeProvisionesContables();
		c.generar(DateUtil.toDate("29/02/2012"));
		
	}

		
}


