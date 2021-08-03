/*
 * Copyright (C) 2017 B3Partners B.V.
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
package nl.tailormap.viewer.util;

import nl.tailormap.viewer.config.app.ApplicationLayer;
import nl.tailormap.viewer.config.services.FeatureSource;
import nl.tailormap.viewer.config.services.SimpleFeatureType;
import nl.tailormap.viewer.config.services.WFSFeatureSource;
import nl.tailormap.viewer.helpers.featuresources.FeatureSourceFactoryHelper;
import org.geotools.data.Query;
import org.json.JSONArray;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author Meine Toonen meinetoonen@b3partners.nl
 */
public class FeatureToJsonTest {

    private final String url = "https://flamingo5.b3p.nl/geoserver/Test_omgeving/wms";
    private final String featureTypeName = "Test_omgeving:cbs_gemeente_2014";
    private final ApplicationLayer al = new ApplicationLayer();
    private final FeatureSource fs = new WFSFeatureSource();
    private final SimpleFeatureType ft = new SimpleFeatureType();

    public FeatureToJsonTest() {

    }

    @BeforeEach
    public void setup() {
        ft.setFeatureSource(fs);
        ft.setTypeName(featureTypeName);
        fs.setUrl(url);
        fs.getFeatureTypes().add(ft);

    }

    /**
     * Test of getJSONFeatures method, of class FeatureToJson.
     *
     * @throws java.lang.Exception if any
     */
    @Test
    public void testGetJSONFeaturesFirstPage() throws Exception {
        org.geotools.data.FeatureSource gtFS = FeatureSourceFactoryHelper.openGeoToolsFeatureSource(ft);
        assertNotNull(gtFS);

        Query q = new Query();
        q.setStartIndex(0);
        q.setMaxFeatures(10);
        String sort = "GM_NAAM";
        String dir = null;
        List<Long> attributesToInclude = new ArrayList<>();

        FeatureToJson instance = new FeatureToJson(false, true, false, attributesToInclude);
        JSONArray result = instance.getJSONFeatures(al, ft, gtFS, q, sort, dir, null, null, null);
        assertNotNull(result);
        assertFalse(result.length() == 0);
        assertEquals(10, result.length());
    }

    @Test
    public void testGetJSONFeaturesSecondPage() throws Exception {
        org.geotools.data.FeatureSource gtFS = FeatureSourceFactoryHelper.openGeoToolsFeatureSource(ft);
        assertNotNull(gtFS);

        Query q = new Query();
        q.setStartIndex(10);
        q.setMaxFeatures(10);
        String sort = "GM_NAAM";
        String dir = null;
        List<Long> attributesToInclude = new ArrayList<>();

        FeatureToJson instance = new FeatureToJson(false, true, false, attributesToInclude);
        JSONArray result = instance.getJSONFeatures(al, ft, gtFS, q, sort, dir, null, null, null);
        assertNotNull(result);
        assertFalse(result.length() == 0);
        assertEquals(10, result.length());
    }
}
