/* 
 * polymap.org
 * Copyright (C) 2016, the @authors. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3.0 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.core.style.model.feature;

import org.opengis.filter.Filter;

import org.polymap.model2.CollectionProperty;

/**
 * Base class for all filter mapped values, like numbers and colors.
 * 
 * @author Falko Br�utigam
 */
public abstract class FilterMappedValues<V>
        extends MappedValues<Filter,V> {

    //@Concerns( StylePropertyChange.Concern.class )
    protected CollectionProperty<String>    encodedFilters;

    
    @Override
    public MappedValues<Filter,V> add( Filter key, V value ) {
        encodedFilters.add( ConstantFilter.encode( key ) );
        return this;
    }


    @Override
    public void clear() {
        encodedFilters.clear();
        super.clear();
    }
    
}
