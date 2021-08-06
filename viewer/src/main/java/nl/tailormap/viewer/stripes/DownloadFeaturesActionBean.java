/*
 * Copyright (C) 2012-2014 B3Partners B.V.
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
import net.sourceforge.stripes.action.After;
import net.sourceforge.stripes.action.Before;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.StreamingResolution;
import net.sourceforge.stripes.action.StrictBinding;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.controller.LifecycleStage;
import net.sourceforge.stripes.validation.OneToManyTypeConverter;
import net.sourceforge.stripes.validation.Validate;
import nl.tailormap.geotools.filter.visitor.RemoveDistanceUnit;
import nl.tailormap.viewer.audit.AuditMessageObject;
import nl.tailormap.viewer.audit.Auditable;
import nl.tailormap.viewer.config.app.Application;
import nl.tailormap.viewer.config.app.ApplicationLayer;
import nl.tailormap.viewer.config.app.ConfiguredAttribute;
import nl.tailormap.viewer.config.app.ConfiguredComponent;
import nl.tailormap.viewer.config.services.AttributeDescriptor;
import nl.tailormap.viewer.config.services.FeatureTypeRelation;
import nl.tailormap.viewer.config.services.Layer;
import nl.tailormap.viewer.config.services.SimpleFeatureType;
import nl.tailormap.viewer.config.services.WFSFeatureSource;
import nl.tailormap.viewer.features.CSVDownloader;
import nl.tailormap.viewer.features.ExcelDownloader;
import nl.tailormap.viewer.features.FeatureDownloader;
import nl.tailormap.viewer.features.GeoJSONDownloader;
import nl.tailormap.viewer.features.ShapeDownloader;
import nl.tailormap.viewer.helpers.AuthorizationsHelper;
import nl.tailormap.viewer.helpers.featuresources.FeatureSourceFactoryHelper;
import nl.tailormap.viewer.helpers.featuresources.WFSFeatureSourceHelper;
import nl.tailormap.viewer.util.ChangeMatchCase;
import nl.tailormap.viewer.util.FilterHelper;
import nl.tailormap.viewer.util.TailormapCQL;
import nl.tailormap.web.SharedSessionData;
import nl.tailormap.web.stripes.ErrorMessageResolution;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geotools.data.FeatureSource;
import org.geotools.data.Query;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.wfs.WFSDataStoreFactory;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.util.factory.GeoTools;
import org.json.JSONException;
import org.json.JSONObject;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.sort.SortBy;
import org.opengis.filter.sort.SortOrder;
import org.stripesstuff.stripersist.Stripersist;

import javax.activation.MimetypesFileTypeMap;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Meine Toonen
 */
@UrlBinding("/action/downloadfeatures")
@StrictBinding
public class DownloadFeaturesActionBean extends LocalizableApplicationActionBean implements Auditable {

    private static final Log log = LogFactory.getLog(DownloadFeaturesActionBean.class);

    private ActionBeanContext context;
    private boolean unauthorized;

    @Validate
    private Application application;

    @Validate
    private ApplicationLayer appLayer;

    @Validate
    private SimpleFeatureType featureType;

    private Layer layer = null;

    @Validate
    private boolean debug;

    @Validate
    private String type;

    @Validate
    private String params;

    @Validate(converter = OneToManyTypeConverter.class)
    private List<String> columns = new ArrayList<>();

    private AuditMessageObject auditMessageObject;

    //<editor-fold defaultstate="collapsed" desc="getters and setters">
    @Override
    public void setContext(ActionBeanContext abc) {
        this.context = abc;
    }

    @Override
    public ActionBeanContext getContext() {
        return context;
    }

    public boolean isUnauthorized() {
        return unauthorized;
    }

    public void setUnauthorized(boolean unauthorized) {
        this.unauthorized = unauthorized;
    }

    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }

    public ApplicationLayer getAppLayer() {
        return appLayer;
    }

    public void setAppLayer(ApplicationLayer appLayer) {
        this.appLayer = appLayer;
    }

    public SimpleFeatureType getFeatureType() {
        return featureType;
    }

    public void setFeatureType(SimpleFeatureType featureType) {
        this.featureType = featureType;
    }

    public Layer getLayer() {
        return layer;
    }

    public void setLayer(Layer layer) {
        this.layer = layer;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getParams() {
        return params;
    }

    public void setParams(String params) {
        this.params = params;
    }

    public AuditMessageObject getAuditMessageObject() {
        return this.auditMessageObject;
    }

    public List<String> getColumns() {
        return columns;
    }

    public void setColumns(List<String> columns) {
        this.columns = columns;
    }

    // </editor-fold>

    @After(stages=LifecycleStage.BindingAndValidation)
    public void loadLayer() {
        layer = appLayer.getService().getSingleLayer(appLayer.getLayerName(), Stripersist.getEntityManager());
    }

    @Before(stages=LifecycleStage.EventHandling)
    public void checkAuthorization() {
        if(application == null || appLayer == null
                || !AuthorizationsHelper.isAppLayerReadAuthorized(application, appLayer, context.getRequest(),Stripersist.getEntityManager())) {
            unauthorized = true;
        }
        auditMessageObject = new AuditMessageObject();
    }

    public Resolution download() throws JSONException, FileNotFoundException {
        JSONObject json = new JSONObject();
        if (unauthorized) {
            json.put("success", false);
            json.put("message", getBundle().getString("viewer.general.noauth"));
            return new StreamingResolution("application/json", new StringReader(json.toString(4)));
        }
        String sId = context.getRequest().getSession().getId();
        Map<String, String> sharedData = SharedSessionData.find(sId);
        String filter = sharedData.get(appLayer.getId().toString());
        File output = null;
        try {
            if (featureType != null || (layer != null && layer.getFeatureType() != null)) {
                FeatureSource fs;
                SimpleFeatureType ft = featureType;
                if (ft == null) {
                    ft = layer.getFeatureType();
                }

                if (isDebug() && ft.getFeatureSource() instanceof WFSFeatureSource) {
                    Map extraDataStoreParams = new HashMap();
                    extraDataStoreParams.put(WFSDataStoreFactory.TRY_GZIP.key, Boolean.FALSE);
                    SimpleFeatureType sft = layer.getFeatureType();
                    fs = WFSFeatureSourceHelper.openGeoToolsFSFeatureSource(sft, extraDataStoreParams, (WFSFeatureSource) sft.getFeatureSource());
                } else {
                    fs = FeatureSourceFactoryHelper.openGeoToolsFeatureSource(ft.getFeatureSource(), ft);
                }

                final Query q = new Query(fs.getName().toString());
                q.setMaxFeatures(getMaxFeatures());

                setFilter(filter, q, ft, Stripersist.getEntityManager());

                Map<String, AttributeDescriptor> featureTypeAttributes = new HashMap<String, AttributeDescriptor>();
                featureTypeAttributes = makeAttributeDescriptorList(ft);

                List<ConfiguredAttribute> attributes =  appLayer.getAttributes();

                attributes = filterAttributes(attributes);

                output = convert(ft, fs, q, type, attributes,featureTypeAttributes);

                json.put("success", true);

                // TODO see what is useful
                this.auditMessageObject.addMessage(ft);
                this.auditMessageObject.addMessage(q);
                this.auditMessageObject.addMessage(fs);
            }
        } catch (Exception e) {
            log.error("Error loading features", e);

            json.put("success", false);
            String message = "";
            if (type.equalsIgnoreCase("XLS") && e.toString().contains("The maximum length")) {
                message = MessageFormat.format(getBundle().getString("viewer.downloadfeaturesactionbean.1"),
                        getBundle().getString("viewer.downloadfeaturesactionbean.2"));
            } else {
                message = MessageFormat.format(getBundle().getString("viewer.downloadfeaturesactionbean.1"), e.toString() );
            }
            Throwable cause = e.getCause();
            while (cause != null) {
                message += "; " + cause.toString();
                cause = cause.getCause();
            }
            json.put("message", message);
        }

        if(json.getBoolean("success")){
            final FileInputStream fis = new FileInputStream(output);
            try{
                StreamingResolution res = new StreamingResolution(MimetypesFileTypeMap.getDefaultFileTypeMap().getContentType(output)) {
                    @Override
                    public void stream(HttpServletResponse response) throws Exception {
                        OutputStream out = response.getOutputStream();
                        IOUtils.copy(fis, out);
                        fis.close();
                    }
                };
                String name = output.getName();
                String extension = name.substring(name.lastIndexOf("."));
                String newName = "Download-"+ appLayer.getDisplayName(Stripersist.getEntityManager()) + "-"+type + extension;
                res.setFilename(newName);
                res.setAttachment(true);
                return res;
            }finally{
                output.delete();
            }
        }else{
            return new ErrorMessageResolution (json.getString("message"));
        }
    }

    private List<ConfiguredAttribute> filterAttributes(List<ConfiguredAttribute> attributes) {
        List<ConfiguredAttribute> newAttrs = new ArrayList<>();
        if (!columns.isEmpty()) {
            for (String column : columns) {
                for (ConfiguredAttribute attr : attributes) {
                    if(attr.getAttributeName().equals(column)){
                        newAttrs.add(attr);
                        break;
                    }
                }
            }
            attributes = newAttrs;

        }
        return attributes;
    }

    private File convert(SimpleFeatureType ft, FeatureSource fs, Query q, String type, List<ConfiguredAttribute> attributes, Map<String, AttributeDescriptor> featureTypeAttributes) throws IOException {
        Map<String, String> attributeAliases = new HashMap<String, String>();
        for (AttributeDescriptor ad : ft.getAttributes()) {
            if (ad.getAlias() != null) {
                attributeAliases.put(ad.getName(), ad.getAlias());
            }else{
                attributeAliases.put(ad.getName(), ad.getName());
            }
        }
        List<String> propertyNames = new ArrayList<String>();
        for (AttributeDescriptor ad : ft.getAttributes()) {
            propertyNames.add(ad.getName());
        }

        /* Use the first property as sort field, otherwise geotools while give a error when quering
         * a JDBC featureType without a primary key.
         */
        if (fs instanceof org.geotools.jdbc.JDBCFeatureSource && !propertyNames.isEmpty()) {
            setSortBy(q, propertyNames.get(0));
        }
        SimpleFeatureCollection fc =(SimpleFeatureCollection) fs.getFeatures(q);
        File f = null;

        FeatureDownloader downloader = null;
        if (type.equalsIgnoreCase("SHP")) {
            downloader = new ShapeDownloader(attributes,(SimpleFeatureSource) fs, featureTypeAttributes,attributeAliases, params);
        } else if (type.equalsIgnoreCase("XLS")) {
            downloader = new ExcelDownloader(attributes,(SimpleFeatureSource) fs, featureTypeAttributes,attributeAliases, params);
        } else if (type.equals("CSV")){
            downloader = new CSVDownloader(attributes, (SimpleFeatureSource)fs, featureTypeAttributes,attributeAliases, params);
        }else if (type.equals("GEOJSON")){
            downloader = new GeoJSONDownloader(attributes, (SimpleFeatureSource)fs, featureTypeAttributes,attributeAliases, params);
        }else {
            throw new IllegalArgumentException("No suitable type given: " + type);
        }

        try {
            downloader.init();

            SimpleFeatureIterator it = fc.features();
            try {
                while (it.hasNext()) {
                    SimpleFeature feature = it.next();
                    downloader.processFeature(feature);
                }
            } finally {
                it.close();
            }
            f = downloader.write();
        } catch (IOException ex) {
            log.error("Cannot create outputfile: ", ex);
            throw ex;
        } finally {
            fs.getDataStore().dispose();
        }
        return f;
    }

    /**
     * Makes a list of al the attributeDescriptors of the given FeatureType and
     * all the child FeatureTypes (related by join/relate)
     */
    private Map<String, AttributeDescriptor> makeAttributeDescriptorList(SimpleFeatureType ft) {
        Map<String,AttributeDescriptor> featureTypeAttributes = new HashMap<String,AttributeDescriptor>();
        for(AttributeDescriptor ad: ft.getAttributes()) {
            String name=ft.getId()+":"+ad.getName();
            //stop when already added. Stop a infinite configurated loop
            if (featureTypeAttributes.containsKey(name)){
                return featureTypeAttributes;
            }
            featureTypeAttributes.put(name, ad);
        }
        if (ft.getRelations()!=null){
            for (FeatureTypeRelation rel : ft.getRelations()){
                featureTypeAttributes.putAll(makeAttributeDescriptorList(rel.getForeignFeatureType()));
            }
        }
        return featureTypeAttributes;
    }

    protected void setFilter(String filter, Query q, SimpleFeatureType ft, EntityManager em) throws Exception {
        if (filter != null && filter.trim().length() > 0) {
            Filter f = TailormapCQL.toFilter(filter, em);
            f = (Filter) f.accept(new RemoveDistanceUnit(), null);
            f = (Filter) f.accept(new ChangeMatchCase(false), null);
            f = FilterHelper.reformatFilter(f, ft);
            q.setFilter(f);
        }
    }

    /**
     * Set sort on query
     *
     * @param q the query on which the sort is added
     * @param sort the name of the sort column
     */
    private void setSortBy(Query q, String sort) {
        FilterFactory2 ff2 = CommonFactoryFinder.getFilterFactory2(GeoTools.getDefaultHints());

        if (sort != null) {
            q.setSortBy(new SortBy[]{
                ff2.sort(sort, SortOrder.ASCENDING)
            });
        }
    }

    private static final String COMPONENT_NAME_EXTVERSION = "viewer.components.AttributeList";
    private static final String COMPONENT_NAME_NGVERSION = "viewer.components.NgAttributeList";
    private int getMaxFeatures(){
        EntityManager em = Stripersist.getEntityManager();
        int max = 1000;
        Set components = application.getComponents();
        for (Iterator it = components.iterator(); it.hasNext();) {
            ConfiguredComponent comp = (ConfiguredComponent) it.next();
            if ( (comp.getClassName().equals(COMPONENT_NAME_EXTVERSION)
                    || comp.getClassName().equals(COMPONENT_NAME_NGVERSION))
                    && comp.getConfig() != null) {
                JSONObject config = new JSONObject(comp.getConfig());
                String maxFeatures = config.optString("maxFeatures");
                if (maxFeatures != null && !maxFeatures.isEmpty()) {
                    try{
                        max = Integer.parseInt(maxFeatures);
                    }catch (NumberFormatException e){
                    }
                }
            }
        }
        return max;
    }
}
