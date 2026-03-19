package com.example.opencodeapi.integration;

import com.example.opencodeapi.client.api.PathApiClient;
import com.example.opencodeapi.client.api.ProjectApiClient;
import com.example.opencodeapi.client.api.VcsApiClient;
import tools.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.example.opencodeapi.integration.ApiAssertions.*;
import static org.junit.jupiter.api.Assertions.*;

class ProjectApiClientIT extends IntegrationTestBase {

    private static ProjectApiClient projectClient;
    private static PathApiClient pathClient;
    private static VcsApiClient vcsClient;

    @BeforeAll
    static void setUp() {
        projectClient = new ProjectApiClient(httpClient);
        pathClient = new PathApiClient(httpClient);
        vcsClient = new VcsApiClient(httpClient);
    }

    @Test
    void listProjects_returnsArray() {
        JsonNode projects = projectClient.list();
        assertJsonArray(projects);
    }

    @Test
    void getCurrentProject_returnsProject() {
        JsonNode project = projectClient.getCurrent();
        assertNotNull(project);
    }

    @Test
    void getPath_returnsPathInfo() {
        JsonNode path = pathClient.get();
        assertNotNull(path);
    }

    @Test
    void getVcs_returnsVcsInfo() {
        JsonNode vcs = vcsClient.get();
        assertNotNull(vcs);
    }
}
