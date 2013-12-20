package com.luxsoft.sw3.replica;

import java.util.Map;
import java.util.TreeMap;

import org.springframework.util.Assert;

public class AuditUtils {
	
	public static final Map<String,String> classTableMap;
	
	/**
	 * Regresa el nombre de la tabla que corresponde a la entidad
	 * 
	 * @param entityName
	 * @return
	 */
	public static String getTableName(String entityName){
		String res= classTableMap.get(entityName);
		//Assert.notNull(res,"No pudo resolver el nombre de la tabla para la clase: "+entityName+ " se requiere para la replica");
		return res;
	}
	
	static{
		classTableMap=new TreeMap<String,String>();
		classTableMap.put("Abono","SX_CXC_ABONOS");
		classTableMap.put("ActivoFijo","SW_ACTIVO_FIJO");
		classTableMap.put("Almacen","SX_ALMACENES");
		classTableMap.put("AnalisisDeFactura","SX_ANALISIS");
		classTableMap.put("AnalisisDeFacturaDet","SX_ANALISISDET");
		classTableMap.put("AnalisisDeFlete","SX_MAQ_ANALISIS_FLETE");
		classTableMap.put("AnalisisDeHojeo","SX_MAQ_ANALISIS_HOJEO");
		classTableMap.put("AnalisisDeMaterial","SX_MAQ_ANALISI_MAT");
		classTableMap.put("AnalisisDeTransformacion","SX_ANALISIS_TRS");
		classTableMap.put("AnticipoDeCompra","SX_CXP_ANTICIPOS");
		classTableMap.put("Aplicacion","SX_CXC_APLICACIONES");
		classTableMap.put("AplicacionDeAnticiposFacturados","SX_ANTICIPOS_APLICADOS");
		classTableMap.put("AplicacionDeNota","SX_CXC_APLICACIONES");
		classTableMap.put("AplicacionDePago","SX_CXC_APLICACIONES");
		classTableMap.put("Asociado","SX_SOCIOS");
		classTableMap.put("Autorizacion","SW_AUTORIZACIONES");
		classTableMap.put("Autorizacion2","SX_AUTORIZACIONES2");
		classTableMap.put("AutorizacionClientePCE","SX_AUTORIZACIONES2");
		classTableMap.put("AutorizacionDeAbono","SX_AUTORIZACIONES2");
		classTableMap.put("AutorizacionDeAplicacionCxC","SX_AUTORIZACIONES2");
		classTableMap.put("AutorizacionDeCargoCXP","SX_AUTORIZACIONES2");
		classTableMap.put("AutorizacionDeMovimiento","SX_AUTORIZACIONES2");
		classTableMap.put("AutorizacionDePedido","SX_AUTORIZACIONES2");
		classTableMap.put("AutorizacionParaCargo","SX_AUTORIZACIONES2");
		classTableMap.put("AutorizacionParaFacturarSinExistencia","SX_AUTORIZACIONES2");
		classTableMap.put("Banco","SW_BANCOS");
		classTableMap.put("Caja","SX_CAJA");
		classTableMap.put("CXPAbono","SX_CXP");
		classTableMap.put("CXPAnalisisDet","SX_CXP_ANALISISDET");
		classTableMap.put("CXPAnticipo","SX_CXP");
		classTableMap.put("CXPAplicacion","SX_CXP_APLICACIONES");
		classTableMap.put("CXPCargo","SX_CXP");
		classTableMap.put("CXPCargoAbono","SX_CXP");
		classTableMap.put("CXPFactura","SX_CXP");
		classTableMap.put("CXPNota","SX_CXP");
		classTableMap.put("CXPPago","SX_CXP");
		classTableMap.put("Cancelacion","( select IMPORTE, FECHA, CREADO, null as CARGO_ID, MODIFICADO, MONEDA, null as TX_IMPORTADO, DOCUMENTO, CREADO_USERID, COMENTARIO, MODIFICADO_USERID, version, ID, AUT_ID, null as TX_REPLICADO, ABONO_ID, 1 as clazz_ from SX_CXC_ABONOS_CANCELADOS union select IMPORTE, FECHA, CREADO, CARGO_ID, MODIFICADO, MONEDA, TX_IMPORTADO, DOCUMENTO, CREADO_USERID, COMENTARIO, MODIFICADO_USERID, version, ID, AUT_ID, TX_REPLICADO, null as ABONO_ID, 2 as clazz_ from SX_CXC_CARGOS_CANCELADOS )");
		classTableMap.put("CancelacionDeAbono","SX_CXC_ABONOS_CANCELADOS");
		classTableMap.put("CancelacionDeCargo","SX_CXC_CARGOS_CANCELADOS");
		classTableMap.put("CancelacionDeVenta","SX_VENTAS_CANCELADAS");
		classTableMap.put("Cargo","SX_VENTAS");
		classTableMap.put("CargoAbono","SW_BCARGOABONO");
		classTableMap.put("CargoPorDiferencia","SX_VENTAS");
		classTableMap.put("CargoPorTesoreria","SX_VENTAS");
		classTableMap.put("CertificadoDeSelloDigital","SX_CFD_CERTIFICADOS");
		classTableMap.put("ChequeDevuelto","SX_VENTAS");
		classTableMap.put("Chofer","SX_CHOFERES");
		classTableMap.put("ChoferFacturista","SX_CHOFER_FACTURISTA");
		classTableMap.put("Clase","SX_CLASES");
		classTableMap.put("ClasificacionDeActivo","SW_ACTIVO_TIPO");
		classTableMap.put("Cliente","SX_CLIENTES");
		classTableMap.put("ClienteCredito","SX_CLIENTES_CREDITO");
		classTableMap.put("ClientePorTonelada","SX_EMBARQUES_CLIENTES_TON");
		classTableMap.put("Cobrador","SX_COBRADORES");
		classTableMap.put("ComisionBancaria","SX_COMISIONES_BANCARIAS");
		classTableMap.put("ComisionVenta","SX_COMISIONES");
		classTableMap.put("Compra2","SX_COMPRAS2");
		classTableMap.put("CompraUnitaria","SX_COMPRAS2_DET");
		classTableMap.put("ComprobanteFiscal","SX_CFD");
		classTableMap.put("Concepto","SW_CONCEPTOS");
		classTableMap.put("ConceptoContable","SX_CONCEPTOS_CONTABLES");
		classTableMap.put("ConceptoDeGasto","SW_CONCEPTO_DE_GASTOS");
		classTableMap.put("Conciliacion","SW_CONCILIACION");
		classTableMap.put("Configuracion","SX_CONFIGURACION");
		classTableMap.put("ConfiguracionKit","SX_KITCONFIG");
		classTableMap.put("Consignatario","SW_CONSIGNATARIOS");
		classTableMap.put("Conteo","SX_CONTEO");
		classTableMap.put("ConteoDet","SX_CONTEODET");
		classTableMap.put("ContraRecibo","SX_CXP_RECIBOS");
		classTableMap.put("ContraReciboDet","SX_CXP_RECIBOS_DET");
		classTableMap.put("CorreccionDeFicha","SX_FICHAS_CORRECIONES");
		classTableMap.put("Corte","SX_CORTES");
		classTableMap.put("CorteDeTarjeta","SX_CORTE_TARJETAS");
		classTableMap.put("CorteDeTarjetaDet","SX_CORTE_TARJETASDET");
		classTableMap.put("CostoPromedio","SX_COSTOS_P");
		classTableMap.put("Cuenta","SW_CUENTAS");
		classTableMap.put("CuentaContable","SX_CUENTAS_CONTABLES");
		classTableMap.put("Departamento","SW_DEPARTAMENTOS");
		classTableMap.put("DescPorVol","SX_DESC_VOL");
		classTableMap.put("DescuentoEspecial","SX_VENTAS_DESCUENTOS");
		classTableMap.put("Devolucion","SX_DEVOLUCIONES");
		classTableMap.put("DevolucionDeCompra","SX_DEVOLUCION_COMPRAS");
		classTableMap.put("DevolucionDeCompraDet","SX_INVENTARIO_DEC");
		classTableMap.put("DevolucionDeVenta","SX_INVENTARIO_DEV");
		classTableMap.put("DevolucionPorTesoreria","SX_VENTAS");
		classTableMap.put("Embarque","SX_EMBARQUES");
		classTableMap.put("EmbarqueForaneo","SX_EMBARQUES");
		classTableMap.put("Empresa","SW_EMPRESAS");
		classTableMap.put("EntradaDeMaquila","SX_INVENTARIO_MAQ");
		classTableMap.put("EntradaDeMaterial","SX_MAQ_ENTRADAS");
		classTableMap.put("EntradaDeMaterialDet","SX_MAQ_ENTRADASDET");
		classTableMap.put("EntradaPorCompra","SX_INVENTARIO_COM");
		classTableMap.put("Entrega","SX_ENTREGAS");
		classTableMap.put("EntregaDet","SX_ENTREGAS_DET");
		classTableMap.put("Esquema","SX_ESQUEMAS");
		classTableMap.put("Existencia","SX_EXISTENCIAS");
		classTableMap.put("ExistenciaConteo","SX_EXISTENCIA_CONTEO");
		classTableMap.put("Ficha","SX_FICHAS");
		classTableMap.put("FichaDet","SX_FICHASDET");
		classTableMap.put("Folio","SX_FOLIOS");
		classTableMap.put("FolioFiscal","SX_CFD_FOLIOS");
		classTableMap.put("GCompra","SW_GCOMPRA");
		classTableMap.put("GCompraDet","SW_GCOMPRADET");
		classTableMap.put("GFacturaPorCompra","SW_FACTURAS_GASTOS");
		classTableMap.put("GProductoServicio","SW_GPRODUCTOSERVICIO");
		classTableMap.put("GProveedor","SW_GPROVEEDOR");
		classTableMap.put("GTipoProveedor","SW_GTIPOS_DE_ROVEEDOR");
		classTableMap.put("INPC","SW_IPC");
		classTableMap.put("InstruccionDeEntrega","SX_PEDIDOS_ENTREGAS");
		//classTableMap.put("Inventario","( select UNIDAD_ID, null as gastos, null as DSCTO, null as costoOrigen, null as DSCTO_NOTA, COSTOP, null as TRANSFORMACION_ID, null as TIP, MODIFICADO_USERID, COSTOU, null as RECEPCION_ID, null as REMISION, null as PRECIO, CANTIDAD, null as MAQUILA_TIPO, null as COMPRADET_ID, YEAR, null as ABONO_ID, CLAVE, null as MOVI_ID, null as TRTIP, null as COSTO_GASTO, null as ANALISIS_GASTO_ID, KILOS, null as COMPRA, null as COSTO_CORTE, null as IMPORTE_NETO, null as MAQUILA_ID, null as DESTINO_ID, null as SIIPAPWIN_ID, null as TRASLADO_ID, null as VENTA_ID, null as VENTADET_ID, null as COMENTARIO2, null as ANALISIS_FLETE_ID, null as GASTO_COMENTARIO, null as COSTO_FLETE, null as SUBTOTAL, null as DSCTO_ORIG, ALMACEN_ID, null as DEVOLUCION_ID, COMENTARIO, null as ORDENP, FACTORU, null as SOLICITADO, null as CORTES, null as TIPO_CIS, null as CORTE_ANCHO, null as MAQUILADOR, null as SER, RENGLON, CREADO, null as REMISION_F, null as PRECIO_CORTES, MODIFICADO, null as ANALISIS_ID, SUCURSAL_ID, COSTO, version, null as TPS_ORIGEN, null as CORTE_LARGO, null as PRECIO_L, MES, null as PROVEEDOR_ID, null as ANALISIS_HOJEO_ID, null as COSTO_MP, null as IMPORTE, null as CONCEPTO, null as IDX, FECHA, INVENTARIO_ID, DESCRIPCION, DOCUMENTO, PRODUCTO_ID, CREADO_USERID, EXISTENCIA, NACIONAL, null as DEVO_ID, null as SERIE, null as CORTES_INSTRUCCION, null as SUCCOM, null as COMS2_ID, null as TIPO, null as COMPRA_F, null as GASTO_DOCTO, null as RNGL, 1 as clazz_ from SX_INVENTARIO_INI union select UNIDAD_ID, null as gastos, null as DSCTO, null as costoOrigen, null as DSCTO_NOTA, COSTOP, null as TRANSFORMACION_ID, null as TIP, MODIFICADO_USERID, COSTOU, null as RECEPCION_ID, null as REMISION, null as PRECIO, CANTIDAD, null as MAQUILA_TIPO, null as COMPRADET_ID, null as YEAR, null as ABONO_ID, CLAVE, MOVI_ID, null as TRTIP, null as COSTO_GASTO, null as ANALISIS_GASTO_ID, KILOS, null as COMPRA, null as COSTO_CORTE, null as IMPORTE_NETO, null as MAQUILA_ID, null as DESTINO_ID, null as SIIPAPWIN_ID, null as TRASLADO_ID, null as VENTA_ID, null as VENTADET_ID, COMENTARIO2, null as ANALISIS_FLETE_ID, null as GASTO_COMENTARIO, null as COSTO_FLETE, null as SUBTOTAL, null as DSCTO_ORIG, ALMACEN_ID, null as DEVOLUCION_ID, COMENTARIO, null as ORDENP, FACTORU, null as SOLICITADO, null as CORTES, TIPO_CIS, null as CORTE_ANCHO, null as MAQUILADOR, null as SER, RENGLON, CREADO, null as REMISION_F, null as PRECIO_CORTES, MODIFICADO, null as ANALISIS_ID, SUCURSAL_ID, COSTO, version, null as TPS_ORIGEN, null as CORTE_LARGO, null as PRECIO_L, null as MES, null as PROVEEDOR_ID, null as ANALISIS_HOJEO_ID, null as COSTO_MP, null as IMPORTE, CONCEPTO, null as IDX, FECHA, INVENTARIO_ID, DESCRIPCION, DOCUMENTO, PRODUCTO_ID, CREADO_USERID, EXISTENCIA, NACIONAL, null as DEVO_ID, null as SERIE, null as CORTES_INSTRUCCION, null as SUCCOM, null as COMS2_ID, null as TIPO, null as COMPRA_F, null as GASTO_DOCTO, null as RNGL, 2 as clazz_ from SX_INVENTARIO_MOV union select UNIDAD_ID, null as gastos, null as DSCTO, null as costoOrigen, null as DSCTO_NOTA, COSTOP, null as TRANSFORMACION_ID, null as TIP, MODIFICADO_USERID, COSTOU, null as RECEPCION_ID, null as REMISION, null as PRECIO, CANTIDAD, null as MAQUILA_TIPO, null as COMPRADET_ID, null as YEAR, null as ABONO_ID, CLAVE, null as MOVI_ID, null as TRTIP, null as COSTO_GASTO, null as ANALISIS_GASTO_ID, KILOS, null as COMPRA, null as COSTO_CORTE, null as IMPORTE_NETO, null as MAQUILA_ID, null as DESTINO_ID, null as SIIPAPWIN_ID, null as TRASLADO_ID, null as VENTA_ID, null as VENTADET_ID, null as COMENTARIO2, null as ANALISIS_FLETE_ID, null as GASTO_COMENTARIO, null as COSTO_FLETE, null as SUBTOTAL, null as DSCTO_ORIG, ALMACEN_ID, null as DEVOLUCION_ID, COMENTARIO, null as ORDENP, FACTORU, null as SOLICITADO, null as CORTES, null as TIPO_CIS, null as CORTE_ANCHO, null as MAQUILADOR, null as SER, RENGLON, CREADO, null as REMISION_F, null as PRECIO_CORTES, MODIFICADO, null as ANALISIS_ID, SUCURSAL_ID, COSTO, version, null as TPS_ORIGEN, null as CORTE_LARGO, null as PRECIO_L, null as MES, null as PROVEEDOR_ID, null as ANALISIS_HOJEO_ID, null as COSTO_MP, null as IMPORTE, null as CONCEPTO, null as IDX, FECHA, INVENTARIO_ID, DESCRIPCION, DOCUMENTO, PRODUCTO_ID, CREADO_USERID, EXISTENCIA, NACIONAL, null as DEVO_ID, null as SERIE, null as CORTES_INSTRUCCION, null as SUCCOM, null as COMS2_ID, null as TIPO, null as COMPRA_F, null as GASTO_DOCTO, null as RNGL, 3 as clazz_ from SX_INVENTARIO_KIT union select UNIDAD_ID, gastos, null as DSCTO, costoOrigen, null as DSCTO_NOTA, COSTOP, TRANSFORMACION_ID, null as TIP, MODIFICADO_USERID, COSTOU, null as RECEPCION_ID, REMISION, null as PRECIO, CANTIDAD, null as MAQUILA_TIPO, null as COMPRADET_ID, null as YEAR, null as ABONO_ID, CLAVE, null as MOVI_ID, TRTIP, null as COSTO_GASTO, null as ANALISIS_GASTO_ID, KILOS, null as COMPRA, COSTO_CORTE, null as IMPORTE_NETO, null as MAQUILA_ID, DESTINO_ID, null as SIIPAPWIN_ID, null as TRASLADO_ID, null as VENTA_ID, null as VENTADET_ID, null as COMENTARIO2, ANALISIS_FLETE_ID, GASTO_COMENTARIO, COSTO_FLETE, null as SUBTOTAL, null as DSCTO_ORIG, ALMACEN_ID, null as DEVOLUCION_ID, COMENTARIO, null as ORDENP, FACTORU, null as SOLICITADO, null as CORTES, null as TIPO_CIS, null as CORTE_ANCHO, null as MAQUILADOR, null as SER, RENGLON, CREADO, null as REMISION_F, null as PRECIO_CORTES, MODIFICADO, ANALISIS_ID, SUCURSAL_ID, COSTO, version, null as TPS_ORIGEN, null as CORTE_LARGO, null as PRECIO_L, null as MES, null as PROVEEDOR_ID, ANALISIS_HOJEO_ID, null as COSTO_MP, null as IMPORTE, null as CONCEPTO, null as IDX, FECHA, INVENTARIO_ID, DESCRIPCION, DOCUMENTO, PRODUCTO_ID, CREADO_USERID, EXISTENCIA, NACIONAL, null as DEVO_ID, null as SERIE, null as CORTES_INSTRUCCION, null as SUCCOM, null as COMS2_ID, null as TIPO, null as COMPRA_F, GASTO_DOCTO, null as RNGL, 4 as clazz_ from SX_INVENTARIO_TRS union select UNIDAD_ID, null as gastos, null as DSCTO, null as costoOrigen, null as DSCTO_NOTA, COSTOP, null as TRANSFORMACION_ID, null as TIP, MODIFICADO_USERID, COSTOU, null as RECEPCION_ID, REMISION, null as PRECIO, CANTIDAD, null as MAQUILA_TIPO, null as COMPRADET_ID, null as YEAR, null as ABONO_ID, CLAVE, null as MOVI_ID, null as TRTIP, null as COSTO_GASTO, null as ANALISIS_GASTO_ID, KILOS, null as COMPRA, null as COSTO_CORTE, null as IMPORTE_NETO, null as MAQUILA_ID, null as DESTINO_ID, null as SIIPAPWIN_ID, TRASLADO_ID, null as VENTA_ID, null as VENTADET_ID, null as COMENTARIO2, ANALISIS_FLETE_ID, null as GASTO_COMENTARIO, COSTO_FLETE, null as SUBTOTAL, null as DSCTO_ORIG, ALMACEN_ID, null as DEVOLUCION_ID, COMENTARIO, null as ORDENP, FACTORU, SOLICITADO, null as CORTES, null as TIPO_CIS, null as CORTE_ANCHO, null as MAQUILADOR, null as SER, RENGLON, CREADO, null as REMISION_F, null as PRECIO_CORTES, MODIFICADO, null as ANALISIS_ID, SUCURSAL_ID, COSTO, version, TPS_ORIGEN, null as CORTE_LARGO, null as PRECIO_L, null as MES, null as PROVEEDOR_ID, null as ANALISIS_HOJEO_ID, null as COSTO_MP, null as IMPORTE, null as CONCEPTO, null as IDX, FECHA, INVENTARIO_ID, DESCRIPCION, DOCUMENTO, PRODUCTO_ID, CREADO_USERID, EXISTENCIA, NACIONAL, null as DEVO_ID, null as SERIE, null as CORTES_INSTRUCCION, null as SUCCOM, null as COMS2_ID, TIPO, null as COMPRA_F, null as GASTO_DOCTO, RNGL, 5 as clazz_ from SX_INVENTARIO_TRD union select UNIDAD_ID, null as gastos, null as DSCTO, null as costoOrigen, null as DSCTO_NOTA, COSTOP, null as TRANSFORMACION_ID, null as TIP, MODIFICADO_USERID, COSTOU, RECEPCION_ID, REMISION, null as PRECIO, CANTIDAD, null as MAQUILA_TIPO, COMPRADET_ID, null as YEAR, null as ABONO_ID, CLAVE, null as MOVI_ID, null as TRTIP, COSTO_GASTO, ANALISIS_GASTO_ID, KILOS, COMPRA, null as COSTO_CORTE, null as IMPORTE_NETO, null as MAQUILA_ID, null as DESTINO_ID, null as SIIPAPWIN_ID, null as TRASLADO_ID, null as VENTA_ID, null as VENTADET_ID, null as COMENTARIO2, ANALISIS_FLETE_ID, null as GASTO_COMENTARIO, COSTO_FLETE, null as SUBTOTAL, null as DSCTO_ORIG, ALMACEN_ID, null as DEVOLUCION_ID, COMENTARIO, null as ORDENP, FACTORU, null as SOLICITADO, null as CORTES, null as TIPO_CIS, null as CORTE_ANCHO, null as MAQUILADOR, null as SER, RENGLON, CREADO, REMISION_F, null as PRECIO_CORTES, MODIFICADO, null as ANALISIS_ID, SUCURSAL_ID, COSTO, version, null as TPS_ORIGEN, null as CORTE_LARGO, null as PRECIO_L, null as MES, PROVEEDOR_ID, null as ANALISIS_HOJEO_ID, COSTO_MP, null as IMPORTE, null as CONCEPTO, null as IDX, FECHA, INVENTARIO_ID, DESCRIPCION, DOCUMENTO, PRODUCTO_ID, CREADO_USERID, EXISTENCIA, NACIONAL, null as DEVO_ID, null as SERIE, null as CORTES_INSTRUCCION, SUCCOM, COMS2_ID, null as TIPO, COMPRA_F, null as GASTO_DOCTO, null as RNGL, 6 as clazz_ from SX_INVENTARIO_COM union select UNIDAD_ID, null as gastos, null as DSCTO, null as costoOrigen, null as DSCTO_NOTA, COSTOP, null as TRANSFORMACION_ID, null as TIP, MODIFICADO_USERID, COSTOU, null as RECEPCION_ID, null as REMISION, null as PRECIO, CANTIDAD, null as MAQUILA_TIPO, null as COMPRADET_ID, null as YEAR, null as ABONO_ID, CLAVE, null as MOVI_ID, null as TRTIP, null as COSTO_GASTO, null as ANALISIS_GASTO_ID, KILOS, null as COMPRA, null as COSTO_CORTE, null as IMPORTE_NETO, null as MAQUILA_ID, null as DESTINO_ID, null as SIIPAPWIN_ID, null as TRASLADO_ID, null as VENTA_ID, null as VENTADET_ID, null as COMENTARIO2, null as ANALISIS_FLETE_ID, null as GASTO_COMENTARIO, null as COSTO_FLETE, null as SUBTOTAL, null as DSCTO_ORIG, ALMACEN_ID, DEVOLUCION_ID, COMENTARIO, null as ORDENP, FACTORU, null as SOLICITADO, null as CORTES, null as TIPO_CIS, null as CORTE_ANCHO, null as MAQUILADOR, null as SER, RENGLON, CREADO, null as REMISION_F, null as PRECIO_CORTES, MODIFICADO, null as ANALISIS_ID, SUCURSAL_ID, COSTO, version, null as TPS_ORIGEN, null as CORTE_LARGO, null as PRECIO_L, null as MES, null as PROVEEDOR_ID, null as ANALISIS_HOJEO_ID, null as COSTO_MP, null as IMPORTE, null as CONCEPTO, null as IDX, FECHA, INVENTARIO_ID, DESCRIPCION, DOCUMENTO, PRODUCTO_ID, CREADO_USERID, EXISTENCIA, NACIONAL, null as DEVO_ID, null as SERIE, null as CORTES_INSTRUCCION, null as SUCCOM, null as COMS2_ID, null as TIPO, null as COMPRA_F, null as GASTO_DOCTO, null as RNGL, 7 as clazz_ from SX_INVENTARIO_DEC union select UNIDAD_ID, null as gastos, DSCTO, null as costoOrigen, DSCTO_NOTA, COSTOP, null as TRANSFORMACION_ID, null as TIP, MODIFICADO_USERID, COSTOU, null as RECEPCION_ID, null as REMISION, PRECIO, CANTIDAD, null as MAQUILA_TIPO, null as COMPRADET_ID, null as YEAR, null as ABONO_ID, CLAVE, null as MOVI_ID, null as TRTIP, null as COSTO_GASTO, null as ANALISIS_GASTO_ID, KILOS, null as COMPRA, null as COSTO_CORTE, IMPORTE_NETO, null as MAQUILA_ID, null as DESTINO_ID, SIIPAPWIN_ID, null as TRASLADO_ID, VENTA_ID, null as VENTADET_ID, null as COMENTARIO2, null as ANALISIS_FLETE_ID, null as GASTO_COMENTARIO, null as COSTO_FLETE, SUBTOTAL, DSCTO_ORIG, ALMACEN_ID, null as DEVOLUCION_ID, COMENTARIO, ORDENP, FACTORU, null as SOLICITADO, CORTES, null as TIPO_CIS, CORTE_ANCHO, null as MAQUILADOR, null as SER, RENGLON, CREADO, null as REMISION_F, PRECIO_CORTES, MODIFICADO, null as ANALISIS_ID, SUCURSAL_ID, COSTO, version, null as TPS_ORIGEN, CORTE_LARGO, PRECIO_L, null as MES, null as PROVEEDOR_ID, null as ANALISIS_HOJEO_ID, null as COSTO_MP, IMPORTE, null as CONCEPTO, IDX, FECHA, INVENTARIO_ID, DESCRIPCION, DOCUMENTO, PRODUCTO_ID, CREADO_USERID, EXISTENCIA, NACIONAL, null as DEVO_ID, SERIE, CORTES_INSTRUCCION, null as SUCCOM, null as COMS2_ID, TIPO, null as COMPRA_F, null as GASTO_DOCTO, null as RNGL, 8 as clazz_ from SX_VENTASDET union select UNIDAD_ID, null as gastos, null as DSCTO, null as costoOrigen, null as DSCTO_NOTA, COSTOP, null as TRANSFORMACION_ID, TIP, MODIFICADO_USERID, COSTOU, null as RECEPCION_ID, null as REMISION, null as PRECIO, CANTIDAD, null as MAQUILA_TIPO, null as COMPRADET_ID, null as YEAR, null as ABONO_ID, CLAVE, null as MOVI_ID, null as TRTIP, null as COSTO_GASTO, null as ANALISIS_GASTO_ID, KILOS, null as COMPRA, null as COSTO_CORTE, null as IMPORTE_NETO, null as MAQUILA_ID, null as DESTINO_ID, null as SIIPAPWIN_ID, null as TRASLADO_ID, null as VENTA_ID, VENTADET_ID, null as COMENTARIO2, null as ANALISIS_FLETE_ID, null as GASTO_COMENTARIO, null as COSTO_FLETE, null as SUBTOTAL, null as DSCTO_ORIG, ALMACEN_ID, null as DEVOLUCION_ID, COMENTARIO, null as ORDENP, FACTORU, null as SOLICITADO, null as CORTES, null as TIPO_CIS, null as CORTE_ANCHO, null as MAQUILADOR, SER, RENGLON, CREADO, null as REMISION_F, null as PRECIO_CORTES, MODIFICADO, null as ANALISIS_ID, SUCURSAL_ID, COSTO, version, null as TPS_ORIGEN, null as CORTE_LARGO, null as PRECIO_L, null as MES, null as PROVEEDOR_ID, null as ANALISIS_HOJEO_ID, null as COSTO_MP, null as IMPORTE, null as CONCEPTO, null as IDX, FECHA, INVENTARIO_ID, DESCRIPCION, DOCUMENTO, PRODUCTO_ID, CREADO_USERID, EXISTENCIA, NACIONAL, null as DEVO_ID, null as SERIE, null as CORTES_INSTRUCCION, null as SUCCOM, null as COMS2_ID, null as TIPO, null as COMPRA_F, null as GASTO_DOCTO, null as RNGL, 9 as clazz_ from SX_INVENTARIO_FAC union select UNIDAD_ID, null as gastos, null as DSCTO, null as costoOrigen, null as DSCTO_NOTA, COSTOP, null as TRANSFORMACION_ID, null as TIP, MODIFICADO_USERID, COSTOU, null as RECEPCION_ID, null as REMISION, null as PRECIO, CANTIDAD, null as MAQUILA_TIPO, null as COMPRADET_ID, null as YEAR, ABONO_ID, CLAVE, null as MOVI_ID, null as TRTIP, null as COSTO_GASTO, null as ANALISIS_GASTO_ID, KILOS, null as COMPRA, null as COSTO_CORTE, null as IMPORTE_NETO, null as MAQUILA_ID, null as DESTINO_ID, null as SIIPAPWIN_ID, null as TRASLADO_ID, null as VENTA_ID, VENTADET_ID, null as COMENTARIO2, null as ANALISIS_FLETE_ID, null as GASTO_COMENTARIO, null as COSTO_FLETE, null as SUBTOTAL, null as DSCTO_ORIG, ALMACEN_ID, null as DEVOLUCION_ID, COMENTARIO, null as ORDENP, FACTORU, null as SOLICITADO, CORTES, null as TIPO_CIS, null as CORTE_ANCHO, null as MAQUILADOR, null as SER, RENGLON, CREADO, null as REMISION_F, null as PRECIO_CORTES, MODIFICADO, null as ANALISIS_ID, SUCURSAL_ID, COSTO, version, null as TPS_ORIGEN, null as CORTE_LARGO, null as PRECIO_L, null as MES, null as PROVEEDOR_ID, null as ANALISIS_HOJEO_ID, null as COSTO_MP, null as IMPORTE, null as CONCEPTO, null as IDX, FECHA, INVENTARIO_ID, DESCRIPCION, DOCUMENTO, PRODUCTO_ID, CREADO_USERID, EXISTENCIA, NACIONAL, DEVO_ID, null as SERIE, null as CORTES_INSTRUCCION, null as SUCCOM, null as COMS2_ID, null as TIPO, null as COMPRA_F, null as GASTO_DOCTO, null as RNGL, 10 as clazz_ from SX_INVENTARIO_DEV union select UNIDAD_ID, null as gastos, null as DSCTO, null as costoOrigen, null as DSCTO_NOTA, COSTOP, null as TRANSFORMACION_ID, null as TIP, MODIFICADO_USERID, COSTOU, RECEPCION_ID, REMISION, null as PRECIO, CANTIDAD, MAQUILA_TIPO, null as COMPRADET_ID, null as YEAR, null as ABONO_ID, CLAVE, null as MOVI_ID, null as TRTIP, null as COSTO_GASTO, null as ANALISIS_GASTO_ID, KILOS, null as COMPRA, COSTO_CORTE, null as IMPORTE_NETO, MAQUILA_ID, null as DESTINO_ID, null as SIIPAPWIN_ID, null as TRASLADO_ID, null as VENTA_ID, null as VENTADET_ID, null as COMENTARIO2, ANALISIS_FLETE_ID, null as GASTO_COMENTARIO, COSTO_FLETE, null as SUBTOTAL, null as DSCTO_ORIG, ALMACEN_ID, null as DEVOLUCION_ID, COMENTARIO, null as ORDENP, FACTORU, null as SOLICITADO, null as CORTES, null as TIPO_CIS, null as CORTE_ANCHO, MAQUILADOR, null as SER, RENGLON, CREADO, REMISION_F, null as PRECIO_CORTES, MODIFICADO, null as ANALISIS_ID, SUCURSAL_ID, COSTO, version, null as TPS_ORIGEN, null as CORTE_LARGO, null as PRECIO_L, null as MES, null as PROVEEDOR_ID, ANALISIS_HOJEO_ID, COSTO_MP, null as IMPORTE, null as CONCEPTO, null as IDX, FECHA, INVENTARIO_ID, DESCRIPCION, DOCUMENTO, PRODUCTO_ID, CREADO_USERID, EXISTENCIA, NACIONAL, null as DEVO_ID, null as SERIE, null as CORTES_INSTRUCCION, null as SUCCOM, null as COMS2_ID, null as TIPO, null as COMPRA_F, null as GASTO_DOCTO, null as RNGL, 11 as clazz_ from SX_INVENTARIO_MAQ )");
		classTableMap.put("InventarioAnual","SX_INVENTARIO_INI");
		classTableMap.put("Inversion","SX_TRASPASOS_CUENTAS");
		classTableMap.put("Juridico","SX_JURIDICO");
		classTableMap.put("Kit","SX_KITS");
		classTableMap.put("KitDet","SX_INVENTARIO_KIT");
		classTableMap.put("Linea","SX_LINEAS");
		classTableMap.put("ListaDePrecios","SX_LP_PROVS");
		classTableMap.put("ListaDePreciosCliente","SX_LP_CLIENTE");
		classTableMap.put("ListaDePreciosClienteDet","SX_LP_CLIENTE_DET");
		classTableMap.put("ListaDePreciosDet","SX_LP_PROVS_DET");
		classTableMap.put("ListaDePreciosVenta","SX_LP_VENT");
		classTableMap.put("ListaDePreciosVentaDet","SX_LP_VENT_DET");
		classTableMap.put("Marca","SX_MARCAS");
		classTableMap.put("MedidaPorCorte","SX_CORTES_MEDIDAS");
		classTableMap.put("Movimiento","SX_MOVI");
		classTableMap.put("MovimientoDet","SX_INVENTARIO_MOV");
		classTableMap.put("NotaDeCargo","SX_VENTAS");
		classTableMap.put("NotaDeCargoDet","SX_NOTADECARGO_DET");
		classTableMap.put("NotaDeCredito","SX_CXC_ABONOS");
		classTableMap.put("NotaDeCreditoBonificacion","SX_CXC_ABONOS");
		classTableMap.put("NotaDeCreditoDescuento","SX_CXC_ABONOS");
		classTableMap.put("NotaDeCreditoDet","SX_NOTA_DET");
		classTableMap.put("NotaDeCreditoDevolucion","SX_CXC_ABONOS");
		classTableMap.put("OrdenDeCorte","SX_MAQ_ORDENES");
		classTableMap.put("OrdenDeCorteDet","SX_MAQ_ORDENESDET");
		classTableMap.put("Pago","SX_CXC_ABONOS");
		classTableMap.put("PagoConCheque","SX_CXC_ABONOS");
		classTableMap.put("PagoConDeposito","SX_CXC_ABONOS");
		classTableMap.put("PagoConEfectivo","SX_CXC_ABONOS");
		classTableMap.put("PagoConTarjeta","SX_CXC_ABONOS");
		classTableMap.put("PagoDeDiferencias","SX_CXC_ABONOS");
		classTableMap.put("PagoDeRequisicion","SW_TPAGO");
		classTableMap.put("PagoEnEspecie","SX_CXC_ABONOS");
		classTableMap.put("PagoPorCambioDeCheque","SX_CXC_ABONOS");
		classTableMap.put("PagoPorCambioDeTarjeta","SX_CXC_ABONOS");
		classTableMap.put("Pedido","SX_PEDIDOS");
		classTableMap.put("PedidoDet","SX_PEDIDOSDET");
		classTableMap.put("PedidoPendiente","SX_PEDIDOS_PENDIENTES");
		classTableMap.put("Permiso","SX_PERMISOS");
		classTableMap.put("Poliza","SX_POLIZAS");
		classTableMap.put("PolizaDet","SX_POLIZASDET");
		classTableMap.put("PreDevolucion","SX_PREDEVOLUCIONES");
		classTableMap.put("PreDevolucionDet","SX_PREDEVOLUCIONES_DET");
		classTableMap.put("Producto","SX_PRODUCTOS");
		classTableMap.put("Proveedor","SX_PROVEEDORES");
		classTableMap.put("RecepcionDeCompra","SX_ENTRADA_COMPRAS");
		classTableMap.put("RecepcionDeCorte","SX_MAQ_RECEPCION_CORTE");
		classTableMap.put("RecepcionDeCorteDet","SX_MAQ_RECEPCION_CORTEDET");
		classTableMap.put("RecepcionDeMaquila","SX_RECEPCION_MAQUILA");
		classTableMap.put("Requisicion","SW_TREQUISICION");
		classTableMap.put("RequisicionDe","SW_TREQUISICIONDET");
		classTableMap.put("Role","SX_ROLE");
		classTableMap.put("SaldoDeCuenta","SX_CONTABILIDAD_SALDOS");
		classTableMap.put("SaldoDeCuentaBancaria","SW_CUENTAS_SALDOS");
		classTableMap.put("SaldoDeCuentaPorConcepto","SX_CONTABILIDAD_SALDOSDET");
		classTableMap.put("SalidaDeBobinas","SX_MAQ_SALIDA_BOBINAS");
		classTableMap.put("SalidaDeHojasDet","SX_MAQ_SALIDA_HOJEADODET");
		classTableMap.put("SalidaPorVenta","SX_INVENTARIO_FAC");
		classTableMap.put("ServicioDeTransporte","SX_TRANSPORTES_FORANEOS");
		classTableMap.put("Socio","SX_SOCIOS");
		classTableMap.put("SolicitudDeDeposito","SX_SOLICITUDES_DEPOSITO");
		classTableMap.put("SolicitudDeEmbarque","SX_EMBARQUE_SOLICITUDES");
		classTableMap.put("SolicitudDeTraslado","SX_SOLICITUD_TRASLADOS");
		classTableMap.put("Sucursal","SW_SUCURSALES");
		classTableMap.put("Tarjeta","SX_TARJETAS");
		classTableMap.put("TipoDeCambio","SX_TIPO_DE_CAMBIO");
		classTableMap.put("TipoDeCliente","SX_TIPO_CLIENTE");
		classTableMap.put("Transferencia","SW_TRANSFERENCIAS");
		classTableMap.put("Transformacion","SX_TRANSFORMACIONES");
		classTableMap.put("TransformacionDet","SX_INVENTARIO_TRS");
		classTableMap.put("Transporte","SX_TRANSPORTES");
		classTableMap.put("Traslado","SX_TRASLADOS");
		classTableMap.put("TrasladoDet","SX_INVENTARIO_TRD");
		classTableMap.put("TraspasoDeCuenta","SX_TRASPASOS_CUENTAS");
		classTableMap.put("Unidad","SX_UNIDADES");
		classTableMap.put("User","SX_USUARIOS");
		classTableMap.put("Vendedor","SX_VENDEDORES");
		classTableMap.put("Venta","SX_VENTAS");
		classTableMap.put("VentaDet","SX_VENTASDET");
		classTableMap.put("Zona","SX_EMBARQUE_ZONAS");
		classTableMap.put("ZonaDeEnvio","SX_EMBARQUES_ZONAS_CARGO");
		classTableMap.put("AsignacionVentaCE","SX_ASIGNACION_CE");
		classTableMap.put("CheckPlusOpcion", "SX_CHECKPLUS_OPCION");
		classTableMap.put("CheckPlusVenta", "SX_CHECKPLUS_VENTA");
		classTableMap.put("CheckPlusCliente", "SX_CHECKPLUS_CLIENTE");
		classTableMap.put("CheckPlusReferenciaBancaria", "SX_CHECKPLUS_REFBANCOS");
		classTableMap.put("CheckPlusDocumento", "SX_CHECKPLUS_DOCTOS");
		classTableMap.put("SolicitudDeModificacion", "SX_SOLICITUD_MODIFICACIONES");
		classTableMap.put("EstadoDeVenta", "SX_VENTA_ESTADO");
		classTableMap.put("CFDI", "SX_CFDI");
		classTableMap.put("CFDIClienteMails", "SX_CLIENTES_CFDI_MAILS");
		classTableMap.put("Gasto", "SX_GASTOS");
		
		
		

	}	

	public static void main(String[] args) {
		
	}

}
