package com.luxsoft.sw3.services.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;

/**
 * Aspect para registrar la generacion de pedidos para embarques
 * NOTA: Status ALFA en fase de pruebas y analisis
 */
@Aspect
public class RegistroDeEmbarquesAspect {
	
	@After("execution(* com.luxsoft.sw3..*.PedidosManager.save(..))")
	public void registrarEmbarque(JoinPoint joinPoint){
		//joinPoint.
	}

}
