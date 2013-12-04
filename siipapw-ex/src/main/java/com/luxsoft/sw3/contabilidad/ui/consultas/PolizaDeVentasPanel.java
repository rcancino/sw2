package com.luxsoft.sw3.contabilidad.ui.consultas;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Action;
import javax.swing.JOptionPane;

import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.gui.TableFormat;

import com.luxsoft.siipap.swing.reports.ReportUtils;
import com.luxsoft.siipap.swing.utils.CommandUtils;
import com.luxsoft.sw3.contabilidad.model.CuentaContable;
import com.luxsoft.sw3.contabilidad.model.Poliza;
import com.luxsoft.sw3.contabilidad.model.PolizaDet;



/**
 * Mantenimiento y control de las polizas de ventas
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class PolizaDeVentasPanel extends PanelGenericoDePoliza{
	
	

	public PolizaDeVentasPanel() {
		super();
		setClase("VENTAS");
	}
	

	@Override
	public void drill(PolizaDet det) {
		if(det!=null){
			
			CuentaContable cuenta =det.getCuenta();
			String desc=det.getDescripcion2();
			//String desc1=det.getDescripcion();
			String asiento=det.getAsiento();
			if(cuenta.getClave().equalsIgnoreCase("401")){
				AnalisisDeVentasPanel.show(det.getPoliza().getFecha(), det.getReferencia());
			}else if(desc.startsWith("FICHA")){
				AnalisisDeFichasDeDepositoPanel.show(det.getPoliza().getFecha(), det.getReferencia());
			}else if(desc.startsWith("CORTE")){
				AnalisisDeCortesDeTarjetaPanel.show(det.getPoliza().getFecha(), det.getReferencia());
			}else if(desc.startsWith("TRANS") ||desc.startsWith("DEPO")){
				AnalisisDeIngresosPorDepositosAutorizadosPanel.show(det.getPoliza().getFecha(), det.getReferencia());
			}else if(desc.startsWith("ANTICI")){
				AnalisisDeAnticiposPanel.show(det.getPoliza().getFecha(), det.getReferencia());
			}else if(desc.startsWith("Saldo a Favor")){
				AnalisisSaldosAFavorPanel.show(det.getPoliza().getFecha(), det.getReferencia());
			}else if(desc.startsWith("OG Ajustes automaticos menores a $1")){				
				AnalisisDeOtrosGastosPanel.show(det.getPoliza().getFecha(), det.getReferencia());
			}else if(desc.startsWith("OI Ajustes automaticos menores a $10")){				
				AnalisisDeOtrosProductosPanel.show(det.getPoliza().getFecha(), det.getReferencia());
			}else if(asiento.startsWith("Cobranza CAM") && cuenta.getClave().equalsIgnoreCase("105")){
				AnalisisDeAplicacionesPanel.show(det.getPoliza().getFecha(), det.getReferencia());
			}else if(cuenta.getClave().equalsIgnoreCase("405")){
				AnalisisDeNotasDeCreditoPanel.show(det.getPoliza().getFecha(), det.getReferencia());
			}
			
		}
	}


	public Action[] getActions() {
		if(actions==null)
			actions=new Action[]{
				getLoadAction()				
				,getInsertAction()
				,getDeleteAction()
				,CommandUtils.createPrintAction(this, "imprimirPoliza")
				,addAction(null,"imprimirPolizaSucursal","Imprimir Póliza x Suc.")
				,addAction(null, "generarPoliza", "Salvar póliza")
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
