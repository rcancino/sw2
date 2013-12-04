package com.luxsoft.sw3.replica.parches;

import java.sql.Types;
import java.util.List;

import org.springframework.jdbc.core.SqlParameterValue;

import com.luxsoft.sw3.replica.ReplicaServices;
import com.luxsoft.sw3.services.Services;
import com.luxsoft.sw3.tesoreria.model.SolicitudDeDeposito;

public class ReparaFechaDepositoCalle4 {
	
	public void execute(){
		
		String[] ids={"8a8a8584-2add1789-012a-dd5f95b5-0004",
				"8a8a8584-2add1789-012a-dd604469-0005",
				"8a8a8584-2ba5eefe-012b-a6a7568b-0020",
				"8a8a8584-2bee136d-012b-ee252a08-0001",
				"8a8a8584-2bee5b51-012b-eecbc78f-0025",
				"8a8a8585-29a7f27c-0129-a84d45fa-0023",
				"8a8a8585-29bd34fd-0129-bd96294a-0013",
				"8a8a8585-29c8153f-0129-c85c0a8e-003a",
				"8a8a8585-29f02840-0129-f044db02-0019",
				"8a8a8585-29f5e49a-0129-f66b9660-0024",
				"8a8a8585-2a33e503-012a-33eef276-0006",
				"8a8a8585-2a39952c-012a-3a1cc5b8-0036",
				"8a8a8585-2a3d67e9-012a-3da09a2c-003a",
				"8a8a8585-2a3d67e9-012a-3db871ab-0044",
				"8a8a8585-2a42fcfb-012a-43117665-0005",
				"8a8a8585-2a7c9a71-012a-7ce870a9-001f",
				"8a8a8585-2a804bd9-012a-80e7e1c9-004b",
				"8a8a8585-2a86899c-012a-87283358-001e",
				"8a8a8585-2a8fe0e2-012a-90258144-0050",
				"8a8a8585-2ad330db-012a-d366b9f9-0010",
				"8a8a8585-2ad330db-012a-d385f11e-0022",
				"8a8a8585-2ad96b7f-012a-d97b33fc-0009",
				"8a8a8585-2ad96b7f-012a-d980570a-000c",
				"8a8a8585-2aec891f-012a-eca1794a-0016",
				"8a8a8585-2b164a00-012b-16585d8b-0013",
				"8a8a8585-2b164a00-012b-16aa78da-003d",
				"8a8a8585-2b49ab6a-012b-49b5de16-0009",
				"8a8a8585-2b8c7a86-012b-8cc04eb1-001a",
				"8a8a8585-2b9d33b8-012b-9d44f76a-0004",
				"8a8a8585-2bca56b5-012b-ca582dd3-0002",
				"8a8a8585-2bee0aaa-012b-ee4547c1-002a",
				"8a8a8585-2bee76b1-012b-ee8a15b3-0018",
				"8a8a8587-29a2ea8f-0129-a2fd230b-0003",
				"8a8a8588-2a195bd4-012a-19f7cab8-0016",
				"8a8a8588-2ac3c29c-012a-c4b36e42-0050",
				"8a8a8588-2ac87586-012a-c8dc3bf9-0011",
				"8a8a8588-2af1ecff-012a-f31116f6-0043",
				"8a8a8589-2ad29f6b-012a-d2a0ed54-0002",
				"8a8a858a-2ad2944d-012a-d34e5df4-0113",
				"8a8a858a-2ad7e829-012a-d875b6e1-00b2",
				"8a8a858a-2b9b77ba-012b-9ca3dd74-019c",
				"8a8a858c-29b24137-0129-b3462882-0018",
				"8a8a858c-29b7816f-0129-b7f89af4-0010",
				"8a8a858c-29fac54b-0129-fbd34cbe-0011",
				"8a8a858c-2b91928e-012b-91b25616-0006",
				"8a8a858d-29ad4554-0129-ae4e05e4-0035",
				"8a8a858d-29b2666d-0129-b43a4c10-0054",
				"8a8a858d-29d11fb0-0129-d33b65d9-004c",
				"8a8a858d-29db8443-0129-dcb78aec-006c",
				"8a8a858d-29db8443-0129-dd17cc1b-0087",
				"8a8a858d-29db8443-0129-dd261a45-008c",
				"8a8a858d-2a1547ae-012a-1578031b-0015",
				"8a8a858d-2a298da7-012a-299df56c-0004",
				"8a8a858d-2a7b1de6-012a-7bfcb007-001d",
				"8a8a858d-2a7b1de6-012a-7bfd98a9-001e",
				"8a8a858d-2aa4702b-012a-a491f412-0007",
				"8a8a858d-2aa99f0c-012a-aa20fedc-0022",
				"8a8a858d-2af75f6c-012a-f76fb427-0004",
				"8a8a858d-2b401847-012b-405d5132-0017",
				"8a8a858d-2b779f0c-012b-77f78d64-0014",
				"8a8a858d-2bee9582-012b-eea80cfd-0005"};
		for(String id:ids){
			SolicitudDeDeposito sol=(SolicitudDeDeposito)Services.getInstance().getHibernateTemplate().get(SolicitudDeDeposito.class, id);
			String sql="UPDATE SX_SOLICITUDES_DEPOSITO SET FECHA_DEPOSITO=? WHERE SOL_ID=?";
			Object[] args={
					new SqlParameterValue(Types.DATE,sol.getFechaDeposito())
					,new SqlParameterValue(Types.VARCHAR,sol.getId())
			};
			int res=ReplicaServices.getInstance().getJdbcTemplate(2L).update(sql, args);
			System.out.println("Actualizo: "+id+" rows: "+res);
		}
		
	}
	
	public static void main(String[] args) {
		new ReparaFechaDepositoCalle4().execute();
	}

}
