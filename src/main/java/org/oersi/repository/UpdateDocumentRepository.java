package org.oersi.repository;

public interface UpdateDocumentRepository<T> {

  T createOrUpdate(T entity);

}
