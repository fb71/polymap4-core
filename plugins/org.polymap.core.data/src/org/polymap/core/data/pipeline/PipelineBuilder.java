/*
 * polymap.org
 * Copyright (C) 2009-2018, Polymap GmbH. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.core.data.pipeline;

import java.util.Optional;

/**
 * Provides the logic to create a {@link Pipeline} out of a usecase defined by a
 * {@link PipelineProcessor} interface.
 *
 * @author <a href="http://www.polymap.de">Falko Br�utigam</a>
 */
public interface PipelineBuilder {

    /**
     * Attempts to create a new {@link Pipeline} for the given configuration. 
     *
     * @param layerId An identifier of the layer the pipeline is created for.
     * @param usecase A processor interface that defines the interface of the pipeline. 
     * @param dsd Describes the data source.
     * @return Newly created {@link Pipeline} instance.
     * @throws PipelineBuilderException
     */
    public Optional<Pipeline> createPipeline( String layerId, Class<? extends PipelineProcessor> usecase, 
            DataSourceDescriptor dsd ) throws PipelineBuilderException;

}
