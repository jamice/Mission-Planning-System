/*
 *  File: RowContextMenuHandler.java 
 *  Copyright (c) 2004-2007  Peter Kliem (Peter.Kliem@jaret.de)
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
package de.jaret.util.ui.timebars.swt;

import org.eclipse.swt.widgets.Menu;

import de.jaret.util.ui.timebars.model.TimeBarRow;

/**
 * Callback interface for retrieving a context menu on a header or hierarchy popup.
 * 
 * @author Peter Kliem
 * @version $Id: RowContextMenuHandler.java 800 2008-12-27 22:27:33Z kliem $
 */
public interface RowContextMenuHandler {
    /**
     * Retrieve a configured context menu appropriate for the row selection. May return <code>null</code> indicating no
     * context menu.
     * 
     * @param tbv requesting TimeBarViewer.
     * @param row row on which the context menu is popped up
     * @return a configured context menu or null.
     */
    Menu getContextMenu(TimeBarViewer tbv, TimeBarRow row);
}
