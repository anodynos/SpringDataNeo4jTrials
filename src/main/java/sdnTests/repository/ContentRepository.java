package sdnTests.repository;

import java.util.Iterator;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.neo4j.conversion.EndResult;
import org.springframework.data.neo4j.repository.GraphRepository;
import org.springframework.data.neo4j.repository.NamedIndexRepository;
import org.springframework.data.neo4j.repository.RelationshipOperationsRepository;

import sdnTests.domain.Content;

public interface ContentRepository extends GraphRepository<Content>, NamedIndexRepository<Content>, RelationshipOperationsRepository<Content>{
	
	Content getByPrimitiveId(int primitiveId);	
	Content getByLongId(Long longId);	
	Content getByStringId(String stringId);
	
	Page<Content> findByTitleLike(String title, Pageable page);
	Content findByTitleLike(String title);
	
    Page<Content> findByDescrLike(String title, Pageable page);	
}
