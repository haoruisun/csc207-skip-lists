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
      for (SLNode<K, V> cur : this.Search(key)) {
        int level = this.height - 1;
        if (cur.next(level) != null) {
          if (this.comparator.compare(cur.next(level).key, key) == 0) {
            V val = cur.next(level).value;
            cur.next(level).value = value;
            return val;
          } // if
        } // if
        level--;
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
        this.front.add(node);
        node.setNext(i, null);
      } // for
    } // If the list is empty, add a new node
    else {
      if (h > INITIAL_HEIGHT) {
        for (int i = this.height; i < h; i++) {
          this.front.add(node);
          node.setNext(i, null);
        } // for
      } // if the height of the node is higher
      ArrayList<SLNode<K, V>> nodes = this.Search(key);
      for (int level = h - 1; level > -1; level--) {
        node.setNext(level, nodes.get(level).next(level));
        nodes.get(level).setNext(level, node);
        level--;
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
    for (SLNode<K, V> cur : this.Search(key)) {
      int level = this.height - 1;
      if (cur.next(level) != null) {
        if (this.comparator.compare(cur.next(level).key, key) == 0) {
          return cur.value;
        } // if
      } // if
      level--;
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
    int level = this.height - 1;
    for (SLNode<K, V> cur : this.Search(key)) {
      if (cur.next(level) != null) {
        if (this.comparator.compare(cur.next(level).key, key) == 0) {
          return true;
        } // if
      } // if
      level--;
    } // for
    return false;
  } // containsKey(K)

  @Override
  public V remove(K key) {
    ArrayList<SLNode<K, V>> nodes = this.Search(key);

    V val = null;
    for (int level = nodes.size() - 1; level > -1; level--) {
      SLNode<K, V> prev = nodes.get(level);
      if (prev != null) {
        val = prev.next(level).value;
        prev.setNext(level, prev.next(level).next(level));
      }
    } // for
    return val;
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
   * Check if the skip list is empty
   * 
   * @return
   */
  public boolean isEmpty() {
    for (SLNode<K, V> node : this.front) {
      if (node != null) {
        return false;
      } // if
    } // for
    return true;
  }// isEmpty()

  public ArrayList<SLNode<K, V>> Search(K key) {
    if (this.isEmpty()) {
      return this.front;
    } // if

    ArrayList<SLNode<K, V>> nodes = new ArrayList<SLNode<K, V>>();
    // Set up dummy node that points to front
    SLNode<K, V> dummy = new SLNode<K, V>(this.front);
    for (int level = this.height - 1; level > -1; level--) {
      SLNode<K, V> cur = dummy;
      while (cur.next(level) != null && this.comparator.compare(cur.key, key) < 0) {
        cur = cur.next(level);
      } // while
      nodes.add(cur);
    } // for
    return nodes;
  }// Search()


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

    public SLNode(ArrayList<SLNode<K, V>> next) {
      this.key = null;
      this.value = null;
      this.next = next;
    }

    // +---------+-----------------------------------------------------
    // | Methods |
    // +---------+

    /**
     * Get the next node at the specified level.
     */
    public SLNode<K, V> next(int level) {
      return this.next.get(level);
    } // next

    /**
     * Set the next node at the specified level.
     */
    public void setNext(int level, SLNode<K, V> next) {
      this.next.set(level, next);
    } // setNext(int, SLNode<K,V>)
  } // SLNode<K,V>
}
