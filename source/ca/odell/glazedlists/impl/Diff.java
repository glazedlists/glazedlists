/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.impl;

import java.util.*;
// the core Glazed Lists package
import ca.odell.glazedlists.*;
import ca.odell.glazedlists.event.*;

/**
 * Implementation of Eugene W. Myer's paper, "An O(ND) Difference Algorithm and Its
 * Variations".
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class Diff {
    
    /**
     * Replace the complete contents of the target {@link EventList} with the complete
     * contents of the source {@link EventList} while making as few list changes
     * as possible.
     *
     * @param updates whether to fire update events for Objects that are equal in
     *      both {@link List}s.
     */
    public static void replaceAll(EventList target, List source, boolean updates) {
        Matcher listMatcher = new ListMatcher(target, source);
        List editScript = shortestEditScript(listMatcher);
        
        // target is x axis. Changes in X mean advance target index
        // source is y axis. Changes to y mean advance source index
        int targetIndex = 0;
        int sourceIndex = 0;

        // walk through points, applying changes as they arrive
        Point previousPoint = null;
        for(Iterator i = editScript.iterator(); i.hasNext(); ) {
            Point currentPoint = (Point)i.next();
            
            // skip the first point
            if(previousPoint == null) {
                previousPoint = currentPoint;
                continue;
            }
            
            // figure out what the relationship in the values is
            int deltaX = currentPoint.getX() - previousPoint.getX();
            int deltaY = currentPoint.getY() - previousPoint.getY();
            
            // handle an update
            if(deltaX == 1 && deltaY == 1) {
                if(updates) target.set(targetIndex, source.get(sourceIndex));
                targetIndex++;
                sourceIndex++;

            // handle a remove
            } else if(deltaX == 1 && deltaY == 0) {
                target.remove(targetIndex);

            // handle an insert
            } else if(deltaX == 0 && deltaY == 1) {
                target.add(targetIndex, source.get(sourceIndex));
                sourceIndex++;
                targetIndex++;

            // should never be reached
            } else {
                throw new IllegalStateException();
            }
            
            // the next previous point is this current point
            previousPoint = currentPoint;
        }
    }
    

    /**
     * Calculate the length of the longest common subsequence for the specified input.
     */
    private static List shortestEditScript(Matcher input) {
        // calculate limits based on the size of the input matcher
        int N = input.getAlphaLength();
        int M = input.getBetaLength();
        Point maxPoint = new Point(N, M);
        int maxSteps = N + M;
        
        // use previous round furthest reaching D-path to determine the 
        // new furthest reaching (D+1)-path
        Map furthestReachingPoints = new HashMap();

        // walk through in stages, each stage adding one non-diagonal.
        // D == count of non-diagonals in current stage
        for(int D = 0; D <= maxSteps; D++) {
            
            // exploit diagonals in order to save storing both X and Y
            // diagonal k means every point on k, (k = x - y)
            for(int k = -D; k <= D; k+=2) {
                // the furthest reaching D-path on the left and right diagonals
                // either of these may be null. The terms 'below left' and 'above right'
                // refer to the diagonals that the points are on and may not be
                // representative of the point positions
                Point belowLeft = (Point)furthestReachingPoints.get(new Integer(k-1));
                Point aboveRight = (Point)furthestReachingPoints.get(new Integer(k+1));
                
                // the new furthest reaching point to create
                Point point;
                
                // first round: we have matched zero in word X
                if(furthestReachingPoints.isEmpty()) {
                    point = new Point(0, 0);

                // if this is the leftmost diagonal, or the left edge is further
                // than the right edge, our new X is that value and our y is one greater
                // (shift verically by one)
                } else if(k == -D || (k != D && belowLeft.getX() < aboveRight.getX())) {
                    point = aboveRight.createDeltaPoint(0, 1);

                // if the right edge is further than the left edge, use that x
                // and keep y the same (shift horizontally by one)
                } else {
                    point = belowLeft.createDeltaPoint(1, 0);
                }
                
                // match as much diagonal as possible from the previous endpoint
                while(point.isLessThan(maxPoint) && input.matchPair(point.getX(), point.getY())) {
                    point = point.createDeltaPoint(1, 1);
                }

                // save this furthest reaching path
                furthestReachingPoints.put(new Integer(k), point);
                
                // if we're past the end, we have a solution!
                if(point.isEqualToOrGreaterThan(maxPoint)) {
                    return point.trail();
                }
            }
        }
        // no solution was found
        throw new IllegalStateException();
    }

    /**
     * Simple test program for Diff.
     */
    public static void main(String[] args) {
        if(args.length != 2) {
            System.out.println("Usage: LCS <alpha> <beta>");
            return;
        }
        
        String alpha = args[0];
        EventList alphaList = new BasicEventList();
        for(int c = 0; c < alpha.length(); c++) {
            alphaList.add(new Character(alpha.charAt(c)));
        }

        String beta = args[1];
        List betaList = new ArrayList();
        for(int c = 0; c < beta.length(); c++) {
            betaList.add(new Character(beta.charAt(c)));
        }
        
        drawGrid(new ListMatcher(alphaList, betaList));
        
        replaceAll(alphaList, betaList, false);
        System.out.println(alphaList);
    }
    
    /**
     * Draws a simple grid describing the specified matcher.
     */
    public static void drawGrid(Matcher matcher) {
        System.out.print("    ");
        for(int x = 0; x < matcher.getAlphaLength(); x++) {
            System.out.print(x);
        }
        System.out.println("");
        System.out.print("    ");
        for(int x = 0; x < matcher.getAlphaLength(); x++) {
            System.out.print(matcher.alphaAt(x));
        }
        System.out.println("");
        
        for(int y = 0; y < matcher.getBetaLength(); y++) {
            System.out.print(y);
            System.out.print(" ");
            System.out.print(matcher.betaAt(y));
            System.out.print(" ");
            for(int x = 0; x < matcher.getAlphaLength(); x++) {
                boolean match = matcher.matchPair(x, y);
                if(match) System.out.print("*");
                else System.out.print(".");
            }
            System.out.println("");
        }
    }

    /**
     * Models an X and Y point in a path. The top-left corner of the axis is the point
     * (0, 0). This is the lowest point in both the x and y dimensions. Negative points
     * are not allowed.
     */
    private static class Point {
        private int x = 0;
        private int y = 0;
        private Point predecessor = null;
        
        /**
         * Create a new point with the specified coordinates and no predecessor.
         */
        public Point(int x, int y) {
            this.x = x;
            this.y = y;
        }
        
        /**
         * Creates a new point from this point by shifting its values as specified.
         * The new point keeps a reference to its source in order to create a path later.
         */
        public Point createDeltaPoint(int deltaX, int deltaY) {
            Point result = new Point(x + deltaX, y + deltaY);
            result.predecessor = this;
            return result;
        }
    
        public int getX() {
            return x;
        }
        public int getY() {
            return y;
        }
        
        public boolean isLessThan(Point other) {
            return x < other.x && y < other.y;
        }
        public boolean isEqualToOrGreaterThan(Point other) {
            return x >= other.x && y >= other.y;
        }
        public String toString() {
            return "(" + x + "," + y + ")";
        }
        
        /**
         * Get a trail from the original point to this point. This is a list of all points
         * created via a series of {@link createDeltaPoint(int,int)} calls.
         */
        public List trail() {
            List reverse = new ArrayList();
            Point current = this;
            while(current != null) {
                reverse.add(current);
                current = current.predecessor;
            }
            Collections.reverse(reverse);
            return reverse;
        }
    }

    /**
     * Determines if the values at the specified points match or not.
     */
    private interface Matcher {
        public int getAlphaLength();
        public int getBetaLength();
        public boolean matchPair(int alphaIndex, int betaIndex);
        public char alphaAt(int index);
        public char betaAt(int index);
    }

    /**
     * Matcher for Strings.
     */
    private static class StringMatcher implements Matcher {
        private String alpha;
        private String beta;
        
        public StringMatcher(String alpha, String beta) {
            this.alpha = alpha;
            this.beta = beta;
        }
        public int getAlphaLength() {
            return alpha.length();
        }
        public char alphaAt(int index) {
            return alpha.charAt(index);
        }
        public char betaAt(int index) {
            return beta.charAt(index);
        }
        public int getBetaLength() {
            return beta.length();
        }
        public boolean matchPair(int alphaIndex, int betaIndex) {
            return alpha.charAt(alphaIndex) == beta.charAt(betaIndex);
        }
    }

    /**
     * Matcher for Lists.
     */
    private static class ListMatcher implements Matcher {
        private List alpha;
        private List beta;
        
        public ListMatcher(List alpha, List beta) {
            this.alpha = alpha;
            this.beta = beta;
        }
        public int getAlphaLength() {
            return alpha.size();
        }
        public char alphaAt(int index) {
            return alpha.get(index).toString().charAt(0);
        }
        public char betaAt(int index) {
            return beta.get(index).toString().charAt(0);
        }
        public int getBetaLength() {
            return beta.size();
        }
        public boolean matchPair(int alphaIndex, int betaIndex) {
            Object alphaValue = alpha.get(alphaIndex);
            Object betaValue = beta.get(betaIndex);
            if(alphaValue == null && betaValue == null) return true;
            else if(alphaValue == null || betaValue == null) return false;
            else return alphaValue.equals(betaValue);
        }
    }
}