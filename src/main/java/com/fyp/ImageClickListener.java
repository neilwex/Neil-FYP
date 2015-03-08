package com.fyp;

import com.vaadin.event.MouseEvents;
import com.vaadin.server.Resource;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Image;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 * Created by Neil on 07/03/2015.
 */
public class ImageClickListener extends VerticalLayout implements MouseEvents.ClickListener {

    private final UI ui;
    private String imageFile;

    public ImageClickListener(String imageFile, UI ui) {
        this.imageFile = imageFile;
        this.ui = ui;
    }

    @Override
    public void click(MouseEvents.ClickEvent clickEvent) {

        final Window window = new Window();
        VerticalLayout content = new VerticalLayout();
        content.setMargin(true);
        content.setSpacing(true);
        window.setWidth("-1px");
        window.setHeight("-1px");
        window.setModal(true);
        window.setClosable(true);
        window.setResizable(false);
        window.setContent(content);
        window.center();
        window.setImmediate(true);

        Resource res1 = new ThemeResource(imageFile);
        content.addComponent(new Image(null, res1));

        // Open pop-up in the UI
        ui.addWindow(window);
    }

}