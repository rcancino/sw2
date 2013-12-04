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

package com.luxsoft.siipap.dao.core;

import com.luxsoft.siipap.dao.hibernate.GenericDaoHibernate;
import com.luxsoft.siipap.model.core.Producto;

import java.sql.SQLException;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.orm.hibernate3.HibernateCallback;

/**
 *
 * @author Ruben Cancino
 */
public class ProductoDaoImpl extends GenericDaoHibernate<Producto,Long> implements ProductoDao{

    public ProductoDaoImpl() {
        super(Producto.class);
    }
    
    @Override
	public Producto get(final Long id) {
		List<Producto> l=getHibernateTemplate().executeFind(new HibernateCallback(){
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				return session.createQuery(" from Producto p left join fetch p.existencias e" +
						" where p.id=? order by p.clave asc")
						.setLong(0, id)
						.list();
			}
		});
		if(l.isEmpty())
			throw new ObjectRetrievalFailureException(Producto.class,id);
		return l.get(0);
	}

	@SuppressWarnings("unchecked")
	public Producto buscarPorClave(String clave) {
        List<Producto> res=getHibernateTemplate().find("from Producto p left join fetch p.existencias ex where p.clave=?", clave);
        return res.isEmpty()?null:res.get(0);
    }
    
 
	public List<Producto> buscarPorLinea(String nombre) {
		return getHibernateTemplate().find("from Producto p where p.linea.nombre=?", nombre);
	}

	public List<Producto> buscarActivos() {
		String hql="from Producto p where p.activo=? order by p.clave asc";
		return getHibernateTemplate().find(hql, Boolean.TRUE);
	}
	
	

	public List<Producto> buscarProductosActivosYDeLinea() {
		String hql="from Producto p where p.activo=true and p.deLinea=true order by p.clave asc";
		return getHibernateTemplate().find(hql);
	}

	public List<Producto> buscarInventariablesActivos() {
		String hql="from Producto p where p.activo=? and p.inventariable=?";
		return getHibernateTemplate().find(hql, new Object[]{Boolean.TRUE,Boolean.TRUE});
	}

	@Override
	public List<Producto> getAll() {
		String hql="from Producto p order by p.clave asc";
		return getHibernateTemplate().find(hql);
	}
	
	

}
