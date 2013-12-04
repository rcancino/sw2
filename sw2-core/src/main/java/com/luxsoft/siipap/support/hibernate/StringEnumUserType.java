package com.luxsoft.siipap.support.hibernate;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.usertype.EnhancedUserType;
import org.hibernate.usertype.ParameterizedType;
import org.hibernate.util.ReflectHelper;

/**
 * String Enumeration 
 * 
 * @author Ruben Cancino
 *
 */
public class StringEnumUserType implements EnhancedUserType,ParameterizedType{
	
	private Class<Enum> enumClass;
	
	@SuppressWarnings("unchecked")
	public void setParameterValues(Properties parameters) {
		final String enumClassName=parameters.getProperty("enumClassName");
		try {			
			enumClass=ReflectHelper.classForName(enumClassName);
		} catch (ClassNotFoundException e) {
			throw new HibernateException("Enum class not found",e);
			
		}		
	}
	
	public Class returnedClass() {
		return enumClass;
	}
	
	public int[] sqlTypes() {		
		return new int[]{Hibernate.STRING.sqlType()};
	}

	@SuppressWarnings("unchecked")
	public Object fromXMLString(String xmlValue) {		
		return Enum.valueOf(enumClass, xmlValue);
	}

	public String objectToSQLString(Object value) {		
		return "\'"+((Enum)value).name()+"\'";
	}

	public String toXMLString(Object value) {		
		return ((Enum)value).name();
	}
	

	@SuppressWarnings("unchecked")
	public Object nullSafeGet(ResultSet rs, String[] names, Object owner) throws HibernateException, SQLException {
		final String name=rs.getString(names[0]);
		return rs.wasNull()?null:Enum.valueOf(enumClass, name);
	}

	public void nullSafeSet(PreparedStatement st, Object value, int index) throws HibernateException, SQLException {
		if(value==null){
			st.setNull(index, Hibernate.STRING.sqlType());
		}else
			st.setString(index, ((Enum)value).name());
	}
	
	/*** Normal methods for an inmmutable type***/
	
	public boolean isMutable() {		
		return false;
	}

	public Object deepCopy(Object value) throws HibernateException {		
		return value;
	}

	public Serializable disassemble(Object value) throws HibernateException {		
		return (Serializable)value;
	}

	public Object replace(Object original, Object target, Object owner) throws HibernateException {		
		return original;
	}
	public Object assemble(Serializable cached, Object owner) throws HibernateException {		
		return cached;
	}		

	public boolean equals(Object x, Object y) throws HibernateException {
		if(x==y) return true;
		if(x==null || y==null) return false;
		return x.equals(y);
	}

	public int hashCode(Object x) throws HibernateException {		
		return x.hashCode();
	}
	
	/*** End Normal methods for an inmmutable type***/

}
