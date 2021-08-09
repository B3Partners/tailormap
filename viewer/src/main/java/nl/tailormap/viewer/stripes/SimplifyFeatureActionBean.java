/*
 * Copyright (C) 2019 B3Partners B.V.
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
package nl.tailormap.viewer.stripes;

import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.StreamingResolution;
import net.sourceforge.stripes.action.StrictBinding;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.validation.Validate;
import nl.tailormap.viewer.audit.AuditMessageObject;
import nl.tailormap.viewer.audit.Auditable;
import nl.tailormap.viewer.config.app.Application;
import nl.tailormap.viewer.config.app.ApplicationLayer;
import nl.tailormap.viewer.config.app.ConfiguredComponent;
import nl.tailormap.viewer.config.services.Layer;
import nl.tailormap.viewer.config.services.SimpleFeatureType;
import nl.tailormap.viewer.config.services.SolrConf;
import nl.tailormap.viewer.helpers.AuthorizationsHelper;
import nl.tailormap.viewer.helpers.featuresources.FeatureSourceFactoryHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geotools.data.FeatureSource;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.FeatureIterator;
import org.geotools.filter.identity.FeatureIdImpl;
import org.json.JSONArray;
import org.json.JSONObject;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.precision.GeometryPrecisionReducer;
import org.locationtech.jts.simplify.TopologyPreservingSimplifier;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.stripesstuff.stripersist.Stripersist;

import javax.persistence.EntityManager;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.Set;

/**
 *
 * @author Meine Toonen
 */
@UrlBinding("/action/simplify")
@StrictBinding
public class SimplifyFeatureActionBean extends LocalizableApplicationActionBean implements Auditable {

    private static final Log LOG = LogFactory.getLog(SimplifyFeatureActionBean.class);

    private ActionBeanContext context;

    @Validate
    private SimpleFeatureType sft;

    @Validate
    private SolrConf solrconfig;

    @Validate
    private ApplicationLayer appLayer;
    @Validate
    private Application application;
    @Validate
    private String featureId;

    // <editor-fold  defaultstate="collapsed" desc="Getters and Setters">
    @Override
    public ActionBeanContext getContext() {
        return context;
    }

    @Override
    public void setContext(ActionBeanContext context) {
        this.context = context;
    }

    public ApplicationLayer getAppLayer() {
        return appLayer;
    }

    public void setAppLayer(ApplicationLayer appLayer) {
        this.appLayer = appLayer;
    }

    public String getFeatureId() {
        return featureId;
    }

    public void setFeatureId(String featureId) {
        this.featureId = featureId;
    }

    @Override
    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }

    public SimpleFeatureType getSft() {
        return sft;
    }

    public void setSft(SimpleFeatureType sft) {
        this.sft = sft;
    }

    public SolrConf getSolrconfig() {
        return solrconfig;
    }

    public void setSolrconfig(SolrConf solrconfig) {
        this.solrconfig = solrconfig;
    }
    // </editor-fold>

    @Override
    public AuditMessageObject getAuditMessageObject() {
        AuditMessageObject aum = new AuditMessageObject();
        
        if(solrconfig != null){
            aum.addMessage("SolrConfig: " + solrconfig.getId());
        }
        if(appLayer != null){
            aum.addMessage("appLayer: " + appLayer.getId());
        }
        aum.addMessage("application: " + application.getId());
        aum.addMessage("featureId: " + featureId);
        if(sft != null){
            aum.addMessage("sft: " + sft.getId());
        }
        return aum;
    }

    @DefaultHandler
    public Resolution simplify() {
        JSONObject result = new JSONObject();
        result.put("success", false);
        FeatureSource fs = null;
        if (checkAuthorizations(result)) {
            try {
                JSONObject json = new JSONObject();

                json.put("success", Boolean.FALSE);
                Layer layer;
                EntityManager em = Stripersist.getEntityManager();

                if (appLayer != null) {
                    layer = appLayer.getService().getLayer(appLayer.getLayerName(), em);

                    if (layer == null) {
                        throw new Exception(getBundle().getString("viewer.simplifyfeatureactionbean.4"));
                    }

                    if (layer.getFeatureType() == null) {
                        throw new Exception(getBundle().getString("viewer.simplifyfeatureactionbean.5"));
                    }

                    sft = layer.getFeatureType();
                }

                fs = FeatureSourceFactoryHelper.openGeoToolsFeatureSource(sft);


                FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2();

                Filter filter = ff.id(new FeatureIdImpl(featureId));

                FeatureIterator<SimpleFeature> it = fs.getFeatures(filter).features();
                SimpleFeature sf = null;
                while (it.hasNext()) {
                    sf = it.next();
                }
                if (sf != null) {
                    Geometry geom = (Geometry) sf.getDefaultGeometry();
                    result.put("geom", simplify(geom));
                    result.put("success", true);
                }

            } catch (Exception ex) {
                LOG.error("Cannot simplify feature: ", ex);
                result.put("message", ex.getLocalizedMessage());
            } finally {
                if (fs != null) {
                    fs.getDataStore().dispose();
                }
            }
        }
        return new StreamingResolution("application/json", new StringReader(result.toString()));
    }

    private String simplify(Geometry geom) throws UnsupportedEncodingException {
        PrecisionModel pm = new PrecisionModel(100);
        GeometryPrecisionReducer gpr = new GeometryPrecisionReducer(pm);
        geom = gpr.reduce(geom);
        Geometry bbox = geom.getEnvelope();
        int megabytes = (2097152/* 2MB is the default tomcat max post size */ - 100 * 1024);
        double simplify = 1.0;
        String geomTxt = geom.toText();

        while ((geomTxt.getBytes("UTF-8").length > megabytes || geom.getCoordinates().length > 600) && simplify < 9999) {
            // start simplifying to reduce size, start of with 1 and
            // each iteration multiply with 10, max 4 steps, so [1,10, 100, 1000]
            // if geom still too large bail out and use bbox
            LOG.debug("Simplify selected feature geometry with distance of: " + simplify);
            geom = TopologyPreservingSimplifier.simplify(geom, simplify);
            geom = gpr.reduce(geom);
            geomTxt = geom.toText();
            simplify = 10 * simplify;
        }

        if (simplify > 9999) {
            return bbox.toText();
        } else {
            return geomTxt;
        }
    }

    private boolean checkAuthorizations(JSONObject result) {
        if (appLayer == null && sft == null) {
            result.put("message", getBundle().getString("viewer.simplifyfeatureactionbean.1"));
            return false;
        }
        EntityManager em = Stripersist.getEntityManager();
        if (appLayer != null) {
            if (!AuthorizationsHelper.isAppLayerWriteAuthorized(application, appLayer, context.getRequest(), em)) {
                result.put("message", getBundle().getString("viewer.simplifyfeatureactionbean.2"));
                return false;
            }
            return true;

        } else if (sft != null) {
            // Dit kan alleen als er via een zoekopdracht een call wordt gedaan, dus checken of solrconfig is geconfigureerd voor deze applicatie
            Set<ConfiguredComponent> comps = application.getComponents();
            for (ConfiguredComponent comp : comps) {
                if (comp.getClassName().equals("viewer.components.Search") || comp.getClassName().equals("viewer.components.DirectSearch")) {
                    JSONObject config = new JSONObject(comp.getConfig());
                    JSONArray searchConfigs = config.getJSONArray("searchconfigs");
                    for (Iterator<Object> iterator = searchConfigs.iterator(); iterator.hasNext();) {
                        JSONObject searchConfig = (JSONObject) iterator.next();
                        String type = searchConfig.getString("type");
                        if (type.equals("solr") || type.equals("attributesource") ){
                            JSONObject solrConfigs = searchConfig.optJSONObject("solrConfig");
                            if(solrConfigs == null){
                                solrConfigs = searchConfig.optJSONObject("asConfig");
                            }
                            Set<String> configs = solrConfigs.keySet();
                            for (String c : configs) {
                                JSONObject sc = solrConfigs.getJSONObject(c);
                                if (sc.getInt("solrConfigid") == solrconfig.getId()) {
                                    return true;
                                }

                            }
                        }

                    }
                }
            }
            result.put("message", getBundle().getString("viewer.simplifyfeatureactionbean.3"));
        }
        return false;

    }
}
