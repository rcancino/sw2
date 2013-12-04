package com.luxsoft.sw2.replica.valida;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;

import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.utils.LoggerHelper;

/**
 * Importa los clientes faltantes en la central
 * 
 * @author Ruben Cancino
 *
 */
public class ImportadorCentralCXC {
	
	protected Set<Long> sucursales=new HashSet<Long>();
	
	protected Logger logger=LoggerHelper.getLogger();
	
	protected List<ImportadorDeFaltantes> importadoresFaltantes;
	
	public ImportadorCentralCXC(){
		importadoresFaltantes=new ArrayList<ImportadorDeFaltantes>();
	  // importadoresFaltantes.add(new ImportadorCentralDeClientes());
		//importadoresFaltantes.add(new ImportadorCentralDeCFDs());
		importadoresFaltantes.add(new ImportadorCentralDeVentas());
	   // importadoresFaltantes.add(new ImportadorCentralDeSolicitudesDeDepositos());
		//importadoresFaltantes.add(new ImportadorCentralDeAbonos());
		//importadoresFaltantes.add(new ImportadorCentralDeEmbarques());
		//importadoresFaltantes.add(new ImportadorCentralDeFichas());
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
	
	public void importarFaltantes(final Date fecha){
		
		for(Long suc:sucursales){
			JdbcTemplate template=ConnectionServices.getInstance().getJdbcTemplate(suc);
			importarFaltantes(fecha,template,suc);
		}
	}
	
	public void importarFaltantes(final Date fecha,JdbcTemplate template,Long sucursalId){
		for(ImportadorDeFaltantes importador:importadoresFaltantes){
			try {
				importador.importarFaltantes(fecha, template, sucursalId);
			} catch (Exception e) {
				logger.error("Error importando entidad en la sucursal: "+sucursalId+ " Importador: "+importador.getClass().getName()+ " Causa: "+ExceptionUtils.getRootCauseMessage(e),e);
			}
		}
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
		ImportadorCentralCXC importador=new ImportadorCentralCXC();
	//	importador.addSucursal(9l);
		importador.addSucursal(9L);
		importador.importarFaltantes(DateUtil.toDate("20/09/2013"));
	//	importador.importarFaltantes();

	}

}
