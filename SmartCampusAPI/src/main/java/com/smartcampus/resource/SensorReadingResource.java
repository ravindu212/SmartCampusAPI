package com.smartcampus.resource;

import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;
import com.smartcampus.store.DataStore;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorReadingResource {

    private final String sensorId;
    private final DataStore store = DataStore.getInstance();

    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }

    // GET /api/v1/sensors/{sensorId}/readings
    @GET
    public Response getReadings() {
        Sensor sensor = store.getSensors().get(sensorId);
        if (sensor == null) {
            return Response.status(404)
                    .entity("{\"error\":\"Sensor not found\"}")
                    .build();
        }
        List<SensorReading> list = store.getReadingsForSensor(sensorId);
        return Response.ok(list).build();
    }

    // POST /api/v1/sensors/{sensorId}/readings
    @POST
    public Response addReading(SensorReading reading) {
        Sensor sensor = store.getSensors().get(sensorId);
        if (sensor == null) {
            return Response.status(404)
                    .entity("{\"error\":\"Sensor not found\"}")
                    .build();
        }

        if ("MAINTENANCE".equalsIgnoreCase(sensor.getStatus())) {
            throw new com.smartcampus.exception.SensorUnavailableException(sensorId);
        }

        // Auto-generate id and timestamp if not provided
        if (reading.getId() == null || reading.getId().isEmpty()) {
            reading.setId(java.util.UUID.randomUUID().toString());
        }
        if (reading.getTimestamp() == 0) {
            reading.setTimestamp(System.currentTimeMillis());
        }

        // Update the sensor's currentValue
        sensor.setCurrentValue(reading.getValue());

        // Save the reading
        store.getReadingsForSensor(sensorId).add(reading);

        return Response.status(201).entity(reading).build();
    }
}
