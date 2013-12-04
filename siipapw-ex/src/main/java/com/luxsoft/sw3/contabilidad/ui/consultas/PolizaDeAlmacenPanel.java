package com.luxsoft.sw3.contabilidad.ui.consultas;

import java.text.MessageFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;

import org.apache.commons.lang.StringUtils;

import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.gui.TableFormat;

import com.luxsoft.siipap.cxp.model.CXPFactura;
import com.luxsoft.siipap.swing.dialog.AbstractSelector;
import com.luxsoft.siipap.swing.reports.ReportUtils;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.sw3.contabilidad.model.CuentaContable;
import com.luxsoft.sw3.contabilidad.model.Poliza;
import com.luxsoft.sw3.contabilidad.model.PolizaDet;


/**
 * Panel para la administración de la poliza de Compras - Almancen
 * 
 * @author Ruben Cancino
 *
 */
public class PolizaDeAlmacenPanel extends PanelGenericoDePoliza{

	public PolizaDeAlmacenPanel() {
		super();
		setClase("COMPRAS ALMACEN");
		setManager(new PolizaDeAlmacenController());
	}

	@Override
	public void drill(PolizaDet det) {
		if(det!=null){
			CuentaContable cuenta =det.getCuenta();
			String desc=det.getDescripcion2();
			final Date fecha=det.getPoliza().getFecha();
			if(det.getCuenta().getClave().equals("200")){
				if(det.getDescripcion().startsWith("PROVEEDOR")){
					if(det.getDescripcion2().startsWith("Fac")){
						String filtro=StringUtils.substring(det.getReferencia(), 0, 4);
						AnalisisDeCXPFacturas.show(fecha,filtro);
					}
				}
			}
			if(det.getCuenta().getClave().equals("119")){
				if(det.getDescripcion().startsWith("INVENTARIO")){
					if(det.getDescripcion2().startsWith("Inv")){
						String claveProveedor=StringUtils.substring(det.getReferencia(),0,4);
						AnalisisDeCXPFacturas.show(fecha,claveProveedor);
						//AnalisisDeEntradasPorCompra.buscarEntradas(claveProveedor, fecha);
					}
				}
			}
			if(det.getCuenta().getClave().equals("200")){
				if(det.getDescripcion2().startsWith("Nota")||det.getDescripcion2().startsWith("Descto Nota")){
					AnalisisDeNotasDeCreditoCxPPanel.show(fecha);
				}
			}
		} 
			
	}
	
	public void imprimirPoliza(Poliza bean){
		Map params=new HashMap();
		params.put("ID", bean.getId());
		//params.put(key, value)
		String path=ReportUtils.toReportesPath("contabilidad/Poliza.jasper");
		if(ReportUtils.existe(path))
			ReportUtils.viewReport(path, params);
		else
			JOptionPane.showMessageDialog(this.getControl()
					,MessageFormat.format("El reporte:\n {0} no existe",path),"Reportes",JOptionPane.ERROR_MESSAGE);
	}
	
	
	

}
