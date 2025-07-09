/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package entity;

import jakarta.json.bind.annotation.JsonbTransient;
import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author Dell
 */
@Entity
@Table(name = "property_images")
@NamedQueries({
    @NamedQuery(name = "PropertyImages.findAll", query = "SELECT p FROM PropertyImages p"),
    @NamedQuery(name = "PropertyImages.findById", query = "SELECT p FROM PropertyImages p WHERE p.id = :id"),
    @NamedQuery(name = "PropertyImages.findByImg", query = "SELECT p FROM PropertyImages p WHERE p.img = :img"),
    @NamedQuery(name = "PropertyImages.findByCreatedAt", query = "SELECT p FROM PropertyImages p WHERE p.createdAt = :createdAt")})
public class PropertyImages implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    @Basic(optional = false)
    @Column(name = "img")
    private String img;
    @Basic(optional = false)
    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;
    
    @JoinColumn(name = "property_id", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private Properties propertyId;

    public PropertyImages() {
    }

    public PropertyImages(Integer id) {
        this.id = id;
    }

    public PropertyImages(Integer id, String img, Date createdAt) {
        this.id = id;
        this.img = img;
        this.createdAt = createdAt;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Properties getPropertyId() {
        return propertyId;
    }

    public void setPropertyId(Properties propertyId) {
        this.propertyId = propertyId;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof PropertyImages)) {
            return false;
        }
        PropertyImages other = (PropertyImages) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entity.PropertyImages[ id=" + id + " ]";
    }
    
}
