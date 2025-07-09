package api;

import dto.PropertyDTO;
import entity.Properties;
import entity.Users;
import jakarta.ejb.Stateless;
import jakarta.persistence.*;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.util.Date;
import java.util.List;

@Stateless
@Path("/properties")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PropertyService {

    @PersistenceContext(unitName = "RealEstatePU")
    private EntityManager em;

    @GET
    public Response getAllProperties() {
        List<Properties> properties = em.createNamedQuery("Properties.findAll", Properties.class).getResultList();
        return Response.ok(properties).build();
    }

    @GET
    @Path("/{id}")
    public Response getProperty(@PathParam("id") int id) {
        Properties property = em.find(Properties.class, id);
        if (property == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Property not found").build();
        }

        property.getSellerId().getName();
        return Response.ok(property).build();
    }

    @GET
    @Path("/seller/{userId}")
    public Response getPropertiesBySeller(@PathParam("userId") Integer userId) {
        try {
            List<Properties> properties = em.createQuery(
                    "SELECT p FROM Properties p WHERE p.sellerId.id = :userId", Properties.class)
                    .setParameter("userId", userId)
                    .getResultList();

            List<PropertyDTO> dtos = properties.stream()
                    .map(this::toDTOWithImages)
                    .toList();

            return Response.ok(dtos).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error retrieving properties by user.").build();
        }
    }

    
@GET
@Path("/search")
public Response searchProperties(@QueryParam("query") String query) {
    try {
        if (query == null || query.trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Query parameter is required.")
                    .build();
        }

        String searchText = "%" + query.toLowerCase() + "%";

        List<Properties> properties = em.createQuery(
                "SELECT p FROM Properties p WHERE LOWER(p.title) LIKE :query OR LOWER(p.address) LIKE :query",
                Properties.class)
                .setParameter("query", searchText)
                .getResultList();

        List<PropertyDTO> dtos = properties.stream()
                .map(this::toDTOWithImages)
                .toList();

        return Response.ok(dtos).build();
    } catch (Exception e) {
        e.printStackTrace();
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("Error occurred during search.")
                .build();
    }
}

    
    
    @POST
    public Response createProperty(PropertyDTO dto) {
        try {
            if (dto.seller_id == null) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Missing seller_id").build();
            }
            Users seller = em.find(Users.class, dto.seller_id);
            if (seller == null) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Seller not found").build();
            }

            Properties property = new Properties();
            property.setSellerId(seller);
            property.setTitle(dto.title);
            property.setDescription(dto.description);
            property.setLatitude(dto.latitude);
            property.setLongitude(dto.longitude);
            property.setAddress(dto.address);
            property.setType(dto.type);
            property.setBedrooms(dto.bedrooms);
            property.setBathrooms(dto.bathrooms);
            property.setAreaSqrt(dto.area_sqrt);
            property.setStatus(dto.status);
            property.setPrice(dto.price);
            property.setCreatedAt(new Date());

            em.persist(property);
            em.flush(); // Ensures ID is generated before returning

            return Response.status(Response.Status.CREATED).entity(property).build(); // ✅ Return full property object
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.BAD_REQUEST).entity("Error: " + e.getMessage()).build();
        }
    }

    @GET
    @Path("/type/{type}")
    public Response getPropertiesByTypeWithImages(@PathParam("type") String type) {
        try {
            List<Properties> properties = em.createQuery(
                    "SELECT p FROM Properties p WHERE p.type = :type", Properties.class)
                    .setParameter("type", type)
                    .getResultList();

            List<PropertyDTO> dtos = properties.stream()
                    .map(this::toDTOWithImages)
                    .toList();

            return Response.ok(dtos).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error retrieving properties by type.")
                    .build();
        }
    }

    @GET
    @Path("/with-images")
    public Response getAllPropertiesWithImages() {
        List<Properties> properties = em.createNamedQuery("Properties.findAll", Properties.class).getResultList();

        List<PropertyDTO> dtos = properties.stream()
                .map(this::toDTOWithImages)
                .toList();

        return Response.ok(dtos).build();
    }

    @PUT
    @Path("/{id}")
    public Response updateProperty(@PathParam("id") int id, Properties updated) {
        Properties existing = em.find(Properties.class, id);
        if (existing == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Property not found").build();
        }

        existing.setTitle(updated.getTitle());
        existing.setDescription(updated.getDescription());
        existing.setLatitude(updated.getLatitude());
        existing.setLongitude(updated.getLongitude());
        existing.setAddress(updated.getAddress());
        existing.setType(updated.getType());
        existing.setBedrooms(updated.getBedrooms());
        existing.setBathrooms(updated.getBathrooms());
        existing.setAreaSqrt(updated.getAreaSqrt());
        existing.setStatus(updated.getStatus());
        existing.setPrice(updated.getPrice());

        em.merge(existing);
        return Response.ok("Property updated").build();
    }

    @DELETE
    @Path("/{id}")
    public Response deleteProperty(@PathParam("id") int id) {
        Properties property = em.find(Properties.class, id);
        if (property == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Property not found").build();
        }
        em.remove(property);
        return Response.ok("Property deleted").build();
    }
    
    private PropertyDTO toDTOWithImages(Properties p) {
        PropertyDTO dto = new PropertyDTO();

        dto.id = p.getId();
        dto.seller_id = p.getSellerId().getId();
        dto.seller_name = p.getSellerId().getName();
        dto.seller_email = p.getSellerId().getEmail();
        dto.title = p.getTitle();
        dto.description = p.getDescription();
        dto.latitude = p.getLatitude();
        dto.longitude = p.getLongitude();
        dto.address = p.getAddress();
        dto.type = p.getType();
        dto.bedrooms = p.getBedrooms();
        dto.bathrooms = p.getBathrooms();
        dto.area_sqrt = p.getAreaSqrt();
        dto.status = p.getStatus();
        dto.price = p.getPrice();

        dto.imageLinks = p.getPropertyImagesCollection()
                         .stream()
                         .map(img -> img.getImg())
                         .toList();

        return dto;
    }
}
