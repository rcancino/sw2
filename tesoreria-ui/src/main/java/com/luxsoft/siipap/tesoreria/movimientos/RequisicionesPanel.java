package com.luxsoft.siipap.tesoreria.movimientos;

import com.luxsoft.siipap.model.tesoreria.Requisicion;
import com.luxsoft.siipap.swing.browser.FilteredBrowserPanel;
import com.luxsoft.siipap.swing.matchers.FechaMayorAMatcher;
import com.luxsoft.siipap.swing.matchers.FechaMenorAMatcher;

public class RequisicionesPanel extends FilteredBrowserPanel<Requisicion>{
	
	public RequisicionesPanel() {
		super(Requisicion.class);
	}
	
	public FechaMayorAMatcher fechaInicialSelector;
	public FechaMenorAMatcher fechaFinalSelector;
	
	@Override
	protected void init() {
		addProperty("id","afavor","fecha","moneda","total.amount","porPagar","estado.name","origen","pago.id","pago.fecha","pago.importe","pago.referencia","pago.cuenta","pago.impreso");
		addLabels("Id","A Favor","Fecha","Mon","Total","Por Pagar","estado","Origen","PagoId","Fecha (P)","Total (P)","Referencia","Cuenta","Impreso");
	}

}
