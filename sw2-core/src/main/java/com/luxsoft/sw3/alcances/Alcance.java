package com.luxsoft.sw3.alcances;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.luxsoft.siipap.util.SQLUtils;

public class Alcance {
	
	private double meses;
	
	private String SUCURSAL;
	
	private BigDecimal MESES_POR_PERIODO;
	
	private String LINEA;
	
	private String CLASE;
	
	private String MARCA;
	
	private String CLAVE;
	
	private String DESCRIPCION;
	
	private boolean DELINEA;
	
	private double KILOS;
	
	private double EXISTENCIA;
	
	private double VENTAS;
	
	private double PROMEDIO_VTA;
	
	private double ALCANCE_TOTAL;
	
	private double PEDIDOS_PENDIENTES;
	
	private String PROVEEDOR;
	
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


	public double getALCANCE_TOTAL() {
		return ALCANCE_TOTAL;
	}


	public void setALCANCE_TOTAL(double alcance_total) {
		ALCANCE_TOTAL = alcance_total;
	}


	public double getPEDIDOS_PENDIENTES() {
		return PEDIDOS_PENDIENTES;
	}


	public void setPEDIDOS_PENDIENTES(double pedidos_pendientes) {
		PEDIDOS_PENDIENTES = pedidos_pendientes;
	}	
	
	public String getPROVEEDOR() {
		return PROVEEDOR;
	}

	public void setPROVEEDOR(String proveedor) {
		PROVEEDOR = proveedor;
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
		Alcance other = (Alcance) obj;
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
		String sql=SQLUtils.loadSQLQueryFromResource("sql/alcances_v1.sql");
		//System.out.println(sql);
		SQLUtils.printBeanClasFromSQL(sql);
	}

}
