package com.luxsoft.siipap.inventario.ui.consultas;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.swing.Action;
import javax.swing.JFormattedTextField;
import javax.swing.JTextField;
import javax.swing.text.NumberFormatter;

import org.apache.commons.lang.time.DateUtils;
import org.eclipse.jdt.internal.eval.InstallException;

import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.matchers.Matcher;
import ca.odell.glazedlists.matchers.MatcherEditor;
import ca.odell.glazedlists.matchers.Matchers;

import com.luxsoft.siipap.cxc.model.Cargo;
import com.luxsoft.siipap.inventario.InventariosActions;
import com.luxsoft.siipap.inventario.ui.reports.InventarioCosteadoReportForm;
import com.luxsoft.siipap.inventarios.model.Conteo;
import com.luxsoft.siipap.inventarios.model.ExistenciaConteo;
import com.luxsoft.siipap.reportes.AnalisisDeDiferenciasForm;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.browser.FilteredBrowserPanel;
import com.luxsoft.siipap.swing.matchers.CheckBoxMatcher;
import com.luxsoft.siipap.util.DateUtil;


public class AnalisisCentralizadoDeConteoDeInventario extends FilteredBrowserPanel<ExistenciaConteo>{

	public AnalisisCentralizadoDeConteoDeInventario() {
		super(ExistenciaConteo.class);
		setTitle("Análisis de conteo");
	}
	
	CheckBoxMatcher<ExistenciaConteo> contadosMatcher;
	CheckBoxMatcher<ExistenciaConteo> noContadosMatcher;
	
	private JFormattedTextField fechaField;
	private DateFormat df;
	
	public void init(){
		addProperty("sucursal.nombre","clave","descripcion"
				,"producto.kilos"
				,"producto.gramos"
				,"producto.unidad","existencia"
				//,"conteoEnLinea"
				,"conteo"
				,"diferencia"
				,"ajuste"
				,"existenciaFinal"
				,"sectores"
				,"fijado"
				,"producto.linea.nombre"
				,"producto.clase.nombre"
				);
		addLabels("Suc","Producto","Descripción","Kg"
				,"g","U","Exis","Conteo"
				,"Dif","Ajuste","Exis final","Sectores"
				,"Fijado","Línea","Clase"
				);
		installTextComponentMatcherEditor("Sucursal", "sucursal.nombre");		
		installTextComponentMatcherEditor("Clave", "clave","descripcion");
		installTextComponentMatcherEditor("Sectores", "sectores");
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
		difMayorField=new JFormattedTextField(formatter);
		df=new SimpleDateFormat("dd/MM/yyyy");
		TextFilterator<ExistenciaConteo> fechaFilterator=new TextFilterator<ExistenciaConteo>(){
			public void getFilterStrings(List<String> baseList, ExistenciaConteo element) {
				baseList.add(df.format(element.getFijado()));
			}			
		};
		JTextField fechaField=new JTextField(12);
		installTextComponentMatcherEditor("Fijado ", fechaFilterator,fechaField);
		/*
		installCustomMatcherEditor("Diferencia >=", difMayorField, new MatcherEditor<ExistenciaConteo>(){
			public void addMatcherEditorListener(ca.odell.glazedlists.matchers.MatcherEditor.Listener<ExistenciaConteo> listener) {
				
				
			}

			public Matcher<ExistenciaConteo> getMatcher() {
				Number val=(Number)difMayorField.getValue();
				if(val==null)
					return Matchers.trueMatcher();
				return Matchers.beanPropertyMatcher(ExistenciaConteo.class, "diferencia", val);
			}

			public void removeMatcherEditorListener(ca.odell.glazedlists.matchers.MatcherEditor.Listener<ExistenciaConteo> listener) {
								
			}
			
		});
		*/
		/*
		installTextComponentMatcherEditor("Diferencia >", new TextFilterator<ExistenciaConteo>(){
			public void getFilterStrings(List<String> baseList,ExistenciaConteo element) {
				Number val=(Number)difMayorField.getValue();
				if(val!=null)
					if(element.getDiferencia()>=val.doubleValue()){
						baseList.add(val.toString());				
					}
				
					
			}
			
		}, difMayorField);
		*/
		difMenorField=new JFormattedTextField(formatter);
		/*
		installCustomMatcherEditor("Diferencia <=", difMenorField, new MatcherEditor<ExistenciaConteo>(){
			public void addMatcherEditorListener(ca.odell.glazedlists.matchers.MatcherEditor.Listener<ExistenciaConteo> listener) {
				
				
			}

			public Matcher<ExistenciaConteo> getMatcher() {
				Number val=(Number)difMenorField.getValue();
				if(val==null)
					return Matchers.trueMatcher();
				return Matchers.beanPropertyMatcher(ExistenciaConteo.class, "diferencia", val);
			}

			public void removeMatcherEditorListener(ca.odell.glazedlists.matchers.MatcherEditor.Listener<ExistenciaConteo> listener) {
								
			}
			
		});*/
		/*
		installTextComponentMatcherEditor("Diferencia <=", new TextFilterator<ExistenciaConteo>(){
			public void getFilterStrings(List<String> baseList,ExistenciaConteo element) {
				Number val=(Number)difMenorField.getValue();
				if(val!=null)
					if(element.getDiferencia()<=val.doubleValue()){
						baseList.add(val.toString());				
					}
			}
			
		}, difMenorField);
		*/
		manejarPeriodo();
	}
	
	private JFormattedTextField difMayorField;
	private JFormattedTextField difMenorField;
	Date fecha;
	
	
	protected void beforeLoad(){
		//fecha=SelectorDeFecha.seleccionar();
		//if(fecha!=null)		
			super.beforeLoad();
		
	}

	@Override
	protected List<ExistenciaConteo> findData() {		
		String hql="from ExistenciaConteo e where e.fecha between ? and ?";
		return ServiceLocator2.getHibernateTemplate().find(hql,new Object[]{periodo.getFechaInicial(),periodo.getFechaFinal()});
	}

	@Override
	public Action[] getActions() {
		if(actions==null)
			actions=new Action[]{
				getLoadAction()
				};
		return actions;
	}
	
	@Override
	protected List<Action> createProccessActions() {
		List<Action> procesos=super.createProccessActions();
		procesos.add(addAction("", "reporteAnalisisDeDiferencias","Analisis de diferencias"));		
		return procesos;
	}
	
	public void reporteAnalisisDeDiferencias(){
		AnalisisDeDiferenciasForm.run();
	}
	
	

}
