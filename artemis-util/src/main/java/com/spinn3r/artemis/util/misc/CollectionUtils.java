package com.spinn3r.artemis.util.misc;

import com.google.common.collect.Lists;
import org.hamcrest.Matcher;

import java.util.*;

/**
 *
 */
public class CollectionUtils {

    /**
     * Count the number of times the given value appears in the list.
     */
    public static <T> int count( List<T> list, T value ) {

        int result = 0;

        for (T current : list) {

            if ( value.equals( current ) ) {
                ++result;
            }

        }

        return result;

    }

    /**
     * Filter the collection by applying a Predicate to each element. If the
     * predicate returns false, remove the element. If the input collection or
     * predicate is null, there is no change made.
     *
     * @param input
     * @param predicate
     * @param <V>
     * @return
     */
    public static <V> void filter( Collection<V> input, Predicate<V> predicate ) {

        Iterator<V> iterator = input.iterator();

        while( iterator.hasNext() ) {

            V value = iterator.next();

            if ( ! predicate.evaluate( value ) ) {
                iterator.remove();
            }

        }

    }

    public static <V> void filter( Collection<V> input, Matcher<V> matcher ) {

        Iterator<V> iterator = input.iterator();

        while( iterator.hasNext() ) {

            V value = iterator.next();

            if ( ! matcher.matches( value ) ) {
                iterator.remove();
            }

        }

    }

    /**
     * Filter only items with a toString() value starting with prefix.
     * @param input
     * @param prefix
     * @param <V>
     */
    public static <V> void startsWith( Collection<V> input, final String prefix ) {

        CollectionUtils.filter( input, new Predicate<V>() {
            @Override
            public boolean evaluate(V value) {
                return value.toString().startsWith( prefix );
            }
        } );

    }

    /**
     * Shuffle the given list, then return it.
     */
    public static <T> List<T> shuffle( List<T> list ) {
        java.util.Collections.shuffle( list );
        return list;
    }

    public static <T> List<List<T>> group( Collection<T> input, int groupSize ) {
        return group( input, groupSize, true );
    }

    /**
     * Take the input and return it as groups of collections based on the size.
     *
     * For example, if the groupSize is 2, and the input is a list of integers,
     * and we're given [1, 2, 3, 4, 5] we will return [[1,2],[3,4],[5]]
     *
     * If trailing is false we only return collections that are full, not partial.
     * This is the last few if they don't equal the size.
     *
     * @return
     */
    public static <T> List<List<T>> group( Collection<T> input, int groupSize, boolean partial ) {

        List<List<T>> result = Lists.newArrayList();

        List<T> current = Lists.newArrayList();

        for (T entry : input) {

            if ( current.size() == groupSize ) {
                result.add( current );
                current = Lists.newArrayList();
            }

            current.add( entry );

        }

        if ( partial && current.size() > 0 ) {
            result.add( current );
        }

        return result;

    }

    public static <T> T first( Collection<T> input ) {

        if ( input.size() == 0 )
            return null;

        ArrayList<T> tmp = Lists.newArrayList();
        tmp.addAll( input );

        return tmp.get( 0 );

    }

    public static <T> T last( Collection<T> input ) {

        if ( input.size() == 0 )
            return null;

        ArrayList<T> tmp = Lists.newArrayList();
        tmp.addAll( input );

        return tmp.get( tmp.size() - 1 );

    }


    /**
     * Return the first N items in a collection.
     *
     */
    public static <T> List<T> head( Collection<T> input, int count ) {

        List<T> result = Lists.newArrayList();

        ArrayList<T> tmp = Lists.newArrayList();
        tmp.addAll( input );

        for (int i = 0; i < input.size(); i++) {

            if ( result.size() == count )
                break;

            result.add( tmp.get( i ) );

        }

        return result;

    }

    /**
     * Return the last N items in a collection.
     *
     */
    public static <T> List<T> tail( Collection<T> input, int count ) {

        List<T> result = Lists.newArrayList();

        ArrayList<T> tmp = Lists.newArrayList();
        tmp.addAll( input );

        int start = Math.max( 0, input.size() - count );

        for (int i = start; i < input.size(); i++) {

            if ( result.size() == count )
                break;

            result.add( tmp.get( i ) );

        }

        return result;

    }

    /**
     * Sort a collection of objects and then return the a new sorted list so
     * we can keep things functional.  This also works on Collection types that
     * can't easily be sorted (sets, etc).
     *
     */
    public static <T extends Comparable<T>> List<T> sort( Collection<T> input ) {
        List<T> copy = Lists.newArrayList( input );
        Collections.sort(copy);
        return copy;
    }

    /**
     * Convert the collection of objects to strings by caling toString() on each.
     */
    public static <T> List<String> strings( Collection<T> input ) {
        
        List<String> result = Lists.newArrayList();

        for (T current : input) {
            result.add( current.toString() );
        }

        return result;

    }

}
