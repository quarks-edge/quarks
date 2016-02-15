package quarks.window;
import java.util.AbstractSequentialList;
import java.util.LinkedList;
import java.util.ListIterator;

/**
 * A window contents list that maintains insertion time.
 *
 * @param <T> Type of tuples in the list
 */
public class InsertionTimeList<T> extends AbstractSequentialList<T> {
    
    private final LinkedList<T> tuples = new LinkedList<>();
    private final LinkedList<Long> times = new LinkedList<>();
    
    void evictOlderThan(long evictTime) {
        while(!times.isEmpty() && times.getFirst() <= evictTime){
            remove();
        }
    }
    
    long nextEvictDelay(long timeMs) {
        long firstTupleTime = times.get(0);
        long nextEvictTime = firstTupleTime + timeMs;
        
        long timeToNextEvict = nextEvictTime - System.currentTimeMillis();
        
        return Math.max(0, timeToNextEvict);
    }
    
    @Override
    public ListIterator<T> listIterator(int index) {
         return new TimedListIterator<>(tuples.listIterator(index), times.listIterator(index));
    }
    
    @Override
    public boolean add(T tuple) {
        tuples.add(tuple);
        times.add(System.currentTimeMillis());
        return true;
    }
    @Override
    public void clear() {
        tuples.clear();
        times.clear();
    }
    
    private void remove() {
        tuples.remove();
        times.remove();
    }

    @Override
    public int size() {
         return tuples.size();
    }
    
    private static class TimedListIterator<T> implements ListIterator<T> {
        
        private final ListIterator<T> ti;
        private final ListIterator<Long> iti;
        
        TimedListIterator(ListIterator<T> ti, ListIterator<Long> iti) {
            this.ti = ti;
            this.iti = iti;
        }     

        @Override
        public void add(T tuple) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean hasNext() {
            return ti.hasNext();
        }

        @Override
        public boolean hasPrevious() {
            return ti.hasPrevious();
        }

        @Override
        public T next() {
            iti.next();
            return ti.next();
        }

        @Override
        public int nextIndex() {
            return ti.nextIndex();
        }

        @Override
        public T previous() {
            iti.previous();
            return ti.previous();
        }

        @Override
        public int previousIndex() {
            return ti.previousIndex();
        }

        @Override
        public void remove() {
            ti.remove();
            iti.remove();            
        }

        @Override
        public void set(T arg0) {
            throw new UnsupportedOperationException();
        }       
    }
}
