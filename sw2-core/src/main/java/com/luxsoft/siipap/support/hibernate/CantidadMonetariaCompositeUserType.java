/*
 * Created on 27-feb-2005
 *
 * TODO Colocar informacion de licencia
 */
package com.luxsoft.siipap.support.hibernate;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Currency;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.type.Type;
import org.hibernate.usertype.CompositeUserType;

import com.luxsoft.siipap.model.CantidadMonetaria;




/**
 * Implementacion de CompositeUserType para CantidadMonetaria
 * 
 * @author Ruben Cancino 
 */
public class CantidadMonetariaCompositeUserType implements CompositeUserType {

    
    public Class returnedClass() {
        return CantidadMonetaria.class;
    }
    
    public boolean equals(Object x, Object y) throws HibernateException {
        if(x==y) return true;
        if(x==null || y==null)return false;
        return x.equals(y);
    }
    
    public int hashCode(Object o){
        return o.hashCode();
    }
    
    public Object deepCopy(Object value) throws HibernateException {
        return value;
    }
    
    public boolean isMutable(){
        return false;
    }
    
    public Object nullSafeGet(ResultSet rs, String[] names,
            SessionImplementor session, Object owner)
            throws HibernateException, SQLException {
        
        BigDecimal  value=rs.getBigDecimal(names[0]);
        if(rs.wasNull()) return null;
        Currency currency=Currency.getInstance(rs.getString(names[1]));
        return new CantidadMonetaria(value,currency,BigDecimal.ROUND_HALF_EVEN);
        
        
    }
    
    
    public void nullSafeSet(PreparedStatement st, Object value, int index,
            SessionImplementor session) throws HibernateException, SQLException {
        
        if(value==null){
            st.setNull(index,Types.NUMERIC);
            st.setNull(index+1,Types.VARCHAR);
        }else{
            CantidadMonetaria amount=(CantidadMonetaria)value;
            String currencyCode=amount.getCurrency().getCurrencyCode();
            st.setBigDecimal(index,amount.getAmount());
            st.setString(index+1,currencyCode);
        }

    }
    
    public String[] getPropertyNames() {
        return new String[]{"amount","currency"};
    }
    
    public Type[] getPropertyTypes() {
        return new Type[]{Hibernate.BIG_DECIMAL,Hibernate.CURRENCY};
    }
    
    
    public Object getPropertyValue(Object component, int property)
            throws HibernateException {
        CantidadMonetaria amount=(CantidadMonetaria)component;
        if(property==0)
            return amount.getAmount();
        return amount.getCurrency();
    }
    
    
    public void setPropertyValue(Object component, int property, Object value)
            throws HibernateException {
        throw new UnsupportedOperationException("CantidadMonetaria es Inmutable");

    }
    
    public Object assemble(Serializable cached, SessionImplementor session,
            Object owner) throws HibernateException {
        return cached;
    }
    public Serializable disassemble(Object value, SessionImplementor session)
            throws HibernateException {
        return (Serializable)value;

    }

	public Object replace(Object original, Object target, SessionImplementor session, Object owner) throws HibernateException {
		return original;
	}
}
