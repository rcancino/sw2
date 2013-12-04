/*
 *  Copyright 2008 Ruben Cancino Ramos.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */

package com.luxsoft.siipap.ventas.dao;

import java.util.Date;
import java.util.List;

import com.luxsoft.siipap.cxc.model.OrigenDeOperacion;
import com.luxsoft.siipap.dao.GenericDao;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.ventas.model.Venta;

/**
 *
 * @author Ruben Cancino Ramos
 * 
 */
public interface VentaDao extends GenericDao<Venta,String>{ 
	
	public Venta eliminarCredito(final Venta v);
	
	public Venta buscarVentaInicializada(String id);
	
	public Venta buscarPorOracleId(final Long id);
	
	/**
	 * Localiza las ventas para el periodo indicado
	 * 
	 * @param p
	 * @return
	 */
	public List<Venta> buscarVentas(final Periodo p);
	
	/**
	 * Regresa una lista de las ventas con saldo para el tipo de venta 
	 * indicado. El parametro ALL regresa todas las ventas con saldo
	 * 
	 * @param tipo
	 * @return
	 */
	public List<Venta> buscarVentasConSaldo(String tipo);
	
	/**
	 * Regresa la lista de ventas para el periodo y origen indicado 
	 * 
	 * @param periodo
	 * @param origen
	 * @return
	 */
	public List<Venta> buscarVentas(Periodo periodo,OrigenDeOperacion origen);
	
	/**
	 * Regresa la lista de ventas para el cliente y periodo indicados
	 * 
	 * @param periodo
	 * @param cliente
	 * @return
	 */
	public List<Venta> buscarVentas(Periodo periodo,Cliente cliente);
	
	public Venta buscarVenta(final long sucursalId,final Long docto,final OrigenDeOperacion origen);
	
	public Venta buscarVenta(final long sucursalId,final Long docto,final OrigenDeOperacion origen,final Date fecha);
	
	
	

}
