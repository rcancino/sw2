package com.luxsoft.sw3.contabilidad.ui.consultas;

import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.util.Assert;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.GroupingList;

import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.contabilidad.GeneradorDePoliza;
import com.luxsoft.siipap.model.gastos.ConceptoDeGasto;
import com.luxsoft.siipap.model.gastos.GCompra;
import com.luxsoft.siipap.model.gastos.GCompraDet;
import com.luxsoft.siipap.model.gastos.GFacturaPorCompra;
import com.luxsoft.siipap.model.tesoreria.RequisicionDe;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.sw3.contabilidad.model.Poliza;
import com.luxsoft.sw3.contabilidad.model.Poliza.Tipo;
import com.luxsoft.sw3.contabilidad.model.PolizaDet;
import com.luxsoft.sw3.contabilidad.services.AbstractPolizaManager;
import com.luxsoft.sw3.contabilidad.services.PolizaContableManager;

/**
 * Implementacion de {@link GeneradorDePoliza} para crear la o las polizas
 * de gastos a partir de Facturas de gastos
 * 
 * @author Ruben Cancino 
 *
 */
public class PolizaDeAnticiposGastosController extends AbstractPolizaManager{
	

	@Override
	protected void procesarPoliza() {
	}

	@SuppressWarnings("unchecked")
	public Poliza generar(final Date fecha) {
		final Poliza p=new Poliza();
		p.setTipo(Tipo.DIARIO);
		p.setDescripcion(MessageFormat.format("Provision de facturas", DateUtil.convertDateToString(fecha)));
		p.setFecha(fecha);
		
		getHibernateTemplate().execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException,SQLException {
				final List<GFacturaPorCompra> facs=session.createQuery(
						"from GFacturaPorCompra fac where fac.fecha=?")
						.setParameter(0, fecha,Hibernate.DATE)
						.list();
				ListIterator<GFacturaPorCompra> iter=facs.listIterator();
				while(iter.hasNext()){
					GFacturaPorCompra ff=iter.next();
					for(RequisicionDe det:ff.getRequisiciones()){
						if(det.getRequisicion().getConcepto()!=null){
							if(det.getRequisicion().getConcepto().getId()==201136){
								PolizaDet abono=p.agregarPartida();
								abono.setCuenta(getCuenta("111"));
								abono.setDescripcion("ANTICIPOS PROVEEDORES GASTOS");
								String pattern="ANTICIPO F:{0} {1,date,short}";			
								String descripcion2=MessageFormat.format(pattern, det.getDocumento(),det.getFechaDocumento());
								abono.setDescripcion2(descripcion2);
								abono.setReferencia(det.getFacturaDeGasto().getProveedor());
								abono.setReferencia2(det.getFacturaDeGasto().getCompra().getSucursal().getNombre());
								abono.setHaber(det.getTotalMN().amount());
							}
						}
					}
				}
				p.actualizar();
				return p;
			}
		});
		return p;
	}
	
	
	
	
		
	

}
