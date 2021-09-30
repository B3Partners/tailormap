package nl.tailormap.viewer.userlayer;

import nl.tailormap.viewer.audit.AuditMessageObject;
import nl.tailormap.viewer.config.ClobElement;
import nl.tailormap.viewer.config.app.Application;
import nl.tailormap.viewer.config.app.ApplicationLayer;
import nl.tailormap.viewer.config.app.Level;
import nl.tailormap.viewer.config.app.StartLayer;
import nl.tailormap.viewer.config.services.Category;
import nl.tailormap.viewer.config.services.FeatureTypeRelation;
import nl.tailormap.viewer.config.services.FeatureTypeRelationKey;
import nl.tailormap.viewer.config.services.GeoService;
import nl.tailormap.viewer.config.services.JDBCFeatureSource;
import nl.tailormap.viewer.config.services.Layer;
import nl.tailormap.viewer.config.services.SimpleFeatureType;
import nl.tailormap.viewer.config.services.UpdateResult;
import nl.tailormap.viewer.config.services.WMSExceptionType;
import nl.tailormap.viewer.config.services.WMSService;
import nl.tailormap.viewer.helpers.featuresources.FeatureSourceFactoryHelper;
import nl.tailormap.viewer.helpers.featuresources.SimpleFeatureTypeHelper;
import nl.tailormap.viewer.helpers.services.GeoserviceFactoryHelper;
import nl.tailormap.viewer.helpers.services.WMSServiceHelper;
import nl.tailormap.viewer.util.SelectedContentCache;
import nl.tailormap.web.WaitPageStatus;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.persistence.EntityManager;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TailormapDBManager {
    private ApplicationLayer appLayer;
    private ApplicationLayer createdAppLayer;
    private EntityManager entityManager;
    private Application application;
    private String geoserverWorkspace;
    private String baseUrl;
    private Layer layer;
    private String filter;
    private AuditMessageObject auditMessageObject;

    private GeoService service;

    private final DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
    private static final Log LOG = LogFactory.getLog(TailormapDBManager.class);
    private static final String USERLAYER_NAME = "B3P - Gebruikerslagen (niet aanpassen)";


    public TailormapDBManager(EntityManager entityManager,
                              Application application,
                              ApplicationLayer appLayer,
                              GeoService service,
                              Layer layer,
                              String filter,
                              String geoserverWorkspace,
                              String baseUrl,
                              AuditMessageObject auditMessageObject) {
        this.entityManager = entityManager;
        this.application = application;
        this.appLayer = appLayer;
        this.geoserverWorkspace = geoserverWorkspace;
        this.baseUrl = baseUrl;
        this.service = service;
        this.layer = layer;
        this.filter = filter;
        this.auditMessageObject = auditMessageObject;
    }

    public boolean addLayer(String viewName, String title) {
        GeoService gs = retrieveUserLayerService();
        if (gs == null) {
            gs = createUserLayerService();
        }

        if (gs == null) {
            return false;
        }
        Layer newLayer = createLayer(viewName, gs, title);

        ApplicationLayer appLayer = null;
        if(newLayer != null){
            appLayer = createAppLayer(newLayer, gs, viewName);
        }

        return appLayer != null;
    }

    public boolean removeLayer(ApplicationLayer appLayer){
        // verwijder uit level
        // verwijder startlayers
        // verwijder applayer

        Level level = application.getRoot().getParentInSubtree(appLayer);
        level.getLayers().remove(appLayer);

        StartLayer sl = appLayer.getStartLayers().get(application);

        application.getStartLayers().remove(sl);
        entityManager.remove(sl);
        entityManager.remove(appLayer);
        entityManager.getTransaction().commit();

        entityManager.getTransaction().begin();
        SelectedContentCache.setApplicationCacheDirty(application, Boolean.TRUE, true, entityManager);
        entityManager.getTransaction().commit();

        return true;
    }

    private Layer createLayer(String viewName, GeoService gs, String title) {
        MutablePair<Layer, UpdateResult.Status> pair = null;
        try {
            // update service

            UpdateResult result =  GeoserviceFactoryHelper.update(entityManager, gs);
            gs.setName(USERLAYER_NAME);
            entityManager.persist(gs);
            if (result.getStatus() == UpdateResult.Status.FAILED) {
                LOG.error("Updating service failed: " + result.getMessage(), result.getException());
                return null;
            }
            pair = result.getLayerStatus().get(viewName);
        } catch (Exception e) {
            LOG.error("Error updating service failed: ", e);
        }

        if (pair.right == UpdateResult.Status.NEW) {
            Layer l = pair.left;
            l.setUserlayer(true);
            l.getDetails().put(Layer.DETAIL_USERLAYER_DATE_ADDED, new ClobElement(dateFormat.format(new Date())));
            l.getDetails().put(Layer.DETAIL_USERLAYER_FILTER,  new ClobElement(this.filter));
            l.getDetails().put(Layer.DETAIL_USERLAYER_ORIGINAL_LAYER_ID, new ClobElement(this.layer.getId().toString()));
            l.getDetails().put(Layer.DETAIL_USERLAYER_ORIGINAL_LAYERNAME, new ClobElement(this.layer.getName()));
            l.getDetails().put(Layer.DETAIL_USERLAYER_ORIGINAL_FEATURE_TYPE_NAME, new ClobElement(this.layer.getFeatureType().getTypeName()));
            l.getDetails().put(Layer.DETAIL_USERLAYER_USER, new ClobElement(this.auditMessageObject.getUsername()));
            l.getReaders().addAll(layer.getReaders());
            processFeatureType(l, viewName);
            return l;
        }
        return null;
    }

    private void processFeatureType(Layer l, String viewName){
        if(this.layer.getFeatureType()!= null && this.layer.getFeatureType().getFeatureSource() instanceof JDBCFeatureSource){
            JDBCFeatureSource fs = (JDBCFeatureSource)this.layer.getFeatureType().getFeatureSource();
            try {
                Date start = new Date();
                FeatureSourceFactoryHelper.update(entityManager,fs);
                Date end = new Date();
                LOG.error("time: " + (end.getTime() - start.getTime()));
                SimpleFeatureType newFt = fs.getFeatureType(viewName);
                l.setFeatureType(newFt);
                copyRelations(newFt);
            } catch (Exception e) {
                LOG.error("Error processing featuretype/featuresource",e);
            }

        }
    }

    private void copyRelations(SimpleFeatureType sft) {
        List<FeatureTypeRelation> rels = this.layer.getFeatureType().getRelations();
        for (FeatureTypeRelation rel : rels) {
            FeatureTypeRelation copy = new FeatureTypeRelation();
            copy.setFeatureType(sft);
            copy.setForeignFeatureType(rel.getForeignFeatureType());
            copy.setType(rel.getType());

            List<FeatureTypeRelationKey> keys = rel.getRelationKeys();
            for (FeatureTypeRelationKey key : keys) {
                FeatureTypeRelationKey keyCopy = new FeatureTypeRelationKey();
                keyCopy.setRightSide(key.getRightSide());
                keyCopy.setLeftSide(key.getLeftSide());
                keyCopy.setRelation(copy);
                copy.getRelationKeys().add(keyCopy);
            }
            sft.getRelations().add(copy);
        }
    }

    private ApplicationLayer createAppLayer(Layer l, GeoService gs, String viewName) {
        try {
            // maak applayer
            ApplicationLayer newAppLayer = new ApplicationLayer();
            newAppLayer.setService(gs);
            newAppLayer.setLayerName(viewName);

            newAppLayer.getReaders().addAll(this.appLayer.getReaders());

            StartLayer sl = new StartLayer();
            sl.setApplication(application);
            sl.setApplicationLayer(newAppLayer);

            newAppLayer.getStartLayers().put(application, sl);
            application.getStartLayers().add(sl);

            Level currentLevel = application.getRoot().getParentInSubtree(this.appLayer);
            currentLevel.getLayers().add(0,newAppLayer);

            entityManager.persist(application);
            entityManager.getTransaction().commit();
            this.createdAppLayer = newAppLayer;
            this.layer = l;

            entityManager.getTransaction().begin();
            SelectedContentCache.setApplicationCacheDirty(application, Boolean.TRUE, true, entityManager);
            entityManager.getTransaction().commit();
            entityManager.getTransaction().begin();
            SimpleFeatureTypeHelper.synchronizeFeaturetype(newAppLayer, entityManager, null, null, null, true);
            newAppLayer.getAttributes().forEach(configuredAttribute -> configuredAttribute.setVisible(true));
            entityManager.persist(newAppLayer);
            entityManager.getTransaction().commit();
            return newAppLayer;
        } catch (Exception e) {
            LOG.error("Error while inserting new layer into tailormap database: ", e);
        }
        return null;
    }

    // <editor-fold desc="Helper functions" defaultstate="collapsed">

    private GeoService retrieveUserLayerService() {

        List<GeoService> services = entityManager.createQuery("select distinct gs from GeoService gs "
                + "where gs.url like :q ")
                .setParameter("q", "%" + this.geoserverWorkspace + "%")
                .setMaxResults(1)
                .getResultList();

        return services.isEmpty() ? null : services.get(0);
    }

    private GeoService createUserLayerService() {
        GeoService userlayerService = null;
        try {
            Map params = new HashMap();

            params.put(GeoService.PARAM_USERNAME, this.service.getUsername());
            params.put(GeoService.PARAM_PASSWORD, this.service.getPassword());

            params.put(WMSService.PARAM_SKIP_DISCOVER_WFS, true);
            String url = this.baseUrl + this.geoserverWorkspace + "/wms";
            WaitPageStatus status = new WaitPageStatus();
            userlayerService = WMSServiceHelper.loadFromUrl(url, params, status, entityManager);
            ((WMSService) userlayerService).setException_type(WMSExceptionType.Inimage);

            userlayerService.setName(USERLAYER_NAME);
            userlayerService.setUsername(this.service.getUsername());
            userlayerService.setPassword(this.service.getPassword());

            userlayerService.getReaders().addAll(this.service.getReaders());

            Category category = entityManager.find(Category.class, this.service.getCategory().getId());
            userlayerService.setCategory(category);
            category.getServices().add(userlayerService);
            status.setCurrentAction("Service opslaan.");
            entityManager.persist(userlayerService);
            entityManager.getTransaction().commit();

        } catch (Exception e) {
            LOG.error("Error creating GeoService: ", e);
        }

        return userlayerService;
    }

    // </editor-fold>

    public ApplicationLayer getCreatedAppLayer() {
        return createdAppLayer;
    }

    public Layer getLayer() {
        return layer;
    }
}
