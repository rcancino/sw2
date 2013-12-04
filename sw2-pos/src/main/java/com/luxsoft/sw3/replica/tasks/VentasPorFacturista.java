package com.luxsoft.sw3.replica.tasks;

import java.sql.Types;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlParameterValue;

import com.luxsoft.siipap.model.Periodo;

import com.luxsoft.siipap.util.SQLUtils;
import com.luxsoft.sw3.replica.ReplicaServices;
import com.luxsoft.sw3.services.POSDBUtils;
import com.luxsoft.sw3.services.Services;
import com.luxsoft.utils.LoggerHelper;

/**
 * Actualiza la tabla de ventas por facturista en el repositorio central 
 * 
 * @author Ruben Cancino Ramos
 *
 */

public class VentasPorFacturista {
	
static Logger logger=LoggerHelper.getLogger();
	
	private DateFormat df=new SimpleDateFormat("yyyy-MM-dd");
	
	String[] COLS={"FECHA","SUCURSAL_ID","CREADO_USR","FACTURISTA","PED","MOS","CAM","CRE","CANC","FACS","IMPORTE","PART"};
	
	private Set<Long> sucursales=new HashSet<Long>();
	
	public VentasPorFacturista(){
		
	}
	
	public void execute() {
		actualizar();
		
	}
	
	public void actualizar(){
		
		Periodo periodo=Periodo.periodoDeloquevaDelMes();
		limpiar(periodo);
		actualizar(periodo);
	}
	
	public void actualizar(final Periodo periodo){
		logger.info("Actualizando ventas por facturista periodo: "+periodo);
		JdbcTemplate target=Services.getInstance().getJdbcTemplate();
		
		for(Long sucursalId:getSucursales()){
			String sql=SQLUtils.loadSQLQueryFromResource("sql/ventas_x_facturista.sql");
			sql=sql.replaceAll("@FECHA_INI","\'"+df.format(periodo.getFechaInicial())+"\'");
			sql=sql.replaceAll("@FECHA_FIN","\'"+df.format(periodo.getFechaFinal())+"\'");
			logger.info("Actualizando sucursal: "+sucursalId);
			sql=sql.replaceAll("@SUCURSAL_ID", sucursalId.toString());
			
			JdbcTemplate source=ReplicaServices.getInstance().getJdbcTemplate(sucursalId);
			List<Map<String, Object>> rows=source.queryForList(sql);
			logger.info("Registros: "+rows.size());
			
			String INSERT="INSERT INTO SX_VENTAS_FACTURISTA " +
					"(FECHA" +
					",SUCURSAL_ID" +
					",CREADO_USR" +
					",FACTURISTA" +
					",PED" +
					",MOS" +
					",CAM" +
					",CRE" +
					",CANC" +
					",FACS" +
					",IMPORTE" +
					",PART)" +
					" VALUES(?,?,?,?,?,?,?,?,?,?,?,?)";
			
			
			for(Map<String, Object> row:rows){
				Object[] params={
						row.get("FECHA")
						,row.get("SUCURSAL_ID")
						,row.get("CREADO_USR")
						,row.get("FACTURISTA")
						,row.get("PED")						
						,row.get("MOS")
						,row.get("CAM")
						,row.get("CRE")
						,row.get("CANC")
						,row.get("FACS")
						,row.get("IMPORTE")
						,row.get("PART")
						};
				target.update(INSERT, params);
				System.out.println("Registro insertado: "+row);
			}
			
		}
	}
	
	public void limpiar(final Periodo periodo){
		
		String DELETE="DELETE FROM SX_VENTAS_FACTURISTA WHERE DATE(FECHA) BETWEEN ? AND ?";
		Services.getInstance().getJdbcTemplate()
			.update(DELETE, new Object[]{
					new SqlParameterValue(Types.DATE,periodo.getFechaInicial())
					,new SqlParameterValue(Types.DATE,periodo.getFechaFinal())
					});
				
	}

	public Set<Long> getSucursales() {
		return sucursales;
	}

	public void setSucursales(Set<Long> sucursales) {
		this.sucursales = sucursales;
	}
	
	public VentasPorFacturista addSucursal(Long... sucursales){
		for (Long sucursalId:sucursales){
			getSucursales().add(sucursalId);
		}
		return this;
	}
	
	
	public static void main(String[] args) {
		POSDBUtils.whereWeAre();
		final VentasPorFacturista task=new VentasPorFacturista();
		task.addSucursal(2L,3L,6L,5L);
		task.actualizar(new Periodo("01/08/2011","01/08/2011"));
		//task.actualizar();
		/*Periodo periodo=new Periodo("01/07/2011","08/07/2011");
		System.out.println("la fechaini"+periodo.getFechaInicial());
		
		task.limpiar(periodo);*/
		/*List<Periodo> lista=Periodo.periodosMensuales(periodo);
		for(Periodo p:lista){
			System.out.println("Ejecutando periodo: "+p);
			task.actualizar(p);
		}*/
		//task.limpiar(periodo);
		task.actualizar();
		
		
	}

}
