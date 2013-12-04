package com.luxsoft.sw3.contabilidad.polizas.gastos;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

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
public class ControladorDeProvisionDeNomina extends ControladorDinamico{
	
	
	public  ControladorDeProvisionDeNomina() {
		setClase("PROVISION_NOMINA");
	}
	
	
	@Override
	public Poliza generar(final Date fecha1, String referencia) {
		final Periodo per=Periodo.getPeriodoDelMesActual(fecha1);
		final Date fecha=per.getFechaFinal();
		final Poliza p=new Poliza();
		p.setTipo(Tipo.DIARIO);
		p.setFecha(fecha1);
		p.setClase(getClase());
		p.setReferencia(referencia);
		p.setDescripcion(MessageFormat.format("PROVISION DE NOMINA", DateUtil.convertDateToString(fecha)));
		final String asiento="NOMINA";
		getHibernateTemplate().execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException,SQLException {
				final List<GCompra> facs=session.createQuery(
						"from GCompra c where c.fecha between ? and ? and c.tipo=\'PROVISION_N\'")
						.setParameter(0, per.getFechaInicial(),Hibernate.DATE)
						.setParameter(1, per.getFechaFinal(),Hibernate.DATE)
						.list();
				ListIterator<GCompra> iter=facs.listIterator();
				
				while(iter.hasNext()){
					GCompra compra=iter.next();
					for(GCompraDet det:compra.getPartidas()){
						String cuenta=det.getRubro().getRubroCuentaOrigen().getCuentaOrigen();
						cuenta=StringUtils.substring(cuenta,0, 3);
						ConceptoDeGasto conceptoGasto=det.getRubro().getRubroSegundoNivel(det.getRubro());
						boolean cargo=det.getImporte().doubleValue()>0?true:false;
						BigDecimal importe=det.getImporte().abs();
						PolizaDetFactory.generarPolizaDet(p, cuenta, conceptoGasto.getId().toString(), cargo, importe, det.getRubro().getDescripcion(), "", "OFICINAS", asiento);
					}
					p.setDescripcion("ID:"+compra.getId()+"-"+p.getDescripcion());
				}
				p.actualizar();
				return p;
			}
		});
		return p;
	}
	
	
	
	public static void main(String[] args) {
		ControladorDeProvisionDeNomina c=new ControladorDeProvisionDeNomina();
		c.generar(DateUtil.toDate("29/02/2012"));
		
	}

		
}


