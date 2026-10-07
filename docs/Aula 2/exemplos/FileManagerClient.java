package net.weg.capture.client;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import net.weg.capture.dto.project.DeleteFilesRequestDTO;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "file-manager-api")
@RegisterProvider(BearerTokenRequestFilter.class)
@Path("/api/file-manager")
public interface FileManagerClient {

    @DELETE
    @Path("/delete-files")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    String deleteFiles(DeleteFilesRequestDTO request);
}