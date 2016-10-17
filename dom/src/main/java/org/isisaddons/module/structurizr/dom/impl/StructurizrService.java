package org.isisaddons.module.structurizr.dom.impl;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import javax.annotation.PostConstruct;

import com.structurizr.Workspace;
import com.structurizr.api.StructurizrClient;
import com.structurizr.model.Model;
import com.structurizr.model.Person;
import com.structurizr.model.SoftwareSystem;
import com.structurizr.model.Tags;
import com.structurizr.view.PaperSize;
import com.structurizr.view.Styles;
import com.structurizr.view.SystemContextView;
import com.structurizr.view.ViewSet;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.Programmatic;

@DomainService(nature = NatureOfService.DOMAIN)
public class StructurizrService {

    // TODO: remove this hardcoded stuff
    private static final long WORKSPACE_ID = 19031;
    private static final String API_KEY = "97c1a59d-f3db-41bc-9170-509e7a52b966";
    private static final String API_SECRET = "767aa5ac-1d9b-474a-a178-92288289c2b7";

    private long workspaceId;
    private StructurizrClient structurizrClient;

    @PostConstruct
    public void init(Map<String,String> properties) {
        // TODO: read instead from properties
        String apiKey = API_KEY;
        String apiSecret = API_SECRET;
        workspaceId = WORKSPACE_ID;

        structurizrClient = new StructurizrClient(apiKey, apiSecret);
    }

    @Programmatic
    public URL export() throws Exception {
        Workspace workspace = new Workspace("My model", "This is a model of my software system.");
        Model model = workspace.getModel();
        ViewSet viewSet = workspace.getViews();
        Styles styles = viewSet.getConfiguration().getStyles();

        Person user = model.addPerson("User", "A sdfsdfs user of my software system.");
        SoftwareSystem softwareSystem = model.addSoftwareSystem("Software System", "My software system.");
        user.uses(softwareSystem, "Uses");

        SystemContextView contextView = viewSet.createSystemContextView(softwareSystem, "Context", "A description of this diagram.");
        contextView.addAllSoftwareSystems();
        contextView.addAllPeople();
        contextView.setPaperSize(PaperSize.A5_Landscape);

        styles.addElementStyle(Tags.SOFTWARE_SYSTEM).background("#1168bd").color("#ffffff");
        styles.addElementStyle(Tags.PERSON).background("#08427b").color("#ffffff");

        uploadWorkspaceToStructurizr(workspace);

        return getWorkspaceUrl();
    }

    @Programmatic
    public URL getWorkspaceUrl() throws MalformedURLException {
        return new URL(String.format("https://www.structurizr.com/workspace/%d#Context", workspaceId));
    }


    private void uploadWorkspaceToStructurizr(Workspace workspace) throws Exception {
        structurizrClient.mergeWorkspace(workspaceId, workspace);
        getWorkspaceUrl();
    }


}