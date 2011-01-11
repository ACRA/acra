package util;

import java.util.Collection;
import java.util.LinkedList;

@SuppressWarnings("serial")
public class BoundedLinkedList<E> extends LinkedList<E> {
    private int maxSize = -1;

    public BoundedLinkedList(int maxSize) {
        super();
        this.maxSize = maxSize;
    }

    /* (non-Javadoc)
     * @see java.util.LinkedList#add(java.lang.Object)
     */
    @Override
    public boolean add(E object) {
        if(size() == maxSize) {
            removeFirst();
        }
        return super.add(object);
    }

    /* (non-Javadoc)
     * @see java.util.LinkedList#add(int, java.lang.Object)
     */
    @Override
    public void add(int location, E object) {
        if(size() == maxSize) {
            removeFirst();
        }
        super.add(location, object);
    }

    /* (non-Javadoc)
     * @see java.util.LinkedList#addAll(java.util.Collection)
     */
    @Override
    public boolean addAll(Collection<? extends E> collection) {
        int totalNeededSize = size() + collection.size();
        int overhead = totalNeededSize - maxSize;
        if(overhead > 0) {
            removeRange(0, overhead);
        }
        return super.addAll(collection);
    }

    /* (non-Javadoc)
     * @see java.util.LinkedList#addAll(int, java.util.Collection)
     */
    @Override
    public boolean addAll(int location, Collection<? extends E> collection) {
//        int totalNeededSize = size() + collection.size();
//        int overhead = totalNeededSize - maxSize;
//        if(overhead > 0) {
//            removeRange(0, overhead);
//        }
//        return super.addAll(location, collection);
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see java.util.LinkedList#addFirst(java.lang.Object)
     */
    @Override
    public void addFirst(E object) {
        // TODO Auto-generated method stub
//        super.addFirst(object);
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see java.util.LinkedList#addLast(java.lang.Object)
     */
    @Override
    public void addLast(E object) {
        add(object);
    }

    /* (non-Javadoc)
     * @see java.util.AbstractCollection#toString()
     */
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        
        for(E object : this) {
            result.append(object.toString());
        }
        
        return result.toString();
    }
    
    

}
