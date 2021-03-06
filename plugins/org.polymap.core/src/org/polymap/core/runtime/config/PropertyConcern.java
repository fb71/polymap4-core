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

/**
 * Provides a way to implement concerns or side effects of getting/setting the value
 * of a {@link Config}.
 *
 * @see Concern
 * @author <a href="http://www.polymap.de">Falko Br�utigam</a>
 */
public interface PropertyConcern<T> {

    /**
     * Intercepts construction of a {@link Config} instance.
     *
     * @param obj
     * @param prop
     * @param value The current value of the property after default value annotations have been applied.
     * @return The value to initialize the property with.
     */
    public T doInit( Object obj, Config<T> prop, T value );
    
    /**
     * Intercepts invocations of {@link Config#get()}.
     *
     * @param obj
     * @param prop
     * @param value The current value of the property.
     * @return The value to return to the caller of {@link Config#get()}.
     */
    public T doGet( Object obj, Config<T> prop, T value );

    /**
     * Intercepts invocations of {@link Config#set()}.
     *
     * @param obj
     * @param prop
     * @param newValue The value given to {@link Config#set(Object)}
     * @return The value to set to the property.
     */
    public T doSet( Object obj, Config<T> prop, T newValue );
    
}
