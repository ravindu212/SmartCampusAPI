package com.smartcampus.resource;

import com.smartcampus.model.Room;
import com.smartcampus.store.DataStore;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RoomResource {

    private final DataStore store = DataStore.getInstance();

    // GET /api/v1/rooms — list all rooms
    @GET
    public Response getAllRooms() {
        List<Room> roomList = new ArrayList<>(store.getRooms().values());
        return Response.ok(roomList).build();
    }

    // POST /api/v1/rooms — create a room
    @POST
    public Response createRoom(Room room) {
        if (room.getId() == null || room.getId().isEmpty()) {
            return Response.status(400)
                    .entity("{\"error\":\"Room ID is required\"}")
                    .build();
        }
        if (store.getRooms().containsKey(room.getId())) {
            return Response.status(409)
                    .entity("{\"error\":\"Room with this ID already exists\"}")
                    .build();
        }
        store.getRooms().put(room.getId(), room);
        return Response.status(201).entity(room).build();
    }

    // GET /api/v1/rooms/{roomId} — get one room
    @GET
    @Path("/{roomId}")
    public Response getRoom(@PathParam("roomId") String roomId) {
        Room room = store.getRooms().get(roomId);
        if (room == null) {
            return Response.status(404)
                    .entity("{\"error\":\"Room not found\"}")
                    .build();
        }
        return Response.ok(room).build();
    }

    // DELETE /api/v1/rooms/{roomId} — delete a room
    @DELETE
    @Path("/{roomId}")
    public Response deleteRoom(@PathParam("roomId") String roomId) {
        Room room = store.getRooms().get(roomId);
        if (room == null) {
            return Response.status(404)
                    .entity("{\"error\":\"Room not found\"}")
                    .build();
        }
        if (!room.getSensorIds().isEmpty()) {
            throw new com.smartcampus.exception.RoomNotEmptyException(roomId);
        }
        store.getRooms().remove(roomId);
        return Response.noContent().build();
    }
}
