package com.luxsoft.siipap.cxc.ui.model;

import java.util.Date;

import org.hibernate.validator.AssertTrue;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotEmpty;

import com.luxsoft.luxor.utils.Bean;
import com.luxsoft.siipap.cxc.model.Cargo;
import com.luxsoft.siipap.model.Autorizacion2;
import com.luxsoft.siipap.model.core.Descuento;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.siipap.ventas.model.DescuentoEspecial;

public class DescuentoEspecialFormModel extends DefaultFormModel{
	
	private final Cargo cargo;
	
	
	public DescuentoEspecialFormModel(final Cargo cargo) {
		super(Bean.proxy(DescuentoEspecialModel.class));
		this.cargo=cargo;
	}
	
	public Cargo getCargo(){
		return cargo;
	}
	
	public DescuentoEspecial commit(){
		DescuentoEspecial de=new DescuentoEspecial();
		Descuento d=new Descuento();
		d.setDescripcion((String)getValue("comentarioDescuento"));
		d.setDescuento((Double)getValue("descuento"));
		de.setDescuento(d);
		Autorizacion2 aut=new Autorizacion2();
		aut.setComentario((String)getValue("comentarioAutorizacion"));
		aut.setFechaAutorizacion((Date)getValue("fecha"));
		de.setAutorizacion(aut);
		de.setCargo(cargo);
		return de;
	}

	public static class DescuentoEspecialModel{
		
		private Date fecha=new Date();
		
		@Length(max=255)
		@NotEmpty(message="Se requiere comentario para autorizacion")
		private String comentarioAutorizacion;
		
		@Length(max=255)
		@NotEmpty(message="Se requiere comentario del descuento")
		private String comentarioDescuento;
		
		
		private double descuento;
		
		public Date getFecha() {
			return fecha;
		}
		public void setFecha(Date fecha) {
			this.fecha = fecha;
		}
		public String getComentarioAutorizacion() {
			return comentarioAutorizacion;
		}
		public void setComentarioAutorizacion(String comentario) {
			this.comentarioAutorizacion = comentario;
		}
		public String getComentarioDescuento() {
			return comentarioDescuento;
		}
		public void setComentarioDescuento(String comentarioDescuento) {
			this.comentarioDescuento = comentarioDescuento;
		}
		public double getDescuento() {
			return descuento;
		}
		public void setDescuento(double descuento) {
			this.descuento = descuento;
		}
		
		@AssertTrue(message="Descuento incorrecto (Rango: 1 a 99)")
		public boolean validar(){
			return descuento>0 && descuento<99;
		}
		
	}

}
