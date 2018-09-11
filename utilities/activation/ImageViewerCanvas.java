package ADaMSoft.utilities.activation;

import java.awt.*;

public class ImageViewerCanvas extends Canvas
{
  private Image canvas_image = null;

  /**
   * The constructor
   */
  public ImageViewerCanvas()
    {

    }

  /**
   * set the image
   */
  public void setImage(Image new_image)
    {
      canvas_image = new_image;
      this.invalidate();
      this.repaint();
    }

  /**
   * getPreferredSize
   */
  public Dimension getPreferredSize()
    {
      Dimension d = null;

      if(canvas_image == null)
	{
	  d = new Dimension(200, 200);
	}
      else
	d = new Dimension(canvas_image.getWidth(this),
			  canvas_image.getHeight(this));

      return d;
    }
  /**
   * paint method
   */
  public void paint(Graphics g)
    {

      if(canvas_image != null)
	g.drawImage(canvas_image, 0, 0, this);

    }

}
