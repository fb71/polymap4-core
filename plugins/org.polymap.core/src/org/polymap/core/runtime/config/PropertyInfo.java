/* 
 * polymap.org
 * Copyright (C) 2015, Falko Br�utigam. All rights reserved.
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
package org.polymap.core.runtime.config;

import java.lang.annotation.Annotation;

/**
 * 
 * @param <H> - The type of the host class.
 * @param <V> - The type of the value of this property. 
 * @author <a href="http://www.polymap.de">Falko Br�utigam</a>
 */
@SuppressWarnings("javadoc")
public interface PropertyInfo/*<H,V>*/ {

    public String getName();
    
    public Class<?> getType();
    
    /**
     * Returns the annotation of the given type, or null.
     */
    public <A extends Annotation> A getAnnotation( Class<A> type );

    public <H extends Object> H getHostObject();

    /**
     * The raw value of the property without checking concerns. This is useful
     * to access {@link Mandatory} property.
     */
    public <R extends Object> R getRawValue();
    
    /**
     * Set raw value of the property without checking concerns.
     * 
     * @return The previous value.
     */
    public <R extends Object> R setRawValue( R value );
    
}
