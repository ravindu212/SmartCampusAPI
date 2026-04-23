package com.smartcampus.resource;

import com.smartcampus.model.Sensor;
import com.smartcampus.store.DataStore;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorResource {

    private final DataStore store = DataStore.getInstance();

    // GET /api/v1/sensors or /api/v1/sensors?type=CO2
    @GET
    public Response getAllSensors(@QueryParam("type") String type) {
        List<Sensor> sensorList = new ArrayList<>(store.getSensors().values());

        if (type != null && !type.isEmpty()) {
            List<Sensor> filtered = new ArrayList<>();
            for (Sensor s : sensorList) {
                if (s.getType() != null && s.getType().equalsIgnoreCase(type)) {
                    filtered.add(s);
                }
            }
            return Response.ok(filtered).build();
        }

        return Response.ok(sensorList).build();
    }

    // POST /api/v1/sensors
    @POST
    public Response createSensor(Sensor sensor) {
        if (sensor.getId() == null || sensor.getId().isEmpty()) {
            return Response.status(400)
                    .entity("{\"error\":\"Sensor ID is required\"}")
                    .build();
        }
        if (sensor.getRoomId() == null || !store.getRooms().containsKey(sensor.getRoomId())) {
            throw new com.smartcampus.exception.LinkedResourceNotFoundException(
                    "The roomId '" + sensor.getRoomId() + "' does not exist in the system.");
        }

        // Validate that the roomId exists
        if (sensor.getRoomId() == null || !store.getRooms().containsKey(sensor.getRoomId())) {
            return Response.status(422)
                    .entity("{\"error\":\"The specified roomId does not exist\"}")
                    .build();
        }

        // Set default status if not provided
        if (sensor.getStatus() == null || sensor.getStatus().isEmpty()) {
            sensor.setStatus("ACTIVE");
        }

        // Add sensor ID to the room's sensorIds list
        store.getRooms().get(sensor.getRoomId()).getSensorIds().add(sensor.getId());

        store.getSensors().put(sensor.getId(), sensor);
        return Response.status(201).entity(sensor).build();
    }

    // GET /api/v1/sensors/{sensorId}
    @GET
    @Path("/{sensorId}")
    public Response getSensor(@PathParam("sensorId") String sensorId) {
        Sensor sensor = store.getSensors().get(sensorId);
        if (sensor == null) {
            return Response.status(404)
                    .entity("{\"error\":\"Sensor not found\"}")
                    .build();
        }
        return Response.ok(sensor).build();
    }

    // Sub-resource locator for readings (Part 4)
    @Path("/{sensorId}/readings")
    public SensorReadingResource getReadingResource(@PathParam("sensorId") String sensorId) {
        return new SensorReadingResource(sensorId);
    }
}
