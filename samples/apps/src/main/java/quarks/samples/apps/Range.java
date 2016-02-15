/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015, 2016
*/
package quarks.samples.apps;

import java.util.Comparator;

/**
 * A range of values and and a way to check for containment.
 * <p>
 * Useful in filtering in predicates.  
 * <p> 
 * Poor mans Guava Range.  No analog in Apache Math?
 * TODO remove this and directly use Guava Range.
 * 
 * e.g.
 * <pre>{@code
 * Range.open(2,4).contains(2);      // returns false
 * Range.closed(2,4).contains(2);    // returns false
 * Range.atLeast(2).contains(2);     // returns true
 * Range.greaterThan(2).contains(2); // returns false
 * Range.atMost(2).contains(2);      // returns true
 * Range.lessThan(2).contains(2);    // returns false
 * }</pre>
 *
 * @param <T> value type
 */
public class Range<T> {
    private final T lowerBound;
    private final T upperBound;
    private final BoundType lbt;
    private final BoundType ubt;
    
    private enum BoundType {/** exclusive */ OPEN, /** inclusive */ CLOSED};
    
    private Range(T lowerBound, BoundType lbt, T upperBound, BoundType ubt) {
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        this.lbt = lbt;
        this.ubt = ubt;
    }
    
    // TODO defer making these public
    private static <T> Range<T> range(T lowerBound, BoundType b1, T upperBound, BoundType b2) {  return new Range<T>(lowerBound, b1, upperBound, b2); }
//    public static <T> Range<T> downTo(T v, BoundType b) { return range(v, b, null, null); }
//    public static <T> Range<T> upTo(T v, BoundType b) { return range(null, null, v, b); }

    /** (a..b) (both exclusive) */
    public static <T> Range<T> open(T lowerBound, T upperBound) { return range(lowerBound, BoundType.OPEN, upperBound, BoundType.OPEN); }
    /** [a..b] (both inclusive) */
    public static <T> Range<T> closed(T lowerBound, T upperBound) { return range(lowerBound, BoundType.CLOSED, upperBound, BoundType.CLOSED); }
    /** (a..b] (exclusive,inclusive) */
    public static <T> Range<T> openClosed(T lowerBound, T upperBound) { return range(lowerBound, BoundType.OPEN, upperBound, BoundType.CLOSED); }
    /** [a..b) (inclusive,exclusive)*/
    public static <T> Range<T> closedOpen(T lowerBound, T upperBound) { return range(lowerBound, BoundType.CLOSED, upperBound, BoundType.OPEN); }
    /** (a..+INF) (exclusive) */
    public static <T> Range<T> greaterThan(T v) { return range(v, BoundType.OPEN, null, null); }
    /** [a..+INF) (inclusive) */
    public static <T> Range<T> atLeast(T v) { return range(v, BoundType.CLOSED, null, null); }
    /** (-INF..b) (exclusive) */
    public static <T> Range<T> lessThan(T v) { return range(null, null, v, BoundType.OPEN); }
    /** (-INF..b] (inclusive) */
    public static <T> Range<T> atMost(T v) { return range(null, null, v, BoundType.CLOSED); }
    
    public T lowerBound() {
        return lowerBound;
    }
    
    public T upperBound() {
        return upperBound;
    }
    
    /**
     * Determine if the Region contains the value.
     * <p>
     * For typical numeric types, and String and Character,
     * {@code contains(v)} typically sufficies.  Though this
     * can be useful for unsigned integer comparasons.
     * <p>
     * @param v the value to check for containment
     * @param cmp the Comparator to use
     * @return true if the Region contains the value
     */
    public boolean contains(T v, Comparator<T> cmp) {
        if (lbt==null) {
            int r = cmp.compare(v, upperBound);
            return ubt == BoundType.OPEN ? r < 0 : r <= 0; 
        }
        if (ubt==null) {
            int r = cmp.compare(v, lowerBound);
            return lbt == BoundType.OPEN ? r > 0 : r >= 0; 
        }
        int r = cmp.compare(v, upperBound);
        boolean ok1 = ubt == BoundType.OPEN ? r < 0 : r <= 0;
        if (!ok1) return false;
        r = cmp.compare(v, lowerBound);
        return lbt == BoundType.OPEN ? r > 0 : r >= 0; 
    }
    
    /**
     * Determine if the Region contains the value.
     * <p>
     * For typical numeric types, and String and Character.
     * The Comparator used is the default one for the type
     * (e.g., {@code Integer#compareTo(Integer)}.
     * <p>
     * Use {@link #contains(Object, Comparator)} for other
     * types or to use a non-default Comparator.
     * <p>
     * @param v the value to check for containment
     * @return true if the Region contains the value
     */
    public boolean contains(T v) {
        Comparator<T> cmp = getComparator(v);
        return contains(v, cmp);
    }
    
    private Comparator<T> getComparator(T v) {
        if (v instanceof Double) 
            return (lowerBound,upperBound) -> ((Double)lowerBound).compareTo((Double)upperBound);
        if (v instanceof Float) 
            return (lowerBound,upperBound) -> ((Float)lowerBound).compareTo((Float)upperBound);
        if (v instanceof Long) 
            return (lowerBound,upperBound) -> ((Long)lowerBound).compareTo((Long)upperBound);
        if (v instanceof Integer) 
            return (lowerBound,upperBound) -> ((Integer)lowerBound).compareTo((Integer)upperBound);
        if (v instanceof Short) 
            return (lowerBound,upperBound) -> ((Short)lowerBound).compareTo((Short)upperBound);
        if (v instanceof Byte) 
            return (lowerBound,upperBound) -> ((Byte)lowerBound).compareTo((Byte)upperBound);
        if (v instanceof String) 
            return (lowerBound,upperBound) -> ((String)lowerBound).compareTo((String)upperBound);
        if (v instanceof Character) 
            return (lowerBound,upperBound) -> ((Character)lowerBound).compareTo((Character)upperBound);
        throw new IllegalArgumentException("Unsupported type: "+v.getClass());
    }
    
    /**
     * Create a Range from a string produced by toString()
     * @param s value from toString()
     * @param clazz the class of the values in {@code s}
     */
    public static <T> Range<T> valueOf(String s, Class<T> clazz) {
        char lbm = s.charAt(0);
        if (lbm != '[' && lbm != '(')
            throw new IllegalArgumentException(s);
        char ubm = s.charAt(s.length()-1);
        if (ubm != ']' && ubm != ')')
            throw new IllegalArgumentException(s);
        
        BoundType lbt = lbm == '[' ? BoundType.CLOSED : BoundType.OPEN;
        BoundType ubt = ubm == ']' ? BoundType.CLOSED : BoundType.OPEN;
        
        s = s.substring(1,s.length()-1);
        // this parsing is weak - broken for String bounds with embedded ".."
        String[] parts = s.split("\\.\\.");
        
        String lbs = parts[0];
        String ubs = parts[1];

        T lowerBound = lbs.equals("*") ? null : boundValue(lbs, clazz);
        T upperBound = ubs.equals("*") ? null : boundValue(ubs, clazz);
        
        return range(lowerBound, lbt, upperBound, ubt);
    }
    
    @SuppressWarnings("unchecked")
    private static <T> T boundValue(String strVal, Class<T> clazz) {
        if (strVal.equals("*"))
            return null;
        if (clazz.equals(Integer.class))
            return (T) Integer.valueOf(strVal);
        if (clazz.equals(Long.class))
            return (T) Long.valueOf(strVal);
        if (clazz.equals(Short.class))
            return (T) Short.valueOf(strVal);
        if (clazz.equals(Byte.class))
            return (T) Byte.valueOf(strVal);
        if (clazz.equals(Float.class))
            return (T) Float.valueOf(strVal);
        if (clazz.equals(Double.class))
            return (T) Double.valueOf(strVal);
        throw new IllegalArgumentException("Unhandled type "+clazz);
    }
    
    /**
     * Yields {@code <lowerBoundMarker><lowerBound>..<upperBound><upperBoundMarker>}.
     * <p>
     * Where the lowerBoundMarker is either "[" (inclusive) or "(" (exclusive)
     * and the upperBoundMarker is  either "]" (inclusive) or ")" (exclusive)
     * <p>
     * The bound value "*" is used to indicate an infinite value.
     * <p>
     * .e.g.,
     * <pre>
     * "[120..156)"  // lowerBound=120 inclusive, upperBound=156 exclusive
     * "[120..*]"    // an "atLeast" 120 range
     * </pre> 
     */
    public String toString() {
        String[] parts = { "(", "*", "*", ")" };
        if (lowerBound!=null) {
            parts[0] = lbt==BoundType.CLOSED ? "[" : "(";
            parts[1] = lowerBound.toString();
        }
        if (upperBound!=null) {
            parts[2] = upperBound.toString();
            parts[3] = ubt==BoundType.CLOSED ? "]" : ")";
        }
            
        return parts[0]+parts[1]+".."+parts[2]+parts[3];
    }
    
    private static <T> boolean testContains(Range<T> range, T v, Boolean expected) {
        boolean act = range.contains(v);
        boolean pass = act==expected;
        String passLabel = pass ? "PASS" : "FAIL";
        System.out.println(String.format("[%s] test range%s.contains(%s)==%s", passLabel, range.toString(), v.toString(), expected.toString()));
        return pass;
    }
    
    private static <T> boolean testToString(Range<T> range, String expected) {
        String act = range.toString();
        boolean pass = act.equals(expected);
        String passLabel = pass ? "PASS" : "FAIL";
        System.out.println(String.format("[%s] test Range.toString() actual=\"%s\" expected=\"%s\"", passLabel, range.toString(), expected));
        return pass;
    }

    private static <T> boolean testValueOf(String str, Class<T> clazz) {
        Range<T> range = Range.valueOf(str, clazz);
        String s2 = range.toString();
        boolean pass = s2.equals(str);

        pass &= range.lowerBound() == null || clazz.isInstance(range.lowerBound());
        pass &= range.upperBound() == null || clazz.isInstance(range.upperBound());
        String passLabel = pass ? "PASS" : "FAIL";
        System.out.println(String.format("[%s] test Range.valueOf(\"%s\", %s) yields Range with toString()=>\"%s\"", passLabel, str, clazz.getName(), range.toString()));
        return pass;
    }

    public static void main(String[] args) {
        boolean pass = true;

        System.out.println("open()");
        pass &= testContains(Range.open(2,4), 1, false);
        pass &= testContains(Range.open(2,4), 2, false);
        pass &= testContains(Range.open(2,4), 3, true);
        pass &= testContains(Range.open(2,4), 4, false);
        pass &= testContains(Range.open(2,4), 5, false);

        System.out.println("closed()");
        pass &= testContains(Range.closed(2,4), 1, false);
        pass &= testContains(Range.closed(2,4), 2, true);
        pass &= testContains(Range.closed(2,4), 3, true);
        pass &= testContains(Range.closed(2,4), 4, true);
        pass &= testContains(Range.closed(2,4), 5, false);

        System.out.println("openClosed()");
        pass &= testContains(Range.openClosed(2,4), 1, false);
        pass &= testContains(Range.openClosed(2,4), 2, false);
        pass &= testContains(Range.openClosed(2,4), 3, true);
        pass &= testContains(Range.openClosed(2,4), 4, true);
        pass &= testContains(Range.openClosed(2,4), 5, false);

        System.out.println("closedOpen()");
        pass &= testContains(Range.closedOpen(2,4), 1, false);
        pass &= testContains(Range.closedOpen(2,4), 2, true);
        pass &= testContains(Range.closedOpen(2,4), 3, true);
        pass &= testContains(Range.closedOpen(2,4), 4, false);
        pass &= testContains(Range.closedOpen(2,4), 5, false);

        System.out.println("greaterThan()");
        pass &= testContains(Range.greaterThan(2), 1, false);
        pass &= testContains(Range.greaterThan(2), 2, false);
        pass &= testContains(Range.greaterThan(2), 3, true);

        System.out.println("atLeast()");
        pass &= testContains(Range.atLeast(2), 1, false);
        pass &= testContains(Range.atLeast(2), 2, true);
        pass &= testContains(Range.atLeast(2), 3, true);

        System.out.println("lessThan()");
        pass &= testContains(Range.lessThan(2), 1, true);
        pass &= testContains(Range.lessThan(2), 2, false);
        pass &= testContains(Range.lessThan(2), 3, false);

        System.out.println("atMost()");
        pass &= testContains(Range.atMost(2), 1, true);
        pass &= testContains(Range.atMost(2), 2, true);
        pass &= testContains(Range.atMost(2), 3, false);
        
        System.out.println("Byte open()");
        pass &= testContains(Range.open((byte)2,(byte)4), (byte)1, false);
        pass &= testContains(Range.open((byte)2,(byte)4), (byte)2, false);
        pass &= testContains(Range.open((byte)2,(byte)4), (byte)3, true);
        pass &= testContains(Range.open((byte)2,(byte)4), (byte)4, false);
        pass &= testContains(Range.open((byte)2,(byte)4), (byte)5, false);
        
        System.out.println("Short open()");
        pass &= testContains(Range.open((short)2,(short)4), (short)1, false);
        pass &= testContains(Range.open((short)2,(short)4), (short)2, false);
        pass &= testContains(Range.open((short)2,(short)4), (short)3, true);
        pass &= testContains(Range.open((short)2,(short)4), (short)4, false);
        pass &= testContains(Range.open((short)2,(short)4), (short)5, false);
        
        System.out.println("Long open()");
        pass &= testContains(Range.open(2L,4L), 1L, false);
        pass &= testContains(Range.open(2L,4L), 2L, false);
        pass &= testContains(Range.open(2L,4L), 3L, true);
        pass &= testContains(Range.open(2L,4L), 4L, false);
        pass &= testContains(Range.open(2L,4L), 5L, false);
        
        System.out.println("Float open()");
        pass &= testContains(Range.open(2f,4f), 1f, false);
        pass &= testContains(Range.open(2f,4f), 2f, false);
        pass &= testContains(Range.open(2f,4f), 2.001f, true);
        pass &= testContains(Range.open(2f,4f), 3.999f, true);
        pass &= testContains(Range.open(2f,4f), 4f, false);
        pass &= testContains(Range.open(2f,4f), 5f, false);
        
        System.out.println("Double open()");
        pass &= testContains(Range.open(2d,4d), 1d, false);
        pass &= testContains(Range.open(2d,4d), 2d, false);
        pass &= testContains(Range.open(2d,4d), 2.001d, true);
        pass &= testContains(Range.open(2d,4d), 3.999d, true);
        pass &= testContains(Range.open(2d,4d), 4d, false);
        pass &= testContains(Range.open(2d,4d), 5d, false);
        
        System.out.println("Character open()");
        pass &= testContains(Range.open('b','d'), 'a', false);
        pass &= testContains(Range.open('b','d'), 'b', false);
        pass &= testContains(Range.open('b','d'), 'c', true);
        pass &= testContains(Range.open('b','d'), 'd', false);
        pass &= testContains(Range.open('b','d'), 'e', false);
        
        System.out.println("String open()");
        pass &= testContains(Range.open("b","d"), "a", false);
        pass &= testContains(Range.open("b","d"), "b", false);
        pass &= testContains(Range.open("b","d"), "bc", true);
        pass &= testContains(Range.open("b","d"), "c", true);
        pass &= testContains(Range.open("b","d"), "cd", true);
        pass &= testContains(Range.open("b","d"), "d", false);
        pass &= testContains(Range.open("b","d"), "de", false);
        pass &= testContains(Range.open("b","d"), "e", false);
        
        System.out.println("toString()");
        pass &= testToString(Range.open(2,4), "(2..4)");
        pass &= testToString(Range.closed(2,4), "[2..4]");
        pass &= testToString(Range.openClosed(2,4), "(2..4]");
        pass &= testToString(Range.closedOpen(2,4), "[2..4)");
        pass &= testToString(Range.greaterThan(2), "(2..*)");
        pass &= testToString(Range.atLeast(2), "[2..*)");
        pass &= testToString(Range.lessThan(2), "(*..2)");
        pass &= testToString(Range.atMost(2), "(*..2]");
        
        System.out.println("Integer valueOf()");
        pass &= testValueOf("(2..4)", Integer.class);
        pass &= testValueOf("[2..4]", Integer.class);
        pass &= testValueOf("(2..4]", Integer.class);
        pass &= testValueOf("[2..4)", Integer.class);
        pass &= testValueOf("(2..*)", Integer.class);
        pass &= testValueOf("[2..*)", Integer.class);
        pass &= testValueOf("(*..2)", Integer.class);
        pass &= testValueOf("(*..2]", Integer.class);
        
        System.out.println("Float valueOf()");
        pass &= testValueOf("(2.128..4.25)", Float.class);
        
        System.out.println("Double valueOf()");
        pass &= testValueOf("(2.128..4.25)", Double.class);
        
        if (!pass)
            throw new IllegalStateException("Tests did not pass");
        else
            System.out.println("All passed.");
    }
    
}
