/**
 * Implementation of lazy loading. When {@link org.sormula.annotation.cascade.SelectCascade#lazy()} true,
 * for a field, the field is not selected when the source row is selected. The lazy select field
 * can be selected at a later time by using the method
 * {@link org.sormula.operation.cascade.lazy.AbstractLazySelector#checkLazySelects(String)} in one
 * of the subclasses {@link org.sormula.operation.cascade.lazy.SimpleLazySelector}, 
 * {@link org.sormula.operation.cascade.lazy.DurableLazySelector}, your own subclass, or
 * your own implementation of {@link org.sormula.operation.cascade.lazy.LazySelectable}.
 * <p>
 * See org.sormula.tests.cascade.lazy package in this project for examples.
 */
package org.sormula.operation.cascade.lazy;