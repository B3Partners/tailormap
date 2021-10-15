package nl.tailormap.viewer.userlayer;


import nl.tailormap.viewer.audit.AuditMessageObject;
import nl.tailormap.viewer.config.app.Application;
import nl.tailormap.viewer.config.app.ApplicationLayer;
import nl.tailormap.viewer.config.services.Category;
import nl.tailormap.viewer.config.services.GeoService;
import nl.tailormap.viewer.config.services.Layer;
import nl.tailormap.viewer.config.services.WMSExceptionType;
import nl.tailormap.viewer.config.services.WMSService;
import nl.tailormap.viewer.helpers.featuresources.FeatureSourceFactoryHelper;
import nl.tailormap.viewer.helpers.services.WMSServiceHelper;
import nl.tailormap.viewer.util.TailormapCQL;
import nl.tailormap.web.WaitPageStatus;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geotools.data.jdbc.FilterToSQLException;
import org.geotools.filter.text.cql2.CQLException;
import org.geotools.jdbc.JDBCDataStore;

import javax.persistence.EntityManager;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class UserLayerHandler {
    private static final Log LOG = LogFactory.getLog(UserLayerHandler.class);
    private static final String USERLAYER_NAME = "B3P - Gebruikerslagen (niet aanpassen)";
    private final AuditMessageObject auditMessageObject;
    private final ApplicationLayer appLayer;
    private final String query;
    private final String layerTitle;
    private final EntityManager entityManager;
    private final Application application;
    private final GeoServerManager manager;
    private final String geoserverStore;
    private final String geoserverWorkspace;
    private final String baseUrl;
    private ApplicationLayer createdAppLayer;
    private Layer layer;
    private String tableName;
    private GeoService service;
    private JDBCDataStore dataStore;
    private DataBase dataBase;
    private TailormapDBManager tmManager;

    /**
     * Construct an instance with all the things we need to do our jobs.
     * <strong>Note:</strong> You must call the {@link #dispose()} method when you are done, to prevent possible
     * leakage of resources.
     *
     * @param auditMessageObject for audit messages, not {@code null}
     * @param entityManager      needed to get access to the database, not {@code null}
     * @param application        Application we're working for
     * @param appLayer           appLayer to remove or that is the base for the userlayer, not {@code null}
     * @param query              CQL query, can be {@code null}
     * @param userLayerTitle     user friendly layername
     * @param geoserverWorkspace Workspacename for storing userlayers in geoserver
     * @param geoserverStore     Name of store in geoserver for userlayers
     */
    public UserLayerHandler(AuditMessageObject auditMessageObject, EntityManager entityManager, Application application,
                            ApplicationLayer appLayer, String query, String userLayerTitle, String geoserverWorkspace,
                            String geoserverStore) {
        this.auditMessageObject = auditMessageObject;
        this.entityManager = entityManager;
        this.application = application;
        this.appLayer = appLayer;
        this.query = query;
        this.layerTitle = userLayerTitle;
        this.geoserverWorkspace = geoserverWorkspace;
        this.geoserverStore = geoserverStore;

        try {
            this.service = this.appLayer.getService();
            this.layer = this.service.getLayer(this.appLayer.getLayerName(), this.entityManager);
            this.tableName = this.layer.getFeatureType().getTypeName();
            this.dataStore = (JDBCDataStore) FeatureSourceFactoryHelper.openGeoToolsFeatureSource(layer.getFeatureType()).getDataStore();
            this.dataBase = DataBaseFactory.getDataBase(dataStore);
        } catch (Exception e) {
            LOG.fatal("Problem opening datastore. " + e.getLocalizedMessage());
        }
        String serviceUrl = this.service.getUrl();
        this.baseUrl = serviceUrl.substring(0, serviceUrl.indexOf(GeoServerManager.GEOSERVER_PATTERN)
                + GeoServerManager.GEOSERVER_PATTERN.length());
        manager = new GeoServerManager(
                this.service.getUsername(),
                this.service.getPassword(),
                this.geoserverWorkspace,
                this.geoserverStore,
                this.baseUrl
        );

        tmManager = new TailormapDBManager(
                entityManager,
                application,
                appLayer,
                service,
                layer,
                this.query,
                geoserverWorkspace,
                baseUrl,
                this.auditMessageObject);
    }

    /**
     * validate query. Parses CQL to SQL for this {@code dataBase} and tries the produced SQl on the database
     *
     * @return {@code null} when OK or an error message when validation fails
     */
    public String validate() {
        String message;
        try {
            String sql = this.getSQLQuery();
            message = this.dataBase.preValidateView(tableName, sql);
            if (message != null) {
                message = "Selectielaag kan niet gemaakt worden. " + message;
            }
        } catch (CQLException | IOException e) {
            message = "Selectielaag kan niet gemaakt worden. Syntax fout in CQL expressie: " + e.getLocalizedMessage();
        } catch (FilterToSQLException e) {
            message = "Selectielaag kan niet gemaakt worden. Syntax fout in SQL expressie: " + e.getLocalizedMessage();
        }
        return message;
    }

    public boolean add() {
        String viewName = this.dataBase.createViewName(tableName);

        boolean succes = createView(viewName);
        if (succes) {
            succes = createWMSLayer(viewName);
        }

        if (succes) {
            succes = createUserLayer(viewName, this.layerTitle);
        } else {
            deleteWMSLayer(viewName);
            dropview(viewName);
        }

        return succes;
    }

    public boolean delete() {
        boolean success = removeApplayerFromApplication(appLayer);
        if (success) {
            success = deleteWMSLayer(this.layer.getName());
        } else {
            return success;
        }
        if (success) {
            success = dropview(this.layer.getName());

            if (!success) {
                createWMSLayer(this.layer.getName());
                createUserLayer(this.layer.getName(), this.layer.getTitle());
            }
        } else {
            createUserLayer(this.layer.getName(), this.layer.getTitle());
        }

        return success;
    }

    public boolean updateStyle(String cssStyle) {
        return this.manager.addStyleToLayer(
                this.layer.getName(),
                cssStyle
        );
    }

    /**
     * Must be called when you are done with creating/modifying your userlayer.
     * Will be implicitly called during GC/finalization.
     *
     * @see #finalize()
     */
    public void dispose() {
        if (this.dataBase != null) {
            this.dataBase.close();
        }
        if (this.dataStore != null) {
            this.dataStore.dispose();
        }
    }

    @Override
    public void finalize() throws Throwable {
        this.dispose();
        super.finalize();
    }

    public String getSQLQuery() throws CQLException, FilterToSQLException, IOException {
        TMFilterToSQL f = new TMFilterToSQL(this.dataStore, this.tableName);
        f.createFilterCapabilities();
        return f.encodeToString(TailormapCQL.toFilter(this.query, this.entityManager, false));
    }

    private boolean createView(String viewName) {
        boolean ok = false;
        try {
            String where = this.getSQLQuery();
            ok = this.dataBase.createView(viewName, this.tableName, where,
                    String.format(Locale.forLanguageTag("nl"),
                            /* Note that if you change this string you need to make sure that it does not contain
                            user input, or that it is properly SQL sanitized */
                            "GBI userlayer gemaakt van %s met query %s op %tc door gebruiker %s",
                            this.tableName, where, new Date(), this.auditMessageObject.getUsername())
            );

            this.auditMessageObject.addMessage("Aanmaken van view " + viewName + " is " + (ok ? "gelukt" : "mislukt"));
        } catch (FilterToSQLException | CQLException | IOException e) {
            LOG.error("Problem converting CQL to SQL. " + e.getLocalizedMessage());
        }
        return ok;
    }

    private boolean dropview(String viewName) {
        boolean ok = this.dataBase.dropView(viewName);
        this.auditMessageObject.addMessage("Verwijderen van view " + viewName + " is " + (ok ? "gelukt" : "mislukt"));
        return ok;
    }

    private boolean createWMSLayer(String viewName) {
        boolean ok = manager.createLayer(viewName, this.layerTitle, viewName);
        this.auditMessageObject.addMessage("Aanmaken van WMS layer " + viewName + " is " + (ok ? "gelukt" : "mislukt"));
        return ok;
    }

    private boolean deleteWMSLayer(String layerName) {
        boolean ok = manager.deleteLayer(layerName);
        this.auditMessageObject.addMessage(
                "Verwijderen van WMS layer " + layerName + " is " + (ok ? "gelukt" : "mislukt"));
        return ok;
    }

    /**
     * create the userlayer in the tailormap database.
     *
     * @return
     */
    private boolean createUserLayer(String viewName, String title) {
        boolean success = tmManager.addLayer(viewName, title);
        this.auditMessageObject.addMessage("Aanmaken van laag in Tailormap database " + title + " - " + viewName
                + " is " + (success ? "gelukt" : "mislukt"));

        this.createdAppLayer = tmManager.getCreatedAppLayer();
        this.layer = tmManager.getLayer();
        return success;
    }

    private boolean removeApplayerFromApplication(ApplicationLayer applicationLayer) {
        boolean success = tmManager.removeLayer(applicationLayer);
        this.auditMessageObject.addMessage(
                "Verwijderen van laag uit Tailormap database " + applicationLayer.getLayerName() + " is " + (success
                        ? "gelukt" : "mislukt"));
        this.createdAppLayer = appLayer;
        // delete appLayer, update application
        return true;
    }

    public ApplicationLayer getApplicationLayer() {
        return this.appLayer;
    }

    public long getAppLayerId() {
        return this.createdAppLayer.getId();
    }

    public String getLayerName() {
        return this.layer.getName();
    }

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

    public ApplicationLayer getCreatedAppLayer() {
        return createdAppLayer;
    }
}
