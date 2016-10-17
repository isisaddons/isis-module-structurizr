package org.isisaddons.module.structurizr.dom.impl;

import java.net.URL;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.DomainServiceLayout;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.SemanticsOf;

@DomainService(nature = NatureOfService.VIEW_MENU_ONLY)
@DomainServiceLayout(menuBar = DomainServiceLayout.MenuBar.SECONDARY)
public class StructurizrMenu {

    @Action(semantics = SemanticsOf.IDEMPOTENT)
    public URL export() throws Exception {
        return structurizrService.export();
    }

    @Inject
    StructurizrService structurizrService;

}