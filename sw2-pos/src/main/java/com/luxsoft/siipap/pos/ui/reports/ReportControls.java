package com.luxsoft.siipap.pos.ui.reports;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.swing.JComboBox;

import org.apache.commons.lang.StringUtils;
import org.springframework.jdbc.core.RowMapper;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.matchers.TextMatcherEditor;
import ca.odell.glazedlists.swing.AutoCompleteSupport;

import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.model.core.Linea;
import com.luxsoft.siipap.pos.ui.reports.ConteoSelectivoDeInventarioForm.Familia;
import com.luxsoft.sw3.services.Services;

/**
 * Facade para generar algunos controles
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class ReportControls {
	
	public static JComboBox createSucursalesBox(){
		JComboBox box=new JComboBox(Services.getInstance().getSucursalesOperativas().toArray(new Object[0]));
		return box;
	}
	
	public static JComboBox createSucursalesBox(Long local){
		List<Sucursal> sucs=Services.getInstance().getSucursalesOperativas();
		int index=0;
		for(int i=0;i<sucs.size();i++){
			Sucursal s=sucs.get(i);
			if(s.getId()==local){
				index=i;
				break;
			}
		}
		if(index!=0){
			Sucursal target=sucs.get(index);
			sucs.set(index, target);
		}
		JComboBox box=new JComboBox(Services.getInstance().getSucursalesOperativas().toArray(new Object[0]));
		return box;
	}
	
	public static JComboBox createLineasBox(){
		return createHQLCombo("from Linea l order by l.nombre", new String[]{"nombre"});
	}
	
	public static JComboBox createClaseBox(){
		return createHQLCombo("from Clase l order by l.nombre", new String[]{"nombre"});
	}
	
	public static JComboBox createFamiliasBox(){
		List<Familia> fams=Services.getInstance().getJdbcTemplate().query(
				"select * from sx_familias  order by clave", new RowMapper(){

			public Object mapRow(ResultSet rs, int rowNum)throws SQLException {
				String clave=rs.getString("CLAVE");
				String nombre=rs.getString("NOMBRE");
				String tipo=rs.getString("TIPO");
				String bloque=rs.getString("BLOQUE");
				return new Familia(clave,StringUtils.deleteWhitespace(nombre),tipo,bloque);
			}
			
		});
		return createCombo(fams, new String[]{"clave","nombre"});
	}
	
	public static JComboBox createHQLCombo(String hql,String[] filteProperties,Object...params){
		final JComboBox box = new JComboBox();
		List data=Services.getInstance().getHibernateTemplate().find(hql, params);
		final EventList source = GlazedLists.eventList(data);
		final TextFilterator filterator = GlazedLists.textFilterator(filteProperties);
		AutoCompleteSupport support = AutoCompleteSupport.install(box,source, filterator);
		support.setFilterMode(TextMatcherEditor.STARTS_WITH);
		support.setCorrectsCase(true);
		return box;
	}
	
	public static JComboBox createEntityCombo(Class clazz,String[] filteProperties,Object...params){
		final JComboBox box = new JComboBox();
		List data=Services.getInstance().getUniversalDao().getAll(clazz);
		final EventList source = GlazedLists.eventList(data);
		final TextFilterator filterator = GlazedLists.textFilterator(filteProperties);
		AutoCompleteSupport support = AutoCompleteSupport.install(box,source, filterator);
		support.setFilterMode(TextMatcherEditor.STARTS_WITH);
		support.setCorrectsCase(true);
		return box;
	}
	
	public static JComboBox createCombo(List data,String[] filteProperties){
		final JComboBox box = new JComboBox();
		final EventList source = GlazedLists.eventList(data);
		final TextFilterator filterator = GlazedLists.textFilterator(filteProperties);
		AutoCompleteSupport support = AutoCompleteSupport.install(box,source, filterator);
		support.setFilterMode(TextMatcherEditor.STARTS_WITH);
		support.setCorrectsCase(true);
		return box;
	}

}
