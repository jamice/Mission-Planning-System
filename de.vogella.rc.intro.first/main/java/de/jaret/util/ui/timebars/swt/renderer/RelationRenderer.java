/*
 *  File: RelationRenderer.java 
 *  Copyright (c) 2004-2008  Peter Kliem (Peter.Kliem@jaret.de)
 *  A commercial license is available, see http://www.jaret.de.
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package de.jaret.util.ui.timebars.swt.renderer;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.printing.Printer;

import de.jaret.util.date.Interval;
import de.jaret.util.ui.timebars.TimeBarViewerDelegate;
import de.jaret.util.ui.timebars.model.IIntervalRelation;
import de.jaret.util.ui.timebars.model.IIntervalRelation.Type;
import de.jaret.util.ui.timebars.model.IRelationalInterval;
import de.jaret.util.ui.timebars.model.TimeBarRow;

/**
 * Renderer rendering relations between intervals that implement the
 * IRelationalInterval interface. Does not support vertical orientation.
 * 
 * @author kliem
 * @version $Id: RelationRenderer.java 1073 2010-11-22 21:25:33Z kliem $
 */
public class RelationRenderer extends RendererBase implements IRelationRenderer {
	/** default cache size. */
	private static final int DEFAULT_CACHE_SIZE = 200;

	/** default rgb value for the lines. */
	private static final RGB DEFAULT_LINE_COLOR = new RGB(0, 0, 0);
	/** default rgb value for the selected lines. */
	private static final RGB DEFAULT_SELECTED_COLOR = new RGB(0, 0, 255);
	/** default line width for the connection lines. */
	private static final int DEFAULT_LINE_WIDTH = 1;
	/** default arrow size. */
	private static final int DEFAULT_ARROW_SIZE = 5;

	/** cache holding the drawn lines. */
	private List<Line> _cache;
	/** color used for the lines. */
	protected Color _lineColor;
	/** color used for the lines when selected. */
	protected Color _selectedColor;

	/** linewidth to use. */
	protected int _lineWidth = DEFAULT_LINE_WIDTH;
	/** arrow size. */
	protected int _arrowSize = DEFAULT_ARROW_SIZE;

	/**
	 * Create the renderer for a printer.
	 * 
	 * @param printer
	 *            printer device
	 */
	public RelationRenderer(Printer printer) {
		super(printer);
	}

	/**
	 * Create the renderer for use on displays.
	 */
	public RelationRenderer() {
		super(null);
	}

	/**
	 * Iniitlize the used colors for the device of the GC.
	 * 
	 * @param gc
	 *            GC
	 */
	private void initColors(GC gc) {
		if (_lineColor == null) {
			_lineColor = new Color(gc.getDevice(), DEFAULT_LINE_COLOR);
			_selectedColor = new Color(gc.getDevice(), DEFAULT_SELECTED_COLOR);
		}
	}

	/**
	 * {@inheritDoc} Processes all rows that are displayed.
	 */
	public void renderRelations(TimeBarViewerDelegate delegate, GC gc,
			boolean printing) {

		if (_lineColor == null) {
			initColors(gc);
		}
		_cache = new ArrayList<Line>(DEFAULT_CACHE_SIZE);

		int firstRow = delegate.getFirstRow();

		// set the clipping to include only the area of the diagram rect
		Rectangle clipSave = gc.getClipping();
		Rectangle nc = new Rectangle(delegate.getDiagramRect().x, delegate
				.getDiagramRect().y, delegate.getDiagramRect().width, delegate
				.getDiagramRect().height);
		gc.setClipping(gc.getClipping().intersection(nc));

		int upperYBound = delegate.getDiagramRect().y;
		int lowerYBound = upperYBound + delegate.getDiagramRect().height;
		// when using the clip bounds lines for non drawn relations will be lost
		// if (gc.isClipped()) {
		// upperYBound = gc.getClipping().y;
		// lowerYBound = upperYBound + gc.getClipping().height;
		// }

		int rowsDisplayed = delegate.getRowsDisplayed();
		for (int r = firstRow; r <= firstRow + rowsDisplayed + 1
				&& r < delegate.getRowCount(); r++) {
			TimeBarRow row = delegate.getRow(r);
			int y = delegate.yForRow(row);
			int rowHeight = delegate.getTimeBarViewState().getRowHeight(row);
			if (y == -1) {
				// no coord -> is not displayed
				break;
			}

			// row is drawn if either the beginning or the end is inside the
			// clipping rect
			// or if the upperBound is inside the row rect (clipping rect is
			// inside the row rect
			if ((y >= upperYBound && y <= lowerYBound)
					|| (y + rowHeight >= upperYBound && y + rowHeight <= lowerYBound)
					|| (upperYBound > y && upperYBound < y + rowHeight)) {
				drawRow(delegate, gc, delegate.getRow(r), y, printing);
			}
		}
		gc.setClipping(clipSave);

	}

	/**
	 * Proceses a single row.
	 * 
	 * @param delegate
	 *            the delegate
	 * @param gc
	 *            gc to draw on
	 * @param row
	 *            row to process
	 * @param y
	 *            y coordinate of the row
	 * @param printing
	 *            true when printing
	 */
	private void drawRow(TimeBarViewerDelegate delegate, GC gc, TimeBarRow row,
			int y, boolean printing) {
		for (Interval interval : row.getIntervals()) {
			// do not process filtered intervals
			if (delegate.getIntervalFilter() == null
					|| delegate.getIntervalFilter().isInResult(interval)) {
				if (interval instanceof IRelationalInterval) {
					IRelationalInterval rInterval = (IRelationalInterval) interval;
					for (IIntervalRelation relation : rInterval.getRelations()) {
						// double check both ends of the relation to be
						// unfiltered
						if (delegate.getIntervalFilter() == null
								|| (delegate.getIntervalFilter().isInResult(
										relation.getStartInterval()) && delegate
										.getIntervalFilter().isInResult(
												relation.getEndInterval()))) {
							if (!hasBeenDrawn(relation)) {
								drawDependency(delegate, gc, rInterval, y, row,
										relation);
							}
						}
					}
				}
			}
		}

	}
    int limitCoord(int coord) {
    	if (coord<-100) {
    		coord = -100;
    	} else if (coord>15000) {
    		coord = 15000;
    	}
    	return coord;
    }
    void limitCoord(int[] coords) {
    	for (int i=0;i<coords.length;i++) {
    		coords[i] = limitCoord(coords[i]);
    	}
    }
    
    Rectangle limitCoord(Rectangle rect) {
    	if (rect.x<-100) {
    		rect.width = rect.width+rect.x+100;
    		rect.x= -100;
    	}
    	if (rect.width>15000) {
    		rect.width = 15000;
    	}
    	return rect;
    }

	/**
	 * Draw a single relation.
	 * 
	 * @param delegate
	 *            the delegate
	 * @param gc
	 *            the gc to draw on
	 * @param rInterval
	 *            the interval from which we are coming
	 * @param y
	 *            y of the row
	 * @param row
	 *            the row the interval is on
	 * @param relation
	 *            the relation to draw
	 */
	private void drawDependency(TimeBarViewerDelegate delegate, GC gc,
			IRelationalInterval rInterval, int y, TimeBarRow row,
			IIntervalRelation relation) {
		int off = scaleX(_arrowSize + _arrowSize / 2);

		boolean selected = delegate.getSelectionModel().isSelected(relation);

		Color fg = gc.getForeground();
		int lineWidth = gc.getLineWidth();
		int linejoin = gc.getLineJoin();
		gc.setLineJoin(SWT.JOIN_ROUND);

		Color color = null;
		if (selected) {
			color = _selectedColor;
		} else {
			color = _lineColor;
		}
		gc.setForeground(color);
		gc.setLineWidth(scaleX(_lineWidth));

		IRelationalInterval beginTask;
		TimeBarRow beginRow;
		Point begin;

		IRelationalInterval endTask;
		TimeBarRow endRow;
		Point end;

		if (relation.getStartInterval().equals(rInterval)) {
			beginTask = rInterval;
			beginRow = row;

			endTask = relation.getEndInterval();
			endRow = getRowForInterval(delegate, endTask);
		} else {
			endTask = rInterval;
			endRow = row;

			beginTask = relation.getStartInterval();
			beginRow = getRowForInterval(delegate, beginTask);
		}

		int rheight = delegate.getTimeBarViewState().getRowHeight(beginRow);

		if (relation.getType() == IIntervalRelation.Type.END_BEGIN) {
			begin = getRightPoint(delegate, beginRow, beginTask);
			end = getLeftPoint(delegate, endRow, endTask);
			int ydir = end.y > begin.y ? 1 : -1;

			int[] points = new int[] { begin.x, begin.y, begin.x + off,
					begin.y, begin.x + off, begin.y + (rheight / 2 * ydir),
					end.x - off, begin.y + (rheight / 2 * ydir), end.x - off,
					end.y, end.x, end.y };
			limitCoord(points);
			gc.drawPolyline(points);

			gc.setForeground(color);
			registerLines(relation, points);

			drawArrow(gc, begin, end, color, relation.getDirection(), relation
					.getType());

		} else if (relation.getType() == IIntervalRelation.Type.BEGIN_BEGIN) {
			begin = getLeftPoint(delegate, beginRow, beginTask);
			end = getLeftPoint(delegate, endRow, endTask);
			int ydir = end.y > begin.y ? 1 : -1;

			int[] points = new int[] { begin.x, begin.y, begin.x - off,
					begin.y, begin.x - off, begin.y + (rheight / 2 * ydir),
					end.x - off, begin.y + (rheight / 2 * ydir), end.x - off,
					end.y, end.x, end.y };

			limitCoord(points);
			gc.drawPolyline(points);
			registerLines(relation, points);
			drawArrow(gc, begin, end, color, relation.getDirection(), relation
					.getType());
		} else if (relation.getType() == IIntervalRelation.Type.END_END) {
			begin = getRightPoint(delegate, beginRow, beginTask);
			end = getRightPoint(delegate, endRow, endTask);
			int ydir = end.y > begin.y ? 1 : -1;

			int[] points = new int[] { begin.x, begin.y, begin.x + off,
					begin.y, begin.x + off, begin.y + (rheight / 2 * ydir),
					end.x + off, begin.y + (rheight / 2 * ydir), end.x + off,
					end.y, end.x, end.y };
			limitCoord(points);
			gc.drawPolyline(points);
			registerLines(relation, points);
			drawArrow(gc, begin, end, color, relation.getDirection(), relation
					.getType());
		} else if (relation.getType() == IIntervalRelation.Type.BEGIN_END) {
			begin = getLeftPoint(delegate, beginRow, beginTask);
			end = getRightPoint(delegate, endRow, endTask);
			int ydir = end.y > begin.y ? 1 : -1;
			// unused int xdir = end.x > begin.x ? 1 : -1;

			int[] points = new int[] { begin.x, begin.y, begin.x - off,
					begin.y, begin.x - off, begin.y + (rheight / 2 * ydir),
					end.x + off, begin.y + (rheight / 2 * ydir), end.x + off,
					end.y, end.x, end.y };
			limitCoord(points);
			gc.drawPolyline(points);
			registerLines(relation, points);
			drawArrow(gc, begin, end, color, relation.getDirection(), relation
					.getType());
		}
		gc.setForeground(fg);
		gc.setLineWidth(lineWidth);
		gc.setLineJoin(linejoin);
	}

	/**
	 * Calculate the point on the right side of an interval for connection.
	 * 
	 * @param delegate
	 *            the delegate
	 * @param row
	 *            the row
	 * @param interval
	 *            the interval
	 * @return the point on the right side of the interval to connect the draw
	 *         relation to
	 */
	private Point getRightPoint(TimeBarViewerDelegate delegate, TimeBarRow row,
			Interval interval) {
		java.awt.Rectangle rect = delegate.getIntervalBounds(row, interval);
		return new Point(rect.x + rect.width, rect.y + rect.height / 2);
	}

	/**
	 * Calculate the point on the left side of an interval for connection.
	 * 
	 * @param delegate
	 *            the delegate
	 * @param row
	 *            the row
	 * @param interval
	 *            the interval
	 * @return the point on the left side of the interval to connect the draw
	 *         relation to
	 */
	private Point getLeftPoint(TimeBarViewerDelegate delegate, TimeBarRow row,
			Interval interval) {
		java.awt.Rectangle rect = delegate.getIntervalBounds(row, interval);
		return new Point(rect.x, rect.y + rect.height / 2);
	}

	/**
	 * Wrapper supplying the row for a given interval.
	 * 
	 * @param delegate
	 *            the delegate
	 * @param interval
	 *            interval to search the
	 * @return row or <code>null</code>
	 */
	private TimeBarRow getRowForInterval(TimeBarViewerDelegate delegate,
			Interval interval) {
		return delegate.getModel().getRowForInterval(interval);
	}

	/**
	 * Check whether a relation has already been drawn.
	 * 
	 * @param relation
	 *            relation to check
	 * @return <code>true</code> if the relation has already been drawn
	 */
	private boolean hasBeenDrawn(IIntervalRelation relation) {
		if (_cache != null) {
			for (Line l : _cache) {
				if (l.relation.equals(relation)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Register a line for later recognition.
	 * 
	 * @param relation
	 *            the relation
	 * @param coords
	 *            pairs of xy coords
	 */
	private void registerLines(IIntervalRelation relation, int[] coords) {
		for (int i = 0; i <= coords.length - 4; i += 2) {
			_cache.add(new Line(relation, coords[i], coords[i + 1],
					coords[i + 2], coords[i + 3]));
		}
	}

	/**
	 * Draw begin and/or end arrows for a relation depending on the direction.
	 * 
	 * @param gc
	 *            GC to paint on
	 * @param begin
	 *            begin point
	 * @param end
	 *            end point
	 * @param color
	 *            color to use
	 * @param direction
	 *            direction
	 * @param type
	 *            type of the relation used to determine which arrows to draw
	 */
	private void drawArrow(GC gc, Point begin, Point end, Color color,
			IIntervalRelation.Direction direction, Type type) {

		if (direction.equals(IIntervalRelation.Direction.BACK)
				|| direction.equals(IIntervalRelation.Direction.BI)) {
			if (type.equals(Type.END_BEGIN) || type.equals(Type.END_END)) {
				drawArrow(gc, begin, false, color);
			} else {
				drawArrow(gc, begin, true, color);
			}
		}
		if (direction.equals(IIntervalRelation.Direction.FORWARD)
				|| direction.equals(IIntervalRelation.Direction.BI)) {
			if (type.equals(Type.BEGIN_END) || type.equals(Type.END_END)) {
				drawArrow(gc, end, false, color);
			} else {
				drawArrow(gc, end, true, color);
			}
		}
	}

	/**
	 * Draw an arrow triangle.
	 * 
	 * @param gc
	 *            GC
	 * @param p
	 *            Point of the vertex
	 * @param leftToRight
	 *            <code>true</code> for left to right pointing
	 * @param color
	 *            color to use
	 */
	private void drawArrow(GC gc, Point p, boolean leftToRight, Color color) {
		Color bg = gc.getBackground();
		gc.setBackground(color);
		int off = scaleX(_arrowSize);
		int[] points;
		if (leftToRight) {
			int[] pts = { p.x, p.y, p.x - off, p.y - off, p.x - off, p.y + off };
			points = pts;
		} else {
			int[] pts = { p.x, p.y, p.x + off, p.y - off, p.x + off, p.y + off };
			points = pts;
		}
		limitCoord(points);
		gc.fillPolygon(points);
		gc.setBackground(bg);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<IIntervalRelation> getRelationsForCoord(int x, int y) {
		List<IIntervalRelation> result = new ArrayList<IIntervalRelation>(2);
		if (_cache != null) {
			for (Line line : _cache) {
				if (line.hit(x, y, 2)) {
					result.add(line.relation);
				}
			}
		}

		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getTooltip(int x, int y) {
		List<IIntervalRelation> result = getRelationsForCoord(x, y);
		if (result.size() == 0) {
			return null;
		} else {
			return result.get(0).toString();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void dispose() {
		_cache = null;
		if (_lineColor != null) {
			_lineColor.dispose();
			_selectedColor.dispose();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public IRelationRenderer createPrintRenderer(Printer printer) {
		return new RelationRenderer(printer);
	}

	/**
	 * Internal line represenation for caching.
	 * 
	 * @author kliem
	 * @version $Id: RelationRenderer.java 1073 2010-11-22 21:25:33Z kliem $
	 */
	public class Line {
		/** relation the line belongs to. */
		public IIntervalRelation relation;
		/** start x coordinate. */
		public int x1;
		/** start y coordinate. */
		public int y1;
		/** end x coordinate. */
		public int x2;
		/** end y coordinate. */
		public int y2;

		/**
		 * Construct a line.
		 * 
		 * @param relation
		 *            relation the line belongs to
		 * @param x1
		 *            start x
		 * @param y1
		 *            start y
		 * @param x2
		 *            end x
		 * @param y2
		 *            end y
		 */
		public Line(IIntervalRelation relation, int x1, int y1, int x2, int y2) {
			this.relation = relation;
			this.x1 = x1;
			this.y1 = y1;
			this.x2 = x2;
			this.y2 = y2;
		}

		/**
		 * Check whether the line is hit by a certain coordinate.
		 * 
		 * @param x
		 *            x coord
		 * @param y
		 *            y coord
		 * @param tolerance
		 *            the max delta
		 * @return true if hit
		 */
		public boolean hit(int x, int y, int tolerance) {
			if (x1 == x2) {
				if (x1 - tolerance <= x && x <= x1 + tolerance) {
					if ((y1 < y2 && y1 - tolerance <= y && y <= y2 + tolerance)
							|| (y2 < y1 && y2 - tolerance <= y && y <= y1
									+ tolerance)) {
						return true;
					}
				}
			} else {
				if (y1 == y2) {
					if (y1 - tolerance <= y && y <= y1 + tolerance) {
						if ((x1 < x2 && x1 - tolerance <= x && x <= x2
								+ tolerance)
								|| (x2 < x1 && x2 - tolerance <= x && x <= x1
										+ tolerance)) {
							return true;
						}
					}
				}
			}
			return false;
		}
	}

	/**
	 * Retrieve the current line width.
	 * 
	 * @return the current line width
	 */
	public int getLineWidth() {
		return _lineWidth;
	}

	/**
	 * Set the line width for the connection lines.
	 * 
	 * @param lineWidth
	 *            line width to set
	 */
	public void setLineWidth(int lineWidth) {
		_lineWidth = lineWidth;
	}

	/**
	 * Retrieve the current size set for the arrows.
	 * 
	 * @return the arrow size
	 */
	public int getArrowSize() {
		return _arrowSize;
	}

	/**
	 * Set the arrow size.
	 * 
	 * @param arrowSize
	 *            arrow size
	 */
	public void setArrowSize(int arrowSize) {
		_arrowSize = arrowSize;
	}

}
