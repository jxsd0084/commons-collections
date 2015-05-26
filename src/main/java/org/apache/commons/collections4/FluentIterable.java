/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.collections4;

import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

/**
 * A FluentIterable provides a powerful yet simple API for manipulating
 * Iterable instances in a fluent manner.
 * <p>
 * A FluentIterable can be created either from an Iterable or from a set
 * of elements. The following types of methods are provided:
 * <ul>
 *   <li>fluent methods which return a new {@code FluentIterable} instance,
 *       providing a view of the original iterable (e.g. filter(Predicate));
 *   <li>conversion methods which copy the FluentIterable's contents into a
 *       new collection or array (e.g. toList());
 *   <li>utility methods which answer questions about the FluentIterable's
 *       contents (e.g. size(), anyMatch(Predicate)).
 *   <li> 
 * </ul>
 * <p>
 * The following example outputs the first 3 even numbers in the range [1, 10]
 * into a list:
 * <pre>
 * List&lt;String&gt; result =
 *   FluentIterable
 *       .of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
 *       .filter(new Predicate<Integer>() {
 *                   public boolean evaluate(Integer number) {
 *                        return number % 2 == 0;
 *                   }
 *              )
 *       .transform(TransformerUtils.stringValueTransformer())
 *       .limit(3)
 *       .toList();
 * </pre>
 * The resulting list will contain the following elements:
 * <pre>[2, 4, 6]</pre>
 *
 * @param <E>  the element type
 * @since 4.1
 * @version $Id: $
 */
public class FluentIterable<E> implements Iterable<E> {

    /** A reference to the wrapped iterable. */
    private final Iterable<E> iterable;

    // Static factory methods
    // ----------------------------------------------------------------------

    /**
     * Creates a new FluentIterable from the provided elements.
     * <p>
     * The returned iterable's iterator does not support {@code remove()}.
     *
     * @param <T>  the element type
     * @param elements  the elements to be contained in the FluentIterable
     * @return a new FluentIterable containing the provided elements
     */
    public static <T> FluentIterable<T> of(final T... elements) {
        return of(Arrays.asList(elements));
    }

    /**
     * Construct a new FluentIterable from the provided iterable. If the
     * iterable is already an instance of FluentIterable, the instance
     * will be returned instead.
     * <p>
     * The returned iterable's iterator supports {@code remove()} when the
     * corresponding input iterator supports it.
     *
     * @param <T>  the element type
     * @param iterable  the iterable to wrap into a FluentIterable
     * @return a new FluentIterable wrapping the provided iterable
     */
    public static <T> FluentIterable<T> of(final Iterable<T> iterable) {
        if (iterable == null) {
            throw new NullPointerException("Iterable must not be null");
        }
        if (iterable instanceof FluentIterable<?>) {
            return (FluentIterable<T>) iterable;
        } else {
            return new FluentIterable<T>(iterable);
        }
    }

    // Constructor
    // ----------------------------------------------------------------------

    /**
     * Package-private constructor, used by IterableUtils.
     */
    FluentIterable() {
        this.iterable = this;
    }

    /**
     * Create a new FluentIterable by wrapping the provided iterable.
     * @param iterable  the iterable to wrap
     */
    private FluentIterable(final Iterable<E> iterable) {
        this.iterable = iterable;
    }

    // fluent construction methods
    // ----------------------------------------------------------------------

    /**
     * Returns a new FluentIterable whose iterator will first traverse
     * the elements of the current iterable, followed by the provided
     * elements.
     *
     * @param elements  the elements to append to the iterable
     * @return a new iterable, combining this iterable with the elements
     */
    public FluentIterable<E> append(final E... elements) {
        return append(Arrays.asList(elements));
    }

    /**
     * Returns a new FluentIterable whose iterator will first traverse
     * the elements of the current iterable, followed by the elements
     * of the provided iterable.
     * <p>
     * A <code>null</code> iterable will be treated as an empty iterable.
     *
     * @param other  the other iterable to combine, may be null
     * @return a new iterable, combining this iterable with other
     */
    public FluentIterable<E> append(final Iterable<E> other) {
        return of(IterableUtils.chainedIterable(iterable, other));
    }

    /**
     * This method fully traverses an iterator of this iterable and returns
     * a new iterable with the same contents, but without any reference
     * to the originating iterables and/or iterators.
     * <p>
     * Calling this method is equivalent to:
     * <pre>
     *   FluentIterable<E> someIterable = ...;
     *   FluentIterable.of(someIterable.toList());
     * </pre>
     *
     * @return a new iterable with the same contents as this iterable
     */
    public FluentIterable<E> eval() {
        return of(toList());
    }

    /**
     * Returns a new FluentIterable whose iterator will only return
     * elements from this iterable matching the provided predicate.
     *
     * @param predicate  the predicate used to filter elements
     * @return a new iterable, providing a filtered view of this iterable
     * @throws NullPointerException if predicate is null
     */
    public FluentIterable<E> filter(final Predicate<E> predicate) {
        return of(IterableUtils.filteredIterable(iterable, predicate));
    }

    /**
     * Returns a new FluentIterable whose iterator will return at most
     * the provided maximum number of elements from this iterable.
     *
     * @param maxSize  the maximum number of elements
     * @return a new iterable, providing a bounded view of this iterable
     * @throws IllegalArgumentException if maxSize is negative
     */
    public FluentIterable<E> limit(final int maxSize) {
        return of(IterableUtils.boundedIterable(iterable, maxSize));
    }

    /**
     * Returns a new FluentIterable whose iterator will loop infinitely
     * over the elements from this iterable.
     *
     * @return a new iterable, providing a looping view of this iterable
     */
    public FluentIterable<E> loop() {
        return of(IterableUtils.loopingIterable(iterable));
    }

    /**
     * Returns a new FluentIterable whose iterator will skip the first
     * N elements from this iterable.
     *
     * @param elementsToSkip  the number of elements to skip
     * @return a new iterable, providing a view of this iterable by skipping
     *   the first N elements
     * @throws IllegalArgumentException if elementsToSkip is negative
     */
    public FluentIterable<E> skip(int elementsToSkip) {
        return of(IterableUtils.skippingIterable(iterable, elementsToSkip));
    }

    /**
     * Returns a new FluentIterable whose iterator will return all elements
     * of this iterable transformed by the provided transformer.
     *
     * @param transformer  the transformer applied to each element
     * @return a new iterable, providing a transformed view of this iterable
     * @throws NullPointerException if transformer is null
     */
    public <O> FluentIterable<O> transform(final Transformer<? super E, ? extends O> transformer) {
        return of(IterableUtils.transformedIterable(iterable, transformer));
    }

    /**
     * Returns a new FluentIterable whose iterator will return a unique view
     * of this iterable.
     *
     * @return a new iterable, providing a unique view of this iterable
     */
    public FluentIterable<E> unique() {
        return of(IterableUtils.uniqueIterable(iterable));
    }
    
    // convenience methods
    // ----------------------------------------------------------------------

    /** {@inheritDoc} */
    public Iterator<E> iterator() {
        return iterable.iterator();
    }

    /**
     * Returns an Enumeration that will enumerate all elements contained
     * in this iterable.
     *
     * @return an Enumeration over the elements of this iterable
     */
    public Enumeration<E> asEnumeration() {
        return IteratorUtils.asEnumeration(iterator());
    }

    /**
     * Checks if all elements contained in this iterable are matching the
     * provided predicate.
     * <p>
     * A <code>null</code> or empty iterable returns true.
     *
     * @param predicate  the predicate to use, may not be null
     * @return true if all elements contained in this iterable match the predicate,
     *   false otherwise
     * @throws NullPointerException if predicate is null
     */
    public boolean allMatch(final Predicate<? super E> predicate) {
        return IterableUtils.matchesAll(iterable, predicate);
    }

    /**
     * Checks if this iterable contains any element matching the provided predicate.
     * <p>
     * A <code>null</code> or empty iterable returns false.
     *
     * @param predicate  the predicate to use, may not be null
     * @return true if at least one element contained in this iterable matches the predicate,
     *   false otherwise
     * @throws NullPointerException if predicate is null
     */
    public boolean anyMatch(final Predicate<? super E> predicate) {
        return IterableUtils.matchesAny(iterable, predicate);
    }

    /**
     * Checks if this iterable is empty.
     *
     * @return true if this iterable does not contain any elements, false otherwise
     */
    public boolean isEmpty() {
        return IterableUtils.isEmpty(iterable);
    }

    /**
     * Checks if the object is contained in this iterable.
     * 
     * @param object  the object to check
     * @return true if the object is contained in this iterable, false otherwise
     */
    public boolean contains(final Object object) {
        return IterableUtils.contains(iterable, object);
    }

    /**
     * Returns the element at the provided position in this iterable.
     * In order to return the element, an iterator needs to be traversed
     * up to the requested position.
     *
     * @param position  the position of the element to return
     * @return the element
     * @throws IndexOutOfBoundsException if the provided position is outside the
     *   valid range of this iterable: [0, size)
     */
    public E get(final int position) {
        return IterableUtils.get(iterable, position);
    }

    /**
     * Returns the number of elements that are contained in this iterable.
     * In order to determine the size, an iterator needs to be traversed.
     *
     * @return the size of this iterable
     */
    public int size() {
        return IterableUtils.size(iterable);
    }

    /**
     * Traverses an iterator of this iterable and adds all elements
     * to the provided collection.
     *
     * @param collection  the collection to add the elements
     * @throws NullPointerException if collection is null
     */
    public void copyInto(final Collection<? super E> collection) {
        if (collection == null) {
            throw new NullPointerException("Collection must not be null");
        }

        for (final E element : iterable) {
            collection.add(element);
        }
    }

    /**
     * Returns an array containing all elements of this iterable by traversing
     * its iterator.
     *
     * @param arrayClass  the class of array to create 
     * @return an array of the iterable contents
     * @throws ClassCastException if arrayClass is invalid
     */
    public E[] toArray(final Class<E> arrayClass) {
        return IteratorUtils.toArray(iterator(), arrayClass);
    }

    /**
     * Returns a list containing all elements of this iterable by traversing
     * its iterator.
     * <p>
     * The returned list is mutable.
     *
     * @return a list of the iterable contents
     */
    public List<E> toList() {
        return IteratorUtils.toList(iterator());
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return IterableUtils.toString(iterable);
    }

}
