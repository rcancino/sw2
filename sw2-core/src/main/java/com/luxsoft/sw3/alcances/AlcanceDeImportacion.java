package com.luxsoft.sw3.alcances;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.luxsoft.siipap.util.SQLUtils;

public class AlcanceDeImportacion {
	
	private double meses;
	
	private String PROVEEDOR;
	
	private String SUCURSAL;
	
	private BigDecimal MESES_POR_PERIODO;
	
	private String LINEA;
	
	private String CLASE;
	
	private String MARCA;
	
	private String CLAVE;
	
	private String DESCRIPCION;
	
	private double ANCHO;
	private double LARGO;
	private double GRAMOS;

	private boolean DELINEA;
	
	private double KILOS;
	
	private double EXISTENCIA;
	
	private double VENTAS;
	
	private double PROMEDIO_VTA;
	
	private double ALCANCE_INV;
	
	private double PEDIDOS_SOLICITADOS;
	private double DEPURADOS;
	private double ADUANA;
	private double ENTRADA;

	private double PEDIDOS_PENDIENTES;
	
	private double ALCANCE_PED;
	private double EXISTENCIA_TON;
	private double PROMEDIO_VTA_TON;
	private double PEDIDOS_PEND_TON;

	
	
	
	public double getMeses() {
		return meses;
	}

	public void setMeses(double meses) {
		this.meses = meses;
	}

	public String getSUCURSAL() {
		return SUCURSAL;
	}

	public void setSUCURSAL(String sucursal) {
		SUCURSAL = sucursal;
	}


	public BigDecimal getMESES_POR_PERIODO() {
		return MESES_POR_PERIODO;
	}

	public void setMESES_POR_PERIODO(BigDecimal meses_por_periodo) {
		MESES_POR_PERIODO = meses_por_periodo;
	}


	public String getLINEA() {
		return LINEA;
	}


	public void setLINEA(String linea) {
		LINEA = linea;
	}


	public String getCLASE() {
		return CLASE;
	}


	public void setCLASE(String clase) {
		CLASE = clase;
	}


	public String getMARCA() {
		return MARCA;
	}


	public void setMARCA(String marca) {
		MARCA = marca;
	}


	public String getCLAVE() {
		return CLAVE;
	}


	public void setCLAVE(String clave) {
		CLAVE = clave;
	}


	public String getDESCRIPCION() {
		return DESCRIPCION;
	}


	public void setDESCRIPCION(String descripcion) {
		DESCRIPCION = descripcion;
	}
	
	


	public boolean isDELINEA() {
		return DELINEA;
	}

	public void setDELINEA(boolean delinea) {
		DELINEA = delinea;
	}

	public double getKILOS() {
		return KILOS;
	}


	public double getANCHO() {
		return ANCHO;
	}

	public void setANCHO(double ancho) {
		ANCHO = ancho;
	}

	public double getLARGO() {
		return LARGO;
	}

	public void setLARGO(double largo) {
		LARGO = largo;
	}

	public double getGRAMOS() {
		return GRAMOS;
	}

	public void setGRAMOS(double gramos) {
		GRAMOS = gramos;
	}

	public double getPEDIDOS_SOLICITADOS() {
		return PEDIDOS_SOLICITADOS;
	}

	public void setPEDIDOS_SOLICITADOS(double pedidos_solicitados) {
		PEDIDOS_SOLICITADOS = pedidos_solicitados;
	}

	public double getDEPURADOS() {
		return DEPURADOS;
	}

	public void setDEPURADOS(double depurados) {
		DEPURADOS = depurados;
	}

	public double getADUANA() {
		return ADUANA;
	}

	public void setADUANA(double aduana) {
		ADUANA = aduana;
	}

	public double getENTRADA() {
		return ENTRADA;
	}

	public void setENTRADA(double entrada) {
		ENTRADA = entrada;
	}

	public double getALCANCE_PED() {
		return ALCANCE_PED;
	}

	public void setALCANCE_PED(double alcance_ped) {
		ALCANCE_PED = alcance_ped;
	}

	public double getEXISTENCIA_TON() {
		return EXISTENCIA_TON;
	}

	public void setEXISTENCIA_TON(double existencia_ton) {
		EXISTENCIA_TON = existencia_ton;
	}

	public double getPROMEDIO_VTA_TON() {
		return PROMEDIO_VTA_TON;
	}

	public void setPROMEDIO_VTA_TON(double promedio_vta_ton) {
		PROMEDIO_VTA_TON = promedio_vta_ton;
	}

	public double getPEDIDOS_PEND_TON() {
		return PEDIDOS_PEND_TON;
	}

	public void setPEDIDOS_PEND_TON(double pedidos_pend_ton) {
		PEDIDOS_PEND_TON = pedidos_pend_ton;
	}

	public void setKILOS(double kilos) {
		KILOS = kilos;
	}


	public double getEXISTENCIA() {
		return EXISTENCIA;
	}


	public void setEXISTENCIA(double existencia) {		
		EXISTENCIA = existencia;
	}


	public double getVENTAS() {
		return VENTAS;
	}


	public void setVENTAS(double ventas) {
		VENTAS = ventas;
	}


	public double getPROMEDIO_VTA() {
		return PROMEDIO_VTA;
	}


	public void setPROMEDIO_VTA(double promedio_vta) {
		PROMEDIO_VTA = promedio_vta;
	}


	public double getALCANCE_INV() {
		return ALCANCE_INV;
	}


	public void setALCANCE_INV(double alcance_inv) {
		ALCANCE_INV = alcance_inv;
	}


	public double getPEDIDOS_PENDIENTES() {
		return PEDIDOS_PENDIENTES;
	}


	public void setPEDIDOS_PENDIENTES(double pedidos_pendientes) {
		PEDIDOS_PENDIENTES = pedidos_pendientes;
	}

	public double getAlcanceProyectado(){
		if(getPROMEDIO_VTA()!=0){
			double res=(getEXISTENCIA()+getPEDIDOS_PENDIENTES())/getPROMEDIO_VTA();
			return new BigDecimal(res).setScale(1, RoundingMode.CEILING).doubleValue();
		}return 0;
	}
	
	public double getPorPedir(){
		double res= (getPROMEDIO_VTA()*getMeses())-(getEXISTENCIA()+getPEDIDOS_PENDIENTES());
		return res<0?0:Math.ceil(res);
	}
	
	public double getPorPedirTon(){
		double res= (((getPROMEDIO_VTA()*getMeses())-(getEXISTENCIA()+getPEDIDOS_PENDIENTES()))*getKILOS())/1000;
		return res<0?0:Math.ceil(res);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((CLAVE == null) ? 0 : CLAVE.hashCode());
		result = prime * result
				+ ((SUCURSAL == null) ? 0 : SUCURSAL.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AlcanceDeImportacion other = (AlcanceDeImportacion) obj;
		if (CLAVE == null) {
			if (other.CLAVE != null)
				return false;
		} else if (!CLAVE.equals(other.CLAVE))
			return false;
		if (SUCURSAL == null) {
			if (other.SUCURSAL != null)
				return false;
		} else if (!SUCURSAL.equals(other.SUCURSAL))
			return false;
		return true;
	}


	public static void main(String[] args) {
		String sql=SQLUtils.loadSQLQueryFromResource("sql/alcances_v2.sql");
		//System.out.println(sql);
		SQLUtils.printBeanClasFromSQL(sql);
	}

	public void setPROVEEDOR(String pROVEEDOR) {
		PROVEEDOR = pROVEEDOR;
	}

	public String getPROVEEDOR() {
		return PROVEEDOR;
	}
	
	public double getToneladasExis(){
		return getEXISTENCIA()*getKILOS()/1000;
	}
	
	public double getToneladasPromVenta(){
		return getPROMEDIO_VTA()*getKILOS()/1000;
	}
	
	public double getToneladasPorPedir(){
		return getPorPedir()*getKILOS()/1000;
	}

}
