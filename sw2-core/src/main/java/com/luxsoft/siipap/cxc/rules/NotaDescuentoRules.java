package com.luxsoft.siipap.cxc.rules;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.springframework.util.Assert;

import com.luxsoft.siipap.cxc.model.AplicacionDeNota;
import com.luxsoft.siipap.cxc.model.Cargo;
import com.luxsoft.siipap.cxc.model.NotaDeCreditoDescuento;
import com.luxsoft.siipap.cxc.model.OrigenDeOperacion;
import com.luxsoft.siipap.cxc.model.NotaDeCreditoDescuento.TipoDeDescuento;
import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.siipap.ventas.model.Venta;

/**
 * Reglas de negocios aplicables en la generacion y mantenimiento de
 * notas de credito de descuento
 * 
 * @author Ruben Cancino
 *
 */
public class NotaDescuentoRules {
	
	
	/**
	 * Genera una lista de aplicaciones para cada factura
	 * 
	 * TODO Preparar un test
	 * @param facturas
	 * @return
	 */
	public List<AplicacionDeNota> generarAplicaciones(final List<Cargo> facturas){
		CXCUtils.validarMismoCliente(facturas);
		CXCUtils.validarMismoTipoDeOperacion(facturas);
		//int atrasos=getAtrasos(facturas.get(0).getClave());
		//Assert.isTrue(atrasos==0,"El cliente tiene "+atrasos+" atrasos");
		List<AplicacionDeNota> aplicaciones=new ArrayList<AplicacionDeNota>();
		for(Cargo v:facturas){
			if(v instanceof Venta){				
				AplicacionDeNota ap=new AplicacionDeNota();
				ap.setCargo(v);				
				ap.setDescuentoPorNota(BigDecimal.valueOf(v.getDescuentoGeneral()));
				BigDecimal importe=calcularImporteDeDescuento(v);
				if(importe.doubleValue()!=0){
					ap.setImporte(importe);
					aplicaciones.add(ap);
					v.setDescuentos(importe);
				}				
			}
			
		}
		return aplicaciones;		
	}
	
	
	
	
	/**
	 * Calcula el importe de la nota de credito 
	 * Esta implementacion se basa en importes registrados
	 * en el cargo como parte de su estado. Por lo tanto asume
	 * que los datos registrados son correctos. No verifica si
	 * estos datos son concistentes con la notas existentes
	 * 
	 * @return El importe del descuento correspondiente al cargo, cero si el cargo no califica para un descuento
	 */
	public BigDecimal calcularImporteDeDescuento(final Cargo cargo){
		
		/*if(!cargo.isPrecioBruto() || (cargo.getDescuentoFinanciero()==0)){
			return BigDecimal.ZERO; //Las ventas a precio neto no requieren nota de descuento
		}else if(cargo.getDescuentos().doubleValue()>0)
			return BigDecimal.ZERO; //Ya existe un descuento aplicado
		*/
		double descuentoFinanciero=cargo.getDescuentoFinanciero();
		if(descuentoFinanciero>0){
			BigDecimal val=cargo.getTotal();		
			CantidadMonetaria vval=CantidadMonetaria.pesos(val.doubleValue());
			vval=vval.subtract(CantidadMonetaria.pesos(cargo.getDevoluciones().doubleValue()));
			vval=vval.subtract(CantidadMonetaria.pesos(cargo.getBonificaciones().doubleValue()));
			CantidadMonetaria impDDesc=vval.multiply(descuentoFinanciero/100);		
			return impDDesc.amount().abs();
		}
		return BigDecimal.ZERO;
	}
	
	/**
	 * Actualiza de manera adecuada los importes de la nota de credito en funcion 
	 * del tipo de bonificacion y el modo de calculo
	 * 
	 * @param nota
	 *//*
	@Override
	public void actualizarImportes(final NotaDeCredito notaCredito){
		NotaDeCreditoDescuento nota=(NotaDeCreditoDescuento)notaCredito;
		aplicarDescuento(nota);
		actualizarImportesDesdeAplicaciones(nota);
	}*/
	
	/**
	 * Calcula el importe de las aplicaciones en funcion
	 * del tipo de descuento
	 * 
	 *//*
	public void aplicarDescuento(final NotaDeCredito nota){
		NotaDeCreditoDescuento nd=(NotaDeCreditoDescuento)nota;
		for(Aplicacion a:nd.getAplicaciones()){
			BigDecimal importe=calcularImporteDeDescuento(a.getCargo());
			a.setImporte(importe);
			a.setDescuentoPorNota(BigDecimal.valueOf(a.getCargo().getDescuentoGeneral()));
		}
		
	}*/
	
	/**
	 * Genera una lista de {@link NotaDeCreditoDescuento} a partir de las aplicaciones
	 * indicadas atendiendo a las reglas vigentes para la generacion de cada nota
	 *   
	 * 
	 * @param aplicaciones La lista de las aplicaciones a procesar
	 * @return La lista de notas de credito por descuento
	 * @see #crearNotaDeCredito(List, OrigenDeOperacion)
	 * 
	 */
	public List<NotaDeCreditoDescuento> generarNotas(List<AplicacionDeNota> aplicaciones){
		List<NotaDeCreditoDescuento> notas=new ArrayList<NotaDeCreditoDescuento>();
		while(!aplicaciones.isEmpty()){
			NotaDeCreditoDescuento nota=crearNotaDeCredito(aplicaciones);
			notas.add(nota);
		}
		return notas;		
	}
	
	/**
	 * Genera una nota de credito de descuento a partir de una lista de aplicaciones
	 * 
	 * Solo toma en cuenta 10 partidas y cada Aplicacion es removida de la lista,
	 * dejando la lista solo con las aplicaciones no asignadas  
	 * 
	 * @param aplicaciones
	 * @param origen
	 * @return
	 */
	public NotaDeCreditoDescuento crearNotaDeCredito(final List<AplicacionDeNota> aplicaciones){
		Assert.notEmpty(aplicaciones,"No se puede generar una nota de credito de descuento sin aplicaciones");
		
		
		final NotaDeCreditoDescuento nota=new NotaDeCreditoDescuento();
		nota.setCliente(aplicaciones.get(0).getCargo().getCliente());
		nota.setDescuento(aplicaciones.get(0).getDescuentoPorNota().doubleValue());
		nota.setOrigen(aplicaciones.get(0).getCargo().getOrigen());
		nota.setTipoDeDescuento(TipoDeDescuento.ASIGNADO);
		nota.setComentario("DESCUENTO SOBRE VENTAS");
		
		ListIterator<AplicacionDeNota> iterator=aplicaciones.listIterator();
		int index=0;
		while(iterator.hasNext()){
			AplicacionDeNota next=iterator.next();
			nota.agregarAplicacion(next);
			iterator.remove();
			index++;
			if(index>=10)
				break;
		}
		
		nota.actualizarTotal();
		return nota;
	}
	
	public static int getAtrasos(final String clienteClave ){
		String sql="SELECT COUNT(V.DOCTO)  FROM SX_VENTAS V " +
				" WHERE V.ORIGEN=\'CRE\' AND V.CLAVE=? " +
				" AND V.TOTAL-IFNULL((SELECT SUM(X.IMPORTE) FROM sx_cxc_aplicaciones X WHERE X.CARGO_ID=V.CARGO_ID),0)>0 " +
				" AND TO_DAYS(CURRENT_DATE)-TO_DAYS(V.VTO)>0 AND V.FECHA>? " +
				"GROUP BY V.CLAVE";
		List<Integer> res=ServiceLocator2.getJdbcTemplate().queryForList(sql
				,new Object[]{clienteClave,DateUtil.toDate("31/03/2009")},Integer.class);
		return res.isEmpty()?0:res.get(0);
	}
	
	public static void main(String[] args) {
		int atrasos=getAtrasos("E030985");
		System.out.println(atrasos);
	}

}
