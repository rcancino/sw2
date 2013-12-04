package com.luxsoft.sw2.server.ui.consultas;





import java.sql.Types;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.SqlParameterValue;

import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.core.ClienteRow2;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.utils.Renderers;

/**
 * Panel para generar mensajes JMS de replica en el servidor de la sucursal
 * 
 * @author Ruben Cancino
 *
 */
public class ClientesReplicaSucursalPanel extends DefaultSucursalReplicaPanel<ClienteRow2>{
	
	private String sucursalNombre;

	public ClientesReplicaSucursalPanel() {
		super(ClienteRow2.class);	
		setTitle("Clientes");
	}
	
	@Override
	protected void init() {
		addProperty("clave","nombre","creado","modificado","importado","cliente_id");
		addLabels("Clave","Nombre","Creado","Modificado","Importado","Id");
	}
	
	@Override
	protected void manejarPeriodo() {
		periodo=Periodo.periodoDeloquevaDelYear();
	}
	@Override
	protected void adjustMainGrid(JXTable grid) {
		grid.getColumnExt("Creado").setCellRenderer(new DefaultTableRenderer(new Renderers.ToHourConverter()));
		grid.getColumnExt("Importado").setCellRenderer(new DefaultTableRenderer(new Renderers.ToHourConverter()));
		grid.getColumnExt("Modificado").setCellRenderer(new DefaultTableRenderer(new Renderers.ToHourConverter()));
	}
	
	@Override
	protected List findData() {
		String sql="select clave,cliente_id,nombre,creado,modificado from sx_clientes where clave like ?  and DATE(creado)  between ? and ? order by creado desc";
		String sucPart=StringUtils.substring(getSucursalNombre(), 0, 2);
		SqlParameterValue p1=new SqlParameterValue(Types.VARCHAR, sucPart+"%");
		SqlParameterValue p2=new SqlParameterValue(Types.DATE,periodo.getFechaInicial());
		SqlParameterValue p3=new SqlParameterValue(Types.DATE,periodo.getFechaFinal());
		return ServiceLocator2.getJdbcTemplate().query(sql
				, new Object[]{p1,p2,p3}
				, new BeanPropertyRowMapper(ClienteRow2.class));
	}

	public String getSucursalNombre() {
		return sucursalNombre;
	}

	public void setSucursalNombre(String sucursalNombre) {
		this.sucursalNombre = sucursalNombre;
	}
	
	

	
	
}
