import java.awt.BorderLayout;
import java.util.Random;
import javax.swing.JFrame;

// Citation: Assignment 7

/**
 * Draw a scatter plot of time used by skiplist with increasing length.
 * 
 * @author Haorui Sun
 */
public class Analysis {

  // Plot's width and height
  private static final int WIDTH = 1800;
  private static final int HEIGHT = 1200;

  public static void main(String[] args) {
    // frame setup
    JFrame frame = new JFrame();
    frame.getContentPane().setLayout(new BorderLayout());
    Random rand = new Random();
    int num = 0;

    // Test the skiplist's time consume by increasing length.
    int[] time = new int[1000];
    for (int i = 1; i < 1000; i++) {
      SkipList<Integer, Integer> test = new SkipList<Integer, Integer>();
      for (int j = 0; j <= i; j++) {
        test.set(j, j);
      } // for
      for (int k = 0; k < 20; k++) {
        test.recounter();
        test.get(rand.nextInt(i));
        num += test.counter;
      } // for
      time[i] = num / 20;
      num = 0;
    } // for

    // Draw the scatter plot.
    ArrayPanel arrayPanel = new ArrayPanel(time, WIDTH, HEIGHT);
    frame.setTitle("Time Analysis");
    frame.add(arrayPanel, BorderLayout.CENTER);
    frame.pack();
    frame.setResizable(false);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setVisible(true);
  }// main()

}// Analysis
