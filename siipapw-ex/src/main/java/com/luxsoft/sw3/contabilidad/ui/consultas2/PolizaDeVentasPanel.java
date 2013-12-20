package com.luxsoft.sw3.contabilidad.ui.consultas2;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Action;
import javax.swing.JOptionPane;

import org.apache.commons.lang.StringUtils;

import com.luxsoft.siipap.cxc.model.Ficha;
import com.luxsoft.siipap.swing.reports.ReportUtils;
import com.luxsoft.siipap.swing.utils.CommandUtils;
import com.luxsoft.sw3.contabilidad.model.CuentaContable;
import com.luxsoft.sw3.contabilidad.model.Poliza;
import com.luxsoft.sw3.contabilidad.model.PolizaDet;
import com.luxsoft.sw3.contabilidad.polizas.ControladorDinamico;
import com.luxsoft.sw3.contabilidad.ui.consultas.AnalisisDeAnticiposPanel;
import com.luxsoft.sw3.contabilidad.ui.consultas.AnalisisDeAplicacionesPanel;
import com.luxsoft.sw3.contabilidad.ui.consultas.AnalisisDeCortesDeTarjetaPanel;
import com.luxsoft.sw3.contabilidad.ui.consultas.AnalisisDeFichasDeDepositoPanel;
import com.luxsoft.sw3.contabilidad.ui.consultas.AnalisisDeIngresosPorDepositosAutorizadosPanel;
import com.luxsoft.sw3.contabilidad.ui.consultas.AnalisisDeNotasDeCreditoPanel;
import com.luxsoft.sw3.contabilidad.ui.consultas.AnalisisDeOtrosGastosPanel;
import com.luxsoft.sw3.contabilidad.ui.consultas.AnalisisDeOtrosProductosPanel;
import com.luxsoft.sw3.contabilidad.ui.consultas.AnalisisDeVentasPanel;
import com.luxsoft.sw3.contabilidad.ui.consultas.AnalisisSaldosAFavorPanel;
import com.luxsoft.sw3.contabilidad.ui.consultas.FilterBrowserDialog;
import com.luxsoft.sw3.contabilidad.ui.consultas.SelectorDeSucursal;
import com.luxsoft.sw3.tesoreria.TESORERIA_ROLES;

/**
 * Panel para el mantenimiento de polizas de cobranza camioneta
 * 
 * @author Ruben Cancino
 *
 */
public class PolizaDeVentasPanel extends PolizaDinamicaPanel{

	public PolizaDeVentasPanel(ControladorDinamico controller) {
		super(controller);
	}
	
	@Override
	public void drill(PolizaDet det) {
		if(det!=null){
			
			//CuentaContable cuenta =det.getCuenta();
			//String desc=det.getDescripcion2();
			
			String asiento=det.getAsiento();
			if(asiento.startsWith("VENTAS")){
				AnalisisDeVentasPanel.show(det.getPoliza().getFecha(), det.getReferencia(),det.getReferencia2());
			}else if(StringUtils.contains(asiento,"FICHA")){
				AnalisisDeFichasDeDepositoPanel.show(det.getPoliza().getFecha(), det.getReferencia());
			}else if(StringUtils.contains(asiento,"TARJETA")){
				AnalisisDeCortesDeTarjetaPanel.show(det.getPoliza().getFecha(), det.getReferencia());
			}else if(StringUtils.contains(asiento,"DEPOSITO") ||StringUtils.contains(asiento,"POR IDENT")){
				AnalisisDeIngresosPorDepositosAutorizadosPanel.show(det.getPoliza().getFecha(), det.getReferencia());
			}else if(StringUtils.contains(asiento,"ANTICI")){
				AnalisisDeAnticiposPanel.show(det.getPoliza().getFecha(), det.getReferencia());
			}else if(StringUtils.contains(det.getDescripcion2(),"SAF:")){
				AnalisisSaldosAFavorPanel.show(det.getPoliza().getFecha(), det.getReferencia());
			}else if(StringUtils.contains(asiento,"DIFERENCIAS")){				
				AnalisisDeOtrosGastosPanel.show(det.getPoliza().getFecha(), det.getReferencia());
			}else if(StringUtils.contains(det.getDescripcion2(),"OI AJUSTE")){				
				AnalisisDeOtrosProductosPanel.show(det.getPoliza().getFecha(), det.getReferencia());
			}else if(det.getCuenta().getClave().equalsIgnoreCase("405")){
				AnalisisDeNotasDeCreditoPanel.show(det.getPoliza().getFecha(), det.getReferencia());
			}
			
		}
	}
	
	@Override
	public Action[] getActions() {
		if(actions==null)
			actions=new Action[]{
				getLoadAction()
				,addAction(null,"generar","Generar poliza")
				,addAction(null, "salvar", "Salvar póliza")
				,getEditAction()
				//,addAction(null, "cuadrar", "Otros (Ingreso/Gasto)")
				,getDeleteAction()
				,CommandUtils.createPrintAction(this, "imprimirPoliza")
				,addAction(null, "imprimirPolizaSucursal" , "imprimir X Suc.")
				
												};
		return actions;
	}
	
	public void imprimirPoliza(){
		if(getSelectedObject()!=null){
			imprimirPoliza((Poliza)getSelectedObject());
		}
	}
	
	
	public void imprimirPoliza(Poliza bean){
		
		Map params=new HashMap();
		params.put("ID", bean.getId());
		params.put("ORDEN", "ORDER BY 13,3");
		String path=ReportUtils.toReportesPath("contabilidad/Poliza.jasper");
		if(ReportUtils.existe(path))
			ReportUtils.viewReport(path, params);
		else
			JOptionPane.showMessageDialog(this.getControl()
					,MessageFormat.format("El reporte:\n {0} no existe",path),"Reportes",JOptionPane.ERROR_MESSAGE);
	}
	
	public void imprimirPolizaSucursal(){
		if(getSelectedObject()!=null){
			imprimirPolizaSucursal((Poliza)getSelectedObject());
		}
	}
	
	public void imprimirPolizaSucursal(Poliza bean){
		String sucursal=SelectorDeSucursal.seleccionar();
		Map params=new HashMap();
		params.put("ID", bean.getId());
		params.put("SUCURSAL",sucursal);
		String path=ReportUtils.toReportesPath("contabilidad/PolizaxSucursal.jasper");
		if(ReportUtils.existe(path))
			ReportUtils.viewReport(path, params);
		else
			JOptionPane.showMessageDialog(this.getControl()
					,MessageFormat.format("El reporte:\n {0} no existe",path),"Reportes",JOptionPane.ERROR_MESSAGE);
	}
	
}
