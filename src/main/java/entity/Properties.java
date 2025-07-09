/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package entity;

import jakarta.json.bind.annotation.JsonbTransient;
import jakarta.persistence.Basic;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;

/**
 *
 * @author Dell
 */
@Entity
@Table(name = "properties")
@NamedQueries({
    @NamedQuery(name = "Properties.findAll", query = "SELECT p FROM Properties p"),
    @NamedQuery(name = "Properties.findById", query = "SELECT p FROM Properties p WHERE p.id = :id"),
    @NamedQuery(name = "Properties.findByTitle", query = "SELECT p FROM Properties p WHERE p.title = :title"),
    @NamedQuery(name = "Properties.findByLatitude", query = "SELECT p FROM Properties p WHERE p.latitude = :latitude"),
    @NamedQuery(name = "Properties.findByLongitude", query = "SELECT p FROM Properties p WHERE p.longitude = :longitude"),
    @NamedQuery(name = "Properties.findByAddress", query = "SELECT p FROM Properties p WHERE p.address = :address"),
    @NamedQuery(name = "Properties.findByType", query = "SELECT p FROM Properties p WHERE p.type = :type"),
    @NamedQuery(name = "Properties.findByBedrooms", query = "SELECT p FROM Properties p WHERE p.bedrooms = :bedrooms"),
    @NamedQuery(name = "Properties.findByBathrooms", query = "SELECT p FROM Properties p WHERE p.bathrooms = :bathrooms"),
    @NamedQuery(name = "Properties.findByAreaSqrt", query = "SELECT p FROM Properties p WHERE p.areaSqrt = :areaSqrt"),
    @NamedQuery(name = "Properties.findByStatus", query = "SELECT p FROM Properties p WHERE p.status = :status"),
    @NamedQuery(name = "Properties.findByCreatedAt", query = "SELECT p FROM Properties p WHERE p.createdAt = :createdAt"),
    @NamedQuery(name = "Properties.findByPrice", query = "SELECT p FROM Properties p WHERE p.price = :price")})
public class Properties implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    @Basic(optional = false)
    @Column(name = "title")
    private String title;
    @Basic(optional = false)
    @Lob
    @Column(name = "description")
    private String description;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Basic(optional = false)
    @Column(name = "latitude")
    private BigDecimal latitude;
    @Basic(optional = false)
    @Column(name = "longitude")
    private BigDecimal longitude;
    @Basic(optional = false)
    @Column(name = "address")
    private String address;
    @Basic(optional = false)
    @Column(name = "type")
    private String type;
    @Basic(optional = false)
    @Column(name = "bedrooms")
    private int bedrooms;
    @Basic(optional = false)
    @Column(name = "bathrooms")
    private int bathrooms;
    @Basic(optional = false)
    @Column(name = "area_sqrt")
    private int areaSqrt;
    @Basic(optional = false)
    @Column(name = "status")
    private String status;
    @Basic(optional = false)
    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;
    @Basic(optional = false)
    @Column(name = "price")
    private long price;
    
    @JsonbTransient
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "propertyId")
    private Collection<Bookings> bookingsCollection;
    @JoinColumn(name = "seller_id", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private Users sellerId;
    
    @JsonbTransient
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "propertyId")
    private Collection<PropertyImages> propertyImagesCollection;

    public Collection<PropertyImages> getPropertyImagesCollection() {
        return propertyImagesCollection;
    }

    public void setPropertyImagesCollection(Collection<PropertyImages> propertyImagesCollection) {
        this.propertyImagesCollection = propertyImagesCollection;
    }


    public Properties() {
    }

    public Properties(Integer id) {
        this.id = id;
    }

    public Properties(Integer id, String title, String description, BigDecimal latitude, BigDecimal longitude, String address, String type, int bedrooms, int bathrooms, int areaSqrt, String status, Date createdAt, long price) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
        this.type = type;
        this.bedrooms = bedrooms;
        this.bathrooms = bathrooms;
        this.areaSqrt = areaSqrt;
        this.status = status;
        this.createdAt = createdAt;
        this.price = price;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getBedrooms() {
        return bedrooms;
    }

    public void setBedrooms(int bedrooms) {
        this.bedrooms = bedrooms;
    }

    public int getBathrooms() {
        return bathrooms;
    }

    public void setBathrooms(int bathrooms) {
        this.bathrooms = bathrooms;
    }

    public int getAreaSqrt() {
        return areaSqrt;
    }

    public void setAreaSqrt(int areaSqrt) {
        this.areaSqrt = areaSqrt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public long getPrice() {
        return price;
    }

    public void setPrice(long price) {
        this.price = price;
    }

    public Collection<Bookings> getBookingsCollection() {
        return bookingsCollection;
    }

    public void setBookingsCollection(Collection<Bookings> bookingsCollection) {
        this.bookingsCollection = bookingsCollection;
    }

    public Users getSellerId() {
        return sellerId;
    }

    public void setSellerId(Users sellerId) {
        this.sellerId = sellerId;
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
        if (!(object instanceof Properties)) {
            return false;
        }
        Properties other = (Properties) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entity.Properties[ id=" + id + " ]";
    }
    
}
