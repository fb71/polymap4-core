/*
 * polymap.org 
 * Copyright (C) 2016-2018, the @authors. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software
 * Foundation; either version 3.0 of the License, or (at your option) any later
 * version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 */
package org.polymap.core.style.model.feature;

import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Lists;

/**
 * Commonly used web save fonts from
 * http://www.w3schools.com/cssref/css_websafe_fonts.asp
 *
 * @author Steffen Stundzig
 */
public enum FontFamily {
    Arial( "Arial" ),
    Monospaced( "Monospaced" ),
    SansSerif( "sans-serif" ),
    Serif( "Serif" );

    private String      families;


    FontFamily( String families ) {
        this.families = families;
    }

    public String families() {
        return families;
    }

    public List<String> splitted() {
        return Lists.newArrayList( StringUtils.split( families, ", " ) );
    }

    public static Optional<FontFamily> find( String searchFamily ) {
        for (FontFamily ff : values()) {
            if (ff.splitted().stream().anyMatch( family -> family.equals( searchFamily ) )) {
                return Optional.of( ff );
            }
        }
        return Optional.empty();
    }

}
