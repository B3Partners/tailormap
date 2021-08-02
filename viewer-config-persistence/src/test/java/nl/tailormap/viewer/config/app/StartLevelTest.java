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

import nl.tailormap.viewer.util.TestUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author Meine Toonen meinetoonen@b3partners.nl
 */
public class StartLevelTest extends TestUtil {

    private static final Log log = LogFactory.getLog(StartLevelTest.class);

    @Test
    public void persistLevel() {
        StartLevel sl = new StartLevel();
        sl.setSelectedIndex(16);
        persistEntityTest(sl, StartLevel.class);

        entityManager.refresh(sl);
        StartLevel test = entityManager.find(StartLevel.class, sl.getId());
        assertNotNull(test);

        assertEquals(new Integer(16), test.getSelectedIndex());
        assertEquals(6, entityManager.createQuery("FROM Level").getResultList().size());
    }

    @Test
    public void deleteStartLevel() throws URISyntaxException, SQLException, IOException {
        Application app = entityManager.find(Application.class, applicationId);

        Level level = entityManager.find(Level.class, 5L);

        StartLevel sl = new StartLevel();
        sl.setLevel(level);
        sl.setApplication(app);
        sl.setSelectedIndex(16);
        persistEntityTest(sl, StartLevel.class);

        Level levelExists = entityManager.find(Level.class, 5L);
        Application appExists = entityManager.find(Application.class, applicationId);

        assertNotNull(levelExists);
        assertNotNull(appExists);
        assertEquals(6, entityManager.createQuery("FROM Level").getResultList().size());
    }

}
