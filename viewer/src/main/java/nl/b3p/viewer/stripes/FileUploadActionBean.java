package nl.b3p.viewer.stripes;

import net.sourceforge.stripes.action.*;
import net.sourceforge.stripes.validation.Validate;
import nl.b3p.viewer.config.app.Application;
import nl.b3p.viewer.config.app.ApplicationLayer;
import nl.b3p.viewer.config.app.FileUpload;
import nl.b3p.viewer.config.services.Layer;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.stripesstuff.stripersist.Stripersist;

import javax.persistence.EntityManager;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.Date;
import java.util.List;

@UrlBinding("/action/upload")
@StrictBinding
public class FileUploadActionBean implements ActionBean {
    private static final Log log = LogFactory.getLog(FileUploadActionBean.class);
    private ActionBeanContext context;
    public static final String DATA_DIR = "flamingo.data.dir";

    private List<FileBean> files;

    @Validate
    private ApplicationLayer appLayer;

    @Validate
    private Application application;

    @Validate
    private String type;

    @Validate
    private String fid;

    // <editor-fold default-state="collapsed" desc="Getters and setters">
    @Override
    public ActionBeanContext getContext() {
        return context;
    }

    public void setContext(ActionBeanContext context) {
        this.context = context;
    }

    public ApplicationLayer getAppLayer() {
        return appLayer;
    }

    public void setAppLayer(ApplicationLayer appLayer) {
        this.appLayer = appLayer;
    }

    public String getFid() {
        return fid;
    }

    public void setFid(String fid) {
        this.fid = fid;
    }

    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }

    public List<FileBean> getFiles() {
        return files;
    }

    public void setFiles(List<FileBean> files) {
        this.files = files;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
    // </editor-fold>

    @DefaultHandler
    public Resolution uploadFile(){
        JSONObject json = new JSONObject();
        String datadir =  context.getServletContext().getInitParameter(DATA_DIR);
        if(datadir.isEmpty()){
            json.put("success", false);
            json.put("message", "Upload directory niet geconfigureerd. Neem contact op met de systeembeheerder.");
        }else{
            File dir = new File(datadir);
            if(dir.exists() && dir.canWrite()){
                Long time = System.currentTimeMillis();
                EntityManager em = Stripersist.getEntityManager();
                Layer layer = appLayer.getService().getLayer(appLayer.getLayerName(), em);
                for (FileBean fb : files) {
                    String filename = fb.getFileName();
                    String extension = filename.substring(filename.lastIndexOf("."));
                    filename = filename.substring(0, filename.lastIndexOf("."));

                    File f =  new File(dir, "uploads" + File.separator + appLayer.getLayerName() + "_" + fid +"_" + filename + "_" +time + extension );
                    try {
                        FileUtils.copyToFile(fb.getInputStream(), f);
                        if(!em.getTransaction().isActive()){
                            em.getTransaction().begin();
                        }
                        FileUpload fu =new FileUpload();
                        fu.setCreatedAt(new Date());
                        fu.setFid(fid);
                        fu.setFile(f.getName());
                        fu.setSft(layer.getFeatureType());
                        em.persist(fu);
                        em.getTransaction().commit();
                    } catch (IOException e) {
                        log.error("Cannot write file", e);
                    }
                }
                json.put("success", true);
            }else{
                json.put("success", false);
                json.put("message", "Upload directory niet goed geconfigureerd: bestaat niet of kan niet schrijven. Neem contact op met de systeembeheerder.");
            }
        }
        return new StreamingResolution("application/json", new StringReader(json.toString(4)));

    }
}
