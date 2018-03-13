/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl;

import ca.odell.glazedlists.EventList;

import java.util.*;

/**
 * Implementation of Eugene W. Myer's paper, "An O(ND) Difference Algorithm and
 * Its Variations", the same algorithm found in GNU diff.
 *
 * <p>Note that this is a cleanroom implementation of this popular algorithm
 * that is particularly suited for the Java programmer. The variable names are
 * descriptive and the approach is more object-oriented than Myer's sample
 * algorithm.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public final class Diff {

    /**
     * Convenience method for {@link #replaceAll(EventList,List,boolean,Comparator)
     * replaceAll()} that uses {@link Object#equals(Object)} to determine
     * equality.
     */
    public static <E> void replaceAll(EventList<E> target, List<E> source, boolean updates) {
        replaceAll(target, source, updates, GlazedListsImpl.<E>equalsComparator());
    }

    /**
     * Replace the complete contents of the target {@link EventList} with the
     * complete contents of the source {@link EventList} while making as few
     * list changes as possible.
     *
     * @param comparator a {@link Comparator} to use to test only for equality.
     *      This comparator shall return 0 to signal that two elements are
     *      equal, and nonzero otherwise.
     * @param updates whether to fire update events for Objects that are equal
     *      in both {@link List}s.
     */
    public static <E> void replaceAll(EventList<E> target, List<E> source,
                                      boolean updates, Comparator<E> comparator) {
        DiffMatcher listDiffMatcher = new ListDiffMatcher<>(target, source, comparator);
        List<Point> editScript = shortestEditScript(listDiffMatcher);

        // target is x axis. Changes in X mean advance target index
        // source is y axis. Changes to y mean advance source index
        int targetIndex = 0;
        int sourceIndex = 0;

        // walk through points, applying changes as they arrive
        Point previousPoint = null;
        for(Iterator<Point> i = editScript.iterator(); i.hasNext();) {
            Point currentPoint = i.next();

            // skip the first point
            if(previousPoint == null) {
                previousPoint = currentPoint;
                continue;
            }

            // figure out what the relationship in the values is
            int deltaX = currentPoint.getX() - previousPoint.getX();
            int deltaY = currentPoint.getY() - previousPoint.getY();

            // handle an update
            if(deltaX == deltaY) {
                if(updates) {
                    for(int u = 0; u < deltaX; u++) {
                        target.set(targetIndex + u, source.get(sourceIndex + u));
                    }
                }
                targetIndex += deltaX;
                sourceIndex += deltaY;

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
     * Calculate the length of the longest common subsequence for the specified
     * input.
     */
    private static List<Point> shortestEditScript(DiffMatcher input) {
        // calculate limits based on the size of the input matcher
        int N = input.getAlphaLength();
        int M = input.getBetaLength();
        Point maxPoint = new Point(N, M);
        int maxSteps = N + M;

        // use previous round furthest reaching D-path to determine the
        // new furthest reaching (D+1)-path
        Map<Integer,Point> furthestReachingPoints = new HashMap<>();

        // walk through in stages, each stage adding one non-diagonal.
        // D == count of non-diagonals in current stage
        for(int D = 0; D <= maxSteps; D++) {

            // exploit diagonals in order to save storing both X and Y
            // diagonal k means every point on k, (k = x - y)
            for(int k = -D; k <= D; k += 2) {
                // the furthest reaching D-path on the left and right diagonals
                // either of these may be null. The terms 'below left' and 'above
                // right' refer to the diagonals that the points are on and may
                // not be representative of the point positions
                Point belowLeft = furthestReachingPoints.get(new Integer(k - 1));
                Point aboveRight = furthestReachingPoints.get(new Integer(k + 1));

                // the new furthest reaching point to create
                Point point;

                // first round: we have matched zero in word X
                if(furthestReachingPoints.isEmpty()) {
                    point = new Point(0, 0);

                // if this is the leftmost diagonal, or the left edge is
                // further than the right edge, our new X is that value and
                // our y is one greater (shift verically by one)
                } else if(k == -D || (k != D && belowLeft.getX() < aboveRight.getX())) {
                    point = aboveRight.createDeltaPoint(0, 1);

                // if the right edge is further than the left edge, use that
                // x and keep y the same (shift horizontally by one)
                } else {
                    point = belowLeft.createDeltaPoint(1, 0);
                }

                // match as much diagonal as possible from the previous endpoint
                while(point.isLessThan(maxPoint) && input.matchPair(point.getX(), point.getY())) {
                    point = point.incrementDiagonally();
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
     * Models an X and Y point in a path. The top-left corner of the axis is the point (0,
     * 0). This is the lowest point in both the x and y dimensions. Negative points are
     * not allowed.
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
         * Creates a new point from this point by shifting its values as specified. The
         * new point keeps a reference to its source in order to create a path later.
         */
        public Point createDeltaPoint(int deltaX, int deltaY) {
            Point result = new Point(x + deltaX, y + deltaY);
            result.predecessor = this;
            return result;
        }

        /**
         * Shifts <code>x</code> and <code>y</code> values down and to the
         * right by one.
         */
        public Point incrementDiagonally() {
            Point result = createDeltaPoint(1, 1);

            // shortcut to the predecessor (to save memory!)
            if(predecessor != null) {
                int deltaX = result.x - predecessor.x;
                int deltaY = result.y - predecessor.y;

                if(deltaX == deltaY) {
                    result.predecessor = this.predecessor;
                }
            }

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

        @Override
        public String toString() {
            return "(" + x + "," + y + ")";
        }

        /**
         * Get a trail from the original point to this point. This is a list of
         * all points created via a series of {@link #createDeltaPoint(int,int)}
         * calls.
         */
        public List<Point> trail() {
            List<Point> reverse = new ArrayList<>();
            Point current = this;
            while (current != null) {
                reverse.add(current);
                current = current.predecessor;
            }
            Collections.reverse(reverse);
            return reverse;
        }
    }

    /**
     * Determines if the values at the specified points match or not.
     *
     * <p>This class specifies that each element should specify a character value.
     * This is for testing and debugging only and it is safe for implementing
     * classes to throw {@link UnsupportedOperationException} for both the
     * {@link #alphaAt(int)} and {@link #betaAt(int)} methods.
     */
    interface DiffMatcher {
        public int getAlphaLength();

        public int getBetaLength();

        public boolean matchPair(int alphaIndex, int betaIndex);

        /**
         * Output a character representing the specified element, for
         * the convenience of testing.
         */
        public char alphaAt(int index);
        public char betaAt(int index);
    }

    /**
     * Matcher for Lists.
     */
    static class ListDiffMatcher<E> implements DiffMatcher {
        private List<E> alpha;
        private List<E> beta;
        private Comparator<E> comparator;

        public ListDiffMatcher(List<E> alpha, List<E> beta, Comparator<E> comparator) {
            this.alpha = alpha;
            this.beta = beta;
            this.comparator = comparator;
        }

        @Override
        public int getAlphaLength() {
            return alpha.size();
        }

        @Override
        public char alphaAt(int index) {
            return alpha.get(index).toString().charAt(0);
        }

        @Override
        public char betaAt(int index) {
            return beta.get(index).toString().charAt(0);
        }

        @Override
        public int getBetaLength() {
            return beta.size();
        }

        @Override
        public boolean matchPair(int alphaIndex, int betaIndex) {
            return (comparator.compare(alpha.get(alphaIndex), beta.get(betaIndex)) == 0);
        }
    }
}