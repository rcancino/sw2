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

import java.sql.SQLException;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.SetUtils;
import org.hibernate.FetchMode;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.UniqueList;

import com.luxsoft.siipap.dao.hibernate.GenericDaoHibernate;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.model.core.ClienteRow;

/**
 *
 * @author Ruben Cancino
 */
@SuppressWarnings("unchecked")
public class ClienteDaoImpl extends GenericDaoHibernate<Cliente,Long> implements ClienteDao{

    public ClienteDaoImpl() {
        super(Cliente.class);
    }
    
    

    @Transactional(propagation=Propagation.REQUIRED)
    public Cliente buscarPorClave(String clave) {
        List<Cliente> clientes=getHibernateTemplate().find(
        		"from Cliente c where c.clave=?"
        		, clave);
        if(clientes.isEmpty()){
        	return null;
        }else{
        	return get(clientes.get(0).getId());
        }
        
    }

    public void buscarClientes(final List<ClienteRow> clientes){
		getHibernateTemplate().execute(new HibernateCallback(){
			public Object doInHibernate(Session session)
					throws HibernateException, SQLException {
				List res=session.createQuery("select c.id,c.nombre from Cliente c order by c.nombre desc")
				.setMaxResults(1000)
				.list();
				for(Object o:res){
					Object[] row=(Object[])o;
					ClienteRow cl=new ClienteRow((Long)row[0],"",(String)row[1],"");
					clientes.add(cl);
				}
				return null;
			}
			
		});
	}

	
	@Override
	@Transactional(propagation=Propagation.REQUIRED)
	public Cliente get(final Long id) {
		
		Cliente c=(Cliente)getHibernateTemplate().execute(new HibernateCallback(){
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				Cliente c=(Cliente)session.get(Cliente.class, id);
				if(c!=null){
					if(!c.getComentarios().isEmpty())
						c.getComentarios().entrySet().iterator().next();
					if(!c.getDirecciones().isEmpty())
						c.getDirecciones().entrySet().iterator().next();
					
					if(!c.getTelefonos().isEmpty())
						c.getTelefonos().entrySet().iterator().next();
					if(!c.getContactos().isEmpty())
						c.getContactos().iterator().next();
					if(!c.getCuentas().isEmpty())
						c.getCuentas().iterator().next();
										
				}
				return c;
			}
			
		});
		if(c==null){
			throw new ObjectRetrievalFailureException(Cliente.class,id);
		}
		return c;
		
	}

	public List<Cliente> buscarClientePorClave(final String clave) {
		return getHibernateTemplate().executeFind(new HibernateCallback(){
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				EventList<Cliente> res=GlazedLists.eventList(session.createCriteria(Cliente.class)
				.setFetchMode("contactos", FetchMode.JOIN)
				.setFetchMode("telefonos", FetchMode.JOIN)
				.add(Restrictions.like("clave", clave, MatchMode.START).ignoreCase())
				.setMaxResults(500)
				.list());
				return new UniqueList<Cliente>(res,GlazedLists.beanPropertyComparator(Cliente.class, "id"));
			}
			
		});
	}

	public List<Cliente> buscarClientePorNombre(final String nombre) {
		return getHibernateTemplate().executeFind(new HibernateCallback(){
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				EventList<Cliente> res=GlazedLists.eventList(session.createCriteria(Cliente.class)
				.setFetchMode("contactos", FetchMode.JOIN)
				.setFetchMode("telefonos", FetchMode.JOIN)
				.add(Restrictions.like("nombre", nombre, MatchMode.ANYWHERE).ignoreCase())
				.setMaxResults(500)
				.list());
				return new UniqueList<Cliente>(res,GlazedLists.beanPropertyComparator(Cliente.class, "id"));
			}
			
		});
	}

	public List<Cliente> buscarClientesCredito() {
		return getHibernateTemplate().find("from Cliente c  left join fetch c.credito where c.credito!=null");
	}
    
	
	/**
     * Regresa una lista de las cuentas registradas para el cliente
     * 
     * @param clave
     * @return
     */
    public Set<String> buscarCuentasRegistradas(final String clave){
    	final List<Cliente> cliente=getHibernateTemplate().find("from Cliente c left join fetch c.cuentas x where c.clave=?", clave);
    	if(!cliente.isEmpty())
    		return cliente.get(0).getCuentas();
    	return SetUtils.EMPTY_SET;
    }
    
    public void eliminarCredito(final String clave){
    	getHibernateTemplate().bulkUpdate("delete from ClienteCredito where clave=?", clave);
    }
    

}
