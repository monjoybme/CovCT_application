package com.emory.covCT;

import android.graphics.Color;

public class Gradients
{
    /**
     * Produces a gradient using the University of Minnesota's school colors, from maroon (low) to gold (high)
     */
    public final static int[] GRADIENT_MAROON_TO_GOLD = createGradient(Color.rgb(128,0,0), Color.rgb(255, 215, 0), 500);

    /**
     * Produces a gradient from blue (low) to red (high)
     */
    public final static int[] GRADIENT_BLUE_TO_RED = createGradient(Color.BLUE, Color.RED, 500);

    /**
     * Produces a gradient from black (low) to white (high)
     */
    public final static int[] GRADIENT_BLACK_TO_WHITE = createGradient(Color.BLACK, Color.WHITE, 500);

    /**
     *Produces a gradient from red (low) to green (high)
     */
    public final static int[] GRADIENT_RED_TO_GREEN = createGradient(Color.RED, Color.GREEN, 500);

    /**
     *Produces a gradient through green, yellow, orange, red
     */
    //public final static int[] GRADIENT_GREEN_YELLOW_ORANGE_RED = createMultiGradient(new int[]{Color.GREEN, Color.YELLOW, Color.rgb(255,165,0), Color.RED}, 500);

    /**
     * From https://revolution-computing.typepad.com/.a/6a010534b1db25970b01bb0931cd68970d-pi
     */
    public final static int[] GRADIENT_PLASMA = createMultiGradient(new int[]{Color.rgb(11, 1, 116), Color.rgb(188, 46, 102), Color.rgb(240, 250, 29)}, 500);

  //  public final static int[] GRADIENT_ALPHA = createGradient(new Color(0, 0, 0, 255), new Color(0, 0, 0, 0), 500);
    /**
     *Produces a gradient through the rainbow: violet, blue, green, yellow, orange, red
     */
  //  public final static int[] GRADIENT_RAINBOW = createMultiGradient(new Color[]{new Color(181, 32, 255), Color.blue, Color.green, Color.yellow, Color.orange, Color.red}, 500);

    /**
     *Produces a gradient for hot things (black, red, orange, yellow, white)
     */
   // public final static int[] GRADIENT_HOT = createMultiGradient(new Color[]{Color.black, new Color(87, 0, 0), Color.red, Color.orange, Color.yellow, Color.white}, 500);

    /**
     *Produces a different gradient for hot things (black, brown, orange, white)
     */
  //  public final static int[] GRADIENT_HEAT = createMultiGradient(new Color[]{Color.black, new Color(105, 0, 0), new Color(192, 23, 0), new Color(255, 150, 38), Color.white}, 500);

    /**
     *Produces a gradient through red, orange, yellow
     */
  //  public final static int[] GRADIENT_ROY = createMultiGradient(new Color[]{Color.red, Color.orange, Color.yellow}, 500);

    /**
     * Creates an array of Color objects for use as a gradient, using a linear
     * interpolation between the two specified colors.
     * @param one Color used for the bottom of the gradient
     * @param two Color used for the top of the gradient
     * @param numSteps The number of steps in the gradient. 250 is a good number.
     */
    public static int[] createGradient(final int one, final int two, final int numSteps)
    {
        int r1 = Color.red(one);
        int g1 = Color.green(one);
        int b1 = Color.blue(one);
        int a1 = Color.alpha(one);

        int r2 = Color.red(two);
        int g2 = Color.green(two);
        int b2 = Color.blue(two);
        int a2 = Color.alpha(two);

        int newR = 0;
        int newG = 0;
        int newB = 0;
        int newA = 0;

        int[] gradient = new int[numSteps];
        double iNorm;
        for (int i = 0; i < numSteps; i++)
        {
            iNorm = i / (double)numSteps; //a normalized [0:1] variable
            newR = (int) (r1 + iNorm * (r2 - r1));
            newG = (int) (g1 + iNorm * (g2 - g1));
            newB = (int) (b1 + iNorm * (b2 - b1));
            newA = (int) (a1 + iNorm * (a2 - a1));
            gradient[i] = Color.rgb(newR,newG,newB);
        }

        return gradient;
    }

    /**
     * Creates an array of Color objects for use as a gradient, using an array of Color objects. It uses a linear interpolation between each pair of points. The parameter numSteps defines the total number of colors in the returned array, not the number of colors per segment.
     * @param colors An array of Color objects used for the gradient. The Color at index 0 will be the lowest color.
     * @param numSteps The number of steps in the gradient. 250 is a good number.
     */
    public static int[] createMultiGradient(int[] colors, int numSteps)
    {
        //we assume a linear gradient, with equal spacing between colors
        //The final gradient will be made up of n 'sections', where n = colors.length - 1
        int numSections = colors.length - 1;
        int gradientIndex = 0; //points to the next open spot in the final gradient
        int[] gradient = new int[numSteps];
        int[] temp;

        if (numSections <= 0)
        {
            throw new IllegalArgumentException("You must pass in at least 2 colors in the array!");
        }

        for (int section = 0; section < numSections; section++)
        {
            //we divide the gradient into (n - 1) sections, and do a regular gradient for each
            temp = createGradient(colors[section], colors[section+1], numSteps / numSections);
            for (int i = 0; i < temp.length; i++)
            {
                //copy the sub-gradient into the overall gradient
                gradient[gradientIndex++] = temp[i];
            }
        }

        if (gradientIndex < numSteps)
        {
            //The rounding didn't work out in our favor, and there is at least
            // one unfilled slot in the gradient[] array.
            //We can just copy the final color there
            for (/* nothing to initialize */; gradientIndex < numSteps; gradientIndex++)
            {
                gradient[gradientIndex] = colors[colors.length - 1];
            }
        }

        return gradient;
    }
}