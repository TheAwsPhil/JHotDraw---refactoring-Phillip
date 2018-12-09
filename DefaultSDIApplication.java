/*
 * @(#)DefaultSDIApplication.java  1.5.1  2008-07-13
 *
 * Copyright (c) 1996-2008 by the original authors of JHotDraw
 * and all its contributors.
 * All rights reserved.
 *
 * The copyright of this software is owned by the authors and
 * contributors of the JHotDraw project ("the copyright holders").
 * You may not use, copy or modify this software, except in
 * accordance with the license agreement you entered into with
 * the copyright holders. For details see accompanying license terms.
 */
package org.jhotdraw.app;

import dk.sdu.mmmi.featuretracer.lib.FeatureEntryPoint;
import java.awt.*;
import java.beans.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import org.jhotdraw.app.action.*;
import org.jhotdraw.util.*;
import org.jhotdraw.util.prefs.*;

/**
 * A DefaultSDIApplication can handle the life cycle of a single document window
 * being presented in a JFrame. The JFrame provides all the functionality needed
 * to work with the document, such as a menu bar, tool bars and palette windows.
 * <p>
 * The life cycle of the application is tied to the JFrame. Closing the JFrame
 * quits the application.
 *
 * @author Werner Randelshofer
 * @version 1.5.1 2008-07-13 Don't add the view menu to the menu bar if it is
 * empty.
 * <br>1.5 2007-12-25 Added method updateViewTitle. Replaced currentProject by
 * activeProject in super class.
 * <br>1.4 2007-01-11 Removed method addStandardActionsTo.
 * <br>1.3 2006-05-03 Show asterisk in window title, when view has unsaved
 * changes.
 * <br>1.2.1 2006-02-28 Stop application when last view is closed.
 * <br>1.2 2006-02-06 Support for multiple open id added.
 * <br>1.1 2006-02-06 Revised.
 * <br>1.0 October 16, 2005 Created.
 */
public abstract class DefaultSDIApplication extends AbstractApplication {
    
    /**
     * Creates a new instance.
     */
    
    
    @FeatureEntryPoint(JHotDrawFeatures.APPLICATION_STARTUP)
    public DefaultSDIApplication() {
    }

    @Override
    public void launch(String[] args) {
        super.launch(args);
    }
    
    @Override
    public void init() {
        super.init();
    }

    @Override
    public void remove(View p) {
        super.remove(p);
    }
    
    public void dispose(View p) {
        super.dispose(p);
        }
    @Override
    public void configure(String[] args) {
        System.setProperty("apple.laf.useScreenMenuBar", "false");
        System.setProperty("com.apple.macos.useScreenMenuBar", "false");
        System.setProperty("apple.awt.graphics.UseQuartz", "false");
        System.setProperty("swing.aatext", "true");
    }



   
    /**
     * Returns the view component. Eventually wraps it into another component in
     * order to provide additional functionality.
     */
    
   protected void wrapViewComponentHelper(JComponent com, View p){
       LinkedList<Action> toolBarActions = new LinkedList<Action>();
       int id = 0;
       for (JToolBar tb : new ReversedList<JToolBar>(getModel().createToolBars(this, p))) {
           id++;
           JPanel panel = new JPanel(new BorderLayout());
           panel.add(tb, BorderLayout.NORTH);
           panel.add(com, BorderLayout.CENTER);
           com = panel;
           PreferencesUtil.installToolBarPrefsHandler(prefs, "toolbar." + id, tb);
           toolBarActions.addFirst(new ToggleVisibleAction(tb, tb.getName()));
       }
       com.putClientProperty("toolBarActions", toolBarActions);
   }
    
    protected Component wrapViewComponent(View p) {
        JComponent c = p.getComponent();
        if (getModel() != null) {
            wrapViewComponentHelper(c, p);          
        }
        return c;
    }

    
    //helper method for createMenuBar with focus on view menu
   protected void createMenuBarViewMenu(JMenu viewMenu, JMenu lastMenu, JMenuBar mb){
       if (viewMenu != null) {
            if (lastMenu != null && lastMenu.getText().equals(viewMenu.getText())) {
                for (Component c : lastMenu.getMenuComponents()) {
                    viewMenu.add(c);
                }
                mb.remove(lastMenu);
            }
            mb.add(viewMenu);
        }
   }
    //helper method for createMenuBar with focus on help menu
    protected void createMenuBarHelpMenu(JMenu helpMenu, JMenuBar mb){
        for (Component mc : mb.getComponents()) {
            JMenu m = (JMenu) mc;
            if (m.getText().equals(helpMenu.getText())) {
                for (Component c : helpMenu.getMenuComponents()) {
                    m.add(c);
                }
                helpMenu = null;
                break;
            }
        }
        if (helpMenu != null) {
            mb.add(helpMenu);
        }
    }
    
    /**
     * The view menu bar is displayed for a view. The default implementation
     * returns a new screen menu bar.
     */
    protected JMenuBar createMenuBar(final View p, java.util.List<Action> toolBarActions) {
        JMenuBar mb = new JMenuBar();
        mb.add(createFileMenu(p));
        JMenu lastMenu = null;
        for (JMenu mm : getModel().createMenus(this, p)) {
            mb.add(mm);
            lastMenu = mm;
        }
        JMenu viewMenu = createViewMenu(p, toolBarActions);
        createMenuBarViewMenu(viewMenu, lastMenu, mb);

        JMenu helpMenu = createHelpMenu(p);
        createMenuBarHelpMenu(helpMenu, mb);
        return mb;
    }

    protected JMenu createFileMenu(final View p) {
        ApplicationModel model = getModel();
        ResourceBundleUtil labels = ResourceBundleUtil.getBundle("org.jhotdraw.app.Labels");

        JMenuBar mb = new JMenuBar();
        JMenu m;
        JMenuItem mi;
        final JMenu openRecentMenu;

        m = new JMenu();
        labels.configureMenu(m, "file");
        m.add(model.getAction(ClearAction.ID));
        m.add(model.getAction(NewAction.ID));
        m.add(model.getAction(LoadAction.ID));
        if (model.getAction(LoadDirectoryAction.ID) != null) {
            m.add(model.getAction(LoadDirectoryAction.ID));
        }
        openRecentMenu = new JMenu();
        labels.configureMenu(openRecentMenu, "file.openRecent");
        openRecentMenu.add(model.getAction(ClearRecentFilesAction.ID));
        updateOpenRecentMenu(openRecentMenu);
        m.add(openRecentMenu);
        m.addSeparator();
        m.add(model.getAction(SaveAction.ID));
        m.add(model.getAction(SaveAsAction.ID));
        if (model.getAction(ExportAction.ID) != null) {
            mi = m.add(model.getAction(ExportAction.ID));
        }
        if (model.getAction(PrintAction.ID) != null) {
            m.addSeparator();
            m.add(model.getAction(PrintAction.ID));
        }
        m.addSeparator();
        m.add(model.getAction(ExitAction.ID));
        mb.add(m);

        addPropertyChangeListener(new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent evt) {
                String name = evt.getPropertyName();
                if (name == "viewCount") {
                    if (p == null || views().contains(p)) {
                    } else {
                        removePropertyChangeListener(this);
                    }
                } else if (name == "recentFiles") {
                    updateOpenRecentMenu(openRecentMenu);
                }
            }
        });

        return m;
    }

    /**
     * Updates the title of a view and displays it in the given frame.
     *
     * @param p The view.
     * @param f The frame.
     */
    protected void updateViewTitle(View p, JFrame f) {
        File file = p.getFile();
        String title;
        if (file == null) {
            title = labels.getString("unnamedFile");
        } else {
            title = file.getName();
        }
        if (p.hasUnsavedChanges()) {
            title += "*";
        }
        p.setTitle(labels.getFormatted("frame.title", title, getName(), p.getMultipleOpenId()));
        f.setTitle(p.getTitle());
    }

    /**
     * Updates the "file &gt; open recent" menu item.
     *
     * @param openRecentMenu
     */
    protected void updateOpenRecentMenu(JMenu openRecentMenu) {
        if (openRecentMenu.getItemCount() > 0) {
            JMenuItem clearRecentFilesItem = (JMenuItem) openRecentMenu.getItem(
                    openRecentMenu.getItemCount() - 1);
            openRecentMenu.removeAll();
            for (File f : recentFiles()) {
                openRecentMenu.add(new LoadRecentAction(DefaultSDIApplication.this, f));
            }
            if (recentFiles().size() > 0) {
                openRecentMenu.addSeparator();
            }
            openRecentMenu.add(clearRecentFilesItem);
        }
    }

    //Helper method for createViewMenu
    protected void createViewMenuHelper(JMenu m, JMenu b, JCheckBoxMenuItem c, java.util.List<Action> viewActions){
        if (viewActions != null && viewActions.size() > 0) {
            b = (viewActions.size() == 1) ? m : new JMenu(labels.getString("toolBars"));
            labels.configureMenu(m, "view");
            for (Action a : viewActions) {
                c = new JCheckBoxMenuItem(a);
                Actions.configureJCheckBoxMenuItem(c, a);
                b.add(c);
            }
            if (b != m) {
                m.add(b);
            }
        }

    }
    
    
    /**
     * Creates the view menu.
     *
     * @param p The View
     * @param viewActions Actions for the view menu
     * @return A JMenu or null, if no view actions are provided
     */
    protected JMenu createViewMenu(final View p, java.util.List<Action> viewActions) {
        ResourceBundleUtil labels = ResourceBundleUtil.getBundle("org.jhotdraw.app.Labels");

        JMenu m;
        JMenu m2 = null;
        JCheckBoxMenuItem cbmi = null;
        
        m = new JMenu();
        createViewMenuHelper(m, m2, cbmi, viewActions);
        return (m.getComponentCount() > 0) ? m : null;
    }

    protected JMenu createHelpMenu(View p) {
        ApplicationModel model = getModel();
        ResourceBundleUtil labels = ResourceBundleUtil.getBundle("org.jhotdraw.app.Labels");

        JMenu m;
        
        m = new JMenu();
        labels.configureMenu(m, "help");
        m.add(model.getAction(AboutAction.ID));

        return m;
    }
}
