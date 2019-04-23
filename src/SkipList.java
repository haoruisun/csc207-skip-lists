import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.function.BiConsumer;

/**
 * An implementation of skip lists.
 */
public class SkipList<K, V> implements SimpleMap<K, V> {

  // +-----------+---------------------------------------------------
  // | Constants |
  // +-----------+

  /**
   * The initial height of the skip list.
   */
  static final int INITIAL_HEIGHT = 16;

  // +---------------+-----------------------------------------------
  // | Static Fields |
  // +---------------+

  static Random rand = new Random();

  // +--------+------------------------------------------------------
  // | Fields |
  // +--------+

  /**
   * Pointers to all the front elements.
   */
  ArrayList<SLNode<K, V>> front;

  /**
   * The comparator used to determine the ordering in the list.
   */
  Comparator<K> comparator;

  /**
   * The number of values in the list.
   */
  int size;

  /**
   * The current height of the skiplist.
   */
  int height;

  /**
   * The probability used to determine the height of nodes.
   */
  double prob = 0.5;

  // +--------------+------------------------------------------------
  // | Constructors |
  // +--------------+

  /**
   * Create a new skip list that orders values using the specified comparator.
   */
  public SkipList(Comparator<K> comparator) {
    this.front = new ArrayList<SLNode<K, V>>(INITIAL_HEIGHT);
    for (int i = 0; i < INITIAL_HEIGHT; i++) {
      front.add(null);
    } // for
    this.comparator = comparator;
    this.size = 0;
    this.height = INITIAL_HEIGHT;
  } // SkipList(Comparator<K>)

  /**
   * Create a new skip list that orders values using a not-very-clever default comparator.
   */
  public SkipList() {
    this((k1, k2) -> k1.toString().compareTo(k2.toString()));
  } // SkipList()


  // +-------------------+-------------------------------------------
  // | SimpleMap methods |
  // +-------------------+

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Override
  public V set(K key, V value) {
    if (this.containsKey(key)) {
      for (int level = INITIAL_HEIGHT - 1; level > 0; level--) {
        SLNode<K, V> cur = this.front.get(level);
        while (cur != null && this.comparator.compare(cur.key, key) <= 0) {
          if (this.comparator.compare(cur.key, key) == 0) {
            V val = cur.value;
            cur.value = value;
            return val;
          } // if
          cur = cur.next.get(level);
        } // while
      } // for
    } // If the list contains key, find the key and update the value
    // Initialize a new node
    int h = randomHeight();
    SLNode<K, V> node = new SLNode(key, value, h);
    if (this.isEmpty()) {
      if (h > INITIAL_HEIGHT) {
        this.height = h;
      } // if
      for (int i = 0; i < h; i++) {
        this.front.add(i, node);
      } // for
    } // If the list is empty, add a new node
    else {
      ArrayList<SLNode<K, V>> refer = new ArrayList<SLNode<K, V>>();
      for (int level = h - 1; level > 0; level--) {
        SLNode<K, V> cur = this.front.get(level);
        while (cur.next.get(level) != null && this.comparator.compare(cur.key, key) <= 0) {
          cur = cur.next.get(level);
        } // while
        refer.add(cur);
      } // for
      int index = refer.size() - 1;
      for (int level = 0; level < node.height; level++) {
        if (index >= 0) {
          node.next.add(level, refer.get(index));
          refer.get(index).next.add(level, node);
        } // if
        else {
          node.next.add(level, null);
          this.front.add(level, node);
        } // else
      } // for
    } // else: the list is not empty and does not contain key

    return null;
  } // set(K,V)

  @Override
  public V get(K key) {
    if (key == null) {
      throw new NullPointerException("null key");
    } // if
    if (!this.containsKey(key)) {
      throw new NoSuchElementException("invalid key");
    }
    for (int level = INITIAL_HEIGHT - 1; level > 0; level--) {
      SLNode<K, V> cur = this.front.get(level);
      while (cur != null && this.comparator.compare(cur.key, key) <= 0) {
        if (this.comparator.compare(cur.key, key) == 0) {
          return cur.value;
        } // if
        cur = cur.next.get(level);
      } // while
    } // for
    return null;
  } // get(K,V)

  @Override
  public int size() {
    return this.size;
  } // size()

  @Override
  public boolean containsKey(K key) {
    // Check if is initialized
    if (this.isEmpty()) {
      return false;
    } // if
      // Walk through the list and check if any node contains key
    for (int level = INITIAL_HEIGHT - 1; level > 0; level--) {
      SLNode<K, V> cur = this.front.get(level);
      while (cur != null && this.comparator.compare(cur.key, key) <= 0) {
        if (this.comparator.compare(cur.key, key) == 0) {
          return true;
        } // if
        cur = cur.next.get(level);
      } // while
    } // for
    return false;
  } // containsKey(K)

  @Override
  public V remove(K key) {
    // TODO Auto-generated method stub
    return null;
  } // remove(K)

  @Override
  public Iterator<K> keys() {
    return new Iterator<K>() {
      Iterator<SLNode<K, V>> nit = SkipList.this.nodes();

      @Override
      public boolean hasNext() {
        return nit.hasNext();
      } // hasNext()

      @Override
      public K next() {
        return nit.next().key;
      } // next()

      @Override
      public void remove() {
        nit.remove();
      } // remove()
    };
  } // keys()

  @Override
  public Iterator<V> values() {
    return new Iterator<V>() {
      Iterator<SLNode<K, V>> nit = SkipList.this.nodes();

      @Override
      public boolean hasNext() {
        return nit.hasNext();
      } // hasNext()

      @Override
      public V next() {
        return nit.next().value;
      } // next()

      @Override
      public void remove() {
        nit.remove();
      } // remove()
    };
  } // values()

  @Override
  public void forEach(BiConsumer<? super K, ? super V> action) {
    // TODO Auto-generated method stub

  } // forEach

  // +----------------------+----------------------------------------
  // | Other public methods |
  // +----------------------+

  /**
   * Dump the tree to some output location.
   */
  public void dump(PrintWriter pen) {
    // Forthcoming
  } // dump(PrintWriter)

  // +---------+-----------------------------------------------------
  // | Helpers |
  // +---------+

  /**
   * Pick a random height for a new node.
   */
  int randomHeight() {
    int result = 1;
    while (rand.nextDouble() < prob) {
      result = result + 1;
    }
    return result;
  } // randomHeight()

  /**
   * Get an iterator for all of the nodes. (Useful for implementing the other iterators.)
   */
  Iterator<SLNode<K, V>> nodes() {
    return new Iterator<SLNode<K, V>>() {

      /**
       * A reference to the next node to return.
       */
      SLNode<K, V> next = SkipList.this.front.get(0);

      @Override
      public boolean hasNext() {
        return this.next != null;
      } // hasNext()

      @Override
      public SLNode<K, V> next() {
        if (this.next == null) {
          throw new IllegalStateException();
        }
        SLNode<K, V> temp = this.next;
        this.next = this.next.next.get(0);
        return temp;
      } // next();
    }; // new Iterator
  } // nodes()

  // +---------+-----------------------------------------------------
  // | Helpers |
  // +---------+
  public boolean isEmpty() {
    for (SLNode<K, V> node : this.front) {
      if (node != null) {
        return false;
      } // if
    } // for
    return true;
  }// isEmpty()

  /*
   * public int search(K key) { // Check if is initialized if (this.isEmpty()) { return -1; }//if if
   * (this.containsKey(key)) { for (int level = INITIAL_HEIGHT - 1; level > 0; level--) { SLNode<K,
   * V> cur = this.front.get(level); while (cur != null && this.comparator.compare(cur.key, key) <=
   * 0) { if (this.comparator.compare(cur.key, key) == 0) { return this.front.get(0).next.; } // if
   * cur = cur.next.get(level); } // while } // for } }//search()
   */} // class SkipList


/**
 * Nodes in the skip list.
 */
class SLNode<K, V> {

  // +--------+------------------------------------------------------
  // | Fields |
  // +--------+

  /**
   * The key.
   */
  K key;

  /**
   * The value.
   */
  V value;

  /**
   * Pointers to the next nodes.
   */
  ArrayList<SLNode<K, V>> next;

  /**
   * The height.
   */
  int height;
  // +--------------+------------------------------------------------
  // | Constructors |
  // +--------------+

  /**
   * Create a new node of height n with the specified key and value.
   */
  public SLNode(K key, V value, int n) {
    this.key = key;
    this.value = value;
    this.next = new ArrayList<SLNode<K, V>>(n);
    for (int i = 0; i < n; i++) {
      this.next.add(null);
    } // for
    this.height = n;
  } // SLNode(K, V, int)

  // +---------+-----------------------------------------------------
  // | Methods |
  // +---------+

} // SLNode<K,V>
