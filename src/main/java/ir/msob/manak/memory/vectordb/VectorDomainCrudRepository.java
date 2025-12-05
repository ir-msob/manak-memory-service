package ir.msob.manak.memory.vectordb;

import ir.msob.jima.core.commons.domain.DomainInfo;
import ir.msob.jima.core.commons.util.GenericTypeUtil;
import ir.msob.manak.core.model.jima.domain.Domain;


public abstract class VectorDomainCrudRepository<D extends Domain> extends VectorCrudRepository {

    protected VectorDomainCrudRepository(VectorStoreFactory vectorStoreFactory) {
        super(vectorStoreFactory);
    }

    private Class<D> getDomainClass() {
        @SuppressWarnings("unchecked")
        Class<D> domainClass = (Class<D>) GenericTypeUtil.resolveTypeArguments(getClass(), VectorDomainCrudRepository.class, 0);
        return domainClass;
    }

    protected String getCollectionName() {
        DomainInfo domainInfo = DomainInfo.info.getAnnotation(getDomainClass());
        return domainInfo.domainName();
    }

}
