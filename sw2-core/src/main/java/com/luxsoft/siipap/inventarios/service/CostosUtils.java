package com.luxsoft.siipap.inventarios.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Types;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.math.NumberUtils;
import org.springframework.jdbc.core.SqlParameterValue;

import com.luxsoft.siipap.inventarios.model.CostoPromedio;
import com.luxsoft.siipap.model.core.Producto;
import com.luxsoft.siipap.service.ServiceLocator2;


/**
 * Metodos utilies en el manejo de ultimos costos
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class CostosUtils {
	
	/**
	 * Regresa un map con el ultimo costo del producto solicitado
	 * 
	 * @param clave
	 * @return
	 */
	public static Map<String, Object> buscarUltimoCostoEnOracle(final String clave){
		String sql="select b.FECHA as FECHA,a.ARTCLAVE" +
			",a.neto*b.tc as NETO, b.tc as TC ,b.FACTURA" +
			" from SW_ANALISISDET a join SW_ANALISIS b on(a.ANALISIS_ID=b.ANALISIS_ID) " +
			" where a.ARTCLAVE=? order by b.fecha desc";
		List<Map<String, Object>> data=ServiceLocator2.getAnalisisJdbcTemplate()
		.queryForList(sql,new Object[]{clave});
		return data.isEmpty()?null:data.get(0);
	}
	
	
	public static Map<String, Object> buscarUltimoCosto(final String clave,final Date fecha){
		
		String sql="select b.fecha,b.clave,x.costo*y.tc as COSTO ,y.fecha,y.documento as FACTURA " +
				"from sx_cxp_analisisdet x " +
				" join sx_cxp y on(x.CXP_ID=y.CXP_ID) " +
				" join sx_inventario_com b on(x.ENTRADA_ID=b.INVENTARIO_ID) " +
				" where b.clave=? and b.fecha<=?" +
				" order by b.fecha desc";
		SqlParameterValue p1=new SqlParameterValue(Types.VARCHAR,clave);
		SqlParameterValue p2=new SqlParameterValue(Types.DATE,fecha);
		List<Map<String, Object>> rows=ServiceLocator2.getJdbcTemplate()
			.queryForList(sql, new Object[]{p1,p2});
		
		return rows.isEmpty()?null:rows.get(0);		
	}
	
	/**
	 * Procedimiento temporal para  actualizar algunos productos pendientes 
	 * de costo ultimo y suministrados mediante un archivo separado por comas
	 * 
	 * @throws IOException
	 */
	public static void importPendientes() throws IOException{
		//FileInputStream is=new FileInputStream();
		File file=new File("C:\\LUXSOFTNET\\faltantes.csv");
		FileReader reader=new FileReader(file);
		BufferedReader br=new BufferedReader(reader);
		String line=br.readLine();
		while(line!=null){
			line=br.readLine();
			if(line==null)
				break;
			
			line.replaceAll("\"", "");
			String[] data=line.split(",");
			int mes=Integer.valueOf(data[0]);
			String clave=data[1];
			Number costo=NumberUtils.toDouble(data[2]);
			System.out.println("Procesando: "+mes+" "+clave+" "+costo);
			CostoPromedio cp=buscarCosto(2009, mes, clave);	
			if(cp==null){
				Producto prod=ServiceLocator2.getProductoManager().buscarPorClave(clave);
				cp=new CostoPromedio(2009,mes,prod);
				cp.setCostop(BigDecimal.valueOf(costo.doubleValue()));
				System.out.println("Costo promedio generado: "+cp);
			}
			cp.setCostoUltimo(BigDecimal.valueOf(costo.doubleValue()));
			cp=ServiceLocator2.getCostoPromedioManager().save(cp);
			//System.out.println(MessageFormat.format("{0},{1},{2}", mes,clave,costo)+" "+cp);
		}
	}
	
	private static CostoPromedio buscarCosto(int year,int mes,String clave){
		String hql="from CostoPromedio p where p.year=2009 and p.mes=? and p.clave=?";
		List<CostoPromedio> data=ServiceLocator2.getHibernateTemplate().find(hql,new Object[]{mes,clave});
		return data.isEmpty()?null:data.get(0);
	}
	
	public static void main(String[] args) throws IOException {
		//System.out.println("CAP7811314 MySql  : "+buscarUltimoCosto("CAP7811314",DateUtil.toDate("31/01/2009")));
		//System.out.println("CAP7811314 Oracle : "+buscarUltimoCostoEnOracle("CAP7811314"));
		//DBUtils.whereWeAre();
		importPendientes();
	}

}
