package com.luxsoft.sw3.bi.ui.consultas;

import java.util.List;

import javax.swing.Action;

import com.luxsoft.siipap.swing.browser.FilteredBrowserPanel;
import com.luxsoft.siipap.ventas.model.Venta;

public class AnalisisDeVentasPanel extends FilteredBrowserPanel<Venta>{

	public AnalisisDeVentasPanel() {
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
				addAction("", "reporte1", "Reporte 1")
				};
		return actions;
	}

	@Override
	protected List<Action> createProccessActions() {		
		List<Action> procesos=super.createProccessActions();
		procesos.add(addAction("","reporte2", "Reporte 2"));
		return procesos;
	}
	
	public void reporte1(){
		System.out.println("Reporte 1");
	}
	
	public void reporte2(){
		System.out.println("Reporte 2");
	}
	

}
