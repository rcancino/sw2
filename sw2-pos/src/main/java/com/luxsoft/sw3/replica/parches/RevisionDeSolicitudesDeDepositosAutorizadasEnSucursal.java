package com.luxsoft.sw3.replica.parches;

import java.sql.Types;
import java.util.Date;
import java.util.List;

import org.hibernate.ReplicationMode;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.orm.hibernate3.HibernateTemplate;

import com.luxsoft.siipap.cxc.model.Pago;
import com.luxsoft.siipap.cxc.model.PagoConDeposito;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.sw3.replica.ReplicaServices;
import com.luxsoft.sw3.services.Services;
import com.luxsoft.sw3.tesoreria.model.SolicitudDeDeposito;

/**
 * Baja cambios de datos en la entidad de sucursales de Produccion a las sucursales
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class RevisionDeSolicitudesDeDepositosAutorizadasEnSucursal {
	
	
	public void execute(String sfecha,Long... sucursales){
		final Date fecha=DateUtil.toDate(sfecha);
		
		for(Long suc:sucursales){
			
			try {
				HibernateTemplate source=ReplicaServices.getInstance().getHibernateTemplate(suc);
				List<SolicitudDeDeposito> solsSucursal=source.find("from SolicitudDeDeposito s where date(s.fecha)=?",fecha);
				for(SolicitudDeDeposito solSucursal:solsSucursal){
					SolicitudDeDeposito solOficinas= (SolicitudDeDeposito)Services.getInstance().getHibernateTemplate().get(SolicitudDeDeposito.class, solSucursal.getId());
					if(solOficinas==null){						
						importar(solSucursal,source);
						System.out.println("Solicitud faltante: "+solSucursal+ " Importada en oficinas");
					}else{
						//Comparar pago
						if(solSucursal.getPago()!=null){
							if(solOficinas.getPago()==null){
								//importar(solSucursal, source);
								System.out.println("Solicitud existente pero falta el pago generado en la sucursal.  Sol: " +solSucursal.getId());
							}else{
								PagoConDeposito depositoSucursal=solSucursal.getPago();
								PagoConDeposito depositoOficina=solOficinas.getPago();
								if(!depositoSucursal.getId().equals(depositoOficina.getId())){
									System.out.println("ERROR Solicitud con pago generado diferente en sucursal y oficinas Sol.id:"+solSucursal.getId());
								}
							}
						}
					}
					
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
	}
	
	private void importar(SolicitudDeDeposito sol,HibernateTemplate source){
		System.out.println("Importando sol: "+sol.getDocumento()+ " Id:"+sol.getId());
		sol.setImportado(new Date());
		sol.setReplicado(new Date());
		Services.getInstance().getHibernateTemplate().replicate(sol, ReplicationMode.OVERWRITE);
		source.merge(sol);
		System.out.println("Solicitud importada: "+sol);
	}
	
	private void actualizarImportado(SolicitudDeDeposito sol){
		String UPDATE="UPDATE sx_solicitudes_deposito SET TX_REPLICADO=? where SOL_ID=?";
		SqlParameterValue p1=new SqlParameterValue(Types.TIMESTAMP,sol.getReplicado());
		SqlParameterValue p2=new SqlParameterValue(Types.VARCHAR,sol.getId());
		Services.getInstance().getJdbcTemplate().update(UPDATE,new Object[]{p1,p2});
	}
	
	public static void main(String[] args) {
		new RevisionDeSolicitudesDeDepositosAutorizadasEnSucursal().execute("03/04/2012",3L);
	}

}
