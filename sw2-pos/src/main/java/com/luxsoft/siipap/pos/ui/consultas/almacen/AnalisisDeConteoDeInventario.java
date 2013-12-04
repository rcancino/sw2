package com.luxsoft.siipap.pos.ui.consultas.almacen;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.swing.Action;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.text.NumberFormatter;

import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.matchers.AbstractMatcherEditor;
import ca.odell.glazedlists.matchers.Matcher;

import com.luxsoft.siipap.inventarios.model.ExistenciaConteo;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.pos.ui.reports.DiferenciasConteoReportForm;
import com.luxsoft.siipap.swing.browser.FilteredBrowserPanel;
import com.luxsoft.siipap.swing.matchers.CheckBoxMatcher;
import com.luxsoft.siipap.swing.reports.ReportUtils;
import com.luxsoft.sw3.services.Services;

public class AnalisisDeConteoDeInventario extends FilteredBrowserPanel<ExistenciaConteo>{

	public AnalisisDeConteoDeInventario() {
		super(ExistenciaConteo.class);
	}
	
	CheckBoxMatcher<ExistenciaConteo> contadosMatcher;
	CheckBoxMatcher<ExistenciaConteo> noContadosMatcher;
	
	
	private JTextField sucursalField;
	private JTextField lineaField;
	private JTextField claseField;
	private JTextField marcaField;
	private JTextField sectorField;
	
	
	private DateFormat df;
	
	public void init(){
		addProperty(
				"fecha","sucursal.nombre","clave","descripcion"
				,"producto.kilos"
				,"producto.gramos"
				,"producto.unidad.unidad","existencia"
				,"conteoEnLinea"
				,"conteo"
				,"diferencia"
				,"ajuste"
				,"existenciaFinal"
				,"sectorEnLinea"
				,"sectores"
				,"fijado"
				,"producto.linea.nombre"
				,"producto.clase.nombre"
				,"producto.marca.nombre"
				);
		addLabels("Fecha","Suc","Producto","Descripcion","Kg"
				,"g","U","Exis","Conteo","Conteo F."
				,"Dif","Ajuste","Exis final","Sector","Sectores"
				,"Fijado","Línea","Clase","Marca"
				);
		sucursalField=new JTextField();
		lineaField=new JTextField();
		claseField=new JTextField();
		marcaField=new JTextField();
		
		
		installTextComponentMatcherEditor("Sucursal",sucursalField, "sucursal.nombre");
		installTextComponentMatcherEditor("Línea", lineaField, "producto.linea.nombre");
		installTextComponentMatcherEditor("Clase", claseField, "producto.clase.nombre");
		installTextComponentMatcherEditor("Marca", marcaField, "producto.marca.nombre");
		installTextComponentMatcherEditor("Clave", "clave","descripcion");
		installTextComponentMatcherEditor("Sectores", "sectores");
		sectorField=new JTextField();
		installTextComponentMatcherEditor("Sector en Linea",sectorField, "sectorEnLinea");
		contadosMatcher=new CheckBoxMatcher<ExistenciaConteo>(false) {			
			protected Matcher<ExistenciaConteo> getSelectMatcher(Object... obj) {				
				return new Matcher<ExistenciaConteo>() {					
					public boolean matches(ExistenciaConteo item) {
						return item.getConteoEnLinea()!=0;
					}					
				};
			}
		};
		installCustomMatcherEditor("Contados", contadosMatcher.getBox(), contadosMatcher);
		
		noContadosMatcher=new CheckBoxMatcher<ExistenciaConteo>(false) {			
			protected Matcher<ExistenciaConteo> getSelectMatcher(Object... obj) {				
				return new Matcher<ExistenciaConteo>() {					
					public boolean matches(ExistenciaConteo item) {
						return item.getConteoEnLinea()==0;
					}					
				};
			}
		};
		installCustomMatcherEditor("No Contados", noContadosMatcher.getBox(), noContadosMatcher);
		NumberFormat format=NumberFormat.getNumberInstance();
		format.setGroupingUsed(false);
		
		NumberFormatter formatter=new NumberFormatter(format);
		formatter.setCommitsOnValidEdit(true);
		formatter.setValueClass(Double.class);
		
		formatter.setAllowsInvalid(true);
		
		df=new SimpleDateFormat("dd/MM/yyyy");
		TextFilterator<ExistenciaConteo> fechaFilterator=new TextFilterator<ExistenciaConteo>(){
			public void getFilterStrings(List<String> baseList, ExistenciaConteo element) {
				baseList.add(df.format(element.getFijado()));
			}			
		};
		JTextField fechaField=new JTextField(12);
		installTextComponentMatcherEditor("Fijado ", fechaFilterator,fechaField);
		
		
		CantidadMatcherEditor editor=new CantidadMatcherEditor();
		installCustomMatcherEditor("Existencia", editor.getSelector(), editor);
		manejarPeriodo();
	}
	
	@Override
	protected void manejarPeriodo() {
		periodo=Periodo.getPeriodo(-7);
	}
	
	
	protected List<Action> createProccessActions() {
		List<Action> procesos=new ArrayList<Action>();
		procesos.add(addAction("", "reporteDeDiferenciasConteo", "Rep Diferencias De Inventario"));
		procesos.add(addAction("", "reporteConteoSelectivo", "Rep Conteo selectivo"));
		
		return procesos;
	}

	
	public void reporteDeDiferenciasConteo(){
		DiferenciasConteoReportForm.run();
	}
	
	public void reporteConteoSelectivo(){
		java.util.Map map=new HashMap();
		map.put("FECHA_INI", periodo.getFechaInicial());
		map.put("FECHA_FIN", periodo.getFechaFinal());
		map.put("SUCURSAL", sucursalField.getText());
		map.put("LINEA", lineaField.getText());
		map.put("CLASE", claseField.getText());
		map.put("MARCA", marcaField.getText());
		map.put("SECTOR", sectorField.getText());
		ReportUtils.viewReport(ReportUtils.toReportesPath("invent/ConteoSelectivo2.jasper"), map,grid.getModel());
	}
	
	
	Date fecha;
	
	
	protected void beforeLoad(){
		//fecha=SelectorDeFecha.seleccionar();
		//if(fecha!=null)		
			super.beforeLoad();
		
	}
	
	@Override
	protected String getDeleteMessage(ExistenciaConteo bean) {
		String pattern="Eliminar:  {0} ({1}) existencia:{2} conteo: {3}";
		return MessageFormat.format(pattern, bean.getClave(),bean.getDescripcion(),bean.getExistencia(),bean.getConteoEnLinea());
	}
	
	@Override
	public boolean doDelete(ExistenciaConteo bean) {
		if(bean.getConteoEnLinea()<=0 || bean.getConteo()<=0){
			Services.getInstance().getHibernateTemplate().delete(bean);
		}
		return false;
	}
	

	@Override
	protected List<ExistenciaConteo> findData() {		
		String hql="from ExistenciaConteo e where e.fecha between ? and ? order by e.fecha";
		return Services.getInstance().getHibernateTemplate().find(hql,new Object[]{periodo.getFechaInicial(),periodo.getFechaFinal()});
	}

	static class CantidadMatcherEditor extends AbstractMatcherEditor implements ActionListener{
		private JComboBox selector;
		
		public CantidadMatcherEditor() {
			selector=new JComboBox(new String[]{"TODOS","POSITIVOS","NEGATIVOS","CERO","DIF CERO"});
			selector.getModel().setSelectedItem("Seleccion por cantidad");
			selector.addActionListener(this);
		}
		
		public void actionPerformed(ActionEvent e) {
			final String tipo=(String)this.selector.getSelectedItem();
			System.out.println("Seleccion: "+tipo);
			if(tipo==null || "TODOS".equals(tipo))
				fireMatchAll();
			else if("POSITIVOS".equals(tipo)){
				fireChanged(new Matcher(){
					public boolean matches(Object item) {
						ExistenciaConteo exis=(ExistenciaConteo)item;
						return exis.getConteo()>0;
					}
				});
			}else if("NEGATIVOS".equals(tipo)){
				fireChanged(new Matcher(){
					public boolean matches(Object item) {
						ExistenciaConteo exis=(ExistenciaConteo)item;
						return exis.getConteo()<0;
					}
				});
			}
		}
		
		public JComboBox getSelector(){
			return selector;
		}
		
		
	}
	

}
