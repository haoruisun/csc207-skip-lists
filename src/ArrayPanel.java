import java.awt.Dimension;
import java.awt.Color;
import java.awt.Graphics;
import javax.swing.JPanel;

// Citation: Assignment 7.

/**
 * Constructs a new ArrayPanel that renders the given time to the screen.
 * 
 * @author Haorui Sun
 */
@SuppressWarnings("serial")
public class ArrayPanel extends JPanel {
  private int[] time;

  public ArrayPanel(int[] time2, int width, int height) {
    this.time = time2;
    this.setPreferredSize(new Dimension(width, height));
  }// ArrayPanel()

  /**
   * Constructs a new bar graph for notes
   * 
   * @param g Graphics
   */
  @Override
  public void paintComponent(Graphics g) {
    Dimension di = this.getPreferredSize();
    g.setColor(Color.white);
    g.fillRect(0, 0, di.width, di.height);
    for (int itemIndex = 0; itemIndex < time.length; itemIndex++) {
      g.setColor(Color.black);
      g.fillRect(itemIndex * 3, (di.height - time[itemIndex] / 2), 3, 3);
    } // for

  }// paintComponent()
}
