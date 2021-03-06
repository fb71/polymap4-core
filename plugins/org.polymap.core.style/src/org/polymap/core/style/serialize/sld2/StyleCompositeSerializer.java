/* 
 * polymap.org
 * Copyright (C) 2018, the @authors. All rights reserved.
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
package org.polymap.core.style.serialize.sld2;

import static java.util.Collections.singletonList;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.Rule;
import org.geotools.styling.StyleFactory;
import org.geotools.styling.Symbolizer;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.expression.Expression;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.FluentIterable;
import com.rits.cloning.Cloner;

import org.polymap.core.runtime.config.Config2;
import org.polymap.core.runtime.config.Configurable;
import org.polymap.core.runtime.config.Mandatory;
import org.polymap.core.style.model.Style;
import org.polymap.core.style.model.StyleComposite;
import org.polymap.core.style.model.StylePropertyValue;
import org.polymap.core.style.model.feature.AttributeValue;
import org.polymap.core.style.model.feature.ConstantFilter;
import org.polymap.core.style.model.feature.ConstantValue;
import org.polymap.core.style.model.feature.FilterMappedValues;
import org.polymap.core.style.model.feature.FilterStyleProperty;
import org.polymap.core.style.model.feature.MappedValues.Mapped;
import org.polymap.core.style.model.feature.NoValue;
import org.polymap.core.style.model.feature.ScaleMappedValues;
import org.polymap.core.style.model.feature.ScaleMappedValues.ScaleRange;
import org.polymap.core.style.model.feature.ScaleRangeFilter;
import org.polymap.core.style.serialize.FeatureStyleSerializer.Context;

import org.polymap.model2.Property;

/**
 * 
 * @param <T> The type to be {@link #serialize(StyleComposite, FeatureTypeStyle)}d.
 * @param <S> The target type to be expected in {@link #set(FeatureTypeStyle, Property, Setter)}. 
 * @author Falko Br�utigam
 */
public abstract class StyleCompositeSerializer<T extends StyleComposite,S>
        extends Configurable {

    private static final Log log = LogFactory.getLog( StyleCompositeSerializer.class );
    
    public static final StyleFactory sf = SLDSerializer2.sf;

    public static final FilterFactory2 ff = SLDSerializer2.ff;

    protected Context               context;
    
    @Mandatory
    protected Config2<StyleCompositeSerializer<T,S>,SymbolizerAccessor<S>> accessor;
    
    
    public StyleCompositeSerializer( Context context ) {
        this.context = context;
    }


    public abstract void serialize( T style, FeatureTypeStyle fts );

    
    /**
     * Sets the value(s) of the given style property via the given setter in the
     * target symbolizer.
     * <p/>
     * More specificly, for the given sub-class of {@link StylePropertyValue} one or
     * more values for the target symbolizer are generated (see
     * {@link RuleModifier}). Then all rules of the given {@link FeatureTypeStyle}
     * are copied and modified by the given <b>setter</b>. The actual target
     * <b>symbolizer</b>, that is given to the setter, is determined by the current
     * {@link #accessor}.
     * <p/>
     * The setter is not called if the given property is not set.
     *
     * @param fts
     * @param prop
     * @param setter
     */
    protected <V> void set( FeatureTypeStyle fts, Property<StylePropertyValue<V>> prop, Setter<V,S> setter ) {
        FluentIterable<RuleModifier<V,S>> modifiers = FluentIterable.from( handle( prop ) )
                .filter( modifier -> !(modifier instanceof NoValueRuleModifier) );
        multiply( fts, modifiers, setter );
    }


    /**
     * The given setter is called only if the property is <b>not</b> set. The value
     * argument of the setter is always <b>null</b>.
     * 
     * @see #set(FeatureTypeStyle, Property, Setter)
     */
    protected <V> void setDefault( FeatureTypeStyle fts, Property<StylePropertyValue<V>> prop, Setter<V,S> setter ) {
        FluentIterable<RuleModifier<V,S>> modifiers = FluentIterable.from( handle( prop ) )
                .filter( modifier -> modifier instanceof NoValueRuleModifier );
        multiply( fts, modifiers, setter );        
    }


    /**
     * Do the magic of building the cross-product of the rules in the given
     * {@link FeatureTypeStyle} and the given modifiers.
     *
     * @param fts
     * @param prop
     * @param setter
     */
    protected <V> void multiply( FeatureTypeStyle fts, FluentIterable<RuleModifier<V,S>> modifiers, Setter<V,S> setter ) {
        if (modifiers.isEmpty()) {
            return;
        }
        List<Rule> newRules = new ArrayList();
        for (Rule rule : fts.rules()) {
            for (RuleModifier<V,S> modifier : modifiers) {
                Rule copy = Cloner.standard().deepCloneDontCloneInstances( rule, ff, sf );
                modifier.apply( copy, accessor.get().apply( copy ), (Setter<V,S>)setter );

                // XXX check if filter and scales are the same for rule and copy; in this
                // case symbolizer can(?) / should (?) be merged
                newRules.add( copy );
            }
        }
        fts.rules().clear();
        fts.rules().addAll( newRules );
    }


    protected <V> Iterable<RuleModifier<V,S>> handle( Property<StylePropertyValue<V>> prop ) {
        StylePropertyValue<V> styleProp = prop.get();

        // no value -> nothing to modify
        if (styleProp == null || styleProp instanceof NoValue) {
            return singletonList( new NoValueRuleModifier() );
        }
//        // ConstantRasterBand
//        else if (styleProp instanceof ConstantRasterBand) {
//            Expression expr = ff.literal( ((ConstantValue)styleProp).value() );
//            return singletonList( new SimpleRuleModifier( expr ) );
//        }
        // ConstantValue
        else if (styleProp instanceof ConstantValue) {
            Expression expr = ff.literal( ((ConstantValue)styleProp).value() );
            return singletonList( new SimpleRuleModifier( expr ) );
        }
        // AttributeValue
        else if (styleProp instanceof AttributeValue) {
            String attributeName = (String)((AttributeValue)styleProp).attributeName.get();
            Expression expr = ff.property( attributeName );
            return singletonList( new SimpleRuleModifier( expr ) );
        }
        // FilterMappedValues
        else if (styleProp instanceof FilterMappedValues) {
            List<Mapped<Filter,Object>> values = ((FilterMappedValues)styleProp).values();
            return FluentIterable.from( values )
                    .transform( mapped -> new SimpleRuleModifier( ff.literal( mapped.value() ), mapped.key() ) );
        }
        // ScaleMappedValues
        else if (styleProp instanceof ScaleMappedValues) {
            List<Mapped<ScaleRange,Object>> values = ((ScaleMappedValues)styleProp).values();
            return FluentIterable.from( values )
                    .transform( mapped -> new SimpleRuleModifier( ff.literal( mapped.value() ), mapped.key().min.get(), mapped.key().max.get() ) );
        }
        else {
            throw new RuntimeException( "Unhandled StylePropertyValue type: " + styleProp.getClass().getSimpleName() );
        }
    }


    protected FeatureTypeStyle defaultFeatureTypeStyle( org.geotools.styling.Style result, Style style, Symbolizer... symbolizers ) {
        Rule rule = sf.createRule();
        
        // handle visibleIf
        FilterStyleProperty visibleIf = (FilterStyleProperty)style.visibleIf.get();
        if (visibleIf instanceof ScaleRangeFilter) {
            rule.setMinScaleDenominator( ((ScaleRangeFilter)visibleIf).minScale.get() );
            rule.setMaxScaleDenominator( ((ScaleRangeFilter)visibleIf).maxScale.get() );
        }
        else if (visibleIf instanceof ConstantFilter) {
            Filter filter = ((ConstantFilter)visibleIf).filter();
            if (!filter.equals( Filter.INCLUDE )) {
                rule.setFilter( filter );
            }
        }
        else {
            throw new RuntimeException( "Unhandled Style.visibleIf type: " + visibleIf.getClass() );
        }
        
        for (Symbolizer s : symbolizers) {
            rule.symbolizers().add( s );
        };
        FeatureTypeStyle fts = sf.createFeatureTypeStyle();
        fts.rules().add( rule );
        result.featureTypeStyles().add( fts );
        return fts;
    }


    /**
     * 
     */
    @FunctionalInterface
    public static interface SymbolizerAccessor<S>
            extends Function<Rule,S> {
    }
    
    
    /**
     * 
     * @param <V> The type of the value to set.
     * @param <S> The target type to set the value in (Symbolizer, Fill, ...).
     */
    @FunctionalInterface
    public static interface Setter<V,S> {
        public void apply( Expression value, S symbolizer );
    }


    /**
     * 
     */
    public static interface RuleModifier<V,S> {
        public void apply( Rule copy, S symbolizer, Setter<V,S> setter );
    }
    

    /**
     * 
     */
    public static class NoValueRuleModifier<V,S>
            implements RuleModifier<V,S> {

        @Override
        public void apply( Rule copy, S symbolizer, Setter<V,S> setter ) {
            setter.apply( null, symbolizer );
        }
    }

    
    /**
     * 
     */
    public static class SimpleRuleModifier<V,S>
            implements RuleModifier<V,S> {
        public double           minScale = -1, maxScale = -1;
        public Filter           filter;
        public Expression/*<V>*/value;
        
        public SimpleRuleModifier( Expression value ) {
            this( value, null );
        }

        public SimpleRuleModifier( Expression value, Filter filter ) {
            this.value = value;
            this.filter = filter;
        }

        public SimpleRuleModifier( Expression value, double minScale, double maxScale ) {
            this.value = value;
            this.minScale = minScale;
            this.maxScale = maxScale;
        }

        @Override
        public void apply( Rule rule, S symbolizer, Setter<V,S> setter ) {
            assert value != null;
            setter.apply( value, symbolizer );
            if (filter != null) {
                Filter and = rule.getFilter() != null
                        ? ff.and( rule.getFilter(), filter )
                        : filter;
                rule.setFilter( and );
            }
            if (minScale > -1) {
                assert rule.getMinScaleDenominator() == 0;
                rule.setMinScaleDenominator( minScale );
            }
            if (maxScale > -1) {
                assert rule.getMaxScaleDenominator() == Double.POSITIVE_INFINITY;
                rule.setMaxScaleDenominator( maxScale );
            }
        }
    }
    
}
