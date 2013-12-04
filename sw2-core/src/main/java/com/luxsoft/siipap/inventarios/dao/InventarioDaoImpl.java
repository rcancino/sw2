/*
 *  Copyright 2008 Ruben Cancino <rcancino@luxsoftnet.com>.
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

package com.luxsoft.siipap.inventarios.dao;


import java.sql.SQLException;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

import com.luxsoft.siipap.compras.model.EntradaPorCompra;
import com.luxsoft.siipap.dao.hibernate.GenericDaoHibernate;
import com.luxsoft.siipap.inventarios.model.Inventario;

/**
 *
 * @author Ruben Cancino <rcancino@luxsoftnet.com>
 */
public class InventarioDaoImpl extends GenericDaoHibernate<Inventario,String> implements InventarioDao{

    public InventarioDaoImpl() {
        super(Inventario.class);
    }

	public void eliminarEntradaPorCompra(final EntradaPorCompra c) {
		getHibernateTemplate().execute(new HibernateCallback(){
			public Object doInHibernate(Session session)
					throws HibernateException, SQLException {
				EntradaPorCompra ee=(EntradaPorCompra)session.get(EntradaPorCompra.class, c.getId());
				//ee.eliminar();
				session.delete(ee);
				return null;
			}
			
		});
		
	}

}
