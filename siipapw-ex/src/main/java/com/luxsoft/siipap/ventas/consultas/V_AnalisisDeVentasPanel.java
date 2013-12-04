package com.luxsoft.siipap.ventas.consultas;

import java.util.List;

import javax.swing.Action;

import com.luxsoft.siipap.reports.FacturasCanceladasBi;
import com.luxsoft.siipap.reports.VentasDiariasBI;
import com.luxsoft.siipap.swing.browser.FilteredBrowserPanel;
import com.luxsoft.siipap.ventas.model.Venta;

public class V_AnalisisDeVentasPanel extends FilteredBrowserPanel<Venta>{

	public V_AnalisisDeVentasPanel() {
		super(Venta.class);
	}
	
	public void init(){
		String[] props=new String[]{"origen","tipoDocto"
				,"documento","numeroFiscal","tipoSiipap"
				,"precioNeto","fecha","vencimiento","reprogramarPago","atraso","sucursal.nombre","clave","nombre","total"
				,"devoluciones","bonificaciones"
				,"descuentos"
				,"descuentoNota","pagos"
				,"saldoCalculado","saldo","origen","cargosAplicados","cargosPorAplicar"
				,"cargosImpPorAplicar"
				,"descuentoFinanciero"				
				};
		String[] names=new String[]{
				"Origen","Tipo"
				,"Docto","N.Fiscal","TipSip"
				,"PN","Fecha","Vto","Rep. Pago","Atr","Suc","Cliente","Nombre","Total","Devs","Bonific"
				,"Descuentos"
				,"Desc (Nota)","Pagos"
				,"Saldo","Saldo Oracle","Origen","Car (Aplic)","Car(%)"
				,"Car($)"
				,"DF"
				};
		addProperty("id");
		addLabels("Id");
	}

	@Override
	public Action[] getActions() {
		if(actions==null)
			actions=new Action[]{
				addAction("", "reporte1", "Reporte 5")
				};
		return actions;
	}

	@Override
	protected List<Action> createProccessActions() {		
		List<Action> procesos=super.createProccessActions();
		procesos.add(addAction("","reporteVentasDiarias", "Ventas Diarias"));
		procesos.add(addAction("","reporteFacturasCanceladas", "Facturas Canceladas"));		
		return procesos;
	}
	
	public void reporteVentasDiarias(){
		VentasDiariasBI.run();
	}
	
	public void reporteFacturasCanceladas(){
		FacturasCanceladasBi.run();
	}
	
	public void reporte2(){
		System.out.println("Reporte 2");
	}
	

}
