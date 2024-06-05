package org.sidre.repository;

public interface UpdateDocumentRepository<T> {

  T createOrUpdate(T entity);

}
