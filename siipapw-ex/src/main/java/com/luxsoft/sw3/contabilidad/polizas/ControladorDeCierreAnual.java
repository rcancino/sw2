package com.luxsoft.sw3.contabilidad.polizas;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.sw3.contabilidad.model.Poliza;
import com.luxsoft.sw3.contabilidad.model.SaldoDeCuentaPorConcepto;

/**
 * Controlador para el mantenimiento de polizas de pago de gastos
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class ControladorDeCierreAnual extends ControladorDinamico{
	
	
	
	public  ControladorDeCierreAnual() {
		
	}
	
	
	public List<Poliza> generar(Date fecha) {
		List<Poliza> polizas=new ArrayList<Poliza>();
		polizas.add(generarEliminacionIetu(fecha, "CANCELACION_IETU "+Periodo.obtenerYear(fecha)));
		polizas.add(generarCierreAnual(fecha, "CIERRE_ANUAL "+Periodo.obtenerYear(fecha)));
		return polizas;
	}
	
	
	
	private Poliza generarEliminacionIetu(Date fecha, String referencia){
		Poliza poliza=new Poliza();
		poliza.setTipo(Poliza.Tipo.DIARIO);
		poliza.setClase(getClase());
		poliza.setReferencia(referencia);
		poliza.setFecha(fecha);
		poliza.setDescripcion(getClase()+ " - "+referencia);
		poliza.setDescripcion("ELIMINACION DE CUENTAS DE IETU "+poliza.getYear());
		
		final Integer year=Periodo.obtenerYear(fecha);
		
		final List<SaldoDeCuentaPorConcepto> saldos=getHibernateTemplate().executeFind(new HibernateCallback() {			
			public Object doInHibernate(Session session) throws HibernateException,SQLException {
				List<SaldoDeCuentaPorConcepto> res= session.createQuery(
						"from SaldoDeCuentaPorConcepto s where s.year=? and s.mes=13 and s.concepto.cuenta.clave like \'90%\'")
						.setInteger(0, year)
						.list();
				
				return res;
			}
		});
		for(final SaldoDeCuentaPorConcepto saldo:saldos){
			
			BigDecimal imp=saldo.getSaldoInicial();
			boolean cargo=true;
			if(imp.doubleValue()>=0)
				cargo=false;
			
			PolizaDetFactory.generarPolizaDet(poliza
					, saldo.getConcepto().getCuenta().getClave()
					, saldo.getConcepto().getClave()
					, cargo
					, saldo.getSaldoInicial().abs()
					, "ELIMINACION CUENTAS IETU "+year
					,""
					, "OFICINAS"
					, "IETU");
		}
		poliza.actualizar();
		return poliza;
	}
	
	private Poliza generarCierreAnual(Date fecha, String referencia){
		Poliza poliza=new Poliza();
		poliza.setTipo(Poliza.Tipo.DIARIO);
		poliza.setClase(getClase());
		poliza.setReferencia(referencia);
		poliza.setFecha(fecha);
		poliza.setDescripcion(getClase()+ " - "+referencia);
		poliza.setDescripcion("Cierre del ejercicio "+poliza.getYear());
		
		final Integer year=Periodo.obtenerYear(fecha);
		
		final List<SaldoDeCuentaPorConcepto> saldos=getHibernateTemplate().executeFind(new HibernateCallback() {			
			public Object doInHibernate(Session session) throws HibernateException,SQLException {
				List<SaldoDeCuentaPorConcepto> res= session.createQuery(
						"from SaldoDeCuentaPorConcepto s where s.year=? and s.mes=13 and s.concepto.cuenta.deResultado=true ")
						.setInteger(0, year)
						.list();
				return res;
			}
		});
		for(final SaldoDeCuentaPorConcepto saldo:saldos){
			
			BigDecimal imp=saldo.getSaldoInicial();
			boolean cargo=true;
			if(imp.doubleValue()>=0)
				cargo=false;
			
			PolizaDetFactory.generarPolizaDet(poliza
					, saldo.getConcepto().getCuenta().getClave()
					, saldo.getConcepto().getClave()
					, cargo
					, saldo.getSaldoInicial().abs()
					, "CIERRE DELEJERCICIO "+year
					,""
					, "OFICINAS"
					, "CIERRE_ANUAL");
		}
		poliza.actualizar();
		BigDecimal imp=poliza.getDebe().subtract(poliza.getHaber());
		boolean cargo=true;
		if(imp.doubleValue()>=0)
			cargo=false;
		PolizaDetFactory.generarPolizaDet(poliza
				, "304"
				, "RESE01"
				, cargo
				, imp.abs()
				, "CIERRE DELEJERCICIO "+year
				,""
				, "OFICINAS"
				, "CIERRE_ANUAL");
		poliza.actualizar();
		return poliza;
	}

		
}


