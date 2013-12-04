package com.luxsoft.sw3.embarques.ui.forms;

import org.springframework.util.ClassUtils;

import com.jgoodies.binding.beans.Model;
import com.jgoodies.validation.ValidationResult;
import com.jgoodies.validation.util.PropertyValidationSupport;

import com.luxsoft.siipap.swing.form2.DefaultFormModel;

/**
 * Model para el mantenimiento de las comisiones para chofer por traslados
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class ComisionPorTrasladoFormModel extends DefaultFormModel{
	
	public ComisionPorTrasladoFormModel(){
		this(new ComisionPorTraslado());
	}

	public ComisionPorTrasladoFormModel(ComisionPorTraslado traslado) {
		super(traslado);
		
	}
	
	public ComisionPorTraslado getTraslado(){
		return (ComisionPorTraslado)getBaseBean();
	}

	public ValidationResult validate(){
		final Class clazz=getBaseBeanClass();
		final String role=ClassUtils.getShortName(clazz);
		final PropertyValidationSupport support =new PropertyValidationSupport(clazz,role);
		return support.getResult();
	}
	
	
	public static class ComisionPorTraslado extends Model{
		
		private double precioPorTonelada;
		private double comision=1.1;
		private String comentario;
		
		public double getPrecioPorTonelada() {
			return precioPorTonelada;
		}
		public void setPrecioPorTonelada(double precioPorTonelada) {
			double old=this.precioPorTonelada;
			this.precioPorTonelada = precioPorTonelada;
			firePropertyChange("precioPorTonelada", old, precioPorTonelada);
		}
		public String getComentario() {
			return comentario;
		}
		public void setComentario(String comentario) {
			Object old=this.comentario;
			this.comentario = comentario;
			firePropertyChange("comentario", old, comentario);
		}
		public double getComision() {
			return comision;
		}
		public void setComision(double comision) {
			double old=this.comision;
			this.comision = comision;
			firePropertyChange("comision", old, comision);
		}
		
		
		
		
	}

}
