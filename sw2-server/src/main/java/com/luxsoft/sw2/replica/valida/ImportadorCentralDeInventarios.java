package com.luxsoft.sw2.replica.valida;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;

import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.utils.LoggerHelper;

/**
 * Importa los clientes faltantes en la central
 * 
 * @author Ruben Cancino
 *
 */
public class ImportadorCentralDeInventarios {
	
	protected Set<Long> sucursales=new HashSet<Long>();
	
	protected Logger logger=LoggerHelper.getLogger();
	
	protected List<ImportadorDeFaltantes> importadoresFaltantes;
	
	public ImportadorCentralDeInventarios(){
		importadoresFaltantes=new ArrayList<ImportadorDeFaltantes>();
	    importadoresFaltantes.add(new ImportadorCentralDeMovimientos());
		//importadoresFaltantes.add(new ImportadorCentralDeCompras());
		//importadoresFaltantes.add(new ReplicadorCentralDeCompras());
		importadoresFaltantes.add(new ImportadorCentralDeRecepcionDeCompras());
		importadoresFaltantes.add(new ImportadorCentralDeDevoluciuonDeCompras());
		importadoresFaltantes.add(new ImportadorCentralDeMaquilas());
		importadoresFaltantes.add(new ImportadorCentralDeDevolucionDeVenta());
	    importadoresFaltantes.add(new ImportadorCentralDeTransformaciones());		
		importadoresFaltantes.add(new ImportadorCentralDeSols());
		importadoresFaltantes.add(new ImportadorCentralDeTraslados());
		
		
	}
	
	public void importarFaltantes(String fechaIni,String fechaFin){
		Periodo periodo=new Periodo(fechaIni,fechaFin);
		List<Date> dias=periodo.getListaDeDias();
		for(Date dia:dias){
			importarFaltantes(dia);
		}
	}
	
	public void importarFaltantes(){
		importarFaltantes(new Date());
	}
	

	
	public void importarFaltantes(final String sfecha){
		importarFaltantes(DateUtil.toDate(sfecha));
	}
	
	public void importarFaltantes(final Date fecha){
		for(Long suc:sucursales){
			JdbcTemplate template=ConnectionServices.getInstance().getJdbcTemplate(suc);
			importarFaltantes(fecha,template,suc);
		}
	}
	
	ExportadorDeTraslados exportador=new ExportadorDeTraslados();
	ExportadorDeSols exportador2=new ExportadorDeSols();
	
	public void importarFaltantes(final Date fecha,JdbcTemplate template,Long sucursalId){
		for(ImportadorDeFaltantes importador:importadoresFaltantes){
			try {
				importador.importarFaltantes(fecha, template, sucursalId);
			} catch (Exception e) {
				logger.error("Error importando entidad en la sucursal: "+sucursalId+ " Importador: "+importador.getClass().getName()+ " Causa: "+ExceptionUtils.getRootCauseMessage(e),e);
			}
		}
		exportador2.exportar(fecha, sucursalId);
		exportador.exportar(fecha, sucursalId);
		
	}
	
	

	public void setSucursales(Set<Long> sucursales) {
		this.sucursales = sucursales;
	}
	
	public void addSucursal(Long...ids){
		sucursales.clear();
		for(Long id:ids){
			sucursales.add(id);
		}
	}
	
	public static void main(String[] args) {
		ImportadorCentralDeInventarios importador=new ImportadorCentralDeInventarios();
		importador.addSucursal(2l,3l,5L,6L,9L,11L);
	//	importador.addSucursal(11L);
	    importador.importarFaltantes("26/09/2013","30/09/2013");
	//	importador.importarFaltantes();
				
	}

}
