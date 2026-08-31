package br.edu.iffar.box.component.menu;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MenuTest {

    @Test
    void normalizeViewIdHandlesExtensionsAndSlashes() {
        assertEquals("/index", MenuItem.normalizeViewId("index"));
        assertEquals("/index", MenuItem.normalizeViewId("/index"));
        assertEquals("/index", MenuItem.normalizeViewId("/index.xhtml"));
        assertEquals("/macroprocessos/list", MenuItem.normalizeViewId("/macroprocessos/list.xhtml"));
        assertEquals("", MenuItem.normalizeViewId(null));
    }
}
