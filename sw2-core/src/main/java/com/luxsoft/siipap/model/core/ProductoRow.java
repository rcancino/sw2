package com.luxsoft.siipap.model.core;

import org.apache.commons.lang.StringUtils;



public class ProductoRow {
	
	 
	    private String clave;
	    private String descripcion;
	    private int gramos=0;
	    private boolean nacional=true;
	    private String linea;
	    private String clase;
	    private String marca;
	    private int caras=1;
	    private String acabado;
	    private String presentacion;
	    private double kilos=0;
	    private double largo=0;
	    private double ancho=0;
	    private int calibre=0;   
		
	    
	    public ProductoRow() {}
	    public ProductoRow(Producto p){
	    	this(p.getClave(),p.getDescripcion(),p.getGramos());
	    }
	    public ProductoRow(String clave, String descripcion,int gramos) {
			this.clave = clave;
			this.descripcion = descripcion;
			this.gramos=gramos;
		}

		public String getClave() {
			return clave;
		}

		public void setClave(String clave) {
			this.clave = clave;
		}

		public String getDescripcion() {
			return descripcion;
		}

		public void setDescripcion(String descripcion) {
			this.descripcion = descripcion;
		}
		public int getGramos() {
			return gramos;
		}
		public void setGramos(int gramos) {
			this.gramos = gramos;
		}

		
		public boolean isNacional() {
			return nacional;
		}
		public void setNacional(boolean nacional) {
			this.nacional = nacional;
		}
		public String getLinea() {
			return linea;
		}
		public void setLinea(String linea) {
			this.linea = linea;
		}
		public String getClase() {
			return clase;
		}
		public void setClase(String clase) {
			this.clase = clase;
		}
		public String getMarca() {
			return marca;
		}
		public void setMarca(String marca) {
			this.marca = marca;
		}
		public int getCaras() {
			return caras;
		}
		public void setCaras(int caras) {
			this.caras = caras;
		}
		public String getAcabado() {
			return acabado;
		}
		public void setAcabado(String acabado) {
			this.acabado = acabado;
		}
		public String getPresentacion() {
			return presentacion;
		}
		public void setPresentacion(String presentacion) {
			this.presentacion = presentacion;
		}
		public double getKilos() {
			return kilos;
		}
		public void setKilos(double kilos) {
			this.kilos = kilos;
		}
		public double getLargo() {
			return largo;
		}
		public void setLargo(double largo) {
			this.largo = largo;
		}
		public double getAncho() {
			return ancho;
		}
		public void setAncho(double ancho) {
			this.ancho = ancho;
		}
		public int getCalibre() {
			return calibre;
		}
		public void setCalibre(int calibre) {
			this.calibre = calibre;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((clave == null) ? 0 : clave.hashCode());
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
			ProductoRow other = (ProductoRow) obj;
			if (clave == null) {
				if (other.clave != null)
					return false;
			} else if (!clave.equals(other.clave))
				return false;
			return true;
		}

		

		@Override
	    public String toString() {
	    	return StringUtils
	    		.rightPad(getClave(),10,' ')+"  "+getDescripcion()+"   "+getGramos()+"g.";
	    }

}
