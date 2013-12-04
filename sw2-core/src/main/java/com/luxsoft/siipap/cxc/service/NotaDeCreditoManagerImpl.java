package com.luxsoft.siipap.cxc.service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;

import org.apache.poi.util.ArrayUtil;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.SortedList;

import com.luxsoft.siipap.cxc.model.AplicacionDeNota;
import com.luxsoft.siipap.cxc.model.NotaDeCreditoDevolucion;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.siipap.ventas.model.Devolucion;
import com.luxsoft.siipap.ventas.model.DevolucionDeVenta;

public class NotaDeCreditoManagerImpl extends HibernateDaoSupport implements NotaDeCreditoManager{

	/**
	 * TODO No funciona bien, falta q vincule la segunda nota con el abono
	 * DevolucionDeVenta.nota
	 */
	@Transactional(propagation=Propagation.REQUIRED)
	public NotaDeCreditoDevolucion[] generarNotaDeDevolucion(Devolucion devo,final Date fecha,int folio) {
		
		NotaDeCreditoDevolucion[] res;
		getHibernateTemplate().update(devo);
		SortedList<DevolucionDeVenta> sorted=new SortedList<DevolucionDeVenta>(
				GlazedLists.eventList(devo.getPartidas())
				,GlazedLists.beanPropertyComparator(DevolucionDeVenta.class,"renglon")
				);
		DevolucionDeVenta[] rmds=sorted.toArray(new DevolucionDeVenta[0]);
		
		
		if(rmds.length>8){ //Generamos 2 notas
			res=new NotaDeCreditoDevolucion[2];
			
			//Nota 1
			NotaDeCreditoDevolucion nota1=generarNota(devo,fecha);
			nota1.setFolio(folio++);
			
			BigDecimal impNetoNota1=BigDecimal.valueOf(0);
			BigDecimal impBrutoNota1=BigDecimal.valueOf(0);
			BigDecimal impCortesNota1=BigDecimal.valueOf(0);
			
			
			for(int index=0;index<8;index++){
				DevolucionDeVenta rmd=rmds[index];//partidas.get(index);
				System.out.println("Referenciando renglon :"+rmd.getRenglon()+" Index: "+index);
				impNetoNota1=impNetoNota1.add(rmd.getImporteNeto());
				impBrutoNota1=impBrutoNota1.add(rmd.getImporteBruto());
				impCortesNota1=impCortesNota1.add(rmd.getImporteCortesCalculado());
				rmd.setNota(nota1);
			}
			nota1.setImporte(impNetoNota1);
			nota1.actualizarImpuesto();
			nota1.actualizarTotal();
			
			//Adicional para la impresion
			nota1.setCortes(impCortesNota1);
			nota1.setDescuentos(impBrutoNota1.subtract(impNetoNota1));
			BigDecimal saldo=devo.getVenta().getSaldoCalculado();
			
			if(saldo.doubleValue()>0)
				generarAplicacion(nota1);
			nota1=(NotaDeCreditoDevolucion)getHibernateTemplate().merge(nota1);
			res[0]=nota1;
			
			//Nota 2
			
			NotaDeCreditoDevolucion nota2=generarNota(devo,fecha);
			nota2.setFolio(folio);
			BigDecimal impNetoNota2=BigDecimal.valueOf(0);
			BigDecimal impBrutoNota2=BigDecimal.valueOf(0);
			BigDecimal impCortesNota2=BigDecimal.valueOf(0);
			
			for(int index=8;index<rmds.length;index++){				
				DevolucionDeVenta rmd=rmds[index];
				System.out.println("Referenciando renglon :"+rmd.getRenglon()+ "Index: "+index);
				impNetoNota2=impNetoNota2.add(rmd.getImporteNeto());
				impBrutoNota2=impBrutoNota2.add(rmd.getImporteBruto());
				impCortesNota2=impCortesNota2.add(rmd.getImporteCortesCalculado());
				rmd.setNota(nota2);
				
			}
			
			//Adicional para la impresion
			nota2.setCortes(impCortesNota2);
			nota2.setDescuentos(impBrutoNota2.subtract(impNetoNota2));
			BigDecimal flete=nota2.getDevolucion().getVenta().getFlete();
			BigDecimal cargos=nota2.getDevolucion().getVenta().getCargos();
			nota2.setManiobras(flete.add(cargos));			
			impNetoNota2=impNetoNota2.add(flete).add(cargos);
			
			nota2.setImporte(impNetoNota2);
			nota2.actualizarImpuesto();
			nota2.actualizarTotal();
			if(saldo.doubleValue()>0)
				generarAplicacion(nota2);
			nota2=(NotaDeCreditoDevolucion)getHibernateTemplate().merge(nota2);
			res[1]=nota2;
			//getHibernateTemplate().flush();
			/*
			for(int index=8;index<rmds.length;index++){
				DevolucionDeVenta rmd=rmds[index];
				rmd.setNota(nota2);
				getHibernateTemplate().update(rmd);
				
			}*/
			return res;
			
		}else{
			res=new NotaDeCreditoDevolucion[0];
			
			NotaDeCreditoDevolucion nota=generarNota(devo,fecha);
			nota.setFolio(folio);
			
			BigDecimal impNetoNota2=BigDecimal.valueOf(0);
			BigDecimal impBrutoNota2=BigDecimal.valueOf(0);
			BigDecimal impCortesNota2=BigDecimal.valueOf(0);
			
			for(int index=8;index<rmds.length;index++){
				DevolucionDeVenta rmd=rmds[index];
				impNetoNota2=impNetoNota2.add(rmd.getImporteNeto());
				impBrutoNota2=impBrutoNota2.add(rmd.getImporteBruto());
				impCortesNota2=impCortesNota2.add(rmd.getImporteCortesCalculado());
				rmd.setNota(nota);
			}
			
			//Adicional para la impresion
			nota.setCortes(impCortesNota2);
			nota.setDescuentos(impBrutoNota2.subtract(impNetoNota2));
			BigDecimal flete=devo.getVenta().getFlete();
			BigDecimal cargos=devo.getVenta().getCargos();
			nota.setManiobras(flete.add(cargos));			
			impNetoNota2=impNetoNota2.add(flete).add(cargos);
			
			nota.setImporte(impNetoNota2);
			nota.actualizarImpuesto();
			nota.actualizarTotal();
			
			BigDecimal saldo=devo.getVenta().getSaldoCalculado();
			if(saldo.doubleValue()>0)
				generarAplicacion(nota);
			res[0]=nota;			
			return res;
			
		}		
	}
	
	private NotaDeCreditoDevolucion generarNota(Devolucion devo,final Date fecha){
		NotaDeCreditoDevolucion nota=new NotaDeCreditoDevolucion();
		nota.setDevolucion(devo);
		nota.setComentario(devo.getComentario());
		nota.setFecha(fecha);
		nota.setOrigen(devo.getVenta().getOrigen());
		nota.setSucursal(devo.getVenta().getSucursal());
		return nota;
	}
	
	private AplicacionDeNota generarAplicacion(final NotaDeCreditoDevolucion nota){
		AplicacionDeNota a=new AplicacionDeNota();
		BigDecimal saldo=nota.getDevolucion().getVenta().getSaldoCalculado();
		BigDecimal total=nota.getTotal();
		BigDecimal porAplicar;
		if(saldo.doubleValue()<=total.doubleValue()){
			porAplicar=saldo;
		}else{
			porAplicar=total;
		}
		a.setCargo(nota.getDevolucion().getVenta());
		a.setComentario("APLICACION AUTOMATICA POR DEVOLUCION");
		a.setFecha(nota.getFecha());	
		a.setImporte(porAplicar);
		nota.agregarAplicacion(a);		
		return a;
	}
	
	public static void main(String[] args) {
		String id="8a8a8486-27f25031-0127-f2e7945b-001d";
		Date fecha=DateUtil.toDate("14/04/2010");
		Devolucion devo=(Devolucion)ServiceLocator2.getUniversalDao().get(Devolucion.class, id);
		ServiceLocator2.getNotasManager().generarNotaDeDevolucion(devo, fecha, 11567);
	}

}
