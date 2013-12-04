package com.luxsoft.siipap.inventario.ui.consultas;

import java.util.List;

import org.springframework.jdbc.core.BeanPropertyRowMapper;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;

import com.luxsoft.siipap.model.core.Comentario;
import com.luxsoft.siipap.model.core.Producto;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.siipap.swx.catalogos.ProductoFormModel.Familia;

public class ProductoParaInventarioFormModel extends DefaultFormModel{
	
	
	private EventList<Comentario> comentarios;
	

	public ProductoParaInventarioFormModel() {
		super(Producto.class);
	}

	public ProductoParaInventarioFormModel(Object bean, boolean readOnly) {
		super(bean, readOnly);
	}

	public ProductoParaInventarioFormModel(Object bean) {
		super(bean);
	}
	
	protected void init(){
		comentarios=GlazedLists.eventList(new BasicEventList<Comentario>());
	}
	
	protected Producto getProducto(){
		return (Producto)getBaseBean();
	}
	
	public EventList<Comentario> getComentarios(){
		return GlazedLists.readOnlyList(comentarios);
	}
	
	/**
	 * Servicio temporal para accesar las familias de SIIPAP
	 * 
	 * @return
	 */
	public List<Familia> getFamilias(){
		List<Familia> fams=ServiceLocator2.getJdbcTemplate().query("select clave,nombre from sx_familias  order by clave", new BeanPropertyRowMapper(Familia.class));
		return fams;
	}
	
	

}
