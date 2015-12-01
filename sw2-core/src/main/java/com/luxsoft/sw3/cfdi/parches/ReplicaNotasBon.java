package com.luxsoft.sw3.cfdi.parches;

import java.io.File;
import java.io.FileOutputStream;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.bouncycastle.util.encoders.Base64;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.util.Assert;

import com.edicom.ediwinws.cfdi.client.CfdiClient;
import com.edicom.ediwinws.service.cfdi.CancelaResponse;
import com.luxsoft.siipap.model.Empresa;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.util.DBUtils;
import com.luxsoft.sw3.cfdi.model.CFDI;

/**
 * Parche para realizar la replica de notas de bonificacion y su autorizacion.
 * 
 * @author Luis Quintanilla
 *
 */
public class ReplicaNotasBon {
	
	CfdiClient client;
	Empresa empresa;
	
	
	
	
	public void validacion (){
		
		DBUtils.whereWeAre();
		
		
		
		/*String sql="SELECT a.ABONO_ID,CAR_SUCURSAL,A.AUTORIZACION_ID FROM sx_cxc_abonos a join sx_cxc_aplicaciones p on (a.ABONO_ID=p.ABONO_ID)"+
				" where TO_DAYS(a.saf)= (TO_DAYS(CURRENT_DATE)-(case when DAYOFWEEK(CURRENT_DATE)=2 then 2 else 1 end))"+
				" and tipo_id='nota_bon' and origen='cam' group by a.ABONO_ID ";*/
		
		String sql="SELECT a.ABONO_ID,CAR_SUCURSAL,A.AUTORIZACION_ID FROM sx_cxc_abonos a join sx_cxc_aplicaciones p on (a.ABONO_ID=p.ABONO_ID)"+
				" where a.saf=CURRENT_DATE"+
				" and tipo_id='nota_bon' and origen='cam' group by a.ABONO_ID ";
			
		
		
		List <Map<Object, String>> rows=ServiceLocator2.getJdbcTemplate().queryForList(sql);
		
		
		for(Map<Object,String> id: rows){
		
			System.out.println("Abono_id:"+id.get("ABONO_ID")+" Sucursal: "+ id.get("CAR_SUCURSAL")+" AUTORIZACION: "+id.get("AUTORIZACION_ID"));
			String sql1="INSERT INTO audit_log "+
                        " (entityId,entityName,action,tableName,ip,SUCURSAL_ORIGEN,SUCURSAL_DESTINO,dateCreated,lastUpdated,replicado,message,version)"+ 
                        " VALUES"+ 
                        " (?,'AutorizacionDeAbono','INSERT','SX_AUTORIZACIONES2','10.10.1.0','OFICINAS',?,NOW(),NOW(),null,null,1);";
			
			if(id.get("AUTORIZACION_ID")!= null){
				System.out.println("Generando AuditLog para: "+id.get("AUTORIZACION_ID"));
				Object[] args1={id.get("AUTORIZACION_ID"),id.get("CAR_SUCURSAL")};
				ServiceLocator2.getJdbcTemplate().update(sql1, args1);
			}
			
			
			String sql2="INSERT INTO audit_log "+
					" (entityId,entityName,action,tableName,ip,SUCURSAL_ORIGEN,SUCURSAL_DESTINO,dateCreated,lastUpdated,replicado,message,version) "+
					" VALUES "+
					"(?,'NotaDeCreditoBonificacion','INSERT','SX_CXC_ABONOS','10.10.1.0','OFICINAS',?,now(),now(),null,null,0)";
			
			Object[] args2={id.get("ABONO_ID"),id.get("CAR_SUCURSAL")};
			 
			ServiceLocator2.getJdbcTemplate().update(sql2, args2);
			
			
		}
	
		
		
		System.out.println("Notas Por Replicar: : "+rows.size());
		
		JOptionPane.showMessageDialog(null, "El envio de aplicacion de notas se ha completado.");
		
	}
	
	
	

	
	public static void main(String[] args) {
		ReplicaNotasBon task=new ReplicaNotasBon();
		task.validacion();
	}

}
