package com.luxsoft.siipap.gastos.operaciones;

import java.util.List;

import com.luxsoft.siipap.model.gastos.ActivoFijo;
import com.luxsoft.siipap.swing.browser.FilteredBrowserPanel;
import com.luxsoft.siipap.swing.matchers.FechaMayorAMatcher;
import com.luxsoft.siipap.swing.matchers.FechaMenorAMatcher;

public class ActivoFijoPanel extends FilteredBrowserPanel<ActivoFijo>{

	public FechaMayorAMatcher fechaInicialSelector;
	public FechaMenorAMatcher fechaFinalSelector;
	
	public ActivoFijoPanel() {
		super(ActivoFijo.class);
	}
	
	protected void init(){
		addProperty("id","rubro.descripcion","documento","producto.descripcion","moi","fechaDeAdquisicion","depreciacionInicial"
				,"remanenteInicial","ultimaFechaActualizable"
				,"tasaDepreciacion","depreciacionDelEjercicio","depreciacionAcumulada","remanente","ultimoINPC.indice","inpcOriginal.indice"
				,"factorDeActualizacion","depreciacionActualizada");
		
		addLabels("Id","Concepto","Docto","Descripcion","MOI","Fecha (Adq)","Depreciacio Acu Ant","Remanente","Fecha (Act)"
				,"Tasa","Dep (Ejercicio)","Dep (Acu)","Saldo","INPC (Ultimo)","INPC (Original)"
				,"Factor","Dep Actual");
		fechaInicialSelector=new FechaMayorAMatcher();
		fechaInicialSelector.setDateField("fechaDeAdquisicion");
		fechaFinalSelector=new FechaMenorAMatcher();
		fechaFinalSelector.setDateField("fechaDeAdquisicion");
		installTextComponentMatcherEditor("Clasificación", "clasificacion.nombre");
		installTextComponentMatcherEditor("Sucursal", "sucursal");
		installTextComponentMatcherEditor("Departamento", "departamento");
		installTextComponentMatcherEditor("Descripción", "producto.descripcion");
		installTextComponentMatcherEditor("Documento", "documento");
		installCustomMatcherEditor("F Inicial", fechaInicialSelector.getFechaField(), fechaInicialSelector);
		installCustomMatcherEditor("F Final", fechaFinalSelector.getFechaField(), fechaFinalSelector);
	}
	
	@Override
	protected List<ActivoFijo> findData() {
		// TODO Auto-generated method stub
		return super.findData();
	}

}
