package org.sakaiproject.component.app.messageforums;

import java.sql.SQLException;
import java.util.Date;

import net.sf.hibernate.Hibernate;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Query;
import net.sf.hibernate.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.app.messageforums.Area;
import org.sakaiproject.api.app.messageforums.AreaManager;
import org.sakaiproject.api.app.messageforums.MessageForumsTypeManager;
import org.sakaiproject.api.kernel.id.IdManager;
import org.sakaiproject.api.kernel.session.SessionManager;
import org.sakaiproject.api.kernel.tool.Placement;
import org.sakaiproject.api.kernel.tool.ToolManager;
import org.sakaiproject.component.app.messageforums.dao.hibernate.AreaImpl;
import org.springframework.orm.hibernate.HibernateCallback;
import org.springframework.orm.hibernate.support.HibernateDaoSupport;

public class AreaManagerImpl extends HibernateDaoSupport implements AreaManager {
    private static final Log LOG = LogFactory.getLog(AreaManagerImpl.class);

    private static final String CONTEXT_ID = "contextId";

    private static final String QUERY_AREA_BY_CONTEXT_ID = "findAreaByContextIdAndTypeId";

    private ToolManager toolManager;
    
    private IdManager idManager;

    private SessionManager sessionManager;

    private MessageForumsTypeManager typeManager;

    public void init() {
        ;
    }
    
    public MessageForumsTypeManager getTypeManager() {
        return typeManager;
    }

    public void setTypeManager(MessageForumsTypeManager typeManager) {
        this.typeManager = typeManager;
    }

    public void setSessionManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    public IdManager getIdManager() {
        return idManager;
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }

    public void setIdManager(IdManager idManager) {
        this.idManager = idManager;
    }

    public Area getPrivateArea() {
        return getAreaByContextIdAndTypeId(typeManager.getPrivateType());
    }
    
    public Area getDiscusionArea() {
        return getAreaByContextIdAndTypeId(typeManager.getDiscussionForumType());        
    }

    public boolean isPrivateAreaEnabled() {
        return getPrivateArea().getEnabled().booleanValue();
    }

    public Area createArea(String typeId) {
        Area area = new AreaImpl();
        area.setUuid(getNextUuid());
        area.setTypeUuid(typeId);
        area.setCreated(new Date());
        area.setCreatedBy(getCurrentUser());
        area.setContextId(getContextId());
        LOG.debug("createArea executed with areaId: " + area.getId());
        return area;
    }

    public void saveArea(Area area) {
        area.setModified(new Date());
        area.setModifiedBy(getCurrentUser());
        getHibernateTemplate().saveOrUpdate(area);
        LOG.debug("saveArea executed with areaId: " + area.getId());
    }

    public void deleteArea(Area area) {
        getHibernateTemplate().delete(area);
        LOG.debug("deleteArea executed with areaId: " + area.getId());
    }

    /**
     * ContextId is present site id for now.
     */
    private String getContextId() {
        if (TestUtil.isRunningTests()) {
            return "test-context";
        }
        Placement placement = toolManager.getCurrentPlacement();
        String presentSiteId = placement.getContext();
        return presentSiteId;
    }

    public Area getAreaByContextIdAndTypeId(final String typeId) {
        LOG.debug("getPrivateArea executing for current user: " + getCurrentUser());
        HibernateCallback hcb = new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                Query q = session.getNamedQuery(QUERY_AREA_BY_CONTEXT_ID);
                q.setParameter(CONTEXT_ID, getContextId(), Hibernate.STRING);
                q.setParameter("typeId", typeId, Hibernate.STRING);
                return q.uniqueResult();
            }
        };

        return (Area) getHibernateTemplate().execute(hcb);
    }

    private String getNextUuid() {
        return idManager.createUuid();
    }

    public ToolManager getToolManager() {
        return toolManager;
    }

    public void setToolManager(ToolManager toolManager) {
        this.toolManager = toolManager;
    }

    
    // helpers

    private String getCurrentUser() {
        if (TestUtil.isRunningTests()) {
            return "test-user";
        }
        return sessionManager.getCurrentSessionUserId();
    }

}
