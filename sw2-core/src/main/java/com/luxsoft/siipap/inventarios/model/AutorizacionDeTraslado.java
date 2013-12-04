package com.luxsoft.siipap.inventarios.model;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import com.luxsoft.siipap.model.Autorizacion2;

@Entity
@DiscriminatorValue("TRASLADO_DE_MATERIAL")
public class AutorizacionDeTraslado extends Autorizacion2{

}
