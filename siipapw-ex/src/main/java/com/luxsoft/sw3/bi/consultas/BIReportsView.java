package com.luxsoft.sw3.bi.consultas;

import javax.swing.Action;
import javax.swing.tree.DefaultMutableTreeNode;

import com.luxsoft.siipap.cxc.CXCActions;
import com.luxsoft.siipap.reportes.ComsSinAnalizarReportForm;
import com.luxsoft.siipap.inventario.ui.reports.CostoPromedioReportForm;
import com.luxsoft.siipap.inventario.ui.reports.FacturasPorProveedorForm;
import com.luxsoft.siipap.inventario.ui.reports.InventarioCosteadoReportForm;
import com.luxsoft.siipap.inventario.ui.reports.KardexReportForm;
import com.luxsoft.siipap.model.tesoreria.EstadoDeCuenta;
import com.luxsoft.siipap.reportes.AnalisisDeEmbarqueForm;
import com.luxsoft.siipap.reportes.BajaEnVentas;
import com.luxsoft.siipap.reportes.ClientesSinVentas;
import com.luxsoft.siipap.reportes.ComisionPorChofer;
import com.luxsoft.siipap.reportes.ComisionPorFacturista;
import com.luxsoft.siipap.reportes.ComparativoClientesPorVendedor;
import com.luxsoft.siipap.reportes.ComparativoMejoresClientes;
import com.luxsoft.siipap.reportes.ComparativoVentasPorSucursal;
import com.luxsoft.siipap.reportes.ComparativoVentasXLinea;
import com.luxsoft.siipap.reportes.ComparativoVentasXLineaXCte;
import com.luxsoft.siipap.reportes.LineaProdXCliente;
import com.luxsoft.siipap.reportes.LineasMejoresClientes;
import com.luxsoft.siipap.reportes.MejoresClientes;
import com.luxsoft.siipap.reportes.ReporteDeAlcancesForm;
import com.luxsoft.siipap.reportes.ReporteDeAlcancesImpoForm;
import com.luxsoft.siipap.reportes.ReporteDeSugerenciaDeTrasladoForm;
import com.luxsoft.siipap.reportes.VentasPorCliente;
import com.luxsoft.siipap.reportes.VentasPorSucursal;
import com.luxsoft.siipap.reportes.VentasXLineaXDia;
import com.luxsoft.siipap.reports.AntiguedadDeSaldoAcumuladoReportForm;
import com.luxsoft.siipap.reports.AntiguedadDeSaldoConCorteReportForm;
import com.luxsoft.siipap.reports.AntiguedadDeSaldoReportForm;
import com.luxsoft.siipap.reports.CargosNoCobradosCredito;
import com.luxsoft.siipap.reports.ClientesNuevosBI;
import com.luxsoft.siipap.reports.CobranzaCamioneta;
import com.luxsoft.siipap.reports.CobranzaCredito;
import com.luxsoft.siipap.reports.EdoDeMovCxc;
import com.luxsoft.siipap.reports.EstadoDeCuentaReport;
import com.luxsoft.siipap.reports.ExcepcionesEnDesctoReportForm;
import com.luxsoft.siipap.reports.ExcepcionesEnPrecioReportForm;
import com.luxsoft.siipap.reports.FacturasCanceladasBi;
import com.luxsoft.siipap.reports.FacturasCanceladasNCBI;
import com.luxsoft.siipap.reports.FacturasPendientesCamioneta;
import com.luxsoft.siipap.reports.FacturasPendientesCamionetaTab;
import com.luxsoft.siipap.reports.Juridico_ReciboEntregaAbogadoForm;
import com.luxsoft.siipap.reports.Juridico_RelacionDeTraspasosForm;
import com.luxsoft.siipap.reports.NotasDeCargoGeneradas;
import com.luxsoft.siipap.reports.NotasDeCreditoGeneradas;
import com.luxsoft.siipap.reports.RelacionDeChequesDevueltosReport;
import com.luxsoft.siipap.reports.RmdCobranzaContadoReportForm;
import com.luxsoft.siipap.reports.SaldosPendienteXAbogadoReportForm;
import com.luxsoft.siipap.reports.VentasDiariasBI;
import com.luxsoft.siipap.reports.VentasGlobalesBI;
import com.luxsoft.siipap.reports.VentasPorFacturistaBI;
import com.luxsoft.siipap.reports.VentasXSucReportForm;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.actions.DispatchingAction;
import com.luxsoft.siipap.swing.reports.ReportView2;

public class BIReportsView extends ReportView2{
	
	protected void buildTree(DefaultMutableTreeNode root){
		
		//Nodo de BI
		DefaultMutableTreeNode biNode=new DefaultMutableTreeNode("BI",true);
				biNode.add(buildActionNode("Baja en ventas", "reporteBajaEnVentas"));
				biNode.add(buildActionNode( "Mejores Clientes","reporteMejoresClientes"));
				biNode.add(buildActionNode("Ventas Por Cliente","reporteVentasPorCliente"));
				biNode.add(buildActionNode("Clientes Sin Venta","reporteClientesSinVenta"));
				biNode.add(buildActionNode("Comparativo Mejores Clientes","reporteComparativoMejoresClientes"));
				biNode.add(buildActionNode("Mejores Clientes Por Linea","reporteLineasMejoresClientes"));
				biNode.add(buildActionNode("Ventas Cliente Por Linea ","reporteLineasXProdCliente"));
				biNode.add(buildActionNode("Comparativo Ventas Por Linea ","reporteComparativoLineas"));
				biNode.add(buildActionNode("Ventas Por Linea Por Dia ","reporteVentasXLineaXDia"));
				biNode.add(buildActionNode("Comparativo Ventas Por Linea Por Cliente","reporteComparativoLineasporCliente" ));
				biNode.add(buildActionNode("Ventas Por Sucursal","reporteVentasPorSucursal" ));
				biNode.add(buildActionNode("Comparativo Ventas Por Sucursal","reporteVentasPorSucursalComparativo" ));
				biNode.add(buildActionNode("Comparativo Clientes Por Vendedor","reporteClientesPorVendedor" ));
		root.add(biNode);
		
				//Nodo de ventas
		DefaultMutableTreeNode ventasNode=new DefaultMutableTreeNode("Ventas",true);
		 		ventasNode.add(buildActionNode( "Ventas Diarias","reporteVentasDiarias"));
		 		ventasNode.add(buildActionNode("Ventas Mensuales","ventasMensuales"));
		 		ventasNode.add(buildActionNode("Ventas Por Facturista","reporteFacsPorFacturista"));
		 		ventasNode.add(buildActionNode("Facturas Canceladas","reporteFacturasCanceladas"));
		 		ventasNode.add(buildActionNode( "Clientes Nuevos","reporteClientesNuevos"));
		 		ventasNode.add(buildActionNode( "Ventas Globales","reporteVentasGlobales"));	
		 		ventasNode.add(buildActionNode( "Devoluciones","reporteDeDevoluciones"));
		root.add(ventasNode);
		
		//Nodo de CXC
		DefaultMutableTreeNode cxcNode=new DefaultMutableTreeNode("Cuentas por Cobrar",true);
		// Cuentas Por Cobrar
				cxcNode.add(buildActionNode("Antigüedad de Saldos","reporteAntiguedadDeSaldos"));
				cxcNode.add(buildActionNode("Antigüedad de Saldos Fecha Corte","reporteAntiguedadDeSaldosCorte"));
				cxcNode.add(buildActionNode("Antigüedad de Saldos Anual ","reporteAntiguedadDeSaldosAcumulado"));
				cxcNode.add(buildActionNode("Facturas Canceladas Con Nota de Credito","reporteFacturasCancNC"));
				cxcNode.add(buildActionNode("Excepciones en Descuento","reporteExcepcionesDescuento"));
				cxcNode.add(buildActionNode("Excepciones en Precio","reporteExcepcionesPrecio"));
				cxcNode.add(buildActionNode("Facturas Pendientes Camioneta","reporteFacturasPendientes"));
				cxcNode.add(buildActionNode("Cargos No Cobrados De Credito","reporteCargosNoCobrados"));
				cxcNode.add(buildActionNode("Estado de Mov. Cxc","reporteEstadoDeMovimientosCxc"));
				cxcNode.add(buildActionNode("Cobranza Credito","reporteCobranzaCredito"));
				cxcNode.add(buildActionNode("Cobranza Camioneta","reporteCobranzaCamioneta"));
				cxcNode.add(buildActionNode("Estado de Cuenta","reporteEstadoDeCuenta"));
				cxcNode.add(buildActionNode("Notas de Cargo emitidas","reporteNotasDeCargo"));
				cxcNode.add(buildActionNode("Notas de Credito emitidas","reporteNotasDeCredito"));
				
		// Juridico		
				cxcNode.add(buildActionNode("Traspasos a Juridico","reporteDeTraspasos"));
				cxcNode.add(buildActionNode("Entrega al Abogado","reporteDeEntrega"));		
				cxcNode.add(buildActionNode("Saldos pendientes Por Abogado ","saldosPendientes"));
				
		//Cheques Devueltos
				cxcNode.add(buildActionNode("Cheques Devueltos Por Cte","reporteRelacionDeChequesDevueltos"));
		root.add(cxcNode);
		
		//Nodo de Embarques
		DefaultMutableTreeNode embarquesNode=new DefaultMutableTreeNode("Embarques",true);
		   		embarquesNode.add(buildActionNode("Facturas Pendientes Camioneta Emb","reporteFacturasPendientesTab"));
		   		embarquesNode.add(buildActionNode("Analisis De Embarques","reporteAnalisisEmbarques"));
		   		embarquesNode.add(buildActionNode("Comisión Por Chofer","reporteComisionChofer"));
		   		embarquesNode.add(buildActionNode("Comisión Por Facturista","reporteComisionFacturista"));
		root.add(embarquesNode);
		
		//Nodo de Inventarios
				DefaultMutableTreeNode inventariosNode=new DefaultMutableTreeNode("Inventarios",true);
						inventariosNode.add(buildActionNode("Kardex", "reporteKardex"));
						inventariosNode.add(buildActionNode("Sugerencia Traslados", "reporteSugerenciaTraslados"));
						inventariosNode.add(buildActionNode("Costo Promedio", "reporteCostoPromedio"));
						inventariosNode.add(buildActionNode("Inventario Costeado", "reporteInventarioCosteado"));
						
				root.add(inventariosNode);
		
		//Nodo de compras
		DefaultMutableTreeNode comprasNode=new DefaultMutableTreeNode("Compras",true);
				comprasNode.add(buildActionNode("Alcance", "reporteAlcance"));
				comprasNode.add(buildActionNode("Alcance Importación", "reporteAlcanceImportacion"));
				comprasNode.add(buildActionNode("Entradas Sin Analizar", "reporteComsSinAnalizar"));
				comprasNode.add(buildActionNode("Facturas Analizadas", "reporteFacturasAnalizadas"));
		root.add(comprasNode);
		
		
		
	}
	
	protected DefaultMutableTreeNode buildActionNode(String label,String methodName){
		DispatchingAction action=new DispatchingAction(this, methodName);
		action.putValue(Action.SHORT_DESCRIPTION, label);
		DefaultMutableTreeNode node=new DefaultMutableTreeNode(action,false);
		return node;
	}
	
	/*
	 * Reportes de BI
	 */
	public void reporteBajaEnVentas(){
		BajaEnVentas report= new BajaEnVentas();
		report.run();
	}
	public void reporteMejoresClientes(){
		MejoresClientes report= new  MejoresClientes();
		report.run();
	}
	public void reporteVentasPorCliente(){
		VentasPorCliente report= new  VentasPorCliente();
		report.run();
	}
	public void reporteClientesSinVenta(){
		ClientesSinVentas report= new ClientesSinVentas ();
		report.run();
	}
	public void reporteComparativoMejoresClientes(){
		ComparativoMejoresClientes report =new ComparativoMejoresClientes();
		report.run();
	}
	
	public void reporteLineasMejoresClientes(){
		LineasMejoresClientes report=new LineasMejoresClientes();
		report.run();
	}

	public void reporteLineasXProdCliente(){
		LineaProdXCliente report =new LineaProdXCliente();
		report.run();
	}
	
	public void reporteComparativoLineas(){
		ComparativoVentasXLinea report=new ComparativoVentasXLinea();
		report.run();
			
	}
	
	public void reporteComparativoLineasporCliente(){
		ComparativoVentasXLineaXCte report=new ComparativoVentasXLineaXCte();
		report.run();
			
	}
	
	public void reporteVentasXLineaXDia(){
		VentasXLineaXDia report=new VentasXLineaXDia();
		report.run();
			
	}
	
	public void reporteClientesPorVendedor(){
		ComparativoClientesPorVendedor.run();
			
	}
	
	public void reporteVentasPorSucursalComparativo(){
		ComparativoVentasPorSucursal.run();
	}
	
	/*Reportes de Ventas
	 * 
	 */
	public void reporteVentasDiarias(){
		VentasDiariasBI.run();
	}
	public void ventasMensuales() {
		VentasXSucReportForm.run();
	}
	public void reporteFacsPorFacturista(){
		VentasPorFacturistaBI.run();
	}
	
	public void reporteFacturasPendientesTab(){
		FacturasPendientesCamionetaTab.run();
	}
	public void reporteFacturasCanceladas(){
		FacturasCanceladasBi.run();
	}
	public void reporteClientesNuevos(){
		ClientesNuevosBI.run();
	}
	
	public void reporteVentasGlobales(){
		VentasGlobalesBI.run();
	}
	
	public void reporteDeDevoluciones(){
		RmdCobranzaContadoReportForm.run();
	}
	
	public void reporteVentasPorSucursal(){
		VentasPorSucursal.run();
	}
	
	/*              
	 * REportes de CXC
	 */
	
	public void reporteAntiguedadDeSaldos(){
		AntiguedadDeSaldoReportForm.run();
	}
	
	public void reporteAntiguedadDeSaldosCorte(){
		AntiguedadDeSaldoConCorteReportForm.run();
	}
	
	public void reporteAntiguedadDeSaldosAcumulado(){
		AntiguedadDeSaldoAcumuladoReportForm.run();
	}
	
	public void reporteFacturasPendientes(){
		FacturasPendientesCamioneta.run();
	}
	
	public void reporteFacturasCancNC(){
		FacturasCanceladasNCBI.run();
	}
	
	public void reporteCargosNoCobrados(){
		CargosNoCobradosCredito.run();
	}
	
	public void reporteEstadoDeMovimientosCxc(){
		EdoDeMovCxc.run();
	}
		
	public void reporteExcepcionesPrecio(){
		ExcepcionesEnPrecioReportForm.run();
	}
	
	public void reporteExcepcionesDescuento(){
		ExcepcionesEnDesctoReportForm.run();
	}
	
	public void reporteCobranzaCredito(){
		CobranzaCredito.run();
	}
	
	public void reporteCobranzaCamioneta(){
		CobranzaCamioneta.run(ServiceLocator2.getJdbcTemplate());
	}
	public void reporteEstadoDeCuenta(){
		EstadoDeCuentaReport.run();
	}
	
	public void reporteNotasDeCargo(){
		NotasDeCargoGeneradas.run();
	}
	
	public void reporteNotasDeCredito(){
		NotasDeCreditoGeneradas.run();
	}
	
	//juridico
	
	public void reporteDeTraspasos(){
		Juridico_RelacionDeTraspasosForm.run();
	}
	
	public void reporteDeEntrega(){
		Juridico_ReciboEntregaAbogadoForm.run();
	}
	public void saldosPendientes(){
		SaldosPendienteXAbogadoReportForm.run();
	}
	//Cheques Devueltos
	public void reporteRelacionDeChequesDevueltos(){
		RelacionDeChequesDevueltosReport report=new RelacionDeChequesDevueltosReport();
		report.run();
		
	}
	/*
	 * Reportes de Embarques
	 */
	
	public void reporteAnalisisEmbarques(){
	  AnalisisDeEmbarqueForm.run();
	}
	
	public void reporteComisionChofer(){
		  ComisionPorChofer.run();
		}
	
	public void reporteComisionFacturista(){
		  ComisionPorFacturista.run();
		}
	/*
	 * Reportes de inventarios
	 */
	public void reporteKardex(){
		  KardexReportForm.run();
		}
	public void reporteSugerenciaTraslados(){
		ReporteDeSugerenciaDeTrasladoForm.run();
	}
	
	public void reporteCostoPromedio(){
		CostoPromedioReportForm.run();
	}
	
	public void reporteInventarioCosteado(){
		InventarioCosteadoReportForm.run();
	}
	
	
	/*
	 * Reportes de compras
	 */
	
	public void reporteAlcance(){
		ReporteDeAlcancesForm.run();
	}
	
	public void reporteAlcanceImportacion(){
		ReporteDeAlcancesImpoForm.run();
	}
	
	public void reporteComsSinAnalizar(){
		ComsSinAnalizarReportForm.run();
	}
	
	public void reporteFacturasAnalizadas(){
		FacturasPorProveedorForm.run();
	}
}
