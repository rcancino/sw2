package com.luxsoft.siipap.inventarios.model;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import com.luxsoft.siipap.model.Autorizacion2;

@Entity
@DiscriminatorValue("INVENTARIOS_MOV")
public class AutorizacionDeMovimiento extends Autorizacion2{

}
