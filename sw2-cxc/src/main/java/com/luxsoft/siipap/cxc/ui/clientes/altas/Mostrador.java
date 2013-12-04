/**
 * 
 */
package com.luxsoft.siipap.cxc.ui.clientes.altas;

import java.text.MessageFormat;

import org.apache.commons.lang.StringUtils;
import org.hibernate.validator.AssertTrue;
import org.hibernate.validator.Length;

import com.luxsoft.luxor.utils.Bean;

public class Mostrador{
	
		    
    @Length (max=100)
    private String nombre;	    
    
    private boolean personaFisica = false;	    
    
    @Length (max=50)
    private String apellidoP;	    
    
    @Length (max=50)
    private String apellidoM;
    
    @Length (max=250)
    private String nombres;

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public boolean isPersonaFisica() {
		return personaFisica;
	}

	public void setPersonaFisica(boolean personaFisica) {
		this.personaFisica = personaFisica;
	}		

	public String getApellidoP() {
		return apellidoP;
	}

	public void setApellidoP(String apellidoP) {
		this.apellidoP = apellidoP;
	}

	public String getApellidoM() {
		return apellidoM;
	}

	public void setApellidoM(String apellidoM) {
		this.apellidoM = apellidoM;
	}	
	
    
    public String getNombres() {
		return nombres;
	}

	public void setNombres(String nombres) {
		this.nombres = nombres;
	}

	public static Mostrador getMostrador(){
    	Mostrador cliente=(Mostrador)Bean.proxy(Mostrador.class);
    	return cliente;
    }
	
	/**
	 * 
	 * @return
	 */
	@AssertTrue(message="El nombre es incorrecto")
	public boolean valid(){
		if(isPersonaFisica()){
			return( StringUtils.isNotBlank(apellidoP)
			&& StringUtils.isNotBlank(apellidoM)
			&& StringUtils.isNotBlank(nombres));
		}else
			return StringUtils.isNotBlank(nombre);
	}
	
	public String toString(){
		String pattern = "{0} {1} {2} ";
		return MessageFormat.format(pattern, getApellidoP(),getApellidoM(),getNombres());
	}
    
}