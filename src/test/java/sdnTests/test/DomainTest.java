package sdnTests.test;

import static java.util.Arrays.asList;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.validation.constraints.AssertTrue;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.collection.IsIterableWithSize.iterableWithSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import org.hamcrest.collection.IsIterableWithSize;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.helpers.collection.ClosableIterable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.neo4j.annotation.RelatedTo;
import org.springframework.data.neo4j.conversion.EndResult;
import org.springframework.data.neo4j.support.Neo4jTemplate;
import org.springframework.data.neo4j.template.Neo4jOperations;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import sdnTests.domain.Content;
import sdnTests.domain.Person;
import sdnTests.domain.VideoContent;
import sdnTests.repository.ContentRepository;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"/test-context.xml"})
@Transactional
public class DomainTest {
	private final static Logger log = LoggerFactory.getLogger(DomainTest.class);
	
	@Autowired
	protected ContentRepository contentRepository;
		
	@Autowired
	Neo4jTemplate nt;
	
	public static int iterSize(Iterator iter){ int i=0; while (iter.hasNext()){ i++; iter.next();} return i;}	
	public static int iterSize(Iterable iter) {return iterSize(iter.iterator());}	
	
	Content c, content;
	VideoContent videoContent;	
	Person person1, person2; 
	
	@Before
	@Transactional 
	public void setUpSampleData() {
		// Content 
		content = new Content("Simple content", "A nice simple content");		
		content.setStringId("11"); 				
		content.setLongId(new Long(11));  		
		content.setPrimitiveId(11);  
		content.persist(); 
		
		// VideoContent 
		videoContent = new VideoContent("Video content", "A nice video content");
		videoContent.setStringId("22");		
		videoContent.setLongId(new Long(22));
		videoContent.setPrimitiveId(22);
		videoContent.persist();		

		// Person
		person1 = new Person("Person 1");
		person1.persist();		
		person2 = new Person("Person 2");
		person2.persist();
				
		// LIKES Relationships 
		// person1 LIKES two content objects
		person1.relateTo(content, "LIKES");
		person1.relateTo(videoContent, "LIKES");
		
		// a person also LIKES other persons
		person1.relateTo(person2, "LIKES");		
	}
	
		
	@Test
	public void relatedTo_retrieves_elementClasses_and_SubClasses_Only() {						
		log.warn("### @Test relatedTo_retrieves_elementClasses_and_SubClasses_Only");

		for (Content cc : person1.getLikedContent())
			log.warn("Class : " + cc.getClass().getName() + ",  __type__ : " + cc.getPersistentState().getProperty("__type__"));				
				
		/**
		 * getLikedContent() should contain only 2 objects, of class 'Content' or subclasses of it
		 * 
		 * But it fails, cause it returns all of 'LIKES' related nodes, even those not being a 'Content'
		 */
		assertEquals("", 2, person1.getLikedContent().size()); 		
	}
	
	@Test
	public void retrieved_objectNode_properties_are_read_correctly() {						
		log.warn("### @Test relatedTo_retrieved_objectNode_properties_are_read_correctly");
		
		for (Content cc : person1.getLikedContent()) {
			log.warn( " cc.getTitle() = " + cc.getTitle() + "\n cc.title = " + cc.title); 

			/* cc.tittle == null,  when read directly, although @Fetch was used.			 * 
			 * It works OK through getXXX(), with or without @Fetch 
			 */
			assertEquals("cc.title == cc.getTitle()", cc.getTitle(), cc.title ); 
		}		
	}
	
	@Test	
	public void endResult_Iterator_restarts() {						
		log.warn("### @endResult_Iterator_restarts");
		
		assertEquals("There are 2 content items", 2, contentRepository.count());		
		EndResult<Content> contents = contentRepository.findAllByQuery("title", "*conte*");		
		
		//assertThat(contents, IsIterableWithSize.<Content>iterableWithSize(2)); // hamcrest's 'IsIterableWithSize' gives the wrong message when it fails 
		assertEquals("contents contain 2 items", 2, iterSize(contents));

		/* 
		 * 'contents' should again contain 2 items, but there are 0. 
		 * Apparently its Iterator does not restart it self. 
		 * 
		 * I think this behavior is related to IndexHits extending both Iterable/Iterator 
		 * as discussed in https://github.com/neo4j/community/issues/141
		 * hence its not able to restart it self http://stackoverflow.com/questions/839178/why-is-javas-iterator-not-an-iterable
		*/
		assertEquals("contents contain 2 items", 2, iterSize(contents));	
	}
	
	@Test
	public void repository_retrieval_of_indexed_string() {
		c = contentRepository.getByStringId("11");
		
		assertEquals(c, contentRepository.findByPropertyValue("StringIds", "stringId", "11"));
		assertEquals( content, c);		
		assertEquals("Simple content", c.getTitle());
		assertEquals("11", c.getStringId());
		assertEquals(new Long(11), c.getLongId());		
		assertEquals(11, c.getPrimitiveId());
	}
	
	@Test
	public void repository_retrieval_of_indexed_primitive_int() {
		c = contentRepository.getByPrimitiveId(11);

//		if (c == null )
//			c = contentRepository.findByPropertyValue("primitiveId", 11); // works OK 

		assertEquals( content, c);
		assertEquals("Simple content", c.getTitle());
		assertEquals("11", c.getStringId());
		assertEquals(new Long(11), c.getLongId());		
		assertEquals(11, c.getPrimitiveId());
	}
	
	@Test
	public void repository_retrieval_of_indexed_Long() {		
		c = contentRepository.getByLongId(new Long(11)); // returns null
		if (c == null )
			c = contentRepository.getByLongId((long)11); // returns null		
		if (c == null ) 
			c = contentRepository.findByPropertyValue("longId", 11); // returns null 
		if (c == null ) 
			c = contentRepository.findByPropertyValue("LongIds", "longId", 11); // returns null 

		/* These DO work */
//		if (c == null )
//			c = contentRepository.findByPropertyValue("LongIds", "longId", new Long(11)); // works OK 
//		if (c == null ) 
//			c = contentRepository.findByPropertyValue("LongIds", "longId", (long)11); // works OK
//		if (c == null )
//			c = contentRepository.findByPropertyValue("longId", new Long(11)); // works OK 

		assertEquals( content, c);
		assertEquals("Simple content", c.getTitle());
		assertEquals("11", c.getStringId());
		assertEquals(new Long(11), c.getLongId());		
		assertEquals(11, c.getPrimitiveId());
	}
	
	@Test
	public void repository_subclass_retrieval_of_indexed_Long() {		
		c = contentRepository.getByLongId(new Long(22)); // returns null
		if (c == null )
			c = contentRepository.getByLongId((long)22); // returns null		
		if (c == null ) 
			c = contentRepository.findByPropertyValue("longId", 22); // returns null 
		if (c == null ) 
			c = contentRepository.findByPropertyValue("LongIds", "longId", 22); // returns null 

		/* These DO work */
//		if (c == null )
//			c = contentRepository.findByPropertyValue("LongIds", "longId", new Long(22)); // works OK 
//		if (c == null ) 
//			c = contentRepository.findByPropertyValue("LongIds", "longId", (long)22); // works OK
//		if (c == null )
//			c = contentRepository.findByPropertyValue("longId", new Long(22)); // works OK 

		assertEquals( videoContent, c);
		assertEquals("Video content", c.getTitle());
		assertEquals("22", c.getStringId());
		assertEquals(new Long(22), c.getLongId());		
		assertEquals(22, c.getPrimitiveId());
	}
	
	
	@Test
	public void index_query_String() {		
		Index<Node> ic = nt.getIndex("StringIds", Content.class);				
		IndexHits<Node> ihc = ic.query("stringId", "11");
		c = nt.projectTo(ihc.getSingle(), Content.class);

		assertEquals(content, c);		
	}
	
	
	@Test
	public void index_query_Long() {		
		Index<Node> ic = nt.getIndex("LongIds", Content.class);		
		IndexHits<Node> ihc = ic.query("longId", new Long(11));		
		if (ihc.size()<=0) 
			ihc = ic.query("longId", (long)11);
		if (ihc.size()<=0) 
			ihc = ic.query("longId", new Long(11));
				
		if (ihc.size()<=0)
			c=null;
		else
			c = nt.projectTo(ihc.getSingle(), Content.class);

		assertEquals(content, c);		
	}
	
	@Test
	public void repository_retrieval() {						
		log.warn("### @Test repositories_retrieval");
		EndResult<Content> contents;
		
		c = contentRepository.findByPropertyValue("title", "Video content");
		assertEquals( videoContent, c);
		assertEquals("Video content", c.getTitle());
		assertEquals(22, c.getPrimitiveId());		
				 
		contents = contentRepository.findAllByQuery("title", "*content*");				
		assertEquals("contents has 2 items", 2, iterSize(contents)); 
		
		contents = contentRepository.findAllByQuery("descr", "*nice*");				
		assertEquals("contents has 2 items", 2, iterSize(contents));

		contents = contentRepository.findAllByQuery("contentDescriptions", "descr", "*nice*");				
		assertEquals("contents has 2 items", 2, iterSize(contents));
		
		contents = contentRepository.findAllByQuery("title", "*");				
		assertEquals("contents has 2 items", 2, iterSize(contents));
		
	}	
}
