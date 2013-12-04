package com.luxsoft.siipap.inventarios.procesos;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.exception.ExceptionUtils;

import com.luxsoft.siipap.inventarios.model.CostoPromedio;
import com.luxsoft.siipap.inventarios.model.InventarioAnual;
import com.luxsoft.siipap.inventarios.model.TransformacionDet;
import com.luxsoft.siipap.service.ServiceLocator2;

public class AjusteDeCostoParaTransformaciones {
	
	private Set<String> sinCosto=new HashSet<String>();
	
	public void actualizarCostoOrigen(final String clave,int year,int mes){
		List<TransformacionDet> salidas=ServiceLocator2.getHibernateTemplate()
			.find("from TransformacionDet d " +
					" where year(d.fecha)=? " +
					"  and month(d.fecha)=? " +
					"  and d.clave=? "+
					"  and d.cantidad<0  "
					,new Object[]{year,mes,clave});		
		for(TransformacionDet salida:salidas){			
			int year_ini=year;
			int mes_ini=mes-1;
			
			if(mes==1){
				year_ini=year-1;
				mes_ini=12;
			}			
			BigDecimal costoOrigen=null;
			if((mes==1) && (year==2009)){
				List<InventarioAnual> inv=ServiceLocator2.getHibernateTemplate().find("from InventarioAnual i where i.clave=?",salida.getClave());
				costoOrigen=inv.isEmpty()?null:inv.get(0).getCostoPromedio();
			}else{				
				CostoPromedio costop=ServiceLocator2.getCostoPromedioManager().buscarCostoPromedio(year_ini, mes_ini, salida.getClave());
				if(costop!=null)
					costoOrigen=costop.getCostop();
			}
			if(costoOrigen==null){
				continue;
			}
			salida.setCostoOrigen(costoOrigen);
			TransformacionDet entrada=salida.getDestino();
			entrada.actualizarCostoOrigen();			
			salida=ServiceLocator2.getTransformacionesManager().save(salida);
		}				
	}
	
	public void actualizarCostoOrigen(int year,int mes){
		List<TransformacionDet> salidas=ServiceLocator2.getHibernateTemplate()
			.find("from TransformacionDet d " +
					" where year(d.fecha)=?" +
					"  and month(d.fecha)=? " +
					" and d.cantidad<0  "
					,new Object[]{year,mes});
		System.out.println("Registros a procesar: "+salidas.size());
		for(TransformacionDet salida:salidas){
			System.out.println("Procesando salida TransformacionDet: "+salida);
			try {
				int year_ini=year;
				int mes_ini=mes-1;
				
				if(mes==1){
					year_ini=year-1;
					mes_ini=12;
				}
				
				BigDecimal costoOrigen=null;
				if((mes==1) && (year==2009)){
					List<InventarioAnual> inv=ServiceLocator2.getHibernateTemplate().find("from InventarioAnual i where i.clave=?",salida.getClave());
					costoOrigen=inv.isEmpty()?null:inv.get(0).getCostoPromedio();
				}else{				
					CostoPromedio costop=ServiceLocator2.getCostoPromedioManager().buscarCostoPromedio(year_ini, mes_ini, salida.getClave());
					if(costop!=null)
						costoOrigen=costop.getCostop();
					else
						sinCosto.add(salida.getClave());
				}
				
				if(costoOrigen==null){
					sinCosto.add(salida.getClave());
					continue;
				}
				salida.setCostoOrigen(costoOrigen);
				TransformacionDet entrada=salida.getDestino();
				entrada.actualizarCostoOrigen();
				
				salida=ServiceLocator2.getTransformacionesManager().save(salida);
				
				/*entrada=salida.getDestino();
				if(salida.getClave().equals("ADHBTEF48")){
					String pattern="Clave: {0}  " +
					" {1} " +
					" Costo Origen :{2} " +
					" Costo Prom:   {3}" +
					" Costo:  {4} Docto: {5} Tipo: {6}";
					System.out.println(MessageFormat.format(pattern, salida.getClave(),salida.getCantidad(),salida.getCostoOrigen(),salida.getCostoPromedio(),salida.getCosto(),salida.getDocumento(),salida.getTipoDocto()));
					System.out.println(MessageFormat.format(pattern, entrada.getClave(),entrada.getCantidad(),entrada.getCostoOrigen(),entrada.getCostoPromedio(),entrada.getCosto(),entrada.getDocumento(),entrada.getTipoDocto()));						
					System.out.println("....");
				}*/
			} catch (Exception e) {
				System.out.println("Error procesando: "+salida.getClave()+ ExceptionUtils.getRootCause(e));
				e.printStackTrace();
			}
			
		}		
				
	}
	
	public static void main(String[] args) {
		AjusteDeCostoParaTransformaciones task=new AjusteDeCostoParaTransformaciones();
		int mes=3;
		//task.actualizarCostoOrigen(2009,mes);
		task.actualizarCostoOrigen("NEV74",2009,mes);
		//task.actualizarCostoPromedio(2009, mes);
		//ServiceLocator2.getCostosServices().actualizarMovimientosPromedio(2009, mes);
		//System.out.println("Sin costo:"+task.sinCosto);
		
		/*for(int mes=7;mes<=7;mes++){
			task.actualizarCostoOrigen(2009,mes);
			task.actualizarCostoPromedio(2009, mes);
			ServiceLocator2.getCostosServices().actualizarMovimientosPromedio(2009, mes);
			System.out.println("Sin costo:"+task.sinCosto);
		}*/
	}

}
