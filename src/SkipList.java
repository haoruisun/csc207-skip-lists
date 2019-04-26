import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
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

  /**
   * The counter used to determine the process that search uses.
   */
  double counter = 0;

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

  /**
   * Set the value associated with key.
   * 
   * @return the previous value associated with key (or null, if there's no such value)
   * @post If this.containsKey(key) == true, after set, get(key) = value. Otherwise, we create a new
   *       node to store data.
   * @throws NullPointerException if the key is null.
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  @Override
  public V set(K key, V value) {
    ArrayList<SLNode<K, V>> nodes = this.search(key);
    if (nodes.get(0).next(0) != null
        && this.comparator.compare(nodes.get(0).next(0).key, key) == 0) {
      V val = nodes.get(0).next(0).value;
      nodes.get(0).next(0).value = value;
      return val;
    } // If the list contains key, find the key and update the value

    // Initialize a new node
    int h = randomHeight();
    SLNode<K, V> node = new SLNode(key, value, h);

    if (this.isEmpty()) {
      if (h > INITIAL_HEIGHT) {
        for (int i = this.height; i < h; i++) {
          this.front.add(node);
          node.setNext(i, null);
          counter++;
        } // for
        this.height = h;
      } // if
      for (int i = 0; i < h; i++) {
        this.front.set(i, node);
        node.setNext(i, null);
      } // for
    } // If the list is empty, add a new node

    else {
      if (h > INITIAL_HEIGHT) {
        for (int i = this.height; i < h; i++) {
          this.front.add(node);
          node.setNext(i, null);
        } // for
        this.height = h;
        for (int level = nodes.size() - 1; level > -1; level--) {
          node.setNext(level, nodes.get(level).next(level));
          nodes.get(level).setNext(level, node);
          counter++;
        } // for
      } // if the height of the node is higher
      else {
        for (int level = h - 1; level > -1; level--) {
          node.setNext(level, nodes.get(level).next(level));
          nodes.get(level).setNext(level, node);
          counter++;
        } // for
      } // else: the height of the node is lower than
    } // else: the list is not empty and does not contain key
    this.size++;
    return null;
  } // set(K,V)

  /**
   * Get the value associated with key.
   * 
   * @throws IndexOutOfBoundsException if the key is not in the map.
   * @throws NullPointerException if the key is null.
   */
  @Override
  public V get(K key) {
    if (key == null) {
      throw new NullPointerException("null key");
    } // if
    if (!this.containsKey(key)) {
      throw new IndexOutOfBoundsException("invalid key");
    }
    return this.search(key).get(0).next(0).value;
  } // get(K,V)

  /**
   * Determine how many values are in the map.
   */
  @Override
  public int size() {
    return this.size;
  } // size()

  /**
   * Determine if a key appears in the table.
   */
  @Override
  public boolean containsKey(K key) {
    // Check if is initialized
    if (this.isEmpty()) {
      return false;
    } // if
      // Walk through the list and check if any node contains key
    ArrayList<SLNode<K, V>> nodes = this.search(key);
    return nodes.get(0).next(0) != null
        && this.comparator.compare(nodes.get(0).next(0).key, key) == 0;
  } // containsKey(K)

  /**
   * Remove the value with the given key.
   * 
   * @return The associated value (or null, if there is no associated value).
   * @throws NullPointerException if the key is null.
   */
  @Override
  public V remove(K key) {
    if (this.containsKey(key)) {
      ArrayList<SLNode<K, V>> nodes = this.search(key);
      V val = null;
      for (int level = 0; level < nodes.size(); level++) {
        SLNode<K, V> prev = nodes.get(level);
        if (prev.next(level) != null) {
          val = prev.next(level).value;
          prev.setNext(level, prev.next(level).next(level));
        }
        counter++;
      } // for
      this.size--;
      return val;
    } // if the key is in the list
    else
      return null;
  } // remove(K)

  /**
   * Get an iterator for all of the keys in the map.
   */
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

  /**
   * Get an iterator for all of the values in the map.
   */
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

  /**
   * Apply a function to each key/value pair.
   */
  @Override
  public void forEach(BiConsumer<? super K, ? super V> action) {
    Iterator<K> ik = this.keys();
    Iterator<V> iv = this.values();
    while (ik.hasNext() && iv.hasNext()) {
      action.accept(ik.next(), iv.next());
    } // while
  } // forEach

  // +----------------------+----------------------------------------
  // | Other public methods |
  // +----------------------+

  /**
   * Dump the list to some output location.
   */
  public void dump(PrintWriter pen) {
    String leading = "          ";

    SLNode<K, V> current = front.get(0);

    // Print some X's at the start
    pen.print(leading);
    for (int level = 0; level < this.height; level++) {
      pen.print(" X");
    } // for
    pen.println();
    printLinks(pen, leading);

    while (current != null) {
      // Print out the key as a fixed-width field.
      // (There's probably a better way to do this.)
      String str;
      if (current.key == null) {
        str = "<null>";
      } else {
        str = current.key.toString();
      } // if/else
      if (str.length() < leading.length()) {
        pen.print(leading.substring(str.length()) + str);
      } else {
        pen.print(str.substring(0, leading.length()));
      } // if/else

      // Print an indication for the links it has.
      for (int level = 0; level < current.next.size(); level++) {
        pen.print("-*");
      } // for
      // Print an indication for the links it lacks.
      for (int level = current.next.size(); level < this.height; level++) {
        pen.print(" |");
      } // for
      pen.println();
      printLinks(pen, leading);

      current = current.next.get(0);
    } // while

    // Print some O's at the start
    pen.print(leading);
    for (int level = 0; level < this.height; level++) {
      pen.print(" O");
    } // for
    pen.println();

  } // dump(PrintWriter)

  /**
   * Print some links (for dump).
   */
  void printLinks(PrintWriter pen, String leading) {
    pen.print(leading);
    for (int level = 0; level < this.height; level++) {
      pen.print(" |");
    } // for
    pen.println();
  } // printLinks

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
   * Re-zero the counter.
   */
  public void recounter() {
    this.counter = 0;
  }

  /**
   * Get an iterator for all of the nodes. (Useful for implementing the other iterators.)
   */
  Iterator<SLNode<K, V>> nodes() {
    return new Iterator<SLNode<K, V>>() {

      /**
       * A nodesence to the next node to return.
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

  /**
   * Determine if the skip list is empty
   */
  public boolean isEmpty() {
    return this.size <= 0;
  }// isEmpty()

  /**
   * Search for node that has key, and return all the nodes that are supposed to point to the node
   * with key.
   * 
   * @param key
   * @return nodes ArrayList<SLNode<K, V>>
   */
  public ArrayList<SLNode<K, V>> search(K key) {
    SLNode<K, V> dummy = new SLNode<K, V>(this.front);
    ArrayList<SLNode<K, V>> nodes = new ArrayList<SLNode<K, V>>();
    for (int i = 0; i < this.height; i++) {
      nodes.add(null);
    } // for

    if (this.isEmpty()) {
      for (int level = this.height - 1; level > -1; level--) {
        SLNode<K, V> cur = dummy;
        nodes.set(level, cur);
        counter++;
      }
      return nodes;
    } // if the list is empty

    // Set up dummy node that points to front
    for (int level = this.height - 1; level > -1; level--) {
      SLNode<K, V> cur = dummy;
      while (cur.next(level) != null && this.comparator.compare(cur.next(level).key, key) < 0) {
        cur = cur.next(level);
      } // while
      nodes.set(level, cur);
      counter++;
    } // for
    return nodes;
  }// search()


  /**
   * Nodes in the skip list.
   */
  @SuppressWarnings("hiding")
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

    /**
     * Create a new node with specified next.
     * 
     * @post this.key == null
     * @post this.value == null
     */
    public SLNode(ArrayList<SLNode<K, V>> next) {
      this.key = null;
      this.value = null;
      this.next = next;
      this.height = next.size();
    }// SLNode(next)

    // +---------+-----------------------------------------------------
    // | Methods |
    // +---------+

    /**
     * Get the next node at the specified level.
     */
    public SLNode<K, V> next(int level) {
      counter++;
      return this.next.get(level);
    } // next

    /**
     * Set the next node at the specified level.
     */
    public void setNext(int level, SLNode<K, V> next) {
      counter++;
      this.next.set(level, next);
    } // setNext(int, SLNode<K,V>)
  } // SLNode<K,V>
}
