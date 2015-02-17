package com.fyp;

import com.vaadin.data.Property;
import com.vaadin.ui.AbstractTextField;

/**
 * Created by Neil on 16/02/2015.
 */
public class FieldChangeListener implements Property.ValueChangeListener {

    private AbstractTextField field;

    public FieldChangeListener(AbstractTextField field) {
        this.field = field;
    }

    @Override
    public void valueChange(Property.ValueChangeEvent valueChangeEvent) {

        // if field is empty, attach style name for CSS highlighting
        if (! field.getValue().trim().isEmpty()) {
            field.removeStyleName("emptyField");
        }
    }
}
