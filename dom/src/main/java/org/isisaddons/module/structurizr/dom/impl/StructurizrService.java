package org.isisaddons.module.structurizr.dom.impl;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import com.google.common.collect.Lists;
import com.google.inject.name.Names;
import com.structurizr.Workspace;
import com.structurizr.api.StructurizrClient;
import com.structurizr.model.Container;
import com.structurizr.model.Model;
import com.structurizr.model.Person;
import com.structurizr.model.SoftwareSystem;
import com.structurizr.model.Tags;
import com.structurizr.view.ComponentView;
import com.structurizr.view.ContainerView;
import com.structurizr.view.PaperSize;
import com.structurizr.view.Shape;
import com.structurizr.view.Styles;
import com.structurizr.view.SystemContextView;
import com.structurizr.view.ViewSet;

import org.apache.isis.applib.AppManifest;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.services.classdiscovery.ClassDiscoveryService2;
import org.apache.isis.applib.services.config.ConfigurationService;
import org.apache.isis.applib.services.guice.GuiceBeanProvider;
import org.apache.isis.applib.services.registry.ServiceRegistry2;
import org.apache.isis.core.commons.lang.ClassUtil;

@DomainService(nature = NatureOfService.DOMAIN)
public class StructurizrService {

    // TODO: remove this hardcoded stuff
    private static final long WORKSPACE_ID = 19031;
    private static final String API_KEY = "97c1a59d-f3db-41bc-9170-509e7a52b966";
    private static final String API_SECRET = "767aa5ac-1d9b-474a-a178-92288289c2b7";

    private long workspaceId;
    private StructurizrClient structurizrClient;

    AppManifest appManifest;

    @PostConstruct
    public void init(Map<String,String> properties) throws IllegalAccessException, InstantiationException {
        // TODO: read instead from properties
        String apiKey = API_KEY;
        String apiSecret = API_SECRET;
        workspaceId = WORKSPACE_ID;

        final String appManifestStr = properties.get("isis.appManifest");
        final Class<?> appManifestClass = ClassUtil.forName(appManifestStr);
        appManifest = (AppManifest) appManifestClass.newInstance();

        structurizrClient = new StructurizrClient(apiKey, apiSecret);
    }

    @Programmatic
    public URL export() throws Exception {

        final String applicationName = guiceBeanProvider.lookup(String.class, Names.named("applicationName"));

        Workspace workspace = new Workspace(applicationName, "Model of " + applicationName);

        Model model = workspace.getModel();
        ViewSet viewSet = workspace.getViews();

        final Person user = model.addPerson("User", "A user of the system.");

        final SoftwareSystem softwareSystem = model.addSoftwareSystem(applicationName, "");
        user.uses(softwareSystem, "Uses");


        final SystemContextView contextView = viewSet.createSystemContextView(softwareSystem, "Context", "System context.");

        contextView.addAllSoftwareSystems();
        contextView.addAllPeople();
        contextView.setPaperSize(PaperSize.A5_Landscape);

        final List<Object> platformServices = Lists.newArrayList();
        final List<Object> registeredServices = serviceRegistry2.getRegisteredServices();
        // these get removed as we discover services residing in other modules...
        platformServices.addAll(registeredServices);

        final List<Class<?>> moduleClasses = appManifest.getModules();
        for (Class<?> moduleClass : moduleClasses) {
            final Package modulePackage = moduleClass.getPackage();
            final String modulePackageName = modulePackage.getName();

            final Container containerForModule = softwareSystem.addContainer(modulePackageName, "", "");
            final ContainerView containerView = viewSet.createContainerView(softwareSystem, modulePackageName, modulePackage.getName());

            for (final Object serviceObj : registeredServices) {
                final Class<?> serviceClass = serviceObj.getClass();
                final Package servicePackage = serviceClass.getPackage();
                final String servicePackageName = servicePackage.getName();

                if(servicePackageName.startsWith(modulePackageName)) {
                    containerForModule.addComponent(serviceClass.getSimpleName(), serviceClass, "", "");
                    platformServices.remove(serviceObj);

                    ComponentView componentViewForService = viewSet.createComponentView(containerForModule, serviceClass.getName(), serviceClass.getSimpleName());
                    contextView.setPaperSize(PaperSize.A3_Landscape);
                    componentViewForService.addAllElements();

                }
            }

            contextView.setPaperSize(PaperSize.A4_Landscape);
            containerView.addAllElements();

        }



        Styles styles = viewSet.getConfiguration().getStyles();
        styles.addElementStyle(Tags.SOFTWARE_SYSTEM).background("#1168bd").color("#ffffff");
        styles.addElementStyle(Tags.PERSON).background("#08427b").color("#ffffff").shape(Shape.Person);

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


    @Inject
    GuiceBeanProvider guiceBeanProvider;

    @Inject
    ServiceRegistry2 serviceRegistry2;

    @Inject
    ConfigurationService configurationService;

    @Inject
    ClassDiscoveryService2 classDiscoveryService2;

}