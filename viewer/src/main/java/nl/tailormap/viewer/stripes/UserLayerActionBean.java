package nl.tailormap.viewer.stripes;

import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.action.Before;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.StreamingResolution;
import net.sourceforge.stripes.action.StrictBinding;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.controller.LifecycleStage;
import net.sourceforge.stripes.validation.Validate;
import net.sourceforge.stripes.validation.ValidationErrorHandler;
import net.sourceforge.stripes.validation.ValidationErrors;
import net.sourceforge.stripes.validation.ValidationMethod;
import net.sourceforge.stripes.validation.ValidationState;
import nl.tailormap.i18n.LocalizableActionBean;
import nl.tailormap.viewer.audit.AuditMessageObject;
import nl.tailormap.viewer.audit.Auditable;
import nl.tailormap.viewer.config.app.Application;
import nl.tailormap.viewer.config.app.ApplicationLayer;
import nl.tailormap.viewer.helpers.app.ApplicationLayerHelper;
import nl.tailormap.viewer.helpers.services.GeoServiceHelper;
import nl.tailormap.viewer.userlayer.UserLayerHandler;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geotools.data.jdbc.FilterToSQLException;
import org.geotools.filter.text.cql2.CQLException;
import org.json.JSONObject;
import org.stripesstuff.stripersist.Stripersist;

import javax.servlet.http.HttpServletResponse;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.Locale;

@UrlBinding("/action/userlayer/{$event}/{application}/{appLayer}")
@StrictBinding
public class UserLayerActionBean extends LocalizableActionBean implements ActionBean, ValidationErrorHandler,
        Auditable {
    private static final Log LOG = LogFactory.getLog(UserLayerActionBean.class);
    private ActionBeanContext context;

    @Validate(required = true)
    private Application application;

    @Validate(required = true)
    private ApplicationLayer appLayer;

    @Validate(required = true, on = "add")
    private String query;

    @Validate(required = true, on = "add")
    private String title;

    @Validate(required = true, on = "put")
    private String style;

    private AuditMessageObject auditMessageObject;
    private boolean unauthorized;

    private String wellKnownUserLayerWorkspaceName;
    private String wellKnownUserLayerStoreName;

    // <editor-fold desc="Validation handlers" defaultstate="collapsed">
    @ValidationMethod(when = ValidationState.NO_ERRORS)
    public void validateUser(ValidationErrors errors) {
        Principal p = context.getRequest().getUserPrincipal();
        if (p == null) {
      //      errors.addGlobalError(new SimpleError("Geen gebruiker gevonden of niet aangemeld"));
        } else {
            this.auditMessageObject.setUsername(p.getName());
            this.auditMessageObject.setEvent(this.context.getEventName() + " userlayer");
        }
    }

    @Before(stages = LifecycleStage.EventHandling)
    public void checkLayerAndAuthorization() {
        if (application == null || appLayer == null
            // TODO add a new method "isLayerTODOAuthorized" to Authorizations
            // Layer layer = appLayer.getService().getLayer(appLayer.getLayerName(), Stripersist.getEntityManager());
            //  || !Authorizations.isLayerGeomWriteAuthorized(layer, context.getRequest(), Stripersist
            //  .getEntityManager())
        ) {
            unauthorized = true;
        }
    }

    @Before(stages = LifecycleStage.BindingAndValidation)
    public void initializeAuditMessageObject() {
        this.auditMessageObject = new AuditMessageObject();
    }

    @Before(stages = LifecycleStage.EventHandling)
    public void readContextParams() {
        this.wellKnownUserLayerStoreName = this.getContext().getServletContext().getInitParameter(
                "userlayer.geoserver.store");
        this.wellKnownUserLayerWorkspaceName = this.getContext().getServletContext().getInitParameter(
                "userlayer.geoserver.workspace");
    }
    // </editor-fold>

    @Override
    public Resolution handleValidationErrors(ValidationErrors errors) {
        StringBuilder msg = new StringBuilder("Validatiefouten: ");

        JSONObject json = (new JSONObject()).put("success", Boolean.FALSE);

        if (errors.hasFieldErrors()) {
            errors.entrySet().stream().forEach((entry) -> {
                entry.getValue().stream().map((e) -> {
                    if (LOG.isTraceEnabled()) {
                        msg.append("veld: ").append(entry.getKey()).append(", waarde: ");
                        msg.append(e.getFieldValue()).append(", melding: ");
                    }
                    return e;
                }).forEach((e) -> {
                    msg.append(e.getMessage(Locale.ROOT)).append("; ");
                });
            });
        }
        if (errors.get(ValidationErrors.GLOBAL_ERROR) != null) {
            errors.get(ValidationErrors.GLOBAL_ERROR).stream().forEach((e) -> {
                msg.append(e.getMessage(Locale.ROOT));
            });
        }
        json.put("error", msg.toString());
        return new StreamingResolution("application/json") {
            @Override
            public void stream(HttpServletResponse response) throws Exception {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                IOUtils.copy(new StringReader(json.toString()), response.getOutputStream(), StandardCharsets.UTF_8);
            }
        };
    }

    @DefaultHandler
    public Resolution noop() {
        JSONObject json = (new JSONObject()).put("success", Boolean.FALSE);
        json.put("error", "invalid request");
        return new StreamingResolution("application/json") {
            @Override
            public void stream(HttpServletResponse response) throws Exception {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                IOUtils.copy(new StringReader(json.toString()), response.getOutputStream(), StandardCharsets.UTF_8);
            }
        };
    }

    public Resolution validate() throws FilterToSQLException, CQLException {

        final UserLayerHandler ulh = new UserLayerHandler(auditMessageObject, Stripersist.getEntityManager(),
                application, appLayer, query, title, wellKnownUserLayerWorkspaceName,
                wellKnownUserLayerStoreName);

        boolean success = true;
        String isInvalidMsg = ulh.validate();
        final JSONObject jsonObject = (new JSONObject()).put("success", Boolean.TRUE);
        jsonObject.put("valid", isInvalidMsg);
        jsonObject.put("sql", ulh.getSQLQuery());

        return new StreamingResolution("application/json") {
            @Override
            public void stream(HttpServletResponse response) throws Exception {
                if (unauthorized) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                } else if (jsonObject.getBoolean("success")) {
                    response.setStatus(HttpServletResponse.SC_CREATED);
                } else {
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                }
                IOUtils.copy(new StringReader(jsonObject.toString()), response.getOutputStream(),
                        StandardCharsets.UTF_8);
            }
        };
    }

    public Resolution add() {
        final JSONObject jsonObject = (new JSONObject()).put("success", Boolean.FALSE);
        if (unauthorized) {
            jsonObject.put("error", "not authorized to add");
        } else {
            try {
                final JSONObject message = new JSONObject();
                final UserLayerHandler ulh = new UserLayerHandler(auditMessageObject, Stripersist.getEntityManager(),
                        application, appLayer, query, title, wellKnownUserLayerWorkspaceName,
                        wellKnownUserLayerStoreName);

                boolean success;
                String isInvalidMsg = ulh.validate();
                if (isInvalidMsg != null) {
                    success = false;
                    jsonObject.put("error", isInvalidMsg);
                } else {
                    success = ulh.add();
                }
                jsonObject.put("success", success);

                if (success) {
                    message.put("appLayerId", ulh.getAppLayerId());
                    message.put("layerName", ulh.getLayerName());
                    message.put("appLayer", ApplicationLayerHelper.toJSONObject(ulh.getCreatedAppLayer(), Stripersist.getEntityManager()));
                    message.put("service", GeoServiceHelper.toJSONObject(ulh.getCreatedAppLayer().getService(), false, null, false, false, Stripersist.getEntityManager()));
                    this.auditMessageObject.addMessage(
                            "UserLayer " + ulh.getLayerName() + " aangemaakt met id " + ulh.getAppLayerId());
                }
                jsonObject.put("message", message);

                ulh.dispose();
            } catch (Exception unforeseen) {
                LOG.error(unforeseen);
                jsonObject.put("error", "Fatale fout: " + unforeseen.getLocalizedMessage());
            }
        }
        return new StreamingResolution("application/json") {
            @Override
            public void stream(HttpServletResponse response) throws Exception {
                if (unauthorized) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                } else if (jsonObject.getBoolean("success")) {
                    response.setStatus(HttpServletResponse.SC_CREATED);
                } else {
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                }
                IOUtils.copy(new StringReader(jsonObject.toString()), response.getOutputStream(),
                        StandardCharsets.UTF_8);
            }
        };
    }

    public Resolution delete() {
        final JSONObject jsonObject = (new JSONObject()).put("success", Boolean.FALSE);
        if (unauthorized) {
            jsonObject.put("error", "not authorized to delete");
        } else {
            try {
                final UserLayerHandler ulh = new UserLayerHandler(auditMessageObject, Stripersist.getEntityManager(),
                        application, appLayer, query, title, wellKnownUserLayerWorkspaceName,
                        wellKnownUserLayerStoreName);

                boolean ok = ulh.delete();
                jsonObject.put("success", ok);

                this.auditMessageObject.addMessage(
                        "UserLayer " + ulh.getLayerName() + " met id " + ulh.getAppLayerId() + (ok ? "" : " niet") +
                                " " +
                                "verwijderd.");
                ulh.dispose();

            } catch (Exception unforeseen) {
                LOG.error(unforeseen);
                jsonObject.put("error", "Fatale fout: " + unforeseen.getLocalizedMessage());
            }
        }
        return new StreamingResolution("application/json") {
            @Override
            public void stream(HttpServletResponse response) throws Exception {
                if (unauthorized) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                } else if (jsonObject.getBoolean("success")) {
                    response.setStatus(HttpServletResponse.SC_OK);
                } else {
                    jsonObject.put("error", "verzoek kon niet verwerkt worden");
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                }
                IOUtils.copy(new StringReader(jsonObject.toString()), response.getOutputStream(),
                        StandardCharsets.UTF_8);
            }
        };
    }

    public Resolution put() {
        final JSONObject jsonObject = (new JSONObject()).put("success", Boolean.FALSE);
        if (unauthorized) {
            jsonObject.put("error", "not authorized to change style");
        } else {
            try {
                final UserLayerHandler ulh = new UserLayerHandler(auditMessageObject, Stripersist.getEntityManager(),
                        application, appLayer, query, title, wellKnownUserLayerWorkspaceName,
                        wellKnownUserLayerStoreName);

                boolean ok = ulh.updateStyle(this.style);
                jsonObject.put("success", ok);

                this.auditMessageObject.addMessage(
                        "Stijl van userLayer " + ulh.getLayerName() + " met id " + appLayer.getId() +
                                " is " + (ok ? "" : "niet") + " aangepast.");
                ulh.dispose();
            } catch (Exception unforeseen) {
                LOG.error(unforeseen);
                jsonObject.put("error", "Fatale fout: " + unforeseen.getLocalizedMessage());
            }
        }
        return new StreamingResolution("application/json") {
            @Override
            public void stream(HttpServletResponse response) throws Exception {
                if (unauthorized) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                } else if (jsonObject.getBoolean("success")) {
                    response.setStatus(HttpServletResponse.SC_OK);
                } else {
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                }
                IOUtils.copy(new StringReader(jsonObject.toString()), response.getOutputStream(),
                        StandardCharsets.UTF_8);
            }
        };
    }
    // <editor-fold desc="Getters and Setters" defaultstate="collapsed">

    /**
     * Accessor to get the auditing information that has been provided by the
     * implementing class.
     *
     * @return the audit information
     * @see AuditMessageObject
     */
    @Override
    public AuditMessageObject getAuditMessageObject() {
        return this.auditMessageObject;
    }

    @Override
    public ActionBeanContext getContext() {
        return this.context;
    }

    @Override
    public void setContext(ActionBeanContext context) {
        this.context = context;
    }

    public ApplicationLayer getAppLayer() {
        return this.appLayer;
    }

    public void setAppLayer(ApplicationLayer appLayer) {
        this.appLayer = appLayer;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }
    // </editor-fold>
}
