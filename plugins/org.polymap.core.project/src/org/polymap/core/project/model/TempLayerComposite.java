/* 
 * polymap.org
 * Copyright 2009, Polymap GmbH, and individual contributors as indicated
 * by the @authors tag.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 *
 * $Id$
 */
package org.polymap.core.project.model;

import org.qi4j.api.composite.TransientComposite;
import org.qi4j.api.concern.Concerns;
import org.qi4j.api.mixin.Mixins;

import org.polymap.core.project.ITempLayer;
import org.polymap.core.qi4j.QiEntity;
import org.polymap.core.qi4j.event.ModelChangeSupport;
import org.polymap.core.qi4j.event.PropertyChangeSupport;
import org.polymap.core.qi4j.security.ACL;
import org.polymap.core.qi4j.security.ACLCheckConcern;
import org.polymap.core.qi4j.security.ACLFilterConcern;

/**
 * The composite providing the implementation of the {@link ITempLayer} interface.
 * 
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @since 3.1
 */
@Concerns( {
        ACLCheckConcern.class, 
        ACLFilterConcern.class, 
        PropertyChangeSupport.Concern.class
})
@Mixins( {
        LayerState.Mixin.class, 
        Labeled.Mixin.class, 
        ACL.Mixin.class, 
        ParentMap.Mixin.class,
        PipelineHolder.Mixin.class,
        PropertyChangeSupport.Mixin.class,
        ModelChangeSupport.Mixin.class,
        QiEntity.Mixin.class
} )
public interface TempLayerComposite
        extends ITempLayer, LayerState, Labeled, ACL, ParentMap, PipelineHolder,
                PropertyChangeSupport, ModelChangeSupport, TransientComposite {

}
