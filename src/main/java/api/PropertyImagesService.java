package api;

import dto.PropertyImageDTO;
import entity.PropertyImages;
import entity.Properties;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;

@Stateless
@Path("property-images")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PropertyImagesService {

    @PersistenceContext(unitName = "RealEstatePU")
    private EntityManager em;

    @GET
    public List<PropertyImages> getAll() {
        return em.createNamedQuery("PropertyImages.findAll", PropertyImages.class).getResultList();
    }
    
    @GET
    @Path("/property/{propertyId}")
    public List<PropertyImages> getImagesByPropertyId(@PathParam("propertyId") int propertyId) {
        return em.createQuery("SELECT p FROM PropertyImages p WHERE p.propertyId.id = :propertyId", PropertyImages.class)
                 .setParameter("propertyId", propertyId)
                 .getResultList();
    }

    @GET
    @Path("{id}")
    public PropertyImages getById(@PathParam("id") Integer id) {
        return em.find(PropertyImages.class, id);
    }

    @POST
    public Response create(List<PropertyImageDTO> imageDTOs) {
        try {
            List<PropertyImages> savedImages = new ArrayList<>();

            for (PropertyImageDTO dto : imageDTOs) {
                // Find the referenced property
                Properties property = em.find(Properties.class, dto.property_id);
                if (property == null) {
                    return Response.status(Response.Status.BAD_REQUEST)
                        .entity("Property with ID " + dto.property_id + " not found.")
                        .build();
                }

                // Map DTO to entity
                PropertyImages img = new PropertyImages();
                img.setPropertyId(property);
                img.setImg(dto.img);
                img.setCreatedAt(new Date());

                em.persist(img);
                savedImages.add(img);
            }

            return Response.status(Response.Status.CREATED).entity(savedImages).build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("Error while processing image upload.").build();
        }
    }

    @PUT
    @Path("{id}")
    public void update(@PathParam("id") Integer id, PropertyImages img) {
        img.setId(id);
        em.merge(img);
    }

    @DELETE
    @Path("{id}")
    public void delete(@PathParam("id") Integer id) {
        PropertyImages existing = em.find(PropertyImages.class, id);
        if (existing != null) {
            em.remove(existing);
        }
    }
}
