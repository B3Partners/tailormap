package nl.tailormap.viewer.admin.stripes;

import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.action.After;
import net.sourceforge.stripes.action.Before;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.DontValidate;
import net.sourceforge.stripes.action.FileBean;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.StreamingResolution;
import net.sourceforge.stripes.action.StrictBinding;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.controller.LifecycleStage;
import net.sourceforge.stripes.validation.SimpleError;
import net.sourceforge.stripes.validation.Validate;
import net.sourceforge.stripes.validation.ValidateNestedProperties;
import nl.tailormap.gbi.converter.Attribuut;
import nl.tailormap.gbi.converter.Converter;
import nl.tailormap.gbi.converter.Formulier;
import nl.tailormap.gbi.converter.Parser;
import nl.tailormap.gbi.converter.Paspoort;
import nl.tailormap.i18n.LocalizableActionBean;
import nl.tailormap.viewer.config.forms.Form;
import nl.tailormap.viewer.config.metadata.Metadata;
import nl.tailormap.viewer.config.security.Group;
import nl.tailormap.viewer.config.services.FeatureSource;
import nl.tailormap.viewer.config.services.JDBCFeatureSource;
import nl.tailormap.viewer.config.services.SimpleFeatureType;
import nl.tailormap.viewer.helpers.featuresources.FeatureSourceFactoryHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geotools.data.DataStore;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.opengis.feature.simple.SimpleFeature;
import org.stripesstuff.stripersist.Stripersist;

import javax.annotation.security.RolesAllowed;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

@UrlBinding("/action/form/{$event}")
@StrictBinding
@RolesAllowed({Group.ADMIN, Group.REGISTRY_ADMIN})
public class FormActionBean extends LocalizableActionBean {

    private static final Log log = LogFactory.getLog(FormActionBean.class);
    public ActionBeanContext context;
    private static final String JSP = "/WEB-INF/jsp/services/formAngular.jsp";
    private static final String EDITJSP = "/WEB-INF/jsp/services/editform.jsp";

    private Set<String> featureTypes = new HashSet<>();

    private List<Form> formList = new ArrayList<>();

    private JSONArray forms;

    private String defaultFeatureSourceId;

    private List<FeatureSource> featureSources;

    private List<Group> allGroups = new ArrayList<>();
    @Validate
    private List<String> groupsRead = new ArrayList<>();

    @Validate
    private FeatureSource defaultFeatureSource;

    @Validate
    private JSONArray filter;
    @Validate
    private int page;
    @Validate
    private int start;
    @Validate
    private int limit;
    @Validate
    private String sort;
    @Validate
    private String dir;

    @Validate(on = {"save", "edit","cancel", "delete"})
    @ValidateNestedProperties({
            @Validate(field="name", required=true, maxlength=255, label="Naam", on = {"save"}),
            @Validate(field="featureTypeName", maxlength=255, label="Feature Type Name", on = {"save"}),
            @Validate(field="json", required=true, label="JSON", on = {"save"})
    })
    private Form form;

    @Validate
    private FileBean file;

    @Override
    public ActionBeanContext getContext() {
        return context;
    }

    @Override
    public void setContext(ActionBeanContext context) {
        this.context = context;
    }

    @Before
    public void init(){
        EntityManager em = Stripersist.getEntityManager();
        formList = em.createQuery("FROM Form", Form.class).getResultList();
        formList.forEach(form -> {
            featureTypes.add(form.getFeatureTypeName());
        });
        forms = new JSONArray(formList);

        allGroups = em.createQuery("FROM Group ", Group.class).getResultList();
    }
    @After
    public void makeLists(){
        EntityManager em = Stripersist.getEntityManager();
        if (form != null && em.contains(form)) {
            groupsRead = new ArrayList(form.getReaders());
        }
    }

    @DefaultHandler
    @DontValidate
    public Resolution view() {
        return new ForwardResolution(JSP);
    }

    public Resolution cancel() {
        return new ForwardResolution(EDITJSP);
    }

    public Resolution add() {
        form = new Form();
        return new ForwardResolution(EDITJSP);
    }

    public Resolution save() {
        EntityManager em = Stripersist.getEntityManager();
        try {
            form = processForm(form);

            form.getReaders().clear();
            form.getReaders().addAll(groupsRead);
            em.persist(form);
            em.getTransaction().commit();
        } catch (IOException e) {
            log.error("Exception occured during processing of form json");
            context.getValidationErrors().add("json", new SimpleError(e.getLocalizedMessage()));
        }
        return new ForwardResolution(EDITJSP);
    }

    private Form processForm(Form f) throws IOException {
        String json = f.getJson();
        if(json.contains("GeoVisia")){
            List<Attribuut> attrs = getLookupDB();

            Converter c = new Converter(attrs);
            Parser p = new Parser();

            List<Paspoort> ps = p.parse(json);

            List<Formulier> fs = c.convert(ps);
            Formulier formulier = fs.get(0);
            f.setFeatureTypeName(formulier.getFeatureType());
            f.setJson(formulier.toString());
        }
        return f;
    }

    public Resolution delete() {
        EntityManager em = Stripersist.getEntityManager();
        em.remove(form);
        em.getTransaction().commit();
        return new ForwardResolution(EDITJSP);
    }

    public Resolution edit() {
        return new ForwardResolution(EDITJSP);
    }

    public Resolution getGridData() throws JSONException {
        JSONArray jsonData = new JSONArray();

        String filterName = "";
        String featureTypeName = "";
        /*
         * FILTERING: filter is delivered by frontend as JSON array [{property,
         * value}] for demo purposes the value is now returned, ofcourse here
         * should the DB query be built to filter the right records
         */
        if (this.getFilter() != null) {
            for (int k = 0; k < this.getFilter().length(); k++) {
                JSONObject j = this.getFilter().getJSONObject(k);
                String property = j.getString("property");
                String value = j.getString("value");
                if (property.equals("name")) {
                    filterName = value;
                }
                if (property.equals("featureTypeName")) {
                    featureTypeName = value;
                }
            }
        }

        Session sess = (Session) Stripersist.getEntityManager().getDelegate();
        Criteria c = sess.createCriteria(Form.class);

        /*
         * Sorting is delivered by the frontend as two variables: sort which
         * holds the column name and dir which holds the direction (ASC, DESC).
         */
        if (sort != null && dir != null) {
            Order order = null;
            if (dir.equals("ASC")) {
                order = Order.asc(sort);
            } else {
                order = Order.desc(sort);
            }
            order.ignoreCase();
            c.addOrder(order);

        }

        if (filterName != null && filterName.length() > 0) {
            Criterion nameCrit = Restrictions.ilike("name", filterName, MatchMode.ANYWHERE);
            c.add(nameCrit);
        }
        if (featureTypeName != null && featureTypeName.length() > 0) {
            Criterion urlCrit = Restrictions.ilike("featureTypeName", featureTypeName, MatchMode.ANYWHERE);
            c.add(urlCrit);
        }

        List sources = c.list();
        int rowCount = c.list().size();

        c.setMaxResults(limit);
        c.setFirstResult(start);


        for (Iterator it = sources.iterator(); it.hasNext();) {
            Form form = (Form) it.next();
            JSONObject j = this.getGridRow(form);
            jsonData.put(j);
        }

        final JSONObject grid = new JSONObject();
        grid.put("totalCount", rowCount);
        grid.put("gridrows", jsonData);

        return new StreamingResolution("application/json") {

            @Override
            public void stream(HttpServletResponse response) throws Exception {
                response.getWriter().print(grid.toString());
            }
        };
    }
    public Resolution saveDefaultFeatureSource() throws JSONException {
        JSONObject json = new JSONObject();

        json.put("success", Boolean.FALSE);
        try {
            EntityManager em = Stripersist.getEntityManager();
            Metadata md = null;
            try {
                md = em.createQuery("from Metadata where configKey = :key", Metadata.class).setParameter("key", Metadata.DEFAULT_FORM_FEATURESOURCE).getSingleResult();
            } catch (NoResultException e) {
                md = new Metadata();
                md.setConfigKey(Metadata.DEFAULT_FORM_FEATURESOURCE);
            }
            if (defaultFeatureSource != null) {
                md.setConfigValue(defaultFeatureSource.getId().toString());
            } else {
                md.setConfigValue(null);
            }
            defaultFeatureSourceId = md.getConfigValue();
            em.persist(md);
            em.getTransaction().commit();
            json.put("success", Boolean.TRUE);
        } catch (Exception ex) {
            log.error("Error during setting the default application: ", ex);
        }
        return new StreamingResolution("application/json", new StringReader(json.toString()));
    }

    @After(stages = {LifecycleStage.BindingAndValidation})
    public void createLists() {
        EntityManager em = Stripersist.getEntityManager();
        featureSources = em.createQuery("From FeatureSource", FeatureSource.class).getResultList();
        try {
            Metadata md = em.createQuery("from Metadata where configKey = :key", Metadata.class).setParameter("key", Metadata.DEFAULT_FORM_FEATURESOURCE).getSingleResult();
            defaultFeatureSourceId = md.getConfigValue();
        } catch (NoResultException e) {
        }
    }

    private  List<Attribuut> getLookupDB(){
        EntityManager em = Stripersist.getEntityManager();
        Metadata md = null;
        List<Attribuut> attrs = new ArrayList<>();
        try {
            md = em.createQuery("from Metadata where configKey = :key", Metadata.class).setParameter("key", Metadata.DEFAULT_FORM_FEATURESOURCE).getSingleResult();
            if(md.getConfigValue() == null){
                return attrs;
            }
            FeatureSource fs = em.find(FeatureSource.class, Long.parseLong(md.getConfigValue()));
            DataStore ds = null;
            SimpleFeatureSource sfs = null;
            if(fs != null && fs instanceof JDBCFeatureSource){
                try {
                    SimpleFeatureType sft=fs.getFeatureTypes().get(0);

                    ds = ((DataStore) FeatureSourceFactoryHelper.openGeoToolsFeatureSource(sft).getDataStore());
                    sfs = ds.getFeatureSource("attribuut");
                    SimpleFeatureCollection sfc = sfs.getFeatures();
                    for (SimpleFeatureIterator i = sfc.features(); i.hasNext(); ) {
                        Attribuut a = new Attribuut();
                        SimpleFeature sf = i.next();
                        a.setId((int) sf.getAttribute("id"));
                        a.setKolom_naam((String) sf.getAttribute("kolom_naam"));
                        a.setTabel_naam((String) sf.getAttribute("tabel_naam"));
                        a.setNaam((String) sf.getAttribute("naam"));
                        a.setObject_naam((String) sf.getAttribute("object_naam"));
                        Object mut = sf.getAttribute("muteerbaar");
                        a.setMuteerbaar(mut != null ? (Boolean) mut : null);
                        attrs.add(a);
                    }
                }catch (Exception e){
                    log.error("Cannot retrieve lookup featuresource: ", e);
                }finally{
                    if(ds != null){
                        ds.dispose();
                    }
                }
            }
        } catch (NoResultException e) {
            return attrs;
        }

        return attrs;
    }

    private JSONObject getGridRow(Form form) throws JSONException {
        JSONObject j = new JSONObject();
        j.put("id", form.getId());
        j.put("name", form.getName());
        j.put("featureTypeName", form.getFeatureTypeName());
        return j;
    }

    public Set<String> getFeatureTypes() {
        return featureTypes;
    }

    public void setFeatureTypes(Set<String> featureTypes) {
        this.featureTypes = featureTypes;
    }

    public List<Form> getFormList() {
        return formList;
    }

    public void setFormList(List<Form> formList) {
        this.formList = formList;
    }

    public JSONArray getForms() {
        return forms;
    }

    public void setForms(JSONArray forms) {
        this.forms = forms;
    }

    public JSONArray getFilter() {
        return filter;
    }

    public void setFilter(JSONArray filter) {
        this.filter = filter;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public String getSort() {
        return sort;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }

    public String getDir() {
        return dir;
    }

    public void setDir(String dir) {
        this.dir = dir;
    }

    public Form getForm() {
        return form;
    }

    public void setForm(Form form) {
        this.form = form;
    }

    public FileBean getFile() {
        return file;
    }

    public void setFile(FileBean file) {
        this.file = file;
    }

    public String getDefaultFeatureSourceId() {
        return defaultFeatureSourceId;
    }

    public void setDefaultFeatureSourceId(String defaultFeatureSourceId) {
        this.defaultFeatureSourceId = defaultFeatureSourceId;
    }

    public FeatureSource getDefaultFeatureSource() {
        return defaultFeatureSource;
    }

    public void setDefaultFeatureSource(FeatureSource defaultFeatureSource) {
        this.defaultFeatureSource = defaultFeatureSource;
    }

    public List<FeatureSource> getFeatureSources() {
        return featureSources;
    }

    public void setFeatureSources(List<FeatureSource> featureSources) {
        this.featureSources = featureSources;
    }

    public List<Group> getAllGroups() {
        return allGroups;
    }

    public void setAllGroups(List<Group> allGroups) {
        this.allGroups = allGroups;
    }

    public List<String> getGroupsRead() {
        return groupsRead;
    }

    public void setGroupsRead(List<String> groupsRead) {
        this.groupsRead = groupsRead;
    }
}
