package com.luxsoft.siipap.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.acegisecurity.GrantedAuthority;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * This class is used to represent available roles in the database.
 *
 * @author <a href="mailto:matt@raibledesigns.com">Matt Raible</a>
 *         Version by Dan Kibler dan@getrolling.com
 *         Extended to implement Acegi GrantedAuthority interface
 *         by David Carter david@carter.net
 */
@Entity
@Table(name="SX_ROLE")
public class Role extends BaseBean implements Serializable, GrantedAuthority {
    private static final long serialVersionUID = 3690197650654049848L;
    private Long id;
    private String name;
    private String description;
    private String modulo;
    
    
    private Set<Permiso> permisos=new HashSet<Permiso>();

    /**
     * Default constructor - creates a new instance with no values set.
     */
    public Role() {
    }

    /**
     * Create a new instance and set the name.
     * @param name name of the role.
     */
    public Role(final String name) {
        this.name = name;
    }

    @Id  @GeneratedValue(strategy=GenerationType.AUTO)
    public Long getId() {
        return id;
    }

    /**
     * @see org.acegisecurity.GrantedAuthority#getAuthority()
     * @return the name property (getAuthority required by Acegi's GrantedAuthority interface)
     */
    @Transient
    public String getAuthority() {
        return getName();
    }

    @Column(length=255)
    public String getName() {
        return this.name;
    }

    @Column(length=64)
    public String getDescription() {
        return this.description;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    
    

    public String getModulo() {
		return modulo;
	}

    @Column
	public void setModulo(String modulo) {
		this.modulo = modulo;
	}

	/**
     * {@inheritDoc}
     */
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Role)) {
            return false;
        }

        final Role role = (Role) o;

        return !(name != null ? !name.equals(role.name) : role.name != null);

    }

    /**
     * {@inheritDoc}
     */
    public int hashCode() {
        return (name != null ? name.hashCode() : 0);
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SIMPLE_STYLE)
                .append(this.name)
                .toString();
    }

    @ManyToMany(fetch = FetchType.EAGER) 
    @JoinTable(
            name="SX_ROLES_PERMISOS",
            joinColumns = { @JoinColumn( name="role_id") },
            inverseJoinColumns = @JoinColumn( name="permiso_id")
    )
	public Set<Permiso> getPermisos() {
		return permisos;
	}
    
    @Transient
    public List<Permiso> getPermisosAsList(){
    	return new ArrayList<Permiso>(permisos);
    }
    
    
	
	public void setPermisos(Set<Permiso> permisos) {
		this.permisos = permisos;
	}

	public boolean addPermiso(Permiso p){
		return permisos.add(p);
	}
	
	public boolean removePermiso(Permiso p){
		return permisos.remove(p);
	}
    
    
	
	public static final String ROLE_ADMIN="ROLE_ADMIN";

}
