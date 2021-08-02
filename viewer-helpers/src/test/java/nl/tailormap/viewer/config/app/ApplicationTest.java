/*
 * Copyright (C) 2015 B3Partners B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package nl.tailormap.viewer.config.app;

import nl.tailormap.viewer.helpers.app.ApplicationHelper;
import nl.tailormap.viewer.util.TestUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Meine Toonen meinetoonen@b3partners.nl
 */
public class ApplicationTest extends TestUtil {

    private static final Log log = LogFactory.getLog(ApplicationTest.class);

    @Test
    public void testDeepCopy() throws Exception {
        initData(true);

        int expectedStartLayerSize = app.getStartLayers().size();
        int expectedStartLevelSize = app.getStartLevels().size();

        Application copy = ApplicationHelper.deepCopy(app);
        copy.setVersion("" + 666);
        entityManager.detach(app);
        entityManager.persist(copy);
        entityManager.getTransaction().commit();
        entityManager.getTransaction().begin();
        Application toTest = entityManager.find(Application.class, copy.getId());

        assertFalse(app.getId().equals(toTest.getId()));
        assertEquals(expectedStartLayerSize, toTest.getStartLayers().size());
        assertEquals(expectedStartLevelSize, toTest.getStartLevels().size());

        for (StartLayer startLayer : toTest.getStartLayers()) {
            assertEquals(toTest.getId(), startLayer.getApplication().getId());
        }

        for (StartLevel startLevel : toTest.getStartLevels()) {
            assertEquals(toTest.getId(), startLevel.getApplication().getId());
        }
        app = entityManager.merge(app);
    }

    @Test
    public void testDeepCopyReaders() throws Exception {
        initData(true);
        Application copy = ApplicationHelper.deepCopy(app);
        assertEquals(2, copy.getReaders().size());
        for (String reader : app.getReaders()) {
            assertTrue(copy.getReaders().contains(reader));
        }
    }

    @Test
    public void testDeleteApplications() throws Exception {
        initData(true);
        Application application = entityManager.find(Application.class, app.getId());
        Application copy = ApplicationHelper.deepCopy(application);
        entityManager.detach(application);
        copy.setVersion("123");
        entityManager.persist(copy);

        application = entityManager.merge(application);
    }

    @Test
    public void testMakeMashupLinkComponents() throws Exception {
        initData(true);
        try {
            int expectedStartLayerSize = app.getStartLayers().size();
            int expectedStartLevelSize = app.getStartLevels().size();
            int expectedRootStartLevelSize = app.getRoot().getStartLevels().size() * 2;

            Application mashup = ApplicationHelper.createMashup(app, "mashup", entityManager, true);
            entityManager.persist(mashup);

            entityManager.getTransaction().commit();
            entityManager.getTransaction().begin();

            assertFalse(app.getId().equals(mashup.getId()));
            assertEquals(expectedStartLayerSize, mashup.getStartLayers().size());
            assertEquals(expectedStartLevelSize, mashup.getStartLevels().size());

            for (StartLayer startLayer : mashup.getStartLayers()) {
                assertEquals(mashup.getId(), startLayer.getApplication().getId());
            }

            for (StartLevel startLevel : mashup.getStartLevels()) {
                assertEquals(mashup.getId(), startLevel.getApplication().getId());
            }

            assertEquals(expectedRootStartLevelSize, app.getRoot().getStartLevels().size());
            assertEquals(app.getRoot(), mashup.getRoot());

            Application.TreeCache tc = mashup.loadTreeCache(entityManager);
            List<Level> levels = tc.getLevels();
            List<ApplicationLayer> appLayers = tc.getApplicationLayers();
            for (ApplicationLayer appLayer : appLayers) {
                assertTrue(appLayer.getStartLayers().containsKey(mashup));
            }

            for (Level level : levels) {
                if (level.getParent() != null) {
                    assertTrue(level.getStartLevels().containsKey(mashup));
                }
            }
        } catch (Exception e) {
            log.error("Fout", e);
            assert (false);
        }
    }

    @Test
    public void testMakeMashupDontDuplicateStartLayers() {
        initData(true);
        try {
            int expectedStartLayerSize = app.getStartLayers().size();


            Application mashup = ApplicationHelper.createMashup(app, "mashup", entityManager, false);
            entityManager.persist(mashup);

            Application secondMashup = ApplicationHelper.createMashup(app, "mashup2", entityManager, false);
            entityManager.persist(secondMashup);

            entityManager.getTransaction().commit();
            entityManager.getTransaction().begin();

            // Check first mashup
            assertFalse(app.getId().equals(mashup.getId()));
            assertEquals(expectedStartLayerSize, mashup.getStartLayers().size());

            for (StartLayer startLayer : mashup.getStartLayers()) {
                assertEquals(mashup.getId(), startLayer.getApplication().getId());
            }

            Application.TreeCache tc = mashup.loadTreeCache(entityManager);
            List<ApplicationLayer> appLayers = tc.getApplicationLayers();
            for (ApplicationLayer appLayer : appLayers) {
                assertTrue(appLayer.getStartLayers().containsKey(mashup));
            }

            // second mashup
            assertFalse(app.getId().equals(secondMashup.getId()));
            assertEquals(expectedStartLayerSize, secondMashup.getStartLayers().size());

            for (StartLayer startLayer : secondMashup.getStartLayers()) {
                assertEquals(secondMashup.getId(), startLayer.getApplication().getId());
            }

            Application.TreeCache tc2 = secondMashup.loadTreeCache(entityManager);
            List<ApplicationLayer> appLayers2 = tc2.getApplicationLayers();
            for (ApplicationLayer appLayer : appLayers2) {
                assertTrue(appLayer.getStartLayers().containsKey(secondMashup));
            }
        } catch (Exception e) {
            log.error("Fout", e);
            assert (false);
        }
    }

    @Test
    public void testMakeMashupDontDuplicateStartLevels() {
        initData(true);
        try {
            int expectedStartLevelSize = app.getStartLevels().size();
            int expectedRootStartLevelSize = app.getRoot().getStartLevels().size() * 2;


            Application mashup = ApplicationHelper.createMashup(app, "mashup", entityManager, false);
            entityManager.persist(mashup);

            Application secondMashup = ApplicationHelper.createMashup(app, "mashup2", entityManager, false);
            entityManager.persist(secondMashup);

            entityManager.getTransaction().commit();
            entityManager.getTransaction().begin();

            // Check first mashup
            assertFalse(app.getId().equals(mashup.getId()));
            assertEquals(expectedStartLevelSize, mashup.getStartLevels().size());
            assertEquals(expectedRootStartLevelSize, app.getRoot().getStartLevels().size());
            assertEquals(app.getRoot(), mashup.getRoot());

            for (StartLevel startLevel : mashup.getStartLevels()) {
                assertEquals(mashup.getId(), startLevel.getApplication().getId());
            }

            // second mashup
            assertFalse(app.getId().equals(secondMashup.getId()));
            assertEquals(expectedStartLevelSize, secondMashup.getStartLevels().size());
            assertEquals(expectedRootStartLevelSize, app.getRoot().getStartLevels().size());
            assertEquals(app.getRoot(), secondMashup.getRoot());

            for (StartLevel startLevel : secondMashup.getStartLevels()) {
                assertEquals(secondMashup.getId(), startLevel.getApplication().getId());
            }

            Application.TreeCache tc2 = secondMashup.loadTreeCache(entityManager);

        } catch (Exception e) {
            log.error("Fout", e);
            assert (false);
        }
    }

    @Test
    public void testMakeMashupDontLinkComponents() throws Exception {
        initData(true);
        try {
            int expectedStartLayerSize = app.getStartLayers().size();
            int expectedStartLevelSize = app.getStartLevels().size();
            int expectedRootStartLevelSize = app.getRoot().getStartLevels().size() * 2;

            Application mashup = ApplicationHelper.createMashup(app, "mashup", entityManager, false);
            entityManager.persist(mashup);

            entityManager.getTransaction().commit();
            entityManager.getTransaction().begin();

            assertFalse(app.getId().equals(mashup.getId()));
            assertEquals(expectedStartLayerSize, mashup.getStartLayers().size());
            assertEquals(expectedStartLevelSize, mashup.getStartLevels().size());

            for (StartLayer startLayer : mashup.getStartLayers()) {
                assertEquals(mashup.getId(), startLayer.getApplication().getId());
            }

            for (StartLevel startLevel : mashup.getStartLevels()) {
                assertEquals(mashup.getId(), startLevel.getApplication().getId());
            }

            assertEquals(expectedRootStartLevelSize, app.getRoot().getStartLevels().size());
            assertEquals(app.getRoot(), mashup.getRoot());

            Application.TreeCache tc = mashup.loadTreeCache(entityManager);
            List<Level> levels = tc.getLevels();
            List<ApplicationLayer> appLayers = tc.getApplicationLayers();
            for (ApplicationLayer appLayer : appLayers) {
                assertTrue(appLayer.getStartLayers().containsKey(mashup));
            }

            for (Level level : levels) {
                if (level.getParent() != null) {
                    assertTrue(level.getStartLevels().containsKey(mashup));
                }
            }
        } catch (Exception e) {
            log.error("Fout", e);
            assert (false);
        }
    }

    @Test
    public void testMakeMashupOfApplicationWithExistingMashup() throws Exception {
        initData(true);
        try {
            int expectedStartLayerSize = app.getStartLayers().size();
            int expectedStartLevelSize = app.getStartLevels().size();
            int expectedRootStartLevelSize = app.getRoot().getStartLevels().size() * 3;

            Application mashup1 = ApplicationHelper.createMashup(app, "mashup", entityManager, false);
            entityManager.persist(mashup1);

            entityManager.getTransaction().commit();
            entityManager.getTransaction().begin();


            Application mashup = ApplicationHelper.createMashup(app, "mashup2", entityManager, false);
            entityManager.persist(mashup);

            entityManager.getTransaction().commit();
            entityManager.getTransaction().begin();

            assertFalse(app.getId().equals(mashup.getId()));
            assertEquals(expectedStartLayerSize, mashup.getStartLayers().size());
            assertEquals(expectedStartLevelSize, mashup.getStartLevels().size());

            for (StartLayer startLayer : mashup.getStartLayers()) {
                assertEquals(mashup.getId(), startLayer.getApplication().getId());
            }

            for (StartLevel startLevel : mashup.getStartLevels()) {
                assertEquals(mashup.getId(), startLevel.getApplication().getId());
            }

            assertEquals(expectedRootStartLevelSize, app.getRoot().getStartLevels().size());
            assertEquals(app.getRoot(), mashup.getRoot());

            Application.TreeCache tc = mashup.loadTreeCache(entityManager);
            List<Level> levels = tc.getLevels();
            List<ApplicationLayer> appLayers = tc.getApplicationLayers();
            for (ApplicationLayer appLayer : appLayers) {
                assertTrue(appLayer.getStartLayers().containsKey(mashup));
            }

            for (Level level : levels) {
                if (level.getParent() != null) {
                    assertTrue(level.getStartLevels().containsKey(mashup));
                }
            }
        } catch (Exception e) {
            log.error("Fout", e);
            assert (false);
        }
    }

}
